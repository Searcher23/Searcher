package com.free.searcher;
import android.os.*;
import android.webkit.*;
import java.util.regex.*;
import android.util.*;
import junrar.exception.*;
import java.io.*;
import com.free.p7zip.*;
import android.net.*;
import android.content.*;
import java.net.*;
import java.util.*;

class WebTask extends AsyncTask<Void, String, String> {

	private static final String PATTERN_STR = 
	"(src\\s*=|href\\s*=|url\\s*\\()\\s*" +
	"([^\\s'\">\\)]+" +
	"|'[^']+'" +
	"|\"[^\"]+\")";

	private static final Pattern LINK_PATTTERN = Pattern.compile(PATTERN_STR);
	/**
	 *  Đường dẫn đến curFullEntryName tính từ getFileDir() dùng trong zip:/
	 */
	String storeFileName;

	private WebView mWebView;
	// private WeakReference<WebView> mWebView;
	private String mUrl;
	// private String urlExtensionLower = "";

	/**
	 * tên entry sẽ được extract trong list các entries của zip file
	 */
	String curFullEntryName = "";

	/**
	 * đường dẫn đầy đủ của file (đã extracted) từ getFileDirs hoặc gốc được
	 * tính toán từ mUrl
	 */
	String pathToOpenFile;
	String exeptFilePrefix;
	String zipPartWithSlash = "";
	boolean isHome = false;
	String status = "";

	String mime;
	private MainFragment s;

	public WebTask(MainFragment s) {
		this.s = s;
	}

	public WebTask(MainFragment s, WebView webView, String url) {
		this.s = s;
		init(webView, url);
	}

	public WebTask(MainFragment s, WebView webView, String url, String status) {
		this.s = s;
		init(webView, url);
		this.status = status;
	}

	public WebTask(MainFragment s, WebView webView, String url, boolean isHome) {
		this.s = s;
		init(webView, url);
		this.isHome = isHome;
	}

	public WebTask(MainFragment s, WebView webView, String url, boolean isHome, String status) {
		this.s = s;
		init(webView, url);
		this.isHome = isHome;
		this.status = status;
	}

