package smirnp.debs17.system;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractCommandReceivingComponent;
import org.hobbit.core.data.RabbitQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smirnp.debs17.Processor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;


public class SmirnpDebsSystem extends AbstractCommandReceivingComponent {
    public static final String TRANSITIONS_COUNT_INPUT_NAME = "http://www.debs2017.org/gc/transitionsCount";
    public static final String MAX_CLUSTER_ITERATIONS_INPUT_NAME = "http://www.debs2017.org/gc/maxClusterIterations";
    public static final String WINDOW_SIZE_INPUT_NAME = "http://www.debs2017.org/gc/windowSize";

    private static final Logger logger = LoggerFactory.getLogger(SmirnpDebsSystem.class);
    private static final String TERMINATION_MESSAGE = "~~Termination Message~~";
    private static final Charset CHARSET = Charset.forName("UTF-8");

    private final CountDownLatch startExecutionBarrier = new CountDownLatch(1);
    private final CountDownLatch terminationMessageBarrier = new CountDownLatch(1);
    private final Map<String, Object> parameters;

    private RabbitQueue inputQueue;
    private RabbitQueue outputQueue;

    private Processor processor;


    public SmirnpDebsSystem(Map<String, Object> _parameters){
        parameters = _parameters;
    }

    @Override
    public void init() throws Exception {
        logger.debug("Initializing...");
        super.init();
        String hobbitSessionId = getHobbitSessionId();
        if (hobbitSessionId.equals(Constants.HOBBIT_SESSION_ID_FOR_BROADCASTS) ||
                hobbitSessionId.equals(Constants.HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS)) {
            throw new IllegalStateException("Wrong hobbit session id. It must not be equal to HOBBIT_SESSION_ID_FOR_BROADCASTS or HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS");
        }
        initCommunications();

        processor = new Processor.ProcessorBuilder()
                .windowSize((int)parameters.get(WINDOW_SIZE_INPUT_NAME))
                .iterationsCount((int)parameters.get(MAX_CLUSTER_ITERATIONS_INPUT_NAME))
                .transisionsCount((int)parameters.get(TRANSITIONS_COUNT_INPUT_NAME))
                .metadataFilePath("data/1000molding_machine.metadata.nt")
                //.metadataFilePath("molding_machine_308dp.metadata.nt")
                .build();
        processor.init();

        logger.debug("Initialized");
    }

    private void initCommunications() throws Exception {
        outputQueue = createQueueWithName(getOutputQueueName());
        inputQueue = createQueueWithName(getInputQueueName());
        registerConsumerFor(inputQueue);
    }

    private RabbitQueue createQueueWithName(String name) throws IOException, TimeoutException {
        Channel channel = createConnection().createChannel();
        channel.basicQos(getPrefetchCount());
        channel.queueDeclare(name, false, false, true, null);
        return new RabbitQueue(channel, name);
    }

    protected Connection createConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(getHost());
        return factory.newConnection();
    }

    private void registerConsumerFor(RabbitQueue queue) throws IOException {
        Channel channel = queue.getChannel();
        channel.basicConsume(queue.getName(), false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                getChannel().basicAck(envelope.getDeliveryTag(), false);
                SmirnpDebsSystem.this.handleDelivery(body);
            }
        });
    }

    private String getHost() {
        return System.getenv().get(Constants.RABBIT_MQ_HOST_NAME_KEY);
    }

    private int getPrefetchCount() {
        return 1;
    }

    private String getInputQueueName() {
        return toPlatformQueueName(Constants.DATA_GEN_2_SYSTEM_QUEUE_NAME);
    }

    private static String toPlatformQueueName(String queueName) {
        return queueName + "." + System.getenv().get(Constants.HOBBIT_SESSION_ID_KEY);
    }

    private String getOutputQueueName() {
        return toPlatformQueueName(Constants.SYSTEM_2_EVAL_STORAGE_QUEUE_NAME);
    }

    @Override
    public void run() throws Exception {
        logger.debug("Sending SYSTEM_READY_SIGNAL...");
        sendToCmdQueue(Commands.SYSTEM_READY_SIGNAL);   // Notifies PlatformController that it is ready to start
        logger.debug("Waiting for TASK_GENERATION_FINISHED...");
        startExecutionBarrier.await();
        logger.debug("Starting system execution...");
        execute();
        logger.debug("Finished");
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        if (command == Commands.TASK_GENERATION_FINISHED) {
            startExecutionBarrier.countDown();
        }
    }

    /**
     * This is where system execution starts when it receives {@code Commands.TASK_GENERATION_FINISHED}
     * from the PlatformController. Since all the processing done upon receiving a message in {@link #handleDelivery(byte[])}
     * this method is just blocked.
     */
    private void execute() throws Exception {
        try {
            logger.debug("Waiting for termination message...");
            terminationMessageBarrier.await();
            logger.debug("Sending termination message...");
            sendTerminationMessage();
        } catch (Exception e) {
            logger.error("Exception", e);
        }
        logger.debug("Execution finished.");
    }

    private void sendTerminationMessage() throws Exception {
        logger.debug("Sending termination message to: {} sender: {}", outputQueue.getName(), this);
        send(TERMINATION_MESSAGE);
    }

    private void send(String string) throws IOException {
        send(string.getBytes(CHARSET));
    }

    private void send(byte[] bytes) throws IOException {
        Channel channel = outputQueue.getChannel();
        channel.basicPublish("", outputQueue.getName(), MessageProperties.PERSISTENT_BASIC, bytes);
    }

    private void handleDelivery(byte[] bytes) {
        try {
            String message = new String(bytes, CHARSET);
            if (TERMINATION_MESSAGE.equals(message)) {
                logger.debug("Got termination message");
                terminationMessageBarrier.countDown();
            } else {
                List<byte[]> anomalies = processor.processTupleInBytes(bytes);
                for (byte[] anomaly : anomalies)
                    send(anomaly);
                logger.debug("Repeating message: {}", message);
                //send(bytes);
            }
        } catch (Exception e) {
            logger.error("Exception", e);
        }
    }



    @Override
    public void close() throws IOException {
        super.close();
        try {
            Channel channel = inputQueue.getChannel();
            Connection connection = channel.getConnection();
            channel.close();
            connection.close();
            channel = outputQueue.getChannel();
            connection = channel.getConnection();
            channel.close();
            connection.close();
        } catch (TimeoutException e) {
            logger.debug("Exception", e);
        }
    }
}