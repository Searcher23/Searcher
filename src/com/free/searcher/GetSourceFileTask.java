package com.free.searcher;

import android.os.*;
import java.util.*;
import java.io.*;
import android.util.*;
import java.net.*;
import org.apache.tika.metadata.*;
import org.apache.poi.hpbf.extractor.*;
import org.apache.poi.hdgf.extractor.*;
import org.apache.poi.hslf.extractor.*;
import org.apache.poi.hssf.extractor.*;
import org.apache.poi.poifs.filesystem.*;
import com.free.p7zip.*;
import android.webkit.*;
import org.apache.tika.sax.*;
import org.xml.sax.*;

public class GetSourceFileTask extends AsyncTask<Void, String, String> {
	String resultStr = "Nothing to load";
//		int totalFileSizeRead = 0;
	// folder đã chọn gồm cả text lẫn misc
	private List<File> initFolderFiles = new LinkedList<File>();
	// sau khi đã filter folder và file và chỉ chọn theo suffix
	private List<File> readTextFiles = new LinkedList<File>();
	public volatile List<File> convertedFileList;
//		volatile Collection<String> entryFileList = new HashSet<String>();
	public long totalSelectedSize = 0;
	
	private MainFragment s;

	public GetSourceFileTask(MainFragment s) {
		this.s = s;
	}

