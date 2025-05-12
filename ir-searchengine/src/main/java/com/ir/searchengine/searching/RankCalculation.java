package com.ir.searchengine.searching;

import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.BytesRef;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class RankCalculation {
    protected PriorityQueue<DocumentScore> ranking;
    protected DocumentData data;
    
    protected LeafReaderContext leaf;
    protected RankCalculation rc;
    private double score;

    public void setLeaf(LeafReaderContext leaf) {
        this.leaf = leaf;
    }

    public RankCalculation() {

        this.data = new DocumentData();
    }

    // public RankCalculation(RankCalculation rc) {
    //     this.rc = rc;
    //     this.data = new DocumentData();
    // }

    public RankCalculation(LeafReaderContext leaf){

        this.data = new DocumentData();
        this.leaf = leaf;
    }


    public Queue<Double> processRanking(DocumentData other){
        return null;
    }

    // double score(DocumentData other) {
    //     return 0.0;
    // };

    public void init() throws IOException {

        // Buka leaf (segmen) reader
        LeafReader reader = this.leaf.reader();

        // Nomor index (Hanya penanda nomor document di index) dari document
        int docBase = leaf.docBase;

        // Baca hanya untuk term yang pada field "BODY"
        Terms terms = reader.terms("BODY");
                
        // Insialisasi iterator dari term di dalam index (*_)
        TermsEnum termsEnum = terms.iterator();

        // Inisialisasi penyimpanan data (khusus untuk dokument *)
        DocumentData documentData = new DocumentData();
        
        // Inisialisasi Reference Byte untuk konversi ke string
        BytesRef term = null;
        
        // Iterasi untuk semua term yang ada di inverted index (dan juga frequencynya)
        while((term = termsEnum.next())!= null){
            String convertedString = term.utf8ToString();
            PostingsEnum docs = termsEnum.postings(null, PostingsEnum.POSITIONS);
            
            int localDocId;
            while ((localDocId = docs.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS){
                int freq = docs.freq();
                // System.out.println(convertedString + " " + docBase +" "+freq);
                documentData.addData(docBase+localDocId, convertedString, freq);
                
            }
            
        }

        // Ambil total jumlah dokumen untuk proses selanjutnya
        int maxDoc = reader.maxDoc();

        // Proses untuk idf ,...
        documentData.startProcess(maxDoc);

        // Debugging
        System.out.println(documentData.toString());
        // System.out.println(documentData);

       this.data = documentData;
 
    };

    protected class DocumentScore{
        int docId;
        double score;

        DocumentScore(int docId, double score) {
            this.docId = docId;
            this.score = score;
        }
    }
}
