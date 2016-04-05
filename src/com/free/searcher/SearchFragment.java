package com.free.searcher;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.os.AsyncTask.*;
import android.util.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import org.apache.poi.hdgf.extractor.*;
import org.apache.poi.hpbf.extractor.*;
import org.apache.poi.hslf.extractor.*;
import org.apache.poi.hssf.extractor.*;
import org.apache.poi.poifs.filesystem.*;

import org.apache.tika.metadata.*;
import org.apache.tika.parser.rtf.*;
import org.apache.tika.sax.*;
import org.xml.sax.*;
import android.view.View.*;
import junrar.exception.*;


import android.support.annotation.*;

import android.annotation.*;
import android.support.v4.provider.*;
import com.free.p7zip.*;
import com.free.translation.*;

//import com.netcompss.loader.*;
//import com.netcompss.ffmpeg4android.*;

public class SearchFragment extends Fragment implements SearchView.OnQueryTextListener, View.OnSystemUiVisibilityChangeListener, View.OnLongClickListener {

	static String PRIVATE_PATH = "";
	private static File PRIVATE_DIR = null;

	private static void init() {
		PRIVATE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.com.free.searcher";
		PRIVATE_DIR = new File(PRIVATE_PATH);
		PRIVATE_DIR.mkdirs();
	}
	
	static {
		String sdCardPath = System.getenv("SECONDARY_STORAGE");
		File tmp = null;
		if (sdCardPath == null) {
			init();
			Log.d("sdCardPath = null", "PRIVATE_PATH = " + PRIVATE_PATH);
		} else if (!sdCardPath.contains(":")) {
			PRIVATE_PATH = sdCardPath + "/.com.free.searcher";
			PRIVATE_DIR = new File(PRIVATE_PATH);
			tmp = new File(PRIVATE_PATH + "/xxx" + System.currentTimeMillis());
			try {
				if (PRIVATE_DIR.mkdirs() || tmp.createNewFile()) {
					if (tmp != null) {
						Log.d("delete 1", tmp + ": " + tmp.delete());
					}
					Log.d(sdCardPath, PRIVATE_DIR.getTotalSpace() + " bytes");
				} else {
					init();
				}
			} catch (IOException e) {
				//e.printStackTrace();
				Log.e("tmp", tmp + ".");
				Log.e("PRIVATE_DIR", PRIVATE_DIR + ".");
				init();
				Log.d("sdCardPath 1", "PRIVATE_PATH = " + PRIVATE_PATH);
			}
		} else if (sdCardPath.contains(":")) {
			//Multiple Sdcards show root folder and remove the Internal storage from that.
			File storage = new File("/storage");
			File[] fs = storage.listFiles();
			init();
			if (fs != null) {
				File maxPrev = PRIVATE_DIR;
				long maxTotal = PRIVATE_DIR.getTotalSpace();
				for (File f : fs) {
					String absolutePath = f.getAbsolutePath();
					long totalSpace = f.getTotalSpace();
					Log.d(absolutePath, totalSpace + " bytes, can write " + f.canWrite());
					try {
						String comPath = absolutePath + "/.com.free.searcher";
						if (totalSpace > maxTotal && f.canWrite() && (new File(comPath).mkdirs() || (tmp = new File(comPath + "/xxx" + System.currentTimeMillis())).createNewFile())) {
							PRIVATE_PATH = comPath;
							PRIVATE_DIR = new File(PRIVATE_PATH);
							Log.d("sdCard ok", PRIVATE_DIR + ", tmp = " + tmp);
							maxTotal = totalSpace;
							// max old
							Log.d("delete ", maxPrev + ": " + maxPrev.delete());
							maxPrev = f;
							if (tmp != null) {
								Log.d("delete 2", tmp + ": " + tmp.delete());
								tmp = null;
							}
						}
					} catch (IOException e) {
						Log.e("tmp", tmp + ".");
						Log.e("PRIVATE_DIR", PRIVATE_DIR + ".");
						Log.d("sdCardPath 2", "PRIVATE_PATH = " + PRIVATE_PATH);
					}
				}
			}
		}
	}

	static final String CHOOSER_TITLE = "chooserTitle";
	static final String SUFFIX = "suffix";
	private static final int SEARCH_MENU_ID = Menu.FIRST;
	private Button nextButton, backButton, closeButton, clearButton;
	private EditText findBox;
	private TextView findRet;
	private RelativeLayout container;
	private boolean showFind = false;
	Typeface tf;
	private static final int MOOD_NOTIFICATIONS = R.layout.main;
	
	private SearchTask searchTask = null;
	GetSourceFileTask getSourceFileTask = null;
	private ZipReadingTask zr = null;
	private DupFinderTask dupTask = null;
	private CompareTask compTask = null;
	private volatile ZipExtractionTask zextr = null;
	private GenStardictTask genStardictTask = null;
	private RestoreStardictTask restoreStardictTask = null;
	private TranslateTask translateTask = null;
	File[] files;
	private WebTask webTask = null;
	public volatile int locX = 0;
	public volatile int locY = 0;
	
	private static final String LINE_SEP = "\n";

	private static final String END_BODY_HTML = "</body></html>";

	private static final int SEARCH_REQUEST_CODE = 1;
	private static final int ZIP_REQUEST_CODE = 2;
	private static final int COMPARE_REQUEST_CODE1 = 3;
	private static final int COMPARE_REQUEST_CODE2 = 4;
	private static final int GEN_REQUEST_CODE = 5;
	private static final int RESTORE_REQUEST_CODE = 6;
	private static final int DUP_REQUEST_CODE = 7;
	private static final int INTENT_WRITE_REQUEST_CODE = 10;
//	private static final int BATCH_REQUEST_CODE_1 = 8;
//	private static final int BATCH_REQUEST_CODE_2 = 9;
	private static final int TRANSLATE_REQUEST_CODE = 11;
	
	
	String[] selectedFiles = new String[0];
	public WebView webView = null;
	SearchView mSearchView;

	String currentSearching = "";
	public TextView statusView = null;

	String load = "Search";
	volatile String currentUrl = "";
	static DateFormat df = DateFormat.getDateTimeInstance();
	private boolean backForward = false;
	private String searchFileResult = "";
	
	private boolean nameOrder = true;
	private boolean groupViewChanged = false;
	private Cache cache = null;

	private static final String TITLE_ERROR_PROCESSING_FILES = "<br/>Error Files: <br/>";

	static final String SEARCH_FILES_SUFFIX = ".fb2; .epub; .txt; .pdf; .htm; .html; .shtm; .shtml; .xhtm; .xhtml; .xml; .rtf; .java; .c; .cpp; .h; .md; .lua; .sh; .bat; .list; .depend; .js; .jsp; .mk; .config; .configure; .machine; .asm; .css; .desktop; .inc; .i; .plist; .pro; .py; .s; .xpm; "
	+ ".7z; .bz2; .bzip2; .tbz2; .tbz; .gz; .gzip; .tgz; .tar; .swm; .xz; .txz; .zip; .zipx; .jar; .apk; .xpi; .odt; .ods; .odp; .docx; .xlsx; .pptx; .epub; .apm; .ar; .a; .deb; .lib; .arj; .cab; .chm; .chw; .chi; .chq; .msi; .msp; .doc; .xls; .ppt; .pps; .cpio; .cramfs; .dmg; .ext; .ext2; .ext3; .ext4; .img; .fat; .hfs; .hfsx; .hxs; .hxi; .hxr; .hxq; .hxw; .lit; .ihex; .iso; .lzh; .lha; .lzma; .mbr; .mslz; .mub; .nsis; .ntfs; .rar; .r00; .rpm; .ppmd; .qcow; .qcow2; .qcow2c; .squashfs; .udf; .iso; .scap; .uefif; .vdi; .vhd; .vmdk; .wim; .esd; .xar; .pkg; .z; .taz; .cpio; .sz; .dump; .pub; .vsd" ;
	static final String SEARCH_FILES_TITLE = "Searching Files (" + SEARCH_FILES_SUFFIX + ")";
	static final String ZIP_SUFFIX = ".7z; .bz2; .bzip2; .tbz2; .tbz; .gz; .gzip; .tgz; .tar; .dump; .swm; .xz; .txz; .zip; .zipx; .jar; .apk; .xpi; .odt; .ods; .odp; .docx; .xlsx; .pptx; .epub; .apm; .ar; .a; .deb; .lib; .arj; .cab; .chm; .chw; .chi; .chq; .msi; .msp; .doc; .xls; .ppt; .cpio; .cramfs; .dmg; .ext; .ext2; .ext3; .ext4; .img; .fat; .hfs; .hfsx; .hxs; .hxi; .hxr; .hxq; .hxw; .lit; .ihex; .iso; .lzh; .lha; .lzma; .mbr; .mslz; .mub; .nsis; .ntfs; .rar; .r00; .rpm; .ppmd; .qcow; .qcow2; .qcow2c; .squashfs; .udf; .iso; .scap; .uefif; .vdi; .vhd; .vmdk; .wim; .esd; .xar; .pkg; .z; .taz";
	static final String ZIP_TITLE = "Zip file (" + ZIP_SUFFIX + ")";
	static final String DOC_FILES_SUFFIX =
	".doc; .docx; .txt; .html; .odt; .rtf; .epub; .fb2; .pdf; .pps; .ppt; .pptx; .xls; .xlsx; " +
	".ods; .odp; .pub; .vsd; .htm; .xml; .xhtml; .java; .c; .cpp; .h; .md; .lua; .sh; bat; .list; .depend; .js; .jsp; .mk; .config; .configure; .machine; .asm; .css; .desktop; .inc; .shtm; .shtml; .i; .plist; .pro; .py; .s; .xpm";
	static final String TRANSLATE_SUFFIX_TITLE = "Document (" + DOC_FILES_SUFFIX + ")";
	static final String ORI_SUFFIX_TITLE = "Origin Document (" + DOC_FILES_SUFFIX + ")";
	static final String MODI_SUFFIX_TITLE = "Modified Document (" + DOC_FILES_SUFFIX + ")";
	static final String TXT_SUFFIX = ".txt";
	static final String TXT_SUFFIX_TITLE = "Dictionary Text (" + TXT_SUFFIX + ")";
	static final String IFO_SUFFIX = ".ifo";
	static final String IFO_SUFFIX_TITLE = "Dictionary Info File (" + IFO_SUFFIX + ")";
	static final String MEDIA_SUFFIX = ".mp3; .mp4; .wma; .amr; .avi; .mov; .vob; .ogg; .flac; .wmv; .mkv; .mpg; .mpeg; .mp2; .aac";
	static final String MEDIA_SUFFIX_TITLE = "Media Files (" + MEDIA_SUFFIX + ")";
	static final String ALL_SUFFIX = ".*";
	static final String ALL_SUFFIX_TITLE = "Select Files/Folders";
	String compressExtension = ".*\\.(7z|bz2|bzip2|tbz2|tbz|gz|gzip|tgz|tar|swm|xz|txz|zip|zipx|jar|apk|xpi|odt|ods|odp|docx|xlsx|pptx|epub|apm|ar|a|deb|lib|arj|cab|chm|chw|chi|chq|msi|msp|doc|xls|ppt|cpio|cramfs|dmg|ext|ext2|ext3|ext4|img|fat|hfs|hfsx|hxs|hxi|hxr|hxq|hxw|lit|ihex|iso|lzh|lha|lzma|mbr|mslz|mub|nsis|ntfs|rar|r00|rpm|ppmd|qcow|qcow2|qcow2c|squashfs|udf|iso|scap|uefif|vdi|vhd|vmdk|wim|esd|xar|pkg|z|taz|fb2|epub|cpio|sz|dump)"; //sz, dump self added
	//String CAN_PROCESS2 = ".*(.zip|.rar|.chm|.docx|.xlsx|.pptx|.odt|.ods|.odp|.epub|.jar|.apk|.7z|.gz|.bz2|.tar|.dump|.ar|.arj|.cpio|.xz|.z|.sz|.lzma|.pdf|.txt|.doc|.docx|.fb2|.odt|.epub|.rtf|.pptx|.xlsx|.ods|.odp|.pub|.vsd)";
	String CAN_PROCESS = ".*\\.(7z|bz2|bzip2|tbz2|tbz|gz|gzip|tgz|tar|swm|xz|txz|zip|zipx|jar|apk|xpi|odt|ods|odp|docx|xlsx|pptx|epub|apm|ar|a|deb|lib|arj|cab|chm|chw|chi|chq|msi|msp|doc|xls|ppt|pps|cpio|cramfs|dmg|ext|ext2|ext3|ext4|img|fat|hfs|hfsx|hxs|hxi|hxr|hxq|hxw|lit|ihex|iso|lzh|lha|lzma|mbr|mslz|mub|nsis|ntfs|rar|r00|rpm|ppmd|qcow|qcow2|qcow2c|squashfs|udf|iso|scap|uefif|vdi|vhd|vmdk|wim|esd|xar|pkg|z|taz|fb2|epub|cpio|sz|dump|pub|vsd|txt|pdf|htm|html|shtm|shtml|xhtm|xhtml|xml|rtf|java|c|cpp|h|md|lua|sh|bat|list|depend|js|jsp|mk|config|configure|machine|asm|css|desktop|inc|i|plist|pro|py|s|xpm)";
	
	private static final String HTML_STYLE = 
	"<html>\r\n"
	+ "<head>\r\n"
	+ "<meta http-equiv='Content-Language' content='en-us' />\r\n"
	+ "<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />\r\n"
	+ "<style type=\"text/css\">" + "@font-face {"
	+ "    font-family: DejaVuSerifCondensed;"
	+ "    src: url('file:///android_asset/fonts/DejaVuSerifCondensed.ttf');"
	+ "}"
	+ "body {"
	+ "    font-family: DejaVuSerifCondensed;"
	+ "    font-size: small;"
	// + "    text-align: justify;"
	+ "}"
	+ "</style>";
	
	private static final String EMPTY_HEAD = 
	HTML_STYLE
	+ "</head>\r\n"
	+ "<body text='#000000' link='#0000ff' vlink='#0000ff'>\r\n";
	
	private static final String HEAD_TABLE = 
	"</head>\r\n"
	+ "<body text='#000000' link='#0000ff' vlink='#0000ff'>\r\n"
	+ "<div align='center'>\r\n"
	+ "<table border='0' cellspacing='0' cellpadding='0' width='100%' style='width:100.0%;border-collapse:collapse'>\r\n";

	private static final String TD1 = "<td width='3%' valign='top' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	private static final String TD2 = "<td width='97%' valign='top' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	public static final String TD_COMPARE1 = "\r\n<td width='30%' valign='top' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	public static final String TD_COMPARE2 = "\r\n<td width='40%' valign='top' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	private static final String TD1_CENTER = "<td width='4%' align='center' valign='middle' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	private static final String TD2_CENTER = "<td width='76%' align='center' valign='middle' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	private static final String TD3_CENTER = "<td width='4%' align='center' valign='middle' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	private static final String TD1_LEFT = "<td width='4%' valign='top' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	private static final String TD2_LEFT = "<td width='76%' valign='top' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	private static final String TD3_LEFT = "<td width='4%' valign='top' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	
	public static final String SELECTED_DIR = Activity.class.getPackage().getName() + ".selectedDir";
	
	private String currentContent = null;
	private String contentLower = null;
	
	String home = "";

	CharSequence status = "";
	private Activity activity;
	Bundle webViewBundle = null;
	private ActionBar actionBar;

	void setNavVisibility(boolean visible) {
		int newVis = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
		if (!visible) {
			container.setVisibility(View.INVISIBLE);
			newVis = View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_IMMERSIVE
				//| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				;
		} else if (showFind) {
			container.setVisibility(View.VISIBLE);
			actionBar.show();
		} else {
			actionBar.show();
		}
		Log.d("newVis", newVis + "");
		// Set the new desired visibility.
		statusView.setSystemUiVisibility(newVis);
	}

	@Override public void onSystemUiVisibilityChange(int visibility) {
		Log.d("onSystemUiVisibilityChange", visibility + "");
		if (visibility ==  View.SYSTEM_UI_FLAG_VISIBLE) {
			actionBar.show();
			if (showFind) {
				container.setVisibility(View.VISIBLE);
			}
		} else {
			actionBar.hide();
		}
//		extendPause(visibility);
	 }
	 
//	Runnable mNavHider = new Runnable() {
//		@Override public void run() {
//			setNavVisibility(false);
//		}
//	};
//	private int currentPause = 8000;
//	long pastMilli = System.currentTimeMillis();
//	private void extendPause(int visibility) {
//		Handler h = statusView.getHandler();
//		if (h != null && visibility == 0) { // 0 = see
//			Log.d("currentPause", currentPause + ", currentTimeMillis = " + System.currentTimeMillis() + ", pastMilli = " + pastMilli + ", " + (System.currentTimeMillis() - pastMilli));
//			h.removeCallbacks(mNavHider);
//			if (System.currentTimeMillis() < pastMilli + currentPause + 2000) {
//				currentPause += 2000;
//			} else {
//				currentPause = 8000;
//			}
//			Log.d("currentPause", currentPause + "");
//			pastMilli = System.currentTimeMillis();
//			statusView.getHandler().postDelayed(mNavHider, currentPause);
//		}
//	}	

