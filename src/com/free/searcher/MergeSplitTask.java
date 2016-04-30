package com.free.searcher;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.nio.*;
import java.text.*;
import android.util.*;
import android.app.*;
import android.os.*;
import android.widget.*;

public class MergeSplitTask  extends AsyncTask<Void, Void, String> {

	private Activity activity = null;
	private Collection<String> filePaths;
	private static NumberFormat nf = NumberFormat.getInstance();
	private static final String SPLIT_PAT01 = ".+\\.001";
	private static final String SPLIT_PAT = ".+\\.\\d{3}";
	private static final Pattern SPLIT_PATTERN01 = Pattern.compile(SPLIT_PAT01);
	private static final Pattern SPLIT_PATTERN = Pattern.compile(SPLIT_PAT);
	private static final int defaultArrayLength = 32768;
	private int parts;
	private long size;
	private String saveTo;
	private static final String TAG = "MergeSplitTask";
	
	public MergeSplitTask(Activity s, Collection<String> filePaths, String saveTo, int parts, long size) {
		this.filePaths = filePaths;
		this.parts = parts;
		this.size = size;
		this.activity = s;
		this.saveTo = saveTo;
	}
	
	protected String doInBackground(Void... s) {
		try {
			if (size == 0) {
				mergeSplit(filePaths, parts, 0, saveTo);
			} else {
				mergeSplit(filePaths, 0, size, saveTo);
			}
			return "Spliting and Merging are finished";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Spliting and Merging are unsuccessful";
	}
	
	@Override
	protected void onPostExecute(String result) {
		Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
		Log.d(TAG, result);
	}
	
	public void mergeSplit(Collection<String> filePaths, long parts, long sizeOfPart, String savePath) throws IOException {
		nf.setMinimumIntegerDigits(3);
		for (String st : filePaths) {
			if (!isCancelled()) {
				if (SPLIT_PATTERN01.matcher(st).matches()) {
					merge(st, savePath);
				} else if (!SPLIT_PATTERN.matcher(st).matches()) {
					split(st, parts, sizeOfPart, savePath);
				}
			}
		}
	}

	private void split(String fPath, long parts, long sizeOfPart, String savePath) throws IOException {
		
		Log.d(TAG, fPath + ", " + parts + ", " + sizeOfPart + ", " + saveTo);
		RandomAccessFile f = new RandomAccessFile(new File(fPath), "r");
		long len = f.length();
		if (len < parts) {
			parts = len;
		}
		if (len < sizeOfPart) {
			sizeOfPart = len;
		}
		if (parts == 0 && sizeOfPart == 0) {
			parts = 1;
		}
		if (parts == 1) {
			return;
		}
		if (parts > 0) {
			sizeOfPart = len / parts;
		} else {
			parts = ((len % sizeOfPart) == 0) ? len / sizeOfPart : len / sizeOfPart + 1;
		}
		byte[] bArr = new byte[(int)Math.min(defaultArrayLength, sizeOfPart)];
		Log.d(TAG, len + ", " + parts + ", " + sizeOfPart + ", " + bArr.length);
		
		String newPath = savePath + fPath;
		new File(newPath).getParentFile().mkdirs();
		
		if (sizeOfPart <= defaultArrayLength) {
			for (long i = 1; i < parts; i++) {
				writeClose(f, bArr, newPath, i);
			}
//			bArr = new byte[(int)(len - (parts - 1) * sizeOfPart)];
			writeClose(f, new byte[(int)(len - (parts - 1) * sizeOfPart)], newPath, parts);
		} else {
			for (long i = 1; i <= parts; i++) {
				String format = newPath + "." + nf.format(i);
				File file = new File(format);
				file.delete();
				file.createNewFile();
				OutputStream os = new FileOutputStream(format, true);
				BufferedOutputStream bos = new BufferedOutputStream(os);
				
				for (long j = 0; j < (sizeOfPart / defaultArrayLength) - 1; j++) {
					writeNotClose(f, bArr, bos);
				}
				if (i < parts) {
//					bArr = new byte[(int)(sizeOfPart - (sizeOfPart / S32768b - 1) * bArr.length)];
					writeNotClose(f, new byte[(int)(sizeOfPart - (sizeOfPart / defaultArrayLength - 1) * bArr.length)], bos);
				} else {
//					bArr = new byte[(int)(len - (parts-1)*sizeOfPart - (sizeOfPart/S32768b - 1)*bArr.length)];
					writeNotClose(f, new byte[(int)(len - (parts-1)*sizeOfPart - (sizeOfPart/defaultArrayLength - 1)*bArr.length)], bos);
				}
				bos.close();
			}
		}
		//AndroidHttp
	}

	private void writeClose(RandomAccessFile f, byte[] bArr, String st2, long parts) throws IOException {
		OutputStream os = new FileOutputStream(st2 + "." + nf.format(parts));
		BufferedOutputStream bos = new BufferedOutputStream(os);
		writeNotClose(f, bArr, bos);
		bos.close();
	}

	private void writeNotClose(RandomAccessFile f, byte[] bArr, BufferedOutputStream bos) throws IOException {
		f.readFully(bArr);
		bos.write(bArr);
		bos.flush();
	}
	
	private void merge(String fPath, String savePath) throws IOException {
		Log.d(TAG, fPath);
		String realName = fPath.substring(0, fPath.lastIndexOf("."));

		Log.d(TAG, realName);
		String pat = realName.replaceAll(Util.SPECIAL_CHAR_PATTERNSTR, "\\\\$1") + "\\.\\d{3}";
		Pattern M_PATTERN = Pattern.compile(pat);
		File fTmp = new File(savePath + realName + ".tmp");
		fTmp.getParentFile().mkdirs();
		Log.d(TAG, fTmp.getAbsolutePath());
		fTmp.delete();
		fTmp.createNewFile();
		FileOutputStream fos = new FileOutputStream(fTmp, true);
		BufferedOutputStream bos = new BufferedOutputStream(fos);

		// copy s to bos
		File[] list = new File(fPath).getParentFile().listFiles();
		Arrays.sort(list);
		for (File sst : list) {
//			System.out.println(sst);
			if (M_PATTERN.matcher(sst.getAbsolutePath()).matches()) {
//				System.err.println(sst);
				saveISToFile(new FileInputStream(sst), bos);
			}
		}
		bos.close();
		fos.close();
		File file = new File(savePath + realName);
		file.delete();
		fTmp.renameTo(file);
	}
	
	public static void saveISToFile(InputStream is, BufferedOutputStream bos)
	throws IOException {
		BufferedInputStream bis = new BufferedInputStream(is);
		byte[] barr = new byte[defaultArrayLength];
		int read = 0;
		while ((read = bis.read(barr)) > 0) {
			bos.write(barr, 0, read);
		}
		bis.close();
		is.close();
		bos.flush();
	}
}
