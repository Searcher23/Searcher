package com.free.searcher;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.os.*;
import android.text.TextUtils.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import com.free.translation.*;
import com.free.searcher.R;

public class FolderChooserActivity extends Activity {

	public static final String SELECTED_DIR = FolderChooserActivity.class.getPackage().getName() + ".selectedDir";
	static final String MODE = "multiFiles";
	// all selected file
	private ArrayList<String> selectedFiles = new ArrayList<String>();
	private ArrayList<String> curDirSelectedFiles = new ArrayList<String>();
	private ArrayList<String> curDirUnSelectedFiles = new ArrayList<String>();
	private ArrayList<String> srcFiles = new ArrayList<String>();;
	String[] previousSelectedStr = new String[0];
	private TextView dir = null;
	private ListView listView1 = null;
	private ListView listView2 = null;
	String suffix = "";
	boolean multiFiles = false;
	ArrayAdapter srcAdapter;
	ArrayAdapter destAdapter;
//	Typeface tf;
	FileObserver mFileObserver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("FolderChooserActivity onCreate", savedInstanceState + ".");
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		
//		Log.d("getApplication", getApplication().toString());
//		Log.d("getApplicationInfo", getApplicationInfo().toString());
//		Log.d("getApplicationContext", getApplicationContext().toString());
//		Log.d("getWindow", getWindow().toString());
//		Log.d("getWindowManager", getWindowManager().toString());
		Point op = new Point();
		getWindowManager().getDefaultDisplay().getSize(op);
//		Log.d("getDefaultDisplay", getWindowManager().getDefaultDisplay() + ".");
//		Log.d("op", op + ".");
//		Log.d("getComponentName", getComponentName().toString());
//		Log.d("getRequestedOrientation()", getRequestedOrientation() + ".");
		
		if (op.x < op.y) {
			setContentView(R.layout.activity_folder_chooser_vertical);
		} else {
			setContentView(R.layout.activity_folder_chooser);
		}

		dir = (TextView) findViewById(R.id.dir);

		listView1 = (ListView) findViewById(R.id.files);
		srcAdapter = new ArrayAdapter(this, R.layout.simple_list_item_activated_1, srcFiles);
		listView1.setAdapter(srcAdapter);
		listView1.setFastScrollEnabled(true);
		listView1.setScrollBarStyle(0);
		
		listView2 = (ListView) findViewById(R.id.selectedFiles);
		listView2.setFastScrollEnabled(true);
		listView2.setScrollBarStyle(0);
		
//		tf = Typeface.createFromAsset(getAssets(), "fonts/DejaVuSerifCondensed.ttf");

		setTitle(getIntent().getStringExtra(MainFragment.CHOOSER_TITLE));
		suffix = getIntent().getStringExtra(MainFragment.SUFFIX);
		Log.d("suffix", suffix);
		multiFiles = getIntent().getBooleanExtra(MainFragment.MODE, true);
		Log.d("multiFiles", multiFiles + "");
		if (multiFiles) {
			showToast("Long press to select folder.\nClick to select/unselect files or folders");
			destAdapter = new ArrayAdapter(this, R.layout.simple_list_item_activated_1, selectedFiles);
			listView2.setAdapter(destAdapter);
			destAdapter.setup("", curDirUnSelectedFiles, null, multiFiles);
		} else {
			listView2.setVisibility(View.GONE);
			findViewById(R.id.horizontalDivider1).setVisibility(View.GONE);
			findViewById(R.id.horizontalDivider2).setVisibility(View.GONE);
			findViewById(R.id.horizontalDivider3).setVisibility(View.GONE);
			findViewById(R.id.horizontalDivider4).setVisibility(View.GONE);
			findViewById(R.id.horizontalDivider5).setVisibility(View.GONE);
			
			findViewById(R.id.add).setVisibility(View.GONE);
			findViewById(R.id.addAll).setVisibility(View.GONE);
			findViewById(R.id.remove).setVisibility(View.GONE);
			findViewById(R.id.removeAll).setVisibility(View.GONE);
		}

