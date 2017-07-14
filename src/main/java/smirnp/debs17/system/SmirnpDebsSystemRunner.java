package smirnp.debs17.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class SmirnpDebsSystemRunner {
    private static final Logger logger = LoggerFactory.getLogger(SmirnpDebsSystemRunner.class);

    public static void main(String... args) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(SmirnpDebsSystem.MAX_CLUSTER_ITERATIONS_INPUT_NAME, 50);
        params.put(SmirnpDebsSystem.TRANSITIONS_COUNT_INPUT_NAME, 5);
        params.put(SmirnpDebsSystem.WINDOW_SIZE_INPUT_NAME, 10);


        logger.debug("Running...");
        SmirnpDebsSystem system = null;
        try {
            system = new SmirnpDebsSystem(params);
            system.init();
            system.run();
        } finally {
            if (system != null) {
                system.close();
            }
        }
        logger.debug("Finished.");
    }
}