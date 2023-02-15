package com.kboat.imagefinder.model.graph;

import com.kboat.imagefinder.enums.CrawlMode;
import com.kboat.imagefinder.model.preprocessor.RegexProcessor;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
    A Multithreaded WebGraph.

    Refer to AbstractWebGraphADT for implementation details.
 */
public class ConcurrentWebGraphADT extends AbstractWebGraphADT implements Runnable{
    public ConcurrentWebGraphADT(String url, ConcurrentHashMap<String, Set<String>> imageMap, RegexProcessor stringProcessor, CrawlMode mode) {
        super(url, imageMap, stringProcessor, mode);
    }

    @Override
    public void run() {
        try {
            this.beginTraversal();
        } catch (IOException e) {
            System.out.println("CAUGHT IN CLASS: " + ConcurrentWebGraphADT.class);
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("CAUGHT IN CLASS: " + ConcurrentWebGraphADT.class);
            System.out.println("[ERROR]: Thread interruption: " + e);
            e.printStackTrace();
        }
    }
}
