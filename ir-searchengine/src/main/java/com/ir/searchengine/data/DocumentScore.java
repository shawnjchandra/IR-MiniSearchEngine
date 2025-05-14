package com.ir.searchengine.searching;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class DocumentScore {
  
    public int docId;
    public double score;

    DocumentScore(int docId, double score) {
        this.docId = docId;
        this.score = score;
    }
    
}
