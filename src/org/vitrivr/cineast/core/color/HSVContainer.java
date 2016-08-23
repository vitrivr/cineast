package org.vitrivr.cineast.core.color;

import org.vitrivr.cineast.core.data.FloatVector;

public class HSVContainer extends ReadableHSVContainer implements FloatVector{

	public HSVContainer(ReadableHSVContainer hsv){
		super(hsv.h, hsv.s, hsv.v);
	}
	
	public HSVContainer(float h, float s, float v) {
		super(h, s, v);
	}

	public HSVContainer(double h, double s, double v) {
		super(h, s, v);
	}

	@Override
	public void setElement(int num, float val) {
		switch(num){
		case 0:{h = val; break;}
		case 1:{s = val; break;}
		case 2:{v = val; break;}
		}

	}

}
