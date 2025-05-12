package com.ir.searchengine;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

import com.ir.searchengine.indexer.Indexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefArray;
import org.apache.lucene.store.Directory;

import com.ir.searchengine.preprocess.CustomAnalyzer;
import com.ir.searchengine.preprocess.DocumentParser;
import com.ir.searchengine.searching.BM25;
import com.ir.searchengine.searching.CustomQuery;
import com.ir.searchengine.searching.DocumentData;
import com.ir.searchengine.searching.RankCalculation;
import com.ir.searchengine.searching.VSM;
import com.ir.searchengine.util.Config;
import com.ir.searchengine.util.FileCleaner;

/**
 * Hello world!
 *
 */
public class App 
{
    // private final static String indexPath = Config.get("index.directory");
    private final static String docsPath = Config.get("document.directory");
    private static String indexPath;
    private static String queryPath;

    public static void main( String[] args )
    {
        try {

            /* @Setup - Start */
            indexPath = Config.get("index.directory") != null ? Config.get("index.directory") : /*CustomPath implement*/ "";

            queryPath = Config.get("query.directory") != null ? Config.get("query.directory") : /* Custom Path Implementa */"";
            Path docsDirectoryPath = Paths.get(docsPath).toAbsolutePath();
            
            // Bersihin dulu untuk isi dari indexDirectory (supaya ga numpuk)
            boolean recreateIndex = Boolean.parseBoolean(Config.get("index.recreate", "false"));
            if (recreateIndex) {
                FileCleaner.clearIndexDirectory(indexPath);
                FileCleaner.clearIndexDirectory(queryPath);    
            }

            // Bikin custom anaylzer dengan stopwords
            Analyzer analyzer = CustomAnalyzer.getCustomAnalyzer();
            
            /* @Setup - End */ 

            // Bikin indexer, set Directory dan analyzer yang bakal dipakai ketika .add
            Directory indexDirectory = FSDirectory.open(Paths.get(indexPath));
            Indexer indexer = new Indexer(indexDirectory, analyzer);
            
            // Call pertama kali untuk bikin index dari dokumen
            WriteToIndex(indexer, docsDirectoryPath, null);

            Scanner sc = new Scanner(System.in);

            /* Setup untuk Query */ 
            Directory queryDirectory = FSDirectory.open(Paths.get(queryPath));
            indexer = new Indexer(queryDirectory, analyzer);
            
            // Start Query Process
            String method = startQuery(sc, indexer);


            // DirectoryReader untuk buka si index directory
            DirectoryReader indexDirectoryReader = DirectoryReader.open(indexDirectory);
            // Directory untuk buka si query 
            DirectoryReader queryDirectoryReader = DirectoryReader.open(queryDirectory);
            
            // Proses untuk masing - masing index dari document dan juga query
            RankCalculation processedQuery = processDataIndex(queryDirectoryReader);
            RankCalculation processedDocument = processDataIndex(indexDirectoryReader);


            // Proses yang dokumen
            if (method.equals("vsm")){
                VSM processedVSM = new VSM(processedDocument);
        
                Queue<Double> scores = processedVSM.processRanking(processedQuery.getData());
                
            }else {
                BM25 processedBm25 = new BM25(processedDocument);
                processedBm25.processRanking(processedQuery.getData());
            }

            // processedDocument.score(processedQuery.getData())
            // processedDocument.processRanking(processedQuery.getData());


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
       
        // preprocess.preprocess(text);
    }


    static void WriteToIndex(Indexer indexer, Path sourceDirectoryPath, List<CustomQuery> queries){
        try {
            if (queries != null){
                // Bersihkan dulu index query
                
                for (CustomQuery query : queries){
                    String body = query.process();
                    indexer.indexNewDocument(null, body);
                }

            }else{
                if (!Files.exists(sourceDirectoryPath)){
                        System.err.println("Docs directory doesnt exist: "+ sourceDirectoryPath);
                    }else {
                        // Masukin document yang bakal di index
                        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDirectoryPath, "*.txt")){
                
                            for (Path filePath : stream){
                                String content = Files.readString(filePath);
                                DocumentParser.ParsedDocument parsed = DocumentParser.parse(content);
                                indexer.indexNewDocument(parsed.title, parsed.body);
            
                            }
                        } catch (Exception e) {
                            System.err.println("Error during indexing: ");
                            e.printStackTrace();
                            // TODO: handle exception
                        }            
                    }

            }

            // tutup indexer sekali saja
            indexer.close();
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
    }

    static String startQuery(Scanner scanner, Indexer indexer) throws Exception{
        String input, method;
        int val;
        System.out.print("How many queries do you want to do (default: 1) :");

        
        val = Integer.parseInt(scanner.nextLine());
        if( val == 0 ) return null;
        
        CustomQuery customQuery;
        List<CustomQuery> list = new ArrayList<>();
        while (val > 0) {
            System.out.print("\nType what you want here (use 'stop' to end the process) : ");
            input = scanner.nextLine().toLowerCase();
            
            customQuery = new CustomQuery(input);
            list.add(customQuery);
            
            if(input.isEmpty() || input.toLowerCase().equals("stop")){
                break;
            }
            
            val--;
        }

        System.out.print("\nProcessing...");
 
        // Tulis index query
        WriteToIndex(indexer, null, list);
        
        
        System.out.print("\nSearch using -VSM- or -BM25- : ");
        method = scanner.nextLine().toLowerCase();

        if (method.equals("vsm") ||method.equals("bm25")){
            return method;
        } else {
            throw new Exception("Method is not allowed");
        }

    }

    static RankCalculation processDataIndex(DirectoryReader sourceDirectoryReader) throws IOException{
        // Ini untuk dapetin reader dari semua segmen (1 segmen = 300 docs) -> (_0 = 0-299)
            List<LeafReaderContext> leaves = sourceDirectoryReader.leaves();

            // Polymorphism untuk metode SVM dan BM25
            RankCalculation rc = new RankCalculation(null);
            
            int i =0;
            // Iterasi untuk setiap segmen (docs nya juga)
            for (LeafReaderContext leaf : leaves){
                // rc = new RankCalculation(leaf);
                rc.setLeaf(leaf);
                rc.init();
                // System.out.println(i);
                // System.out.println("di processed data index"+ rc.getData().getDocuments().size());
                i++;
            }
            
            // SVM svm = new SVM(rc);
            // BM25 bm25 = new BM25(rc);
            return rc;
    }

  
}
