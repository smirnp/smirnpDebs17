package smirnp.debs17;

import org.junit.Test;
import smirnp.debs17.system.SmirnpDebsSystemRunner;

/**
 * Created by root on 7/13/17.
 */
public class SmirnpSystemTests {

    @Test
    public void checkSystemStarts(){
        try {
            SmirnpDebsSystemRunner.main();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
