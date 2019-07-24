package org.vitrivr.cineast.standalone.importer;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

/**
 * 
 * Copies data from an {@link Importer} to a {@link PersistencyWriter}
 *
 */
public class Copier implements AutoCloseable {

	private final String entityName;
	private final Importer<?> importer;
	private final PersistencyWriter<?> writer;
	
	public Copier(String entityName, Importer<?> importer){
		this(entityName, importer, Config.sharedConfig().getDatabase().getWriterSupplier().get());
	}
	
	public Copier(String entityName, Importer<?> importer, PersistencyWriter<?> writer){
		this.entityName = entityName;
		this.importer = importer;
		this.writer = writer;
	}

	public void copy(){
		this.copyFrom(this.importer);
	}

	public void copyFrom(Importer<?> importer){
		Map<String, PrimitiveTypeProvider> map = importer.readNextAsMap();
		
		if(map == null){
			return;
		}
		
		Set<String> keyset = map.keySet();
		String[] names = new String[keyset.size()];
		
		int i = 0;
		for(String name : keyset){
			names[i++] = name;
		}
		
		this.writer.open(entityName);
		this.writer.setFieldNames(names);
		
		Object[] objects = new Object[names.length];
		
		do{
			for(i = 0; i < names.length; ++i){
				objects[i] = PrimitiveTypeProvider.getObject(map.get(names[i]));
			}
			persistTuple(this.writer.generateTuple(objects));
		}while((map = importer.readNextAsMap()) != null);

	}

	private void persistTuple(PersistentTuple tuple) {
		this.writer.persist(tuple);
	}

	public void copyBatched(int batchSize){
		this.copyBatchedFrom(batchSize, this.importer);
	}

	public void copyBatchedFrom(int batchSize, Importer<?> importer){
	  
	  if(batchSize <= 0){
	    copy();
	    return;
	  }
	  
	  Map<String, PrimitiveTypeProvider> map = importer.readNextAsMap();
    
    if(map == null){
      return;
    }
    
    Set<String> keyset = map.keySet();
    String[] names = new String[keyset.size()];
    
    int i = 0;
    for(String name : keyset){
      names[i++] = name;
    }
    
    this.writer.open(entityName);
    this.writer.setFieldNames(names);
    
    Object[] objects = new Object[names.length];
    
    ArrayList<PersistentTuple> tupleCache = new ArrayList<>(batchSize);
    
    do{
      for(i = 0; i < names.length; ++i){
        objects[i] = PrimitiveTypeProvider.getObject(map.get(names[i]));
      }
      PersistentTuple tuple = this.writer.generateTuple(objects);
      tupleCache.add(tuple);
      if(tupleCache.size() >= batchSize){
        this.writer.persist(tupleCache);
        tupleCache.clear();
      }
      
    }while((map = importer.readNextAsMap()) != null);
    
    this.writer.persist(tupleCache);

	}

	@Override
	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}

	@Override
	public void close(){
		this.writer.close();
	}
}
