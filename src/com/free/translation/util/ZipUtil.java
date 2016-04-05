package com.free.translation.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.http.util.*;


import java.io.*;
import java.net.*;

import java.util.zip.*;
import com.free.translation.*;
import android.util.*;
import com.free.searcher.*;


public class ZipUtil {

	

	
	
	

	
	
	
	
	
	
	/**
	 * 
	 * @param file
	 *            zip file
	 * @return
	 * @throws ZipException
	 * @throws IOException
	 */
	public static List<ZipEntry> loadFiles(File file) throws ZipException,
			IOException {
		return loadFiles(file, "");
	}

//	public static List<ZipEntry> loadAllFilesInFolder(ZipFile file) throws ZipException,
//			IOException {
//		return loadAllFilesInFolder(file, "");
//	}
	
	/**
	 * 
	 * @param zipFile
	 *            zip file
	 * @param folderName
	 *            folder to load
	 * @return list of entry in the folder
	 * @throws ZipException
	 * @throws IOException
	 */
	public static List<ZipEntry> loadFiles(File zipFile, String folderName)
			throws ZipException, IOException {
		Log.d("loadFiles", folderName);
		long start = System.currentTimeMillis();
		FileInputStream fis = new FileInputStream(zipFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ZipInputStream zis = new ZipInputStream(bis);

		ZipEntry zipEntry = null;
		List<ZipEntry> list = new LinkedList<ZipEntry>();
		String folderLowerCase = folderName.toLowerCase();
		int counter = 0;
		while ((zipEntry = zis.getNextEntry()) != null) {
			counter++;
			String zipEntryName = zipEntry.getName();
			Log.d("loadFolder.zipEntry.getName()", zipEntryName);
			if (zipEntryName.toLowerCase().startsWith(folderLowerCase)
					&& !zipEntry.isDirectory()) {
				list.add(zipEntry);
			}
		}
		Log.d("Loaded 1", counter + " files, took: "
				+ (System.currentTimeMillis() - start) + " milliseconds.");
		zis.close();
		bis.close();
		fis.close();
		return list;
	}

	

	/**
	* folderName rong hoac co / o cuoi
	*/
	private static Set<String> filesFoldersInFolder(Set<String> entrySet, String folderName) {
		Log.d("filesFoldersInFolder", folderName);
		String folderLowerCase = folderName.toLowerCase();
		int folderLength = folderName.length();
		Set<String> list = new LinkedHashSet<String>();
		for (String name : entrySet) {
			if (name.toLowerCase().startsWith(folderLowerCase) && name.length() > folderLength) {
				int indexOf = name.indexOf("/", folderLength);
				if ((indexOf < 0 // file
					|| indexOf == name.length() - 1)// for folder of zip
					)
				{
					list.add(name);
					Log.d("list.add(name):", name);
				} else {
					list.add(name.substring(0, indexOf + 1));
				}
			}
		}
		return list;
	}
	
	/**
	 * 
	 * @param file
	 *            zip file
	 * @param folderName
	 *            folder name in zip file
	 * @throws ZipException
	 * @throws IOException
	 */
	public static void saveFolderInZipToDisk(File file, String folderName)
			throws ZipException, IOException {
		Log.d("file1: ", file.toString());
		Log.d("folderName2: ", folderName);
		List<ZipEntry> entries = loadFiles(file, folderName);
		ZipFile zipFile = new ZipFile(file);
		for (ZipEntry entry : entries) {
			String name = entry.getName();
			if (!entry.isDirectory()) {
				new File(name.substring(0, name.lastIndexOf("/"))).mkdirs();
				FileUtil.saveISToFile(zipFile.getInputStream(entry), name);
			}
		}
		zipFile.close();
	}
	
	/**
	 * Compress a String to a zip file that has only one entry like zipName
	 * The name shouldn't have .zip
	 * 
	 * @param content
	 * @param fName
	 * @throws IOException 
	 */
	public static void zipAContentAsFileName(String content, String fName, String charset) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes(charset));
		BufferedInputStream bis = new BufferedInputStream(bais);
		
