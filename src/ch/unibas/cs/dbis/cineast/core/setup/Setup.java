package ch.unibas.cs.dbis.cineast.core.setup;

public class Setup {

	public static void main(String[] args) { //TODO this should ideally not be its own main
		EntityCreator ec = new EntityCreator();
		
		ec.createMultiMediaObjectsEntity();
		ec.createSegmentEntity();
		
		ec.createFeatureEntity("features.AverageColor", true);
		ec.createFeatureEntity("features.AverageColorARP44", true);
		ec.createFeatureEntity("features.AverageColorARP44Normalized", true);
		ec.createFeatureEntity("features.AverageColorCLD", true);
		ec.createFeatureEntity("features.AverageColorCLDNormalized", true);
		ec.createFeatureEntity("features.AverageColorGrid8", true);
		ec.createFeatureEntity("features.AverageColorGrid8Normalized", true);
		ec.createFeatureEntity("features.AverageColorRaster", true, "raster", "hist");
		ec.createFeatureEntity("features.AverageFuzzyHist", true);
		ec.createFeatureEntity("features.AverageFuzzyHistNormalized", true);
		ec.createFeatureEntity("features.ChromaGrid8", true);
		ec.createFeatureEntity("features.CLD", true);
		ec.createFeatureEntity("features.CLDNormalized", true);
		ec.createFeatureEntity("features.DominantColors", true);
		ec.createFeatureEntity("features.DominantEdgeGrid16", true);
		ec.createFeatureEntity("features.DominantEdgeGrid8", true);
		ec.createFeatureEntity("features.EdgeARP88", true);
		ec.createFeatureEntity("features.EdgeARP88Full", true);
		ec.createFeatureEntity("features.EdgeGrid16", true);
		ec.createFeatureEntity("features.EdgeGrid16Full", true);
		ec.createFeatureEntity("features.EHD", true);
		ec.createFeatureEntity("features.HueValueVarianceGrid8", true);
		ec.createFeatureEntity("features.MedianColor", true);
		ec.createFeatureEntity("features.MedianColorARP44", true);
		ec.createFeatureEntity("features.MedianColorARP44Normalized", true);
		ec.createFeatureEntity("features.MedianColorGrid8", true);
		ec.createFeatureEntity("features.MedianColorGrid8Normalized", true);
		ec.createFeatureEntity("features.MedianColorRaster", true, "raster", "hist");
		ec.createFeatureEntity("features.MedianFuzzyHist", true);
		ec.createFeatureEntity("features.MedianFuzzyHistNormalized", true);
		ec.createFeatureEntity("features.MotionHistogram", true);
		ec.createFeatureEntity("features.SaturationGrid8", true);
		ec.createFeatureEntity("features.STMP7EH", true);
		ec.createFeatureEntity("features.SubDivAverageFuzzyColor", true);
		ec.createFeatureEntity("features.SubDivMedianFuzzyColor", true);
		ec.createFeatureEntity("features.SubDivMotionHistogram2", true, "hist", "sums");
		ec.createFeatureEntity("features.SubDivMotionHistogram3", true, "hist", "sums");
		ec.createFeatureEntity("features.SubDivMotionHistogram4", true, "hist", "sums");
		ec.createFeatureEntity("features.SubDivMotionHistogram5", true, "hist", "sums");

		
	}

}
