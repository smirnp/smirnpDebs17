package smirnp.debs17.system;

import com.agtinternational.hobbit.sdk.JenaKeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.hobbit.core.Constants.SYSTEM_PARAMETERS_MODEL_KEY;
import static smirnp.debs17.system.SmirnpDebsSystem.*;

public class SmirnpDebsSystemRunner {
    private static final Logger logger = LoggerFactory.getLogger(SmirnpDebsSystemRunner.class);

    public static void main(String... args) throws Exception {

        logger.debug("Creating system component...");
        Map<String, Object> params = new HashMap<>();
        params.put(MAX_CLUSTER_ITERATIONS_INPUT_NAME, 50);
        params.put(TRANSITIONS_COUNT_INPUT_NAME, 5);
        params.put(WINDOW_SIZE_INPUT_NAME, 10);


        try {
            String encodedModel = System.getenv().get(SYSTEM_PARAMETERS_MODEL_KEY);
            logger.debug("Got params from platform:{}", encodedModel);
            params = new JenaKeyValue.Builder().buildFrom(encodedModel).toMap();
        }
        catch (Exception e){
            logger.debug("No parameters were passed to the system. Using defaults");
        }

        StringBuilder sb = new StringBuilder();
        for(Object key : params.keySet())
            sb.append(key.toString()+"="+ params.get(key).toString()+", ");
        logger.debug("Initializing with params: "+sb.toString());

        SmirnpDebsSystem system = new SmirnpDebsSystem(params);
        try {
            system.init();
        }catch (Exception e){
            logger.error("System failed to init: "+e.getMessage());
        }

        try {
            system.run();
        }catch (Exception e){
            logger.error("System failed during run: "+e.getMessage());
        }

        finally {
            if (system != null) {
                system.close();
            }
        }
        logger.debug("Finished.");
    }
}