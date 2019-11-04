package org.vitrivr.cineast.core.extraction.decode.subtitle;

/**
 * Represents a single subtitle item as found in videos. Typically, a subtitle item has a text, and a specified
 * presentation time within the video.
 */
public interface SubtitleItem {

	/**
	 * Returns the presentation length of the {@link SubtitleItem} in ms.
	 *
	 * @return presentation length of the {@link SubtitleItem} in ms
	 */
	int getLength();

	/**
	 * Returns the text of the {@link SubtitleItem}.
	 *
	 * @return Text of the {@link SubtitleItem}.
	 */
	String getText();

	/**
	 * Returns the start presentation timestamp in ms.
	 *
	 * @return Start presentation timestamp in m
	 */
	long getStartTimestamp();

	/**
	 * Returns the start presentation timestamp in ms.
	 *
	 * @return Start presentation timestamp in m
	 */
	long getEndTimestamp();
}