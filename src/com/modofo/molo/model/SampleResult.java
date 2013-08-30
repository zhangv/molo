package com.modofo.molo.model;

/**
 * 样本提交结果
 * @author Zhang Wei
 */
public class SampleResult {
	private Sample original;
	private String status;
	private String errorMessage;
	
	public Sample getOriginal() {
		return original;
	}
	public void setOriginal(Sample original) {
		this.original = original;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
