package com.baixing.molo.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 学习主题
 * @author Zhang Wei
 */
public class Topic {
	private String name;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private List<SampleClass> sampleClasses = new ArrayList<SampleClass>();

	public List<SampleClass> getSampleClasses() {
		return sampleClasses;
	}

	public void setSampleClasses(List<SampleClass> sampleClasses) {
		this.sampleClasses = sampleClasses;
	}
	
	public void addClass(SampleClass sampleClass){
		this.sampleClasses.add(sampleClass);
	}
	
}
