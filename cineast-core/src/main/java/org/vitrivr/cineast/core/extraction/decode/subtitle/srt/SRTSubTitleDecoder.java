package org.vitrivr.cineast.core.extraction.decode.subtitle.srt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.extraction.decode.subtitle.SubTitleDecoder;
import org.vitrivr.cineast.core.extraction.decode.subtitle.SubtitleItem;
import org.vitrivr.cineast.core.util.LogHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SRTSubTitleDecoder implements SubTitleDecoder {

    private static final Logger LOGGER = LogManager.getLogger();

    private List<SRTSubtitleItem> items = new ArrayList<>();

    private int maxId = -1;
    private long startTime = -1, endTime = -1;
    private int pointer = 0;

    public SRTSubTitleDecoder(Path file) {
        LOGGER.info("Loading SRT subtitle from {}", file);
        try (final BufferedReader reader = Files.newBufferedReader(file)) {
            String line1, line2, line3;
            StringBuffer text;
            int id = 0;
            long start, end;
            loop:
            while ((line1 = reader.readLine()) != null) {

                while (line1.isEmpty()) {
                    line1 = reader.readLine();
                    if (line1 == null) {
                        break loop;
                    }
                }

                try {
                    id = Integer.parseInt(line1);

                    this.maxId = Math.max(maxId, id);

                    line2 = reader.readLine();
                    if (line2 == null) {
                        break;
                    }
                    String[] timing = line2.split(" --> ");
                    if (timing.length != 2) {
                        break;
                    }

                    start = parseTime(timing[0]);
                    end = parseTime(timing[1]);

                    if (this.startTime == -1) {
                        this.startTime = start;
                    }

                    this.endTime = end;

                    text = new StringBuffer();
                    while ((line3 = reader.readLine()) != null && !line3.isEmpty()) {
                        text.append(line3);
                        text.append('\n');
                    }

                    items.add(new SRTSubtitleItem(id, start, end, text.toString()));
                } catch (NumberFormatException e) {
                    LOGGER.warn("Error while parsing subtitle item");
                    LOGGER.warn(LogHelper.getStackTrace(e));
                }
            }

            reader.close();
        } catch (IOException e) {
            LOGGER.warn("Error while loading subtitle");
            LOGGER.warn(LogHelper.getStackTrace(e));
        }
    }

    private static long parseTime(String time) {
        long h = 0, m = 0, s = 0, ms = 0;
        String[] splits = time.split(":");
        if (splits.length != 3) {
            return -1;
        }

        h = Long.parseLong(splits[0]);
        m = Long.parseLong(splits[1]);
        splits = splits[2].split(",");

        if (splits.length != 2) {
            return -1;
        }

        s = Long.parseLong(splits[0]);
        ms = Long.parseLong(splits[1]);

        return ms + 1000L * s + 60000L * m + 3600000L * h;
    }

    /**
     * Returns the number of {@link SubtitleItem}s in this {@link SubTitleDecoder}.
     *
     * @return Number of {@link SubtitleItem}s in this {@link SubTitleDecoder
     */
    @Override
    public int getNumerOfItems() {
        return this.items.size();
    }

    /**
     * Returns the subtitle item at the specified index position.
     *
     * @param index Position index.
     * @return {@link SubtitleItem} at the specified index position.
     */
    public SubtitleItem get(int index) {
        return this.items.get(index);
    }

    /**
     * Returns the {@link SubtitleItem} at the current pointer position.
     *
     * @return {@link SubtitleItem} at the current pointer position.
     */
    public SubtitleItem getLast() {
        return this.items.get(pointer);
    }

    /**
     * Increments the internal pointer by one. Returns true, if increment was successful and false otherwise.
     *
     * @return True if increment was successful and false otherwise.
     */
    public boolean increment() {
        if (this.pointer + 1 < this.items.size()) {
            this.pointer += 1;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Rewinds the {@link SubTitleDecoder} stream and sets the internal pointer to 0.
     */
    public void rewind() {
        this.pointer = 0;
    }

    @Override
    public String toString() {
        return "SRT Subtitle, " + getNumerOfItems() + " elements, maxId: " + this.maxId + ", startTime:  " + this.startTime + ", endTime:  " + this.endTime;
    }
}
