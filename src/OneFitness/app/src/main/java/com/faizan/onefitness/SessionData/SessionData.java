package com.faizan.onefitness.SessionData;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "session") // set table name
public class SessionData implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    // title for each column
    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "startTime")
    private long startTime;

    @ColumnInfo(name = "endTime")
    private long endTime;

    @ColumnInfo(name = "activeTime")
    private String activeTime;

    @ColumnInfo(name = "distance")
    private String distance;

    @ColumnInfo(name = "calories")
    private String calories;

    @ColumnInfo(name = "speed")
    private String speed;

    @ColumnInfo(name = "track")
    private LatLngs track;

    @ColumnInfo(name = "steps")
    private String steps;

    public SessionData(int id, String name, long startTime, long endTime, String activeTime, String distance, String calories,
                       String speed, LatLngs track, String steps) {
        // set value from constructor as new field in table
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.activeTime = activeTime;
        this.distance = distance;
        this.calories = calories;
        this.speed = speed;
        this.track = track;
        this.steps = steps;
    }

    @Ignore
    public SessionData(int id, long startTime, long endTime, String name, String activeTime, String distance, String calories,
                       String speed, LatLngs track) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.activeTime = activeTime;
        this.distance = distance;
        this.calories = calories;
        this.speed = speed;
        this.track = track;
    }

    @Ignore
    public SessionData(String name, long startTime, long endTime, String activeTime, String distance, String calories,
                       String speed, LatLngs track, String steps) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.activeTime = activeTime;
        this.distance = distance;
        this.calories = calories;
        this.speed = speed;
        this.track = track;
        this.steps = steps;
    }

    @Ignore
    public SessionData(String name, long startTime, long endTime, String activeTime, String distance, String calories,
                       String speed, LatLngs track) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.activeTime = activeTime;
        this.distance = distance;
        this.calories = calories;
        this.speed = speed;
        this.track = track;
    }

    // getters a.d setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(String activeTime) {
        this.activeTime = activeTime;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getCalories() {
        return calories;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public LatLngs getTrack() {
        return track;
    }

    public void setTrack(LatLngs track) {
        this.track = track;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }
}
