package com.ir.searchengine.preprocess;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;


public class Preprocess {
    private Analyzer analyzer;

    public Preprocess() {
        try {
            this.analyzer = CustomAnalyzer.getCustomAnalyzer();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Analyzer error");
            e.printStackTrace();
        }
    }

    public List<String> tokenize(String text) throws IOException  {

        List<String> tokens = new ArrayList<>();
        
        try (TokenStream tokenStream = this.analyzer.tokenStream("field", new StringReader(text))){
            CharTermAttribute termAttribute = tokenStream.addAttribute(CharTermAttribute.class);

            tokenStream.reset();
            

            // System.out.println("Tokens: ");
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
