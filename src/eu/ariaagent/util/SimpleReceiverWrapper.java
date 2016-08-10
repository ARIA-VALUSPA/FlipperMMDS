package eu.ariaagent.util;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Objects;

/**
 * Created by adg on 27/01/2016.
 *
 */
public class SimpleReceiverWrapper extends StatusWrapper implements ExceptionListener {

    private String url;
    private String name;
    private boolean isTopic;

    private IMessageReceiver messageReceiver;

    private boolean running;

    public SimpleReceiverWrapper(String url, String destination, boolean isTopic) {
        this.url = url;
        this.name = destination;
        this.isTopic = isTopic;
    }

    public void start(IMessageReceiver messageReceiver) {
        this.messageReceiver = Objects.requireNonNull(messageReceiver, "Null IMessageReceiver");
        running = true;

        Thread thread = new Thread(this::startInternal);
        thread.start();
    }

    public void stop() {
        running = false;
    }

    private void startInternal() {
        ActiveMQConnectionFactory connectionFactory =  new ActiveMQConnectionFactory(url);

        try {
            Connection connection = connectionFactory.createConnection();
            connection.start();

            connection.setExceptionListener(this);

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination destination = isTopic ? session.createTopic(name) : session.createQueue(name);

            MessageConsumer consumer = session.createConsumer(destination);

            changeStatus(Status.Ready);
            while (running) {
                Message message = consumer.receive(10000);
                if (message != null) {
                    messageReceiver.consume(message);
                }
            }
            changeStatus(Status.Closed);
        } catch (JMSException e) {
            changeStatus(Status.Error);
            onException(e);
        }
    }

    @Override
    public synchronized void onException(JMSException e) {
        changeStatus(Status.Error);
        e.printStackTrace();
    }

    public interface IMessageReceiver {
        void consume(Message message);
    }
}

