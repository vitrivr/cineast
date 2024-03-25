package org.vitrivr.cineast.core.data.m3d.texturemodel;


import static org.lwjgl.assimp.Assimp.AI_MATKEY_COLOR_AMBIENT;
import static org.lwjgl.assimp.Assimp.AI_MATKEY_COLOR_DIFFUSE;
import static org.lwjgl.assimp.Assimp.AI_MATKEY_SHININESS;
import static org.lwjgl.assimp.Assimp.AI_MATKEY_SHININESS_STRENGTH;
import static org.lwjgl.assimp.Assimp.aiGetMaterialColor;
import static org.lwjgl.assimp.Assimp.aiGetMaterialFloatArray;
import static org.lwjgl.assimp.Assimp.aiGetMaterialTexture;
import static org.lwjgl.assimp.Assimp.aiImportFile;
import static org.lwjgl.assimp.Assimp.aiProcess_CalcTangentSpace;
import static org.lwjgl.assimp.Assimp.aiProcess_FixInfacingNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_GenNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_GenSmoothNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_GlobalScale;
import static org.lwjgl.assimp.Assimp.aiProcess_JoinIdenticalVertices;
import static org.lwjgl.assimp.Assimp.aiProcess_LimitBoneWeights;
import static org.lwjgl.assimp.Assimp.aiProcess_PreTransformVertices;
import static org.lwjgl.assimp.Assimp.aiProcess_Triangulate;
import static org.lwjgl.assimp.Assimp.aiReleaseImport;
import static org.lwjgl.assimp.Assimp.aiReturn_SUCCESS;
import static org.lwjgl.assimp.Assimp.aiTextureType_DIFFUSE;
import static org.lwjgl.assimp.Assimp.aiTextureType_NONE;
import static org.lwjgl.assimp.Assimp.aiTextureType_NORMALS;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector4f;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;
import org.vitrivr.cineast.core.data.m3d.texturemodel.util.TextureLoadException;
import org.vitrivr.cineast.core.data.m3d.texturemodel.util.TimeLimitedFunc;

public final class ModelLoader {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Loads a model from a file. Generates all the standard flags for Assimp. For more details see <a
     * href="https://javadoc.lwjgl.org/org/lwjgl/assimp/Assimp.html">Assimp</a>.
     * <ul>
     *   <li><b>aiProcess_GenSmoothNormals:</b>
     *        This is ignored if normals are already there at the time this flag
     *       is evaluated. Model importers try to load them from the source file, so
     *       they're usually already there.
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
     *          <li>Specify both #aiProcess_Triangulate and #aiProcess_SortByPType </li>
     *          </li>Ignore all point and line meshes when you process assimp's output</li>
     *        </ul>
     *   </li>
     *   <li><b>aiProcess_FixInf acingNormals:</b>
     *        This step tries to determine which meshes have normal vectors that are facing inwards and inverts them.
     *        The algorithm is simple but effective: the bounding box of all vertices + their normals is compared against
     *        the volume of the bounding box of all vertices without their normals. This works well for most objects, problems might occur with
     *        planar surfaces. However, the step tries to filter such cases.
     *        The step inverts all in-facing normals. Generally it is recommended to enable this step, although the result is not always correct.
     *   </li>
     *   <li><b>aiProcess_CalcTangentSpace:</b>
     *        Calculates the tangents and bi tangents for the imported meshes
     *        Does nothing if a mesh does not have normals.
     *        You might want this post processing step to be executed if you plan to use tangent space calculations such as normal mapping applied to the meshes.
     *        There's an importer property, AI_CONFIG_PP_CT_MAX_SMOOTHING_ANGLE, which allows you to specify a maximum smoothing angle for the algorithm.
     *        However, usually you'll want to leave it at the default value.
     *   </li>
     *   <li><b>aiProcess_LimitBoneWeights:</b>
     *    Limits the number of bones simultaneously affecting a single vertex to a maximum value.
     *    If any vertex is affected by more than the maximum number of bones,
     *    the least important vertex weights are removed and the remaining vertex weights are normalized so that the weights still sum up to 1.
     *    The default bone weight limit is 4 (defined as AI_LBW_MAX_WEIGHTS in config.h),
     *    but you can use the AI_CONFIG_PP_LBW_MAX_WEIGHTS importer property to supply your own limit to the post processing step.
     *    If you intend to perform the skinning in hardware, this post processing step might be of interest to you.
     *   </li>
     *   <li><b>aiProcess_PreTransformVertices:</b>
     *    Removes the node graph and pre-transforms all vertices with the local transformation matrices of their nodes.
     *    If the resulting scene can be reduced to a single mesh, with a single material, no lights, and no cameras,
     *    then the output scene will contain only a root node (with no children) that references the single mesh.
     *    Otherwise, the output scene will be reduced to a root node with a single level of child nodes, each one referencing one mesh,
     *    and each mesh referencing one material
     *    In either case, for rendering, you can simply render all meshes in order - you don't need to pay attention to local transformations and the node hierarchy.
     *    Animations are removed during this step
     *    This step is intended for applications without a scenegraph.
     *    The step CAN cause some problems: if e.g. a mesh of the asset contains normals and another, using the same material index,
     *    does not, they will be brought together, but the first mesh's part of the normal list is zeroed. However, these artifacts are rare.
     *   </li>
     * </ul>
     *
     * @param modelId   The ID of the model.
     * @param modelPath Path to the model file.
     * @return Model object.
     */
    public static Model loadModel(String modelId, String modelPath) throws TextureLoadException, TimeoutException {
        var model = loadModel(modelId, modelPath,
                aiProcess_JoinIdenticalVertices |
                        aiProcess_GlobalScale |
                        aiProcess_FixInfacingNormals |
                        aiProcess_Triangulate |
                        aiProcess_CalcTangentSpace |
                        aiProcess_LimitBoneWeights |
                        aiProcess_PreTransformVertices |
                        aiProcess_GenSmoothNormals
        );
        LOGGER.trace("Try return Model 2");
        return model;
    }

