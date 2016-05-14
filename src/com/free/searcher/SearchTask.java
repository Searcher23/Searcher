package com.free.searcher;

import android.os.*;
import java.io.*;
import android.util.*;
import java.util.regex.*;
/**
 * String searchValue String publish current status String result String
 * status
 * 
 * @author Dell
 * 
 */
 class SearchTask extends AsyncTask<String, String, String> {
	private static final String TD1 = "<td width='3%' valign='top' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	private static final String TD2 = "<td width='97%' valign='top' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	
	private static final String SEARCH_TITLE = MainFragment.HTML_STYLE
	+ "<title>Search Result</title>\r\n" 
	+ MainFragment.HEAD_TABLE;
	private int matched = 0;
	private int charsPreview = 512;
	
	private MainFragment s;

	public SearchTask(MainFragment s) {
		this.s = s;
	}

	protected String doInBackground(String... search) {
		int count = s.files.length;
//			long totalSize = 0;
		if (search[0].trim().length() == 0) {
			return "Nothing to search. Please type something for searching";
		}

//			if (s.searchFileResult != null && s.searchFileResult.exists()) {
//				FileUtil.delete(s.searchFileResult);
//			}
//			if (s.searchFileResult == null || s.searchFileResult.length() == 0) {
		s.searchFileResult = MainFragment.PRIVATE_PATH +
			"/SearchResult_" 
			+ MainFragment.df.format(System.currentTimeMillis()).replaceAll("[/\\?<>\"':|]", "_") + ".html";
//			}
		File searchFile = new File(s.searchFileResult);
//			searchFile.delete();

		//tempFList.add(s.searchFileResult.getAbsolutePath());
		// Log.d("writeable", (s.searchFileResult + " can write: " +

		String resultStr = "";
		StringBuilder sb = new StringBuilder(4096);
		sb.append(SEARCH_TITLE);

		try {
			long timeMillis = System.currentTimeMillis();
			matched = 0;
			// chạy xong display thì cache cũng empty
			s.cache.reset();
			// Map.Entry<File, Map.Entry<String, String>> entry = null; //
			// for DoubleCache
			File f = null;
			int numFileSearched = 0;
			boolean checkRE2 = MainActivity.checkRE;
			charsPreview = MainActivity.charsPreview;
			String query = search[0].toLowerCase();

			while (s.cache.hasNext()) { // && isAlive
				f = s.cache.next();
				Log.d("Searching", f.getAbsolutePath());
				// s.currentContent = entry.getValue().getKey(); // for
				// DoubleCache
				// s.contentLower = entry.getValue().getValue(); // for
				// DoubleCache
				s.currentContent = s.cache.get(f);
//					s.contentLower = s.currentContent.toLowerCase();
//					totalSize += s.currentContent.length();
				publishProgress(new StringBuilder("Searching ")
								.append(f.getAbsolutePath()).append(": ")
								.append(++numFileSearched).append("/")
								.append(count).toString());


				if (isCancelled())
					break;
				matcher(sb, query, f, checkRE2);
				if (sb.length() > 65535) {
					FileUtil.writeAppendFileAsCharset(searchFile, sb.toString(), Util.UTF8);
					sb = new StringBuilder();
				}
			}
			resultStr = new StringBuilder(matched + "")
				.append(" matches, result size ").append(MainFragment.nf.format(searchFile.length() + sb.toString().getBytes().length + 180))
				.append(" bytes, cached ").append(MainFragment.nf.format(s.cache.cached())).append("/").append(count)
				.append(" files, cached size ").append(MainFragment.nf.format(s.cache.getCurrentSize())).append("/")
				.append(MainFragment.nf.format(s.cache.getTotalSize()))
				.append(" bytes, took ")
				.append(MainFragment.nf.format(System.currentTimeMillis() - timeMillis))
				.append(" milliseconds.").toString();
			sb.append("</table>\r\n<strong><br/>")
				.append(resultStr)
				.append("</p></strong>\r\n</div>\r\n</body>\r\n</html>");
			FileUtil.writeAppendFileAsCharset(searchFile, sb.toString(), Util.UTF8);
		} catch (Throwable e) {
			publishProgress("Result length " + sb.length() + ", error: " + e.getMessage() 
							+ ". Free memory is " + MainFragment.nf.format(Runtime.getRuntime().freeMemory())
							+ ". Max memory is " + MainFragment.nf.format(Runtime.getRuntime().maxMemory()));
			Log.d("SearchTask.doInBackground", e.getMessage(), e);
			return null;
		}
		return resultStr;
	}

	protected void onProgressUpdate(String... progress) {
		if (progress != null && progress.length > 0 
			&& progress[0] != null && progress[0].trim().length() > 0) {
			s.statusView.setText(progress[0]);
		}
	}

	protected void onPostExecute(String result) {
		try {
			if (result == null) {
				// s.showToast("Out of memory: " + Runtime.getRuntime().freeMemory());
				// s.statusView.setText("Out of memory: " + Runtime.getRuntime().freeMemory());
			} else {
				s.statusView.setText(result);
				if (!result.startsWith("Nothing")) {
					s.currentUrl = new File(s.searchFileResult).toURI().toURL().toString();
					s.locX = 0;
					s.locY = 0;
					s.webView.loadUrl(s.currentUrl);
					// home = s.currentUrl;
					Log.d("s.currentUrl 5", s.currentUrl);
				}
				Log.d("SearchTask", result);
				// setDefault(Notification.DEFAULT_ALL, "Searching " + s.currentSearching + " finished");
//					setMood(R.drawable.stat_happy, "Searching '" + s.currentSearching + "' finished",
//							true);
				s.showToast("Searching '" + s.currentSearching + "' finished");
			}
		} catch (Throwable e) {
			publishProgress(e.getMessage());
			Log.e("SearchTask.onPostExecute", e.getMessage(), e);
		}
	}

	private int matcher(StringBuilder sb, String pattern, File inFile, boolean checkRE) throws IOException {
		boolean found = false;
		int presentCursorPos = 0;
		int curFoundPos = -1;
		int nextFindPos = -1;
		int repeatedFindNextCurPos;

		if (!checkRE) {
			// TODO s.local tránh multithread
			// TODO cần replaceAll cả ở query thay vì ở đây thôi 
			// String caseStr = Util.replaceAll(s.currentContent, new  String[]{"&", "<"}, 
//												 new  String[]{"&amp;", "&lt;"});
//				StringBuilder lowerStr = new StringBuilder(caseStr.toLowerCase()); //Util.replaceAll(s.contentLower, "<",
			// "&lt;"); // s.contentLower; //
			s.contentLower = s.currentContent.toLowerCase();
			int contentLength = s.currentContent.length();
			int patternLength = pattern.length();

			while ((presentCursorPos + patternLength < contentLength) // isAlive &&
				   && ((curFoundPos = s.contentLower.indexOf(pattern, presentCursorPos)) > -1)) {
				if (!found) {
					sb.append(
						"<tr>\r\n<td width='100%' colspan='2' align='left' style='border:solid black 1.0pt; padding:1.4pt 1.4pt 1.4pt 1.4pt;'><strong>")
						.append(inFile.getAbsolutePath())
						.append(" [number of characters: ")
						.append(MainFragment.nf.format(contentLength))
						.append("]</strong></td>\r\n</tr>\r\n");
					found = true;
				}

				sb.append("<tr>\n").append(TD1).append("<a href=\"")
					.append(inFile.toURI().toURL().toString())
					// .append("?counter=").append(++counter)
					// .append("' target='_blank'>")
					.append("\">")
					.append(++matched)
					.append("</a>\n</td>\n").append(TD2);

				// sb.append("<a title='").append(inFile.getAbsolutePath())
				// .append("'>");
				Log.d("presentCursorPos, curFoundPos", presentCursorPos + ", " + curFoundPos);
				Util.replaceAll(
					s.currentContent,
					curFoundPos - ((curFoundPos > charsPreview) ? charsPreview : curFoundPos), 
					curFoundPos, sb, new String[]{"&", "<", "\n"}, 
					new String[]{"&amp;", "&lt;", "<br/>"});

				sb.append("<b><font color='blue'>");
				nextFindPos = curFoundPos + patternLength;
				Util.replaceAll(s.currentContent, curFoundPos, nextFindPos, sb,
								new String[]{"&", "<", "\n"}, 
								new String[]{"&amp;", "&lt;", "<br/>"});
				sb.append("</font></b>");
				presentCursorPos = nextFindPos;
				Log.d("presentCursorPos, curFoundPos, nextFindPos", presentCursorPos + ", " + curFoundPos + ", " + nextFindPos);

				while (presentCursorPos > (repeatedFindNextCurPos = s.contentLower.indexOf(pattern, presentCursorPos)) - charsPreview
					   && repeatedFindNextCurPos >= presentCursorPos) {
					presentCursorPos = repeatedFindNextCurPos + patternLength;
					Log.d("presentCursorPos, repeatedFindNextCurPos", presentCursorPos + ", " + repeatedFindNextCurPos);
					Util.replaceAll(s.currentContent, nextFindPos, repeatedFindNextCurPos, sb,
									new String[]{"&", "<", "\n"}, 
									new String[]{"&amp;", "&lt;", "<br/>"});
					sb.append("<b><font color='blue'>");
					Util.replaceAll(s.currentContent, repeatedFindNextCurPos, presentCursorPos, sb,
									new String[]{"&", "<", "\n"}, 
									new String[]{"&amp;", "&lt;", "<br/>"});
					sb.append("</font></b>");
					nextFindPos = presentCursorPos;
				}

				if (presentCursorPos > nextFindPos) {

				} else {
					Util.replaceAll(s.currentContent, nextFindPos, 
									Math.min(contentLength, presentCursorPos + charsPreview), sb,
									new String[]{"&", "<", "\n"}, 
									new String[]{"&amp;", "&lt;", "<br/>"});
					Log.d("nextFindPos, min(contentLength, presentCursorPos + charsPreview)2", nextFindPos + ", " + contentLength + ", " + (presentCursorPos + charsPreview));
				}
				sb.append("\n</td>\n</tr>\n");
			}
		} else {
			Pattern pat = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);//.replaceAll(Util.SPECIAL_CHAR_PATTERNSTR, "\\\\$1"), Pattern.UNICODE_CASE);
			Matcher mat = pat.matcher(s.currentContent);//.toLowerCase()); 
			int contentLength = s.currentContent.length();
			while (mat.find(presentCursorPos)) { // && isAlive
				if (!found) {
					sb.append(
						"<tr>\r\n<td width='100%' colspan='2' align='left' "
						+ "style='border:solid black 1.0pt; padding:1.4pt 1.4pt 1.4pt 1.4pt;'><strong>")
						.append(inFile.getAbsolutePath())
						.append(" [number of characters: ")
						.append(contentLength)
						.append("]</strong></td>\r\n</tr>\r\n");
					found = true;
				}

				curFoundPos = mat.start();

				sb.append("<tr>\r\n").append(TD1).append("<a href=\"")
					.append(inFile.toURI().toURL().toString())
					// .append("?counter=").append(++counter)
					.append("\">")
					.append(++matched)
					.append("</a>\r\n</td>\r\n").append(TD2);

				// sb.append("<a title='").append(inFile.getAbsolutePath())
				//		.append("'>");
				Util.replaceAll(
					s.currentContent,
					curFoundPos - ((curFoundPos > charsPreview) ? charsPreview : curFoundPos), 
					curFoundPos, sb, 
					new String[]{"&", "<", "\n"}, 
					new String[]{"&amp;", "&lt;", "<br/>"});

				sb.append("<b><font color='blue'>");
				nextFindPos = curFoundPos + (mat.end() - mat.start());
				Util.replaceAll(s.currentContent, curFoundPos, nextFindPos, sb,
								new String[]{"&", "<", "\n"}, 
								new String[]{"&amp;", "&lt;", "<br/>"});
				sb.append("</font></b>");

				presentCursorPos = nextFindPos;

				while (mat.find(presentCursorPos) && presentCursorPos > (repeatedFindNextCurPos = mat.start()) - charsPreview
					   && repeatedFindNextCurPos >= presentCursorPos) {
					presentCursorPos = repeatedFindNextCurPos + (mat.end() - mat.start());
					sb.append(Util.replaceAll(s.currentContent.substring(nextFindPos, 
																	   repeatedFindNextCurPos), 
											  new String[]{"&", "<", "\n"}, 
											  new String[]{"&amp;", "&lt;", "<br/>"}));
					sb.append("<b><font color='blue'>");
					Util.replaceAll(s.currentContent, repeatedFindNextCurPos, presentCursorPos, sb,
									new String[]{"&", "<", "\n"}, 
									new String[]{"&amp;", "&lt;", "<br/>"});
					sb.append("</font></b>");
					nextFindPos = presentCursorPos;
				}
				Util.replaceAll(s.currentContent, nextFindPos, 
								Math.min(contentLength, presentCursorPos + charsPreview), sb,
								new String[]{"&", "<", "\n"}, 
								new String[]{"&amp;", "&lt;", "<br/>"});
				sb.append("\n</td>\n</tr>\n");
			}
		}
		return matched;
	}
}
