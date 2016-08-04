/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.aria.dialogue.behaviours;

/**
 *
 * @author By Kevin Bowden
 */
import eu.aria.dialogue.KnowledgeDB.AgentHistory;
import eu.aria.dialogue.gui.GuiController;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.aria.dialogue.KnowledgeDB.KnowledgeBase;
import eu.aria.dialogue.util.HedgeFactory;
import eu.aria.dialogue.util.Say;
import eu.aria.dialogue.util.Wordnet;
import eu.ariaagent.managers.Manager;
import eu.ariaagent.util.ManageableBehaviourClass;
import hmi.flipper.defaultInformationstate.DefaultList;
import hmi.flipper.defaultInformationstate.DefaultRecord;
import hmi.flipper.informationstate.List;
import hmi.flipper.informationstate.Record;

public class BehaviourToGui implements ManageableBehaviourClass{

    private String userposPath = "$userstates.utterance.pos";
    private String userStoryPath = "$userstates.utterance.story";

    List nouns, lastStated, adjectives, keywords, prevAgentIntentions;

    GuiController gui;

    int numArgValues;

    Record posUtterance, storyUtterance;
    Manager manager;

    private String agentName = "Agent";
    private double currTime;
    private HashMap localReplacements;

    HedgeFactory hf =HedgeFactory.getHF();

    Wordnet wn = new Wordnet();
    KnowledgeBase kb = KnowledgeBase.getKB();
    AgentHistory ah = AgentHistory.getAH();

