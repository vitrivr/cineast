package org.vitrivr.cineast.core.features.neuralnet.label;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Provides Labels for .txt files with format n... $LABELS (formatted as .csv)
 * Example: n01518878 ostrich, Struthio camelus
 */
@Deprecated
public class SynLabelProvider implements LabelProvider {

    private List<List<String>> labels;
    private String[] synLabels;
    private Map<String, String[]> labelMappings = new HashMap<>();

    public SynLabelProvider(InputStream is) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            labels = new LinkedList<>();
            LinkedList<String> wnLabels = new LinkedList<>();

            String line;
            while ((line = br.readLine()) != null) {
                //+1 because we don't want the space
                String[] readable = line.substring(line.indexOf(" ")+1, line.length()).split(", ");
                labels.add(Arrays.asList(readable));
                String lbl = line.substring(0, line.indexOf(" "));
                wnLabels.add(lbl);
                labelMappings.put(lbl, readable);
            }
            this.synLabels = wnLabels.toArray(new String[wnLabels.size()]);
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
