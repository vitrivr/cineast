package org.vitrivr.cineast.core.segmenter.general;

import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.decode.general.Decoder;

import java.util.Map;
import java.util.function.Consumer;

/**
 * {@link Segmenter}s split a media file into chunks (segments). The nature of that chunk is specific to
 * the media type and the{@link Segmenter}s implementation. A segment could be anything from a shot of a video
 * to an arbitrary part of song or just a single image.
 *
 * @author rgasser
 * @version 1.0
 * @created 16.01.17
 */
public interface Segmenter<A> extends Runnable, AutoCloseable {
    /**
     * Method used to initialize the {@link Segmenter} and make it ready for use. It should be valid to call this multiple times
     * in order to, for example, re-use the {@link Segmenter} with different {@link Decoder}s. In this case, this method is supposed to
     * take care of the necessary cleanup, including but not limited to the disposal of the {@link Decoder}.
     *
     * @param decoder  {@link Decoder} used for media decoding.
     * @param object Media object that is about to be segmented.
     */
    void init(Decoder<A> decoder, MultimediaObjectDescriptor object);
    
    /**
     * Adds known segments from external source
     * @param segments 
     */
    void addKnownSegments(Iterable<SegmentDescriptor> segments);

    /**
     * Returns the next SegmentContainer from the source OR null if there are no more segments in the queue. As
     * generation of SegmentContainers can take some time (depending on the media-type), a null return does not
     * necessarily mean that the Segmenter is done segmenting. Use the complete() method to check this.
     *
     * <strong>Important:</strong> This method should be designed to block and wait for an appropriate amount of time if the
     * Segmenter is not yet ready to deliver another segment! It's up to the Segmenter how long that timeout should last.
     *
     * @return
     */
    SegmentContainer getNext() throws InterruptedException;

    /**
     * Indicates that the {@link Segmenter} is complete i.e. no new segments are to be expected.
     *
     * @return true if work is complete, false otherwise.
     */
    boolean complete();

    /**
     * Closes the {@link Segmenter}. This method should cleanup and relinquish all resources. Especially,
     * calling this method should also close the {@link Decoder} associated with this {@link Segmenter} instance.
     *
     * <strong>Note:</strong> It is unsafe to re-use a {@link Segmenter} after it has been closed.
     */
    @Override
    void close();
}
