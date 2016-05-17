package ch.unibas.cs.dbis.cineast.core.features.abstracts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.db.DBSelector;
import ch.unibas.cs.dbis.cineast.core.features.retriever.Retriever;
import georegression.struct.point.Point2D_F32;

public abstract class MotionHistogramCalculator implements Retriever {

	protected DBSelector selector;
	protected final float maxDist;
	protected final String tableName;
	private static Logger LOGGER = LogManager.getLogger();


	protected MotionHistogramCalculator(String tableName, float maxDist){
		this.maxDist = maxDist;
		this.tableName = tableName;
	}
	
	@Override
	public void init(DBSelector selector) {
		this.selector = selector;
	}

	private static int getidx(int subdiv, float x, float y) {
		int ix = (int) Math.floor(subdiv * x), iy = (int) Math.floor(subdiv * y);
		ix = Math.max(Math.min(ix, subdiv - 1), 0);
		iy = Math.max(Math.min(iy, subdiv - 1), 0);

		return ix * subdiv + iy;
	}

	protected Pair<List<Double>, ArrayList<ArrayList<Float>>> getSubDivHist(
			int subdiv, List<Pair<Integer, LinkedList<Point2D_F32>>> list) {

		double[] sums = new double[subdiv * subdiv];
		float[][] hists = new float[subdiv * subdiv][8];

		for (Pair<Integer, LinkedList<Point2D_F32>> pair : list) {
			LinkedList<Point2D_F32> path = pair.second;
			if (path.size() > 1) {
				Iterator<Point2D_F32> iter = path.iterator();
				Point2D_F32 last = iter.next();
				while (iter.hasNext()) {
					Point2D_F32 current = iter.next();
					double dx = current.x - last.x, dy = current.y - last.y;
					int idx = ((int) Math.floor(4 * Math.atan2(dy, dx)
							/ Math.PI) + 4) % 8;
					double len = Math.sqrt(dx * dx + dy * dy);
					hists[getidx(subdiv, last.x, last.y)][idx] += len;
					last = current;
				}
			}
		}

		for (int i = 0; i < sums.length; ++i) {
			float[] hist = hists[i];
			double sum = 0;
			for (int j = 0; j < hist.length; ++j) {
				sum += hist[j];
			}
			if (sum > 0) {
				for (int j = 0; j < hist.length; ++j) {
					hist[j] /= sum;
				}
				hists[i] = hist;
			}
			sums[i] = sum;
		}

		ArrayList<Double> sumList = new ArrayList<Double>(sums.length);
		for (double d : sums) {
			sumList.add(d);
		}

		ArrayList<ArrayList<Float>> histList = new ArrayList<ArrayList<Float>>(
				hists.length);
		for (float[] hist : hists) {
			ArrayList<Float> h = new ArrayList<Float>(8);
			for (float f : hist) {
				h.add(f);
			}
			histList.add(h);
		}

		return new Pair<List<Double>, ArrayList<ArrayList<Float>>>(sumList,
				histList);
	}
	
	@Override
	public void finish(){
		if(this.selector != null){
			this.selector.close();
		}
	}

}
