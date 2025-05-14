package com.ir.searchengine.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties properties = new Properties();

    static {
        try(InputStream input = Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE)){
         
            if ( input != null) {

                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("Failed to load config.properties");
            e.printStackTrace();
        }

        for (String key : properties.stringPropertyNames()){
            String val = System.getProperty(key);
            if (val != null) {
                properties.setProperty(key, val);
            }
        }
    }

    public static String get(String key){
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultVal){
        return properties.getProperty(key, defaultVal);
    }

}
