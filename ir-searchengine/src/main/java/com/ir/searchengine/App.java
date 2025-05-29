package com.ir.searchengine;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

import com.ir.searchengine.core.Config;
import com.ir.searchengine.customQuery.CustomQuery;
import com.ir.searchengine.data.DocumentData;
import com.ir.searchengine.data.DocumentScore;
import com.ir.searchengine.indexer.Indexer;
import com.ir.searchengine.models.BM25;
import com.ir.searchengine.models.RankCalculation;
import com.ir.searchengine.models.VSM;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;

import com.ir.searchengine.preprocess.CustomAnalyzer;
import com.ir.searchengine.preprocess.DocumentParser;
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
    private static Map<Integer, DocumentParser.ParsedDocument> parsedDocuments;
    private static int k;

    public static void main( String[] args )
    {
        Logger.getLogger("jdk.incubator.vector").setLevel(Level.SEVERE);

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

            
            /* Setup untuk Query */ 
            Scanner sc = new Scanner(System.in);
            Directory queryDirectory = FSDirectory.open(Paths.get(queryPath));

            // Indexer dengan analyzer nonstopwords

            // Non-filtered analyzer
            // indexer = new Indexer(queryDirectory, new KeywordAnalyzer());

            //Filtered out analyzer
            indexer = new Indexer(queryDirectory, analyzer);
            
            // Start Query Process
            String method = startQuery(sc, indexer);

            /* Masuk ke Searching / Ranking */

            // DirectoryReader untuk buka si index directory
            DirectoryReader indexDirectoryReader = DirectoryReader.open(indexDirectory);
            // Directory untuk buka si query 
            DirectoryReader queryDirectoryReader = DirectoryReader.open(queryDirectory);
            
            // Proses untuk masing - masing index dari document dan juga query
            RankCalculation processedQuery = processDataIndex(queryDirectoryReader,"query");
            RankCalculation processedDocument = processDataIndex(indexDirectoryReader,"non-query");

            
            Queue<DocumentScore> scores;
            // Proses yang dokumen
            if (method.equals("vsm")){
                VSM processedVSM = new VSM(processedDocument);
                scores = processedVSM.processRanking(processedQuery.getData());
                
            }else {
                int totalDocs = parsedDocuments.size();
                BM25 processedBm25 = new BM25(processedDocument, totalDocs);
                scores = processedBm25.processRanking(processedQuery.getData());      
            }

            // Get top k Docs
            String result = RankCalculation.getTopKDocs(App.k, scores, App.parsedDocuments);

            // Print out result
            System.out.println(result);;


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
                    // System.out.println("Query : "+body);
                    indexer.indexNewDocument(null, body);
                }

            }else{
                if (!Files.exists(sourceDirectoryPath)){
                        System.err.println("Docs directory doesnt exist: "+ sourceDirectoryPath);
                    }else {
                        int i = 0;
                        App.parsedDocuments = new HashMap<>();

                        // Masukin document yang bakal di index
                        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDirectoryPath, "*.txt")){
                            
                            for (Path filePath : stream){
                                String content = Files.readString(filePath);
                                DocumentParser.ParsedDocument parsed = DocumentParser.parse(content);
                                App.parsedDocuments.put(i, parsed);
                                indexer.indexNewDocument(parsed.title, parsed.body);
                                i++;
            
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

        CustomQuery customQuery;
        List<CustomQuery> list = new ArrayList<>();

        System.out.print("\nType what you want here (use 'stop' to end the process) : ");
        input = scanner.nextLine().toLowerCase();
        
        customQuery = new CustomQuery(input);
        list.add(customQuery);
        
        if(input.isEmpty()){
            return new Exception("Input empty").toString();

        } 
        
        if (input.toLowerCase().equals("stop")){
            return "Stop Process...";
        }
    

        System.out.print("\nProcessing...");
 
        
        // Tulis index query
        WriteToIndex(indexer, null, list);
        
        Thread.sleep(1000);
        
        System.out.print("\n\nSearch using -VSM- or -BM25- : ");
        method = scanner.nextLine().toLowerCase();
        
        System.out.print("\nHow many documents (max 30) : ");
        int num = scanner.nextInt();
        App.k = Math.clamp(num, 0, 30);

        if (method.equals("vsm") ||method.equals("bm25")){
            return method;
        } else {
            throw new Exception("Method is not allowed or you have stopped");
        }

    }

    static RankCalculation processDataIndex(DirectoryReader sourceDirectoryReader, String type) throws IOException{

        // Ini untuk dapetin reader dari semua segmen (1 segmen = 300 docs) -> (_0 = 0-299)
            List<LeafReaderContext> leaves = sourceDirectoryReader.leaves();

            // Polymorphism untuk metode VSM dan BM25
            RankCalculation rc = new RankCalculation(null);
            
            // Iterasi untuk setiap segmen (docs nya juga)
            for (LeafReaderContext leaf : leaves){
                // rc = new RankCalculation(leaf);
                rc.wrap(leaf, type);
                // rc.setLeaf(leaf);
                // rc.init();

            }
            
            return rc;
    }

  
}
