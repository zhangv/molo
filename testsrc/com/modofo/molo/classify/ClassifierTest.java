package com.modofo.molo.classify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.mahout.classifier.ConfusionMatrix;
import org.junit.Before;
import org.junit.Test;

import com.modofo.molo.classify.Classifier;
import com.modofo.molo.classify.ClassifyException;
import com.modofo.molo.model.ClassifyResult;
import com.modofo.molo.util.Utils;

public class ClassifierTest {
	private Classifier target = new Classifier();
	private String tempDir,modelDir;
	private static String rootDir = "/Users/derekzhangv/Develop";

	@Before
	public void setUp() {
		this.tempDir = "/temptest";// +System.currentTimeMillis();
		this.modelDir = "/sampling-models";
		target.setTempDir(rootDir + tempDir);
		target.setModelDir(rootDir+modelDir);
	}

	@Test
	public void testCategory() {
		try {
			String topic = "secondaryCategory";
			// target.train(topic);
			List<ClassifyResult> result = null;
			result = target.classify(topic, "本人有一辆二手马自达6出售,车况不错，去年才买的，价格10万",1);
			System.out.println(result.get(0).getLabel());
			result = target.classify(topic, "二手房出租，面积68平米，租金300/月",1);
			System.out.println(result.get(0).getLabel());
			result = target.classify(topic, "专业通下水道，电话34342525",1);
			System.out.println(result.get(0).getLabel());
			result = target.classify(topic, "我有一只狗狗",1);
			System.out.println(result.get(0).getLabel());
		} catch (ClassifyException e) {
			fail(e.toString());
			e.printStackTrace();
		}
	}
	
	@Test
	public void testClassify() {
		try {
			List<ClassifyResult> result = null;
			result = target.classify("testTopic", "二手电冰箱",1);
			assertEquals("good",result.get(0).getLabel());
			result = target.classify("testTopic", "世界",1);
			assertEquals("good",result.get(0).getLabel());
			result = target.classify("testTopic", "asdf",1);
			assertEquals("bad",result.get(0).getLabel());
		} catch (ClassifyException e) {
			fail(e.toString());
			e.printStackTrace();
		}
	}

	@Test
	public void testClassifyF(){
		try {
			List<ClassifyResult> result = target.classify("testFixed", "1,0,2,1,2,0,1","",5);
			System.out.println(result.get(0).getLabel());
			 result = target.classify("testFixed", "1,0,2,1,1,0,1","",5);
			assertEquals("good",result.get(0).getLabel());
			 result = target.classify("testFixed", "1,0,4,1,1,1,1","",5);
			assertEquals("bad",result.get(0).getLabel());
		} catch (ClassifyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testClassifyQitazhuanrang() throws ClassifyException, FileNotFoundException{
		Collection<String> labels = target.labels("ershou").values();
		File dir = new File(this.rootDir+"/sampling/nick");
		for(File sdir:dir.listFiles(new FileFilter(){
			public boolean accept(File arg0) {
				return arg0.isDirectory();
			}
		})){
			
			//ConfusionMatrix cm = new ConfusionMatrix(labels,"");
			HashMap<String,Integer> map = new HashMap<String,Integer>();
			for(String lb:labels){
				map.put(lb, 0);
			}
			System.out.println(sdir.getName());
			for(File f:sdir.listFiles()){
				Scanner sc = new Scanner(f);
				StringBuilder text = new StringBuilder(); 
				while(sc.hasNextLine()){
					text.append(" ").append(sc.nextLine());
				}
				List<ClassifyResult> results = target.classify("ershou", text.toString(), "text", 1);
				//cm.addInstance("ershou_qitazhuanrang", results.get(0).getLabel());
				String lb = results.get(0).getLabel();
				map.put(lb, map.get(lb)+1);
			}
			for(String lb:labels){
				System.out.print(lb+",");
			}
			System.out.println();
			for(String lb:labels){
				System.out.print(map.get(lb)+",");
			}
			System.out.println();
			//System.out.println(cm.getMatrix().getColumnLabelBindings());
			//System.out.println(cm.getMatrix().viewRow(qitazhuanrangIndex));
		}
		
	}

	@Test
	public void testAccuracy() {
		HashMap<String, Double> d = target.accuracy("ershou",10);
		
		for(String s:d.keySet()){
			System.out.println(s+","+d.get(s));
		}
		
		//LinkedHashMap<String,Double> dd = Utils.sortByValueD(d, false);
		
		//System.out.println(dd);
		//System.out.println(target.accuracy("ershou"));
	}

	@Test
	public void testConfusionMatrix() {
		String d = target.confusionmatrix("ershou").toString();
		System.out.println(d);
	}
	
	@Test
	public void testLabels() {
		Map<Integer, String> d = target.labels("secondlevelcategory");
		System.out.println(d);
	}
	
	@Test
	public void testDna() {
		Map<String, Integer> d = target.dna("ershou","ershou_bijiben",1);
//		Map<String, Integer> d = target.dna("cheliang","cheliang_ershouqiche");
		System.out.println(d);
//		Map<String, Integer> dd = target.dna("secondlevelcategory","fang");
//		System.out.println(dd);
	}
	
	@Test
	public void testTopWords() {
		Map<String, List<String>> d = target.topWords("gongzuo",50);
		for(String s:d.keySet()){
			System.out.println(d.get(s));
			System.out.println();
		}
//		Map<String, Integer> d = target.dna("cheliang","cheliang_ershouqiche");
		System.out.println(d);
//		Map<String, Integer> dd = target.dna("secondlevelcategory","fang");
//		System.out.println(dd);
	}

}
