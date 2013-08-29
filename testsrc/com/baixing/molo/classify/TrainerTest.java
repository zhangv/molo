package com.baixing.molo.classify;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.baixing.molo.SampleException;
import com.baixing.molo.SampleManager;
import com.baixing.molo.TopicManager;
import com.baixing.molo.model.Sample;
import com.baixing.molo.model.Topic;
import com.baixing.molo.model.TrainStatus;

public class TrainerTest {
	private Trainer target = new Trainer();
	private TopicManager tm = new TopicManager();
	private SampleManager sampler = null;
	private String sampleDir, tempDir,modelDir;
	private static String rootDir = "/Users/derekzhangv/Develop";

	@Before
	public void setUp() {
		this.sampleDir = "/sampling";// +System.currentTimeMillis();
		this.tempDir = "/temptest";// +System.currentTimeMillis();
		this.modelDir = "/sampling-models";
		target.setSampleDir(rootDir + sampleDir);
		target.setTempDir(rootDir + tempDir);
		target.setModelDir(rootDir + modelDir);
		sampler = new SampleManager(rootDir+sampleDir);
		tm.setSampleManager(sampler);
		tm.setSampleDir(sampleDir);
		tm.setTempDir(tempDir);
	}

	@After
	public void tearDown() {
		File f = new File(this.sampleDir);
		f.deleteOnExit();
		f = new File(this.tempDir);
		f.deleteOnExit();
	}

	@Test
	public void testStatus() {
		TrainStatus s = target.status("secondaryCategory");
		System.out.println(s.getStage());
	}

	@Test
	public void testTrainAll() {
		File f = new File(target.getSampleDir());
		for(File ff:f.listFiles()){
			
			Topic sampleTopic = new Topic();
			String name = ff.getName();
	
			// target.clear(name);
			sampleTopic.setName(name);
			try {
				tm.createTopic(sampleTopic);
			} catch (SampleException e) {
				fail(e.toString());
			}
			try {
				target.train(name);
			} catch (TrainException e) {
				e.printStackTrace();
				//fail(e.toString());
			}
		}
	}
	
	@Test
	public void testTrain() { //not work, too few words
		Topic sampleTopic = new Topic();
		String name = "testTopic";// +System.currentTimeMillis();
		sampleTopic.setName(name);
		target.clear(name);
		try {
			tm.createTopic(sampleTopic);
		} catch (SampleException e) {
			fail(e.toString());
		}
		String[] goodSamples = new String[] { "二手电冰箱", "电动车", "婴儿用品", "世界很大",
				"浪潮之巅" };
		String[] badSamples = new String[] { "asdf", "zzzzz", "太平洋", "我饿了，想吃饭",
				"上海好热" };
		Random r = new Random();
		for (String s : goodSamples) {
			Sample sample1 = new Sample();
			sample1.setId(r.nextInt(100000) + "");
			sample1.setClazz("good");
			sample1.setText(s);
			try {
				sampler.addSample(name, sample1);
			} catch (SampleException e) {
				fail(e.toString());
			}
		}
		for (String s : badSamples) {
			Sample sample1 = new Sample();
			sample1.setId(r.nextInt(100000) + "");
			sample1.setClazz("bad");
			sample1.setText(s);
			try {
				sampler.addSample(name, sample1);
			} catch (SampleException e) {
				fail(e.toString());
			}
		}
		try {
			target.train(name);
		} catch (TrainException e) {
			e.printStackTrace();
			fail(e.toString());
		}

	}
	
	@Test
	public void testTrainNew(){
		String name = "ershou2";
		target.clear(name );
		try {
			target.train(name);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} 
	}
	

	@Test
	public void testTrainF() {
		//prepare test data
		//1. class folders
		String[] goodsamples = new String[]{
			"1,0,2,1,1,0,1",
			"1,0,1,1,1,0,1",
			"1,0,1,2,1,0,1",
			"1,0,1,1,2,0,3"
		};
		
		String[] badsamples = new String[]{
			"1,0,4,1,1,1,1",
			"1,1,1,1,1,1,1",
		};
		this.addSamples(goodsamples, "testFixed", "good");
		this.addSamples(badsamples, "testFixed", "bad");
		
		//2. for each class, give one or more file, in each file,  each line for each instance
		try {
			target.trainF("testFixed");
		} catch (TrainException e) {
			e.printStackTrace();
			fail();
			
		}
	}
	
	@Test
	public void testTrainPosting() {
		String name = "postingbeha";
		target.clear(name );
		try {
			//sampler.generateSampleSet(name);
			target.trainAll(name);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} 
	}
	
	@Test
	public void testTrainCategory() {
		String name = "firstlevelcategory";
		target.clear(name );
		try {
			target.train(name);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} 
	}
	
	@Test
	public void testTrainEachCategory() {
		String[] names = {"cheliang","ershou","chongwuleimu","gongzuo","jianzhi","jianli","huodong","jiaoyupeixun","fuwu","fang"};
		for(String name:names){
			target.clear(name );
			try {
				target.train(name);
			} catch (Exception e) {
				e.printStackTrace();
				fail();
			}
		}
	}

	@Test
	public void testTrainGongzuoktv() {
		String name = "gongzuo-ktv";
		target.clear(name );
		try {
			target.train(name);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} 
	}
	
	private void addSamples(String[] samples, String name, String sampleClass) {
		Random r = new Random();
		for (String s : samples) {
			Sample sample1 = new Sample();
			sample1.setId(r.nextInt(100000) + "");
			sample1.setClazz(sampleClass);
			sample1.setText(s);
			try {
				sampler.addSample(name, sample1);
			} catch (SampleException e) {
				fail(e.toString());
			}
		}
	}
	
}
