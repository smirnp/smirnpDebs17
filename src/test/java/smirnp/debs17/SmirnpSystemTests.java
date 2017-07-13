package smirnp.debs17;

import org.junit.Test;

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
