package org.vitrivr.cineast.core.util.pose;

import com.google.protobuf.InvalidProtocolBufferException;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.ndarray.buffer.FloatDataBuffer;
import org.tensorflow.proto.framework.GraphDef;
import org.tensorflow.types.TFloat32;
import org.vitrivr.cineast.core.data.Skeleton;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.images.ImagePreprocessingHelper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Based on https://github.com/liljom/openpose-tf-mobilenet-java
 */
public class OpenPoseDetector implements PoseDetector, AutoCloseable
{
    private static final String MODEL_FILE = "resources/openpose/pose.pb";
    private static final String INPUT_NAME = "image";
    private static final String OUTPUT_NAME = "Openpose/concat_stage7";
    private static final int[][] COCO_PAIRS = {{1, 2}, {1, 5}, {2, 3}, {3, 4}, {5, 6}, {6, 7}, {1, 8}, {8, 9}, {9, 10}, {1, 11},
            {11, 12}, {12, 13}, {1, 0}, {0, 14}, {14, 16}, {0, 15}, {15, 17}};
    private final int[][] COCO_PAIR_CONNECTIONS = {{12, 13}, {20, 21}, {14, 15}, {16, 17}, {22, 23}, {24, 25}, {0, 1}, {2, 3},
            {4, 5}, {6, 7}, {8, 9}, {10, 11}, {28, 29}, {30, 31}, {34, 35}, {32, 33}, {36, 37}, {18, 19}, {26, 27}};
    private final float[] STD = new float[]{1f/255f, 1f/255f, 1f/255f};


    private final Graph graph;
    private final Session session;

    protected static byte[] load(String path) {
        try {
            return Files.readAllBytes((Paths.get(path)));
        } catch (IOException e) {
            throw new RuntimeException(
                    "could not load graph for DeepLab: " + LogHelper.getStackTrace(e));
        }
    }

