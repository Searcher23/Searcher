///*
// * Copyright (C) 2009 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.free.p7zip;
//
//import android.app.Activity;
//import android.widget.TextView;
//import android.os.Bundle;
//import java.io.*;
//import java.util.*;
//
//import android.content.*;
//import android.annotation.*;
//import android.os.*;
//import android.support.annotation.*;
//
//import android.net.*;
//import android.widget.*;
//import android.app.*;
//import com.free.searcher.*;
//import com.free.searcher.R.*;
//
//public class HelloJni extends Activity {
//	
////	public static Context applicationContext;
//	public static Uri treeUri;
//
//	/**
//	 * The requestCode with which the storage access framework is triggered for folder.
//	 */
//	private static final int READ_REQUEST_CODE = 42;
//	private static final int WRITE_REQUEST_CODE = 43;
//	private static final int EDIT_REQUEST_CODE = 44;
//	private static final int INTENT_WRITE_REQUEST_CODE = 10;
//	
//    /** Called when the activity is first created. */
//    @Override
//    public void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//
//		new File("/storage/emulated/0/AppProjects/p7zip_15_14/bin/p7zip_15_14.apk").delete();
//		new File("/storage/emulated/0/AppProjects/p7zip_15_14/bin/classes.dex").delete();
//		new File("/storage/emulated/0/AppProjects/p7zip_15_14/bin/resources.ap_").delete();
//        /* Create a TextView and set its content.
//         * the text is retrieved by calling a native
//         * function.
//         */
//        TextView  tv = new TextView(this);
//        tv.setText("hi");
//        setContentView(tv);
//		try {
//			Collection<String> l = new Andro7za().listing("/storage/MicroSD/Zip/7z1514-src.7z", "");
//			l = new Andro7za().listing("/storage/MicroSD/temp/zip/java-unrar-decryption-supported-src-20120903.rar", "");
//			String st = Andro7za.collectionToString(l, true, "\n");
////			applicationContext = getApplicationContext();
//			
//			FileUtils.applicationContext = getApplicationContext();
//			if (FileUtils.treeUri == null) {// && !checkFolder(new File(st).getParentFile(), WRITE_REQUEST_CODE)) {
//				
//				FileUtils.treeUri = FileUtils.getSharedPreferenceUri(R.string.key_internal_uri_extsdcard);
//				if (FileUtils.treeUri == null) {
//					AlertDialog.Builder alert = new AlertDialog.Builder(this);
//					alert.setTitle("Grant Permission in extSdCard");
//					alert.setMessage("In the following Android dialog, " 
//									 + "please select the external SD card and confirm at the bottom.");
//					alert.setCancelable(true);
//					alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//								triggerStorageAccessFramework(INTENT_WRITE_REQUEST_CODE);
//							}
//						});
//					alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//								dialog.cancel();
//							}
//						});
//					AlertDialog alertDialog = alert.create();
//					alertDialog.show();
//				}
//			}
//			FileUtils.copyFile(new File("/storage/MicroSD/temp/zip/java-unrar-decryption-supported-src-20120903.rar"), 
//			new File("/storage/MicroSD/temp/zip/java-unrar-decryption-supported-src-20120903xxx.rar"));
//			tv.setText(st);
////			System.out.println(st);
//
//		} catch (IOException e) {
//			tv.setText(e.getMessage());
//			e.printStackTrace();
//		}
//    }
//	
//	/**
//	 * Check the folder for writeability. If not, then on Android 5 retrieve Uri for extsdcard via Storage
//	 * Access Framework.
//	 *
//	 * @param folder The folder to be checked.
//	 * @param code   The request code of the type of folder check.
//	 * @return true if the check was successful or if SAF has been triggered.
//	 */
////	private boolean checkFolder(@NonNull final File folder, final int code) {
////		
////		if (SystemUtil.isAndroid5() && FileUtils.isOnExtSdCard(folder)) {
////			if (!folder.exists() || !folder.isDirectory()) {
////				return false;
////			}
////
////			// On Android 5, trigger storage access framework.
////			if (!FileUtils.isWritableNormalOrSaf(folder)) {
////				// Ensure via listener that storage access framework is called only after information
////				// message.
////				Toast.makeText(activity, "In the following Android dialog, " 
////							   + "please select the external SD card and confirm at the bottom.", Toast.LENGTH_LONG);
////				//DialogUtil.displayInfo(getActivity(), listener, R.string.message_dialog_select_extsdcard);
////				triggerStorageAccessFramework(code);
////				return false;
////			}
////			// Only accept after SAF stuff is done.
////			return true;
////		} else if (SystemUtil.isKitkat() && FileUtils.isOnExtSdCard(folder)) {
////			// Assume that Kitkat workaround works
////			return true;
////		}
////		else if (FileUtils.isWritable(new File(folder, "DummyFile"))) {
////			return true;
////		}
////		else {
////			Toast.makeText(activity, "Cannot write to folder " + folder, Toast.LENGTH_LONG);
//////			DialogUtil.displayError(getActivity(), R.string.message_dialog_cannot_write_to_folder, false,
//////									mCurrentFolder);
//////
//////			mCurrentFolder = null;
////			return false;
////		}
////	}
//
//	/**
//	 * Trigger the storage access framework to access the base folder of the ext sd card.
//	 *
//	 * @param code The request code to be used.
//	 */
//	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
//	private void triggerStorageAccessFramework(final int code) {
//		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//		startActivityForResult(intent, code);
//	}
//	
//	@Override
//	public final void onActivityResult(final int requestCode, final int resultCode, @NonNull final Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//
//		if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
//			onActivityResultLollipop(requestCode, resultCode, data);
//		}
//	}
//
//	/**
//	 * After triggering the Storage Access Framework, ensure that folder is really writable. Set preferences
//	 * accordingly.
//	 *
//	 * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who
//	 *                    this result came from.
//	 * @param resultCode  The integer result code returned by the child activity through its setResult().
//	 * @param data        An Intent, which can return result data to the caller (various data can be attached to Intent
//	 *                    "extras").
//	 */
//	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
//	private void onActivityResultLollipop(final int requestCode, final int resultCode, @NonNull final Intent data) {
//
//		if (requestCode == INTENT_WRITE_REQUEST_CODE) { 
//
//			if (resultCode == Activity.RESULT_OK) { 
//				// Get Uri from Storage Access Framework. 
//				Uri treeUri = data.getData(); 
//				// Persist URI in shared preference so that you can use it later. 
//				// Use your own framework here instead of PreferenceUtil. 
//
//				FileUtils.setSharedPreferenceUri(R.string.key_internal_uri_extsdcard, treeUri); 
//				// Persist access permissions. 
//				final int takeFlags = data.getFlags() & 
//					(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION); 
//				System.out.println("treeUri:" + treeUri);
//				System.out.println("takeFlags" + String.valueOf(takeFlags));
//				System.out.println("data.getFlags()" + String.valueOf(data.getFlags()));
//				getContentResolver().takePersistableUriPermission(treeUri, takeFlags); 
//			} 
//		}
//
//	}
//}
