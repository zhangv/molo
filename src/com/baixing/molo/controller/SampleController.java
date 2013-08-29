package com.baixing.molo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baixing.molo.SampleException;
import com.baixing.molo.SampleManager;
import com.baixing.molo.TopicManager;
import com.baixing.molo.model.Sample;
import com.baixing.molo.model.SampleResult;
import com.baixing.molo.model.Topic;
import com.baixing.molo.model.TrainResult;

@Controller
@RequestMapping("/sample")
public class SampleController {
	
	@Autowired
	private SampleManager sampleManager;
	
	/**
	 * 删除样本
	 * @param sampleId
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/{topicId}/{sampleId}", method = RequestMethod.DELETE)
	//@RequestMapping(value="/{topicId}/delete", method=RequestMethod.GET)
	public @ResponseBody TrainResult deleteTopic(@PathVariable String topicId,@PathVariable String sampleId){
		TrainResult result = new TrainResult();
		sampleManager.deleteSample(topicId,sampleId);
		return result;
	}
	
	/**
	 * 提交样本
	 * @param sampleId
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/{topicId}", method = RequestMethod.POST)
	public @ResponseBody SampleResult createSample(@PathVariable String topicId,Sample sample){
		SampleResult result2 = new SampleResult();
		try {
			sampleManager.addSample(topicId,sample);
			result2.setStatus("OK");
		} catch (SampleException e) {
			result2.setStatus("FAIL");
		}
		return result2; 
	}
}