package org.vitrivr.cineast.core.extraction.segmenter;

import org.vitrivr.cineast.core.color.FuzzyColorHistogramQuantizer;
import org.vitrivr.cineast.core.data.Histogram;

public class SubdividedFuzzyColorHistogram extends Histogram {

	private int subdivisions;
	
	public SubdividedFuzzyColorHistogram(int subdivisions) {
		super(subdivisions * subdivisions * FuzzyColorHistogramQuantizer.Color.values().length);
		this.subdivisions = subdivisions;
		for(int s = 0; s < subdivisions*subdivisions; ++s){
			int i = 0;
			for(FuzzyColorHistogramQuantizer.Color c : FuzzyColorHistogramQuantizer.Color.values()){
				this.binNames.put(c.toString() + s, i++);
			}
		}
	}

	@Override
	public boolean areCompatible(Histogram hist) {
		return hist instanceof SubdividedFuzzyColorHistogram
				&& ((SubdividedFuzzyColorHistogram)hist).subdivisions == subdivisions;
	}
	
	public void add(FuzzyColorHistogramQuantizer.Color color, int subdivision){
		int index = this.binNames.get(color.toString() + subdivision);
		this.bins[index]++;
	}
	
	public double getBin(FuzzyColorHistogramQuantizer.Color color, int subdivision){
		return getBin(color.toString() + subdivision);
	}

}
