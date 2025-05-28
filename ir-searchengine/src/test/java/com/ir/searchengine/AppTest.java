package com.ir.searchengine;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        

        // String text = "Lucene is a powerful text search engine library written in Java.";
        // List<String> tokens;
        // try {

        //     Preprocess preprocess = new Preprocess();
        //     tokens = preprocess.tokenize(text);
        //     System.out.println(tokens);
        //     List<String> expected = List.of("lucene","is","a", "powerful", "text", "search", "engine", "library", "written", "in","java");
    

        //     assertEquals(expected, tokens);
        // } catch (IOException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }    

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
        
    }
}
