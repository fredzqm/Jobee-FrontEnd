package com.fredzqm.jobee.job_seeker;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.fredzqm.jobee.ContainedFragment;
import com.fredzqm.jobee.job_seeker.AppliedJob.AppliedJobFragment;
import com.fredzqm.jobee.job_seeker.AppliedJob.AppliedJobListAdapter;
import com.fredzqm.jobee.job_seeker.AppliedJob.AppliedJobListFragment;
import com.fredzqm.jobee.job_seeker.resume.QRCodeFragment;
import com.fredzqm.jobee.model.Job;
import com.fredzqm.jobee.LoginActivity;
import com.fredzqm.jobee.R;
import com.fredzqm.jobee.job_seeker.Home.HomeFragment;
import com.fredzqm.jobee.job_seeker.JobList.JobDetailFragment;
import com.fredzqm.jobee.job_seeker.JobList.JobListFragment;
import com.fredzqm.jobee.job_seeker.resume.ResumeFragment;
import com.fredzqm.jobee.model.JobSeeker;
import com.fredzqm.jobee.model.Submission;
import com.fredzqm.jobee.notification.Notifier;
import com.fredzqm.jobee.recruiter.JobList.JobFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class JobSeekerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        ResumeFragment.Callback, HomeFragment.Callback, JobListFragment.Callback, JobFragment.Callback,
        AppliedJobListFragment.Callback, JobDetailFragment.Callback, QRCodeFragment.Callback, AppliedJobFragment.Callback {
    private static final String TAG = "JobSeekerActivity";

    private FloatingActionButton mFab;
    private TextView mNavTitleTextView;
    private TextView mNavSmallTextView;
    private AppliedJobListAdapter mAppliedJobListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.js_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContainedFragment container = ((ContainedFragment) getSupportFragmentManager().findFragmentById(R.id.js_fragment_container));
                container.clickFab();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.js_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headView = navigationView.getHeaderView(0);
        mNavTitleTextView = (TextView) headView.findViewById(R.id.js_nav_text_title);
        mNavSmallTextView = (TextView) headView.findViewById(R.id.js_nav_text_small);

        Intent intent = getIntent();
        if (intent != null && intent.getStringExtra(Notifier.NOTIF_TYPE) != null) {
            swapFragment(AppliedJobListFragment.newInstance(), null);
            mAppliedJobListAdapter = new AppliedJobListAdapter(this, intent.getStringExtra(Notifier.SUBMISSION_KEY));
        } else if (savedInstanceState == null) {
            swapFragment(HomeFragment.newInstance(), null);
        }
        DatabaseReference mRef = JobSeeker.getRefernce().child(getUserID());
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mJobseeker = dataSnapshot.getValue(JobSeeker.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled " + databaseError);
            }
        });
    }

    private void swapFragment(ContainedFragment fragment, String tag) {
        mFab.hide();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.js_fragment_container, fragment);
        if (tag != null)
            ft.addToBackStack(tag);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.login_action_logout) {
            Intent intent = this.getIntent();
            this.setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        FragmentManager fm = getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            fm.popBackStackImmediate();
        }

        switch (item.getItemId()) {
            case R.id.js_nav_home:
                swapFragment(HomeFragment.newInstance(), null);
                break;
            case R.id.js_nav_resume:
                swapFragment(ResumeFragment.newInstance(), null);
                break;
            case R.id.js_nav_joblist:
                swapFragment(JobListFragment.newInstance(), null);
                break;
            case R.id.js_nav_applied:
                swapFragment(AppliedJobListFragment.newInstance(), null);
                break;
            default:
                throw new RuntimeException("Not implemented navigation bar yet");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getSupportFragmentManager().findFragmentById(R.id.js_fragment_container).onActivityResult(requestCode, resultCode, data);
    }

    // for home fragment
    private JobSeeker mJobseeker;

    @Override
    public void updateAccount(JobSeeker jobSeeker) {
        mJobseeker = jobSeeker;
        mNavTitleTextView.setText(getString(R.string.hello, jobSeeker.getName()));
    }

    public JobSeeker getJobSeeker() {
        return mJobseeker;
    }


    @Override
    public void showJobDetail(Submission submission) {
        swapFragment(AppliedJobFragment.newInstance(submission), "Job Detail");
    }

    @Override
    public void showJobDetail(Job mItem) {
        swapFragment(JobDetailFragment.newInstance(mItem), "Job Detail");
    }

    @Override
    public void showQRCode(String resumeKey) {
        swapFragment(QRCodeFragment.newInstance(resumeKey), "QR code");
    }

    @Override
    public FloatingActionButton getFab() {
        return mFab;
    }

    @Override
    public String getUserID() {
        return LoginActivity.getUserID();
    }

    @Override
    public RecyclerView.Adapter getAppliedJobListAdapter() {
        if (mAppliedJobListAdapter == null)
            mAppliedJobListAdapter = new AppliedJobListAdapter(this);
        return mAppliedJobListAdapter;

    }
}
