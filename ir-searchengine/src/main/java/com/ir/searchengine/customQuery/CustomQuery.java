package com.ir.searchengine.customQuery;

import java.io.IOException;
import java.util.List;

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

        return result.toString();
    }

    public String getQueryText() {
        return queryText;
    }

    public List<String> getTokens() {
        return tokens;
    }

}

