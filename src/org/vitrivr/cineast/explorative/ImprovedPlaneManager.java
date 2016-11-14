package org.vitrivr.cineast.explorative;

import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.hct.DistanceCalculation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImprovedPlaneManager<T extends Printable> implements TreeTraverserHorizontal<T> {

    private List<List<T>> valuesPerCell;
    private List<T> representativePerCell;
    private DistanceCalculation<T> distanceCalculation;
    List<Map<T, T>> representativeToParentRepresentativePerLevel = new ArrayList<>();
    private int level;
    private List<VisualizationElement<T>[][]> flatPlanes = new ArrayList<>();

    public ImprovedPlaneManager(DistanceCalculation distanceCalculation, String featureName){
        this.distanceCalculation = distanceCalculation;
    }

    @Override
    public void start() {

    }

    @Override
    public void newLevel() {
        valuesPerCell = new ArrayList<>();
        representativePerCell = new ArrayList<T>();
        level++;
        representativeToParentRepresentativePerLevel.add(new HashMap<>());
    }

    @Override
    public void newCell() {

    }

    @Override
    public void processValues(List<T> values, T representativeValue, T parentRepresentativeValue) {
        valuesPerCell.add(values);
        representativePerCell.add(representativeValue);
        if(parentRepresentativeValue != null) {
            representativeToParentRepresentativePerLevel.get(level-1).put(representativeValue, parentRepresentativeValue);
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
            flatPlanes.add(createFlatPlane(wholePlane));
        }

    }

    @Override
    public void finished() {
        File path = new File("results/html/experimental/");
        if (!path.exists()) path.mkdirs();

        for(int i = 0; i < level; i++){
            VisualizationElement<T>[][] flatPlane = createParentFlatPlane(flatPlanes.get(i), i);
            flatPlanes.add(flatPlane);
            print(new File(path, "improvedFlatPlane_level_" + i + ".html"), flatPlanes.get(i));
        }
    }

    private VisualizationElement<T>[][] createParentFlatPlane(VisualizationElement<T>[][] childFlatPlane, int level){
        VisualizationElement<T>[][] parentFlatPlane = new VisualizationElement[childFlatPlane.length][childFlatPlane[0].length];
        for(int x =0; x < childFlatPlane.length; x++){
            for(int y = 0; y < childFlatPlane[0].length; y++){
                if (childFlatPlane[x][y] == null) continue;
                T key = childFlatPlane[x][y].getVector();
                if (representativeToParentRepresentativePerLevel.get(level).containsKey(key)){
                    T parentKey = representativeToParentRepresentativePerLevel.get(level).get(key);
                    parentFlatPlane[x][y] = new VisualizationElement<T>(key, new Position(x, y), null);
                }
            }
        }
        return parentFlatPlane;
    }

    private VisualizationElement<T>[][] createFlatPlane(Plane<Plane<T>> plane) {
        int maxX = 0;
        int maxY = 0;
        int i = 0;
        int j = 0;
        for(i = 0; i < plane.getPlane().length; i++){
            for(j = 0; j < plane.getPlane()[i].length; j++){
                if(plane.getPlane()[i][j] != null && plane.getPlane()[i][j].getVector() != null){
                    int width = plane.getPlane()[i][j].getVector().getWidth();
                    int height = plane.getPlane()[i][j].getVector().getHeight();
                    if(width > maxX){
                        maxX = width;
                    }
                    if(height > maxY){
                        maxY = height;
                    }
                }
            }
        }

        VisualizationElement<T>[][] flatPlane = new VisualizationElement[i * maxX][j * maxY];
        for(int x = 0; x < plane.getPlane().length; x++){
            for(int y = 0; y < plane.getPlane()[x].length; y++){
                if(plane.getPlane()[x][y] == null || plane.getPlane()[x][y].getVector() == null) continue;
                for(int tempX = 0; tempX < plane.getPlane()[x][y].getVector().getWidth(); tempX++){
                    for(int tempY = 0; tempY < plane.getPlane()[x][y].getVector().getHeight(); tempY++){
                        VisualizationElement<T> element = plane.getPlane()[x][y].getVector().getPlane()[tempX][tempY];
                        flatPlane[x * maxX + tempX][y *maxY + tempY] = element;
                    }
                }
            }
        }
        return flatPlane;
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

    private void print(File fileExperimental, VisualizationElement<T>[][] flatPlane){
        try {
            PrintWriter pw = new PrintWriter(fileExperimental);
            pw.print("<html><head><link rel=\"stylesheet\" href=\"/Users/silvanstich/IdeaProjects/cineast_new/results/html/style.css\"></head><body><table>");
            for(int x = 0; x < flatPlane.length; x++){
                pw.print("<tr>");
                for(int y = 0; y < flatPlane[x].length; y++){
                    if(flatPlane[x][y] != null){
                        pw.print("<td>");
                        pw.print(flatPlane[x][y].getVector().printHtml());
                        pw.print("</td>");
                    } else {
                        pw.append("<td></td>");
                    }
                }
                pw.append("</tr>");
            }
            pw.print("</table></body></html>");
            pw.flush();
            pw.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

}
