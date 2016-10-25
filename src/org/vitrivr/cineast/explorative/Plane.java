package org.vitrivr.cineast.explorative;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.hct.DistanceCalculation;

import java.util.*;

enum Direction {UP, LEFT, DOWN, RIGHT}

public class Plane<T extends Printable> implements Printable {

    private final VisualizationElement<T>[][] plane;
    private final static Logger logger = LogManager.getLogger();
    private final List<VisualizationElement<T>> insertOrderVisualizationElement;
    private final HashSet<T> addedVectors;
    private final int height;
    private final int width;
    private final List<T> vectors;
    private final Iterator<T> iterator;
    private final DistanceCalculation<T> distanceCalculator;
    private final T representative;
    private VisualizationElement<T> startItem;

    public Plane(List<T> vectors, DistanceCalculation<T> distanceCalculator, T representative){
        this.height = ((int)Math.sqrt(vectors.size()) + 1);
        this.width = ((int)Math.sqrt(vectors.size()) + 1);
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
        startItem = new VisualizationElement<>(representative, startPos, this);
        insert(startItem, startPos);

//        Random rand = new Random();
//        for(int i = 0; i < 10 && i < vectors.size(); i++){
//            int x;
//            int y;
//            x = rand.nextInt(width);
//            y = rand.nextInt(height);
//            T item = vectors.get(rand.nextInt(vectors.size()));
//            vectors.remove(item);
//            Position pos = new Position(x,y);
//            while(!isFreePosition(pos)){
//                x = rand.nextInt(width);
//                y = rand.nextInt(height);
//                pos = new Position(x,y);
//            }
//            insert(new VisualizationElement<T>(item, pos, this), pos);
//        }

        while(addedVectors.size() < vectors.size()){
            Pair<Position, T> optimalItemAndPosition = getOptimalItemAndPosition();
            T nextItem = optimalItemAndPosition.second;
            Position optimalPosition = optimalItemAndPosition.first;

            VisualizationElement<T> newVisElement = new VisualizationElement<>(nextItem, optimalPosition, this);
            insert(newVisElement, optimalPosition);
        }
//        boolean hasMoved = true;
//        while(hasMoved){
//            hasMoved = rearrangeItems();
//            logger.debug("Rearranging items");
//        }
    }

    private boolean rearrangeItems() {
        boolean atLeastOneItemHasBeenMoved = false;
        for(VisualizationElement<T> item : insertOrderVisualizationElement){
            boolean hasBeenMoved = false;
            Position p = item.getPosition();
            Position center = new Position(width/2, height/2);
            Direction[] directions = new Direction[2];
            if(p.getX() > center.getX()) directions[0] = Direction.LEFT;
            if(p.getX() < center.getX()) directions[0] = Direction.RIGHT;
            if(p.getY() > center.getY()) directions[1] = Direction.DOWN;
            if(p.getY() < center.getY()) directions[1] = Direction.UP;
            hasBeenMoved = moveItem(item, directions);
            if(hasBeenMoved) atLeastOneItemHasBeenMoved = true;

        }
        return atLeastOneItemHasBeenMoved;
    }

    private boolean moveItem(VisualizationElement<T> item, Direction[] directions){
        for(Direction d : directions){
            if(d == null) continue;
            Position oldPos = item.getPosition();
            Position newPos = null;
            if(d == Direction.UP){
                newPos = new Position(oldPos.getX(), oldPos.getY() + 1);
            }
            if(d == Direction.LEFT){
                newPos = new Position(oldPos.getX() - 1, oldPos.getY());
            }
            if(d == Direction.DOWN){
                newPos = new Position(oldPos.getX(), oldPos.getY() - 1);
            }
            if(d == Direction.RIGHT){
                newPos = new Position(oldPos.getX() + 1, oldPos.getY());
            }
            if(isFreePosition(newPos)){
                plane[oldPos.getX()][oldPos.getY()] = null;
                item.setPosition(newPos);
                plane[newPos.getX()][newPos.getY()] = item;
                return true;
            }
        }
        return false;
    }

    private Pair<Position, Double> getOptimalPosition(T nextItem){

        List<VisualizationElement<T>> elementsWithFreeNeigbors = getVisualizationItemWithFreeNeighbourhood();
        double minDist = Double.MAX_VALUE;
        Position optimalPosition = null;
        for(VisualizationElement<T> elementWithFreeNeighbors : elementsWithFreeNeigbors){
            for(Position p : elementWithFreeNeighbors.getPosition().getNeighbors()){
                if(p.getX() < 0 || p.getY() < 0 || p.getX() >= width || p.getY() >= height) continue;
                double actDist = 0;
                int nbrOfNeighbors = 0;
                if(getVisElementAtPos(p) != null) continue;
                for(Position pos : p.getNeighbors()){
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
        for (T vector : vectors) {
            if(addedVectors.contains(vector)) continue;
            Pair<Position, Double> optmimumPerVektor = getOptimalPosition(vector);
            if(minDist > optmimumPerVektor.second){
                minDist = optmimumPerVektor.second;
                optimalPosition = optmimumPerVektor.first;
                optimalVector = vector;
            }
        }
        return new Pair<>(optimalPosition, optimalVector);
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

    public VisualizationElement<T>[][] getPlane() {
        return plane;
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
