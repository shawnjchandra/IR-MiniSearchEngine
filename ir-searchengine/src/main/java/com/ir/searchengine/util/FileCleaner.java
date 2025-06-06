package com.ir.searchengine.util;

import java.io.File;

public class FileCleaner {
    public static boolean clearIndexDirectory(String indexPath) {
    
    System.gc();
    
    if (indexPath == null || indexPath.isEmpty()) {
        System.err.println("Index directory path is not configured");
        return false;
    }
    File indexDir = new File(indexPath);

    // Cek kalo ada pathnya
    if (!indexDir.exists()) {
        indexDir.mkdirs();
        System.out.println("Directory has been created");
        return true;
    }
    
    // Cek kalo tipenya directory ato bukan
    if (!indexDir.isDirectory()) {
        System.err.println("Index Path exist but not a directory");
        return false;
    }
    
    boolean success = true;
    File[] files = indexDir.listFiles();
    if (files != null) {
        for (File file : files) {
            if (file.isDirectory()) {
                success &= deleteDirectoryContents(file);
                if (!file.delete()) {
                    System.err.println("Failed to delete directory: " + file.getAbsolutePath());
                    success = false;
                }
            } else {
                if (!file.delete()) {
                    System.err.println("Failed to delete file: " + file.getAbsolutePath());
                    System.err.println("File exists: " + file.exists());
                    System.err.println("Can write: " + file.canWrite());
                    success = false;
                }
            }
        }
    }
    
    return success;
}

    private static boolean deleteDirectoryContents(File directory) {
    boolean success = true;
    File[] files = directory.listFiles();
    if (files != null) {
        for (File file : files) {
            if (file.isDirectory()) {
                success &= deleteDirectoryContents(file);
                if (!file.delete()) {
                    System.err.println("Failed to delete file (directory) : "+ file.getName());
                    success = false;
                }
            } else {
                if (!file.delete()) {
                    System.err.println("Failed to delete file: " + file.getName());
                    success = false;
                }
            }
        }
    }
    return success;
}
}


