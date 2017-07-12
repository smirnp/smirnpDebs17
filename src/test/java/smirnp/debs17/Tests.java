package smirnp.debs17;


import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by root on 7/8/17.
 */
public class Tests {

    @Test
    public void checkAnomalyDetection(){

        Processor processor = new Processor.ProcessorBuilder()
                .windowSize(10)
                .iterationsCount(100)
                .transisionsCount(5)
                .metadataFilePath("data/1molding_machine/molding_machine_308dp.metadata.nt")
                .build();
        processor.init();


        //Emitter emitter = new Emitter(processor, Paths.get("data","1molding_machine/molding_machine_308dp.csv"), 1);
        Emitter emitter = new Emitter(processor, Paths.get("data","1molding_machine/molding_machine_308dp.nt"), 944);
        emitter.start();

    }
}
