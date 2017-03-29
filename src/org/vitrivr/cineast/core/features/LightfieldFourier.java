package org.vitrivr.cineast.core.features;

import boofcv.alg.filter.binary.Contour;
import com.twelvemonkeys.image.ImageUtil;
import georegression.struct.point.Point2D_I32;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.m3d.ReadableMesh;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractLightfieldDescriptor;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.images.ContourHelper;
import org.vitrivr.cineast.core.util.math.MathConstants;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 14.03.17
 */
public class LightfieldFourier extends AbstractLightfieldDescriptor {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Weights used for kNN retrieval based on images / sketches. Higher frequency components (standing for finer details) will have less
     * weight towards the final result. */
    private static final float[] WEIGHTS = new float[SIZE+1];
    static {
        for (int i=0;i<SIZE;i++) {
            WEIGHTS[i] = 1.0f - (i-1)*(1.0f/(2*SIZE));
        }
    }

    /** Helper class that is used to perform FFT. */
    private final FastFourierTransformer transformer;

    /**
     * Default constructor for LightfieldFourier class.
     */
    public LightfieldFourier() {
        super("features_lightfieldfourier", 2.0f, MathConstants.VERTICES_3D_DODECAHEDRON);
        this.transformer = new FastFourierTransformer(DftNormalization.STANDARD);
    }

    /**
     *
     * @param sc
     * @param qc
     * @return
     */
    @Override
    public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {

        /* Initialize helper data structures. */
        TObjectDoubleHashMap<String> map = new TObjectDoubleHashMap<>(Config.sharedConfig().getRetriever().getMaxResultsPerModule(), 0.5f, 0.0f);
        TObjectDoubleHashMap<String> partialMap = new TObjectDoubleHashMap<>(Config.sharedConfig().getRetriever().getMaxResultsPerModule(), 0.5f, 0.0f);


        List<StringDoublePair> results = new ArrayList<>();

        qc.setDistance(QueryConfig.Distance.manhattan);


        List<Pair<Integer,float[]>> features;

        /* Extract features from either the provided Mesh (1) or image (2). */
        ReadableMesh mesh = sc.getNormalizedMesh();
        if (mesh.isEmpty()) {
            BufferedImage image = ImageUtil.createResampled(sc.getAvgImg().getBufferedImage(), SIZE, SIZE, Image.SCALE_SMOOTH);
            features = this.featureVectorsFromImage(image,POSEIDX_UNKNOWN);
            qc.setDistanceWeights(WEIGHTS);
        } else {
            features = this.featureVectorsFromMesh(mesh);
        }

        /* */
        for (Pair<Integer,float[]> feature : features) {
            partialMap.clear();
            for (Map<String, PrimitiveTypeProvider> result : this.selector.getNearestNeighbourRows(Config.sharedConfig().getRetriever().getMaxResultsPerModule(), feature.second, FIELDS[1], qc)) {

                String id = result.get(FIELDS[0]).getString();
                double score = MathHelper.getScore(result.get("ap_distance").getDouble(), this.maxDist);
                int poseidx = result.get(FIELDS[2]).getInt();


                if (poseidx != feature.first && feature.first != POSEIDX_UNKNOWN) {
                    score *= 0.9;
                }

                double newValue = Math.max(partialMap.get(id), score);
                partialMap.putIfAbsent(id, newValue);
            }

            for (String key : partialMap.keySet()) {
                map.adjustOrPutValue(key, partialMap.get(key)/features.size(), partialMap.get(key)/features.size());
            }
        }

        /* Add results to list. */
        for (String key : map.keySet()) {
            results.add(new StringDoublePair(key, map.get(key)));
        };

        /* Return results. */
        return results;
    }

    /**
     * Extracts the Lightfield Fourier descriptors from a provided BufferedImage. The returned list contains
     * elements for each identified contour of adequate size.
     *
     * @param image Image for which to extract the Lightfield Fourier descriptors.
     * @param poseidx Poseidx of the extracted image.
     * @return List of descriptors for image.
     */
    protected List<Pair<Integer,float[]>> featureVectorsFromImage(BufferedImage image, int poseidx) {
        List<Contour> contours = ContourHelper.getContours(image);
        List<Pair<Integer,float[]>> features = new ArrayList<>();

        int fv_size = 128;

        /* Select the largest, inner contour from the list of available contours. */
        for (Contour contour : contours) {
            for (List<Point2D_I32> inner : contour.internal) {
                /* Check size of selected contour. */
                if (inner.size() < fv_size * 2) continue;

                /* Calculate the descriptor for the selected contour. */
                double[] cds = ContourHelper.centroidDistance(inner, true);
                Complex[] results = this.transformer.transform(cds, TransformType.FORWARD);
                double magnitude = results[0].abs();
                float[] feature = new float[fv_size];
                for (int i = 0; i < fv_size; i++) {
                    feature[i] = (float) (results[i+1].abs() / magnitude);
                }
                features.add(new Pair<>(poseidx, MathHelper.normalizeL2(feature)));
            }
        }

        return features;
    }
}
