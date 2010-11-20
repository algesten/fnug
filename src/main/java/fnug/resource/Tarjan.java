package fnug.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
 * Implementation of Tarjan's strongly connected components algorithm. Used for
 * finding load order the dependent resources, and to discover cyclic
 * dependencies.
 * 
 * <p>
 * <a href=
 * "http://en.wikipedia.org/wiki/Tarjan's_strongly_connected_components_algorithm"
 * >Tarjan's_strongly_connected_components_algorithm</a>
 * 
 * @author Martin Algesten
 * 
 */
public class Tarjan {

    private final static Logger LOG = LoggerFactory.getLogger(Tarjan.class);

    private HashMap<String, Node> nodes = new HashMap<String, Node>();

    private RootNode root;

    private boolean checkModified;

    /**
     * Performs a tarjan's calculation of the given resources. These resources
     * are exactly those configured in {@link BundleConfig#files()}, additional
     * dependencies are discovered as part of this algorithm using
     * {@link Resource#findRequiresTags()}.
     * 
     * @param resources
     *            starting resources.
     * @param checkModified
     */
    public Tarjan(List<Resource> resources, boolean checkModified) {
        root = new RootNode(resources);
        this.checkModified = checkModified;
        tarjan(root);
    }

    /**
     * Same as {@link #Tarjan(List, boolean)}, with checkModified set to
     * false,but provided as array. For testing.
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
    public List<List<Resource>> getResult() {
        List<List<Resource>> r = new LinkedList<List<Resource>>();
        for (List<Node> n : result) {
            List<Resource> inner = new LinkedList<Resource>();
            for (Node node : n) {
                if (node instanceof ResourceNode) {
                    inner.add(((ResourceNode) node).getResource());
                }
            }
            if (!inner.isEmpty()) {
                r.add(inner);
            }
        }
        return r;
    }

    private int index = 0;
    private ArrayList<Node> stack = new ArrayList<Node>();
    private ArrayList<ArrayList<Node>> result = new ArrayList<ArrayList<Node>>();

    private ArrayList<ArrayList<Node>> tarjan(Node v) {
        v.setIndex(index);
        v.setLowLink(index);
        index++;
        stack.add(0, v);
        for (Node n : v.getAdjacent()) {
            if (n.getIndex() == -1) {
                tarjan(n);
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
        return result;
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

        public abstract List<Node> getAdjacent();

        public abstract String getPath();

    }

    private class RootNode extends Node {

        List<Resource> resources;

        RootNode(List<Resource> resources) {
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

    private Node getNodeForResource(Resource res) {
        Node n = nodes.get(res.getPath());
        if (n == null) {
            n = new ResourceNode(res);
            nodes.put(res.getPath(), n);
        }
        return n;
    }
}