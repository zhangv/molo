package com.baixing.molo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value={"/cluster", "/clusterer"})
public class ClustererController {

	@RequestMapping(value="/", method = RequestMethod.GET)
    public @ResponseBody String index() {
		return "1";
    }

}
