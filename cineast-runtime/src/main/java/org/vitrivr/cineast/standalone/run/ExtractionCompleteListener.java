package org.vitrivr.cineast.standalone.run;

/**
 * Listener to be used to monitor the progress of an extraction
 */
public interface ExtractionCompleteListener {

    /**
     * This method is called after the decoder for the object has been closed.
     * There might still be scheduled or ongoing {@link org.vitrivr.cineast.standalone.runtime.ExtractionTask}s for this object.
     */
    default void onCompleted(ExtractionItemContainer path){
        //ignore
    }

    /**
     * This method is called when the extraction is completely finished. All resources are relinquished and no tasks are ongoing anymore.
     */
    default void extractionComplete(){
        //ignore
    }
}
