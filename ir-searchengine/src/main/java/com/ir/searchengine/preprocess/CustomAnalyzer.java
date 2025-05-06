package com.ir.searchengine.preprocess;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class CustomAnalyzer {
    public static StandardAnalyzer getCustomAnalyzer() throws IOException{
        try( Reader reader = new InputStreamReader(CustomAnalyzer.class.getClassLoader().getResourceAsStream("stopwords.txt"))){
        
            return new StandardAnalyzer(reader);
        }
    }
}
