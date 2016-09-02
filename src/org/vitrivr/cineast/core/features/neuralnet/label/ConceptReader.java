package org.vitrivr.cineast.core.features.neuralnet.label;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            List<String> blacklist = new ArrayList();
            blacklist.add("adult");
            blacklist.add("object");
            blacklist.add("Tree");
            blacklist.add("Fish");
            blacklist.add("Bush");
            blacklist.add("flower with stem");
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if(blacklist.contains(data[1])){
                    continue;
                }
                String[] concepts = data[2].split(" ");
                if(concepts==null){
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
