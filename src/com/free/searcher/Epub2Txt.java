package com.free.searcher;
import android.util.*;
import java.io.*;
import java.util.zip.*;
import org.apache.http.util.*;

public class Epub2Txt {
	
	public static String epub2txt(File inFile) throws IOException {
		long millis = System.currentTimeMillis();
		Log.d("epub2txt", inFile.getAbsolutePath());
		String wholeFile = readZipEntryContent(inFile.getAbsolutePath());
		int fileLength = wholeFile.length();
		
		wholeFile = Util.removeTags(wholeFile);
		wholeFile = Util.fixCharCode(wholeFile);
		wholeFile = Util.replaceAll(wholeFile, Util.ENTITY_NAME,
									Util.ENTITY_CODE);
		
		Log.d("epub to text", inFile.getAbsolutePath() + " char num: "
			  + fileLength + " used: "
			  + (System.currentTimeMillis() - millis));

		return wholeFile;
	}
	
	public static String readZipEntryContent(String inFile) 
			throws IOException {
		Log.d("epub readZipEntryContent", inFile);
		FileInputStream fis = new FileInputStream(inFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ZipInputStream zis = new ZipInputStream(bis);
		StringBuilder sb = new StringBuilder();
		try {
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				String zeName = ze.getName().toLowerCase();
				//System.out.println("ze, entryName " + ze);
				if (!ze.isDirectory() 
					&& (zeName.endsWith("htm")
					|| zeName.endsWith("html")
					|| zeName.endsWith("xml")
					|| zeName.endsWith("xhtm")
					|| zeName.endsWith("xhtml"))) {
					System.out.println("read entryName " + ze + ", size " + ze.getSize());
					
					int count = 0;
					byte[] buffer = new byte[4096];
//					int read = 0;
//					while ((count = zis.read(buffer, (read += count), buffer.length - read)) != -1) {
//					}
//					System.out.println("count " + count);
//					sb.append(new  String(buffer, "utf-8")).append("\r\n");
					
					ByteArrayBuffer bb = new ByteArrayBuffer(65536);
					while ((count = zis.read(buffer, 0, buffer.length)) != -1) {
						bb.append(buffer, 0, count);
					}
					
					sb.append(new  String(bb.toByteArray(), "utf-8")).append("\r\n");
					System.out.println("entryName " + ze + " read, size: " + sb.length());
				}
			}
		} catch (IOException e) {
			Log.e("readZipEntryContent", e.getMessage(), e);
		} finally {
			zis.close();
			bis.close();
			fis.close();
		}
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		System.out.println(epub2txt(new  File("/storage/emulated/0/Alice in Wonderland.epub")));
	}
}
