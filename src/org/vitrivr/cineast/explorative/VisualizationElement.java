package org.vitrivr.cineast.explorative;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.vitrivr.cineast.core.data.Position;

class VisualizationElement<T extends Printable> implements Printable, Serializable {

  private static final long serialVersionUID = 3023060852779864874L;
    private Position position;
    private final T vector;
    private final Plane<T> plane;
    private Printable representative;

    VisualizationElement(T vector, Position position, Printable representative, Plane<T> plane){
        this.vector = vector;
        this.position = position;
        this.representative = representative;
        this.plane = plane;
    }

    public T getVector() {
        return vector;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean hasFreeNeighborTop(){
        return plane.isFreePosition(position.getPosTop());
    }

    public boolean hasFreeNeighborLeft(){
        return plane.isFreePosition(position.getPosLeft());
    }

    public boolean hasFreeNeighborBottom(){
        return plane.isFreePosition(position.getPosBottom());
    }

    public boolean HasFreeNeighborRight(){
        return plane.isFreePosition(position.getPosRight());
    }

    public String getRepresentative(){
        return representative.getId();
    }

    public List<VisualizationElement<T>> getNeighbors(){
        Position[] neighborPositions = position.getNeighborPositions();
        List<VisualizationElement<T>> neighbors = new ArrayList<>();
        for(Position p : neighborPositions){
            if(!plane.isFreePosition(p)){
                neighbors.add(plane.getVisElementAtPos(p));
            }
        }
        return neighbors;
    }

    @Override
    public String printHtml() {
        return vector.printHtml();
    }

    @Override
    public String getId() {
        return vector.getId();
    }

    public void setRepresentative(T representative) {
        this.representative = representative;
    }
}
