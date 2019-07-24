package org.vitrivr.cineast.core.util.math.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialsUtils;
import org.apache.commons.math3.util.FastMath;

/**
 * @author rgasser
 * @version 1.0
 * @created 21.03.17
 */
public final class AssociatedLegendrePolynomial implements UnivariateFunction {

    /** Legendre used to generate base values for the associated Legendre polynomial. That polynomial
     * is the m-th derivative of the l-th Legendre polynomial.
     */
    private final PolynomialFunction legendre;

    /** Degree of the associated Legendre polynomial */
    private final int l;

    /** Order of the associated Legendre polynomial */
    private final int m;

    /** Sign of the polynomial which is only determined by m. */
    private final double sign;

    /**
     * Constructor for the AssociatedLegendrePolynomial class.
     *
     * @param l Degree of the associated Legendre polynomial
     * @param m Order of the associated Legendre polynomial
     */
    public AssociatedLegendrePolynomial(int l, int m) {

        /* Make some basic, arithmetic checks. */
        if (m > l) {
          throw new IllegalArgumentException("Associated Legendre Polynomials are defined for 0 <= m <= l. You provided m > l!");
        }
        if (m < 0) {
          throw new IllegalArgumentException("Associated Legendre Polynomials are defined for 0 <= m <= l. You provided m < 0!");
        }
        if (l < 0) {
          throw new IllegalArgumentException("Associated Legendre Polynomials are defined for 0 <= m <= l. You provided m < 0!");
        }

        /* Find m-th derivative of Legendre Polynomial of degree l. */
        PolynomialFunction fkt = PolynomialsUtils.createLegendrePolynomial(l);
        for (int i = 0; i < m; i++) {
            fkt = fkt.polynomialDerivative();
        }
        this.legendre = fkt;

        /* Determine sign. */
        this.sign = Math.pow(-1,m);
        this.m = m;
        this.l = l;
    }

    /**
     * Compute the value of the function.
     *
     * @param x Point at which the function value should be computed.
     * @return the value of the function.
     * @throws IllegalArgumentException when the activated method itself can
     *                                  ascertain that a precondition, specified in the API expressed at the
     *                                  level of the activated method, has been violated.
     *                                  When Commons Math throws an {@code IllegalArgumentException}, it is
     *                                  usually the consequence of checking the actual parameters passed to
     *                                  the method.
     */
    @Override
    public final double value(double x) {
        return this.sign * (FastMath.pow(1.0-FastMath.pow(x,2.0),m/2.0) * legendre.value(x));
    }

    /**
     * Returns the value of L for the AssociatedLegendrePolynomial
     *
     * @return L
     */
    public final int getL() {
        return this.l;
    }

    /**
     * Returns the value of M for the AssociatedLegendrePolynomial
     *
     * @return M
     */
    public final int getM() {
        return this.m;
    }
}
