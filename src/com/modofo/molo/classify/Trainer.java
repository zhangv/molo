package com.modofo.molo.classify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.lang.time.StopWatch;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.classifier.naivebayes.test.TestNaiveBayesDriver;
import org.apache.mahout.classifier.naivebayes.training.TrainNaiveBayesJob;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.text.SequenceFilesFromDirectory;
import org.apache.mahout.utils.SplitInput;
import org.apache.mahout.vectorizer.SparseVectorsFromSequenceFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.google.common.io.Closeables;
import com.modofo.molo.SampleException;
import com.modofo.molo.model.TrainResult;
import com.modofo.molo.model.TrainStatus;
import com.modofo.molo.util.Utils;

/**
 * 模型训练器
 * 
 * @author zhangwei
 * 
 */
public class Trainer implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(Trainer.class);
	private Configuration HADOOP_CONF = new Configuration();
	
	private String sampleDir;
	private String tempDir;
	private String modelDir;
	private String topicId;
	public Trainer() {
		// HADOOP_CONF.set("hadoop.log.dir",
		// "/Users/derekzhangv/Develop/hadoop/logs");
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
	}
	
	public Trainer(String topicId){
		this();
		this.topicId = topicId;
	}

	private Configuration getConf() {
		return HADOOP_CONF;
	}

	/**
	 * 训练
	 * 
	 * @param sampleTopicId
	 * @throws SampleException
	 */
	public void train(String sampleTopicId) throws TrainException {
		String topicSampleDir = this.sampleDir + "/" + sampleTopicId;
		String topicTempDir = this.tempDir + "/" + sampleTopicId;
		String sequenceDir = topicTempDir + "/" + sampleTopicId + "-seq";
		String trainVectorDir = topicTempDir + "/" + sampleTopicId
				+ "-train-vectors";
		String vectorDir = topicTempDir + "/" + sampleTopicId + "-vectors";
		String testVectorDir = topicTempDir + "/" + sampleTopicId
				+ "-test-vectors";
		String tfidfVectorDir = vectorDir + "/tfidf-vectors";
		String tfVectorDir = vectorDir + "/tf-vectors";
		String modelDir = this.modelDir + "/" + sampleTopicId + "-model";
		String labelIndexDir = topicTempDir + "/" + sampleTopicId
				+ "-labelIndex";
		String testingDir = topicTempDir + "/" + sampleTopicId + "-testing";
		String[] args = null;
		String weight = "tfidf";
		
		Utils.cleanDir(topicSampleDir);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		// 1.SeqfromDir
		args = new String[] { "-i", topicSampleDir, "-o", sequenceDir };
		SequenceFilesFromDirectory sequenceJob = new SequenceFilesFromDirectory();
		sequenceJob.setConf(getConf());
		try {
			sequenceJob.run(args);
			Utils.sequenceDump(getConf(),sequenceDir);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TrainException(
					"error happend converting to sequence file - "
							+ e.toString());
		}
		stopWatch.split();
		// 2.Seq2vector
		
		args = new String[] { "-i", sequenceDir, "-o", vectorDir, "-lnorm",
				"-nv", "-wt", weight, "-s", "2" // minSuport, default 2 //TODO
													// prod时参数修改为2+
				//, "-a", net.paoding.analysis.analyzer.PaodingAnalyzer.class.getName()
				//, "-a", org.wltea.analyzer.lucene.IKAnalyzer.class.getName()
				, "-a", IKSmartAnalyzer.class.getName()
				, "-md", "2" //最小document frequency 
				//, "-xs", "3.0" //sigma --the vocab count has to be greater than 0!
				//, "--maxDFPercent","20"
				};
		SparseVectorsFromSequenceFiles vectorizeJob = new SparseVectorsFromSequenceFiles();
		vectorizeJob.setConf(getConf());
		try {
			vectorizeJob.run(args);
			if (log.isInfoEnabled()) {
				Utils.sequenceDump(getConf(),vectorDir + "/wordcount");
				Utils.sequenceDump(getConf(),vectorDir + "/tokenized-documents");
				Utils.sequenceDump(getConf(),vectorDir + "/dictionary.file-0");
				Utils.sequenceDump(getConf(),vectorDir + "/frequency.file-0");
				Utils.vectorDump(getConf(),vectorDir + "/df-count"); //java.lang.ClassCastException: org.apache.hadoop.io.LongWritable cannot be cast to org.apache.mahout.math.VectorWritable
				Utils.vectorDump(getConf(),vectorDir + "/tf-vectors");
				Utils.sequenceDump(getConf(),vectorDir + "/tf-vectors");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new TrainException(
					"error happend converting to vector file - " + e.toString());
		}
		stopWatch.split();
		// 3.split
		String inputDir = weight.equals("tf")?tfVectorDir:tfidfVectorDir;
		args = new String[] { "-i", inputDir, "--trainingOutput",
				trainVectorDir, "--testOutput", testVectorDir,
				"--randomSelectionPct", "20", "--overwrite", "--sequenceFiles",
				"-xm", "sequential" };
		SplitInput splitJob = new SplitInput();
		splitJob.setConf(getConf());
		try {
			splitJob.run(args);
			if (log.isDebugEnabled()) {
//				Utils.vectorDump(getConf(),tfidfVectorDir);
//				Utils.vectorDump(getConf(),testVectorDir);
//				Utils.vectorDump(getConf(),trainVectorDir);
			}
		} catch (Exception e) {
			throw new TrainException(
					"error happend spliting the vector files - " + e.toString());
		}
		stopWatch.split();
		// 4.train
		args = new String[] { "-i", trainVectorDir, "-o", modelDir, "-li",
				labelIndexDir, "-el", "-ow" };
		TrainNaiveBayesJob trainJob = new TrainNaiveBayesJob();
		trainJob.setConf(getConf());
		try {
			trainJob.run(args);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TrainException("error happend training - " + e.toString());
		}
		stopWatch.split();
		// 5.test
		args = new String[] { "-i", testVectorDir, 
				"-m", modelDir, "-l",
				labelIndexDir, "-ow", "-o", testingDir };
		// 如果加 -seq 指定不用mapreduce，但是会报test-vector目录找不到，猜想和hdfs有关
		TestNaiveBayesDriver testJob = new TestNaiveBayesDriver();
		testJob.setConf(getConf());
		try {
			testJob.run(args);
			if (log.isDebugEnabled()) {
				Utils.vectorDump(getConf(),testingDir);
				Utils.sequenceDump(getConf(),testingDir);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new TrainException("error happend testing - " + e.toString());
		}
		stopWatch.split();
		stopWatch.stop();
		log.info("Time elapsed:"+stopWatch.getTime()/1000);
	}

	@Override
	public void run() {
		try {
			if(this.topicId != null)
				train(this.topicId);
			else throw new TrainException("TopicId required");
		} catch (TrainException e) {
			log.error(e.toString());
			if (log.isDebugEnabled())
				e.printStackTrace();
		}
	}

	public void trainAll(final String topicId){//训练所有以topicId开头的样本
		File sampleDirF = new File(this.tempDir);
		File[] topics = sampleDirF.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.startsWith(topicId);
			}
		});
		Classifier classifier = new Classifier();
		classifier.setTempDir(this.tempDir);
		classifier.setModelDir(this.modelDir);
		HashMap map = new HashMap();
		ArrayList<Double> accus = new ArrayList<Double>();
		for(File topic:topics){
			try {
				//this.trainF(topic.getName());
				accus.add(classifier.accuracy(topic.getName()));
				map.put(classifier.accuracy(topic.getName()),topic.getName());
			} catch (Exception e) {
				log.error(topic.getName()+","+e.toString());
				e.printStackTrace();
				continue;
			}
		}
		Collections.sort(accus,new Comparator<Double>(){
			@Override
			public int compare(Double o1, Double o2) {
				return o2>o1?1:-1;
			}
		});
		for(int i = 0;i<accus.size();i++){
			System.out.println(map.get(accus.get(i))+" - "+accus.get(i));
		}
	}
	/**
	 * 训练非文本主题
	 * @param topicId
	 * @throws TrainException
	 */
	public void trainF(String topicId) throws TrainException {
		String topicSampleDir = this.sampleDir + "/" + topicId;
		String topicTempDir = this.tempDir + "/" + topicId;
		String trainSequence = topicTempDir + "/" + topicId + "-seq";
		String testSequence = topicTempDir + "/" + topicId + "-test-seq";
		String modelDir = this.modelDir + "/" + topicId + "-model";
		String labelIndexDir = topicTempDir + "/" + topicId
				+ "-labelIndex";
		double testPct = 20;
		Utils.deleteDir(new File(topicTempDir));
		String[] args = null;
		//1. prepare csv to sequence file format
		SequenceFile.Writer trainWriter = null;
		SequenceFile.Writer testWriter = null;
		try {
			trainWriter = new SequenceFile.Writer(FileSystem.get(getConf()),
					getConf(), new Path(trainSequence), Text.class,
					VectorWritable.class);
			testWriter = new SequenceFile.Writer(FileSystem.get(getConf()),
					getConf(), new Path(testSequence), Text.class,
					VectorWritable.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		File sampleDirF = new File(topicSampleDir);
		try {
			for (File f : sampleDirF.listFiles()) { // classes
				File[] fs = f.listFiles();
				if(fs == null || fs.length < 5) continue;
				for (File ff : f.listFiles()) { // samples
					Scanner sc = new Scanner(ff);
					while (sc.hasNextLine()) {
						String line = sc.nextLine();
						String[] features = line.split(","); // csv
						DenseVector trainingInstance = new DenseVector(
								features.length);
						for (int i = 0; i < features.length; i++) {
							try{
								Double db = new Double(features[i]); // 在加入之前必须要已经normalized
								trainingInstance.set(i, db);
								
							}catch(Exception e){
								System.out.println(topicId+","+ff.getName());
								e.printStackTrace();
							}
							
						}
						Text t = new Text("/"+f.getName()+"/"); //必须要加这两个“/”，参考NaiveBayesTest
						Random r = new Random();
						int ri = r.nextInt(100);
						if(ri >= testPct){
							trainWriter.append(t, new VectorWritable(trainingInstance));
						}else testWriter.append(t, new VectorWritable(trainingInstance));
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Closeables.closeQuietly(trainWriter);
			Closeables.closeQuietly(testWriter);
		}
		Utils.sequenceDump(getConf(),trainSequence);
		Utils.sequenceDump(getConf(),testSequence);
		
		// 3. train
		args = new String[]{"--input", trainSequence,
				"--output", modelDir, "-el", "--tempDir", topicTempDir ,"-li",
				labelIndexDir};
		TrainNaiveBayesJob trainNaiveBayes = new TrainNaiveBayesJob();
		trainNaiveBayes.setConf(getConf());
		try {
			trainNaiveBayes.run(args);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TrainException(e.toString());
		}
		
		// 4.test
		String testingDir = topicTempDir + "/" + topicId + "-testing";
		args = new String[] { "-i", testSequence, "-m", modelDir, "-l",
				labelIndexDir, "-ow", "-o", testingDir };
		try {
			ToolRunner.run(getConf(), new TestNaiveBayesDriver(), args);
			if (log.isDebugEnabled()) {
				Utils.vectorDump(getConf(),testingDir);
				Utils.sequenceDump(getConf(),testingDir);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new TrainException("error happend testing - " + e.toString());
		}
		
	}
	
	/**
	 * 训练状态
	 * 
	 * @param topicId
	 * @return
	 */
	public TrainStatus status(String topicId) {
		File f = new File(this.tempDir +"/" + topicId+ "/" + topicId + "-seq");
		TrainStatus status = TrainStatus.WAITING;
		if (f.exists()){
			status = TrainStatus.STARTED;
			f = new File(this.tempDir +"/" + topicId + "/" + topicId + "-model");
			if (f.exists()) {
				File binfile = new File(f.getAbsolutePath()
						+ "/naiveBayesModel.bin"); // FIXME 需要有一种更好的判断方法，临时方案
				if (binfile.exists())
					status = TrainStatus.COMPLETED;
			}
		}
		return status;
	}

	/**
	 * 清空所有样本
	 * 
	 * @param topicId
	 * @return
	 */
	public TrainResult clear(String topicId) {
		File tempf = new File(this.tempDir+"/"+topicId);
		TrainResult result = new TrainResult();
		result.setStatus("OK");
		try{
			Utils.deleteDir(tempf);
		}catch(Exception e){
			result.setStatus("FAIL");
			if(log.isDebugEnabled()) e.printStackTrace();
		}
		return result;
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

	public String getModelDir() {
		return modelDir;
	}

	public void setModelDir(String modelDir) {
		this.modelDir = modelDir;
	}

}

