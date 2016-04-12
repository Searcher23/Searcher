package com.free.translation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import com.free.translation.util.*;
import org.apache.poi.hdf.model.hdftypes.*;
import org.apache.poi.poifs.filesystem.*;
import java.io.*;
import android.util.*;
import com.free.searcher.*;
//import android.util.*;

public class DictionaryLoader implements Iterable<ComplexWordDef> {
	private Dictionary genDic;
	private Dictionary oriDic;
	private static Logger logger = Constants.LOGGER;

	public static final TreeSet<Entry<String, List<String>>> IRREGULAR_VERBS = new TreeSet<Entry<String, List<String>>>();
	
	public static Map<String, String[]> IRREGULAR_NOUN_MAP = null;

	static {
		try {

			// Đọc danh sách động từ bất quy tắc
			Workbook irregularWB = FileUtil.readWorkBook(Constants.IRREGULAR_VERBS_EXCEL_FILE);
			// lưu động từ bất quy tắc vào TreeSet IRREGULAR_VERBS
			fillIrregularVerbs(irregularWB);
			
			// Đọc danh sách danh từ số nhiều bất quy tắc
			IRREGULAR_NOUN_MAP = readIrregularPluralNoun(Constants.IRREGULAR_PLURAL_NOUN_FILE);
			
//			LOG.info(Util.setToSlashString(IRREGULAR_VERBS));
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public DictionaryLoader() {
		genDic = new Dictionary();
		oriDic = new Dictionary();
	}

	/**
	 * Điền động từ bất quy tắc vào TreeSet IRREGULAR_VERBS
	 */
	private static void fillIrregularVerbs(Workbook wb) {
		Entry<String, List<String>> newIrregularVerb = null;
		List<String> values = null;
		Row row;
		Sheet sheet = wb.getSheetAt(0);
		int lastRowNum = sheet.getLastRowNum();
		for (int j = 0; j <= lastRowNum; j++) {
			row = sheet.getRow(j);
			if (row != null) {
				Cell cellBaseForm = row.getCell(0);
				if (cellBaseForm != null) {
					values = new LinkedList<String>();
					for (int k = 1; k < 5; k++) {
						Cell cellk = row.getCell(k);
						if (cellk != null) {
							String cellkValue = cellk.getStringCellValue();
//							String[] cellkArr = cellkValue.split("/");
//							for (int l = 0; l < cellkArr.length; l++) {
//								values.add(cellkArr[l].toLowerCase());
//							}
							values.add(cellkValue.toLowerCase());
						}
					}
					newIrregularVerb = new Entry<String, List<String>>(
							cellBaseForm.getStringCellValue().toLowerCase(),
							values);
					IRREGULAR_VERBS.add(newIrregularVerb);
				}
			}
		}
//		LOG.info(Util.setToString(IRREGULAR_VERBS));
	}

	/**
	 * Đọc danh sách danh từ có số nhiều bất quy tắc
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static Map<String, String[]> readIrregularPluralNoun(String fileName) throws IOException {
		Map<String, String[]> map = new TreeMap<String, String[]>();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName), Constants.UTF8), (int) new File(fileName).length());
		while (in.ready()) {
			String line = in.readLine();
			//System.out.println("line " + line);
			if (line != null) {
				String[] lineSplitted = line.split("/");
				String[] list = new String[lineSplitted.length - 1];
				if (lineSplitted.length > 1) {
					for (int i = 1; i < lineSplitted.length; i++) {
						list[i - 1] = lineSplitted[i];
					}
					map.put(lineSplitted[0], list);
				}
			}
		}
		in.close();
		return map;
	}

	/**
	 * Đọc mảng danh từ bất quy tắc
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public static String[] readIrregularNoun(String key) throws IOException {
		if (IRREGULAR_NOUN_MAP == null) {
			IRREGULAR_NOUN_MAP = readIrregularPluralNoun(Constants.IRREGULAR_PLURAL_NOUN_FILE);
		}
		return IRREGULAR_NOUN_MAP.get(key);
	}

	/**
	 * Đọc file từ điển excel gốc chưa chia số nhiều, quá khứ, tạo ra Dictionary có số nhiều, quá khú, qkpt...
	 * @param inputFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Dictionary readOriExcelDict(String inputFile, boolean manySheet) throws FileNotFoundException, IOException {
		logger.log(Level.INFO, inputFile);
		Workbook wb = FileUtil.readWorkBook(inputFile);
		//		Dictionary dic = new Dictionary();
		Sheet sheet;
		Row row;
		int lastRowNum = 0;
		int numberOfSheets = 1;
		if (manySheet) {
			numberOfSheets = wb.getNumberOfSheets();
		}
		
		for (int i = 0; i < numberOfSheets; i++) {
			sheet = wb.getSheetAt(i);
			//			LOG.info("sheet.getSheetName(): " + sheet.getSheetName());
			lastRowNum = sheet.getLastRowNum();
			ComplexWordDef newWord = null;

			List<Entry<String, String>> pluralNounList = new LinkedList<Entry<String, String>>();
			List<Entry<String, String>> verbSet = new LinkedList<Entry<String, String>>();

			for (int j = 0; j <= lastRowNum; j++) {
				row = sheet.getRow(j);
				if (row != null) {
					newWord = null;
					Cell cell0 = row.getCell(0);
					Cell cell1 = row.getCell(1);
					Cell cell2 = row.getCell(2);
					if (cell0 != null && cell2 != null) {
						String cell2Value = cell2.getStringCellValue();
						String cell0Value = cell0.getStringCellValue();
						if (Util.isNotEmpty(cell0Value)) {
							if (cell1 != null) {
								String cell1Value = cell1.getStringCellValue();
								int cell1IntValue = getIntType(cell1Value);
								newWord = new ComplexWordDef(cell0Value).add(cell1IntValue, "", cell2Value);
								if ("N".equalsIgnoreCase(cell1Value)) {
									String[] pluralNouns = makeRegularPluralNounsForCustomDict(cell0Value);
									for (String pluralNoun : pluralNouns) {
										pluralNounList.add(new Entry<String, String>(pluralNoun, cell0Value));
									}
								} else if ("V".equals(cell1Value)) {
									verbSet.add(new Entry<String, String>(cell0Value, cell2Value));
	//							} else if ("Adj".equals(cell1Value)) {
								}
							} else {
								newWord = new ComplexWordDef(cell0Value).add(Dictionary.OTHER, "", cell2Value);
							}
							genDic.addAppend(newWord);
							oriDic.addAppend(newWord);
						}
//						LOG.info("newWord.toString(): " + newWord.toString());
					}
				}
			}

			addPluralNounForms(pluralNounList);
			for (Entry<String, String> entry : verbSet) {
				addForRegularVerbs(entry.getKey(), entry.getValue());
			}
		}
//		logger.info("dic.toString(): " + dic.toString());
		logger.log(Level.INFO, "Total: {0} words", genDic.size());
//		correctPastVerb();
		return genDic;
	}

	/**
	 * Thêm định nghĩa cho các động từ có quy tắc quá khứ, hiện tại tiếp diễn...
	 * 
	 */
	private void addForRegularVerbs(String exampleName, String definition) {
//		LOG.info("exampleName: " + exampleName + ", definition: " + definition);
		int index = exampleName.indexOf(" ");
		String verb = index > 0 ? exampleName.substring(0, index) : exampleName;
		Entry<String, List<String>> floor = IRREGULAR_VERBS.floor(new Entry<String, List<String>>(verb, null));
		if (floor != null && verb.equalsIgnoreCase(floor.getKey())) {
//			logger.info("floor: " + floor);
			int counter = 0;
			for (String value : floor.getValue()) {
//				logger.info("value: [" + value + "], definition: [" + definition + "]");
//				logger.info("definition: " + definition);
				String[] splitValues = value.split("/");
//				Arrays.sort(splitValues);
				int splitValuesLength = splitValues.length;
				for (int i = 0; i < splitValuesLength; i++) {
//					logger.info("splitValues[" + i +"]: " + splitValues[i]);
					ComplexWordDef newDW = new ComplexWordDef(splitValues[i] + ((index > 0) ? exampleName.substring(index) : ""));
//					logger.info("newDW 0: " + newDW);
					if (counter == 0) {
						newDW.add(Dictionary.VERB, "past", definition);
					} else if (counter == 3) {
						newDW.add(Dictionary.VERB, "pres", definition);
					} else {
						newDW.add(Dictionary.VERB, "", definition);
					}
//					logger.info("newDW 1: " + newDW);
					add(newDW);
//					logger.info("newDW 2: " + newDW);
				}
				counter++;
			}
		} else {
			StringBuilder sbS = new StringBuilder();
			StringBuilder sbED = new StringBuilder();
			StringBuilder sbING = new StringBuilder();
			if (index > 0) {
//				LOG.info("makeOtherVerbForms: " + verb);

				if (verb.length() >= 2) {
					makeVerbForms(verb, sbS, sbING, sbED);

					add(new ComplexWordDef(exampleName).add(Dictionary.VERB, "", definition));

					add(new ComplexWordDef(sbS.append(
							exampleName.substring(index)).toString()).add(Dictionary.VERB, "", definition));

					add(new ComplexWordDef(sbING.append(
							exampleName.substring(index)).toString()).add(Dictionary.VERB, "", definition));

					add(new ComplexWordDef(sbED.append(
							exampleName.substring(index)).toString()).add(Dictionary.VERB, "", definition));
				}
			} else {
//				LOG.info("exampleName: " + exampleName + ", definition: " + definition);
				makeVerbForms(exampleName, sbS, sbING, sbED);

				add(new ComplexWordDef(sbS.toString()).add(Dictionary.VERB, "", definition));

				add(new ComplexWordDef(sbING.toString()).add(Dictionary.VERB, "", definition));

				add(new ComplexWordDef(sbED.toString()).add(Dictionary.VERB, "", definition));
			}
		}
	}

	/**
	 * Tạo các dạng động từ quy tắc, hên xui theo nhấn âm
	 * @param name
	 * @param sbS
	 * @param sbING
	 * @param sbED
	 */
	private static void makeVerbForms(String name, StringBuilder sbS, StringBuilder sbING, StringBuilder sbED) {
		int length = name.length();
		char thirdChar;
		char secondLast = name.charAt(length - 2);
		char last = name.charAt(length - 1);

		if (last == 'y'
			&& 'a' != secondLast
			&& 'e' != secondLast
			&& 'i' != secondLast
			&& 'o' != secondLast
			&& 'u' != secondLast) { // carry caries carrying carried but not buy buys bought bought
			sbS.append(name, 0, length - 1).append("ies");
			sbING.append(name).append("ing");
			sbED.append(name, 0, length - 1).append("ied");
		} else if (name.endsWith("ie")) {	// die
			sbS.append(name).append("s");
			sbING.append(name, 0, length - 2).append("ying");
			sbED.append(name).append("d");
		} else if (last == 'e') {	// line
			sbS.append(name).append("s");
			sbING.append(name, 0, length - 1).append("ing");
			sbED.append(name).append("d");
		} else if (last == 's'
				   || last == 'x'
				   || last == 'z'
				   || name.endsWith("ch")
				   || name.endsWith("sh")
				   ) {	// box
			sbS.append(name).append("es");
			sbING.append(name).append("ing");
			sbED.append(name).append("ed");
		} else if ('a' != last
				   && 'e' != last
				   && 'i' != last
				   && 'o' != last
				   && 'u' != last
				   && 'y' != last
				   && ('a' == secondLast
				   || 'e' == secondLast
				   || 'i' == secondLast
				   || 'o' == secondLast
				   || 'u' == secondLast)
				   && (length > 2 && length < 6 ? ((thirdChar = name.charAt(length - 3)) != 'a'
				   && thirdChar != 'e'
				   && thirdChar != 'i'
				   && thirdChar != 'o'
				   && thirdChar != 'u'
				   && thirdChar != 'y'
				   ) : true)) { // stop stops stopping stopped
			sbS.append(name).append("s");
			sbING.append(name).append(last).append("ing");
			sbED.append(name).append(last).append("ed");
		} else {
			sbS.append(name).append("s");
			sbING.append(name).append("ing");
			sbED.append(name).append("ed");
		}
	}
	
	/**
	 * Tạo mảng các plural nouns
	 * @param name
	 * @return
	 */
	private static String[] makeRegularPluralNounsForCustomDict(String name) {
		if (IRREGULAR_NOUN_MAP.containsKey(name)) {
			return IRREGULAR_NOUN_MAP.get(name);
		} else {
			return new String[]{makeRegularPluralNounsForm(name)};
		}
	}

	/**
	 * Tạo danh từ số nhiều, hên xui trong trường hợp double ở các danh từ có nhấn âm cuối
	 * @param name
	 * @return
	 */
	private static String makeRegularPluralNounsForm(String name) {
		StringBuilder sb = null;
		int length = name.length();
		char last = name.charAt(length - 1);
//		LOG.info("makeNormalPluralForm: " + name);
		if (length > 1) {
			char secondLast = name.charAt(length - 2);
			if (name.endsWith("fe") && !name.endsWith("ffe")) {
				sb = new StringBuilder().append(name, 0, length - 2).append("ves");	// knife
			} else if (name.endsWith("ch")
					   || name.endsWith("sh")
					   ) {
				sb = new StringBuilder(name).append("es");	// inch dish
			} else if (name.endsWith("ex")) {
				sb = new StringBuilder().append(name, 0, length - 2).append("ices");	// vertex/vertices, vortex/vortices
			} else if (last =='y'
					   && 'a' != secondLast
					   && 'e' != secondLast
					   && 'i' != secondLast
					   && 'o' != secondLast
					   && 'u' != secondLast
					   ) {
				sb = new StringBuilder().append(name, 0, length - 1).append("ies"); 	// spy spies
			} else if (last == 'o'
					   && 'a' != secondLast
					   && 'e' != secondLast
					   && 'i' != secondLast
					   && 'o' != secondLast
					   && 'u' != secondLast
					   && 'y' != secondLast
					   ) {	
				sb = new StringBuilder(name).append("es");	// tomato potato
			} else if (last == 'f' && !name.endsWith("ff")) { // calf
				sb = new StringBuilder().append(name, 0, length - 1).append("ves");
			} else if (last == 's'
					   || last == 'z'
					   || last == 'x'
					   ) {
				sb = new StringBuilder(name).append("es");	// class buzz box
			} else {
				sb = new StringBuilder(name).append("s");
			}
//		} else if (length > 1) {
//			if (last == 'f') { // calf
//				sb = new StringBuilder().append(name, 0, length - 1).append("ves");
//			} else if (last == 's'
//					   || last == 'z'
//					   || last == 'x'
//					   ) {
//				sb = new StringBuilder(name).append("es");	// class buzz box
//			} else {
//				sb = new StringBuilder(name).append("s");
//			}
		} else {
			sb = new StringBuilder(name).append("s");
		}
		return sb.toString();
	}

	/**
	 *  add số nhiều vào trong Dictionary
		key: số nhiều, value: số ít
	 * @param pluralNounList
	 */
	private void addPluralNounForms(List<Entry<String, String>> pluralNounList) {
		ComplexWordDef dwTemp = null;
		for (Entry<String, String> entry : pluralNounList) {
			dwTemp = genDic.floor(new ComplexWordDef(entry.getValue()));
//			LOG.info("pluralNounList dwTemp: " + dwTemp);
//			LOG.info("pluralNounList entry: " + entry);
			if (dwTemp != null && dwTemp.getName().equalsIgnoreCase(entry.getValue())) {
				ComplexWordDef dw = new ComplexWordDef(entry.getKey());
				StringBuilder sb = new StringBuilder("những ");
				for (WordClass wc : dwTemp.getDefinitions()) {
					if (wc.getType() == Dictionary.NOUN) {
//						LOG.info("wc.getDefinition(): " + wc.getDefinition());
						sb.append(wc.getDefinition()).append("/");
					}
				}
				dw.add(Dictionary.NOUN, "", sb.toString());
				add(dw);
//				LOG.info("dw.toString(): " + dw.toString());
			} else {
				logger.log(Level.INFO, "problems with plural nouns in HND dictionary: {0}", entry);
			}
		}
	}

	/**
	 * Đọc mã của WordClass
	 * @param cell1Value
	 * @return
	 */
	private static int getIntType(String cell1Value) {
		int cell1IntValue;
		if ("N".equals(cell1Value)) {
			cell1IntValue = Dictionary.NOUN;
		} else if ("V".equals(cell1Value)) {
			cell1IntValue = Dictionary.VERB;
		} else if ("Adj".equals(cell1Value)) {
			cell1IntValue = Dictionary.ADJECTIVE;
		} else if ("Adv".equals(cell1Value)) {
			cell1IntValue = Dictionary.ADVERB;
		} else if ("Adj. comp".equals(cell1Value)) {
			cell1IntValue = Dictionary.ADJECTIVE_COMPARATIVE;
		} else {
			cell1IntValue = Dictionary.OTHER;
		}
		return cell1IntValue;
	}

	/**
	 * Giải mã WordClass
	 * @param cell1IntValue
	 * @return
	 */
	static String getStringType(int cell1IntValue) {
		String cell1Value;
		if (cell1IntValue == Dictionary.NOUN) {
			cell1Value = "N";
		} else if (cell1IntValue == Dictionary.VERB) {
			cell1Value = "V";
		} else if (cell1IntValue == Dictionary.ADJECTIVE) {
			cell1Value = "Adj";
		} else if (cell1IntValue == Dictionary.ADVERB) {
			cell1Value = "Adv";
		} else if (cell1IntValue == Dictionary.ADJECTIVE_COMPARATIVE) {
			cell1Value = "Adj. comp";
		} else {
			cell1Value = "Other";
		}
		return cell1Value;
	}

	/**
	 * Đọc từ điển theo nguồn xác định trong properties file
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Dictionary readGenExcelDict(boolean manySheet) throws FileNotFoundException, IOException, ClassNotFoundException {
//		if (Constants.READ_ORI_EXCEL_FILE) {
//			logger.log(Level.INFO, "ORI_EXCEL_FILE_NAME: {0}", Constants.ORI_EXCEL_FILE);
			return readOriExcelDict(Constants.ORI_EXCEL_FILE, manySheet);
//		} else if (Constants.READ_GEN_EXCEL_FILE) {
//			logger.log(Level.INFO, "GENERATED_EXCEL_FILE_NAME: {0}", Constants.GEN_EXCEL_FILE);
//			Workbook wb = FileUtil.readWorkBook(Constants.GEN_EXCEL_FILE);
//			return readGenExcelDict(wb);
//		} else {
//			return Dictionary.restore(Constants.SERIALED_EXCEL_FILE_NAME);
//		}
	}
	
	public Dictionary readOriExcelDict() throws FileNotFoundException, IOException, ClassNotFoundException {
		return oriDic;
	}

	/**
	 * Đọc từ điển đã được generate số nhiều, số ít, quá khứ
	 * @param wb
	 * @return
	 * @throws IOException
	 */
	private Dictionary readGenExcelDict(Workbook wb) throws IOException {
//		Dictionary dic = new Dictionary();
		Sheet sheet;
		Row row;
		int lastRowNum = 0;
		int numberOfSheets = wb.getNumberOfSheets();
		System.out.println("readGenExcelDict numberOfSheets " + numberOfSheets);
		for (int i = 0; i < numberOfSheets; i++) {
			sheet = wb.getSheetAt(i);
//			LOG.info("sheet.getSheetName(): " + sheet.getSheetName());
			lastRowNum = sheet.getLastRowNum();
			ComplexWordDef newWord = null;
			
			for (int j = 0; j <= lastRowNum; j++) {
				row = sheet.getRow(j);
				if (row != null) {
					newWord = null;
					Cell cell0 = row.getCell(0);
					Cell cell1 = row.getCell(1);
					Cell cell2 = row.getCell(2);
					if (cell0 != null && cell2 != null) {
						String cell2Value = cell2.getStringCellValue();
						String cell0Value = cell0.getStringCellValue();
						if (cell1 != null) {
							newWord = new ComplexWordDef(cell0Value).add(getIntType(cell1.getStringCellValue()), "", cell2Value);
						} else {
							newWord = new ComplexWordDef(cell0Value).add(Dictionary.OTHER, "", cell2Value);
						}
						genDic.addAppend(newWord);
//						LOG.info("newWord.toString(): " + newWord.toString());
					}
				}
			}
		}
//		logger.info("dic.toString(): " + dic.toString());
		logger.log(Level.INFO, "Total: {0} words", genDic.size());
		genDic.save(Constants.SERIALED_EXCEL_FILE_NAME);
		return genDic;
	}

	public static void writeWorkBook(Dictionary dic, String outputFile)
			throws FileNotFoundException, IOException {
		Workbook wb = DictionaryLoader.createDictionaryInNewWorkBook(dic);
		FileOutputStream fos = new FileOutputStream(outputFile);
		wb.write(fos);
		fos.flush();
		fos.close();
	}

	public static void writeNewWordsSheet(Dictionary dic, String outputFile, String sheetName) throws IOException {
		Log.d("writeNewWordsSheet", outputFile + ", " + sheetName);
		Workbook wb = DictionaryLoader.createDictionaryInNewWorkBook(dic, outputFile, sheetName);
		OutputStream fos = new BufferedOutputStream(new FileOutputStream(outputFile));
		wb.write(fos);
		wb.close();
		fos.flush();
		fos.close();
	}

	/**
	 * Lưu Dictionary vào Workbook
	 * @param dic
	 * @return,
	 */
	public static Workbook createDictionaryInNewWorkBook(Dictionary dic, String book, String sheetName) throws IOException {
		FileInputStream fis = new FileInputStream(book);
		POIFSFileSystem pOIFSFileSystem = new POIFSFileSystem(fis);
		HSSFWorkbook wb = new HSSFWorkbook(pOIFSFileSystem);
		HSSFSheet activeSheet = wb.getSheet(sheetName);

		int activeSheetIndex = wb.getSheetIndex(activeSheet);
		if (activeSheetIndex < 0) {
			activeSheet = wb.createSheet();
			activeSheet.setActive(true);
			wb.setSheetName(wb.getSheetIndex(activeSheet), sheetName);
		} else {
			wb.setActiveSheet(activeSheetIndex);
		}
		Row row;
		int i = activeSheet.getLastRowNum();
		if (i > 0) {
			i++;
		}
		Log.d("activeSheet.getLastRowNum()", "" + activeSheet.getLastRowNum());
		for (ComplexWordDef wd : dic) {
			for (WordClass wc : wd.getDefinitions()) {
				row = activeSheet.createRow(i++);

				row.createCell(0).setCellValue(wd.getName());
				row.createCell(1).setCellValue(getStringType(wc.getType()));
				row.createCell(2).setCellValue(wc.getDefinition());
			}
		}
		fis.close();
		return wb;
	}
	
	public static void writeWorkBooks(Dictionary dic, String outputFile)
			throws FileNotFoundException, IOException {
		Workbook wb = DictionaryLoader.createDictionaryInNewWorkBooks(dic);
		FileOutputStream fos = new FileOutputStream(outputFile);
		wb.write(fos);
		fos.flush();
		fos.close();
	}

	/**
	 * Lưu Dictionary vào Workbook
	 * @param dic
	 * @return
	 */
	public static Workbook createDictionaryInNewWorkBook(Dictionary dic) {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet activeSheet = wb.createSheet(Constants.MAIN_DICT_SHEET_NAME);

		int activeSheetIndex = wb.getSheetIndex(Constants.MAIN_DICT_SHEET_NAME);
		wb.setActiveSheet(activeSheetIndex);
		
//		Font font = wb.createFont();
//		font.setFontName(Constants.TIMES_NEW_ROMAN);

//		HSSFCellStyle style = wb.createCellStyle();
//        style.setFont(font);

//		activeSheet.setDefaultColumnStyle(0, style);
//		activeSheet.setDefaultColumnStyle(1, style);
//		activeSheet.setDefaultColumnStyle(2, style);
        
		Row row;
		int i = 0;
		for (ComplexWordDef wd : dic) {
			for (WordClass wc : wd.getDefinitions()) {

				row = activeSheet.createRow(i++);
				
				row.createCell(0).setCellValue(wd.getName());
				row.createCell(1).setCellValue(getStringType(wc.getType()));
				row.createCell(2).setCellValue(wc.getDefinition());
			}
		}
//		activeSheet.autoSizeColumn(0);
//		activeSheet.autoSizeColumn(1);
//		activeSheet.autoSizeColumn(2);
		return wb;
	}
	
	/**
	 * Lưu DictionaryReader vào Workbook trong trường hợp có nhiều sheet
	 * @param dic
	 * @return
	 */
	public static Workbook createDictionaryInNewWorkBooks(Dictionary dic) {
		HSSFWorkbook wb = new HSSFWorkbook();
		int numOfSheets = (dic.size() / 65535) + 1;

//		Font font = wb.createFont();
//		font.setFontName(Constants.TIMES_NEW_ROMAN);
//
//		HSSFCellStyle style = wb.createCellStyle();
//        style.setFont(font);

		for (int curSheet = 0; curSheet < numOfSheets; curSheet++) {
			Sheet activeSheet = wb.createSheet(Constants.MAIN_DICT_SHEET_NAME + "_" + curSheet);
			int activeSheetIndex = wb.getSheetIndex(Constants.MAIN_DICT_SHEET_NAME + "_" + curSheet);
			wb.setActiveSheet(activeSheetIndex);

//			activeSheet.setDefaultColumnStyle(0, style);
//			activeSheet.setDefaultColumnStyle(1, style);
//			activeSheet.setDefaultColumnStyle(2, style);
			
			Row row;
			int counter = 0;
			for (ComplexWordDef dw : dic) {
				for (WordClass wc : dw.getDefinitions()) {
					if (counter >= (65535 * curSheet) 
							&& counter < 65535 * (curSheet + 1)) {
						
						row = activeSheet.createRow(counter - 65535 * curSheet);

						row.createCell(0).setCellValue(dw.getName());
						row.createCell(1).setCellValue(getStringType(wc.getType()));
						row.createCell(2).setCellValue(wc.getDefinition());

						counter++;
					}
				}
			}
//			activeSheet.autoSizeColumn(0);
//			activeSheet.autoSizeColumn(1);
//			activeSheet.autoSizeColumn(2);
		}

		return wb;
	}

	/**
	 * Số mục từ
	 * @return
	 */
	public int size() {
		return genDic.size();
	}
	
	/**
	 * Check 1 mục từ xem có tồn tại trong Dictionary hay không
	 * @param dword
	 * @return
	 */
	public boolean contains(ComplexWordDef dword) {
		return genDic.contains(dword);
	}

	/**
	 * Thêm 1 mục từ vào Dictionary
	 * @param newWord
	 * @return
	 */
	public DictionaryLoader add(ComplexWordDef newWord) {
		if (newWord != null && genDic.contains(newWord)) {
			ComplexWordDef oldWord = genDic.floor(newWord);
			for (WordClass newWC : newWord.getDefinitions()) {
				boolean isDifferentWC = true;
				for (WordClass oldWC : oldWord.getDefinitions()) {
					if (oldWC.getType() == newWC.getType()) {
						isDifferentWC = false;
						oldWC.addDefinition(newWC.getDefinition());
					}
				}
				if (isDifferentWC) {
					oldWord.add(newWC);
				}
			}
		} else if (newWord != null) {
			genDic.add(newWord);
		}
		return this;
	}

	@Override
	public Iterator<ComplexWordDef> iterator() {
		return genDic.iterator();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////// Kinh Hoàng//////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// line	@line /lain/
	private static final Pattern NAME_PATTERN = Pattern.compile(
			"^([^@]+?)[\\p{Space}]*@([\\u0020-\\uFFFF]+?)$");

	// *  danh từ
	private static final Pattern TYPE_PATTERN = Pattern.compile(
			"^[\\p{Space}]*[\\\\*][\\p{Space}]*([\\u0020-\\uFFFF]+)$");

	// @line
	private static final Pattern ATSIGN_PATTERN = Pattern.compile(
			"^[\\p{Space}]*@(.+)$");

	// - (quân sự) tuyến, phòng tuyến
	private static final Pattern PROFESSION_PATTERN = Pattern.compile(
			"^[\\p{Space}]*-[\\p{Space}]*[\\(]([\\u0020-\\uFFFF]+?)[\\)]([\\u0020-\\uFFFF]*+)$");
	
	// !to bring into line [with]
	private static final Pattern REMOVE_BRACKET_PATTERN = Pattern.compile("[\\[\\]]");

	private static final Pattern REMOVE_BRACKET_CONTENT_PATTERN = Pattern.compile(" ?\\[[^\\]]*+\\] ?");
	
	private static final Pattern REMOVE_NESTED_PARENTHESES_CONTENT_PATTERN = Pattern.compile(
			" ??\\([\\u0020-\\uFFFF]+\\) ??");

	private static final Pattern REMOVE_CLOSED_PARENTHESES_CONTENT_PATTERN = Pattern.compile(
			"[\\u0020-\\uFFFF]+\\) ??");

	private static final Pattern REMOVE_PARENTHESES_CONTENT_PATTERN = Pattern.compile(
			" ??\\([\\u0020-\\uFFFF]+?\\) ??");

	private static final Pattern REMOVE_NGOAC_NHON_CONTENT_PATTERN = Pattern.compile(
			"<([\\u0020-\\uFFFF]+?)>");

	// - (xem) read
	private static final Pattern XEM_PATTERN = Pattern.compile("\\(xem\\)([\u0020-\uFFFF]+)");
	
	// - dây, dây thép
	private static final Pattern DEFINITION_PATTERN = Pattern.compile(
			"^[\\p{Space}]*-[\\p{Space}]*([\\u0020-\\uFFFF]+)$");
	
	// =line of sight+ đường ngắm (súng)
	private static final Pattern EXAMPLE_PATTERN = Pattern.compile(
			"^[\\p{Space}]*=[\\p{Space}]*([\\u0020-\\uFFFF]+?)\\+([\\u0020-\\uFFFF]+)$");
	
	// =to begin a new line+ xuống dòng
	// !to give someone line enough
	private static final Pattern TO_PATTERN = Pattern.compile(
			"^[\\p{Space}]*to[\\p{Space}]+([\\u0020-\\uFFFF]+?)$");
	
	// !in two twos
	private static final Pattern IDIOM_PATTERN = Pattern.compile(
			"^[\\p{Space}]*![\\p{Space}]*([\\u0020-\\uFFFF]+)$");

	private static final Pattern REMOVE_SONHIEU_PATTERN = Pattern.compile("số nhiều ([\\u0020-\\uFFFF]+)");

	private static final Pattern PLU_PATTERN = Pattern.compile("([^ ]+)[\\u0020-\\uFFFF]*");
	
	private static final Pattern PLUS_PRO_PATTERN = Pattern.compile("\\+( [^ \\)]+)");

	private static final Pattern WORD_PATTERN = Pattern.compile("([^ ,]+)([ ,]*)");
	private static final Pattern REMOVE_DANH_TU_PATTERN = Pattern.compile("danh từ[ ,;]*([\\u0020-\uFFFF]*)");
	private static final Pattern TWO_WORDS_PATTERN = Pattern.compile("^([^ ]+ +[^ ]+)");
	
	/**
	 * Đọc từ điển HNĐ original (dạng text file) vào DictionaryReader
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public DictionaryLoader readOriTextDictionary(String fileName)
			throws FileNotFoundException, IOException {

		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName), Constants.UTF8)); //, (int) new File(fileName).length());
	    
		logger.log(Level.INFO, "fileName: {0}", fileName);
//		DictionaryReader dr = new DictionaryReader();
		boolean isPluralNotChange = false;
		boolean noAddPluralMore = false;

		List<Entry<String, String>> pluralNounList = new LinkedList<Entry<String, String>>();
		List<Entry<String, String>> pluralNounDefinitionList = new LinkedList<Entry<String, String>>();
		List<Entry<String, String>> atsignIdiomList = new LinkedList<Entry<String, String>>();
		List<Entry<ComplexWordDef, String>> xemWaitingList = new LinkedList<Entry<ComplexWordDef, String>>();
		List<Entry<String, String>> toList = new LinkedList<Entry<String, String>>();
	
		int numWords = 0;
		for (int i = 0; in.ready(); i++) {
			numWords = i;
			String word = in.readLine();
			logger.log(Level.INFO, "dic[{0}]: {1}", new Object[]{i, word});
			if (i % 100 == 0) {
				System.out.println(i);
			}
			if (word == null) {
				break;
			}
			String[] defArr = word.split("\\\\n");
			ComplexWordDef currentDW = null;
			WordClass currentWC = null;
			noAddPluralMore = false;
			for (int j = 0; j < defArr.length; j++) {
				// System.gc();
				logger.log(Level.INFO, "def[{0}]: {1}", new Object[]{j, defArr[j]});
				if (j == 0) {
					// def[0]: line	@line /lain/
					Matcher matcher = NAME_PATTERN.matcher(defArr[j]);
					if (matcher.find()) {
						String names = matcher.group(1);
//						LOG.info("names: " + names);
						if (Util.isNotEmpty(names)) {
							String[] nameArr = names.split("=");
							// logger.log(Level.INFO, "names: {0}, length = {1}", new Object[]{names, nameArr.length});
							for (int k = 0; k < nameArr.length; k++) {
								String nameKTrimed = nameArr[k].trim();
								currentDW = new ComplexWordDef(nameKTrimed);
								add(currentDW);
								currentDW = genDic.floor(currentDW);	// trùng giữa example và new word
								if (k > 0) {
									String name0Trimed = nameArr[k-1].trim();
									if (nameKTrimed.compareTo(name0Trimed) >= 0) {
										pluralNounList.add(new Entry<String, String>(
												name0Trimed, nameKTrimed));
									} else {
										pluralNounList.add(new Entry<String, String>(
												nameKTrimed, name0Trimed));
									}
									// logger.log(Level.INFO, "nameArr[k].trim(): {0}", nameKTrimed);
									// logger.log(Level.INFO, "nameArr[0].trim(): {0}", name0Trimed);
								}
							}
							continue;
						} else {
							// logger.log(Level.INFO, "Incorrect word format: {0}{1}", new Object[]{defArr[j], NAME_PATTERN});
							break;
						}
					} else {
						// logger.log(Level.INFO, "Incorrect word format: {0}{1}", new Object[]{defArr[j], NAME_PATTERN});
						break;
					}
				}
				// def[1]: *  danh từ
				// *  danh từ,  số nhiều pence chỉ giá trị,  pennies chỉ số đồng tiền
				// *  danh từ số nhiều của zygoma
				// *  danh từ,  số nhiều không đổi
				// *  danh từ,  số nhiều calycex,  calyxes
				// * danh từ số nhiều calxes,  calces
				// *  danh từ số nhiều
				// * danh từ (số nhiều không thay đổi)
				// * danh từ số nhiều không đổi hoặc sherpas
				
				// remove danh tu hay danh tu,
				// check so nhieu cua
				// check so nhieu khong doi
				// remove so nhieu
				// split , lay [^ ]+ dau tien
				// TYPE_PATTERN = * xxxxx	"[\\p{Space}]*[\\\\*][\\p{Space}]*([\\u0020-\\uFFFF]+)"

				Matcher matType = TYPE_PATTERN.matcher(defArr[j]);
				if (matType.find()) {
					String type = matType.group(1);
					Matcher twoWordType = TWO_WORDS_PATTERN.matcher(type);
					if (type.startsWith("danh từ")) {
						currentWC = new WordClass(Dictionary.NOUN);
					} else if (type.startsWith("ngoại động từ")) {
						currentWC = new WordClass(Dictionary.VERB);
					} else if (type.startsWith("nội động từ")) {
						currentWC = new WordClass(Dictionary.VERB);
					} else if (type.startsWith("tính từ")) {
						currentWC = new WordClass(Dictionary.ADJECTIVE);
					} else if (type.startsWith("phó từ")) {
						currentWC = new WordClass(Dictionary.ADVERB);
					} else if (twoWordType.find()) {
//						currentWC = new WordClass(twoWordType.group(1));
						currentWC = new WordClass(Dictionary.OTHER);
					}
					currentDW.add(currentWC);
					// logger.log(Level.INFO, "type currentDW: {0}", currentDW);
					// logger.log(Level.INFO, "type: {0}", type);
					//REMOVE_SONHIEU_PATTERN = Pattern.compile("số nhiều ([\\u0020-\\uFFFF]+)");
					//PLU_PATTERN = Pattern.compile("([^ ]+)[\\u0020-\\uFFFF]*");
					isPluralNotChange = false;
					Matcher danhTuMat = REMOVE_DANH_TU_PATTERN.matcher(type);
					// bat dau xu ly rieng cho phan danh tu
					// [capt] []
					// [caps = capitals] [: Tech: [các chữ hoa]]
					// capt	@capt /kæpt/\n*  danh từ,  (viết tắt) của captain

					if (danhTuMat.find()) {
						// remain after remove danhtu la sonhieu
						String soNhieu = danhTuMat.group(1).trim();
						// check so nhieu cua
						if (soNhieu.indexOf("số nhiều của") >= 0) {
							noAddPluralMore = true;
						} else if (soNhieu.indexOf("số nhiều không đổi") >= 0) {
							isPluralNotChange = true;
						} else if (soNhieu.indexOf("viết tắt") >= 0) {
							String[] soNhieuArr = soNhieu.split(" ");
							for (int k = 0; k < soNhieuArr.length; k++) {
								// logger.log(Level.INFO, "soNhieuArr[{0}]: {1}", new Object[]{k, soNhieuArr[k]});
							}
							pluralNounList.add(new Entry<String, String>(
									currentDW.getName(), soNhieuArr[soNhieuArr.length - 1]));
						} else {
							Matcher removeSoNhieuMat = REMOVE_SONHIEU_PATTERN.matcher(type);
							if (removeSoNhieuMat.find()) {
								String soNhieuRemovedStr = removeSoNhieuMat.group(1).trim();
								String[] danhtuSoNhieuArr = soNhieuRemovedStr.split("[,;]|(\\bhay\\b)|(\\bhoặc\\b)");
								for (int k = 0; k < danhtuSoNhieuArr.length; k++) {
									Matcher firstWordMat = PLU_PATTERN.matcher(danhtuSoNhieuArr[k]);
									if (firstWordMat.find()) {
										String firstSoNhieu = firstWordMat.group(1);
										// logger.log(Level.INFO, "firstSoNhieu: {0}", firstSoNhieu);
//										if (!pluralNounList.contains(new Entry<String, String>(
//												firstSoNhieu, ""))) {
											pluralNounList.add(new Entry<String, String>(
													firstSoNhieu, currentDW.getName()));
//										}
									}
								}
								noAddPluralMore = true;
							} else if (!"số nhiều".equals(soNhieu)
									&& !noAddPluralMore) {
								String makeRegularPluralNounsForm = makeRegularPluralNounsForm(currentDW.getName());
								if (!pluralNounList.contains(new Entry<String, String>(
										makeRegularPluralNounsForm, ""))) {
									pluralNounList.add(new Entry<String, String>(
											makeRegularPluralNounsForm,
											currentDW.getName()));
								}
							}
						}
					}
					continue;
						
				}
				// in reply to...
				// def[5]: =to draw a line+ kẻ một đường
				// =to be a good (poor) knife and fork+ là một người ăn khoẻ (yếu)
				Matcher matExam = EXAMPLE_PATTERN.matcher(defArr[j]);
				if (matExam.find()) {
					String group1 = matExam.group(1);
					String exampleName = group1;
					// logger.log(Level.INFO, "exampleName origin: {0}", exampleName);
					String[] multiMeaning = exampleName.split(";");
					String definition = matExam.group(2);
					definition = removeContents(definition,
							REMOVE_PARENTHESES_CONTENT_PATTERN).toString().trim().replaceAll("\\.", "").replaceAll("  ", " ");
					// logger.log(Level.INFO, "definition: {0}", definition);
					if (multiMeaning.length == 1) {

//						exampleName = formatNameValueAsGroup1(TO_PATTERN,
//								exampleName);
//						exampleName = removeContents(exampleName,
//								REMOVE_PARENTHESES_CONTENT_PATTERN).toString()
//								.trim().replaceAll("\\.", "")
//								.replaceAll("  ", " ");
//
//						group1 = removeContents(group1,
//								REMOVE_PARENTHESES_CONTENT_PATTERN).toString()
//								.trim().replaceAll("\\.", "")
//								.replaceAll("  ", " ");

//						LOG.info("new examTmp: " + exampleName);
//						LOG.info("new defTmp: " + definition);
//
//						addMoreDerivedTo(
//								exampleName,
//								group1, definition,
//								dr);
						toList.add(new Entry<String, String>(exampleName, definition));
						
//						// xoa ngoac vuong
//						exampleName = removeContents(exampleName,
//								REMOVE_BRACKET_CONTENT_PATTERN).toString().replaceAll("  ", " ");
//
//						group1 = removeContents(group1,
//								REMOVE_BRACKET_CONTENT_PATTERN).toString().replaceAll("  ", " ");
//						addMoreDerivedTo(exampleName, group1, definition, dr);
					} else {
						for (int k = 0; k < multiMeaning.length; k++) {
//							exampleName = multiMeaning[k];
							
//							exampleName = formatNameValueAsGroup1(TO_PATTERN, exampleName);
//							exampleName = removeContents(exampleName,
//									REMOVE_PARENTHESES_CONTENT_PATTERN).toString().trim().replaceAll("\\.", "").replaceAll("  ", " ");
//							multiMeaning[k] = removeContents(multiMeaning[k],
//									REMOVE_PARENTHESES_CONTENT_PATTERN).toString().trim().replaceAll("\\.", "").replaceAll("  ", " ");
							
//							LOG.info("new exampleName: " + exampleName);
//							addMoreDerivedTo(
//									exampleName,
//									multiMeaning[k],
//									definition, dr);
							
							toList.add(new Entry<String, String>(multiMeaning[k], definition));

//							// xoa ngoac vuong
//							exampleName = removeContents(exampleName,
//									REMOVE_BRACKET_CONTENT_PATTERN).toString().replaceAll("  ", " ");
//							group1 = removeContents(multiMeaning[k],
//									REMOVE_BRACKET_CONTENT_PATTERN).toString().replaceAll("  ", " ");
				
//							addMoreDerivedTo(exampleName, multiMeaning[k], definition, dr);
						}
					}
					continue;
				}
				// def[46]: !on the line
				// 			- mập mờ ở giữa
				// !to plane away
				// !to plane down
				// - bào nhẵn
				// !not to know B from a bull's foot (brom a broom-stick, from a bufalo foot)
				// - không biết gì cả, dốt đặc cán mai
				Matcher matIdiom = IDIOM_PATTERN.matcher(defArr[j]);
				if (matIdiom.find()) {
					String oriIdiomWithoutParentheses = removeContents(matIdiom.group(1),
							REMOVE_PARENTHESES_CONTENT_PATTERN).toString().trim().replaceAll("\\.", "").replaceAll("  ", " ");
					String[] idiomArr = oriIdiomWithoutParentheses.split("[;]");
					
//					idiomName = removeContents(idiomName,
//							REMOVE_PARENTHESES_CONTENT_PATTERN).toString().trim().replaceAll("\\.", "").replaceAll("  ", " ");

					while (++j < defArr.length) {
						Matcher mat = DEFINITION_PATTERN.matcher(defArr[j]);
						if (mat.find()) {
							String definition = mat.group(1);
							// xử lý (xem) trong definition
							// - (xem) read
							Matcher matXem = XEM_PATTERN.matcher(definition);
							// logger.log(Level.INFO, "xem definition: {0}", definition);
							if (!matXem.find()) {
								definition = removeContents(definition,
										REMOVE_PARENTHESES_CONTENT_PATTERN)
										.toString().trim()
										.replaceAll("\\.", "")
										.replaceAll("  ", " ");
								// logger.log(Level.INFO, "idiom definition: {0}", definition);
								for (int k = 0; k < idiomArr.length; k++) {
//									String idiomName = idiomArr[k];
//									idiomName = formatNameValueAsGroup1(
//											TO_PATTERN, idiomName);
//									LOG.info("idiomName: " + idiomName);
//									addMoreDerivedTo(idiomName, idiomArr[k],
//											definition, dr);
									toList.add(new Entry<String, String>(idiomArr[k], definition));
								}
							}
						} else {
							break;
						}
					}
					j--;
					continue;
				}
				
				// def[138]: @in
				// def[139]: - trong, ở trong i. case trong trường hợp; i. fact thực vậy, thực ra; i. the
				// def[140]: - large, i. general nói chung; i. order to để; i. particular nói riêng, đặc
				// def[141]: - biệt; i. particularr nói riêng, đặc biệt; i. the small cục bộ
				// /tổng hợp in g. nói chung/cái chung/
				// ([ \w]+)*? (\w)\. ([ \w]+)*?([\u0020-\uFFFF]+?);

				Matcher atsignIdiom = ATSIGN_PATTERN.matcher(defArr[j]);
				if (atsignIdiom.find()) {
					String atsignIdiomName = atsignIdiom.group(1).trim();
					// logger.log(Level.INFO, "atsignIdiomName: {0}", atsignIdiomName);
					// logger.log(Level.INFO, "currentDW.getName(): {0}", currentDW.getName());
					if (atsignIdiomName.equalsIgnoreCase(currentDW.getName())) {
						if (j + 1 < defArr.length) {
							String definition = defArr[++j].trim().replaceFirst("^ *- *", "");//.concat(";");
							definition = removeContents(definition,
									REMOVE_NESTED_PARENTHESES_CONTENT_PATTERN).toString();
							add(new ComplexWordDef(currentDW.getName()).add(Dictionary.OTHER, "", definition));
							// logger.log(Level.INFO, "atsign Definition: {0}", definition);
						}

						while (++j < defArr.length && defArr[j].trim().length() > 0) {
							// logger.log(Level.INFO, "in atsign 2: [{0}] : {1}", new Object[]{atsignIdiomName, defArr[j]});
							atsignIdiomList.add(new Entry<String, String>(atsignIdiomName, defArr[j]));
						}
						j--;
					} else {
						while (++j < defArr.length
								&& !ATSIGN_PATTERN.matcher(defArr[j]).matches()
								&& defArr[j].trim().length() > 0) {
							// logger.log(Level.INFO, "in atsign 2: {0}", defArr[j]);
							String definition = defArr[j].trim().replaceFirst("^-[ ]?", "");
							definition = removeContents(definition,
									REMOVE_NESTED_PARENTHESES_CONTENT_PATTERN).toString();
							add(new ComplexWordDef(atsignIdiomName).add(Dictionary.OTHER, "", definition));
						}
						j--;
					}
					continue;
				}
				
				// def[20]: - (quân sự) tuyến, phòng tuyến
				// - ((thường) + out) do thám, dò xét, theo dõi\
				// - (+ down) lướt xuống (máy bay)
				// - (số nhiều không đổi) (hàng hải) tàu
				// dialysability	@dialysability\n* danh từ\n- (xem) dialysable chỉ tính chất
				// dialysable	@dialysable /'daiəlaizəbl/\n*  tính từ\n- (hoá học) có thể phân tách
				Matcher matPro = PROFESSION_PATTERN.matcher(defArr[j]);
				if (matPro.find()) {
					String pro = matPro.group(1);
					String definition = matPro.group(2);
					// logger.log(Level.INFO, "pro: {0}", pro);
					// logger.log(Level.INFO, "origin definition: {0}", definition);

					if (pro.indexOf("không dịch") >= 0) {
						continue;
					} else if ("xem".equals(pro)) {
						xemWaitingList.add(new Entry<ComplexWordDef, String>(
								new ComplexWordDef(currentDW.getName()),
								definition));
//						LOG.info("xemWaitingList: " + xemWaitingList.toString());
						continue;
					}
					
					definition = removeContents(definition,
							REMOVE_NESTED_PARENTHESES_CONTENT_PATTERN).toString().trim();
					definition = removeContents(definition,
							REMOVE_CLOSED_PARENTHESES_CONTENT_PATTERN).toString().trim();
					// logger.log(Level.INFO, "new definition: {0}", definition);
					
					if (definition == null || definition.trim().length() == 0) {
						if (j + 1 < defArr.length) {
							definition = defArr[++j];
							Matcher defMat = DEFINITION_PATTERN.matcher(definition);
							if (defMat.find()) {
								definition = defMat.group(1);
								definition = removeContents(definition,
										REMOVE_NESTED_PARENTHESES_CONTENT_PATTERN).toString().trim();
								definition = removeContents(definition,
										REMOVE_CLOSED_PARENTHESES_CONTENT_PATTERN).toString().trim();
							}
						}
					}
					
					if ("số nhiều".equals(pro)) {
						if (isPluralNotChange) {
							definition = "những|các " + definition;
						}
						// - (số nhiều) (chính trị) (the ins) Đảng đang nắm chính quyền
						pluralNounDefinitionList.add(new Entry<String, String>(currentDW.getName(), definition));
					} else {
						Matcher plusMat = PLUS_PRO_PATTERN.matcher(pro);
						if (pro.indexOf("số nhiều") >= 0) {
							if (isPluralNotChange || pro.indexOf("số nhiều không đổi") >= 0) {
								definition = "những|các " + definition;
							} else {
								pluralNounDefinitionList.add(new Entry<String, String>(currentDW.getName(), definition));
								continue;
							}
							// - (+ down) lướt xuống (máy bay)
						} else if (plusMat.find()) {
							add(new ComplexWordDef(currentDW.getName() + plusMat.group(1)).add(Dictionary.OTHER, "", definition));
							currentWC = null;
							continue;
						}
						WordClass tempWC = null;
						if (currentWC == null) {
							currentWC = new WordClass();
							currentWC.setProfession(pro);
							currentWC.addDefinition(definition);
							currentDW.add(currentWC);
						} else {
							tempWC = new WordClass(currentWC.getType());
							tempWC.setProfession(pro);
							tempWC.addDefinition(definition);
							currentDW.add(tempWC);
						}
					}
					continue;
				}

				// def[2]: - dây, dây thép
				//		   - <động> con xà cừ
				// inalterably	@inalterably\n- (xem) inalterable
				Matcher matDef = DEFINITION_PATTERN.matcher(defArr[j]);
				if (matDef.find()) {
					String defStr = matDef.group(1);
					if (currentWC == null) {
						currentWC = new WordClass();
					}
					
					StringBuffer sb = removeContents(defStr, REMOVE_PARENTHESES_CONTENT_PATTERN);
					sb = removeContents(sb.toString(), REMOVE_NGOAC_NHON_CONTENT_PATTERN);
					if (isPluralNotChange) {
						sb.insert(0, "những|các ");
					}
					currentWC.addDefinition(sb.toString().trim().replaceAll("\\.", "").replaceAll("  ", " "));
					currentDW.add(currentWC);
					// logger.log(Level.INFO, "definition defStr: {0}", defStr);
					// logger.log(Level.INFO, "currentWC: {0}", currentWC);
					// logger.log(Level.INFO, "currentDW: {0}", currentDW);
					continue;
				}
			}
		}
		in.close();
		// logger.log(Level.INFO, "number of words from origin dictionary: {0}", numWords);
		// logger.log(Level.INFO, "total words without plural nouns and verbs tenses: {0}", size());
		
		// logger.log(Level.INFO, "pluralNounDefinitionList: {0}", Util.collectionToStr(pluralNounDefinitionList));
		// logger.log(Level.INFO, "pluralNounList: {0}", Util.collectionToStr(pluralNounList));
		
		addPluralNounForms(pluralNounList);

		for (Entry<String, String> entry : pluralNounDefinitionList) {
			add(new ComplexWordDef(makeRegularPluralNounsForm(entry.getKey()))
			.add(Dictionary.NOUN, "", "những|các " + entry.getValue()));
		}

		// collection of verbs in the dictionary
		TreeSet<String> verbSet = addVerbForms();

		addVerbForms(toList, verbSet);
		

		// @in
		// - trong, ở trong
		// - i. case trong trường hợp;
		// - i. fact thực vậy, thực ra;
		// - i. the large, i. general nói chung;
		// [marginal classification sự] [: : [phân loại biên duyên]]
		// marginal c. sự phân loại biên duyên
		for (Entry<String, String> entry : atsignIdiomList) {
			// logger.log(Level.INFO, "entryAssign: {0}", entry);
			if (entry.getValue().indexOf(".") > 0) {
				String sb = entry.getValue().trim().replaceFirst(
						entry.getKey().charAt(0) + "\\.", entry.getKey()).substring(1).trim().replaceAll("  ", " ");
				// logger.log(Level.INFO, "sb: {0}", sb);
				StringBuffer sbIdiomName = new StringBuffer();
				StringBuffer sbIdiomDef = new StringBuffer();

				Matcher mat2 = WORD_PATTERN.matcher(sb);
				boolean notEmpty = false;
				while ((notEmpty = mat2.find())) {
					// logger.log(Level.INFO, "mat2.group(1): {0}", mat2.group(1));
					// logger.log(Level.INFO, "mat2.group(2): {0}", mat2.group(2));
					if (genDic.contains(new ComplexWordDef(mat2.group(1)))) {
						sbIdiomName.append(mat2.group(1)).append(mat2.group(2));
					} else {
						break;
					}
				}
				// entryAssign: [space]: [- null s.]
				if (notEmpty) {
					sbIdiomDef.append(mat2.group(1)).append(mat2.group(2)).append(sb.substring(mat2.end()));
				} else {
					// logger.log(Level.INFO, "entry of origin dictionary error: {0}", entry);
				}
				// logger.log(Level.INFO, "sbIdiomName: {0}", sbIdiomName);
				// logger.log(Level.INFO, "sbIdiomDef: {0}", sbIdiomDef);
				String[] idiomArr = sbIdiomName.toString().trim().split("[,;]");
				for (String idiom : idiomArr) {
					if (Util.isNotEmpty(idiom)) {
						add(new ComplexWordDef(idiom).add(Dictionary.OTHER, "", 
								removeContents(sbIdiomDef.toString(), REMOVE_PARENTHESES_CONTENT_PATTERN).toString()));
					}
				}
			} else {
				add(new ComplexWordDef(entry.getKey()).add(Dictionary.OTHER, "", 
						removeContents(entry.getValue().trim().replaceFirst("^ *- *", ""), REMOVE_PARENTHESES_CONTENT_PATTERN).toString()));
			}
		}
		
		for (Entry<ComplexWordDef, String> entry : xemWaitingList) {
			if (contains(new ComplexWordDef(entry.getValue()))) {
				ComplexWordDef wordHasDef = genDic.floor(new ComplexWordDef(entry
						.getValue()));
				ComplexWordDef wordNotHasDef = genDic.floor(entry.getKey());
				for (WordClass wc : wordHasDef.getDefinitions()) {
					wordNotHasDef.add(wc);
				}
			}
		}
		
		// logger.log(Level.INFO, "total words: {0}", size());
		return this;
	}

	/**
	 * Add các dạng verb của example vào dictionary
	 * @param toList
	 * @param verbSet
	 */
	private void addVerbForms(List<Entry<String, String>> toList, TreeSet<String> verbSet) {
		for (Entry<String, String> entry : toList) {
			String idiomExampleName = formatNameValueAsGroup1(TO_PATTERN, entry.getKey());
			// logger.log(Level.INFO, "idiomExampleName: {0}", idiomExampleName);
			addMoreDerivedTo(idiomExampleName, entry.getKey(), entry.getValue(), verbSet);
		}
	}

	/**
	 * Thêm các dạng quá khứ, qkpt vào verbs
	 */
	private TreeSet<String> addVerbForms() {
		boolean isAddMoreRegularVerb = false;
		boolean isAddMoreIrregularVerb = false;
		List<ComplexWordDef> setTmp = new LinkedList<ComplexWordDef>();
		List<ComplexWordDef> irregularVerbs = new LinkedList<ComplexWordDef>();
		TreeSet<String> verbSet = new TreeSet<String>();
		for (ComplexWordDef dw : genDic) {
			isAddMoreRegularVerb = false;
			isAddMoreIrregularVerb = false;
			ComplexWordDef dwS = new ComplexWordDef();
			ComplexWordDef dwIng = new ComplexWordDef();
			ComplexWordDef dwEd = new ComplexWordDef();
			for (WordClass wc : dw.getDefinitions()) {
				if (wc.getType() == Dictionary.VERB) {
					verbSet.add(dw.getName());
					// logger.info("dw.getName(): " + dw.getName());
					if (!IRREGULAR_VERBS.contains(new Entry<String, Set<String>>(dw.getName(), null))) {
						isAddMoreRegularVerb = true;
						
						StringBuilder sbS = new StringBuilder();
						StringBuilder sbED = new StringBuilder();
						StringBuilder sbING = new StringBuilder();

						makeVerbForms(dw.getName(), sbS, sbING, sbED);
						// logger.log(Level.INFO, "dw.getName(): {0}", dw.getName());
						// logger.log(Level.INFO, "sbS: {0}", sbS);
						// logger.log(Level.INFO, "sbING: {0}", sbING);
						// logger.log(Level.INFO, "sbED: {0}", sbED);

						dwS.setName(sbS.toString());
						dwS.add(wc);

						dwIng.setName(sbING.toString());
						dwIng.add(wc);

						dwEd.setName(sbED.toString());
						dwEd.add(wc);
					} else {
						isAddMoreIrregularVerb = true;
						
						Entry<String, List<String>> entry = IRREGULAR_VERBS
								.floor(new Entry<String, List<String>>(dw.getName(), null));
						int counter = 0;
						for (String irregularVerb : entry.getValue()) {
							// logger.info("irregularVerb: " + irregularVerb);
							String[] split = irregularVerb.split("/");
							for (int i = 0; i < split.length; i++) {
								if (counter == 0) {
									irregularVerbs.add(new ComplexWordDef(split[i]).add(wc.setDefinition("đã " + wc.getDefinition())));
								} else if (counter == 3) {
									irregularVerbs.add(new ComplexWordDef(split[i]).add(wc.setDefinition("đang " + wc.getDefinition())));
								} else {
									irregularVerbs.add(new ComplexWordDef(split[i]).add(wc));
								}
								
							}
							counter++;
						}
					}
				}
			}
			if (isAddMoreRegularVerb) {
				setTmp.add(dwS);
				setTmp.add(dwIng);
				setTmp.add(dwEd);
			} else if (isAddMoreIrregularVerb) {
				setTmp.addAll(irregularVerbs);
			}
		}
		
		for (ComplexWordDef complexWordDef : setTmp) {
			if (genDic.contains(complexWordDef)) {
				ComplexWordDef floor = genDic.floor(complexWordDef);
				for(WordClass wc : complexWordDef.getDefinitions()) {
					floor.add(wc);
				}
			} else {
				add(complexWordDef);
			}
		}
		return verbSet;
	}

	/**
	 * Xóa nội dung của String defStr theo Pattern pat
	 * @param defStr
	 * @param pat
	 * @return
	 */
	private static StringBuffer removeContents(String defStr, Pattern pat) {
		Matcher mat = pat.matcher(defStr);
		StringBuffer sb = new StringBuffer();
		while (mat.find()) {
			mat.appendReplacement(sb, " ");
		}
		mat.appendTail(sb);
		return sb;
	}

	/**
	 * Thêm các quá khứ, qkpt vào các ví dụ có "to" ở đầu câu
	 * @param removedTo
	 * @param group1
	 * @param definition
	 * @param verbSet
	 */
	private void addMoreDerivedTo(String removedTo, String group1,
			String definition, TreeSet<String> verbSet) {
		// logger.log(Level.INFO, "removedTo: {0}", removedTo);
		// logger.log(Level.INFO, "group1: {0}", group1);
		int indexOfSpace = removedTo.indexOf(" ");
		if (indexOfSpace > 0) {
		// logger.log(Level.INFO, "verbSet.contains(removedTo.substring(0, removedTo.indexOf(\" \"))): {0}", verbSet.contains(removedTo.substring(0, indexOfSpace)));
		// logger.log(Level.INFO, "removedTo.equals(group1) && removedTo.indexOf(\" \") >= 0 {0}", (removedTo.equals(group1) && indexOfSpace > 0));
		}
		// khac nhau va la irregular
		if ((!removedTo.equals(group1) && indexOfSpace > 0 && IRREGULAR_VERBS
				.contains(new Entry<String, Set<String>>(removedTo.substring(0, indexOfSpace), null)))
				|| (!removedTo.equals(group1) && indexOfSpace < 0 && IRREGULAR_VERBS
						.contains(new Entry<String, Set<String>>(removedTo, null)))) {
			String idiomTmp = REMOVE_BRACKET_PATTERN.matcher(removedTo).replaceAll("").replaceAll("  ", " ");
			idiomTmp = removeContents(idiomTmp,
					REMOVE_PARENTHESES_CONTENT_PATTERN).toString().replaceAll("  ", " ");
			addForIrregularVerbs(idiomTmp, definition);
			
			StringBuffer sb = removeContents(removedTo, REMOVE_BRACKET_CONTENT_PATTERN);
			if (sb.toString().length() > 0) {
				addForIrregularVerbs(sb.toString().trim().replaceAll("  ", " "), definition);
			}
		} else if ((!removedTo.equals(group1) && indexOfSpace > 0 && verbSet
				.contains(removedTo.substring(0, indexOfSpace)))
				|| (!removedTo.equals(group1) && indexOfSpace < 0 && verbSet
						.contains(removedTo))) {
			// khac nhau nhung la regular verbs
			String idiomTmp = REMOVE_BRACKET_PATTERN.matcher(removedTo).replaceAll("").replaceAll("  ", " ");
			idiomTmp = removeContents(idiomTmp,
					REMOVE_PARENTHESES_CONTENT_PATTERN).toString().replaceAll("  ", " ");
			addForRegularVerbs(idiomTmp, definition);
			
			StringBuffer sb = removeContents(removedTo, REMOVE_BRACKET_CONTENT_PATTERN);
			if (sb.toString().length() > 0) {
				addForRegularVerbs(sb.toString().trim().replaceAll("  ", " "), definition);
			}
		} else if (removedTo.equals(group1) && indexOfSpace >= 0) {
			// giong nhau ma khong co to
			String idiomTmp = REMOVE_BRACKET_PATTERN.matcher(removedTo).replaceAll("").replaceAll("  ", " ");
			idiomTmp = removeContents(idiomTmp,
					REMOVE_PARENTHESES_CONTENT_PATTERN).toString().replaceAll("  ", " ");
			// logger.log(Level.INFO, "no verb idiomTmp: {0}", idiomTmp);
			ComplexWordDef newDW = new ComplexWordDef(idiomTmp);
			newDW.add(Dictionary.OTHER, "", definition);
			add(newDW);
			
			String st = removeContents(removedTo.trim(), REMOVE_BRACKET_CONTENT_PATTERN).toString();
			st = st.replaceAll("[\\(\\)]", "");
			if (st.length() > 0) {
				newDW = new ComplexWordDef(st.replaceAll("  ", " "));
				newDW.add(Dictionary.OTHER, "", definition);
				add(newDW);
			}
		} else {
			// giong nhau ma co to
			ComplexWordDef newDW = new ComplexWordDef(group1);
			newDW.add(Dictionary.OTHER, "", definition);
			add(newDW);
		}
	}

	/**
	 * Thêm định nghĩa cho các động từ bất quy tắc quá khứ, hiện tại tiếp diễn...
	 * @param verbPhrase
	 * @param definition
	 */
	private void addForIrregularVerbs(String verbPhrase, String definition) {
		int index = verbPhrase.indexOf(" ");
		String verb = index > 0 ? verbPhrase.substring(0, index) : verbPhrase;
		Entry<String, List<String>> floor = IRREGULAR_VERBS
				.floor(new Entry<String, List<String>>(verb, null));
//		for (Entry<String, Set<String>> entry : IRREGULAR_VERBS) {
			if (verb.equalsIgnoreCase(floor.getKey())) {
				int counter = 0;
				for (String value : floor.getValue()) {
					String[] splitValues = value.split("/");
//					Arrays.sort(splitValues);
					ComplexWordDef newDW = new ComplexWordDef(value 
							+ ((index > 0) ? verbPhrase.substring(index) : ""));
					// logger.info("newDW: " + newDW);
					for (int i = 0; i < splitValues.length; i++) {
						if (counter == 0) {
							newDW.add(Dictionary.VERB, "", "đã " + definition);
						} else if (counter == 3) {
							newDW.add(Dictionary.VERB, "", "đang " + definition);
						} else {
							newDW.add(Dictionary.VERB, "", definition);
						}
					}
					add(newDW);
					counter++;
				}

			}
//		}
	}

	/**
	 * 
	 * @param pattern
	 * @param name
	 * @return
	 */
	private static String formatNameValueAsGroup1(Pattern pattern, String name) {
		Matcher mat = pattern.matcher(name);
		if (mat.find()) {
			name = mat.group(1);
		}
		return name;
	}

	/**
	 * Đọc file đã format, serialize nó, return DictionaryReader
	 * @param dictAnhVietName
	 * @return
	 * @throws IOException
	 */
	public static Dictionary readNewDictionary(String dictAnhVietName)
			throws IOException {
		DictionaryLoader dicLoader = new DictionaryLoader();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dictAnhVietName)));
//		String wholeFile = FileUtil.readPlainTextFile(dictAnhVietName);
//		String[] wordDefs = wholeFile.split("[\r\n]+");
//		for (int i = 0; i < wordDefs.length; i++) {
		while (br.ready()) {
//			int indexOfTab = wordDefs[i].indexOf("\t");
//			ComplexWordDef dw = new ComplexWordDef(wordDefs[i].substring(0, indexOfTab));
//			dw.add("", "", wordDefs[i].substring(indexOfTab + 1));
			String line = br.readLine();
			// logger.info("line: " + line);
			int indexOfTab1 = line.indexOf("\t");
			int indexOfTab2 = line.indexOf("\t", indexOfTab1 + 1);
			ComplexWordDef dw = new ComplexWordDef(line.substring(0, indexOfTab1));
			String type = line.substring(indexOfTab1 + 1, indexOfTab2);
			
			dw.add(getIntType(type), "", line.substring(indexOfTab2 + 1));
			dicLoader.add(dw);
//			System.out.println("new dw: " + dw.toString());
		}
//		System.out.println("dicReader.getDictionary().toString(): " + dicReader.getDictionary().toString());
		br.close();
//		dicLoader.genDic.save(Constants.SERIALED_PLAIN_FILE_NAME);
		return dicLoader.genDic;
	}
	/**
	 * Đọc file text từ điển HNĐ vào DictionaryReader
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public DictionaryLoader readOriTextDictionary() throws FileNotFoundException, IOException {
		return readOriTextDictionary(Constants.DICT_ANH_VIET_NAME);
	}

	/**
	 * Đọc file từ điển HNĐ đã format lại
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Dictionary readNewTextDictionary()
			throws FileNotFoundException, IOException {
		return readNewDictionary(Constants.NEW_DICT_ANH_VIET_NAME);
	}

	/**
	 * Lưu DictionaryReader vào file từ điển mới có WordClass
	 * @param dicReader
	 * @param outFile
	 * @throws IOException
	 */
	public static void createDictionaryInPlainTextFile(
		DictionaryLoader dicReader, String outFile) throws IOException {
		FileWriter fw = new FileWriter(outFile);
		BufferedWriter bw = new BufferedWriter(fw);
		for (ComplexWordDef dw : dicReader) {
			for (WordClass wc : dw.getDefinitions()) {
				String definition = wc.getDefinition().trim();
				if (definition.length() > 0) {
					bw.write(dw.getName());
					bw.write("\t");
					bw.write(getStringType(wc.getType()));
					bw.write("\t");
					bw.write(definition);
					bw.write("\r\n");
				}
			}
		}
		bw.flush();
		fw.flush();
		bw.close();
		fw.close();
	}

