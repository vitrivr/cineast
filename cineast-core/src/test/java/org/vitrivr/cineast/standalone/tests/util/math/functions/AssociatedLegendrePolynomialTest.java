package org.vitrivr.cineast.standalone.tests.util.math.functions;

import org.apache.commons.math3.util.CombinatoricsUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.math.functions.AssociatedLegendrePolynomial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author rgasser
 * @version 1.0
 * @created 21.03.17
 */
public class AssociatedLegendrePolynomialTest {

    /**
     * Tests definition of AssociatedLegendrePolynomials, which are only
     * defined for 0 <= m <= l
     */
    @Test
    @DisplayName("Test Definition")
    public void testDefinition() {
        for (int l=-10;l<10;l++) {
            for (int m=0;m<=2*l;m++) {
                final int l_u = l;
                final int m_u = m;
                if (m > l || l < 0 || m < 0) {
                    assertThrows(IllegalArgumentException.class, () -> {
                        new AssociatedLegendrePolynomial(l_u, m_u);
                    });
                } else {
                    new AssociatedLegendrePolynomial(l_u, m_u);
                }
            }
        }
    }

    /**
     * Tests if orthogonality relation regarding a fixed l holds true for all l between 0 and 5.
     */
    @Test
    @DisplayName("Test Orthogonality (l)")
    public void testOrthogonalityL() {
        final double dx = 1e-4;
        for (int l1=0;l1<=5;l1++) {
            for (int l2=0;l2<=5;l2++) {
                for (int m=0;m<=l1 && m<=l2;m++) {
                    final AssociatedLegendrePolynomial alp1 = new AssociatedLegendrePolynomial(l1,m);
                    final AssociatedLegendrePolynomial alp2 = new AssociatedLegendrePolynomial(l2,m);
                    double result = 0.0;
                    final double expected = (2.0)/(2*l1+1) * ((double)CombinatoricsUtils.factorial(l1+m)/(double)CombinatoricsUtils.factorial(l1-m)) * MathHelper.kronecker(l1,l2);
                    for (double x = -1.0; x <= 1.0; x+=dx) {
                        result += (alp1.value(x) * alp2.value(x)) * dx;
                    }
                    assertEquals(expected, result,  1e-3);
                }
            }

        }
    }

    /**
     * Test P_00 (l=0, m=0)
     */
    @Test
    @DisplayName("Test P00")
    public void testPOO() {
        final double increment = 1e-4;
        final AssociatedLegendrePolynomial alp = new AssociatedLegendrePolynomial(0,0);
        for (double x = -1.0;x<=1.0;x+=increment) {
            assertEquals(1.0, alp.value(x), 1e-8);
        }
    }

    /**
     * Test P_10 (l=1, m=0)
     */
    @Test
    @DisplayName("Test P10")
    public void testP1O() {
        final double increment = 1e-4;
        final AssociatedLegendrePolynomial alp = new AssociatedLegendrePolynomial(1,0);
        for (double x = -1.0;x<=1.0;x+=increment) {
            assertEquals(x, alp.value(x), 1e-8);
        }
    }

    /**
     * Test P_11 (l=1, m=1)
     */
    @Test
    @DisplayName("Test P11")
    public void testP11() {
        final double increment = 1e-4;
        final AssociatedLegendrePolynomial alp = new AssociatedLegendrePolynomial(1,1);
        for (double x = -1.0;x<=1.0;x+=increment) {
            assertEquals(-Math.sqrt(1-Math.pow(x,2)), alp.value(x), 1e-8);
        }
    }

    /**
     * Test P_20 (l=2, m=0)
     */
    @Test
    @DisplayName("Test P20")
    public void testP20() {
        final double increment = 1e-4;
        final AssociatedLegendrePolynomial alp = new AssociatedLegendrePolynomial(2,0);
        for (double x = -1.0;x<=1.0;x+=increment) {
            assertEquals(0.5 * (3*Math.pow(x,2)-1), alp.value(x), 1e-8);
        }
    }

    /**
     * Test P_21 (l=2, m=1)
     */
    @Test
    @DisplayName("Test P21")
    public void testP21() {
        final double increment = 1e-4;
        final AssociatedLegendrePolynomial alp = new AssociatedLegendrePolynomial(2,1);
        for (double x = -1.0;x<=1.0;x+=increment) {
            assertEquals(-3.0*x*Math.sqrt(1-Math.pow(x,2)), alp.value(x), 1e-8);
        }
    }

    /**
     * Test P_21 (l=2, m=2)
     */
    @Test
    @DisplayName("Test P22")
    public void testP22() {
        final double increment = 1e-4;
        final AssociatedLegendrePolynomial alp = new AssociatedLegendrePolynomial(2,2);
        for (double x = -1.0;x<=1.0;x+=increment) {
            assertEquals(3.0*(1-Math.pow(x,2)), alp.value(x), 1e-8);
        }
    }

    /**
     * Test P_30 (l=3, m=0)
     */
    @Test
    @DisplayName("Test P30")
    public void testP30() {
        final double increment = 1e-4;
        final AssociatedLegendrePolynomial alp = new AssociatedLegendrePolynomial(3,0);
        for (double x = -1.0;x<=1.0;x+=increment) {
            assertEquals(0.5*(5.0*Math.pow(x,3) - 3.0*x), alp.value(x), 1e-8);
        }
    }

    /**
     * Test P_30 (l=3, m=1)
     */
    @Test
    @DisplayName("Test P31")
    public void testP31() {
        final double increment = 1e-4;
        final AssociatedLegendrePolynomial alp = new AssociatedLegendrePolynomial(3,1);
        for (double x = -1.0;x<=1.0;x+=increment) {
            assertEquals(-(3.0/2.0)*(5.0*Math.pow(x,2) - 1.0)*Math.sqrt((1-Math.pow(x,2))), alp.value(x), 1e-8);
        }
    }

    /**
     * Test P_30 (l=3, m=2)
     */
    @Test
    @DisplayName("Test P32")
    public void testP32() {
        final double increment = 1e-4;
        final AssociatedLegendrePolynomial alp = new AssociatedLegendrePolynomial(3,2);
        for (double x = -1.0;x<=1.0;x+=increment) {
            assertEquals(15*x*(1-Math.pow(x,2)), alp.value(x), 1e-8);
        }
    }

    /**
     * Test P_30 (l=3, m=3)
     */
    @Test
    @DisplayName("Test P33")
    public void testP33() {
        final double increment = 1e-4;
        final AssociatedLegendrePolynomial alp = new AssociatedLegendrePolynomial(3,3);
        for (double x = -1.0;x<=1.0;x+=increment) {
            assertEquals(-15*Math.pow(1-Math.pow(x,2),3.0/2.0), alp.value(x), 1e-8);
        }
    }

    /**
     * Test P_40 (l=4, m=0)
     */
    @Test
    @DisplayName("Test P40")
    public void testP40() {
        final double increment = 1e-4;
        final AssociatedLegendrePolynomial alp = new AssociatedLegendrePolynomial(4,0);
        for (double x = -1.0;x<=1.0;x+=increment) {
            assertEquals((1.0/8.0)*(35*Math.pow(x,4) - 30*Math.pow(x,2) + 3), alp.value(x), 1e-8);
        }
    }
}
