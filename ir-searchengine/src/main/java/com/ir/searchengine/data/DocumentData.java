package com.ir.searchengine.data;

import java.security.KeyStore.Entry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ir.searchengine.util.Config;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class DocumentData {

    @Data
    @Getter
    public class InnerDocumentData {
        private final String[] config = Config.get("document.settings.print").split(",");
        private Map<String, Boolean> settings;
        
        Map<String, Double> tf;
        Map<String, Double> tf_idft;
        Map<String, Double> cft;

        public InnerDocumentData(){
            
            this.tf = new HashMap<>(); 
            this.tf_idft = new HashMap<>(); 
            this.cft = new HashMap<>();
            
            this.settings = new HashMap<>();

            // DFT nya dipindahin keluar ,jadi i nya dari 1 
            List<String> settingNames = Arrays.asList("dft", "tf", "idf", "cft", "tf_idft");
            for (int i = 1; i < config.length; i++) {
                this.settings.put(settingNames.get(i), Boolean.parseBoolean(config[i]));
            }
        }

        

        // term frequency
        public void tf(String term, int freq){
            this.tf.merge(term, (double)freq, Double::sum);
        }

        // term frequency - inverse doc..
        public double tf_idft(String term, double idft){
         
            // Ambil tf nya
            double tf_term = tf.get(term);
            double tf_idft_score = tf_term * idft;
            
            this.tf_idft.put(term, tf_idft_score);

            return tf_idft_score;
        }

        public void normalize_tf_idft(String term, double vector_norm){
            double tf_idft_score = tf_idft.get(term);
            this.tf_idft.put(term, tf_idft_score/vector_norm );
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            if (!tf.isEmpty()) {
                if (this.settings.get("tf")) {
                    sb.append("tf : \n");
                    for (Map.Entry<String, Double> entry : tf.entrySet()) {
                        sb.append(" ").append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
                    }
                } else {
                    sb.append("tf print setting has been disabled\n");
                }
            } else {
                sb.append("tf is empty\n");
            }

            

            if (!cft.isEmpty()) {
                if (this.settings.get("cft")) {
                    sb.append("cft : \n");
                    for (Map.Entry<String, Double> entry : cft.entrySet()) {
                        sb.append(" ").append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
                    }
                } else {
                    sb.append("cft print setting has been disabled\n");
                }
            } else {
                sb.append("cft is empty\n");
            }
            if (!tf_idft.isEmpty()) {
                if (this.settings.get("tf_idft")) {
                    sb.append("tf_idft : \n");
                    for (Map.Entry<String, Double> entry : tf_idft.entrySet()) {
                        sb.append(" ").append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
                    }
                } else {
                    sb.append("tf_idft print setting has been disabled\n");
                }
            } else {
                sb.append("tf_idft is empty\n");
            }

            return sb.toString();
        }
            
    }

    // DocumentId ke data - datanya (yang tabel itu)
    private final String[] config = Config.get("document.settings.print").split(",");
    protected Map<Integer, InnerDocumentData> documents;
    Map<String, Set<Integer>> termToDoc;
    Map<String, Integer> dft;
    Map<String, Double> idf;
    

    public DocumentData() {
        this.documents = new HashMap<>();
        this.termToDoc = new HashMap<>(); 
        this.dft = new HashMap<>(); 
        this.idf = new HashMap<>(); 
        
    }


    public void addData(int docId, String term, int freq){
        term = term.toLowerCase();

        if (!this.documents.containsKey(docId)) {
            this.documents.put(docId, new InnerDocumentData());
        }
        // Masukin ke table tf
        this.documents.get(docId).tf(term, freq); 
        
        // Masukin ke dft
        this.addToDFT(term, docId);
     
    }

    public void startProcess(int totalDocs){
        
        // Process dft
        for (String term : termToDoc.keySet()){
            dft.put(term, termToDoc.get(term).size());
        }
        // Process idft
        standard_idf(totalDocs);
        // Test untuk pake idf lain
        // probabilistic_idf(totalDocs);
        // smooth_idf(totalDocs);


        // Untuk Normalisasi Dokumen
        double vector_norm = 0.0;

        // Process tf-idft
        double val;
        for (Integer key : this.documents.keySet()){

            int entry = Integer.valueOf(key);
            Map<String, Double > map_term = this.documents.get(entry).getTf();
            // System.out.println(map_term.size());
            for (String term : map_term.keySet()){
                double idft_score = idf.get(term);
                val = this.documents.get(entry).tf_idft(term, idft_score);
                
                // Jumlahin semua nilai kuadrat tf_idft 
                vector_norm += Math.pow(val, 2);
            }
        }
        
        // Akarin total kuadrat
        vector_norm = Math.sqrt(vector_norm);
        
        // Normalisasi Vector
        for (Integer key : this.documents.keySet()){

            int entry = Integer.valueOf(key);
            Map<String, Double > map_term = this.documents.get(entry).getTf();
    
            for (String term : map_term.keySet()){
    
                // Ganti isi map dari tf_idft sebelumnya menjadi bentuk ternormalisasi
                this.documents.get(entry).normalize_tf_idft(term, vector_norm);
            }
        }

    }

    // Dokument term frequency
    private void addToDFT(String term, int docId){
        if (!termToDoc.containsKey(term)){
            termToDoc.put(term, new HashSet<>());
        }
        
        Set<Integer> docSet = termToDoc.get(term);
        if(!docSet.contains(docId)){
            docSet.add(docId);
        }
        // System.out.println(term +" "+docSet.size());
    }

    // Standard idf
    private void standard_idf(int totalDocs){
    
        for (Map.Entry<String, Integer> entry : dft.entrySet()){

            // +1 Biar tidak pembagian 0 di tf-idf nya
            double idf = Math.log10((double)totalDocs / (double)entry.getValue() + 1);
            this.idf.put(entry.getKey(), idf);
        }
    }

    // probabilistic idf 
    private void probabilistic_idf(int totalDocs){
    
        for (Map.Entry<String, Integer> entry : dft.entrySet()){

            double idf = Math.max(0 , Math.log10(((double)totalDocs- (double)entry.getValue())/ (double)entry.getValue() + 1));
            this.idf.put(entry.getKey(), idf);
        }
    }
    // smooth idf 
    private void smooth_idf(int totalDocs){
    
        for (Map.Entry<String, Integer> entry : dft.entrySet()){

            double idf = Math.log10(1 + ((double)totalDocs / ((double)entry.getValue() + 1) ));
            this.idf.put(entry.getKey(), idf);
        }
    }




    

    @Override
    public String toString(){

        StringBuilder sb = new StringBuilder();
        if (!documents.isEmpty()){
            for (Map.Entry<Integer, InnerDocumentData> doc : documents.entrySet()){
                sb.append("Document ID: ").append(doc.getKey()).append("\n");
                sb.append(doc.getValue().toString()).append("\n");

            }
        } else {
            System.out.println("document list is empty \n");
        }

        sb.append("=========================================\n");
        if (!dft.isEmpty()) {
                if (Boolean.parseBoolean(this.config[0])) {
                    sb.append("dft : \n");
                    for (Map.Entry<String, Set<Integer>> entry : termToDoc.entrySet()) {
                        sb.append(" ").append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
                    }
                } else {
                    sb.append("dft print setting has been disabled\n");
                }
            } else {
                sb.append("dft is empty\n");
            }

        if (!idf.isEmpty()) {
                if (Boolean.parseBoolean(this.config[2])) {
                    sb.append("idf : \n");
                    for (Map.Entry<String, Double> entry : idf.entrySet()) {
                        sb.append(" ").append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
                    }
                } else {
                    sb.append("idf print setting has been disabled\n");
                }
            } else {
                sb.append("idf is empty\n");
            }

        return sb.toString();
    }


}
