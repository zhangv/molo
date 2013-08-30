package com.modofo.molo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.modofo.molo.SampleException;
import com.modofo.molo.TopicManager;
import com.modofo.molo.model.Sample;
import com.modofo.molo.model.SampleResult;
import com.modofo.molo.model.Topic;
import com.modofo.molo.model.TrainResult;

@Controller
@RequestMapping("/topic")
public class TopicController {
	
	@Autowired
	private TopicManager topicManager;
	
	/**
	 * 列出所有的sample topic
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/", method = RequestMethod.GET)
    public @ResponseBody List<Topic> index() {
		List<Topic> samples = topicManager.getTopics();
        return samples;
    }
	
	/**
	 * 新建一个sample topic
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/", method = RequestMethod.POST)
    public @ResponseBody SampleResult createTopic(Topic sampleTopic) {
		SampleResult result = new SampleResult();
		if(!topicManager.existsTopic(sampleTopic.getName())){
			try {
				topicManager.createTopic(sampleTopic);
				result.setStatus("OK");
			} catch (SampleException e) {
				result.setErrorMessage(e.toString());
				result.setStatus("FAIL");
			}
		}else{
			result.setStatus("EXISTS");
		}
		return result;
    }
	
	/**
	 * 显示sample topic属性和样本列表
	 * @param sampleId
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/{topicId}", method = RequestMethod.GET)
	public @ResponseBody Topic retrieveTopic(@PathVariable String topicId) {
		Topic sampleTopic = topicManager.getTopic(topicId);
        return sampleTopic;
    }
	
	/**
	 * 修改sample
	 * @param sampleId
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/{topicId}", method = RequestMethod.POST)
	public @ResponseBody SampleResult updateTopic(@PathVariable String topicId, Topic sampleTopic){
		topicManager.updateTopic(sampleTopic);
		SampleResult result = new SampleResult();
		result.setStatus("OK");
		return result;
	}
	
	/**
	 * 删除topic
	 * @param sampleId
	 * @param model
	 * @return
	 */
	//@RequestMapping(value="/{topicId}", method = RequestMethod.DELETE)
	@RequestMapping(value="/{topicId}/delete", method=RequestMethod.GET)
	public @ResponseBody TrainResult deleteTopic(@PathVariable String topicId){
		TrainResult result = new TrainResult();
		topicManager.deleteTopic(topicId);
		return result;
	}

}