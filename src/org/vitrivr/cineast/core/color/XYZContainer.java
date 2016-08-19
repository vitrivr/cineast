package org.vitrivr.cineast.core.color;

import org.vitrivr.cineast.core.data.FloatVector;

public class XYZContainer extends ReadableXYZContainer implements FloatVector{
	
	public XYZContainer(float x, float y, float z){
		super(x, y, z);
	}
	
	public XYZContainer(double x, double y, double z){
		super(x, y, z);
	}
	
	XYZContainer() {
		this(0f, 0f, 0f);
	}


	@Override
	public void setElement(int num, float val) {
		switch(num){
		case 0:{x = val; break;}
		case 1:{y = val; break;}
		case 2:{z = val; break;}
		}
	}

	
}
