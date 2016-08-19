package org.vitrivr.cineast.core.util;

import java.util.List;

import org.vitrivr.cineast.core.data.LongDoublePair;

public class RankUtil {

	private RankUtil(){}
	
	public static int getRankOfShot(List<LongDoublePair> resultList, long id, int notFoundValue){
		int _return = notFoundValue;
		int i = 0;
		for(LongDoublePair ldp : resultList){
			++i;
			if(ldp.key == id){
				_return = i;
				break;
			}
		}
		return _return;
	}
	
	public static float getInvertedRank(List<LongDoublePair> resultList, long id){
		int rank = getRankOfShot(resultList, id, -1);
		if(rank == -1){
			return 0f;
		}
		return 1f / rank;
	}
	
}
