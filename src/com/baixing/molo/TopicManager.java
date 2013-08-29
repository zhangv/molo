package com.baixing.molo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.baixing.molo.model.Sample;
import com.baixing.molo.model.SampleClass;
import com.baixing.molo.model.Topic;
import com.baixing.molo.model.TrainResult;
import com.baixing.molo.util.Utils;

/**
 * 主题管理器
 * 对于classifier来说是样本，每个文件可以是text类型（一段原始文本），也可以是vector（csv格式的属性值，目前仅支持属性全部是数值）
 * 对于recommend来说是数据，prefs文件，每行一条数据，格式：[userId],[itemId],[preference value]
 * 
 * @author zhangwei
 *
 */
public class TopicManager {
	private static final Logger log = LoggerFactory
			.getLogger(TopicManager.class);
	@Autowired
	private SampleManager sampleManager;
	private String sampleDir;
	private String tempDir;

	public TopicManager() {
	}

	public String getTempDir() {
		return tempDir;
	}

	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}

	public String getSampleDir() {
		return sampleDir;
	}

	public void setSampleDir(String sampleDir) {
		this.sampleDir = sampleDir;
	}

	/**
	 * 获取主题
	 * 
	 * @param topicId
	 * @return
	 */
	public Topic getTopic(String topicId) {
		File topicDir = new File(this.sampleDir + "/" + topicId);
		if (!topicDir.exists() || !topicDir.isDirectory())
			return null;
		Topic topic = new Topic();
		topic.setName(topicId);
		for (File f : topicDir.listFiles()) {
			SampleClass sclass = new SampleClass();
			sclass.setName(f.getName());
			topic.addClass(sclass);
		}
		return topic;
	}

	/**
	 * 是否存在主題
	 * 
	 * @param name
	 * @return
	 */
	public boolean existsTopic(String name) {
		File topicDir = new File(this.sampleDir);
		if (null == topicDir.listFiles())
			return false;
		boolean exists = false;
		for (File f : topicDir.listFiles()) {
			if (name.equalsIgnoreCase(f.getName())) {
				exists = true;
				break;
			}
		}
		return exists;
	}

	/**
	 * 創建新主題
	 * 
	 * @param sampleTopic
	 * @throws SampleException
	 */
	public void createTopic(Topic sampleTopic) throws SampleException {
		File sampleDir = new File(this.sampleDir);
		if (!sampleDir.exists())
			sampleDir.mkdir();
		File topicDir = new File(this.sampleDir + "/" + sampleTopic.getName());
		if (!topicDir.exists())
			topicDir.mkdir();
		if (null != sampleTopic.getSampleClasses()) {
			for (SampleClass sclass : sampleTopic.getSampleClasses()) {
				for (Sample sample : sclass.getSamples()) {
					sample.setClazz(sclass.getName());
					this.sampleManager.addSample(sampleTopic.getName(), sample);
				}
			}
		}
	}

	/**
	 * 更新主題基本属性（不修改下面的样本） TODO：考虑可能需要在topic下增加个meta文件
	 * 
	 * @param sampleTopic
	 */
	public void updateTopic(Topic sampleTopic) {

	}
	
	/**
	 * 删除主题和样本
	 * @param topicId
	 * @return 
	 */
	public TrainResult deleteTopic(String topicId) {
		File samplef = new File(this.sampleDir +"/" + topicId);
		File tempf = new File(this.tempDir+"/"+topicId);
		TrainResult result = new TrainResult();
		result.setStatus("OK");
		try{
			Utils.deleteDir(samplef);
			Utils.deleteDir(tempf);
		}catch(Exception e){
			result.setStatus("FAIL");
			if(log.isDebugEnabled()) e.printStackTrace();
		}
		return result;
	}
	

	/**
	 * 清空所有样本
	 * 
	 * @param topicId
	 * @return
	 */
	public TrainResult clear(String topicId) {
		File tempf = new File(this.sampleDir+"/"+topicId);
		TrainResult result = new TrainResult();
		result.setStatus("OK");
		try{
			Utils.deleteDir(tempf);
		}catch(Exception e){
			result.setStatus("FAIL");
			if(log.isDebugEnabled()) e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取所有的主题
	 * 
	 * @return
	 */
	public List<Topic> getTopics() {
		File sampleDirFile = new File(this.sampleDir);
		ArrayList<Topic> result = new ArrayList<Topic>();
		Topic stopic = null;
		for (File dir : sampleDirFile.listFiles()) {
			stopic = new Topic();
			stopic.setName(dir.getName());
			ArrayList<SampleClass> sampleClasses = new ArrayList<SampleClass>();
			SampleClass sclass = null;
			for (File clz : dir.listFiles()) {
				sclass = new SampleClass();
				sclass.setName(clz.getName());
				ArrayList<Sample> samples = new ArrayList<Sample>();
				Sample sample = null;
				for (File smp : clz.listFiles()) {
					sample = new Sample();
					sample.setId(smp.getName());
					samples.add(sample);
				}
				sclass.setSamples(samples);
				sampleClasses.add(sclass);
			}
			stopic.setSampleClasses(sampleClasses);
			result.add(stopic);
		}
		return result;
	}

	public SampleManager getSampleManager() {
		return sampleManager;
	}

	public void setSampleManager(SampleManager sampleManager) {
		this.sampleManager = sampleManager;
	}
}