	 void init(WebView webView, String url) {
		mWebView = webView;
		mUrl = url;
		Log.d("PATTERN_STR", PATTERN_STR);
		int end = mUrl.indexOf("?") >= 0 ? mUrl.indexOf("?") : mUrl.length();
		end = Math.min(mUrl.indexOf("#") >= 0 ? mUrl.indexOf("#"): mUrl.length(), end);

		exeptFilePrefix = mUrl.substring("file:".length(), end).replaceAll("///", "/").replaceAll("//", "/");
		exeptFilePrefix = Util.decodeUrlToFS(exeptFilePrefix);
		Log.d("mUrl, exeptFilePrefix", url + ", " + exeptFilePrefix);
		int lastIndexOfDot = exeptFilePrefix.lastIndexOf(".");
		String fileExtensionFromUrl = lastIndexOfDot >= 0 ? exeptFilePrefix.substring(lastIndexOfDot + 1) : "";
		mime = MainFragment.mimeTypeMap.getMimeTypeFromExtension(fileExtensionFromUrl.toLowerCase());
		if (s.currentZipFileName.length() > 0 && s.extractFile == null) {
			try {
				s.extractFile = new ExtractFile(s.currentZipFileName, MainFragment.PRIVATE_PATH + s.currentZipFileName);
			} catch (RarException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/* 
	 * 
	 * * 
	 * 
	 * Các yếu tố cần giải quyết
	 * file: cần xử lý kiểm tra zip, directory, đã extract chưa
	 * file đã extract vào getFilesDir nên đã có sẵn chữ zip trong thành phần file, nhưng zip này là thư mục
	 * zip cần kiểm tra thư mục
	 * directory
	 * file
	 * đã được extract
	 * text nhưng trong nội dung có html =>
	 * mime text
	 * mime html
	 * mime null
	 * 					chưa được extract
	 * 						text nhưng trong nội dung có html =>
	 *   					mime text
	 *   					mime html
	 *   					mime null
	 *  	not zip
	 *  		text nhưng trong nội dung có html
	 *  		mime text
	 *   		mime html
	 *   		mime null
	 * http: cứ việc dùng mWebView loadUrl?
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected String doInBackground(Void... params) {
		Log.d("mime", mUrl + " is " + mime);
		try {
			if (mUrl.startsWith("file:")) {
				// currentZFile file zip
				// curFullEntryName entryName trong file zip
				// storeFileName entry không tính currentZFile
				// pathToOpenFile địa chỉ file không phải là zip

				// mime sẽ được xử lý trong post, trong zip luôn cần phải extract trước, đọc file hay không thì tính trên mime

				String exeptFilePrefixLower = exeptFilePrefix.toLowerCase();
				// String mUrlLower = mUrl.toLowerCase();

				File exeptFilePrefixFile = new File(exeptFilePrefix);
				int start = 0;
				if ((start = exeptFilePrefixLower.lastIndexOf(".7z/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".bz2/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".bzip2/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".tbz2/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".tbz/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".gz/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".gzip/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".tgz/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".tar/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".dump/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".swm/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".xz/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".txz/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".zip/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".zipx/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".jar/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".apk/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".xpi/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".odt/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".ods/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".odp/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".docx/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".xlsx/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".pptx/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".epub/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".apm/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".ar/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".a/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".deb/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".lib/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".arj/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".cab/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".chm/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".chw/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".chi/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".chq/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".msi/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".msp/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".doc/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".xls/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".ppt/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".cpio/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".cramfs/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".dmg/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".ext/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".ext2/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".ext3/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".ext4/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".img/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".fat/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".hfs/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".hfsx/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".hxs/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".hxi/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".hxr/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".hxq/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".hxw/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".lit/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".ihex/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".iso/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".lzh/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".lha/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".lzma/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".mbr/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".mslz/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".mub/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".nsis/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".ntfs/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".rar/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".r00/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".rpm/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".ppmd/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".qcow/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".qcow2/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".qcow2c/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".squashfs/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".udf/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".iso/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".scap/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".uefif/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".vdi/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".vhd/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".vmdk/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".wim/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".esd/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".xar/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".pkg/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".z/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".sz/")) >= 0 
					|| (start = exeptFilePrefixLower.lastIndexOf(".taz/")) >= 0) {
					pathToOpenFile = "";
					curFullEntryName = exeptFilePrefix.substring(exeptFilePrefix.indexOf("/", start) + 1);
					zipPartWithSlash = exeptFilePrefix.substring(0, exeptFilePrefix.length() - curFullEntryName.length());
					Log.d("zipPartWithSlash 1", zipPartWithSlash + " ");
					Log.d("exeptFilePrefix", exeptFilePrefix + " ");
					Log.d("curFullEntryName", curFullEntryName + " ");
					Log.d("s.currentZipFileName", s.currentZipFileName + " ");
					// publishProgress(s.currentZipFileName + "/" + curFullEntryName);

					if (exeptFilePrefixFile.exists()) { // trên đường dẫn file thiệt có .zip
						if (exeptFilePrefixFile.isFile()) {
							// file trong zip đã được extract trong getFilesDir
							s.currentUrl = exeptFilePrefixFile.toURI().toURL().toString();
							Log.d("exeptFilePrefixFile.isFile()", s.currentUrl);
							return fileHandler(exeptFilePrefixFile.getAbsolutePath(), mime);
						} else { // directory
							// String st = SearchFragment.EMPTY_HEAD + listingFolder(exeptFilePrefixFile) + SearchFragment.END_BODY_HTML;
							// save file
							// get url
							// return url
							// s.currentUrl = ???
							Log.d("exeptFilePrefixFile.isDirectory", "true");
							return "";
						}
					} else { // file zip thiệt
						if (s.currentZipFileName.length() == 0) {
							Log.d("s.currentZipFileName.length() == 0", "true");
							publishProgress("Entry does not exist");
							return null; // do nothing
						}
						//ZipEntry entry = currentZFile.getEntry(curFullEntryName);
						if (curFullEntryName.length() == 0) {//(entry == null) {
//								Set<String> zipList = Arc.loadFilesFolders(currentZFile, curFullEntryName);
//								String zipToUrlStr = Arc.fileNames2UrlStr(new  File(s.currentZipFileName), zipList, true, "<br/>\r\n");
							String zipToUrlStr = s.extractFile.compressedFile2UrlStr(curFullEntryName, true, "<br/>\r\n");
							Log.d("zipList 1", zipToUrlStr);
							String name = curFullEntryName.endsWith("/") ? 
								curFullEntryName.substring(0, curFullEntryName.length() - 1) : 
								curFullEntryName; // do là folder
							Log.d("entryName 1", name);

							File tempFName = new  File(MainFragment.PRIVATE_PATH + s.currentZipFileName + "/" + name + "-0123456789.list.converted.html");
							//File.createTempFile(name + "-", ".html", new File(SearchFragment.PRIVATE_PATH + s.currentZipFileName));
							//tempFList.add(tempFName.getAbsolutePath());
							Log.d("(!tempFName.exists()", 
								  (!tempFName.exists() || tempFName.lastModified() < new  File(s.currentZipFileName).lastModified()) + "");
							if (!tempFName.exists() || tempFName.lastModified() < new  File(s.currentZipFileName).lastModified()) {
								FileUtil.writeContentToFile(tempFName.getAbsolutePath(), MainFragment.EMPTY_HEAD + zipToUrlStr + MainFragment.END_BODY_HTML);
							}
							s.currentUrl = tempFName.toURI().toURL().toString();
							s.home = s.currentUrl;
							return "";
//								publishProgress("Entry does not exist");
//								return null; // do nothing
						} else {
							// publishProgress(s.currentZipFileName + "/" + curFullEntryName);
							if (curFullEntryName.endsWith("/")) { //}(entry.isDirectory()) { // directory
								// Set<String> zipList = Arc.loadFilesFolders(currentZFile, curFullEntryName);
								// String zipToUrlStr = Arc.fileNames2UrlStr(new  File(s.currentZipFileName), zipList, true, "<br/>\r\n");
								String zipToUrlStr = s.extractFile.compressedFile2UrlStr(curFullEntryName, true, "<br/>\r\n");
								Log.d("zipList 2", zipToUrlStr);
								//String folderEntryName = entry.getName();
								Log.d("folderEntryName", curFullEntryName);
								String name = curFullEntryName.substring(0, curFullEntryName.length() - 1); // do là folder
								Log.d("entryName", name);
//									int lastIndexOf = name.lastIndexOf("/");
//									name = name.substring(lastIndexOf > 0 ? lastIndexOf : 0);
//									Log.d("entryName", name);
								Log.d("SearchFragment.PRIVATE_PATH + currentZFile", MainFragment.PRIVATE_PATH + s.currentZipFileName);
								File tempFName = new  File(MainFragment.PRIVATE_PATH + s.currentZipFileName + "/" + name + ".000.list.html"); 
								// File.createTempFile(name + "--", ".html", new File(SearchFragment.PRIVATE_PATH + s.currentZipFileName));
								//tempFList.add(tempFName.getAbsolutePath());
								Log.d("(!tempFName.exists()2", 
									  (!tempFName.exists() || tempFName.lastModified() < new  File(s.currentZipFileName).lastModified()) + "");
								if (!tempFName.exists() || tempFName.lastModified() < new  File(s.currentZipFileName).lastModified()) {
									FileUtil.writeContentToFile(tempFName.getAbsolutePath(), MainFragment.EMPTY_HEAD + zipToUrlStr + MainFragment.END_BODY_HTML);
								}
								s.currentUrl = tempFName.toURI().toURL().toString();
								//home = s.currentUrl;
								return "";	// "" thì đọc displayData, null thì error // không cần hậu xử lý
							} else { // file trong zip
								// sẽ bị chuyển vị trí sang getFilesDir() + s.currentZipFileName + "/" + entryName;
//									storeFileName = extract(curFullEntryName); // file ngoài không đọc được do extract vào chỗ hiểm
								//Arc archive = new Arc(s.currentZipFileName, SearchFragment.PRIVATE_PATH + s.currentZipFileName);
								storeFileName = s.extractFile.extractFile(curFullEntryName);
								Log.d("storeFileName", storeFileName);
								//archive.close();
								s.currentUrl = new File(storeFileName).toURI().toURL().toString();
								publishProgress(s.currentZipFileName + "/" + curFullEntryName);
								return fileHandler(storeFileName, mime);
							}
						}
					}
				} else { // không phải file zip
					pathToOpenFile = exeptFilePrefix;
					s.currentUrl = exeptFilePrefixFile.toURI().toURL().toString();
					publishProgress(pathToOpenFile);
					Log.d("pathToOpenFile", pathToOpenFile);
					return fileHandler(exeptFilePrefixFile.getAbsolutePath(), mime);
				}

			} else { // http:/ https/
				return "";
			}
		} catch (Throwable t) {
			publishProgress(t.getMessage());
			Log.d("doInBackground", t.getMessage(), t);
			return null;
		}
	}

	private String fileHandler(String storeFileName, String mime) throws IOException {
		Log.d("fileHandler opening", storeFileName + ", " + mime);
		// 3 kiểu return: 
		// (1): content,
		// (2): "" = có sao webView vậy, 
		// (3): null = không làm gì cả
		if (mime == null || mime.startsWith("image")) { // .project có sao biểu diễn vậy
			return "";
		} else { // mime != null
			if (mime.startsWith("text")) { // cần phân biệt với text/plain, text/html
				String data = FileUtil.readFileWithCheckEncode(storeFileName);
				return data; // cần hậu xử lý để biết html, txt...
			} else { // mime is application...
				//String storeUrl = new File(storeFileName).toURI().toURL().toString();
				//Uri parseUri = Uri.parse(storeUrl);
				Uri parseUri = Uri.fromFile(new File(storeFileName));
				publishProgress("opening " + parseUri);
				Log.d("opening", parseUri + "");
				Intent intent = new Intent(Intent.ACTION_VIEW, parseUri);
				s.startActivity(intent);
				return null; 
			}
		}
	}

	static final String VU_TIMES_STYLE = "<style type='text/css'>"
	+ "@font-face {	font-family: VU Times;"
	+ "	src: url('file:///android_asset/fonts/DejaVuSerifCondensed.ttf'); }"
	+ "@font-face {	font-family: times;"
	+ "	src: url('file:///android_asset/fonts/DejaVuSerifCondensed.ttf'); }"
	+ "@font-face {	font-family: sans;"
	+ "	src: url('file:///android_asset/fonts/DejaVuSerifCondensed.ttf'); }"
	+ "@font-face {	font-family: serif;"
	+ "	src: url('file:///android_asset/fonts/DejaVuSerifCondensed.ttf'); }"
	+ "@font-face {	font-family: arial;"
	+ "	src: url('file:///android_asset/fonts/DejaVuSerifCondensed.ttf'); }"
	+ " body, h1, h2, h3, h4, h5, h6, li, p, span, div, td {"
	+ "	font-family: VU Times;   font-size: small;}"
	+ "</style>";

	@Override
	protected void onPostExecute(String result) {
		try {
			if (result == null) { // mở application ngoài
				// do nothing
			} else {
				Log.d("result.length()", result.length() + "");
				// s.statusView.setText(mUrl);
				if (result.length() == 0) { // do đã xử lý từ doInBackground
					Log.d("mWebView.loadUrl(s.currentUrl)", s.currentUrl);
					mWebView.loadUrl(s.currentUrl); //  displayData làm nhiệm vụ lưu url trong path và ...
					// String file = s.currentUrl.startsWith("file:") ? s.currentUrl.substring(5) : s.currentUrl;
					// Log.d("File to be read for sourceContent", file);
					// byte[] pageContent = FileUtil.readFileToMemory(file);
					// sourceContent = new String(pageContent);
					// showToast(file);
				} else { // result.length() > 0
					int hedpos = result.toLowerCase().indexOf("</head");
					if (hedpos < 0) {
						hedpos = result.toLowerCase().indexOf("</html>");
					}
					Log.d("hedpos </head", hedpos + "");
					// html cần phải add unicode & chạy loadUrl để extract hình...
					// text chuyển thành unicode
					// java, xml cứ để vậy mà show

					if (hedpos >= 0 // hậu xử lý data của html thêm thắt unicode
						&& (mime.startsWith("text/html"))) { // phòng trường hợp nội dung file có chữ </html>
						if (result.indexOf("src: url('file:///android_asset/fonts/DejaVuSerifCondensed.ttf');") < 0) {
							StringBuilder builder = new StringBuilder(result.substring(0, hedpos)).append(VU_TIMES_STYLE).append(result, hedpos, result. length());
							// builder = builder.insert(hedpos, VU_TIMES_STYLE);
							// Log.d("builder", builder.toString());
							loadUrl(builder.toString());
							// showToast("builder");
						} else {
							loadUrl(result, false);
							// showToast(s.statusView.getText().toString());
						}
					}  else { 
						// xử lý hậu data của plain/text có </html> trong nội dung file
						// tất cả loại file file:/ và zip:/ không phải là html (khỏi phải thêm unicode, extract image)
						if (mime != null && (mime.startsWith("text/plain") || mime.startsWith("text/txt"))) { 

							// load file text ra ngoài theo đúng file name gốc hay entry
							// text file có thể có nguồn gốc từ file: hoặc zip:
							StringBuilder sb = new StringBuilder(MainFragment.EMPTY_HEAD);
							Log.d("mSearchView", s.mSearchView + "");
							//Log.d("((FragmentStack)activity).mSearchView.getQuery()", ((SearchStack)activity).mSearchView.getQuery() + "");

							// String s.currentSearching = ((SearchStack)activity).mSearchView.getQuery().toString();
							if (s.currentSearching.trim().length() > 0) {
								sb.append(Util.replaceAll(result,
														  new String[] { "&", "<", "\n", s.currentSearching },
														  new String[] { "&amp;", "&lt;", "<br/>", "<b><font color='blue'>" + s.currentSearching + "</font></b>" }));
							} else {
								sb.append(Util.replaceAll(result,
														  new String[] { "&", "<", "\n", },
														  new String[] { "&amp;", "&lt;", "<br/>" }));
							}
							sb.append(MainFragment.END_BODY_HTML);
							loadUrl(sb.toString());
							// chỉ nhằm save file rồi load, không cần phải extract gì vì đó là text
							// mWebView.loadDataWithBaseURL("file:///android_asset/",
							// sb.toString(), "text/html", "utf-8",
							// "about:blank");
						} else { // đúng ra là không xảy ra, đã không biết kiểu mime thì đọc content làm gì
							// String file = s.currentUrl.startsWith("file:") ? s.currentUrl.substring(5) : s.currentUrl;
							// byte[] pageContent = FileUtil.readFileToMemory(file);
							// sourceContent = new String(pageContent);
							Log.d("s.currentUrl 7", s.currentUrl);
							mWebView.loadUrl(s.currentUrl);
						}
					}
				}
			}
//				setNavVisibility(false);
		} catch (Throwable t) {
			s.statusView.setText(t.getMessage());
			Log.d("WebTask.post", t.getMessage(), t);
		}
		if (isHome) {
			s.home = s.currentUrl;
		}
		if (status.length() > 0) {
			s.statusView.setText(status);
		}
	}

	private String loadUrl(String contentStr)
	throws IOException, MalformedURLException {
		return loadUrl(contentStr, true);
	}

	private String loadUrl(String contentStr, boolean needWrite)
	throws IOException, MalformedURLException {

		final String tempFileName;
		Log.d("s.currentZipFileName", s.currentZipFileName + " ");
		Log.d("zipPartWithSlash", zipPartWithSlash + " ");
		Log.d("loadUrl curFullEntryName", curFullEntryName + " "); // chỉ có file:/ mới cần xử lý
		Log.d("pathToOpenFile", pathToOpenFile + " ");
		Log.d("exeptFilePrefix", exeptFilePrefix + " ");
		if (s.currentZipFileName.length() > 0) {
			if (zipPartWithSlash.startsWith(MainFragment.PRIVATE_PATH)) {
				tempFileName = zipPartWithSlash + curFullEntryName;
			} else {
				tempFileName = MainFragment.PRIVATE_PATH + "/" + zipPartWithSlash + curFullEntryName;
			}

			/*} else {
			 tempFile = new File(zipPartWithSlash + curFullEntryName);
			 }*/
		} else {
			if (pathToOpenFile.startsWith(MainFragment.PRIVATE_PATH)) {
				tempFileName = pathToOpenFile;
			} else if (pathToOpenFile.length() > 0){
				tempFileName = MainFragment.PRIVATE_PATH + "/" + pathToOpenFile;
			} else {
				tempFileName = exeptFilePrefix;
			}
		}
		Log.d("tempFile", tempFileName + "");

		if (curFullEntryName.length() > 0 || pathToOpenFile.length() > 0) {
			final Collection<String> urls = getSaveOtherUrl(contentStr);
//				new Thread(new Runnable() {
//						public void run() {
//							String st = s.statusView.getText().toString();
//							for (String file : urls) {
//								try {
//									extract(file);
//								} catch (Throwable t) {
//									publishProgress(t.getMessage());
//									Log.d("extract", t.getMessage(), t);
//								}
//							}
//							try {
//								Log.d("thread extract", urls.size() + "");
			if (s.currentZipFileName.length() > 0) {
				s.currentUrl = "";
//									locX = 0;
//									locY = 0;
				s.zextr = new ZipExtractionTask(s, urls);
				s.zextr.execute();
				//extract(urls);
			}
//							} catch (Throwable t) {
//								publishProgress(t.getMessage());
//								Log.e("extract", t.getMessage(), t);
//							}
//							publishProgress(st);
//						}
//					}).start();
		}

		// do dùng chung với load html và text
		File tempFile = new File(tempFileName);
		boolean already = false;
		if (!tempFileName.toLowerCase().endsWith(".html") && !tempFileName.toLowerCase().endsWith(".htm")) {
			// trước là text
			File tempF = new File(tempFileName + ".html");
			if (tempFile.exists() && tempF.exists() && tempFile.lastModified() < tempF.lastModified()) {
				already = true;
			} 
			tempFile = tempF;
			tempFileName = tempFile.getAbsolutePath();
		} else {
			// sẵn là html
		}

		Log.d("needWrite && !already", tempFileName + (needWrite && !already));
		if (needWrite && !already) {
			FileUtil.writeContentToFile(tempFileName, contentStr);
		}

		// home = s.currentUrl;
		//String curUrl = tempFile.toURI().toURL().toString();
		mWebView.loadUrl((s.currentUrl = tempFile.toURI().toURL().toString()));
		//s.currentUrl = curUrl;
		Log.d("s.currentUrl 4", s.currentUrl);
		//tempFList.add(tempFileName);
//			while (!tempFile.getParent().equals(SearchFragment.PRIVATE_PATH)) {
//				tempFolderList.add((tempFile = tempFile.getParentFile()).getAbsolutePath());
//			}

		return s.currentUrl;
	}

	// String patternStr =
	// "(src[ \t]*|href[ \t]*|url[ \t]*\\()=?['\" \t]*([^ '\">]*)[ '\">\\)]?";
	// Pattern pat = Pattern.compile(patternStr);

	/**
	 * Lấy các url trong content để sau này extract trong zip
	 *
	 * @param contentStr
	 * @return
	 */
	private Collection<String> getSaveOtherUrl(String contentStr) {

//			Log.d("contentStr", contentStr);

		Collection<String> lF = new HashSet<String>();
		// File currentFile = new File(curEntryName);
		Log.d("getSaveOtherUrl curFullEntryName", curFullEntryName);

		String contentLower = contentStr.toLowerCase();
		Matcher mat = LINK_PATTTERN.matcher(contentLower);

		while (mat.find()) {
			Log.d("mat.group()", mat.group());
			String linkFile;
			if (curFullEntryName.length() > 0) {
				// trường hợp file nằm ở gốc
				if (curFullEntryName.lastIndexOf("/") >= 0) {
					linkFile = curFullEntryName.substring(0, curFullEntryName.lastIndexOf("/"));
				} else {
					linkFile = "";
				}
//					linkFile = curFullEntryName.substring(0, 
//							curFullEntryName.lastIndexOf("/") >= 0 ? 
//							curFullEntryName.lastIndexOf("/") : curFullEntryName.length());
			} else {
				if (pathToOpenFile.lastIndexOf("/") >= 0) {
					linkFile = pathToOpenFile.substring(pathToOpenFile.toLowerCase().indexOf(".zip/") + 5, 
														pathToOpenFile.lastIndexOf("/"));// trường hợp file nằm ở gốc
				} else {
					linkFile = "";
				}
			}
			Log.d("linkFile 1", linkFile);
			String newLink = contentStr.substring(mat.start(2), mat.end(2));
//				if (mat.group().startsWith("url")) {
			if (newLink.endsWith("'") || newLink.endsWith("\"")) {
				newLink = newLink.substring(1, newLink.length() - 1);
			}
			//}
			newLink = skipParam(newLink, "?");
			newLink = skipParam(newLink, "#");
			newLink = Util.decodeUrlToFS(newLink);

			Log.d("newLink", newLink);
			// không cần extract link http://
			if (newLink.length() > 0 && newLink.indexOf(":") < 0){
				if (!newLink.startsWith(".")) {
					// thư mục con tương đối
					if (linkFile.length() > 0) {
						linkFile = linkFile + "/" + newLink; 
					} else {
						linkFile = newLink;
					}
				} else if (newLink.startsWith("./")) {
					// thư mục hiện hành
					newLink = newLink.substring("./".length());
					linkFile = linkFile + "/" + newLink;
				} else if (newLink.startsWith("../")) {
					// thư mục cha tương đối
					String tempEntryName = linkFile;
					while (newLink.startsWith("../")) {
						newLink = newLink.substring("../".length());
						Log.d("getSaveOtherUrl tempEntryName", tempEntryName);
						int lastIndexOf = tempEntryName.lastIndexOf("/");
						if (lastIndexOf >= 0) {
							tempEntryName = tempEntryName.substring(0, lastIndexOf);
						} else {
							tempEntryName = "";
						}
					}
					if (tempEntryName.length() > 0) {
						linkFile = tempEntryName + "/" + newLink;
					} else {
						linkFile = newLink;
					}
					// System.out.println(linkFile);
				}
				lF.add(linkFile);
				Log.d("linkFile 2", linkFile.toString());
//				} else if (newLink.length() == 0 && newLink.indexOf(":") != 4 && newLink.indexOf(":") != 5) {
//					lF.add(newLink);
			}
		}
		return lF;
	}

	private String skipParam(String newLink, String sign) {
		int indexQ = newLink.indexOf(sign);
		if (indexQ > 0) {
			newLink = newLink.substring(0, indexQ);
		} else if (indexQ == 0) {
			newLink = "";
		}
		return newLink;
	}

	protected void onProgressUpdate(String... progress) {
		if (progress != null && progress.length > 0 
			&& progress[0] != null && progress[0].trim().length() > 0) {
			s.statusView.setText(progress[0]);
		}
	}
}
