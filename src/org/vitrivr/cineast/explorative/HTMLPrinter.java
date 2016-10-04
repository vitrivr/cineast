package org.vitrivr.cineast.explorative;

public class HTMLPrinter<HCTFloatVectorValue> implements Printer<HCTFloatVectorValue>{

    StringBuilder sb = new StringBuilder();

    @Override
    public void printHead() {
        sb.append("<html><body>");
    }

    @Override
    public void printLevel() {
        sb.append("");
    }

    @Override
    public void printCell() {

    }

    @Override
    public void printElement(HCTFloatVectorValue Element) {

    }

    @Override
    public void printFooter() {

    }
}
