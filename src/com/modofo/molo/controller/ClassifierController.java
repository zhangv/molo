package com.modofo.molo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.modofo.molo.classify.Classifier;
import com.modofo.molo.classify.ClassifyException;
import com.modofo.molo.classify.TrainException;
import com.modofo.molo.classify.Trainer;
import com.modofo.molo.model.ClassifyResult;
import com.modofo.molo.model.TrainResult;
import com.modofo.molo.model.TrainStatus;

@Controller
@RequestMapping(value={"/classify", "/classifier"})
public class ClassifierController {
	
	@Autowired
	private Trainer trainer;
	@Autowired
	private Classifier classifier;
	/**
	 * 训练
	 * @param sampleId
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/{topicId}/train/{method}", method = RequestMethod.GET)
	public @ResponseBody TrainResult train(@PathVariable String topicId,@PathVariable String method){
		TrainResult result = new TrainResult();
		TrainStatus status = trainer.status(topicId);
		if(status == TrainStatus.STARTED) {
			result.setStatus("FAIL");
		}else{
			try {
				if("text".equals(method)) {
					trainer.train(topicId);
				}else trainer.trainF(topicId);
			} catch (TrainException e) {
				e.printStackTrace();
			}
			result.setStatus("OK");
		}
		return result;
	}
	
	/**
	 * 分类
	 * @param sampleId
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/{topicId}/classify/{text}", method = RequestMethod.GET)
	public @ResponseBody List<ClassifyResult> classify(@PathVariable String topicId,@PathVariable String text){
		List<ClassifyResult> result = null;
		try {
			 result = classifier.classify(topicId,text,10);
		} catch (ClassifyException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 训练状态
	 * @param sampleId
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/{topicId}/status", method = RequestMethod.GET)
	public @ResponseBody TrainStatus status(@PathVariable String topicId){
		TrainStatus status = trainer.status(topicId);
		return status;
	}
	
	/**
	 * 清空样本
	 * @param sampleId
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/{topicId}/clear", method = RequestMethod.GET)
	public @ResponseBody TrainResult clear(@PathVariable String topicId){
		TrainResult result = new TrainResult();
		result = trainer.clear(topicId);
		return result;
	}
	
	/**
	 * 获取测试准确率
	 * @param topicId
	 * @return
	 */
	@RequestMapping(value="/{topicId}/accuracy", method = RequestMethod.GET)
	public @ResponseBody Double accuracy(@PathVariable String topicId){
		return classifier.accuracy(topicId);
	}
 
	/**
	 * 获取confusion matrix
	 * @param topicId
	 * @return
	 */
	@RequestMapping(value="/{topicId}/confusionmatrix", method = RequestMethod.GET)
	public @ResponseBody String confusionmatrix(@PathVariable String topicId){
		return classifier.confusionmatrix(topicId).toString();
	}
	
	/**
	 * 获取top关键词
	 * @param topicId
	 * @return
	 */
	@RequestMapping(value="/{topicId}/dna/{classId}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Integer> dna(@PathVariable String topicId,@PathVariable String classId){
		return classifier.dna(topicId,classId,1000);
	}
}