package ch.unibas.cs.dbis.cineast.core.data;

public interface ReadableFloatVector extends FeatureString, Distance<ReadableFloatVector> {

	int getElementCount();
	
	float getElement(int num);
	
	float[] toFloatArray();
	
}
