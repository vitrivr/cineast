package ch.unibas.cs.dbis.cineast.core.db;

import ch.unibas.cs.dbis.cineast.core.data.FeatureString;

public class CSVTuple extends PersistentTuple<String> {

	protected CSVTuple(PersistencyWriter<?> phandler) {
		super(phandler);
	}

	@Override
	public String getPersistentRepresentation() {
		StringBuilder sb = new StringBuilder();
		for(Object o : this.elements){
			if(o instanceof FeatureString){
				sb.append('\'');
				sb.append((((FeatureString)o).toFeatureString()));
				sb.append('\'');
			}else if(o instanceof String){
				String s = (String) o;
				sb.append('"');
				sb.append(s.replaceAll("\"", "\\\""));
				sb.append('"');
			}else{
				sb.append(o);
			}
			sb.append("; ");
		}
		return sb.toString();
	}

}
