package com.hashbangbash.trie;

import java.util.List;
import java.util.ArrayList;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.redhat.trie.HuffNode;
import com.redhat.trie.NodePair;
import com.redhat.trie.PathNode;
import com.redhat.trie.PathTree;
import com.redhat.trie.PayloadException;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;

import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.x509.extension.X509ExtensionUtil;


/*
 * App
 *
 * This is just a simple class to handle command line interactions
 *
 */
public class App {

    /* 
     * PrettyPrint a PathNode tree
     */
    public static void printTree(PathNode pn, int tab) {
        StringBuffer nodeRep = new StringBuffer();
        for (int i = 0; i <= tab; i++) {
            nodeRep.append("  ");
        }
        nodeRep.append("Node [");
        nodeRep.append(pn.getId());
        nodeRep.append("]");

        for (PathNode parent : pn.getParents()) {
            nodeRep.append(" ^ [");
            nodeRep.append(parent.getId());
            nodeRep.append("]");
        }
        for (NodePair cp : pn.getChildren()) {
            nodeRep.append(" v [");
            nodeRep.append(cp.getName());
            nodeRep.append(" {");
            nodeRep.append(cp.getConnection().getId());
            nodeRep.append("} ]");
        }
        System.out.println(nodeRep);
        for (NodePair cp : pn.getChildren()) {
            printTree(cp.getConnection(), tab + 1);
        }
    }

    /* 
     * PrettyPrint a HuffNode tree
     */
    public static void printTrie(HuffNode hn, int tab) {
        StringBuffer nodeRep = new StringBuffer();
        for (int i = 0; i <= tab; i++) {
            nodeRep.append("  ");
        }
        nodeRep.append("Node [");
        nodeRep.append(hn.getId());
        nodeRep.append("]");

        nodeRep.append(", Weight [");
        nodeRep.append(hn.getWeight());
        nodeRep.append("]");

        nodeRep.append(", Value = [");
        nodeRep.append(hn.getValue());
        nodeRep.append("]");

        System.out.println(nodeRep);
        if (hn.getLeft() != null) {
            printTrie(hn.getLeft(), tab + 1);
        }
        if (hn.getRight() != null) {
            printTrie(hn.getRight(), tab + 1);
        }
    }

    /*
     * From the deflated payload, produce the content set lists
     *
     *
     * FIXME - break this apart, so that the hydrated payload
     *         can be structure to more quickly search, and use less memory
     *
     *      Rename it for tracking, and to be clear about what is happening
     */
    public static List<String> hydrateContentPackage(byte[] compressedBlob) {
        PathTree pt = new PathTree(compressedBlob);
        return pt.toList();
    }


    public static ASN1Encodable objectFromOid(X509Certificate cert, String oid) {
        if (cert == null) { return null; }

        try {
            for (String thisOid : cert.getNonCriticalExtensionOIDs()) {
                if (thisOid.equals(oid)) {
                    return X509ExtensionUtil.fromExtensionValue(cert.getExtensionValue(oid));
                }
            }
        } catch (IOException ex) { }
        return null;
    }
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
            && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }


    public static List<String> hydrateFromBytes(byte[] compressedBlob) {
        return hydrateContentPackage(compressedBlob);
    }

    public static List<String> hydrateFromFile(String filename) {
        try {
            return hydrateFromBytes(getBytesFromFile(new File(filename)));
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return null;
    }

    public static void byteArrayToFile(byte[] output, String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
         
            /*
             * To write byte array to a file, use
             * void write(byte[] bArray) method of Java FileOutputStream class.
             *
             * This method writes given byte array to a file.
             */
            fos.write(output);
     
            /*
            * Close FileOutputStream using,
            * void close() method of Java FileOutputStream class.
            *
            */
            fos.flush();
            fos.close();
     
        } catch(FileNotFoundException ex) {
            System.out.println("FileNotFoundException : " + ex);
        } catch(IOException ioe) {
            System.out.println("IOException : " + ioe);
        }
    }

    public static List<String> listFromFile(String filename) throws IOException, FileNotFoundException {
        FileInputStream fis;
        DataInputStream in;
        BufferedReader br;

        String content;
        List<String> contentList;

        try {
            fis = new FileInputStream(filename);
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (Throwable t) {
            System.out.printf("ERROR: [%s] %s\n", filename, t);
            return new ArrayList<String>();
        }

        in = new DataInputStream(fis);
        br = new BufferedReader(new InputStreamReader(in));
        contentList = new ArrayList<String>();

        try {
        while ((content = br.readLine()) != null) {
            contentList.add(content);
        }
        } catch (IOException ex) {
            throw ex;
        }
        return contentList;
    }

    public static void showTree(String filename) {
        List<String> contentList;
        try {
            contentList = listFromFile(filename);
        } catch (IOException ex) {
            System.out.printf("ERROR: [%s] - %s\n", filename, ex);
            return;
        }
        showTree(contentList);
    }

    public static void showTree(List<String> contentList) {
        PathTree pt;
        try {
            pt = new PathTree(contentList);
            printTree(pt.getRootPathNode(), 0);
        } catch (PayloadException ex) {
            System.out.println(ex);
        }
    }

    public static ASN1Encodable objectFromCertOid(String certFilename, String oid) {
        X509Certificate cert;
        cert = certFromFile(certFilename);
        return objectFromOid(cert,oid);
    }

    public static X509Certificate certFromFile(String certFilename) {
        FileInputStream fis;
        BufferedInputStream bis;
        CertificateFactory cf;
        X509Certificate cert;

        try {
            fis = new FileInputStream(certFilename);
        } catch (FileNotFoundException ex) {
            return null;
        }

        bis = new BufferedInputStream(fis);

        
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException ex) {
            return null;
        }

        try {
            while (bis.available() > 0) {
                cert = (X509Certificate) cf.generateCertificate(bis);
                return cert;
            }
        } catch (IOException ex) {
        } catch (CertificateException cex) {
        }
        return null;
    }

    public static void main(String[] args) {
        for (String arg : args) {
            //showTreeFromCSFIle(arg);
            //showTreeFromCSFIle(arg);

            DEROctetString dos;
            List<String> contents;

            dos = (DEROctetString)objectFromCertOid(arg, "1.3.6.1.4.1.2312.9.7");
            //byteArrayToFile(dos.getOctets(), "herp.bin");
            if ((contents = hydrateFromBytes(dos.getOctets())) == null) {
                System.out.println("FAIL");
                return;
            }

            for (String content : contents) {
                System.out.println(content);
            }
        }
    }
}

