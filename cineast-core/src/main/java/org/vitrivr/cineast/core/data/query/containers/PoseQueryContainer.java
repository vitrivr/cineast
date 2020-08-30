package org.vitrivr.cineast.core.data.query.containers;

import static java.util.stream.Collectors.toList;
import static org.vitrivr.cineast.core.util.StreamUtil.streamOfIterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.Floats;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.IntFunction;
import org.vitrivr.cineast.core.util.web.DataURLParser;

public class PoseQueryContainer extends QueryContainer {
	final private String poseSpec;
	final private float[][] pose;
	final private boolean[] orientations;

	/**
	 * Constructs a {@link PoseQueryContainer} from base 64 encoded query json.
	 *
	 */
	public PoseQueryContainer(String data) {
		Optional<JsonNode> maybeQuery = DataURLParser.dataURLtoJsonNode(data);
		if (!maybeQuery.isPresent()) {
			throw new IllegalArgumentException("Couldn't get pose data from query");
		}
		JsonNode query = maybeQuery.get();
		this.poseSpec = query.get("mode").asText();
		this.pose = streamOfIterator(query.get("pose").get("keypoints").elements()).map(xyc ->
			Floats.toArray(streamOfIterator(xyc.elements()).map(JsonNode::asDouble).collect(toList()))
		).toArray(float[][]::new);
		JsonNode orientations = query.get("orientations");
		this.orientations = new boolean[] {orientations.get(0).asBoolean(), orientations.get(1).asBoolean()};
	}

	@Override
	public float[][][] getPose() {
		return new float[][][]{pose};
	}

	public Optional<String> getPoseModel() {
		return Optional.of(this.poseSpec);
	}

	public boolean[] getOrientations() {
		return this.orientations;
	}
}
