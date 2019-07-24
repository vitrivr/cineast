package org.vitrivr.cineast.core.data;

import java.io.Serializable;

/**
 * Created by silvanstich on 04.10.16.
 */
public class Position implements Serializable {

  private static final long serialVersionUID = -4360148820779089159L;
  public final int x;
  public final int y;

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

  public Position[] getNeighborPositions() {
    return new Position[] { getPosTop(), getPosLeft(), getPosBottom(), getPosRight() };
  }

  @Override
  public int hashCode() {
    return (x >>> 16) | (x << 16) | y;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Position other = (Position) obj;
    if (x != other.x) {
      return false;
    }
    if (y != other.y) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Position is (" + x + "," + y + ")";
  }
}
