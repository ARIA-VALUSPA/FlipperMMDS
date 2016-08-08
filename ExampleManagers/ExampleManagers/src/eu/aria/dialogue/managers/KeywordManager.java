/*
by Kubra
 */
package eu.aria.dialogue.managers;

import eu.aria.dialogue.util.*;
import eu.ariaagent.managers.DefaultManager;
import hmi.flipper.defaultInformationstate.DefaultList;
import hmi.flipper.defaultInformationstate.DefaultRecord;
import hmi.flipper.informationstate.List;
import hmi.flipper.informationstate.Record;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.*;

public class KeywordManager extends DefaultManager {
    private String userUtterancePath = "$userstates.utterance";
    private String userIntentionPath = "$userstates.intention";
    private String userStoryPath = "$userstates.utterance.story";
    private String userKeywordPath = "$userstates.utterance.keywords";
    private String shortAnswerPath = "$userstates.shortAnswer";
    private String longAnswerPath = "$userstates.longAnswer";
    private String countryFoundPath = "$userstates.countryFound";
    private String specificAnswerPath = "$userstates.specificAnswer";



    private ArrayList<String> countries = new ArrayList<String>();


    public KeywordManager(DefaultRecord is) {
        super(is);
        interval = 75; //fast default interval
        getDefaultCountryList(countries);
        getIS().set(specificAnswerPath, "false");



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
                countries.add(name.toLowerCase());
                ;
            }
        }
    }

    public void getCountryFound(String[] splited, List keyword){
        for (int i = 0; i < splited.length; i++) {
            if (countries.contains(splited[i].toLowerCase())){
                keyword.addItemEnd(splited[i]);
                getIS().set(userKeywordPath, keyword);
                getIS().set(countryFoundPath, "true");
            }
            else {
                getIS().set(countryFoundPath, "false");
            }
        }
        if (!getIS().getString(countryFoundPath).equals("true")){
            getLengthUserSays(splited,keyword);
        }
    }
    public void getLengthUserSays(String[] splited, List keyword){
       // if (getIS().getString(specificAnswerPath).equals("false")){
            if(splited.length < 4){
                getIS().set(userKeywordPath, keyword);
                getIS().set(shortAnswerPath, "true");
            }
            else{
                getIS().set(userKeywordPath, keyword);
                getIS().set(longAnswerPath, "true");
            }
        //}
    }

    @Override
    public void process() {
        super.process();

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
        if (utterance.getString("consumed") == null) {
            utterance.set("consumed", "true");
        }
        if (!utterance.getString("consumed").equals("true")) {

            if (getIS().getList(userKeywordPath) == null) {
                getIS().set(userKeywordPath, new DefaultList());
            }
            String userSay = utterance.getString("text");
            if (userSay != null) {
                List keyword = getIS().getList(userKeywordPath);
                String[] splited = userSay.split("\\s+");
                getCountryFound(splited,keyword);
            }
        }
    }
}
