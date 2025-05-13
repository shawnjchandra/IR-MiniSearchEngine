package com.ir.searchengine.searching;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.lucene.index.LeafReaderContext;

import com.ir.searchengine.App;
import com.ir.searchengine.preprocess.DocumentParser;
import com.ir.searchengine.searching.DocumentData.InnerDocumentData;

/*
    Bikin tf, dft ,tf-idf 
    Vector tf-idf dinormlisasiin

    Querynya juga
    terus dot product
    habis itu urutin
*/ 
public class VSM extends RankCalculation  {
    
    public VSM (LeafReaderContext leaf){
        super(leaf);
    }

    public VSM (RankCalculation rc){
        this.rc = rc;
        ranking = new PriorityQueue<>((a, b) -> Double.compare(b.score, a.score));

    }

    @Override
    public Queue<DocumentScore> processRanking(DocumentData query) {
        
        Map<Integer, InnerDocumentData> documents = this.rc.data.getDocuments();
        Map<Integer, InnerDocumentData> queryData = query.getDocuments();

        // System.out.println(documents.size() +" "+queryData.size());
        
        if (documents.isEmpty() || queryData.isEmpty()){
            System.out.println("Neither data nor documents is avaialble");
            return null;
        }

        for( Map.Entry<Integer, InnerDocumentData> entry : documents.entrySet()){
            int docId = entry.getKey();
            InnerDocumentData document_vector = entry.getValue();

            double dot_product = score(document_vector, queryData);
            
            ranking.offer(new DocumentScore(docId, dot_product));
        }

        Queue<DocumentScore> scoreQueue = new LinkedList<>();
        while (!ranking.isEmpty()) {
            DocumentScore docScore = ranking.poll();
            
            // System.out.println(docScore.docId +" "+ docScore.score);
            scoreQueue.add(docScore);
            
        }

        return scoreQueue;
        
    }

    // @Override
    double score(InnerDocumentData documentVector, 
        Map<Integer, InnerDocumentData> queryData) {
            double dot_product = 0.0;

            Map<String, Double> document_Term_Weight = documentVector.getTf_idft();
            
            for (InnerDocumentData queryVector : queryData.values()){
                Map<String, Double> query_Term_Weight = queryVector.getTf_idft();

                for (Map.Entry<String, Double> termEntry : document_Term_Weight.entrySet()){
                    String term = termEntry.getKey();
                    double document_weight = termEntry.getValue();
                    
                    if (query_Term_Weight.containsKey(term)){
                        // System.out.println(term+" : "+document_weight+" "+query_Term_Weight.get(term));
                        dot_product += document_weight * query_Term_Weight.get(term);
                    }
                }

            }

        return dot_product;
    }

    



}
