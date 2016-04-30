package com.free.translation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Logger;

import java.io.*;
import android.util.*;

public class Dictionary implements Iterable<ComplexWordDef>, Serializable {
	private static final long serialVersionUID = -6438419426826900803L;

	//private transient static Logger LOG = Constants.LOGGER; //Logger.getLogger(Dictionary.class.getName());

	private transient static final String SPLIT_CHAR = "/";

	private TreeSet<ComplexWordDef> dictionary = new TreeSet<ComplexWordDef>();
	
	private transient List<ComplexWordDef> duplicateList = new LinkedList<ComplexWordDef>();
	
	public Dictionary() {
		super();
	}
	
//	private int dictSize = 0;
//	private Object[] dictArr = null;
	
//	public Object[] getDictArr() {
//		if (dictSize != dictionary.size()) {
//			dictArr = dictionary.toArray();
//			dictSize = dictionary.size();
//			return dictArr;
//		} else {
//			return dictArr;
//		}
//	}
	
//	public void appendDictionary(Dictionary dic) {
//		for (ComplexWordDef complexWordDef : dic) {
//			this.addAppend(complexWordDef);
//		}
//	}
	
//	public void addNewDictionary(Dictionary dic) {
//		for (ComplexWordDef complexWordDef : dic) {
//			this.addNewOnly(complexWordDef);
//		}
//	}
	
//	public void addDictionary(TreeSet<ComplexWordDef> dic) {
//		dictionary.addAll(dic);
//	}
	
//	public void setDictionary(TreeSet<ComplexWordDef> dic) {
//		this.dictionary = dic;
//		
//	}

//	public void addNewOnly(ComplexWordDef newWord) {
//		if (!dictionary.contains(newWord)) {
//			dictionary.add(newWord);
//			
//		}
//	}
	
	public void addAppend(ComplexWordDef newWord) {
		if (dictionary.contains(newWord)) {
			duplicateList.add(newWord);

			ComplexWordDef oldWord = dictionary.floor(newWord);
			for (WordClass newWC : newWord.getDefinitions()) {
				boolean isDifferentWordClass = true;
				for (WordClass oldWC : oldWord.getDefinitions()) {
					if (oldWC.getType() == newWC.getType()) {
						isDifferentWordClass = false;
						String[] oldSplit = oldWC.getDefinition().split(SPLIT_CHAR);
						String[] newSplit = newWC.getDefinition().split(SPLIT_CHAR);
						List<String> oldList = Arrays.asList(oldSplit);
//						List<String> newList = Arrays.asList(newSplit);
						Collections.sort(oldList);
//						Collections.sort(newList);

//						StringBuilder duplicateSB = new StringBuilder();
						StringBuilder addNewDefinitionSB = new StringBuilder();
						String newString;
						for (int i = 0; i < newSplit.length; i++) {
							newString = newSplit[i];
							if (!newString.equals("")) {
								if (Collections.binarySearch(oldList, newString) < 0) {
//									oldDefinitionSB.append(SPLIT_CHAR).append(newString);
									addNewDefinitionSB.append(SPLIT_CHAR).append(newString);
//								} else {
//									duplicateSB.append(SPLIT_CHAR).append(newString);
								}
							}
						}
//						if (duplicateSB.length() > 0) {
////							log.info("Duplicate ["
////									+ duplicateSB.append("] of origin word [")
////									.append(oldWord).append("]")
////									.substring(SPLIT_LENGTH));
//						}
						if (addNewDefinitionSB.length() > 0) {
//							log.info(addNewDefinitionSB.insert(
//									SPLIT_LENGTH, "Add New [").append("] of origin word [").append(
//											oldWord).append("]").substring(SPLIT_LENGTH));
							oldWC.addDefinition(addNewDefinitionSB.toString());
						}
					}
				}
				if (isDifferentWordClass) {
					oldWord.add(newWC);
//					Constants.LOG.info("newWC: " + oldWord);
				}
			}
		} else {
//			Constants.LOG.info("Add [" + newWord + "]");
			dictionary.add(newWord);
		}
	}

//	public void remove(ComplexWordDef word) {
//		dictionary.remove(word);
//		
//	}

//	public void removeAll() {
//		Iterator<ComplexWordDef> iter = dictionary.iterator();
//		while (iter.hasNext()) {
//			iter.next();
//			iter.remove();
//		}
//		
//	}

	@Override
	public Iterator<ComplexWordDef> iterator() {
		return dictionary.iterator();
	}

	public ComplexWordDef getComplexWordDef(String name) {
		if (name != null) {
			ComplexWordDef e = new ComplexWordDef(name);
			ComplexWordDef floor = dictionary.floor(e);
			if (floor != null && floor.getName().equalsIgnoreCase(name)) {
				return floor;
			}
		}
		return null;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("Dictionary:\n");
		int counter = 0;
		for (ComplexWordDef wd : dictionary) {
			sb.append(++counter).append(": ");
			sb.append(wd).append("\n");
		}

		sb.append("Duplicated Words:\n");
		counter = 0;
		if (duplicateList != null) {
			for (ComplexWordDef wd : duplicateList) {
				sb.append(++counter).append(": ");
				sb.append(wd).append("\n");
			}
		}
		return sb.toString();
	}

