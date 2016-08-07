/*
by Kevin Bowden
 */
package eu.aria.dialogue.managers;

import eu.ariaagent.managers.DefaultManager;
import hmi.flipper.defaultInformationstate.DefaultList;
import hmi.flipper.defaultInformationstate.DefaultRecord;
import hmi.flipper.informationstate.List;
import hmi.flipper.informationstate.Record;

import java.util.Map;

/**
 *
 * By Kevin Bowden
 */
 public class StoryManager extends DefaultManager {
    private String userUtterancePath = "$userstates.utterance";
    private String userStoryPath = "$userstates.utterance.story";

    public StoryManager(DefaultRecord is) {
        super(is);
        System.out.println("IntentGenerator is WIP. It should be using a Flipper template later. Right now it uses: ");
        System.out.println("$userstates.utterance(.consumed <boolean> |.timestamp = 't:<long ms since 1970>' |.text = <string> ) -> $userstates.intention = <string>");
        interval = 75; //fast default interval
    }

    @Override
    public void setParams(Map<String, String> params, Map<String, String[]> paramList) {
        String path = params.get("user_utterance_is_path");
        if (path != null) {
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

        Record storyUtterance = getIS().getRecord(userStoryPath);
        if (storyUtterance == null) {
            storyUtterance = new DefaultRecord();
            getIS().set(userStoryPath, storyUtterance);
        }

        if (storyUtterance.getList("sentences") == null) {
            storyUtterance.set("sentences", new DefaultList());
        }
        if (storyUtterance.getList("finishedStating") == null) {
            storyUtterance.set("finishedStating", new DefaultList());
        }
        if (storyUtterance.getInteger("numSentences") == null) {
            storyUtterance.set("numSentences", 0);
        }
        if (utterance.getString("consumed") == null) {
            utterance.set("consumed", "true");
        }
        if (!utterance.getString("consumed").equals("true")) {

            double currTime = (double) System.currentTimeMillis();
            String userSay = utterance.getString("text");

            if (userSay != null) {
                List sentences = storyUtterance.getList("sentences");
                List finishedStating = storyUtterance.getList("finishedStating");
                storyUtterance.set("numSentences", sentences.size());

                sentences.addItemEnd(userSay);
                finishedStating.addItemEnd(currTime);
                storyUtterance.set("sentences", sentences);
                storyUtterance.set("finishedStating", finishedStating);

            }
        }
    }
}
