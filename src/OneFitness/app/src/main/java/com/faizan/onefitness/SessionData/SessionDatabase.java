package com.faizan.onefitness.SessionData;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Database(entities = SessionData.class, exportSchema = false, version = 1)
@TypeConverters({LatLngsConverter.class})
public abstract class SessionDatabase extends RoomDatabase {
    private static final String TAG = "MyFit-SessionDatabase";
    private static final String DB_NAME = "session_db";
    private static SessionDatabase instance;

    private static Context context;
    // streams to copy files
    private static InputStream inStream = null;
    private static OutputStream outStream = null;

    // make sure theres one one instance so database is not corrupted
    public static synchronized SessionDatabase getInstance(Context cntxt) {
        context = cntxt;
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), SessionDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return instance;
    }

    public void destroyInstance() {
        if (instance != null && instance.isOpen() == true) {
            instance.close();
        }

        instance = null;
    }

    public boolean save(final Uri savePath) {
        destroyInstance();
        File db = context.getDatabasePath(DB_NAME); // get the database file
        Log.d(TAG, "save: " + db.getPath()); // log the file's name

        // open directory as DocumentFile
        DocumentFile saveDirectory = DocumentFile.fromTreeUri(context, savePath);
        // create new file in the directory, file type is an .sqlite3
        DocumentFile saveFile = saveDirectory.createFile("multipart/form-data", "OneFitnessData");

        // due to file handling it's best to add exceptions so all files are closed appropriately
        try {
            inStream = new FileInputStream(db); // read data from the app's database
            // output the data to the file we just created
            outStream = context.getContentResolver().openOutputStream(saveFile.getUri());

            copyBytes();
        } catch (FileNotFoundException e) { // if database was not found
            e.printStackTrace(); // log message but don't crash
        } catch (IOException e) { // if there was input with reading and writing
            e.printStackTrace();
        } catch (Exception e) { // handle other exceptions
            e.printStackTrace();
        } finally { // make sure to close files
            try {
                // if files have been opened
                if (inStream != null && outStream != null) {
                    // close files
                    inStream.close();
                    outStream.close();
                    // let user know it saved
                    Toast.makeText(context, "Database exported: " + saveFile.getName(), Toast.LENGTH_SHORT).show();
                    return true; // return success
                }

            } catch (IOException e) { // make sure files close properly
                e.printStackTrace();
            }
        }
        // let user know an error occurred
        Toast.makeText(context, "Failed to export!", Toast.LENGTH_SHORT).show();
        return false; // return failure if files were not opened and closed appropriately
    }

    public boolean load(final Uri fileUri) {
        destroyInstance(); // make sure to close the database
        File db = context.getDatabasePath(DB_NAME); // get the database file to overwrite
        DocumentFile userDatabase = DocumentFile.fromSingleUri(context, fileUri); // get the user chosen file

        // due to file handling it's best to add exceptions so all files are closed appropriately
        try {
            inStream = context.getContentResolver().openInputStream(userDatabase.getUri()); // read data from database
            // output data to the database file to overwrite
            outStream = new FileOutputStream(db, false);

            copyBytes();
        } catch (FileNotFoundException e) { // if database was not found
            e.printStackTrace(); // log message but don't crash
        } catch (IOException e) { // if there was input with reading and writing
            e.printStackTrace();
        } catch (Exception e) { // handle other exceptions
            e.printStackTrace();
        } finally { // make sure to close files
            try {
                // if files have been opened
                if (inStream != null && outStream != null) {
                    // close files
                    inStream.close();
                    outStream.close();
                    // let user know database was imported
                    Toast.makeText(context, "Database imported: " + userDatabase.getName(), Toast.LENGTH_SHORT).show();
                    return true; // return success
                }

            } catch (IOException e) { // make sure files close properly
                e.printStackTrace();
            }
        }
        // let user know an error occurred
        Toast.makeText(context, "Failed to import!", Toast.LENGTH_SHORT).show();
        return false; // return failure if files were not opened and closed appropriately
    }

    private void copyBytes() throws IOException {
        // read 2048 bytes each time
        byte[] buffer = new byte[2048];

        int bytesRead;
        // keep reading the data until the end is reached
        while((bytesRead = inStream.read(buffer)) != -1) {
            // save each piece of data in the new filemim
            outStream.write(buffer, 0, bytesRead);
        }
    }

    public abstract SessionDataDao sessionDataDao();
}