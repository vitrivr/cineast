package org.vitrivr.cineast.core.util;

import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;
import boofcv.struct.image.GrayU8;
import georegression.struct.shapes.Quadrilateral_F64;
import org.vitrivr.cineast.core.data.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * MultiTracker allows for multiple objects to be tracked across frames.
 * 1. Initialize the MultiTracker with the initial frame and coordinates of the objects to be tracked
 * 2. Call update(GrayU8 frame) with the new frame to obtain the coordinates of the objects in the new frame
 */
public class MultiTracker {

  public enum TRACKER_TYPE {CIRCULANT, TLD}

  ;
  List<TrackerObjectQuad> trackers;
  List<Quadrilateral_F64> coordinates;
  GrayU8 initialFrame;

  /**
   * @param frame       The initialization frame
   * @param coordinates The coordinates within the initialization frame it should track
   * @param type        The type of the tracker
   */
  public MultiTracker(GrayU8 frame, List<Quadrilateral_F64> coordinates, TRACKER_TYPE type) {
    this.trackers = new ArrayList<>();
    this.coordinates = new ArrayList<>();
    this.initialFrame = frame;
    for (int i = 0; i < coordinates.size(); i++) {
      this.add(coordinates.get(i), type);
    }
  }

  /**
   * @param coordinate Coordinate of object that should be tracked
   * @param type       Type of the tracker
   */
  public void add(Quadrilateral_F64 coordinate, TRACKER_TYPE type) {
    TrackerObjectQuad tracker = null;
    if (type == TRACKER_TYPE.TLD) {
      tracker = FactoryTrackerObjectQuad.tld(null, GrayU8.class);
    } else if (type == TRACKER_TYPE.CIRCULANT) {
      tracker = FactoryTrackerObjectQuad.circulant(null, GrayU8.class);
    } else {
      // Defaulting to CIRCULANT
      tracker = FactoryTrackerObjectQuad.circulant(null, GrayU8.class);
    }

    tracker.initialize(this.initialFrame, coordinate);
    this.trackers.add(tracker);
    this.coordinates.add(coordinate);
  }

  /**
   * @param frame The new frame in which the objects should be located
   * @return A list of the new coordinates. If the first value of the pair is false, the object was not found
   */
  public List<Pair<Boolean, Quadrilateral_F64>> update(GrayU8 frame) {
    List<Pair<Boolean, Quadrilateral_F64>> result = new ArrayList<>();
    for (int i = 0; i < trackers.size(); i++) {
      Quadrilateral_F64 new_coord = this.coordinates.get(i).copy();
      @SuppressWarnings("unchecked")
      boolean found = this.trackers.get(i).process(frame, new_coord);
      if (new_coord.getA().x < 0) {
        new_coord.getA().x = 0;
        new_coord.getD().x = 0;
      }
      if (new_coord.getB().x > frame.width) {
        new_coord.getB().x = frame.width;
        new_coord.getC().x = frame.width;
      }
      if (new_coord.getA().y < 0) {
        new_coord.getB().y = 0;
        new_coord.getA().y = 0;
      }
      if (new_coord.getD().y > frame.height) {
        new_coord.getD().y = frame.height;
        new_coord.getC().y = frame.height;
      }
      result.add(new Pair<>(found, found ? new_coord : null));
    }
    return result;
  }
}