	protected String doInBackground(Void... urls) {
		// int count = urls.length;
		// long totalSize = 0;
		totalSelectedSize = 0;
		synchronized (MainFragment.PRIVATE_PATH) {
			Log.d("lock start", MainFragment.df.format(System.currentTimeMillis()));
			s.currentContent = "";
			s.contentLower = "";

			Log.d("s.selectedFiles", Util.arrayToString(s.selectedFiles, true, MainFragment.LINE_SEP));
			convertedFileList = new LinkedList<File>();
			File f;
			errorProcessFiles = new StringBuilder(MainFragment.TITLE_ERROR_PROCESSING_FILES);
			initFolderFiles = new LinkedList<File>();
			readTextFiles = new LinkedList<File>();
			for (int i = 0; i < s.selectedFiles.length; i++) {
				f = new File(s.selectedFiles[i]);
				// lấy hết file trong các thư mục con lưu vào initFolderFiles
				// bất kể file đó có tồn tại hay ko
				// lấy hết file trong các thư mục con lưu vào fList chỉ những
				// file có tồn tại
				if (f.exists()) {
					List<File> listFilesByName = FileUtil.listFilesByName(f);
					initFolderFiles.addAll(listFilesByName);
					// initFolderFiles.append(f.getAbsolutePath() + ": " +
					// f.length() + " bytes\r\n");
					Log.d("f", f.getAbsolutePath() + ": " + f.length() + " bytes");
					convertedFileList.addAll(listFilesByName);
				} else {
					initFolderFiles.add(f);
				}
			}
			// chỉ lấy hết các file thực sự tồn tại
			s.files = new File[convertedFileList.size()];
			s.files = convertedFileList.toArray(s.files);
			Log.d("getSourceFile fList", Util.collectionToString(convertedFileList, true, MainFragment.LINE_SEP));

			s.cache = null;
			//convertedFileList = FileUtil.getFiles(files, SEARCH_FILES_SUFFIX);
			Log.d("getSourceFile filtered", Util.collectionToString(convertedFileList, true, MainFragment.LINE_SEP));

			try {
				long milliTime = System.currentTimeMillis();
				// list file converted
				convertedFileList = readFile(convertedFileList);
				Log.d("fileList", Util.collectionToString(convertedFileList, true, MainFragment.LINE_SEP));
				publishProgress("Free memory = " + MainFragment.nf.format(Runtime.getRuntime().freeMemory()) 
								+ " bytes. Caching...");
				s.cache = new Cache(convertedFileList);

				resultStr = new StringBuilder("Processed ")
					.append(MainFragment.nf.format(convertedFileList.size()))
					.append(" files, cached ").append(MainFragment.nf.format(s.cache.cached()))
					.append(" files, cached size ").append(MainFragment.nf.format(s.cache.getCurrentSize()))
					.append("/").append(MainFragment.nf.format(s.cache.getTotalSize()))
					.append(" bytes, took: ")
					.append(MainFragment.nf.format(System.currentTimeMillis() - milliTime))
					.append(" milliseconds. Current free memory ")
					.append(MainFragment.nf.format(Runtime.getRuntime().freeMemory()))
					.append(" bytes.").toString();
				publishProgress(resultStr);

				s.files = new File[convertedFileList.size()];
				convertedFileList.toArray(s.files);

				if (s.files.length == 1 && s.files[0].isFile()) {
					Log.d("getSourceFile isFile", s.files[0].toString());
					if (s.cache.hasNext()) {
						// Map.Entry<File, Map.Entry<String, String>> entry = s.cache.next(); // for DoubleCache
						f = s.cache.next();
						// s.currentContent = entry.getValue().getKey(); // for DoubleCache
						// s.contentLower = entry.getValue().getValue(); // for DoubleCache
						s.currentContent = s.cache.get(f);
						s.contentLower = s.currentContent.toLowerCase();
						Log.d("s.currentContent", s.currentContent);
					}
					// không có lỗi
					if (errorProcessFiles.length() == MainFragment.TITLE_ERROR_PROCESSING_FILES.length()) {
						s.currentUrl = s.files[0].toURI().toURL().toString();
						// (webTask = new WebTask(webView, displayData)).execute();
						Log.d("s.currentUrl 1", s.currentUrl);
					} else {
						// có lỗi
						s.currentUrl = new StringBuilder(MainFragment.EMPTY_HEAD).append(
							"Chosen files: <br/>").append(
							filesToHref(initFolderFiles)).append(
							errorProcessFiles).append(
							(readTextFiles.size() > 0) ? new StringBuilder("<br/> Converted files: <br/>").append(filesToHref(readTextFiles)) : "").append(
							MainFragment.END_BODY_HTML).toString();
						// displayData = loadUrl(webView, displayData);
						Log.d("s.currentUrl 2", s.currentUrl);
					}
				} else if (s.files.length > 0) {
					Log.d("getSourceFile files.length", "" + s.files.length);
					s.currentContent = "";
					s.contentLower = "";
					readTextFiles = convertedFileList;
					if (errorProcessFiles.length() > MainFragment.TITLE_ERROR_PROCESSING_FILES.length()) {
						s.currentUrl = new StringBuilder(MainFragment.EMPTY_HEAD).append(
							"Chosen files: <br/>").append(
							filesToHref(initFolderFiles)).append( // "Converted succesfully <br />"
							errorProcessFiles).append(
							(readTextFiles.size() > 0) ? new StringBuilder("<br/> Converted files: <br/>").append(filesToHref(readTextFiles)) : "").append( // filesToHref(readTextFiles)
							MainFragment.END_BODY_HTML).toString();
						// displayData = loadUrl(webView, displayData);
					} else {
						s.currentUrl = new StringBuilder(MainFragment.EMPTY_HEAD).append(
							"Chosen files: <br/>").append(
							filesToHref(initFolderFiles)).append( // "Converted succesfully <br />"
							((readTextFiles.size() > 0) ? new StringBuilder("<br/> Converted files: <br/>").append(filesToHref(readTextFiles)) : "")).append( // filesToHref(readTextFiles)
							MainFragment.END_BODY_HTML).toString();
						// displayData = loadUrl(webView, displayData);
					}
					Log.d("s.currentUrl 3", s.currentUrl);
				} else if (s.files.length == 0) {
					s.currentUrl = new StringBuilder(MainFragment.EMPTY_HEAD).append(
						"Chosen files: <br/>").append(
						filesToHref(initFolderFiles)).append(
						errorProcessFiles).append(
						(readTextFiles.size() > 0) ? new StringBuilder("<br/> Converted files: <br/>").append(filesToHref(readTextFiles)) : "").append(
						MainFragment.END_BODY_HTML).toString();
//					resultStr = "Nothing to load";
				}
			} catch (MalformedURLException e) {
				publishProgress(e.getMessage());
				Log.e("getSourceFile", e.getMessage(), e);
			} catch (IOException e) {
				publishProgress(e.getMessage());
				Log.e("getSourceFile", e.getMessage(), e);
			} catch (Throwable e) {
				publishProgress(e.getMessage());
//				publishProgress("Current memory is " + SearchFragment.nf.format(Runtime.getRuntime().freeMemory())
//								+ ", max memory is " + SearchFragment.nf.format(Runtime.getRuntime().maxMemory()));
				Log.e("SearchTask", e.getMessage(), e);
			}
		}
		return resultStr;
	}

