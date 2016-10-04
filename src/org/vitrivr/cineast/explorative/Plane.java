package org.vitrivr.cineast.explorative;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.hct.HCTVisualizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class Plane {

    private final VisualizationElement[][] plane;
    private final static Logger logger = LogManager.getLogger();
    private final List<VisualizationElement> insertOrderVisualizationElement;
    private final HashSet<HCTFloatVectorValue> addedVectors;
    private final int height;
    private final int width;
    private final List<HCTFloatVectorValue> vectors;
    private final FloatArrayEuclideanDistance distanceCalculator = new FloatArrayEuclideanDistance();

    public Plane(List<HCTFloatVectorValue> vectors){
        this.height = (int)Math.sqrt(vectors.size()) + 1;
        this.width = (int)Math.sqrt(vectors.size()) + 1;
        this.vectors = vectors;
        plane = new VisualizationElement[width][height];
        insertOrderVisualizationElement = new ArrayList<>();
        addedVectors = new HashSet<>();
    }

    public void processCollection(HCTFloatVectorValue startElement){
        Position startPos = new Position(width/2, height/2);
        insert(new VisualizationElement(startElement, startElement.getSegment_id(), startPos, this), startPos);

        while(insertOrderVisualizationElement.size() < vectors.size()){
            VisualizationElement firstVisElementWithFreeNeighbors = getVisualizationItemWithFreeNeighbourhood();
            HCTFloatVectorValue nextElement = getClosestElement(firstVisElementWithFreeNeighbors.getVector());
            Position firstFreePosition = firstVisElementWithFreeNeighbors.getFirstFreeNeighborPosition();
            VisualizationElement newVisElement = new VisualizationElement(nextElement, nextElement.getSegment_id(), firstFreePosition, this);
            insert(newVisElement, firstFreePosition);

        }


    }

    private HCTFloatVectorValue getClosestElement(HCTFloatVectorValue vector) {
        double minDist = Double.MAX_VALUE;
        HCTFloatVectorValue closestElement = null;
        for(HCTFloatVectorValue otherVector : vectors){
            double actDist = distanceCalculator.distance(vector, otherVector);
            if(actDist < minDist && otherVector != vector && !addedVectors.contains(otherVector)){
                closestElement = otherVector;
            }
        }
        return closestElement;
    }

    private void insert(VisualizationElement newElement, Position position){
        if(plane[position.getX()][position.getY()] != null) logger.debug("The position (" + position.getX() + ", " + position.getY() + ") is already in use!");
        plane[position.getX()][position.getY()] = newElement;
        insertOrderVisualizationElement.add(newElement);
        addedVectors.add(newElement.getVector());
    }

    public VisualizationElement getVisualizationItemWithFreeNeighbourhood(){
        for(VisualizationElement vElement : insertOrderVisualizationElement){
            if(vElement.hasFreeNeighborTop() || vElement.hasFreeNeighborLeft() || vElement.hasFreeNeighborBottom() || vElement.HasFreeNeighborRight()){
                return vElement;
            }
        }
        return null;
    }

    public boolean isFreePosition(Position pos){
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
        sb.append("<html><body><table>");
        for(int i = 0; i < width; i++){
            sb.append("<tr>");
            for(int j = 0; j < height; j++){
                if(plane[i][j] != null){
                    String segementId = plane[i][j].getKey();
                    String multimediaobject = HCTVisualizer.segments.get(segementId);
                    sb.append("<td>"+ "<img src= " + "/Applications/XAMPP/htdocs/vitrivr-ui/thumbnails/" + multimediaobject + "/" + segementId + ".jpg" + "> </img>" + "</td>");

                } else {
                    sb.append("<td></td>");
                }

            }
            sb.append("</tr>").append(System.lineSeparator());
        }
        sb.append("</table></body></html>");
        return sb.toString();
    }
}
