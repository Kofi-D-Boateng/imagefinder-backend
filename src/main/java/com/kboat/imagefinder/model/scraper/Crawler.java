package com.kboat.imagefinder.model.scraper;


import com.kboat.imagefinder.enums.CrawlMode;

import java.io.IOException;

public interface Crawler {
    int itemsFound();
    void startCrawler(CrawlMode mode) throws IOException, InterruptedException;
}
