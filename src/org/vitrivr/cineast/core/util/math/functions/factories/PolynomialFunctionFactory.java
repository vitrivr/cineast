package org.vitrivr.cineast.core.util.math.functions.factories;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.util.CombinatoricsUtils;

/**
 * This class can be used to generate different types of polynomials. It leverages the
 * PolynomialFunction class provided by Apache Commons Math library.
 *
 *
 * @author rgasser
 * @version 1.0
 * @created 16.03.17
 */
public final class PolynomialFunctionFactory {
    /**
     * Creates and returns a new radial polynomial (R_nm) given two moments.
     *
     * @param n 1st moment of the radial polynomial.
     * @param m 2nd moment of the radial polynomial.
     * @return PolynomialFunction representing R_nm
     */
    public static PolynomialFunction createRadialPolynomial(final int n, int m) {
        m = Math.abs(m); /* Make sure that m is positive. */
        double[] coefficients = new double[n + 1];

        if ((n-m) % 2 != 0) return new PolynomialFunction(coefficients); /* If (n-m) != even, return 0 function. */

        int s_max = (n-m)/2;

        /* Prepare coefficients used to calculate the polynomial. */
        long a = CombinatoricsUtils.factorial(n);
        long b = 1;
        long c = CombinatoricsUtils.factorial(((n + m) / 2));
        long d = CombinatoricsUtils.factorial(((n - m) / 2));

        double sign = 1.0;
        for (int s=0; s<=s_max;++s) {
            int position = n - 2*s;
            coefficients[position] = (sign * a) / (c * d * b);

            /* Update coefficients for next iteration. */
            if (s < s_max) {
                sign = -sign;
                a /= (n - s);
                b *= (s + 1);
                c /= ((n + m) / 2 - s);
                d /= ((n - m) / 2 - s);
            }

        }

        return new PolynomialFunction(coefficients);
    }
}
