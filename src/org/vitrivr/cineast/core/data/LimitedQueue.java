package org.vitrivr.cineast.core.data;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LimitedQueue<E> extends LinkedBlockingQueue<E> implements BlockingQueue<E> 
{
    /**
	 * http://stackoverflow.com/questions/4521983/java-executorservice-that-blocks-on-submission-after-a-certain-queue-size
	 */
	private static final long serialVersionUID = -107554551692604207L;

	public LimitedQueue(int maxSize)
    {
        super(maxSize);
    }

	/**
	 * blocking
	 */
    @Override
    public boolean offer(E e)
    {
        // turn offer() and add() into a blocking calls (unless interrupted)
        try {
            put(e);
            return true;
        } catch(InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
    
    

}