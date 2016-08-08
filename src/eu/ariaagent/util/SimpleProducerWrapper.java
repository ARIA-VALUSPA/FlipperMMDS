package eu.ariaagent.util;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * Created by adg on 08/08/2016.
 *
 */
public class SimpleProducerWrapper {
    private String url;
    private String name;
    private boolean isTopic;

    private Connection connection;
    private Session session;
    private MessageProducer producer;

    private StatusListener statusListener;

    private volatile Status status = Status.Closed;

    public enum Status {
        Ready, Error, Closed
    }

    public SimpleProducerWrapper(String url, String name, boolean isTopic) {
        this.url = url;
        this.name = name;
        this.isTopic = isTopic;
    }

    public void init() {
        try {
            // Create a ConnectionFactory
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);

            // Create a Connection
            connection = connectionFactory.createConnection();
            connection.start();

            // Create a Session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination (Topic or Queue)
            Destination destination = isTopic ? session.createTopic(name) : session.createQueue(name);

            // Create a MessageProducer from the Session to the Topic or Queue
            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            changeStatus(Status.Ready);
        } catch (Exception e) {
            System.err.println("Exception while initialising SimpleProducerWrapper(" + url + ", " + name + ", " + isTopic + ")");
            e.printStackTrace();
            changeStatus(Status.Error);
        }
    }

    public void close() {
        try {
            // Clean up
            session.close();
            connection.close();
            changeStatus(Status.Closed);
        } catch (Exception e) {
            System.err.println("Exception while closing SimpleProducerWrapper(" + url + ", " + name + ", " + isTopic + ")");
            e.printStackTrace();
            changeStatus(Status.Error);
        }
    }

    public TextMessage createTextMessage(String message) {
        try {
            return session.createTextMessage(message);
        } catch (Exception e) {
            System.err.println("Exception while creating a text message in SimpleProducerWrapper(" + url + ", " + name + ", " + isTopic + ")");
            e.printStackTrace();
            changeStatus(Status.Error);
            return null;
        }
    }

    public void sendMessage(Message message)  {
        try {
            producer.send(message);
            changeStatus(Status.Ready);
        } catch (JMSException e) {
            System.err.println("Exception while sending message (" + url + ", " + name + ", " + isTopic + ")");
            e.printStackTrace();
            changeStatus(Status.Error);
        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatusListener(StatusListener statusListener) {
        this.statusListener = statusListener;
    }

    private void changeStatus(Status status) {
        this.status = status;
        if (statusListener != null) {
            statusListener.onStatusChanged(status);
        }
    }

    public interface StatusListener {
        void onStatusChanged(Status status);
    }
}
