package smirnp.debs17.processing;

        import java.util.ArrayList;
        import java.util.List;

public class Cluster {

    public List<Double> points;
    public Double centroid;
    public int id;

    //Creates a new Cluster
    public Cluster(int id) {
        this.id = id;
        this.points = new ArrayList();
        this.centroid = null;
    }

    public List<Double> getPoints() {
        return points;
    }

    public void addPoint(Double point) {
        points.add(point);
    }

    public void setPoints(List points) {
        this.points = points;
    }

    public Double getCentroid() {
        return centroid;
    }

    public void setCentroid(Double centroid) {
        this.centroid = centroid;
    }

    public int getId() {
        return id;
    }

    public void clear() {
        points.clear();
    }

    public void plotCluster() {
        System.out.println("[Cluster: " + id+"]");
        System.out.println("[Centroid: " + centroid + "]");
        System.out.println("[Points: \n");
        for(Object p : points) {
            System.out.println(p);
        }
        System.out.println("]");
    }

}