package com.modofo.molo.classify;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.mahout.classifier.df.DecisionForest;
import org.apache.mahout.classifier.df.builder.DecisionTreeBuilder;
import org.apache.mahout.classifier.df.data.Data;
import org.apache.mahout.classifier.df.data.DataConverter;
import org.apache.mahout.classifier.df.data.DataLoader;
import org.apache.mahout.classifier.df.data.Dataset;
import org.apache.mahout.classifier.df.data.DescriptorException;
import org.apache.mahout.classifier.df.data.Instance;
import org.apache.mahout.classifier.df.node.Node;

import com.google.common.collect.Lists;
import com.modofo.molo.util.Utils;

public class DFClassifier {
	
	private String sampleDir;
	private String modelDir;
	private String treeFilename = "trees.txt";
	private Random rng = new Random();

	public double classify(String topicId,String instanceStr,String descriptor) throws DescriptorException, FileNotFoundException {
		Configuration config = new Configuration();
		Path path = new Path(modelDir + "/" + topicId+"/" + treeFilename);
		DecisionForest forest = null;
		try{
			forest = DecisionForest.load(config, path);
		}catch(Exception e){}
		Data[] datas = generateTrainingData(topicId,descriptor);
		String[] instanceArr = instanceStr.split(",");
		Dataset dataset = datas[0].getDataset();
		
		DataConverter converter = new DataConverter(datas[0].getDataset());
		Instance instance = converter.convert(instanceStr);
		return forest.classify(dataset,rng,instance);
	}

	public DecisionForest train(String topicId,String descriptor) throws DescriptorException, IOException{
		Data[] datas = generateTrainingData(topicId,descriptor);
		// Build Forest
		DecisionForest forest = buildForest(datas);
		saveTree(forest,topicId);
		return forest;
	}
	private Data[] generateTrainingData(String topicId,String descriptor) throws DescriptorException,
			FileNotFoundException {
		// Dataset
		Scanner sc = new Scanner(new File(this.sampleDir+"/"+topicId+"/input.csv"));
		ArrayList<String> input = new ArrayList<String>();
		while (sc.hasNextLine()) {
			String s = sc.nextLine();
			if(s.startsWith("#")) continue; //#开头的直接skip掉
			String[] tmp = s.split(",");
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < tmp.length ; i++) {
				sb.append(tmp[i]).append(",");
			}
			input.add(sb.toString());
		}

		Dataset dataset = DataLoader.generateDataset(
				descriptor//"N N N N N N N N N N N N N L"
				, false,
				input.toArray(new String[input.size()]));

		// Training data
		Data data = DataLoader.loadData(dataset,
				input.toArray(new String[input.size()]));
		@SuppressWarnings("unchecked")
		List<Instance>[] instances = new List[3];
		for (int i = 0; i < instances.length; i++) {
			instances[i] = Lists.newArrayList();
		}
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).get(0) == 0.0d) {
				instances[0].add(data.get(i));
			} else {
				instances[1].add(data.get(i));
			}
		}
		Data[] datas = new Data[instances.length];
		for (int i = 0; i < datas.length; i++) {
			datas[i] = new Data(dataset, instances[i]);
		}

		return datas;
	}

	private DecisionForest buildForest(Data[] datas) {
		List<Node> trees = Lists.newArrayList();
		for (Data data : datas) {
			// build tree
			DecisionTreeBuilder builder = new DecisionTreeBuilder();
			builder.setM(data.getDataset().nbAttributes() - 1);
			builder.setMinSplitNum(0);
			builder.setComplemented(false);
			Node node = builder.build(rng, data);
			System.out.println(node);
			trees.add(node);
		}
		return new DecisionForest(trees);
	}

	private void saveTree(DecisionForest dt,String topicId) throws IOException {
		Utils.mkDir(modelDir + "/" + topicId);
		String path = modelDir + "/" + topicId+"/" + treeFilename;
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(path));
		dt.write(dos);
	}

	public String getSampleDir() {
		return sampleDir;
	}

	public void setSampleDir(String sampleDir) {
		this.sampleDir = sampleDir;
	}

	public String getModelDir() {
		return modelDir;
	}

	public void setModelDir(String modelDir) {
		this.modelDir = modelDir;
	}
}
