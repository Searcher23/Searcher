package com.free.searcher;

import android.widget.*;
import android.os.*;
import android.app.*;
import android.view.View.*;
import android.view.*;
import android.util.*;
import android.content.*;
import java.util.*;
import java.io.*;
import android.graphics.*;

/**
 * Activities that contain this fragment must implement the
 * {@link net.rdrei.android.dirchooser.DirectoryChooserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link net.rdrei.android.dirchooser.DirectoryChooserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReplaceAllFragment extends DialogFragment implements  Serializable {

	private static final long serialVersionUID = 2904216776319456127L;

	String files = null;
	String saveTo = null;
	String stardict = null;
	String replace = "";
	String by = "";
	boolean isRegex = false;
	boolean caseSensitive = false;
	boolean includeEnter = false;

    private transient Button mBtnConfirm;
    private transient Button mBtnCancel;
    private transient Button filesBtn;
    private transient Button saveToBtn;
	private transient Button stardictBtn;
    public transient EditText fileET;
    public transient EditText saveToET;
	public transient EditText stardictET;
	public transient EditText replaceET;
    public transient EditText byET;
    public transient CheckBox isRegexCB;
    public transient CheckBox caseSensitiveCB;
	public transient CheckBox includeEnterCB;

	private transient OnFragmentInteractionListener mListener;

    public ReplaceAllFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param newDirectoryName Name of the directory to create.
     * @param initialDirectory Optional argument to define the path of the directory
     *                         that will be shown first.
     *                         If it is not sent or if path denotes a non readable/writable directory
     *                         or it is not a directory, it defaults to
     *                         {@link android.os.Environment#getExternalStorageDirectory()}
     * @return A new instance of fragment DirectoryChooserFragment.
     */
    public static ReplaceAllFragment newInstance(
		final String newDirectoryName,
		final String initialDirectory) {
        if (newDirectoryName == null) {
            return null;
        }
        ReplaceAllFragment fragment = new ReplaceAllFragment();
        Bundle args = new Bundle();
        args.putString("Files", newDirectoryName);
        args.putString("SaveTo", initialDirectory);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.e("ReplaceAllFragment", "onSaveInstanceState");
        if (outState == null) {
            return;
        }
        super.onSaveInstanceState(outState);

		outState.putString("Files", fileET.getText() + "");
		outState.putString("SaveTo", saveToET.getText() + "");
		outState.putString("stardict", stardictET.getText() + "");
		outState.putBoolean("isRegex", isRegex);
		outState.putBoolean("caseSensitive", caseSensitive);
		outState.putBoolean("includeEnter", includeEnter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.e("ReplaceAllFragment", "onCreate");


        if (savedInstanceState != null && fileET != null) {
//            files = savedInstanceState.getString("Files");
//			saveTo = savedInstanceState.getString("SaveTo");

			fileET.setText(savedInstanceState.getString("Files"));
			saveToET.setText(savedInstanceState.getString("SaveTo"));
			stardictET.setText(savedInstanceState.getString("stardict"));
			isRegexCB.setChecked(savedInstanceState.getBoolean("isRegex", false));
			caseSensitiveCB.setChecked(savedInstanceState.getBoolean("caseSensitive", false));
			includeEnterCB.setChecked(savedInstanceState.getBoolean("includeEnter", false));
        }

        if (this.getShowsDialog()) {
            setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        } else {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
        assert getActivity() != null;
		Point op = new Point();
		getActivity().getWindowManager().getDefaultDisplay().getSize(op);
//		Log.d("getDefaultDisplay", getWindowManager().getDefaultDisplay() + ".");
//		Log.d("op", op + ".");
//		Log.d("getComponentName", getComponentName().toString());
//		Log.d("getRequestedOrientation()", getRequestedOrientation() + ".");
		View view;
		if (op.x < op.y) {
			view = inflater.inflate(R.layout.replace_portrait, container, false);
		} else {
			view = inflater.inflate(R.layout.replace_dialog, container, false);
		}

		Log.e("ReplaceAllFragment", "onCreateView");
        mBtnConfirm = (Button) view.findViewById(R.id.okDir);
        mBtnCancel = (Button) view.findViewById(R.id.cancelDir);
        fileET = (EditText) view.findViewById(R.id.files);
        saveToET = (EditText) view.findViewById(R.id.saveTo);
		stardictET = (EditText) view.findViewById(R.id.stardict);
		replaceET = (EditText) view.findViewById(R.id.replace);
        byET = (EditText) view.findViewById(R.id.by);
        isRegexCB = (CheckBox) view.findViewById(R.id.regex);
        caseSensitiveCB = (CheckBox) view.findViewById(R.id.caseSensitive);
		includeEnterCB = (CheckBox) view.findViewById(R.id.includeEnter);
		filesBtn = (Button) view.findViewById(R.id.filesBtn);
        saveToBtn = (Button) view.findViewById(R.id.saveToBtn);
		stardictBtn = (Button) view.findViewById(R.id.stardictBtn);

		fileET.setText(files);//savedInstanceState.getString("Files"));
		saveToET.setText(saveTo);//savedInstanceState.getString("SaveTo"));
		stardictET.setText(stardict);
		replaceET.setText(replace);
		byET.setText(by);
		isRegexCB.setChecked(isRegex);
		caseSensitiveCB.setChecked(caseSensitive);
		includeEnterCB.setChecked(includeEnter);

		filesBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ReplaceAllFragment.this.dismiss();

					Intent intent = new Intent(getActivity(), FolderChooserActivity.class);
					intent.putExtra(MainFragment.SELECTED_DIR, new String[] {Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()});
					intent.putExtra(MainFragment.SUFFIX, MainFragment.ALL_SUFFIX);
					intent.putExtra(MainFragment.MODE, MainFragment.MULTI_FILES);
					intent.putExtra(MainFragment.CHOOSER_TITLE, MainFragment.ALL_SUFFIX_TITLE);
					getActivity().startActivityForResult(intent, MainFragment.FILES_REQUEST_CODE);
				}
			});

		saveToBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ReplaceAllFragment.this.dismiss();
					
					Intent intent = new Intent(getActivity(), FolderChooserActivity.class);
					intent.putExtra(MainFragment.SELECTED_DIR, new String[] {Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()});
					intent.putExtra(MainFragment.SUFFIX, "");
					intent.putExtra(MainFragment.MODE, !MainFragment.MULTI_FILES);
					intent.putExtra(MainFragment.CHOOSER_TITLE, "Output Folder");
					getActivity().startActivityForResult(intent, MainFragment.SAVETO_REQUEST_CODE);
				}
			});
			
		stardictBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ReplaceAllFragment.this.dismiss();

					Intent intent = new Intent(getActivity(), FolderChooserActivity.class);
					intent.putExtra(MainFragment.SELECTED_DIR, new String[] {Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()});
					intent.putExtra(MainFragment.SUFFIX, MainFragment.TXT_SUFFIX);
					intent.putExtra(MainFragment.MODE, !MainFragment.MULTI_FILES);
					intent.putExtra(MainFragment.CHOOSER_TITLE, MainFragment.TXT_SUFFIX_TITLE);
					getActivity().startActivityForResult(intent, MainFragment.STARDICT_REQUEST_CODE);
				}
			});

        mBtnConfirm.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mListener.onOk(ReplaceAllFragment.this);
				}
			});

        mBtnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mListener.onCancelChooser(ReplaceAllFragment.this);
				}
			});
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
		Log.e("ReplaceAllFragment", "onAttach");
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
										 + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
		Log.e("ReplaceAllFragment", "onDetach");
        mListener = null;
    }

    void debug(String message, Object... args) {
        Log.e("chooser", String.format(message, args));
    }
	

}


