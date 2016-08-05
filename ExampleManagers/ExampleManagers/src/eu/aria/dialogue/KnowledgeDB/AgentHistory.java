package eu.aria.dialogue.KnowledgeDB;

import eu.ariaagent.util.FilePointer;

import java.util.*;

/**
 * Created by Kevin Bowden on 8/3/2016.
 *
 */
public class AgentHistory {
    public static AgentHistory ah = new AgentHistory();
    public HashMap<String, Integer> templateHistory = new HashMap<>();

    public static AgentHistory getAH() {
        return ah;
    }

    public void storeRule(String value) {
        int freq = 1;

        if (templateHistory.containsKey(value)) {
            freq = templateHistory.get(value) + 1;
        }
        templateHistory.put(value, freq);
    }

    public int getRuleFrequency(String value) {
        int freq = 0;

        if (templateHistory.containsKey(value)) {
            freq = templateHistory.get(value);
        }
        return freq;
    }

    public ArrayList<FilePointer> sortValues(ArrayList<FilePointer> values, int threshold, boolean justSmallest) {
        ArrayList<HashMap.SimpleEntry<FilePointer, Integer>> sortedList = new ArrayList<>();
        ArrayList<FilePointer> stringList = new ArrayList<>();

        for (FilePointer value : values) {
            int freq = getRuleFrequency(value.getCurSpeechContent());
            AbstractMap.SimpleEntry<FilePointer, Integer> newEntry = new AbstractMap.SimpleEntry<>(value, freq);
            sortedList.add(newEntry);
        }
        Collections.sort(sortedList, new Comparator<Map.Entry<?, Integer>>() {

            public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        int smallest = sortedList.get(0).getValue();
        for (HashMap.SimpleEntry<FilePointer, Integer> entry : sortedList) {
            if (entry.getValue() < threshold) {
                if (justSmallest && smallest != entry.getValue()) {
                    break;
                }
                stringList.add(entry.getKey());
            }
        }
        return stringList;
    }
}
