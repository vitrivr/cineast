package org.vitrivr.cineast.core.color;

import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;


/* for equations see http://www.easyrgb.com/ */
public final class ColorConverter {

  private ColorConverter() {
  }

  public static LabContainer XYZtoLab(float x, float y, float z) {
    return XYZtoLab((double) x, (double) y, (double) z);
  }

  public static LabContainer XYZtoLab(ReadableXYZContainer xyz) {
    return XYZtoLab(xyz.x, xyz.y, xyz.z);
  }

  public static LabContainer XYZtoLab(double x, double y, double z) {
    x /= 95.047d;
    y /= 100d;
    z /= 108.883d;

    if (x > 0.008856f) {
      x = Math.pow(x, (1d / 3d));
    } else {
      x = x * 7.787 + (16d / 116d);
    }

    if (y > 0.008856f) {
      y = Math.pow(y, (1d / 3d));
    } else {
      y = y * 7.787 + (16d / 116d);
    }

    if (z > 0.008856f) {
      z = Math.pow(z, (1d / 3d));
    } else {
      z = z * 7.787 + (16d / 116d);
    }

    return new LabContainer((116d * y) - 16d, 500d * (x - y), 200d * (y - z));
  }

  public static XYZContainer RGBtoXYZ(ReadableRGBContainer rgb) {
    return RGBtoXYZ(rgb.r / 255d, rgb.g / 255d, rgb.b / 255d);
  }

  public static XYZContainer RGBtoXYZ(double r, double g, double b) {
    if (r > 1f || r < 0f) {
      throw new IllegalArgumentException(r + " is outside of allowed range for red");
    }
    if (g > 1f || g < 0f) {
      throw new IllegalArgumentException(g + " is outside of allowed range for green");
    }
    if (b > 1f || b < 0f) {
      throw new IllegalArgumentException(b + " is outside of allowed range for blue");
    }

    if (r > 0.04045) {
      r = Math.pow((r + 0.055) / 1.055, 2.4);
    } else {
      r /= 12.92;
    }

    if (g > 0.04045) {
      g = Math.pow((g + 0.055) / 1.055, 2.4);
    } else {
      g /= 12.92;
    }

    if (b > 0.04045) {
      b = Math.pow((b + 0.055) / 1.055, 2.4);
    } else {
      b /= 12.92;
    }

    r *= 100d;
    g *= 100d;
    b *= 100d;

    return new XYZContainer(
        r * 0.4121 + g * 0.3576 + b * 0.1805,
        r * 0.2126 + g * 0.7152 + b * 0.0722,
        r * 0.0193 + g * 0.1192 + b * 0.9505);
  }

  public static LabContainer RGBtoLab(ReadableRGBContainer rgb) {
    return RGBtoLab(rgb.r, rgb.g, rgb.b);
  }

  public static LabContainer RGBtoLab(double r, double g, double b) {
    return XYZtoLab(RGBtoXYZ(r, g, b));
  }

  public static LabContainer RGBtoLab(int r, int g, int b) {
    return RGBtoLab(r / 255f, g / 255f, b / 255f);
  }

  public static LabContainer RGBtoLab(float r, float g, float b) {
    return RGBtoLab((double) r, (double) g, (double) b);
  }

  public static XYZContainer LabtoXYZ(ReadableLabContainer lab) {
    return LabtoXYZ(lab.L, lab.a, lab.b);
  }

  public static XYZContainer LabtoXYZ(float L, float a, float b) {
    return LabtoXYZ((double) L, (double) a, (double) b);
  }

  public static XYZContainer LabtoXYZ(double L, double a, double b) {
    return LabtoXYZ(L, a, b, null);
  }

  public static XYZContainer LabtoXYZ(double L, double a, double b, XYZContainer out) {
    if (out == null) {
      out = new XYZContainer();
    }

    double y = (L + 16d) / 116d;
    double x = a / 500d + y;
    double z = y - b / 200d;

    if (x > 0.20689303442) {
      x = x * x * x;
    } else {
      x = (x - 16d / 116d) / 7.787;
    }

    if (y > 0.20689303442) {
      y = y * y * y;
    } else {
      y = (y - 16d / 116d) / 7.787;
    }

    if (z > 0.20689303442) {
      z = z * z * z;
    } else {
      z = (z - 16d / 116d) / 7.787;
    }

    out.x = (float) (x * 95.047d);
    out.y = (float) (y * 100d);
    out.z = (float) (z * 108.883d);

    return out;
  }

  public static RGBContainer XYZtoRGB(ReadableXYZContainer xyz) {
    double x = xyz.x / 100d;
    double y = xyz.y / 100d;
    double z = xyz.z / 100d;

    double r = x * 3.2406 + y * -1.5372 + z * -0.4986;
    double g = x * -0.9689 + y * 1.8758 + z * 0.0415;
    double b = x * 0.0557 + y * -0.2040 + z * 1.0570;

    if (r > 0.0031308) {
      r = 1.055 * Math.pow(r, (1d / 2.4)) - 0.055;
    } else {
      r *= 12.92;
    }

    if (g > 0.0031308) {
      g = 1.055 * Math.pow(g, (1d / 2.4)) - 0.055;
    } else {
      g *= 12.92;
    }

    if (b > 0.0031308) {
      b = 1.055 * Math.pow(b, (1d / 2.4)) - 0.055;
    } else {
      b *= 12.92;
    }

    return new RGBContainer(r, g, b);
  }

