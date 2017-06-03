package org.vitrivr.cineast.core.db;

import java.util.LinkedList;
import java.util.List;

public class PersistentTuple {

	protected LinkedList<Object> elements = new LinkedList<>();
	
	protected PersistentTuple(Object...objects){
		if(objects != null){
			for(Object obj : objects){
				addElement(obj);
			}
		}
	}
	
	public void addElement(Object o){
		this.elements.add(o);
	}
	
	public List<Object> getElements(){
		return this.elements;
	}
	
}
