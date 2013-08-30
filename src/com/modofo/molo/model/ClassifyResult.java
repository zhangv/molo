package com.modofo.molo.model;


public class ClassifyResult {
	private String label;
	private double score;
	private double logLikelihood = Double.MAX_VALUE;
	private String original;
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public double getLogLikelihood() {
		return logLikelihood;
	}
	public void setLogLikelihood(double logLikelihood) {
		this.logLikelihood = logLikelihood;
	}

}
