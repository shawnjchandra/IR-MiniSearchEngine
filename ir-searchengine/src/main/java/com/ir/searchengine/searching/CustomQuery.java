package com.ir.searchengine.searching;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import com.ir.searchengine.preprocess.CustomAnalyzer;
import com.ir.searchengine.preprocess.Preprocess;

public class CustomQuery {
    private String queryText;
    private List<String> tokens;

    public CustomQuery(String queryText){
        this.queryText = queryText;
    }

    public String process(){

        StringBuilder result = new StringBuilder();
        try {
            this.tokens = Preprocess.tokenize("field", queryText);
       
            for (String token : tokens){
                result.append(token+" ");
            }
        } catch (IOException e) {
            
            e.printStackTrace();
        }
        System.out.println(result);
        return result.toString();
    }

    public String getQueryText() {
        return queryText;
    }

    public List<String> getTokens() {
        return tokens;
    }

    

    

}
