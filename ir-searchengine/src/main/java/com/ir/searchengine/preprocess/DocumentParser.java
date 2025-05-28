package com.ir.searchengine.preprocess;

public class DocumentParser {
    public static class ParsedDocument {
        public final String title;
        public final String body;

        public ParsedDocument (String title, String body){
            this.title = title;
            this.body = body;
        }
    }

    public static ParsedDocument parse(String rawText){
        String[] lines = rawText.split("\n",2);

        if ( lines.length < 2 || !lines[0].toLowerCase().startsWith("title")){
            throw new IllegalArgumentException("Invalid document");
        }

        String title = lines[0].substring("Title:".length()).trim();
        String body = lines[1].trim();
    

        return new ParsedDocument(title, body);
    }
}
