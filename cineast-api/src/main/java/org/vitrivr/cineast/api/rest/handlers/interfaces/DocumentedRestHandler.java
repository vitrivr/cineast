package org.vitrivr.cineast.api.rest.handlers.interfaces;

import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;

/**
 * Documented {@link RestHandler}, providing OpenAPI documentation about itself.<br>
 * Due to the API setup, we rely on this documentation to actually produce the OpenAPI specs.
 *
 * <h2>Usage</h2>
 * Implementing handlers have to provide documentation in the form of an {@link OpenApiDocumentation} object.
 * The following snipped shows how to produce such documentation:
 * <pre>
 *  return OpenApiBuilder.document()
 *    .operation(op -> {
 *      op.summary("The short one-liner description");
 *      op.description("A longer description of the operation / handler");
 *      op.operationId("A unique operation id");
 *      op.addTagsItem("TAG"); // to group similar handlers use the same tag
 *    })
 *    .pathParam("id", String.class, param -> param.description("The id of the object")) // the route would have to have the id prefixed with colon: route/to/this/handler/:id
 *    .json("200", ResultClass.class)
 * </pre>
 *
 *
 * @author loris.sauter
 * @version 1.0
 */
public interface DocumentedRestHandler extends RestHandler{
  
  /**
   * Provides the documentation of this handler.
   * @return The documentation of this handler
   */
  OpenApiDocumentation docs();
}
