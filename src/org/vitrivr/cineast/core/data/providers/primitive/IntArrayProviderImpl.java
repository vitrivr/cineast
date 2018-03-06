package org.vitrivr.cineast.core.data.providers.primitive;

import java.util.Arrays;

public class IntArrayProviderImpl implements IntArrayProvider, FloatArrayProvider {

	private final int[] value;
  private float[] fvalue = null;
	
	public IntArrayProviderImpl(int[] value){
		if(value == null){
			throw new NullPointerException("int[] cannot be null");
		}
		this.value = value;
	}

	@Override
	public int[] getIntArray() {
		return this.value;
	}

  @Override
  public float[] getFloatArray() {
    if (this.fvalue == null) {
      this.fvalue = new float[this.value.length];
      synchronized (this.fvalue) {
        for (int i = 0; i < this.value.length; ++i) {
          this.fvalue[i] = this.value[i];
        }
      }
    }
    return this.fvalue;
  }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(value);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
      return true;
    }
		if (obj == null) {
      return false;
    }
		if (getClass() != obj.getClass()) {
      return false;
    }
		IntArrayProviderImpl other = (IntArrayProviderImpl) obj;
		if (!Arrays.equals(value, other.value)) {
      return false;
    }
		return true;
	}

	@Override
	public String toString() {
		return String.format("IntArrayProviderImpl [value=%s]", Arrays.toString(value));
	}
	
}
