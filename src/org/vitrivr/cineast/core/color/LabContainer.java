package org.vitrivr.cineast.core.color;

import org.vitrivr.cineast.core.data.FloatVector;

public class LabContainer extends ReadableLabContainer implements FloatVector{

	public LabContainer(){
		super(0, 0, 0);
	}
	
	public LabContainer(double L, double a, double b) {
		super(L, a, b);
	}
	
	public LabContainer(double L, double a, double b, double alpha) {
		super(L, a, b, alpha);
	}
	
	public LabContainer(float L, float a, float b) {
		super(L, a, b);
	}
	
	public LabContainer(float L, float a, float b, float alpha) {
		super(L, a, b, alpha);
	}
	
	public LabContainer(ReadableLabContainer lab){
		super(lab.L, lab.a, lab.b, lab.alpha);
	}

	public void setL(float L){
		this.L = L;
	}
	
	public void setA(float a){
		this.a = a;
	}
	
	public void setB(float b){
		this.b = b;
	}
	
	public void setAlpha(float alpha){
		this.alpha = alpha;
	}
	
	@Override
	public void setElement(int num, float val) {
		switch(num){
		case 0:{L = val; break;}
		case 1:{a = val; break;}
		case 2:{b = val; break;}
		case 3:{alpha = val; break;}
		}
	}
}
