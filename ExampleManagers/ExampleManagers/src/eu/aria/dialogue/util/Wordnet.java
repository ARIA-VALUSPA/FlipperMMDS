package eu.aria.dialogue.util;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.stanford.nlp.ling.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Kevin Bowden on 8/1/2016.
 */
public class Wordnet {

    public Wordnet(){

    }

//    List<IWordID> wordIDList = indexWordList.get(0).getWordIDs();
//        for(int idIndex = 0; idIndex < wordIDList.size(); idIndex++)
//    {
//        IWordID wordID = wordIDList.get(idIndex);
//        IWord word = m_Dict.getWord(wordID);
//        System.out.println("Id = " + wordID);
//
//        System.out.println(" Lemma = " + word.getLemma());
//        System.out.println(" Gloss = " + word.getSynset().getGloss());
//
//        ISynset synset = word.getSynset();
//        String LexFileName = synset.getLexicalFile().getName();
//        System.out.println("Lexical Name : " + LexFileName);
//
//        /** Finding stem for the word. */
//        WordnetStemmer stem = new WordnetStemmer(m_Dict);
//        //System.out.println("test" + stem.findStems(key, POS.NOUN));
//
//        ArrayList<String> antonymsList = new ArrayList<String>();
//
//        List<IWordID> relatedWords = word.getRelatedWords();
//        Map<IPointer, List<IWordID>> map = word.getRelatedMap();
//        AdjMarker marker = word.getAdjectiveMarker();
//
//        for (IWordID antonym : word.getRelatedWords()) {
//            String meaning = m_Dict.getWord(antonym).getLemma();
//            antonymsList.add(meaning);
//            System.out.println("Antonym: " + meaning);
//            System.out.println("Antonym POS: " + m_Dict.getWord(antonym).getPOS());
//        }
//
//    }

    // construct the URL to the Wordnet dictionary directory
    public void runExample() {
//        String wnhome = System.getenv("WNHOME");
//        String path = wnhome + File.separator + "dict";
//        URL url = null;
//        try {
//            url = new URL("file", null, path);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        if (url == null) return;
//
//        // construct the dictionary object and open it
//        IDictionary dict = new Dictionary(url);
//        try {
//            dict.open();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        // look up first sense of the word "dog"
//        IIndexWord idxWord = dict.getIndexWord("dog", POS.NOUN);
//        IWordID wordID = idxWord.getWordIDs().get(0);
//        IWord word = dict.getWord(wordID);
//        System.out.println("Id = " + wordID);
//        System.out.println("Lemma = " + word.getLemma());
//        System.out.println("Gloss = " + word.getSynset().getGloss());

//        URL url = null;
//        try {
//            url = new URL("file", null, "../WordNet3/dict");
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        if (url == null) return;
//
//        IDictionary dict = new Dictionary(url);

        IDictionary dict = new Dictionary(new File("../WordNet3/dict"));
        try {
            dict.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        WordnetStemmer stemmer = new WordnetStemmer(dict);

        List<String> test = stemmer.findStems("feet", POS.NOUN);
        for (int i = 0; i < test.size(); i++) {
            System.out.println(test.get(i));
        }
    }
}