		previousSelectedStr = getIntent().getStringArrayExtra(MainFragment.SELECTED_DIR);
		Log.d("Folder onCreate", Util.arrayToString(previousSelectedStr, true, "\r\n"));
		if (previousSelectedStr != null) {
			Arrays.sort(previousSelectedStr);
			if (previousSelectedStr.length > 0) {
				File file = new File(previousSelectedStr[0]);
				if (file.exists()) {
					if (file.isDirectory() && file.getParentFile() == null) {
						dir.setText(previousSelectedStr[0]);
						changeDir(file);
					} else {
						dir.setText(file.getParent());
						changeDir(file.getParentFile());
					}
				}
			}
		}
		if (dir.getText() == null || dir.getText().toString().trim().length() == 0) {
			File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			dir.setText(f.getAbsolutePath());
			changeDir(f);
		}
		srcAdapter.setup(dir.getText().toString(), curDirSelectedFiles, selectedFiles, multiFiles);
	}

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.folder_chooser, menu);
		return true;
	}
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
		Log.d("FolderChooserActivity onSaveInstanceState", outState + ".");
		outState.putString("dirText", dir.getText().toString());
		outState.putStringArrayList("srcFiles", srcFiles);
		outState.putStringArrayList("selectedFiles", selectedFiles);
		outState.putStringArrayList("curDirSelectedFiles", curDirSelectedFiles);
		if (multiFiles) {
			Log.d("curDirUnSelectedFiles", curDirUnSelectedFiles.toString());
			outState.putStringArrayList("curDirUnSelectedFiles", curDirUnSelectedFiles);
		}
    }
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.d("FolderChooserActivity onRestoreInstanceState", savedInstanceState + ".");
		String dirText = savedInstanceState.getString("dirText");
		dir.setText(dirText);
		srcFiles.addAll(savedInstanceState.getStringArrayList("srcFiles"));
		selectedFiles.addAll(savedInstanceState.getStringArrayList("selectedFiles"));
		changeDir(new File(dirText));
		curDirSelectedFiles.addAll(savedInstanceState.getStringArrayList("curDirSelectedFiles"));
		if (multiFiles) {
			curDirUnSelectedFiles.addAll(savedInstanceState.getStringArrayList("curDirUnSelectedFiles"));
			Log.d("curDirUnSelectedFiles", curDirUnSelectedFiles.toString());
			destAdapter.notifyDataSetChanged();
		}
	}

	private void showToast(String st) {
		Toast.makeText(this, st, Toast.LENGTH_LONG).show();
	}

    /**
     * Sets up a FileObserver to watch the current directory.
     */
    private FileObserver createFileObserver(String path) {
        return new FileObserver(path, FileObserver.CREATE | FileObserver.DELETE
								| FileObserver.MOVED_FROM | FileObserver.MOVED_TO) {

            @Override
            public void onEvent(int event, String path) {
                debug("FileObserver received event %d", event);
                final Activity activity = FolderChooserActivity.this;
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								refreshDirectory();
							}
						});
                }
            }
        };
    }
	private void debug(String message, Object... args) {
        Log.d("FolderChooserActivity", String.format(message, args));
    }
	
    /**
     * Refresh the contents of the directory that is currently shown.
     */
    private void refreshDirectory() {
        changeDir(new File(dir.getText().toString()));
    }
	
	@Override
    public void onPause() {
        super.onPause();
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFileObserver != null) {
            mFileObserver.startWatching();
        }
    }
    /**
     * Lưu danh sách file và thư mục trong curDir vào list, allFiles để hiển thị trong TextView List
     * @param curDir
     */
	private void changeDir(File curDir) {
		dir.setText(curDir.getAbsolutePath());
//		File[] files = curDir.listFiles();
		List<File> files = FileUtil.fileFolderListing(curDir);
//		Log.d("filesListing", Util.listToString(files, true, "\r\n"));

		if (files != null) {	// always dir, already checked
			srcFiles.clear();
			// tìm danh sách các file có ext thích hợp
//			Log.d("SearchActivity.SUFFIX_FILES_CONVERTING", SearchActivity.SUFFIX_FILES_CONVERTING);
			String[] suffixes = suffix.toLowerCase().split("; *");
			Arrays.sort(suffixes);
			for (File f : files) {
				String fName = f.getName();
				//Log.d("fName", fName);
				if (f.isDirectory()) {
					srcFiles.add(fName);
				} else {
					if (suffix.length() > 0) {
						if (".*".equals(suffix)) {
							srcFiles.add(fName);
						} else {
							int lastIndexOf = fName.lastIndexOf(".");
							if (lastIndexOf >= 0) {
								String ext = fName.substring(lastIndexOf);
								boolean chosen =Arrays.binarySearch(suffixes, ext.toLowerCase()) >= 0;
								if (chosen) {
									srcFiles.add(fName);
								}
							}
						}
					}
				}
			}
			// điền danh sách vào allFiles
			Log.d("srcFiles.size()", "" + srcFiles.size());
			
		} else {
			srcFiles.clear();
		}
		curDirSelectedFiles.clear();
		srcAdapter.dir = dir.getText().toString();
		srcAdapter.notifyDataSetChanged();
		listView1.setActivated(true);
		listView1.smoothScrollToPositionFromTop(0, 0, 0);
	}

	public void up(View view) {
		File curDir = new File(dir.getText().toString());
		Log.d("curDir", curDir.getAbsolutePath());
		File parentFile = curDir.getParentFile();
		if (parentFile != null) {
			Log.d("curDir.getParentFile()", parentFile.getAbsolutePath());
			changeDir(parentFile);
		}
	}

	public void removeFiles(View view) {
		if (curDirUnSelectedFiles.size() > 0) {
			if (multiFiles) {
				selectedFiles.removeAll(curDirUnSelectedFiles);
			} else {
				selectedFiles.clear();
			}
			curDirUnSelectedFiles.clear();
			destAdapter.notifyDataSetChanged();
			srcAdapter.notifyDataSetChanged();
		}
	}

	public void removeAllFiles(View view) {
		selectedFiles.clear();
		destAdapter.notifyDataSetChanged();
		srcAdapter.notifyDataSetChanged();
	}

	public void addFiles(View view) {
		if (curDirSelectedFiles.size() > 0) {
			if (multiFiles) {
				for (String st : curDirSelectedFiles) {
					if (!selectedFiles.contains(st)) {
						selectedFiles.add(st);
					}
				}
			} else {
				selectedFiles.clear();
				selectedFiles.addAll(curDirSelectedFiles);
			}
			curDirSelectedFiles.clear();
			srcAdapter.notifyDataSetChanged();
			destAdapter.notifyDataSetChanged();
		}
	}

	public void addAllFiles(View view) {
		if (multiFiles) {
			String dirSt = dir.getText().toString() + "/";
			for (String st : srcFiles) {
				String st2 = dirSt + st;
				if (!selectedFiles.contains(st2)) {
					selectedFiles.add(st2);
				}
			}
			curDirSelectedFiles.clear();
			srcAdapter.notifyDataSetChanged();
			destAdapter.notifyDataSetChanged();
		} else {
			String dirSt = dir.getText().toString() + "/";
			if (srcFiles.size() == 1 
				&& new File(dirSt, srcFiles.get(0)).isFile()) {
				selectedFiles.clear();
				selectedFiles.add(dirSt + srcFiles.get(0));
				curDirSelectedFiles.clear();
				srcAdapter.notifyDataSetChanged();
				destAdapter.notifyDataSetChanged();
			}
		}
	}

	public void ok(View view) {
		// if (currentSelectedList.size() == 0 && multiFiles) {
		// 	currentSelectedList.add(new File(dir.getText().toString()));
		// } else 
		if (selectedFiles.size() == 0 && curDirSelectedFiles.size() == 0 && !multiFiles && suffix.length() > 0) {
			Toast.makeText(this, "Please select a file", Toast.LENGTH_LONG).show();
			return;
		}
		Log.d("selected file", Util.collectionToString(selectedFiles, false, "\r\n"));
		String[] fileArr = null;
		if (multiFiles) {
			if (selectedFiles.size() > 0) {
				fileArr = new String[selectedFiles.size()];
				selectedFiles.toArray(fileArr);
				Arrays.sort(fileArr);
			} else if (curDirSelectedFiles.size() > 0) {
				fileArr = new String[curDirSelectedFiles.size()];
				curDirSelectedFiles.toArray(fileArr);
				Arrays.sort(fileArr);
			} else {
				fileArr = new String[] {dir.getText().toString()};
			}
		} else {
			if (curDirSelectedFiles.size() > 0) {
				fileArr = new String[] {curDirSelectedFiles.get(0)};
			} else {
				fileArr = new String[] {dir.getText().toString()};
			}
		}
		
		Intent intent = this.getIntent();
		intent.putExtra(SELECTED_DIR, fileArr);
		intent.putExtra(MODE, multiFiles);
	    setResult(RESULT_OK, intent);
	    this.finish();
	}

	public void cancel(View view) {
		Log.d("select previous file", Util.arrayToString(previousSelectedStr, true, "\r\n"));
		Intent intent = this.getIntent();
		Arrays.sort(previousSelectedStr);
		intent.putExtra(SELECTED_DIR, previousSelectedStr);
		intent.putExtra(MODE, multiFiles);
		setResult(RESULT_CANCELED, intent);
	    this.finish();
	}

	class ArrayAdapter extends android.widget.ArrayAdapter<String> implements OnLongClickListener, OnClickListener {

		private View selectedTV = null;
		private static final int LIGHT_BROWN = 0xFFFFE6D9;
		private static final int LIGHT_YELLOW = 0xFFFFFFF0;
		private static final int LIGHT_YELLOW2 = 0xFFFFF8D9;
		private static final int LIGHT_YELLOW3 = 0xFFF7C0C1;
		private List<String> dataset;
		private String dir;
		private List<String> curSelectedFiles;
		private List<String> oppoSelect;
		private boolean multiFiles;

		public ArrayAdapter(Context context, int textViewResourceId,
							List<String> objects) {
			super(context, textViewResourceId, objects);
			dataset = objects;
		}

		public void setup(String dir, List<String> curSelect, List<String> oppoSelect, boolean multi) {
			this.dir = dir;
			this.curSelectedFiles = curSelect;
			this.oppoSelect = oppoSelect;
			this.multiFiles = multi;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			TextView text;

	        if (convertView != null) {
	        	text = (TextView) convertView;
	        } else {
	        	text = new TextView(getContext());
//	        	text.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT,
//	        			GridView.LayoutParams.WRAP_CONTENT));
	        	text.setMinHeight(36);
	        	text.setEnabled(true);
	        	text.setClickable(true);
	        	text.setLongClickable(true);
	        	text.setTextSize(14);
				text.setPadding(10, 11, 10, 11);
	        	text.setSingleLine();
	        	text.setHorizontallyScrolling(true);
				if (dir.length() > 0) {
					text.setEllipsize(TruncateAt.MIDDLE);
				} else {
					text.setEllipsize(TruncateAt.START);
				}
	        	text.setOnLongClickListener(this);
	        	text.setOnClickListener(this);
	        }

	        String fileName = getItem(position);
	        text.setText((CharSequence)fileName);
	        File f = new File(dir, fileName);
			
	        if (f.isDirectory()) {
	        	text.setTypeface(Typeface.DEFAULT);
				text.setTextColor(Color.BLUE);
	        } else {
	        	text.setTypeface(Typeface.DEFAULT);
				text.setTextColor(Color.BLACK);
	        }
			String fPath = f.getAbsolutePath();
			boolean inSelectedFiles = false;
			boolean isPartial = false;
			if (oppoSelect != null)
				for (String st : oppoSelect) {
					if (st.equals(fPath) || fPath.startsWith(st + "/")) {
						inSelectedFiles = true;
						break;
					} else if (st.startsWith(fPath + "/")) {
						isPartial = true;
					}
				}
			//Log.d("f.getAbsolutePath()", f.getAbsolutePath());
			//Log.d("curSelectedFiles", curSelectedFiles.toString());
			if (inSelectedFiles) {
				text.setBackgroundColor(LIGHT_YELLOW2);
			} else if (curSelectedFiles.contains(fPath)) {
	        	text.setBackgroundColor(LIGHT_BROWN);
			} else if (isPartial) {
				text.setBackgroundColor(LIGHT_YELLOW3);
	        } else {
	        	text.setBackgroundColor(LIGHT_YELLOW);
	        }
	        return text;
	    }
		
		public void onClick(View v) {
			String fileName = ((TextView) v).getText().toString();
			File f = new File(dir, fileName);
			String fPath = f.getAbsolutePath();
			Log.d("File onClick", fPath);
			Log.d("currentSelectedList", Util.collectionToString(curSelectedFiles, true, "\r\n"));
			Log.d("currentSelectedList.contains(f)", "" + curSelectedFiles.contains(f));
			Log.d("multiFiles", "" + multiFiles);
			Log.d("f.exists()", f.exists() + "");
			if (f.exists()) {
				boolean inSelectedFiles = false;
				if (oppoSelect != null)
					for (String st : oppoSelect) {
						if (fPath.equals(st) || fPath.startsWith(st + "/")) {
							inSelectedFiles = true;
							break;
						}
					}
				if (!inSelectedFiles) {
					if (multiFiles || suffix.length() == 0) {
						if (f.isDirectory() && curSelectedFiles.size() == 0 && dir.length() > 0) {
							changeDir(f);
						} else if (curSelectedFiles.contains(fPath)) {
							curSelectedFiles.remove(fPath);
							v.setBackgroundColor(LIGHT_YELLOW);
						} else {
							curSelectedFiles.add(fPath);
							v.setBackgroundColor(LIGHT_BROWN);
						}
					} else {
						if (f.isFile()) {
							// chọn mới đầu tiên
							if (curSelectedFiles.size() == 0) {
								curSelectedFiles.add(fPath);
								selectedTV = v;
								v.setBackgroundColor(LIGHT_BROWN);
							} else if (curSelectedFiles.size() > 0) {
								if (curSelectedFiles.contains(fPath)) { // đã chọn
									curSelectedFiles.clear();
									v.setBackgroundColor(LIGHT_YELLOW);
									selectedTV = null;
								} else { // chọn mới bỏ cũ
									curSelectedFiles.clear();
									curSelectedFiles.add(fPath);
									selectedTV.setBackgroundColor(LIGHT_YELLOW);
									v.setBackgroundColor(LIGHT_BROWN);
									selectedTV = v;
								}
							}
						} else { // is Directory
							curSelectedFiles.clear();
							if (selectedTV != null) {
								selectedTV.setBackgroundColor(LIGHT_YELLOW);
								selectedTV = null;
							}
							if (dir.length() > 0) {
								changeDir(f);
							}
						}
					}
					notifyDataSetChanged();
				}
			}
		}
	
		public boolean onLongClick(View v) {
			String fileName = ((TextView) v).getText().toString();
			File f = new File(dir, fileName);
			String fPath = f.getAbsolutePath();
			Log.d("File onLongClick", fPath);
			Log.d("currentSelectedList", Util.collectionToString(curSelectedFiles, true, "\r\n"));
			Log.d("currentSelectedList.contains(f)", "" + curSelectedFiles.contains(f));
			Log.d("multiFiles", multiFiles + "");

			boolean inSelectedFiles = false;
			if (oppoSelect != null)
				for (String st : oppoSelect) {
					if (fPath.equals(st) || fPath.startsWith(st + "/")) {
						inSelectedFiles = true;
						break;
					}
				}
			if (!inSelectedFiles) {
				if (multiFiles || suffix.length() == 0) {
					if (curSelectedFiles.contains(fPath)) {
						curSelectedFiles.remove(fPath);
						v.setBackgroundColor(LIGHT_YELLOW);
					} else {
						curSelectedFiles.add(fPath);
						v.setBackgroundColor(LIGHT_BROWN);
					}
				} else { // single file
					if (f.isFile()) {
						// chọn mới đầu tiên
						if (curSelectedFiles.size() == 0) {
							curSelectedFiles.add(fPath);
							selectedTV = v;
							v.setBackgroundColor(LIGHT_BROWN);
						} else if (curSelectedFiles.size() > 0) {
							if (curSelectedFiles.contains(fPath)) {
								// đã chọn
								selectedTV = null;
								v.setBackgroundColor(LIGHT_YELLOW);
								curSelectedFiles.clear();
							} else {
								// chọn mới bỏ cũ
								curSelectedFiles.clear();
								curSelectedFiles.add(fPath);
								selectedTV.setBackgroundColor(LIGHT_YELLOW);
								v.setBackgroundColor(LIGHT_BROWN);
								selectedTV = v;
							}
						}
					} else { // is Directory
						curSelectedFiles.clear();
						if (selectedTV != null) {
							selectedTV.setBackgroundColor(LIGHT_YELLOW);
							selectedTV = null;
						}
						if (dir.length() > 0) {
							changeDir(f);
						}
					}
				}
				notifyDataSetChanged();
			}
			return true;
		}
	}
}
