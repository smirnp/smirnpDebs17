package smirnp.debs17;

import com.agtinternational.hobbit.deployment.sml.SMLBenchmarkDockerBuilder;
import com.agtinternational.hobbit.sdk.JenaKeyValue;
import com.agtinternational.hobbit.sdk.KeyValue;
import com.agtinternational.hobbit.sdk.docker.Dockerizer;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.hobbit.core.Commands;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

import static com.agtinternational.hobbit.common.SMLConstants.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.agtinternational.hobbit.common.*;
import com.agtinternational.hobbit.sdk.ComponentsExecutor;
import com.agtinternational.hobbit.sdk.docker.RabbitMqDockerizer;
import com.agtinternational.hobbit.sdk.utils.CommandQueueListener;
import com.agtinternational.hobbit.sdk.utils.commandreactions.StartBenchmarkWhenSystemAndBenchmarkReady;
import com.agtinternational.hobbit.sdk.utils.commandreactions.TerminateServicesWhenBenchmarkControllerFinished;
import static com.agtinternational.hobbit.sdk.CommonConstants.*;

import smirnp.debs17.system.SmirnpSystemDockerizer;

/**
 * @author Roman Katerinenko
 */
@RunWith(Parameterized.class)
public class SMLBenchmarkTest extends EnvironmentVariables {
    private static final String EXPERIMENT_URI = "http://agt.com/exp1";
    private static final String SYSTEM_URI = "http://agt.com/systems#sys10";
    private static final String FAKE_SYSTEM_URI = "http://example.com/fakeSystemId";
    private static final String SESSION_ID = EXPERIMENT_URI;
    //private static final String RABBIT_HOST_NAME = "127.0.0.1";
    private static final String RABBIT_HOST_NAME = "172.19.0.1";
    private static final String SYSTEM_CONTAINER_ID = "anythingGoesHere-weDontCheck";

    private enum SystemType {
        POSITIVE,
        NEGATIVE,
        Smirnp
    }

    private int benchmarkOutputFormat;
    private boolean testShouldPass;
    private SystemType systemType;

    @Parameterized.Parameters
    public static Collection parameters() throws Exception {
        return Arrays.asList(new Object[][]{
                //{FORMAT_CSV, false, SystemType.NEGATIVE}, // 2 anomalies - fail
                //{FORMAT_CSV, true, SystemType.POSITIVE},  // 3 anomalies - success
                //{FORMAT_RDF, false, SystemType.POSITIVE}  // 0 anomalies - fail
                {FORMAT_RDF, false, SystemType.Smirnp}
        });
    }

    public SMLBenchmarkTest(int benchmarkOutputFormat, boolean testShouldPass, SystemType systemType) {
        this.benchmarkOutputFormat = benchmarkOutputFormat;
        this.testShouldPass = testShouldPass;
        this.systemType = systemType;
    }

    @Test
    public void checkAnomaliesMatch() throws InterruptedException, TimeoutException, DockerCertificateException, DockerException {

        RabbitMqDockerizer rabbitMqDockerizer = RabbitMqDockerizer.builder()
                .host(RABBIT_HOST_NAME)
                .containerName(RABBIT_MQ_CONTAINER_NAME)
                .build();
        rabbitMqDockerizer.run();
        rabbitMqDockerizer.waitUntilRunning();

        setupCommunicationEnvironmentVariables(RABBIT_HOST_NAME, SESSION_ID);
        setupBenchmarkEnvironmentVariables(EXPERIMENT_URI);
        setupSystemEnvironmentVariables(SYSTEM_URI);
        ComponentsExecutor componentsExecutor = new ComponentsExecutor();
        CommandQueueListener commandQueue = new CommandQueueListener();
        commandQueue.setCommandReactions(
                new TerminateServicesWhenBenchmarkControllerFinished(commandQueue, componentsExecutor) {
                    @Override
                    public void accept(Byte command, byte[] data) {
                        if (command == Commands.BENCHMARK_FINISHED_SIGNAL){
                            SMLBenchmarkTest.this.checkBenchmarkResult(data);
                            //correct.set(checkReceivedModel(modelBytes));
                        }
                        super.accept(command, data);
                    }
                },
                new StartBenchmarkWhenSystemAndBenchmarkReady(SYSTEM_CONTAINER_ID));
        componentsExecutor.submit(commandQueue);
        commandQueue.waitForInitialisation();


        //KeyValue inputParams = createTaskParameters();
        //BenchmarkTask task = new SMLTask(inputParams);
        //int timeout = inputParams.getIntValueFor(SMLConstants.TIMEOUT_MINUTES_INPUT_NAME);

//        componentsExecutor.submit(new TaskBasedBenchmarkController(timeout, task));
//        executor.submit(new ContainerSimulatedComponent(newSystem(), SYSTEM_CONTAINER_ID));

        Dockerizer benchmarkDockerizer = newBenchmarkDockerizer();
        componentsExecutor.submit(benchmarkDockerizer);

        //Dockerizer systemDockerizer = newSystemDockerizer();
        //componentsExecutor.submit(systemDockerizer);

        //
        commandQueue.waitForTermination();
        assertFalse(componentsExecutor.anyExceptions());

        //assertTrue(task.isSuccessful() == testShouldPass);
        //Assert.assertTrue(correct.get());
        rabbitMqDockerizer.stopAndRemoveContainer();
    }

//    private AbstractCommandReceivingComponent newSystem() {
//        KeyValue systemParameters = createSystemParameters();
//        if (systemType == SystemType.POSITIVE)
//            return new SMLCsvSystem(systemParameters);
//        if (systemType == SystemType.POSITIVE)
//            return new SMLCsvSystemNegative(systemParameters);
//        if (systemType == SystemType.Smirnp)
//            return new SmirnpDebsSystem(systemParameters.toMap());
//        return null;
//    }

