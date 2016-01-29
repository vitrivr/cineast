package ch.unibas.cs.dbis.cineast.core.segmenter;

import ch.unibas.cs.dbis.cineast.core.color.FuzzyColorHistogramQuantizer;
import ch.unibas.cs.dbis.cineast.core.data.Histogram;

public class FuzzyColorHistogram extends Histogram {

	public FuzzyColorHistogram() {
		super(FuzzyColorHistogramQuantizer.Color.values().length);
		int i = 0;
		for(FuzzyColorHistogramQuantizer.Color c : FuzzyColorHistogramQuantizer.Color.values()){
			this.binNames.put(c.toString(), i++);
		}
	}
	
	@Override
	public boolean areCompatible(Histogram hist) {
		return hist instanceof FuzzyColorHistogram;
	}
	
	public void add(FuzzyColorHistogramQuantizer.Color color){
		int index = this.binNames.get(color.toString());
		this.bins[index]++;
	}
	
	public double getBin(FuzzyColorHistogramQuantizer.Color color){
		return getBin(color.toString());
	}
	
	

}
