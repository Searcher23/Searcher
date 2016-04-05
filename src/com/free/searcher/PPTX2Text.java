package com.free.searcher;
import java.io.*;
import java.util.regex.*;
import java.util.zip.*;
import org.apache.http.util.*;
import android.util.*;

public class PPTX2Text {
	private static Pattern CR_TAGS = Pattern.compile(
		"</?(a:p [^>]*?|a:p)>",
		Pattern.UNICODE_CASE);

	private static Pattern REMOVE_TAGS = Pattern.compile(
		"</?(a:|a14:|p:|p14:|mc:|c:|v:|r:|\\?xml)[^>]*?>",
		Pattern.UNICODE_CASE);
		
	private static Pattern REMOVE_CONTENT = Pattern.compile(
		"<p:attrName[^>]*?>.*?</p:attrName>",
		Pattern.UNICODE_CASE);

	public static String pptx2Text(File inFile) {
		long millis = System.currentTimeMillis();
		StringBuilder document = new StringBuilder(pptxToString(inFile, "ppt/slides/slide", ".xml"));
		Log.d("pptx2Text: ", inFile.getAbsolutePath() + " char num: "
			+ document.length() + " used: "
			  + (System.currentTimeMillis() - millis));
		return document.toString();
	}
	
	public static String pptx2Text(File inFile,  String outDir) throws IOException {
		String toString = pptx2Text(inFile);
		FileUtil.writeFileAsCharset(new File(outDir + "/" + inFile.getName() + ".txt"), toString, "utf-8");
		return toString;
	}

	private static String pptxToString(File inFile, String prefix, String suffix) {
		Log.d("pptxToString, v ", inFile + ", " + prefix);
		try {
			String wholeFile = FileUtil.readZipEntryContent(inFile.getAbsolutePath(), prefix, suffix);
			wholeFile = removeTags(wholeFile);
			wholeFile = Util.replaceAll(wholeFile, Util.ENTITY_NAME, Util.ENTITY_CODE);
			wholeFile = Util.fixCharCode(wholeFile);
			return wholeFile;
		} catch (IOException e) {
			Log.e("pptxToString", e.getMessage(), e);
		}
		return "";
	}

	private static String removeTags(String wholeFile) {

		long millis = System.currentTimeMillis();
		wholeFile = CR_TAGS.matcher(wholeFile).replaceAll("\n");
		wholeFile = REMOVE_CONTENT.matcher(wholeFile).replaceAll("");
		wholeFile = REMOVE_TAGS.matcher(wholeFile).replaceAll("");
		Log.d("Time for converting: ", (System.currentTimeMillis() - millis) + "");
		return wholeFile;
	}

	public static void main(String[] args) throws Exception {
		
	}
}
