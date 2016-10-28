package org.vitrivr.cineast.explorative;


import java.util.ArrayList;
import java.util.List;

class VisualizationElement<T extends Printable> implements Printable {

    private Position position;
    private final T vector;
    private final Plane plane;


    VisualizationElement(T vector, Position position, Plane plane) {
        this.vector = vector;
        this.position = position;
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
    public String print() {
        return vector.print();
    }
}