	protected void onProgressUpdate(String... progress) {
		if (progress != null && progress.length > 0 
			&& progress[0] != null && progress[0].trim().length() > 0) {
			s.statusView.setText(progress[0]);
			Log.d("GetTask.publish", progress[0]);
		}
	}

	private String filesToHref(List<File> initFolderFiles) throws MalformedURLException {
		StringBuilder sb = new StringBuilder();
		if (initFolderFiles != null) {
			int counter = 0;
			for (File file : initFolderFiles) {
				sb.append(++counter).append(": <a href=\"")
					.append(file.toURI().toURL().toString()).append("\">")
					.append(file.toString()).append("</a><br/>\r\n");
			}
		}
		return sb.toString();
	}
	
	protected void onPostExecute(final String result) {
		try {
			// chỉ khi nào thuần search thì mới show, còn 
// xài chung với ReadZip thì khỏi show
			// sourceContent = s.currentUrl;
			if (s.currentZipFileName.length() == 0) {
				if (s.currentUrl.startsWith("file:/")) {
					(s.webTask = new WebTask(s, s.webView, s.currentUrl, true, result)).execute();
//						webView.loadUrl(s.currentUrl);
				} else {
					loadUrl(s.webView, s.currentUrl);
					s.home = s.currentUrl;
				}
			}
			s.statusView.setText(result);
			if (s.requestCompare) {
				(s.compTask = new CompareTask(s)).execute();
			} else if (s.requestSearching) {
				if (s.searchTask != null) {
					s.searchTask.cancel(true);
				}
				s.searchTask = new SearchTask(s);
				s.searchTask.execute(s.currentSearching);
				s.requestSearching = false;
			} else if (s.requestTranslate) {
				if (s.translateTask != null) {
					s.translateTask.cancel(true);
				}
				s.translateTask = new TranslateTask(s, s.getSourceFileTask.convertedFileList);
				s.translateTask.execute();
				s.requestTranslate = false;
			}
			if (MainActivity.autoBackup) {
				new Thread(new Runnable() {
						@Override
						public void run() {
							for (String st : s.selectedFiles) {
								File f = new File(st);
								if (f.isDirectory()) {
									File fp = new File(MainFragment.PRIVATE_PATH + st);
									try {
										FileUtil.compressAFileOrFolderRecursiveToZip(fp, st + ".converted.7z", ".+\\.converted\\.7z.*", "*.converted.txt");// ".+\\.converted\\.7z.*", ".*\\.converted.txt");
									} catch (IOException e) {
										Log.e("autoBackup", e.getMessage(), e);
									}
								}
							}
						}
					}).start();
			}
		} catch (Throwable e) {
			s.statusView.setText(e.getMessage());
			Log.e("GetSourceFileTask", e.getMessage(), e);
		}
	}

	private List<File> readFile(final List<File> files) throws IOException {
		Log.d("readFile1", files.toString());
		int totalFiles = files.size();
		final List<File> fileList = new ArrayList<File>(totalFiles);
		int counter = 0;
		errorCounter = 0;
		for (final File file : files) {
			if (isCancelled())
				return fileList;
			String status = new StringBuilder("Scanning ")
				.append(file.getName()).append("... (")
				.append(++counter).append("/").append(totalFiles)
				.append(" files)").toString();
			publishProgress(status);
			Log.d("Scanning ", status);
			try {
				if (file.length() > 0) {
					// trả về tên file đã được convert
					readFile(file, fileList);
				}
			} catch (Throwable e) {
				Log.e("readFile", e.getMessage(), e);
				errorProcessFiles
					.append(++errorCounter)
					.append(". ")
					.append("<a href=\"")
					.append(file.toURI().toURL()).append("\">").append(file.getAbsolutePath()).append("</a>: ")
					.append(e.getMessage())
					.append("<br/>");
			}
		}

		return fileList;
	}

	StringBuilder errorProcessFiles = null;
	int errorCounter = 0;
	String inFilePath = "";

