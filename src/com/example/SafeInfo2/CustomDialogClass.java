package com.example.SafeInfo2;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;

public class CustomDialogClass extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button yes, no;
    public static final String log="mylog";
    public static final String DIR_FILES = "SafeInfoFiles";
    EditText editTextSave;
    String message;
    String fileNameForSave;
    int mode=0;
    private DropboxAPI<AndroidAuthSession> mDBApi;


    public String getFileNameForSave() {
        return fileNameForSave;
    }



    public CustomDialogClass(Activity a, String m) {
        super(a);
        this.c = a;
        message=m;
    }

    public CustomDialogClass(Activity a, String m, int mod, DropboxAPI<AndroidAuthSession> mDBApi ) {
        super(a);
        this.c = a;
        message=m;
        mode=mod;
        this.mDBApi = mDBApi;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
        yes = (Button) findViewById(R.id.saveDialogButton);
        no = (Button) findViewById(R.id.cancelDialogButton);
        editTextSave=(EditText) findViewById(R.id.ediTextSaveAs);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveDialogButton:
                try {
                    File sdPath1 = Environment.getExternalStorageDirectory();      // находим путь к файловому хранилищу
                    sdPath1 = new File(sdPath1.getAbsolutePath() + "/" + DIR_FILES);   // добавляем в путь нашу папку
                    fileNameForSave = editTextSave.getText().toString();      // вынимаем из выбраной editTExt текст
                    Log.d(log, "fileNameForSave: " + fileNameForSave);
                    File sdFile = new File(sdPath1, fileNameForSave);            // создаем файл по пути и с именем
                    BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));       // буферезированый поток записи в файл
                    //String encodeText = android.util.Base64.encodeToString(textForSave.getBytes("UTF8"), android.util.Base64.DEFAULT);    //кодировка сообщения
                    Log.d(log, "encodeText на SD: " + message);//+ encodeText);
                    bw.write(message);//(encodeText);    //запись в файл
                    bw.flush();
                    bw.close();

                    if (mode==1){
                        if (getFileNameForSave().length()>0){
                            File file = new File(sdPath1,getFileNameForSave());
                            new DropBoxAsyncTask(mDBApi,file).execute();
                            //FileInputStream inputStream = null;
//                            try {
//                                inputStream = new FileInputStream(file);
//                                DropboxAPI.Entry response = mDBApi.putFile("db-"+getFileNameForSave(), inputStream, file.length(), null, null);
//                                Log.d(log, "DbExampleLog The uploaded file's rev is: " + response.rev);
//                            } catch (FileNotFoundException e) {
//                                e.printStackTrace();
//                            } catch (DropboxException e) {
//                                e.printStackTrace();
//                            }
                        }    else {
                            Log.d(log, "Error with name or some else: " + getFileNameForSave() + "length:" + getFileNameForSave().length());
                        }
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.d(log, "encryptToFile(), FileNotFoundException " + e.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(log, "Exception save " + e.toString());
                }
                break;
//                c.finish();
//                break;
            case R.id.cancelDialogButton:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}

