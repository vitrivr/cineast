package ch.unibas.cs.dbis.cineast.core.db;

import java.sql.PreparedStatement;
import java.util.LinkedList;

import ch.unibas.cs.dbis.cineast.core.data.FeatureString;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;

public abstract class AbstractADAMTuple extends PersistentTuple<PreparedStatement>{

	private String[] names;
	private int count;
	
	public AbstractADAMTuple(ADAMWriter phandler, String[] names, int count) {
		super(phandler);
		this.names = names;
		this.count = count;
		
	}
	
	protected String makeSQL(){
		StringBuffer buf = new StringBuffer();
		buf.append("INSERT INTO ");
		buf.append(((ADAMWriter)phandler).name);
		if(names != null && names.length > 0){
			buf.append(" (");
			for(int i = 0; i < names.length; ++i){
				buf.append(names[i]);
				if(i < names.length - 1){
					buf.append(", ");
				}
			}
			buf.append(")");
		}
		buf.append(" VALUES (");
		if(this.elements.size() >= count){
			for(int i = 0; i < this.elements.size(); ++i){
				buf.append('\'');
				Object o = this.elements.get(i);
				if(o instanceof FeatureString) {
					buf.append(((FeatureString)o).toFeatureString());
				}else{
					buf.append(escape(o.toString()));
				}
				buf.append('\'');
				if(i < this.elements.size() - 1){
					buf.append(", ");
				}
			}
		}else{
			for(int i = 0; i < count - 1; ++i){
				buf.append('\'');
				Object o = this.elements.get(i);
				if(o instanceof FeatureString) {
					buf.append(((FeatureString)o).toFeatureString());
				}else{
					buf.append(escape(o.toString()));
				}
				buf.append('\'');
				buf.append(", ");
			}
			LinkedList<Float> floats = new LinkedList<Float>();
			for(int i = count - 1; i < this.elements.size(); ++i){
				Object o = this.elements.get(i);
				if(o instanceof FloatVector){
					FloatVector fv = (FloatVector) o;
					for(int j = 0; j < fv.getElementCount(); ++j){
						floats.add(fv.getElement(j));
					}
				}
			}
			buf.append("\'<");
			for(int i = 0; i < floats.size(); ++i){
				buf.append(floats.get(i));
				if(i < floats.size() - 1){
					buf.append(", ");
				}
			}
			buf.append(">\'");
			
		}
		buf.append(")");
		return buf.toString();
	}

	@Override
	public abstract PreparedStatement getPersistentRepresentation();
	
	public static String escape(String in){
		return in.replaceAll("'", "''");//in.replaceAll("['\"]", "\\\\$0");
	}
}