    private void checkBenchmarkResult(byte[] bytes) {
        JenaKeyValue keyValue = new JenaKeyValue.Builder().buildFrom(bytes);
        String matchResult = keyValue.getStringValueFor(SMLConstants.ANOMALY_MATCH_OUTPUT_NAME);
        assertTrue(SMLConstants.ANOMALY_MATCH_SUCCESS.equals(matchResult) == testShouldPass);
        int matchedDataPoints = keyValue.getIntValueFor(SMLConstants.ANOMALY_MATCH_COUNT_OUTPUT_NAME);
        assertTrue((SMLConstants.EXPECTED_ANOMALIES_COUNT == matchedDataPoints) == testShouldPass);
        double throughput = keyValue.getDoubleValueFor(SMLConstants.THROUGHPUT_BYTES_PER_SEC_OUTPUT_NAME);
        assertTrue(Double.compare(throughput, .0) >= 0);
        String actualTermination = keyValue.getStringValueFor(SMLConstants.TERMINATION_TYPE_OUTPUT_NAME);
        Assert.assertEquals(TERMINATION_TYPE_NORMAL, actualTermination);
    }



    private KeyValue createTaskParameters(){

        KeyValue kv = new KeyValue();
        kv.setValue(SMLConstants.BENCHMARK_MODE_INPUT_NAME, BENCHMARK_MODE_STATIC);
        //kv.setValue(SMLConstants.BENCHMARK_MODE_INPUT_NAME, BENCHMARK_MODE_DYNAMIC+":10:1");
        kv.setValue(SMLConstants.TIMEOUT_MINUTES_INPUT_NAME, -1);
        kv.setValue(SMLConstants.DATA_POINT_COUNT_INPUT_NAME, 63);
        kv.setValue(SMLConstants.MACHINE_COUNT_INPUT_NAME, 1);
        kv.setValue(SMLConstants.PROBABILITY_THRESHOLD_INPUT_NAME, 0.05);
        kv.setValue(SMLConstants.WINDOW_SIZE_INPUT_NAME, 10);
        kv.setValue(SMLConstants.TRANSITIONS_COUNT_INPUT_NAME, 5);
        kv.setValue(SMLConstants.MAX_CLUSTER_ITERATIONS_INPUT_NAME, 50);
        kv.setValue(SMLConstants.INTERVAL_NANOS_INPUT_NAME, 10);
        kv.setValue(SMLConstants.SEED_INPUT_NAME, 123);
        kv.setValue(SMLConstants.FORMAT_INPUT_NAME, benchmarkOutputFormat);


        return kv;
    }

    private static JenaKeyValue createSystemParameters() {
        JenaKeyValue kv = new JenaKeyValue();
        kv.setValue(SMLConstants.MACHINE_COUNT_INPUT_NAME, SMLConstants.MACHINE_COUNT_DEFAULT);
        kv.setValue(SMLConstants.PROBABILITY_THRESHOLD_INPUT_NAME, SMLConstants.PROBABILITY_THRESHOLD_DEFAULT);
        kv.setValue(SMLConstants.WINDOW_SIZE_INPUT_NAME, SMLConstants.WINDOW_SIZE_DEFAULT);
        kv.setValue(SMLConstants.TRANSITIONS_COUNT_INPUT_NAME, SMLConstants.TRANSITIONS_COUNT_DEFAULT);
        kv.setValue(SMLConstants.MAX_CLUSTER_ITERATIONS_INPUT_NAME, SMLConstants.MAX_CLUSTER_ITERATIONS_DEFAULT);
        return kv;
    }

    private Dockerizer newBenchmarkDockerizer() {

            return new SMLBenchmarkDockerBuilder()
                    .benchmarkOutputFormat(benchmarkOutputFormat)
                    .hobbitSessionId(SESSION_ID)
                    .hobbitSessionId(SESSION_ID)
                    .systemUri(FAKE_SYSTEM_URI)
                    .build();

    }

    private Dockerizer newSystemDockerizer() {
        return new SmirnpSystemDockerizer()
                .parameters(createSystemParameters().encodeToString())
                .hobbitSessionId(SESSION_ID)
                .systemUri(FAKE_SYSTEM_URI)
                .build();

    }
}