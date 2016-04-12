package com.free.searcher;

import java.util.*;
import android.os.*;
import android.util.*;

class ZipExtractionTask extends AsyncTask<Void, String, String> {
	Collection<String> urls;
	
	private SearchFragment s;

	ZipExtractionTask(SearchFragment s, final Collection<String> urls) {
		this.s = s;
		this.urls = urls;
	}

	@Override
	protected String doInBackground(Void[] p1) {
		try {
			Log.d("extract size", urls.size() + "");
			s.extractFile.extractEntries(urls, false);
		} catch (Exception e) {
			Log.d("zip result", e.getMessage(), e);
			publishProgress(e.getMessage());
		} 
		return null;
	}

	protected void onProgressUpdate(String... progress) {
		if (progress != null && progress.length > 0 
			&& progress[0] != null && progress[0].trim().length() > 0) {
			s.statusView.setText(progress[0]);
		}
	}

	@Override
	protected void onPostExecute(String result) {
		if (s.currentUrl.length() > 0) {
			if (s.webView.getUrl().equals(s.currentUrl)) {
				s.locX = s.webView.getScrollX();
				s.locY = s.webView.getScrollY();
			}
			s.webView.loadUrl(s.currentUrl);
		}
		Log.d("x, y, cur, ori", s.locX + ", " + s.locY 
			  + ", s.currentUrl= " + s.currentUrl 
			  + ", origin = " + s.webView.getOriginalUrl() 
			  + ", url = " + s.webView.getUrl());
	}
}
