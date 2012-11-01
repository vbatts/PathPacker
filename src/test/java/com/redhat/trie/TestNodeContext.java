package com.redhat.trie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TestNodeContext {

    @Test
    public void testNew1() {
        NodeContext nc = new NodeContext();
        assertNotNull(nc);
    }

    @Test
    public void testNew2() {
        NodeContext nc = new NodeContext(1);
        assertNotNull(nc);
        assertEquals(nc.getId(), 1);
    }

    @Test
    public void testCounter() {
        NodeContext nc = new NodeContext();
        assertEquals(nc.getId(), 0);

        long curr = nc.nextId();
        assertEquals(nc.getId(), 1);
        assertEquals(nc.getId(), (curr + 1));
    }
}


