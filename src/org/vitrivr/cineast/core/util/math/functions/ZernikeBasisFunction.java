package org.vitrivr.cineast.core.util.math.functions;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.complex.Complex;

import org.apache.commons.math3.util.FastMath;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.math.functions.factories.PolynomialFunctionFactory;
import org.vitrivr.cineast.core.util.math.functions.interfaces.UnivariateComplexFunction;

/**
 * This class represents a Zernike Basis Function of order m,n. These functions can be
 * used to construct a Zernike Polynomial.
 *
 * @author rgasser
 * @version 1.0
 * @created 16.03.17
 */
public class ZernikeBasisFunction implements UnivariateComplexFunction {

    /** Radial polynomial (R_nm) used to calculate the ZernikeBasisFunction. */
    private final PolynomialFunction radialPolynomial;

    /** 1st moment of the radial polynomial (order). */
    private final int n;

    /** 2nd moment of the radial polynomial (repetition). */
    private final int m;

    /**
     * Constructs a new ZernikeBasisFunction given the 1st and 2nd moment used
     * to construct the radial polynomial associated with it.
     *
     * @param n 1st moment of the radial polynomial.
     * @param m 2nd moment of the radial polynomial.
     * @return PolynomialFunction representing R_nm
     */
    public ZernikeBasisFunction(int n, int m) {
        this.radialPolynomial = PolynomialFunctionFactory.createRadialPolynomial(n,m);
        this.n = n;
        this.m = m;
    }

    /**
     * Computes the value of the function.
     *
     * @param value Point at which the function value should be computed.
     * @return the value of the function.
     */
    @Override
    public Complex value(Complex value) {
        /* Check that length of r is smaller or equal 1. */
        if (value.abs() > 1.0) return new Complex(0.0, 0.0);

        /* Calculate value of Zernike Basis Function at point. */
        double r = this.radialPolynomial.value(value.abs());
        double a = 0.0;
        double b = 0.0;

        if (m >= 0) {
            a = r * FastMath.cos(value.getArgument() * this.m);
        } else {
            b = r * FastMath.sin(value.getArgument() * this.m);
        }

        return new Complex(a, b).multiply(this.normalization());
    }

    /**
     * Getter for the radial polynomial.
     *
     * @return PolynomialFunction
     */
    public final PolynomialFunction getRadialPolynomial() {
        return this.radialPolynomial;
    }


    /**
     * Normaliztion factor for ANSI Zernike Polynomials.
     *
     * @return Normalization factor.
     */
    public final double normalization() {
        return FastMath.sqrt((2*this.n + 2)/(1 + MathHelper.kronecker(0, this.m)));
    }

    /**
     * Custom equals implementation.
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZernikeBasisFunction that = (ZernikeBasisFunction) o;

        if (n != that.n) return false;
        return m == that.m;
    }

    /**
     * Custom hashCode() implementation.
     *
     * @return
     */
    @Override
    public int hashCode() {
        int result = n;
        result = 31 * result + m;
        return result;
    }
}
