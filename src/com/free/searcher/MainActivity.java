package com.free.searcher;

import android.app.*;
import android.app.ActionBar.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.geometerplus.android.fbreader.*;
import android.webkit.*;
import java.net.*;
import com.free.p7zip.*;
import com.free.translation.*;


public class MainActivity extends Activity implements OnFragmentInteractionListener { //implements GestureDetector.OnGestureListener {
	
    private int mStackLevel = 0;
	private ClipboardManager mClipboard;
	private ActionBar actionBar;
	private SplitMergeFragment mDialog;
	ReplaceAllFragment replaceFrag;
	CompareFragment compFrag;
	static final String TAG = "MainActivity";
	private static final String SPLIT = "SplitMergeFragment";
	private static final String REPLACE = "ReplaceAllFragment";
	private static final String COMPARE = "CompareFragment";
	MainFragment curFrag;
	static boolean popup = false;
	static boolean checkRE = false;
	static boolean autoTranslate = true;
	static boolean autoBackup = true;
	
	static int charsPreview = 512;
	static final int attribNo = 9;
	
	private CharSequence clipData = "";
	
	public static final String SEP_FILE = "|<>|";
	static final String propFileName = MainFragment.PRIVATE_PATH + "/searcher.ini";

	SearchView mSearchView;
	
	private ClipboardManager.OnPrimaryClipChangedListener mPrimaryChangeListener
			= new ClipboardManager.OnPrimaryClipChangedListener() {
    		    public void onPrimaryClipChanged() {
        		    updateClipData(true);
 		       }
 		   };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		getWindow().requestFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
        setContentView(R.layout.fragment_stack); //trong rong linear framelayout
		
		mClipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
		mClipboard.addPrimaryClipChangedListener(mPrimaryChangeListener);
        //updateClipData(true);
		Log.i(TAG, "protected void onCreate " + savedInstanceState);
    }

	protected void onPostCreate(android.os.Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		Log.i(TAG, "protected void onPostCreate " + savedInstanceState);
		DictionaryUtil.init(this, null);
		actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		Log.i("actionBar.getTabCount()", actionBar.getTabCount() + "");
		Log.i("onPostCreate getSelectedNavigationIndex()", actionBar.getSelectedNavigationIndex() + "");
		
		Log.i("System.getenv(\"SECONDARY_STORAGE\")", System.getenv("SECONDARY_STORAGE"));
		Log.i("getDataDirectory", Environment.getDataDirectory().toString());
		Log.i("ExternalStorageDirectory", Environment.getExternalStorageDirectory().toString());
		
		//FileUtil.copyAssetToDir(this, Constants.PRIVATE_PATH, "data/lic.html");
//		FileUtil.copyAssetToDir(this, Constants.PRIVATE_PATH, "data/List of English Irregular Verbs.xls");
//		FileUtil.copyAssetToDir(this, Constants.PRIVATE_PATH, "data/Irregular-Plural-Nouns.txt");
//		FileUtil.copyAssetToDir(this, Constants.PRIVATE_PATH, "data/Ori_Dictionary.xls");
//		FileUtil.copyAssetToDir(this, Constants.PRIVATE_PATH, "data/englishPCFG.ser.gz");
//		FileUtil.copyAssetToDir(this, Constants.PRIVATE_PATH, "data/englishFactored.ser.gz");
//		FileUtil.copyAssetToDir(this, Constants.PRIVATE_PATH, "data/new_dictd_www.freedict.de_anh-viet.txt");
//		FileUtil.copyAssetToDir(this, Constants.PRIVATE_PATH, "data/new_dictd_www.freedict.de_anh-viet.txt.dict");
//		FileUtil.copyAssetToDir(this, Constants.PRIVATE_PATH, "data/new_dictd_www.freedict.de_anh-viet.txt.idx");
        new File("/sdcard/AppProjects/Searcher/bin/resources.ap_").delete();
		new File("/sdcard/AppProjects/Searcher/bin/classes.dex").delete();
		new File("/sdcard/AppProjects/Searcher/bin/Searcher.apk").delete();
//		new Thread(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						new DictionaryLoader().readOriTextDictionary();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//		}
//			
//		).start();
		
//		new  Andro7za().extract("/storage/emulated/0/Download/K.ppt", "", "", "", "/sdcard/rar");
		
		if (savedInstanceState == null || actionBar.getTabCount() == 0) {
			try {
				initDialog(SPLIT);
				initDialog(REPLACE);
				initDialog(COMPARE);
				final Properties prop = new Properties();
				
				File f = new File(propFileName);
				if (!f.exists()) {
					addFragmentToStack();
					return;
				}
				prop.load(new FileInputStream(propFileName));
				if (prop.size() == 0) {
					addFragmentToStack();
					return;
				}
				Set<Map.Entry<String, String>> se = (Set<Map.Entry<String, String>>) prop.entrySet();
				//Iterator<Map.Entry<String, String>> iter = (Iterator<Map.Entry<String, String>>) se.iterator();
				
				final List<Entry<String, String>> arrList = new ArrayList<Entry<String, String>>(16);
				for (final Map.Entry<String, String> entry : se) {
				//while (iter.hasNext()) {
					//final Map.Entry<String, String> entry = iter.next();
					//System.out.println(entry.getKey() + "=" + entry.getValue());
					arrList.add(new Entry<String, String>(entry.getKey(), entry.getValue()));
				}
				Collections.sort(arrList);
				
				for (int i = 0; i < (arrList.size())/ attribNo; i++) {
					
					addFragmentToStack();
					for (int j = 0; j < attribNo; j++) {
						final Entry<String, String> entry = arrList.get(i * attribNo + j);
						
						Log.i("i, entry", i + ", " + entry);
						if (entry.getKey().endsWith(".load")){
							curFrag.load = entry.getValue();
						} else if (entry.getKey().endsWith(".currentUrl")){
							curFrag.currentUrl = entry.getValue(); // URLDecoder.decode(entry.getValue().replaceAll("\\+", "-000000000000000-"), "utf-8");
							Log.i("i, currentUrl, tempFrag", i + ", " + curFrag.currentUrl + ", " + curFrag);
						} else if (entry.getKey().endsWith(".currentSearching")){
							curFrag.currentSearching = entry.getValue();
						} else if (entry.getKey().endsWith(".status")){
							curFrag.status = entry.getValue();
						} else if (entry.getKey().endsWith(".X")){
							curFrag.locX = Integer.parseInt(entry.getValue());
						} else if (entry.getKey().endsWith(".Y")){
							curFrag.locY = Integer.parseInt(entry.getValue());
						} else if (entry.getKey().endsWith(".currentZipFileName")){
							curFrag.currentZipFileName = entry.getValue();
							
						} else if (entry.getKey().endsWith(".selectedFiles")){
							curFrag.selectedFiles = entry.getValue().split("\\|<>\\|");
//						} else if (entry.getKey().endsWith(".tempFList")){
//							List<String> asList = Arrays.asList(entry.getValue().split("\\|<>\\|"));
//							curFrag.tempFList = new ArrayList<String>();
//							curFrag.tempFList.addAll(asList);
						} else if (entry.getKey().endsWith(".home")){
							curFrag.home = entry.getValue();
//						} else if (entry.getKey().endsWith(".searchFileResult")){
//							curFrag.searchFileResult = entry.getValue();
						} 

					} // end for
					if ("Search".equals(curFrag.load)) {
						curFrag.getSourceFileTask = new GetSourceFileTask(curFrag);
						curFrag.getSourceFileTask.execute();
					}
					if (i < (arrList.size())/attribNo - 1) {
						getFragmentManager().beginTransaction().detach(curFrag).commit();
						Log.i("detach(curFrag)", (i + 1) + "");
					}
					showToast("Loading...");
				} // end for
				checkRE = Boolean.parseBoolean(prop.get("checkRE") + "");
				Log.i("checkRE", checkRE + "");
				autoBackup = Boolean.parseBoolean(prop.get("autoBackup") + "");
				Log.i("autoBackup", autoBackup + "");
				popup = Boolean.parseBoolean(prop.get("popup") + "");
				Log.i("popup", popup + "");
				autoTranslate = Boolean.parseBoolean(prop.get("autoTranslate") + "");
				Log.i("autoTranslate", autoTranslate + "");
				charsPreview = Integer.parseInt(prop.get("charsPreview") + "");
				Log.i("charsPreview", charsPreview + "");
				int tLevel = Integer.parseInt(prop.get("tLevel") + "");
				Log.i("tLevel", tLevel + "");
				if (!(tLevel == actionBar.getTabCount() - 1)) {
					getFragmentManager().beginTransaction().detach(curFrag).commit();
					actionBar.getTabAt(tLevel).select();
				}
				
				
//				mDialog.files = (String)prop.get("files");
//				mDialog.saveTo = (String)prop.get("saveTo");
//				mDialog.parts = (String)prop.get("parts");
//				mDialog.partSize = (String)prop.get("partSize");
				showToast("Long click screen to switch on/off full screen mode.\nLong press back button to exit.");
				//myG = new GestureDetector(this, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
        } else {
			actionBar.setSelectedNavigationItem(savedInstanceState.getInt("level", 0));
			Log.i("actionBar.setSelectedNavigationItem", actionBar.getSelectedNavigationIndex() + "");
        }
		
		
	}
	//GestureDetector myG;
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        //boolean retVal = myG.onTouchEvent(event);
        return super.onTouchEvent(event); // || retVal;
    }
	
	
	private void showToast(String st) {
		Toast.makeText(this, st, Toast.LENGTH_SHORT).show();
	}
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
		outState.putInt("level", actionBar.getSelectedNavigationIndex());
		outState.putString("currentDialog", currentDialog);
		Log.i(TAG, "protected onSaveInstanceState.getSelectedNavigationIndex" + actionBar.getSelectedNavigationIndex());
		saveAppStatus();
    }
	
	@Override
    protected void onPause() {
        super.onPause();
		Log.i(TAG, "onPause");
		//if (isFinishing()) {
			saveAppStatus();
		//}
    }

	private void saveAppStatus() {
		try {
			if (mDialog != null && mDialog.fileET != null) {
				mDialog.files = mDialog.fileET.getText().toString();
				mDialog.saveTo = mDialog.saveToET.getText().toString();
				mDialog.parts = mDialog.partsET.getText().toString();
				mDialog.partSize = mDialog.partSizeET.getText().toString();

				FileOutputStream fos = new FileOutputStream(MainFragment.PRIVATE_PATH + "/SplitMergeFragment.ser");
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.writeObject(mDialog);
				bos.flush();
				bos.close();
				fos.close();
			}
			if (replaceFrag != null && replaceFrag.fileET != null) {
				replaceFrag.files = replaceFrag.fileET.getText().toString();
				replaceFrag.saveTo = replaceFrag.saveToET.getText().toString();
				replaceFrag.replace = replaceFrag.replaceET.getText().toString();
				replaceFrag.by = replaceFrag.byET.getText().toString();
				replaceFrag.isRegex = replaceFrag.isRegexCB.isChecked();
				replaceFrag.caseSensitive = replaceFrag.caseSensitiveCB.isChecked();
				replaceFrag.includeEnter = replaceFrag.includeEnterCB.isChecked();
				FileOutputStream fos = new FileOutputStream(MainFragment.PRIVATE_PATH + "/ReplaceAllFragment.ser");
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.writeObject(replaceFrag);
				bos.flush();
				bos.close();
				fos.close();
			}
			if (compFrag != null && compFrag.fileET != null) {
				compFrag.files = compFrag.fileET.getText().toString();
				compFrag.saveTo = compFrag.saveToET.getText().toString();
				
				FileOutputStream fos = new FileOutputStream(MainFragment.PRIVATE_PATH + "/CompareFragment.ser");
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.writeObject(compFrag);
				bos.flush();
				bos.close();
				fos.close();
			}
			Properties prop = new Properties();
			
			for (int i = 0; i < actionBar.getTabCount(); i++) {
				String barText = actionBar.getTabAt(i).getText().toString();
				MainFragment tempFrag = (MainFragment) getFragmentManager().findFragmentByTag(barText);
				Log.i(TAG, "saveAppStatus actionBar.getTabAt(i)" + actionBar.getTabAt(i).getText());
				if (tempFrag != null) {

					//prop.put(barText + ".checkRE", "" + tempFrag.checkRE);
					//prop.put(barText + ".popup", "" + tempFrag.popup);
					//prop.put(barText + ".fullScreen", "" + tempFrag.fullScreen);
					//prop.put(barText + ".charsPreview", "" +  tempFrag.charsPreview);
					prop.put(barText + ".load", tempFrag.load);
					prop.put(barText + ".currentUrl", tempFrag.currentUrl == null ? "" : tempFrag.currentUrl);

					prop.put(barText + ".currentSearching", tempFrag.currentSearching);
					prop.put(barText + ".status", "" + tempFrag.statusView.getText());
					prop.put(barText + ".X", "" + tempFrag.webView.getScrollX());
					prop.put(barText + ".Y", "" + tempFrag.webView.getScrollY());

					prop.put(barText + ".currentZipFileName", tempFrag.currentZipFileName);
					prop.put(barText + ".selectedFiles", "" + Util.arrayToString(tempFrag.selectedFiles, false, SEP_FILE));
					prop.put(barText + ".home", "" + tempFrag.home);
//					prop.put(barText + ".searchFileResult", "" + tempFrag.searchFileResult);
//					prop.put(barText + ".tempFList", "" + Util.listToString(tempFrag.tempFList, false, SEP_FILE));
//					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
//							SearchFragment.PRIVATE_PATH + "/Tab " + (i + 1) + ".webViewBundle"));
//					oos.writeObject(curFrag.webViewBundle);
					
					Log.i("onSaveInstanceState.prop.put(i)", barText + ", " + tempFrag.currentUrl);
				}
			}
			prop.put("popup", popup + "");
			prop.put("autoTranslate", autoTranslate + "");
			prop.put("checkRE", checkRE + "");
			prop.put("autoBackup", autoBackup + "");
			prop.put("charsPreview", charsPreview + "");
			prop.put("tLevel", actionBar.getSelectedNavigationIndex() + "");
			
			prop.store(new BufferedOutputStream(new FileOutputStream(propFileName)), "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		currentDialog = savedInstanceState.getString("currentDialog");
		Log.i(TAG, "protected onRestoreInstanceState" + savedInstanceState);
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
		Log.i(TAG, "protected onDestroy");
        mClipboard.removePrimaryClipChangedListener(mPrimaryChangeListener);
    }

	void updateClipData(boolean updateType) {
        ClipData clip = mClipboard.getPrimaryClip();
//        String[] mimeTypes = clip != null ? clip.getDescription().filterMimeTypes("*/*") : null;
//        if (mimeTypes != null) {
//            mMimeTypes.setText("");
//            for (int i=0; i<mimeTypes.length; i++) {
//                if (i > 0) {
//                    mMimeTypes.append("\n");
//                }
//                mMimeTypes.append(mimeTypes[i]);
//            }
//        } else {
//            mMimeTypes.setText("NULL");
//        }

        if (clip != null && curFrag.getView().isShown()) {
            ClipData.Item item = clip.getItemAt(0);
            clipData = item.getText();
			if (autoTranslate) {
				if (clipData != null && clipData.length() > 0) {
					DictionaryUtil.openTextInDictionary(this, clipData + "", false, 100, 10);
				}
			}
		}
    }
	
    MainFragment addFragmentToStack() {
		Log.i(TAG, "addFragmentToStack");
		ActionBar.Tab newTab = actionBar.newTab();
		TabListener tabListener = new TabListener(this, "Tab " + ++mStackLevel);
		newTab.setText("Tab " + mStackLevel).setTabListener(tabListener);
		actionBar.addTab(newTab);
		if (actionBar.getTabCount() > 1) {
			MainFragment tempFrag = curFrag;
			newTab.select();
			MainFragment tabFrag = tabListener.mFragment;
			//tabFrag.checkRE = tempFrag.checkRE;
			//tabFrag.popup = tempFrag.popup;
			//tabFrag.fullScreen = tempFrag.fullScreen;
			//tabFrag.charsPreview = tempFrag.charsPreview;
			tabFrag.load = tempFrag.load;
			tabFrag.currentSearching = tempFrag.currentSearching;
			tabFrag.selectedFiles = tempFrag.selectedFiles;
			tabFrag.files = tempFrag.files;
		}

		Log.i("addFragmentToStack", mStackLevel+"");
		return tabListener.mFragment;
    }
	
	public boolean onKeyLongPress(int keyCode, android.view.KeyEvent event) {
		super.onBackPressed();
		return true;
	}
	
	private static final int TIME_INTERVAL = 1000; // milliseconds, desired time passe. between two back presses.
	private long mBackPressed;
	@Override
	public void onBackPressed(){
		if (mBackPressed +TIME_INTERVAL <System.currentTimeMillis()){
			//super.onBackPressed();
			//return;
			WebView webView = curFrag.webView;
			if (webView.canGoBack()) {
				webView.goBack();
				String originalUrl = webView.getOriginalUrl();
				if (originalUrl.indexOf("android_asset") < 0) {
					try {
						curFrag.currentUrl = originalUrl;
						String st = URLDecoder.decode(originalUrl.replaceAll("\\+", "-000000000000000-"), "utf-8").replaceAll("-000000000000000-", "+");
						curFrag.statusView.setText(st.substring(
											   "file://".length(),st.length()));//.replaceAll("%20", " "));
					}
					catch (UnsupportedEncodingException e) {
						Log.i("android_asset", e.getMessage(), e);
					}
				} else {
					curFrag.statusView.setText("Can't show source page");
				}
			} else {
				showToast("No more page");
			}
		}else{
			Toast.makeText(getBaseContext(), "Long press back button to exit",
			Toast.LENGTH_SHORT).show(); 
		}
		mBackPressed =System.currentTimeMillis();
	}
//	private boolean SHOW = false;
//	@Override
//	public boolean onTouchEvent(android.view.MotionEvent event) {
//		Log.i("MotionEvent", event + "");
//		
//		if ((event.getAction() ==  MotionEvent.ACTION_DOWN || event.getAction() ==  MotionEvent.ACTION_POINTER_UP) && curFrag.statusView != null) {
//			int curVis = curFrag.statusView.getSystemUiVisibility();
//			SHOW = !SHOW;
//			Log.i("SHOW", "cur = " + curVis + ", need show = " + SHOW + "");
//			curFrag.setNavVisibility(SHOW); //((curVis&View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0);
//		}
//		
////		if ((event.getAction() ==  MotionEvent.ACTION_UP || event.getAction() ==  MotionEvent.ACTION_POINTER_UP) && curFrag.statusView != null) {
////			int curVis = curFrag.statusView.getSystemUiVisibility();
////			Log.i("curVis", curVis + ", " + ((curVis&View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0));
////			curFrag.setNavVisibility((curVis&View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0);
////		} else if (curFrag.statusView != null) { //event.getAction() ==  MotionEvent.ACTION_MOVE && 
////			// When the user scrolls, we hide navigation elements.
////			curFrag.setNavVisibility(false);
////		}
//
//		return super.onTouchEvent(event);
//	}
	
	
//	@Override
//	public boolean onQueryTextSubmit(String query) {
//		return curFrag.onQueryTextSubmit(query);
//	}
//
//	@Override
//	public boolean onQueryTextChange(String newText) {
//		return curFrag.onQueryTextChange(newText);
//	}
//
	Menu menu;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		Log.i(TAG, "onCreateOptionsMenu");
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.search, menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search, menu);
		this.menu = menu;

		menu.add(0, Menu.FIRST, 307, "Find in page");

		MenuItem searchItem = menu.findItem(R.id.action_search);
		// searchItemId = searchItem.getItemId();
		// Log.i("searchItemId", searchItemId + "");
		mSearchView = (SearchView) searchItem.getActionView();
		mSearchView.setEnabled(true);
		MenuItem popup = menu.findItem(R.id.action_popup);
		popup.setChecked(this.popup);
		MenuItem autoTranslate = menu.findItem(R.id.action_autoTranslate);
		autoTranslate.setChecked(this.autoTranslate);
		MenuItem checkRE = menu.findItem(R.id.action_regex);
		checkRE.setChecked(this.checkRE);
		MenuItem autoBackup = menu.findItem(R.id.action_autoBackup);
		autoBackup.setChecked(this.autoBackup);
		//mSearchView.setQuery(currentSearching, false);
		MenuItem batchConvert = menu.findItem(R.id.action_batchConvert);
		batchConvert.setVisible(false);
		
		if (curFrag != null) {
			curFrag.mSearchView = mSearchView;
			curFrag.mSearchView.setOnQueryTextListener(curFrag);
			// mSearchView.setQuery(curFrag.currentSearching, false);
		}
		
		// Log.i("searchText", mSearchView.getQuery().toString());
		
		System.out.println(Constants.PRIVATE_PATH);
		if (!new File (Constants.PRIVATE_PATH + "/data/List of English Irregular Verbs.xls").exists() ||
			! new File (Constants.PRIVATE_PATH, "/data/Irregular-Plural-Nouns.txt").exists() ||
			! new File (Constants.PRIVATE_PATH + "/data/Ori_Dictionary.xls").exists() ||
			! new File (Constants.PRIVATE_PATH + "/data/englishPCFG.ser.gz").exists() ||
			! new File (Constants.PRIVATE_PATH + "/data/englishFactored.ser.gz").exists() ||
			! new File (Constants.PRIVATE_PATH + "/data/new_dictd_www.freedict.de_anh-viet.txt.dict").exists() ||
			! new File (Constants.PRIVATE_PATH + "/data/new_dictd_www.freedict.de_anh-viet.txt.idx").exists()) {
			MenuItem translate = menu.findItem(R.id.action_translate);
			translate.setVisible(false);
		}
		return true;
	}
	int searchItemId;
	
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		Log.i(TAG, "onPrepareOptionsMenu " + menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "onOptionsItemSelected " + item);
		return curFrag.onOptionsItemSelected(item);
	}
	
