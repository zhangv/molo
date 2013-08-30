package com.modofo.molo.recommend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.MD5Hash;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.hadoop.item.RecommenderJob;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.iterator.FileLineIterable;
import org.apache.mahout.math.hadoop.similarity.cooccurrence.measures.PearsonCorrelationSimilarity;
import org.apache.mahout.math.hadoop.similarity.cooccurrence.measures.TanimotoCoefficientSimilarity;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.modofo.molo.util.Utils;


/**
 * 基于主题的推荐，根据不同的主题和对应算法来获取相似用户和item，目前主要支持collaborative filter(userbased/itembased)
 * 推荐初始数据的提交见TopicManager
 * @author zhangwei
 *
 */
public class Recommender {
	private static final Logger log = LoggerFactory.getLogger(Recommender.class);
	private String sampleDir;
	private String tempDir;

	public List<RecommendedItem> recommend(String topic,long userId,int howmany) throws RecommendException{
		String inputFile = this.sampleDir + "/" + topic + "/prefs.csv";
		String outputDir = this.tempDir + "/" + topic + "/output";
		String tmpDir = this.tempDir + "/"+ topic+"/tmp";
		if(isModified(inputFile)){ 
			log.info("input file is changed, will rebuild the recommendation");
			Utils.deleteDir(new File(outputDir));
			Utils.deleteDir(new File(tmpDir));
			Configuration conf = new Configuration();
		    conf.setBoolean("mapred.output.compress", false);
	
		    RecommenderJob recommenderJob = new RecommenderJob();
			recommenderJob.setConf(conf);
		    try {
		    	
				recommenderJob.run(new String[] { "-i", inputFile, "-o", outputDir, 
						"--tempDir", tmpDir , 
						"-s",TanimotoCoefficientSimilarity.class.getName(),
						//"-s",EuclideanDistanceSimilarity.class.getName(), //你妹！另外eu和pearson都不能用！LATER 搞清楚
						//"-s",PearsonCorrelationSimilarity.class.getName(),
						"-n", howmany+""});
				
				if(log.isInfoEnabled()){
					Utils.sequenceDump(conf, tmpDir+"/similarityMatrix/part-r-00000");
					Utils.sequenceDump(conf, tmpDir+"/pairwiseSimilarity/part-r-00000");
					Utils.sequenceDump(conf, tmpDir+"/partialMultiply/part-r-00000");
					Utils.sequenceDump(conf, tmpDir+"/preparePreferenceMatrix/itemIDIndex/part-r-00000");
					Utils.sequenceDump(conf, tmpDir+"/preparePreferenceMatrix/ratingMatrix/part-r-00000");
					Utils.sequenceDump(conf, tmpDir+"/preparePreferenceMatrix/userVectors/part-r-00000");
					Utils.sequenceDump(conf, tmpDir+"/prePartialMultiply1/part-r-00000");
					Utils.sequenceDump(conf, tmpDir+"/prePartialMultiply2/part-r-00000");
					Utils.sequenceDump(conf, tmpDir+"/weights/part-r-00000");
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RecommendException(e.toString());
			}
		}
		File outputFile = new File(outputDir,"part-r-00000");
		List<RecommendedItem> result = null;
		try {
			result = this.readRecommendations(userId, outputFile);
		} catch (IOException e) {
			log.error("error reading recommendations - "+e.toString());
			if(log.isDebugEnabled()) e.printStackTrace();
		}
		return result;
	    
	}
	
	private boolean isModified(String inputFile)  {
		File f = new File(inputFile);
		File dir = f.getParentFile();
		FileInputStream fis;
		try {
			fis = new FileInputStream(f);
			String md5 = MD5Hash.digest(fis).toString();
			fis.close();
			File md5file	 = new File(dir.getAbsolutePath()+"/md5");
			if(md5file.exists()) {
				Scanner sc = new Scanner(md5file);
				if(sc.hasNextLine()){
					String md5str = sc.nextLine();
					sc.close();
					if(md5.equals(md5str)){
						return false;
					}
				}
			}
			//update md5
			Writer writer = new OutputStreamWriter(new FileOutputStream(md5file), Charsets.UTF_8); 
			writer.write(md5);
			writer.flush();
			writer.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private List<RecommendedItem> readRecommendations(long userId,File file) throws IOException {
	    Iterable<String> lineIterable = new FileLineIterable(file);
	    for (String line : lineIterable) {

	      String[] keyValue = line.split("\t");
	      long userID = Long.parseLong(keyValue[0]);
	      if(userID != userId) continue;
	      String[] tokens = keyValue[1].replaceAll("\\[", "")
	          .replaceAll("\\]", "").split(",");

	      List<RecommendedItem> items = new LinkedList<RecommendedItem>();
	      for (String token : tokens) {
	        String[] itemTokens = token.split(":");
	        long itemID = Long.parseLong(itemTokens[0]);
	        float value = Float.parseFloat(itemTokens[1]);
	        items.add(new GenericRecommendedItem(itemID, value));
	      }
	      return items;
	    }
	    return null;
	  }
	
	public void evaluate(String topicId){
		DataModel model = createDataModel(topicId);
	    RecommenderBuilder builder = new RecommenderBuilder() {
	      @Override
	      public org.apache.mahout.cf.taste.recommender.Recommender buildRecommender(DataModel dataModel) throws TasteException {
	        return new SlopeOneRecommender(dataModel);
	      }
	    };
	    RecommenderIRStatsEvaluator evaluator = new GenericRecommenderIRStatsEvaluator();
	    IRStatistics stats;
		try {
			stats = evaluator.evaluate(builder, null, model, null, 1, 0.2, 1.0);
			System.out.println(stats);
		} catch (TasteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
	//非hadoop版本，只能用来做小样本
	public List<RecommendedItem> userBased(String topic,long userId,int howmany) throws RecommendException{
		//1. 从topic文件夹中拿到所有的sample，并转换为datamodel
		
		DataModel dataModel = createDataModel0(topic);
		//2. 参考GenericUserBasedRecommenderTest，获得推荐结果
		UserSimilarity similarity;
		try {
			//similarity = new PearsonCorrelationSimilarity(dataModel);
			similarity = new EuclideanDistanceSimilarity(dataModel);
			UserNeighborhood neighborhood = new NearestNUserNeighborhood(2, similarity, dataModel);
		    GenericUserBasedRecommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
		    List<RecommendedItem> recommended = recommender.recommend(userId, howmany);
			return recommended;
		} catch (TasteException e) {
			// TODO Auto-generated catch block
			if(Log.isDebugEnabled()) e.printStackTrace();
			throw new RecommendException(e.toString());
		}
	}

	private DataModel createDataModel(String topic){
		File prefFile = new File(sampleDir + "/"+topic+"/prefs");
		FastByIDMap<PreferenceArray> result = new FastByIDMap<PreferenceArray>();
		Scanner scanner;
		try {
			scanner = new Scanner(prefFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		List<Preference> prefsList = Lists.newArrayList();;
		long curUserId = -1L;
		while(scanner.hasNextLine()){
			String[] prefs = scanner.nextLine().split(",");
			long userId = Long.parseLong(prefs[0]);
			if(curUserId != -1L && curUserId != userId){
				if (!prefsList.isEmpty()) {
					result.put(userId, new GenericUserPreferenceArray(prefsList));
				}
				prefsList = Lists.newArrayList();
			}
			curUserId = userId;
			long itemId = Long.parseLong(prefs[1]);
			float pref = Float.parseFloat(prefs[2]);
			prefsList.add(new GenericPreference(curUserId, itemId, pref));
		}
		if (!prefsList.isEmpty()) {
			result.put(curUserId, new GenericUserPreferenceArray(prefsList));
		}
		
		return new GenericDataModel(result);
	}
	//old
	private DataModel createDataModel0(String topic){
		File topicDir = new File(sampleDir + "/"+topic);
		Scanner scanner = null;
		FastByIDMap<PreferenceArray> result = new FastByIDMap<PreferenceArray>();
		for(File f:topicDir.listFiles()){
			log.debug("parsing pref file - "+f.getAbsolutePath());
			if(f.isHidden()) continue;
			long eachUserId = Long.parseLong(f.getName());
			try {
				scanner = new Scanner(f);
				if(scanner.hasNextLine()){
					String[] prefs = scanner.nextLine().split(",");
					List<Preference> prefsList = Lists.newArrayList();
					for (int j = 0; j < prefs.length; j++) {
					if (prefs[j] != null && !"null".equals(prefs[j])) {
					      prefsList.add(new GenericPreference(eachUserId, j, Float.parseFloat(prefs[j])));
					    }
					}
					if (!prefsList.isEmpty()) {
						//log.info(eachUserId + ":"+prefsList);
						result.put(eachUserId, new GenericUserPreferenceArray(prefsList));
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return new GenericDataModel(result);
	}
	
	public List<RecommendedItem> itemBased(String topic,long itemId,int howmany){
		return null;
	}


	public String getSampleDir() {
		return sampleDir;
	}


	public void setSampleDir(String sampleDir) {
		this.sampleDir = sampleDir;
	}

	public String getTempDir() {
		return tempDir;
	}

	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}
}
