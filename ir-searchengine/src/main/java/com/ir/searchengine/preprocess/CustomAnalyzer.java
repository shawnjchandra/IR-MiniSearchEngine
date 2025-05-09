package com.ir.searchengine.preprocess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class CustomAnalyzer {
    public static StandardAnalyzer getCustomAnalyzer() throws IOException{

        InputStream inputStream = CustomAnalyzer.class.getClassLoader().getResourceAsStream("stopwords.txt");

        try (BufferedReader reader = new BufferedReader( new InputStreamReader(inputStream))){
            List<String> stopwords = reader.lines().map(String::trim)
                                                .filter(line -> !line.isEmpty())
                                                .collect(Collectors.toList());

            CharArraySet stopSet = new CharArraySet(stopwords, true);
            return new StandardAnalyzer(stopSet);
        }

        
    }
}