  public static RGBContainer LabtoRGB(ReadableLabContainer lab) {
    return XYZtoRGB(LabtoXYZ(lab));
  }

  public static HSVContainer RGBtoHSV(ReadableRGBContainer rgb) {
    double r = rgb.r / 255d;
    double g = rgb.g / 255d;
    double b = rgb.b / 255d;

    double max = Math.max(Math.max(r, g), b);
    double min = Math.min(Math.min(r, g), b);
    double del = max - min;

    double h = 0, s = 0, v = max;
    if (max > 0) {
      s = del / max;

      double dr = (((max - r) / 6d) + (del / 2)) / del;
      double dg = (((max - g) / 6d) + (del / 2)) / del;
      double db = (((max - b) / 6d) + (del / 2)) / del;

      if (Double.compare(r, max) == 0) {
        h = db - dg;
      } else if (Double.compare(g, max) == 0) {
        h = (1d / 3d) + dr - db;
      } else {
        h = (2d / 3d) + dg - dr;
      }

      if (h < 0) {
        h += 1;
      }
      if (h > 1) {
        h -= 1;
      }
    }

    return new HSVContainer(h, s, v);
  }

  public static RGBContainer HSVtoRGB(ReadableHSVContainer hsv) {
    int r = 0, g = 0, b = 0;
    if (hsv.s < 0.00001f) {
      r = g = b = (int) (hsv.v * 255f);
    } else {
      double h = (hsv.h * 6d) % 6d;
      int i = (int) Math.floor(h);
      int v1 = (int) (hsv.v * (1d - hsv.s) * 255f);
      int v2 = (int) (hsv.v * (1d - hsv.s * (h - i)) * 255f);
      int v3 = (int) (hsv.v * (1d - hsv.s * (1 - (h - i))) * 255f);

      switch (i) {
        case 0: {
          r = (int) (255f * hsv.v);
          g = v3;
          b = v1;
          break;
        }
        case 1: {
          r = v2;
          g = (int) (255f * hsv.v);
          b = v1;
          break;
        }
        case 2: {
          r = v1;
          g = (int) (255f * hsv.v);
          b = v3;
          break;
        }
        case 3: {
          r = v1;
          g = v2;
          b = (int) (hsv.v * 255f);
          break;
        }
        case 4: {
          r = v3;
          g = v1;
          b = (int) (hsv.v * 255f);
          break;
        }
        default: {
          r = (int) (255f * hsv.v);
          g = v1;
          b = v2;
          break;
        }
      }
    }
    return new RGBContainer(r, g, b);
  }

  public static YCbCrContainer RGBtoYCbCr(ReadableRGBContainer rgb) {
    int y = Math.round((rgb.r * 65.738f + rgb.g * 129.057f + rgb.b * 25.064f) / 256f + 16);
    int cb = Math.round((rgb.r * -37.945f + rgb.g * -74.494f + rgb.b * 112.439f) / 256f + 128);
    int cr = Math.round((rgb.r * 112.439f + rgb.g * -94.154f + rgb.b * -18.285f) / 256f + 128);

    return new YCbCrContainer(y, cb, cr);

  }

  /**
   * can be used to collapse Lab values which are equivalent in RGB into consistent representation
   */
  public static ReadableLabContainer toRGBandBack(ReadableLabContainer lab) {
    ReadableRGBContainer rgb = LabtoRGB(lab);
    return cachedRGBtoLab(rgb.toIntColor());
  }

  private static TIntObjectMap<ReadableLabContainer> rgbToLabCache = TCollections
      .synchronizedMap(new TIntObjectHashMap<ReadableLabContainer>(100000));

  public static ReadableLabContainer cachedRGBtoLab(int rgb) {

    ReadableLabContainer _return = rgbToLabCache.get(rgb);
    if (_return == null) {
      _return = RGBtoLab(new RGBContainer(rgb));
      rgbToLabCache.put(rgb, _return);
    }
    return _return;
  }

  private static TIntObjectMap<ReadableHSVContainer> rgbToHSVCache = TCollections
      .synchronizedMap(new TIntObjectHashMap<ReadableHSVContainer>(100000));

  public static ReadableHSVContainer cachedRGBtoHSV(int rgb) {
    ReadableHSVContainer _return = rgbToHSVCache.get(rgb);
    if (_return == null) {
      _return = RGBtoHSV(new RGBContainer(rgb));
      rgbToHSVCache.put(rgb, _return);
    }
    return _return;
  }

  /**
   * @param colors assumed to be RGB and (x,y) can be accessed at y*width+x TODO Mabye use
   * https://stackoverflow.com/questions/596216/formula-to-determine-brightness-of-rgb-color for
   * optimization
   */
  public static float[] RGBtoLuminance(int[] colors) {
    float[] luminance = new float[colors.length];

    for (int i = 0; i < colors.length; i++) {
      luminance[i] = ReadableRGBContainer.getLuminanceBT601(colors[i]);
    }
    return luminance;
  }

}
