package org.vitrivr.cineast.core.util.math.functions.interfaces;

import org.apache.commons.math3.complex.Complex;

/**
 * This interface can be implemented by classes that implement a univariate complex function, i.e. a function
 * that take a complex argument and returns a complex result.
 *
 */
public interface UnivariateComplexFunction {
    /**
     * Computes the value of the function.
     *
     * @param value Point at which the function value should be computed.
     * @return the value of the function.
     */
    public Complex value(Complex value);
}
