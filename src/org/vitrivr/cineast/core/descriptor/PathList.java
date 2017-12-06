package org.vitrivr.cineast.core.descriptor;

import boofcv.abst.filter.derivative.ImageGradient;
import boofcv.alg.misc.PixelMath;
import boofcv.alg.tracker.klt.KltTrackFault;
import boofcv.alg.tracker.klt.PkltConfig;
import boofcv.alg.tracker.klt.PyramidKltFeature;
import boofcv.alg.tracker.klt.PyramidKltTracker;
import boofcv.alg.transform.pyramid.PyramidOps;
import boofcv.factory.filter.derivative.FactoryDerivative;
import boofcv.factory.geo.ConfigRansac;
import boofcv.factory.geo.FactoryMultiViewRobust;
import boofcv.factory.tracker.FactoryTrackerAlg;
import boofcv.factory.transform.pyramid.FactoryPyramid;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.geo.AssociatedPair;
import boofcv.struct.image.GrayS16;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;
import boofcv.struct.pyramid.PyramidDiscrete;
import georegression.struct.homography.Homography2D_F64;
import georegression.struct.point.Point2D_F32;
import georegression.struct.point.Point2D_F64;
import georegression.transform.homography.HomographyPointOps_F64;
import org.ddogleg.fitting.modelset.ModelMatcher;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.frames.VideoFrame;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


public class PathList {

    private PathList() {
    }

    public static int samplingInterval = 15;
    public static int frameInterval = 2;

    public static double backwardTrackingDistanceThreshold = 5.0;
    public static double successTrackingRatioThreshold = 0.50;
    public static double ransacInlierRatioThreshold = 0.65;
    public static double successFrameSRatioThreshold = 0.70;

    public static void showBineryImage(GrayU8 image) {
        PixelMath.multiply(image, 255, image);
        BufferedImage out = ConvertBufferedImage.convertTo(image, null);
        ShowImages.showWindow(out, "Output");
    }

    public static void separateFgBgPaths(List<VideoFrame> videoFrames,
                                         LinkedList<Pair<Integer, ArrayList<AssociatedPair>>> allPaths,
                                         List<Pair<Integer, LinkedList<Point2D_F32>>> foregroundPaths,
                                         List<Pair<Integer, LinkedList<Point2D_F32>>> backgroundPaths) {

        ModelMatcher<Homography2D_F64, AssociatedPair> robustF = FactoryMultiViewRobust.homographyRansac(null, new ConfigRansac(200, 3.0f));
        if (allPaths == null || videoFrames == null || videoFrames.isEmpty()) {
            return;
        }

        int width = videoFrames.get(0).getImage().getWidth();
        int height = videoFrames.get(0).getImage().getHeight();
        int maxTracksNumber = (width * height) / (samplingInterval * samplingInterval);
        int failedFrameCount = 0;

        for (Pair<Integer, ArrayList<AssociatedPair>> pair : allPaths) {
            List<AssociatedPair> inliers = new ArrayList<AssociatedPair>();
            List<AssociatedPair> outliers = new ArrayList<AssociatedPair>();

            int frameIdx = pair.first;
            ArrayList<AssociatedPair> matches = pair.second;

            if (matches.size() < maxTracksNumber * successTrackingRatioThreshold) {
                failedFrameCount += 1;
                continue;
            }

            Homography2D_F64 curToPrev = new Homography2D_F64(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0);
            if (robustF.process(matches) && robustF.getMatchSet().size() > matches.size() * ransacInlierRatioThreshold) {
                curToPrev = robustF.getModelParameters().invert(null);
                inliers.addAll(robustF.getMatchSet());
                for (int i = 0, j = 0; i < matches.size(); ++i) {
                    if (i == robustF.getInputIndex(j)) {
                        if (j < inliers.size() - 1) {
                            ++j;
                        }
                    } else {
                        outliers.add(matches.get(i));
                    }
                }
            } else {
                curToPrev = new Homography2D_F64(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0);
                failedFrameCount += 1;
            }

            for (AssociatedPair p : inliers) {
                LinkedList<Point2D_F32> path = new LinkedList<Point2D_F32>();
                path.add(new Point2D_F32((float) p.p1.x / width, (float) p.p1.y / height));
                path.add(new Point2D_F32((float) p.p2.x / width, (float) p.p2.y / height));
                backgroundPaths.add(new Pair<Integer, LinkedList<Point2D_F32>>(frameIdx, path));
            }

            for (AssociatedPair p : outliers) {
                p.p2 = HomographyPointOps_F64.transform(curToPrev, p.p2, p.p2);
                LinkedList<Point2D_F32> path = new LinkedList<Point2D_F32>();
                path.add(new Point2D_F32((float) p.p1.x / width, (float) p.p1.y / height));
                path.add(new Point2D_F32((float) p.p2.x / width, (float) p.p2.y / height));
                foregroundPaths.add(new Pair<Integer, LinkedList<Point2D_F32>>(frameIdx, path));
            }
        }

        int frameNum = allPaths.size();
        if (frameNum - failedFrameCount < frameNum * successFrameSRatioThreshold) {
            foregroundPaths.clear();
            backgroundPaths.clear();
        }

        return;
    }

