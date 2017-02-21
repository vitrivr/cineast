package org.vitrivr.cineast.core.util.fft;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 05.02.17
 */
public class SpectrumVisalizer {

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

    private SpectrumVisalizer() {}


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

        if (spectra.size() == 0) return null;

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
                image.setRGB(x, (height - 1) - y, SpectrumVisalizer.color(-60, 0, intensity).getRGB());
            }
        }

        return image;

    }

    /**
     * Returns the color-code for a dB value given a range that should be color-coded.
     *
     * @param min Minimal value in dB that should be color-coded.
     * @param max Maximal value in db should be color-coded.
     * @param value value for which a color-code is required.
     * @return
     */
    public static Color color (double min, double max, double value) {
        if (min > max) throw new IllegalArgumentException("Minimum must be smaller than maximum.");
        if (Double.isNaN(value) || value == 0.0f) return YlOrRd[YlOrRd.length-1];
        double ratio = (YlOrRd.length-1)/(max-min);
        int idx = (YlOrRd.length-1) - (int)((value-min) * ratio);

        if (idx > YlOrRd.length-1) return YlOrRd[YlOrRd.length-1];
        if (idx < 0) return YlOrRd[0];
        return YlOrRd[idx];
    }

}
