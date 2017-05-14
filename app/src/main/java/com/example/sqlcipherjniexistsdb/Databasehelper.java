package com.example.sqlcipherjniexistsdb;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

/**
 * Created by Sumeet on 23-12-2016.
 */

public class Databasehelper extends SQLiteOpenHelper {
    public static String DB_PATH;
    public static String DB_NAME = "DICTIONARY_NEW";
    //Version Done (2) on 30-04-17
    public static final int DB_VERSION = 1;
    public static final String TB_FTS = "FTS";
    private net.sqlcipher.database.SQLiteDatabase myDB;
    private Context context;
    private static Databasehelper instance;
    public static String KEY;


    public static synchronized Databasehelper getInstance(Context context) {
        if (instance == null) {
            DB_PATH = BuildConfig.DB_PATH;
            instance = new Databasehelper(context);
        }
        return instance;
    }

    public Databasehelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(net.sqlcipher.database.SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(net.sqlcipher.database.SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    }


    /***
     * Check if the database doesn't exist on device, create new one
     *
     * @throws IOException
     */
    public void createOpenDataBase(String encryptKey) throws IOException {
        //VIP -> checkDataBase method, return false if press Apps in background button (Bottom Right button in device) and remove it from back-stack.
        boolean dbExist = checkDataBase();
        //This key is used to encrypt and decrypt the db!
        KEY = encryptKey;
        if (!dbExist) {
            try {
                //we should pass empty string, when we have to open any other database which is not encrypted by SqlCipher!
                this.getReadableDatabase("");

                //When we use encrypted DB (so that no one can get the data after extracting apk->assets and -> DB)
                //So we are keeping encrypting file only.
                //We made the DB using the above KEY!
                //this.getReadableDatabase(KEY);

                //first we copy from assets folder to app's internal storage!
                copyDataBase();
                //Then we make another db and copy the data in encrypted manner!
                encryptDB();
            } catch (IOException e) {
                Log.d("createOpenDatabase", e.getMessage());
            } catch (Exception e) {
                Log.d("createOpenDB", e.getMessage());
            }
        }

        //Then opening DB!, This is not required as we can db gets open when we call getReadable/getWritable!
        try {
            openDataBase();
        } catch (net.sqlcipher.SQLException e) {
            e.printStackTrace();
        }
    }

    /***
     * Check if the database is exist on device or not
     * This method will throw an error when it does not get any database.
     *
     * @return
     */
    private boolean checkDataBase() {
        net.sqlcipher.database.SQLiteDatabase tempDB = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            tempDB = net.sqlcipher.database.SQLiteDatabase.openDatabase(myPath, KEY, null, net.sqlcipher.database.SQLiteDatabase.OPEN_READWRITE);
        } catch (net.sqlcipher.database.SQLiteException e) {
            Log.d("checkDataBase", e.getMessage());
        }
        if (tempDB != null)
            tempDB.close();
        return tempDB != null ? true : false;
    }


    /***
     * Copy database from source code assets to device
     *
     * @throws IOException
     */
    public void copyDataBase() throws IOException {
        try {
            InputStream myInput = context.getAssets().open(DB_NAME);
            String outputFileName = DB_PATH + DB_NAME;
            OutputStream myOutput = new FileOutputStream(outputFileName);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }

            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (Exception e) {
            Log.d("copyDatabase", e.getMessage());
        }

    }


    /***
     * Open database
     *
     * @throws SQLException
     */
    public void openDataBase() throws net.sqlcipher.SQLException {
        String myPath = DB_PATH + DB_NAME;
        myDB = net.sqlcipher.database.SQLiteDatabase.openDatabase(myPath, KEY, null, net.sqlcipher.database.SQLiteDatabase.OPEN_READWRITE);
    }


    public SQLiteDatabase getMYDB() {
        return myDB;
    }

    private void encryptDB() throws IOException {
        String myPath = DB_PATH + DB_NAME;
        File originalFile = context.getDatabasePath(myPath);

        File newFile = File.createTempFile("sqlcipherutils", "tmp", context.getCacheDir());

        //As this was for default db (without encryption)!
        net.sqlcipher.database.SQLiteDatabase existing_db = net.sqlcipher.database.SQLiteDatabase.openDatabase(myPath, "", null, net.sqlcipher.database.SQLiteDatabase.OPEN_READWRITE);

        //And now we are using already encrypted DB!
        //KEY
        //net.sqlcipher.database.SQLiteDatabase existing_db = net.sqlcipher.database.SQLiteDatabase.openDatabase(myPath, KEY, null, net.sqlcipher.database.SQLiteDatabase.OPEN_READWRITE);
        String newPath = newFile.getPath();
        existing_db.rawExecSQL("ATTACH DATABASE '" + newPath + "' AS encrypted KEY '" + KEY + "';");
        existing_db.rawExecSQL("SELECT sqlcipher_export('encrypted');");
        existing_db.rawExecSQL("DETACH DATABASE encrypted;");

        existing_db.close();
        //Deleting original plain text db!
        originalFile.delete();
        //Renaming the new db same as old db!
        newFile.renameTo(originalFile);

    }


    @Override
    public synchronized void close() {
        if (myDB != null) {
            myDB.close();
        }
        super.close();
    }

}