	public int size() {
		return dictionary.size();
	}

	public void printContents() {
		Log.i("Dictionary", this.toString());
	}

	public boolean contains(ComplexWordDef dword) {
		return dictionary.contains(dword);
	}

	public ComplexWordDef floor(ComplexWordDef dword) {
		return dictionary.floor(dword);
	}

	public void add(ComplexWordDef dword) {
		dictionary.add(dword);
	}
	
//	public static final int NOT_SET = -1;
	public static final int NOUN = 1;
	public static final int VERB = 2;
	public static final int ADJECTIVE = 3;
	public static final int ADVERB = 4;
	public static final int OTHER = -1;
	public static final int ADJECTIVE_COMPARATIVE = 6;

//	String[] columnNames = new String[]{"Word", "Noun", "Verb", "Adjective", "Adverb", "Other"};
//	String[] normalColumnNames = new String[]{"word", "noun", "verb", "adjective", "adverb", "other"};
	
//	public void importFromModel(TableModel model) {
//		dictionary.clear();
//		boolean shouldBeAdded = false;
//		for (int i = 0; i < model.getRowCount(); i++) {
//			if (Util.isNotEmpty(model.getValueAt(i, 0))) {
//				ComplexWordDef wd = new ComplexWordDef((String) model.getValueAt(i, 0));
//				for (int j = 1; j < model.getColumnCount(); j++) {
//					if (Util.isNotEmpty(model.getValueAt(i, j))) {
//						WordClass wc = new WordClass(j);
//						// WordClass wc = new WordClass(normalColumnNames[j]);
//						wc.addDefinition(model.getValueAt(i, j).toString().replaceAll("[,;]", "/"));
//						wd.add(wc);
//						shouldBeAdded = true;
//					}
//				}
//				if (shouldBeAdded) {
//					dictionary.add(wd);
//					dictSize = dictionary.size();
//					dictArr = dictionary.toArray();
//				}
//				shouldBeAdded = false;
//			}
//		}
//	}
	
//	private Vector<String> convertColumnNamesToVector() {
//		Vector<String> vector = new Vector<String>();
//		for (String st : columnNames) {
//			vector.add(st);
//		}
//		return vector;
//	}
	
//	public DefaultTableModel convertToModel() {
//		DefaultTableModel model = new DefaultTableModel();
//		model.setDataVector(convertDicToVector(), convertColumnNamesToVector());
//		return model;
//	}

//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	private Vector convertDicToVector() {
//		Vector dataVector = new Vector();
//		boolean isAdded = false;
//		for (ComplexWordDef wd : dictionary) {
//			Vector vector = new Vector();
//			vector.add(wd.getName());
//			for (int i = 1; i < columnNames.length; i++) {
//				isAdded = false;
//				for (WordClass wc : wd.getDefinitions()) {
//					if (i < columnNames.length - 1) {
//						if (i == wc.getType()) {
////							if (columnNames[i].equalsIgnoreCase(wc.getType())) {
//							vector.add(wc.getDefinition());
//							isAdded = true;
//						}
//					} else if (Constants.NOT_SET == wc.getType() || i == wc.getType()) {
////					} else if ("".equals(wc.getType()) || columnNames[i].equalsIgnoreCase(wc.getType())) {
//						vector.add(wc.getDefinition());
//						isAdded = true;
//					}
//				}
//				if (!isAdded) {
//					vector.add("");
//				}
//			}
//			dataVector.add(vector);
//		}
//		return dataVector;
//	}

	public void save(String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		ObjectOutputStream out = new ObjectOutputStream(bos);
		out.writeObject(this);
		out.flush();
		out.close();
		bos.close();
		fos.close();
	}

	public static void save(Dictionary dictionary, String fileName) throws IOException {
		FileOutputStream fos = new FileOutputStream(fileName);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		ObjectOutputStream out = new ObjectOutputStream(bos);
		out.writeObject(dictionary);
		out.flush();
		out.close();
		bos.close();
		fos.close();
	}
	
	public static Dictionary restore(String fileName) throws IOException, ClassNotFoundException {
		Log.i("Dictionary", "processing: " + fileName);
		FileInputStream fis = new FileInputStream(fileName);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ObjectInputStream in = new ObjectInputStream(bis);
		Dictionary dic = (Dictionary) in.readObject();
		in.close();
		bis.close();
		fis.close();
		return dic;
	}

	public static void main(String[] args) {
		TreeSet<String> set = new TreeSet<String>();
		set.add("world2");
		set.add("hello");
		set.add("world");
	}
}
