package smirnp.debs17;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by root on 7/8/17.
 */
public class Emitter {

    private List<String> lines;
    Processor processor;
    public Emitter(Processor processor1){
        processor = processor1;

    }

    public void init(){
        Path path = Paths.get("data","1molding_machine/molding_machine_308dp.csv");
        //Path path = Paths.get("data","1molding_machine/molding_machine_308dp.nt");
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        for(String line : lines)
            emit(line);
    }

    private void emit(String line){
        String test=line.toString();
        processor.processTuple(line);
    }

//    public static class GeneratorBuilder{
//        public Emitter build(){
//            return new Emitter();
//
//        }
//
//    }

}
