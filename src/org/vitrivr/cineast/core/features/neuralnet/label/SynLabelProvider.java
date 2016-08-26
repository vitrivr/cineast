package org.vitrivr.cineast.core.features.neuralnet.label;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 * Provides Labels for .txt files with format n... $LABEL
 */
public class SynLabelProvider implements LabelProvider {

    private String[] labels;
    private String[] synLabels;

    public SynLabelProvider(InputStream is) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            LinkedList<String> ll = new LinkedList();
            LinkedList<String> synl = new LinkedList();
            String line = null;
            while ((line = br.readLine()) != null) {
                ll.add(line.substring(line.indexOf(" "), line.length()));
                synl.add(line.substring(0, line.indexOf(" ")));
            }
            this.labels = ll.toArray(new String[ll.size()]);
            this.synLabels = synl.toArray(new String[synl.size()]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getLabel(int index) {
        return labels[index];
    }

    public String[] getLabels() {
        return labels;
    }

    public String[] getSynLabels() {
        return synLabels;
    }
}
