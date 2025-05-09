package com.ir.searchengine;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.ir.searchengine.indexer.Indexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;

import com.ir.searchengine.preprocess.CustomAnalyzer;
import com.ir.searchengine.preprocess.DocumentParser;
import com.ir.searchengine.preprocess.Preprocess;
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

    public static void main( String[] args )
    {
        try {
            indexPath = Config.get("index.directory") != null ? Config.get("index.directory") : /*CustomPath implement*/ "";
            
            // Bersihin dulu untuk isi dari indexDirectory (supaya ga numpuk)
            FileCleaner.clearIndexDirectory(indexPath);

            Directory indexDirectory = FSDirectory.open(Paths.get(indexPath));
            Path docsDirectoryPath = Paths.get(docsPath).toAbsolutePath();
            System.out.println("Resolved docs path : " + docsDirectoryPath );

            // Bikin custom anaylzer dengan stopwords
            Analyzer analyzer = CustomAnalyzer.getCustomAnalyzer();

            // Bikin indexer, set Directory dan analyzer yang bakal dipakai ketika .add
            Indexer indexer = new Indexer(indexDirectory, analyzer);
            
            if (!Files.exists(docsDirectoryPath)){
                System.err.println("Docs directory doesnt exist: "+ docsDirectoryPath);
            }else {
                // Masukin document yang bakal di index
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(docsDirectoryPath, "*.txt")){
          
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

            IndexReader indexReader = DirectoryReader.open(indexDirectory);

            // IndexSearcher searcher = new IndexSearcher(indexReader);

            // TopDocs allDocs = searcher.search(new MatchAllDocsQuery(), indexReader.numDocs());
            // StoredFields storeFields = searcher.storedFields();

            // System.out.println("Total documents in index"+ allDocs.totalHits.value());
            // for (ScoreDoc scoreDoc : allDocs.scoreDocs){
            //     Document doc = storeFields.document(scoreDoc.doc);
            //     System.out.println("----------");
            //     String title = doc.get("TITLE");
            //     String body = doc.get("BODY");

            //         TokenStream tokenStream = CustomAnalyzer.getCustomAnalyzer().tokenStream("body", new StringReader(body));
            //     CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);

            //     // Reset the TokenStream before reading tokens
            //     tokenStream.reset();

            //     // Print out the tokens from the BODY field
            //     System.out.println("BODY (Tokenized via Analyzer): ");
            //     while (tokenStream.incrementToken()) {
            //         System.out.println(attr.toString());
            //     }

            //     // End the token stream processing
            //     tokenStream.end();
            //     tokenStream.close();
            // }
            
            /*Ranking pakai BM25  */ 
     

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
       
        // preprocess.preprocess(text);
    }
}
