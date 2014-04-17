package com.example.SafeInfo2;

import android.os.AsyncTask;
import android.util.Log;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class DropBoxAsyncTask extends AsyncTask<Void, Void, Void> {
    public static final String log="mylog";
    FileInputStream inputStream;
    private DropboxAPI<AndroidAuthSession> mDBApi;
    File file;

    public DropBoxAsyncTask( DropboxAPI<AndroidAuthSession> mDBApi, File file) {
        this.mDBApi=mDBApi;
        this.file=file;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            inputStream = new FileInputStream(file);
            DropboxAPI.Entry response = mDBApi.putFile(file.getName(), inputStream, file.length(), null, null);
            Log.d(log, "doInBackground() The uploaded file's rev is: " + response.rev);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(log, "doInBackground() FileNotFoundException " + e.toString());
        } catch (DropboxException e) {
            e.printStackTrace();
            Log.d(log, "doInBackground() DropboxException " + e.toString());
        }

        return null;
    }
}