package com.free.searcher;

import java.io.*;
import java.util.*;
import org.apache.http.util.*;
import java.text.*;
import java.util.regex.*;
import android.util.*;

public class StardictReader {

	List<Entry<String, int[]>> llIndex = new LinkedList<Entry<String, int[]>>();
	String ifo;
	String idx;
	String dict;
	byte[] dictArr;

	public static void main(String[] st) throws IOException{

		StardictReader lr = createStardictData("");
		List<String> def = lr.readDef("");
		System.out.println(def);
		def = lr.readDef("");
		System.out.println(def);
//
//		lr = instance("",
//					  "");
//		def = lr.readDef("");
//		System.out.println(def);
//		def = lr.readDef("");
//		System.out.println(def);
//
////		StardictReader lr2 = createStardictData("");
////		def = lr2.readDef("hello");
//		System.out.println(def);
//
		System.out.println(1024 << 8);
		System.out.println(-127 << 16);
		System.out.println(127 << 8);
		System.out.println(129 << 8);
		dic2Text("");
	}

	private StardictReader(String idx, String dict) {
		this.idx = idx;
		this.dict = dict;
	}

	private StardictReader(String idx, String dict,  String ifo) {
		this.idx = idx;
		this.dict = dict;
		this.ifo = ifo;
	}

	public static StardictReader createStardictData(String fName) throws IOException {

		long start = System.currentTimeMillis();
		Log.d("StardictReader", fName);
		File f = new File(fName);
		String idx = fName + ".idx";
		String dict = fName + ".dict";
		String ifo = fName + ".ifo";
		StardictReader lr = new StardictReader(idx, dict, ifo);

		InputStream is = null; //new FileInputStream(f);
		InputStreamReader isr = null; //new InputStreamReader(is, "utf-8");
		BufferedReader br = null; //new BufferedReader(isr);

		if (f.length() < Runtime.getRuntime().maxMemory() * 3 / 10) {
			byte[] wholeArr = FileUtil.readFileToMemory(fName);
			Log.d("StardictReader", "readFileToMemory " + wholeArr.length + "/" 
				  + (Runtime.getRuntime().maxMemory() * 3 / 10));
			is = new ByteArrayInputStream(wholeArr);
		} else {
			is = new FileInputStream(f);
			Log.d("StardictReader", "FileInputStream " + f.length());
		}
		isr = new InputStreamReader(is, "utf-8");
		br = new BufferedReader(isr);

		int indexLength = 0;
		String def;
		int indexOfSlashT;
		String name;
		String l = "";
		StringBuilder sbDef = new StringBuilder((int)f.length() * 9 / 10);
		int index = 0;
		int defLen = 0;
		ByteArrayBuffer babIndex = null; //new ByteArrayBuffer(255);
		int count = 0;
		byte[] nameBytes;
		int nameBytesLen;
		List<Entry<String, ByteArrayBuffer>> idxList = new ArrayList<Entry<String, ByteArrayBuffer>>(500000);
		while ((l = br.readLine()) != null) {
			count++;
			if (count % 1000 == 0) {
				System.out.println(count);
			}

			try {
				indexOfSlashT = l.indexOf("\t");
//				if (indexOfSlashT < 0) {
//				} else {
				name = l.substring(0, indexOfSlashT).trim();
//				if (name.indexOf(" ") > 0) {
//					name = "I" + name + "I";
//				}
				def = l.substring(indexOfSlashT + 1, l.length())
					.replaceAll("\\\\n", "\n")
					.replaceAll("\\\\", "\\").trim();
				sbDef.append(def);

				nameBytes = name.getBytes("utf-8");
				nameBytesLen = nameBytes.length;

				babIndex = new ByteArrayBuffer(nameBytesLen + 9);
				babIndex.append(nameBytes, 0, nameBytesLen);

				babIndex.append(0);

				appendInt(babIndex, index);

				defLen = def.getBytes("utf-8").length;
				appendInt(babIndex, defLen);

				lr.llIndex.add(new Entry<String, int[]>(name, new int[]{index , defLen}));
				idxList.add(new Entry<String, ByteArrayBuffer>(name, babIndex));
				indexLength += babIndex.length();

				index += defLen;
				//System.out.println(name);
				//}
			} catch (Throwable t) {
				System.err.println(l + ", " + l.length() + ", " + l.indexOf("\t"));
				t.printStackTrace();
			}

		}
		br.close();
		isr.close();
		is.close();

		Collections.sort(idxList);
		Collections.sort(lr.llIndex);
		babIndex = new ByteArrayBuffer(indexLength);
		for (Entry en : idxList) {
			byte[] toByteArray = ((ByteArrayBuffer)en.getValue()).toByteArray();
			babIndex.append(toByteArray, 0, toByteArray.length);
		}

		FileUtil.saveBArrToFile(idx, babIndex.toByteArray());
		FileUtil.writeContentToFile(dict, sbDef.toString());

		sbDef = new StringBuilder("StarDict's dict ifo file")
			.append("\nversion=2.4.2")
			.append("\nwordcount=" + count)
			.append("\nidxfilesize=" + babIndex.length())
			.append("\nbookname=" + f.getName())
			//.append("\nauthor=sāmaṇera Định Phúc")
			.append("\ndescription=convert to StarDict by Stardict converter")
			.append("\ndate=" + DateFormat.getDateTimeInstance().format(System.currentTimeMillis()))
			.append("\nsametypesequence=m\n");
		FileUtil.writeContentToFile(ifo, sbDef.toString());

		sbDef = new StringBuilder("StarDict's clt file")
			.append("\nversion=2.4.8")
			.append("\nurl=")
			.append(idx)
			.append("\nfunc=0\n");
		FileUtil.writeContentToFile(fName + ".idx.clt", sbDef.toString());

		Log.d("Convert time", "" + (System.currentTimeMillis() - start));
		return lr;
	}

