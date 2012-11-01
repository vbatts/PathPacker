package com.redhat.trie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TestNodePair {

    @Test
    public void testNew0() {
        NodePair np = new NodePair("foo", new PathNode());
        assertNotNull(np);
    }


}

