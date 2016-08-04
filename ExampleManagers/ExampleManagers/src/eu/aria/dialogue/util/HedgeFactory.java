package eu.aria.dialogue.util;

import hmi.flipper.informationstate.*;
import hmi.flipper.informationstate.List;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kevin Bowden on 8/4/2016.
 */

public class HedgeFactory {


public ArrayList<String> hedgesOne, hedgesTwo, topicChangeHedgesOne, topicChangeHedgesTwo, topicContHedgesOne, topicContHedgesTwo;
static HedgeFactory hf = new HedgeFactory();

    private HedgeFactory(){
        hedgesOne = new ArrayList<>();
        hedgesOne = new ArrayList<String>(
                Arrays.asList("I see. ", "Interesting. "));

        hedgesTwo = new ArrayList<>();

        topicChangeHedgesOne = new ArrayList<>();
        topicChangeHedgesOne = new ArrayList<String>(
                Arrays.asList("Okay, well, maybe let's move on to something else.",
                        "I think I would be interested in learning about something new.",
                        "Let's change the topic."));

        topicChangeHedgesTwo = new ArrayList<>();

        topicChangeHedgesOne = new ArrayList<>();
        topicChangeHedgesOne = new ArrayList<String>(
                Arrays.asList("So then, ", "I see, ", "Well then, ", "And "));

        topicChangeHedgesTwo = new ArrayList<>();
    }

    public static HedgeFactory getHF(){
        return hf;
    }

    public String hedgeBuilder(List prevIntentions, ArrayList<String> prevNouns, String previousAgentSent){
        String hedge = "";
        if(prevIntentions.size() > 1) {
            if (prevIntentions.getString(prevIntentions.size()-1).equals("probingQuestion")) {
                for (int i = 0; i < prevIntentions.size() - 1; i++) {
                    if (prevIntentions.getString(i).equals("probingQuestions")) {
                        hedge += hf.insertHedge();
                    }
                }
                hedge += hf.insertTopicChangeHedge();
            }
            if (prevNouns.size() > 0) {
                for (String noun : prevNouns) {
                    Pattern pat = Pattern.compile(noun);
                    Matcher matches = pat.matcher(previousAgentSent);

                    if (matches.find()) {
                        hedge = "";
                        hedge += hf.insertTopicContHedge();
                    }
                }
            }
            if (hedge.equals("")) {
                Random randomGenerator = new Random();
                int index = randomGenerator.nextInt(2);
                if (index == 1) {
                    hedge = hf.insertHedge();
                }
            }
        }
        return hedge;
    }

    public String insertTopicContHedge(){
        String hedge = randomValue(topicContHedgesOne);
        topicContHedgesTwo.add(hedge);
        topicContHedgesOne.remove(hedge);
        if(topicContHedgesOne.isEmpty()){
            topicContHedgesOne = topicContHedgesTwo;
            topicContHedgesTwo = new ArrayList();
        }
        return hedge;
    }

    public String insertTopicChangeHedge(){
        String hedge = randomValue(topicChangeHedgesOne);
        topicChangeHedgesTwo.add(hedge);
        topicChangeHedgesOne.remove(hedge);
        if(topicChangeHedgesOne.isEmpty()){
            topicChangeHedgesOne = topicChangeHedgesTwo;
            topicChangeHedgesTwo = new ArrayList();
        }
        return hedge;
    }


    public String insertHedge(){
        String hedge = randomValue(hedgesOne);
        hedgesTwo.add(hedge);
        hedgesOne.remove(hedge);
        if(hedgesOne.isEmpty()){
            hedgesOne = hedgesTwo;
            hedgesTwo = new ArrayList();
        }
    return hedge;
    }

    private String randomValue(ArrayList<String> values){
        Random randomGenerator = new Random();
        int index = randomGenerator.nextInt(values.size());
        return values.get(index);
    }

}
