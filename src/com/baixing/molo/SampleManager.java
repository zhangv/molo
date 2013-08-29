package com.baixing.molo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import org.apache.commons.lang.ArrayUtils;
import org.jfree.util.Log;

import com.baixing.molo.model.Sample;
import com.baixing.molo.util.Combinations;
import com.baixing.molo.util.Utils;

/**
 * 样本管理器
 * @author zhangwei
 *
 */
public class SampleManager {
	
	private String sampleDir;
	private ArrayList attributeSet;
	public SampleManager(){}
	public SampleManager(String sampleDir){
		this.sampleDir = sampleDir;
	}
	/**
	 * 添加单个样本
	 * 
	 * @param topicId
	 * @param sample
	 * @throws SampleException
	 */
	public void addSample(String sampleTopicId, Sample sample)
			throws SampleException {
		File topicDir = new File(this.sampleDir + "/" + sampleTopicId);
		if (!topicDir.exists())
			topicDir.mkdir();
		String clazzPath = topicDir.getAbsolutePath();
		if(sample.getClazz() != null) clazzPath += ("/"+sample.getClazz());
		File classDir = new File(clazzPath);
		
		if (!classDir.exists())
			classDir.mkdir();
		File tmp = new File(classDir.getAbsolutePath() + "/" + sample.getId());
		Writer output = null;
		try {
			output = new BufferedWriter(new FileWriter(tmp));
			output.write(sample.getText());
		} catch (IOException e) {
			e.printStackTrace();
				throw new SampleException(e.toString());
		} finally {
			if (null != output)
				try {
					output.close();
				} catch (IOException e) {
				}
		}
	}
	
	public void convertFromCsv(String topicId) throws Exception{ //把sample文件夹下所有文件按照csv进行处理生成对应的文件夹
		File topicDir = new File(this.sampleDir + "/" + topicId);
		Scanner sc = null;
		int i = 0;
		int attrCount = 0;
		for(File f:topicDir.listFiles()){
			if(f.getName().equalsIgnoreCase(".DS_Store")) continue;
			if(f.isDirectory()) continue;
			sc = new Scanner(f);
			System.out.println(f.getAbsolutePath());
			while(sc.hasNextLine()){
				String s = sc.nextLine();
				String[] ss = s.split(",");
				attrCount = ss.length;
				for(int m = 0;m<ss.length;m++){//属性个数
					for(int j = 0;j<ss.length;j++){
						for(int k = j;k<m;k++){
							
						}
					}
				}
				Sample sample = new Sample();
				sample.setId(""+i++);
				if(ss[ss.length-1].equals(""))
					sample.setClazz("null");
//				else{
//					if(ss[ss.length-1].equals("good")) sample.setClazz(ss[ss.length-1]);
//					else sample.setClazz("bad");
//				}
				sample.setClazz(ss[ss.length-1]);
				sample.setText(s.substring(0,s.lastIndexOf(",")-1));
				this.addSample(topicId, sample);
			}
			sc.close();
		}
		//clean the sample dir which has less than 5 samples
		final int minSampleCount = 10;
		for(File f:topicDir.listFiles(new FileFilter(){
			@Override
			public boolean accept(File arg0) {
				if(!arg0.isDirectory()) return false;
				File[] children = arg0.listFiles();
				if(children.length < minSampleCount) return true;
				else return false;
			}
		})){
			Utils.deleteDir(f);
			Log.info("Deleting the sample dir - " + f.getName() + ",as the total sample count is less than " + minSampleCount);
		}
	}
	
	public void generateSampleSet(String topicId) throws Exception{ //把sample文件夹下所有文件按照csv进行处理生成对应的文件夹,同时按照不同的属性组合进行分组，进行大规模分析
		File topicDir = new File(this.sampleDir + "/@" + topicId);
		Scanner sc = null;
		int i = 0;
		int attrCount = 0;
		for(File f:topicDir.listFiles()){
			if(f.getName().equalsIgnoreCase(".DS_Store")) continue;
			if(f.isDirectory()) continue;
			sc = new Scanner(f);
			System.out.println(f.getAbsolutePath());
			while(sc.hasNextLine()){
				String s = sc.nextLine();
				String[] ss = s.split(",");
				attrCount = ss.length;
				ArrayList attrSets = this.getAttrSet(attrCount);
				for(Object attrSet:attrSets){
					ArrayList as = (ArrayList) attrSet;
					if(as.size() != 5) continue;
					Sample sample = new Sample();
					sample.setId(""+i);
					sample.setClazz(ss[ss.length-1]);
					StringBuilder text = new StringBuilder();
					StringBuilder index = new StringBuilder();
					for(Object attr:as){
						Integer attrname = (Integer) attr;
						text.append(ss[attrname]).append(',');
						index.append("-").append(attrname);
					}
					sample.setText(text.toString());
					System.out.println(index.toString());
					this.addSample(topicId+index.toString(), sample);
				}
				i++;
			}
			sc.close();
		}
		//clean the sample dir which has less than 5 samples
		final int minSampleCount = 10;
		for(File f:topicDir.listFiles(new FileFilter(){
			@Override
			public boolean accept(File arg0) {
				if(!arg0.isDirectory()) return false;
				File[] children = arg0.listFiles();
				if(children.length < minSampleCount) return true;
				else return false;
			}
		})){
			Utils.deleteDir(f);
			Log.info("Deleting the sample dir - " + f.getName() + ",as the total sample count is less than " + minSampleCount);
		}
	}
	
	private ArrayList getAttrSet(int size){
		if(attributeSet != null) return this.attributeSet;
		ArrayList indexList = new ArrayList();
		Combinations comb = new Combinations();
		for(int k = 0;k<size;k++){
			indexList.add(k);
		}
		attributeSet = comb.getCombs(indexList);
		return this.attributeSet;
	}

	/**
	 * 删除样本
	 * @param topicId
	 * @param sampleId
	 */
	public void deleteSample(String topicId, String sampleId) {
		// TODO 删除样本
	}
	/**
	 * 更新样本
	 * @param topicId
	 * @param sampleId
	 */
	public void updateSample(String topicId, String sampleId) {
		// TODO 更新样本
	}
	/**
	 * 获取样本
	 * @param topicId
	 * @param sampleId
	 */
	public void retrieveSample(String topicId, String sampleId) {
		// TODO 获取样本
	}


	public String getSampleDir() {
		return sampleDir;
	}

	public void setSampleDir(String sampleDir) {
		this.sampleDir = sampleDir;
	}
}
