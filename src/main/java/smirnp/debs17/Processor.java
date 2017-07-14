package smirnp.debs17;

import com.google.common.io.Resources;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.util.FileManager;
import smirnp.debs17.Clustering.KMeans;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private final SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final Pattern observIds = Pattern.compile("WeidmullerMetadata#_([^>]+)>");
    private final Pattern observValues  = Pattern.compile("\"([^\"]+)\"");
    private int[] observablePropertyIds;
    private String anomalyTemplate;
    private int counter = 0;
    private int anomalies = 0;
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
        //getClass().getClassLoader().getResource(metadataFilePath).getFile()


        try {
//            URL url  = Resources.getResource("anomaly.nt");
//            anomalyTemplate = Resources.toString(url, CHARSET).replace("{","${");
            anomalyTemplate = new String(Files.readAllBytes(Paths.get("data", "anomaly.nt")), CHARSET).replace("{","${");
        } catch (IOException e) {
            //e.printStackTrace();
        }

//        FileManager.get().addLocatorClassLoader(getClass().getClassLoader());
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

    public List<byte[]> processTupleInBytes(byte[] data){
        return processTuple(new String(data));
    }

    public List<byte[]> processTuple(String str){
        if(str.contains("http://"))
            str = extractTupleFromRDFString(str);
        Tuple tuple = new Tuple(counter, str);
        window.put(tuple);
        counter++;

        if(window.getActualTuplesCount()==windowSize) {
            return processWindow(window);
        }

        return new ArrayList<byte[]>();
    }


    private String extractTupleFromRDFString(String str){

        List<String[]> observationsIds = new ArrayList<>();
        List<String> observationsValues = new ArrayList<>();

        try {


            Matcher m = observIds.matcher(str);

            while (m.find()) {
                String val = m.group(1);
                String[] splitted = val.split("_");
                observationsIds.add(new String[]{splitted[splitted.length - 2], splitted[splitted.length - 1]});
            }
        }
        catch (Exception e){
            System.out.println(str);
        }

        try{
            Matcher m2 = observValues.matcher(str);

            int index = 0;
            String skipped = "";
            while (m2.find()) {
                String val = m2.group(1);
                if (index == 0) skipped = val;
                else if (index == 1) {
                    val = val.replace("T", ", ").replace("-", "");
                    if(val.indexOf("+")>0)
                        val = val.substring(0, val.indexOf("+"));
                    observationsValues.add(val);
                    observationsValues.add(skipped);
                } else
                    observationsValues.add(val);
                index++;
            }
        }
        catch (Exception e){
            System.out.println(str);
        }

        String ret = observationsIds.get(0)[0] + ", " + String.join(", ", observationsValues);
        return ret;




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

    }

    public List<byte[]> processWindow(Window window){
        List<byte[]> ret = new ArrayList<>();
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
                    Double nTrasitionsProbability = 1.0;
                    for(int i = startIndex; i<dimWindowPoints.length; i++){
                        int clusterId = pointsToClusterAssignment[i];
                        double prob = markov.getProbability(prevClusterId, clusterId);
                        nTrasitionsProbability*=prob;
                        prevClusterId = clusterId;
                    }

                    if(nTrasitionsProbability<probabilityThresholds.get(propertyId)){

                        Tuple tuple = window.getTuple(startIndex-1);
                        LocalDateTime localDateTime = tuple.getLocalDateTime();

                        System.out.println("Anomaly "+String.valueOf(nTrasitionsProbability)+" at "+localDateTime.toString()+" (Dim="+propertyId+" Timestamp="+tuple.getId()+")");
                        Map valuesMap = new HashMap();
                        valuesMap.put("anomaly_counter", anomalies);
                        valuesMap.put("machine_uri", String.format("http://www.agtinternational.com/ontologies/WeidmullerMetadata#Machine_%d",tuple.getMachineId()));
                        valuesMap.put("observed_property_uri", String.format("http://www.agtinternational.com/ontologies/WeidmullerMetadata#_%d_%d",tuple.getMachineId(), dimIndex));
                        valuesMap.put("timestamp_uri", String.format("http://project-hobbit.eu/resources/debs2017#Timestamp_%d", tuple.getId()));
                        valuesMap.put("probability_value", String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#double>", String.valueOf(nTrasitionsProbability)));
                        //valuesMap.put("probability_value", DatatypeConverter.printDouble(nTrasitionsProbability)+"^^<http://www.w3.org/2001/XMLSchema#double>");
                        //valuesMap.put("probability_value", String.format("%d^^<http://www.w3.org/2001/XMLSchema#double>", new Double(nTrasitionsProbability)));

                        StrSubstitutor sub = new StrSubstitutor(valuesMap);
                        String anomalyRDF = sub.replace(anomalyTemplate);
                        //anomalyRDF = anomalyRDF.replace("${probability_value}", "\"" + DatatypeConverter.printDouble(nTrasitionsProbability) + "\"^^<http://www.w3.org/2001/XMLSchema#double>");
                        ret.add(anomalyRDF.getBytes(CHARSET));
                        anomalies++;
                        String alert = "123";

                    }
                    String test="123";
                }else{
                    String test="123";
                }

            }

            String test="123";
        }
        return ret;
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
