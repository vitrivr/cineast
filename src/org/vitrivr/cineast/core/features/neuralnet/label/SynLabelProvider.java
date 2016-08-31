package org.vitrivr.cineast.core.features.neuralnet.label;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Provides Labels for .txt files with format n... $LABEL
 */
public class SynLabelProvider implements LabelProvider {

    private List<List<String>> labels;
    private String[] synLabels;
    private Map<String, String[]> labelMappings = new HashMap<String, String[]>();

    public SynLabelProvider(InputStream is) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            labels = new LinkedList();
            LinkedList<String> synl = new LinkedList();

            String line = null;
            while ((line = br.readLine()) != null) {
                String[] readable = line.substring(line.indexOf(" "), line.length()).split(", ");
                labels.add(Arrays.asList(readable));
                String lbl = line.substring(0, line.indexOf(" "));
                synl.add(lbl);
                labelMappings.put(lbl, readable);
            }
            this.synLabels = synl.toArray(new String[synl.size()]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<List<String>> getAllLabels() {
        return labels;
    }

    @Override
    public String[] getSynSetLabels() {
        return synLabels;
    }

    @Override
    public String[] getLabels(String i) {
        return labelMappings.get(i);
    }
}
