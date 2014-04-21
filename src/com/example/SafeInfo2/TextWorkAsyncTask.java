package com.example.SafeInfo2;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.UnsupportedEncodingException;

import static com.example.SafeInfo2.Consts.log;


public class TextWorkAsyncTask extends AsyncTask<Void, Void, String> {
    Activity a;
    String text;
    Cipher cipher;

    public TextWorkAsyncTask(Activity activity, String string, Cipher cipher) {
        a=activity;
        text=string;
        this.cipher=cipher;

    }

    @Override
    protected String doInBackground(Void... params) {
        String result = null;
        try {
            byte[] input = text.getBytes("UTF8");
            byte[] boxCharMus = cipher.doFinal(input);

            result = android.util.Base64.encodeToString(boxCharMus, android.util.Base64.DEFAULT);

        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            Log.d(log, "IllegalBlockSizeException  " + e.toString());
        } catch (BadPaddingException e) {
            e.printStackTrace();
            Log.d(log, "BadPaddingException " + e.toString());
        }   catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d(log, "UnsupportedEncodingException " + e.toString());
        }

        return result;
    }
}
