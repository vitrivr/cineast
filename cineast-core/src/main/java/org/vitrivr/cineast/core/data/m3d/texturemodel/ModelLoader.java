package org.vitrivr.cineast.core.data.m3d.texturemodel;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.assimp.Assimp.*;

public final class ModelLoader {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Loads a model from a file. Generates all the standard flags for Assimp.
   * <p>
   * <ul>
   *   <li><b>aiProcess_GenSmoothNormals:</b>
   *        This is ignored if normals are already there at the time this flag
   *       is evaluated. Model importers try to load them from the source file, so
   *       they're usually already there.
   *
   *       This flag may not be specified together with
   *       #aiProcess_GenNormals. There's a configuration option,
   *       <tt>#AI_CONFIG_PP_GSN_MAX_SMOOTHING_ANGLE</tt> which allows you to specify
   *       an angle maximum for the normal smoothing algorithm. Normals exceeding
   *       this limit are not smoothed, resulting in a 'hard' seam between two faces.
   *       Using a decent angle here (e.g. 80 degrees) results in very good visual
   *       appearance.
   *       </li>
   *   <li><b>aiProcess_JoinIdenticalVertices:</b></li>
   *   <li><b>aiProcess_Triangulate</b> By default the imported mesh data might contain faces with more than 3
   *        indices. For rendering you'll usually want all faces to be triangles.
   *        This post processing step splits up faces with more than 3 indices into
   *        triangles. Line and point primitives are *not* modified! If you want
   *        'triangles only' with no other kinds of primitives, try the following
   *        solution:
   *        <ul>
   *        <li>Specify both #aiProcess_Triangulate and #aiProcess_SortByPType </li>
   *        </li>Ignore all point and line meshes when you process assimp's output</li>
   *        </ul>
   *        </li>
   *   <li><b>aiProcess_FixInfacingNormals:</b></li>
   *   <li><b>aiProcess_CalcTangentSpace:</b></li>
   *   <li><b>aiProcess_LimitBoneWeights_</b></li>
   *   <li><b>aiProcess_PreTransformVertices:</b></li>
   * </ul>
   *
   * @param modelId   The ID of the model.
   * @param modelPath Path to the model file.
   * @return Model object.
   */
  public static Model loadModel(String modelId, String modelPath) {
    var model = loadModel(modelId, modelPath,
        aiProcess_JoinIdenticalVertices |
            aiProcess_Triangulate |
            aiProcess_CalcTangentSpace |
            aiProcess_LimitBoneWeights |
            aiProcess_PreTransformVertices);
    LOGGER.trace("Try return Model 2");
    return model;
  }

  public static Model loadModel(String modelId, String modelPath, int flags) {
    LOGGER.trace("Try loading file {} from {}", modelId, modelPath);
    var file = new File(modelPath);
    if (!file.exists()) {
      throw new RuntimeException("Model path does not exist [" + modelPath + "]");
    }
    var modelDir = file.getParent();

    LOGGER.trace("Loading aiScene");

    // DO NOT USE AUTOCLOSEABLE TRY CATCH FOR AISCENE!!! THIS WILL CAUSE A FATAL ERROR ON NTH (199) ITERATION!
    // RAPHAEL WALTENSPUEL 2023-01-20
    var aiScene = aiImportFile(modelPath, flags);
    if (aiScene == null) {
      throw new RuntimeException("Error loading model [modelPath: " + modelPath + "]");
    }

    var numMaterials = aiScene.mNumMaterials();
    List<Material> materialList = new ArrayList<>();
    for (var ic = 0; ic < numMaterials; ic++) {
      var aiMaterial = AIMaterial.create(aiScene.mMaterials().get(ic));
      LOGGER.trace("Try processing material {}", ic);
      materialList.add(ModelLoader.processMaterial(aiMaterial, modelDir));
    }

    var numMeshes = aiScene.mNumMeshes();
    var aiMeshes = aiScene.mMeshes();
    var defaultMaterial = new Material();
    for (var ic = 0; ic < numMeshes; ic++) {
      LOGGER.trace("Try create AI Mesh {}", ic);
      var aiMesh = AIMesh.create(aiMeshes.get(ic));
      var mesh = ModelLoader.processMesh(aiMesh);
      LOGGER.trace("Try get Material idx");
      var materialIdx = aiMesh.mMaterialIndex();
      Material material;
      if (materialIdx >= 0 && materialIdx < materialList.size()) {
        material = materialList.get(materialIdx);
      } else {
        material = defaultMaterial;
      }
      LOGGER.trace("Try add Material to Mesh");
      material.addMesh(mesh);
    }

    if (!defaultMaterial.getMeshes().isEmpty()) {
      LOGGER.trace("Try add default Material");
      materialList.add(defaultMaterial);
    }

    LOGGER.trace("Try instantiate Model");
    aiReleaseImport(aiScene);

    var model = new Model(modelId, materialList);
    LOGGER.trace("Try return Model");
    return model;
  }

