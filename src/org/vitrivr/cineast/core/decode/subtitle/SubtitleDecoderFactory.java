package org.vitrivr.cineast.core.decode.subtitle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.decode.subtitle.cc.CCSubTitleDecoder;
import org.vitrivr.cineast.core.decode.subtitle.srt.SRTSubTitleDecoder;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class can be used to instantiate {@link SubTitleDecoder} instances for a given video file or subtitle file. It
 * uses the Java reflection API and a map of suffices to decoder classes to facilitate this functionality.
 *
 * <strong>Important: </strong> Classes that are supposed to be instantiated through this factory must have a public,
 * unary constructor with a {@link Path} argument. Failing to provide such a constructor will result in an runtime error.
 */
public final class SubtitleDecoderFactory {

    /** */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Private constructor for this class cannot be instantiated.
     */
    private SubtitleDecoderFactory() { }

    /**
     * Map of the different subtitle file suffices and the associated {@link SubTitleDecoder} decoder instances.
     */
    private static final Map<String, Class<? extends SubTitleDecoder>> SUBTITLE_EXTENSION_MAP = new HashMap<>();
    static {
        registerSubtitleDecoder(".srt", SRTSubTitleDecoder.class);
        registerSubtitleDecoder(".cc", CCSubTitleDecoder.class);
    }

    /**
     * Tries to return a {@link SubTitleDecoder} object for the specified video file. This method expects a separate file with
     * the same name as the video located in the same folder. If there is a registered decoder for the file, that decoer
     * is instantiated and returned.
     *
     * @see SubtitleDecoderFactory#subtitleForFile
     *
     * @param path Path to the video file for which subtitles should be decoded.
     * @return Optional {@link SubTitleDecoder} object.
     */
    public static Optional<SubTitleDecoder> subtitleForVideo(Path path) {
        final String filename = path.getFileName().toString();
        final String suffix =  filename.substring(filename.lastIndexOf('.'), filename.length());
        for (Map.Entry<String, Class<? extends SubTitleDecoder>> entry : SUBTITLE_EXTENSION_MAP.entrySet()) {
            final Path subtitleFile = path.getParent().resolve(filename.replace(suffix, entry.getKey()));
            final Optional<SubTitleDecoder> item = subtitleForFile(subtitleFile, entry.getValue());
            if (item.isPresent()) {
                return item;
            }
        }

        /* Returns an empty optional. */
        return Optional.empty();
    }

    /**
     * Tries to decode the subtitle located under the specified path using the specified {@link SubTitleDecoder} decoder. If
     * this method succeeds, a new {@link SubTitleDecoder} object is returned. Otherwise, the returned optional will be empty.
     *
     * @param path The subtitle file that should be decoded.
     * @param clazz The {@link SubTitleDecoder} decoder that should be used to decode the file.
     * @return Optional {@link SubTitleDecoder} object.
     */
    public static Optional<SubTitleDecoder> subtitleForFile(Path path, Class<? extends SubTitleDecoder> clazz) {
        if (Files.isReadable(path)) {
            try {
                return Optional.of(clazz.getDeclaredConstructor(Path.class).newInstance(path));
            } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                LOGGER.error("Failed to instantiate the SubTitleDecoder decoder '{}'.", clazz.getName(), e);
            }
        } else {
            LOGGER.warn("The specified file is not readable '{}'. Does it exist?", path);
        }

        /* Returns an empty optional. */
        return Optional.empty();
    }

    /**
     * Registers a new {@link SubTitleDecoder} for a specific suffix. This method cannot be used to override already
     * registered suffices!
     */
    public static void registerSubtitleDecoder(String suffix, Class<? extends SubTitleDecoder> clazz) {
        SUBTITLE_EXTENSION_MAP.putIfAbsent(suffix, clazz);
    }
}
