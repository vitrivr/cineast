package org.vitrivr.cineast.core.util;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class ARPartioner {

	private ARPartioner(){}
	
	public static <T> ArrayList<LinkedList<T>> partition(List<T> input, int width, int height, int angularSegments, int radialSegments){
		ArrayList<LinkedList<T>> _return = new ArrayList<>(angularSegments * radialSegments);
		for(int i = 0; i < angularSegments * radialSegments; ++i){
			_return.add(new LinkedList<T>());
		}
		
		float centerX = width / 2f, centerY = height / 2f, w = width, h = height;
		
		for(int i = 0; i < input.size(); ++i){
			float   x = ((i % width) - centerX) / w,
					y = ((i / width) - centerY) / h;
			
			
			//to polar
			float r = (float) Math.sqrt(x * x + y * y);
			float phi = (float) (Math.atan2(y, x) + Math.PI);
			
			int radialSegment = r >= 0.5f ? radialSegments - 1 : (int) Math.floor(r * 2 * radialSegments);
			int angularSegment = ((int) Math.floor((phi / (2 * Math.PI)) * angularSegments)) % angularSegments;
			
			int index = radialSegment + radialSegments * angularSegment;
			
			_return.get(index).add(input.get(i));
			
		}
		
		return _return;
	}
	
	public static Pair<FloatVector, float[]> partitionImage(MultiImage img, int angularSegments, int radialSegments){
		int[] colors = img.getColors();
		ArrayList<Integer> tmpList = new ArrayList<>(colors.length);
		for(int c : colors){
			tmpList.add(c);
		}
		
		ArrayList<LinkedList<Integer>> partitions = ARPartioner.partition(tmpList, img.getWidth(), img.getHeight(), angularSegments, radialSegments);
		ArrayList<SummaryStatistics> stats = new ArrayList<SummaryStatistics>(angularSegments * radialSegments * 3);
		ArrayList<SummaryStatistics> alphas = new ArrayList<SummaryStatistics>(angularSegments * radialSegments);
		
		for(int i = 0; i < angularSegments * radialSegments * 3; ++i){
			stats.add(new SummaryStatistics());
		}
		
		for(int i = 0; i < angularSegments * radialSegments; ++i){
			alphas.add(new SummaryStatistics());
		}
		
		for(int i = 0; i < angularSegments * radialSegments; ++i){
			LinkedList<Integer> cols = partitions.get(i);
			SummaryStatistics L = stats.get(3 * i);
			SummaryStatistics a = stats.get(3 * i + 1);
			SummaryStatistics b = stats.get(3 * i + 2);
			SummaryStatistics alpha = alphas.get(i);
			
			for(int c : cols){
				ReadableLabContainer lab = ColorConverter.cachedRGBtoLab(c);
				L.addValue(lab.getL());
				a.addValue(lab.getA());
				b.addValue(lab.getB());
				alpha.addValue(ReadableRGBContainer.getAlpha(c) / 255f);
			}
		}
		
		float[] vec = new float[stats.size() * 2];
		float[] weights = new float[vec.length];
		
		for(int i = 0; i < stats.size(); ++i){
		  SummaryStatistics s = stats.get(i);
			vec[2 * i] = (float) s.getMean();
			vec[2 * i + 1] = (float) s.getVariance();
		}
		
		for(int i = 0; i < alphas.size(); ++i){
			weights[3 * i] = (float) alphas.get(i).getMean();
			weights[3 * i + 1] = weights[3 * i];
			weights[3 * i + 2] = weights[3 * i];
		}
		
		return new Pair<FloatVector, float[]>(new FloatVectorImpl(vec), weights);
	}
	
}
