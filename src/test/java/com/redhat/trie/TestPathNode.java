package com.redhat.trie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TestPathNode {

    @Test
    public void testNew0() {
        PathNode pn = new PathNode();
        assertNotNull(pn);
    }

    @Test
    public void testNew1() {
        PathNode pn = new PathNode(new NodeContext());
        assertNotNull(pn);
    }

}

