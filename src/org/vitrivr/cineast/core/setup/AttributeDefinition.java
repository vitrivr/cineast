package org.vitrivr.cineast.core.setup;

import org.vitrivr.adampro.grpc.AdamGrpc.AttributeType;

public class AttributeDefinition{
	final String name;
	final AttributeType type; //FIXME should not directly reference ADAMpro classes in order to enable exchange
	
	
	public AttributeDefinition(String name, AttributeType type){
		this.name = name;
		this.type = type;
	}
}