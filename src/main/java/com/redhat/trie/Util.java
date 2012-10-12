/**
 * Copyright (c) 2009 - 2012 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.trie;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.StringTokenizer;

import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

/*
 * Util
 *
 */
public class Util {
    private NodeContext pathNodeContext;
    private NodeContext huffNodeContext;

    public Util() {
        this.pathNodeContext = new NodeContext();
        this.huffNodeContext = new NodeContext();
    }

    NodeContext getPathNodeContext() {
        return this.pathNodeContext;
    }

    NodeContext getHuffNodeContext() {
        return this.huffNodeContext;
    }

    /*
     * populate the parent PathNode, with the Strings in contents
     */
    public PathNode makePathTree(List<String> contents, PathNode parent) {
        PathNode endMarker = new PathNode(getPathNodeContext());
        for (String path : contents) {
            StringTokenizer st = new StringTokenizer(path, "/");
            makePathForURL(st, parent, endMarker);
        }
        //condenseSubTreeNodes(endMarker);
        return parent;
    }

    /* 
     * PrettyPrint a PathNode tree
     */
    public static void printTree(PathNode pn, int tab) {
        StringBuffer nodeRep = new StringBuffer();
        for (int i = 0; i <= tab; i++) {
            nodeRep.append("  ");
        }
        nodeRep.append("Node [");
        nodeRep.append(pn.getId());
        nodeRep.append("]");

        for (PathNode parent : pn.getParents()) {
            nodeRep.append(" ^ [");
            nodeRep.append(parent.getId());
            nodeRep.append("]");
        }
        for (NodePair cp : pn.getChildren()) {
            nodeRep.append(" v [");
            nodeRep.append(cp.getName());
            nodeRep.append(" {");
            nodeRep.append(cp.getConnection().getId());
            nodeRep.append("} ]");
        }
        System.out.println(nodeRep);
        for (NodePair cp : pn.getChildren()) {
            printTree(cp.getConnection(), tab + 1);
        }
    }

    /* 
     * PrettyPrint a HuffNode tree
     */
    public static void printTrie(HuffNode hn, int tab) {
        StringBuffer nodeRep = new StringBuffer();
        for (int i = 0; i <= tab; i++) {
            nodeRep.append("  ");
        }
        nodeRep.append("Node [");
        nodeRep.append(hn.getId());
        nodeRep.append("]");

        nodeRep.append(", Weight [");
        nodeRep.append(hn.getWeight());
        nodeRep.append("]");

        nodeRep.append(", Value = [");
        nodeRep.append(hn.getValue());
        nodeRep.append("]");

        System.out.println(nodeRep);
        if (hn.getLeft() != null) {
            printTrie(hn.getLeft(), tab + 1);
        }
        if (hn.getRight() != null) {
            printTrie(hn.getRight(), tab + 1);
        }
    }

    /*
     * given a tokenized URL path, build out the PathNode parent,
     * and append endMarker to terminal nodes.
     */
    private void makePathForURL(StringTokenizer st, PathNode parent, PathNode endMarker) {
        if (st.hasMoreTokens()) {
            String childVal = st.nextToken();
            if (childVal.equals("")) {
                return;
            }

            boolean isNew = true;
            for (NodePair child : parent.getChildren()) {
                if (child.getName().equals(childVal) &&
                        !child.getConnection().equals(endMarker)) {
                    makePathForURL(st, child.getConnection(), endMarker);
                    isNew = false;
                }
            }
            if (isNew) {
                PathNode next = null;
                if (st.hasMoreTokens()) {
                    next = new PathNode(parent.getContext());
                    parent.addChild(new NodePair(childVal, next));
                    next.addParent(parent);
                    makePathForURL(st, next, endMarker);
                } else {
                    parent.addChild(new NodePair(childVal, endMarker));
                    if (!endMarker.getParents().contains(parent)) {
                        endMarker.addParent(parent);
                    }
                }
            }
        }
    }

