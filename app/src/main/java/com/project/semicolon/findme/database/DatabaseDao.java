package com.project.semicolon.findme.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DatabaseDao {
    @Query("SELECT * FROM ContactEntity")
    LiveData<List<ContactEntity>> getAll();

    @Insert
    void insert(ContactEntity phone);

    @Delete
    void delete(ContactEntity phone);

    @Query("SELECT phone_number FROM contactentity")
    List<String> getAllPhoneNumbers();

}
