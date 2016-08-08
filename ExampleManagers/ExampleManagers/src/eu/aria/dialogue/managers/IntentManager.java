/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.aria.dialogue.managers;

import eu.aria.dialogue.util.*;
import eu.ariaagent.managers.DefaultManager;
import hmi.flipper.defaultInformationstate.DefaultList;
import hmi.flipper.defaultInformationstate.DefaultRecord;
import hmi.flipper.informationstate.List;
import hmi.flipper.informationstate.Record;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Siewart
 */
 public class IntentManager extends DefaultManager {
    private SentencesToKeywords sk;
    private ArrayList<Rules> rules;
    private RulesReader rulesReader;

    private String intentionPath = "$userstates.intention";
    private String userUtterancePath = "$userstates.utterance";

    private String unknownState = "unknown";
    private String longPauseState = "longPause";

    private long timeout = 100;

    public IntentManager(DefaultRecord is) {
        super(is);
        System.out.println("IntentGenerator is WIP. It should be using a Flipper template later. Right now it uses: ");
        System.out.println("$userstates.utterance(.consumed <boolean> |.timestamp = 't:<long ms since 1970>' |.text = <string> ) -> $userstates.intention = <string>");
        interval = 50; //fast default interval
    }

    @Override
    public void setParams(Map<String, String> params, Map<String, String[]> paramList) {
        String rulesLocation = params.get("rules_path");
        if (rulesLocation == null) {
            System.err.println("Parameter 'rules_path' not set for manager " + getName() + "(" + getID() + "). Without it this manager will do nothing.");
            rules = new ArrayList<>();
        } else {
            rulesReader = new RulesReader(rulesLocation);
            rulesReader.readData();
            rules = rulesReader.getRules();
        }
        String ohTimeout = params.get("oh_timeout");
        try {
            this.timeout = Long.parseLong(ohTimeout);
        } catch (NumberFormatException ex) {
            System.out.println("Parameter 'oh_timeout' is not an integer in manager " + getName() + "(" + getID() + "). Using default of 15000 ms");
        }
        try {
            String stopwords = params.get("stopwords_path");
            String synonyms = params.get("synonyms_path");
            String posModel = params.get("pos_model_path");
            if (stopwords == null || synonyms == null || posModel == null) {
                System.err.println("Parameter 'stopwords_path', 'synonyms_path' and 'pos_model_path' must all be set as filepaths in manager " + getName() + "(" + getID() + ").");
            }
            sk = new SentencesToKeywords(stopwords, synonyms, posModel);
        } catch (IOException ex) {
            System.out.println("Error in manager " + getName() + "(" + getID() + "). Could not create SentencesToKeywords: " + ex.getMessage());
        }
        String path = params.get("user_utterance_is_path");
        if (path != null) {
            userUtterancePath = path;
        }

        path = params.get("intention_is_path");
        if (path != null) {
            intentionPath = path;
        }

        String stateName = params.get("long_pause_state_name");
        if (path != null) {
            longPauseState = stateName;
        }

        stateName = params.get("long_pause_state_name");
        if (path != null) {
            unknownState = stateName;
        }
    }

    long lastLongPause = 0;

    @Override
    public void process() {
        super.process();

        Record utterance = getIS().getRecord(userUtterancePath);
        if (utterance == null) {
            utterance = new DefaultRecord();
            getIS().set(userUtterancePath, utterance);
        }

        Record agentUtterance = getIS().getRecord("$agentstates");
        if (agentUtterance == null) {
            agentUtterance = new DefaultRecord();
            getIS().set("$agentstates", agentUtterance);
        }

        List prevAgentIntentions = agentUtterance.getList("prevIntentions");
        if (prevAgentIntentions == null) {
            prevAgentIntentions = new DefaultList();
            agentUtterance.set("prevIntentions", prevAgentIntentions);
        }


        if (utterance.getString("timestamp") == null) {
            utterance.set("timestamp", "t:" + System.currentTimeMillis());
        }
        if (utterance.getString("text") == null) {
            utterance.set("text", "");
        }

        if (utterance.getString("consumed") == null) {
            utterance.set("consumed", "true");
        }

        if (utterance.getString("consumed").equals("true")) {
            try {
                Long time = Long.parseLong(utterance.getString("timestamp").substring(2));
                if (System.currentTimeMillis() - (time) > timeout && System.currentTimeMillis() - lastLongPause > timeout) {
                    is.set(intentionPath, longPauseState);
                    lastLongPause = System.currentTimeMillis();
                    timeout=10000;
                }
            } catch (NumberFormatException e) {
            }

        } else if (is.getString(intentionPath) == null || is.getString(intentionPath).equals("")) { // if there is no current intention, see if we can create a new one
            String userSay = utterance.getString("text");
            processInternal(userSay);

            utterance.set("consumed", "true");
        }
    }

    private void processInternal(String userSay) {
        ArrayList<String> keywordMatches = new ArrayList<>();
        boolean keywordFound = false;
        HashMap<Rules, Integer> keywordMatchHash = new HashMap<>();

        userSay = userSay.replaceAll("\\?", " ?");
        userSay = userSay.replaceAll("\\!", " !");
        userSay = userSay.replaceAll("\\.", " .");
        userSay = userSay.replaceAll("\\,", " ,");
        ArrayList<String> userSayAL = sk.removeStopWords(userSay);

        ArrayList<String> negationWords = new ArrayList<>(Arrays.asList("no", "not", "don't"));
        for(String nw : negationWords){
            if(userSayAL.contains(nw)){
                is.set("$userstates.dialoguestates", "negation");
            }
        }

        if (userSayAL.contains("?")) {
            is.set("$userstates.dialoguestates", "askQuestion");
        }

        for (Rules rule : rules) {
            int count = 0;

            for (String word : rule.getWords()) {
                for (String userUtt : userSayAL) {
                    if (userUtt.toLowerCase().contains(word)) {
                        count++;
                    }
                }
            }
            if (count == rule.getWords().size()) {
                keywordMatchHash.put(rule, count);
                keywordFound = true;
//                break;
            }
        }


        if (!keywordFound) {
//            is.set(intentionPath, unknownState);
        } else {
            int maxSize = 0;
            Rules maxRule = new Rules();
            for (Map.Entry<Rules, Integer> entry : keywordMatchHash.entrySet()) {
                if (maxSize < entry.getValue()) {
                    maxSize = entry.getValue();
                    maxRule = entry.getKey();
                }
                for (State state : maxRule.getStates()) {
                    is.set(state.getName(), state.getValue());
                }
            }
        }
    }
}
