package org.vitrivr.cineast.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.ReadableFloatVector;

public strictfp class KMeansPP {

  private KMeansPP() {
  }

  public static class KMenasResult<T extends ReadableFloatVector> {
    private final int k;
    private final double[] distances;
    private double distance;
    private ArrayList<FloatVector> centers;
    private ArrayList<ArrayList<T>> points;

    private static final Comparator<ArrayList<?>> comp = new Comparator<ArrayList<?>>() {

      @Override
      public int compare(ArrayList<?> o1, ArrayList<?> o2) {
        return Integer.compare(o2.size(), o1.size());
      }

    };

    private KMenasResult(int k) {
      this.k = k;
      this.distances = new double[this.k];
    }

    public int getK() {
      return this.k;
    }

    private void setCenters(ArrayList<FloatVector> c) {
      this.centers = c;
    }

    private void setPoints(ArrayList<ArrayList<T>> p) {
      this.points = p;
    }

    public void sort() {
      Collections.sort(points, comp);
      for (int i = 0; i < points.size(); ++i) {
        double dist = 0;
        for (T point : points.get(i)) {
          dist += ReadableFloatVector.getEuclideanDistance(centers.get(i), point);
        }
        distances[i] = dist;
        distance += dist;
      }
    }

    public ArrayList<FloatVector> getCenters() {
      return this.centers;
    }

    public ArrayList<ArrayList<T>> getPoints() {
      return this.points;
    }

    public double getDistance() {
      return this.distance;
    }

    public double getDistance(int i) {
      return this.distances[i];
    }

  }

  private static final Random random = new Random(1);

  /**
   * performs {@link KMeansPP} runs times and returns the result with the
   * minimal overall distance
   */
  public static <T extends ReadableFloatVector> KMenasResult<T> bestOfkMeansPP(
      List<T> elements, FloatVector helper, int k, double minDist, int runs) {
    double bestDist = Double.POSITIVE_INFINITY;
    KMenasResult<T> _return = null;
    for (int i = 0; i < runs; ++i) {
      KMenasResult<T> result = kMeansPP(elements, helper, k, minDist,
          random.nextLong());
      if (result.getDistance() < bestDist) {
        _return = result;
        bestDist = _return.getDistance();
      }
    }
    return _return;

  }

  public static <T extends FloatVector> KMenasResult<T> kMeansPP(
      List<T> elements, FloatVector helper, int k, double minDist) {
    return kMeansPP(elements, helper, k, minDist, 3914511920l);
  }

  public static <T extends ReadableFloatVector> KMenasResult<T> kMeansPP(
      List<T> elements, FloatVector helper, int k, double minDist, long randomSeed) {
    // init centers
    ArrayList<FloatVector> centers = getSeeds(elements, k, randomSeed);

    // init clusters
    ArrayList<ArrayList<T>> points = new ArrayList<ArrayList<T>>(k);
    for (int i = 0; i < k; ++i) {
      points.add(new ArrayList<T>(elements.size() / 2));
    }
    int iter = 0, maxIter = 50;
    double dist = 0;
    do {
      for (int i = 0; i < k; ++i) {
        points.get(i).clear();
      }

      for (T element : elements) {
        int j = 0;
        double min = ReadableFloatVector.getEuclideanDistance(element, centers.get(0));
        for (int i = 1; i < centers.size(); ++i) {
          double d = ReadableFloatVector.getEuclideanDistance(element, centers.get(i));
          if (d < min) {
            min = d;
            j = i;
          }
        }
        points.get(j).add(element);
      }

      dist = 0;
      for (int i = 0; i < centers.size(); ++i) {
        helper = ColorUtils.getAvg(points.get(i), helper);
        FloatVector center = centers.get(i);
        dist += ReadableFloatVector.getEuclideanDistance(helper, center);
        for (int j = 0; j < center.getElementCount(); ++j) {
          center.setElement(j, helper.getElement(j));
        }
        centers.set(i, center);
      }
      ++iter;
    } while (dist > minDist && iter < maxIter);

    KMenasResult<T> result = new KMenasResult<T>(k);
    result.setCenters(centers);
    result.setPoints(points);
    result.sort();

    return result;
  }

  // k-means++ part inspired by
  // http://rosettacode.org/wiki/K-means%2B%2B_clustering#Go
  private static <T extends ReadableFloatVector> ArrayList<FloatVector> getSeeds(
      List<T> elements, int k, long randomSeed) {
    Random rand = new Random(randomSeed);
    ArrayList<FloatVector> _return = new ArrayList<FloatVector>(k);
    if (elements.isEmpty()) {
      return _return;
    }
    int j = rand.nextInt(elements.size());
    _return.add(copyVector(elements.get(j)));

    double[] distances = new double[elements.size()];

    for (int i = 1; i < k; ++i) {
      double sum = 0;
      for (j = 0; j < distances.length; ++j) {
        double dMin = Double.POSITIVE_INFINITY;
        for (int ii = 0; ii < i; ++ii) {
          dMin = Math.min(dMin,
              ReadableFloatVector.getEuclideanDistance(_return.get(ii), elements.get(j)));
        }
        distances[j] = dMin * dMin;
        sum += distances[j];
      }

      double target = rand.nextDouble() * sum;
      j = 0;
      for (sum = distances[0]; sum < target; sum += distances[j]) {
        ++j;
      }
      _return.add(copyVector(elements.get(j)));
    }

    return _return;
  }

  private static final FloatVector copyVector(ReadableFloatVector v) {
    FloatVectorImpl _return = new FloatVectorImpl();
    for (int i = 0; i < v.getElementCount(); ++i) {
      _return.add(v.getElement(i));
    }
    return _return;
  }

}
