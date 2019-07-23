package org.vitrivr.cineast.core.util.dsp.visualization;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.audio.HPCP;
import org.vitrivr.cineast.core.util.dsp.fft.Spectrum;

/**
 * @author rgasser
 * @version 1.0
 * @created 05.02.17
 */
public class AudioSignalVisualizer {

    /** YlOrRd palette used for visualization. */
    private final static Color[] YlOrRd = {
            new Color(255,255,255),
            new Color(255,255,234),
            new Color(255,237,160),
            new Color(254,217,118),
            new Color(254,178,76),
            new Color(253,141,61),
            new Color(252,78,42),
            new Color(227,26,28),
            new Color(189,0,38),
            new Color(128,0,38),
            new Color(100,0,32),
            new Color(80,0,16),
            new Color(0,0,0),
    };

    private AudioSignalVisualizer() {}


    /**
     * This method visualizes a spectrogram that shows temporal development of a signals spectogram (i.e time vs. frequency
     * vs power distribution).
     *
     * x-axis: Relative time
     * y-axis: Frequency bins
     * color: Power in bin (dB)
     *
     * @param spectra List of spectra (one per time-slot)
     * @param width Width of spectrogram in pixels.
     * @param height Height of spectrogram in pixels.
     * @return BufferedImage containing the spectogram.
     */
    public static BufferedImage visualizeSpectogram(List<Spectrum> spectra, int width, int height) {

        if (spectra.isEmpty()) {
          return null;
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);


        final float width_time_ratio = (float)spectra.size()/((float)width);
        final float height_freq_ratio = (float)spectra.get(0).size()/((float)height);

        for (int x=0;x<width;x++) {
            Spectrum spectrum = spectra.get((int)Math.floor(x * width_time_ratio));

            double max = spectrum.getMaximum().second;
            double min = spectrum.getMinimum().second;
            double diff = max-min;

            for (int y=0;y<height;y++) {
                double value = 0;
                int freqidx = (int)Math.floor((y) * height_freq_ratio);
                int nextfreqidx = (int)Math.floor((y+1) * height_freq_ratio);
                for (int c=freqidx;c<nextfreqidx; c++) {
                    value += spectrum.getValue(c);
                }
                double intensity = 10*Math.log10((value-min)/diff);
                image.setRGB(x, (height - 1) - y, AudioSignalVisualizer.color(-60, 0, intensity).getRGB());
            }
        }

        return image;

    }

    /**
     * This method visualizes a chromagram that displays the temporal development of the signal's energy in one
     * of the twelve pitch classes.
     *
     * @param hpcp HPCP (Harmonic Pitch Class Profile) from which to derive the chromagram.
     * @param width Width of chromagram in pixels.
     * @param height Height of chromagram in pixels.
     * @return BufferedImage containing the chromagram.
     */
    public static BufferedImage visualizeChromagram(HPCP hpcp, int width, int height) {
        if (hpcp.size() == 0) {
          return null;
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        final float width_time_ratio = (float)hpcp.size()/((float)width);
        final float height_freq_ratio = hpcp.getResolution().bins/((float)height);
        for (int x=0;x<width;x++) {
            float[] spectrum = hpcp.getMaxNormalizedHpcp((int)Math.floor(x * width_time_ratio));
            for (int y=0;y<height;y++) {
                int idx = (int)Math.floor((y) * height_freq_ratio);
                double value = spectrum[idx];
                image.setRGB(x, (height - 1) - y, AudioSignalVisualizer.color(0.0, 1.0, value).getRGB());
            }
        }

        return image;
    }

    /**
     * This method visualizes a CENS chromagram, which displays the temporal development of the signal's energy in one
     * of the twelve pitch classes.
     *
     * @see org.vitrivr.cineast.core.util.audio.CENS
     *
     * @param cens 2D array containing the CENS features.
     * @param width Width of chromagram in pixels.
     * @param height Height of chromagram in pixels.
     * @return BufferedImage containing the chromagram.
     */
    public static BufferedImage visualizeCens(double[][] cens, int width, int height) {
        if (cens.length == 0) {
          return null;
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        final float width_time_ratio = (float)cens.length/((float)width);
        final float height_freq_ratio = cens[0].length/((float)height);
        for (int x=0;x<width;x++) {
            double[] spectrum = MathHelper.normalizeL2(cens[(int)Math.floor(x * width_time_ratio)]);
            for (int y=0;y<height;y++) {
                int idx = (int)Math.floor((y) * height_freq_ratio);
                double value = spectrum[idx];
                image.setRGB(x, (height - 1) - y, AudioSignalVisualizer.color(0.0, 1.0, value).getRGB());
            }
        }

        return image;
    }

    /**
     * Returns the color-code for a dB value given a range that should be color-coded.
     *
     * @param min Minimal value in dB that should be color-coded.
     * @param max Maximal value in dB should be color-coded.
     * @param value value for which a color-code is required.
     * @return
     */
    public static Color color (double min, double max, double value) {
        if (min > max) {
          throw new IllegalArgumentException("Minimum must be smaller than maximum.");
        }
        if (Double.isNaN(value) || value == 0.0f) {
          return YlOrRd[YlOrRd.length-1];
        }
        double ratio = (YlOrRd.length-1)/(max-min);
        int idx = (YlOrRd.length-1) - (int)((value-min) * ratio);

        if (idx > YlOrRd.length-1) {
          return YlOrRd[YlOrRd.length-1];
        }
        if (idx < 0) {
          return YlOrRd[0];
        }
        return YlOrRd[idx];
    }

}
