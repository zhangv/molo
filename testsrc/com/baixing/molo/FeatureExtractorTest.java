package com.baixing.molo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.baixing.molo.util.FeatureExtractor;

public class FeatureExtractorTest {

	@Test
	public void testExtract() {
		FeatureExtractor fe = new FeatureExtractor();
		assertEquals("吴奇隆 医院 胸 ",fe.extract("吴奇隆胸医院", ""));
	}

}
