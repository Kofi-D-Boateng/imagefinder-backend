package com.kboat.imagefinder.model.graph;


import com.kboat.imagefinder.enums.CrawlMode;
import com.kboat.imagefinder.model.preprocessor.RegexProcessor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public abstract class AbstractWebGraphADT implements GraphTraversal {
    protected RegexProcessor processor;
    protected Set<String> visitingNode;
    protected Set<String> visitedNodes;
    protected ConcurrentHashMap<String, Set<String>> imageMap;
    protected String mainUrl;
    protected CrawlMode mode;

    public AbstractWebGraphADT(String url, ConcurrentHashMap<String,Set<String>> imageMap, RegexProcessor stringProcessor, CrawlMode mode) {
        this.visitingNode = new HashSet<>();
        this.visitedNodes = new HashSet<>();
        this.imageMap = imageMap;
        this.mainUrl = url;
        this.processor = stringProcessor;
        this.mode = mode;
    }

    public Set<String> getVisitingNode() {
        return visitingNode;
    }

    public ConcurrentHashMap<String,Set<String>> getImageMap(){
        return imageMap;
    }

    public String getUrl() {
        return mainUrl;
    }

    public CrawlMode getMode() {return mode;}

    public void beginTraversal() throws IOException, InterruptedException {
        Connection.Response response = Jsoup.connect(this.mainUrl)
                .timeout(10 * 1000)
                .ignoreHttpErrors(true).execute();


        System.out.println("statusCode:= " + response.statusCode());

        if(response.statusCode() < 200 || response.statusCode() > 299){
            System.out.println("[ERROR]: Connection to " + getUrl() + " could not be established....\n Status: "+response.statusCode()+"\n");
            return;
        }
        /*
            We will start our traversal based on the url provided. Within the
            traversal we will accomplish these tasks.

            1: Gather all img, svg, and link attributes
            2: Add those images and svg to our imageMap
            3: Create a queue of processed urls
            4: Continue to traverse.
         */
        Document document = response.parse();
        String baseUrl = document.baseUri();
        Elements imageLists = document.getElementsByTag("img");
        Elements linksList = document.getElementsByTag("a");

        /*
            Performing Map and Filter via Streams:
                Return: valid image src and links within the same domain AND subdomain.
                        place srcs into our main map, create a set to utilize range

            Filter Logic:
                Src strings must pass these criteria
                    1: Exist
                    2: Contain a scheme protocol
         */
        System.out.println("[IN PROGRESS]: Processing images....");
        imageLists.stream()
                .map(element -> element.attr("src"))
                .filter(src -> src.trim().length() > 0 &&
                        ((src.contains("https") || src.contains("http") && (src.startsWith("http") || src.startsWith("https")))))
                .forEach(src ->{
                    if(imageMap.containsKey("img")){
                        imageMap.get("img").add(src);
                    }else{
                        Set<String> list = new HashSet<>();
                        list.add(src);
                        imageMap.putIfAbsent("img",list);
                    }
                });
        System.out.println("[COMPLETED]: Finished processing images....");
        /*
            If mode is "Verbose", we will collect all the images from linked under
            the absolute url, otherwise we will return only the images based on the
            url passed to the crawler.
         */

        if(Objects.equals(this.mode, CrawlMode.VERBOSE)){
            /*
                We will create a thread pool and a countdown lock based off the
                size of the initial links find on the first page to help
                synchronize the thread completion.
             */

            List<Runnable> tasksList = new ArrayList<>();
            linksList.stream()
                    .map(element -> element.absUrl("href"))
                    .filter(href-> href.contains(baseUrl)).forEach(link ->{
                        Runnable task = () ->{
                            try {
                                System.out.println("[IN PROGRESS]: Beginning Link Traversal for:= " + link);
                                traverse(link,visitedNodes,visitingNode,imageMap);
                                System.out.println("[COMPLETED]: Link Traversal Complete");

                            } catch (IOException e) {
                                System.out.println("[ERROR]: Error = IOExecption thrown in href Stream");
                                System.out.println("e = " + e.getMessage());
                            } catch (InterruptedException e) {
                                System.out.println("[ERROR]: Error = InterruptedExecption thrown in href Stream");
                                System.out.println("e = " + e.getMessage());
                            }
                        };
                        tasksList.add(task);
                    });

            ExecutorService service = Executors.newFixedThreadPool(tasksList.size());
            CountDownLatch latch = new CountDownLatch(tasksList.size());
            System.out.println("[STARTING]: Initiating Deep Search....");
            for(Runnable task:tasksList){
                task.run();
                latch.countDown();
            }
            latch.await();
            service.shutdown();
            System.out.println("[COMPLETED]: Completed "+ tasksList.size() + " Traversals");
        }else{
            System.out.println(String.format("[COMPLETED]: Completed traversal for {}",this.mainUrl));
        }

    }

    @Override
    public void traverse(String url, Set<String> visitedNodes, Set<String> visitingNodes, Map<String, Set<String>> imageMap) throws IOException, InterruptedException {

        // If there are no more urls to traverse through we will return
        if(url.trim().length() <= 0) return;
        // Detects whether we are at a discovery node or if we are in a cycle.
        // This will by default return a topological sorting on the graph.
        if(visitingNode.contains(url) || visitedNodes.contains(url)) return;


        visitingNodes.add(url);

        /*
            We will generate a random int between 5 and 10, and
            multiply that number by 1000ms to generate a random
            amount of time to sleep a thread before making
            another HTTP call. This will allow the crawler to
            be "server friendly", and give us a greater chance of
            not getting IP banned.
         */
        int randomDelay = ThreadLocalRandom.current().nextInt(1,5);
        Thread.sleep(randomDelay*100);

        /*
            After the first call to traverse, we will connect to the url via Jsoup.
            Once we connect to the url, we will create a new map to traverse and
            continue to traverse until we run out of links.
         */
        Connection.Response response = Jsoup.connect(url).timeout(10 * 1000).ignoreHttpErrors(true).execute();

        if(response.statusCode() < 200 || response.statusCode() > 299){
            return;
        }

        Document document = response.parse();
        String baseUrl = document.baseUri();
        Elements imageLists = document.getElementsByTag("img");
        Elements linksList = document.getElementsByTag("a");

        System.out.println("[IN PROGRESS]: Processing images....");
        imageLists.stream()
                .map(element -> element.attr("src"))
                .filter(src -> src.trim().length() > 0 && (src.contains("https") || src.contains("http") || src.contains(".com")))
                .forEach(src ->{
                    if(imageMap.containsKey("img")){
                        imageMap.get("img").add(src);
                    }else{
                        Set<String> list = new HashSet<>();
                        list.add(src);
                        imageMap.putIfAbsent("img",list);
                    }
                });
        System.out.println("[COMPLETED]: Finished processing images....");

        Set<String> hrefSet = linksList.stream()
                .map(element -> element.absUrl("href"))
                .filter(href-> href.contains(baseUrl) && !visitingNodes.contains(href) && !visitedNodes.contains(href))
                .collect(Collectors.toSet());

        for(String link:hrefSet){
            System.out.println("[IN PROGRESS]: Traversing discovery edge:= " + link);
            traverse(link,visitedNodes,visitingNodes,imageMap);
        }

        /*
            Removes the url from the visiting nodes list once
            we have successfully traversed all of its edges (links).

            Add the url to visited node set to prevent cycles.
         */
        visitingNodes.remove(url);
        visitedNodes.add(url);
    }
}
