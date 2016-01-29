package ch.unibas.cs.dbis.cineast.core.db;

import java.util.LinkedList;
import java.util.List;

public abstract class PersistentTuple<R> {

	protected LinkedList<Object> elements;
	protected PersistencyWriter<?> phandler;
	
	protected PersistentTuple(PersistencyWriter<?> phandler){
		this.elements = new LinkedList<>();
		this.phandler = phandler;
	}
	
	public void addElement(Object element){
		this.elements.add(element);
	}
	
	public List<Object> getElemets(){
		return this.elements;
	}
	
	public abstract R getPersistentRepresentation();
}
