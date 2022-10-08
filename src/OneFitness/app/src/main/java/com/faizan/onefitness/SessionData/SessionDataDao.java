package com.faizan.onefitness.SessionData;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SessionDataDao {
    @Query("Select * from session") // queries to table to get all items
    List<SessionData> getSessionDataList();

    // get in order starting from most recent session
    @Query("Select * from session ORDER BY CAST(startTime AS Long) DESC")
    List<SessionData> getSessionDataListSorted();

    @Query("Select * from session Where id = :id")
    SessionData loadSessionByID(int id);

    @Insert
    void insertSessionData(SessionData session);

    @Update
    void updateSessionData(SessionData session);

    @Delete
    void deleteSessionData(SessionData session);
}

