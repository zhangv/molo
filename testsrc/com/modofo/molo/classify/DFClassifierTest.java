package com.modofo.molo.classify;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.modofo.molo.classify.DFClassifier;

public class DFClassifierTest {
	private DFClassifier target = new DFClassifier();
	private String sampleDir,modelDir;
	private static String rootDir = "/Users/derekzhangv/Develop";

	@Before
	public void setUp() {
		this.sampleDir = "/sampling";// +System.currentTimeMillis();
		this.modelDir = "/sampling-models";
		target.setSampleDir(rootDir + sampleDir);
		target.setModelDir(rootDir+modelDir);
	}
	
	@Test
	public void testClassify() {
		try{
			target.train("testDf", "N N N N N N N N N N N N N L");
			double d = target.classify("testDf", "1,18,1,4,37583,60457,37583,713,0,1,24273,38557,0,-", "N N N N N N N N N N N N N L");
			System.out.println(d);
			d = target.classify("testDf", "1,0,0,4,7965,7965,52756,110,0,1,24273,46169,0,-", "N N N N N N N N N N N N N L");
			System.out.println(d);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
