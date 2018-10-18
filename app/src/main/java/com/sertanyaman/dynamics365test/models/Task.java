package com.sertanyaman.dynamics365test.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Task implements Parcelable {
    @SerializedName("linenum")
    @Expose
    private int lineNum;
    @SerializedName("custaccount")
    @Expose
    private String custAccount;
    @SerializedName("worker")
    @Expose
    private String worker;
    @SerializedName("visitdatetime")
    @Expose
    private Date visitDateTime;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("workername")
    @Expose
    private String workerName;
    @SerializedName("custname")
    @Expose
    private String custName;

    private boolean newRecord;

    public Task() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }


    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public String getCustAccount() {
        return custAccount;
    }

    public void setCustAccount(String custAccount) {
        this.custAccount = custAccount;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public Date getVisitDateTime() {
        return visitDateTime;
    }

    public void setVisitDateTime(Date visitDateTime) {
        this.visitDateTime = visitDateTime;
    }

    public String getReadableDate()
    {
        SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();

        return formatter.format(this.visitDateTime);
    }

    public boolean isNewRecord() {
        return newRecord;
    }

    public void setNewRecord(boolean newRecord) {
        this.newRecord = newRecord;
    }

    /* everything below here is for implementing Parcelable */

    // 99.9% of the time you can just ignore this
    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(lineNum);
        out.writeString(custAccount);
        out.writeString(worker);
        out.writeString(address);
        out.writeString(workerName);
        out.writeString(custName);
        out.writeInt(newRecord ? 1:0);
        out.writeSerializable(visitDateTime);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private Task(Parcel in) {
        lineNum = in.readInt();
        custAccount = in.readString();
        worker = in.readString();
        address = in.readString();
        workerName = in.readString();
        custName = in.readString();
        newRecord = (in.readInt() == 1);
        visitDateTime = (Date) in.readSerializable();
    }


}
