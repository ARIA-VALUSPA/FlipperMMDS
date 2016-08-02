package eu.aria.dialogue.util;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

// By Kevin Bowden
//parse tag meanings: https://gist.github.com/nlothian/9240750

public class StanfordParser {

    static LexicalizedParser parser;

    public StanfordParser(String parseModel) {
        parser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
           }

    public List<TypedDependency> dependencyParse(String text){
        TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
        Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new StringReader(text));
        List<CoreLabel> rawWords2 = tok.tokenize();
        Tree parse = parser.apply(rawWords2);

        TreebankLanguagePack tlp = parser.treebankLanguagePack(); // PennTreebankLanguagePack for English
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();

//        System.out.println(tdl);
//        System.out.println();
//
//        TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
//        tp.printTree(parse);

        return tdl;



    }

    public Tree parseWords(ArrayList<String> wordsList){
        String[] wordsArray = wordsList.toArray(new String[wordsList.size()]);
        List<CoreLabel> rawWords = Sentence.toCoreLabelList(wordsArray);

        //pos tag tree parse, type:Tree
        Tree parse = parser.apply(rawWords);
        return parse;




        }

}