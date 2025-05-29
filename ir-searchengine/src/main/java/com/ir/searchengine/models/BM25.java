package com.ir.searchengine.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.apache.lucene.index.LeafReaderContext;

import com.ir.searchengine.data.DocumentData;
import com.ir.searchengine.data.DocumentScore;
import com.ir.searchengine.data.DocumentData.InnerDocumentData;

public class BM25 extends RankCalculation {

    private static final double k = 1.5;
    private static final double b = 0.75;

    private double avgDocLength;
    private double R;
    private int totalDocs;
    private Map<String, Set<Integer>> termToDoc;
    private Map<String, Integer> RelevanTermMap; //rt
    private Set<Integer> relevantDocs;

    public BM25(LeafReaderContext leaf) {
        super(leaf);
    }

    public BM25(RankCalculation rc, int totalDocs) {
        this.rc = rc;
        this.R = 0;
        this.totalDocs = totalDocs;

        ranking = new PriorityQueue<>((a, b) -> Double.compare(b.score, a.score));
        this.avgDocLength = calculateAvgDocLength();
        this.termToDoc = rc.data.getTermToDoc();
        this.RelevanTermMap = new HashMap<>();
    }

    private double calculateAvgDocLength() {
        double totalLength = 0.0;
        Map<Integer, InnerDocumentData> docs = this.rc.data.getDocuments();
        for (InnerDocumentData doc : docs.values()) {
            totalLength += doc.getTf().values().stream().mapToDouble(Double::doubleValue).sum();
        }
        return totalLength / docs.size();
    }

    @Override
    public Queue<DocumentScore> processRanking(DocumentData query) {

        Map<Integer, InnerDocumentData> documents = this.rc.data.getDocuments();
        Map<Integer, InnerDocumentData> queryData = query.getDocuments();
        
        calculateRelevancy(queryData);
        // Map<String, Double> idf = this.rc.data.getIdf();

        if (documents.isEmpty() || queryData.isEmpty()) {
            System.out.println("No document or query data available.");
            return null;
        }

        // Jumlah dokumen relevan ,dan dokumen yang memiliki
        
        for (Map.Entry<Integer, InnerDocumentData> entry : documents.entrySet()) {
            
            int docId = entry.getKey();
            InnerDocumentData document = entry.getValue();

            double score = computeBM25(document, queryData);
            // double score = computeBM25(document, queryData, idf);
            ranking.offer(new DocumentScore(docId, score));
        }

        Queue<DocumentScore> scoreQueue = new LinkedList<>();
        while (!ranking.isEmpty()) {
            scoreQueue.add(ranking.poll());
        }

        return scoreQueue;
    }

    // return rt
    private void calculateRelevancy(Map<Integer, InnerDocumentData> queryData) {
    // Ambil semua term dari query
    Set<String> queryTerms = new HashSet<>();
    for (InnerDocumentData dd : queryData.values()) {
        queryTerms.addAll(dd.getTf().keySet());
    }

    // Hitung threshold: jumlah minimal term yang harus cocok agar dianggap relevan
    int minMatch = (int) Math.ceil(queryTerms.size() / 2.0);
    if (minMatch < 1) minMatch = 1;
    // System.out.println("minimal match "+minMatch);

    // Map dokumen ke jumlah query term yang cocok
    Map<Integer, Integer> docToMatchedTerm = new HashMap<>();

    // Iterasi semua query term, dan lihat di dokumen mana saja term itu muncul
    for (String term : queryTerms) {
        if (!termToDoc.containsKey(term)) continue;
        for (Integer docId : termToDoc.get(term)) {
            docToMatchedTerm.put(docId, docToMatchedTerm.getOrDefault(docId, 0) + 1);
        }
    }

    // Set dokumen relevan
    relevantDocs = new HashSet<>();
    for (Map.Entry<Integer, Integer> entry : docToMatchedTerm.entrySet()) {
        if (entry.getValue() >= minMatch) {
            relevantDocs.add(entry.getKey());
        }
    }

    // System.out.println("Dokumen relevan (ID): " + relevantDocs);

    // Hitung r_t: berapa dokumen relevan yang mengandung setiap term dari query
    Map<String, Integer> termToRelevantCount = new HashMap<>();
    for (String term : queryTerms) {
        int count = 0;
        if (termToDoc.containsKey(term)) {
            for (Integer docId : termToDoc.get(term)) {
                if (relevantDocs.contains(docId)) {
                    count++;
                }
            }
        }
        termToRelevantCount.put(term, count);
    }

    // Cetak hasil r_t
    // for (String term : queryTerms) {
    //     System.out.println("Term: " + term + ", r_t: " + termToRelevantCount.get(term));
    // }
}

    

    private double computeBM25(InnerDocumentData doc, 
                               Map<Integer, InnerDocumentData> queryData
                            //    Map<String, Double> idfMap
                               ) {

        double score = 0.0;

        // Term frequency dari document
        Map<String, Double> tfMap = doc.getTf();

        // Panjang dokumen
        double docLength = tfMap.values().stream().mapToDouble(Double::doubleValue).sum();


        for (InnerDocumentData queryVec : queryData.values()) {
            for (Map.Entry<String, Double> queryTerm : queryVec.getTf().entrySet()) {
                
                String term = queryTerm.getKey();
                double qf = queryTerm.getValue(); // biasanya 1.0

                if (!tfMap.containsKey(term)) continue;
                

                double tf = tfMap.get(term);
                // double idf = idfMap.getOrDefault(term, 0.0);

                double weight_term = getWeightTerm(term);

                double numerator = tf * (k + 1) * weight_term;
                // System.out.println("numerator : "+ queryTerm.getKey() +" "+tf +" "+ weight_term);

                double denominator = tf + k * (1 - b ) + b * (docLength / avgDocLength);
                // score += idf * (numerator / denominator);
                score += (numerator/denominator);
            }
      
        }

        return score;
    }

    private double getWeightTerm(String term) {
    double N = (double) totalDocs;
    double R = (double) relevantDocs.size();

    // Nt = jumlah dokumen yang mengandung term
    double Nt = 0.0;
    if (termToDoc.containsKey(term)) {
        Nt = termToDoc.get(term).size();
    }

    // rt = jumlah dokumen relevan yang mengandung term
    double rt = 0.0;
    if (termToDoc.containsKey(term)) {
        for (Integer docId : termToDoc.get(term)) {
            if (relevantDocs.contains(docId)) {
                rt++;
            }
        }
    }

    // Avoid division by zero
    if (Nt == 0) return 0.0;

    double numerator = (rt + 0.5) * (N - R + 1);
    double denominator = (R + 1) * (Nt - rt + 0.5);
    
    // Prevent log(0) or log of negative numbers
    if (denominator == 0 || numerator <= 0 || denominator <= 0) {
        return 0.0;
    }

    return Math.log(numerator / denominator);
}



}
