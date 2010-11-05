package fnug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Tarjan {

    private HashMap<String, Node> nodes = new HashMap<String, Node>();

    private RootNode root;

    public Tarjan(List<Resource> resources) {
        root = new RootNode(resources);
        tarjan(root);
    }

    public Tarjan(Resource... resources) {
        this(Arrays.asList(resources));
    }

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
                List<String> deps = resource.parseRequires();
                for (String dep : deps) {
                    Resource res = ResourceResolver.getInstance().getResource(dep);
                    if (res != null && !(res instanceof AggregatedResource)) {
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