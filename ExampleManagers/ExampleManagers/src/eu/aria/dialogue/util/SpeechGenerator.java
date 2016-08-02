/**
 * Created by Alexandru G on 29/09/2015.
 *
 */


package eu.aria.dialogue.util;

import vib.core.intentions.*;
import vib.core.util.Mode;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import vib.core.util.time.TimeMarker;
import vib.core.util.xml.XMLTree;

import java.util.ArrayList;

public class SpeechGenerator {

    private ArrayList<Intention> intentions = new ArrayList<>();
    private ID id = IDProvider.createID("MyID");
    private Mode mode = Mode.append;
    private String fml;

    public void process(String speech, String performance, String emotion) {
        intentions.clear();
        PseudoIntentionSpeech pis = new PseudoIntentionSpeech();
        pis.setId("s1");

        /*
        // for multiple speech:
        for (int i = 0; i < speech.size() ; i++) {
            pis.addSpeechElement(new TimeMarker("tm" + i+1));
            pis.addSpeechElement(speech.get(i));
        }*/
        if(performance.equals("")) performance = "beatsocial";
        //if(emotion.equals("")) emotion = "joyStrong";

        pis.addSpeechElement(new TimeMarker("tm1"));
        pis.addSpeechElement(speech);
        intentions.add(pis);
        TimeMarker biStart = new TimeMarker("start");
        biStart.addReference(pis.getTimeMarker("tm1"));
        TimeMarker biEnd = new TimeMarker("end");

        biEnd.addReference(pis.getTimeMarker("tm1"),2);

        //biEnd.addReference(pis.getTimeMarker("tm" + speech.size()+1)); // multiple speech
        BasicIntention bi = new BasicIntention("performative", "p1", performance , biStart, biEnd, 1);
        intentions.add(bi);

        EmotionIntention ei = new EmotionIntention("e1", emotion, biStart, biEnd, 1, 1, 1 );
        intentions.add(ei);


        mode = Mode.append;
        id = IDProvider.createID("MyID");

        fml = null;
    }

    public ArrayList<Intention> getIntentions() {
        return intentions;
    }

    public ID getId() {
        return id;
    }

    public Mode getMode() {
        return mode;
    }

    public String getFml() {
        if (fml == null) {
            XMLTree tree = FMLTranslator.IntentionsToFML(intentions, mode);
            fml = tree.toString();
        }
        return fml;
    }
}
