package org.vitrivr.cineast.standalone.tests.util.dsp.fft;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.vitrivr.cineast.core.util.dsp.fft.windows.BlackmanHarrisWindow;
import org.vitrivr.cineast.core.util.dsp.fft.windows.HanningWindow;
import org.vitrivr.cineast.core.util.dsp.fft.windows.RectangularWindow;
import org.vitrivr.cineast.core.util.dsp.fft.windows.WindowFunction;

/**
 * @author rgasser
 * @version 1.0
 * @created 07.02.17
 */
public class WindowFunctionTest {
    @Test
    @DisplayName("Rectangular Window Test")
    void testRectangularWindow() {
        RectangularWindow window = new RectangularWindow();
        this.executeTest(window, 512);
        this.executeTest(window, 1024);
        this.executeTest(window, 2048);
        this.executeTest(window, 4096);
    }

    @Test
    @DisplayName("Hanning Window Test")
    void testHanningWindow() {
        HanningWindow window = new HanningWindow();
        this.executeTest(window, 512);
        this.executeTest(window, 1024);
        this.executeTest(window, 2048);
        this.executeTest(window, 4096);
    }

    @Test
    @DisplayName("Blackman Harris Window Test")
    void testBlackmanHarrisWindow() {
        BlackmanHarrisWindow window = new BlackmanHarrisWindow();
        this.executeTest(window, 512);
        this.executeTest(window, 1024);
        this.executeTest(window, 2048);
        this.executeTest(window, 4096);
    }


    /**
     *
     * @param function
     * @param length
     */
    private void executeTest(WindowFunction function, int length) {
        assertAll("Window Function Test (" + length +")",
                () -> testWindowFunctionZeroOutside(function, length),
                () -> testWindowFunctionSymmetry(function, length),
                () -> testNormalization(function, length)
        );
    }

    /**
     * Tests if a given WindowFunction satisfies the condition that it must be zero outside the
     * definition of the window interval (i.e. for i < 0 or i > length).
     *
     * @param function WindowFunction to test
     * @param length Length of the window.
     * @return True if WindowFunction meets normalization criterion, false otherwise.
     */
    private boolean testWindowFunctionZeroOutside(WindowFunction function, int length) {
        for (int i=-length;i<0;i++) {
            assertEquals(0.0, function.value(i, length), "The function '" + function.getClass().getSimpleName() + "' does not become zero at position i=" + i);
        }
        for (int i=length+1;i<=2*length;i++) {
            assertEquals(0.0, function.value(i, length), "The function '" + function.getClass().getSimpleName() + "' does not become zero at position i=" + i);
        }
        return true;
    }

    /**
     * Tests if a given WindowFunction satisfies the symmetry condition.
     *
     * @param function WindowFunction to test
     * @param length Length of the window.
     */
    private void testWindowFunctionSymmetry(WindowFunction function, int length) {
        for (int i=0;i<length/2;i++) {
            assertEquals(function.value(i, length), function.value((length-1)-i, length ), 1e-8, "The function '" + function.getClass().getSimpleName() + "' is not symmetric at position i=" + i);
        }
    }

    /**
     * Tests if a given WindowFunction satisfies the normalization criterion.
     *
     * @param function WindowFunction to test
     * @param length Length of the window.
     */
    private void testNormalization(WindowFunction function, int length) {
        double normal = 0.0;
        for (int i=0; i<length; i++) {
            normal += function.value(i, length);
        }
        assertEquals(normal/length, function.normalization(length), 1e-8, "The function '" + function.getClass().getSimpleName() + "' does not meet the normalization criterion.");
    }
}