//	void find() {
//		curFrag.find();
//	}

	protected boolean isAlwaysExpanded() {
		return false;
	}
	
	public boolean restore(MenuItem item) {
		curFrag.restore(item);
		return true;
	}
	
	public boolean dupFinder(MenuItem item) {
		curFrag.dupFinder(item);
		return true;
	}
	
	public boolean gen(MenuItem item) {
		curFrag.gen(item);
		return true;
	}
	
	public boolean batchConvert(MenuItem item) {
//		curFrag.batchConvert(item);
		return true;
	}
	
	public boolean translate(MenuItem item) {
		curFrag.translate(item);
		return true;
	}
	
	@Override
	public void onOk(DialogFragment fra) {
		Log.i(TAG, "onOk " + fra);
		if (fra instanceof SplitMergeFragment) {
			mDialog = (SplitMergeFragment)fra;
			String p = mDialog.partsET.getText().toString();
			String s = mDialog.partSizeET.getText().toString();
			if (p.length() == 0 && s.length() == 0 || "0".equals(p) && "0".equals(s) || "1".equals(p)) {
				showToast("Invalid number");
				return;
			}
			if (!Util.isInteger(p)) {
				showToast("Invalid part number");
				return;
			}
			if (!Util.isInteger(s)) {
				showToast("Invalid size number");
				return;
			}
			MergeSplitTask ms = new MergeSplitTask(this, 
												   Util.stringToList(mDialog.fileET.getText() + "", "|"), 
												   mDialog.saveToET.getText() + "", 
												   Util.toNumberWithDefault(Util.toNumberWithDefault(p, "0"), 0), 
												   Util.toNumberWithDefault(Util.toNumberWithDefault(s, "0"), 0));
			
			ms.execute();
		} else if (fra instanceof ReplaceAllFragment) {
			replaceFrag = (ReplaceAllFragment)fra;
			String[] stringExtra = Util.stringToArray(replaceFrag.fileET.getText() + "", "|");
			Log.d("REPLACE_REQUEST_CODE.selectedFiles", stringExtra[0]);
			replaceFrag.files = replaceFrag.fileET.getText().toString();
			replaceFrag.saveTo = replaceFrag.saveToET.getText().toString();
			replaceFrag.replace = replaceFrag.replaceET.getText().toString();
			replaceFrag.by = replaceFrag.byET.getText().toString();
			replaceFrag.isRegex = replaceFrag.isRegexCB.isChecked();
			replaceFrag.caseSensitive = replaceFrag.caseSensitiveCB.isChecked();
			replaceFrag.includeEnter = replaceFrag.includeEnterCB.isChecked();
			List<File> lf = FileUtil.getFiles(stringExtra);
			if (replaceFrag.includeEnter) { // multiline
				new ReplaceAllTask(this, lf, replaceFrag.saveTo, replaceFrag.isRegex, replaceFrag.caseSensitive, new String[]{replaceFrag.replace}, new String[]{replaceFrag.by}).execute();
			} else {
				String[] replaces = replaceFrag.replaceET.getText().toString().split("\r?\n");
				String[] bys = replaceFrag.byET.getText().toString().split("\r?\n");
				Log.d("bys.length ", bys.length + ".");
				if (replaces.length == bys.length) {
					new ReplaceAllTask(this, lf, replaceFrag.saveTo, replaceFrag.isRegex, replaceFrag.caseSensitive, replaces, bys).execute();
				} else {
					showToast("The number of lines of replace and by are not equal");
				}
			}
			curFrag.selectedFiles = stringExtra;
			GetSourceFileTask getSourceFileTask = new GetSourceFileTask(curFrag);
			getSourceFileTask.execute();
		} else if (fra instanceof CompareFragment) {
			compFrag = (CompareFragment)fra;
			curFrag.oriDoc = compFrag.fileET.getText().toString();
			curFrag.modifiedDoc = compFrag.saveToET.getText().toString();
			if (new File(curFrag.oriDoc).exists() && new File(curFrag.modifiedDoc).exists()) {
				curFrag.stopReadAndSearch();
				curFrag.currentZipFileName = ""; // làm dấu để khỏi show web getSourceFile
				curFrag.selectedFiles = new String[] {curFrag.oriDoc, curFrag.modifiedDoc};
				curFrag.load = "Search";
				curFrag.requestCompare = true;
				curFrag.requestSearching = false;
				curFrag.getSourceFileTask = new GetSourceFileTask(curFrag);
				curFrag.getSourceFileTask.execute();
			} else {
				showToast("There are no files to compare");
			}
		}
		fra.dismiss();
	}

	@Override
	public void onCancelChooser(DialogFragment fra) {
		Log.i(TAG, "onCancelChooser ");
		fra.dismiss();
		showToast("Nothing to do");
	}

	public boolean splitMerge(MenuItem item) {
		Log.i(TAG, "splitMerge");
//		initDialog(null);
		currentDialog = SPLIT;
		mDialog.show(getFragmentManager(), "SplitMerge");
		return true;
	}

	private void initDialog(String clazz) {
		
		File fi = new File(MainFragment.PRIVATE_PATH + "/" + clazz + ".ser");//"/SplitMergeFragment.ser");
		Log.d(TAG, fi.getAbsolutePath());
		if (fi.exists() && fi.length() > 0) {
			try	{
				FileInputStream fis = new FileInputStream(fi);
				BufferedInputStream bis = new BufferedInputStream(fis);
				ObjectInputStream ois = new ObjectInputStream(bis);
				if (SPLIT.equals(clazz)) {
					mDialog = (SplitMergeFragment) ois.readObject();
				} else if (REPLACE.equals(clazz)) {
					replaceFrag = (ReplaceAllFragment) ois.readObject();
				} else if (COMPARE.equals(clazz)) {
					compFrag = (CompareFragment) ois.readObject();
				}
				ois.close();
				bis.close();
				fis.close();
				//return (DialogFragment) ois.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (SPLIT.equals(clazz)) {
			mDialog = SplitMergeFragment.newInstance("", "");
		} else if (REPLACE.equals(clazz)) {
			replaceFrag = ReplaceAllFragment.newInstance("", "");
		} else if (COMPARE.equals(clazz)) {
			compFrag = CompareFragment.newInstance("", "");
		}

	}
	
	public boolean compare(MenuItem item) {
		currentDialog = COMPARE;
		compFrag.show(getFragmentManager(), "Compare");
		return true;
	}
	
	public boolean replace(MenuItem item) {
		currentDialog = REPLACE;
		replaceFrag.show(getFragmentManager(), "SplitMerge");
		return true;
	}
	
	public boolean about(MenuItem item) {
		FileUtil.copyAssetToDir(this, MainFragment.PRIVATE_PATH, "data/lic.html"); //"/lic.html"

		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.parameters, null);
		final EditText preview = (EditText) textEntryView.findViewById(R.id.preview);
		final TextView previewLabel = (TextView) textEntryView.findViewById(R.id.previewLabel);
		previewLabel.setVisibility(View.GONE);
		preview.setVisibility(View.GONE);
		
		final WebView wv = (WebView) textEntryView.findViewById(R.id.webView);
		wv.loadUrl(new File(MainFragment.PRIVATE_PATH + "/data/lic.html").toURI().toString());
		wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		AlertDialog dialog = new AlertDialog.Builder(this)
			.setIconAttribute(android.R.attr.dialogIcon)
			.setTitle("Searcher")
			.setView(textEntryView)
			.setNegativeButton(R.string.ok,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
				}
			}).create();
		dialog.show();
		return true;
	}
	
	public boolean add(MenuItem item) {
		addFragmentToStack();
		return true;
	}

	public boolean remove(MenuItem item) {
		//ActionBar actionBar = getActionBar();
		if (actionBar.getTabCount() > 1) {
			Log.i("remove", mStackLevel+"");
			FragmentTransaction beginTransaction = curFrag.getFragmentManager().beginTransaction();
			beginTransaction.remove(curFrag);
			beginTransaction.commit();
			actionBar.removeTab(actionBar.getSelectedTab());
		}
		return true;
	}

	public boolean showDict(MenuItem item) {
		if (clipData != null && clipData.length() > 0) {
			DictionaryUtil.openTextInDictionary(this, clipData + "", false, 100, 10);
		}
		return true;
	}

	public boolean home(MenuItem item) {
		return curFrag.home(item);
	}

	
	public boolean closeTab(MenuItem item) {
		return remove(item);
	}
	
