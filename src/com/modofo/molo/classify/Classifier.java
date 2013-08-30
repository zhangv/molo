package com.modofo.molo.classify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.mahout.classifier.ClassifierResult;
import org.apache.mahout.classifier.ConfusionMatrix;
import org.apache.mahout.classifier.ResultAnalyzer;
import org.apache.mahout.classifier.naivebayes.AbstractNaiveBayesClassifier;
import org.apache.mahout.classifier.naivebayes.BayesUtils;
import org.apache.mahout.classifier.naivebayes.NaiveBayesModel;
import org.apache.mahout.classifier.naivebayes.StandardNaiveBayesClassifier;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.StringTuple;
import org.apache.mahout.common.iterator.sequencefile.PathFilters;
import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirIterable;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.math.map.OpenIntLongHashMap;
import org.apache.mahout.math.map.OpenObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.modofo.molo.SampleException;
import com.modofo.molo.model.ClassifyResult;
import com.modofo.molo.util.FeatureExtractor;
import com.modofo.molo.util.Utils;

/**
 * 基于贝叶斯的分类器
 * @author zhangwei
 *
 */
public class Classifier {
	private static final Logger log = LoggerFactory.getLogger(Classifier.class);
	private final OpenObjectIntHashMap<String> dictionary = new OpenObjectIntHashMap<String>();
	private final OpenIntLongHashMap frequency = new OpenIntLongHashMap();
	private boolean dictRebuilt = false;
	private static final int FEATURES = 10000;
	private String tempDir;
	private String modelDir;
	
	//cached
	private NaiveBayesModel model = null;
	private Map<String,Map<Integer,String>> labels = new HashMap<String,Map<Integer,String>>() ;
	
	/**
	 * 分类
	 * 
	 * @param topicId 主题
	 * @param text 要进行分类的文本
	 * @param n 结果数量
	 * @return 分类结果
	 * @throws SampleException
	 */
	public List<ClassifyResult> classify(String topicId, String text, int n)
			throws ClassifyException {
		return this.classify(topicId, text, "text",n);
	}
	
	public List<ClassifyResult> classify(String topicId, String text,String type,int resultCount)
			throws ClassifyException {
		Configuration conf = new Configuration();
		AbstractNaiveBayesClassifier classifier;
		try {
			classifier = loadClassifier(topicId, conf);
		} catch (IOException e) {
			throw new ClassifyException(e.toString());
		}
		Vector instance = null;
		if(type.equals("text")){
			instance = buildInstance(topicId,text);
		}else{
			instance = buildInstance2(topicId,text);
		}
		Vector r = classifier.classifyFull(instance);
		log.info("classifyresult vector:" + r);
		Path labelIndexPath = new Path(this.tempDir + "/" + topicId +"/" + topicId
				+ "-labelIndex");
		Map<Integer, String> labelMap = BayesUtils.readLabelIndex(conf,
				labelIndexPath);

		//int bestIdx = Integer.MIN_VALUE;
		//double bestScore = Long.MIN_VALUE;
		final HashMap<String, Double> resultMap = new HashMap<String, Double>();
		//TreeMap<String, Double> resultMap = new TreeMap<String, Double>();
		for (int i = 0; i < labelMap.size(); i++) {
			Vector.Element element = r.getElement(i);
			resultMap.put(labelMap.get(element.index()), element.get());
			
//			if (element.get() > bestScore) {
//				bestScore = element.get();
//				bestIdx = element.index();
//			}
		}
		
		List<String> infoIds = new ArrayList<String>(resultMap.keySet());
		Collections.sort(infoIds, new Comparator<String>() {   
		    public int compare(String o1, String o2) {      
		        return (int) (resultMap.get(o2) - resultMap.get(o1)); 
		    }
		}); 
		
		log.info("resultmap:" + resultMap);
		List<ClassifyResult> resultList = new ArrayList<ClassifyResult>(resultCount);
		for(int i = 0;i<resultCount;i++){
			ClassifyResult result = new ClassifyResult();
				String label = infoIds.get(i);
				double score = resultMap.get(infoIds.get(i));
				result.setLabel(label);
				result.setScore(score);
				log.info("label:" + label + ",text:" + text);
			resultList.add(result);
		}
		
		return resultList;
	}
	