    public OpenPoseDetector()
    {
        this.graph = new Graph();
        try {
            this.graph.importGraphDef(GraphDef.parseFrom(load(MODEL_FILE)));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        this.session = new Session(this.graph);

    }

    public void close() {
        this.session.close();
        this.graph.close();
    }

    public List<Skeleton> detectPoses(BufferedImage img)
    {

        final int imageSize = 512;
        float scaling = ((float) imageSize) / Math.max(img.getWidth(), img.getHeight());
        int xOffset = (int) ((imageSize - (img.getWidth() * scaling)) / 2f);
        int yOffset = (int) ((imageSize - (img.getHeight() * scaling)) / 2f);

        BufferedImage resizedImg;
        if (img.getWidth() == imageSize && img.getHeight() == imageSize) {
            resizedImg = img;
        } else {
            resizedImg = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = resizedImg.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setColor(Color.white);
            g2.fillRect(0, 0, imageSize, imageSize);
            g2.drawImage(img, xOffset, yOffset, (int)(img.getWidth() * scaling), (int)(img.getHeight() * scaling), null);
            g2.dispose();
        }

        float[] fimg = ImagePreprocessingHelper.imageToHWCArray(resizedImg, null, STD);

        TFloat32 imageTensor = TFloat32.tensorOf(Shape.of(1, imageSize, imageSize, 3), DataBuffers.of(fimg));

        TFloat32 out = (TFloat32) session.runner().feed(INPUT_NAME, imageTensor).fetch(OUTPUT_NAME).run().get(0);
        float[] outputTensor = new float[(int) out.size()];

        FloatDataBuffer floatBuffer = DataBuffers.of(outputTensor);
        out.read(floatBuffer);

        int heatMapCount = 19;
        List<Vector<float[]>> coordinates = new ArrayList<>(heatMapCount - 1);

        int mapHeight = imageSize / 8;
        int mapWidth = imageSize / 8;

        // eliminate duplicate part recognitions
        final int pafMapCount = 38;
        for (int i = 0; i < (heatMapCount - 1); i++) {
            coordinates.add(new Vector<>());
            for (int j = 0; j < mapHeight; j++) {
                for (int k = 0; k < mapWidth; k++) {
                    float[] coordinate = {j, k};
                    float max_value = 0;
                    int maximumFilterSize = 5;
                    for (int dj = -(maximumFilterSize - 1) / 2; dj < (maximumFilterSize + 1) / 2; dj++) {
                        if ((j + dj) >= mapHeight || (j + dj) < 0) {
                            break;
                        }
                        for (int dk = -(maximumFilterSize - 1) / 2; dk < (maximumFilterSize + 1) / 2; dk++) {
                            if ((k + dk) >= mapWidth || (k + dk) < 0) {
                                break;
                            }
                            float value = outputTensor[(heatMapCount + pafMapCount) * mapWidth * (j + dj) + (heatMapCount + pafMapCount) * (k + dk) + i];
                            if (value > max_value) {
                                max_value = value;
                            }
                        }
                    }
                    final float NMS_Threshold = 0.15f;
                    if (max_value > NMS_Threshold) {
                        if (max_value == outputTensor[(heatMapCount + pafMapCount) * mapWidth * j + (heatMapCount + pafMapCount) * k + i]) {
                            coordinates.get(i).addElement(coordinate);
                        }
                    }
                }
            }
        }

        // eliminate duplicate connections
        final int maxPairCount = 17;
        List<Vector<int[]>> pairs = new ArrayList<>(maxPairCount);
        List<Vector<int[]>> pairs_final = new ArrayList<>(maxPairCount);
        List<Vector<Float>> pairs_scores = new ArrayList<>(maxPairCount);
        List<Vector<Float>> pairs_scores_final = new ArrayList<>(maxPairCount);
        for (int i = 0; i < maxPairCount; i++) {
            pairs.add(new Vector<>());
            pairs_scores.add(new Vector<>());
            pairs_final.add(new Vector<>());
            pairs_scores_final.add(new Vector<>());
            Vector<Integer> part_set = new Vector<>();
            for (int p1 = 0; p1 < coordinates.get(COCO_PAIRS[i][0]).size(); p1++) {
                for (int p2 = 0; p2 < coordinates.get(COCO_PAIRS[i][1]).size(); p2++) {
                    int count = 0;
                    float score = 0.0f;
                    float[] scores = new float[10];
                    float p1x = coordinates.get(COCO_PAIRS[i][0]).get(p1)[0];
                    float p1y = coordinates.get(COCO_PAIRS[i][0]).get(p1)[1];
                    float p2x = coordinates.get(COCO_PAIRS[i][1]).get(p2)[0];
                    float p2y = coordinates.get(COCO_PAIRS[i][1]).get(p2)[1];
                    float dx = p2x - p1x;
                    float dy = p2y - p1y;
                    float normVec = (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

                    if (normVec < 0.0001f) {
                        break;
                    }
                    float vx = dx / normVec;
                    float vy = dy / normVec;
                    for (int t = 0; t < 10; t++) {
                        int tx = (int) (p1x + (t * dx / 9) + 0.5);
                        int ty = (int) (p1y + (t * dy / 9) + 0.5);
                        int location = tx * (heatMapCount + pafMapCount) * mapWidth + ty * (heatMapCount + pafMapCount) + heatMapCount;
                        scores[t] = vy * outputTensor[location + COCO_PAIR_CONNECTIONS[i][0]];
                        scores[t] += vx * outputTensor[location + COCO_PAIR_CONNECTIONS[i][1]];
                    }
                    for (int h = 0; h < 10; h++) {
                        float local_PAF_Threshold = 0.2f;
                        if (scores[h] > local_PAF_Threshold) {
                            count += 1;
                            score += scores[h];
                        }
                    }
                    float part_Score_Threshold = 0.2f;
                    int PAF_Count_Threshold = 5;
                    if (score > part_Score_Threshold && count >= PAF_Count_Threshold) {
                        boolean inserted = false;
                        int[] pair = {p1, p2};
                        for (int l = 0; l < pairs.get(i).size(); l++) {
                            if (score > pairs_scores.get(i).get(l)) {
                                pairs.get(i).insertElementAt(pair, l);
                                pairs_scores.get(i).insertElementAt(score, l);
                                inserted = true;
                                break;
                            }
                        }
                        if (!inserted) {
                            pairs.get(i).addElement(pair);
                            pairs_scores.get(i).addElement(score);
                        }
                    }
                }
            }
            for (int m = 0; m < pairs.get(i).size(); m++) {
                boolean conflict = false;
                for (Integer integer : part_set) {
                    if (pairs.get(i).get(m)[0] == integer || pairs.get(i).get(m)[1] == integer) {
                        conflict = true;
                        break;
                    }
                }
                if (!conflict) {
                    pairs_final.get(i).addElement(pairs.get(i).get(m));
                    pairs_scores_final.get(i).addElement(pairs_scores.get(i).get(m));
                    part_set.addElement(pairs.get(i).get(m)[0]);
                    part_set.addElement(pairs.get(i).get(m)[1]);
                }
            }
        }

        ArrayList<Human> humans = new ArrayList<>();
        ArrayList<Skeleton> skeletons = new ArrayList<>();
        for (int i = 0; i < maxPairCount; i++) {
            for (int j = 0; j < pairs_final.get(i).size(); j++) {
                boolean merged = false;
                int p1 = COCO_PAIRS[i][0];
                int p2 = COCO_PAIRS[i][1];
                int ip1 = pairs_final.get(i).get(j)[0];
                int ip2 = pairs_final.get(i).get(j)[1];
                for (Human human : humans) {
                    if ((ip1 == human.coords_index_set[p1] && human.coords_index_assigned[p1]) || (ip2 == human.coords_index_set[p2] && human.coords_index_assigned[p2])) {
                        human.parts_coords[p1] = coordinates.get(p1).get(ip1);
                        human.parts_coords[p2] = coordinates.get(p2).get(ip2);
                        human.coords_index_set[p1] = ip1;
                        human.coords_index_set[p2] = ip2;
                        human.coords_index_assigned[p1] = true;
                        human.coords_index_assigned[p2] = true;
                        merged = true;
                        break;
                    }
                }
                if (!merged) {
                    Human human = new Human();
                    human.parts_coords[p1] = coordinates.get(p1).get(ip1);
                    human.parts_coords[p2] = coordinates.get(p2).get(ip2);
                    human.coords_index_set[p1] = ip1;
                    human.coords_index_set[p2] = ip2;
                    human.coords_index_assigned[p1] = true;
                    human.coords_index_assigned[p2] = true;
                    humans.add(human);
                }
            }
        }

        // remove people with too few parts
        for (Human human : humans) {
            int human_part_count = 0;
            for (int j = 0; j < heatMapCount - 1; j++) {
                if (human.coords_index_assigned[j]) {
                    human_part_count += 1;
                }
            }
            int part_Count_Threshold = 4;
            if (human_part_count > part_Count_Threshold) {

                Skeleton skeleton = human.toSkeleton(xOffset, yOffset, scaling);
                skeletons.add(skeleton);

            }
        }

        return skeletons;
    }


//            "nose",         //0
//            "neck",         //1
//            "rShoulder",    //2
//            "rElbow",       //3
//            "rWist",        //4
//            "lShoulder",    //5
//            "lElbow",       //6
//            "lWrist",       //7
//            "rHip",         //8
//            "rKnee",        //9
//            "rAnkle",       //10
//            "lHip",         //11
//            "lKnee",        //12
//            "lAnkle",       //13
//            "rEye",         //14
//            "lEye",         //15
//            "rEar",         //16
//            "lEar"          //17
    public class Human
    {

        float[][] parts_coords = new float[18][2];
        int[] coords_index_set = new int[18];
        boolean[] coords_index_assigned = new boolean[18];

        public Skeleton toSkeleton(float xOffset, float yOffset, float scale) {

            float[] coordinates = {
                    parts_coords[0][1], parts_coords[0][0],     //NOSE
                    parts_coords[15][1], parts_coords[15][0],   //LEFT_EYE
                    parts_coords[14][1], parts_coords[14][0],   //RIGHT_EYE
                    parts_coords[17][1], parts_coords[17][0],   //LEFT_EAR
                    parts_coords[16][1], parts_coords[16][0],   //RIGHT_EAR
                    parts_coords[5][1], parts_coords[5][0],     //LEFT_SHOULDER
                    parts_coords[2][1], parts_coords[2][0],     //RIGHT_SHOULDER
                    parts_coords[6][1], parts_coords[6][0],     //LEFT_ELBOW
                    parts_coords[3][1], parts_coords[3][0],     //RIGHT_ELBOW
                    parts_coords[7][1], parts_coords[7][0],     //LEFT_WRIST
                    parts_coords[4][1], parts_coords[4][0],     //RIGHT_WRIST
                    parts_coords[11][1], parts_coords[11][0],   //LEFT_HIP
                    parts_coords[8][1], parts_coords[8][0],     //RIGHT_HIP
                    parts_coords[12][1], parts_coords[12][0],   //LEFT_KNEE
                    parts_coords[9][1], parts_coords[9][0],     //RIGHT_KNEE
                    parts_coords[13][1], parts_coords[13][0],   //LEFT_ANKLE
                    parts_coords[10][1], parts_coords[10][0],   //RIGHT_ANKLE
            };

            for (int i = 0; i < coordinates.length; i += 2) {
                coordinates[i] = (coordinates[i] * 8f - xOffset) / scale;
                coordinates[i + 1] = (coordinates[i + 1] * 8f - yOffset) / scale;
            }

            float[] weights = {
                    coords_index_assigned[0] ? 1f : 0f,
                    coords_index_assigned[15] ? 1f : 0f,
                    coords_index_assigned[14] ? 1f : 0f,
                    coords_index_assigned[17] ? 1f : 0f,
                    coords_index_assigned[16] ? 1f : 0f,
                    coords_index_assigned[5] ? 1f : 0f,
                    coords_index_assigned[2] ? 1f : 0f,
                    coords_index_assigned[6] ? 1f : 0f,
                    coords_index_assigned[3] ? 1f : 0f,
                    coords_index_assigned[7] ? 1f : 0f,
                    coords_index_assigned[4] ? 1f : 0f,
                    coords_index_assigned[11] ? 1f : 0f,
                    coords_index_assigned[8] ? 1f : 0f,
                    coords_index_assigned[12] ? 1f : 0f,
                    coords_index_assigned[9] ? 1f : 0f,
                    coords_index_assigned[13] ? 1f : 0f,
                    coords_index_assigned[10] ? 1f : 0f,
            };

            return new Skeleton(coordinates,weights);

        }
    }
}
