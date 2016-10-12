package org.vitrivr.cineast.explorative;


import org.vitrivr.cineast.core.data.hct.HCTVisualizer;

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

    public Position getFirstFreeNeighborPosition(){
        if(plane.isFreePosition(position.getPosTop())) return position.getPosTop();
        if(plane.isFreePosition(position.getPosLeft())) return position.getPosLeft();
        if(plane.isFreePosition(position.getPosBottom())) return position.getPosBottom();
        if(plane.isFreePosition(position.getPosRight())) return position.getPosRight();
        throw new RuntimeException("This is an element without free neighborhood!");
    }

    public List<VisualizationElement<T>> getNeighbors(){
        Position[] neighborPositions = position.getNeighbors();
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
        if(HCTVisualizer.segments.get(vector.print()) == null){
            if(vector == plane.getRepresentative()) {
                return "<div style=\"background-color:green; padding:10px\">" + vector.print() + "</div>";
            }
            return vector.print();
        }
        if(vector == plane.getRepresentative()){
            return "<img style=\" background-color: red; padding: 2px; \" src= /Users/silvanstich/IdeaProjects/cineast_new/data/averagecolors/" + HCTVisualizer.segments.get(vector.print()) + "/" + vector.print() + ".jpg.png></img>";
        } else {
            return "<img src= /Users/silvanstich/IdeaProjects/cineast_new/data/averagecolors/" + HCTVisualizer.segments.get(vector.print()) + "/" + vector.print() + ".jpg.png></img>";
        }
    }
}
