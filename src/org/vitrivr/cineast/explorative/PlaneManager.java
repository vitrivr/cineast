package org.vitrivr.cineast.explorative;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.hct.DistanceCalculation;
import org.vitrivr.cineast.core.data.hct.HCTVisualizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class PlaneManager<T extends Printable> implements TreeTraverserHorizontal<T> {

    private final DistanceCalculation<T> distanceCalculation;
    private final List<List<Plane<T>>> subPlanes = new ArrayList<>();
    private final String timestamp;
    private final static Logger logger = LogManager.getLogger();

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
                        flatPlane[x * maxX + tempX][y *maxY + tempY] = plane.getPlane()[x][y].getVector().getPlane()[tempX][tempY];
                    }
                }
            }
        }
        File path = new File("results/html/" + timestamp);
        if(!path.exists()) path.mkdirs();
        File fileNotOptimized = new File(path.getPath(), "level_notOpt_" + (subPlanes.size() - 1) + ".html");
        printFile(flatPlane, fileNotOptimized);

        rearrangeItems(flatPlane);

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

        File fileOptimized = new File(path.getPath(), "level_opt_" + (subPlanes.size() - 1) + ".html");
        printFile(flatPlane, fileOptimized);
    }

    private void printFile(VisualizationElement<T>[][] flatPlane, File fileOptimized) {
        try {
            PrintWriter pw = new PrintWriter(fileOptimized);
            pw.print("<html><head><link rel=\"stylesheet\" href=\"/Users/silvanstich/IdeaProjects/cineast_new/results/html/style.css\"></head><body><table>");
            for(int x = 0; x < flatPlane.length; x++){
                pw.print("<tr>");
                for(int y = 0; y < flatPlane[x].length; y++){
                    if(flatPlane[x][y] != null){
                        pw.print("<td>");
                        pw.print(flatPlane[x][y].getVector().print());
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

    private void rearrangeItems(VisualizationElement<T>[][] flatPlane){
        boolean doAgain = true;
        long counter = 0;
        while(doAgain){
            doAgain = false;
            counter++;
            for(int i = 0; i < flatPlane.length; i++){
                for(int j = 0; j < flatPlane[i].length; j++){
                    List<Direction> directions = new ArrayList<>();
                    if(flatPlane[i][j] == null) continue;
                    int middleX = flatPlane.length / 2;
                    int middleY = flatPlane.length / 2;
                    if(i < middleX && i < flatPlane.length - 1) directions.add(Direction.RIGHT);
                    if(i > middleX && i > 0) directions.add(Direction.LEFT);
//                    if(j < middleY && j < flatPlane[i].length - 1) directions.add(Direction.DOWN);
//                    if(j > middleY && j > 0) directions.add(Direction.UP);
//                    if(cacheCounter % 2 == 0) Collections.reverse(directions);
                    boolean itemHasBeenMoved = moveItem(flatPlane, directions, i, j);
                    if(itemHasBeenMoved) doAgain = true;
                }
            }
        }
        doAgain = true;
        while(doAgain){
            doAgain = false;
            counter++;
            for(int i = 0; i < flatPlane.length; i++){
                for(int j = 0; j < flatPlane[i].length; j++){
                    List<Direction> directions = new ArrayList<>();
                    if(flatPlane[i][j] == null) continue;
                    int middleX = flatPlane.length / 2;
                    int middleY = flatPlane.length / 2;
//                    if(i < middleX && i < flatPlane.length - 1) directions.add(Direction.RIGHT);
//                    if(i > middleX && i > 0) directions.add(Direction.LEFT);
                    if(j < middleY && j < flatPlane[i].length - 1) directions.add(Direction.DOWN);
                    if(j > middleY && j > 0) directions.add(Direction.UP);
//                    if(cacheCounter % 2 == 0) Collections.reverse(directions);
                    boolean itemHasBeenMoved = moveItem(flatPlane, directions, i, j);
                    if(itemHasBeenMoved) doAgain = true;
                }
            }
        }
    }

    private boolean moveItem(VisualizationElement<T>[][] flatPlane, List<Direction> directions, int x, int y){
        int newX = x;
        int newY = y;
        for(Direction d : directions){
            if(d == Direction.RIGHT) newX = x + 1;
            if(d == Direction.LEFT) newX = x - 1;
            if(d == Direction.DOWN) newY = y + 1;
            if(d == Direction.UP) newY = y - 1;

            if(flatPlane[newX][newY] == null){
                logger.debug("Moved item " + d);
                flatPlane[newX][newY] = flatPlane[x][y];
                flatPlane[x][y] = null;
                return true;
            } else{
                newX = x;
                newY = y;
            }
        }
        return false;
    }

}
