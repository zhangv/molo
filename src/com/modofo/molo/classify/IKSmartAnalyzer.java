package com.modofo.molo.classify;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.wltea.analyzer.lucene.IKTokenizer;

/**
 * 因为mahout只能使用默认无参数构造函数，这里扩展一下。
 * 顺便吐个槽：IKAnalyzer居然是final的
 */
public final class IKSmartAnalyzer extends Analyzer{
	
	private boolean useSmart;
	
	public boolean useSmart() {
		return useSmart;
	}

	public void setUseSmart(boolean useSmart) {
		this.useSmart = useSmart;
	}

	/**
	 * IK分词器Lucene 3.5 Analyzer接口实现类
	 * 
	 * 默认细粒度切分算法
	 */
	public IKSmartAnalyzer(){
		this(true);
	}
	
	/**
	 * IK分词器Lucene Analyzer接口实现类
	 * 
	 * @param useSmart 当为true时，分词器进行智能切分
	 */
	public IKSmartAnalyzer(boolean useSmart){
		super();
		this.useSmart = useSmart;
	}

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		return new IKTokenizer(reader , this.useSmart());
	}
	
	@Override
	public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
		Tokenizer _IKTokenizer = (Tokenizer)getPreviousTokenStream();
		if (_IKTokenizer == null) {
			_IKTokenizer = new IKTokenizer(reader , this.useSmart());
			setPreviousTokenStream(_IKTokenizer);
		} else {
			_IKTokenizer.reset(reader);
		}
		return _IKTokenizer;
	}
}
