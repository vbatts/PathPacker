package com.redhat.trie;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestHuffNode {

    @Test
    public void testNew0(){
        HuffNode hn = new HuffNode(new Object(), 0);
        assertNotNull(hn);
    }

    @Test
    public void testNew1(){
        HuffNode hn = new HuffNode(new NodeContext(), new Object(), 0);
        assertNotNull(hn);
    }

    @Test
    public void testNew2(){
        HuffNode hn = new HuffNode(new Object(),
                                   0,
                                   new HuffNode(new Object(), 1),
                                   new HuffNode(new Object(), 2));
        assertNotNull(hn);
    }

    @Test
    public void testNew3(){
        HuffNode hn = new HuffNode(new NodeContext(),
                                   new Object(),
                                   0,
                                   new HuffNode(new Object(), 1),
                                   new HuffNode(new Object(), 2));
        assertNotNull(hn);
    }

    @Test
    public void testWeight(){
        HuffNode hn = new HuffNode(new Object(),
                                   1,
                                   new HuffNode(new Object(), 2),
                                   new HuffNode(new Object(), 3));
        assertEquals(hn.getWeight(), 1);
        assertEquals(hn.getLeft().getWeight(), 2);
        assertEquals(hn.getRight().getWeight(), 3);
    }

    @Test
    public void testValue(){
        HuffNode hn = new HuffNode(new Object(), 1);
        assertTrue(hn.getValue() instanceof Object);
    }

    @Test
    public void testCtx(){
        NodeContext ctx = new NodeContext(0);
        HuffNode hn = new HuffNode(ctx, new Object(), 0);
        assertEquals(hn.getContext(), ctx);

        assertEquals(hn.getId(), 0);
        // it's +1 because the HuffNode will have run ctx.nextId()
        assertEquals(hn.getContext().getId(), 1);
    }

    @Test
    public void testObject(){
        Object obj = new Object();
        HuffNode hn = new HuffNode(obj, 1);
        assertEquals(hn.getValue(), obj);
    }

    @Test
    public void testChildren(){
        Object obj = new Object();
        HuffNode hn = new HuffNode(obj,
                                   1,
                                   new HuffNode(obj, 2),
                                   new HuffNode(obj, 3));
        assertEquals(hn.getValue(), obj);
        assertEquals(hn.getLeft().getValue(), obj);
        assertEquals(hn.getRight().getValue(), obj);
    }

}


