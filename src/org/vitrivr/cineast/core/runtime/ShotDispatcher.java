package org.vitrivr.cineast.core.runtime;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.LimitedQueue;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.StatElement;
import org.vitrivr.cineast.core.data.providers.SegmentProvider;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.extractor.ExtractorInitializer;
import org.vitrivr.cineast.core.util.DecodingError;
import org.vitrivr.cineast.core.util.LogHelper;

@Deprecated
public class ShotDispatcher implements Runnable, ExecutionTimeCounter {

	private static final int TASK_QUEUE_SIZE = Config.getExtractorConfig().getTaskQueueSize();
	private static final int THREAD_COUNT = Config.getExtractorConfig().getThreadPoolSize();
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private List<Extractor> extractors;
	private LinkedBlockingQueue<SegmentContainer> shotQueue = new LinkedBlockingQueue<SegmentContainer>(Config.getExtractorConfig().getShotQueueSize());
	private ExecutorService executor;
	private ShotProviderThread providerThread;
	private ExtractorInitializer initializer;
	
	private ConcurrentHashMap<Class<?>, StatElement> timeMap = new ConcurrentHashMap<>();

  private final Comparator<Extractor> extractionTimeComparator = new Comparator<Extractor>() {
    
    @Override
    public int compare(Extractor o1, Extractor o2) {
      return Long.compare(getAverageExecutionTime(o2.getClass()), getAverageExecutionTime(o1.getClass()));
    }
  };

  public ShotDispatcher(List<Extractor> extractorList, ExtractorInitializer initializer, SegmentProvider provider){
		this.extractors = extractorList;
		Collections.shuffle(this.extractors);
		LimitedQueue<Runnable> taskQueue = new LimitedQueue<>(TASK_QUEUE_SIZE);
		this.executor = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, 60, TimeUnit.SECONDS, taskQueue){

			@Override
			protected void afterExecute(Runnable r, Throwable t) {
				if(t != null){
					LOGGER.fatal("Decoding Error detected, shutting down");
					LOGGER.fatal(LogHelper.getStackTrace(t));
					this.shutdownNow();
					providerThread.interrupt();
				}
				super.afterExecute(r, null);
			}
			
		};
		
		this.providerThread = new ShotProviderThread(shotQueue, provider);
		this.initializer = initializer;
	}
	
	@Override
	public void run() {

		//init shot provider
		this.providerThread.start();
		LOGGER.debug("ShotSegmenterThread started");
		
		//init extractors
		for(Extractor e : extractors){
			this.initializer.initialize(e);
		}
		LOGGER.info("Features initialized");
		
		whileLoop:
		while(this.providerThread.isAlive() || !this.shotQueue.isEmpty()){
			try {
				SegmentContainer s = this.shotQueue.poll(1, TimeUnit.MINUTES);
				if(s != null){
					LOGGER.info("start dispatching shot " + s.getId());
					for(Extractor f : extractors){
						try{
							this.executor.execute(new ExtractionTask(f, s, this));
							LOGGER.debug("submitted shot {} for feature {}", s, f);
						}catch(RejectedExecutionException e){
							this.providerThread.interrupt();
							this.shotQueue.clear();
							break whileLoop;
						}
					}
					//re-sort extractors
					Collections.sort(this.extractors, this.extractionTimeComparator);
				}else{
					LOGGER.info("Timeout while waiting for shot to dispatch");
				}
			} catch (InterruptedException e) {
				LOGGER.warn("ShotDispatcher was interrupted: {}", LogHelper.getStackTrace(e));
			} catch (DecodingError e){
				LOGGER.fatal("Error while reading video: {}", LogHelper.getStackTrace(e));
			}
		}
		this.providerThread.interrupt();
		this.executor.shutdown();
		try {
			this.executor.awaitTermination(15, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			LOGGER.warn("ShotDispatcher was interrupted: {}", LogHelper.getStackTrace(e));
		}
		
		LOGGER.debug("Closing Extractors");
		for(Extractor e : extractors){
			e.finish();
		}
		LOGGER.info("completed extraction");
	}

	@Override
  public void reportExecutionTime(Class<?> c, long miliseconds) {
    if(!this.timeMap.containsKey(c)){
      this.timeMap.put(c, new StatElement(miliseconds));
    }else{
      StatElement stat = this.timeMap.get(c);
      synchronized (stat) {
        stat.add(miliseconds);
      }
    }
  }

  @Override
  public long getAverageExecutionTime(Class<?> c) {
    if(this.timeMap.containsKey(c)){
      return (long) this.timeMap.get(c).getAvg();
    }
    return 0;
  }

}
