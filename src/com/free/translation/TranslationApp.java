package com.free.translation;

import com.free.translation.html.*;
//import com.free.translation.util.*;
import java.io.*;

import com.free.translation.html.HtmlSAXHandler;
import java.util.*;
import java.text.*;
import android.util.*;
import android.webkit.*;
import com.free.searcher.*;
//import android.util.*;

public class TranslationApp {

	private static final long serialVersionUID = -8841848302971459825L;
	private static final boolean opti = true;
	private static final String idx = Constants.PRIVATE_PATH + "/dtds/new_dictd_www.freedict.de_anh-viet.txt.idx";
	private static final String dict = Constants.PRIVATE_PATH + "/dtds/new_dictd_www.freedict.de_anh-viet.txt.dict";
	
//    static {
//		try {
//			FileUtil.copyResourceToFile(new File(Constants.PRIVATE_PATH), Constants.PARSER_FILE_NAME_VALUE);
//			FileUtil.copyResourceToFile(new File(Constants.PRIVATE_PATH), Constants.PARSED_FILE_NAME_VALUE);
//			FileUtil.copyResourceToFile(new File(Constants.PRIVATE_PATH), Constants.ORI_EXCEL_FILE);
//			FileUtil.copyResourceToFile(new File(Constants.PRIVATE_PATH), Constants.IRREGULAR_VERBS_EXCEL_FILE);
//			FileUtil.copyResourceToFile(new File(Constants.PRIVATE_PATH), Constants.IRREGULAR_PLURAL_NOUN_FILE);
//		} catch (Throwable t) {
//			Log.e(TranslationApp.class.getClass().getName(), t.getMessage(), t);
//		} 
//    }
	
	public static boolean stopTranslate = false;

	public void translate(String sourcePath, SearchFragment searchFragment) {
		long start = System.currentTimeMillis();
		File sourceFile = new File(sourcePath);
		final String sourceLower = sourcePath.toLowerCase();
		try {
			String translatedPath =  Constants.PRIVATE_PATH + sourcePath + ".translated.html";
			File translatedFile = new File(translatedPath);
			translatedFile.getParentFile().mkdirs();
			if (sourceLower.endsWith(".txt")) {
				extractWords(sourceFile);
				Translator.translateFromPlainTextFile(sourceFile, translatedPath, searchFragment);
			} else if (sourceLower.endsWith(".html") || sourceLower.endsWith(".htm")) {
				extractWords(sourceFile);
				HtmlSAXHandler.translateFromHTML(sourceFile, translatedFile);
//			} else if (sourceLower.endsWith(".pdf")) {
//				String tempFileName = Constants.PRIVATE_PATH + sourcePath + ".converted.txt";
//				ItextPdfToHtml.parsePdfToText(sourcePath, tempFileName);
//				extractWords(new  File(tempFileName), Constants.PRIVATE_PATH + sourcePath + ".words.txt");
//				Translator.translateFromPlainTextFile(new  File(tempFileName), translatedPath);
			}
			
//			if (!opti) {
//				postProcessNewWords(WordInfo.notYetDefined, Constants.ORI_EXCEL_FILE, Constants.NEW_WORDS_SHEET_NAME, false);
//			}
		} catch (Throwable t) {
			Log.e("translate", t.getMessage(), t);
		}
		Log.d("translate took", "" + (System.currentTimeMillis() - start));
	}