  private static int[] processIndices(AIMesh aiMesh) {
    LOGGER.trace("Start processing indices");
    List<Integer> indices = new ArrayList<>();
    var numFaces = aiMesh.mNumFaces();
    var aiFaces = aiMesh.mFaces();
    for (var ic = 0; ic < numFaces; ic++) {
      AIFace aiFace = aiFaces.get(ic);
      IntBuffer buffer = aiFace.mIndices();
      while (buffer.remaining() > 0) {
        indices.add(buffer.get());
      }
    }
    LOGGER.trace("End processing indices");
    return indices.stream().mapToInt(Integer::intValue).toArray();
  }

  private static Material processMaterial(AIMaterial aiMaterial, String modelDir) {
    LOGGER.trace("Start processing material");
    var material = new Material();
    try (MemoryStack stack = MemoryStack.stackPush()) {
      AIColor4D color = AIColor4D.create();

      int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0,
          color);
      if (result == aiReturn_SUCCESS) {
        material.setDiffuseColor(new Vector4f(color.r(), color.g(), color.b(), color.a()));
      }

      AIString aiTexturePath = AIString.calloc(stack);
      aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, aiTexturePath, (IntBuffer) null,
          null, null, null, null, null);
      String texturePath = aiTexturePath.dataString();
      if (texturePath != null && texturePath.length() > 0) {
        material.setTexture(new Texture(modelDir + File.separator + new File(texturePath).toPath()));
        material.setDiffuseColor(Material.DEFAULT_COLOR);
      }

      return material;
    }
  }

  private static Mesh processMesh(AIMesh aiMesh) {
    LOGGER.trace("Start processing mesh");
    var vertices = processVertices(aiMesh);
    var normals = processNormals(aiMesh);
    var textCoords = processTextCoords(aiMesh);
    var indices = processIndices(aiMesh);

    // Texture coordinates may not have been populated. We need at least the empty slots
    if (textCoords.length == 0) {
      var numElements = (vertices.length / 3) * 2;
      textCoords = new float[numElements];
    }
    LOGGER.trace("End processing mesh");
    return new Mesh(vertices, normals, textCoords, indices);
  }

  private static float[] processNormals(AIMesh aiMesh) {
    LOGGER.trace("Start processing Normals");
    var buffer = aiMesh.mNormals();
    if (buffer == null) {
      return null;
    }
    var data = new float[buffer.remaining() * 3];
    var pos = 0;
    while (buffer.remaining() > 0) {
      var normal = buffer.get();
      data[pos++] = normal.x();
      data[pos++] = normal.y();
      data[pos++] = normal.z();
    }
    return data;
  }


  private static float[] processTextCoords(AIMesh aiMesh) {
    LOGGER.trace("Start processing Coordinates");
    var buffer = aiMesh.mTextureCoords(0);
    if (buffer == null) {
      return new float[]{};
    }
    float[] data = new float[buffer.remaining() * 2];
    int pos = 0;
    while (buffer.remaining() > 0) {
      AIVector3D textCoord = buffer.get();
      data[pos++] = textCoord.x();
      data[pos++] = 1 - textCoord.y();
    }
    return data;
  }

  private static float[] processVertices(AIMesh aiMesh) {
    LOGGER.trace("Start processing Vertices");
    AIVector3D.Buffer buffer = aiMesh.mVertices();
    float[] data = new float[buffer.remaining() * 3];
    int pos = 0;
    while (buffer.remaining() > 0) {
      AIVector3D textCoord = buffer.get();
      data[pos++] = textCoord.x();
      data[pos++] = textCoord.y();
      data[pos++] = textCoord.z();
    }

    return data;
  }
}