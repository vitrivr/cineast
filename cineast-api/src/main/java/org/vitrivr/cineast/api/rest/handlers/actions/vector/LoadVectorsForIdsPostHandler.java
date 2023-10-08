package org.vitrivr.cineast.api.rest.handlers.actions.vector;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.vitrivr.cineast.api.messages.query.VectorLookup;
import org.vitrivr.cineast.api.messages.result.IdVectorList;
import org.vitrivr.cineast.api.messages.result.IdVectorPair;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import tagbio.umap.Umap;

public class LoadVectorsForIdsPostHandler implements ParsingPostRestHandler<VectorLookup, IdVectorList> {

  private final DBSelectorSupplier selectorSupply;

  public LoadVectorsForIdsPostHandler(DBSelectorSupplier selectorSupply) {
    this.selectorSupply = selectorSupply;
  }

  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Loads the vectors of a particular feature, applies optional projection");
          op.description("Loads the vectors of a particular feature, applies optional projection");
          op.operationId("loadVectors");
          op.addTagsItem("Vectors");
        })
        .body(inClass())
        .json("200", outClass());
  }

  @Override
  public IdVectorList performPost(VectorLookup input, Context ctx) {

    DBSelector selector = this.selectorSupply.get();
    selector.open("feature_" + input.feature());
    List<Map<String, PrimitiveTypeProvider>> rows = selector.getRows("feature", input.ids().ids(), "vector_lookup");

    List<String> ids = new ArrayList<>(input.ids().ids().size());
    List<float[]> vectors = new ArrayList<>(input.ids().ids().size());

    for (Map<String, PrimitiveTypeProvider> row : rows) {
      ids.add(row.get("id").getString());
      vectors.add(row.get("feature").getFloatArray());
    }

    selector.close();

    switch (input.projection().toLowerCase()) {
      case "umap" -> {

        Umap umap = new Umap();

        umap.setMetric(
            input.properties().getOrDefault("metric", "cosine")
        );

        umap.setNumberComponents(
            Integer.parseInt(
                input.properties().getOrDefault("components", "3")
            )
        );

        umap.setNumberNearestNeighbours(
            Integer.parseInt(
                input.properties().getOrDefault("nearestNeighbours", "15")
            )
        );

        umap.setThreads(
            Integer.parseInt(
                input.properties().getOrDefault("threads", Runtime.getRuntime().availableProcessors() + "")
            )
        );

        float[][] data = new float[vectors.size()][];

        for (int i = 0; i < vectors.size(); ++i) {
          data[i] = vectors.get(i);
        }

        float[][] transformed = umap.fitTransform(data);

        List<IdVectorPair> pairs = new ArrayList<>(ids.size());

        for (int i = 0; i < ids.size(); ++i) {
          pairs.add(new IdVectorPair(ids.get(i), transformed[i]));
        }

        return new IdVectorList(pairs);

      }
      case "tsne" -> throw new IllegalStateException("tsne projection not implemented");


      case "raw" -> {

        List<IdVectorPair> pairs = new ArrayList<>(ids.size());

        for (int i = 0; i < ids.size(); ++i) {
          pairs.add(new IdVectorPair(ids.get(i), vectors.get(i)));
        }

        return new IdVectorList(pairs);

      }
    }

    throw new IllegalArgumentException("Projection " + input.projection() + " not known");
  }

  @Override
  public Class<VectorLookup> inClass() {
    return VectorLookup.class;
  }

  @Override
  public Class<IdVectorList> outClass() {
    return IdVectorList.class;
  }

  @Override
  public String route() {
    return "/find/vectors";
  }
}
