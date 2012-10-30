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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;
import java.util.zip.DataFormatException;

/**
 * PathTree
 *
 * An efficient means by which to check the content sets.
 *
 * TODO - this is a prototype stub
 */
public class PathTree {
    private List<HuffNode> nodeDictionary;
    private List<HuffNode> pathDictionary;
    private StringBuffer nodeBits; // TODO make a smart getter for this
    private HuffNode nodeTrie;
    private byte[] payload; // FIXME - may not be needed
    
    private NodeContext pathNodeContext;
    private NodeContext huffNodeContext;

    /**
     * Length of bits read from initial Inflater stream of the payload.
     * Also, this is the offset in the payload.
     *
     */
    private long dictOffset;

    /** 
     * storage for the count of nodes in the packed tree.
     */
    private int nodeCount;

    private boolean modified;

    public PathTree() {
    }

    public PathTree(byte[] payload) {
        setPayload(payload);
    }

    public void setPayload(byte[] payload) {
        this.modified = true;
        this.nodeBits = null;
        this.nodeCount = 0;

        this.pathNodeContext = new NodeContext();
        this.huffNodeContext = new NodeContext();

        this.payload = payload;

        //inflatePathDict

        this.modified = false;
    }

    private NodeContext getPathNodeContext() {
        return this.pathNodeContext;
    }

    private NodeContext getHuffNodeContext() {
        return this.huffNodeContext;
    }

    public long getDictOffset() {
        return this.dictOffset;
    }

    public int getNodeCount() {
        return this.nodeCount;
    }

    /**
     * getter for the compressed payload blob.
     *
     * TODO - add logic to build the payload, it the object was constructed from contentSets
     *
     * @return      byte array of deflated dict and tree.
     */
    public byte[] getPayload() {
        return this.payload;
    }

    /**
     * the buffer of significant bits, with regard to how many nodes there are.
     * 
     * @return      StringBuffer of 
     */
    private StringBuffer getNodeBits() {
        return this.nodeBits;
    }

    private void setDictOffset(long offset) {
        this.dictOffset = offset;
    }

    private void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    /**
     * get the PathNode dictionary. If it is not already built, then get it from the payload
     *
     * @return      List of HuffNode's, with the value set as a the PathNode object 
     * @throws PayloadException  if the relevant section of the payload is not readable
     */
    private List<HuffNode> getPathDictionary() throws PayloadException {
        if (this.modified || this.pathDictionary == null) {
            this.pathDictionary = new ArrayList<HuffNode>();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Inflater inf = new Inflater();
            InflaterOutputStream ios = new InflaterOutputStream(baos, inf);
            try {
                ios.write(getPayload());
                ios.finish();
            } catch (IOException ex) {
                throw new PayloadException();
            }
            setDictOffset(inf.getBytesRead());

            int weight = 1;
            for (String name : byteArrayToStringList(baos.toByteArray())) {
                this.pathDictionary.add(new HuffNode(getHuffNodeContext(), name, weight++));
            }
            this.pathDictionary.add(new HuffNode(HuffNode.END_NODE, weight));
        }
        return this.pathDictionary;
    }

    /**
     * This returns the list of weighted HuffNode from the packed nodes.
     * If the payload has been set, this should regenerate.
     *
     * @return  list of weighted HuffNode's
     * @throws  PayloadException     if the offsetted payload is not readable
     */
    private List<HuffNode> getNodeDictionary() throws PayloadException {
        if (this.pathDictionary == null) {
            getPathDictionary(); // this has to run before the nodeDictionary bits are ready
        }
        if (this.modified || this.pathDictionary == null || this.nodeDictionary == null) {
            this.nodeDictionary = new ArrayList<HuffNode>();
            this.nodeBits = new StringBuffer();

            ByteArrayInputStream bais = new ByteArrayInputStream(getPayload(),
                (new Long(getDictOffset())).intValue(), (new Long(getPayload().length - getDictOffset()).intValue()));
            int value = bais.read();
            // check for size bits
            setNodeCount(value);
            if (value > 127) {
                byte[] count = new byte[value - 128];
                try {
                    bais.read(count);
                } catch (IOException ex) {
                    throw new PayloadException();
                }
                int total = 0;
                for (int k = 0; k < value - 128; k++) {
                    total = (total << 8) | (count[k] & 0xFF);
                }
                setNodeCount(total);
            }
            value = bais.read();
            while (value != -1) {
                String someBits = Integer.toString(value, 2);
                for (int pad = 0; pad < 8 - someBits.length(); pad++) {
                    this.nodeBits.append("0");
                }
                this.nodeBits.append(someBits);
                value = bais.read();
            }

            for (int j = 0; j < getNodeCount(); j++) {
                this.nodeDictionary.add(new HuffNode(new PathNode(getPathNodeContext()), j));
            }
        }
        return this.nodeDictionary;
    }

