package com.sertanyaman.dynamics365test.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.sertanyaman.dynamics365test.R;
import com.sertanyaman.dynamics365test.comm.AzureAuthenticationHelper;
import com.sertanyaman.dynamics365test.comm.D365ServiceHelper;
import com.sertanyaman.dynamics365test.database.TasksDBHelper;
import com.sertanyaman.dynamics365test.models.Task;

import java.util.List;

import com.sertanyaman.dynamics365test.nothub.AzHubNotificationsHandler;
import com.sertanyaman.dynamics365test.nothub.AzureHubsRegistrationHelper;


public class TasksActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TasksRecyclerViewAdapter adapter;
    private SwipeRefreshLayout swipeContainer;
    List<Task>  tasksList;
    TasksDBHelper   dbHelper;
    private SettingsHelper settingsHelper=null;
    private AzureAuthenticationHelper aadHelper = null;
    private AzureAuthenticationHelper.OnAuthorizationResult onAuthorizationResult;
    private D365ServiceHelper serviceHelper = null;
    private AzureHubsRegistrationHelper hubsHelper = null;
    private ProgressBar progressBar;
    private boolean isProgress=false;

    public static TasksActivity mainActivity;
    public static Boolean isVisible = false;
    private static final String TAG = "TasksActivity";


    private void snackIt(String snack) {
        View view = ((Activity) TasksActivity.this).getCurrentFocus();
        if(view!=null) {
            Snackbar.make(view, snack, Snackbar.LENGTH_LONG).show();
        }
    }

    public void toastNotify(final String notificationMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TasksActivity.this, notificationMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        aadHelper.getAadContext().onActivityResult(requestCode, resultCode, data);
    }

    public Activity getActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        //List view
        recyclerView = (RecyclerView) findViewById(R.id.rvTasks);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        dbHelper = TasksDBHelper.getInstance(this);
        tasksList = dbHelper.getAllTasks(tasksList);

        adapter = new TasksRecyclerViewAdapter(tasksList, this);
        recyclerView.setAdapter(adapter);

        //Swipe refresh
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshServer();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                swipeContainer.setEnabled(topRowVerticalPosition >= 0);

            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        //Preferences
        PreferenceManager.setDefaultValues(this, R.xml.app_settings, true);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        settingsHelper = SettingsHelper.getHelper();
        settingsHelper.loadFromSharedPreferences(sharedPreferences);

        //AAD
        aadHelper = AzureAuthenticationHelper.getInstance();
        aadHelper.setOnAuthorizationResult(new AzureAuthenticationHelper.OnAuthorizationResult() {
            @Override
            public void onAuthorizationSuccess(String token) {
                if(!TextUtils.isEmpty(token)) {
                    //Continue Refresh
                    snackIt("Authorization success!");

                    serviceHelper.updateTasks(TasksActivity.this);
                }
                else
                {
                    snackIt("Authorization failed!");
                }
            }


            @Override
            public void onAuthorizationFail() {
                snackIt("Authorization failed!");
                removeProgress();
            }
        });

        serviceHelper = D365ServiceHelper.getInstance();
        hubsHelper = AzureHubsRegistrationHelper.getHelper();

        settingsHelper.setOnSettingChange(new SettingsHelper.OnSettingChange() {
            @Override
            public void settingChanged(String s) {
                aadHelper.initializeFromSettings();
                serviceHelper.initFromSettings();
                hubsHelper.registerWithNotificationHubs(TasksActivity.this, true);
            }
        });

        hubsHelper.setOnNewFirebaseTokenReceived(new AzureHubsRegistrationHelper.OnNewFirebaseTokenReceived() {
            @Override
            public void tokenReceived(String token) {
                hubsHelper.registerWithNotificationHubs(TasksActivity.this);
            }
        });

        serviceHelper.setOnReceiveServerData(new D365ServiceHelper.OnReceiveServerData() {
            @Override
            public void onDataReceived() {
                refreshList();

            }

            @Override
            public void onConnectionError() {
                snackIt("Connection error!");
                removeProgress();
            }
        });

        //FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshServer();
            }
        });

        mainActivity = this;

        hubsHelper.registerWithNotificationHubs(TasksActivity.this);
        AzHubNotificationsHandler.createChannelAndHandleNotifications(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean ret = super.onOptionsItemSelected(item);

        if(!ret) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.settings:
                    intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(intent);
                    ret = true;
                    break;

                case R.id.refresh:
                    refreshServer();
                    ret = true;
                    break;

                case R.id.clear:
                    dbHelper.cleanTasks();
                    refreshList();
                    ret = true;
                    break;

                default:
                    ret = false;
                    break;
            }
        }

        return ret;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
        isVisible = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        isVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isVisible = false;
    }

    private void setProgressOn()
    {
        isProgress = true;
        swipeContainer.setRefreshing(false);
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void removeProgress()
    {
        if(isProgress)
        {
            swipeContainer.setRefreshing(false);
            progressBar.setVisibility(View.INVISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            isProgress = false;
        }

    }

    private void refreshServer()
    {
        setProgressOn();
        aadHelper.getTokenFromServer(TasksActivity.this);
    }

    private void refreshList()
    {
        tasksList = dbHelper.getAllTasks(tasksList);
        recyclerView.getAdapter().notifyDataSetChanged();
        removeProgress();
    }





}
