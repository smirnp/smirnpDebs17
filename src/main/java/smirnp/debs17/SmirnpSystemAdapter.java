//package smirnp.debs17;
//
//
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.util.List;
//
//import com.agt.ferromatikdata.anomalydetector.PlainAnomaly;
//import com.agt.ferromatikdata.anomalydetector.WithinMachineAnomaly;
//import com.agt.ferromatikdata.formatting.RdfAnomalyFormatter;
//import org.hobbit.core.components.AbstractSystemAdapter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class SmirnpSystemAdapter extends AbstractSystemAdapter {
//
//    private static Logger LOGGER = LoggerFactory.getLogger(SmirnpSystemAdapter.class);
//    private static final Charset CHARSET = Charset.forName("UTF-8");
//    private Processor processor;
//
//    public void init() throws Exception {
//        LOGGER.info("Initializing ExampleQF test system...");
//        super.init();
//        LOGGER.info("ExampleQF initialized successfully .");
//
//        processor = new Processor.ProcessorBuilder()
//                .windowSize(10)
//                .iterationsCount(100)
//                .transisionsCount(5)
//                .metadataFilePath("1molding_machine/molding_machine_308dp.metadata.nt")
//                .build();
//        processor.init();
//
//
//        // Your initialization code comes here...
//        // You can access the RDF model this.systemParamModel to retrieve meta data about this system adapter
//    }
//
//    /**
//     * You MIGHT need this, depends on the benchmark.
//     * @see <a href="https://project-hobbit.eu/challenges">Challenges and their Benchmarks</a>
//     */
//    public void receiveGeneratedData(byte[] data) {
//        LOGGER.trace("receiveGeneratedData()");
//    }
//
//
//    public void receiveGeneratedTask(String taskId, byte[] data) {
//        LOGGER.trace("receiveGeneratedTask: "+taskId.toString());
//        List<byte[]> anomalies = processor.processTupleInBytes(data);
//        for(int i=0; i<anomalies.length; i++)
//            sendAnomaly(taskId, anomalies[i]);
//    }
//
//    protected void sendAnomaly(String taskId, PlainAnomaly _anomaly){
//        LOGGER.info("Sending anomaly: "+_anomaly.toString());
//
//        com.agt.ferromatikdata.anomalydetector.WithinMachineAnomaly anomaly = new com.agt.ferromatikdata.anomalydetector.WithinMachineAnomaly(_anomaly);
//        try {
//            RdfAnomalyFormatter f = new RdfAnomalyFormatter(CHARSET);
//            f.init();
//            String string = f.format(anomaly);
//            sendResultToEvalStorage(taskId, string.getBytes(CHARSET));
//        } catch (Exception e) {
//            LOGGER.error("Exception", e);
//        }
//    }
//
//    @Override
//    public void close() throws IOException {
//        LOGGER.info("close()");
//        super.close();
//    }
//
//
//}