    public void condenseSubTreeNodes(PathNode location) {
        // "equivalent" parents are merged
        List<PathNode> parentResult = new ArrayList<PathNode>();
        parentResult.addAll(location.getParents());
        for (PathNode parent1 : location.getParents()) {
            if (!parentResult.contains(parent1)) {
                continue;
            }
            for (PathNode parent2 : location.getParents()) {
                if (!parentResult.contains(parent2) ||
                    parent2.getId() == parent1.getId()) {
                    continue;
                }
                if (parent1.isEquivalentTo(parent2)) {
                    // we merge them into smaller Id
                    PathNode merged = parent1.getId() < parent2.getId() ?
                        parent1 : parent2;
                    PathNode toRemove = parent1.getId() < parent2.getId() ?
                        parent2 : parent1;

                    // track down the name of the string in the grandparent
                    //  that points to parent
                    String name = "";
                    PathNode oneParent = toRemove.getParents().get(0);
                    for (NodePair child : oneParent.getChildren()) {
                        if (child.getConnection().getId() == toRemove.getId()) {
                            name = child.getName();
                            break;
                        }
                    }

                    // copy grandparents to merged parent node.
                    List<PathNode> movingParents = toRemove.getParents();
                    merged.addParents(movingParents);

                    // all grandparents with name now point to merged node
                    for (PathNode pn : toRemove.getParents()) { 
                        for (NodePair child : pn.getChildren()) {
                            if (child.getName().equals(name)) {
                                child.setConnection(merged);
                            }
                        }
                    }
                    parentResult.remove(toRemove);
                }
            }
        }
        location.setParents(parentResult);
        for (PathNode pn : location.getParents()) {
            condenseSubTreeNodes(pn);
        }
    }

    public List<String> orderStrings(PathNode parent) throws IOException {     
        List<String> parts = new ArrayList<String>();                          
        // walk tree to make string map  
        Map<String, Integer> segments =  new HashMap<String, Integer>();       
        Set<PathNode> nodes =  new HashSet<PathNode>();                        
        buildSegments(segments, nodes, parent);                                
        for (String part : segments.keySet()) {                                
            if (!part.equals("")) {
                int count = segments.get(part);                                
                if (parts.size() == 0) {                                       
                    parts.add(part);                                           
                }
                else {
                    int pos = parts.size();
                    for (int i = 0; i < parts.size(); i++) {
                        if (count < segments.get(parts.get(i))) {              
                            pos = i;                                           
                            break;                                             
                        }                                                      
                    }
                    parts.add(pos, part);                                      
                }                                                              
            }                                                                  
        }
        return parts;                                                          
    }                                                                          

    private void buildSegments(Map<String, Integer> segments,
        Set<PathNode> nodes, PathNode parent) {
        if (!nodes.contains(parent)) {
            nodes.add(parent);
            for (NodePair np : parent.getChildren()) {
                Integer count = segments.get(np.getName());
                if (count == null) {
                    count = new Integer(0);
                }
                segments.put(np.getName(), ++count);
                buildSegments(segments, nodes, np.getConnection());
            }
        }
    }

    private List<PathNode> orderNodes(PathNode treeRoot) {
        List<PathNode> result = new ArrayList<PathNode>();

        // walk tree to make string map
        Set<PathNode> nodes =  getPathNodes(treeRoot);
        for (PathNode pn : nodes) {
            int count = pn.getParents().size();
            if (nodes.size() == 0) {
                nodes.add(pn);
            }
            else {
                int pos = result.size();
                for (int i = 0; i < result.size(); i++) {
                    if (count <= result.get(i).getParents().size()) {
                        pos = i;
                        break;
                    }
                }
                result.add(pos, pn);
            }
        }
        return result; 
    }

    private Set<PathNode> getPathNodes(PathNode treeRoot) {
        Set<PathNode> nodes = new HashSet<PathNode>();
        nodes.add(treeRoot);
        for (NodePair np : treeRoot.getChildren()) {
            nodes.addAll(getPathNodes(np.getConnection()));
        }
        return nodes;
    }

