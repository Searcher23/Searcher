package com.free.searcher;
import android.os.*;
import java.util.*;
import java.io.*;
import com.free.p7zip.*;
import android.util.*;
import java.net.*;

class DupFinderTask extends AsyncTask<Void, String, String> {

	String[] fileNames;
	List<List<FileInfo>> groupList;
//		int noFile;
//		int noDup;
//		long totalSize = 0;
//		long dupSize = 0;
//		NumberFormat nf = NumberFormat.getInstance();
	long start = 0;
	static final int GROUP_VIEW = 0;
	static final int NAME_VIEW = 1;
	int curView = 0;
	File fret;

	private static final String DUP_TITLE = 
	MainFragment.HTML_STYLE
	+ "<title>Duplicate Finding Result</title>\r\n" 
	+ MainFragment.HEAD_TABLE;

	private MainFragment s;

	public DupFinderTask(MainFragment s) {
		this.s = s;
	}
	public DupFinderTask(MainFragment s, String[] fs) {
		this.s = s;
		this.fileNames = fs;
	}

	public String deleteFile(final String selectedPath) throws IOException {
		boolean ret = FileUtils.deleteFile(selectedPath);// new File(selectedPath).delete();
		String statusDel = "deleteFile";
		if (ret) {
			statusDel = "Delete file \"" + selectedPath + "\" successfully";
		} else {
			statusDel = "Cannot delete file \"" + selectedPath + "\"";
		}
		Log.d("deleteFile", statusDel + MainFragment.df.format(System.currentTimeMillis()));
		s.showToast(statusDel);
		return genFile(groupList);
	}

	public String deleteGroup(int group, String selectedPath) throws IOException {
		List<FileInfo> l = null; // groupList.get(group - 1);
		for (List<FileInfo> ltemp : groupList) {
			if (ltemp.get(0).group == group) {
				l = ltemp;
				break;
			}
		}
		File selectedF = new File(selectedPath);
		for (int i = l.size()-1; i >= 0; i--) {
			FileInfo ff = l.get(i);
			if (selectedF.exists()) {
				if (!ff.path.equals(selectedPath) && ff.file.exists()) {
					boolean ret = FileUtils.deleteFile(ff.file); // ff.file.delete();
					String statusDel = "deleteGroup";
					if (ret) {
						statusDel = "Delete group " + group + ", \"" + ff.path + "\" successfully";
					} else {
						statusDel = "Cannot delete group " + group + ", \"" + ff.path + "\"";
					}
					Log.d("deleteGroup", statusDel + MainFragment.df.format(System.currentTimeMillis()));
					s.statusView.setText(statusDel);
				}
			} else if (ff.file.exists()) {
				selectedF = ff.file;
				selectedPath = ff.path;
			}
		}
		return genFile(groupList);
	}

	public String deleteFolder(String selectedPath) throws IOException {
		String parentPath = new File(selectedPath).getParentFile().getAbsolutePath();
		for (List<FileInfo> l : groupList) {
			for (FileInfo ff : l) {
				if (isDup(l)) {
					if (ff.file.exists() && ff.file.getParentFile().getAbsolutePath().equals(parentPath)) {
						boolean ret = FileUtils.deleteFile(ff.file); //ff.file.delete();
						String statusDel = "deleteFolder";
						if (ret) {
							statusDel = "Delete \"" + ff.path + "\" successfully. ";
						} else {
							statusDel = "Cannot delete \"" + ff.path + "\". ";
						}
						Log.d("deleteFolder", statusDel + MainFragment.df.format(System.currentTimeMillis()));
						s.statusView.setText(statusDel);
					} 
				}
			}
		}
		return genFile(groupList);
	}

	public String deleteSubFolder(String selectedPath) throws IOException {
		String parentPath = new File(selectedPath).getParentFile().getAbsolutePath();
		for (List<FileInfo> l : groupList) {
			for (FileInfo ff : l) {
				if (isDup(l)) {
					String path = ff.file.getParentFile().getAbsolutePath();
					if (ff.file.exists() && path.startsWith(parentPath) && path.length() > parentPath.length()) {
						boolean ret = FileUtils.deleteFile(ff.file); //ff.file.delete();
						String statusDel = "deleteSubFolder";
						if (ret) {
							statusDel = "Delete \"" + ff.path + "\" successfully";
						} else {
							statusDel = "Cannot delete \"" + ff.path + "\"";
						}
						Log.d("deleteSubFolder", statusDel + MainFragment.df.format(System.currentTimeMillis()));
						s.statusView.setText(statusDel);
					}
				} 
			}
		}
		return genFile(groupList);
	}

