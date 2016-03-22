package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.db.ADAMTuple;
import ch.unibas.cs.dbis.cineast.core.db.PersistentTuple;
import ch.unibas.cs.dbis.cineast.core.decode.subtitle.SubtitleItem;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;

public class SubtitleFulltextSearch extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public SubtitleFulltextSearch(){
		super("features.fulltext", "", 1);
	}
	
	@Override
	public void processShot(FrameContainer shot) {
		LOGGER.entry();
		List<SubtitleItem> subiItems = shot.getSubtitleItems();
		if (!subiItems.isEmpty() && !phandler.check("SELECT * FROM features.fulltext WHERE shotid = " + shot.getId())) {
			StringBuffer buf = new StringBuffer();
			for(SubtitleItem si : subiItems){
				buf.append(si.getText());
				buf.append('\n');
			}
			addToDB(shot.getId(), buf.toString());
		}
		LOGGER.exit();
	}

	private void addToDB(long shotId, String s) {
		PersistentTuple tuple = this.phandler.makeTuple(shotId, s);
		this.phandler.write(tuple);
	}
	
	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		return getSimilar(qc, null);
	}
	
	private List<LongDoublePair> getText(String text, int limit, String resultCacheName){
		String query;
		if(resultCacheName == null){
			query = "WITH q AS ("
				+ "SELECT plainto_tsquery('" + ADAMTuple.escape(text) + "') AS query), "
				+ "ranked AS( "
				+ "SELECT shotid, text, ts_rank_cd(tsv, query) AS rank "
				+ "FROM features.fulltext, q "
				+ "WHERE q.query @@ tsv "
				+ "ORDER BY rank DESC "
				+ "LIMIT " + limit + ") "
				+ "SELECT shotid, rank "
				+ "FROM ranked, q "
				+ "ORDER BY ranked DESC";
		}else{
			query = "WITH c AS (SELECT shotid AS filter FROM cineast.resultcacheelements, cineast.resultcachenames WHERE "
				+ "resultcacheelements.chacheid = resultcachenames.id AND resultcachenames.name = '"
				+ resultCacheName 
				+ "'), q AS ("
				+ "SELECT plainto_tsquery('" + ADAMTuple.escape(text) + "') AS query), "
				+ "ranked AS( "
				+ "SELECT shotid, text, ts_rank_cd(tsv, query) AS rank "
				+ "FROM features.fulltext, q, c "
				+ "WHERE q.query @@ tsv "
				+ "AND shotid = c.filter "
				+ "ORDER BY rank DESC "
				+ "LIMIT " + limit + ") "
				+ "SELECT shotid, rank "
				+ "FROM ranked, q "
				+ "ORDER BY ranked DESC";
		}
		ResultSet rset = this.selector.select(query);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(long shotId) {
		return getSimilar(shotId, null);		
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		StringBuffer buf = new StringBuffer();
		
		List<LongDoublePair> result;
		if(!qc.getSubtitleItems().isEmpty()){
			for(SubtitleItem item : qc.getSubtitleItems()){
				buf.append(item.getText());
				buf.append('\n');
			}
			result = getText(buf.toString(), limit, resultCacheName);
		}else{
			result = new ArrayList<LongDoublePair>(1);
		}
		
		return result;
	}

	@Override
	public List<LongDoublePair> getSimilar(long shotId, String resultCacheName) {
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		ResultSet rset = this.selector.select("select text from features.fulltext where shotid = " + shotId);
		if(rset != null){
			try{
				if(rset.next()){
					String text = rset.getString(1);
					List<LongDoublePair> result = getText(text, limit, resultCacheName);
					return result;
				}
			}catch(SQLException e){
				LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
			}
		}
		return new ArrayList<LongDoublePair>(1);
	}

}
