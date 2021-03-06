package com.fredzqm.jobee.recruiter.JobList;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fredzqm.jobee.R;
import com.fredzqm.jobee.model.Job;
import com.fredzqm.jobee.recruiter.JobList.JobListFragment.Callback;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.fredzqm.jobee.model.Job} and makes a call to the
 * specified {@link Callback}.
 */
public class JobListAdapter extends RecyclerView.Adapter<JobListAdapter.ViewHolder> implements ChildEventListener {
    public final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy/MM/dd");
    private final List<com.fredzqm.jobee.model.Job> mJobs;
    private final Callback mCallback;
    private final Context mContext;

    private DatabaseReference mRef;

    public JobListAdapter(Context context, Callback callback) {
        mJobs = new ArrayList<>();
        mContext = context;
        mCallback = callback;
        mRef = Job.getReference();
        mRef.orderByChild(Job.RECRUITER_KEY).equalTo(mCallback.getUserID())
                .addChildEventListener(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.re_joblist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mJob = mJobs.get(position);
        holder.updateView();
    }

    @Override
    public int getItemCount() {
        return mJobs.size();
    }


    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Job added = dataSnapshot.getValue(Job.class);
        String key = dataSnapshot.getKey();
        added.setKey(key);
        mJobs.add(0, added);
        notifyDataSetChanged();
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Job changedTo = dataSnapshot.getValue(Job.class);
        String key = dataSnapshot.getKey();
        for (int i = 0; i < mJobs.size(); i++) {
            if (key.equals( mJobs.get(i).getKey())) {
                mJobs.set(i, changedTo);
                notifyDataSetChanged();
                return;
            }
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        String key = dataSnapshot.getKey();
        for (int i = 0; i < mJobs.size(); i++) {
            if (key.equals(mJobs.get(i).getKey())) {
                mJobs.remove(i);
                notifyDataSetChanged();
                return;
            }
        }
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.d("Error", "onCancelled: " + databaseError.getMessage());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mTitleTextView;
        public final TextView mCityTextView;
        public final TextView mDateTextView;

        public Job mJob;

        public ViewHolder(View view) {
            super(view);
            mTitleTextView = (TextView) view.findViewById(R.id.re_list_item_title);
            mDateTextView = (TextView) view.findViewById(R.id.re_list_item_date);
            mCityTextView = (TextView) view.findViewById(R.id.re_list_item_city);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mCallback) {
                        mCallback.showJobDetail(mJob.getKey());
                    }
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mRef.child(mJob.getKey()).removeValue();
                    Toast.makeText(mContext, mContext.getString(R.string.re_delete_message , mJob.getTitle()), Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }

        public void updateView() {
            mTitleTextView.setText(mJob.getTitle());
            mDateTextView.setText(DATEFORMAT.format(mJob.getDate()));
            mCityTextView.setText(mJob.getCity());
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleTextView.getText() + "'";
        }

    }
}
