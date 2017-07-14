//package smirnp.debs17;
//
//import java.io.IOException;
//import java.util.List;
//
//import org.apache.jena.rdf.model.*;
//import org.apache.commons.io.IOUtils;
//import org.hobbit.core.Constants;
//import org.hobbit.core.components.AbstractComponent;
//import org.hobbit.core.data.RabbitQueue;
//import org.hobbit.core.rabbit.RabbitMQUtils;
//import org.hobbit.storage.client.StorageServiceClient;
//import org.hobbit.storage.queries.SparqlQueries;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.rabbitmq.client.QueueingConsumer;
//
///**
// * This class implements the functionality for the Analysis Component
// * TODO:: !!REFACTOR INTO A MORE GENERIC DESIGN!!
// */
//public class SmirnpSystemComponent extends AbstractComponent {
//    private static final Logger LOGGER = LoggerFactory.getLogger(SmirnpSystemComponent.class);
//    private static final String GRAPH_URI = Constants.PUBLIC_RESULT_GRAPH_URI;
//    protected RabbitQueue controller2AnalysisQueue;
//    //    protected RabbitQueue analysisQueue;
//    protected QueueingConsumer consumer;
//
//    private Model experimentModel = null;
//    private StorageServiceClient storage;
//
//
//    @Override
//    public void init() throws Exception {
//        super.init();
//
//        LOGGER.debug("Analysis Component Initialized!");
//    }
//
//    @Override
//    public void run() throws Exception {
//        LOGGER.info("Awaiting requests");
//        //QueueingConsumer.Delivery delivery;
////        while (true){
////            delivery = consumer.nextDelivery();
////            AnalysisModel analysis;
////            Model updatedModel = null;
////            if (delivery != null) {
////                LOGGER.info("Received a request. Processing...");
////                String expUri = RabbitMQUtils.readString(delivery.getBody());
////                try{
////                    //retrieve data from storage for the specific experiment Uri
////                    LOGGER.info("Retrieving Data...");
////                    experimentModel = storage.sendConstructQuery(SparqlQueries.getExperimentGraphQuery(expUri, null));
////                    //analyse the experiment
////                    analysis = analyseExperiment(experimentModel, expUri);
////                    //get analysed model
////                    updatedModel = analysis.getUpdatedModel();
////                    System.out.println(updatedModel);
////
////                } catch (Exception e) {
////                    LOGGER.error("Error: " + e.toString());
////                }
////                if (updatedModel != null) {
////                    try {
////                        String sparqlUpdateQuery = null;
////                        //TODO:: handle null exception for sparql queries
////                        sparqlUpdateQuery = SparqlQueries.getUpdateQueryFromDiff(experimentModel,
////                                updatedModel,
////                                GRAPH_URI);
////                        LOGGER.info("Updating model...");
////                        storage.sendUpdateQuery(sparqlUpdateQuery);
////                    } catch (Exception e) {
////                        LOGGER.error("Error: " + e.toString());
////                    }
////                } else {
////                    LOGGER.error("No result model from the analysis.");
////                }
////            }
////        }
//
//    }
//
//    @Override
//    public void close() throws IOException {
//        //IOUtils.closeQuietly(controller2AnalysisQueue);
////        IOUtils.closeQuietly(analysisQueue);
//        //IOUtils.closeQuietly(storage);
//        super.close();
//    }
//
//
//}
