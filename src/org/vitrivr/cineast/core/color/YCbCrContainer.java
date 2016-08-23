package org.vitrivr.cineast.core.color;

import org.vitrivr.cineast.core.data.FloatVector;

public class YCbCrContainer extends ReadableYCbCrContainer implements FloatVector{

	public YCbCrContainer(ReadableYCbCrContainer ycbcr){
		super(ycbcr.y, ycbcr.cb, ycbcr.cr);
	}
	
	public YCbCrContainer(int Y, int Cb, int Cr){
		super(Y, Cb, Cr);
	}
	
	
	@Override
	public void setElement(int num, float val) {
		switch(num){
		case 0:{y = Math.round(val * 255f); break;}
		case 1:{cb = Math.round(val * 255f); break;}
		case 2:{cr = Math.round(val * 255f); break;}
		}
	}


}