	private void postProcessNewWords(TreeSet<String> notYetDefined, String fPath, String sheetName, boolean adddOri) throws IOException, ClassNotFoundException {
		//Log.d("postProcessNewWords", Util.collectionToString(notYetDefined, false, "\r\n") + ".");
		Log.d("postProcessNewWords.sheetName", fPath + ", " + sheetName + ", adddOri " + adddOri);
		StardictReader lr2 = StardictReader.instance(idx, 
													 dict);
//		("/sdcard/dictdata/stardict_en_vi/en_vi.idx",
//													 "/sdcard/dictdata/stardict_en_vi/en_vi.dict");
		List<String> def;
		List<String> notYetDef = new LinkedList<>();
		ComplexWordDef complexWordDef;
		WordClass wc;
		Dictionary newWords = new Dictionary();
		for (String name : notYetDefined) {
//			Log.d("notYetDefined.name", name);
			def = lr2.readDef(name);
			if (def != null) {
				//Log.d("notYetDefined.name.def!", name);
				complexWordDef = new ComplexWordDef(name);
				for (String st : def) {
					//Log.d("notYetDefined.name.st", name);
					String[] split = st.split("\t");
					if (split.length > 1) {
						String type = split[0];
						
						//Log.d(name, type + "|" + split[1]);
						//"Word", "N", "V", "Adjective", "Adverb", "Other"
						if (type.equals("N")) {
							wc = new WordClass(Dictionary.NOUN);
						} else if (type.equals("V")) {
							wc = new WordClass(Dictionary.VERB);
						} else if (type.equals("Adj")) {
							wc = new WordClass(Dictionary.ADJECTIVE);
						} else if (type.equals("Adv")) {
							wc = new WordClass(Dictionary.ADVERB);
						} else {
							wc = new WordClass(Dictionary.OTHER);
						}
						wc.addDefinition(split[1]);
						complexWordDef.add(wc);
					}
				}
				newWords.add(complexWordDef);
			} else {
				notYetDef.add(name);
			}
		}
		FileUtil.writeContentToFile(Constants.PRIVATE_PATH + "/notYetDef.txt", Util.collectionToString(notYetDef, false, "\n"));
		DictionaryLoader.writeNewWordsSheet(newWords, fPath, sheetName);
		if (adddOri) {
			Translator.GEN_DICT = new DictionaryLoader().readGenExcelDict(true);
		}
	}

	private void extractWords(File sourceFile, String words) throws IOException, ClassNotFoundException {
		File wordsFile = new File(words);
		String whole = null;
		if (!wordsFile.exists() || sourceFile.lastModified() > wordsFile.lastModified()) {
			String htmlToText = "";
			if (sourceFile.getName().toLowerCase().endsWith(".html")
				|| sourceFile.getName().toLowerCase().endsWith(".htm")) {
				htmlToText = Util.htmlToText(sourceFile);
			} else {
				htmlToText = FileUtil.readFileAsCharset(sourceFile.getAbsolutePath(), "utf-8");
			}
			whole = htmlToText.replaceAll(
				"[0-9 \r\n\t\f~!@#$%^&*()_+{}|:\"<>?\\-=\\[\\]\\\\;',./`’”“‘]+", 
			"\n");
			FileUtil.writeFileAsCharset(wordsFile, whole, "UTF-8");
		}
		if (opti) {
			backupSaveRunGenDict(whole, words);
		}
	}

	private void backupSaveRunGenDict(String whole, String wordsPath) throws IOException, ClassNotFoundException {
		FileInputStream f = new FileInputStream(Constants.ORI_EXCEL_FILE);
		FileUtil.saveISToFile(
			f, Constants.ORI_EXCEL_FILE + " " 
			+ DateFormat.getDateTimeInstance().format(System.currentTimeMillis()).replaceAll("[:/\\\\]+", "-") +
			".xls");
		f.close();

		if (whole == null) {
			whole =  FileUtil.readFileAsCharset(wordsPath, "utf-8");
		}

		int idx = 0;
		int prev = 0;
		TreeSet<String> wordSet = new TreeSet<String>();
		while ((idx = whole.indexOf("\n", prev)) > 0) {
			wordSet.add(whole.substring(prev, idx));
			prev = idx + 1;
		}

		TreeSet<String> notYetDef = new TreeSet<String>();
		for (String word : wordSet) {
			word = word.toLowerCase();
			if (!Translator.GEN_DICT.contains(new ComplexWordDef(word))) {
				//Log.d("notYetDef.add(word)", word);
				notYetDef.add(word);
			}
		}
		postProcessNewWords(notYetDef, Constants.ORI_EXCEL_FILE, Constants.NEW_WORDS_SHEET_NAME, true);
	}
	
	private void extractWords(File sourceFile) throws IOException, ClassNotFoundException {
		String splitedFile = Constants.PRIVATE_PATH + sourceFile.getAbsolutePath() + ".words.txt";
		extractWords(sourceFile, splitedFile);
	}
	
	public static void main(String args[]) {
//		new TranslationApp().translate(
//		"/storage/MicroSD/Text/Pa-Auk Tawya Sayadaw/Nibanna Gamini Big Five (English)/Vol 3/Vol 3,Introduction.pdf");
		new TranslationApp().translate("/storage/emulated/0/backups/Living and dying new.doc.converted.txt", null);
	}


}