	/**
	 * 初始化词典
	 * @param topicId 主题
	 * @throws IOException
	 */
	private void buildDictionary(String topicId) throws IOException{
		if(dictRebuilt) return;
		Configuration conf = getConf();
	    Path dictionaryFile = new Path(tempDir+"/"+topicId+"/"+topicId+"-vectors/dictionary.file-0");
	    // key is feature, value is the document frequency
	    for (Pair<Text,IntWritable> record 
	         : new SequenceFileIterable<Text,IntWritable>(dictionaryFile, true, conf)) {
	      dictionary.put(record.getFirst().toString(), record.getSecond().get());
	    }
	    Path freqFile = new Path(tempDir+"/"+topicId+"/"+topicId+"-vectors/frequency.file-0");
	    // key is feature, value is the document frequency
	    for (Pair<IntWritable,LongWritable> record 
	         : new SequenceFileIterable<IntWritable,LongWritable>(freqFile, true, conf)) {
	    	frequency.put(record.getFirst().get(), record.getSecond().get());
	    }
	    dictRebuilt = true;
	}
	
	/**
	 * 根据主题将文本转换为向量
	 * @param topicId 主题
	 * @param text 文本
	 * @return
	 */
	private Vector buildInstance(String topicId,String text){
		try {
			buildDictionary(topicId);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Vector vector = new RandomAccessSparseVector(FEATURES);
		FeatureExtractor fe = new FeatureExtractor();
		HashSet<String> fs = fe.extract(text);
		for (String s : fs) {
			int index = dictionary.get(s);
			vector.setQuick(index, frequency.get(index));
		}
		return vector;
	}
	
	/**
	 * 根据主题将逗号分隔的属性转换为向量
	 * @param topicId
	 * @param line
	 * @return
	 */
	private Vector buildInstance2(String topicId,String line){
		String[] features = line.split(","); // csv
		DenseVector instance = new DenseVector(
				features.length);
		for (int i = 0; i < features.length; i++) {
			Double db = new Double(features[i]); 
			instance.set(i, db);
		}
		return instance;
	}

	/**
	 * 加载分类器模型
	 * @param topicId
	 * @param conf
	 * @return
	 * @throws IOException
	 */
	private AbstractNaiveBayesClassifier loadClassifier(String topicId,
			Configuration conf) throws IOException {
		Path modelPath = new Path(this.modelDir + "/" + topicId + "-model");
		if(this.model == null) model = NaiveBayesModel.materialize(modelPath, conf);
		AbstractNaiveBayesClassifier classifier = new StandardNaiveBayesClassifier(
				model);
		// AbstractVectorClassifier classifier = new ComplementaryNaiveBayesClassifier(model);
		return classifier;
	}
	
	/**
	 * Hadoop配置
	 * @return
	 */
	private Configuration getConf() {
		Configuration HADOOP_CONF = new Configuration();
//		HADOOP_CONF.set("hadoop.log.dir",
//				"/Users/derekzhangv/Develop/hadoop/logs");
		HADOOP_CONF.set("hadoop.log.file", "hadoop.log");
		HADOOP_CONF.set("mapred.min.split.size", "512MB");
		HADOOP_CONF.set("mapred.map.child.java.opts", "-Xmx4096m");
		HADOOP_CONF.set("mapred.reduce.child.java.opts", "-Xmx4096m");
		HADOOP_CONF.set("mapred.output.compress", "true");
		HADOOP_CONF.set("mapred.compress.map.output", "true");
		HADOOP_CONF.set("mapred.map.tasks", "1");
		HADOOP_CONF.set("mapred.reduce.tasks", "1");
		HADOOP_CONF.set("io.sort.factor", "30");
		HADOOP_CONF.set("io.sort.mb", "1024");
		HADOOP_CONF.set("io.file.buffer.size", "32786");
		return HADOOP_CONF;
	}

	/**
	 * 获取准确率
	 * 
	 * @param topicId 主题
	 * @return 准确率（0-100）
	 */
	public Double accuracy(String topicId){
		 //load the labels
		Path labelIndexPath = new Path(this.tempDir + "/" + topicId +"/" + topicId + "-labelIndex");
		Path testingDirPath = new Path(this.tempDir+"/"+topicId+"/"+topicId+"-testing");
	    Map<Integer, String> labelMap = BayesUtils.readLabelIndex(getConf(), labelIndexPath);

	    //loop over the results and create the confusion matrix
	    SequenceFileDirIterable<Text, VectorWritable> dirIterable =
	        new SequenceFileDirIterable<Text, VectorWritable>(testingDirPath,
	                                                          PathType.LIST,
	                                                          PathFilters.partFilter(),
	                                                          getConf());
	    ResultAnalyzer analyzer = new ResultAnalyzer(labelMap.values(), "DEFAULT");
	    Double percentageCorrect = analyzeResults(labelMap, dirIterable, analyzer);
	    return 100*percentageCorrect;
	}
	
	/**
	 * 获取多个结果合并后准确率
	 *  
	 * @param topicId
	 * @param optionSize
	 * @return
	 */
	public HashMap<String, Double> accuracy(String topicId,int optionSize){
		ConfusionMatrix cm = this.confusionmatrix(topicId);
		Collection<String> labels = cm.getLabels();
		HashMap<String,Double> accuracy = new HashMap<String,Double>();
		for(String label:labels){
			if(label.equals(".DS_Store") || label.equals("DEFAULT")) continue; 
			HashMap<String,Integer> count = new HashMap<String,Integer>();
			for(String label2:labels){
				if(label2.equals(".DS_Store") || label2.equals("DEFAULT")) continue;
				count.put(label2,cm.getCount(label, label2));
			}
			LinkedHashMap<String,Integer> sorted = Utils.sortByValue(count);
			Integer topSum = 0,totalSum = 0;
			int i=0;
			for(String key:sorted.keySet()){
				if(i<optionSize){
					topSum += sorted.get(key);
					i++;
				}
				totalSum +=sorted.get(key);
			}
			accuracy.put(label, (topSum/(double)totalSum));
		}
		return accuracy;
	}
	   

	/**
	 * 获取confusion matrix
	 * @param topicId 主题
	 * @return
	 */
	public ConfusionMatrix confusionmatrix(String topicId) {
		 //load the labels
		Path labelIndexPath = new Path(this.tempDir + "/" + topicId +"/" + topicId
				+ "-labelIndex");
		Path testingDirPath = new Path(this.tempDir+"/"+topicId+"/"+topicId+"-testing");
	    Map<Integer, String> labelMap = BayesUtils.readLabelIndex(getConf(), labelIndexPath);

	    //loop over the results and create the confusion matrix
	    SequenceFileDirIterable<Text, VectorWritable> dirIterable =
	        new SequenceFileDirIterable<Text, VectorWritable>(testingDirPath,
	                                                          PathType.LIST,
	                                                          PathFilters.partFilter(),
	                                                          getConf());
	    ResultAnalyzer analyzer = new ResultAnalyzer(labelMap.values(), "DEFAULT");
	    analyzeResults(labelMap, dirIterable, analyzer);
	    return analyzer.getConfusionMatrix();
	}
	
	private static Double analyzeResults(Map<Integer, String> labelMap,
			SequenceFileDirIterable<Text, VectorWritable> dirIterable,
			ResultAnalyzer analyzer) {
		int totalClassified = 0;
		int correctlyClassified = 0;
		for (Pair<Text, VectorWritable> pair : dirIterable) {
			int bestIdx = Integer.MIN_VALUE;
			double bestScore = Long.MIN_VALUE;
			for (Vector.Element element : pair.getSecond().get()) {
				if (element.get() > bestScore) {
					bestScore = element.get();
					bestIdx = element.index();
				}
			}
			if (bestIdx != Integer.MIN_VALUE) {
				ClassifierResult classifierResult = new ClassifierResult(
						labelMap.get(bestIdx), bestScore);
				String correctLabel = pair.getFirst().toString();
				String classifiedLabel = classifierResult.getLabel();
				analyzer.addInstance(pair.getFirst().toString(), classifierResult);
				boolean result = correctLabel.equals(classifiedLabel);
			    if (result) {
			    	correctlyClassified++;
			    } 
				totalClassified++;
			}
		}
		return (double) correctlyClassified/totalClassified;
	}

	/**
	 * 获取主题下的所有labels（类名）
	 * @param topicId 主题
	 * @return
	 */
	public Map<Integer, String> labels(String topicId){
		 //load the labels
		if(labels.get(topicId) == null){
			Path labelIndexPath = new Path(this.tempDir + "/" + topicId +"/" + topicId
					+ "-labelIndex");
			Map<Integer, String> labelMap = BayesUtils.readLabelIndex(getConf(), labelIndexPath);
			labels.put(topicId, labelMap);
		}
		return labels.get(topicId);
	}
	
	/**
	 * 获取某个类的top关键词
	 * 
	 * @param topicId 主题
	 * @param clz 类
	 * @param max 关键词数量
	 * @return
	 */
	private HashMap<String,Map<String,Integer>> dnaCache = new HashMap<String,Map<String,Integer>>();
	public Map<String,Integer> dna(String topicId,String clz,int max){
		String key = topicId+clz;
		//System.out.println("cache"+dnaCache.get(key));
		if(dnaCache.get(key) == null){
			Path path = new Path(this.tempDir + "/" + topicId +"/" + topicId
					+ "-vectors/tokenized-documents");
			SequenceFileDirIterable<Text, StringTuple> dirIterable =
				        new SequenceFileDirIterable<Text, StringTuple>(path,
				                                                          PathType.LIST,
				                                                          PathFilters.partFilter(),
				                                                          getConf());
			HashMap<String,HashMap<String,Integer>> stat = new HashMap<String,HashMap<String,Integer>>();
			for (Pair<Text, StringTuple> pair : dirIterable) {
				String first = pair.getFirst().toString();
				String clzname = first.substring(1,first.lastIndexOf('/'));
				if(stat.get(clzname) == null) stat.put(clzname, new HashMap<String,Integer>());
				StringTuple second = pair.getSecond();
				List<String> entries = second.getEntries();
				HashMap<String,Integer> clzstat = stat.get(clzname);
				for(String entry:entries){
					if(clzstat.get(entry) == null) clzstat.put(entry, 0);
					clzstat.put(entry, clzstat.get(entry)+1);
				}
			}
			final HashMap<String,Integer> re = stat.get(clz);
			List<String> keys = new ArrayList(re.keySet());
			Collections.sort(keys, new Comparator(){
				@Override
				public int compare(Object arg0, Object arg1) {
					return re.get(arg1) - re.get(arg0); 
				}
			});
			LinkedHashMap<String,Integer> result = new LinkedHashMap<String,Integer>();
			int count = 0;
			for(String k:keys){
				if(count == max) break;
				result.put(k, re.get(k));
				count++;
			}
			dnaCache.put(key,result);
		}
		return dnaCache.get(key);
	}
	
	/**
	 * 获取一个主题各类的top关键词列表
	 * @param topicId 主题
	 * @param count 返回数量
	 * @return
	 */
	public Map<String,List<String>> topWords(String topicId,int count){
		Path path = new Path(this.tempDir + "/" + topicId +"/" + topicId
				+ "-vectors/tokenized-documents");
		SequenceFileDirIterable<Text, StringTuple> dirIterable =
			        new SequenceFileDirIterable<Text, StringTuple>(path,
			                                                          PathType.LIST,
			                                                          PathFilters.partFilter(),
			                                                          getConf());
		HashMap<String,HashMap<String,Integer>> stat = new HashMap<String,HashMap<String,Integer>>();
		for (Pair<Text, StringTuple> pair : dirIterable) {
			String first = pair.getFirst().toString();
			String clzname = null;
			try{
				clzname = first.substring(1,first.lastIndexOf('/'));
			}catch(Exception e){continue;}
			if(stat.get(clzname) == null) stat.put(clzname, new HashMap<String,Integer>());
			StringTuple second = pair.getSecond();
			List<String> entries = second.getEntries();
			HashMap<String,Integer> clzstat = stat.get(clzname);
			for(String entry:entries){
				if(clzstat.get(entry) == null) clzstat.put(entry, 0);
				clzstat.put(entry, clzstat.get(entry)+1);
			}
		}
		
		Set<String> clzs = stat.keySet();
		HashMap<String,List<String>> result = new HashMap<String,List<String>>();
		for(String clz:clzs){
			final HashMap<String,Integer> re = stat.get(clz);
			List<String> keys = new ArrayList(re.keySet());
			Collections.sort(keys, new Comparator(){
				@Override
				public int compare(Object arg0, Object arg1) {
					return re.get(arg1) - re.get(arg0); 
				}
			});
			result.put(clz,keys.subList(0, count));
		}
		return result;
	}
	
	public String getTempDir() {
		return tempDir;
	}

	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}

	public String getModelDir() {
		return modelDir;
	}

	public void setModelDir(String modelDir) {
		this.modelDir = modelDir;
	}
	
}
