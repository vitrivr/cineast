package org.vitrivr.cineast.explorative;

import java.io.Serializable;

/**
 * Created by silvanstich on 04.10.16.
 */
public class Position implements Serializable{

    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Position getPosBottom() {
        return new Position(x, y - 1);
    }

    public Position getPosLeft() {
        return new Position(x - 1, y);
    }

    public Position getPosRight() {
        return new Position(x + 1, y);
    }

    public Position getPosTop() {
        return new Position(x, y + 1);
    }

    public Position[] getNeighborPositions(){
        return new Position[]{getPosTop(), getPosLeft(), getPosBottom(), getPosRight()};
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Position){
            if(((Position) obj).x == this.x && ((Position) obj).y == y){
                return true;
            } else{
                return false;
            }
        }
        return super.equals(obj);
    }

    @Override
    public String toString(){
        return "Position is (" + x + "," + y + ")";
    }
}
