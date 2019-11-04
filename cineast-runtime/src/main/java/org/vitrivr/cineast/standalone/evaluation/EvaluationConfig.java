package org.vitrivr.cineast.standalone.evaluation;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.extraction.decode.general.Converter;
import org.vitrivr.cineast.core.util.ReflectionHelper;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author rgasser
 * @version 1.0
 * @created 05.05.17
 */
public class EvaluationConfig {

    /** Evaluation mode determines how files should be selected;
     * - COMPLETE: All files in the test files folder.
     * - RANDOM: Random selection of files from the test files folder.
     */
    public enum EvaluationMode {
        COMPLETE,RANDOM
    }

    /** Path to the folder containing the test files. */
    private String testfiles;

    /** Path to the file hat describes the ground truth. */
    private String classfile;

    /** Path to the folder where results should be written. */
    private String results;

    /** The fully qualified name of the converter class used to convert test files to QueryContainers. */
    private String converter;

    /** Delimiter used between columns in the text export. */
    private String delimiter = ",";

    /** The query categories that should be tested. */
    private String[] categories = new String[0];

    /** The evaluation mode that should be used. */
    private EvaluationMode mode = EvaluationMode.COMPLETE;

    /** The size of the test set (i.e. the number of elements in the database). */
    private int size = 0;

    @JsonProperty
    public Path getTestfiles() {
        return Paths.get(testfiles);
    }
    public void setTestfiles(String testfiles) {
        this.testfiles = testfiles;
    }

    @JsonProperty
    public Path getClassfile() {
        return Paths.get(this.classfile);
    }
    public void setClassfile(String classfile) {
        this.classfile = classfile;
    }
    public Groundtruth getGroundtruth() throws EvaluationException {
        return new Groundtruth(this.getClassfile());
    }

    @JsonProperty
    public Path getResults() {
        return Paths.get(this.results);
    }
    public void setResults(String results) {
        this.results = results;
    }

    @JsonProperty
    public Converter getConverter() {
        return ReflectionHelper.newConverter(this.converter);
    }
    public void setConverter(String converter) {
        this.converter = converter;
    }

    @JsonProperty
    public String[] getCategories() {
        return categories;
    }
    public void setCategories(String[] categories) {
        this.categories = categories;
    }

    @JsonProperty
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }

    @JsonProperty
    public String getDelimiter() {
        return delimiter;
    }
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    @JsonProperty
    public EvaluationMode getMode() {
        return mode;
    }
    public void setMode(EvaluationMode mode) {
        this.mode = mode;
    }
}
