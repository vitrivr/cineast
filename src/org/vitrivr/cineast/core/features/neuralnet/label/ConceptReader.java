package org.vitrivr.cineast.core.features.neuralnet.label;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper-class for importing concepts. Give your concept-file as input and access the HashMap.
 * Ideally, you store the concepts in a DB because for large concept-files, this implementation will be memory-intensive.
 * <p>
 * Created by silvan on 26.08.16.
 */
public class ConceptReader {

    private HashMap<String, String[]> conceptMap = new HashMap<>();

    /**
     * @param location This should point to a File which has the format
     *                 id,ngram,annotated hyponyms seperated by whitespace
     *                 So i.e. 1, animal, n... n... n...
     */
    public ConceptReader(String location) {
        try {
            InputStream is = this.getClass().getResourceAsStream(location);
            if (is == null) {
                is = Files.newInputStream(Paths.get(location));
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            //Skip header
            br.readLine();
            /*
              Manual Blacklist for the current concepts-File.
              Some things are there because they appear twice, some are there because they do not add information
             */
            ArrayList<String> blacklist = new ArrayList<>();
            blacklist.add("adult");
            blacklist.add("object");
            blacklist.add("Tree");
            blacklist.add("Fish");
            blacklist.add("Bush");
            blacklist.add("flower with stem");
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (blacklist.contains(data[1])) {
                    continue;
                }
                String[] concepts = data[2].split(" ");

                conceptMap.put(data[1], concepts);
            }
        } catch (IOException e) {
            throw new RuntimeException("Couldn't get labels", e);
        }
    }

    /**
     * @return <human-readable label, [] of wordnet-labels>
     * Example: <animal, [wn..., wn..., wn...]
     */
    public Map<String, String[]> getConceptMap() {
        return conceptMap;
    }
}