	public void onActivityCreated(android.os.Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewContainer,
							 Bundle savedInstanceState) {

		super.onCreateView(inflater, viewContainer,savedInstanceState);
		this.activity = getActivity();
		actionBar = activity.getActionBar();
		
		View v = inflater.inflate(R.layout.main, viewContainer, false);
		v.setOnSystemUiVisibilityChangeListener(this);
		
		webView = (WebView) v.findViewById(R.id.webView1);
		statusView = (TextView) v.findViewById(R.id.statusView);

		if (webViewBundle != null) {
			webView.restoreState(webViewBundle);
			Log.d("onCreateView.webView.restoreState", webViewBundle + "");
		} else if (currentUrl.length() > 0) {
			Log.d("onCreateView.locX, locY", locX + ", " + locY + ", " + currentUrl);
			webView.loadUrl(currentUrl);
			webView.setScrollX(locX);
			webView.setScrollY(locY);
			Log.d("currentUrl 8", currentUrl);
		}
		statusView.setText(status);

		Log.d("onCreateView.savedInstanceState", savedInstanceState + "vvv");
		mNotificationManager = (NotificationManager) activity.getSystemService(activity.NOTIFICATION_SERVICE);
		
		webView.setFocusable(true);
		webView.setFocusableInTouchMode(true);
		webView.requestFocus();
		webView.requestFocusFromTouch();
		webView.getSettings().setAllowContentAccess(false);
		webView.getSettings().setPluginState(WebSettings.PluginState.OFF);
		// webView.setBackgroundColor(LIGHT_BLUE);
		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webView.setBackgroundColor(getResources().getColor(R.color.lightyellow));

		webView.setOnLongClickListener(this);
		statusView.setOnLongClickListener(this);
			
		webView.setWebViewClient(new WebViewClient() {

				private void jumpTo(final int xLocation, final int yLocation) {
					webView.postDelayed(new Runnable() {
							@Override
							public void run() {
								Log.d("jumpTo1.locX, locY", xLocation + ", " + yLocation + ", " + currentUrl);
								try {
									webView.scrollTo(xLocation, yLocation);
									webView.setScrollX(xLocation);
									webView.setScrollY(yLocation);
//									locX = 0;
//									locY = 0;
								} catch (RuntimeException e) {
									Log.e("error jumpTo2.locX, locY", locX + ", " + locY + ", " + currentUrl);
								}
							}
						}, 100);
				}
				
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, final String url) {
					if (currentZipFileName.length() > 0 && (extractFile == null || extractFile.isClosed())) {
						try {
							extractFile = new ExtractFile(currentZipFileName, PRIVATE_PATH + currentZipFileName);
						} catch (RarException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					int ind = url.indexOf("?deleteFile=");
					if (ind>=0) {
						if (dupTask == null) {
							showToast("Please do duplicate finding again.");
							return true;
						}

						String urlStatus = Util.getUrlStatus(url);
						final String selectedFile = urlStatus.substring(urlStatus.indexOf("?deleteFile=")+12, urlStatus.length());
						Log.d("deleteFile", "url=" + url + ", urlStatus="+urlStatus);

						AlertDialog.Builder alert = new AlertDialog.Builder(activity);
						alert.setTitle("Delete File?");
						alert.setMessage("Do you really want to delete file \""+ selectedFile + "\"?");
						alert.setCancelable(true);
						alert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									try {
										locX = webView.getScrollX();
										locY = webView.getScrollY();
										webView.loadUrl(new File(dupTask.deleteFile(selectedFile)).toURI().toURL().toString());
									} catch (IOException e) {
										statusView.setText(e.getMessage());
										Log.d("deleteFile", e.getMessage(), e);
									}
								}
							});
						alert.setPositiveButton("No", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							});
						AlertDialog alertDialog = alert.create();
						alertDialog.show();
						return true;
 					}
					
					ind = url.indexOf("?deleteGroup=");
					if (ind>=0) {
						if (dupTask == null) {
							showToast("Please do duplicate finding again.");
							return true;
						}

						String urlStatus = Util.getUrlStatus(url);
						final String groupFile = urlStatus.substring(urlStatus.indexOf("?deleteGroup=")+13, urlStatus.length());
						int indexOf = groupFile.indexOf(",");
						final int group = Integer.parseInt(groupFile.substring(0, indexOf));
						final String selectedFile = groupFile.substring(indexOf + 1);
						Log.d("groupFile", ",groupFile="+groupFile + ", url=" + url + ", urlStatus="+urlStatus);

						AlertDialog.Builder alert = new AlertDialog.Builder(activity);
						alert.setTitle("Delete Group of Files?");
						alert.setMessage("Do you really want to delete this group, except file \""+ selectedFile + "\"?");
						alert.setCancelable(true);
						alert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									locX = webView.getScrollX();
									locY = webView.getScrollY();
									webView.postDelayed(new Runnable() {
											@Override
											public void run() {
												try {
													webView.loadUrl(new File(dupTask.deleteGroup(group, selectedFile)).toURI().toURL().toString());
												} catch (Throwable e) {
													statusView.setText(e.getMessage());
													Log.e("Delete Group", e.getMessage(), e);
													Log.e("Delete Group", locX + ", " + locY + ", " + currentUrl);
												}
											}
										}, 0);
								}
							});
						alert.setPositiveButton("No", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							});
						AlertDialog alertDialog = alert.create();
						alertDialog.show();
						return true;
 					}
					
					ind = url.indexOf("?deleteFolder=");
					if (ind>=0) {
						if (dupTask == null) {
							showToast("Please do duplicate finding again.");
							return true;
						}

						String urlStatus = Util.getUrlStatus(url);
						final String selectedFile = urlStatus.substring(urlStatus.indexOf("?deleteFolder=")+14, urlStatus.length());
						Log.d("deleteFolder", ",deleteFolder="+selectedFile + ", url=" + url + ", urlStatus="+urlStatus);
						AlertDialog.Builder alert = new AlertDialog.Builder(activity);
						alert.setTitle("Delete folder?");
						alert.setMessage("Do you really want to delete duplicate in folder \"" + selectedFile.substring(0, selectedFile.lastIndexOf("/")) + "\"?");
						alert.setCancelable(true);
						alert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									locX = webView.getScrollX();
									locY = webView.getScrollY();
									webView.postDelayed(new Runnable() {
											@Override
											public void run() {
												try {
													webView.loadUrl(new File(dupTask.deleteFolder(selectedFile)).toURI().toURL().toString());
												} catch (Throwable e) {
													statusView.setText(e.getMessage());
													Log.e("Delete folder", e.getMessage(), e);
													Log.e("Delete folder", locX + ", " + locY + ", " + currentUrl);
												}
											}
										}, 0);
								}
							});
						alert.setPositiveButton("No", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							});
						AlertDialog alertDialog = alert.create();
						alertDialog.show();
						return true;
 					}

					ind = url.indexOf("?deleteSub=");
					if (ind>=0) {
						if (dupTask == null) {
							showToast("Please do duplicate finding again.");
							return true;
						}

						String urlStatus = Util.getUrlStatus(url);
						
						final String selectedFile = urlStatus.substring(urlStatus.indexOf("?deleteSub=")+11, urlStatus.length());
						Log.d("deleteSub", ",deleteSub="+selectedFile + ", url=" + url + ", urlStatus="+urlStatus);
						AlertDialog.Builder alert = new AlertDialog.Builder(activity);
						alert.setTitle("Delete sub folder?");
						alert.setMessage("Do you really want to delete duplicate files in sub folder of \"" + selectedFile.substring(0, selectedFile.lastIndexOf("/")) + "\"?");
						alert.setCancelable(true);
						alert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									locX = webView.getScrollX();
									locY = webView.getScrollY();
									webView.postDelayed(new Runnable() {
											@Override
											public void run() {
												try {
													webView.loadUrl(new File(dupTask.deleteSubFolder(selectedFile)).toURI().toURL().toString());
												} catch (Throwable e) {
													statusView.setText(e.getMessage());
													Log.e("Delete sub folder", e.getMessage(), e);
													Log.e("Delete sub folder", locX + ", " + locY + ", " + currentUrl);
												}
											}
										}, 0);
								}
							});
						alert.setPositiveButton("No", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							});
						AlertDialog alertDialog = alert.create();
						alertDialog.show();
						return true;
 					}

					ind = url.indexOf("?viewName");
					if (ind>=0) {
						if (dupTask == null) {
							showToast("Please do duplicate finding again.");
							return true;
						}
						nameOrder = !nameOrder;
						locX = 0;
						locY = 0;
						Log.d("url=", url + ", viewName");
						try {
							webView.loadUrl(new File(dupTask.genFile(dupTask.groupList, dupTask.NAME_VIEW)).toURI().toURL().toString());
						} catch (IOException e) {
							Log.e("viewName", e.getMessage(), e);
						}
						return true;
 					}
					
					ind = url.indexOf("?viewGroup");
					if (ind>=0) {
						if (dupTask == null) {
							showToast("Please do duplicate finding again.");
							return true;
						}
						groupViewChanged = true;
						locX = 0;
						locY = 0;
						Log.d("url=", url + ", viewGroup");
						try {
							webView.loadUrl(new File(dupTask.genFile(dupTask.groupList, dupTask.GROUP_VIEW)).toURI().toURL().toString());
						} catch (IOException e) {
							Log.e("viewGroup", e.getMessage(), e);
						}
						return true;
 					}
					if (zextr == null) {
						locX = 0;
						locY = 0;
					} else {
						zextr = null;
					}
					
					if (SearchStack.popup) {
						final SearchFragment frag = ((SearchStack)activity).addFragmentToStack();
						frag.status = Util.getUrlStatus(url);
						frag.load = load;
						frag.currentSearching = currentSearching;
						frag.selectedFiles = selectedFiles;
						frag.files = files;
						frag.currentZipFileName = currentZipFileName;
						if (extractFile != null) {
							try {
								frag.extractFile = new ExtractFile();
								extractFile.copyTo(frag.extractFile);
							} catch (RarException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						frag.home = home;
//						if (mSearchView != null && mSearchView.getQuery().length() > 0) {
//							frag.mSearchView.setQuery(mSearchView.getQuery(), false);
//						}
						view.getHandler().postDelayed(new Runnable() {
								@Override public void run() {
									frag.webTask = new WebTask();
									frag.webTask.init(frag.webView, url);
									frag.webTask.execute();
									frag.statusView.setText(frag.status);
								}
							}, 100);
					} else {
						currentUrl = url;
						Log.d("currentUrl 19", currentUrl);
						status = Util.getUrlStatus(url);
						statusView.setText("Opening " + url + "...");
						webTask = new WebTask(webView, url, status.toString());
						webTask.execute();
					}
//					setNavVisibility(false);
					return true;
				}

//			@Override
//			public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//			}

				@Override
				public void onPageFinished(WebView view, String url) {
					if (container != null) {
						if (showFind) {
							container.setVisibility(View.VISIBLE);
							webView.findAllAsync(findBox.getText().toString());
						} else {
							container.setVisibility(View.INVISIBLE);
						}
					}
					Log.d("onPageFinished", locX + ", " + locY + ", currentUrl=" + currentUrl + ", url=" + url);
					setNavVisibility(false);
					if (!backForward) { //if (zextr != null) {
						// zextr = null;
						jumpTo(locX, locY);
					} else {
						backForward = false;
					}
//					locX = 0;
//					locY = 0;
					Log.d("onPageFinished", url);
					/* This call inject JavaScript into the page which just finished loading. */
//		        webView.loadUrl("javascript:window.HTMLOUT.processHTML(" +
//		        		"'<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
				}
			});

		WebSettings settings = webView.getSettings();
