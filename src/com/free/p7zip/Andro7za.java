package com.free.p7zip;

import java.io.File;

import android.util.Log;
import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * <code>Andro7za</code> provided the 7za JNI interface.
 * 
 */
public final class Andro7za {
	private static String JNI_TAG = "7zaJNI";

//	7z <command> [<switch>...] <base_archive_name> [<arguments>...]
//	<arguments> ::= <switch> | <wildcard> | <filename> | <list_file>
//	<switch>::= <switch_symbol><switch_characters>[<option>]
//	<switch_symbol> ::= '/' | '-' 
//	<list_file> ::= @{filename}
	/**
	 * command a x l
	 * archive 7z zip
	 * -t7z -tzip |  | 
	 * pPassword | -pPassword | -pPassword
	 * compression level | outputDir  |
	 * pathToCompress/fList | fileToExtract/fList |  
	 * 
	 * In compress use type. In extract use overwrite mode
	 *   -aoa	Overwrite All existing files without prompt.
	 *   -aos	Skip extracting of existing files.
	 *   -aou	aUto rename extracting file (for example, name.txt will be renamed to name_1.txt).
	 *   -aot	auto rename existing file (for example, name.txt will be renamed to name_1.txt).
	 *
	 */
	public native int a7zaCommand(String command7z, 
	String archive7z, 
	String type, 
	String password, 
	String compressionLevelOrOutputDir, 
	String fList4CompressOrExtract);
	
	public native String stringFromJNI(String outfile, String infile);
	public native void closeStreamJNI();
	
	private static final String mOutfile = "/sdcard/.com.free.searcher" + "/7zaOut.txt";
	private static final String mInfile = "/sdcard/.com.free.searcher" + "/7zaIn.txt";
	private static final String listFile = "/sdcard/.com.free.searcher" + "/7zaFileList.txt";
	
	public void initStream() throws IOException {
		resetFile(mOutfile);
		resetFile(mInfile);
		resetFile(listFile);
		stringFromJNI(mOutfile, mInfile);
	}

	private void resetFile(String f) throws IOException {
		File file = new File(f);
		file.delete();
		// file.getParentFile().mkdirs();
		file.createNewFile();
	}
	
	final private static StringBuilder sb = new StringBuilder();
	public String read() throws IOException {
		synchronized (sb) {
			sb.setLength(0);
			BufferedReader in = null;
			try {
				in = new BufferedReader(new FileReader(mOutfile));
				while(in.ready()) {
					String readLine = in.readLine();
					if (readLine == null || "".equals(readLine)) {
						in.close();
						closeStreamJNI();
						break;
					} else {
						sb.append(readLine);
					}
				}
				return sb.toString();
			} finally {
				if (in != null)
					in.close();
			}
		}
	}
	
