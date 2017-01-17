package org.vitrivr.cineast.core.segmenter.general;

import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.decode.general.Decoder;

import java.util.concurrent.SynchronousQueue;

/**
 * A simple segmenter that passes the output from the decoder straight back to the orchestrator.
 * No aggregation or post-processing will takes place besides packaging of the content in a
 * SegmentContainer.
 *
 * @author rgasser
 * @version 1.0
 * @created 14.01.17
 */
public abstract class PassthroughSegmenter<T> implements Segmenter<T> {

    /** Decoder<T> used for file decoding. */
    private Decoder<T> decoder;

    /** A SynchronousQueue used to pass the element to the orchestrating thread. */
    private final SynchronousQueue<T> queue = new SynchronousQueue<T>();

    /** A flag indicating whether or not the decoder has completed its work. */
    private volatile boolean complete;

    /**
     * @param decoder
     */
    @Override
    public synchronized void init(Decoder<T> decoder) {
        this.decoder = decoder;
        this.complete = false;
    }

    /**
     * Returns the next SegmentContainer from the source OR null if there are no more segments in the queue.
     * Because a SynchronizedQueue is used for the handover, this method will block until the SegmementContainer
     * is made available in the run method (on another thread).
     *
     *
     * @return
     */
    public SegmentContainer getNext() throws InterruptedException {
        T content = this.queue.take();
        if (content != null) {
            synchronized (this) { this.complete = true; }
            return this.getSegmentFromContent(content);
        } else {
            return null;
        }
    }

    /**
     * Indicates whether the Segmenter is complete i.e. no new segments
     * are to be expected.
     *
     * @return true if work is complete, false otherwise.
     */
    @Override
    public synchronized boolean complete() {
        return this.complete;
    }

    /**
     * Closes the decoder.
     */
    @Override
    public void close() {
       this.decoder.close();
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        while (!this.decoder.complete()) {
            try {
                this.queue.put(this.decoder.getNext());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param content
     * @return
     */
    protected abstract SegmentContainer getSegmentFromContent(T content);
}
