package eu.aria.dialogue.util;

import hmi.flipper.informationstate.List;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kevin Bowden on 8/4/2016.
 */

public class HedgeFactory {

    private static HedgeFactory instance = new HedgeFactory();

    public static HedgeFactory getInstance() {
        return instance;
    }

    // public ArrayList<String> hedgesOne, hedgesTwo, topicChangeHedgesOne, topicChangeHedgesTwo, topicContHedgesOne, topicContHedgesTwo;
    private ContinuousList<String> hedges, topicChange, topicCont, repeat;

    private HedgeFactory() {
        hedges = new ContinuousList<>(Arrays.asList("I see. ", "Interesting. "));

        topicChange = new ContinuousList<>(
                Arrays.asList("Okay, well, maybe let's move on to something else. ",
                        "I think I would be interested in learning about something new. ",
                        "Let's change the topic. "));

        topicCont = new ContinuousList<>(Arrays.asList("So then, ", "I see, ", "Well then, ", "And "));


        repeat = new ContinuousList<>(Arrays.asList("Sure, ", "Okay, ", "Fine, ", "Sure, no problem, "));
    }


    public String hedgeBuilder(List prevIntentions, ArrayList<String> prevNouns, String previousAgentSent) {
        String hedge = "";
        if (prevIntentions.size() > 1) {
            if(prevIntentions.getString(prevIntentions.size() - 1).equals("repeatPrevious")){
                hedge = repeat.getValue();
            } else if (prevIntentions.getString(prevIntentions.size() - 1).equals("probingQuestion")) {
                for (int i = 0; i < prevIntentions.size() - 1; i++) {
                    if (prevIntentions.getString(i).equals("probingQuestions")) {
                        hedge += insertHedge();
                    }
                }
                hedge += insertTopicChangeHedge();
            }
            else if (prevNouns.size() > 0) {
                for (String noun : prevNouns) {
                    Pattern pat = Pattern.compile(noun);
                    Matcher matches = pat.matcher(previousAgentSent);

                    if (matches.find()) {
                        hedge = "";
                        hedge += insertTopicContHedge();
                    }
                }
            }
            else if (hedge.equals("")) {
                Random randomGenerator = new Random();
                int index = randomGenerator.nextInt(2);
                if (index == 1) {
                    hedge = insertHedge();
                }
            }
        }
        return hedge;
    }

    public String insertTopicContHedge() {
        return  topicCont.getValue();
    }

    public String insertTopicChangeHedge() {
        return topicChange.getValue();
    }

    public String insertRepeatHedge() {
        return repeat.getValue();
    }

    public String insertHedge() {
        return hedges.getValue();
    }

    private static <T> T randomValue(ArrayList<T> values) {
        Random randomGenerator = new Random();
        int index = randomGenerator.nextInt(values.size());
        return values.get(index);
    }

    public static class ContinuousList<T> {

        private ArrayList<T> one;
        private ArrayList<T> two;

        public ContinuousList(java.util.List<T> init) {
            one = new ArrayList<>(init);
            two = new ArrayList<>();
        }

        public T getValue() {
            T val = randomValue(one);
            two.add(val);
            one.remove(val);
            if (one.isEmpty()) {
                one = two;
                two = new ArrayList<>();
            }
            return val;
        }
    }
}
