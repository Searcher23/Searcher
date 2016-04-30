package com.free.searcher;
import android.os.*;
import android.util.*;
import com.free.p7zip.*;
import java.io.*;

class ZipReadingTask extends AsyncTask<Void, String, String> {
	private MainFragment s;

	public ZipReadingTask(MainFragment s) {
		this.s = s;
	}
	@Override
	protected String doInBackground(Void[] p1) {
		try {
			if (s.extractFile != null) {
				Log.d("ZipReadingTask", "arc.close()");
				s.extractFile.close();
			}
			s.extractFile = new ExtractFile(s.currentZipFileName, MainFragment.PRIVATE_PATH + s.currentZipFileName);
			//arc.saveEntries(entryFileList);
			String zipToUrlStr = s.extractFile.compressedFile2UrlStr("", true, "<br/>\r\n");
			// Log.d("zipList", zipToUrlStr);
			String tempFName = MainFragment.PRIVATE_PATH + s.currentZipFileName + "/sourcelisting" /*+ dateInstance.format(System.currentTimeMillis())*/ + ".0000.html";
			//tempFList.add(tempFName);
			FileUtil.writeContentToFile(tempFName, MainFragment.EMPTY_HEAD + zipToUrlStr + MainFragment.END_BODY_HTML);
			s.currentUrl = new File(tempFName).toURI().toURL().toString();
			s.home = s.currentUrl;
			publishProgress(s.currentZipFileName);
			(s.webTask = new WebTask(s, s.webView, s.currentUrl)).execute();
		} catch (Exception e) {
			Log.e("ZipReadingTask", e.getMessage(), e);
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
}
