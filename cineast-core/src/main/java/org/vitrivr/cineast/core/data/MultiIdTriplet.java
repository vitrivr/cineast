package org.vitrivr.cineast.core.data;

public class MultiIdTriplet{
	
	public long firstId, secondId;
	public double score;
	
	public MultiIdTriplet(long id1, long id2, double score) {
		this.firstId = id1;
		this.secondId = id2;
		this.score = score;
	}

	@Override
	public String toString() {
		return "<" + firstId + ", " + secondId + ", " + score + ">";
	}
	
	
}
