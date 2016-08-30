package org.vitrivr.cineast.core.features.neuralnet.label;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper for importing concepts
 * Redundant Storage for retrieval-performance
 * <p>
 * Created by silvan on 26.08.16.
 */
public class ConceptReader {

    /**
     * Maps a human-readable concept onto wordnet-labels
     */
    Map<String, String[]> conceptMap = new HashMap();

    public ConceptReader() {
        //TODO Read from DB
        throw new UnsupportedOperationException();
    }

    public ConceptReader(String location) {
        try {
            InputStream is = this.getClass().getResourceAsStream(location);
            if (is == null) {
                is = Files.newInputStream(Paths.get(location));
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            //Skip header
            br.readLine();
            while ((line = br.readLine()) != null) {

                //TODO Maybe exclude the following concepts:
                //bird, machine, adult, vehicle, animal, plant, food, fish, flower, tool, tree, object, dog, bush, Tree (big/small???), Fish (big/small???), flower with stem...,
                //vessel, Bush(b/s???), vegetable, fruit, person, grass, furniture, standing bird, cloth, building
                String[] data = line.split(",");
                String[] concepts = data[2].split(" ");
                if(concepts==null){
                    System.out.println(line+" Concepts null");
                    concepts = new String[0];
                }
                conceptMap.put(data[1],concepts);
            }
        } catch (IOException e) {
            throw new RuntimeException("Couldn't get labels", e);
        }
    }

    public Map<String, String[]> getConceptMap() {
        return conceptMap;
    }
}
