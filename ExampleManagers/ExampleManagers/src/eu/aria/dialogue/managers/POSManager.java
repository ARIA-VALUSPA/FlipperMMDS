/*
by Kevin Bowden
 */
package eu.aria.dialogue.managers;

import edu.stanford.nlp.trees.TypedDependency;
import eu.aria.dialogue.KnowledgeDB.KnowledgeBase;
import eu.aria.dialogue.util.*;
import eu.ariaagent.managers.DefaultManager;
import hmi.flipper.defaultInformationstate.DefaultList;
import hmi.flipper.defaultInformationstate.DefaultRecord;
import hmi.flipper.informationstate.List;
import hmi.flipper.informationstate.Record;

import java.util.*;

/**
 *
 * @author Siewart
 */
 public class POSManager extends DefaultManager {
    private String userUtterancePath = "$userstates.utterance";
    private String userposPath = "$userstates.utterance.pos";

    private StanfordTagger stanfordTagger;
    private StanfordParser stanfordParser;

    KnowledgeBase kb = KnowledgeBase.getKB();

    public ArrayList<String> currNouns;

    public POSManager(DefaultRecord is) {
        super(is);
        System.out.println("IntentGenerator is WIP. It should be using a Flipper template later. Right now it uses: ");
        System.out.println("$userstates.utterance(.consumed <boolean> |.timestamp = 't:<long ms since 1970>' |.text = <string> ) -> $userstates.intention = <string>");
        interval = 50; //fast default interval
    }

    @Override
    public void setParams(Map<String, String> params, Map<String, String[]> paramList) {
        String path = params.get("user_utterance_is_path");
        if (path != null) {
            userUtterancePath = path;
        }

        String posModel = params.get("pos_model_path");
        String parseModel = params.get("parse_model");

        stanfordTagger = new StanfordTagger(posModel);
        stanfordParser = new StanfordParser(parseModel);

    }

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

        Record posUtterance = getIS().getRecord(userposPath);
        if (posUtterance == null) {
            posUtterance = new DefaultRecord();
            getIS().set(userposPath, posUtterance);
        }

        if (!utterance.getString("consumed").equals("true")) {
            String basicAdj = null;
            ArrayList<String> nounBuilder = new ArrayList<>();

            if (posUtterance.getInteger("nounSize") == null) {
                posUtterance.set("nounSize", 0);
            }
            if (posUtterance.getInteger("possSize") == null) {
                posUtterance.set("possSize", 0);
            }
            if (posUtterance.getList("nouns") == null) {
                posUtterance.set("nouns", new DefaultList());
            }
            if (posUtterance.getList("adjectives") == null) {
                posUtterance.set("adjectives", new DefaultList());
            }
            if (posUtterance.getList("lastStated") == null) {
                posUtterance.set("lastStated", new DefaultList());
            }
            if (posUtterance.getList("frequency") == null) {
                posUtterance.set("frequency", new DefaultList());
            }
            if (posUtterance.getList("preference") == null) {
                posUtterance.set("preference", new DefaultList());
            }

            String userSay = utterance.getString("text");
            if (userSay != null && !userSay.equals("") && !utterance.getString("consumed").equals("true")) {
                currNouns = new ArrayList<>();
                ArrayList<String> wordset = new ArrayList<>(Arrays.asList(userSay.split(" ")));
                ArrayList<String> taggedText = stanfordTagger.tagFile(wordset);

                for (int i = 0; i < taggedText.size(); i++) {
                    String word = taggedText.get(i);
                    int index = word.lastIndexOf("_");

                    String pos = word.substring(index, word.length());
                    pos = pos.substring(1, pos.length());
                    word = word.substring(0, index);

                    List nounsList = posUtterance.getList("nouns");
                    List frequency = posUtterance.getList("frequency");
                    List preference = posUtterance.getList("preference");
                    List lastStated = posUtterance.getList("lastStated");
                    List adjectives = posUtterance.getList("adjectives");

//                    Random randomGenerator = new Random();
//                    int k = randomGenerator.nextInt(nouns.size());
//                    String randomItem = nouns.getString(k);
//                    nounsList.addItemEnd(randomItem);

//                    if(nounsList.size() > 2) {
//                        nounsList.getItem(0).setStringValue(nounsList.getItem(0).getString() + "::string");
//                    }

                    if (pos.startsWith("JJ") && i + 1 < taggedText.size()) {
                        String nextWord = taggedText.get(i + 1);
                        int adjDex = nextWord.lastIndexOf("_");
                        String nextPos = nextWord.substring(adjDex, nextWord.length());
                        nextPos = nextPos.substring(1, nextPos.length());
                        if (nextPos.startsWith("NN")) {
                            basicAdj = word;
                        }
                    }
                    if (pos.startsWith("NN")) {
                        nounBuilder.add(word);
                    }
                    double currTime = (double) System.currentTimeMillis();
                    if ((!pos.startsWith("NN") || i == taggedText.size() - 1) && nounBuilder.size() > 0) {
                        String noun = "";
                        for (String n : nounBuilder) {
                            noun += n + " ";
                            noun = noun.trim();
                        }
                        nounBuilder.clear();
                        int nid = -1;
                        if (nounsList.size() > 0 && nounsList.contains(noun)) {
                            //update freq by 1 and timestamp
                            for (int j = 0; j < nounsList.size(); j++) {
                                if (noun.equals(nounsList.getString(j))) {
                                    nid = j;
                                    break;
                                }
                            }

                            frequency.getItem(nid).setIntegerValue(frequency.getInteger(nid) + 1);
                            lastStated.getItem(nid).setDoubleValue(currTime);

                            posUtterance.set("frequency", frequency);
                            posUtterance.set("lastStated", lastStated);

                        } else {
                            //add word, freq of 1, timestamp, and preference
                            currNouns.add(noun);
                            nounsList.addItemEnd(noun);
                            frequency.addItemEnd(1);
                            preference.addItemEnd(.5);
                            lastStated.addItemEnd(currTime);
                            adjectives.addItemEnd(0);
                            nid = nounsList.size() - 1;
                            posUtterance.set("nounSize", nounsList.size());
                            posUtterance.set("nouns", nounsList);
                            posUtterance.set("frequency", frequency);
                            posUtterance.set("preference", preference);
                            posUtterance.set("lastStated", lastStated);
                        }
//                        if (basicAdj != null) {
//                            if (basicAdj != null) {
//                                kb.storeAdj(noun, basicAdj);
//                                basicAdj = null;
//                                adjectives.getItem(nid).setIntegerValue(kb.numAdj(noun)+1);
//                                posUtterance.set("adjectives", adjectives);
//                            }
//                        }
                    }
                }
                //TODO implement parsing relationships, mostly modifiers
//                Tree parse = stanfordParser.parseWords(wordset);
                java.util.List<TypedDependency> depParse = stanfordParser.dependencyParse(userSay);

                ArrayList<Integer> nsubjhave = new ArrayList<>();
                HashMap<Integer, ArrayList<String>> adjHash = new HashMap<>();
                for(int k = 0; k < 2; k++) {
                    for (int i = 0; i < depParse.size(); i++) {
                        TypedDependency dep = depParse.get(i);
                        System.out.println(depParse.get(i));
                        //known by a different name - Jack, my brother
                        if (dep.reln().getShortName().equals("appos") && k == 0) {
                            String child = dep.gov().word();
                            String root = dep.dep().word();
                            kb.addNounAlias(child, root);
                            kb.addNounAlias(root, child);
                        }
                        //negations present - Bill is not a doctor
                        else if (dep.reln().getShortName().equals("neg") && k == 1) {
                            String word = dep.gov().word();
                            String pos = dep.gov().tag();
                            System.out.println(word);
                            System.out.println(pos);
                            if (pos.startsWith("NN")) {
                                kb.negateNoun(word);
                            } else if (pos.startsWith("JJ")) {
                                System.out.println(adjHash.keySet());
                                System.out.println("======================");
                                System.out.println(adjHash.entrySet());

                                kb.dumpAdjectives("dog");
                                if (adjHash.containsKey(dep.gov().index())) {
//                                System.out.println(alist);
                                    System.out.println(word);

                                    ArrayList<String> alist = adjHash.get(dep.gov().index());
                                    for (String a : alist) {
                                        System.out.println(a);
                                        System.out.println(word);
                                        kb.negateAdj(a, word);
                                    }
                                }
                            }
                        }
                        //quantity there are 3 sheep
                        else if (dep.reln().getShortName().equals("nummod") && k == 1) {
                            String noun = dep.gov().word();
                            String num = dep.dep().word();
                            kb.addNounQuantity(noun, num);
                        }
                        //direct modifier - the hairy ape
                        else if (dep.reln().getShortName().equals("amod")  && k == 0) {
                            String noun = dep.gov().word();
                            String adj = dep.dep().word();
                            kb.storeAdj(noun, adj);
                            List adjectives = posUtterance.getList("adjectives");
                            List nouns = posUtterance.getList("nouns");
                            int nid = -1;
                            //update freq by 1 and timestamp
                            for (int j = 0; j < nouns.size(); j++) {
                                if (noun.equals(nouns.getString(j))) {
                                    nid = j;
                                    break;
                                }
                            }
                            ArrayList<String> n;
                            if (!adjHash.containsKey(dep.dep().index())) {
                                n = new ArrayList<>();
                            } else {
                                n = adjHash.get(dep.dep().index());
                            }
                            n.add(dep.gov().word());
                            adjHash.put(dep.dep().index(), n);
                            adjectives.getItem(nid).setIntegerValue(kb.numAdj(noun) + 1);
                            posUtterance.set("adjectives", adjectives);

                        }
                        // indirect modifier the ape was hairy
                        else if (dep.reln().getShortName().equals("nsubj") && k == 0) {
                            String adj = "";
                            String noun = "";
                            String depWord = dep.dep().word();
                            String govWord = dep.gov().word();
                            String depPOS = dep.dep().tag();
                            String govPOS = dep.gov().tag();

                            if ((depWord.equals("I")) && (govWord.equals("have") || govWord.equals("had"))) {
                                nsubjhave.add(dep.gov().index());
                            }
                            if (depPOS.startsWith("NN")) {
                                noun = depWord;
                            } else if (depPOS.startsWith("JJ")) {
                                adj = depWord;
                            }
                            if (govPOS.startsWith("NN")) {
                                noun = govWord;
                            } else if (govPOS.startsWith("JJ")) {
                                adj = govWord;
                            }
                            if (!noun.equals("") && !adj.equals("")) {
                                kb.storeAdj(noun, adj);
                                List adjectives = posUtterance.getList("adjectives");
                                List nouns = posUtterance.getList("nouns");
                                int nid = -1;
                                //update freq by 1 and timestamp
                                for (int j = 0; j < nouns.size(); j++) {
                                    if (noun.equals(nouns.getString(j))) {
                                        nid = j;
                                        break;
                                    }
                                }

                                adjectives.getItem(nid).setIntegerValue(kb.numAdj(noun) + 1);
                                posUtterance.set("adjectives", adjectives);
                                ArrayList<String> n;
                                if (!adjHash.containsKey(dep.gov().index())) {
                                    n = new ArrayList<>();
                                } else {
                                    n = adjHash.get(dep.gov().index());
                                }
                                n.add(dep.dep().word());
                                adjHash.put(dep.gov().index(), n);
                            }
                        }
                        // possessive case for 'my' words
                        else if (dep.reln().getShortName().equals("nmod:poss") && k == 1) {
                            String noun = dep.gov().word();
                            if (!kb.isPossession(noun)) {
                                kb.makePossession(noun);
                                posUtterance.set("possSize", posUtterance.getInteger("possSize") + 1);
                            }
                        }
                        //possessive case for 'have' words,
                        else if (dep.reln().getShortName().equals("dobj") && k == 1) {
                            String noun = dep.dep().word();
                            if (nsubjhave.contains(dep.gov().index())) {
                                if (!kb.isPossession(noun)) {
                                    kb.makePossession(noun);
                                    posUtterance.set("possSize", posUtterance.getInteger("possSize") + 1);
                                    System.out.println("made into a possession");
                                }
                            }
                        }
                    }
                }
                kb.storeRelatedNouns(currNouns);
            }
        }
    }


 }
