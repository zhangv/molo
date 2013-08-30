package com.modofo.molo.controller;

import java.util.List;

import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.modofo.molo.recommend.RecommendException;
import com.modofo.molo.recommend.Recommender;

@Controller
@RequestMapping(value={"/recommend", "/recommender"})
public class RecommenderController {
	@Autowired
	private Recommender recommender;
	
	@RequestMapping(value="/{topicId}/userbased/{userId}/{howmany}", method = RequestMethod.GET)
    public @ResponseBody List<RecommendedItem> recommend(@PathVariable String topicId,@PathVariable Long userId,@PathVariable Integer howmany) {
		try {
			return recommender.recommend(topicId, userId, howmany);
		} catch (RecommendException e) {
			e.printStackTrace();
		}
		return null;
    }

	@RequestMapping(value="/{topicId}/itembased/{itemId}/{howmany}", method = RequestMethod.GET)
    public @ResponseBody String itembased(@PathVariable String topicId,@PathVariable Long itemId,@PathVariable Integer howmany) {
		return "1";
    }
	
	//获取item-based的原因
	@RequestMapping(value="/{topicId}/because/{userId}/{itemId}/{howmany}", method = RequestMethod.GET)
    public @ResponseBody String because(@PathVariable String topicId,@PathVariable Long userId,@PathVariable Long itemId,@PathVariable Integer howmany) {
		return "1";
    }
	//提交item的similarity用于item-based
	@RequestMapping(value="/{topicId}/similarity", method = RequestMethod.POST)
    public @ResponseBody String similarity(@PathVariable String topicId) {
		return "1";
    }
}
