package org.vitrivr.cineast.core.db.dao.reader;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.dao.MetadataAccessSpecification;
import org.vitrivr.cineast.core.db.dao.MetadataType;
import org.vitrivr.cineast.core.util.TimeHelper;

/**
 * Abstraction layer for segment and object metadata retrieval.
 *
 * @param <R> result type
 */
public abstract class AbstractMetadataReader<R> extends AbstractEntityReader {

    private static final Logger LOGGER = LogManager.getLogger();
    private final String tableName;
    private final String idColName;

    public AbstractMetadataReader(DBSelector selector, String tableName, String idColName) {
        super(selector);
        this.tableName = tableName;
        this.idColName = idColName;
        this.selector.open(tableName);
    }

    abstract R resultToDescriptor(Map<String, PrimitiveTypeProvider> result) throws DatabaseLookupException;

    public List<R> lookupMultimediaMetadata(String id) {
        return this.lookupMultimediaMetadata(Lists.newArrayList(id));
    }

    public List<R> lookupMultimediaMetadata(List<String> ids) {
        StopWatch watch = StopWatch.createStarted();
        ids = sanitizeIds(ids);
        LOGGER.trace("Loading metadata for {} elements", ids.size());
        final List<Map<String, PrimitiveTypeProvider>> results = this.selector.getRows(idColName, ids);
        if (results.isEmpty()) {
            LOGGER.debug("Could not find any metadata for provided IDs, Excerpt: {}. ID count: {}", String.join(", ", ids.subList(0, Math.min(5, ids.size()))), ids.size());
        }
        List<R> list = mapToResultList(results);
        watch.stop();
        LOGGER.debug("Performed metadata lookup for {} ids in {} ms. {} results.", ids.size(), watch.getTime(TimeUnit.MILLISECONDS), list.size());
        return list;
    }

    public List<R> findBySpec(List<String> ids, List<MetadataAccessSpecification> spec) {
        if (ids == null || spec == null) {
            LOGGER.warn("provided id-list {} or spec {} is null, returning empty list", ids, spec);
            return new ArrayList<>();
        }
        StopWatch watch = StopWatch.createStarted();
        ids = sanitizeIds(ids);
        spec = sanitizeSpec(spec);
        List<Map<String, PrimitiveTypeProvider>> results = selector.getMetadataByIdAndSpec(ids, spec, idColName);
        LOGGER.debug("Performed metadata lookup for {} ids in {} ms. {} results.", ids.size(), watch.getTime(TimeUnit.MILLISECONDS), results.size());
        return mapToResultList(results);
    }

    public List<R> findBySpec(List<MetadataAccessSpecification> spec) {
        StopWatch watch = StopWatch.createStarted();
        spec = sanitizeSpec(spec);
        List<Map<String, PrimitiveTypeProvider>> results = selector.getMetadataBySpec(spec);
        LOGGER.debug("Performed metadata lookup in {} ms. {} results.", watch.getTime(TimeUnit.MILLISECONDS), results.size());
        return mapToResultList(results);
    }

    public List<R> findBySpec(String id, MetadataAccessSpecification spec) {
        return this.findBySpec(id, Lists.newArrayList(spec));
    }

    public List<R> findBySpec(String id, List<MetadataAccessSpecification> spec) {
        if (id == null || id.isEmpty()) {
            LOGGER.warn("Provided id is null or empty, returning empty list");
            return new ArrayList<>();
        }
        return this.findBySpec(Lists.newArrayList(id), spec);
    }

    public List<R> findBySpec(List<String> ids, MetadataAccessSpecification spec) {
        return this.findBySpec(ids, Lists.newArrayList(spec));
    }

    public List<R> findBySpec(MetadataAccessSpecification... spec) {
        return this.findBySpec(Lists.newArrayList(spec));
    }

    public List<R> findBySpec(MetadataAccessSpecification spec) {
        return this.findBySpec(Lists.newArrayList(spec));
    }

    public List<MetadataAccessSpecification> sanitizeSpec(List<MetadataAccessSpecification> spec) {
        // filter null objects
        if (spec.stream().anyMatch(Objects::isNull)) {
            LOGGER.warn("provided spec-list contains null elements which will be ignored");
            spec = spec.stream().filter(Objects::nonNull).collect(Collectors.toList());
        }
        // filter non-object specs if this is an object reader
        if (Objects.equals(this.tableName, MediaObjectMetadataDescriptor.ENTITY) && spec.stream().anyMatch(el -> el.type != MetadataType.OBJECT)) {
            LOGGER.trace("provided spec-list includes non-object tuples, but this is an object reader. These will be ignored.");
            spec = spec.stream().filter(el -> el.type == MetadataType.OBJECT).collect(Collectors.toList());
        }
        // filter non-segment specs if this is a segment reader
        if (Objects.equals(this.tableName, MediaSegmentMetadataDescriptor.ENTITY) && spec.stream().anyMatch(el -> el.type != MetadataType.SEGMENT)) {
            LOGGER.trace("provided spec-list includes non-segment tuples, but this is a segment reader. These will be ignored.");
            spec = spec.stream().filter(el -> el.type == MetadataType.SEGMENT).collect(Collectors.toList());
        }
        return spec;
    }

    public List<R> mapToResultList(List<Map<String, PrimitiveTypeProvider>> results) {
        final ArrayList<R> list = new ArrayList<>(results.size());
        results.forEach(r -> {
            try {
                list.add(resultToDescriptor(r));
            } catch (DatabaseLookupException exception) {
                LOGGER.fatal("Could not map data. This is a programmer's error!");
            }
        });
        return list;
    }

    public static List<String> sanitizeIds(List<String> ids) {
        if (ids.stream().anyMatch(el -> el == null || el.isEmpty())) {
            LOGGER.warn("provided id-list contains null or empty elements which will be ignored");
            ids = ids.stream().filter(el -> el != null && !el.isEmpty()).collect(Collectors.toList());
        }
        return ids;
    }

}
