package org.vitrivr.cineast.core.run.filehandler;

import java.io.IOException;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.segments.Model3DSegment;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.decode.general.Decoder;
import org.vitrivr.cineast.core.decode.m3d.ModularMeshDecoder;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;
import org.vitrivr.cineast.core.run.ExtractionContainerProvider;
import org.vitrivr.cineast.core.segmenter.general.PassthroughSegmenter;
import org.vitrivr.cineast.core.segmenter.general.Segmenter;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.03.17
 */
public class Model3DExtractionFileHandler extends AbstractExtractionFileHandler<Mesh> {
    /**
     * Default constructor used to initialize the class.
     *
     * @param files   List of files that should be extracted.
     * @param context ExtractionContextProvider that holds extraction specific configurations.
     */
    public Model3DExtractionFileHandler(ExtractionContainerProvider files, ExtractionContextProvider context) throws IOException {
        super(files, context);
    }

    /**
     * Returns a new instance of  Decoder<T> that should be used with a concrete implementation
     * of this interface.
     *
     * @return Decoder
     */
    @Override
    public Decoder<Mesh> newDecoder() {
        return new ModularMeshDecoder();
    }

    /**
     * Returns a new instance of Segmenter<T> that should be used with a concrete implementation
     * of this interface.
     *
     * @return Segmenter<T>
     */
    @Override
    public Segmenter<Mesh> newSegmenter() {
        Segmenter<Mesh> segmenter = this.context.newSegmenter();
        if (segmenter == null) segmenter = new PassthroughSegmenter<Mesh>() {
            @Override
            protected SegmentContainer getSegmentFromContent(Mesh content) {
                return new Model3DSegment(content);
            }
        };
        return segmenter;
    }
}
