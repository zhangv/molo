package com.modofo.molo.model;

public enum SampleType {
	NaiveBayes("naive bayes");
	private String description;
	private SampleType(String description){
		this.description = description;
	}
	
	public String description(){
		return this.description;
	}
	
}
