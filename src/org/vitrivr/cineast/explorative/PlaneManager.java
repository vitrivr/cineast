package org.vitrivr.cineast.explorative;

import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.hct.DistanceCalculation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by silvanstich on 05.10.16.
 */
public class PlaneManager<T extends Printable> implements TreeTraverserHorizontal<T> {

    private final DistanceCalculation<T> distanceCalculation;
    private final List<List<Plane<T>>> subPlanes = new ArrayList<>();
    private final String timestamp;
    public PlaneManager(DistanceCalculation<T> distanceCalculation) {
        this.distanceCalculation = distanceCalculation;
        timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date(System.currentTimeMillis()));
    }

    @Override
    public void newLevel() {
        subPlanes.add(new ArrayList<>());
    }

    @Override
    public void newCell() {

    }

    @Override
    public void processValues(List<T> values, T representative) {
        Plane<T> plane = new Plane<T>(values, distanceCalculation, representative);
        plane.processCollection();
        subPlanes.get(subPlanes.size() - 1 ).add(plane);
    }

    @Override
    public void endCell() {

    }

    @Override
    public void endLevel() {
        List<Plane<T>> actSubPlanes = subPlanes.get(subPlanes.size() - 1);
        Plane<Plane<T>> plane = new Plane<>(actSubPlanes, (point1, point2) -> distanceCalculation.distance(point1.getRepresentative(), point2.getRepresentative()), getMiddleElement(actSubPlanes));
        plane.processCollection();



        File path = new File("results/html/" + timestamp);
        if(!path.exists()) path.mkdirs();
        File file = new File(path.getPath(), "level_" + (subPlanes.size() - 1) + ".html");
        try {
            PrintWriter printWriter = new PrintWriter(file);
            printWriter.print("<html><body>");
            printWriter.print(plane.print());
            printWriter.print("</body></html>");
            printWriter.flush();
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Plane<T> getMiddleElement(List<Plane<T>> planes){
        List<Pair<Plane<T>, Double>> distances = new ArrayList<>();
        for(Plane<T> plane : planes){
            double tempDist = 0;
            for(Plane<T> innerPlane : planes){
                if(plane != innerPlane){
                    tempDist += distanceCalculation.distance(plane.getRepresentative(), innerPlane.getRepresentative());
                }
            }
            distances.add(new Pair<>(plane, tempDist));
        }
        double minDist = Double.MAX_VALUE;
        Plane<T> middleElement = null;
        for(Pair<Plane<T>, Double> pair : distances){
            if(pair.second < minDist){
                minDist = pair.second;
                middleElement = pair.first;
            }
        }
        return middleElement;
    }
}
