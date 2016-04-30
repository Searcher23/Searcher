package com.free.searcher;

import android.os.*;
import android.util.*;
import java.net.*;
import java.io.*;

class RestoreStardictTask extends AsyncTask<Void, String, String> {
	private String fName;
	private MainFragment s;

	RestoreStardictTask(MainFragment s, String f) {
		this.s = s;
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
			s.statusView.setText(progress[0]);
		}
	}

	@Override
	protected void onPostExecute(String result) {
		if (result != null) {
			s.showToast("Restore Stardict files successfully");
			try {
				s.webView.loadUrl(new File(result).toURL().toString());
			}
			catch (MalformedURLException e) {}
			s.statusView.setText(result);
		} else {
			s.showToast("Restore Stardict files unsuccessfully");
		}
	}
}
