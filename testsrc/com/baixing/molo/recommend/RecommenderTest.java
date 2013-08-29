package com.baixing.molo.recommend;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.junit.Before;
import org.junit.Test;

import com.baixing.molo.SampleException;
import com.baixing.molo.SampleManager;
import com.baixing.molo.model.Sample;

public class RecommenderTest {
	SampleManager tm = new SampleManager();
	Recommender target;
	private String sampleDir, tempDir,modelDir;
	private static String rootDir = "/Users/derekzhangv/Develop";
	
	@Before
	public void setUp() {
		this.sampleDir = "/sampling";// +System.currentTimeMillis();
		this.tempDir = "/temptest";// +System.currentTimeMillis();
		this.modelDir = "/sampling-models";
		tm.setSampleDir(rootDir + sampleDir);
		target = new Recommender();
		target.setSampleDir(rootDir+sampleDir);
		target.setTempDir(rootDir + tempDir);
	}
	
	@Test
	public void testSimilarUsers() { //基于旧数据格式
		String topicId = "testRec";
		
		//数据来自mahout in action
		long[] userIds = new long[] {1, 2, 3, 4,5};
        Double[][] prefs = new Double[][] {
        		 {5.0, 3.0 , 2.5 , null, null, null, null},
                 {2.0, 2.5 , 5.0 ,  2.0, null, null, null},
                 {2.5, null, null,  4.0, 4.5 , null, 5.0 },
                 {5.0, null, 3.0 ,  4.5, null, 4.0 , null},
                 {4.0, 3.0 , 2.0 ,  4.0, 3.5 , 4.0 , null},
        };
		
        Sample sample = null;
		for(int i=0;i<userIds.length;i++){
			sample = new Sample();
			sample.setId(userIds[i]+"");
			Double[] uprefs = prefs[i];
			StringBuilder sb = new StringBuilder();
			for(int j=0;j<uprefs.length;j++){
				sb.append(uprefs[j]).append(',');
			}
			sample.setText(sb.substring(0, sb.length()-1));
			
		}
		
		try {
			List<RecommendedItem> result = target.userBased(topicId, 1, 4);
		} catch (RecommendException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Test
	public void testRecommend() {
		String topicId = "testRec2";
		String[] prefs = new String[]{
			"1,1,5",
	        "1,2,5",
	        "1,3,2",
	        //"1,4,1", //add one
	        "2,1,2",
	        "2,3,3",
	        "2,4,5",
	        "3,2,5",
	        "3,4,3",
	        "4,1,3",
	        "4,4,5"};
		
        Sample sample = new Sample();
        sample.setId("prefs");
        StringBuilder sb = new StringBuilder();
		for(int i=0;i<prefs.length;i++){
			sb.append(prefs[i]).append('\n');
		}
		sample.setText(sb.toString());
		try {
			tm.addSample(topicId , sample );
		} catch (SampleException e) {
			e.printStackTrace();
		}
		
		try {
			List<RecommendedItem> result = target.recommend(topicId, 1L, 10);
			System.out.println(result);
		} catch (RecommendException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRecommendAd() {
		try {
			String topic = "adrecom";
			long userId = 89080192;
			String inputFile = target.getSampleDir() + "/" + topic + "/prefs.csv";
			try {
				ArrayList<Long> ids = new ArrayList<Long>();
				Scanner sc = new Scanner(new File(inputFile));
				while(sc.hasNextLine()){
					String[] ss = sc.nextLine().split(",");
					if(ss[0].equals(userId+"")){ 
						System.out.println(ss[1]);
						ids.add(Long.parseLong(ss[1]));
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			System.out.println("=====");
			List<RecommendedItem> result = target.recommend(topic, userId, 20);
			for(RecommendedItem ri:result){
				System.out.println(ri.getItemID());
			}
		} catch (RecommendException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRecommendHuge() { //模拟大数据量并benchmarking：随机生成3万item，1万user，随机生成pref ，每人有1~5各pref
		String topicId = "testRec3";
		int userCount = 10;
		int itemCount = 20;
		int maxPref = 10;
		Random r = new Random();
		ArrayList prefs = new ArrayList();
		for(int i = 0; i<userCount; i++){
			for(int j = 0; j<r.nextInt(maxPref);j++){
				int tmpItem = r.nextInt(itemCount);
				prefs.add(i+","+tmpItem+",1");
			}
		}
		
        Sample sample = new Sample();
        sample.setId("prefs");
        StringBuilder sb = new StringBuilder();
		for(int i=0;i<prefs.size();i++){
			sb.append(prefs.get(i)).append('\n');
		}
		sample.setText(sb.toString());
		try {
			tm.addSample(topicId , sample );
		} catch (SampleException e) {
			e.printStackTrace();
		}
		
		try {
			List<RecommendedItem> result  = null;
			for(int k=0;k<userCount;k++){
				result = target.recommend(topicId, (long)k, 20);
				System.out.println(result);
			}
		} catch (RecommendException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEvaluate(){
		String topicId = "testRec3";
		target.evaluate(topicId);
	}

}
