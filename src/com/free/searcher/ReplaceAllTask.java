package com.free.searcher;
import android.os.*;
import java.io.*;
import android.util.*;
import android.app.*;
import java.util.*;
import android.widget.*;

public class ReplaceAllTask extends AsyncTask<String, Void, String> {

	private Activity activity = null;
	private Collection<String> filePaths;
	private boolean isRegex = false;
	private boolean caseSensitive = false;
	private String[] froms;
	private String[] tos;
	private String saveTo;
	private static final String TAG = "ReplaceAllTask";
	
	public ReplaceAllTask(Activity s, Collection<File> files, String saveTo, boolean isRegex, boolean caseSensitive, String[] froms, String[] tos) {

		filePaths = new ArrayList<String>(files.size());
		for (File f : files) {
			filePaths.add(f.getAbsolutePath());
		}
		this.caseSensitive = caseSensitive;
		this.isRegex = isRegex;
		this.saveTo = saveTo;
		this.froms = froms;
		this.tos = tos;
		this.activity = s;
	}
	
	protected String doInBackground(String... urls) {
		try {
			for (String st : filePaths) {
				if (!this.isCancelled()) {
					replaceAll(st);
				}
			}
			return "Replace All is finished";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Replace All is unsuccessful";
	}

	private void replaceAll(String fileP) throws IOException {
		String fileContent = FileUtil.readFileWithCheckEncode(fileP);
		for (int i = 0; i < tos.length; i++) {
			
			Log.d(TAG, froms[i] + ", " + tos[i] + isRegex + ", " + caseSensitive);
			fileContent = Util.replaceRegexAll(fileContent, froms[i], tos[i], isRegex, caseSensitive);
		}
		if (saveTo == null || saveTo.trim().length() == 0) {
			new File(fileP).renameTo(new File(fileP + "_" + MainFragment.df.format(System.currentTimeMillis()).replaceAll("[:/]", "_") + ".old"));
			FileUtil.writeContentToFile(fileP, fileContent);
		} else {
			FileUtil.writeContentToFile(saveTo + fileP, fileContent);
		}
	}

	@Override
	protected void onPostExecute(String result) {
		Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
		Log.d("ReplaceAllTask", result);
	}


}

