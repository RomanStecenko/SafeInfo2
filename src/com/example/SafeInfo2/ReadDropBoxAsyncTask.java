package com.example.SafeInfo2;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import java.io.*;

public class ReadDropBoxAsyncTask extends AsyncTask<Void, Void, String> {

    public static final String log="mylog";
    FileOutputStream outputStream;
    private DropboxAPI<AndroidAuthSession> mDBApi;
    File file,sdPath;
    public static final String DIR_FILES = "SafeInfoFiles";
    String LoadFileNameDropBox;


    public ReadDropBoxAsyncTask( DropboxAPI<AndroidAuthSession> mDBApi ,String fileNameDropBox) {
        this.mDBApi=mDBApi;
        LoadFileNameDropBox=fileNameDropBox;
    }

    public String getStringFromFile(String filePath) {
        File fl = new File(filePath);
        FileInputStream fin = null;
        String ret = "", res = "";
        StringBuilder sb = new StringBuilder();
        String line = null;

        try {
            fin = new FileInputStream(fl);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fin,"UTF-8")); //windows-1251
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            ret = sb.toString();
            String ret1 = new String(sb);


            Log.d(log, "getStringFromFile() ret: " + ret);
            Log.d(log, "getStringFromFile() ret1: " + ret1);

            reader.close();
            fin.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }



    @Override
    protected String doInBackground(Void... params) {
        sdPath = Environment.getExternalStorageDirectory();
        // добавляем свой каталог к пути
        sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_FILES);

        try {
            file = new File(sdPath,"fromDropbox.txt");
            outputStream = new FileOutputStream(file);
            DropboxAPI.DropboxFileInfo info = mDBApi.getFile(LoadFileNameDropBox, null, outputStream, null);
            Log.d(log,"ReadDropBoxAsyncTask The file's rev is: " + info.getMetadata().rev);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(log, "ReadDropBoxAsyncTask doInBackground() FileNotFoundException " + e.toString());
        } catch (DropboxException e) {
            e.printStackTrace();
            Log.d(log, "ReadDropBoxAsyncTask doInBackground() DropboxException " + e.toString());
        }
        return getStringFromFile(sdPath+"/"+"fromDropbox.txt");
    }
}