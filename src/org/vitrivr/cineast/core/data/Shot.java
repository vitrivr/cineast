package org.vitrivr.cineast.core.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.decode.subtitle.SubtitleItem;
import org.vitrivr.cineast.core.descriptor.AvgImg;
import org.vitrivr.cineast.core.descriptor.MedianImg;
import org.vitrivr.cineast.core.descriptor.MostRepresentative;
import org.vitrivr.cineast.core.descriptor.PathList;

import boofcv.struct.geo.AssociatedPair;
import georegression.struct.point.Point2D_F32;

public class Shot implements SegmentContainer{

	private static final Logger LOGGER = LogManager.getLogger();
	
	private LinkedList<Frame> frames = new LinkedList<>();
	private LinkedList<SubtitleItem> subItems = new LinkedList<>();
	private MultiImage avgImg = null, medianImg = null;
	private Frame mostRepresentative = null;
	private List<Pair<Integer, LinkedList<Point2D_F32>>> paths = null;
	private List<Pair<Integer, LinkedList<Point2D_F32>>> bgPaths = null;
	private LinkedList<Pair<Integer,ArrayList<AssociatedPair>>> allPaths = null;
	private ArrayList<String> tags = new ArrayList<>(1);
	private String movieId;
	private String shotId;

	private final int movieFrameCount;

	/**
	 *
	 * @param movieFrameCount
	 */
	public Shot(int movieFrameCount) {
		this.movieFrameCount = movieFrameCount;
	}

	public Shot(String movieId, int movieFrameCount){
		this.movieId = movieId;
		this.movieFrameCount = movieFrameCount;
	}
	
	public int getNumberOfFrames(){
		return this.frames.size();
	}


	public List<Frame> getFrames() {
		return this.frames;
	}

	public void addFrame(Frame f){
		this.frames.add(f);
	}
	
	@Override
	public String toString() {
		return "Shot id: " + this.shotId;
	}

	public void addSubtitleItem(SubtitleItem si){
		this.subItems.add(si);
	}
	
	private Object getAvgLock = new Object();
	public MultiImage getAvgImg(){
		synchronized (getAvgLock) {
			if(avgImg == null){
				avgImg = AvgImg.getAvg(frames);			
			}
			return avgImg;
		}
	}
	
	private Object getMedianLock = new Object();
	public MultiImage getMedianImg(){
		synchronized (getMedianLock) {
			if(this.medianImg == null){
				this.medianImg = MedianImg.getMedian(frames);
		}
		return this.medianImg;
		}
	}
	
	private Object getPathsLock = new Object();
	public List<Pair<Integer, LinkedList<Point2D_F32>>> getPaths() {
		synchronized (getPathsLock) {
			if(this.paths == null){
				this.allPaths = PathList.getDensePaths(frames);
				this.paths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();
				this.bgPaths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();
				PathList.separateFgBgPaths(frames, this.allPaths, this.paths, this.bgPaths);
			}
		}
		return this.paths;
	}
	
	public List<Pair<Integer, LinkedList<Point2D_F32>>> getBgPaths() {
		synchronized (getPathsLock) {
			if(this.bgPaths == null){
				this.allPaths = PathList.getDensePaths(frames);
				this.paths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();
				this.bgPaths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();
				PathList.separateFgBgPaths(frames, this.allPaths, this.paths, this.bgPaths);
			}
		}
		return this.bgPaths;
	}

	public void clear(){
		LOGGER.trace("clear shot {}", shotId);
		for(Frame f : frames){
			f.clear();
		}
		frames.clear();
		subItems.clear();
		this.frames = null;
		if(avgImg != null){
			this.avgImg.clear();
			this.avgImg = null;
		}
		if(medianImg != null){
			this.medianImg.clear();
			this.medianImg = null;
		}
		if(this.paths != null){
			this.paths.clear();
			this.paths = null;
		}
		
		this.mostRepresentative = null;
	}

	private Object getMostRepresentativeLock = new Object();
	public Frame getMostRepresentativeFrame(){
		synchronized (getMostRepresentativeLock) {
			if(this.mostRepresentative == null){
				this.mostRepresentative = MostRepresentative.getMostRepresentative(this);
			}
			return this.mostRepresentative;
		}
	}

	public int getStart(){
		if (this.frames.size() > 0) {
			return this.frames.get(0).getId();
		} else {
			return 0;
		}
	}
	
	public int getEnd(){
		if (this.frames.size() > 0) {
			return this.frames.get(this.frames.size()-1).getId();
		} else {
			return 0;
		}
	}
	
	public String getId(){
		return this.shotId;
	}
	
	public String getSuperId(){
		return this.movieId;
	}

	/**
	 * @param id
	 * @return a unique id of this
	 */
	@Override
	public void setId(String id) {
		this.shotId = id;
	}

	/**
	 * @param id
	 */
	@Override
	public void setSuperId(String id) {
		this.movieId = id;
	}

	@Override
	protected void finalize() throws Throwable {
		clear();
		super.finalize();
	}

	@Override
	public List<SubtitleItem> getSubtitleItems() {
		return this.subItems;
	}

	@Override
	public float getRelativeStart() {
		return (getStart() / (float)this.movieFrameCount);
	}

	@Override
	public float getRelativeEnd() {
		return (getEnd() / (float)this.movieFrameCount);
	}
	
	@Override
	public List<String> getTags() {
		return this.tags;
	}
	
}
