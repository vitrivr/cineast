package org.vitrivr.cineast.explorative;


import org.vitrivr.cineast.core.data.hct.HCTVisualizer;

class VisualizationElement<T extends Printable> implements Printable {

    private final Position position;
    private final T vector;
    private final Plane plane;
    private Position posTop;
    private Position posBottom;
    private Position posLeft;
    private Position posRight;

    VisualizationElement(T vector, Position position, Plane plane) {
        this.vector = vector;
        this.position = position;
        this.plane = plane;
        posTop = new Position(position.getX(), position.getY() + 1);
        posRight = new Position(position.getX() + 1, position.getY());
        posBottom = new Position(position.getX(), position.getY() - 1);
        posLeft = new Position(position.getX() - 1, position.getY());
    }

    public T getVector() {
        return vector;
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

    @Override
    public String print() {
        if(HCTVisualizer.segments.get(vector.print()) == null) return vector.print();
        return "<img src= /Applications/XAMPP/htdocs/vitrivr-ui/thumbnails/" + HCTVisualizer.segments.get(vector.print()) + "/" + vector.print() + ".jpg></img>";
    }
}
