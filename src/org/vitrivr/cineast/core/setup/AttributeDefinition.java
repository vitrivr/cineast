package org.vitrivr.cineast.core.setup;

public class AttributeDefinition{
	final String name;
	final AttributeType type;
	
	public enum AttributeType {
	  UNKOWNAT,
	  AUTO,
	  LONG,
	  INT,
	  FLOAT,
	  DOUBLE,
	  STRING,
	  TEXT,
	  BOOLEAN,
	  VECTOR,
	  GEOMETRY,
	  GEOGRAPHY
	}
	
	public AttributeDefinition(String name, AttributeType type){
		this.name = name;
		this.type = type;
	}
}