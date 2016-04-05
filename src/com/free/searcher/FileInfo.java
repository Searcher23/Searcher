package com.free.searcher;

import java.io.*;
import java.util.*;
import java.net.*;
import java.text.*;

class SortFileSizeDecrease implements Comparator<File> {

	@Override
	public int compare(File p1, File p2) {
		if (p1.length() < p2.length()) {
			return 1;
		} else if (p1.length() == p2.length()) {
			return 0;
		} else {
			return -1;
		}
	}
}

class SortFilePathDecrease implements Comparator<FileInfo> {

	@Override
	public int compare(FileInfo p1, FileInfo p2) {
		return -p1.path.compareTo(p2.path);
	}
}

public class FileInfo implements Comparable,  Comparator {
	
	File file;
	int group;
	long length;
	String path;

	public FileInfo() {
	}

	public FileInfo(File file) {
		this.file = file;
		length = file.length();
		path = file.getAbsolutePath();
	}
	
	public FileInfo(File file, int group) {
		this.file = file;
		this.group = group;
		length = file.length();
		path = file.getAbsolutePath();
	}

	@Override
	public int compare(Object p1, Object p2) {
		FileInfo ff1 = (FileInfo) p1;
		FileInfo ff2 = (FileInfo) p2;
		return ff1.path.compareTo(ff2.path);
	}

	@Override
	public int compareTo(Object p1) {
		FileInfo ff = (FileInfo) p1;
		return path.compareTo(ff.path);
	}
}
