package org.vitrivr.cineast.playground.label;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
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
     * Maps a concept onto labels
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
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                String[] concepts = new String[data.length-2];
                for(int i = 2; i<data.length; i++){
                    concepts[i] = data[i];
                }
                conceptMap.put(data[1],Arrays.copyOfRange(data,2,data.length));
            }
        } catch (IOException e) {
            throw new RuntimeException("Couldn't get labels", e);
        }
    }

    public Map<String, String[]> getConceptMap() {
        return conceptMap;
    }
}
