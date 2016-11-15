package org.vitrivr.cineast.explorative;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.hct.DistanceCalculation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImprovedPlaneManager<T extends Printable> extends PlaneManager<T>{

    private List<List<T>> valuesPerCell;
    private List<T> representativePerCell;
    private DistanceCalculation<T> distanceCalculation;
    List<Map<T, T>> representativeToParentRepresentativePerLevel = new ArrayList<>();
    private List<Map<T,T>> elementToRepresentative = new ArrayList<>();
    private int level;
    private List<VisualizationElement<T>[][]> nonOptimizedPlanes = new ArrayList<>();
    private final Logger LOGGER = LogManager.getLogger();

    public ImprovedPlaneManager(DistanceCalculation distanceCalculation, String featureName){
        super(distanceCalculation, featureName);
        this.distanceCalculation = distanceCalculation;
    }

    @Override
    public void start() {

    }

    @Override
    public void newLevel() {
        valuesPerCell = new ArrayList<>();
        representativePerCell = new ArrayList<T>();
        representativeToParentRepresentativePerLevel.add(new HashMap<>());
        elementToRepresentative.add(new HashMap<T, T>());
    }

    @Override
    public void newCell() {

    }

    @Override
    public void processValues(List<T> values, T representativeValue, T parentRepresentativeValue) {
        valuesPerCell.add(values);
        representativePerCell.add(representativeValue);
        for(T val : values) elementToRepresentative.get(level).put(val, representativeValue);
        if(parentRepresentativeValue != null) {
            representativeToParentRepresentativePerLevel.get(level).put(representativeValue, parentRepresentativeValue);
        }
    }

    @Override
    public void endCell() {

    }

    @Override
    public void endLevel(int levelNo) {
        if (levelNo == 0){
            // base level
            List<Plane<T>> planes = new ArrayList<>();
            for (int i = 0; i < valuesPerCell.size(); i++){
                Plane<T> plane = new Plane<>(valuesPerCell.get(i), distanceCalculation, representativePerCell.get(i));
                plane.processCollection();
                planes.add(plane);
            }
            Plane<Plane<T>> wholePlane = new Plane<>(planes, (point1, point2) -> distanceCalculation.distance(point1.getRepresentative(), point2.getRepresentative()), getMiddleElement(planes));
            wholePlane.processCollection();
            nonOptimizedPlanes.add(createFlatPlane(wholePlane));
        }
    level++;
    }

    @Override
    public void finished() {
        File path = new File("results/html/experimental/");
        if (!path.exists()) path.mkdirs();

        for(int i = 0; i < level; i++){
            VisualizationElement<T>[][] flatPlane = createParentFlatPlane(nonOptimizedPlanes.get(i), i);
            nonOptimizedPlanes.add(flatPlane);
            printFile(nonOptimizedPlanes.get(i), new File(path, "improvedFlatPlane_level_" + i + ".html"));
            VisualizationElement<T>[][] optimizedPlane = rearrangeItems(nonOptimizedPlanes.get(i));
            printFile(optimizedPlane, new File(path, "improvedOptimizedFlatPlane_level_" + i + ".html"));
            saveElementsAndPositions(optimizedPlane);
            flatPlanes.add(optimizedPlane);
        }
        LOGGER.info("# of flat planes is " + flatPlanes.size());
        try {
            serialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private VisualizationElement<T>[][] createParentFlatPlane(VisualizationElement<T>[][] childFlatPlane, int level){
        VisualizationElement<T>[][] parentFlatPlane = new VisualizationElement[childFlatPlane.length][childFlatPlane[0].length];
        for(int x = 0; x < childFlatPlane.length; x++){
            for(int y = 0; y < childFlatPlane[0].length; y++){
                if (childFlatPlane[x][y] == null) continue;
                T key = childFlatPlane[x][y].getVector();
                if(elementToRepresentative.get(level).containsKey(key)){
                    T representative = elementToRepresentative.get(level).get(key);
                    childFlatPlane[x][y].setRepresentative(representative);
                }
                if (representativeToParentRepresentativePerLevel.get(level).containsKey(key)){
                    T parentKey = representativeToParentRepresentativePerLevel.get(level).get(key);
                    parentFlatPlane[x][y] = new VisualizationElement<>(key, new Position(x, y), parentKey);
                }

            }
        }
        return parentFlatPlane;
    }



}
