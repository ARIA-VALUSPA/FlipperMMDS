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
import eu.aria.dialogue.KnowledgeDB.KnowledgeBase;
import eu.aria.dialogue.gui.GuiController;
import eu.aria.dialogue.util.HedgeFactory;
import eu.aria.dialogue.util.Say;
import eu.aria.dialogue.util.Wordnet;
import eu.ariaagent.managers.Manager;
import eu.ariaagent.util.FilePointer;
import eu.ariaagent.util.FileStorage;
import eu.ariaagent.util.ManageableBehaviourClass;
import eu.ariaagent.util.SimpleProducerWrapper;
import hmi.flipper.defaultInformationstate.DefaultList;
import hmi.flipper.defaultInformationstate.DefaultRecord;
import hmi.flipper.informationstate.List;
import hmi.flipper.informationstate.Record;
import vib.core.util.IniManager;

import javax.jms.TextMessage;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BehaviourToGui implements ManageableBehaviourClass {

    private String userposPath = "$userstates.utterance.pos";
    private String userStoryPath = "$userstates.utterance.story";

    private List nouns, lastStated, adjectives, keywords, prevAgentIntentions, as;

    private GuiController gui;

    private int numArgValues;

    private Record posUtterance, storyUtterance, au;
    private Manager manager;

    private String agentName = "Agent";
    private double currTime;
    private HashMap<String, String> localReplacements;

    private HedgeFactory hf = HedgeFactory.getInstance();

    private Wordnet wn = new Wordnet();
    private KnowledgeBase kb = KnowledgeBase.getInstance();
    private AgentHistory ah = AgentHistory.getAH();

    private SimpleProducerWrapper producer;

    public BehaviourToGui() {
        if (new File("config.ini").exists()) {
            IniManager iniManager = new IniManager("config.ini");
            String url = iniManager.getValueString("amq.sender.fml.url");
            String name = iniManager.getValueString("amq.sender.fml.name");
            boolean isTopic = iniManager.getValueBoolean("amq.sender.fml.isTopic");
            producer = new SimpleProducerWrapper(url, name, isTopic);
            producer.init();
        } else {
            System.err.println("No configuration file exists!");
        }
    }

    public ArrayList<String> getAllCurrentNouns() {

        ArrayList<String> values = new ArrayList<>();
        Iterator<Map.Entry<String, String>> it = localReplacements.entrySet().iterator();
        while (it.hasNext()) {

            Map.Entry<String, String> pair = it.next();
            if (!pair.getKey().startsWith("@adj")) {
                values.add(pair.getValue());
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
        return values;
    }

    public boolean queryBuilder(FilePointer value, boolean isRepeat) {
        if(isRepeat){
//            FilePointer.CreateFromSpeech("").getFilePath();
        } else {
            value.prepareCurrent();
        }


        String pattern = "@\\??[a-zA-Z0-9]+";
        localReplacements = new HashMap<>();
        Pattern pat = Pattern.compile(pattern);
        Matcher matches = pat.matcher(value.getCurSpeechContent());
        ArrayList<String> toReplace = new ArrayList<>();

        adjectives = posUtterance.getList("adjectives");
        while (matches.find()) {
            String match = matches.group();
            if (match.startsWith("@?")) {
                toReplace.add(match.substring(2, match.length()));
            } else {
                toReplace.add(match.substring(1, match.length()));
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
                    int numAdj = numVars(value.getCurSpeechContent(), "@adj");
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
                        return false;
                    }
                    Pattern patn = Pattern.compile("@\\??" + r);
                    // Matcher matn = patn.matcher(value);
                    // value = matn.replaceAll(replacementNoun);
                    value.replaceAll(patn, replacementNoun);
                    localReplacements.put(r, replacementNoun);
                } else if (r.startsWith("adj") && iters > 0) {
                    String poss = "";
                    if (r.startsWith("adjposs")) {
                        poss = "poss";
                    }
                    String[] reps = r.split("adj" + poss);
                    String repID = reps[reps.length - 1];

                    String adjNoun = localReplacements.get("noun" + poss + repID.substring(0, 1));
                    String replacementAdj = "";
                    if (kb.numAdj(adjNoun) > 0) {
                        replacementAdj = kb.getAdj(adjNoun, localReplacements.keySet());
                    }
                    Pattern pata = Pattern.compile("@\\??" + r);
                    // Matcher mata = pata.matcher(value);
                    // value = mata.replaceAll(replacementAdj);
                    value.replaceAll(pata, replacementAdj);
                    localReplacements.put(r, replacementAdj);
                } else if (r.startsWith("keyword")) {
                    for (int i = 0; i < keywords.size(); i++) {
                        String replacementNoun = keywords.getString(i);
                        Pattern patn = Pattern.compile("@" + r);
                        //Matcher matn = patn.matcher(value);
                        // value = matn.replaceAll(replacementNoun);
                        value.replaceAll(patn, replacementNoun);
                    }
                } else if (r.startsWith("name")) {
                    String replacementNoun = manager.getIS().getString("$userstates.name");
                    if (replacementNoun == null || replacementNoun.equals("")) {
                        return false;
                    }
                    Pattern patn = Pattern.compile("@" + r);
                    value.replaceAll(patn, replacementNoun);
                }
            }
            iters++;
        }
        return true;
    }

    public double scoreNoun(int frequency, double lastStated, double preference) {
        double timeDiff = currTime - lastStated;
        double sum = frequency - (timeDiff / 1000);
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
    public void findParentSentence(double timestamp) {
        List finishedStating = storyUtterance.getList("finishedStating");
        int sid = -1;
        double currDiff, diff = 0.0;
        for (int i = 0; i < finishedStating.size(); i++) {
            double time = finishedStating.getDouble(i);

            currDiff = Math.abs(time - timestamp);
            if (currDiff < diff || diff == 0.0) {
                diff = currDiff;
                sid = i;
            }
        }

    }

    //TODO finish this thought
    public void getRelatedNoun(String noun) {
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

    public ArrayList<FilePointer> posQualifier(ArrayList<FilePointer> values, int stopNum, String pattern) {
        ArrayList<FilePointer> nvals = new ArrayList<>();
        for (FilePointer value : values) {
            Pattern pat = Pattern.compile(pattern);
            Matcher matches = pat.matcher(value.getCurSpeechContent());
            int count = 0;
            while (matches.find()) {
                count++;
            }
            if (count <= stopNum) {
                nvals.add(value);
            }
        }
        return nvals;
    }

    public int numVars(String value, String pattern) {
        Pattern pat = Pattern.compile(pattern);
        Matcher matches = pat.matcher(value);
        int m = 0;
        while (matches.find()) {
            m++;
        }
        return m;
    }

    public void agentOutput(FilePointer value, boolean isRepeat) {
        boolean resultOK = queryBuilder(value, isRepeat);
        if (resultOK) {
            String hedgeValue;

            ArrayList<String> currNouns = getAllCurrentNouns();
            hedgeValue = hf.hedgeBuilder(prevAgentIntentions, currNouns, manager.getIS().getString("$userstates.utterance.text"));
            value.setHedge(hedgeValue);
            Say newSay = new Say(value.getCurSpeechContent(), agentName, true);
            gui.addAgentSay(newSay, true);
            as.addItemEnd(value.getCurSpeechContent());
            au.set("sentences", as);
            manager.getIS().set("$agentstates.utterance.lastPath", value.getFilePath());
            ah.storeRule(value.getCurSpeechContent());

            if (producer != null) {
                if (producer.getStatus() == SimpleProducerWrapper.Status.Ready) {
                    TextMessage textMessage = producer.createTextMessage(value.getCurXmlContent());
                    if (textMessage != null) {
                        producer.sendMessage(textMessage);
                    }
                } else {
                    System.err.println("Something went wrong, could not send the FML to Greta! Please check your ActiveMQ connection!");
                }
            }
        }
    }

    @Override
    public void execute(ArrayList<String> argNames, ArrayList<String> argValues) {

        //        "Agent":{ "id":2, "timestamp":1469625417747, "text":"Hi, are you still there?" }

        ArrayList<FilePointer> values = new ArrayList<>();
        boolean isRepeat = false;
        if(argValues.get(0).equals("@repeatMostRecent")){
            isRepeat = true;
            String repeatPath = manager.getIS().getString("$agentstates.utterance.lastPath");
            if(repeatPath == null) {
                System.err.println("there is no lastPath");
                return;
            }
            values.add(FileStorage.getInstance().getFile(repeatPath));
        } else {
            for (String val : argValues) {
                FilePointer fp = FileStorage.getInstance().getFileFromValue(val);
                values.add(fp);
            }
        }

        if (this.gui == null) {
            this.gui = GuiController.getInstance(manager.getIS());
        }

        manager.getIS().set("$userstates.intention", "");
        manager.getIS().set("$userstates.dialoguestates", "");
        manager.getIS().set("$dialoguestates.topic", "");
        manager.getIS().set("$userstates.utterance.tdawg.exclaim", "false");



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


        Record agentUtterance = manager.getIS().getRecord("$agentstates");
        if (agentUtterance == null) {
            agentUtterance = new DefaultRecord();
            manager.getIS().set("$agentstates", agentUtterance);
        }

        au = manager.getIS().getRecord("$agentstates.utterance");
        if (au == null) {
            au = new DefaultRecord();
            manager.getIS().set("$agentstates.utterance", au);
        }

        as = au.getList("sentences");
        if (as == null) {
            as = new DefaultList();
        }

        numArgValues = values.size();
        nouns = posUtterance.getList("nouns");
        adjectives = posUtterance.getList("adjectives");
        lastStated = posUtterance.getList("lastStated");
        keywords = manager.getIS().getList("$userstates.utterance.keywords");

        prevAgentIntentions = agentUtterance.getList("prevIntentions");
        if (prevAgentIntentions == null) {
            prevAgentIntentions = new DefaultList();
        }

        int possSize = 0;
        try {
            possSize = posUtterance.getInteger("possSize");
        } catch (NullPointerException e) {
        }

        if (keywords == null) {
            manager.getIS().set("$userstates.utterance.keywords", new DefaultList());
            keywords = manager.getIS().getList("$userstates.utterance.keywords");
        }

        values = ah.sortValues(values, 10, true);

        if (nouns != null && adjectives != null && lastStated != null) {
            values = posQualifier(values, nouns.size(), "@noun");
            values = posQualifier(values, possSize, "@nounposs");
            values = posQualifier(values, keywords.size(), "@keyword");
            if (values.size() > 0) {
                FilePointer value = randomValue(values);
                agentOutput(value, isRepeat);
            }
        }
        if(prevAgentIntentions.size() > as.size()){
            prevAgentIntentions.remove(prevAgentIntentions.size()-1+"");
            agentUtterance.set("prevIntentions", prevAgentIntentions);
        }

    }

    public <T> T randomValue(ArrayList<T> values) {
        Random randomGenerator = new Random();
        int index = randomGenerator.nextInt(values.size());
        return values.get(index);
    }

    @Override
    public void prepare(ArrayList<String> argNames, ArrayList<String> argValues) {

    }

    @Override
    public Manager getManager() {
        return this.manager;
    }

    @Override
    public void setManager(Manager manager) {
        this.manager = manager;

    }

}