		File f = new File(fName);
		File parentFile = f.getParentFile();
		if (!parentFile.exists()) {
			parentFile.mkdirs();
		}
		ZipOutputStream zf = new ZipOutputStream(
				new BufferedOutputStream(new FileOutputStream(f + ".zip")));
		
		ZipEntry entry = new ZipEntry(f.getName());
		zf.putNextEntry(entry);
		byte[] barr = new byte[8192];
		int len = 0;
		while ((len = bis.read(barr)) != -1) {
			zf.write(barr, 0, len);
		}
		zf.flush();
		zf.close();
		bis.close();
		bais.close();
	}
	
	/**
	 * Compress a String to a zip file that has only one entry like zipName
	 * The name shouldn't have .zip. Charset UTF-8
	 * 
	 * @param content
	 * @param fName
	 * @throws IOException 
	 */
	public static void zipAContentAsFileName(String content, String fName) throws IOException {
		zipAContentAsFileName(content, fName, "UTF-8");
	}

	/**
	 * 
	 * @param zfile
	 *            zip file
	 * @param entry
	 *            entry to save to disk
	 * @throws ZipException
	 * @throws IOException
	 */
	public static void saveZipEntryToDisk(File zfile, ZipEntry entry)
			throws ZipException, IOException {
		Log.d("file2: ", zfile.toString());
		Log.d("zipEntry2: ", entry.toString());
		ZipFile zipFile = new ZipFile(zfile);
		String name = entry.getName();
		if (!entry.isDirectory()) {
			new File(name.substring(0, name.lastIndexOf("/"))).mkdirs();
			InputStream inputStream = zipFile.getInputStream(entry);
					FileUtil.saveISToFile(inputStream, name);
		}
		zipFile.close();
	}

	public static File readZipEntry(String inFile, String entryName, String outDirFilePath) 
			throws IOException {
		System.out.println(inFile);
		FileInputStream fis = new FileInputStream(inFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ZipInputStream zis = new ZipInputStream(bis);
		File entryFile = null;
		try {
			ZipEntry ze;

			while ((ze = zis.getNextEntry()) != null) {
				String zeName = ze.getName();

				// khi chạy đệ quy thì tạo thêm getFilesDir()
				//Log.d("convertedEntryFile", convertedEntryFile + " exist: " + convertedEntryFile.exists());

				System.out.println("zeName, entryName " + zeName.toString() + ", " + entryName);

				if (!ze.isDirectory()
					&& zeName.equals(entryName)) {
					System.out.println("extracting zeName2 " + zeName + ", " + entryName);
					entryFile = new File(outDirFilePath + "/" + zeName);
					entryFile.getParentFile().mkdirs();

					OutputStream os = new BufferedOutputStream(new FileOutputStream(entryFile));
					try {
						byte[] buffer = new byte[8192];
						int count;
						while ((count = zis.read(buffer)) != -1) {
							os.write(buffer, 0, count);
						}
						System.out.println("entryFile" + entryFile + " written, size: " + entryFile.length());
						return entryFile;
					} finally {
						os.flush();
						os.close();
					}
				}
			}
		}
		catch (IOException e) {
			Log.d("readZipEntry", e.getMessage(), e);
		} finally {
			zis.close();
			bis.close();
			fis.close();
		}
		return null;
	}
	
	public static byte[] readZipEntry(File zfile, ZipEntry entry)
			throws ZipException, IOException {
		Log.d("file3: ", zfile.toString());
		Log.d("zipEntry3: ", entry.toString());
		ZipFile zipFile = new ZipFile(zfile);
		if (entry != null && !entry.isDirectory()) {
			byte[] barr = new byte[(int) entry.getSize()];
			int read = 0;
			int len = 0;
			InputStream is = zipFile.getInputStream(entry);
			int length = barr.length;
			while ((len = is.read(barr, read, length - read)) != -1) {
				read += len;
			}
			zipFile.close();
			return barr;
		} else {
			zipFile.close();
			return new byte[0];
		}
	}
	
	public static String readZipEntryContent(String inFile, String entryName) 
			throws IOException {
		Log.d("inFile", inFile);
//		FileInputStream fis = new FileInputStream(inFile);
//		BufferedInputStream bis = new BufferedInputStream(fis);
//		InputStream zis = new ZipInputStream(bis);
		ZipFile zf = new ZipFile(inFile);
		ZipEntry ze = new  ZipEntry(entryName);
		InputStream zis = new BufferedInputStream(zf.getInputStream(ze));
		try {
//			while ((ze = zis.getNextEntry()) != null) {
			String zeName = ze.getName();
			System.out.println("ze, entryName " + ze + ", " + entryName);
			if (!ze.isDirectory() && zeName.equals(entryName)) {
				System.out.println("read entryName " + entryName + ", " + ze.getSize());
//					long size = ze.getSize();
//					if (size < 0) {
				byte[] buffer = new byte[4096];
				ByteArrayBuffer bb = new ByteArrayBuffer(4096);
				int count = 0;
				while ((count = zis.read(buffer, 0, buffer.length)) != -1) {
					bb.append(buffer, 0, count);
				}
				System.out.println("entryName " + ze + " read, size: " + bb.length());
				return new  String(bb.toByteArray(), "utf-8");
//					} else {
//						byte[] buffer = new byte[(int) size];
//						int count = 0;
//						int read = 0;
//						while ((count = zis.read(buffer, read += count, buffer.length - read)) != -1) {
//						}
//						System.out.println("entryName " + ze + " read, size: " + size);
//						return buffer;
//					}
			}
//			}
		}
		catch (IOException e) {
			Log.d("readZipEntryContent", e.getMessage(), e);
		} finally {
			zis.close();
//			bis.close();
//			fis.close();
		}
		return "";
	}
	
	public static String readZipEntryContent(String inFile, String prefix, String suffix) 
			throws IOException {
		Log.d("inFile", inFile);
		FileInputStream fis = new FileInputStream(inFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ZipInputStream zis = new ZipInputStream(bis);
		ZipEntry ze;
//		ZipFile zf = new ZipFile(inFile);
//		ZipEntry ze = new  ZipEntry(prefix);
//		InputStream zis = new BufferedInputStream(zf.getInputStream(ze));
		try {
			ByteArrayBuffer bb = new ByteArrayBuffer(4096);
			while ((ze = zis.getNextEntry()) != null) {
				String zeName = ze.getName();
				System.out.println("ze, entryName " + ze + ", " + prefix);
				if (!ze.isDirectory() && zeName.startsWith(prefix) 
					&& zeName.endsWith(suffix)) {
					System.out.println("read entryName " + prefix + ", " + ze.getSize());
//					long size = ze.getSize();
//					if (size < 0) {
					byte[] buffer = new byte[4096];
					bb.append(("\n" + zeName + "\n").getBytes("utf-8"), 0, ("\n" + zeName + "\n").length());
					int count = 0;
					while ((count = zis.read(buffer, 0, buffer.length)) != -1) {
						bb.append(buffer, 0, count);
					}
					System.out.println("entryName " + ze + " read, size: " + bb.length());

//					} else {
//						byte[] buffer = new byte[(int) size];
//						int count = 0;
//						int read = 0;
//						while ((count = zis.read(buffer, read += count, buffer.length - read)) != -1) {
//						}
//						System.out.println("entryName " + ze + " read, size: " + size);
//						return buffer;
//					}
				}
			}
			return new String(bb.toByteArray(), "utf-8");
		}
		catch (IOException e) {
			Log.d("readZipEntryContent", e.getMessage(), e);
		} finally {
			zis.close();
			bis.close();
			fis.close();
		}
		return "";
	}
	
	/**
	 * Nén file hay thư mục vào file zipFileName
	 * 
	 * @param file
	 * @throws IOException
	 */
//	public static void compressAFileOrFolderRecursiveToZip(String file) throws IOException {
//		compressAFileOrFolderRecursiveToZip(new File(file));
//	}
	
	/**
	 * Nén file hay thư mục vào file zipFileName
	 * 
	 * @param file
	 * @throws IOException
	 */
//	public static void compressAFileOrFolderRecursiveToZip(File file) throws IOException {
//		compressAFileOrFolderRecursiveToZip(file, null);
//	}
	
	/**
	 * Nén file hay thư mục vào file zipFileName
	 * 
	 * @param file
	 * @param zipFileName
	 * @throws IOException
	 */
//	public static void compressAFileOrFolderRecursiveToZip(
//			String file, String zipFileName) throws IOException {
//		compressAFileOrFolderRecursiveToZip(new File(file), zipFileName);
//	}
	
	
	/**
	 * Nén file hay thư mục vào file zipFileName
	 * 
	 * @param file
	 * @param zipFileName
	 * @throws IOException
	 */
//	public static void compressAFileOrFolderRecursiveToZip(File file, String zipFileName) 
//			throws IOException {
//		String folderName;
//		List<File> filesNeedCompress;
//		Log.d(file.toString(), "" + file.exists());
//		if (file.isDirectory()) {
//			filesNeedCompress = FileUtil.getFiles(file, null);
//			folderName = file.getAbsolutePath();
//		} else if (file.isFile()) {
//			filesNeedCompress = new LinkedList<File>();
//			filesNeedCompress.add(file);
//			folderName = file.getParent();
//		} else {
//			return;
//		}
//		Log.d("list", Util.listToString(filesNeedCompress, true, "\r\n"));
//		
//		if (zipFileName == null || zipFileName.trim().length() == 0) {
//			zipFileName = file.getAbsolutePath() + ".zip";
//		}
//		
//		OutputStream fos = new FileOutputStream(zipFileName);
//		BufferedOutputStream bos = new BufferedOutputStream(fos);
//		ZipOutputStream zos = new ZipOutputStream(bos);
//		
//		for (File file2 : filesNeedCompress) {
//			ZipEntry zipEntry = new ZipEntry(
//					file2.getAbsolutePath().substring(folderName.length() + 1)); // "/"
//			zipEntry.setTime(file2.lastModified());
//			zos.putNextEntry(zipEntry);
//			
//			FileInputStream fis = new FileInputStream(file2);
//			byte[] bArr = new byte[32768];
//			int byteRead = 0;
//			while ((byteRead = fis.read(bArr)) > 0) {
//				zos.write(bArr, 0, byteRead);
//			}
//			zos.closeEntry();
//			fis.close();
//		}
//		zos.flush();
//		zos.close();
//		bos.flush();
//		bos.close();
//		fos.flush();
//		fos.close();
//	}
	
	
	/**
	 * Nén file vào file +.zip
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static void compressAFileToZip(File file) throws IOException {
		compressAFileToZip(file, file.getAbsolutePath() + ".zip");
	}
	
	/**
	 * Nén file vào file +.zip
	 * 
	 * @param file
	 * @param zipFileName
	 * @throws IOException
	 */
	public static void compressAFileToZip(File file, String zipFileName) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		ZipOutputStream zf = new ZipOutputStream(
				new BufferedOutputStream(new FileOutputStream(zipFileName)));
		
		ZipEntry entry = new ZipEntry(file.getName());
		zf.putNextEntry(entry);
		byte[] barr = new byte[65536];
		int len = 0;
		while ((len = bis.read(barr)) != -1) {
			zf.write(barr, 0, len);
		}
		zf.flush();
		zf.close();
		bis.close();
	}
	
	/**
	 * Giải nén zipFileName vào folder của zip
	 * 
	 * @param zipFileName
	 * @throws IOException
	 */
	public static void extractZipToFolder(String zipFileName) throws IOException {
		extractZipToFolder(zipFileName, null);
	}
	
	/**
	 * Giải nén zipFileName vào parentFolder, tự tạo thư mục nếu parentFolder chưa tồn tại
	 * 
	 * @param zipFileName
	 * @param parentFolder
	 * @throws IOException
	 */
	public static void extractZipToFolder(String zipFileName, String parentFolder)
			throws IOException {
		File f = new File(zipFileName);
		if (parentFolder == null || parentFolder.trim().length() == 0) {
			parentFolder = f.getParent();
		}
		ZipFile zf = new ZipFile(f);

		File parentFile = new File(parentFolder);
		if (!parentFile.exists()) {
			parentFile.mkdirs();
		}

		Enumeration<? extends ZipEntry> entries = zf.entries();
		byte[] barr = new byte[65536];
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (!entry.isDirectory()) {
				InputStream is = zf.getInputStream(entry);

				String entryName = entry.getName();
				String fileName = parentFolder + "/" + entryName;
				new File(fileName).getParentFile().mkdirs();
				OutputStream os = new FileOutputStream(fileName);
				Log.d("extracting", fileName);

				int len = 0;
				while ((len = is.read(barr)) != -1) {
					os.write(barr, 0, len);
				}
				is.close();
				os.flush();
				os.close();
			}
		}
		zf.close();
	}
	
	public static File extractFirstFileInAZip(String zipFileName) throws IOException {
		File f = new File(zipFileName);
		String parent = f.getParent();
		parent = (parent == null) ? f.getAbsolutePath() : parent;
		ZipFile zf = new ZipFile(f);
		Enumeration<? extends ZipEntry> entries = zf.entries();
		byte[] barr = new byte[65536];
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (!entry.isDirectory()) {
				String entryName = entry.getName();
				InputStream is = zf.getInputStream(entry);
				int len = 0;
				String fileName = parent + "/" + entryName.substring(entryName.lastIndexOf("/") + 1);
				Log.d("extracting first", fileName);
				OutputStream os = new FileOutputStream(fileName);
				while ((len = is.read(barr)) != -1) {
					os.write(barr, 0, len);
				}
				is.close();
				zf.close();
				os.flush();
				os.close();
				return new File(fileName);
			}
		}
		zf.close();
		return null;
	}
	
