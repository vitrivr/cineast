package org.vitrivr.cineast.core.util;

import georegression.struct.shapes.Quadrilateral_F64;
import org.vitrivr.cineast.core.data.Pair;

import java.util.*;

/**
 * TextStream is a helper class for the OCRSearch feature extraction
 * It stores the frame-coordinate pairs in which the text appears, as well as the extracted text itself
 */
public class TextStream {
    int first;
    int last;
    int coordinate_id;
    String text;
    private HashMap<Integer, Quadrilateral_F64> coordinates;

    /**
     * TextStream fuses the coordinates supplied by taking the average and sets its own coordinates accordingly
     * @param first the index of the first frame
     * @param last the index of the last frame
     * @param coordinate_id the id of the coordinate
     * @param coordinates1
     * @param coordinates2
     */
    public TextStream(int first, int last, int coordinate_id, List<Quadrilateral_F64> coordinates1, List<Quadrilateral_F64> coordinates2) {
        this.first = first;
        this.last = last;
        this.coordinate_id = coordinate_id;
        this.coordinates = new HashMap<>();
        add(first, last, coordinate_id, coordinates1, coordinates2);
    }

    /**
     * add takes another stream and adds its coordinates to this stream instance
     * @param stream The stream whose values should be added
     */
    public void add(TextStream stream) {
        if (this.first > stream.getFirst()) {
            this.first = stream.getFirst();
        }
        if (this.last < stream.getLast()) {
            this.last = stream.getLast();
        }
        @SuppressWarnings("unchecked")
        Set<Integer> keys = stream.getSet();
        Iterator<Integer> k = keys.iterator();

        while (k.hasNext()) {
            int key = k.next();
            this.coordinates.put(key, stream.getCoordinate(key));
        }
    }

    /**
     * add takes two lists of coordinates, fuses them and adds them to this instance's own coordinates
     * @param start index of the first frame
     * @param end index of the last frame
     * @param coordinate_id id of the coordinate
     * @param coordinates1
     * @param coordinates2
     */
    public void add(int start, int end, int coordinate_id, List<Quadrilateral_F64> coordinates1, List<Quadrilateral_F64> coordinates2) {
        for (int i=0; i<coordinates1.size(); i++) {
            if (coordinates1.get(i) == null || coordinates2.get(i) == null) {
                continue;
            }
            int x_min = (int) Math.round((coordinates1.get(i).getA().x + coordinates2.get(i).getA().x)/2);
            int x_max = (int) Math.round((coordinates1.get(i).getB().x + coordinates2.get(i).getB().x)/2);

            int y_min = (int) Math.round((coordinates1.get(i).getA().y + coordinates2.get(i).getA().y)/2);
            int y_max = (int) Math.round((coordinates1.get(i).getC().y + coordinates2.get(i).getC().y)/2);

            Quadrilateral_F64 new_location = new Quadrilateral_F64(x_min, y_min, x_max, y_min, x_max, y_max, x_min, y_max);
            this.coordinates.put(start+i, new_location);
        }

        if (end > this.last) {
            this.last = end;
            this.coordinate_id = coordinate_id;
        }
        if (start < this.first) {
            this.first = start;
        }
    }

    /**
     * @return The coordinates whose aspect ratio is above the 50th percentile
     */
    public HashMap<Integer, Quadrilateral_F64> getFilteredCoordinates() {
        HashMap<Integer, Quadrilateral_F64> filtered = new HashMap<>();
        Set<Integer> keySet = this.coordinates.keySet();
        Iterator<Integer> it = keySet.iterator();
        List<Pair<Integer, Double>> pairs = new ArrayList<>();
        List<Double> aspectRatios = new ArrayList<>();

        while (it.hasNext()) {
            int frameIndex = it.next();
            Quadrilateral_F64 coordinate = this.coordinates.get(frameIndex);
            double aspectRatio = Math.abs(coordinate.getB().x - coordinate.getA().x) / Math.abs(coordinate.getD().y - coordinate.getA().y);
            pairs.add(new Pair<>(frameIndex, aspectRatio));
            aspectRatios.add(aspectRatio);
        }
        Collections.sort(aspectRatios);

        double threshold = aspectRatios.get(Math.round(aspectRatios.size()/2));
        for (Pair<Integer, Double> pair : pairs) {
            if (pair.second >= threshold) {
                filtered.put(pair.first, this.coordinates.get(pair.first));
            }
        }
        return filtered;
    }

    public Set<Integer> getSet() {
        return this.coordinates.keySet();
    }
    public Quadrilateral_F64 getCoordinate(int key) {
        return coordinates.get(key);
    }
    public int getLast() {
        return this.last;
    }
    public int getFirst() {
        return this.first;
    }
    public int getCoordinate_id() {
        return this.coordinate_id;
    }
    public String getText() {
        return this.text;
    }
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return The coordinate of the first frame
     */
    public Quadrilateral_F64 findFirstBox() {
        int firstBox = Integer.MAX_VALUE;
        for (HashMap.Entry<Integer, Quadrilateral_F64> val : this.coordinates.entrySet()) {
            firstBox = firstBox > val.getKey() ? val.getKey() : firstBox;
        }
        return this.coordinates.get(firstBox);
    }

    /**
     * @return The coordinate of the last frame
     */
    public Quadrilateral_F64 findLastBox() {
        int lastBox = -1;
        for (HashMap.Entry<Integer, Quadrilateral_F64> val : this.coordinates.entrySet()) {
            lastBox = lastBox < val.getKey() ? val.getKey() : lastBox;
        }
        return this.coordinates.get(lastBox);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextStream that = (TextStream) o;
        return this.last == that.getLast() && this.coordinate_id == that.getCoordinate_id();
    }

    @Override
    public int hashCode() {
        return this.last * 31 + this.coordinate_id;
    }
}