    public ArrayList<String> getAllCurrentNouns(){

        ArrayList<String> values = new ArrayList<>();
            Iterator it = localReplacements.entrySet().iterator();
            while (it.hasNext()) {

                Map.Entry pair = (Map.Entry)it.next();
                if(!pair.getKey().toString().startsWith("@adj")){
                    values.add(pair.getValue().toString());
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
            return values;
    }

    public String queryBuilder(String value){
        String pattern = "@\\??[a-zA-Z0-9]+";
        localReplacements = new HashMap();
        Pattern pat = Pattern.compile(pattern);
        Matcher matches = pat.matcher(value);
        ArrayList<String> toReplace = new ArrayList<>();

        adjectives = posUtterance.getList("adjectives");
            while(matches.find()){
            String match = matches.group();
                if(match.startsWith("@?")){
                    toReplace.add(match.substring(2,match.length()));
                } else {
                    toReplace.add(match.substring(1,match.length()));
                }
            }
            int iters = 0;
            ArrayList<String> iterStrList = toReplace;
            while (iters < 2) {
                for (String r : toReplace) {
                    if (r.startsWith("noun")) {
                        ArrayList<Integer> ri = sortNounIndicies(false);
                        String replacementNoun = "";
                        int k = 0;
                        int numAdj = numVars(value, "@adj");
                        while (k < 10 && k < ri.size()) {
                            if (numAdj <= adjectives.getItem(ri.get(k)).getInteger()) {
                                if (!localReplacements.containsValue(replacementNoun)) {
                                    String currNoun = nouns.getItem(ri.get(k)).getString();
                                    if (replacementNoun.startsWith("nounposs") && !kb.isPossession(currNoun)) {
                                        continue;
                                    }
                                    replacementNoun = currNoun;
                                    break;
                                }
                            }
                            k++;
                        }
                        if (replacementNoun.equals("")) {
                            return null;
                        }
                        Pattern patn = Pattern.compile("@\\??" + r);
                        Matcher matn = patn.matcher(value);
                        value = matn.replaceAll(replacementNoun);
                        localReplacements.put(r, replacementNoun);
                    } else if (r.startsWith("adj") && iters > 0) {
                        String poss = "";
                        if (r.startsWith("adjposs")) {
                            poss = "poss";
                        }
                        String[] reps = r.split("adj" + poss);
                        String repID = reps[reps.length - 1];

                        String adjNoun = localReplacements.get("noun" + poss + repID.substring(0, 1)).toString();
                        String replacementAdj = "";
                        if (kb.numAdj(adjNoun) > 0) {
                            replacementAdj = kb.getAdj(adjNoun, localReplacements.keySet());
                        }
                        Pattern pata = Pattern.compile("@\\??" + r);
                        Matcher mata = pata.matcher(value);
                        value = mata.replaceAll(replacementAdj);
                        localReplacements.put(r, replacementAdj);
                    } else if (r.startsWith("keyword")) {
                        for (int i = 0; i < keywords.size(); i++) {
                            String replacementNoun = keywords.getString(i);
                            Pattern patn = Pattern.compile("@" + r);
                            Matcher matn = patn.matcher(value);
                            value = matn.replaceAll(replacementNoun);
                        }
                    }
                }
                    iters++;
            }
                return value;
    }

    public double scoreNoun(int frequency, double lastStated, double preference){
        double timeDiff = currTime - lastStated;
        double sum  = frequency  - (timeDiff/1000);
        sum += sum * preference;
        return sum;
    }

    public int largestIndex(ArrayList<Double> list) {
        int maxIndex = 0;
        for (int i = 0; i < list.size(); i++) {
            double newnumber = list.get(i);
            if ((newnumber > list.get(maxIndex))) {
                maxIndex = i;
            }
        }
    return maxIndex;
    }

    public int findIndex(String key, List list) {
        int nid = -1;
        for (int j = 0; j < list.size(); j++) {
            if (key.equals(list.getString(j))) {
                nid = j;
                break;
            }
        }
        return nid;
    }

    //TODO finish this thought
    public void findParentSentence(double timestamp){
        List finishedStating = storyUtterance.getList("finishedStating");
        int sid = -1;
        double currDiff, diff = 0.0;
        for(int i = 0; i < finishedStating.size() ;i++){
            double time = finishedStating.getDouble(i);

            currDiff = Math.abs(time - timestamp);
            if(currDiff < diff || diff == 0.0){
                    diff = currDiff;
                    sid = i;
            }
        }

    }

    //TODO finish this thought
    public void getRelatedNoun(String noun){
        int nid = findIndex(noun, nouns);
        lastStated.getItem(nid).getDouble();
    }

    public ArrayList<Integer> sortNounIndicies(boolean onlyLargest) {
        List frequency = posUtterance.getList("frequency");
        List preference = posUtterance.getList("preference");

        ArrayList<Double> nounScores = new ArrayList<>();
        for (int j = 0; j < frequency.size(); j++) {
            nounScores.add(scoreNoun(frequency.getInteger(j), lastStated.getDouble(j), preference.getDouble(j)));
        }
        ArrayList<Integer> sortedIndex = new ArrayList<>();

        if (onlyLargest) {
            sortedIndex.add(largestIndex(nounScores));
            return sortedIndex;
        }

        while (!nounScores.isEmpty()) {
            int li = largestIndex(nounScores);
            sortedIndex.add(li);
            nounScores.remove(li);
        }
        return sortedIndex;
    }

    public ArrayList<String> posQualifier(ArrayList<String> values, int stopNum, String pattern){
        ArrayList<String> nvals = new ArrayList<>();
        for(String value :  values) {
            Pattern pat = Pattern.compile(pattern);
            Matcher matches = pat.matcher(value);
            int count = 0;
            while(matches.find()){
                count++;
            }
            if(count <= stopNum) {
                nvals.add(value);
            }
        }
        return nvals;
    }

    public int numVars(String value, String pattern){
        Pattern pat = Pattern.compile(pattern);
        Matcher matches = pat.matcher(value);
        int m = 0;
        while(matches.find()) {
        m++;
        }
        return m;
    }

    public void agentOutput(String value){
        value = queryBuilder(value);
        if(value != null) {
            String hedgeValue = "";
            ArrayList<String> currNouns = getAllCurrentNouns();
            hedgeValue = hf.hedgeBuilder(prevAgentIntentions, currNouns, manager.getIS().getString("$userstates.utterance.text"));
            Say newSay = new Say(hedgeValue + value, agentName, true);
            gui.addAgentSay(newSay, true);
            ah.storeRule(value);
        }
    }

    @Override
    public void execute(ArrayList<String> argNames, ArrayList<String> argValues) {
        
        //        "Agent":{ "id":2, "timestamp":1469625417747, "text":"Hi, are you still there?" }

        if(this.gui == null)
        {
            this.gui = GuiController.getInstance(manager.getIS());
        }

//        Wordnet wn = new Wordnet();
//        wn.findSynonym();

        manager.getIS().set("$userstates.intention", "");



        currTime = (double) System.currentTimeMillis();
        posUtterance = manager.getIS().getRecord(userposPath);
        if (posUtterance == null) {
            posUtterance = new DefaultRecord();
            manager.getIS().set(userposPath, posUtterance);
        }
        storyUtterance = manager.getIS().getRecord(userStoryPath);
        if (storyUtterance == null) {
            storyUtterance = new DefaultRecord();
            manager.getIS().set(userStoryPath, storyUtterance);
        }

        prevAgentIntentions = manager.getIS().getList("$agentstates.prevIntentions");
        if(prevAgentIntentions == null){
            prevAgentIntentions = new DefaultList();
        }

        numArgValues = argValues.size();
        nouns = posUtterance.getList("nouns");
        adjectives = posUtterance.getList("adjectives");
        lastStated = posUtterance.getList("lastStated");
        keywords = manager.getIS().getList("$userstates.utterance.keywords");

        int possSize = 0;
        try {
            possSize = posUtterance.getInteger("possSize");
        } catch(NullPointerException e) { }

        if(keywords == null){
            manager.getIS().set("$userstates.utterance.keywords", new DefaultList());
            keywords = manager.getIS().getList("$userstates.utterance.keywords");
        }

        argValues = ah.sortValues(argValues, 10, true);

        if(nouns != null && adjectives != null && lastStated != null) {
            argValues = posQualifier(argValues, nouns.size(), "@noun");
            argValues = posQualifier(argValues, possSize, "@nounposs");
            argValues = posQualifier(argValues, keywords.size(), "@keyword");
            if (argValues.size() > 0) {
                String value = randomValue(argValues);
                agentOutput(value);
            }
        }
    }

    public String randomValue(ArrayList<String> values){
        Random randomGenerator = new Random();
        int index = randomGenerator.nextInt(values.size());
        return values.get(index);
    }

    @Override
    public void prepare(ArrayList<String> argNames, ArrayList<String> argValues) {
        
    }
    
    @Override
    public Manager getManager(){
        return this.manager;
    }
    
    @Override
    public void setManager(Manager manager){
        this.manager = manager;
        
    }
    
}