    /**
     * Loads a model from a file. 1. Loads the model file to an aiScene. 2. Process all Materials. 3. Process all Meshes.
     * 3.1 Process all Vertices. 3.2 Process all Normals. 3.3 Process all Textures. 3.4 Process all Indices.
     *
     * @param modelId   Arbitrary unique ID of the model.
     * @param modelPath Path to the model file.
     * @param flags     Flags for the model loading process.
     * @return Model object.
     */
    @SuppressWarnings("NullAway")
    public static Model loadModel(String modelId, String modelPath, int flags) throws TextureLoadException, TimeoutException {
        LOGGER.trace("Try loading file {} from {}", modelId, modelPath);

        var file = new File(modelPath);
        if (!file.exists()) {
            throw new RuntimeException("Model path does not exist [" + modelPath + "]");
        }
        var modelDir = file.getParent();

        LOGGER.trace("Loading aiScene");


        // DO NOT USE AUTOCLOSEABLE TRY CATCH FOR AI-SCENE!!! THIS WILL CAUSE A FATAL ERROR ON NTH (199) ITERATION!
        // RAPHAEL WALTENSPUEL 2023-01-20
        var tlf = new TimeLimitedFunc<>(120, () -> aiImportFile(modelPath, flags));
        var aiScene = tlf.<AIScene>runWithTimeout();
        if (aiScene == null) {
            throw new TextureLoadException("Error loading model [modelPath: " + modelPath + "]");
        }





        var numMaterials = aiScene.mNumMaterials();
        List<Material> materialList = new ArrayList<>();
        for (var ic = 0; ic < numMaterials; ic++) {
            //TODO: Warning
            var aiMaterial = AIMaterial.create(aiScene.mMaterials().get(ic));
            LOGGER.trace("Try processing material {}", ic);
            materialList.add(ModelLoader.processMaterial(aiMaterial, modelDir));
        }

        var numMeshes = aiScene.mNumMeshes();
        var aiMeshes = aiScene.mMeshes();
        var defaultMaterial = new Material();
        for (var ic = 0; ic < numMeshes; ic++) {
            LOGGER.trace("Try create AI Mesh {}", ic);
            //TODO: Warning
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

    /**
     * Convert indices from aiMesh to int array.
     *
     * @param aiMesh aiMesh to process.
     * @return flattened int array of indices.
     */
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

    /**
     * Convert an AIMaterial to a Material. Loads the diffuse color and texture.
     *
     * @param aiMaterial aiMaterial to process.
     * @param modelDir   Path to the model file.
     * @return flattened float array of vertices.
     */
    private static Material processMaterial(AIMaterial aiMaterial, String modelDir) {
        LOGGER.trace("Start processing material");
        var material = new Material();
        try (var stack = MemoryStack.stackPush()) {
            var color = AIColor4D.create();

            var result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0, color);
            if (result == aiReturn_SUCCESS) {
                material.setAmbientColor(new Vector4f(color.r(), color.g(), color.b(), color.a()));
            }
            result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, color);
            if (result == aiReturn_SUCCESS) {
                material.setDiffuseColor(new Vector4f(color.r(), color.g(), color.b(), color.a()));
            }
            result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, color);
            if (result == aiReturn_SUCCESS) {
                material.setSpecularColor(new Vector4f(color.r(), color.g(), color.b(), color.a()));
            }

            var reflectance = 0.0f;
            var shininess = new float[]{0.0f};
            var pMax = new int[]{1};
            result = aiGetMaterialFloatArray(aiMaterial, AI_MATKEY_SHININESS_STRENGTH, aiTextureType_NONE, 0, shininess,
                    pMax);
            if (result == aiReturn_SUCCESS) {
                reflectance = shininess[0];
            }
            material.setReflectance(reflectance);

            //** Try load texture
            var aiTexturePath = AIString.calloc(stack);
            aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, aiTexturePath, (IntBuffer) null,
                    null, null, null, null, null);
            var texturePath = aiTexturePath.dataString();
            //TODO: Warning
            if (texturePath != null && texturePath.length() > 0) {
                material.setTexture(new Texture(modelDir + File.separator + new File(texturePath).toPath()));
                material.setDiffuseColor(Material.DEFAULT_COLOR);
            }

