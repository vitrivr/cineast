
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.util.fft.windows.HanningWindow;
import org.vitrivr.cineast.core.util.fft.windows.IdentityWindow;
import org.vitrivr.cineast.core.util.fft.windows.WindowFunction;

import java.util.ArrayList;

/**
 * @author rgasser
 * @version 1.0
 * @created 07.02.17
 */
public class WindowFunctionTest {


    private static final ArrayList<WindowFunction> FUNCTIONS = new ArrayList<>();
    static {
        FUNCTIONS.add(new HanningWindow());
        FUNCTIONS.add(new IdentityWindow());
    }

    @Test
    void testHanningWindow() {
        this.executeTest(new HanningWindow(), 1024);
        this.executeTest(new HanningWindow(), 2048);
        this.executeTest(new HanningWindow(), 4096);
    }


    /**
     *
     * @param function
     * @param length
     */
    private void executeTest(WindowFunction function, int length) {
        assertAll("windows",
                () -> assertTrue(testWindowFunctionZeroOutside(function, length), "The function '" + function.getClass().getSimpleName() + "' does not take zero values outside the window."),
                () -> assertTrue(testNormalization(function, length), "The function '" + function.getClass().getSimpleName() + "' does not meet the normalization criterion.")
        );
    }

    /**
     * Tests if a given WindowFunction satisfies the condition that it must be zero outside the
     * bounds of the window (i.e. for i < 0 or i > length).
     *
     * @param function WindowFunction to test
     * @param size Length of the window.
     * @return True if WindowFunction meets normalization criterion, false otherwise.
     */
    private boolean testWindowFunctionZeroOutside(WindowFunction function, int size) {
        for (int i=-size;i<-1;i++) {
            if (function.value(i, size) != 0) return false;
        }
        for (int i=size;i<2*size;i++) {
            if (function.value(i, size) != 0) return false;
        }
        return true;
    }

    /**
     * Tests if a given WindowFucntion satisfies the normalization criterion. Returns true
     * if so and false otherwise.
     *
     * @param function WindowFunction to test
     * @param length Length of the window.
     * @return True if WindowFunction meets normalization criterion, false otherwise.
     */
    private boolean testNormalization(WindowFunction function, int length) {
        double normal = 0.0f;
        for (int i =0; i<length; i++) {
            normal += Math.pow(function.value(i, length),2);
        }
        return normal == function.normalization(length);
    }
}
