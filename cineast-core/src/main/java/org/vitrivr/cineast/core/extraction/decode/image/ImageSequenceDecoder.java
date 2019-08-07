package org.vitrivr.cineast.core.extraction.decode.image;

import org.vitrivr.cineast.core.config.DecoderConfig;
import org.vitrivr.cineast.core.config.CacheConfig;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;

public class ImageSequenceDecoder implements Decoder<BufferedImage> {

  private final DefaultImageDecoder imageDecoder = new DefaultImageDecoder();

  private final DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {

    Set<String> supportedFiles = imageDecoder.supportedFiles();

    public boolean accept(Path file) throws IOException {
      for (String ending : supportedFiles){
        if (file.toFile().isFile() && file.toFile().getName().toLowerCase().endsWith(ending.toLowerCase())){
          return true;
        }
      }
      return false;
    }
  };

  private final Queue<Path> imagePaths = new ArrayDeque<>();

  private int count = 0;

  private DecoderConfig config;

  /**
   * Initializes the decoder with a file. This is a necessary step before content can be retrieved from
   * the decoder by means of the getNext() method.
   *
   * @param path Path to the file that should be decoded.
   * @param decoderConfig {@link DecoderConfig} used by this {@link Decoder}.
   * @param cacheConfig The {@link CacheConfig} used by this {@link Decoder}
   * @return True if initialization was successful, false otherwise.
   */
  @Override
  public boolean init(Path path, DecoderConfig decoderConfig, CacheConfig cacheConfig) {

    imagePaths.clear();
    this.config = decoderConfig;

    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path, filter)){

      for (Path p: directoryStream){
        imagePaths.add(p);
      }

    } catch (IOException e) { //TODO
      e.printStackTrace();
      return false;
    }

    return false;
  }

  @Override
  public void close() {
    this.imagePaths.clear();
  }

  @Override
  public BufferedImage getNext() {

    Path p = this.imagePaths.poll();
    if (p == null){
      return null;
    }

    this.imageDecoder.init(p, this.config, null);
    ++this.count;

    return this.imageDecoder.getNext();
  }

  @Override
  public int count() {
    return this.count;
  }

  @Override
  public boolean complete() {
    return !this.imagePaths.isEmpty();
  }

  @Override
  public Set<String> supportedFiles() {
    return this.imageDecoder.supportedFiles();
  }

  @Override
  public boolean canBeReused() {
    return false;
  }
}
