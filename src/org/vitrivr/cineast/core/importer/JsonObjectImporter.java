package org.vitrivr.cineast.core.importer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.vitrivr.cineast.core.data.providers.primitive.BooleanTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.DoubleTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.FloatTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.FloatVectorProvider;
import org.vitrivr.cineast.core.data.providers.primitive.IntTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.LongTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.NothingProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class JsonObjectImporter implements Importer<JsonObject> {

	private JsonArray array;
	private int cursor = 0;
	
	public JsonObjectImporter(File input) throws FileNotFoundException, IOException {
		try{
			this.array = JsonValue.readFrom(new FileReader(input)).asArray();
		}catch(UnsupportedOperationException e){
			this.array = new JsonArray();
		}
	} 
	
	@Override
	public JsonObject readNext() {
		if(this.cursor >= this.array.size()){
			return null;
		}
		JsonValue val = this.array.get(this.cursor++);
		try{
			return val.asObject();
		}catch(UnsupportedOperationException e){
			return null;
		}
	}

	@Override
	public Map<String, PrimitiveTypeProvider> convert(JsonObject jobj) {
		if(jobj == null){
			return null;
		}
		
		HashMap<String, PrimitiveTypeProvider> map = new HashMap<>();
		
		for(String name : jobj.names()){
			map.put(name, convert(jobj.get(name)));
		}
		
		return map;
		
	}

	public static PrimitiveTypeProvider convert(JsonValue jval){
		if(jval == null || jval.isNull()){
			return null;
		}
		if(jval.isBoolean()){
			return new BooleanTypeProvider(jval.asBoolean());
		}
		if (jval.isNumber()) {
			try {
				return new IntTypeProvider(jval.asInt());
			} catch (NumberFormatException e) {
				// ignore
			}
			try {
				return new LongTypeProvider(jval.asLong());
			} catch (NumberFormatException e) {
				// ignore
			}
			try {
				return new FloatTypeProvider(jval.asFloat());
			} catch (NumberFormatException e) {
				// ignore
			}
			try {
				return new DoubleTypeProvider(jval.asDouble());
			} catch (NumberFormatException e) {
				// ignore
			}
			return new DoubleTypeProvider(Double.NaN);
		}
		if(jval.isString()){
			return new StringTypeProvider(jval.asString());
		}
		if(jval.isArray()){
			JsonArray jarr = jval.asArray();
			if(jarr.isEmpty()){
				return new FloatVectorProvider(new float[0]);
			}
			float[] arr = new float[jarr.size()];
			for(int i = 0; i < arr.length; ++i){
				try{
					arr[i] = jarr.get(i).asFloat();
				}catch(NumberFormatException | UnsupportedOperationException e){
					//ignore
				}
			}
			return new FloatVectorProvider(arr);
		}
		
		return new NothingProvider();
	}
	
}
