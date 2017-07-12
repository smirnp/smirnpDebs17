package smirnp.debs17;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.util.FileManager;
import smirnp.debs17.Clustering.Cluster;
import smirnp.debs17.Clustering.KMeans;

import java.util.*;

/**
 * Created by root on 7/8/17.
 */
public class Processor {

    private final int windowSize;
    private final int transitionsCount;

    private final int iterationsCount;

    private final Map<Integer, Integer> clustersPerPropertyId;
    private final Map<Integer, Double> probabilityThresholds;
    private final Map<Date, Integer> transitionsMap;
    private int[] observablePropertyIds;
    private int counter = 0;
    private Window window;
    private KMeans kmeans;
    private Markov markov;

    private Processor(ProcessorBuilder builder){
        windowSize = builder.windowSize;
        transitionsCount = builder.transitionsCount;
        iterationsCount = builder.iterationsCount;
        clustersPerPropertyId = new HashMap<>();
        probabilityThresholds = new HashMap<>();
        transitionsMap = new HashMap<Date, Integer>();
    }

    public void init(){

        Model model = FileManager.get().loadModel("data/1molding_machine/molding_machine_308dp.metadata.nt", null, "TURTLE");
        //Model model = FileManager.get().loadModel("data/1000molding_machine/1000molding_machine.metadata.nt", null, "TURTLE");
        StmtIterator iter = model.listStatements();
        try {
            while ( iter.hasNext() ) {
                Statement stmt = iter.next();

                Resource s = stmt.getSubject();
                Resource p = stmt.getPredicate();
                RDFNode o = stmt.getObject();

                if (p.getLocalName().equals("hasNumberOfClusters")){
                    String[] splitted = s.getLocalName().split("_");
                    int dimId = Integer.parseInt(splitted[splitted.length-1]);
                    if(dimId==103){
                        String test="123";
                    }
                    //String sensorId = s.getLocalName();
                    int value = ((LiteralImpl) o).getInt();
                    clustersPerPropertyId.put(dimId, value);
                }

                if (s.getLocalName().startsWith("ProbabilityThreshold") && p.getLocalName().startsWith("value")){
                    String[] splitted = s.getLocalName().split("_");
                    int dimId = Integer.parseInt(splitted[splitted.length-1]);
                    //String sensorId = s.getLocalName();
                    double value = ((LiteralImpl) o).getDouble();
                    probabilityThresholds.put(dimId, value);
                }
            }
        } finally {
            if ( iter != null ) iter.close();
        }

        observablePropertyIds = ArrayUtils.toPrimitive(clustersPerPropertyId.keySet().toArray(new Integer[clustersPerPropertyId.size()]));
        Arrays.sort(observablePropertyIds);

        window = new Window(windowSize, new ArrayList<>(clustersPerPropertyId.keySet()));
        String test="123";
    }


    public void processTuple(String str){
        counter++;

        System.out.println("Processing tuple: "+String.valueOf(counter));

        Tuple tuple = new Tuple(str);
        window.put(tuple);

        if(window.getActualTuplesCount()==windowSize) {
            processWindow(window);

            int windowTransitions = 0;
            //transitionsMap.put(window.getTupleTimestamp(), windowTransitions);
        }

    }

    private void extractTupleFromRDFString(String str){
        //Location location = Location.create ( "target/tdb" );
        //Dataset dataset = TDBFactory.createDataset ( location );
        //dataset.begin ( ReadWrite.WRITE );

//        try {
//            //DatasetGraph dsg = dataset.asDatasetGraph();
//            DatasetGraph dsg2 = RDFDataMgr.loadDatasetGraph("<http://example/org> <http://www.w3.org/2000/01/rdf-schema#label> \"Hello \n World!\" .", Lang.TURTLE );
//            //DatasetGraph dsg2 = RDFDataMgr.loadDatasetGraph(str, Lang.TURTLE );
//            Iterator<Quad> quads = dsg2.find();
//            while ( quads.hasNext() ) {
//                Quad quad = quads.next();
//                String test="1";
//                //dsg.add(quad);
//            }
//        } catch ( Exception e ) {
//            e.printStackTrace(System.err);
//            //dataset.abort();
//        } finally {
//            //dataset.end();
//        }
    }

    public void processWindow(Window window){

        double[][] dimentionedValues = window.getDimentionedValues();

        for(int propertyId : observablePropertyIds){
            int dimIndex = propertyId+1;

            if(clustersPerPropertyId.keySet().contains(propertyId)){ // processing only values, which have clusterCount defined

                int clustersCount = clustersPerPropertyId.get(propertyId);

                double[] dimWindowPoints = dimentionedValues[dimIndex];
                double[] firstKdistinctValues =  Arrays.stream(dimWindowPoints).skip(dimWindowPoints.length-window.getActualTuplesCount()).distinct().limit(clustersCount).toArray();

                double[] centroids = firstKdistinctValues;

                if (centroids != null){

                    kmeans = new KMeans(iterationsCount);
                    kmeans.createClusters(centroids);
                    kmeans.calculate(dimWindowPoints, Arrays.asList(ArrayUtils.toObject(firstKdistinctValues)));

                    int[] pointsToClusterAssignment = kmeans.getPointsToClusterAssignment();

                    markov = new Markov(clustersCount);
                    List<Map<Integer, Double>> probabilities = markov.calculate(pointsToClusterAssignment);

                    int startIndex = dimWindowPoints.length-transitionsCount;
                    int prevClusterId = pointsToClusterAssignment[startIndex-1];
                    double nTrasitionsProbability = 1.0;
                    for(int i = startIndex; i<dimWindowPoints.length; i++){
                        int clusterId = pointsToClusterAssignment[i];
                        double prob = markov.getProbability(prevClusterId, clusterId);
                        nTrasitionsProbability*=prob;
                        prevClusterId = clusterId;
                    }

                    if(nTrasitionsProbability<probabilityThresholds.get(propertyId)){
                        Date date = window.getTupleTimestamp(startIndex-1);
                        String alert = "123";
                    }
                    String test="123";
                }else{
                    String test="123";
                }

            }

            String test="123";
        }

    }



    public static class ProcessorBuilder {
        private int windowSize;
        private int transitionsCount;
        private int iterationsCount;
        private double sequenceProbability;

        public ProcessorBuilder windowSize(int val){
            windowSize = val;
            return this;
        }

        public ProcessorBuilder transisionsCount(int val){
            transitionsCount = val;
            return this;
        }

        public ProcessorBuilder iterationsCount(int val){
            iterationsCount = val;
            return this;
        }

        public Processor build(){
            return new Processor(this);

        }

    }
}
