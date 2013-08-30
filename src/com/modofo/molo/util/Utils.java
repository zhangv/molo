package com.modofo.molo.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.utils.SequenceFileDumper;


public class Utils {

	public static void deleteDir(File f){
		if(f.isDirectory()){
			for (File ff : f.listFiles()) {
				deleteDir(ff);
			}
		}
		f.delete();
	}
	public static void mkDir(String dir){
		File d = new File(dir);
		if(!d.exists()) d.mkdir();
	}
	
	public static void vectorDump(Configuration conf,String input) {
		try {
			ToolRunner.run(conf, new VectorDumper2(), new String[] { "-i",
					input, "-o", input + "-vdump" });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sequenceDump(Configuration conf,String input) {
		try {
			ToolRunner.run(conf, new SequenceFileDumper(), new String[] {
					"-i", input, "-o", input + "-sdump" });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void cleanDir(String topicSampleDir) {
		File f = new File(topicSampleDir);
		if(!f.isDirectory()) return;
		for(File ff:f.listFiles(new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				return ".DS_Store".equalsIgnoreCase(pathname.getName());
			}
		})){
			ff.delete();
		}
	}
	
	public static LinkedHashMap<String, Integer> sortByValue(final Map<String,Integer> m){
		List<String> keys = new ArrayList(m.keySet());
		Collections.sort(keys, new Comparator(){
			@Override
			public int compare(Object arg0, Object arg1) {
				return m.get(arg1) - m.get(arg0); 
			}
		});
		LinkedHashMap<String,Integer> result = new LinkedHashMap<String,Integer>();
		for(String k:keys){
			result.put(k, m.get(k));
		} 
		return result;
	}
	
	public static LinkedHashMap<String, Double> sortByValueD(final Map<String,Double> m,final boolean desc){
		List<String> keys = new ArrayList(m.keySet());
		Collections.sort(keys, new Comparator(){
			@Override
			public int compare(Object arg0, Object arg1) {
				if(desc) return ((m.get(arg1) - m.get(arg0))>0)?1:-1;
				return ((m.get(arg0) - m.get(arg1))>0)?1:-1;
			}
		});
		LinkedHashMap<String,Double> result = new LinkedHashMap<String,Double>();
		for(String k:keys){
			result.put(k, m.get(k));
		} 
		return result;
	}

}
