package com.fredzqm.jobee.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample title for user interfaces created by
 * Android template wizards.
 * <p/>
 */
public class Job implements Parcelable {
    @Exclude
    private String key;

    private String id;
    private String title;
    private String company;
    private String city;
    private String details;
    private Date date;

    public Job() {
        // required constructor for Jackson
    }

    // ---------------------- static methods to load data

    public Job(String id, String title, String city, String company, String details) {
        this.id = id;
        this.title = title;
        this.city = city;
        this.details = details;
        this.company = company;
        this.date = new Date();
    }

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Job> ITEMS = new ArrayList<Job>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Job> ITEM_MAP = new HashMap<String, Job>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    protected Job(Parcel in) {
        key = in.readString();
        id = in.readString();
        title = in.readString();
        company = in.readString();
        city = in.readString();
        details = in.readString();
    }

    public static final Creator<Job> CREATOR = new Creator<Job>() {
        @Override
        public Job createFromParcel(Parcel in) {
            return new Job(in);
        }

        @Override
        public Job[] newArray(int size) {
            return new Job[size];
        }
    };

    private static void addItem(Job item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static Job createDummyItem(int position) {
        return new Job(String.valueOf(position),
                "Item " + position, "city " + position, "company " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }


    // ---------- getters and setters
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(key);
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(company);
        parcel.writeString(city);
        parcel.writeString(details);
    }
}