//		webView.setWebViewClient(new WebViewClient());
//		webView.setWebChromeClient(new WebChromeClient());
//		webView.addJavascriptInterface(new HtmlSourceViewJavaScriptInterface(), "HTMLOUT");
		settings.setMinimumFontSize(13);
		settings.setJavaScriptEnabled(true);
		settings.setDefaultTextEncodingName("UTF-8");
		settings.setBuiltInZoomControls(true);
		// settings.setSansSerifFontFamily("Tahoma");
		settings.setEnableSmoothTransition(true);
		
		return v;
	}
	
	static boolean SHOW = false;
	@Override
	public boolean onLongClick(View p1) {
//		Class<? extends WebView> webViewClass =
//			webView.getClass();
//		java.lang.Class<WebView>[] parameterType = null;
//		try {
//			java.lang.reflect.Method method =
//				webViewClass.getMethod("copySelection",
//											   parameterType);
//			java.lang.Object[] argument = null;
//			method.invoke(webView, argument);
//			Log.e("Activity", "Copied selection into clipboard");
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
		
		SHOW = !SHOW;
		Log.d("onLongClick.SHOW", SHOW + "");
		setNavVisibility(SHOW);
		return false;
	}
	
	private void initFind() {
		container = (RelativeLayout) activity.findViewById(R.id.layoutId);
		findBox = (EditText) container.findViewById(R.id.findBox);
		findBox.setText(currentSearching);
		findBox.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

		findBox.setOnKeyListener(new OnKeyListener() {
				@SuppressWarnings("deprecation")
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					Log.d("onKey", "keyCode=" + keyCode + ",event=" + event + ",v=" + v);
					if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& ((keyCode == KeyEvent.KEYCODE_ENTER))
						) {
						webView.findAllAsync(findBox.getText().toString());
					}
					return false;
				}
			});

		findBox.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View p1, boolean p2) {
					webView.findAllAsync(findBox.getText().toString());
				}
			});

		findRet = (TextView) container.findViewById(R.id.findRet);

		backButton = (Button) container.findViewById(R.id.backButton);
		backButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		backButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					webView.requestFocus();
					webView.findNext(false);
				}
			});

		nextButton = (Button) container.findViewById(R.id.nextButton);
		nextButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		nextButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					webView.requestFocus();
					webView.findNext(true);
				}
			});

		clearButton = (Button) container.findViewById(R.id.clearButton);
		clearButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		clearButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					findBox.setText("");
					webView.findAllAsync(findBox.getText().toString());
					findBox.requestFocus();
				}
			});

		closeButton = (Button) container.findViewById(R.id.closeButton);
		closeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		closeButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showFind = false;
					container.setVisibility(View.INVISIBLE);
				}
			});

		webView.findAllAsync(findBox.getText().toString());
		webView.setFindListener(new WebView.FindListener() {
				@Override
				public void onFindResultReceived(int p1, int p2, boolean p3) {
					findRet.setText((p1 + (p2 > 0 ? 1 : 0)) + "/" + p2);
				}
			});
		if (showFind) {
			container.setVisibility(View.VISIBLE);
		} else {
			container.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);

		if (savedInstanceState == null) {
			return;
		}

		Log.i("SearcheFragment.onViewStateRestored();", savedInstanceState +"uuu");
		selectedFiles = savedInstanceState.getStringArray("selectedFiles");
		if (savedInstanceState.getSerializable("files") instanceof File[]) {
			files = (File[]) savedInstanceState.getSerializable("files");
		}

		if (savedInstanceState.getString("currentSearching") instanceof String) {
			currentSearching = savedInstanceState.getString("currentSearching");
		}

		if (savedInstanceState.getString("currentZipFileName") instanceof String) {
			currentZipFileName = savedInstanceState.getString("currentZipFileName");
			if (currentZipFileName.length() > 0) {
				try {
					extractFile = new ExtractFile(currentZipFileName, PRIVATE_PATH + currentZipFileName);
				} catch (RarException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
     	
		if (savedInstanceState.getString("load") instanceof String) {
			load = savedInstanceState.getString("load");
		}

		if (savedInstanceState.getString("currentUrl") instanceof String) {
			currentUrl = savedInstanceState.getString("currentUrl");
		}
//		HtmlSourceViewJavaScriptInterface.source = savedInstanceState.getString("source");

		if (currentUrl != null && currentUrl.length() > 0) {
			(webTask = new WebTask(webView, currentUrl)).execute();
		}

		if (mSearchView != null && savedInstanceState.getCharSequence("query") != null) {
			mSearchView.setQuery(savedInstanceState.getCharSequence("query"), false);
		}

		if (savedInstanceState.getString("status") instanceof String) {
			status = savedInstanceState.getCharSequence("status");
		}
		locX = savedInstanceState.getInt("locX");
		locY = savedInstanceState.getInt("locY");
		
		webViewBundle = savedInstanceState.getBundle("webViewBundle");
		if (savedInstanceState.getString("home") instanceof String) {
			home = savedInstanceState.getString("home");
		}

//		webView.setOnTouchListener(new View.OnTouchListener() {
//				@Override
//				public boolean onTouch(View p1, MotionEvent event) {
//					//Log.d("onClick", p1 + "");
//					Log.d("MotionEvent", event + "");
//					if ((event.getAction() ==  MotionEvent.ACTION_DOWN || event.getAction() ==  MotionEvent.ACTION_POINTER_UP) && statusView != null) {
//						int curVis = statusView.getSystemUiVisibility();
//						Log.d("curVis", curVis + ", " + ((curVis&View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0));
//						SHOW = !SHOW;
//						Log.d("SHOW", SHOW + "");
//						setNavVisibility(SHOW); //((curVis&View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0);
//					}
//					return false;
//				}
//			});
			
		/**
		 if (savedInstanceState.getString("readTextFiles") != null) {
		 String[] readTextFilesStr = savedInstanceState.getString("readTextFiles").split(";;;"); // ,
		 // Util.listToString(readTextFiles,false, ";;;"));
		 for (String textFile : readTextFilesStr) {
		 readTextFiles.add(new File(textFile));
		 }
		 }

		 if (savedInstanceState.getString("initFolderFiles") != null) {
		 String[] initFolderFilesStr = savedInstanceState.getString("initFolderFiles").split(";;;"); // ,
		 // Util.listToString(initFolderFiles,false, ";;;"));
		 for (String folderFile : initFolderFilesStr) {
		 initFolderFiles.add(new File(folderFile));
		 }
		 }
		 **/
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.i("SearcheFragment.onSaveInstanceState();", outState + ", " + actionBar.getTabCount());
		outState.putInt("level selected", actionBar.getSelectedNavigationIndex());
		outState.putStringArray("selectedFiles", selectedFiles);

		outState.putSerializable("files", files);
		
		outState.putString("currentZipFileName", currentZipFileName);
		if (extractFile != null) {
			extractFile.close();
		}
		outState.putString("currentSearching", currentSearching);
		
		outState.putString("load", load);

		Log.d("load", load + " xxx");
		outState.putString("currentUrl", currentUrl);
//		outState.putString("source", HtmlSourceViewJavaScriptInterface.source);
		outState.putCharSequence("query", (mSearchView != null && mSearchView.getQuery() != null) ? mSearchView.getQuery() : "");
		status = statusView.getText();
		outState.putCharSequence("status", status);

		locX = webView.getScrollX();
		outState.putInt("locX", locX);
		locY = webView.getScrollY();
		outState.putInt("locY", locY);
		
		outState.putString("home", home);

		webView.saveState((webViewBundle = new  Bundle()));
		outState.putBundle("webViewBundle", webViewBundle);
		Log.d("frag.onSaveInstanceState();", outState + "");
		/**
		 try {
		 outState.putString("readTextFiles", Util.listToString(readTextFiles, false, ";;;"));
		 outState.putString("initFolderFiles", Util.listToString(initFolderFiles, false, ";;;"));
		 } catch (Throwable t) {
		 t.printStackTrace();
		 }
		 **/
	}

	@Override
	public void onPause() {
		super.onPause();
//		if (getActivity().isFinishing()) {
//			for (String f : tempFList) {
//				new File(f).delete();
//			}
//			for (String f : tempFolderList) {
//				new File(f).delete();
//			}
//		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Log.d("onViewCreated.savedInstanceState", savedInstanceState + " vvv9");
		tf = Typeface.createFromAsset(activity.getAssets(), "fonts/DejaVuSerifCondensed.ttf");
		initFind();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	
	/**
	 * The requestCode with which the storage access framework is triggered for folder.
	 */
	private static final int READ_REQUEST_CODE = 42;
	private static final int WRITE_REQUEST_CODE = 43;
	private static final int EDIT_REQUEST_CODE = 44;
	
	/**
	 * Check the folder for writeability. If not, then on Android 5 retrieve Uri for extsdcard via Storage
	 * Access Framework.
	 *
	 * @param folder The folder to be checked.
	 * @param code   The request code of the type of folder check.
	 * @return true if the check was successful or if SAF has been triggered.
	 */
//	private boolean checkFolder(@NonNull final File folder, final int code) {
//		
//		if (SystemUtil.isAndroid5() && FileUtils.isOnExtSdCard(folder)) {
//			if (!folder.exists() || !folder.isDirectory()) {
//				return false;
//			}
//
//			// On Android 5, trigger storage access framework.
//			if (!FileUtils.isWritableNormalOrSaf(folder)) {
//				// Ensure via listener that storage access framework is called only after information
//				// message.
//				Toast.makeText(activity, "In the following Android dialog, " 
//							   + "please select the external SD card and confirm at the bottom.", Toast.LENGTH_LONG);
//				//DialogUtil.displayInfo(getActivity(), listener, R.string.message_dialog_select_extsdcard);
//				triggerStorageAccessFramework(code);
//				return false;
//			}
//			// Only accept after SAF stuff is done.
//			return true;
//		} else if (SystemUtil.isKitkat() && FileUtils.isOnExtSdCard(folder)) {
//			// Assume that Kitkat workaround works
//			return true;
//		}
//		else if (FileUtils.isWritable(new File(folder, "DummyFile"))) {
//			return true;
//		}
//		else {
//			Toast.makeText(activity, "Cannot write to folder " + folder, Toast.LENGTH_LONG);
////			DialogUtil.displayError(getActivity(), R.string.message_dialog_cannot_write_to_folder, false,
////									mCurrentFolder);
////
////			mCurrentFolder = null;
//			return false;
//		}
//	}

	/**
	 * Trigger the storage access framework to access the base folder of the ext sd card.
	 *
	 * @param code The request code to be used.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void triggerStorageAccessFramework(final int code) {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
		startActivityForResult(intent, code);
	}

	/**
	 * After triggering the Storage Access Framework, ensure that folder is really writable. Set preferences
	 * accordingly.
	 *
	 * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who
	 *                    this result came from.
	 * @param resultCode  The integer result code returned by the child activity through its setResult().
	 * @param data        An Intent, which can return result data to the caller (various data can be attached to Intent
	 *                    "extras").
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void onActivityResultLollipop(final int requestCode, final int resultCode, @NonNull final Intent data) {

		if (requestCode == INTENT_WRITE_REQUEST_CODE) { 

			if (resultCode == Activity.RESULT_OK) { 
				// Get Uri from Storage Access Framework. 
				Uri treeUri = data.getData(); 
				// Persist URI in shared preference so that you can use it later. 
				// Use your own framework here instead of PreferenceUtil. 

				FileUtils.setSharedPreferenceUri(R.string.key_internal_uri_extsdcard, treeUri); 
				// Persist access permissions. 
				final int takeFlags = data.getFlags() & 
					(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION); 
				System.out.println("treeUri:" + treeUri);
				System.out.println("takeFlags" + String.valueOf(takeFlags));
				System.out.println("data.getFlags()" + String.valueOf(data.getFlags()));
				activity.getContentResolver().takePersistableUriPermission(treeUri, takeFlags); 
			} 
		}
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("data", "" + data);
		Log.d("requestCode", "" + requestCode);
		Log.d("resultCode", "" + resultCode);
		locX = 0;
		locY = 0;
		try {
			if (data != null) {
				String[] stringExtra = data.getStringArrayExtra(FolderChooserActivity.SELECTED_DIR);
				if (requestCode == SEARCH_REQUEST_CODE) {
					if (resultCode == Activity.RESULT_OK) {
						stopReadAndSearch();
						Log.d("SEARCH_REQUEST_CODE.selectedFiles", Util.arrayToString(stringExtra, true, LINE_SEP));
						currentZipFileName = ""; // làm dấu để khỏi show web getSourceFile
						selectedFiles = stringExtra;
						getSourceFileTask = new GetSourceFileTask();
						getSourceFileTask.execute();
						load = "Search";
					} else { // RESULT_CANCEL
						if (selectedFiles.length == 0) {
							showToast("Nothing to search");
							statusView.setText("Nothing to search");
						}
					}
				} else if (requestCode == ZIP_REQUEST_CODE) {
					if (resultCode == Activity.RESULT_OK) {
						stopReadAndSearch();
						currentZipFileName = stringExtra[0];
						selectedFiles = stringExtra;
						load = "Zip Reader";
						Log.d("ZIP_REQUEST_CODE.currentZFile", currentZipFileName);
						statusView.setText("reading " + currentZipFileName + "...");
						try {
							zr = new ZipReadingTask();
							zr.execute();
						} catch (Exception e) {
							statusView.setText(e.getMessage());
							Log.d("zip result", e.getMessage(), e);
							showToast(e.getMessage());
						} 
					} else if (currentZipFileName.length() == 0) {
						showToast("Nothing to read");
						statusView.setText("Nothing to read");
					}
				} else if (requestCode == COMPARE_REQUEST_CODE1) {
					if (resultCode == Activity.RESULT_OK) {
						oriDoc = stringExtra[0];
						Log.d("COMPARE_REQUEST_CODE1.oriDoc", oriDoc);
						Intent intent = new Intent(activity, FolderChooserActivity.class);
						intent.putExtra(SearchFragment.SELECTED_DIR,
										new String[] { oriDoc });
						intent.putExtra(SearchFragment.SUFFIX, DOC_FILES_SUFFIX);
						intent.putExtra(SearchFragment.MODE, !MULTI_FILES);
						intent.putExtra(SearchFragment.CHOOSER_TITLE,MODI_SUFFIX_TITLE);
						activity.startActivityForResult(intent, COMPARE_REQUEST_CODE2);
					} else { // RESULT_CANCEL
						if (selectedFiles.length == 0) {
							showToast("Nothing to compare");
							statusView.setText("Nothing to compare");
						}
					}
				} else if (requestCode == COMPARE_REQUEST_CODE2) {
					if (resultCode == Activity.RESULT_OK) {
						stopReadAndSearch();
						modifiedDoc = stringExtra[0];
						Log.d("COMPARE_REQUEST_CODE2.modifiedDoc", modifiedDoc);
						currentZipFileName = ""; // làm dấu để khỏi show web getSourceFile
						selectedFiles = new  String[] {oriDoc, modifiedDoc};
						load = "Search";
						requestCompare = true;
						requestSearching = false;
						getSourceFileTask = new GetSourceFileTask();
						getSourceFileTask.execute();
					} else { // RESULT_CANCEL
						if (selectedFiles.length == 0) {
							showToast("Nothing to compare");
							statusView.setText("Nothing to compare");
						}
					}
				} else if (requestCode == GEN_REQUEST_CODE) {
					if (resultCode == Activity.RESULT_OK) {
						Log.d("GEN_REQUEST_CODE.selectedFiles", stringExtra[0]);
						genStardictTask = new GenStardictTask(stringExtra[0]);
						genStardictTask.execute();
					} else { // RESULT_CANCEL
						if (selectedFiles.length == 0) {
							showToast("Nothing to generate");
							statusView.setText("Nothing to generate");
						}
					}
				} else if (requestCode == RESTORE_REQUEST_CODE) {
					if (resultCode == Activity.RESULT_OK) {
						Log.d("RESTORE_REQUEST_CODE.selectedFiles", stringExtra[0]);
						restoreStardictTask = new RestoreStardictTask(stringExtra[0]);
						restoreStardictTask.execute();
					} else { // RESULT_CANCEL
						if (selectedFiles.length == 0) {
							showToast("Nothing to restore");
							statusView.setText("Nothing to restore");
						}
					}
				} else if (requestCode == DUP_REQUEST_CODE) {
					if (resultCode == Activity.RESULT_OK) {
						stopReadAndSearch();
						Log.d("DUP_REQUEST_CODE.selectedFiles", Util.arrayToString(stringExtra, true, LINE_SEP));
						
						if (FileUtils.treeUri == null) {// && !checkFolder(new File(st).getParentFile(), WRITE_REQUEST_CODE)) {
							FileUtils.applicationContext = activity;
							FileUtils.treeUri = FileUtils.getSharedPreferenceUri(R.string.key_internal_uri_extsdcard);
							if (FileUtils.treeUri == null) {
								AlertDialog.Builder alert = new AlertDialog.Builder(activity);
								alert.setTitle("Grant Permission in extSdCard");
								alert.setMessage("In the following Android dialog, " 
												 + "please select the external SD card and confirm at the bottom.");
								alert.setCancelable(true);
								alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											triggerStorageAccessFramework(INTENT_WRITE_REQUEST_CODE);
										}
									});
								alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
										}
									});
								AlertDialog alertDialog = alert.create();
								alertDialog.show();
							}
						}
						dupTask = new DupFinderTask(stringExtra);
						dupTask.execute();
					} else { // RESULT_CANCEL
						if (selectedFiles.length == 0) {
							showToast("Nothing to find");
							statusView.setText("Nothing to find");
						}
					}
				} else if (requestCode == INTENT_WRITE_REQUEST_CODE) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						onActivityResultLollipop(requestCode, resultCode, data);
					}
				} else if (requestCode == TRANSLATE_REQUEST_CODE) {
					if (resultCode == Activity.RESULT_OK) {
						Log.d("TRANSLATE_REQUEST_CODE.selectedFiles", stringExtra[0]);
						stopReadAndSearch();
						currentZipFileName = ""; // làm dấu để khỏi show web getSourceFile
						selectedFiles = stringExtra;
						load = "Search";
						requestCompare = false;
						requestTranslate = true;
						requestSearching = false;
						getSourceFileTask = new GetSourceFileTask();
						getSourceFileTask.execute();
					} else { // RESULT_CANCEL
						if (selectedFiles.length == 0) {
							showToast("Nothing to translate");
							statusView.setText("Nothing to translate");
						}
					}
//				} else if (requestCode == BATCH_REQUEST_CODE_1) {
//					if (resultCode == Activity.RESULT_OK) {
//						selectedFiles = stringExtra;
//						Log.d("BATCH_REQUEST_CODE_1.selectedFiles", Util.arrayToString(selectedFiles, true, "\n"));
//						Intent intent = new Intent(activity, FolderChooserActivity.class);
//						intent.putExtra(SearchFragment.SELECTED_DIR,
//										selectedFiles);
//						intent.putExtra(SearchFragment.SUFFIX, "");
//						intent.putExtra(SearchFragment.MODE, !MULTI_FILES);
//						intent.putExtra(SearchFragment.CHOOSER_TITLE, "Output Folder");
//						activity.startActivityForResult(intent, BATCH_REQUEST_CODE_2);
//					} else { // RESULT_CANCEL
//						if (selectedFiles.length == 0) {
//							showToast("Nothing to convert");
//							statusView.setText("Nothing to convert");
//						}
//					}
//				} else if (requestCode == BATCH_REQUEST_CODE_2) {
//					if (resultCode == Activity.RESULT_OK) {
//						stopReadAndSearch();
//						outputFolder = stringExtra[0];
//						Log.d("BATCH_REQUEST_CODE_2.outputfolder", outputFolder);
//						currentZipFileName = ""; // làm dấu để khỏi show web getSourceFile
//						load = "Convert";
//						
//						Log.i(Prefs.TAG, "onCreate ffmpeg4android ProgressBarExample");
////						demoVideoFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/videokit/";
//						Log.i(Prefs.TAG, getString(R.string.app_name) + " version: " + GeneralUtils.getVersionName(activity.getApplicationContext()) );
//						workFolder = PRIVATE_PATH + "/"; //activity.getApplicationContext().getFilesDir() + "/";
//						Log.i(Prefs.TAG, "workFolder (license and logs location) path: " + workFolder);
//						vkLogPath = PRIVATE_PATH + "/vk.log";
//						Log.i(Prefs.TAG, "vk log (native log) path: " + vkLogPath);
//						GeneralUtils.copyLicenseFromAssetsToSDIfNeeded(activity, workFolder);
//						//GeneralUtils.copyDemoVideoFromAssetsToSDIfNeeded(activity, demoVideoFolder);
//						int rc = GeneralUtils.isLicenseValid(activity.getApplicationContext(), workFolder);
//						Log.i(Prefs.TAG, "License check RC: " + rc);
//						new Thread (new Runnable() {
//								@Override
//								public void run() {
//									List<File> lf = FileUtil.getFiles(selectedFiles);
//									for (File f : lf) {
//										Log.i(Prefs.TAG, "f = " + f);
//										synchronized (command) {
//											convertingFile = f.getAbsolutePath();
//											Log.i(Prefs.TAG, convertingFile + "2=" + convertingFile);
//											runTranscoding();
//											while (!progresssBarFinished) {
//												try {
//													Thread.sleep(250);
//												} catch (InterruptedException e) {}
//											}
//										}
//									}
//								}
//						}).start();
//					} else { // RESULT_CANCEL
//						if (selectedFiles.length == 0) {
//							showToast("Nothing to convert");
//							statusView.setText("Nothing to convert");
//						}
//					}
				} 
			}
		} catch (Throwable t) {
			Log.e("onActivityResult", t.getMessage(), t);
		}
		Log.d("onActivityResult.load", load + " onActivityResult");
		webView.requestFocus();
	}
	
	
	private void stopReadAndSearch() {
		if (searchTask != null) {
			searchTask.cancel(true);
			searchTask = null;
		}
		if (getSourceFileTask != null) {
			getSourceFileTask.cancel(true);
			getSourceFileTask = null;
		}
		if (dupTask != null) {
			dupTask.cancel(true);
			dupTask = null;
		}
		if (genStardictTask != null) {
			genStardictTask.cancel(true);
			genStardictTask = null;
		}
		if (restoreStardictTask != null) {
			restoreStardictTask.cancel(true);
			restoreStardictTask = null;
		}
		if (zr != null) {
			zr.cancel(true);
			zr = null;
		}
	}

	public void showToast(String st) {
		Toast.makeText(activity, st, Toast.LENGTH_LONG).show();
	}

	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case SEARCH_MENU_ID:
//				webView.showFindDialog(currentSearching, true);

				showFind = true;
				container.setVisibility(View.VISIBLE);
				if (findBox.getText().length() == 0) {
					findBox.setText(currentSearching);
				}
				findBox.requestFocus();
				return true;
		}
		return true;
	}

	protected boolean isAlwaysExpanded() {
		return false;
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
	
	public boolean back(MenuItem item) {
		if (webView.canGoBack()) {
			backForward = true;
			webView.goBack();
			String originalUrl = webView.getOriginalUrl();
			if (originalUrl.indexOf("android_asset") < 0) {
				try {
					currentUrl = originalUrl;
					String st = URLDecoder.decode(originalUrl.replaceAll("\\+", "-000000000000000-"), "utf-8").replaceAll("-000000000000000-", "+");
					statusView.setText(st.substring(
										   "file://".length(),st.length()));//.replaceAll("%20", " "));
				} catch (UnsupportedEncodingException e) {
					Log.d("android_asset", e.getMessage(), e);
				}
			} else {
				statusView.setText("Can't show source page");
			}
		} else {
			backForward = false;
			showToast("No more page");
		}
		return true;
	}

	public boolean next(MenuItem item) {
		if (webView.canGoForward()) {
			backForward = true;
			webView.goForward();
			String originalUrl = webView.getOriginalUrl();
			if (originalUrl.indexOf("android_asset") < 0) {
				try {
					currentUrl = originalUrl;
					String st = URLDecoder.decode(originalUrl.replaceAll("\\+", "-000000000000000-"), "utf-8").replaceAll("-000000000000000-", "+");
					statusView.setText(st.substring(
										   "file://".length(),st.length()));//.replaceAll("%20", " "));
				} catch (UnsupportedEncodingException e) {
					Log.d("android_asset", e.getMessage(), e);
				}
			} else {
				
				statusView.setText("Can't show source page");
			}
		} else {
			backForward = false;
			showToast("No more page");
		}
		return true;
	}

		public boolean preview(MenuItem item) {
//			final Dialog dialog = new Dialog(CountingFragment.this);
//			dialog.setContentView(R.layout.preview);
//			dialog.setTitle("Preview");
//
//			Button okBtn = (Button) dialog.findViewById(R.id.okBtn);
//			okBtn.setOnClickListener(new OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						EditText preview = (EditText) dialog.findViewById(R.id.preview);
//						CountingFragment.this.charsPreview =
//							Integer.valueOf(preview.getText().toString());
//						diaLog.dismiss();
//					}
//				});
//
//			Button cancelBtn = (Button) dialog.findViewById(R.id.cancelBtn);
//			cancelBtn.setOnClickListener(new OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						diaLog.dismiss();
//					}
//				});
//			dialog.show();

			LayoutInflater factory = LayoutInflater.from(activity);
			final View textEntryView = factory.inflate(R.layout.preview, null);
			final EditText preview = (EditText) textEntryView.findViewById(R.id.preview);
			preview.setText("" + SearchStack.charsPreview);
			AlertDialog dialog = new AlertDialog.Builder(activity)
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setTitle(R.string.charsPreview)
				.setView(textEntryView)
				.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						try {
							SearchStack.charsPreview = Integer.valueOf(preview.getText().toString());
						} catch (NumberFormatException nfe) {
							showToast("Invalid number. Keep the old value " + SearchStack.charsPreview);
						}
						dialog.dismiss();
					}
				})
				.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
					}
				}).create();
			dialog.show();
			return true;
	}

	public boolean regex(MenuItem item) {
		item.setChecked(!item.isChecked());
		SearchStack.checkRE = item.isChecked();
		Log.d("checkRE", "" + SearchStack.checkRE);
		((SearchStack)activity).mSearchView.requestFocus();
		return true;
	}

	public boolean home(MenuItem item) {
		if (home.length() > 0) {
			currentUrl = home;
			webView.loadUrl(home);
			status = Util.getUrlStatus(home);
			statusView.setText(status);
		}
		return true;
	}

	public boolean viewSource(MenuItem item) {
		/*
		 if (webView != null && sourceContent.length() > 0) {
		 webView.loadDataWithBaseURL("file:///android_asset/", sourceContent, "text/plain", "UTF-8", null);
		 } else {
		 webView.loadUrl("javascript:window.HTMLOUT.processHTML(" +
		 "document.getElementsByTagName('html')[0].innerHTML);");
		 webView.loadData(HtmlSourceViewJavaScriptInterface.source, "text/plain", "UTF-8");
		 Log.d("HtmlSourceViewJavaScriptInterface.source", HtmlSourceViewJavaScriptInterface.source);
		 }
		 */
		try {
			String url = webView.getOriginalUrl();
			if (url != null) {
				url = URLDecoder.decode(url.replaceAll("\\+", "-000000000000000-"), "utf-8").replaceAll("-000000000000000-", "+");
				Log.d("viewSource.url", url + " ");
				String data = FileUtil.readFileWithCheckEncode(
					url.substring("file://".length(), url.length()));//.replaceAll("%20", " "));
				webView.loadDataWithBaseURL("file:///android_asset/", data, "text/plain", "UTF-8", null);
			} else {
				showToast("Nothing to view source");
			}
		}
		catch (IOException e) {
			Log.d(e.getMessage(), webView.getOriginalUrl(), e);
		}

		return true;
	}

	public boolean newSearch(MenuItem item) {
		requestSearching = false;
		Intent intent = new Intent(activity, FolderChooserActivity.class);
		Log.d("newSearch.oldselectedFiles", Util.arrayToString(selectedFiles, true, LINE_SEP));
		Arrays.sort(selectedFiles);
		intent.putExtra(SearchFragment.SELECTED_DIR, selectedFiles);
		intent.putExtra(SearchFragment.SUFFIX, SEARCH_FILES_SUFFIX);
		intent.putExtra(SearchFragment.MODE, MULTI_FILES);
		intent.putExtra(SearchFragment.CHOOSER_TITLE, SEARCH_FILES_TITLE);
		activity.startActivityForResult(intent, SEARCH_REQUEST_CODE);
		return true;
	}

	static final boolean MULTI_FILES = true;
	static final String MODE = "multiFiles";
	String currentZipFileName = "";

	public boolean readZip(MenuItem item) {
		requestSearching = false;
		Intent intent = new Intent(activity, FolderChooserActivity.class);
		Log.d("readZip.previous ZipFile", currentZipFileName);
		intent.putExtra(SearchFragment.SELECTED_DIR,
						new String[] { currentZipFileName });
		intent.putExtra(SearchFragment.SUFFIX, ZIP_SUFFIX);
		intent.putExtra(SearchFragment.MODE, !MULTI_FILES);
		intent.putExtra(SearchFragment.CHOOSER_TITLE,ZIP_TITLE);
		activity.startActivityForResult(intent, ZIP_REQUEST_CODE);
		return true;
	}
	
	String oriDoc = "";
	String modifiedDoc = "";

	public boolean compare(MenuItem item) {
		requestSearching = false;
		Intent intent = new Intent(activity, FolderChooserActivity.class);
		Log.d("compare.previous oriDoc", oriDoc);
		intent.putExtra(SearchFragment.SELECTED_DIR,
						new String[] { oriDoc });
		intent.putExtra(SearchFragment.SUFFIX, DOC_FILES_SUFFIX);
		intent.putExtra(SearchFragment.MODE, !MULTI_FILES);
		intent.putExtra(SearchFragment.CHOOSER_TITLE,ORI_SUFFIX_TITLE);
		activity.startActivityForResult(intent, COMPARE_REQUEST_CODE1);
		return true;
	}

	public boolean restore(MenuItem item) {
		Log.d("restore.previous selectedFiles", selectedFiles + ".");
		Intent intent = new Intent(activity, FolderChooserActivity.class);
		intent.putExtra(SearchFragment.SELECTED_DIR, selectedFiles);
		intent.putExtra(SearchFragment.SUFFIX, IFO_SUFFIX);
		intent.putExtra(SearchFragment.MODE, !MULTI_FILES);
		intent.putExtra(SearchFragment.CHOOSER_TITLE, IFO_SUFFIX_TITLE);
		activity.startActivityForResult(intent, RESTORE_REQUEST_CODE);

		return true;
	}
	
//	private String command = "";
//	public boolean batchConvert(MenuItem item) {
//		copyAssetToDir(SearchFragment.PRIVATE_PATH + "/ffmpeg.org.zip", "data/ffmpeg.org.zip");
//		try {
//			Arc.extractZipToFolder(SearchFragment.PRIVATE_PATH + "/ffmpeg.org.zip", SearchFragment.PRIVATE_PATH + "/ffmpeg.org");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		LayoutInflater factory = LayoutInflater.from(activity);
//		final View textEntryView = factory.inflate(R.layout.parameters, null);
//		final EditText preview = (EditText) textEntryView.findViewById(R.id.preview);
//		preview.setText("ffmpeg -y -i $1 -ar 11025 -q:a 9 -f mp3 $2");
//		//ffmpeg -y -i $1 -strict experimental -vf transpose=1 -s 160x120 -r 30 -aspect 4:3 -ab 48000 -ac 2 -ar 22050 -b 2097k $2");
//		final WebView wv = (WebView) textEntryView.findViewById(R.id.webView);
//		//Log.i("url ff", new File("/storage/emulated/0/Download/HTTrack/Websites/ffmpeg/ffmpeg.org/libavfilter.html").toURI().toString());
//		wv.loadUrl(new File(SearchFragment.PRIVATE_PATH + "/ffmpeg.org/ffmpeg.html").toURI().toString() + "#Video-and-Audio-file-format-conversion");
//		AlertDialog dialog = new AlertDialog.Builder(activity)
//			.setIconAttribute(android.R.attr.alertDialogIcon)
//			.setTitle("Batch Convert Parameters")
//			.setView(textEntryView)
//			.setPositiveButton(R.string.ok,
//			new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int whichButton) {
//					command = preview.getText().toString();
//					dialog.dismiss();
//					Log.d("batchConvert.previous selectedFiles", selectedFiles + ".");
//					Intent intent = new Intent(activity, FolderChooserActivity.class);
//					intent.putExtra(SearchFragment.SELECTED_DIR, selectedFiles);
//					intent.putExtra(SearchFragment.SUFFIX, MEDIA_SUFFIX);
//					intent.putExtra(SearchFragment.MODE, MULTI_FILES);
//					intent.putExtra(SearchFragment.CHOOSER_TITLE, MEDIA_SUFFIX_TITLE);
//					activity.startActivityForResult(intent, BATCH_REQUEST_CODE_1);
//				}
//			})
//			.setNegativeButton(R.string.cancel,
//			new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int whichButton) {
//					dialog.dismiss();
//				}
//			}).create();
//		dialog.show();
//
//		return true;
//	}
	
	public boolean gen(MenuItem item) {
		Log.d("gen.previous selectedFiles", selectedFiles + ".");
		Intent intent = new Intent(activity, FolderChooserActivity.class);
		intent.putExtra(SearchFragment.SELECTED_DIR, selectedFiles);
		intent.putExtra(SearchFragment.SUFFIX, TXT_SUFFIX);
		intent.putExtra(SearchFragment.MODE, !MULTI_FILES);
		intent.putExtra(SearchFragment.CHOOSER_TITLE, TXT_SUFFIX_TITLE);
		activity.startActivityForResult(intent, GEN_REQUEST_CODE);
		return true;
	}

	public boolean translate(MenuItem item) {
		Log.d("translate.previous selectedFiles", selectedFiles + ".");
		Intent intent = new Intent(activity, FolderChooserActivity.class);
		intent.putExtra(SearchFragment.SELECTED_DIR, selectedFiles);
		intent.putExtra(SearchFragment.SUFFIX, DOC_FILES_SUFFIX);
		intent.putExtra(SearchFragment.MODE, !MULTI_FILES);
		intent.putExtra(SearchFragment.CHOOSER_TITLE, TRANSLATE_SUFFIX_TITLE);
		activity.startActivityForResult(intent, TRANSLATE_REQUEST_CODE);
		return true;
	}

	public boolean dupFinder(MenuItem item) {
		Log.d("dupFinder.previous selectedFiles", selectedFiles + ".");
		Intent intent = new Intent(activity, FolderChooserActivity.class);
		intent.putExtra(SearchFragment.SELECTED_DIR, selectedFiles);
		intent.putExtra(SearchFragment.SUFFIX, ALL_SUFFIX);
		intent.putExtra(SearchFragment.MODE, MULTI_FILES);
		intent.putExtra(SearchFragment.CHOOSER_TITLE, ALL_SUFFIX_TITLE);
		activity.startActivityForResult(intent, DUP_REQUEST_CODE);

		return true;
	}
	
	public boolean viewMode(MenuItem item) {
		return true;
	}

	public boolean clearCache(MenuItem item) {

		AlertDialog.Builder alert = new AlertDialog.Builder(activity);
		alert.setTitle("Clear Caching Files");
		long[] entry = new long[] {0, 0, 0};
		FileUtil.getDirSize(PRIVATE_DIR, entry);
		alert.setMessage("Cache has " + nf.format(entry[2]) + " folders, " + nf.format(entry[0])
						 + " files, " + nf.format(entry[1])
						 + " bytes. " + "\r\nAre you sure you want to clear the cached files? "
						 + "\r\nAfter cleaning searching will be slow for the first times " +
						 "and the searching task maybe incorrect.");
		alert.setCancelable(true);

		alert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d("Cleaning files", FileUtil.deleteFiles(
							  PRIVATE_DIR, LINE_SEP, true, ""));
				}
			});

		alert.setPositiveButton("No", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

		AlertDialog alertDialog = alert.create();
		alertDialog.show();
		return true;
	}

	public void searchView(final String query) {
		Log.d("searchView.searchValue", query);
		// nếu folder có thực không thay đổi thì không cần update cache mất thời gian
		if (cache != null) {
			Log.d("searchView.cache != null", "search(" + query + ")");
			searchTask = new SearchTask();
			searchTask.execute(query);
		} else {
			requestSearching = true;
			getSourceFileTask = new GetSourceFileTask();
			getSourceFileTask.execute();
		}
	}

	void createFile() {
		String filename = "myfile.txt";
		String string = "Hello world!";
		FileOutputStream outputStream;

		try {
			outputStream = activity.openFileOutput(filename, Context.MODE_PRIVATE);
			outputStream.write(string.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	boolean requestSearching = false;
	boolean requestCompare = false;
	boolean requestTranslate = false;
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		currentSearching = query;
		findBox.setText(currentSearching);
		statusView.setText("Query = '" + query + "' : submitted");
		webView.requestFocus();
		try {
			// chưa load source files
			if (query.trim().length() == 0) {
				status = "Nothing to search. Please type something for searching";
				showToast(status.toString());
				statusView.setText(status);
				return true;
			}

			Log.d("selectedFiles", selectedFiles + "");
			if (selectedFiles == null || selectedFiles.length == 0 || selectedFiles[0].length() == 0) { // (cache ==
				// null &&
				// (!new
				// File(folderStr).exists()))
				status = "No file to search. Please select files or folders for searching";
				showToast(status.toString());
				statusView.setText(status);
				Log.d("search", "No file to search");
				return true;
			}

			if (getSourceFileTask == null) { // Zip Reader or Restart app
				if (currentZipFileName.length() > 0) { // Zip reader
					AlertDialog.Builder alert = new AlertDialog.Builder(activity);
					alert.setTitle("Searching files");
					alert.setMessage("Do you want to search this file " + currentZipFileName + "?");
					alert.setCancelable(true);
					alert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								showToast("Start loading files, please wait...");
								requestSearching = true;
								getSourceFileTask = new GetSourceFileTask();
								getSourceFileTask.execute();
							}
						});
					alert.setPositiveButton("No", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});
					AlertDialog alertDialog = alert.create();
					alertDialog.show();
				} else { // SEARCH Dup Comp
					showToast("Start loading files, please wait...");
					requestSearching = true;
					getSourceFileTask = new GetSourceFileTask();
					getSourceFileTask.execute();
				}
			} else if (getSourceFileTask.getStatus() == Status.RUNNING 
					   || getSourceFileTask.getStatus() == Status.PENDING
					   ) {
				showToast("Still loading files, please wait...");
				requestSearching = true;
			} else if (searchTask == null || searchTask.getStatus() == Status.FINISHED) {
				// load rồi nhưng chưa chạy gì
				searchView(query);
			} else if (searchTask.getStatus() == Status.RUNNING
					   || searchTask.getStatus() == Status.PENDING) {
				  // đã load source file nhưng đang chạy cái khác
				showToast("Stopping previous seaching, please wait...");
				searchTask.cancel(true);
				searchTask = null;
				searchView(query);
			} 
		} catch (Throwable e) {
			Log.d("onQueryTextSubmit()", e.getMessage(), e);
			statusView.setText(e.getMessage());
		}
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// statusView.setText("Query = " + newText);
//		currentPause = 8000;
//		extendPause(0);
		return true;
	}
	
	private NotificationManager mNotificationManager;
	
	private PendingIntent makeMoodIntent(int moodId) {
        // The PendingIntent to launch our activity if the user selects this
        // notification.  Note the use of FLAG_UPDATE_CURRENT so that if there
        // is already an active matching pending intent, we will update its
        // extras (and other Intents in the array) to be the ones passed in here.
        PendingIntent contentIntent = PendingIntent.getActivity(activity, 0,
																new Intent(activity, SearchStack.class).putExtra("moodimg", moodId),
																PendingIntent.FLAG_UPDATE_CURRENT);
        return contentIntent;
    }
	
	private void setMood(int moodId, String text, boolean showTicker) {
//        // In this sample, we'll use the same text for the ticker and the expanded notification
//        CharSequence text = getText(textId);
//
//        // choose the ticker text
        String tickerText = showTicker ? text : null;

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(moodId, tickerText,
													 System.currentTimeMillis());

        // Set the info for the views that show in the notification panel.
//        notification.setLatestEventInfo(activity, "Searching finished",
//										text, makeMoodIntent(moodId));
		notification.defaults = Notification.DEFAULT_ALL;
        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNotificationManager.notify(MOOD_NOTIFICATIONS, notification);
    }
	
	/**
	 * String searchValue String publish current status String result String
	 * status
	 * 
	 * @author Dell
	 * 
	 */
	private class SearchTask extends AsyncTask<String, String, String> {
		
		private static final String SEARCH_TITLE = HTML_STYLE
		+ "<title>Search Result</title>\r\n" 
		+ HEAD_TABLE;
		private int matched = 0;
		private int charsPreview = 512;

		protected String doInBackground(String... search) {
			int count = files.length;
//			long totalSize = 0;
			if (search[0].trim().length() == 0) {
				return "Nothing to search. Please type something for searching";
			}

//			if (searchFileResult != null && searchFileResult.exists()) {
//				FileUtil.delete(searchFileResult);
//			}
//			if (searchFileResult == null || searchFileResult.length() == 0) {
			searchFileResult = PRIVATE_PATH +
				"/SearchResult_" 
				+ df.format(System.currentTimeMillis()).replaceAll("[/\\?<>\"':|]", "_") + ".html";
//			}
			File searchFile = new File(searchFileResult);
//			searchFile.delete();
			
			//tempFList.add(searchFileResult.getAbsolutePath());
			// Log.d("writeable", (searchFileResult + " can write: " +
			
			String resultStr = "";
			StringBuilder sb = new StringBuilder(4096);
			sb.append(SEARCH_TITLE);
			
			try {
				long timeMillis = System.currentTimeMillis();
				matched = 0;
				// chạy xong display thì cache cũng empty
				cache.reset();
				// Map.Entry<File, Map.Entry<String, String>> entry = null; //
				// for DoubleCache
				File f = null;
				int numFileSearched = 0;
				boolean checkRE2 = SearchStack.checkRE;
				charsPreview = SearchStack.charsPreview;
				String query = search[0].toLowerCase();
				
				while (cache.hasNext()) { // && isAlive
					f = cache.next();
					Log.d("Searching", f.getAbsolutePath());
					// currentContent = entry.getValue().getKey(); // for
					// DoubleCache
					// contentLower = entry.getValue().getValue(); // for
					// DoubleCache
					currentContent = cache.get(f);
//					contentLower = currentContent.toLowerCase();
//					totalSize += currentContent.length();
					publishProgress(new StringBuilder("Searching ")
									.append(f.getAbsolutePath()).append(": ")
									.append(++numFileSearched).append("/")
									.append(count).toString());
					
					
					if (isCancelled())
						break;
					matcher(sb, query, f, checkRE2);
					if (sb.length() > 65535) {
						FileUtil.writeAppendFileAsCharset(searchFile, sb.toString(), Util.UTF8);
						sb = new StringBuilder();
					}
				}
				resultStr = new StringBuilder(matched + "")
					.append(" matches, result size ").append(nf.format(searchFile.length() + sb.toString().getBytes().length + 180))
					.append(" bytes, cached ").append(nf.format(cache.cached())).append("/").append(count)
					.append(" files, cached size ").append(nf.format(cache.getCurrentSize())).append("/")
					.append(nf.format(cache.getTotalSize()))
					.append(" bytes, took ")
					.append(nf.format(System.currentTimeMillis() - timeMillis))
					.append(" milliseconds.").toString();
				sb.append("</table>\r\n<strong><br/>")
					.append(resultStr)
					.append("</p></strong>\r\n</div>\r\n</body>\r\n</html>");
				FileUtil.writeAppendFileAsCharset(searchFile, sb.toString(), Util.UTF8);
			} catch (Throwable e) {
				publishProgress("Result length " + sb.length() + ", error: " + e.getMessage() 
								+ ". Free memory is " + nf.format(Runtime.getRuntime().freeMemory())
								+ ". Max memory is " + nf.format(Runtime.getRuntime().maxMemory()));
				Log.d("SearchTask.doInBackground", e.getMessage(), e);
				return null;
			}
			return resultStr;
		}

		protected void onProgressUpdate(String... progress) {
			if (progress != null && progress.length > 0 
				&& progress[0] != null && progress[0].trim().length() > 0) {
				statusView.setText(progress[0]);
			}
		}

		protected void onPostExecute(String result) {
			try {
				if (result == null) {
					// showToast("Out of memory: " + Runtime.getRuntime().freeMemory());
					// statusView.setText("Out of memory: " + Runtime.getRuntime().freeMemory());
				} else {
					statusView.setText(result);
					if (!result.startsWith("Nothing")) {
						currentUrl = new File(searchFileResult).toURI().toURL().toString();
						locX = 0;
						locY = 0;
						webView.loadUrl(currentUrl);
						// home = currentUrl;
						Log.d("currentUrl 5", currentUrl);
					}
					Log.d("SearchTask", result);
					// setDefault(Notification.DEFAULT_ALL, "Searching " + currentSearching + " finished");
//					setMood(R.drawable.stat_happy, "Searching '" + currentSearching + "' finished",
//							true);
					showToast("Searching '" + currentSearching + "' finished");
				}
			} catch (Throwable e) {
				publishProgress(e.getMessage());
				Log.e("SearchTask.onPostExecute", e.getMessage(), e);
			}
		}

		private int matcher(StringBuilder sb, String pattern, File inFile, boolean checkRE) throws IOException {
			boolean found = false;
			int presentCursorPos = 0;
			int curFoundPos = -1;
			int nextFindPos = -1;
			int repeatedFindNextCurPos;
			
			if (!checkRE) {
				// TODO local tránh multithread
				// TODO cần replaceAll cả ở query thay vì ở đây thôi 
				// String caseStr = Util.replaceAll(currentContent, new  String[]{"&", "<"}, 
//												 new  String[]{"&amp;", "&lt;"});
//				StringBuilder lowerStr = new StringBuilder(caseStr.toLowerCase()); //Util.replaceAll(contentLower, "<",
				// "&lt;"); // contentLower; //
				contentLower = currentContent.toLowerCase();
				int contentLength = currentContent.length();
				int patternLength = pattern.length();

				while ((presentCursorPos + patternLength < contentLength) // isAlive &&
					   && ((curFoundPos = contentLower.indexOf(pattern, presentCursorPos)) > -1)) {
					if (!found) {
						sb.append(
							"<tr>\r\n<td width='100%' colspan='2' align='left' style='border:solid black 1.0pt; padding:1.4pt 1.4pt 1.4pt 1.4pt;'><strong>")
							.append(inFile.getAbsolutePath())
							.append(" [number of characters: ")
							.append(nf.format(contentLength))
							.append("]</strong></td>\r\n</tr>\r\n");
						found = true;
					}

					sb.append("<tr>\n").append(TD1).append("<a href=\"")
						.append(inFile.toURI().toURL().toString())
						// .append("?counter=").append(++counter)
						// .append("' target='_blank'>")
						.append("\">")
						.append(++matched)
						.append("</a>\n</td>\n").append(TD2);

					// sb.append("<a title='").append(inFile.getAbsolutePath())
					// .append("'>");
					Log.d("presentCursorPos, curFoundPos", presentCursorPos + ", " + curFoundPos);
					Util.replaceAll(
						currentContent,
						curFoundPos - ((curFoundPos > charsPreview) ? charsPreview : curFoundPos), 
						curFoundPos, sb, new String[]{"&", "<", "\n"}, 
						new String[]{"&amp;", "&lt;", "<br/>"});

					sb.append("<b><font color='blue'>");
					nextFindPos = curFoundPos + patternLength;
					Util.replaceAll(currentContent, curFoundPos, nextFindPos, sb,
									new String[]{"&", "<", "\n"}, 
									new String[]{"&amp;", "&lt;", "<br/>"});
					sb.append("</font></b>");
					presentCursorPos = nextFindPos;
					Log.d("presentCursorPos, curFoundPos, nextFindPos", presentCursorPos + ", " + curFoundPos + ", " + nextFindPos);

					while (presentCursorPos > (repeatedFindNextCurPos = contentLower.indexOf(pattern, presentCursorPos)) - charsPreview
						   && repeatedFindNextCurPos >= presentCursorPos) {
						presentCursorPos = repeatedFindNextCurPos + patternLength;
						Log.d("presentCursorPos, repeatedFindNextCurPos", presentCursorPos + ", " + repeatedFindNextCurPos);
						Util.replaceAll(currentContent, nextFindPos, repeatedFindNextCurPos, sb,
										new String[]{"&", "<", "\n"}, 
										new String[]{"&amp;", "&lt;", "<br/>"});
						sb.append("<b><font color='blue'>");
						Util.replaceAll(currentContent, repeatedFindNextCurPos, presentCursorPos, sb,
										new String[]{"&", "<", "\n"}, 
										new String[]{"&amp;", "&lt;", "<br/>"});
						sb.append("</font></b>");
						nextFindPos = presentCursorPos;
					}

					if (presentCursorPos > nextFindPos) {

					} else {
						Util.replaceAll(currentContent, nextFindPos, 
										Math.min(contentLength, presentCursorPos + charsPreview), sb,
										new String[]{"&", "<", "\n"}, 
										new String[]{"&amp;", "&lt;", "<br/>"});
						Log.d("nextFindPos, min(contentLength, presentCursorPos + charsPreview)2", nextFindPos + ", " + contentLength + ", " + (presentCursorPos + charsPreview));
					}
					sb.append("\n</td>\n</tr>\n");
				}
			} else {
				Pattern pat = Pattern.compile(pattern, Pattern.UNICODE_CASE);
				Matcher mat = pat.matcher(currentContent.toLowerCase()); 
				int contentLength = currentContent.length();
				while (mat.find(presentCursorPos)) { // && isAlive
					if (!found) {
						sb.append(
							"<tr>\r\n<td width='100%' colspan='2' align='left' "
							+ "style='border:solid black 1.0pt; padding:1.4pt 1.4pt 1.4pt 1.4pt;'><strong>")
							.append(inFile.getAbsolutePath())
							.append(" [number of characters: ")
							.append(contentLength)
							.append("]</strong></td>\r\n</tr>\r\n");
						found = true;
					}

					curFoundPos = mat.start();

					sb.append("<tr>\r\n").append(TD1).append("<a href=\"")
						.append(inFile.toURI().toURL().toString())
						// .append("?counter=").append(++counter)
						.append("\">")
						.append(++matched)
						.append("</a>\r\n</td>\r\n").append(TD2);

					// sb.append("<a title='").append(inFile.getAbsolutePath())
					//		.append("'>");
					Util.replaceAll(
						currentContent,
						curFoundPos - ((curFoundPos > charsPreview) ? charsPreview : curFoundPos), 
						curFoundPos, sb, 
						new String[]{"&", "<", "\n"}, 
						new String[]{"&amp;", "&lt;", "<br/>"});

					sb.append("<b><font color='blue'>");
					nextFindPos = curFoundPos + (mat.end() - mat.start());
					Util.replaceAll(currentContent, curFoundPos, nextFindPos, sb,
									new String[]{"&", "<", "\n"}, 
									new String[]{"&amp;", "&lt;", "<br/>"});
					sb.append("</font></b>");

					presentCursorPos = nextFindPos;

					while (mat.find(presentCursorPos) && presentCursorPos > (repeatedFindNextCurPos = mat.start()) - charsPreview
						   && repeatedFindNextCurPos >= presentCursorPos) {
						presentCursorPos = repeatedFindNextCurPos + (mat.end() - mat.start());
						sb.append(Util.replaceAll(currentContent.substring(nextFindPos, 
																		   repeatedFindNextCurPos), 
												  new String[]{"&", "<", "\n"}, 
												  new String[]{"&amp;", "&lt;", "<br/>"}));
						sb.append("<b><font color='blue'>");
						Util.replaceAll(currentContent, repeatedFindNextCurPos, presentCursorPos, sb,
										new String[]{"&", "<", "\n"}, 
										new String[]{"&amp;", "&lt;", "<br/>"});
						sb.append("</font></b>");
						nextFindPos = presentCursorPos;
					}
					Util.replaceAll(currentContent, nextFindPos, 
									Math.min(contentLength, presentCursorPos + charsPreview), sb,
									new String[]{"&", "<", "\n"}, 
									new String[]{"&amp;", "&lt;", "<br/>"});
					sb.append("\n</td>\n</tr>\n");
				}
			}
			return matched;
		}
	}
	
	private class CompareTask extends AsyncTask<Void, String, String> {

		private static final String COMPARE_TITLE = HTML_STYLE
		+ "<title>Document Compare Result</title>\r\n" 
		+ HEAD_TABLE;

		@Override
		protected String doInBackground(Void[] p1) {
//					TextCompare tc = new TextCompare();
//					File file1 = new File(oriDoc);
//					File file2 = new File(modifiedDoc);
//					statusView.setText("Comparing \"" + file1 + "\" and \"" + file2 + "\"");
//					File f = tc.compareText(file1, file2);
//					currentUrl = f.toURL().toString();
//					result = "Compare \"" + file1 + "\" and \"" + file2
//						+ "\" finished";
//					(webTask = new WebTask(webView, currentUrl, true, result)).execute();

			try {
				File f1 = getSourceFileTask.convertedFileList.get(0);
				File f2 = getSourceFileTask.convertedFileList.get(1);
				publishProgress("Comparing \"" + oriDoc + "\" and \"" + modifiedDoc + "\"");
				String oriStr = FileUtil.readFileAsCharset(f1, "utf-8");
				String modiStr = FileUtil.readFileAsCharset(f2, "utf-8");
				String compareText = DiffMatchPatch.compare(oriStr, modiStr);
				File fret = new File(PRIVATE_PATH + oriDoc + "_" + f2.getName() + ".html");
				FileUtil.writeFileAsCharset(fret, compareText, "utf-8");

				StringBuilder sb = new  StringBuilder(COMPARE_TITLE);
				sb.append(
					"<tr>\n<td width='100%' colspan='3' align='center' style='border:solid black 1.0pt; padding:1.4pt 1.4pt 1.4pt 1.4pt;'><strong>")
					.append("Compare " + f1 + "<br/> and " + f2)
					//.append(" [number of characters: ")
					//.append(f1.length() + f2.length() + "")
					//.append("]")
					//.append("<br/>Number of differences: ").append(tc.wordDifferences).append(" words")
					.append("</strong></td>\n</tr>\n");

				NumberFormat instance = NumberFormat.getInstance();
				sb.append("<tr>\n")
					.append(TD_COMPARE1)
					.append("<a href=\"" + f1.toURL() + "\">" + f1.getName() + " [" + instance.format(f1.length()) + " bytes]" + "</a>")
					.append("\n</td>\n")
					.append(TD_COMPARE1)
					.append("<a href=\"" + f2.toURL() + "\">" + f2.getName() + " [" + instance.format(f2.length()) +" bytes]" + "</a>")
					.append("\n</td>\n")
					.append(TD_COMPARE2)
					.append("<a href=\"" + fret.toURL() + "\">" + fret.getName() + " [" + instance.format(compareText.getBytes().length) +" bytes]" + "</a>")
					.append("\n</td>\n")
					.append("</tr>");

				sb.append("<tr>\n")
					.append(TD_COMPARE1)
					.append(oriStr.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll("\n", "<br/>"))
					.append("\n</td>\n")
					.append(TD_COMPARE1)
					.append(modiStr.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll("\n", "<br/>"))
					.append("\n</td>\n")
					.append(TD_COMPARE2)
					.append(compareText)
					.append("\n</td>\n")
					.append("</tr></table>\n<strong><br/>")
					.append("</strong></div>\n</body>\n</html>");
				String name = PRIVATE_PATH + oriDoc + "_" + f2.getName() + ".all.html";
				FileUtil.writeContentToFile(name, sb.toString());

				currentUrl = new File(name).toURL().toString();
				String result = "Compare \"" + oriDoc + "\" and \"" + modifiedDoc
					+ "\" finished";
				(webTask = new WebTask(webView, currentUrl, true, result)).execute();
			} catch (IOException e) {
				publishProgress(e.getMessage());
				Log.d("compare", e.getMessage(), e);
			}
			requestCompare = false;
			return null;
		}

		protected void onProgressUpdate(String... progress) {
			if (progress != null && progress.length > 0 
				&& progress[0] != null && progress[0].trim().length() > 0) {
				statusView.setText(progress[0]);
				Log.d("CompareTask", progress[0]);
			}
		}

		protected void onPostExecute(String result) {
			showToast("Document Compare finished");
		}
	}
	
