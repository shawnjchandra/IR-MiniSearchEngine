package com.ir.searchengine.data;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class DocumentScore {
  
    public int docId;
    public double score;

    public DocumentScore(int docId, double score) {
        this.docId = docId;
        this.score = score;
    }
    
}
