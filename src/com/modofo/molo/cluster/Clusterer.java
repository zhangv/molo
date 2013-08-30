/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.modofo.molo.cluster;

import org.apache.hadoop.conf.Configuration;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.utils.clustering.ClusterDumper;
import org.springframework.beans.factory.annotation.Autowired;

final class Clusterer {
	@Autowired
	private String sampleDir;
	@Autowired
	private String tempDir;
	public void cluster(String topicId) {
		String pointsPath = sampleDir + "/" + topicId +"/points.txt";
		String clustersPath = sampleDir + "/" + topicId +"/clusters.txt";
		String outputDir = tempDir +"/" + topicId;
		KMeansDriver kd = new KMeansDriver();
		String[] args = {"-i", pointsPath,
		          "--clusters", clustersPath,
		          "-o", outputDir,
		          "--distanceMeasure", EuclideanDistanceMeasure.class.getName(),
		          "--convergenceDelta", "0.001",
		          "--maxIter", "2", 
		          "--clustering",
		          "--overwrite",
		          "--method", "sequential" // mapreduce
		};
		
		try {
			kd.run(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run(String topicId){
		String inputDir = this.tempDir + "/" + topicId + "/" + topicId + "-vectors/tfidf-vectors";
		String clusterDir = this.tempDir + "/" + topicId +"-clusters";
		String outputDir = this.tempDir + "/" + topicId +"/cluster" ;
		String[] args = {"-i",inputDir, "-c", clusterDir, "-o", outputDir,
				"-dm", org.apache.mahout.common.distance.CosineDistanceMeasure.class.getName(),
				"-cd", "0.1",
				"-x","10",
				"-k","20",
				"-ow"
		};
		try{
			KMeansDriver kd = new KMeansDriver();
			kd.setConf(new Configuration());
			kd.run(args);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void dump(String topicId,String clusterId){
		String clusterDir = this.tempDir + "/" + topicId + "/cluster/clusters-" + clusterId;
		String dumpDir = this.tempDir + "/" + topicId + "/cluster/clusters-" + clusterId+"-dump";
		String dicDir = this.tempDir+ "/" + topicId + "/" + topicId +"-vectors/dictionary.file-0";
		String[] args = {
				"-i", clusterDir,
				"-o", dumpDir,
				"-d", dicDir,
				"-dt", "sequencefile","-b","100","-n","20"
		};
		ClusterDumper cd = new ClusterDumper();
		cd.setConf(new Configuration());
		try {
			cd.run(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getSampleDir() {
		return sampleDir;
	}

	public void setSampleDir(String sampleDir) {
		this.sampleDir = sampleDir;
	}

	public String getTempDir() {
		return tempDir;
	}

	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}

}
