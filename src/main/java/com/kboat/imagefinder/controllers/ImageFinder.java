package com.kboat.imagefinder.controllers;


import com.kboat.imagefinder.enums.CrawlMode;
import com.kboat.imagefinder.model.preprocessor.RegexProcessor;
import com.kboat.imagefinder.model.scraper.Scraper;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping(path = "/api/v1/")
public class ImageFinder implements Finder {


    @GetMapping("find")
    @Override
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

    @Override
    @GetMapping("download-image")
    public ResponseEntity<byte[]> downloadImage(@RequestParam("src") String src) throws IOException {
        try {
            StringBuilder urlBuilder = new StringBuilder();
            if(!src.startsWith("http") || !src.startsWith("https")){
                urlBuilder.append("https:").append(src);
            }else{
                urlBuilder.append(src);
            }
            byte[] imageBytes = IOUtils.toByteArray(new URL(urlBuilder.toString()).openStream());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(imageBytes.length);
            return new ResponseEntity<>(imageBytes,headers,HttpStatus.OK);
        }catch (IOException e){
            System.out.println(e);
            return new ResponseEntity<>(new byte[0],HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GetMapping("download-zip")
    public ResponseEntity<byte[]> downloadZippedImages(@RequestParam("srcs") String commsepSrc) throws IOException {
        List<String> srcs = new ArrayList<>();

        for(String src:commsepSrc.substring(1,commsepSrc.length()-1).split(",")){
            System.out.println("src = " + src);
            srcs.add(src);
        }
        System.out.println("[IN PROGRESS]: Beginning create of zip file");
        Path tempDir = Files.createTempDirectory("images");
        System.out.println("[IN PROGRESS]: Created directory....");
        srcs.stream().parallel().forEach(src ->{
            try {
                StringBuilder urlBuilder = new StringBuilder();
                if(!src.startsWith("http") && !src.startsWith("https")){
                    urlBuilder.append("https:").append(src);
                }else{
                    urlBuilder.append(src);
                }


                String url = urlBuilder.toString();
                byte[] imageBytes = IOUtils.toByteArray(new URL(url).openStream());

                StringBuilder imagePathBuilder = new StringBuilder("image_").
                        append(UUID.randomUUID()).
                        append(".png");

                Path imagePath = tempDir.resolve(imagePathBuilder.toString());
                Files.write(imagePath,imageBytes);
            } catch (IOException e) {
                System.out.println(e);
            }
        });
        System.out.println("[COMPLETED]: Resolved available src strings....");
        System.out.println("[IN PROGRESS]: Beginning File walk....");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream stream = new ZipOutputStream(outputStream);
        Files.walk(tempDir)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path ->{
                    try {
                        ZipEntry zipEntry = new ZipEntry(tempDir.relativize(path).toString());
                        stream.putNextEntry(zipEntry);
                        stream.write(Files.readAllBytes(path));
                        stream.closeEntry();
                    } catch (IOException e) {
                        System.out.println(e);
                    }
                });
        System.out.println("[COMPLETED]: Created byte stream... Closing stream now....");
        stream.close();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment","images.zip");
        headers.setContentLength(outputStream.size());
        return new ResponseEntity<>(outputStream.toByteArray(),headers,HttpStatus.OK);
    }
}
