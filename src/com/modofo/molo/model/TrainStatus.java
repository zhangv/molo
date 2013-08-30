package com.modofo.molo.model;

/**
 * 训练进度
 * @author Zhang Wei
 */
public enum TrainStatus {
	WAITING("waiting"), STARTED("started"), COMPLETED("completed");
	private String topicId;
	private String stage;
	private String description;
	
	private TrainStatus(String desc){
		this.description = desc;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTopicId() {
		return topicId;
	}
	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}
	public String getStage() {
		return stage;
	}
	public void setStage(String stage) {
		this.stage = stage;
	}
	
}