            // Try Load NormalMap
            var aiNormalMapPath = AIString.calloc(stack);
            aiGetMaterialTexture(aiMaterial, aiTextureType_NORMALS, 0, aiNormalMapPath, (IntBuffer) null,
                    null, null, null, null, null);
            var normalMapPath = aiNormalMapPath.dataString();
            if (normalMapPath != null && normalMapPath.length() > 0) {
                material.setNormalTexture(new Texture(modelDir + File.separator + new File(normalMapPath).toPath()));
            }
            return material;
        }
    }

    /**
     * Convert aiMesh to a Mesh. Loads the vertices, normals, texture coordinates and indices. Instantiates a new Mesh
     * object.
     *
     * @param aiMesh aiMesh to process.
     * @return flattened float array of normals.
     */
    private static Mesh processMesh(AIMesh aiMesh) {
        LOGGER.trace("Start processing mesh");
        var vertices = processVertices(aiMesh);
        var normals = processNormals(aiMesh);
        var textCoords = processTextCoords(aiMesh);
        var indices = processIndices(aiMesh);
        var tangents = processTangents(aiMesh, normals);
        var bitangents = processBitangents(aiMesh, normals);


        // Texture coordinates may not have been populated. We need at least the empty slots
        if (textCoords.length == 0) {
            var numElements = (vertices.length / 3) * 2;
            textCoords = new float[numElements];
        }
        LOGGER.trace("End processing mesh");
        return new Mesh(vertices, normals, tangents, bitangents, textCoords, indices);
    }

    /**
     * Convert normals from aiMesh to float array.
     *
     * @param aiMesh aiMesh to process.
     * @return flattened float array of normals.
     */
    private static float[] processNormals(AIMesh aiMesh) {
        LOGGER.trace("Start processing Normals");
        var buffer = aiMesh.mNormals();
        if (buffer == null) {
            return new float[]{};
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

    /**
     * Convert texture coordinates from aiMesh to float array.
     *
     * @param aiMesh aiMesh to process.
     * @return flattened float array of texture coordinates.
     */
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

    /**
     * Convert vertices from aiMesh to float array.
     *
     * @param aiMesh aiMesh to process.
     * @return flattened float array of vertices.
     */
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

    private static float[] processBitangents(AIMesh aiMesh, float[] normals) {
        var buffer = aiMesh.mBitangents();
        if (buffer == null) {
            return new float[normals.length];
        }
        var data = new float[buffer.remaining() * 3];
        var pos = 0;
        while (buffer.remaining() > 0) {
            var bitangent = buffer.get();
            data[pos++] = bitangent.x();
            data[pos++] = bitangent.y();
            data[pos++] = bitangent.z();
        }

        if (data.length == 0) {
            data = new float[normals.length];
        }
        return data;
    }

    private static float[] processTangents(AIMesh aiMesh, float[] normals) {
        var buffer = aiMesh.mTangents();
        if (buffer == null) {
            return new float[normals.length];
        }
        var data = new float[buffer.remaining() * 3];
        var pos = 0;
        while (buffer.remaining() > 0) {
            var tangent = buffer.get();
            data[pos++] = tangent.x();
            data[pos++] = tangent.y();
            data[pos++] = tangent.z();
        }
        if (data.length == 0) {
            data = new float[normals.length];
        }
        return data;
    }
}