	public static StardictReader instance(String idx, String dict) {
		StardictReader lr = new StardictReader(idx, dict);
		return lr;
	}

	private static void appendInt(ByteArrayBuffer babIndex, int off) {
		babIndex.append(off >> 24);
		babIndex.append(off >> 16);
		babIndex.append(off >> 8);
		babIndex.append(off & 255);
	}

	public List<String> readDef(String name) throws IOException {
		if (llIndex.size() == 0) {
			readDef();
		}
		List<String> lret = null;
		int idxx = Collections.binarySearch(llIndex, new Entry<String, int[]>(name, null));
		if (idxx >= 0) {
			lret = new  LinkedList<String>();
			for (int idx = Math.max(idxx - 3, 0); idx < Math.min(idxx + 3, llIndex.size()); idx++) {
				Entry<String, int[]> en = llIndex.get(idx);
				if (en.getKey().equals(name)) {
					int[] value = en.getValue();
					
					System.out.println(en);
					RandomAccessFile raf = new RandomAccessFile(dict, "r");
					if (dictArr == null && raf.length() < Runtime.getRuntime().maxMemory() * 3 / 10) {
						Log.d("readDef", "read memory");
						dictArr = FileUtil.readFileToMemory(dict);
						raf.close();
						lret.add(new String(dictArr, 
											value[0], 
											value[1],
											"utf-8"));
					} else if (dictArr != null) {
						lret.add(new String(dictArr, 
											value[0], 
											value[1],
											"utf-8"));
					} else {
						Log.d("readDef", "read file");
						byte[] barr = new byte[value[1]];
						raf.seek(value[0]);
						raf.readFully(barr, 0, value[1]);
						raf.close();
						lret.add(new String(barr, 
											0,
											value[1],
											"utf-8"));
					}
				}
			}
		}
		return lret;
	}

	private void readDef() throws UnsupportedEncodingException, FileNotFoundException, IOException {
		byte[] barr = FileUtil.readFileToMemory(idx);
		int zeroIndex = 0;

		for (int i = 0; i < barr.length; i++) {
			if (barr[i] == 0) {
				String name = new String(barr, zeroIndex, i - zeroIndex, "utf-8");
				zeroIndex = i + 9;
				int start = (sign2Unsign(barr[++i]) << 24) + (sign2Unsign(barr[++i]) << 16) + (sign2Unsign(barr[++i]) << 8) + sign2Unsign(barr[++i]);
				int end = (sign2Unsign(barr[++i]) << 24) + (sign2Unsign(barr[++i]) << 16) + (sign2Unsign(barr[++i]) << 8) + sign2Unsign(barr[++i]);
				i = zeroIndex;
				Entry<String, int[]> entry = new Entry<String, int[]>(name, new int[] {start, end});
				llIndex.add(entry);
				//System.out.println(entry);
			}
		}
	}

	public static String dic2Text(String ifo) throws IOException {

		StringBuilder sb = new StringBuilder();
		String dict = ifo.substring(0, ifo.lastIndexOf(".")) + ".dict";
		String idx = ifo.substring(0, ifo.lastIndexOf(".")) + ".idx";
		String txt = ifo.substring(0, ifo.lastIndexOf(".")) + ".txt";
		File txtFile = new File(txt);
		txtFile.delete();
		
		byte[] barr = FileUtil.readFileToMemory(idx);
		int zeroIndex = 0;
		ByteArrayBuffer babDef;
		for (int i = 0; i < barr.length; i++) {
			if (barr[i] == 0) {
				String name = new String(barr, zeroIndex, i - zeroIndex, "utf-8");
				zeroIndex = i + 9;
				int start = (sign2Unsign(barr[++i]) << 24) + (sign2Unsign(barr[++i]) << 16) + (sign2Unsign(barr[++i]) << 8) + sign2Unsign(barr[++i]);
				int end = (sign2Unsign(barr[++i]) << 24) + (sign2Unsign(barr[++i]) << 16) + (sign2Unsign(barr[++i]) << 8) + sign2Unsign(barr[++i]);
				i = zeroIndex;
				babDef = readFile(dict, start, end);
				
				String def = new String(babDef.toByteArray(), 0, end, "utf-8").replaceAll("\\\\", "\\\\").replaceAll("\n", "\\\\n");
				if (sb.length() < 32768) {
					sb.append(name).append("\t").append(def).append("\n");
				} else {
					FileUtil.writeAppendFileAsCharset(txtFile, sb.toString(), "utf-8");
					sb = new StringBuilder();
				}
			}
		}
		if (sb.length() > 0) {
			FileUtil.writeAppendFileAsCharset(txtFile, sb.toString(), "utf-8");
		}
		return txt;
	}
	
	static ByteArrayBuffer readFile(String f, int start, int len) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(f, "r");
//		Log.d("start", start + ".");
//		Log.d("len", len + ".");
		raf.seek(start);
		int bufLen = 4096;
		byte[] buffer = new byte[bufLen];
		ByteArrayBuffer bb = new ByteArrayBuffer(bufLen);
		int read = 0;
		int finished = 0;
		while (finished < len && (read = raf.read(buffer, 0, bufLen)) != -1) {
			finished += read;
			if (finished < len) {
				bb.append(buffer, 0, read);
			} else {
				bb.append(buffer, 0, read - (finished - len));
			}
		}
		raf.close();
		return bb;
	}

	public static int sign2Unsign(final byte b) {
		return (b < 0 ? 256 + b : b);
	}
}
