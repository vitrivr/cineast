package org.vitrivr.cineast.core.util.math.functions;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.math.functions.factories.PolynomialFunctionFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author rgasser
 * @version 1.0
 * @created 16.03.17
 */
public class RadialPolynomialTest {
    /**
     * Tests Orthogonality relation that exists between two radial polynomials.
     */
    @Test
    @DisplayName("Test Orthogonality")
    void testOrthogonality() {
        for (int n1=0;n1<10;n1++) {
            for (int n2=0;n2<10;n2++) {
                int max_m = Math.min(n1, n2);
                for (int m=0;m<=max_m;m++) {
                    PolynomialFunction R1 = PolynomialFunctionFactory.createRadialPolynomial(n1, m);
                    PolynomialFunction R2 = PolynomialFunctionFactory.createRadialPolynomial(n2, m);
                    double result = 0.0;
                    double increment = 1e-6;
                    for (double d=0.0;d<=1.0f;d+=increment) {
                        result += R1.value(d) * R2.value(d) * d * increment;
                    }
                    assertEquals((double)MathHelper.kronecker(n1,n2)/(2*n1 + 2) * R1.value(1.0), result, 1e-2);
                }
            }
        }
    }

    /**
     * Tests the R_nn (m=n) cases for the radial polynomials from n=0 to n=20.
     */
    @Test
    @DisplayName("Test R_nn")
    void testRnn() {
        for (int n=0;n<20;n++) {
            PolynomialFunction Rnn = PolynomialFunctionFactory.createRadialPolynomial(n, n);
            for (double i = -2.0f; i < 2.0f; i += 0.01) {
                assertEquals(Math.pow(i,n), Rnn.value(i), 1e-8);
            }
        }
    }

    /**
     * Tests the values of the R_10 (n=1, m=0) polynomial), which are supposed to be zero.
     */
    @Test
    @DisplayName("Test R_10")
    void testR10() {
        for (int n=0;n<20;n++) {
            PolynomialFunction Rnn = PolynomialFunctionFactory.createRadialPolynomial(1, 0);
            for (double i = -2.0f; i < 2.0f; i += 0.01) {
                assertEquals(0.0, Rnn.value(i), 1e-8);
            }
        }
    }

    /**
     * Tests the values of the R_40 (n=4, m=0) polynomial).
     */
    @Test
    @DisplayName("Test R_40")
    void testR04() {
        PolynomialFunction R40 = PolynomialFunctionFactory.createRadialPolynomial(4, 0);
        for (double i = -2.0f; i < 2.0f; i += 0.01) {
            assertEquals(6*Math.pow(i,4) - 6*Math.pow(i,2) + 1, R40.value(i), 1e-8);
        }
    }

    /**
     * Tests the values of the R_51 (n=5, m=1) polynomial).
     */
    @Test
    @DisplayName("Test R_51")
    void testR51() {
        PolynomialFunction R52N = PolynomialFunctionFactory.createRadialPolynomial(5, -1);
        PolynomialFunction R52P = PolynomialFunctionFactory.createRadialPolynomial(5, 1);
        for (double i = -2.0f; i < 2.0f; i += 0.01) {
            assertEquals(10*Math.pow(i,5) - 12*Math.pow(i,3) + 3*i, R52N.value(i), 1e-8);
            assertEquals(10*Math.pow(i,5) - 12*Math.pow(i,3) + 3*i, R52P.value(i), 1e-8);

        }
    }

    /**
     * Tests the values of the R_53 (n=5, m=3) polynomial).
     */
    @Test
    @DisplayName("Test R_53")
    void testR53() {
        PolynomialFunction R53P = PolynomialFunctionFactory.createRadialPolynomial(5, 3);
        PolynomialFunction R53N = PolynomialFunctionFactory.createRadialPolynomial(5, -3);
        for (double i = -2.0f; i < 2.0f; i += 0.01) {
            assertEquals(5*Math.pow(i,5) - 4*Math.pow(i,3), R53P.value(i), 1e-8);
            assertEquals(5*Math.pow(i,5) - 4*Math.pow(i,3), R53N.value(i), 1e-8);

        }
    }

    /**
     * Tests the values of the R_60 (n=6, m=0) polynomial).
     */
    @Test
    @DisplayName("Test R_60")
    void testR60() {
        PolynomialFunction R40 = PolynomialFunctionFactory.createRadialPolynomial(6, 0);
        for (double i = -2.0f; i < 2.0f; i += 0.01) {
            assertEquals(20*Math.pow(i,6) - 30*Math.pow(i,4) + 12*Math.pow(i,2) - 1, R40.value(i), 1e-8);
        }
    }
}
