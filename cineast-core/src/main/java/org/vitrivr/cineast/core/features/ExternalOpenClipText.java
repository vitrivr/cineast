package org.vitrivr.cineast.core.features;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tensorflow.Result;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.types.TFloat32;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;
import org.vitrivr.cineast.core.data.providers.primitive.FloatArrayTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.render.lwjgl.render.RenderOptions;
import org.vitrivr.cineast.core.render.lwjgl.renderer.RenderJob;
import org.vitrivr.cineast.core.render.lwjgl.renderer.RenderWorker;
import org.vitrivr.cineast.core.render.lwjgl.scene.lights.LightingOptions;
import org.vitrivr.cineast.core.render.lwjgl.window.WindowOptions;
import org.vitrivr.cineast.core.util.KMeansPP;
import org.vitrivr.cineast.core.util.math.MathHelper;
import org.vitrivr.cineast.core.util.texturemodel.Viewpoint.ViewpointHelper;
import org.vitrivr.cineast.core.util.texturemodel.Viewpoint.ViewpointStrategy;
import org.vitrivr.cineast.core.util.web.ImageParser;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

import static org.vitrivr.cineast.core.util.CineastConstants.FEATURE_COLUMN_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

public class ExternalOpenClipText extends AbstractFeatureModule {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String TABLE_NAME = "features_openclip";

    private static final int EMBEDDING_SIZE = 512;
    private static final ReadableQueryConfig.Distance DISTANCE = ReadableQueryConfig.Distance.cosine;
    private static final CorrespondenceFunction CORRESPONDENCE = CorrespondenceFunction.linear(1);

    private static final String DEFAULT_API_ENDPOINT = "http://localhost:8888";

    private static final String API_ENDPOINT_KEY = "api";

    private final String externalApi;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(Version.HTTP_1_1)
            .build();

    private final JsonMapper mapper = new JsonMapper();
    private DBSelector selector;


    public ExternalOpenClipText() {
        super(TABLE_NAME, 1f, EMBEDDING_SIZE);
        this.externalApi = DEFAULT_API_ENDPOINT;
    }

    public ExternalOpenClipText(Map<String, String> properties) {
        super(TABLE_NAME, 1f, EMBEDDING_SIZE);
        this.externalApi = properties.getOrDefault(API_ENDPOINT_KEY, DEFAULT_API_ENDPOINT);
    }

