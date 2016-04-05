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


public class SearchStack extends Activity { //implements GestureDetector.OnGestureListener {
	
    private int mStackLevel = 0;
	private ClipboardManager mClipboard;
	private ActionBar actionBar;
	
	SearchFragment curFrag;
	static boolean popup = false;
	static boolean checkRE = false;
	static boolean autoTranslate = true;
	static boolean autoBackup = true;
	
	static int charsPreview = 512;
	static final int attribNo = 9;
	
	private CharSequence clipData = "";
	
	public static final String SEP_FILE = "|<>|";
	static final String propFileName = SearchFragment.PRIVATE_PATH + "/searcher.ini";

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
		Log.d("protected void onCreate", savedInstanceState+"");
    }

	protected void onPostCreate(android.os.Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		DictionaryUtil.init(this, null);
		actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		Log.d("actionBar.getTabCount()", actionBar.getTabCount() + "");
		Log.d("onPostCreate getSelectedNavigationIndex()", actionBar.getSelectedNavigationIndex() + "");
		
		Log.d("System.getenv(\"SECONDARY_STORAGE\")", System.getenv("SECONDARY_STORAGE"));
		Log.d("getDataDirectory", Environment.getDataDirectory().toString());
		Log.d("ExternalStorageDirectory", Environment.getExternalStorageDirectory().toString());
		
		FileUtil.copyAssetToDir(this, Constants.PRIVATE_PATH, "dtds/List of English Irregular Verbs.xls");
		FileUtil.copyAssetToDir(this, Constants.PRIVATE_PATH, "dtds/Irregular-Plural-Nouns.txt");
		FileUtil.copyAssetToDir(this, Constants.PRIVATE_PATH, "dtds/Ori_Dictionary.xls");
		FileUtil.copyAssetToDir(this, Constants.PRIVATE_PATH, "dtds/englishPCFG.ser.gz");
		FileUtil.copyAssetToDir(this, Constants.PRIVATE_PATH, "dtds/englishFactored.ser.gz");
		FileUtil.copyAssetToDir(this, Constants.PRIVATE_PATH, "dtds/new_dictd_www.freedict.de_anh-viet.txt");
		FileUtil.copyAssetToDir(this, Constants.PRIVATE_PATH, "dtds/new_dictd_www.freedict.de_anh-viet.txt.dict");
		FileUtil.copyAssetToDir(this, Constants.PRIVATE_PATH, "dtds/new_dictd_www.freedict.de_anh-viet.txt.idx");
        new File("/sdcard/AppProjects/Tran/bin/resources.ap_").delete();
		new File("/sdcard/AppProjects/Tran/bin/classes.dex").delete();
		new File("/sdcard/AppProjects/Tran/bin/Tran.apk").delete();
//		new  Andro7za().extract("/storage/emulated/0/Download/K.ppt", "", "", "", "/sdcard/rar");
		
		if (savedInstanceState == null || actionBar.getTabCount() == 0) {
			try {
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
				
				for (int i = 0; i < arrList.size() / attribNo; i++) {
					
					addFragmentToStack();
					for (int j = 0; j < attribNo; j++) {
						final Entry<String, String> entry = arrList.get(i * attribNo + j);
						
						Log.d("i, entry", i + ", " + entry);
						if (entry.getKey().endsWith(".load")){
							curFrag.load = entry.getValue();
						} else if (entry.getKey().endsWith(".currentUrl")){
							curFrag.currentUrl = entry.getValue(); // URLDecoder.decode(entry.getValue().replaceAll("\\+", "-000000000000000-"), "utf-8");
							Log.d("i, currentUrl, tempFrag", i + ", " + curFrag.currentUrl + ", " + curFrag);
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
//						ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
//								SearchFragment.PRIVATE_PATH + "/Tab " + (i + 1) + ".webViewBundle"));
//						try	{
//							curFrag.webViewBundle = (Bundle) ois.readObject();
//							curFrag.webView.restoreState(curFrag.webViewBundle);
//						} catch (ClassNotFoundException e) {
//							e.printStackTrace();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
					} // end for
					if ("Search".equals(curFrag.load)) {
						curFrag.getSourceFileTask = curFrag.new GetSourceFileTask();
						curFrag.getSourceFileTask.execute();
					}
					if (i < arrList.size()/attribNo - 1) {
						getFragmentManager().beginTransaction().detach(curFrag).commit();
						Log.d("detach(curFrag)", (i + 1) + "");
					}
					showToast("Loading...");
				} // end for
				checkRE = Boolean.parseBoolean(prop.get("checkRE") + "");
				Log.d("checkRE", checkRE + "");
				autoBackup = Boolean.parseBoolean(prop.get("autoBackup") + "");
				Log.d("autoBackup", autoBackup + "");
				popup = Boolean.parseBoolean(prop.get("popup") + "");
				Log.d("popup", popup + "");
				autoTranslate = Boolean.parseBoolean(prop.get("autoTranslate") + "");
				Log.d("autoTranslate", autoTranslate + "");
				charsPreview = Integer.parseInt(prop.get("charsPreview") + "");
				Log.d("charsPreview", charsPreview + "");
				int tLevel = Integer.parseInt(prop.get("tLevel") + "");
				Log.d("tLevel", tLevel + "");
				if (!(tLevel == actionBar.getTabCount() - 1)) {
					getFragmentManager().beginTransaction().detach(curFrag).commit();
					actionBar.getTabAt(tLevel).select();
				}
				showToast("Long click screen to switch on/off full screen mode.\nLong press back button to exit.");
				//myG = new GestureDetector(this, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
        } else {
			actionBar.setSelectedNavigationItem(savedInstanceState.getInt("level", 0));
			Log.d("actionBar.setSelectedNavigationItem", actionBar.getSelectedNavigationIndex() + "");
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
		Log.d("protected onSaveInstanceState.getSelectedNavigationIndex", actionBar.getSelectedNavigationIndex() + "");
		saveAppStatus();
    }
	
	@Override
    protected void onPause() {
        super.onPause();
		//if (isFinishing()) {
			saveAppStatus();
		//}
    }

	private void saveAppStatus() {
		try {
			Properties prop = new Properties();
			
			for (int i = 0; i < actionBar.getTabCount(); i++) {
				String barText = actionBar.getTabAt(i).getText().toString();
				SearchFragment tempFrag = (SearchFragment) getFragmentManager().findFragmentByTag(barText);
				Log.d("onSaveInstanceState.actionBar.getTabAt(i)", actionBar.getTabAt(i).getText() + "");
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
					
					Log.d("onSaveInstanceState.prop.put(i)", barText + ", " + tempFrag.currentUrl);
				}
			}
			prop.put("popup", popup + "");
			prop.put("autoTranslate", autoTranslate + "");
			prop.put("checkRE", checkRE + "");
			prop.put("autoBackup", autoBackup + "");
			prop.put("charsPreview", charsPreview + "");
			prop.put("tLevel", actionBar.getSelectedNavigationIndex() + "");
			
			prop.store(new FileOutputStream(propFileName), "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.d("protected onRestoreInstanceState", savedInstanceState + "iii");
		
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
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
	
    SearchFragment addFragmentToStack() {
		ActionBar.Tab newTab = actionBar.newTab();
		TabListener tabListener = new TabListener(this, "Tab " + ++mStackLevel);
		newTab.setText("Tab " + mStackLevel).setTabListener(tabListener);
		actionBar.addTab(newTab);
		if (actionBar.getTabCount() > 1) {
			SearchFragment tempFrag = curFrag;
			newTab.select();
			SearchFragment tabFrag = tabListener.mFragment;
			//tabFrag.checkRE = tempFrag.checkRE;
			//tabFrag.popup = tempFrag.popup;
			//tabFrag.fullScreen = tempFrag.fullScreen;
			//tabFrag.charsPreview = tempFrag.charsPreview;
			tabFrag.load = tempFrag.load;
			tabFrag.currentSearching = tempFrag.currentSearching;
			tabFrag.selectedFiles = tempFrag.selectedFiles;
			tabFrag.files = tempFrag.files;
		}

		Log.d("addFragmentToStack", mStackLevel+"");
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
						Log.d("android_asset", e.getMessage(), e);
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
//		Log.d("MotionEvent", event + "");
//		
//		if ((event.getAction() ==  MotionEvent.ACTION_DOWN || event.getAction() ==  MotionEvent.ACTION_POINTER_UP) && curFrag.statusView != null) {
//			int curVis = curFrag.statusView.getSystemUiVisibility();
//			SHOW = !SHOW;
//			Log.d("SHOW", "cur = " + curVis + ", need show = " + SHOW + "");
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
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.search, menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search, menu);
		this.menu = menu;

		menu.add(0, Menu.FIRST, 307, "Find in page");

		MenuItem searchItem = menu.findItem(R.id.action_search);
		// searchItemId = searchItem.getItemId();
		// Log.d("searchItemId", searchItemId + "");
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
		
		// Log.d("searchText", mSearchView.getQuery().toString());

		return true;
	}
	int searchItemId;
	
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
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
	
	public boolean about(MenuItem item) {
		FileUtil.copyAssetToDir(this, SearchFragment.PRIVATE_PATH + "/lic.html", "data/lic.html");

		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.parameters, null);
		final EditText preview = (EditText) textEntryView.findViewById(R.id.preview);
		final TextView previewLabel = (TextView) textEntryView.findViewById(R.id.previewLabel);
		previewLabel.setVisibility(View.GONE);
		preview.setVisibility(View.GONE);
		
		final WebView wv = (WebView) textEntryView.findViewById(R.id.webView);
		wv.loadUrl(new File(SearchFragment.PRIVATE_PATH + "/lic.html").toURI().toString());
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
			Log.d("remove", mStackLevel+"");
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
	
	public boolean compare(MenuItem item) {
		return curFrag.compare(item);
	}
	
	public boolean closeOthers(MenuItem item) {
		if (actionBar.getTabCount() > 1) {
			Log.d("closeOthers", mStackLevel + "");
			FragmentTransaction beginTransaction;
			int selectedIndex = actionBar.getSelectedNavigationIndex();
			Log.d("closeOthers.selectedIndex", selectedIndex + "");
			for (int i = actionBar.getTabCount() - 1; i >= 0; i--) {
				
				Log.d("closeOthers.i", i +"");
				if (i != selectedIndex) {
					String barText = actionBar.getTabAt(i).getText().toString();
					Fragment fragment = getFragmentManager().findFragmentByTag(barText);
					Log.d("closeOthers.actionBar.getTabAt(i).getText()", actionBar.getTabAt(i).getText() + "");
					if (fragment != null && fragment != curFrag) {
						beginTransaction = fragment.getFragmentManager().beginTransaction();
						beginTransaction.remove(fragment);
						beginTransaction.commit();
						actionBar.removeTabAt(i);
						Log.d("closeOthers.actionBar.removeTabAt(i)", barText + "");
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
		Log.d("popup", "" + popup);
		return true;
	}

	public boolean autoTranslate(MenuItem item) {
		item.setChecked(!item.isChecked());
		autoTranslate = item.isChecked();
		Log.d("autoTranslate", "" + autoTranslate);
		return true;
	}
	
	public boolean autoBackup(MenuItem item) {
		item.setChecked(!item.isChecked());
		autoBackup = item.isChecked();
		Log.d("autoBackup", "" + autoBackup);
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
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		curFrag.onActivityResult(requestCode, resultCode, data);
	}
}

class TabListener implements ActionBar.TabListener {
	final SearchStack mActivity;
	final String mTag;
	final Bundle mArgs;
	SearchFragment mFragment;
//	int clickCount = 0;

	public TabListener(SearchStack activity, String tag) {
		this(activity, tag, new Bundle());
	}

	public TabListener(SearchStack activity, String tag, Bundle args) {
		mActivity = activity;
		mTag = tag;
		mArgs = args;

		// Check to see if we already have a fragment for this tab, probably
		// from a previously saved state.  If so, deactivate it, because our
		// initial state is that a tab isn't shown.
		mFragment = (SearchFragment) mActivity.getFragmentManager().findFragmentByTag(mTag);
		Log.d("TabListener.mFragment", mFragment+"");
		if (mFragment != null && !mFragment.isDetached()) {
			FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
			ft.detach(mFragment);
			ft.commit();
		}
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		Log.d("public void onTabSelected()",mTag+"");
		if (mFragment == null) {
			mFragment = (SearchFragment)Fragment.instantiate(mActivity, SearchFragment.class.getName(), mArgs);
			ft.add(android.R.id.content, mFragment, mTag);
			Log.d("mFragment == null, added", mFragment.getTag() + "");
		} else {
			Log.d("mFragment != null, attach", mFragment.getTag() + "");
			ft.attach(mFragment);
		}
		Log.d("onTabSelected(), currentUrl", mFragment.currentUrl + "");
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

			Log.d("onTabUnselected(), detach", mTag + "");
			Log.d("onTabUnselected.locX, locY", mFragment.locX + ", " + mFragment.locY);
			Log.d("onTabUnselected(), currentUrl", mFragment.currentUrl + "");
			Log.d("onTabUnselected(), status", mFragment.status + "zzz");
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
