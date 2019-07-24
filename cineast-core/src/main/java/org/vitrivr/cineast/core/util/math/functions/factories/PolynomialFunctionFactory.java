package org.vitrivr.cineast.core.util.math.functions.factories;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;

/**
 * This class can be used to generate different types of polynomials. It leverages the PolynomialFunction
 * class provided by Apache Commons Math library.
 *
 * @author rgasser
 * @version 1.0
 * @created 16.03.17
 */
public final class PolynomialFunctionFactory {

    /**
     * Cache for Zernike Coefficients.
     */
    private final static ConcurrentHashMap<String, PolynomialFunction> RADIAL_FUNCTION_CACHE = new ConcurrentHashMap<>();

    /**
     * Creates and returns a new radial polynomial (R_nm) given two moments.
     *
     * @param n 1st moment (order) of the radial polynomial.
     * @param m 2nd moment (repetition) of the radial polynomial.
     * @return PolynomialFunction representing R_nm
     * @throws ArithmeticException If orders are to large and calculation of binomial coefficients fail.
     */
    public static PolynomialFunction createRadialPolynomial(final int n, int m) {
        m = Math.abs(m); /* Make sure that m is positive. */
        String id = n + "-" + m; /* Construct ID for cache lookup. */

        /* Try to retrieve the function from cache. */
        if (RADIAL_FUNCTION_CACHE.containsKey(id)) {
            return RADIAL_FUNCTION_CACHE.get(id);
        }

        /* Initialize coefficients. */
        double[] coefficients = new double[n + 1];

        /* Now check if Polynomial 0 (for n-|m| = odd) .*/
        if ((n - m) % 2 != 0) {
          return new PolynomialFunction(coefficients); /* If (n-m) != even, return 0 function. */
        }
        int s_max = (n - m) / 2;

        double sign = -1.0;
        for (int s = 0; s <= s_max; ++s) {
            int position = n - 2 * s;
            long a = CombinatoricsUtils.binomialCoefficient(n-s, s);
            long b = CombinatoricsUtils.binomialCoefficient(n-2*s, s_max - s);
            coefficients[position] = (FastMath.pow(sign,s) * a * b);
        }

        PolynomialFunction function = new PolynomialFunction(coefficients);
        RADIAL_FUNCTION_CACHE.put(id, function);
        return function;
    }
}
