package org.vitrivr.cineast.core.features;

import com.twelvemonkeys.image.ImageUtil;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.apache.commons.math3.complex.Complex;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;

import org.vitrivr.cineast.core.features.abstracts.AbstractLightfieldDescriptor;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.images.ZernikeHelper;
import org.vitrivr.cineast.core.util.math.MathConstants;
import org.vitrivr.cineast.core.util.math.ZernikeMoments;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author rgasser
 * @version 1.0
 * @created 17.03.17
 */
public class LightfieldZernike extends AbstractLightfieldDescriptor {

    /**
     *
     */
    public LightfieldZernike() {
        super("features_lightfieldzernike", 2.0f, MathConstants.VERTICES_3D_DODECAHEDRON);
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
        Mesh mesh = sc.getNormalizedMesh();
        if (mesh.isEmpty()) {
            BufferedImage image = ImageUtil.createResampled(sc.getAvgImg().getBufferedImage(), SIZE, SIZE, Image.SCALE_SMOOTH);
            features = this.featureVectorsFromImage(image,POSEIDX_UNKNOWN);
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
                    score /= 2.0f;
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
        List<ZernikeMoments> moments = ZernikeHelper.zernikeMomentsForShapes(image, SIZE/2, 10);
        List<Pair<Integer,float[]>> features = new ArrayList<>();
        for (ZernikeMoments moment : moments) {
            float[] vector = new float[36];
            int i = 0;
            for (Complex m : moment.getMoments()) {
                vector[i] = (float)m.abs();
                i++;
            }
            features.add(new Pair<>(poseidx, MathHelper.normalizeL2(vector)));
        }
        return features;
    }
}
