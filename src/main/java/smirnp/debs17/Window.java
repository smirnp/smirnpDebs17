package smirnp.debs17;

import java.util.*;

/**
 * Created by root on 7/8/17.
 */
public class Window{
    private int totalTuplesCounter = 0;
    private final int windowSize;
    private final Tuple[] tuples;
    private final double[][] dimentionedValues;
    private final List clusteringIndexes;

    public Window(int _windowSize, List<Integer> _propertyIds){
        windowSize = _windowSize;
        tuples = new Tuple[_windowSize];
        clusteringIndexes = _propertyIds;
        dimentionedValues = new double[_propertyIds.get(_propertyIds.size()-1)+2][windowSize];
        String test = "";
    }

    public void put(Tuple tuple){

        //Shifting oldees(top) values
        for(int j=0 ; j<windowSize-1; j++)
            tuples[j]=tuples[j+1];
        tuples[windowSize-1] = tuple;



        for (int dim=0; dim<tuple.getValues().length; dim++)
            if (clusteringIndexes.contains(dim)){
                //Shifting oldees(top) values
                for(int j=0 ; j<windowSize-1; j++)
                        dimentionedValues[dim][j] = dimentionedValues[dim][j+1];

                dimentionedValues[dim][windowSize-1] = tuple.getValues()[dim];
                String test="123";
            }

        totalTuplesCounter++;
    }



    public Tuple getTuple(int index){
        return tuples[index];
    }

    public double[][] getDimentionedValues(){
        return dimentionedValues;
    }

    public int getActualTuplesCount(){
        return Math.min(windowSize, totalTuplesCounter);
    }

//    public Date[] getTimestamps(){
//        Date[] ret = new Date[tuples.length];
//        for(int i=0; i< tuples.length; i++)
//            ret[i] = tuples[i].getLocalDateTime();
//        return ret;
//    }
}