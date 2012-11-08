package com.redhat.trie;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.HashSet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
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
        List<String> contents = loadContents("contents.list");
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
        List<String> contents = loadContents("contents.list");
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
        List<String> contents = loadContents("contents.list");
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
        assertTrue(cmpStrings(contents, pt.toList()));

        assertTrue(pt.validate(shouldPass));
        assertFalse(pt.validate(shouldFail));

        // FIXME OH NOES... the PathNode relationships are to generous
        //assertFalse(pt.validate(shouldFailTricky));

    }

    @Test
    public void testRootNode() {
        PathTree pt = new PathTree();
        List<String> contents = loadContents("contents.list");
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

        bytes = loadBytes("test.bin");
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
            assertTrue(cmpStrings(contents0, contents1));
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
        List<String> contents0 = loadContents("contents.list");
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
        assertTrue(cmpStrings(contents0, contents1));
        assertEquals(contents0.size(), contents1.size());


        // LET'S DO THE TIME WARP AGAIN!!
        try {
            pt2 = new PathTree(contents1);
        } catch (PayloadException ex) {
            fail(ex.toString());
        }
        contents2 = pt2.toList();

        assertTrue(cmpStrings(contents1, contents2));
        assertEquals(contents1.size(), contents2.size());

        pt3 = new PathTree(pt2.getPayload());
        contents3 = pt3.toList();

        assertTrue(cmpStrings(contents2, contents3));
        assertEquals(contents2.size(), contents3.size());
    }


    // Helpers
    // 
    private boolean cmpStrings(List<String> thisList, List<String> thatList) {
        Collection<String> thisColl = new ArrayList(thisList);
        Collection<String> thatColl = new ArrayList(thatList);

        Collection<String> similar = new HashSet<String>( thisColl );
        Collection<String> different = new HashSet<String>();
        different.addAll( thisColl );
        different.addAll( thatColl );

        similar.retainAll( thatColl );
        different.removeAll( similar );
        
        if (different.size() > 0) {
            System.out.printf("Different:%s%n", different);
        }
        return (different.size() == 0);
    }

    private void printByteArray(byte[] bytes) {
        int width = 30;
        int counter = 0;

        for (byte b : bytes) {
            System.out.format("%02X ", b);
            counter++;
            if (counter > width) {
                counter = 0;
                System.out.println();
            }
        }
        System.out.println();
    }

    private InputStream resStream(String filename) {
        return getClass().getClassLoader().getResourceAsStream(filename);
    }

    private byte[] loadBytes(String filename) {
        InputStream in = resStream(filename);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16834];

        try {
            while ((nRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException ex) {
            fail(ex.toString());
        }

        return buffer.toByteArray();
    }

    private List<String> loadContents(String filename) {
        String content;
        List<String> contentList = new ArrayList<String>();
        InputStream in = resStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        try {
            while ((content = br.readLine()) != null) {
                contentList.add(content);
            }
        } catch (IOException ex) {
            fail();
        }
        return contentList;
    }
}


