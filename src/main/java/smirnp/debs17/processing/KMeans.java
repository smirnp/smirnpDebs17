package smirnp.debs17.processing;

import java.util.*;


public class KMeans {


    private final int iteratonsCount;
    private static final int MIN_COORDINATE = 0;
    private static final int MAX_COORDINATE = 10;

    private List<Cluster> clusters;
    private int[] pointsToClusterAssignment;

    public KMeans(int iteratons) {
        this.clusters = new ArrayList();
        iteratonsCount = iteratons;
    }

    public void createClusters(double[] centroids){
        clusters.clear();
        for (int i = 0; i < centroids.length; i++) {
            Double centroid = centroids[i];
            Cluster cluster = new Cluster(i);
            cluster.setCentroid(centroid);
            clusters.add(cluster);
        }


    }

    //The processTuple to calculate the K Means, with iterating method.
    public void calculate(double[] points, List<Double> centroidAffectingPoints){
        boolean finish = false;
        int iteration = 0;
        double distance = 0.0;

        while(!finish) {
            clearClusters();
            double[] lastCentroids = getCentroids();
            assignCluster(points);
            calculateCentroids(centroidAffectingPoints);
            iteration++;

            double[] currentCentroids = getCentroids();
            distance = 0;
            for(int i = 0; i < lastCentroids.length; i++)
                distance += Math.abs(currentCentroids[i]-lastCentroids[i]);

            if(distance == 0 || iteration>=iteratonsCount) {
                finish = true;
            }
        }

        pointsToClusterAssignment = new int[points.length];
        for(int i=0; i<points.length; i++)
            pointsToClusterAssignment[i] = findClusterId(points[i]);

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


    private void assignCluster(double[] points) {
        double max = Double.MAX_VALUE;
        double min = max;
        int clusterInd = 0;
        double distance = 0.0;

        for(int j=0; j<points.length; j++){
                Double point = points[j];
                min = max;
                for(int i = 0; i < clusters.size(); i++) {
                    Cluster c = clusters.get(i);
                    Double centroid = c.getCentroid();
                    distance = Math.abs(centroid - point);
                    if (distance < min) {
                        min = distance;
                        clusterInd = i;
                    }
                }
                Cluster cluster = clusters.get(clusterInd);
                cluster.addPoint(point);
            }
    }

    private void calculateCentroids(List<Double> centroidAffectingPoints){
        for(Cluster cluster : clusters) {
            double sumX = 0;
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
            }
        }
    }
}