package com.modofo.molo.classify;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.mahout.classifier.sgd.TrainLogistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SGDClassifier {
	private static final Logger log = LoggerFactory.getLogger(SGDClassifier.class);
	private String sampleDir = "/Users/derekzhangv/Develop/sampling";
	private String tempDir = "/Users/derekzhangv/Develop/temptest";
	private String modelDir = "/Users/derekzhangv/Develop/sampling-models";
	public void train(String topicId) throws IOException{
		StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw, true);
	    String inputFile = sampleDir + "/" + topicId +"/input.csv";
	    String outputFile = modelDir + "/" + topicId+"/model";
	    File outputFileObj = new File(outputFile);
	    if(!outputFileObj.exists()) outputFileObj.createNewFile();
	    String targetVariable = "lastoperation"; //FIXME provide by outside
	    String predictors = "emailhost,titlelen"; //FIXME
	    String categories = "2"; //FIXME
		try {
			TrainLogistic.mainToOutput(new String[]{
			    "--input", inputFile,
			    "--output", outputFile ,
			    "--target", targetVariable, "--categories", categories,
			    "--predictors", "emailhost", "titlelen",
			    "--types", "numeric",
			    "--features", "20",
			    "--passes", "100",
			    "--rate", "50"
			}, pw);
			String trainOut = sw.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
	public void run(String topicId) {
//		RunLogistic rl = new RunLogistic(); //RunLogistic是在0.8才出来的，在0.7需要自己动手
		
		
	}
}
