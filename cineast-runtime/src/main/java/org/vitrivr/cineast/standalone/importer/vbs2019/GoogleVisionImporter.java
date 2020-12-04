package org.vitrivr.cineast.standalone.importer.vbs2019;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.tag.CompleteTag;
import org.vitrivr.cineast.core.data.tag.Tag;
import org.vitrivr.cineast.core.features.TagsFtSearch;
import org.vitrivr.cineast.core.importer.Importer;
import org.vitrivr.cineast.standalone.importer.vbs2019.gvision.GoogleVisionCategory;
import org.vitrivr.cineast.standalone.importer.vbs2019.gvision.GoogleVisionTuple;

public class GoogleVisionImporter implements Importer<GoogleVisionTuple> {

    private JsonParser parser;
    private ObjectMapper mapper;
    private final String fileName;
    private final Path input;
    private Iterator<Entry<String, JsonNode>> _segments;
    private Iterator<Entry<String, JsonNode>> _categories;
    private static final Logger LOGGER = LogManager.getLogger();
    private String _completeID;
    private final GoogleVisionCategory targetCategory;
    private Iterator<JsonNode> _categoryValues;
    private GoogleVisionCategory _category;
    private final boolean importTagsFt;
    private boolean initialized = false;

    /**
     * @param importTagsFt whether tags should be imported into {@link TagsFtSearch} or {@link GoogleVisionCategory#tableName}
     * @param targetCategory only tuples of this kind are imported
     */
    public GoogleVisionImporter(Path input, GoogleVisionCategory targetCategory, boolean importTagsFt) throws IOException {
        this.input = input;
        this.fileName = input.getFileName().toString();
        this.importTagsFt = importTagsFt;
        this.targetCategory = targetCategory;
    }

    /**
     * Generate a {@link GoogleVisionTuple} from the current state
     */
    private Optional<GoogleVisionTuple> generateTuple() {
        JsonNode next = _categoryValues.next();
        try {
            return Optional.of(GoogleVisionTuple.of(targetCategory, next, _completeID));
        } catch (UnsupportedOperationException e) {
            LOGGER.trace("Cannot generate tuple for category {} and tuple {}", targetCategory, next);
            return Optional.empty();
        }
    }

    /**
     * Search for the next valid {@link GoogleVisionTuple} in the current segment
     */
    private Optional<GoogleVisionTuple> searchWithinSegment() {
        //First, check if there's still a value left in the current array
        if (_category == targetCategory && _categoryValues.hasNext()) {
            return generateTuple();
        }

        //if not, check if we can get values for the target category in the given segment
        while (_categories != null && _category != targetCategory && _categories.hasNext()) {
            Entry<String, JsonNode> nextCategory = _categories.next();
            _category = GoogleVisionCategory.valueOf(nextCategory.getKey().toUpperCase());
            _categoryValues = nextCategory.getValue().iterator();
        }

        //If we succeeded in the previous loop, we should be at the target category and still have a value left to hand out.
        if (_category == targetCategory && _categoryValues.hasNext()) {
            return generateTuple();
        }
        //else we have nothing in this category
        return Optional.empty();
    }

    private Optional<GoogleVisionTuple> searchWithinMovie() {
        do {
            Optional<GoogleVisionTuple> tuple = searchWithinSegment();
            if (tuple.isPresent()) {
                return tuple;
            }
            //we need to move on to the next segment
            if (!_segments.hasNext()) {
                return Optional.empty();
            }
            Entry<String, JsonNode> nextSegment = _segments.next();
            _completeID = nextSegment.getKey();
            _categories = nextSegment.getValue().fields();
            //Initialize category values
            Entry<String, JsonNode> nextCategory = _categories.next();
            _category = GoogleVisionCategory.valueOf(nextCategory.getKey().toUpperCase());
            _categoryValues = nextCategory.getValue().iterator();
        } while (_segments.hasNext());
        return Optional.empty();
    }

