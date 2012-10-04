package com.hashbangbash.trie;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.redhat.trie.PathNode;
import com.redhat.trie.Util;

public class App {
    public static void main(String[] args) {
        FileInputStream fis;
        DataInputStream in;
        BufferedReader br;

        String content;
        List<String> contentList;

        for (String arg : args) {
            try {
                fis = new FileInputStream(arg);
            } catch (FileNotFoundException ex) {
                System.out.printf("ERROR: failed to find file %s\n", arg);
                continue;
            } catch (Throwable t) {
                System.out.printf("ERROR: [%s] %s\n", arg, t);
                continue;
            }

            in = new DataInputStream(fis);
            br = new BufferedReader(new InputStreamReader(in));
            contentList = new ArrayList<String>();

            try {
            while ((content = br.readLine()) != null) {
                contentList.add(content);
            }
            } catch (IOException ex) {
                System.out.printf("ERROR: [%s] - %s\n", arg, ex);
                continue;
            }

            //System.out.println(contentList.toString());

            PathNode root = new PathNode();
            Util.makePathTree(contentList, root);

            Util.printTree(root, 0);
        }
    }
}

