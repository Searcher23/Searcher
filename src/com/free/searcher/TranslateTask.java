package com.free.searcher;
import android.os.*;
import android.util.*;
import android.content.*;
import java.net.*;
import java.io.*;
import com.free.translation.*;

public class TranslateTask extends AsyncTask<Void, String, String> {
	String fName;
	SearchFragment searchFragment= null;
	
	TranslateTask(SearchFragment ctx, String f) {
		this.searchFragment = ctx;
		fName = f;
	}

	@Override
	protected String doInBackground(Void[] p1) {
		try {
			publishProgress("Translate files...");
			//String dic2Text = 
			new TranslationApp().translate(fName, searchFragment);
			return Constants.PRIVATE_PATH + fName+".translated.html";
		} catch (Throwable e) {
			publishProgress(e.getMessage());
			Log.e("Error Translate files", e.getMessage(),e);
			return null;
		}
	}

	protected void onProgressUpdate(String... progress) {
		if (progress != null && progress.length > 0 
			&& progress[0] != null && progress[0].trim().length() > 0) {
			searchFragment.statusView.setText(progress[0]);
		}
	}

	@Override
	protected void onPostExecute(String result) {
		if (result != null) {
			searchFragment.showToast("Translate files successfully");
			try {
				searchFragment.locX = searchFragment.webView.getScrollX();
				searchFragment.locY = searchFragment.webView.getScrollY();
				searchFragment.webView.loadUrl(new File(result).toURL().toString());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			searchFragment.statusView.setText(result);
		} else {
			searchFragment.showToast("Translate files unsuccessfully");
		}
	}
}
