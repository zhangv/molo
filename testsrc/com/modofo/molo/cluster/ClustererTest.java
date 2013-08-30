package com.modofo.molo.cluster;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.modofo.molo.cluster.Clusterer;

public class ClustererTest {

	private static String rootDir = "/Users/derekzhangv/Develop";
	private Clusterer target;
	@Before
	public void setUp() {
		target = new Clusterer();
		String tempDir = "/temptest";// +System.currentTimeMillis();
		target.setTempDir(rootDir + tempDir);
	}
	
	@Test
	public void testCluster() {
		
	}

	@Test
	public void testRun() {
		target.run("gongzuo-ktv");
		target.dump("gongzuo-ktv","0");
	}

}