	private void readFile(final File inFile, List<File> fileList) throws IOException, Exception, SAXException {
		Log.d("readFile2", inFile.toString());
		// publishProgress("reading file " + inFile);

		inFilePath = inFile.getAbsolutePath();
		File newFile;
		if (inFilePath.startsWith(MainFragment.PRIVATE_PATH)) {
			newFile = new File(inFilePath + Util.CONVERTED_TXT);
		} else {
			newFile = new File(MainFragment.PRIVATE_PATH + inFilePath + Util.CONVERTED_TXT);
		}
		// file text được chọn đã được convert từ trước
		if (newFile.exists() // && "Search".equals(load)
			&& (newFile.lastModified() > inFile.lastModified())) {
			publishProgress("already converted " + inFilePath);
			fileList.add(newFile);
			totalSelectedSize += newFile.length();
			Log.d("already converted newFile", String.valueOf(newFile));
			return;
		}
		String inFilePathLowerCase = inFilePath.toLowerCase();
		s.currentContent = null;

		String fileExtensionFromUrl = MimeTypeMap.getFileExtensionFromUrl(inFile.toURI().toURL().toString());
		String mimeTypeFromExtension = MainFragment.mimeTypeMap.getMimeTypeFromExtension(fileExtensionFromUrl.toLowerCase());
		//Log.d("mime currentFName", currentFName + " is "+ mimeTypeFromExtension);

		// file duoc chon co duoi .converted
		if (inFilePath.endsWith(Util.CONVERTED_TXT)
			|| mimeTypeFromExtension != null
			&& mimeTypeFromExtension.startsWith("text")
			&& !inFilePathLowerCase.matches(".*(.txt|.htm|.html|.xhtml|.rtf)")
			|| inFilePathLowerCase.matches(".*(.mk|.md|.list|.config|.configure|.bat|.sh|.lua|.depend)")
			)
		{
			publishProgress("adding converted file " + inFilePath);
			fileList.add(inFile);
			// file txt được chọn có thể đã được convert từ trước nhưng đã cũ
		} else if (inFilePathLowerCase.endsWith(".txt")) {
			publishProgress("converting file " + inFilePath);
			String wholeFile = FileUtil.readFileWithCheckEncode(inFilePath);
			s.currentContent = Util.changeToVUTimes(wholeFile);
		} else if (inFilePathLowerCase.matches(".*(.htm|.html|.xhtml)") 
//					   || inFilePathLowerCase.endsWith(".htm")
//					   || inFilePathLowerCase.endsWith(".xhtml")
				   ) {
			publishProgress("converting file " + inFilePath);
			// convert sang dạng text đã convert font
			s.currentContent = Util.htmlToText(inFile);
		} else if (inFilePathLowerCase.endsWith(".pdf")) {
			publishProgress("converting file " + inFilePath);
			// pdf sang text
			File txtFile = Util.fromPDF(inFile);
			// format pdf bỏ dòng \r\n...
			s.currentContent = Util.filterCRPDF(txtFile);
			txtFile.delete();
			// guess font text file
			s.currentContent = Util.changeToVUTimes(s.currentContent);

			// } else if (inFilePathLowerCase.endsWith(".docx")
			// || inFilePathLowerCase.endsWith(".xlsx")
			// || inFilePathLowerCase.endsWith(".pptx")) { 
			// !.pdf" convert sang text rồi tự đoán font
			// s.currentContent = Writer.getChangedFont(inFile);
			// FileInputStream fis = new FileInputStream(inFile);
			// if (currentFName.endsWith("docx")) {
			// XWPFWordExtractor extractor = new XWPFWordExtractor(new XWPFDocument(fis));
			// s.currentContent = extractor.getText();
			// } else if (currentFName.endsWith("xlsx")) {
			// XSSFWorkbook workbook = new XSSFWorkbook(fis);
			// XSSFExcelExtractor extractor = new XSSFExcelExtractor(workbook);
			// s.currentContent = extractor.getText();
			// } else if (currentFName.endsWith("pptx")) {
			// XMLSlideShow slideShow = new XMLSlideShow(fis);
			// XSLFPowerPointExtractor extractor = new XSLFPowerPointExtractor(slideShow);
			// s.currentContent = extractor.getText();
			//}
			// fis.close();
		} else if (inFilePathLowerCase.endsWith(".doc")
				   ) { // !.pdf"
			publishProgress("converting file " + inFilePath);
			s.currentContent = FileUtil.readWordFileToText(inFile);
			s.currentContent = Util.changeToVUTimes(s.currentContent);
		} else // if ("Search".equals(load)) {
		if (inFilePathLowerCase.endsWith("docx")) {
			s.currentContent = Util.changeToVUTimes(DocxToText.docxToText(inFile));
		} else if (inFilePathLowerCase.endsWith("odt")) {
			s.currentContent = Util.changeToVUTimes(OdtToText.odtToText(inFile));
		} else if (inFilePathLowerCase.endsWith("rtf")) {
			Metadata metadata = new Metadata();
			StringWriter writer = new StringWriter();
			FileInputStream fis = new FileInputStream(inFile);
			final org.apache.tika.parser.rtf.TextExtractor ert = new org.apache.tika.parser.rtf.TextExtractor(new XHTMLContentHandler(new WriteOutContentHandler(writer), metadata), metadata);
			ert.extract(fis);
			s.currentContent = Util.changeToVUTimes(writer.toString()); // RTF2Txt.rtfToText(inFile)
		} else if (inFilePathLowerCase.endsWith("epub")) {
			s.currentContent = Util.changeToVUTimes(Epub2Txt.epub2txt(inFile));
		} else if (inFilePathLowerCase.endsWith("fb2")) {
			s.currentContent = Util.changeToVUTimes(FB2Txt.fb2txt(inFile));
		} else if (inFilePathLowerCase.endsWith("xlsx")) {
			s.currentContent = Util.changeToVUTimes(XLSX2Text.getText(inFile));
		} else if (inFilePathLowerCase.endsWith("pptx")) {
			s.currentContent = Util.changeToVUTimes(PPTX2Text.pptx2Text(inFile));
		} else if (inFilePathLowerCase.endsWith("ods")) {
			s.currentContent = Util.changeToVUTimes(ODSToText.odsToText(inFile));
		} else if (inFilePathLowerCase.endsWith("pub")) {
			s.currentContent = Util.changeToVUTimes(new PublisherTextExtractor(new  FileInputStream(inFilePath)).getText());
		} else if (inFilePathLowerCase.endsWith("vsd")) {
			s.currentContent = Util.changeToVUTimes(new VisioTextExtractor(new  FileInputStream(inFilePath)).getText());
		} else if (inFilePathLowerCase.endsWith("odp")) {
			s.currentContent = Util.changeToVUTimes(ODPToText.odpToText(inFile));
		} else if (inFilePathLowerCase.endsWith("ppt")
				   || inFilePathLowerCase.endsWith("pps")) {
			s.currentContent = Util.changeToVUTimes(new PowerPointExtractor(inFilePath).getText());
		} else if (inFilePathLowerCase.endsWith("xls")) {
			s.currentContent = Util.changeToVUTimes(new ExcelExtractor(
													  new POIFSFileSystem(new  FileInputStream(inFilePath))).getText());
			//try {
			//AbstractHtmlExporter exporter = new HtmlExporterNG2();
			//OutputStream os = new FileOutputStream(currentFName + Util.CONVERTED_TXT);
			//StreamResult result = new StreamResult(os);
			//WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(inFile);
			//exporter.html(wordMLPackage, result, new HTMLSettings());
			//result.getOutputStream().close();
			//} catch (Exception e) {
			//e.printStackTrace();
			//}
		} else if (inFilePathLowerCase.matches(MainFragment.compressExtension)) {
			String outDirFilePath = "";
			if (inFile.getAbsolutePath().startsWith(MainFragment.PRIVATE_PATH)) {
				String name = inFile.getName();
				int lastIndexOf = name.lastIndexOf(".");
				outDirFilePath = inFile.getParent() + "/" + name.substring(0, lastIndexOf) + "_" + name.substring(lastIndexOf + 1);
			} else {
				outDirFilePath = MainFragment.PRIVATE_PATH + inFile;
			}

			File outDirFile = new File(outDirFilePath);
			publishProgress("processing file " + inFilePath);
			outDirFile.mkdirs();

			Log.d("outDirFilePath", outDirFilePath);
			ExtractFile extractFile = new ExtractFile(inFilePath, outDirFilePath);
			try {
				String zeName;
				List<File> extractedList = new LinkedList<File>();
				Collection<String> entryFileList = new HashSet<String>();
				while ((zeName = extractFile.getNextEntry()) != null) {

					String zeNameLower = zeName.toLowerCase();
					File entryFile = new File(outDirFilePath + "/" + zeName);
					File convertedEntryFile = new File(entryFile.getAbsolutePath() + Util.CONVERTED_TXT); // khi chạy đệ quy thì tạo thêm getFilesDir()
					Log.d("convertedEntryFile", convertedEntryFile + " exist: " + convertedEntryFile.exists());

					String mimeType = entryFile.toURI().toURL().toString();
					fileExtensionFromUrl = MimeTypeMap.getFileExtensionFromUrl(mimeType);
					mimeTypeFromExtension = MainFragment.mimeTypeMap.getMimeTypeFromExtension(fileExtensionFromUrl.toLowerCase());
					Log.d("mime entryFile", mimeType + " is " + fileExtensionFromUrl + " : " + mimeTypeFromExtension + (mimeTypeFromExtension != null && mimeTypeFromExtension.startsWith(("text"))));

					if (!zeName.endsWith("/")//.isDirectory()
						&& (convertedEntryFile.exists() 
						&& convertedEntryFile.lastModified() >= inFile.lastModified()))
					{
						publishProgress("adding converted file: " + convertedEntryFile);
						Log.d("adding converted file: ", convertedEntryFile + " ");
						fileList.add(convertedEntryFile);
					}
					else if (!zeName.endsWith("/")//.isDirectory()
							 && (entryFile.exists() 
							 && entryFile.lastModified() >= inFile.lastModified()))
					{
						publishProgress("adding source file: " + entryFile);
						Log.d("adding source file: ", entryFile + " ");
						extractedList.add(entryFile);
					} else if (!zeName.endsWith("/")//.isDirectory()
							   && (zeNameLower.matches(MainFragment.CAN_PROCESS)
							   || (mimeTypeFromExtension != null 
							   && mimeTypeFromExtension.startsWith("text")))) {
						publishProgress("extracting " + inFile + "/" + zeName);
						Log.d("extracting zeName", entryFile.toString());
						//zis.saveToFile(zeName);
						entryFileList.add(zeName);
						extractedList.add(entryFile);
						Log.d("entryFile", entryFile + " written, size: " + entryFile.length());
					}               
				}

				if (entryFileList.size() > 0) {
					extractFile.extractEntries(entryFileList, false);
				}
				for (File file : extractedList) {
					readFile(file, fileList);
					String fname = file.getName().toLowerCase();
					if (fname.matches(".*(.doc|.ppt|.xls|.docx|.odt|.pptx|.xlsx|.odp|.ods|.epub|.fb2|.htm|.html|.rtf|.pdf)")) {
						Log.d("delete", file.getAbsolutePath());
						//tempFList.add(file.getAbsolutePath());
						file.delete();
					}
				}
				return;
			} catch (Exception e) {
				Log.d("zip process source file", e.getMessage(), e);
			} finally {
				Log.d("GetSourceFileTask", "zis.close()");
				extractFile.close();
			}
		}
		// }
		if (s.currentContent != null && s.currentContent.length() > 0) {
			// save to text file
			FileUtil.writeFileAsCharset(newFile, s.currentContent, Util.UTF8);
			fileList.add(newFile);
			totalSelectedSize += s.currentContent.getBytes().length;
			Log.d("newFile exist", newFile + " just written: " + newFile.exists());
		} else {
			s.currentContent = "";
			s.contentLower = "";
		}
		Log.d("newFile", String.valueOf(newFile));
	}

	private String loadUrl(WebView mWebView, String sb) throws IOException, MalformedURLException {
		File tempFile;
		if (s.files.length == 1) {
			tempFile = new File(MainFragment.PRIVATE_PATH + "/" + inFilePath /*+ "_" + System.currentTimeMillis() */ + ".1111.html");
		} else {
			tempFile = new File(MainFragment.PRIVATE_PATH + "/ziplisting_" + MainFragment.df.format(System.currentTimeMillis()).replaceAll("[/\\?<>\"':|]", "_") + ".html");
		}
		FileUtil.writeContentToFile(tempFile.getAbsolutePath(), sb);
		s.currentUrl = tempFile.toURI().toURL().toString();
		mWebView.loadUrl(s.currentUrl);
		Log.d("s.currentUrl 6", s.currentUrl);
		//tempFList.add(tempFile.getAbsolutePath());
		return s.currentUrl;
	}
}
