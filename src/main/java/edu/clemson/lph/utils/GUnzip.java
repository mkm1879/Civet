package edu.clemson.lph.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GUnzip {

	public static void decompressGzip(File source, File target) throws IOException {

        try (GZIPInputStream gis = new GZIPInputStream(
                                      new FileInputStream(source) );
             FileOutputStream fos = new FileOutputStream(target) ) {

            // copy GZIPInputStream to FileOutputStream
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = gis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
    }

	public static byte[] compressGzip(String sSource) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ByteArrayInputStream is = new ByteArrayInputStream( sSource.getBytes("UTF-8") );
       		 GZIPOutputStream gos = new GZIPOutputStream(bos) ) {

            // copy GZIPInputStream to FileOutputStream
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) > 0) {
                gos.write(buffer, 0, len);
            }
        }
        return bos.toByteArray();
    }
}
