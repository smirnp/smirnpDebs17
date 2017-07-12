package smirnp.debs17;

import java.util.Date;
import java.util.stream.*;

/**
 * Created by root on 7/11/17.
 */
public class State {
    private final Date timestamp;
    private final double[][] values;
    private final double[] valuesSum;
    private final double[][] transitions;

    public State(Date _timestamp, double[][] _values, double[][] _transitions){
        timestamp = _timestamp;
        values = _values;
        valuesSum = new double[values.length];
        transitions = _transitions;

        for (int dim=0; dim<values.length; dim++)
            if (values[dim]!=null){
                Double valueSum = 0.0;
                for (int i = 0; i < values[dim].length; i++)
                    valueSum+= values[dim][i];
                valuesSum[dim] = valueSum;
            }

    }


    public Date getTimestamp(){
        return timestamp;
    }

    public double[][] getValues(){
        return values;
    }
    public Double getValuesSum(int dim){
        return valuesSum[dim];
    }

}
