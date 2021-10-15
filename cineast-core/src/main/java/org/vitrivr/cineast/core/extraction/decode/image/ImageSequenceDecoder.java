package org.vitrivr.cineast.core.extraction.decode.image;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.DecoderConfig;
import org.vitrivr.cineast.core.config.CacheConfig;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;
import org.vitrivr.cineast.core.util.MimeTypeHelper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Decoder for media object of type {@link MediaType.IMAGE_SEQUENCE}, i.e. a sequence of images contained in a single
 * folder that, in terms of Cineast's data model, belong together.
 *
 * <strong>Important:</strong> Unlike other implementations of {@link Decoder} this class operates on folders only!
 * It assumes that the images belonging to the sequence are contained in that folder (no subfolders) and that the images'
 * occurrence in the sequence correspond to the natural ordering of the filenames in ascending direction.
 *
 * @see ImageSequence
 *
 */
public class ImageSequenceDecoder implements Decoder<ImageSequence> {

  /** Default logging facility. */
  private static final Logger LOGGER = LogManager.getLogger();

  private static final Set<String> SUPPORTED = new HashSet<>();
  static {
    SUPPORTED.add("application/octet-stream");
  }

  private final DirectoryStream.Filter<Path> filter = file -> Files.isRegularFile(file) && ImageSequence.SUPPORTED_FILES.contains(MimeTypeHelper.getContentType(file));

  /** Path to the folder that contains the next {@link ImageSequence}. */
  private Path path;

  /** {@link DecoderConfig} instance to use. */
  private DecoderConfig decoderConfig;

  /** {@link CacheConfig} instance to use. */
  private CacheConfig cacheConfig;

  /**
   * Initializes the decoder with a file. This is a necessary step before content can be retrieved from
   * the decoder by means of the getNext() method.
   *
   * @param path Path to the file that should be decoded.
   * @param decoderConfig {@link DecoderConfig} used by this {@link ImageSequenceDecoder}.
   * @param cacheConfig The {@link CacheConfig} used by this {@link ImageSequenceDecoder}
   * @return True if initialization was successful, false otherwise.
   */
  @Override
  public boolean init(Path path, DecoderConfig decoderConfig, CacheConfig cacheConfig) {
    this.path = path;
    this.decoderConfig = decoderConfig;
    this.cacheConfig = cacheConfig;
    return true;
  }

  @Override
  public void close() { }

  @Override
  public ImageSequence getNext() {
    if (this.path == null) {
      throw new IllegalStateException("Cannot invoke getNext() on ImageSequenceDecoder that has completed.");
    }
    final ImageSequence sequence = new ImageSequence(this.decoderConfig);
    if (this.path != null) {
      if (Files.isDirectory(path)) {
        try (final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path, this.filter)){
          final LinkedList<Path> paths = new LinkedList<>();
          for (Path p: directoryStream){
            paths.add(p);
          }
          paths.sort(Comparator.comparing(Path::getFileName));
          for (Path p: paths){
            sequence.add(p);
          }
        } catch (IOException e) {
          LOGGER.fatal("A severe error occurred while trying to decode an image file for image sequence sequence '{}'.", this.path.getFileName());
        }
      }
    }
    this.path = null;
    return sequence;
  }

  @Override
  public int count() {
    if (this.path != null) {
      return 1;
    } else {
      return 0;
    }
  }

  @Override
  public boolean complete() {
    return this.path == null;
  }

  @Override
  public Set<String> supportedFiles() {
    return SUPPORTED;
  }

  @Override
  public boolean canBeReused() {
    return false;
  }
}