//	public boolean compare(MenuItem item) {
//		return curFrag.compare(item);
//	}
//	
	public boolean closeOthers(MenuItem item) {
		if (actionBar.getTabCount() > 1) {
			Log.i("closeOthers", mStackLevel + "");
			FragmentTransaction beginTransaction;
			int selectedIndex = actionBar.getSelectedNavigationIndex();
			Log.i("closeOthers.selectedIndex", selectedIndex + "");
			for (int i = actionBar.getTabCount() - 1; i >= 0; i--) {
				
				Log.i("closeOthers.i", i +"");
				if (i != selectedIndex) {
					String barText = actionBar.getTabAt(i).getText().toString();
					Fragment fragment = getFragmentManager().findFragmentByTag(barText);
					Log.i("closeOthers.actionBar.getTabAt(i).getText()", actionBar.getTabAt(i).getText() + "");
					if (fragment != null && fragment != curFrag) {
						beginTransaction = fragment.getFragmentManager().beginTransaction();
						beginTransaction.remove(fragment);
						beginTransaction.commit();
						actionBar.removeTabAt(i);
						Log.i("closeOthers.actionBar.removeTabAt(i)", barText + "");
					}
				}
			}
		}
		return true;
	}
	
	public boolean back(MenuItem item) {
		curFrag.back(item);
		curFrag.webView.getHandler().postDelayed(new Runnable() {
				@Override public void run() {
					curFrag.setNavVisibility(true);
				}
			}, 1000);
		return true;
	}

	public boolean next(MenuItem item) {
		curFrag.next(item);
		curFrag.webView.getHandler().postDelayed(new Runnable() {
				@Override public void run() {
					curFrag.setNavVisibility(true);
				}
			}, 1000);
		return true;
	}

	public boolean preview(MenuItem item) {
		return curFrag.preview(item);
	}

	public boolean popup(MenuItem item) {
		item.setChecked(!item.isChecked());
		popup = item.isChecked();
		Log.i("popup", "" + popup);
		return true;
	}

	public boolean autoTranslate(MenuItem item) {
		item.setChecked(!item.isChecked());
		autoTranslate = item.isChecked();
		Log.i("autoTranslate", "" + autoTranslate);
		return true;
	}
	
	public boolean autoBackup(MenuItem item) {
		item.setChecked(!item.isChecked());
		autoBackup = item.isChecked();
		Log.i("autoBackup", "" + autoBackup);
		return true;
	}
	