    public static LinkedList<Pair<Integer, ArrayList<AssociatedPair>>> getDensePaths(List<VideoFrame> videoFrames) {
        if (videoFrames.size() < 2) {
            return null;
        }

        PkltConfig configKlt = new PkltConfig(3, new int[]{1, 2, 4});
        configKlt.config.maxPerPixelError = 45;
        ImageGradient<GrayU8, GrayS16> gradient = FactoryDerivative.sobel(GrayU8.class, GrayS16.class);
        PyramidDiscrete<GrayU8> pyramidForeward = FactoryPyramid.discreteGaussian(configKlt.pyramidScaling, -1, 2, true, ImageType.single(GrayU8.class));
        PyramidDiscrete<GrayU8> pyramidBackward = FactoryPyramid.discreteGaussian(configKlt.pyramidScaling, -1, 2, true, ImageType.single(GrayU8.class));
        PyramidKltTracker<GrayU8, GrayS16> trackerForeward = FactoryTrackerAlg.kltPyramid(configKlt.config, GrayU8.class, null);
        PyramidKltTracker<GrayU8, GrayS16> trackerBackward = FactoryTrackerAlg.kltPyramid(configKlt.config, GrayU8.class, null);

        GrayS16[] derivX = null;
        GrayS16[] derivY = null;

        LinkedList<PyramidKltFeature> tracks = new LinkedList<PyramidKltFeature>();
        LinkedList<Pair<Integer, ArrayList<AssociatedPair>>> paths = new LinkedList<Pair<Integer, ArrayList<AssociatedPair>>>();

        GrayU8 gray = null;
        int frameIdx = 0;
        int cnt = 0;
        for (VideoFrame videoFrame : videoFrames) {
            ++frameIdx;

            if (cnt >= frameInterval) {
                cnt = 0;
                continue;
            }
            cnt += 1;

            gray = ConvertBufferedImage.convertFrom(videoFrame.getImage().getBufferedImage(), gray);
            ArrayList<AssociatedPair> tracksPairs = new ArrayList<AssociatedPair>();

            if (frameIdx == 0) {
                tracks = denseSampling(gray, derivX, derivY, samplingInterval, configKlt, gradient, pyramidBackward, trackerBackward);
            } else {
                tracking(gray, derivX, derivY, tracks, tracksPairs, gradient, pyramidForeward, pyramidBackward, trackerForeward, trackerBackward);
                tracks = denseSampling(gray, derivX, derivY, samplingInterval, configKlt, gradient, pyramidBackward, trackerBackward);
            }

            paths.add(new Pair<Integer, ArrayList<AssociatedPair>>(frameIdx, tracksPairs));
        }
        return paths;
    }

