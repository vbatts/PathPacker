package com.hashbangbash.trie;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;

public class App {
    public static void main(String[] args) {
        FileInputStream fis;
        DataInputStream in;

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
        }
    }
}

