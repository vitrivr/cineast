package org.vitrivr.cineast.core.run;

import java.nio.file.Path;

/**
 * Listener to be used to monitor the progress of an extraction
 */
public interface ExtractionCompleteListener {

    /**
     * This method is called after the decoder for the object has been closed.
     * There might still be scheduled or ongoing {@link org.vitrivr.cineast.core.runtime.ExtractionTask}s for this object.
     */
    void onCompleted(ExtractionItemContainer path);

}
