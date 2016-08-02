package eu.aria.dialogue.util;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by adg on 30/12/2015.
 *
 */
public class ActionTimer {

    public static final int INVALID_MAPPING = -1;
    public static final int ACCEPTED_TIME_IN = -2;
    public static final int INVALID_TIME_OUT = -3;
    public static final int UNDERFLOW_ERROR = -4;

    private HashMap<String, LinkedList<Long>> ins = new HashMap<>();
    private HashMap<String, Integer> outs = new HashMap<>();
    private HashMap<String, String> reverseMappings = new HashMap<>();


    public void addBinding(String in, String out) {
        if (!ins.containsKey(in)) {
            ins.put(in, new LinkedList<>());
        }
        if (!outs.containsKey(out)) {
            outs.put(out, 0);
        }

        reverseMappings.put(out, in);
    }

    public long addTime(String id) {
        long timestamp = System.currentTimeMillis();
        if (ins.containsKey(id)) {
            return addTimeIn(id, timestamp);
        }

        if (outs.containsKey(id)) {
            return addTimeOut(id, timestamp);
        }

        return INVALID_MAPPING;
    }

    private long addTimeIn(String id, long timestamp) {
        ins.get(id).addLast(timestamp);
        return ACCEPTED_TIME_IN;
    }

    private long addTimeOut(String id, long timestamp) {
        String in = reverseMappings.get(id);
        if (in != null) {
            LinkedList<Long> list = ins.get(in);
            if (list.isEmpty()) {
                return UNDERFLOW_ERROR;
            }
            long inTime = list.pollFirst();
            return timestamp - inTime;
        }

        return INVALID_TIME_OUT;
    }

}
