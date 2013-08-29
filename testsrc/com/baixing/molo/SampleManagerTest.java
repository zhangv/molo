package com.baixing.molo;

import static org.junit.Assert.*;

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

public class SampleManagerTest {
	private SampleManager target = new SampleManager();
	private TopicManager topicManager = new TopicManager();
	private String sampleDir, tempDir,modelDir;
	private static String rootDir = "/Users/derekzhangv/Develop";

	@Before
	public void setUp() {
		this.sampleDir = "/sampling";// +System.currentTimeMillis();
		this.tempDir = "/temptest";// +System.currentTimeMillis();
		this.modelDir = "/sampling-models";
		target.setSampleDir(rootDir + sampleDir);
		topicManager.setSampleDir(rootDir+sampleDir);
//		SampleManager sampler = new SampleManager(rootDir+sampleDir);
//		target.setSampleManager(sampler);
	}

	@After
	public void tearDown() {
		File f = new File(this.sampleDir);
		f.deleteOnExit();
		f = new File(this.tempDir);
		f.deleteOnExit();
	}
	@Test
	public void testUpdateSample() throws FileNotFoundException,
			SampleException {
		//target.clear("secondaryCategory");
		File dir = new File("/Users/derekzhangv/Documents/sample/category");
		int index = 0;
		for (File f : dir.listFiles()) {
			Scanner sc = new Scanner(f);
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if ("".equals(line.trim()))
					continue;
				Sample s = new Sample();
				s.setId("" + index++);
				String clname = f.getName().substring(
						f.getName().indexOf(".") + 1);
				clname = clname.substring(0, clname.indexOf("."));
				s.setClazz(clname);
				s.setText(line);
				target.addSample("secondaryCategory", s);
			}
		}

	}

	@Test
	public void testAddSample() {
		Topic sampleTopic = new Topic();
		String name = "test" + System.currentTimeMillis();
		sampleTopic.setName(name);
		try {
			topicManager.createTopic(sampleTopic);
		} catch (SampleException e) {
			fail(e.toString());
		}
		Sample sample = new Sample();
		sample.setId("sampleAdded1");
		sample.setClazz("a");
		sample.setText("sample1a");
		try {
			target.addSample(name, sample);
		} catch (SampleException e) {
			fail(e.toString());
		}

		sampleTopic = topicManager.getTopic(name);
		List<SampleClass> scls = sampleTopic.getSampleClasses();
		assertEquals(1, scls.size());
		assertEquals("a", scls.get(0).getName());
	}

	@Test
	public void testDeleteSample() {
		fail("Not yet implemented");
	}

	@Test
	public void testRetrieveSample() {
		fail("Not yet implemented");
	}

}
