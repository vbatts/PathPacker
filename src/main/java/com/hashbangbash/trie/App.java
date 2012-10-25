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

import java.io.FileNotFoundException;
import java.io.IOException;

import com.redhat.trie.PathNode;
import com.redhat.trie.Util;

import java.util.zip.Inflater;
import java.util.zip.DataFormatException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.bouncycastle.asn1.*;
import org.bouncycastle.x509.extension.X509ExtensionUtil;


public class App {

    public static ASN1InputStream toASN1Stream(byte[] b) {
        return new ASN1InputStream(new ByteArrayInputStream(b));
    }

    public final static byte[] decompress(byte[] input) {
        Inflater inflator = new Inflater();
        inflator.setInput(input);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
        byte[] buf = new byte[1024];
        try {
            while (true) {
                int count = inflator.inflate(buf);
                if (count > 0) {
                    bos.write(buf, 0, count);
                } else if (count == 0 && inflator.finished()) {
                    break;
                } else {
                    throw new RuntimeException("bad zip data, size:"
                            + input.length);
                }
            }
        } catch (DataFormatException t) {
            throw new RuntimeException(t);
        } finally {
            inflator.end();
        }
        return bos.toByteArray();
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


    public static List<String> hydrateFromBytes(byte[] derblob) {
        Util util = new Util();

        try {
            return util.hydrateContentPackage(derblob);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return null;
    }

    public static List<String> hydrateFromFile(String filename) {
        try {
            return hydrateFromBytes(getBytesFromFile(new File(filename)));
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return null;
    }

    public static void showTreeFromCSFIle(String filename) {
        FileInputStream fis;
        DataInputStream in;
        BufferedReader br;

        String content;
        List<String> contentList;
        Util util = new Util();

        try {
            fis = new FileInputStream(filename);
        } catch (FileNotFoundException ex) {
            System.out.printf("ERROR: failed to find file %s\n", filename);
            return;
        } catch (Throwable t) {
            System.out.printf("ERROR: [%s] %s\n", filename, t);
            return;
        }

        in = new DataInputStream(fis);
        br = new BufferedReader(new InputStreamReader(in));
        contentList = new ArrayList<String>();

        try {
        while ((content = br.readLine()) != null) {
            contentList.add(content);
        }
        } catch (IOException ex) {
            System.out.printf("ERROR: [%s] - %s\n", filename, ex);
            return;
        }

        //System.out.println(contentList.toString());
        PathNode root = new PathNode();
        util.makePathTree(contentList, root);
        Util.printTree(root, 0);
    }

    public static ASN1Encodable objectFromCertOid(String certFilename, String oid) {
        X509Certificate cert;
        cert = certFromFile(certFilename);
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

