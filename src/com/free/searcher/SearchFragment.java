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
	static File PRIVATE_DIR = null;
	static final String CHOOSER_TITLE = "chooserTitle";
	static final String SUFFIX = "suffix";
	private static final int SEARCH_MENU_ID = Menu.FIRST;
	private Button nextButton, backButton, closeButton, clearButton;
	private EditText findBox;
	private TextView findRet;
	private RelativeLayout container;
	private boolean showFind = false;
	private Typeface tf;
	private static final int MOOD_NOTIFICATIONS = R.layout.main;

	SearchTask searchTask = null;
	public GetSourceFileTask getSourceFileTask = null;
	ZipReadingTask zr = null;
	DupFinderTask dupTask = null;
	CompareTask compTask = null;
	volatile ZipExtractionTask zextr = null;
	GenStardictTask genStardictTask = null;
	RestoreStardictTask restoreStardictTask = null;
	TranslateTask translateTask = null;
	File[] files;
	WebTask webTask = null;
	public volatile int locX = 0;
	public volatile int locY = 0;

	static final String LINE_SEP = "\n";

	static final String END_BODY_HTML = "</body></html>";

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
	static NumberFormat nf = NumberFormat.getInstance();
	private boolean backForward = false;
	String searchFileResult = "";

	boolean nameOrder = true;
	boolean groupViewChanged = false;
	Cache cache = null;

	static final String TITLE_ERROR_PROCESSING_FILES = "<br/>Error Files: <br/>";

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
	static final String compressExtension = ".*\\.(7z|bz2|bzip2|tbz2|tbz|gz|gzip|tgz|tar|swm|xz|txz|zip|zipx|jar|apk|xpi|odt|ods|odp|docx|xlsx|pptx|epub|apm|ar|a|deb|lib|arj|cab|chm|chw|chi|chq|msi|msp|doc|xls|ppt|cpio|cramfs|dmg|ext|ext2|ext3|ext4|img|fat|hfs|hfsx|hxs|hxi|hxr|hxq|hxw|lit|ihex|iso|lzh|lha|lzma|mbr|mslz|mub|nsis|ntfs|rar|r00|rpm|ppmd|qcow|qcow2|qcow2c|squashfs|udf|iso|scap|uefif|vdi|vhd|vmdk|wim|esd|xar|pkg|z|taz|fb2|epub|cpio|sz|dump)"; //sz, dump self added
	//String SearchFragment.CAN_PROCESS2 = ".*(.zip|.rar|.chm|.docx|.xlsx|.pptx|.odt|.ods|.odp|.epub|.jar|.apk|.7z|.gz|.bz2|.tar|.dump|.ar|.arj|.cpio|.xz|.z|.sz|.lzma|.pdf|.txt|.doc|.docx|.fb2|.odt|.epub|.rtf|.pptx|.xlsx|.ods|.odp|.pub|.vsd)";
	static final String CAN_PROCESS = ".*\\.(7z|bz2|bzip2|tbz2|tbz|gz|gzip|tgz|tar|swm|xz|txz|zip|zipx|jar|apk|xpi|odt|ods|odp|docx|xlsx|pptx|epub|apm|ar|a|deb|lib|arj|cab|chm|chw|chi|chq|msi|msp|doc|xls|ppt|pps|cpio|cramfs|dmg|ext|ext2|ext3|ext4|img|fat|hfs|hfsx|hxs|hxi|hxr|hxq|hxw|lit|ihex|iso|lzh|lha|lzma|mbr|mslz|mub|nsis|ntfs|rar|r00|rpm|ppmd|qcow|qcow2|qcow2c|squashfs|udf|iso|scap|uefif|vdi|vhd|vmdk|wim|esd|xar|pkg|z|taz|fb2|epub|cpio|sz|dump|pub|vsd|txt|pdf|htm|html|shtm|shtml|xhtm|xhtml|xml|rtf|java|c|cpp|h|md|lua|sh|bat|list|depend|js|jsp|mk|config|configure|machine|asm|css|desktop|inc|i|plist|pro|py|s|xpm)";

	static final String HTML_STYLE = 
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

	static final String EMPTY_HEAD = 
	SearchFragment.HTML_STYLE
	+ "</head>\r\n"
	+ "<body text='#000000' link='#0000ff' vlink='#0000ff'>\r\n";

	static final String HEAD_TABLE = 
	"</head>\r\n"
	+ "<body text='#000000' link='#0000ff' vlink='#0000ff'>\r\n"
	+ "<div align='center'>\r\n"
	+ "<table border='0' cellspacing='0' cellpadding='0' width='100%' style='width:100.0%;border-collapse:collapse'>\r\n";

	public static final String TD_COMPARE1 = "\r\n<td width='30%' valign='top' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	public static final String TD_COMPARE2 = "\r\n<td width='40%' valign='top' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	static final String TD1_CENTER = "<td width='4%' align='center' valign='middle' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	static final String TD2_CENTER = "<td width='76%' align='center' valign='middle' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	static final String TD3_CENTER = "<td width='4%' align='center' valign='middle' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	static final String TD1_LEFT = "<td width='4%' valign='top' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	static final String TD2_LEFT = "<td width='76%' valign='top' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	static final String TD3_LEFT = "<td width='4%' valign='top' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";

	public static final String SELECTED_DIR = Activity.class.getPackage().getName() + ".selectedDir";

	String currentContent = null;
	String contentLower = null;

	String home = "";

	CharSequence status = "";
	private Activity activity;
	Bundle webViewBundle = null;
	private ActionBar actionBar;

	ExtractFile extractFile = null;
	static final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
	// private String sourceContent = "";
	private static void init() {
		SearchFragment.PRIVATE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.com.free.searcher";
		SearchFragment.PRIVATE_DIR = new File(SearchFragment.PRIVATE_PATH);
		SearchFragment.PRIVATE_DIR.mkdirs();
	}
	
	static {
		String sdCardPath = System.getenv("SECONDARY_STORAGE");
		File tmp = null;
		if (sdCardPath == null) {
			init();
			Log.d("sdCardPath = null", "SearchFragment.PRIVATE_PATH = " + SearchFragment.PRIVATE_PATH);
		} else if (!sdCardPath.contains(":")) {
			SearchFragment.PRIVATE_PATH = sdCardPath + "/.com.free.searcher";
			SearchFragment.PRIVATE_DIR = new File(SearchFragment.PRIVATE_PATH);
			tmp = new File(SearchFragment.PRIVATE_PATH + "/xxx" + System.currentTimeMillis());
			try {
				if (SearchFragment.PRIVATE_DIR.mkdirs() || tmp.createNewFile()) {
					if (tmp != null) {
						Log.d("delete 1", tmp + ": " + tmp.delete());
					}
					Log.d(sdCardPath, SearchFragment.PRIVATE_DIR.getTotalSpace() + " bytes");
				} else {
					init();
				}
			} catch (IOException e) {
				//e.printStackTrace();
				Log.e("tmp", tmp + ".");
				Log.e("SearchFragment.PRIVATE_DIR", SearchFragment.PRIVATE_DIR + ".");
				init();
				Log.d("sdCardPath 1", "SearchFragment.PRIVATE_PATH = " + SearchFragment.PRIVATE_PATH);
			}
		} else if (sdCardPath.contains(":")) {
			//Multiple Sdcards show root folder and remove the Internal storage from that.
			File storage = new File("/storage");
			File[] fs = storage.listFiles();
			init();
			if (fs != null) {
				File maxPrev = SearchFragment.PRIVATE_DIR;
				long maxTotal = SearchFragment.PRIVATE_DIR.getTotalSpace();
				for (File f : fs) {
					String absolutePath = f.getAbsolutePath();
					long totalSpace = f.getTotalSpace();
					Log.d(absolutePath, totalSpace + " bytes, can write " + f.canWrite());
					try {
						String comPath = absolutePath + "/.com.free.searcher";
						if (totalSpace > maxTotal && f.canWrite() && (new File(comPath).mkdirs() || (tmp = new File(comPath + "/xxx" + System.currentTimeMillis())).createNewFile())) {
							SearchFragment.PRIVATE_PATH = comPath;
							SearchFragment.PRIVATE_DIR = new File(SearchFragment.PRIVATE_PATH);
							Log.d("sdCard ok", SearchFragment.PRIVATE_DIR + ", tmp = " + tmp);
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
						Log.e("SearchFragment.PRIVATE_DIR", SearchFragment.PRIVATE_DIR + ".");
						Log.d("sdCardPath 2", "SearchFragment.PRIVATE_PATH = " + SearchFragment.PRIVATE_PATH);
					}
				}
			}
		}
	}

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
							extractFile = new ExtractFile(currentZipFileName, SearchFragment.PRIVATE_PATH + currentZipFileName);
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
									frag.webTask = new WebTask(SearchFragment.this);
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
						webTask = new WebTask(SearchFragment.this, webView, url, status.toString());
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
					extractFile = new ExtractFile(currentZipFileName, SearchFragment.PRIVATE_PATH + currentZipFileName);
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
			(webTask = new WebTask(SearchFragment.this, webView, currentUrl)).execute();
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
						Log.d("SEARCH_REQUEST_CODE.selectedFiles", Util.arrayToString(stringExtra, true, SearchFragment.LINE_SEP));
						currentZipFileName = ""; // làm dấu để khỏi show web getSourceFile
						selectedFiles = stringExtra;
						getSourceFileTask = new GetSourceFileTask(SearchFragment.this);
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
							zr = new ZipReadingTask(SearchFragment.this);
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
						getSourceFileTask = new GetSourceFileTask(SearchFragment.this);
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
						genStardictTask = new GenStardictTask(SearchFragment.this,  stringExtra[0]);
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
						restoreStardictTask = new RestoreStardictTask(SearchFragment.this, stringExtra[0]);
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
						Log.d("DUP_REQUEST_CODE.selectedFiles", Util.arrayToString(stringExtra, true, SearchFragment.LINE_SEP));
						
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
						dupTask = new DupFinderTask(SearchFragment.this, stringExtra);
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
						getSourceFileTask = new GetSourceFileTask(SearchFragment.this);
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
//						workFolder = SearchFragment.PRIVATE_PATH + "/"; //activity.getApplicationContext().getFilesDir() + "/";
//						Log.i(Prefs.TAG, "workFolder (license and logs location) path: " + workFolder);
//						vkLogPath = SearchFragment.PRIVATE_PATH + "/vk.log";
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
		if (translateTask != null) {
			translateTask.cancel(true);
			translateTask = null;
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
		Log.d("newSearch.oldselectedFiles", Util.arrayToString(selectedFiles, true, SearchFragment.LINE_SEP));
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
//		copyAssetToDir(SearchFragment.SearchFragment.PRIVATE_PATH + "/ffmpeg.org.zip", "data/ffmpeg.org.zip");
//		try {
//			Arc.extractZipToFolder(SearchFragment.SearchFragment.PRIVATE_PATH + "/ffmpeg.org.zip", SearchFragment.SearchFragment.PRIVATE_PATH + "/ffmpeg.org");
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
//		wv.loadUrl(new File(SearchFragment.SearchFragment.PRIVATE_PATH + "/ffmpeg.org/ffmpeg.html").toURI().toString() + "#Video-and-Audio-file-format-conversion");
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
		intent.putExtra(SearchFragment.MODE, MULTI_FILES);
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
		FileUtil.getDirSize(SearchFragment.PRIVATE_DIR, entry);
		alert.setMessage("Cache has " + SearchFragment.nf.format(entry[2]) + " folders, " + SearchFragment.nf.format(entry[0])
						 + " files, " + SearchFragment.nf.format(entry[1])
						 + " bytes. " + "\r\nAre you sure you want to clear the cached files? "
						 + "\r\nAfter cleaning searching will be slow for the first times " +
						 "and the searching task maybe incorrect.");
		alert.setCancelable(true);

		alert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d("Cleaning files", FileUtil.deleteFiles(
							  SearchFragment.PRIVATE_DIR, SearchFragment.LINE_SEP, true, ""));
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
			searchTask = new SearchTask(this);
			searchTask.execute(query);
		} else {
			requestSearching = true;
			getSourceFileTask = new GetSourceFileTask(SearchFragment.this);
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
								getSourceFileTask = new GetSourceFileTask(SearchFragment.this);
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
					getSourceFileTask = new GetSourceFileTask(SearchFragment.this);
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
	
}
