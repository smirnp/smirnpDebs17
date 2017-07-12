package smirnp.debs17;

import com.agt.ferromatikdata.anomalydetector.PlainAnomaly;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.core.RDFParser;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.util.FileManager;
import org.apache.xerces.xni.parser.XMLInputSource;
import smirnp.debs17.Clustering.Cluster;
import smirnp.debs17.Clustering.KMeans;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by root on 7/8/17.
 */
public class Processor {

    private final int windowSize;
    private final int transitionsCount;

    private final int iterationsCount;
    private final String metadataFilePath;
    private final Map<Integer, Integer> clustersPerPropertyId;
    private final Map<Integer, Double> probabilityThresholds;
    private final SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final Pattern observIds = Pattern.compile("(WeidmullerMetadata#_[^>]+>)");
    private final Pattern observValues  = Pattern.compile("\"([^\"]+)\"");
    private int[] observablePropertyIds;
    private int counter = 0;
    private Window window;
    private KMeans kmeans;
    private Markov markov;

    private Processor(ProcessorBuilder builder){
        windowSize = builder.windowSize;
        transitionsCount = builder.transitionsCount;
        iterationsCount = builder.iterationsCount;
        metadataFilePath = builder.metadataFilePath;
        clustersPerPropertyId = new HashMap<>();
        probabilityThresholds = new HashMap<>();
    }

    public void init(){

        FileManager.get().addLocatorClassLoader(getClass().getClassLoader());

        Model model = FileManager.get().loadModel(metadataFilePath, null, "TURTLE");
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

    public PlainAnomaly[] processTupleInBytes(byte[] data){
        return processTuple(new String(data));
    }

    public PlainAnomaly[] processTuple(String str){
        if(str.contains("http://"))
            str = extractTupleFromRDFString(str);
        Tuple tuple = new Tuple(counter, str);
        window.put(tuple);

        if(window.getActualTuplesCount()==windowSize) {
            return processWindow(window);
        }
        counter++;
        return new PlainAnomaly[0];
    }

    private String extractTupleFromRDFString(String str){


        Matcher m = observIds.matcher(str);
        List<String[]> observationsIds = new ArrayList<>();
        while (m.find()) {
            String[] splitted = m.group(0).split("_");
            observationsIds.add(new String[]{ splitted[splitted.length-2], splitted[splitted.length-1] });
        }


        Matcher m2 = observValues.matcher(str);
        List<String> observationsValues = new ArrayList<>();
        int index=0;
        String skipped="";
        while (m2.find()) {
            String val = m2.group(1);
            if(index==0)skipped=val;
            else if(index==1){
                val = val.replace("T",", ").replace("-","");
                val = val.substring(0, val.indexOf("+"));
                observationsValues.add(val);
                observationsValues.add(skipped);
            }else
                observationsValues.add(val);
            index++;
        }

        String ret = observationsIds.get(0)[0]+", "+String.join(", ", observationsValues);
        String abc="123";

//        String ret = "";
//        //Model model0 = FileManager.get().loadModel("/mnt/share133/smirnp/smirnpDebs17/src/main/resources/i40.ttl", null, "TURTLE");
//        Model model = ModelFactory.createDefaultModel();
//        String str1 = "<http://project-hobbit.eu/resources/debs2017#ObservationGroup_0><http://www.w3.org/1999/02/22-rdf-syntax-ns#type><http://www.agtinternational.com/ontologies/I4.0#MoldingMachineObservationGroup>";
//        model.read(new ByteArrayInputStream(str1.getBytes()), "http://project-hobbit.eu");
//
//        StmtIterator iter = model.listStatements();
//        try {
//            while ( iter.hasNext() ) {
//                Statement stmt = iter.next();
//
//                Resource s = stmt.getSubject();
//                Resource p = stmt.getPredicate();
//                RDFNode o = stmt.getObject();
//
//                if (p.getLocalName().equals("hasNumberOfClusters")){
//                    String[] splitted = s.getLocalName().split("_");
//                    int dimId = Integer.parseInt(splitted[splitted.length-1]);
//                    //String sensorId = s.getLocalName();
//                    int value = ((LiteralImpl) o).getInt();
//                    clustersPerPropertyId.put(dimId, value);
//                }
//
//                if (s.getLocalName().startsWith("ProbabilityThreshold") && p.getLocalName().startsWith("value")){
//                    String[] splitted = s.getLocalName().split("_");
//                    int dimId = Integer.parseInt(splitted[splitted.length-1]);
//                    //String sensorId = s.getLocalName();
//                    double value = ((LiteralImpl) o).getDouble();
//                    probabilityThresholds.put(dimId, value);
//                }
//            }
//        } finally {
//            if ( iter != null ) iter.close();
//        }
        return ret;
    }

    public PlainAnomaly[] processWindow(Window window){
        List<PlainAnomaly> ret = new ArrayList<>();
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
                        final double nTrasitionsProbabilityFinalized = nTrasitionsProbability;
                        Tuple tuple = window.getTuple(startIndex-1);
                        Date date = tuple.getTimestamp();
                        ret.add(new PlainAnomaly(){{ setDataPointIndex(tuple.getId()); setDimensionId(propertyId); setProbability(nTrasitionsProbabilityFinalized); }});
                        String alert = "123";
                        System.out.println("Anomaly "+String.valueOf(nTrasitionsProbability)+" at "+date.toString()+" (Dim="+propertyId+" Timestamp="+tuple.getId()+")");
                    }
                    String test="123";
                }else{
                    String test="123";
                }

            }

            String test="123";
        }
        return ret.toArray(new PlainAnomaly[ret.size()]);
    }

    public static class ProcessorBuilder {
        private int windowSize;
        private int transitionsCount;
        private int iterationsCount;
        private double sequenceProbability;
        private String metadataFilePath;

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

        public ProcessorBuilder metadataFilePath(String val){
            metadataFilePath = val;
            return this;
        }

        public Processor build(){
            return new Processor(this);

        }

    }
}
