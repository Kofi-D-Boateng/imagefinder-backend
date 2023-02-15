package com.kboat.imagefinder.model.graph;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@FunctionalInterface
public interface GraphTraversal  {
    // Performs a traversal of a Graph ADT
    void traverse(String url, Set<String> visitedNodes, Set<String> visitingNodes, Map<String,Set<String>> valueList) throws IOException, InterruptedException;
}
