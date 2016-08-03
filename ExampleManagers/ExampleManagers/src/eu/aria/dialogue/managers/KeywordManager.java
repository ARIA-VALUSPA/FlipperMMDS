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


import java.util.Map;
import java.util.*;

public class KeywordManager extends DefaultManager {
    private String userUtterancePath = "$userstates.utterance";
    private String userIntentionPath = "$userstates.intention";
    private String userStoryPath = "$userstates.utterance.story";
    private String userKeywordPath = "$userstates.utterance.keywords";
    private ArrayList<String> countries = new ArrayList<String>();


    public KeywordManager(DefaultRecord is) {
        super(is);
        System.out.println("IntentGenerator is WIP. It should be using a Flipper template later. Right now it uses: ");
        System.out.println("$userstates.utterance(.consumed <boolean> |.timestamp = 't:<long ms since 1970>' |.text = <string> ) -> $userstates.intention = <string>");
        interval = 75; //fast default interval


    }

    @Override
    public void setParams(Map<String, String> params, Map<String, String[]> paramList) {
        String path = params.get("user_utterance_is_path");
        if (path != null) {
            userUtterancePath = path;
        }

    }

    public void getDefaultList(ArrayList countries) {

        String[] isoCountries = Locale.getISOCountries();
        for (String country : isoCountries) {
            Locale locale = new Locale("en", country);
            String name = locale.getDisplayCountry();

            if (!"".equals(name)) {
                countries.add(name);
                ;
            }
        }
    }

    @Override
    public void process() {
        super.process();
        getDefaultList(countries);

        Record utterance = getIS().getRecord(userUtterancePath);
        if (utterance == null) {
            utterance = new DefaultRecord();
            getIS().set(userUtterancePath, utterance);
        }

        Record intentionUtterance = getIS().getRecord(userIntentionPath);
        if (intentionUtterance == null) {
            intentionUtterance = new DefaultRecord();
            getIS().set(userIntentionPath, intentionUtterance);
        }

        if (!utterance.getString("consumed").equals("true")) {

            if (getIS().getList(userKeywordPath) == null) {
                getIS().set(userKeywordPath, new DefaultList());
            }
           // double currTime = (double) System.currentTimeMillis();
            String userSay = utterance.getString("text");
            System.out.println(userSay);
                if (userSay != null) {
                    List keyword = getIS().getList(userKeywordPath);
                    String[] splited = userSay.split("\\s+");
                    for (int i = 0; i < splited.length; i++) {
                        if (countries.contains(splited[i])){
                            System.out.println(splited[i]+" is found");
                            keyword.addItemEnd(splited[i]);
                            getIS().set(userKeywordPath, keyword);
                            getIS().set(userIntentionPath, "countryFound");
                        }
                    }
                }

        }
    }
}
