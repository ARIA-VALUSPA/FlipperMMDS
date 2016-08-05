/*
by Kubra
 */
package eu.aria.dialogue.managers;

import eu.ariaagent.managers.DefaultManager;
import hmi.flipper.defaultInformationstate.DefaultList;
import hmi.flipper.defaultInformationstate.DefaultRecord;
import hmi.flipper.informationstate.List;
import hmi.flipper.informationstate.Record;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class KeywordManager extends DefaultManager {
    private String userUtterancePath = "$userstates.utterance";
    private String userIntentionPath = "$userstates.intention";
    private String userStoryPath = "$userstates.utterance.story";
    private String userKeywordPath = "$userstates.utterance.keywords";
    private ArrayList<String> countries = new ArrayList<String>();


    public KeywordManager(DefaultRecord is) {
        super(is);
        interval = 75; //fast default interval
    }

    @Override
    public void setParams(Map<String, String> params, Map<String, String[]> paramList) {
        String path = params.get("user_utterance_is_path");
        if (path != null) {
            userUtterancePath = path;
        }

    }

    public void getDefaultCountryList(ArrayList countries) {

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
        getDefaultCountryList(countries);

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
            String userSay = utterance.getString("text");
            if (userSay != null) {
                List keyword = getIS().getList(userKeywordPath);
                String[] splited = userSay.split("\\s+");
                for (int i = 0; i < splited.length; i++) {
                    if (countries.contains(splited[i])) {
                        keyword.addItemEnd(splited[i]);
                        getIS().set(userKeywordPath, keyword);
                        getIS().set(userIntentionPath, "countryFound");
                    }
                }
            }
        }
    }
}