//	public boolean fullScreen(MenuItem item) {
//		item.setChecked(!item.isChecked());
//		fullScreen = item.isChecked();
//		//fullScreen(fullScreen);
//		return true;
//	}
	
	public boolean regex(MenuItem item) {
		return curFrag.regex(item);
	}

	public boolean viewSource(MenuItem item) {
		return curFrag.viewSource(item);
	}

	public boolean newSearch(MenuItem item) {
		return curFrag.newSearch(item);
	}
	
	public boolean readZip(MenuItem item) {
		return curFrag.readZip(item);
	}

	public boolean viewMode(MenuItem item) {
		return true;
	}

	public boolean clearCache(MenuItem item) {
		return curFrag.clearCache(item);
	}
	String currentDialog = "";
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MainFragment.FILES_REQUEST_CODE) {
			initDialog(currentDialog);
			if (resultCode == Activity.RESULT_OK) {
				String[] stringExtra = data.getStringArrayExtra(FolderChooserActivity.SELECTED_DIR);
				if (SPLIT.equals(currentDialog)) {
					mDialog.files =  Util.arrayToString(stringExtra, false, "|");
					mDialog.show(getFragmentManager(), "SplitMerge");
				} else if (REPLACE.equals(currentDialog)) {
					replaceFrag.files = Util.arrayToString(stringExtra, false, "|");
					replaceFrag.show(getFragmentManager(), "ReplaceAll");
				} else if (COMPARE.equals(currentDialog)) {
					compFrag.files = stringExtra[0];
					compFrag.show(getFragmentManager(), "Compare");
				}
			} else { // RESULT_CANCEL
				showToast("No file selected");
			}
			
		} else if (requestCode == MainFragment.SAVETO_REQUEST_CODE) {
			initDialog(currentDialog);

			if (resultCode == Activity.RESULT_OK) {
				String[] stringExtra = data.getStringArrayExtra(FolderChooserActivity.SELECTED_DIR);
				if (SPLIT.equals(currentDialog)) {
					mDialog.saveTo =  stringExtra[0];
					mDialog.show(getFragmentManager(), "SplitMerge");
				} else if (REPLACE.equals(currentDialog)) {
					replaceFrag.saveTo = stringExtra[0];
					replaceFrag.show(getFragmentManager(), "ReplaceAll");
				} else if (COMPARE.equals(currentDialog)) {
					compFrag.saveTo = stringExtra[0];
					compFrag.show(getFragmentManager(), "Compare");
				}
			} else { // RESULT_CANCEL
				showToast("No folder selected");
			}
			
		} else if (requestCode == MainFragment.REPLACE_REQUEST_CODE) {
			
		} else {
			curFrag.onActivityResult(requestCode, resultCode, data);
		}
	}
}

