package org.vitrivr.cineast.core.db;

public enum RelationalOperator {
    /** TRUE if A is equal to B. */
    EQ,

    /** TRUE if A is not equal to B. */
    NEQ,

    /** TRUE if A is greater than or equal to B. */
    GEQ,

    /** TRUE if A is less than or equal to B. */
    LEQ,

    /** TRUE if A is greater than B. */
    GREATER,

    /** TRUE if A is less than B. */
    LESS,

    /** TRUE if A is between B and C */
    BETWEEN,

    /** TRUE if string A matches string B (SQL LIKE syntax expected). */
    LIKE,

    /** TRUE if string A does not match string B (SQL LIKE syntax expected). */
    NLIKE,

    /** TRUE for fulltext match; Apache Lucene syntax expected. */
    MATCH,

    /** TRUE if A is null. */
    ISNULL,

    /** TRUE if A is not null. */
    ISNOTNULL,

    IN
}
