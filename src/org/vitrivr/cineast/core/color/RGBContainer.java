package org.vitrivr.cineast.core.color;

import org.vitrivr.cineast.core.data.FloatVector;

public class RGBContainer extends ReadableRGBContainer implements FloatVector{

	public RGBContainer(ReadableRGBContainer rgb){
		super(rgb.r, rgb.g, rgb.b);
	}
	
	RGBContainer() {
		super(0, 0, 0);
	}
	
	public RGBContainer(int r, int g, int b){
		super(r, g, b);
	}
	
	public RGBContainer(int r, int g, int b, int a){
		super(r, g, b, a);
	}
	
	public RGBContainer(float r, float g, float b){
		super(r, g, b);
	}
	
	public RGBContainer(float r, float g, float b, float a){
		super(r, g, b, a);
	}
	
	public RGBContainer(double r, double g, double b){
		super(r, g, b);
	}
	
	public RGBContainer(double r, double g, double b, double a){
		super(r, g, b, a);
	}
	
	public RGBContainer(int color){
		super(color);
	}
	
	@Override
	public void setElement(int num, float val) {
		switch(num){
		case 0:{r = Math.round(val * 255f); break;}
		case 1:{g = Math.round(val * 255f); break;}
		case 2:{b = Math.round(val * 255f); break;}
		case 3:{a = Math.round(val * 255f); break;}
		}
		
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + a;
		result = prime * result + b;
		result = prime * result + g;
		result = prime * result + r;
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
		RGBContainer other = (RGBContainer) obj;
		if (a != other.a) {
      return false;
    }
		if (b != other.b) {
      return false;
    }
		if (g != other.g) {
      return false;
    }
		if (r != other.r) {
      return false;
    }
		return true;
	}

	public RGBContainer set(int intcolor){
	  this.r = getRed(intcolor);
	  this.g = getGreen(intcolor);
	  this.b = getBlue(intcolor);
	  this.a = getAlpha(intcolor);
	  return this;
	}
	
}
