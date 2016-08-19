package org.vitrivr.cineast.core.data;

import java.util.List;

public interface ReadableFloatVector extends Distance<ReadableFloatVector> {

	int getElementCount();
	
	float getElement(int num);
	
	/**
	 * maps the vector to a float array
	 * @param arr the array to write into. If arr is null or does not have the correct length, a new array is generated instead
	 */
	float[] toArray(float[] arr);
	
	List<Float> toList(List<Float> list);
}
