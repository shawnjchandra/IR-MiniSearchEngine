package com.ir.searchengine.indexer;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;


public class Indexer {
    private IndexWriter indexWriter;
    private IndexWriterConfig indexWriterConfig;
    // private static final Logger logger = LoggerFactory.getLogger(Indexer.class);

    public Indexer(Directory indexDirectory, Analyzer analyzer) {
        
        this.indexWriterConfig = new IndexWriterConfig(analyzer);
        
        try {
            this.indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);
        } catch (IOException e) {
           
            System.out.println("Failed index writer");
            e.printStackTrace();
        }
   }
   
   public void indexNewDocument(String title, String body) throws IOException{
        Document document = new Document();
        
        if(title != null){
            document.add(new TextField("TITLE",title, Field.Store.YES));
        }
        document.add(new TextField("BODY",body, Field.Store.YES));

        indexWriter.addDocument(document);
        // indexWriter.commit();
   }

    public void close() throws IOException{
        indexWriter.commit();
        indexWriter.close();
        
    }

   
}
