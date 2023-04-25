package org.vitrivr.cineast.core.util.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Model;
import org.vitrivr.cineast.core.util.LogHelper;


public class ModelParser extends DataURLParser {

  /**
   * Some format specific constants.
   */
  private static final String MIME_TYPE = "application/3d-json";
  private static final String VERTICES_PROPERTY_NAME_THREEV4 = "vertices";
  private static final String NORMAL_PROPERTY_NAME_THREEV4 = "normals";
  private static final String ARRAY_PROPERTY_NAME_THREEV4 = "array";

  /**
   * Parses a Base64 encoded data url and treats it as Geometry JSON used by the Three.js JavaScript library. Tries to parse the structure into a 3D mesh.
   *
   * @param dataUrl Data URL that should be parsed.
   * @return Mesh, if parsing fails that Mesh will be empty!
   */
  public static Model parseThreeJSV4Geometry(String dataUrl) {
    /* Convert Base64 string into byte array. */
    byte[] bytes = dataURLtoByteArray(dataUrl, MIME_TYPE);

    ObjectMapper mapper = new ObjectMapper();
    try {
      /* Read the JSON structure of the transmitted mesh data. */
      JsonNode node = mapper.readTree(bytes);
      JsonNode vertices = node.get(VERTICES_PROPERTY_NAME_THREEV4);
      if (vertices == null || !vertices.isArray() || vertices.size() == 0) {
        LOGGER.error("Submitted mesh does not contain any vertices. Aborting...");
        return Model.EMPTY;
      }

      /* Create new Mesh. */
      Model model = Model.EMPTY;

      // TODO: LWJGL Model for Web. The todo is to crate a model from the serialized api call.
      //  This is needed if example based queries for e.g. similarity search is needed.
      //  The functionality has to be implemented first into vitrivr NG and/or vitrivr VR.

      return model;
    } catch (IOException e) {
      LOGGER.error("Could not create 3d mesh from Base64 input because the file-format is not supported. {}", LogHelper.getStackTrace(e));
      return Model.EMPTY;
    }
  }

  /**
   * Checks, if provided data URL is a valid Three v4 JSON geometry. Returns true if so and false otherwise. No structural analysis is performed! Only the raw, data URL is being checked.
   *
   * @param dataUrl Data URL that should be checked.
   * @return True, if data URL is a valid Three v4 JSON geometry.
   */
  public static boolean isValidThreeJSV4Geometry(String dataUrl) {
    return isValidDataUrl(dataUrl, MIME_TYPE);
  }
}
