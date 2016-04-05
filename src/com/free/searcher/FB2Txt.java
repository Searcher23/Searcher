package com.free.searcher;

import android.util.*;
import java.io.*;
import java.util.zip.*;

public class FB2Txt {

	public static String fb2txt(File inFile) throws IOException {
		long millis = System.currentTimeMillis();
		Log.d("fb2txt", inFile.getAbsolutePath());
		String wholeFile = FileUtil.readFileAsCharset(inFile.getAbsolutePath(), "utf-8");
		int fileLength = wholeFile.length();

		wholeFile = Util.removeTags(wholeFile);
		wholeFile = Util.fixCharCode(wholeFile);
		wholeFile = Util.replaceAll(wholeFile, Util.ENTITY_NAME,
									Util.ENTITY_CODE);

		Log.d("fb2txt", inFile.getAbsolutePath() + " char num: "
			  + fileLength + " used: "
			  + (System.currentTimeMillis() - millis));

		return wholeFile;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(fb2txt(new  File("/storage/emulated/0/MiniHelp.en.fb2")));
	}
}
