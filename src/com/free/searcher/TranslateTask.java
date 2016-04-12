package com.free.searcher;
import android.os.*;
import android.util.*;
import android.content.*;
import java.net.*;
import java.io.*;
import com.free.translation.*;
import java.util.*;

public class TranslateTask extends AsyncTask<Void, String, String> {
	List<File> fList;
	SearchFragment searchFragment= null;
	TranslationSession session;
	
	TranslateTask(SearchFragment ctx, List<File> f) {
		this.searchFragment = ctx;
		fList = f;
		session = new TranslationSession();
	}

	@Override
	protected String doInBackground(Void[] p1) {
		try {
			publishProgress("Translate files...");
			
			for (File f : fList) {
				final File ff = f;
				if (!this.isCancelled()) {
					session.translate(f.getAbsolutePath(), searchFragment);
					searchFragment.statusView.postDelayed(new Runnable() {
							@Override
							public void run() {
								searchFragment.showToast("Translate files " + ff + " successfully");
								try {
									searchFragment.locX = searchFragment.webView.getScrollX();
									searchFragment.locY = searchFragment.webView.getScrollY();
									searchFragment.currentUrl = new File(Constants.PRIVATE_PATH + ff + ".translated.html").toURL().toString();
									searchFragment.home = searchFragment.currentUrl;
									searchFragment.webView.loadUrl(searchFragment.currentUrl);
								} catch (MalformedURLException e) {
									e.printStackTrace();
								}
							} 
					}, 0);
				}
			}
			return Constants.PRIVATE_PATH + fList.get(fList.size() - 1) +".translated.html";
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
