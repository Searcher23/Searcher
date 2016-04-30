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

/**
 * Activities that contain this fragment must implement the
 * {@link net.rdrei.android.dirchooser.DirectoryChooserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link net.rdrei.android.dirchooser.DirectoryChooserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CompareFragment extends DialogFragment implements  Serializable {

	private static final long serialVersionUID = 2904216776319456129L;

	String files = null;
	String saveTo = null;
	

    private transient Button mBtnConfirm;
    private transient Button mBtnCancel;
    private transient Button filesBtn;
    private transient Button saveToBtn;
    public transient EditText fileET;
    public transient EditText saveToET;

	private transient OnFragmentInteractionListener mListener;

    public CompareFragment() {
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
    public static CompareFragment newInstance(
		final String newDirectoryName,
		final String initialDirectory) {
        if (newDirectoryName == null) {
            return null;
        }
        CompareFragment fragment = new CompareFragment();
        Bundle args = new Bundle();
        args.putString("Files", newDirectoryName);
        args.putString("SaveTo", initialDirectory);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.e("CompareFragment", "onSaveInstanceState");
        if (outState == null) {
            return;
        }
        super.onSaveInstanceState(outState);

		outState.putString("Files", fileET.getText() + "");
		outState.putString("SaveTo", saveToET.getText() + "");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.e("CompareFragment", "onCreate");
//        if (getArguments() == null) {
//            throw new IllegalArgumentException(
//                    "You must create DirectoryChooserFragment via newInstance().");
//        } else {
////            files = getArguments().getString("Files");
////            saveTo = getArguments().getString("SaveTo");
////			fileET.setText(getArguments().getString("Files"));
////			saveToET.setText(getArguments().getString("SaveTo"));
//        }

        if (savedInstanceState != null && fileET != null) {
//            files = savedInstanceState.getString("Files");
//			saveTo = savedInstanceState.getString("SaveTo");
//			parts = savedInstanceState.getLong("Parts");
//			partSize = savedInstanceState.getLong("PartSize");
			fileET.setText(savedInstanceState.getString("Files"));
			saveToET.setText(savedInstanceState.getString("SaveTo"));

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
        final View view = inflater.inflate(R.layout.compare, container, false);

		Log.e("CompareFragment", "onCreateView");
        mBtnConfirm = (Button) view.findViewById(R.id.okDir);
        mBtnCancel = (Button) view.findViewById(R.id.cancelDir);
        fileET = (EditText) view.findViewById(R.id.files);
        saveToET = (EditText) view.findViewById(R.id.saveTo);
		
		filesBtn = (Button) view.findViewById(R.id.filesBtn);
        saveToBtn = (Button) view.findViewById(R.id.saveToBtn);

		//if (savedInstanceState != null) {
//            files = savedInstanceState.getString("Files");
//			saveTo = savedInstanceState.getString("SaveTo");
//			parts = savedInstanceState.getLong("Parts");
//			partSize = savedInstanceState.getLong("PartSize");
		fileET.setText(files);//savedInstanceState.getString("Files"));
		saveToET.setText(saveTo);//savedInstanceState.getString("SaveTo"));
		
        //}

		filesBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CompareFragment.this.dismiss();

					Intent intent = new Intent(getActivity(), FolderChooserActivity.class);
					intent.putExtra(MainFragment.SELECTED_DIR, new String[] {files});
					intent.putExtra(MainFragment.SUFFIX, MainFragment.DOC_FILES_SUFFIX);
					intent.putExtra(MainFragment.MODE, !MainFragment.MULTI_FILES);
					intent.putExtra(MainFragment.CHOOSER_TITLE, MainFragment.ORI_SUFFIX_TITLE);
					getActivity().startActivityForResult(intent, MainFragment.FILES_REQUEST_CODE);
				}
			});

		saveToBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CompareFragment.this.dismiss();

					Intent intent = new Intent(getActivity(), FolderChooserActivity.class);
					intent.putExtra(MainFragment.SELECTED_DIR, new String[] {saveTo});
					intent.putExtra(MainFragment.SUFFIX, MainFragment.DOC_FILES_SUFFIX);
					intent.putExtra(MainFragment.MODE, !MainFragment.MULTI_FILES);
					intent.putExtra(MainFragment.CHOOSER_TITLE, MainFragment.MODI_SUFFIX_TITLE);
					getActivity().startActivityForResult(intent, MainFragment.SAVETO_REQUEST_CODE);
				}
			});

        mBtnConfirm.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mListener.onOk(CompareFragment.this);
				}
			});

        mBtnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mListener.onCancelChooser(CompareFragment.this);
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


