package com.fredzqm.jobee.job_seeker.resume;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.SingleLineTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.fredzqm.jobee.R;
import com.fredzqm.jobee.ContainedFragment;
import com.fredzqm.jobee.model.JobSeeker;
import com.fredzqm.jobee.model.Resume;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Callback} interface
 * to handle interaction events.
 * Use the {@link ResumeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResumeFragment extends ContainedFragment implements ChildEventListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String TAG = "ResumeFragment";

    private Callback mCallback;

    private RecyclerView mRecyclerView;
    private ResumeAdapter mResumeAdapter;

    private Spinner mSpinner;
    private ArrayAdapter<String> mSwitchAdapter;

    private ArrayList<Resume> mResumes;
    private int curIndex;
    private DatabaseReference mResumeRef;


    public ResumeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ResumeFragment.
     */
    public static ResumeFragment newInstance() {
        ResumeFragment fragment = new ResumeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mResumes = new ArrayList<>();
        mResumeAdapter = new ResumeAdapter(getContext());
        mResumeRef = Resume.getReference();
        mResumeRef.orderByChild(JobSeeker.JOB_SEEKER_KEY).equalTo(mCallback.getUserID())
                .addChildEventListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mResumeRef.orderByChild(JobSeeker.JOB_SEEKER_KEY).equalTo(mCallback.getUserID())
                .removeEventListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.recyclerview, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mResumeAdapter);
        mRecyclerView.setHasFixedSize(false);
        mCallback.getFab().setImageResource(R.drawable.qr_icon);
        mCallback.getFab().show();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Callback) {
            mCallback = (Callback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Callback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.js_resume, menu);
        MenuItem item = menu.findItem(R.id.js_action_switch);
        item.setActionView(R.layout.resume_switch_list);
        mSpinner = (Spinner) item.getActionView().findViewById(R.id.resume_switch_spiner);
        mSwitchAdapter = new ArrayAdapter<String>(getContext(), R.layout.resume_switch_list_content, R.id.resume_name) {
            public int getCount() {
                return mResumes.size();
            }

            public String getItem(int position) {
                return mResumes.get(position).getResumeName();
            }
        };
        mSwitchAdapter.setDropDownViewResource(R.layout.resume_switch_list_content);
        mSpinner.setAdapter(mSwitchAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switchTo(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final EditText editText = new EditText(getContext());
        switch (item.getItemId()) {
            case R.id.js_action_add_category:
                editText.setHint("Category");
                editText.setTransformationMethod(SingleLineTransformationMethod.getInstance());
                new AlertDialog.Builder(getContext())
                        .setView(editText)
                        .setTitle("Add new Category")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mResumeAdapter.addCategory(editText.getText().toString());
                            }
                        })
                        .show();
                break;
            case R.id.js_action_delete:
                mResumeRef.child(mResumes.get(curIndex).getKey()).removeValue();
                break;
            case R.id.js_action_edit_name:
                final Resume editedResume = mResumes.get(curIndex);
                editText.setHint("Resume name");
                editText.setTransformationMethod(SingleLineTransformationMethod.getInstance());
                editText.setText(editedResume.getResumeName());
                new AlertDialog.Builder(getContext())
                        .setView(editText)
                        .setTitle("Edit the resume name")
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Resume created = Resume.newInstance(editText.getText().toString(), mCallback.getJobSeeker());
                                mResumeRef.child(editedResume.getKey()).setValue(created);
                            }
                        })
                        .show();
                break;
            case R.id.js_action_add_resume:
                editText.setHint("Resume name");
                editText.setTransformationMethod(SingleLineTransformationMethod.getInstance());
                new AlertDialog.Builder(getContext())
                        .setView(editText)
                        .setTitle("Create new resume")
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Resume created = Resume.newInstance(editText.getText().toString(), mCallback.getJobSeeker());
                                mResumeRef.push().setValue(created);
                            }
                        })
                        .show();
                break;
            default:
                throw new RuntimeException("Not implemented");
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchTo(int index) {
        curIndex = index;
        mResumeAdapter.setResume(mResumes.get(index));
        mResumeAdapter.notifyDataSetChanged();
        mSwitchAdapter.notifyDataSetChanged();
        mSpinner.setSelection(index);
    }


    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        String key = dataSnapshot.getKey();
        Resume resume = dataSnapshot.getValue(Resume.class);
        resume.setKey(key);
        mResumes.add(resume);
        switchTo(mResumes.size() - 1);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        String key = dataSnapshot.getKey();
        Resume changedTo = dataSnapshot.getValue(Resume.class);
        changedTo.setKey(key);
        for (int i = 0; i < mResumes.size(); i++) {
            Resume r = mResumes.get(i);
            if (key.equals(r.getKey())) {
                mResumes.set(i, changedTo);
                if (i == curIndex) {
                    switchTo(i);
                } else {
                    mSwitchAdapter.notifyDataSetChanged();
                }
                return;
            }
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        String key = dataSnapshot.getKey();
        for (int i = 0; i < mResumes.size(); i++) {
            Resume r = mResumes.get(i);
            if (key.equals(r.getKey())) {
                mResumes.remove(i);
                if (i == curIndex) {
                    switchTo(0);
                } else {
                    mSwitchAdapter.notifyDataSetChanged();
                }
                return;
            }
        }
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.d(TAG, "onCancelled " + databaseError.getMessage());
    }


    @Override
    public void clickFab() {
        if (mResumes.size() > 0)
            mCallback.showQRCode(mResumes.get(curIndex).getKey());
        else
            Toast.makeText(getContext(), "Please create a resume first!", Toast.LENGTH_SHORT).show();
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
    public interface Callback {
        FloatingActionButton getFab();

        String getUserID();

        void showQRCode(String resumeKey);

        JobSeeker getJobSeeker();
    }
}
