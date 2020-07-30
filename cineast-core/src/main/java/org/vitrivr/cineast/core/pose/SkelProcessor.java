package org.vitrivr.cineast.core.pose;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.opencv.opencv_core.Mat;

import org.bytedeco.openpose.*;
import org.opencv.core.CvType;
import org.vitrivr.cineast.core.config.PoseConfig;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import static org.bytedeco.openpose.global.openpose.ThreadManagerMode;
import static org.bytedeco.openpose.global.openpose.OP_CV2OPCONSTMAT;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

public class SkelProcessor {
    static SkelProcessor skelProcessor = null;
    final private OpWrapper opWrapper;
    static PoseConfig preConfig = null;

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
        String modelPath = config.getModelPath();
        if (modelPath == null) {
            throw new UncheckedIOException(new IOException("modelPath cannot be null"));
        }
        // Configure OpenPose
        this.opWrapper = new OpWrapper(ThreadManagerMode.Asynchronous);
        this.opWrapper.disableMultiThreading();
        WrapperStructPose structPose = new WrapperStructPose();
        structPose.modelFolder(new OpString(modelPath));
        PoseConfig.Resolution bodyNetResolution = config.getBodyNetResolution();
        if (bodyNetResolution != null) {
            structPose.netInputSize(convRes(bodyNetResolution));
        }
        this.opWrapper.configure(structPose);
        WrapperStructHand structHand = new WrapperStructHand();
        structHand.enable(true);
        PoseConfig.Resolution handNetResolution = config.getHandNetResolution();
        if (handNetResolution != null) {
            structHand.netInputSize(convRes(handNetResolution));
        }
        this.opWrapper.configure(structHand);
        // Start OpenPose
        this.opWrapper.start();
    }

    private static Mat bufferedImageToMat(BufferedImage bi) {
        if (bi.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            BufferedImage convertedImg = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            convertedImg.getGraphics().drawImage(bi, 0, 0, null);
            bi = convertedImg;
        }
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        return new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3, new BytePointer(ByteBuffer.wrap(data)));
    }

    private static IntPoint convRes(PoseConfig.Resolution res) {
        return new IntPoint(res.x, res.y);
    }

    private static float[] getKp(FloatArray arr, int i, int j) {
        return new float[] {
            arr.get(new int[]{i, j, 0})[0],
            arr.get(new int[]{i, j, 1})[0],
            arr.get(new int[]{i, j, 2})[0]
        };
    }

    private static float[][][] procKps(Datum dat) {
        FloatArray poseArray = dat.poseKeypoints();
        FloatArray2 handArrays = dat.handKeypoints();
        FloatArray lHandArray = handArrays.get(0);
        FloatArray rHandArray = handArrays.get(1);
        IntPointer dimSizes = poseArray.getSize();
        if (dimSizes == null) {
            return null;
        }
        int numPeople = dimSizes.get(0);
        float[][][] keypoints = new float[numPeople][65][3];
        for (int i = 0; i < numPeople; i++) {
            for (int j = 0; j < 25; j++) {
                keypoints[i][j] = getKp(poseArray, i, j);
            }
            for (int j = 25; j < 45; j++) {
                keypoints[i][j] = getKp(lHandArray, i, j - 24);
            }
            for (int j = 45; j < 65; j++) {
                keypoints[i][j] = getKp(rHandArray, i, j - 44);
            }
        }
        return keypoints;
    }

    public synchronized float[][][] getPose(MultiImage img) {
        Mat ocvIm = bufferedImageToMat(img.getBufferedImage());
        Matrix opIm;
        try {
            opIm = OP_CV2OPCONSTMAT(ocvIm);
        } finally {
            ocvIm.deallocate();
        }
        Datum dat = new Datum();
        Datums dats = new Datums();
        float[][][] keypoints;
        try {
            dat.cvInputData(opIm);
            dats.put(dat);
            this.opWrapper.emplaceAndPop(dats);
            keypoints = procKps(dat);
        } finally {
            // We don't actually deallocate dat because dats will do it when deallocated
            dat.deallocate(false);
            dats.deallocate();
        }
        return keypoints;
    }
}
