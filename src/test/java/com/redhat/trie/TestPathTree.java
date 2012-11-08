package com.redhat.trie;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.HashSet;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestPathTree {

    @Test
    public void testNew0() {
        PathTree pt = new PathTree();
        assertNotNull(pt);
    }

    @Test
    public void testNew1() {
        PathTree pt = new PathTree();
        List<String> contents = TestHelpers.loadContents(this, "contents.list");
        try {
            pt = new PathTree(contents);
        } catch (PayloadException ex) {
            fail(ex.toString());
        }
        assertNotNull(pt);
    }

    @Test
    public void testNew2() {
        PathTree pt = new PathTree();
        List<String> contents = TestHelpers.loadContents(this, "contents.list");
        try {
            pt.setContentSets(contents);
        } catch (PayloadException ex) {
            fail(ex.toString());
        }
        assertNotNull(pt);
    }

    @Test
    public void testValidation() {
        PathTree pt = new PathTree();
        List<String> contents = TestHelpers.loadContents(this, "contents.list");
        // matches a path
        String shouldPass =  "/content/beta/rhel/server/5/5server/x86_64/sap/os/repomd.xml";
        // is not a match
        String shouldFail =  "/fart/face/mcjones";
        // tricky, because it is almost a valid path. All nodes, will have similar children. (the /vt/ is only in /5/, not /6/)
        String shouldFailTricky =  "/content/dist/rhel/server/6/$releasever/$basearch/vt/os";
        try {
            pt.setContentSets(contents);
        } catch (PayloadException ex) {
            fail(ex.toString());
        }
        // for good measure ...
        assertTrue(TestHelpers.cmpStrings(contents, pt.toList()));

        assertTrue(pt.validate(shouldPass));
        assertFalse(pt.validate(shouldFail));

        // FIXME OH NOES... the PathNode relationships are to generous
        //assertFalse(pt.validate(shouldFailTricky));

    }

    @Test
    public void testRootNode() {
        PathTree pt = new PathTree();
        List<String> contents = TestHelpers.loadContents(this, "contents.list");
        try {
            pt.setContentSets(contents);
        } catch (PayloadException ex) {
            fail(ex.toString());
        }

        // generate the root PathNode
        try {
            assertNotNull(pt.getRootPathNode());
        } catch (PayloadException ex) {
            fail(ex.toString());
        }

        // do it again, to make sure it was not a mistake
        try {
            assertNotNull(pt.getRootPathNode());
        } catch (PayloadException ex) {
            fail(ex.toString());
        }
    }

    @Test
    public void testThereAndBackAgainPayload() {
        byte[] bytes;
        PathTree pt0;
        PathTree pt1;
        List<String> contents0;
        List<String> contents1;

        bytes = TestHelpers.loadBytes(this, "test.bin");
        pt0 = new PathTree(bytes);
        contents0 = pt0.toList();
        for (String str : contents0) {
            System.out.println(str);
        }
        
        assertEquals(contents0.size(), 3);
        //printByteArray(bytes);

        try {
            pt1 = new PathTree(contents0);
            assertNotNull(pt1);
            //printByteArray(pt1.getPayload());
            contents1 = pt1.toList();
            assertTrue(TestHelpers.cmpStrings(contents0, contents1));
            for (String str : contents1) {
                System.out.println(str);
            }
        } catch (Throwable ex) {
            fail(ex.toString());
        }

    }

    @Test
    public void testThereAndBackAgainCS() {
        PathTree pt0 = new PathTree();
        PathTree pt1;
        PathTree pt2 = new PathTree();
        PathTree pt3;
        List<String> contents0 = TestHelpers.loadContents(this, "contents.list");
        List<String> contents1;
        List<String> contents2;
        List<String> contents3;
        byte[] bytes;


        try {
            pt0.setContentSets(contents0);
        } catch (PayloadException ex) {
            fail(ex.toString());
        }
        bytes = pt0.getPayload();
        assertNotNull(bytes);
        //printByteArray(bytes);

        pt1 = new PathTree(bytes);
        contents1 = pt1.toList();
        
        // FIXME These next two fail
        assertTrue(TestHelpers.cmpStrings(contents0, contents1));
        assertEquals(contents0.size(), contents1.size());


        // LET'S DO THE TIME WARP AGAIN!!
        try {
            pt2 = new PathTree(contents1);
        } catch (PayloadException ex) {
            fail(ex.toString());
        }
        contents2 = pt2.toList();

        assertTrue(TestHelpers.cmpStrings(contents1, contents2));
        assertEquals(contents1.size(), contents2.size());

        pt3 = new PathTree(pt2.getPayload());
        contents3 = pt3.toList();

        assertTrue(TestHelpers.cmpStrings(contents2, contents3));
        assertEquals(contents2.size(), contents3.size());
    }

    /*
     * FIXME - weird issue, where running setContentSets() twice
     *         causes the nodeBits StringBuffer to not be set correctly ... :-(
    @Test
    public void testSettingContentsTwice() {
        PathNode pn0 = new PathNode();
        PathNode pn1 = new PathNode();
        PathTree pt = new PathTree();
        List<String> contents0 = TestHelpers.loadContents(this, "contents.list");
        List<String> contents1 = TestHelpers.loadContents(this, "contents_small.list");
        try {
            pt.setContentSets(contents0);
            pn0 = pt.getRootPathNode(); // setup the larger PathNode
            System.out.println(contents1);
            pt.setContentSets(contents1);
            pn1 = pt.getRootPathNode(); // setup the small PathNode
        } catch (PayloadException ex) {
            fail(ex.toString());
        }

        assertNotNull(pn0);
    }
    */

}


