package test;

import ch.unibas.cs.dbis.cineast.core.db.ADAMproWriter;
import ch.unibas.cs.dbis.cineast.core.setup.EntityCreator;

public class Test {

	private static final String entityName = "inserttest2";
	
	public static void main(String[] args) {
		
		EntityCreator creator = new EntityCreator();
		creator.createFeatureEntity(entityName, true);
		
		
		ADAMproWriter writer = new ADAMproWriter();
		writer.open(entityName);
		
		for(int i = 0; i < 10; ++i){
			
			float[] vector = new float[10];
			for(int j = 0; j < vector.length; ++j){
				vector[j] = (float) Math.random();
			}
			
			boolean success = writer.persist(writer.generateTuple(i, vector));
			System.out.println(i + " : " + success);
			
		}
		
		
		
//		FeatureExtractionRunner runner = new FeatureExtractionRunner(new File("D:/ownCloud/Shared/iMotion Project/Data/Open Short Video Collection/collection"));
//		runner.extractFolder(new File("D:/ownCloud/Shared/iMotion Project/Data/Open Short Video Collection/collection/Copying Is Not Theft"));

	}

}