    /**
     * Gets the next {@link GoogleVisionTuple} to Import.
     */
    private synchronized Optional<GoogleVisionTuple> nextTuple() {
        //Check first if there are tuples left within the current movie / aggregation of segments
        Optional<GoogleVisionTuple> tuple = searchWithinMovie();
        if (tuple.isPresent()) {
            return tuple;
        }
//if the current movie has no tuples left to import, we need to go to the next movie until we find a tuple
        do {
            //we need to go to the next movie
            try {
                if (parser.nextToken() == JsonToken.START_OBJECT) {
                    ObjectNode nextMovie = mapper.readTree(parser);
                    if (nextMovie == null) {
                        LOGGER.info("File for category {} is done", targetCategory);
                        return Optional.empty();
                    }
                    _segments = nextMovie.fields();
                    tuple = searchWithinMovie();
                    if (tuple.isPresent()) {
                        return tuple;
                    }
                } else {
                    LOGGER.info("File {} done", this.fileName);
                    return Optional.empty();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } while (true);
    }

    private synchronized void init() throws IOException {
        if (initialized) {
            LOGGER.warn("Importer for path {} was already initalized", input);
            return;
        }
        initialized = true;
        LOGGER.info("Starting Importer for path {}, category {} and importTags {}", input, targetCategory, importTagsFt);
        mapper = new ObjectMapper();
        parser = mapper.getFactory().createParser(input.toFile());
        if (parser.nextToken() == JsonToken.START_ARRAY) {
            if (parser.nextToken() == JsonToken.START_OBJECT) {
                ObjectNode node = mapper.readTree(parser);
                _segments = node.fields();
            }
            if (_segments == null) {
                throw new IOException("Empty file");
            }
        } else {
            throw new IOException("Empty file");
        }
    }

    /**
     * @return Pair mapping a segmentID to a List of Descriptions
     */
    @Override
    public GoogleVisionTuple readNext() {
        try {
            if (!initialized) {
                init();
            }
            Optional<GoogleVisionTuple> node = nextTuple();
            if (!node.isPresent()) {
                return null;
            }
            return node.get();
        } catch (NoSuchElementException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts the given {@link GoogleVisionTuple} to a representation appropriate to the given feature.
     *
     * @param data the tuple to be converted to a tuple in the feature table
     * @return a map where the key corresponds to the column-name and the value to the value to be inserted in that column for the given tuple
     */
    @Override
    public Map<String, PrimitiveTypeProvider> convert(GoogleVisionTuple data) {
        final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
        Optional<Tag> tag = Optional.empty();
        PrimitiveTypeProvider id = PrimitiveTypeProvider.fromObject(data.completeID);
        switch (data.category) {
            case PARTIALLY_MATCHING_IMAGES:
                throw new UnsupportedOperationException();
            case WEB:
                if (importTagsFt) {
                    map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[0], id);
                    map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[1], PrimitiveTypeProvider.fromObject(data.web.get().description));
                } else {
                    map.put("id", id);
                    map.put("tagid", PrimitiveTypeProvider.fromObject(data.web.get().labelId));
                    map.put("score", PrimitiveTypeProvider.fromObject(Math.min(1, data.web.get().score)));
                    try {
                        tag = Optional.of(new CompleteTag(data.web.get().labelId, data.web.get().description, data.web.get().description));
                    } catch (IllegalArgumentException e) {
                        LOGGER.trace("Error while initalizing tag {}", e.getMessage());
                    }
                }
                break;
            case PAGES_MATCHING_IMAGES:
                throw new UnsupportedOperationException();
            case FULLY_MATCHING_IMAGES:
                throw new UnsupportedOperationException();
            case LABELS:
                if (importTagsFt) {
                    map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[0], id);
                    map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[1], PrimitiveTypeProvider.fromObject(data.label.get().description));
                } else {
                    map.put("id", id);
                    map.put("tagid", PrimitiveTypeProvider.fromObject(data.label.get().labelId));
                    map.put("score", PrimitiveTypeProvider.fromObject(Math.min(1, data.label.get().score)));
                    tag = Optional.of(new CompleteTag(data.label.get().labelId, data.label.get().description, data.label.get().description));
                }
                break;
            case OCR:
                map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[0], id);
                map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[1], PrimitiveTypeProvider.fromObject(data.ocr.get().description));
                // Score is ignored because it's never used in search and 0 anyways
                //map.put("score", PrimitiveTypeProvider.fromObject(data.ocr.get().score));
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return map;
    }
}
