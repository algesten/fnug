package fnug.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fnug.config.BundleConfig;

/*
 Copyright 2010 Martin Algesten

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/**
 * Implementation of Tarjan's strongly connected components algorithm. Used for finding load order the dependent
 * resources, and to discover cyclic dependencies.
 * 
 * <p>
 * <a href= "http://en.wikipedia.org/wiki/Tarjan's_strongly_connected_components_algorithm"
 * >Tarjan's_strongly_connected_components_algorithm</a>
 * 
 * @author Martin Algesten
 * 
 */
public class Tarjan {

    private final static Logger LOG = LoggerFactory.getLogger(Tarjan.class);

    private HashMap<String, Node> resourceNodes = new HashMap<String, Node>();
    private HashMap<String, Node> bundleNodes = new HashMap<String, Node>();

    private LinkedHashMap<String, LinkedHashSet<String>> bundleDeps = new LinkedHashMap<String, LinkedHashSet<String>>();

    private boolean checkModified;

    private int index = 0;
    private ArrayList<Node> stack = new ArrayList<Node>();

    private ArrayList<ArrayList<Node>> resourceResult = new ArrayList<ArrayList<Node>>();

    private ArrayList<ArrayList<Node>> bundleResult = new ArrayList<ArrayList<Node>>();

    /**
     * Performs a tarjan's calculation of the given resources. These resources are exactly those configured in
     * {@link BundleConfig#files()}, additional dependencies are discovered as part of this algorithm using
     * {@link Resource#findRequiresTags()}.
     * 
     * @param resources
     *            starting resources.
     * @param checkModified
     */
    public Tarjan(List<Resource> resources, boolean checkModified) {

        this.checkModified = checkModified;

        ResourceRootNode resourceRoot = new ResourceRootNode(resources);
        tarjan(resourceRoot, resourceResult);

        index = 0;
        stack.clear();
        BundleRootNode bundleRoot = new BundleRootNode();
        tarjan(bundleRoot, bundleResult);

    }

    /**
     * Same as {@link #Tarjan(List, boolean)}, with checkModified set to false,but provided as array. For testing.
     * 
     * @param resources
     *            resources to start from.
     */
    public Tarjan(Resource... resources) {
        this(Arrays.asList(resources), false);
    }

    /**
     * Returns the result of the algorithm.
     * 
     * @return result of the algorithm.
     */
    public List<Resource> getResult() {

        List<Resource> result = new LinkedList<Resource>();

        for (List<Node> bl : bundleResult) {

            if (bl.size() > 1) {
                StringBuilder bld = new StringBuilder();
                for (Node node : bl) {
                    bld.append(node.getPath() + " -> ");
                }
                bld.append(bl.get(0).getPath());
                throw new IllegalStateException("Found cyclic bundle dependency: " + bld.toString());
            }

            Node bundleNode = bl.get(0);
            if (bundleNode instanceof BundleRootNode) {
                continue;
            }

            String bundleName = bundleNode.getPath();

            for (List<Node> rl : resourceResult) {

                if (rl.size() > 1) {
                    StringBuilder bld = new StringBuilder();
                    for (Node node : rl) {
                        bld.append(node.getPath() + " -> ");
                    }
                    bld.append(rl.get(0).getPath());
                    throw new IllegalStateException("Found cyclic dependency: " + bld.toString());
                }

                Node node = rl.get(0);

                if (node instanceof ResourceRootNode) {
                    continue;
                }

                Resource r = ((ResourceNode) node).getResource();
                Bundle b = ((HasBundle) r).getBundle();
                if (b.getName().equals(bundleName)) {
                    result.add(r);
                }

            }
        }

        return result;
    }

    private void tarjan(Node v, ArrayList<ArrayList<Node>> result) {
        v.setIndex(index);
        v.setLowLink(index);
        index++;
        stack.add(0, v);
        for (Node n : v.getAdjacent()) {
            if (n.getIndex() == -1) {
                tarjan(n, result);
                v.setLowLink(Math.min(v.getLowLink(), n.getLowLink()));
            } else if (stack.contains(n)) {
                v.setLowLink(Math.min(v.getLowLink(), n.getIndex()));
            }
        }
        if (v.getLowLink() == v.getIndex()) {
            Node n;
            ArrayList<Node> component = new ArrayList<Node>();
            do {
                n = stack.remove(0);
                component.add(n);
            } while (n != v);
            result.add(component);
        }
    }

