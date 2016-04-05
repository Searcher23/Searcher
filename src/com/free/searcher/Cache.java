package com.free.searcher;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;
import android.util.LruCache;

public class Cache implements Serializable, Iterator<File> {

	private static final long serialVersionUID = -7028012255185772017L;

	private List<File> data = null;
	private LruCache<File, String> lru = null;
	private int currentSize = 0;
	private int totalSize = 0;
	private int maxSize = 0;
	private List<File> notYet;
	private int counter = 0;
	private int fileNum = 0;
	private Iterator<File> iter = null;

	public Cache(List<File> files) throws IOException {
		fileNum = files.size();
		data = new LinkedList<File>();
		lru = new LruCache<File, String>(9999);
		notYet = new LinkedList<File>();
		Log.i("maxMemory 1", "" + Runtime.getRuntime().maxMemory());
		maxSize = (int) (Runtime.getRuntime().maxMemory() * 0.3); // Runtime.getRuntime().maxMemory()
		for (File file : files) {
//			Log.i("caching file", file.getAbsolutePath() 
//					// + ", isFile: " + file.isFile()
//					+ ", size: " + file.length());
			if (file != null && file.isFile()) {
				long fileLength = file.length();
				totalSize += fileLength;
				String fileName = file.getAbsolutePath();
				if ((currentSize + fileLength) < maxSize) {
					String content = FileUtil
							.readFileAsCharset(fileName, Util.UTF8);
//					Entry<File, String> entry = new Entry<File, String>(file, readFileAsCharset);
					data.add(file);
					lru.put(file, content);
					currentSize += fileLength;
				} else {
					notYet.add(file);
				}
			}
		}
		iter = data.iterator();
		Log.i("freeMemory 2: ", "" + Runtime.getRuntime().freeMemory());
	}

	public int getTotalSize() {
		return totalSize;
	}

	public int getCurrentSize() {
		return currentSize;
	}

	private String add(File file) {
		Log.i("Cache.add", file.getAbsolutePath());
		if (file != null && file.isFile()) {
			long fileLength = file.length();
			String fileName = file.getAbsolutePath();
			String fileContent = null;
			maxSize = (int) (Runtime.getRuntime().freeMemory() * 0.5);
			try {
				if ((currentSize + fileLength) < maxSize) {
					fileContent = FileUtil.readFileAsCharset(fileName, Util.UTF8);
					data.add(file);
					lru.put(file, fileContent);
					currentSize += fileLength;
				} else {
					File f = null;
					String len = "";
					while (data.size() > 0) {
						Log.i("Remove in dataSet " + counter + ": ", f + "");
						f = data.remove(0);
						if ((len = lru.remove(f)) != null) {
							currentSize -= len.length();
						} else {
							currentSize -= f.length();
						}
						notYet.add(f);
						if (currentSize + fileLength < maxSize) {
							break;
						}
					}
					fileContent = FileUtil.readFileAsCharset(fileName, Util.UTF8);
					data.add(file);
					lru.put(file, fileContent);
				}
			} catch (IOException e) {
				Log.e("Cache.add", e.getMessage(), e);
			}
			return fileContent;
		} else {
			return null;
		}
	}
	
	public int cached() {
		return lru.putCount();
	}

	public void reset() {
		counter = 0;
		iter = data.iterator();
	}

	@Override
	public boolean hasNext() {
		return counter < fileNum;
	}

	public String get(File file) throws IOException {
		if (lru.get(file) != null) {
			return lru.get(file);
		} else {
			String fileContent = FileUtil.readFileAsCharset(file.getAbsolutePath(), Util.UTF8);
			lru.put(file, fileContent);
			return fileContent;
		}
	}
	
	@Override
	public File next() {
		if (counter < fileNum) {
			if (counter++ < data.size()) {
				return iter.next(); // data.get(counter++);
			} else {
				File file = notYet.remove(0);
				// System.err.println("removed in notYet: " + + counter + ": " +
				// file);
				add(file);
				return file;
			}
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public static void main(String[] args) throws IOException {
		List<File> files = null; // FileUtil.getFilesDialog(null, null, "/", true, JFileChooser.FILES_AND_DIRECTORIES, true, "Files or Folder");
		int count = 0;
		long start1 = System.currentTimeMillis();
		Cache cache = new Cache(files);
		while (cache.hasNext()) {
			count++;
			File entry = cache.next();
			System.err.println(entry);
		}
		long end1 = System.currentTimeMillis();
		System.err.println("Run 1: " + (end1 - start1));
		System.err.println("Count 1: " + count);

		count = 0;
		long start2 = System.currentTimeMillis();
		cache.reset();
		while (cache.hasNext()) {
			count++;
			File entry = cache.next();
			System.err.println(entry);
		}
		long end2 = System.currentTimeMillis();
		System.err.println("Run 2: " + (end2 - start2));
		System.err.println("Count 2: " + count);
	}

}
