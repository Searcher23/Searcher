package com.free.translation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import android.os.*;
import android.util.*;
import com.free.searcher.*;

public final class Constants {

	public static final String WINDOWS_1252 = "windows-1252";
	public static final String ISO_8859_1 = "ISO-8859-1";
	public static final String UTF8 = "UTF-8";
	public static final String NEW_LINE = "\r\n";
	
//	public static final String DICTIONARY_BUNDLE = "com.translation.translation.resources.Dictionary";
	//public static final ResourceBundle RB = ResourceBundle.getBundle(DICTIONARY_BUNDLE, Locale.US);
//	public static final Logger LOGGER = Logger.getLogger(Constants.class.getName());
	
	public static final String DOCTYPE_WITH_DTD = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"data/xhtml1-transitional.dtd\">\r\n";//RB.getString("doctype");
	public static final String ZIP_SUFFIX = "";
	public static String PRIVATE_PATH = MainFragment.PRIVATE_DIR.getParent() + //"/sdcard" + //Environment.getExternalStorageDirectory().getAbsolutePath() + 
	"/.com.free.translation";
	//private static File PRIVATE_DIR;
//	private static void init() {
//		PRIVATE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.com.free.translation";
//		PRIVATE_DIR = new File(PRIVATE_PATH);
//		PRIVATE_DIR.mkdirs();
//	}

//	static {
//		String sdCardPath = System.getenv("SECONDARY_STORAGE");
//		File tmp = null;
//		if (sdCardPath == null) {
//			init();
//			Log.d("sdCardPath = null", "SearchFragment.PRIVATE_PATH = " + PRIVATE_PATH);
//		} else if (!sdCardPath.contains(":")) {
//			PRIVATE_PATH = sdCardPath + "/.com.free.translation";
//			PRIVATE_DIR = new File(PRIVATE_PATH);
//			tmp = new File(PRIVATE_PATH + "/xxx" + System.currentTimeMillis());
//			try {
//				if (PRIVATE_DIR.mkdirs() || tmp.createNewFile()) {
//					if (tmp != null) {
//						Log.d("delete 1", tmp + ": " + tmp.delete());
//					}
//					Log.d(sdCardPath, PRIVATE_DIR.getTotalSpace() + " bytes");
//				} else {
//					init();
//				}
//			} catch (IOException e) {
//				//e.printStackTrace();
//				Log.e("tmp", tmp + ".");
//				Log.e("SearchFragment.PRIVATE_DIR", PRIVATE_DIR + ".");
//				init();
//				Log.d("sdCardPath 1", "SearchFragment.PRIVATE_PATH = " + PRIVATE_PATH);
//			}
//		} else if (sdCardPath.contains(":")) {
//			//Multiple Sdcards show root folder and remove the Internal storage from that.
//			File storage = new File("/storage");
//			File[] fs = storage.listFiles();
//			init();
//			if (fs != null) {
//				File maxPrev = PRIVATE_DIR;
//				long maxTotal = PRIVATE_DIR.getTotalSpace();
//				for (File f : fs) {
//					String absolutePath = f.getAbsolutePath();
//					long totalSpace = f.getTotalSpace();
//					Log.d(absolutePath, totalSpace + " bytes, can write " + f.canWrite());
//					try {
//						String comPath = absolutePath + "/.com.free.translation";
//						if (totalSpace > maxTotal && f.canWrite() && (new File(comPath).mkdirs() || (tmp = new File(comPath + "/xxx" + System.currentTimeMillis())).createNewFile())) {
//							PRIVATE_PATH = comPath;
//							PRIVATE_DIR = new File(PRIVATE_PATH);
//							Log.d("sdCard ok", PRIVATE_DIR + ", tmp = " + tmp);
//							maxTotal = totalSpace;
//							// max old
//							Log.d("delete ", maxPrev + ": " + maxPrev.delete());
//							maxPrev = f;
//							if (tmp != null) {
//								Log.d("delete 2", tmp + ": " + tmp.delete());
//								tmp = null;
//							}
//						}
//					} catch (IOException e) {
//						Log.e("tmp", tmp + ".");
//						Log.e("SearchFragment.PRIVATE_DIR", PRIVATE_DIR + ".");
//						Log.d("sdCardPath 2", "SearchFragment.PRIVATE_PATH = " + PRIVATE_PATH);
//					}
//				}
//			}
//		}
//	}
	
//	public static final String ORI_WORD_FILE_NAME = Constants.RB.getString("oriWordFile");
//	public static final String ORI_TEXT_FILE_NAME = Constants.RB.getString("oriTextFile");
	public static final String DICT_ANH_VIET_CONVERTED = PRIVATE_PATH + "/" + "data/dictd_www.freedict.de_anh-viet.converted.txt";
//	public static final String TEMP_TEXT_FILE_NAME = Constants.RB.getString("tempTextFile");
	public static final String DICT_ANH_VIET_NAME = PRIVATE_PATH + "/" + "data/dictd_www.freedict.de_anh-viet.txt";
//	public static final String DICT_ANH_VIET_TEST_NAME = Constants.RB.getString("dict_anh_viet_test");
//	public static final String TEMP_DICT_ANH_VIET_NAME = Constants.RB.getString("temp_dict_anh_viet");
	public static final String NEW_DICT_ANH_VIET_NAME = PRIVATE_PATH + "/" + "data/new_dictd_www.freedict.de_anh-viet.txt";
//	public static final String ANH_VIET_PANGO_NAME = Constants.RB.getString("anh_viet_pango");
//	public static final String ORI_HTML_FILE_NAME = Constants.RB.getString("oriHTMLFile");
	public static final String NEW_HTML_FILE_NAME = PRIVATE_PATH + "/" + "backup/translatedResult.htm";
//	public static final String ORI_PLAIN_TEXT_NAME = Constants.RB.getString("oriPlainText");
	public static final String PARSER_FILE_NAME_VALUE = PRIVATE_PATH + "/" + "data/englishPCFG.ser.gz";
	public static final String PARSED_FILE_NAME_VALUE = PRIVATE_PATH + "/" + "data/englishFactored.ser.gz";
	static final String MAX_PARSE_LENGTH_VALUE = "96";
	public static final String MAIN_DICT_SHEET_NAME = "Dictionary";
	public static final String NEW_WORDS_SHEET_NAME = "New Words";
////	public static final String NEW_EXCEL_FILE_NAME = Constants.RB.getString("newExcelFile");
	public static final String ORI_EXCEL_FILE = PRIVATE_PATH + "/" + "data/Ori_Dictionary.xls";
////	public static final String MANUAL_APPEND_DICT_EXCEL_FILE_NAME = Constants.RB.getString("ManualAppendDict.xls");
////	public static final String MANUAL_APPEND_GEN_DICT_EXCEL_FILE_NAME = Constants.RB.getString("ManualAppendGenDict.xls");
	public static final String GEN_EXCEL_FILE = PRIVATE_PATH + "/" + "data/Generated_Dict.xls";
//	public static final String ORI_PDF_FILE_NAME = Constants.RB.getString("oriPdfFile");
//	
//	public static final boolean READ_GEN_EXCEL_FILE = Constants.RB.getString("readGenExcelFile").equals("true");
//	public static final boolean READ_ORI_EXCEL_FILE = Constants.RB.getString("readOriExcelFile").equals("true");
	public static final boolean READ_DICT_FROM_PLAIN_FILE = false; // Constants.RB.getString("readPlainFileDict").equals("true");
	public static final String SERIALED_PLAIN_FILE_NAME = "new_dict_anh_viet.ser";
	public static final String SERIALED_EXCEL_FILE_NAME = "Pa-Auk_Dictionary.ser";
//	
	public static final String IRREGULAR_VERBS_EXCEL_FILE = PRIVATE_PATH + "/" + "data/List of English Irregular Verbs.xls";
	public static final String IRREGULAR_PLURAL_NOUN_FILE = PRIVATE_PATH + "/" + "data/Irregular-Plural-Nouns.txt";
//	//public static final String LOG_FILE = Constants.RB.getString("logFileName");
//
//	public static final String OOO_EXE_FOLDER = RB.getString("oooExeFolder");
//	public static final int NOT_SET = -1;
//	public static final int NOUN = 1;
//	public static final int VERB = 2;
//	public static final int ADJECTIVE = 3;
//	public static final int ADVERB = 4;
//	public static final int OTHER = 5;
//	public static final int ADJECTIVE_COMPARATIVE = 6;
	
