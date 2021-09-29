package org.vitrivr.cineast.core.util.math;


public final class MathConstants {
    /** Definition of the golden ratio PHI. */
    public static final double PHI = ((1.0+Math.sqrt(5.0))/2.0);

    /** Square-root of three.*/
    public static final double SQRT3 = Math.sqrt(3);


    /** Defines the vertices of a regular Cube. */
    public static final double[][] VERTICES_3D_CUBE = {
            {1,1,1},{-1,-1,-1},{1,-1,-1},{-1,-1,1},
            {-1,1,-1},{-1,1,1},{1,-1,1},{1,1,-1}
    };

    /** Defines the vertices of a regular Dodecahedron. */
    public static final double[][] VERTICES_3D_DODECAHEDRON = {
            {1,1,1},{-1,-1,-1},{1,-1,-1},{-1,-1,1},
            {-1,1,-1},{-1,1,1},{1,-1,1},{1,1,-1},
            {0,1/PHI,PHI},{0,-1/PHI,PHI},{0,1/PHI,-PHI},{0,-1/PHI,-PHI},
            {1/PHI,PHI,0},{-1/PHI,PHI,0},{1/PHI,-PHI,0},{-1/PHI,-PHI,0},
            { PHI,0,1/PHI},{- PHI,0,1/PHI},{ PHI,0,-1/PHI},{-PHI,0,-1/PHI}
    };

    /**
     * Private constructor; no instantiation possible!
     */
    private MathConstants() {

    }
}
