package org.vitrivr.cineast.core.extraction.decode.subtitle;

import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.extraction.decode.video.FFMpegVideoDecoder;

/**
 * Represents a subtitle decoder instance. Basically, a {@link SubTitleDecoder} instance provides access to a stream of
 * {@link SubtitleItem}s in temporal order.
 *
 * The decoder uses an internal pointer that can be incremented sequentially to access the latest items. Muxing with a stream of
 * {@link VideoFrame}s is typically done via the presentation timestamp of both the {@link VideoFrame} and {@link SubtitleItem}
 *
 * @see VideoFrame
 * @see FFMpegVideoDecoder
 */
public interface SubTitleDecoder {
    /**
     * Returns the number of {@link SubtitleItem}s in this {@link SubTitleDecoder}.
     *
     * @return Number of {@link SubtitleItem}s in this {@link SubTitleDecoder
     */
    public abstract int getNumerOfItems();

    /**
     * Returns the subtitle item at the specified index position.
     *
     * @param index Position index.
     * @return {@link SubtitleItem} at the specified index position.
     */
    public abstract SubtitleItem get(int index);

    /**
     * Returns the {@link SubtitleItem} at the current pointer position.
     *
     * @return {@link SubtitleItem} at the current pointer position.
     */
    public abstract SubtitleItem getLast();

    /**
     * Increments the internal pointer by one. Returns true, if increment was successful and false otherwise.
     *
     * @return True if increment was successful and false otherwise.
     */
    public abstract boolean increment();

    /**
     * Rewinds the {@link SubTitleDecoder} stream and sets the internal pointer to 0.
     */
    public abstract void rewind();
}