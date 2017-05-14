package com.example.sqlcipherjniexistsdb;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.sqlcipherjniexistsdb.databinding.MainActivityBinding;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private MainActivityBinding mainActivityBinding;
    private Databasehelper databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivityBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);


        //Loading Sqlcipher Library
        SQLiteDatabase.loadLibs(this);

        try {
            //Loading data from existing db to encrypted DB!
            loadAllData();

            //Reading from DB!
            showDataFromDb();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void loadAllData() throws IOException {
        try {
            databaseManager = Databasehelper.getInstance(MainActivity.this);
            databaseManager.createOpenDataBase(stringFromJNI());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showDataFromDb() throws IOException {
        SQLiteDatabase db = Databasehelper.getInstance(this).getWritableDatabase(stringFromJNI());
        Cursor cursor = db.rawQuery("SELECT * FROM '" + Databasehelper.TB_FTS + "';", null);
        //Log.d(MainActivity.class.getSimpleName(), "Rows count: " + cursor.getCount());

        String dbValues = "";

        if (cursor.moveToFirst()) {
            do {
                dbValues = dbValues + "\n" + cursor.getString(0) + " , " + cursor.getString(1);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        mainActivityBinding.sampleText.setText(dbValues);
        databaseManager.close();
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
