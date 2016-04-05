package com.free.searcher;

import java.io.*;
import java.util.regex.*;
import java.util.zip.*;
import org.apache.http.util.*;
import android.util.*;

public class ODPToText {
	private static Pattern CR_TAGS = Pattern.compile(
		"</?(text:p [^>]*?>|text:p>|text:h [^>]*?>|text:h>)",
		Pattern.UNICODE_CASE);

	private static Pattern REMOVE_TAGS = Pattern.compile(
		"</?(office:|config:|style:|text:|table:|draw:|anim:|presentation:|\\?xml)[^>]*?>",
		Pattern.UNICODE_CASE);

	public static String odpToText(File inFile) {
		String wholeFile = odpToString(inFile.getAbsolutePath(), "content.xml");
		return wholeFile;
	}

	public static String odpToText(File inFile,  String outDir) throws IOException {
		String wholeFile = odpToString(inFile.getAbsolutePath(), "content.xml");
		FileUtil.writeFileAsCharset(new File(outDir + "/" + inFile.getName() + ".txt"), wholeFile, "utf-8");
		return wholeFile;
	}

	private static String odpToString(String inFile, String entryName) {
		Log.d("odpToString, entryName ", inFile + ", " + entryName);
		long millis = System.currentTimeMillis();
		try {
			String wholeFile = FileUtil.readZipEntryContent(inFile, entryName);
			wholeFile = removeTags(wholeFile);
			wholeFile = Util.replaceAll(wholeFile, Util.ENTITY_NAME, Util.ENTITY_CODE);
			wholeFile = Util.fixCharCode(wholeFile);
			Log.d("odpToString", inFile + " char num: "
				  + wholeFile.length() + " used: "
				  + (System.currentTimeMillis() - millis));
			return wholeFile;
		} catch (IOException e) {
			Log.e("odpToString", e.getMessage(), e);
		}
		return "";
	}

	private static String removeTags(String wholeFile) {

		long millis = System.currentTimeMillis();

		wholeFile = CR_TAGS.matcher(wholeFile).replaceAll("\n");
		
		wholeFile = REMOVE_TAGS.matcher(wholeFile).replaceAll("");

		Log.d("Time for converting: ", (System.currentTimeMillis() - millis) + "");
		return wholeFile;
	}

	public static void main(String[] args) throws Exception {
	}
}
