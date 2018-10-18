package com.sertanyaman.dynamics365test.comm;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sertanyaman.dynamics365test.activities.SettingsHelper;
import com.sertanyaman.dynamics365test.database.TasksDBHelper;
import com.sertanyaman.dynamics365test.models.Task;


import org.json.JSONObject;
import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class D365ServiceHelper
{
    private static D365ServiceHelper helperInstance;
    private SettingsHelper settings;
    private AzureAuthenticationHelper aadHelper;
    private TasksDBHelper tasksDBHelper=null;
    private static String TAG = "D365 helper";
    private static String SVC_ENDPOINT_GETTASKS = "api/services/AndroidTests/AndVisitSchedule/getNewTasks";
    private static String SVC_ENDPOINT_COUNTTASKS = "api/services/AndroidTests/AndVisitSchedule/newTaskCount";
    private String worker;
    Uri uriGetTasks,uriCountTasks;

    public interface OnReceiveServerData
    {
        public void onDataReceived();
        public void onConnectionError();
    }
    private OnReceiveServerData onReceiveServerData;

    public void setOnReceiveServerData(OnReceiveServerData onReceiveServerData) {
        this.onReceiveServerData = onReceiveServerData;
    }


    //Singleton
    public static synchronized D365ServiceHelper getInstance() {
        if(helperInstance==null)
        {
            helperInstance = new D365ServiceHelper();
        }

        return helperInstance;
    }

    private D365ServiceHelper() {
        settings = SettingsHelper.getHelper();
        aadHelper = AzureAuthenticationHelper.getInstance();
        tasksDBHelper = TasksDBHelper.getInstance(null);
        initFromSettings();
    }

    public void initFromSettings()
    {
        worker = settings.getWorker();
        Uri baseUri = Uri.parse(settings.getAxUrl());
        uriGetTasks = Uri.withAppendedPath(baseUri, SVC_ENDPOINT_GETTASKS);
        uriCountTasks = Uri.withAppendedPath(baseUri, SVC_ENDPOINT_COUNTTASKS);
    }

    private void callD365ServiceNewTasks(Context context) {

        if (aadHelper.getCurToken() == null) {return;}


        RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("_worker", worker);
        } catch (Exception e) {
            Log.d(TAG, "Failed to put parameters: " + e.toString());
        }

        VolleyJsonRequestExtension request = new VolleyJsonRequestExtension(Request.Method.POST, uriGetTasks.toString(), jsonObject,new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {

                updateDBWithData(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Service call error: " + error.toString());
                onReceiveServerData.onConnectionError();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + aadHelper.getCurToken());
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //do the call using Volley
        queue.add(request);
    }

    public void updateTasks(Context context)
    {
        callD365ServiceNewTasks(context);
    }

    private void updateDBWithData(JSONArray    json)
    {
        Log.d(TAG, "Json response: " + json.toString());

        Gson gson = new Gson();
        String jsonResponse = json.toString();
        Type listType = new TypeToken<List<Task>>(){}.getType();
        List<Task> tasks = gson.fromJson(jsonResponse, listType);

        for(Task task : tasks)
        {
            task.setNewRecord(true);
            tasksDBHelper.addTask(task);
        }

        onReceiveServerData.onDataReceived();
    }



}
