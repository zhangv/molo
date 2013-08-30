package com.modofo.molo.model;

/**
 * 样本
 * @author Zhang Wei
 */
public class Sample {
	//private SampleType type;
	private String id = null;
	private String text = null;
	private String clazz = null;
	
	public Sample(){}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getClazz() {
		return clazz;
	}
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
//	public SampleType getType() {
//		return type;
//	}
//	public void setType(SampleType type) {
//		this.type = type;
//	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
}