    private abstract class Node {

        private int index = -1;
        private int lowLink;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getLowLink() {
            return lowLink;
        }

        public void setLowLink(int lowLink) {
            this.lowLink = lowLink;
        }
        
        // for testing
        @Override
        public String toString() {
            return getPath();
        }

        public abstract List<Node> getAdjacent();

        public abstract String getPath();

    }

    private class ResourceRootNode extends Node {

        List<Resource> resources;

        ResourceRootNode(List<Resource> resources) {
            this.resources = resources;
        }

        @Override
        public List<Node> getAdjacent() {
            LinkedList<Node> nodes = new LinkedList<Node>();
            for (Resource res : resources) {
                if (checkModified) {
                    res.checkModified();
                }
                nodes.add(getNodeForResource(res));
            }
            return nodes;
        }

        @Override
        public String getPath() {
            return "$$$ROOT$$$";
        }

    }

    private class ResourceNode extends Node {

        Resource resource;
        List<Node> adjacent;

        ResourceNode(Resource resource) {
            this.resource = resource;
        }

        public Resource getResource() {
            return resource;
        }

        @Override
        public List<Node> getAdjacent() {
            if (adjacent == null) {
                adjacent = new LinkedList<Node>();
                if (checkModified) {
                    resource.checkModified();
                }
                List<String> deps = resource.findRequiresTags();
                for (String dep : deps) {
                    Resource res = ResourceResolver.getInstance().resolve(dep);
                    if (res == null) {
                        LOG.warn("No bundle configured to resolve dependency: " + dep);
                    } else if (res instanceof AggregatedResource) {
                        LOG.warn("Ignoring dependent aggregated resource: " + dep);
                    } else {
                        adjacent.add(getNodeForResource(res));

                        // this is where we add discovered bundle dependencies.
                        if (resource instanceof HasBundle && res instanceof HasBundle) {
                            Bundle b1 = ((HasBundle) resource).getBundle();
                            Bundle b2 = ((HasBundle) res).getBundle();
                            if (!b1.getName().equals(b2.getName())) {
                                bundleDeps.get(b1.getName()).add(b2.getName());
                            }
                        }

                    }
                }
            }
            return adjacent;
        }

        @Override
        public String getPath() {
            return resource.getPath();
        }

    }

    private class BundleRootNode extends Node {

        @Override
        public List<Node> getAdjacent() {
            LinkedList<Node> adjacent = new LinkedList<Node>();
            for (String bundleName : bundleDeps.keySet()) {
                adjacent.add(getNodeForBundle(bundleName));
            }
            return adjacent;
        }

        @Override
        public String getPath() {
            return "$$$ROOT$$$";
        }

    }

    private class BundleNode extends Node {

        private String bundleName;

        public BundleNode(String bundleName) {
            this.bundleName = bundleName;
        }

        @Override
        public List<Node> getAdjacent() {
            LinkedList<Node> adjacent = new LinkedList<Node>();
            for (String dep : bundleDeps.get(bundleName)) {
                adjacent.add(getNodeForBundle(dep));
            }
            return adjacent;
        }

        @Override
        public String getPath() {
            return bundleName;
        }

    }

    private Node getNodeForResource(Resource res) {
        Node n = resourceNodes.get(res.getPath());
        if (n == null) {
            n = new ResourceNode(res);
            resourceNodes.put(res.getPath(), n);
            if (!(res instanceof HasBundle)) {
                throw new IllegalStateException("Resource not instanceof HasBundle: " + res.getFullPath());
            }
            Bundle b = ((HasBundle) res).getBundle();
            if (!bundleDeps.containsKey(b.getName())) {
                bundleDeps.put(b.getName(), new LinkedHashSet<String>());
            }
        }
        return n;
    }

    private Node getNodeForBundle(String bundleName) {
        Node n = bundleNodes.get(bundleName);
        if (n == null) {
            n = new BundleNode(bundleName);
            bundleNodes.put(bundleName, n);
        }
        return n;
    }

}