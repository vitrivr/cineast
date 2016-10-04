package org.vitrivr.cineast.explorative;

public interface Printer<T> {

    void printHead();
    void printLevel();
    void printCell();
    void printElement(T Element);
    void printFooter();
}
