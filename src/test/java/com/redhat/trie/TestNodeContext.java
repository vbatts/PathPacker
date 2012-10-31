package com.redhat.trie;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TestNodeContext {

    @Test
    public void testNew(){
        NodeContext nc = new NodeContext();
        assertNotNull(nc);
    }
}


