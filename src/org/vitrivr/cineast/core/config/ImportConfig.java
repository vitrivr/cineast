package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;

import java.nio.file.Path;
import java.util.ArrayList;

/**
 * @author rgasser
 * @version 1.0
 * @created 13.01.17
 */
public class ImportConfig implements ExtractionContextProvider {
    /**
     *
     */
    public class InputConfig {
        private Path path;
        private String name;
        private String id;

        @JsonProperty
        public Path getPath() {
            return path;
        }
        public void setPath(Path path) {
            this.path = path;
        }

        @JsonProperty
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        @JsonProperty
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
    }

    /** */
    private MediaType type;

    /** */
    private InputConfig input;

    /** */
    private ArrayList<String> categories;

    /** */
    private ArrayList<String> exporters;

    /** */
    private DatabaseConfig database;

    @JsonCreator
    public ImportConfig() {

    }

    @JsonProperty
    public MediaType getType() {
        return type;
    }
    public void setType(MediaType type) {
        this.type = type;
    }

    @JsonProperty
    public InputConfig getInput() {
        return input;
    }
    public void setInput(InputConfig input) {
        this.input = input;
    }

    @JsonProperty
    public ArrayList<String> getCategories() {
        return categories;
    }
    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    @JsonProperty
    public ArrayList<String > getExporters() {
        return exporters;
    }
    public void setExporters(ArrayList<String> exporters) {
        this.exporters = exporters;
    }

    @JsonProperty
    public DatabaseConfig getDatabase() {
        return database;
    }
    public void setDatabase(DatabaseConfig database) {
        this.database = database;
    }

    @Override
    public Path inputPath() {
        if (this.input != null) {
            return this.input.getPath();
        } else {
            return null;
        }
    }

    /**
     *
     */
    @Override
    public MediaType sourceType() {
        return this.type;
    }
}