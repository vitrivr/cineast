package org.vitrivr.cineast.core.util;

import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.color.YCbCrContainer;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.MultiImage;

import java.util.ArrayList;
import java.util.LinkedList;

public class ColorLayoutDescriptor {

	private ColorLayoutDescriptor() {
	}

	private static final int[] scan = { 0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32,
			25, 18, 11, 4, 5, 12, 19, 26, 33, 40, 48, 41, 34, 27, 20, 13, 6, 7,
			14, 21, 28, 35, 42, 49, 56, 57, 50, 43, 36, 29, 22, 15, 23, 30, 37,
			44, 51, 58, 59, 52, 45, 38, 31, 39, 46, 53, 60, 61, 54, 47, 55, 62,
			63 };

	private static final double SQRT1_2 = Math.sqrt(0.5);
	

	public static FloatVector calculateCLD(MultiImage img) {

		ArrayList<Integer> tmpList = new ArrayList<Integer>(img.getWidth() * img.getHeight());
		int[] colors = img.getColors();
		for (int c : colors) {
			//set all sufficiently transparent values to white
			if(ReadableRGBContainer.getAlpha(c) < 127){
				c = ReadableRGBContainer.WHITE_INT;
			}
			tmpList.add(c);
		}
		ArrayList<LinkedList<Integer>> partitions = GridPartitioner.partition(
				tmpList, img.getWidth(), img.getHeight(), 8, 8);

		int[] rgbs = new int[64];

		for (int i = 0; i < partitions.size(); ++i) {
			rgbs[i] = ColorUtils.getAvg(partitions.get(i));
		}

		int[][] ycbcrs = new int[3][64];
		for (int i = 0; i < 64; ++i) {
			YCbCrContainer c = ColorConverter.RGBtoYCbCr(new RGBContainer(
					rgbs[i]));
			ycbcrs[0][i] = c.getY();
			ycbcrs[1][i] = c.getCb();
			ycbcrs[2][i] = c.getCr();
		}

		ycbcrs[0] = dct(ycbcrs[0]);
		ycbcrs[1] = dct(ycbcrs[1]);
		ycbcrs[2] = dct(ycbcrs[2]);
		
		float[] cld = new float[]{
				ycbcrs[0][0], ycbcrs[0][1], ycbcrs[0][2], ycbcrs[0][3], ycbcrs[0][4], ycbcrs[0][5], 
				ycbcrs[1][0], ycbcrs[1][1], ycbcrs[1][2],
				ycbcrs[2][0], ycbcrs[2][1], ycbcrs[2][2]
		};
		
		return new FloatVectorImpl(cld);

	}
	/* based on c implementation by Berk ATABEK (http://www.batabek.com/) */
	private static int[] dct(int[] block) {
		double sum, cu, cv;
		int[] temp = new int[64];

		for (int u = 0; u < 8; ++u) {
			for (int v = 0; v < 8; ++v) {
				sum = 0.0;
				cu = (u == 0) ? SQRT1_2 : 1.0;
				cv = (v == 0) ? SQRT1_2 : 1.0;
				for (int x = 0; x < 8; ++x) {
					for (int y = 0; y < 8; ++y) {
						sum += block[x * 8 + y]
								* Math.cos((2 * x + 1) * u * Math.PI / 16.0)
								* Math.cos((2 * y + 1) * v * Math.PI / 16.0);
					}
				}
				temp[scan[8 * u + v]] = (int) Math.floor((0.25 * cu * cv * sum) + 0.5);
			}
		}
		return temp;
	}
	
}
