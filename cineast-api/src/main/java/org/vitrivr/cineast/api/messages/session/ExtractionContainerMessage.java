package org.vitrivr.cineast.api.messages.session;

import java.util.List;
import org.vitrivr.cineast.standalone.run.ExtractionItemContainer;

/**
 * This object represents a container for multiple extract item requests and contains {@link ExtractionItemContainer} as a body of the message.
 *
 * @param items List of {@link ExtractionItemContainer} items that are part of this extraction container message.
 */
public record ExtractionContainerMessage(List<ExtractionItemContainer> items) {

}
