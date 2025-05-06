package com.ir.searchengine.preprocess;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;


public class Preprocess {
    

    public static List<String> tokenize(String text) throws IOException  {
        Analyzer analyzer = CustomAnalyzer.getCustomAnalyzer();
        System.out.println(analyzer);
        List<String> tokens = new ArrayList<>();
        
        try (TokenStream tokenStream = analyzer.tokenStream("field", new StringReader(text))){
            CharTermAttribute termAttribute = tokenStream.addAttribute(CharTermAttribute.class);

            tokenStream.reset();
            

            System.out.println("Tokens: ");
            while (tokenStream.incrementToken()) {
                // System.out.println(termAttribute.toString());
                tokens.add(termAttribute.toString());
            }

            tokenStream.end();
            
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return tokens;
    }



}
