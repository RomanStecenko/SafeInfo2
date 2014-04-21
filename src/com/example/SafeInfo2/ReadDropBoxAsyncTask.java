package com.example.SafeInfo2;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import java.io.*;

import static com.example.SafeInfo2.Consts.DIR_FILES;
import static com.example.SafeInfo2.Consts.log;

public class ReadDropBoxAsyncTask extends AsyncTask<Void, String, String> {


    FileOutputStream outputStream;
    private DropboxAPI<AndroidAuthSession> mDBApi;
    File file, sdPath;
    String LoadFileNameDropBox;
    ProgressDialog progressDialog;
    long fileSize;


    public ReadDropBoxAsyncTask(DropboxAPI<AndroidAuthSession> mDBApi, String fileNameDropBox, Activity activity) {
        this.mDBApi = mDBApi;
        LoadFileNameDropBox = fileNameDropBox;
        progressDialog = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.setMessage("Downloading source..");
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(String s) {

        progressDialog.dismiss();
    }

    public String getStringFromFile(String filePath) {
        File fl = new File(filePath);
        FileInputStream fin = null;
        String ret = "", res = "";
        StringBuilder sb = new StringBuilder();
        String line = null;
        int count;
        long total = 0;

        try {
            fin = new FileInputStream(fl);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fin, "UTF-8")); //windows-1251

            while ((line = reader.readLine()) != null) {
                total += line.length();
              //  publishProgress("" + (int) ((total * 100) / fileSize));
                sb.append(line).append("\n");
            }
            ret = sb.toString();
//            Log.d(log, "getStringFromFile() ret: " + ret);
//            Log.d(log, "getStringFromFile() ret1: " + ret1);

            reader.close();
            fin.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }


//    @Override
//    protected void onProgressUpdate(String... values) {
//        progressDialog.setProgress(Integer.parseInt(values[0]));
//    }

    @Override
    protected String doInBackground(Void... params) {

            sdPath = Environment.getExternalStorageDirectory();
        sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_FILES);

        try {
            file = new File(sdPath, "fromDropbox");
            outputStream = new FileOutputStream(file);
            DropboxAPI.DropboxFileInfo info = mDBApi.getFile(LoadFileNameDropBox, null, outputStream, null);
            fileSize = info.getFileSize();
            Log.d(log, "FILE SIZE: " + fileSize);
            Log.d(log, "ReadDropBoxAsyncTask The file's rev is: " + info.getMetadata().rev);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(log, "ReadDropBoxAsyncTask doInBackground() FileNotFoundException " + e.toString());
        } catch (DropboxException e) {
            e.printStackTrace();
            Log.d(log, "ReadDropBoxAsyncTask doInBackground() DropboxException " + e.toString());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                    outputStream = null;
                } catch (IOException e) {
                    Log.d(log, "IOException2 load file dropbbox2: " + e.toString());
                }
            }
        }
        return getStringFromFile(sdPath + "/" + "fromDropbox");
    }
}