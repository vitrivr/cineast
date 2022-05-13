package org.vitrivr.cineast.core.data.query.containers;

import georegression.struct.point.Point2D_F32;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.frames.VideoDescriptor;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.extraction.decode.subtitle.SubtitleItem;
import org.vitrivr.cineast.core.util.web.ImageParser;

public class ImageQueryTermContainer extends AbstractQueryTermContainer {

  private MultiImage img;
  private VideoFrame videoFrame;
  private ArrayList<SubtitleItem> subitem = new ArrayList<SubtitleItem>(1);
  private List<Pair<Integer, LinkedList<Point2D_F32>>> paths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();
  private List<Pair<Integer, LinkedList<Point2D_F32>>> bgPaths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();
  private float relativeStart = 0, relativeEnd = 0;

  /**
   * Constructs an {@link ImageQueryTermContainer} from base 64 encoded image data.
   *
   * @param data    The image data that should be converted.
   * @param factory The {@link CachedDataFactory} that should be used to generate the {@link MultiImage}.
   */
  public ImageQueryTermContainer(String data, CachedDataFactory factory) {
    this(ImageParser.dataURLtoBufferedImage(data), factory);
  }

  public ImageQueryTermContainer(BufferedImage image, CachedDataFactory factory) {
    this.img = factory.newInMemoryMultiImage(image);
  }

  public ImageQueryTermContainer(BufferedImage image) {
    this(image, CachedDataFactory.getDefault());
  }

  public ImageQueryTermContainer(String data) {
    this(data, CachedDataFactory.getDefault());
  }

  public ImageQueryTermContainer(MultiImage img) {
    this.img = img;
  }

  @Override
  public MultiImage getAvgImg() {
    return this.img;
  }

  @Override
  public MultiImage getMedianImg() {
    return this.img;
  }

  @Override
  public VideoFrame getMostRepresentativeFrame() {
    if (this.videoFrame == null) {
      int id = (getStart() + getEnd()) / 2;
      this.videoFrame = new VideoFrame(id, 0, this.img, new VideoDescriptor(25, 40, this.img.getWidth(), this.img.getHeight()));
    }
    return this.videoFrame;
  }

  @Override
  public int getStart() {
    return 0;
  }

  @Override
  public int getEnd() {
    return 0;
  }

  @Override
  public List<SubtitleItem> getSubtitleItems() {
    return this.subitem;
  }

  @Override
  public float getRelativeStart() {
    return relativeStart;
  }

  public void setRelativeStart(float relativeStart) {
    this.relativeStart = relativeStart;
  }

  @Override
  public float getRelativeEnd() {
    return relativeEnd;
  }

  public void setRelativeEnd(float relativeEnd) {
    this.relativeEnd = relativeEnd;
  }

  @Override
  public List<Pair<Integer, LinkedList<Point2D_F32>>> getPaths() {
    return this.paths;
  }

  @Override
  public List<Pair<Integer, LinkedList<Point2D_F32>>> getBgPaths() {
    return this.bgPaths;
  }

  @Override
  public List<VideoFrame> getVideoFrames() {
    ArrayList<VideoFrame> _return = new ArrayList<VideoFrame>(1);
    _return.add(this.videoFrame);
    return _return;
  }


  public void addPath(LinkedList<Point2D_F32> path) {
    this.paths.add(new Pair<Integer, LinkedList<Point2D_F32>>(0, path));
  }

  public void addBgPath(LinkedList<Point2D_F32> path) {
    this.bgPaths.add(new Pair<Integer, LinkedList<Point2D_F32>>(0, path));
  }

}
