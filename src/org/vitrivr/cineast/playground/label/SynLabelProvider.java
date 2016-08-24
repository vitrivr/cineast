package org.vitrivr.cineast.playground.label;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;

/**
 * Provides Labels for .txt files with format n... $LABEL
 */
public class SynLabelProvider implements LabelProvider{

    private String[] labels;

    public SynLabelProvider(File labels){
        try {
            BufferedReader br = Files.newBufferedReader(labels.toPath());
            LinkedList<String> ll = new LinkedList();
            String line = null;
            while((line=br.readLine())!=null){
                ll.add(line.substring(line.indexOf(" "), line.length()));
            }
            this.labels = ll.toArray(new String[ll.size()]);
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
}
