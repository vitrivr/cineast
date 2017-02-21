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

            double max = spectrum.getMaximum();
            double min = spectrum.getMinimum();
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
     * @param max Maximal value in DB should be color-coded.
     * @param value value for which a color-code is required.
     * @return
     */
    public static Color color (double min, double max, double value) {
        if (min > max) throw new IllegalArgumentException("Minimum must be smaller than maximum.");
        if (value < min) return Color.BLACK;
        if (value > max) return Color.WHITE;
        return Color.getHSBColor((float)((1.0/(Math.abs(max-min))) * value), 1.0f, 1.0f);
    }

}
