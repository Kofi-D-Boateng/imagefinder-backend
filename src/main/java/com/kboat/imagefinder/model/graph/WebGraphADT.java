package com.kboat.imagefinder.model.graph;

/*
    A Non-Multithreaded WebGraph.

    Refer to AbstractWebGraphADT for implementation details.
 */

import com.kboat.imagefinder.enums.CrawlMode;
import com.kboat.imagefinder.model.preprocessor.RegexProcessor;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebGraphADT extends AbstractWebGraphADT{

    public WebGraphADT(String url, ConcurrentHashMap<String, Set<String>> imageMap, RegexProcessor processor, CrawlMode mode) {
        super(url, imageMap, processor, mode);
    }

}
