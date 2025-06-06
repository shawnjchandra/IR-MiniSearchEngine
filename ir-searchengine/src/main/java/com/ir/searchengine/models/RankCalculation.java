package com.ir.searchengine.models;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.BytesRef;

import com.ir.searchengine.App;
import com.ir.searchengine.data.DocumentData;
import com.ir.searchengine.data.DocumentScore;
import com.ir.searchengine.preprocess.DocumentParser;

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

    
    public RankCalculation() {
        
        this.data = new DocumentData();
    }
    
    
    public RankCalculation(LeafReaderContext leaf){
        
        this.data = new DocumentData();
        this.leaf = leaf;
    }
    
    public void wrap(LeafReaderContext leaf, String type) throws IOException{
        setLeaf(leaf);
        init(type);
    }

    private void setLeaf(LeafReaderContext leaf) {
        this.leaf = leaf;
    }
    
    public Queue<DocumentScore> processRanking(DocumentData other){
        return null;
    }

    // double score(DocumentData other) {
    //     return 0.0;
    // };

    public void init(String type) throws IOException {

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

            // Debug untuk query
            // if(type.equals("query")){
            //     System.out.println(convertedString);
            // }
            
            PostingsEnum docs = termsEnum.postings(null, PostingsEnum.POSITIONS);
            
            int localDocId;
            while ((localDocId = docs.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS){
                
                // Mengambil jumlah frekuensi sebuah term dari posting list
                /*
                Note :
                Cara kerja Postings List di Apache Lucene beda dari umumnya, Untuk mendapatkan jumlah term yang digunakan tidak bisa hanya diloop dan dijumlahkan, tapi perlu menggunakan .freq() atau jumlah frekuensi penggunaan term di dalam postings                
                */ 
                int freq = docs.freq();
                


                documentData.addData(docBase+localDocId, convertedString, freq);
                
            }
            
        }
        System.out.println();

        // Ambil total jumlah dokumen untuk proses selanjutnya
        int maxDoc = reader.maxDoc();

        // Proses untuk idf ,...
        documentData.startProcess(maxDoc);

        // Debugging
        // System.out.println(documentData.toString());
        // System.out.println(documentData);

       this.data = documentData;
 
    };
    
    public static String getTopKDocs(int k,Queue<DocumentScore> scores, Map<Integer, DocumentParser.ParsedDocument> parsedDocuments){
        

        StringBuilder sb = new StringBuilder();
        // Max Length
            // int maxPreview = 5;
            sb.append("Top ").append(k).append(" Ranked Documents: \n");

            int count = 0;

            
            for(DocumentScore doc : scores){
                if(count >= k) break; // Stop after processing k documents

                sb.append("-".repeat(k)).append("\n");
                
                int id = doc.getDocId();
                double score = doc.getScore();
                
                String preview = parsedDocuments.get(id).body.split("\\s+",k+1).length > k ? 
                String.join(" ", Arrays.copyOfRange(parsedDocuments.get(id).body.split("\\s+"),0 , k)) + "..." :

                parsedDocuments.get(id).body; 
                
                String title = parsedDocuments.get(id).title;

                sb.append("Document Title\t: ").append(title).append("\nScore\t\t: ").append(String.format("%.4f\n", score));
                sb.append("Preview\t\t: ").append(preview).append("\n");
                
                count++;
            }
            sb.append("-".repeat(k)).append("\n");

        return sb.toString();
    }
    

}
