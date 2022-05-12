package org.vitrivr.cineast.core.util.pose;

import georegression.struct.point.Point2D_F32;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.vitrivr.cineast.core.data.Skeleton;

public class MergingPoseDetector implements PoseDetector {

  private static final float weightThreshold = 0.25f;
  private final PoseDetector pd1, pd2;

  public MergingPoseDetector(PoseDetector pd1, PoseDetector pd2) {
    this.pd1 = pd1;
    this.pd2 = pd2;
  }

  public MergingPoseDetector() {
    this(new OpenPoseDetector(), new MovenetMultiposeDetector());
  }

  private static boolean matches(Skeleton skeleton1, Skeleton skeleton2) {

    float distanceThreshold = 0.2f * Math.max(size(skeleton1), size(skeleton2));
    distanceThreshold *= distanceThreshold;

    int matchCount = 0;
    int mismatchCount = 0;

    for (Skeleton.SkeletonPointName point : Skeleton.SkeletonPointName.values()) {

      if (skeleton1.getWeight(point) >= weightThreshold && skeleton2.getWeight(point) >= weightThreshold) {

        Point2D_F32 p1 = skeleton1.getPoint(point);
        Point2D_F32 p2 = skeleton2.getPoint(point);

        if (p1.distance2(p2) > distanceThreshold) {
          ++mismatchCount;
        } else {
          ++matchCount;
        }
      }
    }

    return matchCount >= 3 && matchCount >= 2 * mismatchCount;

  }

  private static float size(Skeleton skeleton) {

    float xmin = Float.MAX_VALUE, ymin = Float.MAX_VALUE, xmax = 0, ymax = 0;

    int pointCount = 0;

    for (Skeleton.SkeletonPointName point : Skeleton.SkeletonPointName.values()) {

      if (skeleton.getWeight(point) >= weightThreshold) {
        Point2D_F32 p = skeleton.getPoint(point);

        xmin = Math.min(p.x, xmin);
        ymin = Math.min(p.x, ymin);
        xmax = Math.max(p.x, xmax);
        ymax = Math.max(p.x, ymax);

        ++pointCount;

      }
    }

    if (pointCount < 2) {
      return 0f;
    }

    return Math.max(xmax - xmin, ymax - ymin);

  }

  private static Skeleton merge(Skeleton skeleton1, Skeleton skeleton2) {

    Skeleton merged = new Skeleton();

    for (Skeleton.SkeletonPointName point : Skeleton.SkeletonPointName.values()) {

      if (skeleton1.getWeight(point) >= weightThreshold && skeleton2.getWeight(point) >= weightThreshold) {

        float weight = Math.max(skeleton1.getWeight(point), skeleton2.getWeight(point));
        Point2D_F32 p = skeleton1.getPoint(point).plus(skeleton2.getPoint(point)).times(0.5f);
        merged.setPointWeighted(point, weight, p);

      } else if (skeleton1.getWeight(point) >= weightThreshold) {
        merged.setPointWeighted(point, skeleton1.getWeight(point), skeleton1.getPoint(point));
      } else if (skeleton2.getWeight(point) >= weightThreshold) {
        merged.setPointWeighted(point, skeleton2.getWeight(point), skeleton2.getPoint(point));

      }
    }

    return merged;

  }

  private static List<Skeleton> merge(List<Skeleton> list1, List<Skeleton> list2) {

    if (list1.isEmpty()) {
      return list2;
    }

    if (list2.isEmpty()) {
      return list1;
    }

    ArrayList<Skeleton> merged = new ArrayList<>();

    for (Skeleton skeleton1 : list1) {
      Skeleton toAdd = skeleton1;

      Iterator<Skeleton> iter = list2.iterator();

      while (iter.hasNext()) {
        Skeleton skeleton2 = iter.next();

        if (matches(skeleton1, skeleton2)) {
          toAdd = merge(skeleton1, skeleton2);
          iter.remove();
          break;
        }
      }

      merged.add(toAdd);
    }

    merged.addAll(list2);

    return merged;

  }

  @Override
  public List<Skeleton> detectPoses(BufferedImage img) {
    return merge(pd1.detectPoses(img), pd2.detectPoses(img));
  }
}
