package com.example.SafeInfo2;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;

import java.util.concurrent.ExecutionException;

public class CustomDropBoxDialogClass extends Dialog implements
        android.view.View.OnClickListener {

    private static final String PREFS_LOAD_NAME ="LoadFileNameDropBox";
    public Activity c;
    public Dialog d;
    public Button yes, no;
    public static final String log="mylog";
    public static final String DIR_FILES = "SafeInfoFiles";
    EditText editTextSave;
    String fileNameForLoad;
    private DropboxAPI<AndroidAuthSession> mDBApi;
    EditText editeText;


    public String getFileNameForSave() {
        return fileNameForLoad;
    }


    public CustomDropBoxDialogClass(Activity a, DropboxAPI<AndroidAuthSession> mDBApi, EditText editeText ) {
        super(a);
        this.c = a;
        this.mDBApi = mDBApi;
        this.editeText=editeText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
        TextView tv= (TextView) findViewById(R.id.saveAsTextId);
        tv.setText("Load file with name:");
        yes = (Button) findViewById(R.id.saveDialogButton);
        yes.setText("Load");
        no = (Button) findViewById(R.id.cancelDialogButton);
        editTextSave=(EditText) findViewById(R.id.ediTextSaveAs);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveDialogButton:

                fileNameForLoad = editTextSave.getText().toString();      // вынимаем из выбраной editTExt текст
                Log.d(log, "CustomDropBoxDialogClass fileNameForSave: " + fileNameForLoad);
                try {
                    String result = new ReadDropBoxAsyncTask(mDBApi, fileNameForLoad).execute().get();
                    editeText.setText(result);
//                    SharedPreferences prefs = c.getApplicationContext().getSharedPreferences(PREFS_LOAD_NAME, 0);
//                    SharedPreferences.Editor edit = prefs.edit();
//                    edit.putString(PREFS_LOAD_NAME, result);
//                    edit.commit();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }



                break;
            case R.id.cancelDialogButton:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }


}
