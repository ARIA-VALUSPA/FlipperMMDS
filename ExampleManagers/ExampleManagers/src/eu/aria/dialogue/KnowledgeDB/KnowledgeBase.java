package eu.aria.dialogue.KnowledgeDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Kevin Bowden on 8/1/2016.
 */
public class KnowledgeBase {

public static KnowledgeBase kb = new KnowledgeBase();
private HashMap nounHash = new HashMap();


    public static KnowledgeBase getKB(){
        return kb;
    }

    public KnowledgeItem verifyEntry(String noun){
        if(!nounHash.containsKey(noun)){
            KnowledgeItem ki = new KnowledgeItem();
            nounHash.put(noun, ki);
            return ki;
        }
        return (KnowledgeItem) nounHash.get(noun);
    }

    public void storeAdj(String noun, String adj){
        KnowledgeItem ki = verifyEntry(noun);
        ki.storeAdj(adj);
    }

    public int numAdj(String noun) {
        KnowledgeItem ki = verifyEntry(noun);
        return ki.numAdj();
    }

    public void addNounAlias(String noun, String alias){
        KnowledgeItem ki = verifyEntry(noun);
        ki.addToList("aka", alias);
    }

    public void removePossession(String noun){
        KnowledgeItem ki = verifyEntry(noun);
        ki.setBool("possession", false);
    }

    public void removeNegateNoun(String noun){
        KnowledgeItem ki = verifyEntry(noun);
        ki.setBool("neg", false);
    }

    public boolean isPossession(String noun){
        KnowledgeItem ki = verifyEntry(noun);
        return ki.isPossession();
    }

    public void makePossession(String noun){
        KnowledgeItem ki = verifyEntry(noun);
        ki.setBool("possession", true);
    }

    public int numNouns(){
        return nounHash.size();
    }

    public void negateNoun(String noun){
        KnowledgeItem ki = verifyEntry(noun);
        ki.setBool("neg", true);
    }

    public void negateAdj(String noun, String adj){
        KnowledgeItem ki = verifyEntry(noun);
        ki.negateAdj(adj);
    }

    public void addNounQuantity(String noun, String quantity){
        KnowledgeItem ki = verifyEntry(noun);
        ki.addToList("quantity", quantity);
    }

    public String getAdj(String noun, Set exclude){
        if(numAdj(noun) == 0 ){
            return null;
        }
        KnowledgeItem ki = verifyEntry(noun);
        return ki.getAdj(exclude);
    }

    public void dumpAdjectives(String noun){
        KnowledgeItem ki = verifyEntry(noun);
        ki.dump("adjectives");
    }

    public void dumpRelatedNouns(String noun){
        KnowledgeItem ki = verifyEntry(noun);
        ki.dump("relatedNouns");
    }

    public void storeRelatedNouns(ArrayList<String> nouns){

        for(int i = 0; i < nouns.size(); i++){
            for(int j = 0; j < nouns.size(); j++){
                String rootNoun = nouns.get(i);
                KnowledgeItem ki = verifyEntry(rootNoun);
                if(j != i) {
                    ki.storeRelated(nouns.get(j));
                }
            }
        }
    }

    public int numRelatedNouns(String noun) {
        KnowledgeItem ki = verifyEntry(noun);
        return ki.numRelated();
    }

}
