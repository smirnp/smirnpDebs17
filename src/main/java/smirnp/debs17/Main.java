package smirnp.debs17;

/**
 * Created by root on 7/8/17.
 */
public class Main {

    public static void main(String[] args) {

        //Emitter emitter = new Emitter.GeneratorBuilder().build();
        Processor processor = new Processor.ProcessorBuilder()
                .windowSize(10)
                .iterationsCount(100)
                .transisionsCount(5)
                .build();
        processor.init();

        Emitter emitter = new Emitter(processor);
        emitter.init();
        emitter.start();

        //processor.processTuple();
        System.out.printf("main");
    }
}
