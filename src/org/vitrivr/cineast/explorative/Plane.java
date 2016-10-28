package org.vitrivr.cineast.explorative;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.hct.DistanceCalculation;

import java.util.*;

public class Plane<T extends Printable> implements Printable {

    private final VisualizationElement<T>[][] plane;
    private final static Logger logger = LogManager.getLogger();
    private final List<VisualizationElement<T>> insertedItemsWithFreeNeighbors;
    private final HashSet<T> addedVectors;
    private final List<T> remainingVectors = new ArrayList<>();
    private final int height;
    private final int width;
    private final List<T> vectors;
    private final DistanceCalculation<T> distanceCalculator;
    private final T representative;

    Plane(List<T> vectors, DistanceCalculation<T> distanceCalculator, T representative){
        this.height = (int) Math.ceil(Math.sqrt(vectors.size()));
        this.width = (int) Math.ceil(Math.sqrt(vectors.size()));
        this.vectors = vectors;
        this.remainingVectors.addAll(vectors);
        this.representative = representative;
        if(vectors.contains(representative)) vectors.remove(representative);
        this.distanceCalculator = distanceCalculator;
        plane = new VisualizationElement[width][height];
        insertedItemsWithFreeNeighbors = new ArrayList<>();
        addedVectors = new HashSet<>();
    }

    void processCollection(){
        Position startPos = new Position(width/2, height/2);
        VisualizationElement<T> startItem = new VisualizationElement<>(representative, startPos, this);
        vectors.remove(startItem);
        insert(startItem, startPos);
        Collections.shuffle(vectors, new Random(1));
        Iterator<T> iterator = vectors.iterator();
        while(addedVectors.size() < vectors.size()){
//            Pair<Position, T> optimalItemAndPosition = getOptimalItemAndPosition();

            T nextItem = iterator.next();
            Position optimalPosition = getOptimalPosition(nextItem).first;

            VisualizationElement<T> newVisElement = new VisualizationElement<>(nextItem, optimalPosition, this);
            insert(newVisElement, optimalPosition);
        }
    }

    private Pair<Position, Double> getOptimalPosition(T nextItem){

        double minDist = Double.MAX_VALUE;
        Position optimalPosition = null;
        Iterator<VisualizationElement<T>> iterator = insertedItemsWithFreeNeighbors.iterator();
        while(iterator.hasNext()){
            VisualizationElement<T> elementWithFreeNeighbors = iterator.next();
            if(!elementWithFreeNeighbors.hasFreeNeighborTop() && !elementWithFreeNeighbors.hasFreeNeighborLeft()
                    && !elementWithFreeNeighbors.hasFreeNeighborBottom() && !elementWithFreeNeighbors.HasFreeNeighborRight()){
                iterator.remove();
                continue;
            }

            for(Position p : elementWithFreeNeighbors.getPosition().getNeighborPositions()){
                if(p.getX() < 0 || p.getY() < 0 || p.getX() >= width || p.getY() >= height) continue;
                double actDist = 0;
                int nbrOfNeighbors = 0;
                if(getVisElementAtPos(p) != null) continue;
                for(Position pos : p.getNeighborPositions()){
                    VisualizationElement<T> element = getVisElementAtPos(pos);
                    if(element != null){
                        actDist += distanceCalculator.distance(element.getVector(), nextItem);
                        nbrOfNeighbors++;
                    }
                }
                actDist = actDist / nbrOfNeighbors;
                if(actDist < minDist){
                    optimalPosition = p;
                    minDist = actDist;
                }
            }
        }
        return new Pair<>(optimalPosition, minDist);
    }

    private Pair<Position, T> getOptimalItemAndPosition(){
        T optimalVector = null;
        Position optimalPosition = null;
        double minDist = Double.MAX_VALUE;
        for (T vector : remainingVectors) {
            Pair<Position, Double> optimumPerVector = getOptimalPosition(vector);
            if(minDist > optimumPerVector.second){
                minDist = optimumPerVector.second;
                optimalPosition = optimumPerVector.first;
                optimalVector = vector;
            }
        }
        return new Pair<>(optimalPosition, optimalVector);
    }

    T getRepresentative() {
        return representative;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    VisualizationElement<T>[][] getPlane() {
        return plane;
    }

    private void insert(VisualizationElement<T> newElement, Position position){
        if(plane[position.getX()][position.getY()] != null) logger.info("The position (" + position.getX() + ", " + position.getY() + ") is already in use!");
        plane[position.getX()][position.getY()] = newElement;
        insertedItemsWithFreeNeighbors.add(newElement);
        addedVectors.add(newElement.getVector());
        remainingVectors.remove(newElement.getVector());
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

    VisualizationElement<T> getVisElementAtPos(Position p){
        if(p.getX() < width && p.getY() < height && p.getX() >= 0 && p.getY() >= 0){
            return plane[p.getX()][p.getY()];
        }
        return null;
    }

    @Override
    public String print() {
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
}