	public void write(String content) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mInfile)));
			out.write(content.toCharArray(), 0, content.toCharArray().length);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeContentToFile(String fileName, String contents)
	throws IOException {
		Log.d("writeContentToFile", fileName);
		File f = new File(fileName);
		f.getParentFile().mkdirs();
		File tempFile = new File(fileName + ".tmp");
		FileWriter fw = new FileWriter(tempFile);
		BufferedWriter bw = new BufferedWriter(fw);
//		int length = contents.length();
		try {
			if (contents != null) {
				bw.write(contents); //, i * apart, apart);
			}
		} finally {
			bw.flush();
			fw.flush();
			bw.close();
			fw.close();
			f.delete();
			tempFile.renameTo(f);
		}
	}

	public static String collectionToString(Collection<?> list, boolean number, String sep) {
		StringBuilder sb = new StringBuilder();
		if (!number) {
			for (Object obj : list) {
				sb.append(obj).append(sep);
			}
		} else {
			int counter = 0;
			for (Object obj : list) {
				sb.append(++counter + ": ").append(obj).append(sep);
			}
		}
		return sb.toString();
	}

	public int compress(File archive7z, String type, String password, String compressLevel, File dirToCompress) {
		return compress(archive7z.getAbsolutePath(), type, password, compressLevel, dirToCompress.getAbsolutePath());
	}

	public int compress(String pathArchive7z, String type, String password, String compressLevel, String pathToCompress) {
		if (type == null) {
			type = "";
		}
		if (password == null) {
			password = "";
		}
		if (compressLevel == null) {
			compressLevel = "";
		}
		try {
			initStream();
			Log.d(JNI_TAG, "Call a7zaCommand(), compress: a " + pathArchive7z + " " + type + " " + password + " " + compressLevel + " " + pathToCompress);
			int ret = a7zaCommand("a", pathArchive7z, type, password, compressLevel, pathToCompress);
			Log.d(JNI_TAG, "a7zaCommand() compress ret " + ret + ", file: " + pathArchive7z);
			return ret;
		} catch (IOException e) {
			return 2;
		} finally {
			closeStreamJNI();
		}
	}

	public int compress(File archive7z, String type, String password, String compressLevel, Collection<String> fileList) throws IOException {
		return compress(archive7z.getAbsolutePath(), type, password, compressLevel, fileList);
	}

	public int compress(String pathArchive7z, String type, String password, String compressLevel, Collection<String> fileList) throws IOException {
		if (type == null) {
			type = "";
		}
		if (password == null) {
			password = "";
		}
		if (compressLevel == null) {
			compressLevel = "";
		}
		if (fileList.size() > 0) {
			String outputList = collectionToString(fileList, false, "\n");
			File outListF = new File(listFile);
			outListF.delete();
			writeContentToFile(listFile, outputList);
			
			Log.d(JNI_TAG, "Call a7zaCommand(), compress: \"" + fileList + " to " + pathArchive7z + "\" level " + compressLevel);
			int ret = a7zaCommand("x",pathArchive7z, type, password, compressLevel, "@" + listFile);
			Log.d(JNI_TAG, "a7zaCommand() compress ret " + ret + ", file: " + pathArchive7z);
			return ret;
		} else {
			return -1;
		}
	}
	
	public int extract(File archive7z, String overwriteMode, String password, File dirToExtract) {
		return extract(archive7z.getAbsolutePath(), overwriteMode, password, dirToExtract.getAbsolutePath());
	}
	
	public int extract(String pathArchive7z, String overwriteMode, String password, String pathToExtract) {
		if (overwriteMode == null) {
			overwriteMode = "-aos";
		}
		
		if (password == null) {
			password = "";
		}
		pathToExtract = "-o" + pathToExtract;
		Log.d(JNI_TAG, "Call a7zaCommand(), extracting: \"" + pathArchive7z + "\" to " + pathToExtract);
		int ret = a7zaCommand("x", pathArchive7z, overwriteMode, password, pathToExtract, "");
		Log.d(JNI_TAG, "a7zaCommand() extracting ret " + ret + ", file: " + pathArchive7z);
		return ret;
	}

	public int extract(File archive7z, String overwriteMode, String password, File dirToExtract, Collection<String> fileList) throws IOException {
		return extract(archive7z.getAbsolutePath(), overwriteMode, password, dirToExtract.getAbsolutePath(), fileList);
	}
	
	public int extract(String pathArchive7z, String overwriteMode, String password, String pathToExtract, Collection<String> fileList) throws IOException {
		if (fileList.size() > 0) {
			if (overwriteMode == null) {
				overwriteMode = "-aos";
			}
			
			if (password == null) {
				password = "";
			}
			File f = new File(pathToExtract);
			if (!f.exists()) {
				f.mkdirs();
			}
			String outputList = collectionToString(fileList, false, "\n");
			File outListF = new File(listFile);
			outListF.delete();
			writeContentToFile(listFile, outputList);
			pathToExtract = "-o" + pathToExtract;
			Log.d(JNI_TAG, "Call a7zaCommand(), extract: \"" + fileList + " in " + pathArchive7z + "\" to " + pathToExtract);
			int ret = a7zaCommand("x",pathArchive7z, overwriteMode, password, pathToExtract, "@" + listFile);
			Log.d(JNI_TAG, "a7zaCommand() extract ret " + ret + ", file: " + pathArchive7z);
			return ret;
		} else {
			return -1;
		}
	}
	
	public int extract(File archive7z, String overwriteMode, String password, File dirToExtract, String fileName) {
		return extract(archive7z.getAbsolutePath(), overwriteMode, password, dirToExtract.getAbsolutePath(), fileName);
	}
	
	public int extract(String pathArchive7z, String overwriteMode, String password, String pathToExtract, String fileName) {
		if (overwriteMode == null) {
			overwriteMode = "-aos";
		}
		if (password == null) {
			password = "";
		}
		File f = new File(pathToExtract);
		if (!f.exists()) {
			f.mkdirs();
		}
		pathToExtract = "-o" + pathToExtract;
		Log.d(JNI_TAG, "Call a7zaCommand(), extract: \"" + fileName + " in " + pathArchive7z + "\" to " + pathToExtract);
		int ret = a7zaCommand("x",pathArchive7z, overwriteMode, password, pathToExtract, fileName);
		Log.d(JNI_TAG, "a7zaCommand() extract ret " + ret + ", file: " + pathArchive7z);
		return ret;
	}
	
	// ------------------- ----- ------------ ------------  ------------------------
	// 2016-03-13 22:49:48 ....A         4212               jni/CPP/myWindows/makefile.depend
	// 2016-03-13 23:44:49 D....            0            0  gen/com/hostzi
	// 2015-08-02 17:49:11 .R..A         9405               CPP/Windows/Window.h
	//                     .....                            p7zip_15.14_src_all.tar
	private final static Pattern ENTRY_PATTERN = Pattern.compile("[ \\d]{4}[-/ ][ \\d]{2}[- /][ \\d]{2} [ \\d]{2}[ :][ \\d]{2}[ :][ \\d]{2} ([D\\.]).{3}[A\\.] [ \\d]{12}[ \\d]{15}([^\r\n]+)", Pattern.UNICODE_CASE);
	
	public Collection<String> listing(File archive7z, String password) throws IOException {
		return listing(archive7z.getAbsolutePath(), password);
	}
	
	public Collection<String> listing(String pathArchive7z, String password) throws IOException {
		try {
			initStream();
			if (password == null) {
				password = "";
			}
			Log.d(JNI_TAG, "Call a7zaCommand(), listing: " + pathArchive7z);
			int ret = a7zaCommand("l", pathArchive7z, "", password, "", "");
			Log.d(JNI_TAG, "a7zaCommand() listing ret " + ret + ", file: " + pathArchive7z);

			Collection<String> nameList = new HashSet<>();
			String line ="";
			FileReader fileReader = new FileReader(mOutfile);
			BufferedReader br = new BufferedReader(fileReader, 32768);
			int count = 0;
			while (br.ready() && count < 2) {
				line = br.readLine();
				//System.out.println(line);
				if ("------------------- ----- ------------ ------------  ------------------------".equals(line)) {
					count++;
				}
				if (count == 1) {
					Matcher matcher = ENTRY_PATTERN.matcher(line);
//				System.out.println(line);
//				System.out.println(ENTRY_PATTERN);
					if (matcher.matches()) {
						if ("D".equals(matcher.group(1))) {
							nameList.add(matcher.group(2) + "/");
						} else {
							nameList.add(matcher.group(2));
						}
					}
				}
			}
			br.close();
			fileReader.close();
			//Collections.sort(nameList);
//			Log.i("nameList", collectionToString(nameList, true, "\n"));
			return nameList;
		} finally {
			closeStreamJNI();
		}
	}
	
	static {
		// Dynamically load stl_port, see jni/Application.mk
		// System.loadLibrary("stlport_shared");
		System.loadLibrary("7za");
	}
}
