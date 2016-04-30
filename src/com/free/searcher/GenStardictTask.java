package com.free.searcher;

import android.os.*;
import android.util.*;

class GenStardictTask extends AsyncTask<Void, String, String> {
	private String fName;
	private MainFragment s;

	GenStardictTask(MainFragment s, String f) {
		this.s = s;
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
			s.statusView.setText(progress[0]);
		}
	}

	@Override
	protected void onPostExecute(String result) {
		s.showToast(result);
		s.statusView.setText(result);
	}
}
