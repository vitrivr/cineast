package org.vitrivr.cineast.explorative;


public class VisualizationElement {

    private final Position position;
    private final String key;
    private final HCTFloatVectorValue vector;
    private final Plane plane;
    private Position posTop;
    private Position posBottom;
    private Position posLeft;
    private Position posRight;

    public VisualizationElement(HCTFloatVectorValue vector, String key, Position position, Plane plane) {
        this.vector = vector;
        this.key = key;
        this.position = position;
        this.plane = plane;
        posTop = new Position(position.getX(), position.getY() + 1);
        posRight = new Position(position.getX() + 1, position.getY());
        posBottom = new Position(position.getX(), position.getY() - 1);
        posLeft = new Position(position.getX() - 1, position.getY());
    }

    public HCTFloatVectorValue getVector() {
        return vector;
    }

    public String getKey() {
        return key;
    }

    public Position getPosition() {
        return position;
    }

    public boolean hasFreeNeighborTop(){
        return plane.isFreePosition(posRight);
    }

    public boolean hasFreeNeighborLeft(){
        return plane.isFreePosition(posLeft);
    }

    public boolean hasFreeNeighborBottom(){
        return plane.isFreePosition(posBottom);
    }

    public boolean HasFreeNeighborRight(){
        return plane.isFreePosition(posRight);
    }

    public Position getFirstFreeNeighborPosition(){
        if(plane.isFreePosition(posTop)) return posTop;
        if(plane.isFreePosition(posLeft)) return posLeft;
        if(plane.isFreePosition(posBottom)) return posBottom;
        if(plane.isFreePosition(posRight)) return posRight;
        throw new RuntimeException("This is an element without free neighborhood!");
    }
}
