package com.baixing.molo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.baixing.molo.model.Sample;
import com.baixing.molo.model.SampleClass;
import com.baixing.molo.model.Topic;

public class TopicManagerTest {
	private TopicManager target = new TopicManager();
	private String sampleDir, tempDir,modelDir;
	private static String rootDir = "/Users/derekzhangv/Develop";

	@Before
	public void setUp() {
		this.sampleDir = "/sampling";// +System.currentTimeMillis();
		this.tempDir = "/temptest";// +System.currentTimeMillis();
		this.modelDir = "/sampling-models";
		target.setSampleDir(rootDir + sampleDir);
		target.setTempDir(rootDir + tempDir);
		SampleManager sampler = new SampleManager(rootDir+sampleDir);
		target.setSampleManager(sampler);
	}

	@After
	public void tearDown() {
		File f = new File(this.sampleDir);
		f.deleteOnExit();
		f = new File(this.tempDir);
		f.deleteOnExit();
	}

	@Test
	public void testCreateTopic() {
		Topic sampleTopic = new Topic();
		String name = "test" + System.currentTimeMillis();
		sampleTopic.setName(name);
		try {
			target.createTopic(sampleTopic);
		} catch (SampleException e) {
			fail(e.toString());
		}
		assertTrue(target.existsTopic(name));
		Topic st = target.getTopic(name);
		assertNotNull(st);
		assertEquals(name, st.getName());
	}

}
