package org.vitrivr.cineast.core.util;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;

public final class MimeTypeHelper {

  private MimeTypeHelper(){}

  private static final HashMap<String,String> FILETYPES;

  static{

    FILETYPES = new HashMap<>();

    //Video Mime Types
    FILETYPES.put("avi", "video/avi");
    FILETYPES.put("m4v", "video/mp4");
    FILETYPES.put("m1v", "video/mpeg");
    FILETYPES.put("m2v", "video/mpeg");
    FILETYPES.put("mp2", "video/mpeg");
    FILETYPES.put("mpg", "video/mpeg");
    FILETYPES.put("mpeg", "video/mpeg");
    FILETYPES.put("mpe", "video/mpeg");
    FILETYPES.put("mpa", "video/mpeg");
    FILETYPES.put("mov", "video/quicktime");
    FILETYPES.put("moov", "video/quicktime");
    FILETYPES.put("movie", "video/quicktime");
    FILETYPES.put("ogv", "video/ogg");
    FILETYPES.put("webm", "video/webm");
    FILETYPES.put("mp4", "video/mp4");

    //Image Mime Types
    FILETYPES.put("jpg", "image/jpeg");
    FILETYPES.put("jpeg", "image/jpeg");
    FILETYPES.put("jpe", "image/jpeg");
    FILETYPES.put("png", "image/png");
    FILETYPES.put("tif", "image/tiff");
    FILETYPES.put("tiff", "image/tiff");

    //Audio Mime Types
    FILETYPES.put("m4a", "audio/mp4");
    FILETYPES.put("aac", "audio/aac");
    FILETYPES.put("aif", "audio/aiff");
    FILETYPES.put("aiff", "audio/aiff");
    FILETYPES.put("wav", "audio/wav");
    FILETYPES.put("wave", "audio/wav");
    FILETYPES.put("mp1", "audio/mpeg");
    FILETYPES.put("mp3", "audio/mpeg");
    FILETYPES.put("oga", "audio/ogg");
    FILETYPES.put("ogg", "audio/ogg");
    FILETYPES.put("flac", "audio/flac");

    //3D Mime types (self-defimed)
    FILETYPES.put("stl", "application/3d-stl");
    FILETYPES.put("obj", "application/3d-obj");
    FILETYPES.put("off", "application/3d-off");
  }

  public static String getContentType(File file){
    return getContentType(file.getName());

  }

  public static String getContentType(Path file){
    return getContentType(file.getFileName().toString());
  }

  public static String getContentType(String filename){
    final String[] split = filename.split("\\.");
    if (split.length > 0) {
      return FILETYPES.getOrDefault(split[split.length-1], "application/octet-stream");
    } else {
      return "application/octet-stream";
    }
  }
}
