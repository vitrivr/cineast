package org.vitrivr.cineast.core.util;

import java.io.File;
import javax.activation.MimetypesFileTypeMap;

public final class MimeTypeHelper {

  private MimeTypeHelper(){}

  private static final MimetypesFileTypeMap filetypes;

  static{

    filetypes = new MimetypesFileTypeMap();

    //Video Mime Types
    filetypes.addMimeTypes("video/avi avi");
    filetypes.addMimeTypes("video/mp4 m4v");
    filetypes.addMimeTypes("video/mpeg m1v m2v mp2 mpg mpeg mpe mpa");
    filetypes.addMimeTypes("video/quicktime mov moov movie");
    filetypes.addMimeTypes("video/ogg ogv");
    filetypes.addMimeTypes("video/webm webm");
    filetypes.addMimeTypes("video/mp4 mp4");

    //Image Mime Types
    filetypes.addMimeTypes("image/jpeg jpg jpeg jpe");
    filetypes.addMimeTypes("image/png png");
    filetypes.addMimeTypes("image/tiff tif tiff");

    //Audio Mime Types
    filetypes.addMimeTypes("audio/mp4 m4a");
    filetypes.addMimeTypes("audio/aac aac");
    filetypes.addMimeTypes("audio/aiff aif aiff");
    filetypes.addMimeTypes("audio/mpeg mp1 mp2 mp3 mpg mpeg");
    filetypes.addMimeTypes("audio/ogg oga ogg");
    filetypes.addMimeTypes("audio/wav wav");
    filetypes.addMimeTypes("audio/flac flac");

    //3D Mime types
    filetypes.addMimeTypes("application/3d-stl stl STL");
    filetypes.addMimeTypes("application/3d-obj obj OBJ");
    filetypes.addMimeTypes("application/3d-off off OFF");


  }

  public static String getContentType(File file){
    return filetypes.getContentType(file);
  }

  public static String getContentType(String filename){
    return filetypes.getContentType(filename);
  }

}
