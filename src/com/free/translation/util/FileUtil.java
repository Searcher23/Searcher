//package com.free.translation.util;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.io.PrintWriter;
//import java.io.RandomAccessFile;
//import java.io.UnsupportedEncodingException;
//import java.nio.ByteBuffer;
//import java.nio.channels.FileChannel;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.Comparator;
//import java.util.Date;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.logging.Logger;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;
//
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.hwpf.converter.WordToTextConverter;
//import org.apache.poi.hwpf.extractor.Word6Extractor;
//import org.apache.poi.hwpf.extractor.WordExtractor;
//import org.apache.poi.poifs.filesystem.POIFSFileSystem;
//import org.apache.poi.ss.usermodel.Workbook;
//import android.util.*;
//import com.free.translation.*;
//import java.io.*;
//
//
//public class FileUtil {
//
//	private static final String ISO_8859_1 = "ISO-8859-1";
//	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
//	private static final Logger logger = Logger.getLogger(FileUtil.class.getName());
//
//	public static byte[] readFileToMemory(String file)
//			throws FileNotFoundException, IOException {
//		// FileInputStream fis = new FileInputStream(file);
//		// FileChannel fileChannel = fis.getChannel();
//		// long size = fileChannel.size();
//		// MappedByteBuffer mappedByteBuffer =
//		// fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
//		// byte[] data = new byte[(int) size];
//		// mappedByteBuffer.get(data, 0, (int) size);
//		// fileChannel.close();
//		// fis.close();
//		// return data;
//		return readFileToMemory(new File(file));
//	}
//
//	public static byte[] readFileToMemory(File file)
//			throws FileNotFoundException, IOException {
//		FileInputStream fis = new FileInputStream(file);
//		BufferedInputStream bis = new BufferedInputStream(fis);
//		byte[] data = new byte[(int) file.length()];
//		int start = 0;
//		int read = 0;
//		while ((read = bis.read(data, start, data.length - start)) > 0) {
//			start += read;
//		}
//		bis.close();
//		fis.close();
//		return data;
//	}
//
//	public static String readFileAsCharset(String fileName, String charsetName)
//			throws IOException {
//		byte[] arr = readFileToMemory(fileName);
//		String st = "";
//		try {
//			st = new String(arr, charsetName);
//		} catch (UnsupportedEncodingException e) {
//			st = readFileWithCheckEncode(fileName);
//		}
//		return st;
//	}
//
//	public static String readFileAsCharset(File file, String charsetName)
//			throws IOException {
//		return readFileAsCharset(file.getAbsolutePath(), charsetName);
//	}
//
//	public static String convertFileContentToUTF8(File file, String oriCharset)
//			throws IOException {
//		if (file != null) {
//			String encode = readFileWithCheckEncode(file.getAbsolutePath());
//			return new String(encode.getBytes(oriCharset), "UTF-8");
//		} else {
//			return null;
//		}
//	}
//
//	public static String readFileWithCheckEncode(String filePath)
//			throws FileNotFoundException, IOException,
//			UnsupportedEncodingException {
//		byte[] byteArr = FileUtil.readFileToMemory(filePath);
//		Log.d("readFileWithCheckEncode", filePath);
//		
//		if (byteArr.length > 3 && (byteArr[0] == -17 && byteArr[1] == -69 && byteArr[2] == -65
//			|| byteArr[0] == -61 && byteArr[1] == -96 && byteArr[2] == 13)) {
//			Log.d("char", (char) byteArr[0] + ": " + (int) byteArr[0]);
//			Log.d("char", (char) byteArr[1] + ": " + (int) byteArr[1]);
//			Log.d("char", (char) byteArr[2] + ": " + (int) byteArr[2]);
//
//			String utf8 = new String(byteArr, Util.UTF8);
//			Log.d("is UTF8", filePath + " 1");
//			return utf8;
//		} else {
//			String utf8 = new String(byteArr, Util.UTF8);
////			System.err.println("utf8: " + utf8);
//			String fontName = Util.guessFontName(utf8);
//			Log.d("fontName", "is " + fontName);
//			if (Util.VU_TIMES.equals(fontName)
//					|| Util.TIMES_CSX.equals(fontName)
//					|| Util.TIMES_CSX_1.equals(fontName)) {
//				Log.d("is UTF8", filePath + " 2");
//				return utf8;
//			} else if (filePath.toLowerCase().contains(".pdf.")) {
//				Log.d("is not UTF8", filePath + " pdf String(byteArr)");
//				return new String(byteArr);
//			} else {
//				String notUTF8 = new String(byteArr, ISO_8859_1);
//				Log.d("is not UTF8", filePath + " txt String(byteArr, ISO_8859_1)");
//				return notUTF8;
//			}
//		}
//	}
//
//	public static String readFileByMetaTag(File file)
//			throws FileNotFoundException, IOException {
//		// System.gc();
//		String content = FileUtil.readFileWithCheckEncode(file.getAbsolutePath());
//		String charsetName = "";
//		if ((charsetName = Util.readValue(content, "charset")).length() > 0) {
//			Log.d("file: " + file.getAbsolutePath() + " has charset: "
//					, charsetName);
//		}
//		try {
//			if (charsetName.length() > 0) {
//				content = new String(FileUtil.readFileToMemory(file.getAbsolutePath()), charsetName);
////				Log.d(content);
//			}
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
//		return content;
//	}
//
//	public static String[] readWordFileToParagraphs(String wordFileName)
//			throws IOException {
//		POIFSFileSystem fs = createPOIFSFileSystem(wordFileName);
//		WordExtractor we = new WordExtractor(fs);
//		String[] paragraphs = we.getParagraphText();
//		we.close();
//		return paragraphs;
//	}
//
//	public static String readWordFileToText(File wordFileName) throws FileNotFoundException, IOException {
//		try {
//			return WordToTextConverter.getText(wordFileName);
//		} catch (Exception e) {
//			e.printStackTrace();
//			FileInputStream fis = new FileInputStream(wordFileName);
//			BufferedInputStream bis = new BufferedInputStream(fis);
//			Word6Extractor extractor = new Word6Extractor(bis);
//			String text = extractor.getText();
//			bis.close();
//			fis.close();
//			extractor.close();
//			return text;
//		}
//	}
//
//	public static Workbook readWorkBook(String inputFile)
//			throws FileNotFoundException, IOException {
//		System.out.println(inputFile);
//		FileInputStream fis = new FileInputStream(inputFile);
//		//POIFSFileSystem fileSystem = new POIFSFileSystem(fis);
//		Workbook wb = new HSSFWorkbook(new BufferedInputStream(fis));
//		return wb;
//	}
//
//	public static void writeWorkBook(Workbook wb, String outputFile)
//			throws FileNotFoundException, IOException {
//		FileOutputStream fos = new FileOutputStream(outputFile);
//		wb.write(fos);
//		fos.flush();
//		fos.close();
//	}
//
//	private static final String DOCTYPE_PATTERN = "<!DOCTYPE .+?>";
//	private static final String DOCTYPE_WITH_DTD = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"data/xhtml1-transitional.dtd\">\r\n";
//	public static String initStrictHtml(File oldFile, File newFile)
//			throws FileNotFoundException, IOException {
//		String wholeFile = FileUtil.readFileByMetaTag(oldFile);
//		// String wholeFile =
//		// FileUtil.readFileAsCharset(oldFile.getAbsolutePath(),
//		// currentCharset);
//		wholeFile = Util.fixAttrValueApos(wholeFile);
//		wholeFile = Util.fixMetaTagsUTF8(wholeFile);
//		wholeFile = Util.fixEndTags(wholeFile);
//		wholeFile = Util.fixCharCode(wholeFile);
//		wholeFile = Util.replaceAll(wholeFile, Util.ENTITY_NAME,
//				Util.ENTITY_CODE);
//		wholeFile = wholeFile.replaceAll("[\\r\\n\\f]+", " ");
//		// wholeFile = wholeFile.replaceAll("[\\-]+", "-");
//		// wholeFile = wholeFile.replaceAll("<!-", "<!--");
//		// wholeFile = wholeFile.replaceAll("->", "-->");
//		// wholeFile = wholeFile.replaceAll("&ntilde;", "ñ");
//		// wholeFile = wholeFile.replaceAll("&Ntilde;", "Ñ");
//		// wholeFile = wholeFile.replaceAll("&rsquo;", "’");
//		// wholeFile = wholeFile.replaceAll("&lsquo;", "‘");
//		// wholeFile = wholeFile.replaceAll("&ndash;", "–");
//		if (wholeFile.indexOf("<!DOCTYPE") < 0) {
//			wholeFile = new StringBuilder(DOCTYPE_WITH_DTD).append(wholeFile).toString();
//		} else {
//			wholeFile = wholeFile.replaceFirst(DOCTYPE_PATTERN, DOCTYPE_WITH_DTD);
//		}
//		FileUtil.writeFileAsCharset(newFile, wholeFile, "UTF-8");
//
//		return wholeFile;
//	}
//
//	public static void printInputStream(InputStream is, OutputStream os,
//			String inCharset, String outCharset) throws IOException {
//		if (Util.isEmpty(inCharset)) {
//			inCharset = "UTF-8";
//		}
//		if (Util.isEmpty(outCharset)) {
//			outCharset = "UTF-8";
//		}
//		try {
//			BufferedReader from = new BufferedReader(
//			// new InputStreamReader(new FileInputStream("kanji.txt"),
//					new InputStreamReader(is, inCharset));
//			PrintWriter to = new PrintWriter(new OutputStreamWriter(
//			// check
//			// enco
//					os, outCharset));
//			// new FileOutputStream("sverige.txt"), "ISO8859_3"));
//
//			// reading and writing here...
//			String line = from.readLine();
//			System.out.println("-->" + line + "<--");
//			to.println(line);
//			from.close();
//			to.flush();
//			to.close();
//		} catch (UnsupportedEncodingException exc) {
//			System.err.println("Bad encoding" + exc);
//			return;
//		} catch (IOException err) {
//			System.err.println("I/O Error: " + err);
//			return;
//		}
//	}
//
//	private static final byte[] UTF8_ESCAPE = new byte[] { -17, -69, -65 };
//
//	public static void writeFileAsCharset(File file, String contents,
//			String newCharset) throws IOException {
//		Log.d("Writing file", file.getAbsolutePath());
//		if (file.getParentFile() != null && !file.getParentFile().exists()) {
//			file.getParentFile().mkdirs();
//		}
//		file.delete();
//		FileOutputStream fos = new FileOutputStream(file);
//		FileChannel fileChannel = fos.getChannel();
//		if (Util.UTF8.equalsIgnoreCase(newCharset)) {
//			fileChannel.write(ByteBuffer.wrap(UTF8_ESCAPE));
//		}
//		byte[] oldCharArr = contents.getBytes(newCharset);
//		fileChannel.write(ByteBuffer.wrap(oldCharArr));
//		fileChannel.close();
//		fos.flush();
//		fos.close();
//	}
//	
//	public static void writeAppendFileAsCharset(File file, String contents,
//												String newCharset) throws IOException {
//		if (file.getParentFile() != null && !file.getParentFile().exists()) {
//			file.getParentFile().mkdirs();
//		}
//		//true = append file
//		FileWriter fileWriter = new FileWriter(file, true);
//		BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
//		try {
//			bufferWriter.write(contents);
//		} finally {
//			bufferWriter.flush();
//			bufferWriter.close();
//			fileWriter.close();
//		}
//	}
//
//	public static void changeUtf8NotBOM2Utf8(String src, String dest)
//			throws UnsupportedEncodingException, FileNotFoundException,
//			IOException {
//		writeFileAsCharset(new File(dest), 
//				new String(readFileToMemory(new File(src)), Util.UTF8), 
//				Util.UTF8);
//	}
//
//	public static void saveBArrToFile(String fileName, byte[] barr)
//			throws IOException {
//		File file = new File(fileName);
//		FileOutputStream fos = new FileOutputStream(file);
//		BufferedOutputStream bos = new BufferedOutputStream(fos);
//		bos.write(barr, 0, barr.length);
//		bos.flush();
//		fos.flush();
//		bos.close();
//		fos.close();
//	}
//
//	public static void saveISToFile(String fileName, InputStream is)
//			throws IOException {
//		File file = new File(fileName);
//		file.getParentFile().mkdirs();
//		FileOutputStream fos = new FileOutputStream(file);
//		BufferedOutputStream bos = new BufferedOutputStream(fos);
//		BufferedInputStream bis = new BufferedInputStream(is);
//		byte[] barr = new byte[8196];
//		int read = 0;
//		while ((read = bis.read(barr)) > 0) {
//			bos.write(barr, 0, read);
//		}
//		bis.close();
//		bos.flush();
//		fos.flush();
//		bos.close();
//		fos.close();
//	}
//
//	public static void copyResourceToFile(File root, String resourcePath) {
//		File destFile = new File(root, resourcePath);
//		Log.d("resourcePath", resourcePath);
//		Log.d("destFile", destFile.getAbsolutePath());
//		if (!destFile.exists()) {
//			try {
//				destFile.getParentFile().mkdirs();
//				InputStream is = FileUtil.class.getClassLoader().getResourceAsStream(resourcePath);
//				BufferedInputStream bis = new BufferedInputStream(is);
//				FileOutputStream fos = new FileOutputStream(destFile);
//				BufferedOutputStream bos = new BufferedOutputStream(fos);
//				int len = 0;
//				int counter = 0;
//				byte[] bArr = new byte[8192];
//				while ((len = bis.read(bArr)) != -1) {
//					fos.write(bArr, 0, len);
//					counter += len;
//				}
//				Log.d("resourcePath.length()", counter + "");
//				Log.d("destFile", destFile.length() + "");
//				bos.flush();
//				fos.flush();
//				bos.close();
//				fos.close();
//				bis.close();
//				is.close();
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
//	public static void writeFileAsCharset(String fileName, String contents,
//			String newCharset) throws IOException {
//		writeFileAsCharset(new File(fileName), contents, newCharset);
//	}
//
//	public static void writeContentToFile(String fileName, String contents)
//			throws IOException {
//		Log.d("writeContentToFile", fileName);
//		File f = new File(fileName);
//		f.getParentFile().mkdirs();
//		FileWriter fw = new FileWriter(fileName);
//		fw.write(contents);
//		fw.flush();
//		fw.close();
//	}
//
//	public static void writeToFile(Collection<?> collection, File file)
//			throws IOException {
//		if (collection != null && file != null) {
//			FileWriter fw = new FileWriter(file);
//			for (Object obj : collection) {
//				fw.write(new StringBuilder(obj.toString()).append("\r\n")
//						.toString());
//			}
//			fw.flush();
//			fw.close();
//		}
//	}
//
//	/**
//	 * Lưu từng item của iterator thành 1 dòng của file
//	 * 
//	 * @param iter
//	 * @param file
//	 * @throws IOException
//	 */
//	public static void writeToFile(Iterator<?> iter, File file)
//			throws IOException {
//		if (iter != null && file != null) {
//			FileWriter fw = new FileWriter(file);
//			for (; iter.hasNext();) {
//				fw.write(new StringBuilder(iter.next().toString()).append("\r\n").toString());
//			}
//			fw.flush();
//			fw.close();
//		}
//	}
//
//	public static void writeToZipFile(File zip, File source) throws IOException {
//		Log.d("source: " + source.getAbsolutePath(), "length: "
//				+ source.length());
//		Log.d("zip:", zip.getAbsolutePath());
//
//		FileOutputStream fos = new FileOutputStream(zip);
//		BufferedOutputStream bos = new BufferedOutputStream(fos);
//		ZipOutputStream zos = new ZipOutputStream(bos);
//		ZipEntry zipEntry = new ZipEntry(source.getName());
//		zipEntry.setTime(source.lastModified());
//		zos.putNextEntry(zipEntry);
//
//		FileInputStream fis = new FileInputStream(source);
//		byte[] bArr = new byte[32768];
//		int byteRead = 0;
//		while ((byteRead = fis.read(bArr)) > 0) {
//			zos.write(bArr, 0, byteRead);
//		}
//		zos.closeEntry();
//		zos.flush();
//		zos.close();
//		Log.d("zipEntry: " + zipEntry, "compressedSize: "
//				+ zipEntry.getCompressedSize());
//		bos.flush();
//		fos.flush();
//		bos.close();
//		fos.close();
//		fis.close();
//	}
//
//	public static void writeToZipFileAsDateTime(File source) throws IOException {
//		String dateTime = new Date(System.currentTimeMillis()).toString()
//				.replaceAll(":", ".");
//		String sourceAbsolute = source.getAbsolutePath();
//		Log.d("sourceAbsolute: ", sourceAbsolute);
//		String fullName = (sourceAbsolute + "-" + dateTime + ".zip");
//		writeToZipFile(new File(fullName), source);
//	}
//	
//	public static void getDirSize(File f, Entry<Integer, Long> entry) {
//		if (f.isFile()) {
//			entry.setKey(entry.getKey() + 1);
//			entry.setValue(entry.getValue() + f.length());
//		} else {
//			File[] files = f.listFiles();
//			for (File file : files) {
//				if (file.isFile()) {
//					entry.setKey(entry.getKey() + 1);
//					entry.setValue(entry.getValue() + file.length());
//				} else {
//					getDirSize(file, entry);
//				}
//			}
//		}
//		
//	}
//	
//	public static List<File> getFiles(File f, List<File> fileList) {
//		if (fileList == null) {
//			fileList = new LinkedList<File>();
//		}
//		if (f.isFile()) {
//			fileList.add(f);
//		} else {
//			File[] files = f.listFiles();
//			for (File file : files) {
//				if (file.isFile()) {
//					fileList.add(file);
//				} else {
//					getFiles(file, fileList);
//				}
//			}
//		}
//		return fileList;
//	}
//
//	// suffixFile la danh sach 4 chu cuoi
//	public static void getFiles(File[] files, List<File> fileList, String suffixFiles) {
//		for (File file : files) {
//			if (file.isDirectory()) {
//				getFiles(file.listFiles(), fileList, suffixFiles);
//			} else if (!file.isHidden()) {
//				if (suffixFiles != null && suffixFiles.trim().length() > 0) {
//					String path = file.getAbsolutePath().toLowerCase();
//					String suffix = path.substring(path.lastIndexOf(".") + 1);
//					if (suffixFiles.toLowerCase().contains(suffix)) {
//						fileList.add(file);
//					}
//				} else {
//					fileList.add(file);
//				}
//			}
//		}
//	}
//
//	public static boolean delete(File file) {
//		file.setWritable(true);
//		try {
//			if (!file.delete()) {
//				FileOutputStream fos = new FileOutputStream(file);
//				fos.write(0);
//				fos.flush();
//				fos.close();
//			}
//			Log.d("delete", "Deleted file: " + file + " successfully");
//			return true;
//		} catch (IOException e) {
//			Log.e("delete", "The deleting file: " + file + " is not successfully", e);
//			return false;
//		}
//	}
//
//	private static POIFSFileSystem createPOIFSFileSystem(String fileName)
//			throws IOException {
//		FileInputStream fis = new FileInputStream(fileName);
//		POIFSFileSystem fs = new POIFSFileSystem(fis);
//		return fs;
//	}
//
//	public static String convertToText(File inFile)
//			throws FileNotFoundException, IOException {
//		String inFilePath = inFile.getAbsolutePath().toLowerCase();
//		String currentContent = "";
////		FileInputStream fis = new FileInputStream(inFile);
////		if (inFilePath.endsWith("docx")) {
////			XWPFWordExtractor extractor = new XWPFWordExtractor(new XWPFDocument(fis));
////			currentContent = extractor.getText();
////		} else if (inFilePath.endsWith("xlsx")) {
////			XSSFWorkbook workbook = new XSSFWorkbook(fis);
////			XSSFExcelExtractor extractor = new XSSFExcelExtractor(workbook);
////			currentContent = extractor.getText();
////		} else if (inFilePath.endsWith("pptx")) {
////			XMLSlideShow slideShow = new XMLSlideShow(fis);
////			XSLFPowerPointExtractor extractor = new XSLFPowerPointExtractor(slideShow);
////			currentContent = extractor.getText();
////		} else 
//		if (inFilePath.endsWith("doc") || inFilePath.endsWith("rtf")) {
//			currentContent = readWordFileToText(inFile);
//		}
//		return currentContent;
//	}
//
//	public static String readWordFileToText(String wordFileName) throws IOException {
//		POIFSFileSystem fs = createPOIFSFileSystem(wordFileName);
//		WordExtractor we = new WordExtractor(fs);
//		String[] paragraphs = we.getParagraphText();
//		we.close();
//		StringBuilder sb = new StringBuilder();
//		for (String st : paragraphs) {
//			sb.append(st, 0, st.length());
//		}
//		return sb.toString();
//	}
//
////	public File getTempFile(Context context, String url) {
////		File file = null;
////		try {
////			String fileName = Uri.parse(url).getLastPathSegment();
////			file = File.createTempFile(fileName + "--", null, context.getCacheDir());
////		} catch (IOException e) {
////			// Error while creating file
////		}
////		return file;
////	}
////
////	/* Checks if external storage is available for read and write */
////	public boolean isExternalStorageWritable() {
////		String state = Environment.getExternalStorageState();
////		if (Environment.MEDIA_MOUNTED.equals(state)) {
////			return true;
////		}
////		return false;
////	}
////
////	/* Checks if external storage is available to at least read */
////	public boolean isExternalStorageReadable() {
////		String state = Environment.getExternalStorageState();
////		if (Environment.MEDIA_MOUNTED.equals(state)
////				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
////			return true;
////		}
////		return false;
////	}
////	
////	public File getAlbumStorageDir(String albumName) {
////	    // Get the directory for the user's public pictures directory. 
////	    File file = new File(Environment.getExternalStoragePublicDirectory(
////	            Environment.DIRECTORY_PICTURES), albumName);
////	    if (!file.mkdirs()) {
////	        Log.d("getAlbumStorageDir", "Directory not created");
////	    }
////	    return file;
////	}
////	
////	public File getAlbumStorageDir(Context context, String albumName) {
////	    // Get the directory for the app's private pictures directory. 
////	    File file = new File(context.getExternalFilesDir(
////	            Environment.DIRECTORY_PICTURES), albumName);
////	    if (!file.mkdirs()) {
////	        Log.d("getAlbumStorageDir", "Directory not created");
////	    }
////	    return file;
////	}
////
////	public File getReservedStorageDir(Context context, String albumName) {
////		// Get the directory for the app's private pictures directory. 
////		File file = new File(Environment.getExternalStoragePublicDirectory(
////				Environment.DIRECTORY_PICTURES), albumName);
////		if (!file.mkdirs()) {
////			Log.d("getAlbumStorageDir", "Directory not created");
////		}
////		return file;
////	}
////	
//	
//	
//	public static List<File> fileFolderListing(File f) {
//		List<File> files = new LinkedList<File>();
//		return fileFolderListing(f, files);
//	}
//	
//	private static List<File> fileFolderListing(File f, List<File> l) {
//		if (f != null) {
//			if (f.isFile()) {
//				l.add(f);
//			} else {
//				File[] files = f.listFiles();
//				if (files == null) {
//					l.add(f);
//				} else {
//					Arrays.sort(files, new FolderFirstSorting());
//					for (File file : files) {
//						l.add(file);
//					}
//				}
//			}
//			return l;
//		} else {
//			return null;
//		}
//	}
//
//	public static List<File> filesByName(File f) {
//		List<File> files = new LinkedList<File>();
//		return filesByName(f, files);
//	}
//	
//	private static List<File> filesByName(File f, List<File> l) {
//		if (f != null) {
//			if (f.isFile()) {
//				l.add(f);
//			} else {
//				File[] files = f.listFiles();
//				if (files == null) {
//					l.add(f);
//				} else {
//					Arrays.sort(files, new FileFirstSorting());
//					for (File file : files) {
//						filesByName(file, l);
//					}
//				}
//			}
//			return l;
//		} else {
//			return null;
//		}
//	}
//	
//	public static String listFilesByName(File f, String lineSep) {
//		StringBuilder sb = new StringBuilder();
//		return listFilesByName(f, lineSep, sb).toString();
//	}
//	
//	public static List<File> listFilesByName(File f) {
//		return listFilesByName(f, new LinkedList<File>());
//	}
//	
//	public static List<File> listFilesByName(File f, List<File> fList) {
//		Log.d("listFileByName f", "" + f);
//		if (f.isFile()) {
//			fList.add(f);
//		} else if (f.isDirectory()) {
//			File[] files = f.listFiles();
//			if (files != null) {	// không thể hiểu tại sao ở đây có thể bị null, có lẽ file hệ thống có khác
//				Arrays.sort(files, new FileFirstSorting());
//				for (File file : files) {
//					listFilesByName(file, fList);
//				}
//			}
//		}
//		return fList;
//	}
//
//	public static StringBuilder listFilesByName(File f, String lineSep, StringBuilder sb) {
//		Log.d("listFileByName f", "" + f);
//		if (f != null) {
//			if (f.isFile()) {
//				sb.append(f.getAbsolutePath()).append(": ").append(f.length()).append(" bytes.").append(lineSep);
//			} else if (f.isDirectory()) {
//				File[] files = f.listFiles();
//				if (files != null) {	// không thể hiểu tại sao ở đây có thể bị null, có lẽ file hệ thống có khác
//					if (files.length == 0) {
//						sb.append(f.getAbsolutePath()).append(": ").append(f.length()).append(" bytes.").append(lineSep);
//					} else {
//						Arrays.sort(files, new FileFirstSorting());
//						for (File file : files) {
//							listFilesByName(file, lineSep, sb);
//						}
//					}
//				}
//			} else {
//				
//			}
//			return sb;
//		} else {
//			return null;
//		}
//	}
//
//	public static String deleteFiles(File file, String lineSep, boolean includeFolder, String ext) {
//		StringBuilder sb = new StringBuilder();
//		return deleteFiles(file, lineSep, sb, includeFolder,ext).toString();
//	}
//	
//	/**
//	 * @param file file to delete
//	 * @param lineSep lineSep for summary
//	 * @param sb StringBuilder to store results
//	 * @param includeFolder whether also delete folder or not
//	 * @param ext file extension nào sẽ bị xóa
//	 * @return
//	 */
//	public static StringBuilder deleteFiles(File file, String lineSep, StringBuilder sb, boolean includeFolder, String ext) {
//		if (file != null) {
//			if (file.isFile() && file.getName().toLowerCase().endsWith(ext.toLowerCase())) {
//				delete(file, sb);
//			} else {
//				File[] f = file.listFiles();
//				Arrays.sort(f, new FileFirstSorting());
//				for (int i = 0; i < f.length; i++) {
//					if (f[i].isDirectory()) {
//						deleteFiles(f[i], lineSep, sb, includeFolder, ext);
//						if (includeFolder) {
//							delete(f[i], sb);
//						}
//					} else if (f[i].getName().toLowerCase().endsWith(ext.toLowerCase())) {
//						delete(f[i], sb);
//					}
//				}
//			}
//			return sb;
//		} else {
//			return null;
//		}
//	}
//
//	private static void delete(File file, StringBuilder sb) {
//		long length = file.length();
//		boolean deleted = file.delete();
//		if (deleted) {
//			sb.append(file.getAbsolutePath() + " length " + length + " bytes, deleted.\r\n");
//		} else {
//			sb.append(file.getAbsolutePath() + " length " + length + " bytes, can't delete.\r\n");
//		}
//	}
//	
//}
//
//class FileFirstSorting implements Comparator<File>{
//
//	@Override
//	public int compare(File f1, File f2) {
//		if (f1.isFile() && f2.isFile() || f1.isDirectory() && f2.isDirectory()) {
//			return f1.getAbsolutePath().compareTo(f2.getAbsolutePath());
//		} else if (f1.isFile() && f2.isDirectory()) {
//			return -1;
//		} else {
//			return 1;
//		}
//	}
//}
//
//class FolderFirstSorting implements Comparator<File>{
//	
//	@Override
//	public int compare(File f1, File f2) {
//		if (f1.isFile() && f2.isFile() || f1.isDirectory() && f2.isDirectory()) {
//			return f1.getAbsolutePath().toLowerCase().compareTo(f2.getAbsolutePath().toLowerCase());
//		} else if (f1.isFile() && f2.isDirectory()) {
//			return 1;
//		} else {
//			return -1;
//		}
//	}
//}
//
