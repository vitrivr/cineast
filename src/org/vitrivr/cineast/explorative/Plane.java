package org.vitrivr.cineast.explorative;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.hct.DistanceCalculation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


public class Plane<T extends Printable> implements Printable {

    private final VisualizationElement<T>[][] plane;
    private final static Logger logger = LogManager.getLogger();
    private final List<VisualizationElement<T>> insertOrderVisualizationElement;
    private final HashSet<T> addedVectors;
    private final int height;
    private final int width;
    private final List<T> vectors;
    private final Iterator<T> iterator;
    private int vectorsPointer = 0;
    private final DistanceCalculation<T> distanceCalculator;
    private final T representative;

    public Plane(List<T> vectors, DistanceCalculation<T> distanceCalculator, T representative){
        this.height = (int)Math.sqrt(vectors.size()) + 1;
        this.width = (int)Math.sqrt(vectors.size()) + 1;
        this.vectors = vectors;
        this.representative = representative;
        if(vectors.contains(representative)) vectors.remove(representative);
        this.iterator = vectors.iterator();
        this.distanceCalculator = distanceCalculator;
        plane = new VisualizationElement[width][height];
        insertOrderVisualizationElement = new ArrayList<>();
        addedVectors = new HashSet<>();
    }

    public void processCollection(){
        Position startPos = new Position(width/2, height/2);
        insert(new VisualizationElement<>(representative, startPos, this), startPos);

        while(iterator.hasNext()){
            T nextItem = iterator.next();
            Position optimalPosition = getOptimalPosition(nextItem);

            VisualizationElement<T> newVisElement = new VisualizationElement<>(nextItem, optimalPosition, this);
            insert(newVisElement, optimalPosition);
        }
    }

    private Position getOptimalPosition(T nextItem){

        List<VisualizationElement<T>> elementsWithFreeNeigbors = getVisualizationItemWithFreeNeighbourhood();
        double minDist = Double.MAX_VALUE;
        VisualizationElement<T> optimalItem = null;
        for(VisualizationElement<T> elementWithFreeNeighbors : elementsWithFreeNeigbors){
            double actDist = distanceCalculator.distance(elementWithFreeNeighbors.getVector(), nextItem);
            if(actDist < minDist){
                optimalItem = elementWithFreeNeighbors;
                minDist = actDist;
            }
        }
        return optimalItem.getFirstFreeNeighborPosition();
    }

    public T getRepresentative() {
        return representative;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    private T getClosestElement(List<VisualizationElement<T>> elements) {
        double minDist = Double.MAX_VALUE;
        T closestElement = null;
        for(T otherVector : vectors){
            double actDist = 0;
            for(VisualizationElement<T> element : elements){
                actDist += distanceCalculator.distance(element.getVector(), otherVector);
            }
            if(actDist < minDist && !addedVectors.contains(otherVector)){
                closestElement = otherVector;
            }
        }
        return closestElement;
    }

    private void insert(VisualizationElement<T> newElement, Position position){
        if(plane[position.getX()][position.getY()] != null) logger.debug("The position (" + position.getX() + ", " + position.getY() + ") is already in use!");
        plane[position.getX()][position.getY()] = newElement;
        insertOrderVisualizationElement.add(newElement);
        addedVectors.add(newElement.getVector());
    }

    private List<VisualizationElement<T>> getVisualizationItemWithFreeNeighbourhood(){
        List<VisualizationElement<T>> visualizationElementsWithFreeNeighborhood = new ArrayList<>();
        for(VisualizationElement<T> vElement : insertOrderVisualizationElement){
            if(vElement.hasFreeNeighborTop() || vElement.hasFreeNeighborLeft() || vElement.hasFreeNeighborBottom() || vElement.HasFreeNeighborRight()){
                visualizationElementsWithFreeNeighborhood.add(vElement);
            }
        }
        return visualizationElementsWithFreeNeighborhood;
    }

    boolean isFreePosition(Position pos){
        if(pos.getX() < 0 || pos.getY() < 0) return false;

        if(pos.getX() < width && pos.getY() < height){
            if(plane[pos.getX()][pos.getY()] == null){
                return true;
            }
        }
        return false;
    }

    public String toHTML(){
        StringBuilder sb = new StringBuilder();
        sb.append("<table>");
        for(int i = 0; i < width; i++){
            sb.append("<tr>");
            for(int j = 0; j < height; j++){
                if(plane[i][j] != null){
                    sb.append("<td>");
                    sb.append(plane[i][j].print());
                    sb.append("</td>");

                } else {
                    sb.append("<td></td>");
                }

            }
            sb.append("</tr>").append(System.lineSeparator());
        }
        sb.append("</table>");
        return sb.toString();
    }

    public VisualizationElement<T> getVisElementAtPos(Position p){
        if(p.getX() < width && p.getY() < height && p.getX() >= 0 && p.getY() >= 0){
            return plane[p.getX()][p.getY()];
        }
        return null;
    }

    @Override
    public String print() {
        return toHTML();
    }
}
