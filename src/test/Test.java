package test;

import ch.unibas.cs.dbis.cineast.core.db.ADAMproWriter;
import ch.unibas.cs.dbis.cineast.core.db.PersistentTuple;
import ch.unibas.cs.dbis.cineast.core.setup.EntityCreator;
import ch.unibas.dmi.dbis.adam.http.Adam.InsertMessage.TupleInsertMessage;


public class Test {

	private static final String entityName = "inserttest1";
	
	public static void main(String[] args) {
		
		ADAMproWriter pwriter = new ADAMproWriter();
		pwriter.setFieldNames("id", "multimediaobject", "segmentstart", "segmentend");
		pwriter.open("segment");
		
		
		System.out.println(pwriter.idExists("v_aqua_1"));
		
		EntityCreator creator = new EntityCreator();
		creator.createFeatureEntity(entityName, true);
		
//		EntityNameMessage message = EntityNameMessage.newBuilder().setEntity("segment").build();
//		AckMessage ack = ADAMproWrapper.getInstance().dropEntityBlocking(message);
		
//		System.out.println(ack.getCode());
		
		ADAMproWriter writer = new ADAMproWriter();
		writer.open(entityName);
		
		for(int i = 0; i < 10; ++i){
			
			float[] vector = new float[10];
			for(int j = 0; j < vector.length; ++j){
				vector[j] = (float) Math.random();
			}
			
			PersistentTuple<TupleInsertMessage> t = writer.generateTuple(Integer.toString(i), vector);
			
			System.out.println(t.getPersistentRepresentation());
			
			boolean success = writer.persist(t);
			System.out.println(i + " : " + success);
			
		}
		
	}

}
