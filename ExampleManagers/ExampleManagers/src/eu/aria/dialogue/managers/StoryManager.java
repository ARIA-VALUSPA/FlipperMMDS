/*
by Kevin Bowden
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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * By Kevin Bowden
 */
 public class StoryManager extends DefaultManager {
    private SentencesToKeywords sk;
    private ArrayList<Rules> rules;
    private RulesReader rulesReader;

    private String intentionPath = "$userstates.intention";
    private String userUtterancePath = "$userstates.utterance";
    private String userStoryPath = "$userstates.utterance.story";

    private String unknownState = "unknown";
    private String longPauseState = "longPause";

    private long timeout = 15000;

    public StoryManager(DefaultRecord is) {
        super(is);
        System.out.println("IntentGenerator is WIP. It should be using a Flipper template later. Right now it uses: ");
        System.out.println("$userstates.utterance(.consumed <boolean> |.timestamp = 't:<long ms since 1970>' |.text = <string> ) -> $userstates.intention = <string>");
        interval = 75; //fast default interval
    }

    @Override
    public void setParams(Map<String, String> params, Map<String, String[]> paramList){
        String rulesLocation = params.get("rules_path");
        if(rulesLocation == null){
            System.err.println("Parameter 'rules_path' not set for manager "+ getName() + "(" + getID() + "). Without it this manager will do nothing.");
            rules = new ArrayList<>();
        }else{
            rulesReader = new RulesReader(rulesLocation);
            rulesReader.readData();
            rules = rulesReader.getRules();
        }
        String ohTimeout = params.get("oh_timeout");
        try{
            this.timeout = Long.parseLong(ohTimeout);
        }catch(NumberFormatException ex){
            System.out.println("Parameter 'oh_timeout' is not an integer in manager " + getName() + "(" + getID() + "). Using default of 15000 ms" );
        }
        String path = params.get("user_utterance_is_path");
        if(path != null){
            userUtterancePath = path;
        }

        path = params.get("intention_is_path");
        if(path != null){
            intentionPath = path;
        }

        String stateName = params.get("long_pause_state_name");
        if(path != null){
            longPauseState = stateName;
        }

        stateName = params.get("long_pause_state_name");
        if(path != null){
            unknownState = stateName;
        }

    }

    long lastLongPause = 0;
    @Override
    public void process() {
        super.process();

        //if(is.getString(intentionPath) != null && !is.getString(intentionPath).equals("")){
        //    System.out.println(is.getString(intentionPath));
        //}

        Record utterance = getIS().getRecord(userUtterancePath);
        if (utterance == null) {
            utterance = new DefaultRecord();
            getIS().set(userUtterancePath, utterance);
        }

        Record storyUtterance = getIS().getRecord(userStoryPath);
        if (storyUtterance == null) {
            storyUtterance = new DefaultRecord();
            getIS().set(userStoryPath, storyUtterance);
        }

        if (!utterance.getString("consumed").equals("true")) {

            if (storyUtterance.getList("sentences") == null) {
                storyUtterance.set("sentences", new DefaultList());
            }
            if (storyUtterance.getList("finishedStating") == null) {
                storyUtterance.set("finishedStating", new DefaultList());
            }

            double currTime = (double) System.currentTimeMillis();
            String userSay = utterance.getString("text");
            if (userSay != null) {
                List sentences = storyUtterance.getList("sentences");
                List finishedStating = storyUtterance.getList("finishedStating");
                sentences.addItemEnd(userSay);
                finishedStating.addItemEnd(currTime);
                storyUtterance.set("sentences", sentences);
                storyUtterance.set("finishedStating", finishedStating);

            }
        }
    }


    private void processInternal(String userSay) {

        boolean keywordFound = false;

        // userSay = userSay.replaceAll("\\?", " ?");
        ArrayList<String> userSayAL = sk.removeStopWords(userSay.replaceAll("\\?", " ?"));
        try {
            userSayAL = sk.pickUp(userSayAL/*, bwDet*/);
        } catch (IOException ex) {
            Logger.getLogger(StoryManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (Rules rule : rules) {
            int count = 0;
            for (String word : rule.getWords()) {
                for (String userUtt : userSayAL) {
                    if (userUtt.toLowerCase().contains(word)) count++;
                }
            }
            if (count == rule.getWords().size()) {
                for (State state : rule.getStates()) {
                    is.set(state.getName(), state.getValue());
                }
                keywordFound = true;
                break;
            }
        }
        if (!keywordFound) {
            is.set(intentionPath, unknownState);
        }
    }
 }