	private boolean isDup(List<FileInfo> l) {
		int counter = 0;
		for (FileInfo ff : l) {
			if (ff.file.exists() && ++counter > 1) {
				return true;
			}
		}
		return false;
	}

	List<List<FileInfo>> copyGroupList(List<List<FileInfo>> oriGroupList) {
		List<List<FileInfo>> newGroupList = new LinkedList<List<FileInfo>>();
		for (List<FileInfo> filesInGroup : oriGroupList) {
			List<FileInfo> dest = new LinkedList<FileInfo>();
			newGroupList.add(dest);
			for (FileInfo ff : filesInGroup) {
				dest.add(ff);
			}
		}
		return newGroupList;
	}

	@Override
	protected String doInBackground(Void[] p1) {
		try {
			return new File(duplicateFinder(fileNames)).toURI().toURL().toString();
		} catch (IOException e) {
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
		s.statusView.setText(statusViewResult.toString());
		s.currentUrl = result;
		s.home = s.currentUrl;
		s.showToast("Duplicate finder finished");
		s.webView.loadUrl(result);
	}

	public String duplicateFinder(String[] files) throws IOException {
		File[] fs = new File[files.length];
		int i = 0;
		for (String st : files) {
			fs[i++] = new File(st);
		}
		return duplicateFinder(fs);
	}

	public String duplicateFinder(File[] files) throws IOException {
		start =  System.currentTimeMillis();
		fret = new File(MainFragment.PRIVATE_PATH + "/" + "Duplicate finder result_"
						+ MainFragment.df.format(System.currentTimeMillis()).replaceAll("[/\\?<>\"':|]", "_") + ".html");
		publishProgress("Getting file list...");
		List<File> lf = FileUtil.getFiles(files);
		groupList = dupFinder(lf);
		return genFile(groupList);
	}

	String genFile(List<List<FileInfo>> lset) throws IOException {
		return genFile(groupList, curView);
	}

	String genFile(List<List<FileInfo>> lset, int view) throws IOException {
		StringBuilder sb;
		if (view == GROUP_VIEW) {
			sb = displayGroup(lset);
			curView =  GROUP_VIEW;
		} else {
			sb = displayName(lset);
			curView = NAME_VIEW;
		}
		FileUtil.writeContentToFile(fret.getAbsolutePath(), sb.toString());
		publishProgress(statusViewResult.toString());
		return fret.getAbsolutePath();
	}

	StringBuilder statusViewResult = new StringBuilder();

	private List<List<FileInfo>> dupFinder(final List<File> oriListFile) throws IOException {
		long curSize = - 1;
		List<FileInfo> filesInGroupList = null; //same size, many group, 1 size 1 set
		List<List<FileInfo>> groupList = new LinkedList<List<FileInfo>>();
		Collections.sort(oriListFile, new SortFileSizeDecrease());
//			totalSize = 0;
		for (File f : oriListFile) {
//				totalSize += f.length();
			if (f.length() < curSize) {
				filesInGroupList = new LinkedList<FileInfo>();
				curSize = f.length();
				filesInGroupList.add(new  FileInfo(f));
				groupList.add(filesInGroupList);
			} else {
				boolean same = false;
				for (List<FileInfo> curGroupList : groupList) {
					//if (curGroupList.get(0).length == f.length()) {
					same = compare(curGroupList.get(0).file, f);
					if (same) {
						curGroupList.add(new FileInfo(f));
						break;
					}
					//}
				}
				if (!same) {
					filesInGroupList = new LinkedList<FileInfo>();
					filesInGroupList.add(new FileInfo(f));
					groupList.add(filesInGroupList);
				}
			}
		}
//		Log.d("lset.size()", lset.size() + "");
		for (int i = groupList.size() - 1; i >= 0; i--) {
			if (groupList.get(i).size() == 1) {
				groupList.remove(i);
			}
		}
		int curGroup = 0;
		for (List<FileInfo> filesInGroup : groupList) {
			curGroup++;
			for (FileInfo ff : filesInGroup) {
				ff.group = curGroup;
			}
		}
		return groupList;
	}

	boolean compare(final File f1, final File f2) throws IOException {
		publishProgress("compare \"" + f1.getAbsolutePath() + "\" and \"" + f2.getAbsolutePath() + "\"");
		//Log.d("compare file ", f1.getName() + " and " + f2.getName());
		if (f1.length() != f2.length()) {
			return false;
		}
		FileInputStream fis1 = new FileInputStream(f1);
		FileInputStream fis2 = new FileInputStream(f2);
		BufferedInputStream bis1 = new BufferedInputStream(fis1);
		BufferedInputStream bis2 = new BufferedInputStream(fis2);
		long counter = 0;
		int n;
		while (((n = bis1.read()) != -1) && n == bis2.read()) {
			counter++;
		}
		bis1.close();
		bis2.close();
		fis1.close();
		fis2.close();
		return counter == f2.length();
	}

	private StringBuilder displayGroup(final List<List<FileInfo>> lset) throws MalformedURLException {
		StringBuilder sb = new StringBuilder(DUP_TITLE);
		int groupSize = 0;
		int counter = 0;
		int noFile = 0;
		int noDup = 0;
		long dupSize = 0;
		int curGroup = 0;
		long realSize = 0;
		long totalSize = 0;
		int one = 0;
		if (lset != null && lset.size() > 0) {
			//String findRet = new File(SearchFragment.SearchFragment.PRIVATE_DIR + "/" + "Duplicate finder result.html").toURI().toURL().toString();
			sb.append("<tr bgcolor=\"#FCCC74\">\r\n")
				.append(MainFragment.TD1_CENTER)
				.append("<b>No.</b></td>\n")
				.append(MainFragment.TD1_CENTER)
				.append("<a href=\"").append(fret)
				.append("?viewGroup\">")
				.append("<b>Group</b></a>\n</td>\n")
				.append(MainFragment.TD2_CENTER)
				.append("<a href=\"").append(fret)
				.append("?viewName\">")
				.append("<b>File Name</b></a>\n</td>\n")
				.append(MainFragment.TD2_CENTER)
				.append("<b>Size (bytes)</b></td>\n")
				.append(MainFragment.TD3_CENTER)
				.append("<b>Delete?</b></td>\n")
				.append(MainFragment.TD3_CENTER)
				.append("<b>Delete Group</b></td>\n")
				.append(MainFragment.TD3_CENTER)
				.append("<b>Delete Folder</b></td>\n")
				.append(MainFragment.TD3_CENTER)
				.append("<b>Delete Sub Folder</b></td>\n")
				.append("</tr>");
			List<List<FileInfo>> newGroupList = new LinkedList<>();
			List<List<FileInfo>> newLsetOrder = lset;
			if (s.groupViewChanged) {
				newLsetOrder = new LinkedList<List<FileInfo>>();
				for (int i = lset.size() - 1; i >= 0; i--) {
					newLsetOrder.add(lset.get(i));
				}
				s.groupViewChanged = false;
			}

			for (List<FileInfo> s : newLsetOrder) {
				curGroup++;
				one = 0;
				// kiem tra xem co con la group khong
				int dupInGroup = 0;
				for (FileInfo ff : s) {
					if (ff.file.exists() && ++dupInGroup > 1) {
						break;
					}
				}
				List<FileInfo> newGroupListEle = new LinkedList<>();
				newGroupList.add(newGroupListEle);
				for (FileInfo ff : s) {
					if (curGroup % 2 == 1) {
						sb.append("<tr>\r\n");
					} else {
						sb.append("<tr bgcolor=\"#ffee8d\">\r\n");
					}

					if (ff.file.exists()) {
						totalSize += ff.length;
						noFile++;
						newGroupListEle.add(ff);
						if (++one == 2) {
							groupSize++;
							realSize += ff.length;
							dupSize += ff.length;
							noDup++;
						} else if (one > 2) {
							dupSize += ff.length;
							noDup++;
						}
						sb.append(MainFragment.TD1_LEFT).append(++counter).append("</td>\n")
							.append(MainFragment.TD1_LEFT).append(ff.group).append("</td>\n")
							.append(MainFragment.TD2_LEFT).append("<a href=\"").append(ff.file.toURI().toURL().toString()).append("\">")
							.append(ff.path).append("</a>\n</td>\n")
							.append(MainFragment.TD3_LEFT).append(MainFragment.nf.format(ff.length)).append("</td>")
							.append(MainFragment.TD3_LEFT).append("<a href=\"").append(fret)
							.append("?deleteFile=").append(ff.path).append("\">Delete</a>\n</td>\n");
						if (dupInGroup > 1) {
							sb.append(MainFragment.TD3_LEFT).append("<a href=\"").append(fret)
								.append("?deleteGroup=").append(ff.group).append(",").append(ff.path).append("\">Delete group</a>\n</td>\n");
						} else {
							sb.append(MainFragment.TD3_LEFT).append("&nbsp;\n</td>\n");
						}
						sb.append(MainFragment.TD3_LEFT).append("<a href=\"").append(fret)
							.append("?deleteFolder=").append(ff.path).append("\">Delete Folder</a>\n</td>\n")
							.append(MainFragment.TD3_LEFT).append("<a href=\"").append(fret)
							.append("?deleteSub=").append(ff.path).append("\">Delete Sub Folder</a>\n</td>\n")
							.append("</tr>\n")
							;

					} else {
						sb.append(MainFragment.TD1_LEFT).append(++counter).append("</td>\n")
							.append(MainFragment.TD1_LEFT).append(ff.group).append("</td>\n")
							.append(MainFragment.TD2_LEFT)
							//.append("<a href=\"").append(ff.toURI().toURL().toString()).append("\">")
							.append("<font color='red'><strike>")
							.append(ff.path)
							.append("</strike></font>")
							.append("\n</td>\n")
							.append(MainFragment.TD3_LEFT).append(MainFragment.nf.format(ff.length)).append("</td>")
							.append(MainFragment.TD3_LEFT)
							//.append("<a href=\"").append(findRet).append("?delete=").append(ff.getAbsolutePath())
							//.append("\">Delete</a>\n")
							.append("&nbsp;</td>\n");
						if (dupInGroup > 1) {
							sb.append(MainFragment.TD3_LEFT).append("<a href=\"").append(fret)
								.append("?deleteGroup=").append(ff.group).append(",").append(ff.path).append("\">Delete group</a>\n</td>\n");
						} else {
							sb.append(MainFragment.TD3_LEFT).append("&nbsp;\n</td>\n");
						}
						sb.append(MainFragment.TD3_LEFT).append("<a href=\"").append(fret)
							.append("?deleteFolder=").append(ff.path).append("\">Delete Folder</a>\n</td>\n")
							.append(MainFragment.TD3_LEFT).append("<a href=\"").append(fret)
							.append("?deleteSub=").append(ff.path).append("\">Delete Sub Folder</a>\n</td>\n")
							.append("</tr>\n")
							;
					}
				}
			}
			sb.append("</table>\n");
			for (int i = newGroupList.size() - 1; i >= 0; i--) {
				if (newGroupList.get(i).size() < 2) {
					newGroupList.remove(i);
				}
			}
			groupList = newGroupList;
		}
		long duration = System.currentTimeMillis() - start;
		sb.append("<strong><br/>")
			.append("Total ").append(MainFragment.nf.format(noFile)).append(" files (")
			.append(MainFragment.nf.format(totalSize)).append(" bytes)<br/>")
			.append(MainFragment.nf.format(noDup)).append(" files (").append(MainFragment.nf.format(dupSize)).append(" bytes) duplicate<br/>")
			.append((realSize != 0) ? MainFragment.nf.format(dupSize * 100 / (double) realSize) : "0").append("% duplicate<br/>")
			.append(MainFragment.nf.format(groupSize)).append(" group duplicate<br/>")
			.append("took ").append(MainFragment.nf.format(duration)).append(" milliseconds<br/>")
			.append("</strong></div>\n</body>\n</html>");
		statusViewResult = new StringBuilder();

		statusViewResult.append("Total ").append(MainFragment.nf.format(noFile)).append(" files (")
			.append(MainFragment.nf.format(realSize)).append(" bytes), ")
			.append(MainFragment.nf.format(noDup)).append(" files (").append(MainFragment.nf.format(dupSize)).append(" bytes) duplicate, ")
			.append((realSize != 0) ? MainFragment.nf.format(dupSize * 100 / (double) realSize) : "0").append("% duplicate, ")
			.append(MainFragment.nf.format(groupSize)).append(" duplicate group, ")
			.append("took ").append(MainFragment.nf.format(duration)).append(" milliseconds");
		return sb;
	}

	void calList(List<List<FileInfo>> lset) {
		List<List<FileInfo>> newLset = new LinkedList<>();
		for (List<FileInfo> s : lset) {
			List<FileInfo> newLset2 = new LinkedList<>();
			newLset.add(newLset2);
			for (FileInfo ff : s) {
				if (ff.file.exists()) {
					newLset2.add(ff);
				}
			}
		}
		for (int i = newLset.size() - 1; i >= 0; i--) {
			if (newLset.get(i).size() < 2) {
				newLset.remove(i);
			}
		}
		groupList = newLset;
	}

	private StringBuilder displayName(List<List<FileInfo>> lset) throws MalformedURLException {
		StringBuilder sb = new StringBuilder(DUP_TITLE);
		List<FileInfo> infoList = new LinkedList<FileInfo>();
		int groupSize = 0;
		int noDup = 0;
		int dupSize = 0;
		int noFile = 0;
		int realSize = 0;
		int totalSize = 0;

		//String findRet = new File(SearchFragment.SearchFragment.PRIVATE_DIR + "/" + "Duplicate finder result.html").toURI().toURL().toString();
		if (lset != null && lset.size() > 0) {

			sb.append("<tr bgcolor=\"#FCCC74\">\r\n")
				.append(MainFragment.TD1_CENTER)
				.append("<b>No.</b></td>\n")
				.append(MainFragment.TD1_CENTER)
				.append("<a href=\"").append(fret)
				.append("?viewGroup\">")
				.append("<b>Group</b></a>\n</td>\n")
				.append(MainFragment.TD2_CENTER)
				.append("<a href=\"").append(fret)
				.append("?viewName\">")
				.append("<b>File Name</b></a>\n</td>\n")
				.append(MainFragment.TD3_CENTER)
				.append("<b>Size (bytes)</b></td>\n")
				.append(MainFragment.TD3_CENTER)
				.append("<b>Delete?</b></td>\n")
				.append(MainFragment.TD3_CENTER)
				.append("<b>Delete Group</b></td>\n")
				.append(MainFragment.TD3_CENTER)
				.append("<b>Delete Folder</b></td>\n")
				.append(MainFragment.TD3_CENTER)
				.append("<b>Delete Sub Folder</b></td>\n")
				.append("</tr>");

			int counter = 0;
			for (List<FileInfo> s : lset) {
				for (FileInfo ff : s) {
					infoList.add(ff);
				}
			}
			if (s.nameOrder) {
				Collections.sort(infoList);
			} else {
				Collections.sort(infoList, new SortFilePathDecrease());
			}

			Map<String, Integer> ss = new TreeMap<String, Integer>();
			for (FileInfo ff : infoList) {
				List<FileInfo> l = null; // groupList.get(group - 1);
				for (List<FileInfo> ltemp : groupList) {
					if (ltemp.get(0).group == ff.group) {
						l = ltemp;
						break;
					}
				}
				// kiem tra xem co con la group khong
				int dupInGroup = 0;
				for (FileInfo ff2 : l) {
					if (ff2.file.exists() && ++dupInGroup > 1) {
						break;
					}
				}
				sb.append("<tr>\r\n");
				if (ff.file.exists()) {
					noFile++;
					totalSize += ff.file.length();
					if (ss.get(ff.group + "") == null) {
						ss.put(ff.group + "", 1);
					} else if (ss.get(ff.group + "") == 1) {
						groupSize++;
						realSize += ff.length;
						ss.put(ff.group + "", 2);
						noDup++;
						dupSize += ff.length;
					} else if (ss.get(ff.group + "") >= 2) {
						noDup++;
						dupSize += ff.length;
					}
					sb.append(MainFragment.TD1_LEFT).append(++counter).append("</td>\n")
						.append(MainFragment.TD1_LEFT).append(ff.group).append("</td>\n")
						.append(MainFragment.TD2_LEFT).append("<a href=\"").append(ff.file.toURI().toURL().toString()).append("\">")
						.append(ff.path).append("</a>\n</td>\n")
						.append(MainFragment.TD3_LEFT).append(MainFragment.nf.format(ff.length)).append("</td>")
						.append(MainFragment.TD3_LEFT).append("<a href=\"").append(fret)
						.append("?deleteFile=").append(ff.path).append("\">Delete</a>\n</td>\n");
					if (dupInGroup > 1) {
						sb.append(MainFragment.TD3_LEFT).append("<a href=\"").append(fret)
							.append("?deleteGroup=").append(ff.group).append(",").append(ff.path).append("\">Delete group</a>\n</td>\n");
					} else {
						sb.append(MainFragment.TD3_LEFT).append("&nbsp;\n</td>\n");
					}
					sb.append(MainFragment.TD3_LEFT).append("<a href=\"").append(fret)
						.append("?deleteFolder=").append(ff.path).append("\">Delete Folder</a>\n</td>\n")
						.append(MainFragment.TD3_LEFT).append("<a href=\"").append(fret)
						.append("?deleteSub=").append(ff.path).append("\">Delete Sub Folder</a>\n</td>\n")
						.append("</tr>\n")
						;
				} else {
					sb.append(MainFragment.TD1_LEFT).append(++counter).append("</td>\n")
						.append(MainFragment.TD1_LEFT).append(ff.group).append("</td>\n")
						.append(MainFragment.TD2_LEFT)
						//.append("<a href=\"").append(ff.toURI().toURL().toString()).append("\">")
						.append("<font color='red'><strike>")
						.append(ff.path)
						.append("</strike></font>")
						.append("\n</td>\n")
						.append(MainFragment.TD3_LEFT).append(MainFragment.nf.format(ff.length)).append("</td>")
						.append(MainFragment.TD3_LEFT)
						//.append("<a href=\"").append(findRet).append("?delete=").append(ff.getAbsolutePath())
						//.append("\">Delete</a>\n")
						.append("&nbsp;</td>\n");
					if (dupInGroup > 1) {
						sb.append(MainFragment.TD3_LEFT).append("<a href=\"").append(fret)
							.append("?deleteGroup=").append(ff.group).append(",").append(ff.path).append("\">Delete group</a>\n</td>\n");
					} else {
						sb.append(MainFragment.TD3_LEFT).append("&nbsp;\n</td>\n");
					}
					sb.append(MainFragment.TD3_LEFT).append("<a href=\"").append(fret)
						.append("?deleteFolder=").append(ff.path).append("\">Delete Folder</a>\n</td>\n")
						.append(MainFragment.TD3_LEFT).append("<a href=\"").append(fret)
						.append("?deleteSub=").append(ff.path).append("\">Delete Sub Folder</a>\n</td>\n")
						.append("</tr>\n")
						;
				}
			}
			sb.append("</table>\n");
			calList(groupList);
		}
		long duration = System.currentTimeMillis() - start;
		sb.append("<strong><br/>")
			.append("Total ").append(MainFragment.nf.format(noFile)).append(" files (")
			.append(MainFragment.nf.format(totalSize)).append(" bytes)<br/>")
			.append(MainFragment.nf.format(noDup)).append(" files (").append(MainFragment.nf.format(dupSize)).append(" bytes) duplicate<br/>")
			.append((realSize != 0) ? MainFragment.nf.format(dupSize * 100 / (double) realSize) : "0").append("% duplicate<br/>")
			.append(MainFragment.nf.format(groupSize)).append(" group duplicate<br/>")
			.append("took ").append(MainFragment.nf.format(duration)).append(" milliseconds<br/>")
			.append("</strong></div>\n</body>\n</html>");
		statusViewResult = new StringBuilder();
		statusViewResult.append("Total ").append(MainFragment.nf.format(noFile)).append(" files (")
			.append(MainFragment.nf.format(totalSize)).append(" bytes), ")
			.append(MainFragment.nf.format(noDup)).append(" files (").append(MainFragment.nf.format(dupSize)).append(" bytes) duplicate, ")
			.append((realSize != 0) ? MainFragment.nf.format(dupSize * 100 / (double) realSize) : "0").append("% duplicate, ")
			.append(MainFragment.nf.format(groupSize)).append(" duplicate group, ")
			.append("took ").append(MainFragment.nf.format(duration)).append(" milliseconds");
		return sb;
	}
}
