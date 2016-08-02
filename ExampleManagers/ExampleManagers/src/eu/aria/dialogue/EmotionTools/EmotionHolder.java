package eu.aria.dialogue.EmotionTools;

import eu.aria.dialogue.util.SpeechGenerator;
import eu.aria.util.types.AudioEmotionData;
import eu.aria.util.types.EMaxData;

import java.util.List;

/**
 * Created by adg on 18/01/2016.
 *
 */
public class EmotionHolder {

    private double currEmotionsVal = 0.0;

    BasicEmotions emotionAudio = new BasicEmotions();

    private SpeechGenerator sg = new SpeechGenerator();

    public double getCurrEmotionsVal() {

        return currEmotionsVal;
    }

    public BasicEmotions getEmotionAudio(){
        return emotionAudio;
    }

    public void process(String text) {
        if (currEmotionsVal > -0.2 && currEmotionsVal < 0.2) {
            sg.process(text, "", "");
        } else if (currEmotionsVal <= -0.2) {
            sg.process(text, "", "sadness");
        } else if (currEmotionsVal >= 0.2) {
            sg.process(text, "", "joyStrong");
        }
    }

    public String getFml() {
        return sg.getFml();
    }

    public void onEMaxData(EMaxData data) {

        List<EMaxData.EMaxFace> faces = data.getFaces();

        if (faces.isEmpty()) {
            return;
        }

        EMaxData.EMaxFace face = faces.get(0);
        double anger = normalizeValue(face.getAnger());
        double disgust = normalizeValue(face.getDisgust());
        double fear = normalizeValue(face.getFear());
        double happiness = normalizeValue(face.getHappiness());
        double neutral = normalizeValue(face.getNeutral());
        double sadness = normalizeValue(face.getSadness());
        double surprised = normalizeValue(face.getSurprised());

        double posEmotionsVal = happiness;
        double negEmotionsVal = (anger + fear + disgust + sadness) / 4;
        if (negEmotionsVal > posEmotionsVal) currEmotionsVal = -negEmotionsVal;
        else currEmotionsVal = posEmotionsVal;
    }

    public void onAudioEmotionData(AudioEmotionData data) {
        if(data.hasArousal()){
            emotionAudio.setArousal(data.getArousal());
        }
        if(data.hasValence()){
            emotionAudio.setValence(data.getValence());
        }
        if(data.hasInterest()){
            emotionAudio.setInterest(data.getInterest());
        }
    }

    private static double normalizeValue(double val) {
        double oldMax = 1;
        double oldMin = -1.5;
        double oldRange = oldMax - oldMin;
        double newRange = 1;
        return (((val - oldMin) * newRange) / oldRange);
    }
}
