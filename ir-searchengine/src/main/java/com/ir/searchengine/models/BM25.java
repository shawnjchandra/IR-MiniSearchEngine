package com.ir.searchengine.models;

import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Queue;
import org.apache.lucene.index.LeafReaderContext;

import com.ir.searchengine.data.DocumentData;
import com.ir.searchengine.data.DocumentScore;


public class BM25 extends RankCalculation {

    public BM25 (LeafReaderContext leaf){
        super(leaf);
    }
    public BM25 (RankCalculation rc){
        this.rc = rc;
    }

    // @Override
    // double score(DocumentData othData) {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'score'");
    // }


    public Queue<DocumentScore> processRanking(DocumentData other) {
        // TODO Auto-generated method stub
        return super.processRanking( other);
    }
  
    // void init() throws IOException {
    //     // Base
    //     LeafReader reader = this.leaf.reader();
    //     int docBase = leaf.docBase;
    //     Terms terms = reader.terms("BODY");
                
    //     TermsEnum termsEnum = terms.iterator();
    //     DocumentData data = new DocumentData();
    // }
  

    
}