    /**
     * get the HuffNode trie of the path dictionary
     *
     * @return      the populated HuffNode trie of the PathNode dictionary
     * @throws  PayloadException     if the newly read PathNode dictionary can not be read from the payload
     */
    public HuffNode getPathTrie() throws PayloadException {
        try {
            return makeTrie(getPathDictionary());
        } catch (PayloadException ex) {
            throw ex;
        }
    }

    /**
     * get the HuffNode trie of the node dictionary
     *
     * @return      the populated HuffNode trie of the Node name dictionary
     * @throws  PayloadException     if the newly read Node name dictionary can not be read from the payload
     */
    public HuffNode getNodeTrie() throws PayloadException {
        try {
            return makeTrie(getNodeDictionary());
        } catch (PayloadException ex) {
            throw ex;
        }
    }

    public PathNode getRootPathNode() throws PayloadException {
        // populate the PathNodes so we can rebuild the cool url tree
        Set<PathNode> pathNodes;
        try {
            pathNodes =  populatePathNodes(getNodeDictionary(),
                getPathTrie(), getNodeTrie(), getNodeBits());
        } catch (PayloadException ex) {
            throw ex;
        }
        // find the root, he has no parents
        PathNode root = null;
        for (PathNode pn : pathNodes) {
            if (pn.getParents().size() == 0) {
                root = pn;
                break;
            }
        }
        return root;
    }

    /**
     * TODO - this is a stub
     */
    public boolean validate(String contentPath) {
        return false;
    }

    /*
     * TODO - this is a stub
    public String toString() {
        return "Dict: " + dict + ", Tree: " + tree;
    }
    */

    private List<String> byteArrayToStringList(byte[] ba) {
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

    /**
     * Make a HuffNode trie from a list of weighted HuffNodes
     *
     * @param: nodesList List of individual HuffNode, that have been properly weighted
     */
    public HuffNode makeTrie(List<HuffNode> nodesList) {
        List<HuffNode> trieNodesList = new ArrayList<HuffNode>();

        trieNodesList.addAll(nodesList);

        // drop the first node if path node value, it is not needed
        if (trieNodesList.get(0).getValue() instanceof PathNode) {
            trieNodesList.remove(0);
        }
        while (trieNodesList.size() > 1) {
            int node1 = findSmallest(-1, trieNodesList);
            int node2 = findSmallest(node1, trieNodesList);
            HuffNode hn1 = trieNodesList.get(node1);
            HuffNode hn2 = trieNodesList.get(node2);
            HuffNode merged = mergeNodes(hn1, hn2);
            trieNodesList.remove(hn1);
            trieNodesList.remove(hn2);
            trieNodesList.add(merged);
        }
        /*
        if (treeDebug) {
            printTrie(trieNodesList.get(0), 0);
        }
        */
        return trieNodesList.get(0);
    }

    /**
     * build out the path nodes with their weighted position
     *
     * @return  the Set of weighted PathNode
     */
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

    /**
     * Return the list of all the content sets in the packed payload
     *
     * @return   all the content sets! (unless there was a PayloadException, then empty list)
     */
    public List<String> toList() {
        List<String> urls = new ArrayList<String>();
        StringBuffer aPath = new StringBuffer();
        try {
            makeURLs(getRootPathNode(), urls, aPath);
        } catch (PayloadException ex) {
            // swallow it, I guess. return empty list
        }
        return urls;
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

    private Object findHuffNodeValueByBits(HuffNode trie, String bits) {
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
    
    /*
     * TODO - not sure where all these are to be used

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
    */

}