    @Override
    public void processSegment(SegmentContainer sc) {
        // Return if already processed
        if (phandler.idExists(sc.getId())) {
            return;
        }

        // Case: segment contains video frames
        if (!sc.getVideoFrames().isEmpty() && sc.getVideoFrames().get(0) != VideoFrame.EMPTY_VIDEO_FRAME) {
            var frame = sc.getMostRepresentativeFrame().getImage().getBufferedImage();

            float[] embeddingArray = new float[0];
            embeddingArray = apiRequest(frame);
            LOGGER.debug("ML Clip MostRepresentativeFrame from external API url: {}", externalApi);
            this.persist(sc.getId(), new FloatVectorImpl(embeddingArray));

            return;
        }

        // Case: segment contains image
        if (sc.getMostRepresentativeFrame() != VideoFrame.EMPTY_VIDEO_FRAME) {
            var image = sc.getMostRepresentativeFrame().getImage().getBufferedImage();

            if (image != null) {
                float[] embeddingArray = new float[0];
                embeddingArray = apiRequest(image);
                LOGGER.debug("ML Clip image from external API url: {}", externalApi);
                this.persist(sc.getId(), new FloatVectorImpl(embeddingArray));
            }
            return;
        }

        // Case: segment contains model
        var model = sc.getModel();
        if (model != null) {
            float[] embeddingArray = embedModel(model);
            this.persist(sc.getId(), new FloatVectorImpl(embeddingArray));
            System.gc();
            return;
        }
    }


    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().createFeatureEntity(TABLE_NAME, true, EMBEDDING_SIZE);
    }

    @Override
    public void dropPersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().dropEntity(TABLE_NAME);
    }

    @Override
    public void init(DBSelectorSupplier selectorSupply) {
        this.selector = selectorSupply.get();
        this.selector.open(TABLE_NAME);
    }

    private float[] apiRequest(BufferedImage image) {
        // Image encode to base64
        var imageData = ImageParser.bufferedImageToDataURL(image, "png");

        var builder = new StringBuilder()
                .append(URLEncoder.encode("image", StandardCharsets.UTF_8))
                .append("=")
                .append(URLEncoder.encode(imageData, StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(externalApi + "/image"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(builder.toString()))
                .build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("received response code " + response.statusCode());
            }
            return mapper.readValue(response.body(), float[].class);
        } catch (IOException e) {
            LOGGER.error("Error during CLIPImage execution. Check if the external API is running", e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            LOGGER.error("Error during CLIPImage execution. Check if the external API is running", e);
            throw new RuntimeException(e);
        }

    }

    private float[] apiRequest(String query) throws IOException, InterruptedException {

        String builder = URLEncoder.encode("query", StandardCharsets.UTF_8)
                + "="
                + URLEncoder.encode(query, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(builder))
                .uri(URI.create(externalApi))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("received response code " + response.statusCode());
        }

        return mapper.readValue(response.body(), float[].class);
    }

    @Override
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
        String text = sc.getText();
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        try {
            float[] arr = apiRequest(text);
            return getSimilar(new FloatArrayTypeProvider(arr), qc);
        } catch (Exception e) {
            LOGGER.error("error during CLIPText execution", e);
            return new ArrayList<>();
        }
    }

    public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
        List<PrimitiveTypeProvider> list = this.selector.getFeatures(GENERIC_ID_COLUMN_QUALIFIER, new StringTypeProvider(segmentId), FEATURE_COLUMN_QUALIFIER, qc);
        if (list.isEmpty()) {
            LOGGER.warn("No feature vector for shotId {} found, returning empty result-list", segmentId);
            return Collections.emptyList();
        }
        return getSimilar(list.get(0), qc);
    }

    public List<ScoreElement> getSimilar(PrimitiveTypeProvider queryProvider, ReadableQueryConfig qc) {
        ReadableQueryConfig qcc = QueryConfig.clone(qc).setDistance(DISTANCE);
        List<SegmentDistanceElement> distances = this.selector.getNearestNeighboursGeneric(qc.getResultsPerModule(), queryProvider, FEATURE_COLUMN_QUALIFIER, SegmentDistanceElement.class, qcc);
        CorrespondenceFunction function = qcc.getCorrespondenceFunction().orElse(CORRESPONDENCE);
        return DistanceElement.toScore(distances, function);
    }


    @Override
    public void finish() {
        if (this.selector != null) {
            this.selector.close();
            this.selector = null;
        }
    }


    /**
     * This method takes a list of images and determines, based on {@link ViewpointStrategy}, the embed vector which describes the images most precise. This method can be simplified, once the best strategy is determined.
     *
     * @param images            the list of Images to embed
     * @param viewpointStrategy the strategy to find the vector
     * @return the embedding vector
     */
    private float[] embedMostRepresentativeImages(List<BufferedImage> images, ViewpointStrategy viewpointStrategy) {

        var retVal = new float[EMBEDDING_SIZE];

        switch (viewpointStrategy) {
            // Strategy for embedding multiple images. Choose mean of the most contained cluster. Project mean to unit hypersphere.
            case MULTI_IMAGE_KMEANS -> {
                var floatvectors = new ArrayList<FloatVectorImpl>();
                var vectors = embedMultipleImages(images);
                vectors.forEach(v -> floatvectors.add(new FloatVectorImpl(v)));
                var kmeans = KMeansPP.bestOfkMeansPP(floatvectors, new FloatVectorImpl(new float[EMBEDDING_SIZE]), 3, -1f, 5);
                // Find the index of thr cluster with the most elements
                int maxIndex = 0;
                for (var ic = 0; ic < kmeans.getPoints().size(); ++ic) {
                    if (kmeans.getPoints().get(ic).size() > kmeans.getPoints().get(maxIndex).size()) {
                        maxIndex = ic;
                    }
                    if (kmeans.getPoints().get(ic).size() == kmeans.getPoints().get(maxIndex).size()) {
                        if (kmeans.getDistance(ic) < kmeans.getDistance(maxIndex)) {
                            maxIndex = ic;
                        }
                    }
                }
                ReadableFloatVector.toArray(kmeans.getCenters().get(maxIndex), retVal);
                return MathHelper.normalizeL2InPlace(retVal);
            }
            // Strategy for embedding multiple images. Calculate mean over all. Project mean to unit hypersphere.
            case MULTI_IMAGE_PROJECTEDMEAN -> {
                var vectors = embedMultipleImages(images);
                var vectorsMean = new float[EMBEDDING_SIZE];
                for (var vector : vectors) {
                    for (var ic = 0; ic < vector.length; ++ic) {
                        vectorsMean[ic] += vector[ic] / vectors.size();
                    }
                }
                return MathHelper.normalizeL2InPlace(vectorsMean);
            }
            // Strategy for embedding an image consisting out of four sub images.
            case MULTI_IMAGE_2_2 -> {
                assert images.size() == 4;
                var sz = images.get(0).getWidth();
                var size = sz * 2;
                // Combine the images into a single image.
                var canvas = new BufferedImage(
                        size,
                        size,
                        BufferedImage.TYPE_INT_RGB);
                var graphics = canvas.getGraphics();
                graphics.setColor(Color.BLACK);
                // ic: image counter, idx: x-axis-index, idy: yaxis-index
                var ic = 0;
                for (var partialImage : images) {
                    int idx = ic % 2;
                    int idy = ic < 2 ? 0 : 1;
                    graphics.drawImage(partialImage, idx * sz, idy * sz, null);
                    ++ic;
                }
                retVal = apiRequest(canvas);
            }
        }
        return retVal;
    }


    /**
     * Embeds a list of images
     *
     * @param images the list of images to embed
     * @return the list of embedding vectors
     */
    private List<float[]> embedMultipleImages(List<BufferedImage> images) {
        var vectors = new ArrayList<float[]>();
        for (BufferedImage image : images) {
            float[] embeddingArray = apiRequest(image);
            vectors.add(embeddingArray);
        }
        return vectors;
    }

    /**
     * This method embeds a 3D model and returns the feature vector.
     */
    private float[] embedModel(IModel model) {
        //Options for window
        var windowOptions = new WindowOptions() {{
            this.hideWindow = true;
            this.width = 600;
            this.height = 600;
        }};
        // Options for renderer
        var renderOptions = new RenderOptions() {{
            this.showTextures = true;
            //this.lightingOptions.hasNonDefaultTexture = model.usesNonDefaultTexture();
            this.lightingOptions = LightingOptions.STATIC;
        }};
        // Select the strategy which will be used for model embedding
        var viewpointStrategy = ViewpointStrategy.MULTI_IMAGE_KMEANS;
        // Get camera viewpoint for chosen strategy
        var cameraPositions = ViewpointHelper.getCameraPositions(viewpointStrategy, model);
        // Render an image for each camera position
        var images = RenderJob.performStandardRenderJob(RenderWorker.getRenderJobQueue(),
                model, cameraPositions, windowOptions, renderOptions);

        // Embedding based on strategy return value. Empty if an error occurred
        if (images.isEmpty()) {
            return new float[EMBEDDING_SIZE];
        }
        if (images.size() == 1) {
            return apiRequest(images.get(0));
        }
        return embedMostRepresentativeImages(images, viewpointStrategy);
    }
}