    private byte[] makeNodeDictionary(HuffNode stringParent,
        HuffNode pathNodeParent, List<PathNode> pathNodes)
        throws UnsupportedEncodingException, IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int nodeSize = pathNodes.size();
        if (nodeSize > 127) {
            ByteArrayOutputStream countBaos = new ByteArrayOutputStream();
            boolean start = false;
            for (byte b : toByteArray(nodeSize)) {
                if (b == 0 && !start) {
                    continue;
                }
                else {
                    countBaos.write(b);
                    start = true;
                }
            }
            baos.write(128 + countBaos.size());
            countBaos.close();
            baos.write(countBaos.toByteArray());
        }
        else {
            baos.write(nodeSize);
        }
        StringBuffer bits = new StringBuffer();
        String endNodeLocation = findHuffPath(stringParent, HuffNode.END_NODE);
        for (PathNode pn : pathNodes) {
            for (NodePair np : pn.getChildren()) {
                bits.append(findHuffPath(stringParent, np.getName()));
                bits.append(findHuffPath(pathNodeParent, np.getConnection()));
            }
            bits.append(endNodeLocation);
            while (bits.length() >= 8) {
                int next = 0;
                for (int i = 0; i < 8; i++) {
                    next = (byte) next << 1;
                    if (bits.charAt(i) == '1') {
                        next++;
                    }
                }
                baos.write(next); 
                bits.delete(0, 8); 
            } 
        } 
 
