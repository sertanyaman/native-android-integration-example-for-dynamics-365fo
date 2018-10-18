package com.sertanyaman.dynamics365test.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sertanyaman.dynamics365test.models.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TasksDBHelper extends SQLiteOpenHelper {
    private static TasksDBHelper sInstance;

    private static final String DATABASE_NAME = "tasks.db";

    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_LINENUM = "line_num";
    public static final String COLUMN_WORKER = "worker";
    public static final String COLUMN_WORKERNAME = "worker_name";
    public static final String COLUMN_CUSTACCOUNT = "cust_account";
    public static final String COLUMN_CUSTNAME = "cust_name";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_DATE = "visit_date";
    public static final String COLUMN_NEWRECORD = "new_rec";

    public static final String TABLE_TASKS = "tasks";

    private static final int DATABASE_VERSION = 1;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public TasksDBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, version);
    }

    public TasksDBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, DATABASE_NAME, factory, version, errorHandler);
    }

    private TasksDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //SQLiteOpenHelper standard
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACT_TABLE = "create table "
                + TABLE_TASKS + "( "
                + COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_LINENUM + " INTEGER, "
                + COLUMN_WORKER + " TEXT, "
                + COLUMN_WORKERNAME + " TEXT, "
                + COLUMN_CUSTACCOUNT + " TEXT, "
                + COLUMN_CUSTNAME + " TEXT, "
                + COLUMN_ADDRESS + " TEXT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_NEWRECORD + " INTEGER "
                + ")";

        db.execSQL(CREATE_CONTACT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
            onCreate(db);
        }
    }

    public void open() throws SQLException {
        SQLiteDatabase database = getWritableDatabase();
    }

    //Singleton
    public static synchronized TasksDBHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TasksDBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    //Operations
    public void addTask(Task task) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_LINENUM, task.getLineNum());
            values.put(COLUMN_WORKER, task.getWorker());
            values.put(COLUMN_WORKERNAME, task.getWorkerName());
            values.put(COLUMN_CUSTACCOUNT, task.getCustAccount());
            values.put(COLUMN_CUSTNAME, task.getCustName());
            values.put(COLUMN_ADDRESS, task.getAddress());
            values.put(COLUMN_DATE, formatter.format(task.getVisitDateTime()));
            values.put(COLUMN_WORKER, task.getWorker());
            values.put(COLUMN_NEWRECORD, task.isNewRecord() ? 1 : 0);

            db.insertOrThrow(TABLE_TASKS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("TASKS", "Error while trying to add task to database");
        } finally {
            db.endTransaction();
        }
    }

    public List<Task> getAllTasks(List<Task> tasks) {
        if(tasks == null) {
            tasks = new ArrayList<>();
        }
        else
        {
            tasks.clear();
        }

        String TASKS_SELECT_QUERY =
                String.format("SELECT * FROM %s ORDER BY %s DESC, %s DESC", TABLE_TASKS, COLUMN_DATE, COLUMN_NEWRECORD );

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(TASKS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Task newTask = new Task();
                    newTask.setLineNum(cursor.getInt(cursor.getColumnIndex(COLUMN_LINENUM)));
                    newTask.setWorker(cursor.getString(cursor.getColumnIndex(COLUMN_WORKER)));
                    newTask.setWorkerName(cursor.getString(cursor.getColumnIndex(COLUMN_WORKERNAME)));
                    newTask.setCustAccount(cursor.getString(cursor.getColumnIndex(COLUMN_CUSTACCOUNT)));
                    newTask.setCustName(cursor.getString(cursor.getColumnIndex(COLUMN_CUSTNAME)));
                    newTask.setAddress(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)));
                    newTask.setVisitDateTime(formatter.parse(cursor.getString(cursor.getColumnIndex(COLUMN_DATE))));
                    newTask.setNewRecord(cursor.getInt(cursor.getColumnIndex(COLUMN_NEWRECORD)) == 1);

                    tasks.add(newTask);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("TASKS", "Error while trying to get tasks from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return tasks;
    }

    public int markTaskAsRead(Task task) {
        SQLiteDatabase db = getWritableDatabase();
        int ret = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NEWRECORD, 0);

            // Updating profile picture url for user with that userName
            ret =  db.update(TABLE_TASKS, values, COLUMN_LINENUM + " = ?",
                    new String[] { String.valueOf(task.getLineNum()) });

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("TASKS", "Error while trying to add task to database");
        } finally {
            db.endTransaction();
        }

        return ret;
    }

    public int cleanTasks() {
        SQLiteDatabase db = getWritableDatabase();
        int ret = -1;

        db.beginTransaction();
        try {
            // Updating profile picture url for user with that userName
            ret =  db.delete(TABLE_TASKS, "", new String[]{});

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("TASKS", "Error while deleting tasks database");
        } finally {
            db.endTransaction();
        }

        return ret;
    }
}