	/**
	 * Hàm utility lấy danh sách số nhiều bất quy tắc trong từ điển HNĐ original
	 */
	private static final Pattern PLURAL_NOUN_PATTERN = Pattern.compile("@(.+?)[\\\\|/].+?số nhiều (.+?)\\\\n");
	public static Map<String, String[]> readIrregularPluralNounFromTextDict(String fileName) throws IOException {
		Map<String, String[]> map = new TreeMap<String, String[]>();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName), Constants.UTF8), (int) new File(fileName).length());
		while (in.ready()) {
			String line = in.readLine();
			Matcher mat = PLURAL_NOUN_PATTERN.matcher(line);
			if (mat.find()) {
				String[] pluralNouns = mat.group(2).trim().replaceAll(", +", ",").split("[,;]");
				map.put(mat.group(1).trim(), pluralNouns);
			}
		}
		return map;
	}

	/**
	 * Hàm utility lưu danh từ số nhiều bất quy tắc của từ điển HNĐ original vào file
	 * @param fileName
	 * @param map
	 * @throws IOException
	 */
	public static void writePluralNounToTextFile(String fileName, Map<String, String[]> map) throws IOException {
		FileWriter fw = new FileWriter(fileName);
		for (Map.Entry<String, String[]> entry : map.entrySet()) {
			fw.write(entry.getKey());
			String[] value = entry.getValue();
			for (int i = 0; i < value.length; i++) {
				fw.write("/");
				fw.write(value[i]);
			}
			fw.write("\r\n");
		}
		fw.flush();
		fw.close();
	}
	

	@Override
	public String toString() {
		return Util.iteratorToString(genDic.iterator());
	}
	
	/**
	 * Lưu Dictionary vào file, mỗi item là 1 dòng trong file
	 * @param file
	 * @throws IOException
	 */
	public void writeToFile(File file) throws IOException {
		FileUtil.writeToFile(genDic.iterator(), file);
	}
	
	/**
	 * Lưu DictionaryReader vào file excel
	 * @param fileName
	 * @throws IOException
	 */
	public void writeToExcelFile(String fileName) throws IOException {
		writeWorkBooks(genDic, fileName);
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
//		Dictionary dic = new DictionaryReader().readExcelDictionary(ORI_EXCEL_FILE_NAME);
//		Workbook wb = createDictionaryInNewWorkBook(dic);
//		writeWorkBook(wb, NEW_EXCEL_FILE_NAME);

		long start =  System.currentTimeMillis();
		DictionaryLoader dr = new DictionaryLoader().readOriTextDictionary(Constants.DICT_ANH_VIET_NAME);
//		System.out.println("Duration1: " + (System.currentTimeMillis() - start));
//		dr.writeToFile(new File(Constants.DICT_ANH_VIET_CONVERTED));
		System.out.println("Duration2: " + (System.currentTimeMillis() - start));
		createDictionaryInPlainTextFile(dr, Constants.NEW_DICT_ANH_VIET_NAME);
//		System.out.println("Duration3: " + (System.currentTimeMillis() - start));
		
//		DictionaryReader dr = new DictionaryReader().readOriTextDictionary(Constants.DICT_ANH_VIET_TEST_NAME);
//		dr.writeToFile(new File(Constants.TEMP_TEXT_FILE_NAME));
//		createDictionaryInPlainTextFile(dr, Constants.TEMP_DICT_ANH_VIET_NAME);
		
		System.out.println(makeRegularPluralNounsForm("abbess"));
		System.out.println(makeRegularPluralNounsForm("vertex"));
		System.out.println(makeRegularPluralNounsForm("eighteens"));
		Map<String, String[]> map = readIrregularPluralNoun(new File(Constants.PRIVATE_PATH + "/data/Irregular-Plural-Nouns.txt").getAbsolutePath());
		System.out.println(map.get("index")[0]);
		System.out.println(map.get("index")[1]);
		
		Map<String, String[]> map2 = readIrregularPluralNounFromTextDict(new File(Constants.PRIVATE_PATH + "/data/dictd_anh-viet.txt").getAbsolutePath());
		writePluralNounToTextFile(new File("dicts-parsers/Irregular-Plural-Nouns.txt").getAbsolutePath(), map2);
		
//		writeWorkBook(dr, NEW_EXCEL_FILE_NAME);
//		String strTemp = "acidophilous	@acidophilous\\n* tính từ\\n- (sinh học) ưa axit; ưa chua; mọc ở đất chua";
//		String strTemp = "two	@two /tu:/\\n*  tính từ\\n- hai, đôi\\n=he is two+ nó lên hai\\n*  danh từ\\n- số hai\\n- đôi, cặp\\n=in twos; two and two; two by two+ từng đôi một, từng cặp một\\n=one or two+ một vài\\n- quân hai (quân bài); con hai (súc sắc...)\\n!in two twos\\n- trong nháy mắt, chỉ trong một loáng\\n!to put two and two together\\n- (xem) put\\n\\n@two\\n- hai (2); một cặp, một đôi";
//		DictionaryReader dr2 = readDictionary(Translator.DICT_ANH_VIET_NAME);
//		DictionaryLoader.writeWorkBook(dr2, DictionaryLoader.NEW_EXCEL_FILE_NAME);
//		createDictionaryInPlainTextFile(dr2, Translator.NEW_DICT_ANH_VIET_NAME);
//		LOG.info(dr2);
//		DictionaryReader dr3 = readNewDictionary(Translator.NEW_DICT_ANH_VIET_NAME);
//		Dictionary dic = dr3.convertToDictionary();
//		LOG.info(dic);
		
//		Pattern pat = Pattern.compile("([\\u0020-\\uFFFF]*?)[\\[]([^\\]]*+)[\\]]([\\u0020-\\uFFFF]*?)");
//		String idiomName = "bring into line [with]";
//		Matcher matBracketIdiom = pat.matcher(idiomName);
//		LOG.info("MULTI_IDOMS_PATTERN: " + MULTI_IDOMS_PATTERN);
//		LOG.info("idiomName: " + idiomName);
//		StringBuilder sb = new StringBuilder(idiomName);
//		while (matBracketIdiom.find()) {
//			sb = new StringBuilder(matBracketIdiom.group(1).trim());
//			sb.append(matBracketIdiom.group(3).trim());
//		}
//		LOG.info(sb.toString());
//		DictionaryReader dr3 = new DictionaryReader();
//		DictionaryWord dw3 = new DictionaryWord();
//		dr3.add(dw3);
//		WordClass wc3 = new WordClass("danh từ").addDefinition("hello");
//		dw3.add(wc3);
//		LOG.info(dr3);
	}
}
