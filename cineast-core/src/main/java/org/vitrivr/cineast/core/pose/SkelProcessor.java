package org.vitrivr.cineast.core.pose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.vitrivr.cineast.core.config.PoseConfig;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;

public class SkelProcessor implements AutoCloseable {
    static PoseConfig preConfig = null;
    static SkelProcessor skelProcessor = null;
    private final HandDetector handDetector;
    private final BodyHandsOpenPose bodyHandsOpenPose;
    private final HandOpenPose handOpenPose;

    public static void configure(PoseConfig preConfig) {
        SkelProcessor.preConfig = preConfig;
    }

    public static SkelProcessor getInstance() {
        if (SkelProcessor.preConfig == null) {
            throw new IllegalStateException(
                "getInstance() called with no PoseConfig before configure(config) called"
            );
        }
        return SkelProcessor.getInstance(SkelProcessor.preConfig);
    }

    public static SkelProcessor getInstance(PoseConfig config) {
        if (skelProcessor == null) {
            SkelProcessor.skelProcessor = new SkelProcessor(config);
        }
        return SkelProcessor.skelProcessor;
    }

    SkelProcessor(PoseConfig config) {
        this.handDetector = new HandDetector();
        this.bodyHandsOpenPose = new BodyHandsOpenPose(config);
        this.handOpenPose = new HandOpenPose(config);
    }

    private float iou(float[] boxA, float[] boxB) {
        // Intersection over union
        float xA = Float.max(boxA[0], boxB[0]);
        float xB = Float.min(boxA[2], boxB[2]);
        if (xA >= xB) {
            return 0;
        }

        float yA = Float.max(boxA[1], boxB[1]);
        float yB = Float.min(boxA[3], boxB[3]);
        if (yA >= yB) {
            return 0;
        }

        float interArea = (xB - xA) * (yB - yA);

        float boxAArea = (boxA[2] - boxA[0]) * (boxA[3] - boxA[1]);
        float boxBArea = (boxB[2] - boxB[0]) * (boxB[3] - boxB[1]);

        return interArea / (boxAArea + boxBArea - interArea);
    }

    private float[] getBbox(float[][] pose, int startIdx, int endIdx) {
        float[] bbox = {Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
        int numKps = 0;
        for (int i = startIdx; i < endIdx; i++) {
            if (pose[i][2] <= 0) {
                continue;
            }
            numKps++;
            if (pose[i][0] < bbox[0]) {
                bbox[0] = pose[i][0];
            }
            if (pose[i][0] > bbox[2]) {
                bbox[2] = pose[i][0];
            }
            if (pose[i][1] < bbox[1]) {
                bbox[1] = pose[i][1];
            }
            if (pose[i][1] > bbox[3]) {
                bbox[3] = pose[i][1];
            }
        }
        if (numKps < 5) {
            return null;
        } else {
            return bbox;
        }
    }

    private List<float[]> getHandBboxes(float[][][] poses) {
        ArrayList<float[]> handBboxes = new ArrayList<>();
        for (float[][] pose : poses) {
            float[] leftHand = getBbox(pose, 25, 45);
            if (leftHand != null) {
                handBboxes.add(leftHand);
            }
            float[] rightHand = getBbox(pose, 45, 65);
            if (rightHand != null) {
                handBboxes.add(rightHand);
            }
        }
        return handBboxes;
    }

    private OpenPoseSession start(MultiImage img) {
        return new OpenPoseSession(this.bodyHandsOpenPose, this.handOpenPose, img);
    }

    public synchronized float[][][] getPoses(MultiImage img) {
        return this.start(img).getPoses();
    }

    /**
     * In format: (x1, y1, x2, y2) pixels
     * Out format: (x, y, w, h) pixels
     */
    private Optional<float[]> squarifyBbox(float[] bbox, int width, int height, float scale) {
        // Center to (xc, yc, w, h)
        float[] bboxCentered = new float[4];
        bboxCentered[0] = (bbox[0] + bbox[2]) / 2;
        bboxCentered[1] = (bbox[1] + bbox[3]) / 2;
        bboxCentered[2] = bbox[2] - bbox[0];
        bboxCentered[3] = bbox[3] - bbox[1];
        // Square box
        float widthHeight = Float.max(bboxCentered[2], bboxCentered[3]) * scale;
        if (widthHeight > width || widthHeight > height) {
            return Optional.empty();
        }
        float[] bboxSquare = new float[4];
        bboxSquare[0] = bboxCentered[0] - widthHeight / 2;
        bboxSquare[1] = bboxCentered[1] - widthHeight / 2;
        bboxSquare[2] = widthHeight;
        bboxSquare[3] = widthHeight;
        // Try and bump it within limits in x dim
        if (bboxSquare[0] < 0) {
            bboxSquare[0] = 0;
        }
        if (bboxSquare[0] + bboxSquare[2] > width) {
            bboxSquare[0] = width - bboxSquare[2];
        }
        if (bboxSquare[1] < 0) {
            bboxSquare[1] = 0;
        }
        if (bboxSquare[1] + bboxSquare[3] > height) {
            bboxSquare[1] = height - bboxSquare[3];
        }
        return Optional.of(bboxSquare);
    }

    private Optional<float[]> squarifyBbox(float[] bbox, int width, int height) {
        return Stream.of(1.4f, 1.3f, 1.2f, 1.1f, 1.0f)
            .flatMap(scale ->
                this.squarifyBbox(bbox, width, height, scale)
                    .map(Stream::of).orElseGet(Stream::empty))
            .findFirst();
    }

    public synchronized List<Pair<String, float[][]>> getPosesAndHands(MultiImage img) {
        // TODO: Sort numbering/order in result left to right
        ArrayList<Pair<String, float[][]>> result = new ArrayList<>();
        // Get normal poses
        int poseIdx = 1;
        OpenPoseSession opSession = this.start(img);
        float[][][] poses = opSession.getPoses();
        if (poses == null) {
            poses = new float[0][][];
        }
        for (float[][] pose : poses) {
            result.add(new ImmutablePair<>("Pose " + poseIdx, pose));
            poseIdx++;
        }
        // Get hand bboxes
        List<float[]> existingHandBboxes = getHandBboxes(poses);
        // Now go back and use palm detector
        List<float[]> detectedHandBboxes = Arrays.asList(handDetector.getHandBboxs(img));
        int width = img.getWidth();
        int height = img.getHeight();
        for (float[] handBbox : detectedHandBboxes) {
            handBbox[0] *= width;
            handBbox[1] *= height;
            handBbox[2] *= width;
            handBbox[3] *= height;
        }
        for (float[] existingBox : existingHandBboxes) {
            detectedHandBboxes.removeIf(detectedBox -> iou(detectedBox, existingBox) > 0.8);
        }
        int handIdx = 1;
        for (float[] handBbox : detectedHandBboxes) {
            Optional<float[]> maybeSquareBbox = squarifyBbox(handBbox, width, height);
            if (!maybeSquareBbox.isPresent()) {
                continue;
            }
            float[] squareBbox = maybeSquareBbox.get();
            float[][] leftHand = opSession.getHand(squareBbox, true);
            if (leftHand != null) {
                result.add(new ImmutablePair<>("Left hand " + handIdx, leftHand));
            }
            float[][] rightHand = opSession.getHand(squareBbox, false);
            if (rightHand != null) {
                result.add(new ImmutablePair<>("Right hand " + handIdx, rightHand));
            }
        }
        return result;
    }

    @Override
    public void close() {
        this.handDetector.close();
        this.bodyHandsOpenPose.close();
        this.handOpenPose.close();
    }
}