class TabListener implements ActionBar.TabListener {
	final MainActivity mActivity;
	final String mTag;
	final Bundle mArgs;
	MainFragment mFragment;
//	int clickCount = 0;

	public TabListener(MainActivity activity, String tag) {
		this(activity, tag, new Bundle());
	}

	public TabListener(MainActivity activity, String tag, Bundle args) {
		mActivity = activity;
		mTag = tag;
		mArgs = args;

		// Check to see if we already have a fragment for this tab, probably
		// from a previously saved state.  If so, deactivate it, because our
		// initial state is that a tab isn't shown.
		mFragment = (MainFragment) mActivity.getFragmentManager().findFragmentByTag(mTag);
		Log.i("TabListener.mFragment", mFragment+"");
		if (mFragment != null && !mFragment.isDetached()) {
			FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
			ft.detach(mFragment);
			ft.commit();
		}
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		Log.i("public void onTabSelected()",mTag+"");
		if (mFragment == null) {
			mFragment = (MainFragment)Fragment.instantiate(mActivity, MainFragment.class.getName(), mArgs);
			ft.add(android.R.id.content, mFragment, mTag);
			Log.i("mFragment == null, added", mFragment.getTag() + "");
		} else {
			Log.i("mFragment != null, attach", mFragment.getTag() + "");
			ft.attach(mFragment);
		}
		Log.i("onTabSelected(), currentUrl", mFragment.currentUrl + "");
		mActivity.curFrag = mFragment;
		if (mActivity.mSearchView != null) {
			mActivity.curFrag.mSearchView = mActivity.mSearchView;
			mActivity.curFrag.mSearchView.setOnQueryTextListener(mActivity.curFrag);
			//mActivity.curFrag.mSearchView.setQuery(mActivity.curFrag.currentSearching, false);
		}
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if (mFragment != null && mFragment.webView != null && mFragment.statusView != null) {
			mFragment.locX = mFragment.webView.getScrollX();
			mFragment.locY = mFragment.webView.getScrollY();
			mFragment.status = mFragment.statusView.getText();
			mFragment.webView.saveState((mFragment.webViewBundle = new  Bundle()));

			Log.i("onTabUnselected(), detach", mTag + "");
			Log.i("onTabUnselected.locX, locY", mFragment.locX + ", " + mFragment.locY);
			Log.i("onTabUnselected(), currentUrl", mFragment.currentUrl + "");
			Log.i("onTabUnselected(), status", mFragment.status + "zzz");
			ft.detach(mFragment);
		}
	}

	public void onTabReselected(Tab tab, FragmentTransaction ft) {

		PopupMenu popup = new PopupMenu(mFragment.statusView.getContext(), mFragment.statusView);
        popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				if ("Close Tab".equals(item.getTitle())) {
					mActivity.closeTab(item);
				} else if ("Close Others".equals(item.getTitle())) {
					mActivity.closeOthers(item);
				} else if ("New Tab".equals(item.getTitle())) {
					mActivity.add(item);
				}
				return true;
			}
		});
        popup.show();
	}
}