        if (bits.length() > 0) { 
            int next = 0; 
            for (int i = 0;  i < 8; i++) { 
                next = (byte) next << 1; 
                if (i < bits.length() && bits.charAt(i) == '1') { 
                    next++; 
                } 
            } 
            baos.write(next); 
        } 
        byte[] result = baos.toByteArray(); 
        /* FIXME add debugging? :-)
        if (treeDebug) { 
            ByteArrayInputStream bais = new ByteArrayInputStream(result); 
            int value = bais.read(); 
            while (value != -1) { 
                log.debug(value); 
                value = bais.read(); 
            } 
        } 
        */
        baos.close(); 
        return result; 
    } 

    private byte[] toByteArray(int value) {
        return new byte[] {
            (byte) (value >> 24),
            (byte) (value >> 16),
            (byte) (value >> 8),
            (byte) value};
    }

    public String findHuffPath(HuffNode trie, Object need) {
        HuffNode left = trie.getLeft();
        HuffNode right = trie.getRight();
        if (left != null && left.getValue() != null) {
            if (need.equals(left.getValue())) {
                return "0";
            }
        }
        if (right != null && right.getValue() != null) {
            if (need.equals(right.getValue())) {
                return "1";
            }
        }
        if (left != null) {
            String leftPath = findHuffPath(left, need);
            if (leftPath.length() > 0) {
                return "0" + leftPath;
            }
        }
        if (right != null) {
            String rightPath = findHuffPath(right, need);
            if (rightPath.length() > 0) {
                return "1" + rightPath;
            }
        }
        return "";
    }

    private List<HuffNode> getStringNodeList(List<String> pathStrings) {
        List<HuffNode> nodes = new ArrayList<HuffNode>();
        int idx = 1;
        for (String part : pathStrings) {
            nodes.add(new HuffNode(getHuffNodeContext(), part, idx++));
        }
        nodes.add(new HuffNode(HuffNode.END_NODE, idx));
        return nodes;
    }

    private List<HuffNode> getPathNodeNodeList(List<PathNode> pathNodes) {
        List<HuffNode> nodes = new ArrayList<HuffNode>();
        int idx = 0;
        for (PathNode pn : pathNodes) {
            nodes.add(new HuffNode(getHuffNodeContext(), pn, idx++));
        }
        return nodes;
    }

    public HuffNode makeTrie(List<HuffNode> nodesList) {
        // drop the first node if path node value, it is not needed
        if (nodesList.get(0).getValue() instanceof PathNode) {
            nodesList.remove(0);
        }
        while (nodesList.size() > 1) {
            int node1 = findSmallest(-1, nodesList);
            int node2 = findSmallest(node1, nodesList);
            HuffNode hn1 = nodesList.get(node1);
            HuffNode hn2 = nodesList.get(node2);
            HuffNode merged = mergeNodes(hn1, hn2);
            nodesList.remove(hn1);
            nodesList.remove(hn2);
            nodesList.add(merged);
        }
        /*
        if (treeDebug) {
            printTrie(nodesList.get(0), 0);
        }
        */
        return nodesList.get(0);
    }

    private byte[] byteProcess(List<String> entries)
        throws IOException, UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos,
            new Deflater(Deflater.BEST_COMPRESSION));
        for (String segment : entries) {
            dos.write(segment.getBytes("UTF-8"));
            dos.write("\0".getBytes("UTF-8"));
        }
        dos.finish();
        dos.close();
        return baos.toByteArray();
    }

    private int findSmallest(int exclude, List<HuffNode> nodes) {
        int smallest = -1;
        for (int index = 0; index < nodes.size(); index++) {
            if (index == exclude) {
                continue;
            }
            if (smallest == -1 || nodes.get(index).getWeight() <
                nodes.get(smallest).getWeight()) {
                smallest = index;
            }
        }
        return smallest;
    }

    private HuffNode mergeNodes(HuffNode node1, HuffNode node2) {
        HuffNode left = node1;
        HuffNode right = node2;
        HuffNode parent = new HuffNode(getHuffNodeContext(),
                null, left.getWeight() + right.getWeight(), left, right);
        return parent;
    }
    
    /* fix breakoff of hydrateContentPackage */
    private List<String> byteArrayToStrings(byte[] ba) {
        List<String> strings = new ArrayList<String>();
        String str = "";

        for (byte b : ba) {
            if (b == '\0') {
                strings.add(str);
                str = "";
            } else {
                str += (char) b;

            }
        }
        return strings;
    }

    /*
     * FIXME - break this apart, so that the hydrated payload
     *         can be structure to more quickly search, and use less memory
     */
    public List<String> hydrateContentPackage(byte[] payload)
        throws IOException, UnsupportedEncodingException {
        List<HuffNode> pathDictionary = new ArrayList<HuffNode>();
        List<HuffNode> nodeDictionary = new ArrayList<HuffNode>();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Inflater i = new Inflater();
        InflaterOutputStream ios = new InflaterOutputStream(baos, i);
        ios.write(payload);
        ios.finish();
        long read = i.getBytesRead();

        int weight = 1;
        for (String name : byteArrayToStrings(baos.toByteArray())) {
            pathDictionary.add(new HuffNode(getHuffNodeContext(), name, weight++));
        }
        pathDictionary.add(new HuffNode(HuffNode.END_NODE, weight));
        List<HuffNode> triePathDictionary = new ArrayList<HuffNode>();
        triePathDictionary.addAll(pathDictionary);
        HuffNode pathTrie = makeTrie(triePathDictionary);

        StringBuffer nodeBits = new StringBuffer();
        ByteArrayInputStream bais = new ByteArrayInputStream(payload,
            (new Long(read)).intValue(), (new Long(payload.length - read).intValue()));
        int value = bais.read();
        // check for size bits
        int nodeCount = value;
        if (value > 127) {
            byte[] count = new byte[value - 128];
            bais.read(count);
            int total = 0;
            for (int k = 0; k < value - 128; k++) {
                total = (total << 8) | (count[k] & 0xFF);
            }
            nodeCount = total;
        }
        value = bais.read();
        while (value != -1) {
            String someBits = Integer.toString(value, 2);
            for (int pad = 0; pad < 8 - someBits.length(); pad++) {
                nodeBits.append("0");
            }
            nodeBits.append(someBits);
            value = bais.read();
        }
        for (int j = 0; j < nodeCount; j++) {
            nodeDictionary.add(new HuffNode(new PathNode(), j));
        }
        List<HuffNode> trieNodeDictionary = new ArrayList<HuffNode>();
        trieNodeDictionary.addAll(nodeDictionary);
        HuffNode nodeTrie = makeTrie(trieNodeDictionary);

        // populate the PathNodes so we can rebuild the cool url tree
        Set<PathNode> pathNodes =  populatePathNodes(nodeDictionary,
            pathTrie, nodeTrie, nodeBits);
        // find the root, he has no parents
        PathNode root = null;
        for (PathNode pn : pathNodes) {
            if (pn.getParents().size() == 0) {
                root = pn;
                break;
            }
        }
        // time to make the doughnuts
        List<String> urls = new ArrayList<String>();
        StringBuffer aPath = new StringBuffer();
        makeURLs(root, urls, aPath);
        return urls;
    }

    private Set<PathNode> populatePathNodes(List<HuffNode> nodeDictionary,
        HuffNode pathTrie, HuffNode nodeTrie, StringBuffer nodeBits) {
        Set<PathNode> pathNodes = new HashSet<PathNode>();
        for (HuffNode node : nodeDictionary) {
            pathNodes.add((PathNode) node.getValue());
            boolean stillNode = true;
            while (stillNode) {
                // get first child name
                // if its HuffNode.END_NODE we are done
                String nameValue = null;
                StringBuffer nameBits = new StringBuffer();
                while (nameValue == null && stillNode) {
                    nameBits.append(nodeBits.charAt(0));
                    nodeBits.deleteCharAt(0);
                    Object lookupValue = findHuffNodeValueByBits(pathTrie,
                        nameBits.toString());
                    if (lookupValue != null) {
                        if (lookupValue.equals(HuffNode.END_NODE)) {
                            stillNode = false;
                            break;
                        }
                        nameValue = (String) lookupValue;
                    }
                    if (nodeBits.length() == 0) {
                        stillNode = false;
                    }
                }

                PathNode nodeValue = null;
                StringBuffer pathBits = new StringBuffer();
                while (nodeValue == null && stillNode) {
                    pathBits.append(nodeBits.charAt(0));
                    nodeBits.deleteCharAt(0);
                    PathNode lookupValue = (PathNode) findHuffNodeValueByBits(nodeTrie,
                        pathBits.toString());
                    if (lookupValue != null) {
                        nodeValue = lookupValue;
                        nodeValue.addParent((PathNode) node.getValue());
                        ((PathNode) node.getValue()).addChild(
                            new NodePair(nameValue, nodeValue));
                    }
                    if (nodeBits.length() == 0) {
                        stillNode = false;
                    }
                }
            }
        }
        return pathNodes;
    }

    public Object findHuffNodeValueByBits(HuffNode trie, String bits) {
        HuffNode left = trie.getLeft();
        HuffNode right = trie.getRight();

        if (bits.length() == 0) {
            return trie.getValue();
        }

        char bit = bits.charAt(0);
        if (bit == '0') {
            if (left == null) { throw new RuntimeException("Encoded path not in trie"); }
            return findHuffNodeValueByBits(left, bits.substring(1));
        }
        else if (bit == '1') {
            if (right == null) { throw new RuntimeException("Encoded path not in trie"); }
            return findHuffNodeValueByBits(right, bits.substring(1));
        }
        return null;
    }

    private void makeURLs(PathNode root, List<String> urls, StringBuffer aPath) {
        if (root.getChildren().size() == 0) {
            urls.add(aPath.toString());
        }
        for (NodePair child : root.getChildren()) {
            StringBuffer childPath = new StringBuffer(aPath.substring(0));
            childPath.append("/");
            childPath.append(child.getName());
            makeURLs(child.getConnection(), urls, childPath);
        }
    }


}

