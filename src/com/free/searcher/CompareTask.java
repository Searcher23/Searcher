package com.free.searcher;
import android.os.*;
import java.io.*;
import android.util.*;

class CompareTask extends AsyncTask<Void, String, String> {

	private static final String COMPARE_TITLE = MainFragment.HTML_STYLE
	+ "<title>Document Compare Result</title>\r\n" 
	+ MainFragment.HEAD_TABLE;
	private MainFragment s;

	public CompareTask(MainFragment s) {
		this.s = s;
	}
	
	@Override
	protected String doInBackground(Void[] p1) {
//					TextCompare tc = new TextCompare();
//					File file1 = new File(s.oriDoc);
//					File file2 = new File(s.modifiedDoc);
//					statusView.setText("Comparing \"" + file1 + "\" and \"" + file2 + "\"");
//					File f = tc.compareText(file1, file2);
//					s.currentUrl = f.toURL().toString();
//					result = "Compare \"" + file1 + "\" and \"" + file2
//						+ "\" finished";
//					(webTask = new WebTask(webView, s.currentUrl, true, result)).execute();

		try {
			File f1 = s.getSourceFileTask.convertedFileList.get(0);
			File f2 = s.getSourceFileTask.convertedFileList.get(1);
			publishProgress("Comparing \"" + s.oriDoc + "\" and \"" + s.modifiedDoc + "\"");
			String oriStr = FileUtil.readFileAsCharset(f1, "utf-8");
			String modiStr = FileUtil.readFileAsCharset(f2, "utf-8");
			String compareText = DiffMatchPatch.compare(oriStr, modiStr);
			File fret = new File(MainFragment.PRIVATE_PATH + s.oriDoc + "_" + f2.getName() + ".html");
			FileUtil.writeFileAsCharset(fret, compareText, "utf-8");

			StringBuilder sb = new  StringBuilder(COMPARE_TITLE);
			sb.append(
				"<tr>\n<td width='100%' colspan='3' align='center' style='border:solid black 1.0pt; padding:1.4pt 1.4pt 1.4pt 1.4pt;'><strong>")
				.append("Compare " + f1 + "<br/> and " + f2)
				//.append(" [number of characters: ")
				//.append(f1.length() + f2.length() + "")
				//.append("]")
				//.append("<br/>Number of differences: ").append(tc.wordDifferences).append(" words")
				.append("</strong></td>\n</tr>\n");

			sb.append("<tr>\n")
				.append(MainFragment.TD_COMPARE1)
				.append("<a href=\"" + f1.toURL() + "\">" + f1.getName() + " [" + MainFragment.nf.format(f1.length()) + " bytes]" + "</a>")
				.append("\n</td>\n")
				.append(MainFragment.TD_COMPARE1)
				.append("<a href=\"" + f2.toURL() + "\">" + f2.getName() + " [" + MainFragment.nf.format(f2.length()) +" bytes]" + "</a>")
				.append("\n</td>\n")
				.append(MainFragment.TD_COMPARE2)
				.append("<a href=\"" + fret.toURL() + "\">" + fret.getName() + " [" + MainFragment.nf.format(compareText.getBytes().length) +" bytes]" + "</a>")
				.append("\n</td>\n")
				.append("</tr>");

			sb.append("<tr>\n")
				.append(MainFragment.TD_COMPARE1)
				.append(oriStr.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll("\n", "<br/>"))
				.append("\n</td>\n")
				.append(MainFragment.TD_COMPARE1)
				.append(modiStr.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll("\n", "<br/>"))
				.append("\n</td>\n")
				.append(MainFragment.TD_COMPARE2)
				.append(compareText)
				.append("\n</td>\n")
				.append("</tr></table>\n<strong><br/>")
				.append("</strong></div>\n</body>\n</html>");
			String name = MainFragment.PRIVATE_PATH + s.oriDoc + "_" + f2.getName() + ".all.html";
			FileUtil.writeContentToFile(name, sb.toString());

			s.currentUrl = new File(name).toURL().toString();
			String result = "Compare \"" + s.oriDoc + "\" and \"" + s.modifiedDoc
				+ "\" finished";
			(s.webTask = new WebTask(s, s.webView, s.currentUrl, true, result)).execute();
		} catch (IOException e) {
			publishProgress(e.getMessage());
			Log.d("compare", e.getMessage(), e);
		}
		s.requestCompare = false;
		return null;
	}

	protected void onProgressUpdate(String... progress) {
		if (progress != null && progress.length > 0 
			&& progress[0] != null && progress[0].trim().length() > 0) {
			s.statusView.setText(progress[0]);
			Log.d("CompareTask", progress[0]);
		}
	}

	protected void onPostExecute(String result) {
		s.showToast("Document Compare finished");
	}
}
