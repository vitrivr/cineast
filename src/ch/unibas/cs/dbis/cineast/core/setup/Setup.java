package ch.unibas.cs.dbis.cineast.core.setup;

import ch.unibas.cs.dbis.cineast.core.setup.EntityCreator.AttributeDefinition;
import ch.unibas.dmi.dbis.adam.http.AdamGrpc.AttributeType;

public class Setup {

	public static void main(String[] args) { //TODO this should ideally not be its own main
		EntityCreator ec = new EntityCreator();
		
		ec.createMultiMediaObjectsEntity();
		ec.createSegmentEntity();
		
		ec.createIdEntity("cineast_representativeframes", new AttributeDefinition("frame", AttributeType.INT));
		
		ec.createFeatureEntity("features_AverageColor", true);
		ec.createFeatureEntity("features_AverageColorARP44", true);
		ec.createFeatureEntity("features_AverageColorARP44Normalized", true);
		ec.createFeatureEntity("features_AverageColorCLD", true);
		ec.createFeatureEntity("features_AverageColorCLDNormalized", true);
		ec.createFeatureEntity("features_AverageColorGrid8", true);
		ec.createFeatureEntity("features_AverageColorGrid8Normalized", true);
		ec.createFeatureEntity("features_AverageColorRaster", true, "hist", "raster");
		ec.createFeatureEntity("features_AverageFuzzyHist", true);
		ec.createFeatureEntity("features_AverageFuzzyHistNormalized", true);
		ec.createFeatureEntity("features_ChromaGrid8", true);
		ec.createFeatureEntity("features_CLD", true);
		ec.createFeatureEntity("features_CLDNormalized", true);
		ec.createFeatureEntity("features_DominantColors", true);
		ec.createFeatureEntity("features_DominantEdgeGrid16", true);
		ec.createFeatureEntity("features_DominantEdgeGrid8", true);
		ec.createFeatureEntity("features_EdgeARP88", true);
		ec.createFeatureEntity("features_EdgeARP88Full", true);
		ec.createFeatureEntity("features_EdgeGrid16", true);
		ec.createFeatureEntity("features_EdgeGrid16Full", true);
		ec.createFeatureEntity("features_EHD", true);
		ec.createFeatureEntity("features_HueValueVarianceGrid8", true);
		ec.createFeatureEntity("features_MedianColor", true);
		ec.createFeatureEntity("features_MedianColorARP44", true);
		ec.createFeatureEntity("features_MedianColorARP44Normalized", true);
		ec.createFeatureEntity("features_MedianColorGrid8", true);
		ec.createFeatureEntity("features_MedianColorGrid8Normalized", true);
		ec.createFeatureEntity("features_MedianColorRaster", true, "hist", "raster");
		ec.createFeatureEntity("features_MedianFuzzyHist", true);
		ec.createFeatureEntity("features_MedianFuzzyHistNormalized", true);
		ec.createFeatureEntity("features_MotionHistogram", true);
		ec.createFeatureEntity("features_SaturationGrid8", true);
		ec.createFeatureEntity("features_STMP7EH", true);
		ec.createFeatureEntity("features_SubDivAverageFuzzyColor", true);
		ec.createFeatureEntity("features_SubDivMedianFuzzyColor", true);
		ec.createFeatureEntity("features_SubDivMotionHistogram2", true, "hist", "sums");
		ec.createFeatureEntity("features_SubDivMotionHistogram3", true, "hist", "sums");
		ec.createFeatureEntity("features_SubDivMotionHistogram4", true, "hist", "sums");
		ec.createFeatureEntity("features_SubDivMotionHistogram5", true, "hist", "sums");

	}

}
