package smirnp.debs17;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for Docker.
 *
 * @author Roman Katerinenko
 */
public class DebsParrotBenchmarkSystemRunner {
    private static final Logger logger = LoggerFactory.getLogger(DebsParrotBenchmarkSystemRunner.class);

    public static void main(String... args) throws Exception {
        logger.debug("Running...");
        DebsParrotBenchmarkSystem system = null;
        try {
            system = new DebsParrotBenchmarkSystem();
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