package ch.unibas.cs.dbis.cineast.core.data;

public interface FloatVector extends FeatureString, ReadableFloatVector{
	
	void setElement(int num, float val);
	
}
