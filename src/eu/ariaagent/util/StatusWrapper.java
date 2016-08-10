package eu.ariaagent.util;

/**
 * Created by adg on 22/07/2016.
 *
 */
public class StatusWrapper {

    private StatusListener statusListener;

    private Status status = Status.Closed;

    public enum Status {
        Ready, Error, Closed
    }

    public void setStatusListener(StatusListener statusListener) {
        this.statusListener = statusListener;
    }

    void changeStatus(Status status) {
        this.status = status;
        if (statusListener != null) {
            statusListener.onStatusChanged(status);
        }
    }

    public interface StatusListener {
        void onStatusChanged(Status status);
    }
}