    public static LinkedList<PyramidKltFeature> denseSampling(GrayU8 image, GrayS16[] derivX, GrayS16[] derivY,
                                                              int samplingInterval,
                                                              PkltConfig configKlt,
                                                              ImageGradient<GrayU8, GrayS16> gradient,
                                                              PyramidDiscrete<GrayU8> pyramid,
                                                              PyramidKltTracker<GrayU8, GrayS16> tracker) {
        LinkedList<PyramidKltFeature> tracks = new LinkedList<PyramidKltFeature>();

        pyramid.process(image);
        derivX = declareOutput(pyramid, derivX);
        derivY = declareOutput(pyramid, derivY);
        PyramidOps.gradient(pyramid, gradient, derivX, derivY);
        tracker.setImage(pyramid, derivX, derivY);

        for (int y = 0; y < image.height; y += samplingInterval) {
            for (int x = 0; x < image.width; x += samplingInterval) {
                PyramidKltFeature t = new PyramidKltFeature(configKlt.pyramidScaling.length, configKlt.templateRadius);
                t.setPosition(x, y);
                tracker.setDescription(t);
                tracks.add(t);
            }
        }
        return tracks;
    }

    public static LinkedList<PyramidKltFeature> tracking(GrayU8 image, GrayS16[] derivX, GrayS16[] derivY,
                                                         LinkedList<PyramidKltFeature> tracks,
                                                         ArrayList<AssociatedPair> tracksPairs,
                                                         ImageGradient<GrayU8, GrayS16> gradient,
                                                         PyramidDiscrete<GrayU8> pyramidForeward,
                                                         PyramidDiscrete<GrayU8> pyramidBackward,
                                                         PyramidKltTracker<GrayU8, GrayS16> trackerForeward,
                                                         PyramidKltTracker<GrayU8, GrayS16> trackerBackward
    ) {
        pyramidForeward.process(image);
        derivX = declareOutput(pyramidForeward, derivX);
        derivY = declareOutput(pyramidForeward, derivY);
        PyramidOps.gradient(pyramidForeward, gradient, derivX, derivY);
        trackerForeward.setImage(pyramidForeward, derivX, derivY);

        ListIterator<PyramidKltFeature> listIterator = tracks.listIterator();
        while (listIterator.hasNext()) {
            PyramidKltFeature track = listIterator.next();
            Point2D_F64 pointPrev = new Point2D_F64(track.x, track.y);
            KltTrackFault ret = trackerForeward.track(track);
            boolean success = false;
            if (ret == KltTrackFault.SUCCESS && image.isInBounds((int) track.x, (int) track.y) && trackerForeward.setDescription(track)) {
                Point2D_F64 pointCur = new Point2D_F64(track.x, track.y);
                ret = trackerBackward.track(track);
                if (ret == KltTrackFault.SUCCESS && image.isInBounds((int) track.x, (int) track.y)) {
                    Point2D_F64 pointCurBack = new Point2D_F64(track.x, track.y);
                    if (normalizedDistance(pointPrev, pointCurBack) < backwardTrackingDistanceThreshold) {
                        tracksPairs.add(new AssociatedPair(pointPrev, pointCur));
                        success = true;
                    }
                }
            }
            if (!success) {
                listIterator.remove();
            }
        }
        return tracks;
    }

    public static GrayS16[] declareOutput(PyramidDiscrete<GrayU8> pyramid, GrayS16[] deriv) {
        if (deriv == null) {
            deriv = PyramidOps.declareOutput(pyramid, GrayS16.class);
        } else if (deriv[0].width != pyramid.getLayer(0).width ||
                deriv[0].height != pyramid.getLayer(0).height) {
            PyramidOps.reshapeOutput(pyramid, deriv);
        }
        return deriv;
    }

    public static double normalizedDistance(Point2D_F64 pointA, Point2D_F64 pointB) {
        double dx = (pointA.x - pointB.x);
        double dy = (pointA.y - pointB.y);
        return Math.sqrt(Math.pow((dx), 2) + Math.pow((dy), 2));
    }
}
