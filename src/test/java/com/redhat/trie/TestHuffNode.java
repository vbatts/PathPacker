package com.redhat.trie;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TestHuffNode {

    @Test
    public void testNew(){
        HuffNode hn = new HuffNode(new Object(), 1);
        assertNotNull(hn);
    }
}


