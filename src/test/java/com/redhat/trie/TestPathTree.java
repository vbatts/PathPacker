package com.redhat.trie;

import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        assertNotNull(pt);
    }

    private InputStream resStream(String filename) {
        return getClass().getClassLoader().getResourceAsStream(filename);
    }

    private List<String> loadContents(String filename) throws IOException {
        String content;
        List<String> contentList = new ArrayList<String>();
        InputStream in = resStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        try {
            while ((content = br.readLine()) != null) {
                contentList.add(content);
            }
        } catch (IOException ex) {
            throw ex;
        }
        return contentList;
    }
}


