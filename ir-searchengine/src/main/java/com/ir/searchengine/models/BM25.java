package com.ir.searchengine.models;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.lucene.index.LeafReaderContext;

import com.ir.searchengine.data.DocumentData;
import com.ir.searchengine.data.DocumentScore;
import com.ir.searchengine.data.DocumentData.InnerDocumentData;

public class BM25 extends RankCalculation {

    private static final double k = 1.5;
    private static final double b = 0.75;

    private double avgDocLength;

    public BM25(LeafReaderContext leaf) {
        super(leaf);
    }

    public BM25(RankCalculation rc) {
        this.rc = rc;
        ranking = new PriorityQueue<>((a, b) -> Double.compare(b.score, a.score));
        this.avgDocLength = calculateAvgDocLength();
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
        Map<String, Double> idf = this.rc.data.getIdf();

        if (documents.isEmpty() || queryData.isEmpty()) {
            System.out.println("No document or query data available.");
            return null;
        }

        for (Map.Entry<Integer, InnerDocumentData> entry : documents.entrySet()) {
            int docId = entry.getKey();
            InnerDocumentData document = entry.getValue();

            double score = computeBM25(document, queryData, idf);
            ranking.offer(new DocumentScore(docId, score));
        }

        Queue<DocumentScore> scoreQueue = new LinkedList<>();
        while (!ranking.isEmpty()) {
            scoreQueue.add(ranking.poll());
        }

        return scoreQueue;
    }

    private double computeBM25(InnerDocumentData doc, 
                               Map<Integer, InnerDocumentData> queryData, 
                               Map<String, Double> idfMap) {

        double score = 0.0;
        Map<String, Double> tfMap = doc.getTf();

        // Panjang dokumen
        double docLength = tfMap.values().stream().mapToDouble(Double::doubleValue).sum();

        for (InnerDocumentData queryVec : queryData.values()) {
            for (Map.Entry<String, Double> queryTerm : queryVec.getTf().entrySet()) {
                String term = queryTerm.getKey();
                double qf = queryTerm.getValue(); // biasanya 1.0

                if (!tfMap.containsKey(term)) continue;

                double tf = tfMap.get(term);
                double idf = idfMap.getOrDefault(term, 0.0);

                double numerator = tf * (k + 1);
                double denominator = tf + k * (1 - b + b * (docLength / avgDocLength));
                score += idf * (numerator / denominator);
            }
        }

        return score;
    }
}
