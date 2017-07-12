package smirnp.debs17.Clustering;

import java.util.*;


public class KMeans {

    //Number of Clusters. This metric should be related to the number of points
    //private int NUM_CLUSTERS = 3;
    //Number of Points
    private final int iteratonsCount;
    //Min and Max X and Y
    private static final int MIN_COORDINATE = 0;
    private static final int MAX_COORDINATE = 10;

    //private List<Point> points;
    private List<Cluster> clusters;
    private int[] pointsToClusterAssignment;

    public KMeans(int iteratons) {

        //this.points = new ArrayList();
        this.clusters = new ArrayList();
        iteratonsCount = iteratons;
    }

//    public static void main(String[] args) {
//
//        KMeans kmeans = new KMeans();
//        kmeans.init();
//        kmeans.calculate();
//    }



//    private void plotClusters() {
//        for (int i = 0; i < NUM_CLUSTERS; i++) {
//            Cluster c = clusters.get(i);
//            c.plotCluster();
//        }
//    }

    public void createClusters(double[] centroids){
        //Create Clusters
        //Set Random Centroids
        clusters.clear();
        for (int i = 0; i < centroids.length; i++) {
            Double centroid = centroids[i];

            Cluster cluster = new Cluster(i);



            cluster.setCentroid(centroid);
            clusters.add(cluster);
        }

        //Print Initial state
        //plotClusters();
    }

    //The processTuple to calculate the K Means, with iterating method.
    public void calculate(double[] points, List<Double> centroidAffectingPoints){
        boolean finish = false;
        int iteration = 0;
        double distance = 0.0;

        // Add in new data, one at a time, recalculating centroids with each new one.
        while(!finish) {
            //Clear cluster state
            clearClusters();

            double[] lastCentroids = getCentroids();

            //Assign points to the closer cluster
            assignCluster(points);

            //Calculate new centroids.
            calculateCentroids(centroidAffectingPoints);

            iteration++;

            double[] currentCentroids = getCentroids();

            //Calculates total distance between new and old Centroids
            distance = 0;
            for(int i = 0; i < lastCentroids.length; i++) {
                distance += Math.abs(currentCentroids[i]-lastCentroids[i]); //Point.distance((Point)lastCentroids[i], (Point)currentCentroids[i]);
            }
            //plotClusters();

            if(distance == 0 || iteration>=iteratonsCount) {
                finish = true;
            }
        }

        pointsToClusterAssignment = new int[points.length];
        for(int i=0; i<points.length; i++)
            pointsToClusterAssignment[i] = findClusterId(points[i]);


        //System.out.println("   Iterations done: " + iteration+" final distance: "+String.valueOf(distance));
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public int findClusterId(double point){
        for (Cluster cluster : clusters)
            if (cluster.points.contains(point))
                return cluster.getId();
        return -1;
    }

    public int[] getPointsToClusterAssignment(){
        return pointsToClusterAssignment;
    }

    private void clearClusters() {
        for(Cluster cluster : clusters) {
            cluster.clear();
        }
    }

    public double[] getCentroids() {
        double[] ret = new double[clusters.size()];
        int ind=0;

        for(Cluster cluster : clusters){
            ret[ind] = cluster.getCentroid();
            ind++;
        }
        return ret;
    }

//    public void setCentroids(Point[] centroids) {
//
//        int ind=0;
//        for(Cluster cluster : clusters){
//            cluster.setCentroid(centroids[ind]);
//            ind++;
//        }
//    }

    private void assignCluster(double[] points) {
        double max = Double.MAX_VALUE;
        double min = max;
        int clusterInd = 0;
        double distance = 0.0;

        for(int j=0; j<points.length; j++){
            //if(points[j]!=null){
                Double point = points[j];
                min = max;
                for(int i = 0; i < clusters.size(); i++) {
                    Cluster c = clusters.get(i);
                    Double centroid = c.getCentroid();
                    distance = Math.abs(centroid - point);// Point.distance(point, c.getCentroid());
                    if (distance < min) {
                        min = distance;
                        clusterInd = i;
                    }
                }
                Cluster cluster = clusters.get(clusterInd);
                cluster.addPoint(point);
            }
        String test="123";
    }

    private void calculateCentroids(List<Double> centroidAffectingPoints){
        for(Cluster cluster : clusters) {
            double sumX = 0;
            //double sumY = 0;
            List<Double> list = cluster.getPoints();
            int n_points = 0;
            for(Double point : list)
                if (centroidAffectingPoints.contains(point)){
                    sumX += point;
                    n_points++;
                }

            if (n_points>0){
                double newX = sumX / n_points;
                cluster.centroid = newX;
            }else{
                String test="123";
            }

        }
    }
}