	public static final String TIMES_NEW_ROMAN = "Times New Roman";
//	public static final boolean INIT_ENABLE_LOGGER = Boolean.parseBoolean(Constants.RB.getString("initEnableLogger"));


//	static {
//		try {
//			//new File("logs").mkdir();
//			String dateTime = new Date(System.currentTimeMillis()).toString().replaceAll(":", ".");
////			String logFileName = (Constants.LOG_FILE + ".txt");
//			String logFileName = (Constants.LOG_FILE + " - " + dateTime + ".zip");
//			File logFile = new File(logFileName);
//			FileOutputStream fos = new FileOutputStream(logFile);
//			ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos));
//			ZipEntry zipEntry = new ZipEntry("log - " + dateTime + ".txt");
//			zos.putNextEntry(zipEntry);
//
//			StreamHandler handler = new StreamHandler(zos, new SimpleFormatter());
//			handler.setEncoding(UTF8);
//			handler.setLevel(Level.ALL);
//			LOGGER.addHandler(handler);
//			LOGGER.setLevel(Level.ALL);
//
//			File logDir = new File(LOG_FILE).getParentFile();
//			File[] logFiles = logDir.listFiles();
//			if (logFiles.length > 10) {
//				Arrays.sort(logFiles, 0, logFiles.length, new TimeComparator());
//				for (int i = 0; i < logFiles.length - 10; i++) {
//					logFiles[i].delete();
//				}
//			}
//		} catch (FileNotFoundException e) {
//			throw new RuntimeException(e);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
//		Constants.LOGGER.getHandlers()[0].close();
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		System.out.println(System.getProperty("user.dir"));
//		LOGGER.info(new Date(System.currentTimeMillis()).toString());
//		LOGGER.setLevel(Level.OFF);
//		LOGGER.info(new Date(System.currentTimeMillis()).toString());
//		LOGGER.info(new Date(System.currentTimeMillis()).toString());
//		LOGGER.info(new Date(System.currentTimeMillis()).toString());
//		LOGGER.info(new Date(System.currentTimeMillis()).toString());
//		LOGGER.setLevel(Level.INFO);
//		LOGGER.info("helloworld");
//		LOGGER.info("helloworld2");
	}
}
