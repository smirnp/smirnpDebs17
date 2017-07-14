package smirnp.debs17;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Created by root on 7/8/17.
 */
public class Tuple {
    //private final int skip = 3;
    private LocalDateTime timeStamp;
    private final double[] values;
    private final int id;

    public Tuple(int _id, String str){
        id = _id;
        String[] splitted = str.split(", ");
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
        try {
            timeStamp = LocalDateTime.ofInstant(format.parse(splitted[1]+" "+splitted[2]).toInstant(), ZoneId.systemDefault());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        double[] vector = new double[splitted.length-1];

        for(int i=0; i<splitted.length; i++){
            try {
                Double val = Double.parseDouble(splitted[i]);
                vector[i] = val;
            }
            catch (Exception e){
                String test = e.toString();
            }
        }
        values = vector;
    }

    public LocalDateTime getLocalDateTime(){
        if (timeStamp==null) {
            String test = "123";
        }
        return timeStamp;
    }

    public double[] getValues(){
        return values;
    }

    public int getId(){return id;}

    public int getMachineId(){
        return new Double(values[0]).intValue();
    }
}
