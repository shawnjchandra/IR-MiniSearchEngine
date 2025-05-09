package com.ir.searchengine;

import java.io.IOException;
import java.io.ObjectInputFilter.Config;
import java.util.List;

import com.ir.searchengine.preprocess.Preprocess;

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
        
    }
}
