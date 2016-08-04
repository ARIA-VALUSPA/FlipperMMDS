package eu.aria.dialogue.util;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin Bowden on 8/1/2016.
 */
public class Wordnet {

    IDictionary dict;

    public Wordnet(){
        File file = new File("WordNet3/dict");
        dict = new Dictionary(file);
        try {
            dict.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public POS getWordnetPOS(String pos){
        if(pos.equals("JJ")){
            return POS.ADJECTIVE;
        }
        return null;
    }

    // construct the URL to the Wordnet dictionary directory
    public ArrayList<String> findSynonym(String word, String pos) {
        POS wnPOS = getWordnetPOS(pos);
        if(wnPOS == null){
            return null;
        }

        IIndexWord idxWord = dict.getIndexWord(word, wnPOS);
        IWordID wordID = idxWord.getWordIDs().get(0);
        IWord iWord = dict.getWord(wordID);
        ISynset synset = iWord.getSynset();

        ArrayList<String> syns = new ArrayList<>();
        for(IWord w : synset.getWords()) {
            syns.add(w.getLemma());
        }

        return syns;
    }

    public ArrayList<String> findAntonyms(String word, String pos) {
        POS wnPOS = getWordnetPOS(pos);
        if(wnPOS == null){
            return null;
        }

        IIndexWord idxWord = dict.getIndexWord(word, wnPOS);
        IWordID wordID = idxWord.getWordIDs().get(0);
        IWord iWord = dict.getWord(wordID);
        ArrayList<String> ants = new ArrayList<>();
        for(IPointer k : iWord.getRelatedMap().keySet()){
            if(k.getName().equals("Antonym")){
                List<IWordID> map = iWord.getRelatedMap().get(k);
                for(IWordID m : map){
                    IWord i = dict.getWord(m);
                    for(IWord blah : i.getSynset().getWords()){
                        ants.add(blah.getLemma());
                    }
                }
            }
        }
        return ants;
    }

    public static void main( String args[] ){
        Wordnet wn = new Wordnet();
        System.out.println(wn.findSynonym("happy", "JJ"));

    }

}