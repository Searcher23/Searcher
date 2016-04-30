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
public class SplitMergeFragment extends DialogFragment implements  Serializable {
	
	private static final long serialVersionUID = 2904216776319456127L;
	
	String files = null;
	String saveTo = null;
	String parts = "2";
	String partSize = "0";
	
    private transient Button mBtnConfirm;
    private transient Button mBtnCancel;
    private transient Button filesBtn;
    private transient Button saveToBtn;
    public transient EditText fileET;
    public transient EditText saveToET;
    public transient EditText partsET;
    public transient EditText partSizeET;
	
	private transient OnFragmentInteractionListener mListener;
    
    public SplitMergeFragment() {
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
    public static SplitMergeFragment newInstance(
            final String newDirectoryName,
            final String initialDirectory) {
        if (newDirectoryName == null) {
            return null;
        }
        SplitMergeFragment fragment = new SplitMergeFragment();
        Bundle args = new Bundle();
        args.putString("Files", newDirectoryName);
        args.putString("SaveTo", initialDirectory);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.e("SplitMergeFragment", "onSaveInstanceState");
        if (outState == null) {
            return;
        }
        super.onSaveInstanceState(outState);
		
		outState.putString("Files", fileET.getText() + "");
		outState.putString("SaveTo", saveToET.getText() + "");
		outState.putString("Parts", partsET.getText() + "");
		outState.putString("PartSize", partSizeET.getText() + "");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.e("SplitMergeFragment", "onCreate");
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
			partsET.setText(savedInstanceState.getString("Parts"));
			partSizeET.setText(savedInstanceState.getString("PartSize"));
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
        final View view = inflater.inflate(R.layout.split_merge, container, false);

		Log.e("SplitMergeFragment", "onCreateView");
        mBtnConfirm = (Button) view.findViewById(R.id.okDir);
        mBtnCancel = (Button) view.findViewById(R.id.cancelDir);
        fileET = (EditText) view.findViewById(R.id.files);
        saveToET = (EditText) view.findViewById(R.id.saveTo);
        partsET = (EditText) view.findViewById(R.id.parts);
        partSizeET = (EditText) view.findViewById(R.id.partSize);
		filesBtn = (Button) view.findViewById(R.id.filesBtn);
        saveToBtn = (Button) view.findViewById(R.id.saveToBtn);
		
		//if (savedInstanceState != null) {
//            files = savedInstanceState.getString("Files");
//			saveTo = savedInstanceState.getString("SaveTo");
//			parts = savedInstanceState.getLong("Parts");
//			partSize = savedInstanceState.getLong("PartSize");
			fileET.setText(files);//savedInstanceState.getString("Files"));
			saveToET.setText(saveTo);//savedInstanceState.getString("SaveTo"));
			partsET.setText(parts);//savedInstanceState.getString("Parts") + "");
			partSizeET.setText(partSize);//savedInstanceState.getString("PartSize") + "");
        //}
		
		filesBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SplitMergeFragment.this.dismiss();
//					FragmentTransaction ft = getFragmentManager().beginTransaction();
//					ft.detach(SplitMergeFragment.this);
//					ft.commit();
					Intent intent = new Intent(getActivity(), FolderChooserActivity.class);
//					Log.e("newSearch.oldselectedFiles", Util.arrayToString(selectedFiles, true, SearchFragment.LINE_SEP));
//					Arrays.sort(selectedFiles);
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
					SplitMergeFragment.this.dismiss();
//					FragmentTransaction ft = getFragmentManager().beginTransaction();
//					ft.detach(SplitMergeFragment.this);
//					ft.commit();
					Intent intent = new Intent(getActivity(), FolderChooserActivity.class);
//					Log.e("newSearch.oldselectedFiles", Util.arrayToString(selectedFiles, true, SearchFragment.LINE_SEP));
//					Arrays.sort(selectedFiles);
					intent.putExtra(MainFragment.SELECTED_DIR, new String[] {Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()});
					intent.putExtra(MainFragment.SUFFIX, "");
					intent.putExtra(MainFragment.MODE, !MainFragment.MULTI_FILES);
					intent.putExtra(MainFragment.CHOOSER_TITLE, "Output Folder");
					getActivity().startActivityForResult(intent, MainFragment.SAVETO_REQUEST_CODE);
				}
			});
		
        mBtnConfirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.onOk(SplitMergeFragment.this);
            }
        });

        mBtnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.onCancelChooser(SplitMergeFragment.this);
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
		Log.e("SplitMergeFragment", "onAttach");
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
		Log.e("SplitMergeFragment", "onDetach");
//        mListener = null;
    }
    
    void debug(String message, Object... args) {
        Log.e("chooser", String.format(message, args));
    }
	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	
}


