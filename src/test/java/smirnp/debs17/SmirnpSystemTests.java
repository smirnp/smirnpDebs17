package smirnp.debs17;

import org.hobbit.core.Constants;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import smirnp.debs17.system.SmirnpDebsSystemRunner;

/**
 * Created by root on 7/13/17.
 */
public class SmirnpSystemTests {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Before
    public void init() throws Exception {
        environmentVariables.set(Constants.RABBIT_MQ_HOST_NAME_KEY, "localhost");
        environmentVariables.set(Constants.HOBBIT_SESSION_ID_KEY, "0");
        //environmentVariables.set(Constants.GENERATOR_ID_KEY, "0");
        //environmentVariables.set(Constants.GENERATOR_COUNT_KEY, "1");

    }

    @Test
    public void checkSystemStarts(){

        try {

            SmirnpDebsSystemRunner.main();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
