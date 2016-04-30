package com.free.searcher;

import android.content.*;
import android.net.*;
import android.os.*;
import android.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.zip.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hwpf.converter.*;
import org.apache.poi.hwpf.extractor.*;
import org.apache.poi.poifs.filesystem.*;
import org.apache.poi.ss.usermodel.*;
import java.nio.charset.*;
import org.apache.http.util.*;
import java.util.regex.*;

import android.app.*;
import java.text.*;
import com.free.p7zip.*;


public class FileUtil {

	private static final String ISO_8859_1 = "ISO-8859-1";

	public static void closeQuietly(Closeable closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static void closeWithErrorQuietly(ParcelFileDescriptor pfd, String msg) {
        if (pfd != null) {
            try {
                pfd.closeWithError(msg);
            } catch (IOException ignored) {
            }
        }
    }

    public static void writeFully(File file, byte[] data) throws IOException {
        final OutputStream out = new FileOutputStream(file);
		final BufferedOutputStream bos = new BufferedOutputStream(out);
        try {
            bos.write(data);
        } finally {
			bos.flush();
			bos.close();
        }
    }

    public static byte[] readFully(File file) throws IOException {
        final InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[1 << 20];
            int count;
            while ((count = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        } finally {
			in.close();
        }
    }
	
	public static byte[] readFileToMemory(String file)
	throws IOException {
		FileInputStream fis = new FileInputStream(file);
		FileChannel fileChannel = fis.getChannel();
		long size = fileChannel.size();
		MappedByteBuffer mappedByteBuffer =
			fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
		byte[] data = new byte[(int) size];
		mappedByteBuffer.get(data, 0, (int) size);
		fileChannel.close();
		fis.close();
		return data;
//		return readFileToMemory(new File(file));
	}

	public static byte[] readFileToMemory(File file)
	throws IOException {
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		byte[] data = new byte[(int) file.length()];
		int start = 0;
		int read = 0;
		while ((read = bis.read(data, start, data.length - start)) > 0) {
			start += read;
		}
		bis.close();
		fis.close();
		return data;
	}

	public static String readFileAsCharset(String fileName, String charsetName)
	throws IOException {
		byte[] arr = readFileToMemory(fileName);
		String st = "";
		try {
			st = new String(arr, charsetName);
		} catch (UnsupportedEncodingException e) {
			st = readFileWithCheckEncode(fileName);
		}
		return st;
	}

	public static String readFileAsCharset(File file, String charsetName)
	throws IOException {
		return readFileAsCharset(file.getAbsolutePath(), charsetName);
	}

	public static String convertFileContentToUTF8(File file, String oriCharset)
	throws IOException {
		if (file != null) {
			String encode = readFileWithCheckEncode(file.getAbsolutePath());
			return new String(encode.getBytes(oriCharset), "UTF-8");
		} else {
			return null;
		}
	}

	public static String readFileWithCheckEncode(String filePath)
	throws FileNotFoundException, IOException,
	UnsupportedEncodingException {
		byte[] byteArr = FileUtil.readFileToMemory(filePath);
		if (byteArr.length > 3) {
			Log.d("readFileWithCheckEncode", filePath);
			Log.d("char", (char) byteArr[0] + ": " + (int) byteArr[0]);
			Log.d("char", (char) byteArr[1] + ": " + (int) byteArr[1]);
			Log.d("char", (char) byteArr[2] + ": " + (int) byteArr[2]);

			if (byteArr.length > 3 && (byteArr[0] == -17 && byteArr[1] == -69 && byteArr[2] == -65
				|| byteArr[0] == -61 && byteArr[1] == -96 && byteArr[2] == 13
				|| byteArr[0] == 49 && byteArr[1] == 48 && byteArr[2] == 41
				|| byteArr[0] == 77 && byteArr[1] == -31 && byteArr[2] == -69)) {

				String utf8 = new String(byteArr, Util.UTF8);
				Log.d("is UTF8", filePath + " 1");
				return utf8;
			} else {
				String utf8 = new String(byteArr, Util.UTF8);
//			System.err.println("utf8: " + utf8);
				String fontName = Util.guessFontName(utf8);
				Log.d("fontName", "is " + fontName);
				if (Util.VU_TIMES.equals(fontName)
					|| Util.TIMES_CSX.equals(fontName)
					|| Util.TIMES_CSX_1.equals(fontName)) {
					Log.d("is UTF8", filePath + " 2");
					return utf8;
				} else if (filePath.toLowerCase().contains(".pdf.")) {
					Log.d("is not UTF8", filePath + " pdf String(byteArr)");
					return new String(byteArr);
				} else {
					String notUTF8 = new String(byteArr, ISO_8859_1);
					Log.d("is not UTF8", filePath + " txt String(byteArr, ISO_8859_1)");
					return notUTF8;
				}
			}
		} else {
			return new  String(byteArr);
		}
	}

	public static String readFileByMetaTag(File file)
	throws FileNotFoundException, IOException {
		// System.gc();
		String content = FileUtil.readFileWithCheckEncode(file.getAbsolutePath());
		String charsetName = "";
		if ((charsetName = Util.readValue(content, "charset")).length() > 0) {
			Log.d("file: " + file.getAbsolutePath() + " has charset: "
				  , charsetName);
		}
		try {
			if (charsetName.length() > 0) {
				content = new String(FileUtil.readFileToMemory(file.getAbsolutePath()), charsetName);
//				Log.d(content);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return content;
	}

	public static void copyAssetToDir(Activity activity, String dest, String src) {
		try {
			String newDest = dest + "/" + src;
			File file = new File(newDest);
			if (!file.exists()) {
				System.out.println("ok " + newDest);
				InputStream ins = activity.getAssets().open(src);
				FileUtil.saveISToFile(ins, newDest);
				ins.close();
			} else {
				System.out.println("fail " + newDest);
			}
		} catch (Exception e) {
			Log.e("copyAssetToDir", e.getMessage(), e);
		}
	}

	public static String[] readWordFileToParagraphs(String wordFileName)
	throws IOException {
		POIFSFileSystem fs = createPOIFSFileSystem(wordFileName);
		WordExtractor we = new WordExtractor(fs);
		String[] paragraphs = we.getParagraphText();
		we.close();
		return paragraphs;
	}

	public static String readWordFileToText(File wordFileName) throws FileNotFoundException, IOException {
		try {
			return WordToTextConverter.getText(wordFileName);
		} catch (Exception e) {
			e.printStackTrace();
			FileInputStream fis = new FileInputStream(wordFileName);
			BufferedInputStream bis = new BufferedInputStream(fis);
			Word6Extractor extractor = new Word6Extractor(bis);
			String text = extractor.getText();
			bis.close();
			fis.close();
			extractor.close();
			return text;
		}
	}

	public static Workbook readWorkBook(String inputFile)
	throws FileNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(inputFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		POIFSFileSystem fileSystem = new POIFSFileSystem(bis);
		Workbook wb = new HSSFWorkbook(fileSystem);
		return wb;
	}

	public static void writeWorkBook(Workbook wb, String outputFile)
	throws FileNotFoundException, IOException {
		File fTemp = new File(outputFile + ".tmp");
		FileOutputStream fos = new FileOutputStream(fTemp);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		wb.write(bos);
		bos.flush();
		bos.close();
		fos.close();
		File file = new File(outputFile);
		file.delete();
		fTemp.renameTo(file);
	}

	private static final String DOCTYPE_PATTERN = "<!DOCTYPE .+?>";
	private static final String DOCTYPE_WITH_DTD = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"data/xhtml1-transitional.dtd\">\r\n";
	public static String initStrictHtml(File oldFile, File newFile)
	throws FileNotFoundException, IOException {
		String wholeFile = FileUtil.readFileByMetaTag(oldFile);
		// String wholeFile =
		// FileUtil.readFileAsCharset(oldFile.getAbsolutePath(),
		// currentCharset);
		wholeFile = Util.fixAttrValueApos(wholeFile);
		wholeFile = Util.fixMetaTagsUTF8(wholeFile);
		wholeFile = Util.fixEndTags(wholeFile);
		wholeFile = Util.fixCharCode(wholeFile);
		wholeFile = Util.replaceAll(wholeFile, Util.ENTITY_NAME,
									Util.ENTITY_CODE);
		wholeFile = wholeFile.replaceAll("[\\r\\n\\f]+", " ");
		// wholeFile = wholeFile.replaceAll("[\\-]+", "-");
		// wholeFile = wholeFile.replaceAll("<!-", "<!--");
		// wholeFile = wholeFile.replaceAll("->", "-->");
		// wholeFile = wholeFile.replaceAll("&ntilde;", "ñ");
		// wholeFile = wholeFile.replaceAll("&Ntilde;", "Ñ");
		// wholeFile = wholeFile.replaceAll("&rsquo;", "’");
		// wholeFile = wholeFile.replaceAll("&lsquo;", "‘");
		// wholeFile = wholeFile.replaceAll("&ndash;", "–");
		if (wholeFile.indexOf("<!DOCTYPE") < 0) {
			wholeFile = new StringBuilder(DOCTYPE_WITH_DTD).append(wholeFile).toString();
		} else {
			wholeFile = wholeFile.replaceFirst(DOCTYPE_PATTERN, DOCTYPE_WITH_DTD);
		}
		FileUtil.writeFileAsCharset(newFile, wholeFile, "UTF-8");

		return wholeFile;
	}

	public static void printInputStream(InputStream is, OutputStream os,
										String inCharset, String outCharset) throws IOException {
		if (Util.isEmpty(inCharset)) {
			inCharset = "UTF-8";
		}
		if (Util.isEmpty(outCharset)) {
			outCharset = "UTF-8";
		}
		try {
			BufferedReader from = new BufferedReader(
				// new InputStreamReader(new FileInputStream("kanji.txt"),
				new InputStreamReader(is, inCharset));
			PrintWriter to = new PrintWriter(new OutputStreamWriter(
												 // check
												 // enco
												 os, outCharset));
			// new FileOutputStream("sverige.txt"), "ISO8859_3"));

			// reading and writing here...
			String line = from.readLine();
			System.out.println("-->" + line + "<--");
			to.println(line);
			from.close();
			to.flush();
			to.close();
		} catch (UnsupportedEncodingException exc) {
			System.err.println("Bad encoding" + exc);
			return;
		} catch (IOException err) {
			System.err.println("I/O Error: " + err);
			return;
		}
	}

	private static final byte[] UTF8_ESCAPE = new byte[] { -17, -69, -65 };

	public static void writeFileAsCharset(File file, String contents,
										  String newCharset) throws IOException {
		Log.d("Writing file", file.getAbsolutePath());
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		File tempF = new File(file.getAbsolutePath() + ".tmp");
		FileOutputStream fos = new FileOutputStream(tempF);
		FileChannel fileChannel = fos.getChannel();
		if (Util.UTF8.equalsIgnoreCase(newCharset)) {
			fileChannel.write(ByteBuffer.wrap(UTF8_ESCAPE));
		}
		byte[] oldCharArr = contents.getBytes(newCharset);
		fileChannel.write(ByteBuffer.wrap(oldCharArr));
		fileChannel.close();
		fos.flush();
		fos.close();
		file.delete();
		tempF.renameTo(file);
	}

	public static void writeAppendFileAsCharset(File file, String contents,
												String newCharset) throws IOException {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		raf.seek(raf.length());
		FileChannel fileChannel = raf.getChannel();
		if (raf.length() == 0 && Util.UTF8.equalsIgnoreCase(newCharset)) {
			fileChannel.write(ByteBuffer.wrap(UTF8_ESCAPE));
		}
		byte[] oldCharArr = contents.getBytes(newCharset);
		fileChannel.write(ByteBuffer.wrap(oldCharArr));
		fileChannel.close();
		raf.close();
	}

	public static void changeUtf8NotBOM2Utf8(String src, String dest)
	throws UnsupportedEncodingException, FileNotFoundException,
	IOException {
		writeFileAsCharset(new File(dest), 
						   new String(readFileToMemory(new File(src)), Util.UTF8), 
						   Util.UTF8);
	}

	public static void saveObj(Object obj, String fileName) throws IOException {
		File f = new File(fileName + ".tmp");
		f.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(f);
		ObjectOutputStream out = new ObjectOutputStream(fos);
		out.writeObject(obj);
		out.flush();
		out.close();
		fos.flush();
		fos.close();
		File file = new File(fileName);
		file.delete();
		f.renameTo(file);
	}

	public static Object restore(String fileName) throws IOException,
	ClassNotFoundException {
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream in = new ObjectInputStream(fis);
		Object obj = in.readObject();
		in.close();
		return obj;
	}

	public static void saveBArrToFile(String fileName, byte[] barr)
	throws IOException {
		File file = new File(fileName);
		file.getParentFile().mkdirs();
		File tempFile = new File(fileName + ".tmp");
		FileOutputStream fos = new FileOutputStream(tempFile);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		bos.write(barr, 0, barr.length);
		bos.flush();
		fos.flush();
		bos.close();
		fos.close();
		file.delete();
		tempFile.renameTo(file);
	}

	public static void saveISToFile(InputStream is, String fileName)
	throws IOException {
		File file = new File(fileName);
		file.getParentFile().mkdirs();
		File tempFile = new File(fileName + ".tmp");
		FileOutputStream fos = new FileOutputStream(tempFile);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		BufferedInputStream bis = new BufferedInputStream(is);
		byte[] barr = new byte[32768];
		int read = 0;
		while ((read = bis.read(barr)) > 0) {
			bos.write(barr, 0, read);
		}
		bis.close();
		bos.flush();
		fos.flush();
		bos.close();
		fos.close();
		file.delete();
		tempFile.renameTo(file);
	}

	public static void copyResourceToFile(File root, String resourcePath) {
		File destFile = new File(root, resourcePath);
		Log.d("resourcePath", resourcePath);
		Log.d("destFile", destFile.getAbsolutePath());
		if (!destFile.exists()) {
			try {
				InputStream is = FileUtil.class.getClassLoader().getResourceAsStream(resourcePath);
				saveISToFile(is, destFile.getAbsolutePath());
				is.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void writeFileAsCharset(String fileName, String contents,
										  String newCharset) throws IOException {
		writeFileAsCharset(new File(fileName), contents, newCharset);
	}

	public static void writeContentToFile(String fileName, String contents)
	throws IOException {
		Log.d("writeContentToFile", fileName);
		File f = new File(fileName);
		f.getParentFile().mkdirs();
		File tempFile = new File(fileName + ".tmp");
		FileWriter fw = new FileWriter(tempFile);
		BufferedWriter bw = new BufferedWriter(fw);
		int length = contents.length();
		if (length > 0) {
			bw.write(contents);
//			int apart =  Math.min(length, 65536);
//			int times = length / apart;
//			for (int i = 0; i < times; i++) {
//				bw.write(contents, i * apart, apart);
//			}
//			if (length % apart != 0) {
//				bw.write(contents, times * apart, length - times * apart);
//			}
			bw.flush();
			fw.flush();
			bw.close();
			fw.close();
			f.delete();
			tempFile.renameTo(f);
		}
	}

	public static void writeToFile(Collection<?> collection, File file)
	throws IOException {
		if (collection != null && file != null) {
			file.getParentFile().mkdirs();
			File tempFile = new File(file.getAbsolutePath() + ".tmp");
			FileWriter fw = new FileWriter(tempFile);
			for (Object obj : collection) {
				fw.write(new StringBuilder(obj.toString()).append("\r\n")
						 .toString());
			}
			fw.flush();
			fw.close();
			file.delete();
			tempFile.renameTo(file);
		}
	}

	/**
	 * Lưu từng item của iterator thành 1 dòng của file
	 * 
	 * @param iter
	 * @param file
	 * @throws IOException
	 */
	public static void writeToFile(Iterator<?> iter, File file) throws IOException {
		if (iter != null && file != null) {
			file.getParentFile().mkdirs();
			File tempFile = new File(file.getAbsolutePath() + ".tmp");
			FileWriter fw = new FileWriter(tempFile);
			for (; iter.hasNext();) {
				fw.write(new StringBuilder(iter.next().toString()).append("\r\n").toString());
			}
			fw.flush();
			fw.close();
			file.delete();
			tempFile.renameTo(file);
		}
	}

	/**
	 0: number of files
	 1: total length
	 2: number of directories
	 */
	public static void getDirSize(final File f, final long[] l) {
		if (f.isFile()) {
			l[0]++;
			l[1] += f.length();
		} else {
			Stack<File> stk = new Stack<>();
			stk.add(f);
			File fi = null;
			File[] fs;
			while (stk.size() > 0) {
				fi = stk.pop();
				fs = fi.listFiles();
				for (File f2 : fs) {
					if (f2.isDirectory()) {
						stk.push(f2);
						l[2]++;
					} else {
						l[0]++;
						l[1] += f2.length();
					}
				}
			}
		}
	}

	public static List<File> getFiles(String[] fs) {
		File[] farr = new File[fs.length];
		int i = 0;
		for (String f : fs) {
			farr[i++] = new File(f);
		}
		return FileUtil.getFiles(farr);
	}

	public static List<File> getFiles(File[] fs) {
		final Set<File> set = new HashSet<File>(fs.length);
		final Stack<File> stk = new Stack<>();
		for (File f : fs) {
			if (f.isDirectory()) {
				stk.push(f);
			} else {
				set.add(f);
			}
		}
		File fi = null;
		while (stk.size() > 0) {
			fi = stk.pop();
			fs = fi.listFiles();
			for (File f : fs) {
				if (f.isDirectory()) {
					stk.push(f);
				} else {
					set.add(f);
				}
			}
		}
		ArrayList<File> arrayList = new ArrayList<File>(set.size());
		arrayList.addAll(set);
		return arrayList;
	}

	public static List<File> getFiles(final File ff[], final String suffix) {
		final Set<File> set = new HashSet<File>(ff.length);
		for (File f : ff) {
			set.addAll(getFiles(f, suffix));
		}
		ArrayList<File> arrayList = new ArrayList<File>(set.size());
		arrayList.addAll(set);
		return arrayList;
	}

	public static List<File> getFiles(final File ff, final String suffix) {
		final List<File> lf = new LinkedList<File>();
		final Stack<File> stk = new Stack<>();
		if (ff.isFile()) {
			if (suffix == null 
				|| suffix.trim().length() == 0 
				|| ".*".equals(suffix.trim())) {
				lf.add(ff);
			} else {
				String fName = ff.getName();
				int lastIndexOf = fName.lastIndexOf(".");
				if (lastIndexOf >= 0) {
					String extLower = fName.substring(lastIndexOf).toLowerCase();
					String[] suffixes = suffix.toLowerCase().split("; *");
					Arrays.sort(suffixes);
					boolean chosen = Arrays.binarySearch(suffixes, extLower) >= 0;
					if (chosen) {
						lf.add(ff);
					}
				}
			}
		} else {
			stk.push(ff);
			File[] fs;
			File fi = null;
			if (suffix == null || suffix.trim().length() == 0 || ".*".equals(suffix.trim())) {
				while (stk.size() > 0) {
					fi = stk.pop();
					fs = fi.listFiles();
					for (File f : fs) {
						if (f.isDirectory()) {
							stk.push(f);
						} else {
							lf.add(f);
						}
					}
				}
			} else {
				String[] suffixes = suffix.toLowerCase().split("[; ]*");
				Arrays.sort(suffixes);
				while (stk.size() > 0) {
					fi = stk.pop();
					fs = fi.listFiles();
					for (File f : fs) {
						if (f.isDirectory()) {
							stk.push(f);
						} else {
							String fName = f.getName();
							int lastIndexOf = fName.lastIndexOf(".");
							if (lastIndexOf >= 0) {
								String extLower = fName.substring(lastIndexOf).toLowerCase();
								boolean chosen = Arrays.binarySearch(suffixes, extLower) >= 0;
								if (chosen) {
									lf.add(f);
								}
							}
						}
					}
				}
			}
		}
		return lf;
	}

	// suffixFile la danh sach 4 chu cuoi
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

	public static boolean delete(File file) {
		file.setWritable(true);
		try {
			if (!file.delete()) {
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(0);
				fos.flush();
				fos.close();
			}
			Log.d("delete", "Deleted file: " + file + " successfully");
			return true;
		} catch (IOException e) {
			Log.d("delete", "The deleting file: " + file + " is not successfully", e);
			return false;
		}
	}

	private static POIFSFileSystem createPOIFSFileSystem(String fileName)
	throws IOException {
		FileInputStream fis = new FileInputStream(fileName);
		POIFSFileSystem fs = new POIFSFileSystem(new BufferedInputStream(fis));
		return fs;
	}

	public static String convertToText(File inFile)
	throws FileNotFoundException, IOException {
		String inFilePath = inFile.getAbsolutePath().toLowerCase();
		String currentContent = "";
//		FileInputStream fis = new FileInputStream(inFile);
//		if (inFilePath.endsWith("docx")) {
//			XWPFWordExtractor extractor = new XWPFWordExtractor(new XWPFDocument(fis));
//			currentContent = extractor.getText();
//		} else if (inFilePath.endsWith("xlsx")) {
//			XSSFWorkbook workbook = new XSSFWorkbook(fis);
//			XSSFExcelExtractor extractor = new XSSFExcelExtractor(workbook);
//			currentContent = extractor.getText();
//		} else if (inFilePath.endsWith("pptx")) {
//			XMLSlideShow slideShow = new XMLSlideShow(fis);
//			XSLFPowerPointExtractor extractor = new XSLFPowerPointExtractor(slideShow);
//			currentContent = extractor.getText();
//		} else 
		if (inFilePath.endsWith("doc") || inFilePath.endsWith("rtf")) {
			currentContent = readWordFileToText(inFile);
		}
		return currentContent;
	}

	public static String readWordFileToText(String wordFileName) throws IOException {
		POIFSFileSystem fs = createPOIFSFileSystem(wordFileName);
		WordExtractor we = new WordExtractor(fs);
		String[] paragraphs = we.getParagraphText();
		we.close();
		StringBuilder sb = new StringBuilder();
		for (String st : paragraphs) {
			sb.append(st, 0, st.length());
		}
		return sb.toString();
	}

	public File getTempFile(Context context, String url) {
		File file = null;
		try {
			String fileName = Uri.parse(url).getLastPathSegment();
			file = File.createTempFile(fileName + "--", null, context.getCacheDir());
		} catch (IOException e) {
			// Error while creating file
		}
		return file;
	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
			|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	public File getAlbumStorageDir(String albumName) {
	    // Get the directory for the user's public pictures directory. 
	    File file = new File(Environment.getExternalStoragePublicDirectory(
								 Environment.DIRECTORY_PICTURES), albumName);
	    if (!file.mkdirs()) {
	        Log.d("getAlbumStorageDir", "Directory not created");
	    }
	    return file;
	}

	public File getAlbumStorageDir(Context context, String albumName) {
	    // Get the directory for the app's private pictures directory. 
	    File file = new File(context.getExternalFilesDir(
								 Environment.DIRECTORY_PICTURES), albumName);
	    if (!file.mkdirs()) {
	        Log.d("getAlbumStorageDir", "Directory not created");
	    }
	    return file;
	}

	public File getReservedStorageDir(Context context, String albumName) {
		// Get the directory for the app's private pictures directory. 
		File file = new File(Environment.getExternalStoragePublicDirectory(
								 Environment.DIRECTORY_PICTURES), albumName);
		if (!file.mkdirs()) {
			Log.d("getAlbumStorageDir", "Directory not created");
		}
		return file;
	}

	public static void orderFilesByTime(File sourceDir) {
		if (sourceDir.isDirectory()) {
			File[] files = sourceDir.listFiles();
			Arrays.sort(files, 0, files.length, new TimeComparator());
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				String fileName = file.getName();
				if (!fileName.startsWith("test-")) {
					file.renameTo(new File(file.getParent() + "/" + "test-"
										   + file.lastModified() + fileName));
				}
			}
		}
	}

	public static List<File> fileFolderListing(File f) {
		List<File> l = new LinkedList<File>();
		if (f != null) {
			if (f.isFile()) {
				l.add(f);
			} else {
				File[] files = f.listFiles();
				if (files == null) {
					l.add(f);
				} else {
					Arrays.sort(files, new FolderFirstSorting());
					for (File file : files) {
						l.add(file);
					}
				}
			}
		}
		return l;
	}

	public static List<File> listFilesByName(File f) {
		Log.d("listFileByName f", "" + f);
		final List<File> fList = new  LinkedList<>();
		final Stack<File> stk = new Stack<>();
		if (f.isDirectory()) {
			stk.push(f);
		} else {
			fList.add(f);
		}
		File fi = null;
		File[] fs;
		while (stk.size() > 0) {
			fi = stk.pop();
			fs = fi.listFiles();
			for (File f2 : fs) {
				if (f2.isDirectory()) {
					stk.push(f2);
				} else {
					fList.add(f2);
				}
			}
		}
		return fList;
	}

	public static StringBuilder listFilesByName(File f, String lineSep) {
		Log.d("listFileByName f", "" + f);
		StringBuilder sb = new  StringBuilder();
		if (f != null) {
			final Stack<File> stk = new Stack<>();
			if (f.isDirectory()) {
				stk.push(f);
			} else {
				sb.append(f.getAbsolutePath()).append(": ").append(f.length()).append(" bytes.").append(lineSep);
			}
			File fi = null;
			File[] fs;
			while (stk.size() > 0) {
				fi = stk.pop();
				fs = fi.listFiles();
				for (File f2 : fs) {
					if (f2.isDirectory()) {
						stk.push(f2);
					} else {
						sb.append(f2.getAbsolutePath()).append(": ").append(f2.length()).append(" bytes.").append(lineSep);
					}
				}
			}

		}
		return sb;
	}

	public static String deleteFiles(File file, String lineSep, boolean includeFolder, String ext) {
		StringBuilder sb = new StringBuilder();
		return deleteFiles(file, lineSep, sb, includeFolder,ext).toString();
	}

	/**
	 * @param file file to delete
	 * @param lineSep lineSep for summary
	 * @param sb StringBuilder to store results
	 * @param includeFolder whether also delete folder or not
	 * @param ext file extension nào sẽ bị xóa
	 * @return
	 */
	public static StringBuilder deleteFiles(File file, String lineSep, StringBuilder sb, boolean includeFolder, String ext) {
		if (file != null) {
			if (file.isFile() && file.getName().toLowerCase().endsWith(ext.toLowerCase())) {
				delete(file, sb);
			} else {
				File[] fs = file.listFiles();
				Arrays.sort(fs, new FileFirstSorting());
				for (int i = 0; i < fs.length; i++) {
					if (fs[i].isDirectory()) {
						deleteFiles(fs[i], lineSep, sb, includeFolder, ext);
						if (includeFolder) {
							delete(fs[i], sb);
						}
					} else if (fs[i].getName().toLowerCase().endsWith(ext.toLowerCase())) {
						delete(fs[i], sb);
					}
				}
			}
			return sb;
		} else {
			return null;
		}
	}

	private static void delete(File file, StringBuilder sb) {
		long length = file.length();
		boolean deleted = file.delete();
		if (deleted) {
			sb.append(file.getAbsolutePath() + " length " + length + " bytes, deleted.\r\n");
		} else {
			sb.append(file.getAbsolutePath() + " length " + length + " bytes, can't delete.\r\n");
		}
	}


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
			Log.d("loadFiles.zipEntry.getName()", zipEntryName);
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



	public static void compressAFileToZip(File zip, File source) throws IOException {
		Log.d("source: " + source.getAbsolutePath(), ", length: " + source.length());
		Log.d("zip:", zip.getAbsolutePath());
		zip.getParentFile().mkdirs();
		File tempFile = new File(zip.getAbsolutePath() + ".tmp");
		FileOutputStream fos = new FileOutputStream(tempFile);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		ZipOutputStream zos = new ZipOutputStream(bos);
		ZipEntry zipEntry = new ZipEntry(source.getName());
		zipEntry.setTime(source.lastModified());
		zos.putNextEntry(zipEntry);

		FileInputStream fis = new FileInputStream(source);
		BufferedInputStream bis = new BufferedInputStream(fis);
		byte[] bArr = new byte[4096];
		int byteRead = 0;
		while ((byteRead = bis.read(bArr)) != -1) {
			zos.write(bArr, 0, byteRead);
		}
		zos.flush();
		zos.closeEntry();
		zos.close();
		Log.d("zipEntry: " + zipEntry, "compressedSize: "
			  + zipEntry.getCompressedSize());
		bos.flush();
		fos.flush();
		bos.close();
		fos.close();
		bis.close();
		fis.close();
		zip.delete();
		tempFile.renameTo(zip);
	}

	public static void writeToZipFileAsDateTime(File source) throws IOException {
		String dateTime = new Date(System.currentTimeMillis()).toString()
			.replaceAll("[:/]", ".");
		String sourceAbsolute = source.getAbsolutePath();
		Log.d("sourceAbsolute: ", sourceAbsolute);
		String fullName = (sourceAbsolute + "-" + dateTime + ".zip");
		compressAFileToZip(new File(fullName), source);
	}

	/**
	 * Compress a String to a zip file that has only one entry like zipName
	 * The name shouldn't have .zip
	 * 
	 * @param content
	 * @param fName
	 * @throws IOException 
	 */
	public static void zipAContentAsFileName(String fName, String content, String charset) throws IOException {
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
		zipAContentAsFileName(fName, content, "UTF-8");
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
	public static File extractAZipEntryToDisk(File zfile, ZipEntry entry, String outputFolder)
	throws ZipException, IOException {
		Log.d("saveZipEntryToDisk.zfile", zfile.toString());
		Log.d("saveZipEntryToDisk.entry", entry.toString());
		Log.d("saveZipEntryToDisk.outputFolder", outputFolder);
		ZipFile zipFile = new ZipFile(zfile);
		String zipEntryName = outputFolder + "/" + entry.getName();
		if (!entry.isDirectory()) {
			new File(zipEntryName).getParentFile().mkdirs();
			InputStream inputStream = zipFile.getInputStream(entry);
			FileUtil.saveISToFile(inputStream, zipEntryName);
			inputStream.close();
		}
		zipFile.close();
		return new File(zipEntryName);
	}

	public static void extractEntriesInZipToDisk(File zFile, List<ZipEntry> entries, String outputFolder)
	throws ZipException, IOException {
		Log.d("extractAFolderInZipToDisk.zfile", zFile.toString());
		Log.d("extractAFolderInZipToDisk.outputFolder", outputFolder);
		ZipFile zipF = new ZipFile(zFile);
		for (ZipEntry zipEntry : entries) {
			if (!zipEntry.isDirectory()) {
				String name = outputFolder + "/" + zipEntry.getName();
				new File(name).getParentFile().mkdirs();
				InputStream inputStream = zipF.getInputStream(zipEntry);
				FileUtil.saveISToFile(inputStream, name);
				inputStream.close();
			}
		}
		zipF.close();
	}

	public static void extractAFolderInZipToDisk(File zipFile, String folderNameInZip, String outputFolder)
	throws ZipException, IOException {
		Log.d("extractAFolderInZipToDisk: ", zipFile.toString() + ", folderNameInZip = " + folderNameInZip);
		long start = System.currentTimeMillis();

		ZipFile zipF = new ZipFile(zipFile);
		Enumeration<? extends java.util.zip.ZipEntry> en = zipF.entries();
		ZipEntry zipEntry = null;

		String folderLowerCase = folderNameInZip.toLowerCase();
		int counter = 0;

		while (en.hasMoreElements()) {
			zipEntry = en.nextElement();
			String zipEntryName = zipEntry.getName();
			counter++;
			Log.d("extractAFolderInZipToDisk", zipEntryName);
			if (zipEntryName.toLowerCase().startsWith(folderLowerCase)
				&& !zipEntry.isDirectory()) {
				String name = outputFolder + "/" + zipEntryName;
				new File(name).getParentFile().mkdirs();
				InputStream inputStream = zipF.getInputStream(zipEntry);
				FileUtil.saveISToFile(inputStream, name);
				inputStream.close();
			}
		}
		Log.d("Loaded ", counter + " files, took: "
			  + (System.currentTimeMillis() - start) + " milliseconds.");
		zipF.close();
	}

	public static File extractAZipEntry(String inFile, String entryName, String outDirFilePath) 
	throws IOException {
		Log.d("extractAZipEntry", inFile);
		FileInputStream fis = new FileInputStream(inFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ZipInputStream zis = new ZipInputStream(bis);
		try {
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				String zeName = ze.getName();
				System.out.println("zeName, entryName " + zeName.toString() + ", " + entryName);
				if (!ze.isDirectory() && zeName.equals(entryName)) {
					zis.close();
					bis.close();
					fis.close();
					return extractAZipEntryToDisk(new File(inFile), ze, outDirFilePath);
				}
			}
		} catch (IOException e) {
			Log.d("readZipEntry", e.getMessage(), e);
		} finally {
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
			BufferedInputStream bis = new BufferedInputStream(is);
			int length = barr.length;
			while ((len = bis.read(barr, read, length - read)) != -1) {
				read += len;
			}
			bis.close();
			is.close();
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
	
//	public static String readZipEntryContent(String inFile, String entryName) 
//	throws IOException {
//		Log.d("inFile", inFile);
////		FileInputStream fis = new FileInputStream(inFile);
////		BufferedInputStream bis = new BufferedInputStream(fis);
////		InputStream zis = new ZipInputStream(bis);
//		ZipFile zf = new ZipFile(inFile);
//		ZipEntry ze = new ZipEntry(entryName);
//		InputStream inputStream = zf.getInputStream(ze);
//		BufferedInputStream zis = new BufferedInputStream(inputStream);
//		try {
////			while ((ze = zis.getNextEntry()) != null) {
//			String zeName = ze.getName();
//			System.out.println("ze, entryName " + ze + ", " + entryName);
//			if (!ze.isDirectory() && zeName.equals(entryName)) {
//				System.out.println("read entryName " + entryName + ", " + ze.getSize());
////					long size = ze.getSize();
////					if (size < 0) {
//				byte[] buffer = new byte[4096];
//				ByteArrayBuffer bb = new ByteArrayBuffer(4096);
//				int count = 0;
//				while ((count = zis.read(buffer, 0, buffer.length)) != -1) {
//					bb.append(buffer, 0, count);
//				}
//				System.out.println("entryName " + ze + " read, size: " + bb.length());
//				return new  String(bb.toByteArray(), "utf-8");
////					} else {
////						byte[] buffer = new byte[(int) size];
////						int count = 0;
////						int read = 0;
////						while ((count = zis.read(buffer, read += count, buffer.length - read)) != -1) {
////						}
////						System.out.println("entryName " + ze + " read, size: " + size);
////						return buffer;
////					}
//			}
////			}
//		}
//		catch (IOException e) {
//			Log.d("readZipEntryContent", e.getMessage(), e);
//		} finally {
//			zis.close();
//			inputStream.close();
////			bis.close();
////			fis.close();
//		}
//		return "";
//	}

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
	 * N?n file hay th? m?c v?o file zipFileName
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static void compressAFileOrFolderRecursiveToZip(String file) throws IOException {
		compressAFileOrFolderRecursiveToZip(new File(file));
	}

	/**
	 * N?n file hay th? m?c v?o file zipFileName
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static void compressAFileOrFolderRecursiveToZip(File file) throws IOException {
		compressAFileOrFolderRecursiveToZip(file, null);
	}

	/**
	 * N?n file hay th? m?c v?o file zipFileName
	 * 
	 * @param file
	 * @param zipFileName
	 * @throws IOException
	 */
	public static void compressAFileOrFolderRecursiveToZip(
		String file, String zipFileName) throws IOException {
		compressAFileOrFolderRecursiveToZip(new File(file), zipFileName);
	}


	/**
	 * N?n file hay th? m?c v?o file zipFileName
	 * 
	 * @param file
	 * @param zipFileName
	 * @throws IOException
	 */
	public static void compressAFileOrFolderRecursiveToZip(File file, String zipFileName) 
	throws IOException {
		String folderName;
		List<File> filesNeedCompress;
		Log.d(file.toString(), "" + file.exists());
		if (file.isDirectory()) {
			filesNeedCompress = FileUtil.getFiles(file.listFiles());
			folderName = file.getAbsolutePath();
		} else if (file.isFile()) {
			filesNeedCompress = new LinkedList<File>();
			filesNeedCompress.add(file);
			folderName = file.getParent();
		} else {
			return;
		}
		Log.d("list", Util.collectionToString(filesNeedCompress, true, "\r\n"));

		if (zipFileName == null || zipFileName.trim().length() == 0) {
			zipFileName = file.getAbsolutePath() + ".zip";
		}

		String zipFileNameTmp = zipFileName + ".tmp";
		File fileZFTmp = new File(zipFileNameTmp);
		FileOutputStream fos = new FileOutputStream(fileZFTmp);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		ZipOutputStream zos = new ZipOutputStream(bos);

		try {
			for (File f : filesNeedCompress) {
				ZipEntry zipEntry = new ZipEntry(
					f.getAbsolutePath().substring(folderName.length() + 1));
				zipEntry.setTime(f.lastModified());
				zos.putNextEntry(zipEntry);

				FileInputStream fis = new FileInputStream(f);
				BufferedInputStream bis = new BufferedInputStream(fis);
				byte[] bArr = new byte[4096];
				int byteRead = 0;
				while ((byteRead = bis.read(bArr)) != -1) {
					zos.write(bArr, 0, byteRead);
				}
				zos.flush();
				zos.closeEntry();
				bis.close();
				fis.close();
			}
		} finally {
			bos.flush();
			fos.flush();
			zos.close();
			bos.close();
			fos.close();
		}
		File fileZ = new File(zipFileName);
		fileZ.delete();
		fileZFTmp.renameTo(fileZ);
	}

	public static void compressAFileOrFolderRecursiveToZip(File file, String zipFileName, String exceptFiles) 
	throws IOException {
		String folderName;
		List<File> filesNeedCompress;
		Log.d(file.toString(), "" + file.exists());
		if (file.isDirectory()) {
			filesNeedCompress = FileUtil.getFiles(file.listFiles());
			folderName = file.getAbsolutePath();
		} else if (file.isFile()) {
			filesNeedCompress = new LinkedList<File>();
			filesNeedCompress.add(file);
			folderName = file.getParent();
		} else {
			return;
		}
		Log.d("list", Util.collectionToString(filesNeedCompress, true, "\r\n"));

		Pattern pat = Pattern.compile(exceptFiles, Pattern.CASE_INSENSITIVE);
		if (zipFileName == null || zipFileName.trim().length() == 0) {
			zipFileName = file.getAbsolutePath() + ".zip";
		}

		String zipFileNameTmp = zipFileName + ".tmp";
		File fileZFTmp = new File(zipFileNameTmp);
		FileOutputStream fos = new FileOutputStream(fileZFTmp);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		ZipOutputStream zos = new ZipOutputStream(bos);

		try {
			for (File f : filesNeedCompress) {
				if (!pat.matcher(f.getAbsolutePath()).matches()) {
					ZipEntry zipEntry = new ZipEntry(
						f.getAbsolutePath().substring(folderName.length() + 1));
					zipEntry.setTime(f.lastModified());
					zos.putNextEntry(zipEntry);

					FileInputStream fis = new FileInputStream(f);
					BufferedInputStream bis = new BufferedInputStream(fis);
					byte[] bArr = new byte[4096];
					int byteRead = 0;
					while ((byteRead = bis.read(bArr)) != -1) {
						zos.write(bArr, 0, byteRead);
					}
					zos.flush();
					zos.closeEntry();
					bis.close();
					fis.close();
				}
			}
		} finally {
			bos.flush();
			fos.flush();
			zos.close();
			bos.close();
			fos.close();
		}
		File fileZ = new File(zipFileName);
		fileZ.delete();
		fileZFTmp.renameTo(fileZ);
	}

	public static void compressAFileOrFolderRecursiveToZip(File file, String zipFileName, String excepts, String includes) 
	throws IOException {
		//String folderName;
		List<File> filesNeedCompress;
		Log.d(file.toString(), "" + file.exists());
		if (file.isDirectory()) {
			filesNeedCompress = FileUtil.getFiles(file.listFiles());
			//folderName = file.getAbsolutePath();
		} else if (file.isFile()) {
			filesNeedCompress = new LinkedList<File>();
			filesNeedCompress.add(file);
			//folderName = file.getParent();
		} else {
			return;
		}
		Log.d("list", Util.collectionToString(filesNeedCompress, true, "\r\n"));

		Pattern patExcept = Pattern.compile(excepts, Pattern.CASE_INSENSITIVE);
		//Pattern patInclude= Pattern.compile(includes, Pattern.CASE_INSENSITIVE);

		if (zipFileName == null || zipFileName.trim().length() == 0) {
			zipFileName = file.getAbsolutePath() + ".7z";
		}
		
//		String zipFileNameTmp = zipFileName + ".tmp";
//		File fileZFTmp = new File(zipFileNameTmp);
//		FileOutputStream fos = new FileOutputStream(fileZFTmp);
//		BufferedOutputStream bos = new BufferedOutputStream(fos);
//		ZipOutputStream zos = new ZipOutputStream(bos);

		String zipFileTmp = zipFileName + ".tmp";
		BufferedWriter bw = new BufferedWriter(new FileWriter(zipFileTmp));
		try {
			int filePathLength = file.getAbsolutePath().length() + 1;
			for (File f : filesNeedCompress) {
				String fPath = f.getAbsolutePath();
				if (
				//patInclude.matcher(fPath).matches() &&
					patExcept.matcher(fPath).matches()) { //!
					bw.write(fPath, filePathLength, fPath.length() - filePathLength);
					bw.newLine();
//					ZipEntry zipEntry = new ZipEntry(
//						f.getAbsolutePath().substring(folderName.length() + 1));
//					zipEntry.setTime(f.lastModified());
//					zos.putNextEntry(zipEntry);
//
//					FileInputStream fis = new FileInputStream(f);
//					BufferedInputStream bis = new BufferedInputStream(fis);
//					byte[] bArr = new byte[4096];
//					int byteRead = 0;
//					while ((byteRead = bis.read(bArr)) != -1) {
//						zos.write(bArr, 0, byteRead);
//					}
//					zos.flush();
//					zos.closeEntry();
//					bis.close();
//					fis.close();
				}
			}
		} finally {
//			bos.flush();
//			fos.flush();
//			zos.close();
//			bos.close();
//			fos.close();
			bw.flush();
			bw.close();
		}
//		File fileZ = new File(zipFileName);
//		fileZ.delete();
//		fileZFTmp.renameTo(fileZ);
		new Andro7za().compress(zipFileName, "-t7z", "", "-xr@"+zipFileTmp, "-ir!"+file.getAbsolutePath()+"/"+includes);
		new File(zipFileTmp).delete();
	}

	/**
	 * Gi?i n?n zipFileName v?o folder c?a zip
	 * 
	 * @param zipFileName
	 * @throws IOException
	 */
	public static void extractZipToFolder(String zipFileName) throws IOException {
		extractZipToFolder(zipFileName, null);
	}

	/**
	 * Gi?i n?n zipFileName v?o parentFolder, t? t?o th? m?c n?u parentFolder ch?a t?n t?i
	 * 
	 * @param zipFileName
	 * @param parentFolder
	 * @throws IOException
	 */
	public static void extractZipToFolder(String zipFileName, String parentFolder)
	throws IOException {
		File inF = new File(zipFileName);
		if (parentFolder == null || parentFolder.trim().length() == 0) {
			parentFolder = inF.getParent();
		}
		ZipFile zf = new ZipFile(inF);

		File parentFile = new File(parentFolder);
		if (!parentFile.exists()) {
			parentFile.mkdirs();
		}

		Enumeration<? extends ZipEntry> entries = zf.entries();

		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (!entry.isDirectory()) {
				String fileName = parentFolder + "/" + entry.getName();
				File entryF = new File(fileName);
				if (!entryF.exists() || entryF.lastModified() < inF.lastModified()) {
					InputStream is = zf.getInputStream(entry);
					FileUtil.saveISToFile(is, fileName);
					is.close();
				}
			}
		}
		zf.close();
	}
	static NumberFormat nf = NumberFormat.getInstance();
	public static void fastFileCopy(File source, File target) {
        FileChannel in = null;
        FileChannel out = null;
		long start =  System.currentTimeMillis();
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
            fis = new FileInputStream(source);
			in = fis.getChannel();
            fos = new FileOutputStream(target);
			out = fos.getChannel();

            long size = in.size();
            long transferred = in.transferTo(0, size, out);

            while(transferred != size){
                transferred += in.transferTo(transferred, size - transferred, out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(fis);
            close(fos);
            close(in);
            close(out);
        }
		long end = System.currentTimeMillis();
		
		Log.d(target.getAbsolutePath(), 
			  nf.format(source.length()) + "B: " + 
			  ((end - start > 0) ? nf.format(source.length() / (end - start)) : 0) + " KB/s");
    }

	public static void fastBufferFileCopy(File source, File target) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
		long start =  System.currentTimeMillis();
		FileInputStream fis = null;
		FileOutputStream fos = null;
        long size = source.length();
		try {
            fis = new FileInputStream(source);
			bis = new BufferedInputStream(fis);
            fos = new FileOutputStream(target);
			bos = new BufferedOutputStream(fos);

			byte[] barr = new byte[Math.min((int)size, 1 << 20)];
            int read = 0;
            while((read = bis.read(barr)) != -1){
                bos.write(barr, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(fis);
            close(fos);
            close(bis);
            close(bos);
        }
		long end = System.currentTimeMillis();
		
		String srcPath = source.getAbsolutePath();
		Log.d("Copied " + srcPath + " to " + target, 
			  ", took " + (end - start)/1000 + " ms, total size " + nf.format(size) + " Bytes, speed " + 
			  ((end - start > 0) ? nf.format(size / (end - start)) : 0) + "KB/s");
    }

	public static void close(Closeable closable) {
		if (closable != null) {
			try {
				closable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static List<File> getFiles(String fs) {
		return FileUtil.getFiles(new File(fs));
	}

	public static List<File> getFiles(File f) {
		final Set<File> lf = new HashSet<File>();
		if (f.isDirectory()) {
			final Stack<File> stk = new Stack<>();
			stk.push(f);
			File[] fs = null;
			while (stk.size() > 0) {
				f = stk.pop();
				fs = f.listFiles();
				for (File ff : fs) {
					if (ff.isDirectory()) {
						stk.push(ff);
					} else {
						lf.add(ff);
					}
				}
			}
		} else {
			lf.add(f);
		}
		ArrayList<File> al = new ArrayList<File>(lf.size());
		al.addAll(lf);
		return al;
	}

	public static void copySaveLastModified(String[] fs, String destDir, boolean includeSrcDir) {
		for (String f : fs) {
			FileUtil.copySaveLastModified(f, destDir, includeSrcDir);
		}
	}

	public static void copySaveLastModified(File[] fs, File destDir, boolean includeSrcDir) {
		for (File f : fs) {
			copySaveLastModified(f, destDir, includeSrcDir);
		}
	}

	public static void copySaveLastModified(String srcDir, String destDir, boolean includeSrcDir) {
		copySaveLastModified(new File(srcDir), new File(destDir), includeSrcDir);
	}

	public static void copySaveLastModified(File srcDir, File destDir, boolean includeSrcDir) {
		long size = 0;
		long start = System.currentTimeMillis();
		if (srcDir.isDirectory()) {
			final List<File> lf = getFiles(srcDir);
			int len = 0;
			if (includeSrcDir) {
				len = srcDir.getParentFile().getAbsolutePath().length();
			} else {
				len = srcDir.getAbsolutePath().length();
			}
			String destPath = destDir.getAbsolutePath();
			int counter = 0;

			for (File f : lf) {
				String sub = f.getAbsolutePath().substring(len);//.replace('\\', '/');
				File destF = new File(destPath + "/" + sub);
				if (!destF.exists() || f.length() != destF.length() || f.lastModified() != destF.lastModified()) {
					File parentFile = destF.getParentFile();
					if (!parentFile.exists()) {
						parentFile.mkdirs();
					}
					size += f.length();
					System.out.print(++counter + "/" + lf.size() + " copying: " + f + ": ");
					fastFileCopy(f, destF);
//					fastBufferFileCopy(f, destF);
					destF.setLastModified(f.lastModified());
				}
			}

		} else {
			if (!destDir.exists() || srcDir.length() != destDir.length() || srcDir.lastModified() != destDir.lastModified()) {
				destDir.getParentFile().mkdirs();
				size += srcDir.length();
//				fastFileCopy(srcDir, destDir);
				fastBufferFileCopy(srcDir, destDir);
				destDir.setLastModified(srcDir.lastModified());
			}
		}
		long end = System.currentTimeMillis();
		NumberFormat nf = NumberFormat.getInstance();
		Log.d("Copied " + srcDir.getAbsolutePath() + " to " + destDir, 
			  ", took " + (end - start)/1000 + " s, total size " + nf.format(size) + " Bytes, speed " + 
			  ((end - start > 0) ? nf.format(size / (end - start)) : 0) + "KB/s");
	}

	public static void main ( String[] args) throws IOException {
		//FileUtil.compressAFileOrFolderRecursiveToZip("/data/data/com.aide.ui", "/storage/emulated/0/aide-ndk.zip");
//		File file = new File("F:/finished");
//		System.out.println(Util.listToString(getFiles(file), true, "\r\n"));
//		List<File> files = getFiles(new  String[] {"F:/finished"});
//		Log.d("list", Util.listToString(files, true, "\r\n"));
		String[] srcDir = {
//				"F:/data", 
//				"F:/DCIM", 
//				"F:/Download", 
//				"F:/KINH TUNG PALI & VIET", 
//				"F:/Pali", 
//				"F:/Recordings", 
//				"F:/Sach", 
//				"F:/Searcher", 
//				"F:/Text", 
//				"F:/Trình pháp", 
//				"F:/txt", 
//				"F:/Y Hoc", 
//				"F:/zip", 
//				"C:/Downloads/temp/",
			"C:/Program Files (x86)/Java/jdk1.7.0_72/src.zip"
		};
		copySaveLastModified(srcDir, "F:/temp/", false);
		System.out.println("finished " + srcDir);
	}
}
class FileFirstSorting implements Comparator<File>{

	@Override
	public int compare(File f1, File f2) {
		if (f1.isFile() && f2.isFile() || f1.isDirectory() && f2.isDirectory()) {
			return f1.getAbsolutePath().compareTo(f2.getAbsolutePath());
		} else if (f1.isFile() && f2.isDirectory()) {
			return -1;
		} else {
			return 1;
		}
	}
}

class FolderFirstSorting implements Comparator<File> {

	@Override
	public int compare(File f1, File f2) {
		if (f1.isFile() && f2.isFile() || f1.isDirectory() && f2.isDirectory()) {
			return f1.getAbsolutePath().toLowerCase().compareTo(f2.getAbsolutePath().toLowerCase());
		} else if (f1.isFile() && f2.isDirectory()) {
			return 1;
		} else {
			return -1;
		}
	}
}

class TimeComparator implements Comparator<File> {

	@Override
	public int compare(File o1, File o2) {
		return (int) (o1.lastModified() - o2.lastModified());
	}

}
