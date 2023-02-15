package com.kboat.imagefinder.controllers;


import com.kboat.imagefinder.enums.CrawlMode;
import com.kboat.imagefinder.model.preprocessor.RegexProcessor;
import com.kboat.imagefinder.model.scraper.Scraper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping(path = "/api/v1/")
public class ImageFinder {


    @GetMapping("find")
    public ResponseEntity<ConcurrentHashMap<String, ConcurrentHashMap<String, Set<String>>>> findImages(@RequestParam("url") String[] urls, @RequestParam("mode")CrawlMode mode){
        System.out.println("urls = " + Arrays.toString(urls));
        System.out.println("mode = " + mode);
        RegexProcessor processor = new RegexProcessor("^(https?:\\/\\/)(www\\.)?([a-zA-Z0-9]+\\.)+[a-zA-Z]{2,}(\\/[a-zA-Z0-9-_]*)*\\/?$");
        Set<String> urlSet = processor.processStrings(urls);
        if(urlSet.size() == 0){
            return new ResponseEntity<>(new ConcurrentHashMap<>(), HttpStatus.BAD_REQUEST);
        }
        Scraper scraper = new Scraper(urlSet,new ConcurrentHashMap<>(),processor);

        try {
            scraper.startCrawler(mode);
        }catch (IOException | InterruptedException e){
            if(scraper.itemsFound() > 0){
                return new ResponseEntity<>(scraper.getImagesFound(),HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ConcurrentHashMap<>(),HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(scraper.getImagesFound(),HttpStatus.OK);
    }
}
