package org.vitrivr.cineast.explorative;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.hct.DistanceCalculation;

import javax.swing.tree.ExpandVetoException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class PlaneManager<T extends Printable> implements TreeTraverserHorizontal<T>, Serializable {

    private transient final DistanceCalculation<T> distanceCalculation;
    private transient final List<List<Plane<T>>> subPlanes = new ArrayList<>();
    private transient final String timestamp;
    private transient final static Logger logger = LogManager.getLogger();
    private final String PATH = "data/serialized/";
    private final String FILE_NAME;
    private final List<VisualizationElement<T>[][]> flatPlanes = new ArrayList<>();
    private final List<Map<String, Position>> positionsOfElements = new ArrayList<>();
    private final List<Map<String, String>> representativeOfElements = new ArrayList<>();

    public PlaneManager(DistanceCalculation<T> distanceCalculation, String featureName) {
        this.distanceCalculation = distanceCalculation;
        this.FILE_NAME =  "plane_manager_" + featureName + ".ser".toLowerCase();
        timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date(System.currentTimeMillis()));
    }

    @Override
    public void start(){

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

        VisualizationElement<T>[][] flatPlane = createFlatPlane(plane);

        File path = writeNonOptimizedFile(flatPlane);

        rearrangeItems(flatPlane);

        saveElementsAndPositions(flatPlane);

        writeOptimizedFile(plane, flatPlane, path);
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
        HashMap<String, String> representativeOfElement = new HashMap<>();
        for(int x = 0; x < plane.getPlane().length; x++){
            for(int y = 0; y < plane.getPlane()[x].length; y++){
                if(plane.getPlane()[x][y] == null || plane.getPlane()[x][y].getVector() == null) continue;
                for(int tempX = 0; tempX < plane.getPlane()[x][y].getVector().getWidth(); tempX++){
                    for(int tempY = 0; tempY < plane.getPlane()[x][y].getVector().getHeight(); tempY++){
                        VisualizationElement<T> element = plane.getPlane()[x][y].getVector().getPlane()[tempX][tempY];
                        flatPlane[x * maxX + tempX][y *maxY + tempY] = element;
                        if(element != null) representativeOfElement.put(element.getVector().print(), element.getRepresentative());
                    }
                }
            }
        }
        representativeOfElements.add(representativeOfElement);
        flatPlanes.add(flatPlane);
        return flatPlane;
    }

    private void writeOptimizedFile(Plane<Plane<T>> plane, VisualizationElement<T>[][] flatPlane, File path) {
        File file = new File(path.getPath(), "level_" + (subPlanes.size() - 1) + ".html");
        try {
            PrintWriter printWriter = new PrintWriter(file);
            printWriter.print("<html><body>");
            printWriter.print(plane.printHtml());
            printWriter.print("</body></html>");
            printWriter.flush();
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        File fileOptimized = new File(path.getPath(), "level_opt_" + (subPlanes.size() - 1) + ".html");
        printFile(flatPlane, fileOptimized);
    }

    private File writeNonOptimizedFile(VisualizationElement<T>[][] flatPlane) {
        File path = new File("results/html/" + timestamp);
        if(!path.exists()) path.mkdirs();
        File fileNotOptimized = new File(path.getPath(), "level_notOpt_" + (subPlanes.size() - 1) + ".html");
        printFile(flatPlane, fileNotOptimized);
        return path;
    }

    @Override
    public void finished() {
        try {
            serialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void serialize() throws IOException {
        logger.info("start serialize plane manager.");
        File path = new File(PATH);
        if(!path.exists()) path.mkdirs();

        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(new File(path, FILE_NAME)));
        outputStream.writeObject(this);
        logger.info("serialized plane manager.");
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

    // Algorithm: http://stackoverflow.com/questions/398299/looping-in-a-spiral
    private void rearrangeItems(VisualizationElement<T>[][] flatPlane){
        boolean doAgain = true;
        while(doAgain){
            doAgain = false;
            int x = 0;
            int y = 0;
            int dx = 0;
            int dy = -1;
            int X = flatPlane.length;
            int Y = flatPlane[0].length;
            for(int i = 0; i < X * X; i++){
                if(-X/2 < x && x <= X/2 && -Y/2 < y && y <= Y/2){

                    if(flatPlane[x + X/2 - 1][Y/2 - y] != null){
                        List<Direction> directions = new ArrayList<>();

                        boolean preferShiftUpDown = Math.abs(y) > Math.abs(x);

                        if(y > 0){
                            directions.add(Direction.DOWN);
                        }
                        if(y < 0){
                            directions.add(Direction.UP);
                        }
                        if(x < 0){
                            directions.add(Direction.RIGHT);
                        }
                        if(x > 0){
                            directions.add(Direction.LEFT);
                        }

                        if(!preferShiftUpDown) Collections.reverse(directions);

                        boolean itemHasBeenMoved = moveItem(flatPlane, directions, x + X/2 - 1, Y/2 - y);
                        if(itemHasBeenMoved) doAgain = true;
                    }
                }
                if(x == y || (x < 0 && x == -y) || (x > 0 && x == 1 - y)){
                    int temp = dx;
                    dx = -dy;
                    dy = temp;
                }
                x = x + dx;
                y = y + dy;
            }
        }
    }

    private boolean moveItem(VisualizationElement<T>[][] flatPlane, List<Direction> directions, int x, int y){

        for(Direction d : directions){
            int newX = x;
            int newY = y;
            if(d == Direction.RIGHT) newX = x + 1;
            if(d == Direction.LEFT) newX = x - 1;
            if(d == Direction.DOWN) newY = y + 1;
            if(d == Direction.UP) newY = y - 1;

            if(newX < 0 || newX > flatPlane.length - 1 || newY < 0 || newY > flatPlane[0].length - 1) continue;

            if(flatPlane[newX][newY] == null){
                logger.debug("Moved item " + d);
                flatPlane[newX][newY] = flatPlane[x][y];
                flatPlane[x][y] = null;
                return true;
            }
        }
        return false;
    }

    public JsonArray getElementField(int level, int startX, int startY, int endX, int endY){
        JsonArray jsonArray = new JsonArray();
        VisualizationElement[][] plane = flatPlanes.get(level);

        if(plane.length < endX) endX = plane.length;
        if(plane[0].length < endY) endY = plane[0].length;

        for(int x = startX; x < endX; x++){
            JsonArray col = new JsonArray();
            for(int y = startY; y < endY; y++){
                if(plane[x][y] == null){
                    col.add("");
                } else {
                    col.add(plane[x][y].getVector().printHtml());
                }
            }
            jsonArray.add(col);
        }

        return jsonArray;
    }

    public String getSingleElement(int level, int x, int y){

        VisualizationElement[][] plane = flatPlanes.get(level);

        if(plane.length <= x || x < 0) return "";
        if(plane[0].length <= y || y < 0) return "";


        VisualizationElement element = plane[x][y];
        if(element != null) {
            return element.getVector().print();
        }
        return "";
    }

    public JsonObject getElementPosition(int level, String id){
        Position position = positionsOfElements.get(level).get(id);
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("x", position.getX());
        jsonObject.add("y", position.getY());
        return jsonObject;
    }

    public String getRepresentativeOfElement(String id, int currentLevel){
        if(!representativeOfElements.get(currentLevel).containsKey(id)) throw new RuntimeException("no representative found for " + id);
        return representativeOfElements.get(currentLevel).get(id);
    }

    public int getTopLevel(){
        return flatPlanes.size() - 1;
    }

    private void saveElementsAndPositions(VisualizationElement[][] flatPlane){
        Map<String, Position> elementsAndPositions = new HashMap<>();
        for(int x = 0; x < flatPlane.length; x++){
            for(int y = 0; y < flatPlane[0].length; y++){
                if(flatPlane[x][y] != null) elementsAndPositions.put(flatPlane[x][y].print(), new Position(x, y));
            }
        }
        positionsOfElements.add(elementsAndPositions);
    }

}
