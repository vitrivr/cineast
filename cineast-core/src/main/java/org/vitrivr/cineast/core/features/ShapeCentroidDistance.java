package org.vitrivr.cineast.core.features;

import boofcv.alg.filter.binary.Contour;
import georegression.struct.point.Point2D_I32;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.images.ContourHelper;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


public class ShapeCentroidDistance extends AbstractFeatureModule {

    private static final int DESCRIPTOR_LENGTH = 100;

    /**
     *
     */
    public ShapeCentroidDistance() {
        super("features_shapecentroid", 2.0f, DESCRIPTOR_LENGTH);
    }

    /**
     *
     * @param shot
     */
    @Override
    public void processSegment(SegmentContainer shot) {

        BufferedImage image = shot.getAvgImg().getBufferedImage();

        List<Contour> contours = ContourHelper.getContours(image);
        List<Point2D_I32> contour = contours.get(0).internal.get(0);

        if (image != null) {
            FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
            double[] distancefunction = ContourHelper.centroidDistance(contour, true);
            Complex[] signature = transformer.transform(distancefunction, TransformType.FORWARD);
            float[] descriptors = new float[DESCRIPTOR_LENGTH];
            for (int i = 1;i<DESCRIPTOR_LENGTH;i++) {
                descriptors[i] = (float) (signature[i].abs() / signature[0].abs());
            }
            this.persist(shot.getId(), new FloatVectorImpl(descriptors));
        }
    }

    /**
     *
     * @param sc
     * @param qc
     * @return
     */
    @Override
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {

        BufferedImage image = sc.getAvgImg().getBufferedImage();

        qc = setQueryConfig(qc);

        List<Contour> contours = ContourHelper.getContours(image);
        List<Point2D_I32> contour =  contours.get(0).internal.get(0);

        if (image != null) {
            FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
            double[] distancefunction = ContourHelper.centroidDistance(contour, true);
            Complex[] signature = transformer.transform(distancefunction, TransformType.FORWARD);
            float[] descriptors = new float[DESCRIPTOR_LENGTH];
            for (int i = 1;i<DESCRIPTOR_LENGTH;i++) {
                descriptors[i] = (float) (signature[i].abs() / signature[0].abs());
            }
            return this.getSimilar(descriptors, qc);
        } else {
            return new ArrayList<>();
        }
    }
    
    @Override
    protected ReadableQueryConfig setQueryConfig(ReadableQueryConfig qc) {
      return new QueryConfig(qc).setDistanceIfEmpty(QueryConfig.Distance.euclidean);
    }
    
}