//	private volatile boolean progresssBarFinished = true;
//	private String outputFolder = "";
//	public ProgressDialog progressBar;
//
//	String workFolder = null;
////	String demoVideoFolder = null;
////		String demoVideoPath = null;
//	String vkLogPath = null;
//	LoadJNI vk;
//	private final int STOP_TRANSCODING_MSG = -1;
//	private final int FINISHED_TRANSCODING_MSG = 0;
//	private boolean commandValidationFailedFlag = false;
//	private String[] complexCommand = null;
//	private volatile String convertingFile = "";
//
//	private void runTranscodingUsingLoader() {
//		Log.i(Prefs.TAG, "runTranscodingUsingLoader started..." + convertingFile);
//
//		PowerManager powerManager = (PowerManager)activity.getSystemService(Activity.POWER_SERVICE);
//		android.os.PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK"); 
//		Log.d(Prefs.TAG, "Acquire wake lock");
//		wakeLock.acquire();
//
//// 		EditText commandText = (EditText)findViewById(R.id.CommandText);
//
//		//String commandStr = commandText.getText().toString();
//		///////////// Set Command using code (overriding the UI EditText) /////
//		//String commandStr = "ffmpeg -y -i /sdcard/videokit/in.mp4 -strict experimental -s 320x240 -r 30 -aspect 4:3 -ab 48000 -ac 2 -ar 22050 -vcodec mpeg4 -b 2097152 /sdcard/videokit/out.mp4";
//		//String[] complexCommand = {"ffmpeg", "-y" ,"-i", "/sdcard/videokit/in.mp4","-strict","experimental","-vcodec", "mpeg4", "-b", "150k", "-s", "320x240","-r", "25", "-ab", "48000", "-ac", "2", "-ar", "22050","/sdcard/videokit/out.mp4"};
//
//		// String[] complexCommand = command.split(" +");
//		for (int i = 0; i < complexCommand.length; i++) {
//			String st = complexCommand[i];
//			if ("$1".equals(st)) {
//				complexCommand[i] = convertingFile;
//			}
//			if ("$2".equals(st)) {
//				complexCommand[i] = outputFolder + convertingFile;
//				new File(complexCommand[i]).getParentFile().mkdirs();
//			}
//		}
//
//		Log.i("command", Util.arrayToString(complexCommand, true, ", "));
////			{"ffmpeg","-y","-i","/storage/emulated/0/videokit/in.mp4",
////				"-i","/storage/emulated/0/videokit/in.mp4","-strict","experimental",
////				"-filter_complex",
////				"[0:v]scale=640x480,setsar=1:1[v0];[1:v]scale=640x480,setsar=1:1[v1];[v0][v1] concat=n=2:v=1:a=0",
////				"-ab","48000","-ac","2","-ar","22050","-s","640x480","-r","30","-vcodec","mpeg4","-b","2097k","/storage/emulated/0/videokit/out.mp4"};
//		///////////////////////////////////////////////////////////////////////
//
//
//		vk = new LoadJNI();
//		try {
//			// running complex command with validation 
//			vk.run(complexCommand, workFolder, activity.getApplicationContext());
//
//			// running without command validation
//			//vk.run(complexCommand, workFolder, getApplicationContext(), false);
//
//			// running regular command with validation
//			//vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, activity.getApplicationContext());
//
//			Log.i(Prefs.TAG, "vk.run finished.");
//			// copying vk.log (internal native log) to the videokit folder
////			GeneralUtils.copyFileToFolder(vkLogPath, demoVideoFolder);
//
//		} catch (CommandValidationException e) {
//			Log.e(Prefs.TAG, "vk run exeption.", e);
//			commandValidationFailedFlag = true;
//
//		} catch (Throwable e) {
//			Log.e(Prefs.TAG, "vk run exeption.", e);
//		} finally {
//			progresssBarFinished = true;
//			if (progressBar != null) {
//				progressBar.dismiss();
//			}
//			if (wakeLock.isHeld()) {
//				wakeLock.release();
//				Log.i(Prefs.TAG, "Wake lock released");
//			}
//			else{
//				Log.i(Prefs.TAG, "Wake lock is already released, doing nothing");
//			}
//		}
//
//		// finished Toast
//		String rc = null;
//		if (commandValidationFailedFlag) {
//			rc = "Command Vaidation Failed";
//		}
//		else {
//			rc = GeneralUtils.getReturnCodeFromLog(vkLogPath);
//		}
//		final String status = rc;
//		activity.runOnUiThread(new Runnable() {
//				public void run() {
//					Toast.makeText(activity, status, Toast.LENGTH_LONG).show();
//					if (status.equals("Transcoding Status: Failed")) {
//						Toast.makeText(activity, "Check: " + vkLogPath + " for more information.", Toast.LENGTH_LONG).show();
//					}
//				}
//			});
//	}
//
//
////		@Override
////		public void onCreate(Bundle savedInstanceState) {
////			Log.i(Prefs.TAG, "onCreate ffmpeg4android ProgressBarExample");
////
////			demoVideoFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/videokit/";
////			demoVideoPath = demoVideoFolder + "in.mp4";
////
////			Log.i(Prefs.TAG, getString(R.string.app_name) + " version: " + GeneralUtils.getVersionName(SearchFragment.this.getActivity().getApplicationContext()) );
////
////			workFolder = activity.getApplicationContext().getFilesDir() + "/";
////			Log.i(Prefs.TAG, "workFolder (license and logs location) path: " + workFolder);
////			vkLogPath = workFolder + "vk.log";
////			Log.i(Prefs.TAG, "vk log (native log) path: " + vkLogPath);
////			GeneralUtils.copyLicenseFromAssetsToSDIfNeeded(activity, workFolder);
////			//GeneralUtils.copyDemoVideoFromAssetsToSDIfNeeded(activity, demoVideoFolder);
////			int rc = GeneralUtils.isLicenseValid(activity.getApplicationContext(), workFolder);
////			Log.i(Prefs.TAG, "License check RC: " + rc);
////		}
//
//	private Handler handler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//			Log.i(Prefs.TAG, "Handler got message");
//			try {
//				if (progressBar != null) {
//					progressBar.dismiss();
//
//					// stopping the transcoding native
//					if (msg.what == STOP_TRANSCODING_MSG) {
//						Log.i(Prefs.TAG, "Got cancel message, calling fexit");
//						vk.fExit(activity.getApplicationContext());
//					}
//				}
//			} catch (Throwable t) {
//				t.printStackTrace();
//			} finally {
//				progresssBarFinished = true;
//			}
//		}
//	};
//
//	public void runTranscoding() {
//		
//		
//		Log.i("command", command);
//		complexCommand = command.split(" +");
////						new String[] {"ffmpeg","-y", "-i","/storage/emulated/0/videokit/in.mp4",
////				"-i","/storage/emulated/0/videokit/in.mp4","-strict","experimental",
////				"-filter_complex",
////				"[0:v]scale=640x480,setsar=1:1[v0];[1:v]scale=640x480,setsar=1:1[v1];[v0][v1] concat=n=2:v=1:a=0",
////				"-ab","48000","-ac","2","-ar","22050","-s","640x480","-r","30","-vcodec","mpeg4","-b","2097k","/storage/emulated/0/videokit/out.mp4"};
//		
//		new Thread() {
//			public void run() {
//				synchronized (command) {
//					Log.d(Prefs.TAG,"Worker started " + convertingFile);
//					try {
//						//sleep(5000);
//						Log.i("command.notified()1", "notified");
//						runTranscodingUsingLoader();
//						handler.sendEmptyMessage(FINISHED_TRANSCODING_MSG);
//						new File(outputFolder + convertingFile).setLastModified(new File(convertingFile).lastModified());
//						command.notify();
//						Log.i("command.notified()2", "notified");
//					} catch(Throwable e) {
//						Log.e("threadmessage",e.getMessage());
//					}
//				}
//			}
//		}.start();
//
//		activity.runOnUiThread(new  Runnable() {
//
//				@Override
//				public void run() {
//					progresssBarFinished = false;
//					progressBar = new ProgressDialog(activity);
//					progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//					progressBar.setTitle("Converting " + convertingFile);
//					progressBar.setMessage("Press the cancel button to end the operation");
//					progressBar.setMax(100);
//					progressBar.setProgress(0);
//
//					progressBar.setCancelable(false);
//					progressBar.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//								handler.sendEmptyMessage(STOP_TRANSCODING_MSG);
//							}
//						});
//
//					progressBar.show();
//					// Progress update thread
//					new Thread() {
//						ProgressCalculator pc = new ProgressCalculator(vkLogPath);
//						public void run() {
//
//							Log.d(Prefs.TAG,"Progress update started");
//							int progress = -1;
//							try {
//								while (true) {
//									sleep(300);
//									progress = pc.calcProgress();
//									if (progress != 0 && progress < 100) {
//										progressBar.setProgress(progress);
//									}
//									else if (progress == 100) {
//										Log.i(Prefs.TAG, "==== progress is 100, exiting Progress update thread");
//										pc.initCalcParamsForNextInter();
//										break;
//									}
//								}
//							} catch(Throwable e) {
//								Log.e("threadmessage",e.getMessage());
//							} finally {
//								progresssBarFinished = true;
//							}
//						}
//					}.start();
//				}
//		});
//		
//		try {
//			Log.i("command.wait();", "waiting");
//			command.wait();
//			Log.i("command.notified();", "notified");
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}
		
	
	class GetSourceFileTask extends AsyncTask<Void, String, String> {
		String resultStr = "Nothing to load";
//		int totalFileSizeRead = 0;
		// folder đã chọn gồm cả text lẫn misc
		private List<File> initFolderFiles = new LinkedList<File>();
		// sau khi đã filter folder và file và chỉ chọn theo suffix
		private List<File> readTextFiles = new LinkedList<File>();
		volatile List<File> convertedFileList;
//		volatile Collection<String> entryFileList = new HashSet<String>();

		protected String doInBackground(Void... urls) {
			// int count = urls.length;
			// long totalSize = 0;
			synchronized (PRIVATE_PATH) {
			Log.d("lock start", df.format(System.currentTimeMillis()));
			currentContent = "";
			contentLower = "";
			
			Log.d("selectedFiles", Util.arrayToString(selectedFiles, true, LINE_SEP));
			convertedFileList = new LinkedList<File>();
			File f;
			errorProcessFiles = new StringBuilder(TITLE_ERROR_PROCESSING_FILES);
			initFolderFiles = new LinkedList<File>();
			readTextFiles = new LinkedList<File>();
			for (int i = 0; i < selectedFiles.length; i++) {
				f = new File(selectedFiles[i]);
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
			files = new File[convertedFileList.size()];
			files = convertedFileList.toArray(files);
				Log.d("getSourceFile fList", Util.collectionToString(convertedFileList, true, LINE_SEP));

			cache = null;
			//convertedFileList = FileUtil.getFiles(files, SEARCH_FILES_SUFFIX);
				Log.d("getSourceFile filtered", Util.collectionToString(convertedFileList, true, LINE_SEP));

			try {
				long milliTime = System.currentTimeMillis();
				// list file converted
				convertedFileList = readFile(convertedFileList);
				Log.d("fileList", Util.collectionToString(convertedFileList, true, LINE_SEP));
				publishProgress("Free memory = " + nf.format(Runtime.getRuntime().freeMemory()) 
								+ " bytes. Caching...");
				cache = new Cache(convertedFileList);
				
				resultStr = new StringBuilder("Processed ")
					.append(nf.format(convertedFileList.size()))
					.append(" files, cached ").append(nf.format(cache.cached()))
					.append(" files, cached size ").append(nf.format(cache.getCurrentSize()))
					.append("/").append(nf.format(cache.getTotalSize()))
					.append(" bytes, took: ")
					.append(nf.format(System.currentTimeMillis() - milliTime))
					.append(" milliseconds. Current free memory ")
					.append(nf.format(Runtime.getRuntime().freeMemory()))
					.append(" bytes.").toString();
				publishProgress(resultStr);

				files = new File[convertedFileList.size()];
				convertedFileList.toArray(files);

				if (files.length == 1 && files[0].isFile()) {
					Log.d("getSourceFile isFile", files[0].toString());
					if (cache.hasNext()) {
						// Map.Entry<File, Map.Entry<String, String>> entry = cache.next(); // for DoubleCache
						f = cache.next();
						// currentContent = entry.getValue().getKey(); // for DoubleCache
						// contentLower = entry.getValue().getValue(); // for DoubleCache
						currentContent = cache.get(f);
						contentLower = currentContent.toLowerCase();
						Log.d("currentContent", currentContent);
					}
					// không có lỗi
					if (errorProcessFiles.length() == TITLE_ERROR_PROCESSING_FILES.length()) {
						currentUrl = files[0].toURI().toURL().toString();
						// (webTask = new WebTask(webView, displayData)).execute();
						Log.d("currentUrl 1", currentUrl);
					} else {
						// có lỗi
						currentUrl = new StringBuilder(EMPTY_HEAD).append(
							"Chosen files: <br/>").append(
							filesToHref(initFolderFiles)).append(
							errorProcessFiles).append(
							(readTextFiles.size() > 0) ? new StringBuilder("<br/> Converted files: <br/>").append(filesToHref(readTextFiles)) : "").append(
							END_BODY_HTML).toString();
						// displayData = loadUrl(webView, displayData);
						Log.d("currentUrl 2", currentUrl);
					}
				} else if (files.length > 0) {
					Log.d("getSourceFile files.length", "" + files.length);
					currentContent = "";
					contentLower = "";
					readTextFiles = convertedFileList;
					if (errorProcessFiles.length() > TITLE_ERROR_PROCESSING_FILES.length()) {
						currentUrl = new StringBuilder(EMPTY_HEAD).append(
							"Chosen files: <br/>").append(
							filesToHref(initFolderFiles)).append( // "Converted succesfully <br />"
							errorProcessFiles).append(
							(readTextFiles.size() > 0) ? new StringBuilder("<br/> Converted files: <br/>").append(filesToHref(readTextFiles)) : "").append( // filesToHref(readTextFiles)
							END_BODY_HTML).toString();
						// displayData = loadUrl(webView, displayData);
					} else {
						currentUrl = new StringBuilder(EMPTY_HEAD).append(
							"Chosen files: <br/>").append(
							filesToHref(initFolderFiles)).append( // "Converted succesfully <br />"
							((readTextFiles.size() > 0) ? new StringBuilder("<br/> Converted files: <br/>").append(filesToHref(readTextFiles)) : "")).append( // filesToHref(readTextFiles)
							END_BODY_HTML).toString();
						// displayData = loadUrl(webView, displayData);
					}
					Log.d("currentUrl 3", currentUrl);
				} else if (files.length == 0) {
					currentUrl = new StringBuilder(EMPTY_HEAD).append(
						"Chosen files: <br/>").append(
						filesToHref(initFolderFiles)).append(
						errorProcessFiles).append(
						(readTextFiles.size() > 0) ? new StringBuilder("<br/> Converted files: <br/>").append(filesToHref(readTextFiles)) : "").append(
						END_BODY_HTML).toString();
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
//				publishProgress("Current memory is " + nf.format(Runtime.getRuntime().freeMemory())
//								+ ", max memory is " + nf.format(Runtime.getRuntime().maxMemory()));
				Log.e("SearchTask", e.getMessage(), e);
			}
			}
			return resultStr;
		}

		protected void onProgressUpdate(String... progress) {
			if (progress != null && progress.length > 0 
				&& progress[0] != null && progress[0].trim().length() > 0) {
				statusView.setText(progress[0]);
				Log.d("GetTask.publish", progress[0]);
			}
		}

		protected void onPostExecute(final String result) {
			try {
				// chỉ khi nào thuần search thì mới show, còn 
// xài chung với ReadZip thì khỏi show
				// sourceContent = currentUrl;
				if (currentZipFileName.length() == 0) {
					if (currentUrl.startsWith("file:/")) {
						(webTask = new WebTask(webView, currentUrl, true, result)).execute();
//						webView.loadUrl(currentUrl);
					} else {
						loadUrl(webView, currentUrl);
						home = currentUrl;
					}
				}
				statusView.setText(result);
				if (requestCompare) {
					(compTask = new CompareTask()).execute();
				} else if (requestSearching) {
					if (searchTask != null) {
						searchTask.cancel(true);
					}
					searchTask = new SearchTask();
					searchTask.execute(currentSearching);
					requestSearching = false;
				} else if (requestTranslate) {
					if (translateTask != null) {
						translateTask.cancel(true);
					}
					translateTask = new TranslateTask(SearchFragment.this, getSourceFileTask.convertedFileList.get(0).getAbsolutePath());
					translateTask.execute();
					requestTranslate = false;
				}
				if (SearchStack.autoBackup) {
					new Thread(new Runnable() {
							@Override
							public void run() {
								for (String st : selectedFiles) {
									File f = new File(st);
									if (f.isDirectory()) {
										File fp = new File(PRIVATE_PATH + st);
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
				statusView.setText(e.getMessage());
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
			if (inFilePath.startsWith(PRIVATE_PATH)) {
				newFile = new File(inFilePath + Util.CONVERTED_TXT);
			} else {
				newFile = new File(PRIVATE_PATH + inFilePath + Util.CONVERTED_TXT);
			}
			// file text được chọn đã được convert từ trước
			if (newFile.exists() // && "Search".equals(load)
				&& (newFile.lastModified() > inFile.lastModified())) {
				publishProgress("already converted " + inFilePath);
				fileList.add(newFile);
				Log.d("already converted newFile", String.valueOf(newFile));
				return;
			}
			String inFilePathLowerCase = inFilePath.toLowerCase();
			currentContent = null;

			String fileExtensionFromUrl = MimeTypeMap.getFileExtensionFromUrl(inFile.toURI().toURL().toString());
			String mimeTypeFromExtension = mimeTypeMap.getMimeTypeFromExtension(fileExtensionFromUrl.toLowerCase());
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
				currentContent = Util.changeToVUTimes(wholeFile);
			} else if (inFilePathLowerCase.matches(".*(.htm|.html|.xhtml)") 
//					   || inFilePathLowerCase.endsWith(".htm")
//					   || inFilePathLowerCase.endsWith(".xhtml")
					 ) {
				publishProgress("converting file " + inFilePath);
				// convert sang dạng text đã convert font
				currentContent = Util.htmlToText(inFile);
			} else if (inFilePathLowerCase.endsWith(".pdf")) {
				publishProgress("converting file " + inFilePath);
				// pdf sang text
				File txtFile = Util.fromPDF(inFile);
				// format pdf bỏ dòng \r\n...
				currentContent = Util.filterCRPDF(txtFile);
				txtFile.delete();
				// guess font text file
				currentContent = Util.changeToVUTimes(currentContent);

				// } else if (inFilePathLowerCase.endsWith(".docx")
				// || inFilePathLowerCase.endsWith(".xlsx")
				// || inFilePathLowerCase.endsWith(".pptx")) { 
				// !.pdf" convert sang text rồi tự đoán font
				// currentContent = Writer.getChangedFont(inFile);
				// FileInputStream fis = new FileInputStream(inFile);
				// if (currentFName.endsWith("docx")) {
				// XWPFWordExtractor extractor = new XWPFWordExtractor(new XWPFDocument(fis));
				// currentContent = extractor.getText();
				// } else if (currentFName.endsWith("xlsx")) {
				// XSSFWorkbook workbook = new XSSFWorkbook(fis);
				// XSSFExcelExtractor extractor = new XSSFExcelExtractor(workbook);
				// currentContent = extractor.getText();
				// } else if (currentFName.endsWith("pptx")) {
				// XMLSlideShow slideShow = new XMLSlideShow(fis);
				// XSLFPowerPointExtractor extractor = new XSLFPowerPointExtractor(slideShow);
				// currentContent = extractor.getText();
				//}
				// fis.close();
			} else if (inFilePathLowerCase.endsWith(".doc")
					 ) { // !.pdf"
				publishProgress("converting file " + inFilePath);
				currentContent = FileUtil.readWordFileToText(inFile);
				currentContent = Util.changeToVUTimes(currentContent);
			} else // if ("Search".equals(load)) {
			if (inFilePathLowerCase.endsWith("docx")) {
				currentContent = Util.changeToVUTimes(DocxToText.docxToText(inFile));
			} else if (inFilePathLowerCase.endsWith("odt")) {
				currentContent = Util.changeToVUTimes(OdtToText.odtToText(inFile));
			} else if (inFilePathLowerCase.endsWith("rtf")) {
				Metadata metadata = new Metadata();
				StringWriter writer = new StringWriter();
				FileInputStream fis = new FileInputStream(inFile);
				final org.apache.tika.parser.rtf.TextExtractor ert = new org.apache.tika.parser.rtf.TextExtractor(new XHTMLContentHandler(new WriteOutContentHandler(writer), metadata), metadata);
				ert.extract(fis);
				currentContent = Util.changeToVUTimes(writer.toString()); // RTF2Txt.rtfToText(inFile)
			} else if (inFilePathLowerCase.endsWith("epub")) {
				currentContent = Util.changeToVUTimes(Epub2Txt.epub2txt(inFile));
			} else if (inFilePathLowerCase.endsWith("fb2")) {
				currentContent = Util.changeToVUTimes(FB2Txt.fb2txt(inFile));
			} else if (inFilePathLowerCase.endsWith("xlsx")) {
				currentContent = Util.changeToVUTimes(XLSX2Text.getText(inFile));
			} else if (inFilePathLowerCase.endsWith("pptx")) {
				currentContent = Util.changeToVUTimes(PPTX2Text.pptx2Text(inFile));
			} else if (inFilePathLowerCase.endsWith("ods")) {
				currentContent = Util.changeToVUTimes(ODSToText.odsToText(inFile));
			} else if (inFilePathLowerCase.endsWith("pub")) {
				currentContent = Util.changeToVUTimes(new PublisherTextExtractor(new  FileInputStream(inFilePath)).getText());
			} else if (inFilePathLowerCase.endsWith("vsd")) {
				currentContent = Util.changeToVUTimes(new VisioTextExtractor(new  FileInputStream(inFilePath)).getText());
			} else if (inFilePathLowerCase.endsWith("odp")) {
				currentContent = Util.changeToVUTimes(ODPToText.odpToText(inFile));
			} else if (inFilePathLowerCase.endsWith("ppt")
					 || inFilePathLowerCase.endsWith("pps")) {
				currentContent = Util.changeToVUTimes(new PowerPointExtractor(inFilePath).getText());
			} else if (inFilePathLowerCase.endsWith("xls")) {
				currentContent = Util.changeToVUTimes(new ExcelExtractor(
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
			} else if (inFilePathLowerCase.matches(compressExtension)) {
					String outDirFilePath = "";
					if (inFile.getAbsolutePath().startsWith(PRIVATE_PATH)) {
						String name = inFile.getName();
						int lastIndexOf = name.lastIndexOf(".");
						outDirFilePath = inFile.getParent() + "/" + name.substring(0, lastIndexOf) + "_" + name.substring(lastIndexOf + 1);
					} else {
						outDirFilePath = PRIVATE_PATH + inFile;
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
							mimeTypeFromExtension = mimeTypeMap.getMimeTypeFromExtension(fileExtensionFromUrl.toLowerCase());
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
									 && (zeNameLower.matches(CAN_PROCESS)
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
								Log.d("deleteOnExit", file.getAbsolutePath());
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
			if (currentContent != null && currentContent.length() > 0) {
				// save to text file
				FileUtil.writeFileAsCharset(newFile, currentContent, Util.UTF8);
				fileList.add(newFile);
				Log.d("newFile exist", newFile + " just written: " + newFile.exists());
			} else {
				currentContent = "";
				contentLower = "";
			}
			Log.d("newFile", String.valueOf(newFile));
		}
		
		private String loadUrl(WebView mWebView, String sb) throws IOException, MalformedURLException {
			File tempFile;
			if (files.length == 1) {
				tempFile = new File(PRIVATE_PATH + "/" + inFilePath /*+ "_" + System.currentTimeMillis() */ + ".1111.html");
			} else {
				tempFile = new File(PRIVATE_PATH + "/ziplisting_" + df.format(System.currentTimeMillis()).replaceAll("[/\\?<>\"':|]", "_") + ".html");
			}
			FileUtil.writeContentToFile(tempFile.getAbsolutePath(), sb);
			currentUrl = tempFile.toURI().toURL().toString();
			mWebView.loadUrl(currentUrl);
			Log.d("currentUrl 6", currentUrl);
			//tempFList.add(tempFile.getAbsolutePath());
			return currentUrl;
		}
	}
	
	class GenStardictTask extends AsyncTask<Void, String, String> {
		String fName;
		
		GenStardictTask(String f) {
			fName = f;
		}
		
		@Override
		protected String doInBackground(Void[] p1) {
			try {
				publishProgress("Generating Stardict files...");
				StardictReader.createStardictData(fName);
				return "Generate Stardict files successfully";
			} catch (Throwable e) {
				publishProgress(e.getMessage());
				Log.e("Generate Stardict files unsuccessfully", e.getMessage(),e);
				return "Generate Stardict files unsuccessfully";
			}
		}
		
		protected void onProgressUpdate(String... progress) {
			if (progress != null && progress.length > 0 
				&& progress[0] != null && progress[0].trim().length() > 0) {
				statusView.setText(progress[0]);
			}
		}
		
		@Override
		protected void onPostExecute(String result) {
			showToast(result);
			statusView.setText(result);
		}
	}
	
	ExtractFile extractFile = null;
	class ZipReadingTask extends AsyncTask<Void, String, String> {

		@Override
		protected String doInBackground(Void[] p1) {
			try {
				if (extractFile != null) {
					Log.d("ZipReadingTask", "arc.close()");
					extractFile.close();
				}
				extractFile = new ExtractFile(currentZipFileName, PRIVATE_PATH + currentZipFileName);
				//arc.saveEntries(entryFileList);
				String zipToUrlStr = extractFile.compressedFile2UrlStr("", true, "<br/>\r\n");
				// Log.d("zipList", zipToUrlStr);
				String tempFName = PRIVATE_PATH + currentZipFileName + "/sourcelisting" /*+ dateInstance.format(System.currentTimeMillis())*/ + ".0000.html";
				//tempFList.add(tempFName);
				FileUtil.writeContentToFile(tempFName, EMPTY_HEAD + zipToUrlStr + END_BODY_HTML);
				currentUrl = new File(tempFName).toURI().toURL().toString();
				home = currentUrl;
				publishProgress(currentZipFileName);
				(webTask = new WebTask(webView, currentUrl)).execute();
			} catch (Exception e) {
				Log.e("ZipReadingTask", e.getMessage(), e);
				publishProgress(e.getMessage());
			} 
			return null;
		}

		protected void onProgressUpdate(String... progress) {
			if (progress != null && progress.length > 0 
				&& progress[0] != null && progress[0].trim().length() > 0) {
				statusView.setText(progress[0]);
			}
		}
	}

	class RestoreStardictTask extends AsyncTask<Void, String, String> {
		String fName;

		RestoreStardictTask(String f) {
			fName = f;
		}

		@Override
		protected String doInBackground(Void[] p1) {
			try {
				publishProgress("Restoring Stardict files...");
				String dic2Text = StardictReader.dic2Text(fName);
				return dic2Text;
			} catch (Throwable e) {
				publishProgress(e.getMessage());
				Log.e("Error Restoring Stardict files", e.getMessage(),e);
				return null;
			}
		}

		protected void onProgressUpdate(String... progress) {
			if (progress != null && progress.length > 0 
				&& progress[0] != null && progress[0].trim().length() > 0) {
				statusView.setText(progress[0]);
			}
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				showToast("Restore Stardict files successfully");
				try {
					webView.loadUrl(new File(result).toURL().toString());
				}
				catch (MalformedURLException e) {}
				statusView.setText(result);
			} else {
				showToast("Restore Stardict files unsuccessfully");
			}
		}
	}
	
	NumberFormat nf = NumberFormat.getInstance();
	class DupFinderTask extends AsyncTask<Void, String, String> {
		
		String[] fileNames;
		List<List<FileInfo>> groupList;
//		int noFile;
//		int noDup;
//		long totalSize = 0;
//		long dupSize = 0;
//		NumberFormat nf = NumberFormat.getInstance();
		long start = 0;
		static final int GROUP_VIEW = 0;
		static final int NAME_VIEW = 1;
		int curView = 0;
		File fret;
		
		private static final String DUP_TITLE = 
		HTML_STYLE
		+ "<title>Duplicate Finding Result</title>\r\n" 
		+ HEAD_TABLE;

		public DupFinderTask() {
		}

		public DupFinderTask(String[] fs) {
			this.fileNames = fs;
		}

		public String deleteFile(final String selectedPath) throws IOException {
			boolean ret = FileUtils.deleteFile(selectedPath);// new File(selectedPath).delete();
			String statusDel = "deleteFile";
			if (ret) {
				statusDel = "Delete file \"" + selectedPath + "\" successfully";
			} else {
				statusDel = "Cannot delete file \"" + selectedPath + "\"";
			}
			Log.d("deleteFile", statusDel + df.format(System.currentTimeMillis()));
			showToast(statusDel);
			return genFile(groupList);
		}
		
		public String deleteGroup(int group, String selectedPath) throws IOException {
			List<FileInfo> l = null; // groupList.get(group - 1);
			for (List<FileInfo> ltemp : groupList) {
				if (ltemp.get(0).group == group) {
					l = ltemp;
					break;
				}
			}
			File selectedF = new File(selectedPath);
			for (int i = l.size()-1; i >= 0; i--) {
				FileInfo ff = l.get(i);
				if (selectedF.exists()) {
					if (!ff.path.equals(selectedPath) && ff.file.exists()) {
						boolean ret = FileUtils.deleteFile(ff.file); // ff.file.delete();
						String statusDel = "deleteGroup";
						if (ret) {
							statusDel = "Delete group " + group + ", \"" + ff.path + "\" successfully";
						} else {
							statusDel = "Cannot delete group " + group + ", \"" + ff.path + "\"";
						}
						Log.d("deleteGroup", statusDel + df.format(System.currentTimeMillis()));
						statusView.setText(statusDel);
					}
				} else if (ff.file.exists()) {
					selectedF = ff.file;
					selectedPath = ff.path;
				}
			}
			return genFile(groupList);
		}

		public String deleteFolder(String selectedPath) throws IOException {
			String parentPath = new File(selectedPath).getParentFile().getAbsolutePath();
			for (List<FileInfo> l : groupList) {
				for (FileInfo ff : l) {
					if (isDup(l)) {
						if (ff.file.exists() && ff.file.getParentFile().getAbsolutePath().equals(parentPath)) {
							boolean ret = FileUtils.deleteFile(ff.file); //ff.file.delete();
							String statusDel = "deleteFolder";
							if (ret) {
								statusDel = "Delete \"" + ff.path + "\" successfully. ";
							} else {
								statusDel = "Cannot delete \"" + ff.path + "\". ";
							}
							Log.d("deleteFolder", statusDel + df.format(System.currentTimeMillis()));
							statusView.setText(statusDel);
						} 
					}
				}
			}
			return genFile(groupList);
		}
		
		public String deleteSubFolder(String selectedPath) throws IOException {
			String parentPath = new File(selectedPath).getParentFile().getAbsolutePath();
			for (List<FileInfo> l : groupList) {
				for (FileInfo ff : l) {
					if (isDup(l)) {
						String path = ff.file.getParentFile().getAbsolutePath();
						if (ff.file.exists() && path.startsWith(parentPath) && path.length() > parentPath.length()) {
							boolean ret = FileUtils.deleteFile(ff.file); //ff.file.delete();
							String statusDel = "deleteSubFolder";
							if (ret) {
								statusDel = "Delete \"" + ff.path + "\" successfully";
							} else {
								statusDel = "Cannot delete \"" + ff.path + "\"";
							}
							Log.d("deleteSubFolder", statusDel + df.format(System.currentTimeMillis()));
							statusView.setText(statusDel);
						}
					} 
				}
			}
			return genFile(groupList);
		}

		private boolean isDup(List<FileInfo> l) {
			int counter = 0;
			for (FileInfo ff : l) {
				if (ff.file.exists() && ++counter > 1) {
					return true;
				}
			}
			return false;
		}
		
		List<List<FileInfo>> copyGroupList(List<List<FileInfo>> oriGroupList) {
			List<List<FileInfo>> newGroupList = new LinkedList<List<FileInfo>>();
			for (List<FileInfo> filesInGroup : oriGroupList) {
				List<FileInfo> dest = new LinkedList<FileInfo>();
				newGroupList.add(dest);
				for (FileInfo ff : filesInGroup) {
					dest.add(ff);
				}
			}
			return newGroupList;
		}

		@Override
		protected String doInBackground(Void[] p1) {
			try {
				return new File(duplicateFinder(fileNames)).toURI().toURL().toString();
			} catch (IOException e) {
				publishProgress(e.getMessage());
			}
			return null;
		}
		
		protected void onProgressUpdate(String... progress) {
			if (progress != null && progress.length > 0 
				&& progress[0] != null && progress[0].trim().length() > 0) {
				statusView.setText(progress[0]);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			statusView.setText(statusViewResult.toString());
			currentUrl = result;
			home = currentUrl;
			showToast("Duplicate finder finished");
			webView.loadUrl(result);
		}
		
		public String duplicateFinder(String[] files) throws IOException {
			File[] fs = new File[files.length];
			int i = 0;
			for (String st : files) {
				fs[i++] = new File(st);
			}
			return duplicateFinder(fs);
		}

		public String duplicateFinder(File[] files) throws IOException {
			start =  System.currentTimeMillis();
			fret = new File(SearchFragment.PRIVATE_PATH + "/" + "Duplicate finder result_"
							+ df.format(System.currentTimeMillis()).replaceAll("[/\\?<>\"':|]", "_") + ".html");
			publishProgress("Getting file list...");
			List<File> lf = FileUtil.getFiles(files);
			groupList = dupFinder(lf);
			return genFile(groupList);
		}

		String genFile(List<List<FileInfo>> lset) throws IOException {
			return genFile(groupList, curView);
		}
		
		String genFile(List<List<FileInfo>> lset, int view) throws IOException {
			StringBuilder sb;
			if (view == GROUP_VIEW) {
				sb = displayGroup(lset);
				curView =  GROUP_VIEW;
			} else {
				sb = displayName(lset);
				curView = NAME_VIEW;
			}
			FileUtil.writeContentToFile(fret.getAbsolutePath(), sb.toString());
			publishProgress(statusViewResult.toString());
			return fret.getAbsolutePath();
		}

		StringBuilder statusViewResult = new StringBuilder();
		
		private List<List<FileInfo>> dupFinder(final List<File> oriListFile) throws IOException {
			long curSize = - 1;
			List<FileInfo> filesInGroupList = null; //same size, many group, 1 size 1 set
			List<List<FileInfo>> groupList = new LinkedList<List<FileInfo>>();
			Collections.sort(oriListFile, new SortFileSizeDecrease());
//			totalSize = 0;
			for (File f : oriListFile) {
//				totalSize += f.length();
				if (f.length() < curSize) {
					filesInGroupList = new LinkedList<FileInfo>();
					curSize = f.length();
					filesInGroupList.add(new  FileInfo(f));
					groupList.add(filesInGroupList);
				} else {
					boolean same = false;
					for (List<FileInfo> curGroupList : groupList) {
						//if (curGroupList.get(0).length == f.length()) {
							same = compare(curGroupList.get(0).file, f);
							if (same) {
								curGroupList.add(new FileInfo(f));
								break;
							}
						//}
					}
					if (!same) {
						filesInGroupList = new LinkedList<FileInfo>();
						filesInGroupList.add(new FileInfo(f));
						groupList.add(filesInGroupList);
					}
				}
			}
//		Log.d("lset.size()", lset.size() + "");
			for (int i = groupList.size() - 1; i >= 0; i--) {
				if (groupList.get(i).size() == 1) {
					groupList.remove(i);
				}
			}
			int curGroup = 0;
			for (List<FileInfo> filesInGroup : groupList) {
				curGroup++;
				for (FileInfo ff : filesInGroup) {
					ff.group = curGroup;
				}
			}
			return groupList;
		}

		boolean compare(final File f1, final File f2) throws IOException {
			publishProgress("compare \"" + f1.getAbsolutePath() + "\" and \"" + f2.getAbsolutePath() + "\"");
			//Log.d("compare file ", f1.getName() + " and " + f2.getName());
			if (f1.length() != f2.length()) {
				return false;
			}
			FileInputStream fis1 = new FileInputStream(f1);
			FileInputStream fis2 = new FileInputStream(f2);
			BufferedInputStream bis1 = new BufferedInputStream(fis1);
			BufferedInputStream bis2 = new BufferedInputStream(fis2);
			long counter = 0;
			int n;
			while (((n = bis1.read()) != -1) && n == bis2.read()) {
				counter++;
			}
			bis1.close();
			bis2.close();
			fis1.close();
			fis2.close();
			return counter == f2.length();
		}
		
		private StringBuilder displayGroup(final List<List<FileInfo>> lset) throws MalformedURLException {
			StringBuilder sb = new StringBuilder(DUP_TITLE);
			int groupSize = 0;
			int counter = 0;
			int noFile = 0;
			int noDup = 0;
			int dupSize = 0;
			int curGroup = 0;
			int realSize = 0;
			int totalSize = 0;
			int one = 0;
			if (lset != null && lset.size() > 0) {
				//String findRet = new File(SearchFragment.PRIVATE_DIR + "/" + "Duplicate finder result.html").toURI().toURL().toString();
				sb.append("<tr bgcolor=\"#FCCC74\">\r\n")
					.append(TD1_CENTER)
					.append("<b>No.</b></td>\n")
					.append(TD1_CENTER)
					.append("<a href=\"").append(fret)
				.append("?viewGroup\">")
				.append("<b>Group</b></a>\n</td>\n")
					.append(TD2_CENTER)
					.append("<a href=\"").append(fret)
				.append("?viewName\">")
				.append("<b>File Name</b></a>\n</td>\n")
					.append(TD2_CENTER)
					.append("<b>Size (bytes)</b></td>\n")
					.append(TD3_CENTER)
					.append("<b>Delete?</b></td>\n")
					.append(TD3_CENTER)
					.append("<b>Delete Group</b></td>\n")
					.append(TD3_CENTER)
					.append("<b>Delete Folder</b></td>\n")
					.append(TD3_CENTER)
					.append("<b>Delete Sub Folder</b></td>\n")
					.append("</tr>");
				List<List<FileInfo>> newGroupList = new LinkedList<>();
				List<List<FileInfo>> newLsetOrder = lset;
				if (groupViewChanged) {
					newLsetOrder = new LinkedList<List<FileInfo>>();
					for (int i = lset.size() - 1; i >= 0; i--) {
						newLsetOrder.add(lset.get(i));
					}
					groupViewChanged = false;
				}
				
				for (List<FileInfo> s : newLsetOrder) {
					curGroup++;
					one = 0;
					// kiem tra xem co con la group khong
					int dupInGroup = 0;
					for (FileInfo ff : s) {
						if (ff.file.exists() && ++dupInGroup > 1) {
							break;
						}
					}
					List<FileInfo> newGroupListEle = new LinkedList<>();
					newGroupList.add(newGroupListEle);
					for (FileInfo ff : s) {
						if (curGroup % 2 == 1) {
							sb.append("<tr>\r\n");
						} else {
							sb.append("<tr bgcolor=\"#ffee8d\">\r\n");
						}
						
						if (ff.file.exists()) {
							totalSize += ff.length;
							noFile++;
							newGroupListEle.add(ff);
							if (++one == 2) {
								groupSize++;
								realSize += ff.length;
								dupSize += ff.length;
								noDup++;
							} else if (one > 2) {
								dupSize += ff.length;
								noDup++;
							}
							sb.append(TD1_LEFT).append(++counter).append("</td>\n")
								.append(TD1_LEFT).append(ff.group).append("</td>\n")
								.append(TD2_LEFT).append("<a href=\"").append(ff.file.toURI().toURL().toString()).append("\">")
								.append(ff.path).append("</a>\n</td>\n")
								.append(TD3_LEFT).append(nf.format(ff.length)).append("</td>")
								.append(TD3_LEFT).append("<a href=\"").append(fret)
								.append("?deleteFile=").append(ff.path).append("\">Delete</a>\n</td>\n");
							if (dupInGroup > 1) {
								sb.append(TD3_LEFT).append("<a href=\"").append(fret)
								.append("?deleteGroup=").append(ff.group).append(",").append(ff.path).append("\">Delete group</a>\n</td>\n");
							} else {
								sb.append(TD3_LEFT).append("&nbsp;\n</td>\n");
							}
							sb.append(TD3_LEFT).append("<a href=\"").append(fret)
								.append("?deleteFolder=").append(ff.path).append("\">Delete Folder</a>\n</td>\n")
								.append(TD3_LEFT).append("<a href=\"").append(fret)
								.append("?deleteSub=").append(ff.path).append("\">Delete Sub Folder</a>\n</td>\n")
								.append("</tr>\n")
								;
								
						} else {
							sb.append(TD1_LEFT).append(++counter).append("</td>\n")
								.append(TD1_LEFT).append(ff.group).append("</td>\n")
								.append(TD2_LEFT)
								//.append("<a href=\"").append(ff.toURI().toURL().toString()).append("\">")
								.append("<font color='red'><strike>")
								.append(ff.path)
								.append("</strike></font>")
								.append("\n</td>\n")
								.append(TD3_LEFT).append(nf.format(ff.length)).append("</td>")
								.append(TD3_LEFT)
								//.append("<a href=\"").append(findRet).append("?delete=").append(ff.getAbsolutePath())
								//.append("\">Delete</a>\n")
								.append("&nbsp;</td>\n");
							if (dupInGroup > 1) {
								sb.append(TD3_LEFT).append("<a href=\"").append(fret)
									.append("?deleteGroup=").append(ff.group).append(",").append(ff.path).append("\">Delete group</a>\n</td>\n");
							} else {
								sb.append(TD3_LEFT).append("&nbsp;\n</td>\n");
							}
							sb.append(TD3_LEFT).append("<a href=\"").append(fret)
								.append("?deleteFolder=").append(ff.path).append("\">Delete Folder</a>\n</td>\n")
								.append(TD3_LEFT).append("<a href=\"").append(fret)
								.append("?deleteSub=").append(ff.path).append("\">Delete Sub Folder</a>\n</td>\n")
								.append("</tr>\n")
								;
						}
					}
				}
				sb.append("</table>\n");
				for (int i = newGroupList.size() - 1; i >= 0; i--) {
					if (newGroupList.get(i).size() < 2) {
						newGroupList.remove(i);
					}
				}
				groupList = newGroupList;
			}
			long duration = System.currentTimeMillis() - start;
			sb.append("<strong><br/>")
				.append("Total ").append(nf.format(noFile)).append(" files (")
				.append(nf.format(totalSize)).append(" bytes)<br/>")
				.append(nf.format(noDup)).append(" files (").append(nf.format(dupSize)).append(" bytes) duplicate<br/>")
				.append((realSize != 0) ? nf.format(dupSize * 100 / (double) realSize) : "0").append("% duplicate<br/>")
				.append(nf.format(groupSize)).append(" group duplicate<br/>")
				.append("took ").append(nf.format(duration)).append(" milliseconds<br/>")
				.append("</strong></div>\n</body>\n</html>");
			statusViewResult = new StringBuilder();
			
			statusViewResult.append("Total ").append(nf.format(noFile)).append(" files (")
				.append(nf.format(realSize)).append(" bytes), ")
				.append(nf.format(noDup)).append(" files (").append(nf.format(dupSize)).append(" bytes) duplicate, ")
				.append((realSize != 0) ? nf.format(dupSize * 100 / (double) realSize) : "0").append("% duplicate, ")
				.append(nf.format(groupSize)).append(" duplicate group, ")
				.append("took ").append(nf.format(duration)).append(" milliseconds");
			return sb;
		}

		void calList(List<List<FileInfo>> lset) {
			List<List<FileInfo>> newLset = new LinkedList<>();
			for (List<FileInfo> s : lset) {
				List<FileInfo> newLset2 = new LinkedList<>();
				newLset.add(newLset2);
				for (FileInfo ff : s) {
					if (ff.file.exists()) {
						newLset2.add(ff);
					}
				}
			}
			for (int i = newLset.size() - 1; i >= 0; i--) {
				if (newLset.get(i).size() < 2) {
					newLset.remove(i);
				}
			}
			groupList = newLset;
		}
		
		private StringBuilder displayName(List<List<FileInfo>> lset) throws MalformedURLException {
			StringBuilder sb = new StringBuilder(DUP_TITLE);
			List<FileInfo> infoList = new LinkedList<FileInfo>();
			int groupSize = 0;
			int noDup = 0;
			int dupSize = 0;
			int noFile = 0;
			int realSize = 0;
			int totalSize = 0;
			
			//String findRet = new File(SearchFragment.PRIVATE_DIR + "/" + "Duplicate finder result.html").toURI().toURL().toString();
			if (lset != null && lset.size() > 0) {
				
				sb.append("<tr bgcolor=\"#FCCC74\">\r\n")
					.append(TD1_CENTER)
					.append("<b>No.</b></td>\n")
					.append(TD1_CENTER)
					.append("<a href=\"").append(fret)
				.append("?viewGroup\">")
				.append("<b>Group</b></a>\n</td>\n")
					.append(TD2_CENTER)
					.append("<a href=\"").append(fret)
				.append("?viewName\">")
				.append("<b>File Name</b></a>\n</td>\n")
					.append(TD3_CENTER)
					.append("<b>Size (bytes)</b></td>\n")
					.append(TD3_CENTER)
					.append("<b>Delete?</b></td>\n")
					.append(TD3_CENTER)
					.append("<b>Delete Group</b></td>\n")
					.append(TD3_CENTER)
					.append("<b>Delete Folder</b></td>\n")
					.append(TD3_CENTER)
					.append("<b>Delete Sub Folder</b></td>\n")
					.append("</tr>");
					
				int counter = 0;
				for (List<FileInfo> s : lset) {
					for (FileInfo ff : s) {
						infoList.add(ff);
					}
				}
				if (nameOrder) {
					Collections.sort(infoList);
				} else {
					Collections.sort(infoList, new SortFilePathDecrease());
				}

				Map<String, Integer> ss = new TreeMap<String, Integer>();
				for (FileInfo ff : infoList) {
					List<FileInfo> l = null; // groupList.get(group - 1);
					for (List<FileInfo> ltemp : groupList) {
						if (ltemp.get(0).group == ff.group) {
							l = ltemp;
							break;
						}
					}
					// kiem tra xem co con la group khong
					int dupInGroup = 0;
					for (FileInfo ff2 : l) {
						if (ff2.file.exists() && ++dupInGroup > 1) {
							break;
						}
					}
					sb.append("<tr>\r\n");
					if (ff.file.exists()) {
						noFile++;
						totalSize += ff.file.length();
						if (ss.get(ff.group + "") == null) {
							ss.put(ff.group + "", 1);
						} else if (ss.get(ff.group + "") == 1) {
							groupSize++;
							realSize += ff.length;
							ss.put(ff.group + "", 2);
							noDup++;
							dupSize += ff.length;
						} else if (ss.get(ff.group + "") >= 2) {
							noDup++;
							dupSize += ff.length;
						}
						sb.append(TD1_LEFT).append(++counter).append("</td>\n")
							.append(TD1_LEFT).append(ff.group).append("</td>\n")
							.append(TD2_LEFT).append("<a href=\"").append(ff.file.toURI().toURL().toString()).append("\">")
							.append(ff.path).append("</a>\n</td>\n")
							.append(TD3_LEFT).append(nf.format(ff.length)).append("</td>")
							.append(TD3_LEFT).append("<a href=\"").append(fret)
							.append("?deleteFile=").append(ff.path).append("\">Delete</a>\n</td>\n");
						if (dupInGroup > 1) {
							sb.append(TD3_LEFT).append("<a href=\"").append(fret)
								.append("?deleteGroup=").append(ff.group).append(",").append(ff.path).append("\">Delete group</a>\n</td>\n");
						} else {
							sb.append(TD3_LEFT).append("&nbsp;\n</td>\n");
						}
						sb.append(TD3_LEFT).append("<a href=\"").append(fret)
							.append("?deleteFolder=").append(ff.path).append("\">Delete Folder</a>\n</td>\n")
							.append(TD3_LEFT).append("<a href=\"").append(fret)
							.append("?deleteSub=").append(ff.path).append("\">Delete Sub Folder</a>\n</td>\n")
							.append("</tr>\n")
							;
					} else {
						sb.append(TD1_LEFT).append(++counter).append("</td>\n")
							.append(TD1_LEFT).append(ff.group).append("</td>\n")
							.append(TD2_LEFT)
							//.append("<a href=\"").append(ff.toURI().toURL().toString()).append("\">")
							.append("<font color='red'><strike>")
							.append(ff.path)
							.append("</strike></font>")
							.append("\n</td>\n")
							.append(TD3_LEFT).append(nf.format(ff.length)).append("</td>")
							.append(TD3_LEFT)
							//.append("<a href=\"").append(findRet).append("?delete=").append(ff.getAbsolutePath())
							//.append("\">Delete</a>\n")
							.append("&nbsp;</td>\n");
						if (dupInGroup > 1) {
							sb.append(TD3_LEFT).append("<a href=\"").append(fret)
								.append("?deleteGroup=").append(ff.group).append(",").append(ff.path).append("\">Delete group</a>\n</td>\n");
						} else {
							sb.append(TD3_LEFT).append("&nbsp;\n</td>\n");
						}
						sb.append(TD3_LEFT).append("<a href=\"").append(fret)
							.append("?deleteFolder=").append(ff.path).append("\">Delete Folder</a>\n</td>\n")
							.append(TD3_LEFT).append("<a href=\"").append(fret)
							.append("?deleteSub=").append(ff.path).append("\">Delete Sub Folder</a>\n</td>\n")
							.append("</tr>\n")
							;
					}
				}
				sb.append("</table>\n");
				calList(groupList);
			}
			long duration = System.currentTimeMillis() - start;
			sb.append("<strong><br/>")
				.append("Total ").append(nf.format(noFile)).append(" files (")
				.append(nf.format(totalSize)).append(" bytes)<br/>")
				.append(nf.format(noDup)).append(" files (").append(nf.format(dupSize)).append(" bytes) duplicate<br/>")
				.append((realSize != 0) ? nf.format(dupSize * 100 / (double) realSize) : "0").append("% duplicate<br/>")
				.append(nf.format(groupSize)).append(" group duplicate<br/>")
				.append("took ").append(nf.format(duration)).append(" milliseconds<br/>")
				.append("</strong></div>\n</body>\n</html>");
			statusViewResult = new StringBuilder();
			statusViewResult.append("Total ").append(nf.format(noFile)).append(" files (")
				.append(nf.format(totalSize)).append(" bytes), ")
				.append(nf.format(noDup)).append(" files (").append(nf.format(dupSize)).append(" bytes) duplicate, ")
				.append((realSize != 0) ? nf.format(dupSize * 100 / (double) realSize) : "0").append("% duplicate, ")
				.append(nf.format(groupSize)).append(" duplicate group, ")
				.append("took ").append(nf.format(duration)).append(" milliseconds");
			return sb;
		}
	}
	
	class ZipExtractionTask extends AsyncTask<Void, String, String> {
		Collection<String> urls;
		ZipExtractionTask(final Collection<String> urls) {
			this.urls = urls;
		}

		@Override
		protected String doInBackground(Void[] p1) {
			try {
				Log.d("extract size", urls.size() + "");
				extractFile.extractEntries(urls, false);
			} catch (Exception e) {
				Log.d("zip result", e.getMessage(), e);
				publishProgress(e.getMessage());
			} 
			return null;
		}

		protected void onProgressUpdate(String... progress) {
			if (progress != null && progress.length > 0 
				&& progress[0] != null && progress[0].trim().length() > 0) {
				statusView.setText(progress[0]);
			}
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (currentUrl.length() > 0) {
				if (webView.getUrl().equals(currentUrl)) {
					locX = webView.getScrollX();
					locY = webView.getScrollY();
				}
				webView.loadUrl(currentUrl);
			}
			Log.d("x, y, cur, ori", locX + ", " + locY 
			+ ", currentUrl= " + currentUrl 
			+ ", origin = " + webView.getOriginalUrl() 
			+ ", url = " + webView.getUrl());
		}
	}

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

	static final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
	// private String sourceContent = "";

	class WebTask extends AsyncTask<Void, String, String> {

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
		public WebTask() {
		}

		public WebTask(WebView webView, String url) {
			init(webView, url);
		}

		public WebTask(WebView webView, String url, String status) {
			init(webView, url);
			this.status = status;
		}
		
		public WebTask(WebView webView, String url, boolean isHome) {
			init(webView, url);
			this.isHome = isHome;
		}
		
		public WebTask(WebView webView, String url, boolean isHome, String status) {
			init(webView, url);
			this.isHome = isHome;
			this.status = status;
		}

		private void init(WebView webView, String url) {
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
			mime = mimeTypeMap.getMimeTypeFromExtension(fileExtensionFromUrl.toLowerCase());
			if (currentZipFileName.length() > 0 && extractFile == null) {
				try {
					extractFile = new ExtractFile(currentZipFileName, PRIVATE_PATH + currentZipFileName);
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
						Log.d("currentZipFileName", currentZipFileName + " ");
						// publishProgress(currentZipFileName + "/" + curFullEntryName);

						if (exeptFilePrefixFile.exists()) { // trên đường dẫn file thiệt có .zip
							if (exeptFilePrefixFile.isFile()) {
								// file trong zip đã được extract trong getFilesDir
								currentUrl = exeptFilePrefixFile.toURI().toURL().toString();
								Log.d("exeptFilePrefixFile.isFile()", currentUrl);
								return fileHandler(exeptFilePrefixFile.getAbsolutePath(), mime);
							} else { // directory
								// String st = EMPTY_HEAD + listingFolder(exeptFilePrefixFile) + END_BODY_HTML;
								// save file
								// get url
								// return url
								// currentUrl = ???
								Log.d("exeptFilePrefixFile.isDirectory", "true");
								return "";
							}
						} else { // file zip thiệt
							if (currentZipFileName.length() == 0) {
								Log.d("currentZipFileName.length() == 0", "true");
								publishProgress("Entry does not exist");
								return null; // do nothing
							}
							//ZipEntry entry = currentZFile.getEntry(curFullEntryName);
							if (curFullEntryName.length() == 0) {//(entry == null) {
//								Set<String> zipList = Arc.loadFilesFolders(currentZFile, curFullEntryName);
//								String zipToUrlStr = Arc.fileNames2UrlStr(new  File(currentZipFileName), zipList, true, "<br/>\r\n");
								String zipToUrlStr = extractFile.compressedFile2UrlStr(curFullEntryName, true, "<br/>\r\n");
								Log.d("zipList 1", zipToUrlStr);
								String name = curFullEntryName.endsWith("/") ? 
									curFullEntryName.substring(0, curFullEntryName.length() - 1) : 
									curFullEntryName; // do là folder
								Log.d("entryName 1", name);
								
								File tempFName = new  File(PRIVATE_PATH + currentZipFileName + "/" + name + "-0123456789.list.converted.html");
								//File.createTempFile(name + "-", ".html", new File(PRIVATE_PATH + currentZipFileName));
								//tempFList.add(tempFName.getAbsolutePath());
								Log.d("(!tempFName.exists()", 
									  (!tempFName.exists() || tempFName.lastModified() < new  File(currentZipFileName).lastModified()) + "");
								if (!tempFName.exists() || tempFName.lastModified() < new  File(currentZipFileName).lastModified()) {
									FileUtil.writeContentToFile(tempFName.getAbsolutePath(), EMPTY_HEAD + zipToUrlStr + END_BODY_HTML);
								}
								currentUrl = tempFName.toURI().toURL().toString();
								home = currentUrl;
								return "";
//								publishProgress("Entry does not exist");
//								return null; // do nothing
							} else {
								// publishProgress(currentZipFileName + "/" + curFullEntryName);
								if (curFullEntryName.endsWith("/")) { //}(entry.isDirectory()) { // directory
									// Set<String> zipList = Arc.loadFilesFolders(currentZFile, curFullEntryName);
									// String zipToUrlStr = Arc.fileNames2UrlStr(new  File(currentZipFileName), zipList, true, "<br/>\r\n");
									String zipToUrlStr = extractFile.compressedFile2UrlStr(curFullEntryName, true, "<br/>\r\n");
									Log.d("zipList 2", zipToUrlStr);
									//String folderEntryName = entry.getName();
									Log.d("folderEntryName", curFullEntryName);
									String name = curFullEntryName.substring(0, curFullEntryName.length() - 1); // do là folder
									Log.d("entryName", name);
//									int lastIndexOf = name.lastIndexOf("/");
//									name = name.substring(lastIndexOf > 0 ? lastIndexOf : 0);
//									Log.d("entryName", name);
									Log.d("PRIVATE_PATH + currentZFile", PRIVATE_PATH + currentZipFileName);
									File tempFName = new  File(PRIVATE_PATH + currentZipFileName + "/" + name + ".000.list.html"); 
									// File.createTempFile(name + "--", ".html", new File(PRIVATE_PATH + currentZipFileName));
									//tempFList.add(tempFName.getAbsolutePath());
									Log.d("(!tempFName.exists()2", 
										  (!tempFName.exists() || tempFName.lastModified() < new  File(currentZipFileName).lastModified()) + "");
									if (!tempFName.exists() || tempFName.lastModified() < new  File(currentZipFileName).lastModified()) {
										FileUtil.writeContentToFile(tempFName.getAbsolutePath(), EMPTY_HEAD + zipToUrlStr + END_BODY_HTML);
									}
									currentUrl = tempFName.toURI().toURL().toString();
									//home = currentUrl;
									return "";	// "" thì đọc displayData, null thì error // không cần hậu xử lý
								} else { // file trong zip
									// sẽ bị chuyển vị trí sang getFilesDir() + currentZipFileName + "/" + entryName;
//									storeFileName = extract(curFullEntryName); // file ngoài không đọc được do extract vào chỗ hiểm
									//Arc archive = new Arc(currentZipFileName, PRIVATE_PATH + currentZipFileName);
									storeFileName = extractFile.extractFile(curFullEntryName);
									Log.d("storeFileName", storeFileName);
									//archive.close();
									currentUrl = new File(storeFileName).toURI().toURL().toString();
									publishProgress(currentZipFileName + "/" + curFullEntryName);
									return fileHandler(storeFileName, mime);
								}
							}
						}
					} else { // không phải file zip
						pathToOpenFile = exeptFilePrefix;
						currentUrl = exeptFilePrefixFile.toURI().toURL().toString();
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
					startActivity(intent);
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
					// statusView.setText(mUrl);
					if (result.length() == 0) { // do đã xử lý từ doInBackground
						Log.d("mWebView.loadUrl(currentUrl)", currentUrl);
						mWebView.loadUrl(currentUrl); //  displayData làm nhiệm vụ lưu url trong path và ...
						// String file = currentUrl.startsWith("file:") ? currentUrl.substring(5) : currentUrl;
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
								// showToast(statusView.getText().toString());
							}
						}  else { 
							// xử lý hậu data của plain/text có </html> trong nội dung file
							// tất cả loại file file:/ và zip:/ không phải là html (khỏi phải thêm unicode, extract image)
							if (mime != null && (mime.startsWith("text/plain") || mime.startsWith("text/txt"))) { 

								// load file text ra ngoài theo đúng file name gốc hay entry
								// text file có thể có nguồn gốc từ file: hoặc zip:
								StringBuilder sb = new StringBuilder(SearchFragment.EMPTY_HEAD);
								Log.d("mSearchView", mSearchView + "");
								//Log.d("((FragmentStack)activity).mSearchView.getQuery()", ((SearchStack)activity).mSearchView.getQuery() + "");

								// String currentSearching = ((SearchStack)activity).mSearchView.getQuery().toString();
								if (currentSearching.trim().length() > 0) {
									sb.append(Util.replaceAll(result,
															  new String[] { "&", "<", "\n", currentSearching },
															  new String[] { "&amp;", "&lt;", "<br/>", "<b><font color='blue'>" + currentSearching + "</font></b>" }));
								} else {
									sb.append(Util.replaceAll(result,
															  new String[] { "&", "<", "\n", },
															  new String[] { "&amp;", "&lt;", "<br/>" }));
								}
								sb.append(END_BODY_HTML);
								loadUrl(sb.toString());
								// chỉ nhằm save file rồi load, không cần phải extract gì vì đó là text
								// mWebView.loadDataWithBaseURL("file:///android_asset/",
								// sb.toString(), "text/html", "utf-8",
								// "about:blank");
							} else { // đúng ra là không xảy ra, đã không biết kiểu mime thì đọc content làm gì
								// String file = currentUrl.startsWith("file:") ? currentUrl.substring(5) : currentUrl;
								// byte[] pageContent = FileUtil.readFileToMemory(file);
								// sourceContent = new String(pageContent);
								Log.d("currentUrl 7", currentUrl);
								mWebView.loadUrl(currentUrl);
							}
						}
					}
				}
//				setNavVisibility(false);
			} catch (Throwable t) {
				statusView.setText(t.getMessage());
				Log.d("WebTask.post", t.getMessage(), t);
			}
			if (isHome) {
				home = currentUrl;
			}
			if (status.length() > 0) {
				statusView.setText(status);
			}
		}

		private String loadUrl(String contentStr)
		throws IOException, MalformedURLException {
			return loadUrl(contentStr, true);
		}

		private String loadUrl(String contentStr, boolean needWrite)
		throws IOException, MalformedURLException {

			final String tempFileName;
			Log.d("currentZipFileName", currentZipFileName + " ");
			Log.d("zipPartWithSlash", zipPartWithSlash + " ");
			Log.d("loadUrl curFullEntryName", curFullEntryName + " "); // chỉ có file:/ mới cần xử lý
			Log.d("pathToOpenFile", pathToOpenFile + " ");
			Log.d("exeptFilePrefix", exeptFilePrefix + " ");
			if (currentZipFileName.length() > 0) {
				if (zipPartWithSlash.startsWith(PRIVATE_PATH)) {
					tempFileName = zipPartWithSlash + curFullEntryName;
				} else {
					tempFileName = PRIVATE_PATH + "/" + zipPartWithSlash + curFullEntryName;
				}

				/*} else {
				 tempFile = new File(zipPartWithSlash + curFullEntryName);
				 }*/
			} else {
				if (pathToOpenFile.startsWith(PRIVATE_PATH)) {
					tempFileName = pathToOpenFile;
				} else if (pathToOpenFile.length() > 0){
					tempFileName = PRIVATE_PATH + "/" + pathToOpenFile;
				} else {
					tempFileName = exeptFilePrefix;
				}
			}
			Log.d("tempFile", tempFileName + "");

			if (curFullEntryName.length() > 0 || pathToOpenFile.length() > 0) {
				final Collection<String> urls = getSaveOtherUrl(contentStr);
//				new Thread(new Runnable() {
//						public void run() {
//							String st = statusView.getText().toString();
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
								if (currentZipFileName.length() > 0) {
									currentUrl = "";
//									locX = 0;
//									locY = 0;
									zextr = new ZipExtractionTask(urls);
									zextr.execute();
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

			// home = currentUrl;
			//String curUrl = tempFile.toURI().toURL().toString();
			mWebView.loadUrl((currentUrl = tempFile.toURI().toURL().toString()));
			//currentUrl = curUrl;
			Log.d("currentUrl 4", currentUrl);
			//tempFList.add(tempFileName);
//			while (!tempFile.getParent().equals(PRIVATE_PATH)) {
//				tempFolderList.add((tempFile = tempFile.getParentFile()).getAbsolutePath());
//			}

			return currentUrl;
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
				statusView.setText(progress[0]);
			}
		}
	}

}
