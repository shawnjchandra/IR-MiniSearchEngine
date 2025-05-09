package com.ir.searchengine.util;

import java.io.File;

public class FileCleaner {
    public static void clearIndexDirectory(String indexPath){
        File indexDir = new File(indexPath);

        if(indexDir.exists() && indexDir.isDirectory()){
            deleteFilesRecursive(indexDir);
        }
    }

    private static void deleteFilesRecursive(File file){
        if (file.isDirectory()){
            File[] files = file.listFiles();
            if (files != null){
                for (File f : files){
                    deleteFilesRecursive(f);
                }
            }
        }else{
            file.delete();

        }
        
    }
}


