package com.baixing.molo.classify;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class SgdTrainerTest {

	@Test
	public void testTrain() {
		SGDClassifier st = new SGDClassifier();
		try {
			st.train("testSgd");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
