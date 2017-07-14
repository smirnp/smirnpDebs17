package smirnp.debs17.processing;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created by root on 7/10/17.
 */
public class Markov {

    private final List<Map<Integer, Double>> probabilities;

    public Markov(int clustersCount){
        probabilities = new ArrayList<>();
        for (int i = 0; i < clustersCount; i++)
            probabilities.add(new HashMap<Integer, Double>());
    }

    public List<Map<Integer, Double>> calculate(int[] pointClusterIds){
        for(int i=1; i<pointClusterIds.length; i++){
            int from = pointClusterIds[i-1];
            int to = pointClusterIds[i];
            Double value = getProbability(from, to)+1;
            probabilities.get(from).put(to, value);
        }

        for(int from=0; from<probabilities.size()-1; from++){
            if (probabilities.get(from).size()>1){
                double sum = Stream.of(probabilities.get(from)).flatMap(e->e.values().stream()).mapToDouble(e->e).sum();
                for(int to : probabilities.get(from).keySet())
                    probabilities.get(from).put(to, getProbability(from,to)/sum);
            }

        }
        return probabilities;
    }


    public Double getProbability(int from, int to){
        try {
            if (!probabilities.get(from).containsKey(to))
                return 0.0;
            else
                return probabilities.get(from).get(to);
        }
        catch (Exception e){
            System.out.print(e.getMessage());
        }
        return 0.0;
    }



}
