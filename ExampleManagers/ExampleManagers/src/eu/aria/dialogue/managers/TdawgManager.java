/*
by Kevin Bowden
 */
package eu.aria.dialogue.managers;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import eu.aria.dialogue.util.StanfordParser;
import eu.aria.dialogue.util.StanfordTagger;
import eu.ariaagent.managers.DefaultManager;
import hmi.flipper.defaultInformationstate.DefaultList;
import hmi.flipper.defaultInformationstate.DefaultRecord;
import hmi.flipper.informationstate.List;
import hmi.flipper.informationstate.Record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author Siewart
 */




 public class TdawgManager extends DefaultManager {
    private String userUtterancePath = "$userstates.utterance";
    private String userTdawgPath = "$userstates.utterance.tdawg";
    private String userposPath = "$userstates.utterance.pos";


    public static String[] country = {"germany","america","england"}; ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public TdawgManager(DefaultRecord is) {
        super(is);
        System.out.println("IntentGenerator is WIP. It should be using a Flipper template later. Right now it uses: ");
        System.out.println("$userstates.utterance(.consumed <boolean> |.timestamp = 't:<long ms since 1970>' |.text = <string> ) -> $userstates.intention = <string>");
        interval = 50; //fast default interval
    }

    @Override
    public void setParams(Map<String, String> params, Map<String, String[]> paramList){
        String path = params.get("user_utterance_is_path");
        if(path != null){
            userUtterancePath = path;
        }

    }

    @Override
    public void process() {
        super.process();


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

        Record tdawgUtterance = getIS().getRecord(userTdawgPath);

        if (tdawgUtterance == null) {
            tdawgUtterance = new DefaultRecord();
            getIS().set(userTdawgPath, tdawgUtterance);

        }

        if (!utterance.getString("consumed").equals("true")) {
            String basicAdj = null;
            ArrayList<String> nounBuilder = new ArrayList<>();

                if (posUtterance.getList("nouns") == null) {
                    posUtterance.set("nouns", new DefaultList());
                }
                if (tdawgUtterance.getString("exclaim") == null) {
                    tdawgUtterance.set("exclaim", "");
                }

            String userSay = utterance.getString("text");

            if (userSay != null && !utterance.getString("consumed").equals("true")) {

               // if(userSay.contains("germany")){}


                if(userSay.endsWith("!")){
                    tdawgUtterance.set("exclaim", "true");

                } else {
                    tdawgUtterance.set("exclaim", "false");
                }
                List nounsList = posUtterance.getList("nouns");
            }
        }
    }
 }
