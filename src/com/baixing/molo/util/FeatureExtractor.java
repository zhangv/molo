package com.baixing.molo.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.baixing.molo.classify.IKSmartAnalyzer;

/**
 * 从文本中获取特征词
 * @author Zhang Wei
 */
public class FeatureExtractor {
	private Analyzer analyzer;
	public FeatureExtractor(){
	}
	public String extract(String content,String classifier){
		HashSet<String> hs = this.extract(content);
		StringBuilder sb = new StringBuilder();
		for(String s:hs){
				sb.append(s).append(' ');
		}
		return sb.toString();
	}
	
	public String split(String content){ //句子分词
		StringReader sreader = new StringReader(content);
		TokenStream stream = analyzer.tokenStream("", sreader);
		try {
			stream.reset();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		TermAttribute termAtt = (TermAttribute) stream
				.addAttribute(TermAttribute.class);
		StringBuilder sb = new StringBuilder();
		try {
			while (stream.incrementToken()) {
				String tm = termAtt.term();
				if(tm.length()>1){ //只考虑超过2个字的词
					sb.append(tm).append(" ");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}finally{
			try{
				stream.close();
				sreader.close();
			}catch(Exception e2){}
		}
		return sb.toString();
	}
	
	public HashMap<String,Integer> extractFreq(String content){
		StringReader sreader = new StringReader(content);
		TokenStream stream = analyzer.tokenStream("", sreader);
		HashMap<String,Integer> map = new HashMap<String,Integer>(); //优化点：去掉重复词
		try {
			stream.reset();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		TermAttribute termAtt = (TermAttribute) stream
				.addAttribute(TermAttribute.class);
		try {
			while (stream.incrementToken()) {
				String tm = termAtt.term();
				if(map.containsKey(tm)){
					map.put(tm, map.get(tm)+1);
				}else map.put(tm, 1);
//				if(tm.length()>1){ //只考虑超过2个字的词
//						hashset.add(tm);
//					}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return map;
	}
	
//	public HashSet<String> extract(String content){
//		 StringReader reader = new StringReader(content); 
//	    IKSegmentation ik = new IKSegmentation(reader,true);//当为true时，分词器进行最大词长切分 
//	    Lexeme lexeme = null; 
//	    while((lexeme = ik.next())!=null) 
//	    System.out.println(lexeme.getLexemeText()); 
//	    } 
//	}
	
	public HashSet<String> extract(String content){ //IK
		analyzer = new IKSmartAnalyzer();  
        StringReader reader = new StringReader(content);     
            
        //TokenStream ts = analyzer.tokenStream("*", reader);     
        HashSet<String> hashset = new HashSet<String>();
        IKSegmenter iks = new IKSegmenter(reader, true);
        Lexeme lexem = null;
        try {
			while ((lexem = iks.next()) != null) {
				String tmp =lexem.getLexemeText();
				hashset.add(tmp);  
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	    return hashset;
	}
	
}