//	public static void main(String[] args) throws IOException, ArchiveException {
//		// List<Entry<ZipEntry, byte[]>> loadZip = ZipUtil.loadZip((
//		// new File(
//		// "xmlte.zip")));
////		ZipUtil.saveFolderInZipToDisk(new File("xmlte.zip"), "xmlte");
////		compressAFileToZip(new File(
////				"D:/Temp/En-Vi Breakthrough in Samatha Meditation and Vipassana Meditation.html"));
////		extractFirstFileInAZip(
////				"D:/Temp/En-Vi Breakthrough in Samatha Meditation and Vipassana Meditation_files.zip");
////		extractZipToFolder("D:/Temp/En-Vi Breakthrough in Samatha Meditation and Vipassana Meditation_files.zip");
////		compressAFileOrFolderRecursiveToZip(
////				"D:/Temp/En-Vi Breakthrough in Samatha Meditation and Vipassana Meditation_files");
////		compressAFileOrFolderRecursiveToZip(
////				"D:/Temp/En-Vi Breakthrough in Samatha Meditation and Vipassana Meditation.html");
////		compressedFile2UrlStr("/storage/emulated/0/rar/lzma920.tar.bz2", "", true, "\n");
////		compressedFile2UrlStr("/storage/emulated/0/rar/httpcomponents-client-4.3.6-src.tar.gz", "", true, "\n");
//		compressedFile2UrlStr("/storage/emulated/0/rar/httpcomponents-client-4.3.6-src.tar.gz", "httpcomponents-client-4.3.6/", true, "\n");
//		
//		Archive ar = new Archive("/storage/emulated/0/rar/httpcomponents-client-4.3.6-src.tar.gz");
//		ar.getFile("httpcomponents-client-4.3.6/README.txt", "/storage/emulated/0/rar");
//		ar = new Archive("/storage/emulated/0/rar/stardict-cmupd-2.4.2.tar.bz2");
//		ar.getFile("stardict-cmupd-2.4.2/cmupd.ifo", "/storage/emulated/0/rar");
//		
//	}
}
