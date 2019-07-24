package org.vitrivr.cineast.standalone.tests.util.math.functions;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.math.functions.ZernikeBasisFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author rgasser
 * @version 1.0
 * @created 16.03.17
 */
public class ZernikePolynomialsTest {
    /**
     * Tests definition of Zernike Polynomials, which are only defined
     * for n > 0, |m| <= n and n-|m| = even
     */
    @Test
    @DisplayName("Test Definition")
    void testDefinition() {
        for (int n=-20;n<=20;n++) {
            for (int m=-2*n; m<=2*n;m++) {
                if (((n-Math.abs(m)) % 2 == 1) || n < 0 || Math.abs(m) > n) {
                    final int n_u = n;
                    final int m_u = m;
                    assertThrows(IllegalArgumentException.class, () -> {
                        new ZernikeBasisFunction(n_u,m_u);
                    });
                }
            }
        }
    }

    /**
     * Tests if orthogonality relation that exists between two Zernike Polynoms holds true
     * for all n between 1 and 5.
     */
    @Test
    @DisplayName("Test Orthogonality")
    void testOrthogonality() {
        final double increment = 0.25e-2;
        final double n_max = 5;
        for (int n1=1;n1<=n_max;n1++) {
            for (int m1=0; m1<=n1;m1++) {
                for (int n2=1;n2<=n_max;n2++) {
                    for (int m2=0;m2<=n2;m2++) {
                        if (((n1-Math.abs(m1)) % 2 == 0) && ((n2-Math.abs(m2)) % 2 == 0)) {
                            Complex result = new Complex(0, 0);

                            /* Initialize ZernikeBasisFunctions for n1,m1 and n2,m2. */
                            final ZernikeBasisFunction ZF1 = new ZernikeBasisFunction(n1, m1);
                            final ZernikeBasisFunction ZF2 = new ZernikeBasisFunction(n2, m2);
                            final double expected = ((Math.PI) / (n1 + 1)) * MathHelper.kronecker(n1,n2) * MathHelper.kronecker(m1,m2);

                            /* Calculate integral (approximation). */
                            for (double theta = 0.0; theta <= 2 * Math.PI; theta += increment) {
                                for (double r = 0.0; r <= 1.0f; r += increment) {
                                    Complex v = new Complex(r * FastMath.cos(theta), r * FastMath.sin(theta));
                                    Complex res1 = ZF1.value(v);
                                    Complex res2 = ZF2.value(v);
                                    result = result.add(res1.conjugate().multiply(res2).multiply(r * increment * increment));
                                }
                            }

                            /* Result of integral must be equal to expected value. */
                            assertEquals(expected, result.abs(), 1e-2);
                        }
                    }
                }
            }
        }
    }

    /**
     * Test Z_00, which must be equal to 1.0 + 0.0i for all values ofs r
     * and theta.
     */
    @Test
    @DisplayName("Test Z_00")
    void testZ00() {
        final ZernikeBasisFunction ZF1 = new ZernikeBasisFunction(0, 0);
        final double increment = 1e-3;
        for (double r = 0.0; r <= 1.0f; r += increment) {
            for (double theta = 0; theta <= 2*Math.PI; theta += increment) {
                Complex v = new Complex(r * FastMath.cos(theta), r * FastMath.sin(theta));
                assertEquals(1.0, ZF1.value(v).abs(), 1e-8);
                assertEquals(0.0, ZF1.value(v).getArgument(), 1e-8);
            }
        }
    }
}
