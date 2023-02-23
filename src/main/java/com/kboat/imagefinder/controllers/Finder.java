package com.kboat.imagefinder.controllers;

import com.kboat.imagefinder.enums.CrawlMode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface Finder {
    ResponseEntity<ConcurrentHashMap<String, ConcurrentHashMap<String, Set<String>>>> findImages(@RequestParam("url") String url, @RequestParam("mode") CrawlMode mode);
    ResponseEntity<byte[]> downloadImage(@RequestParam("src") String src) throws IOException;
    ResponseEntity<byte[]> downloadZippedImages(@RequestParam("srcs") String commasepSrc) throws IOException;
}
