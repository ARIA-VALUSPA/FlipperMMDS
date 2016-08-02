package eu.aria.dialogue.KnowledgeDB;

import java.util.*;


/**
 * Created by Kevin Bowden on 8/1/2016.
 */
public class KnowledgeItem {

    private HashMap<String, Integer> adjectives;
    private HashMap<String, Integer> relatedNouns;
    private ArrayList<String> aka, quantity;
    private boolean neg;
    private boolean possession;

    public KnowledgeItem(){
        adjectives = new HashMap<>();
        relatedNouns = new HashMap<>();
        aka = new ArrayList<>();
        quantity = new ArrayList<>();
        neg = false;
        possession = false;
    }

    public void setBool(String bootToInvert, boolean bool){
        if(bootToInvert.equals("neg")){
            neg = bool;
        } else if(bootToInvert.equals("possession")){
            possession = bool;
        }

    }

    public boolean hasAdj(boolean includeNegations, String adj){
        if(adjectives.containsKey(adj)){
            return true;
        }
        else if(includeNegations && adjectives.containsKey("not " + adj)){
            return true;
        }
        return false;
    }

    public void negateAdj(String adj){
        int freq = 1;
        if(hasAdj(false, adj)){
            freq = adjectives.get(adj);
            adjectives.remove(adj);
        }
        adjectives.put("not " + adj, freq);
    }

    public boolean isPossession(){
        return possession;
    }

    public void storeAdj(String adj){
        int adjCount = 0;
        if(adjectives.containsKey(adj)) {
            adjCount = (int) adjectives.get(adj);
        }
        adjectives.put(adj, adjCount+1);
    }

    public void addToList(String listName, String value){
        if(listName.equals("aka")){
            aka.add(value);
        } else if (listName.equals("quantity")){
            quantity.add(value);
        }
    }

    public int numAdj() {
        return adjectives.size();
        }

    public int numRelated(){
        return relatedNouns.size();
    }

    public void storeRelated(String noun){
        int adjCount = 0;
        if(relatedNouns.containsKey(noun)) {
            adjCount = (int) relatedNouns.get(noun);
        }
        relatedNouns.put(noun, adjCount+1);
    }

    public void dump(String hash){
        if(hash.equals("adjectives")){
            System.out.println(adjectives.entrySet());
        } else if(hash.equals("related")){
            System.out.println(relatedNouns.entrySet());
        }
    }

    public String getAdj(Set exclude){
        Map.Entry<String, Integer> maxEntry = null;

        for (Map.Entry<String, Integer> entry : adjectives.entrySet())
        {
            if ((maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)) {
                if( exclude == null || !exclude.contains(entry.getKey())) {
                    maxEntry = entry;
                }
            }
        }
        return maxEntry.getKey();
    }

    public String getMostFreqAdj(){
        Map.Entry<String, Integer> maxEntry = null;

        for (Map.Entry<String, Integer> entry : adjectives.entrySet())
        {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
            {
                maxEntry = entry;
            }
        }
        return maxEntry.getKey();
    }

}
