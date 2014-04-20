package com.example.SafeInfo2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static com.example.SafeInfo2.Consts.*;


public class MainFragment extends Fragment implements View.OnClickListener {
    private Button buttonSwap, buttonFirstMethod, buttonSecondMethod, makeButton;
    private Animation animMove, animMove2, animRotate;
    private boolean methodFlag, aes, blow, des, autentif, error, firstLvl = true;
    private String encryptMethods[], encryptMethodsValue[], text2, text3, readText, algorithm, chosenFile, accessToken, toDo;
    private EditText  encryptEditText;
    private TextView resultTextView;
    private byte[] input, keyValue, keyValueToCheck, standardKeyValue24, standardKeyValue;
    private Cipher ecipher, dcipher;
    private ArrayList<String> str = new ArrayList<String>();
    private Item[] fileList;
    private ListAdapter adapterDialog;
    private MenuItem inOut;
    private File path = new File(Environment.getExternalStorageDirectory() + "");

    final static private String APP_KEY = "kmm0ovc79qua3q9";
    final static private String APP_SECRET = "ynjvf1poc5zbpw4";
    final static private String ACCESS_TOKEN = "access_token";
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
    private AndroidAuthSession session = new AndroidAuthSession(appKeys);  //, ACCESS_TYPE

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ActionBar ab = ((ActionBarActivity) activity).getSupportActionBar();
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);
        final LayoutInflater inflater = (LayoutInflater) activity.getSystemService("layout_inflater");
        View view = inflater.inflate(R.layout.action_bar_edit_mode, null);
        buttonFirstMethod = (Button) view.findViewById(R.id.action_bar_firstMethod);
        buttonFirstMethod.setVisibility(View.VISIBLE);
        buttonFirstMethod.setBackgroundColor(Color.TRANSPARENT);
        buttonSecondMethod = (Button) view.findViewById(R.id.action_bar_secondMethod);
        buttonSecondMethod.setVisibility(View.VISIBLE);
        buttonSecondMethod.setBackgroundColor(Color.TRANSPARENT);
        buttonSwap = (Button) view.findViewById(R.id.action_bar_swap);
        ab.setCustomView(view);
        ab.setDisplayShowCustomEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment, container, false);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        loadFileList();
        initializationVariables();
        initializationMainObjects();


        if (savedInstanceState != null){
            methodFlag = savedInstanceState.getBoolean(METHOD_FLAG);
            keyValue = savedInstanceState.getByteArray(KEY_VALUE);
            keyValueToCheck = savedInstanceState.getByteArray(KEY_VALUE_TO_CHECK);

            if (!methodFlag) {
                buttonFirstMethod.setText("Decrypt");
                buttonSecondMethod.setText("Encrypt");
                makeButton.setText("Decrypt");
            } else {
                buttonFirstMethod.setText("Encrypt");
                buttonSecondMethod.setText("Decrypt");
                makeButton.setText("Encrypt");
            }
        }

    }

    @Override
    public void onResume() {

        if (mDBApi != null) {
            if (mDBApi.getSession().authenticationSuccessful()) {
                try {
                    // Required to complete auth, sets the access token on the session
                    mDBApi.getSession().finishAuthentication();

                    String accessToken = mDBApi.getSession().getOAuth2AccessToken();
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString(ACCESS_TOKEN, accessToken);
                    edit.commit();
                    autentif=true;
                    inOut.setTitle("Logout Dropbox");
                    Log.d(log, "onResume() 4"  );
                    Log.d(log, "accessToken= " + accessToken );
                } catch (IllegalStateException e) {
                    Log.d(log, "DbAuthLog Error authenticating: " + e.toString());
                }
            }
        }
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_bar_firstMethod:
            break;
            case R.id.action_bar_swap:
                swap();
                break;
            case R.id.action_bar_secondMethod:
                break;
            case R.id.lookKeyButton:
                lookKey();
                break;
            case R.id.make:
                String text = encryptEditText.getText().toString();
                Log.d(log, "TEXT length: " + text.length());
                if (text.length() <= 0) {
                    Toast.makeText(getActivity(), "Input your text first", Toast.LENGTH_SHORT).show();
                } else {

                    if (algorithm.equals(AES) && keyValueToCheck.length == 16) {                    //lookKeyEditText.getText().toString().length()  <=>    keyValueToCheck.length
                        keyValue = keyValueToCheck;                 //lookKeyEditText.getText().toString().getBytes() <=>   keyValueToCheck
                        Log.d(log, "keyValue : " + new String(keyValue));
                    } else if (algorithm.equals(BLOWFISH) && keyValueToCheck.length <= 16 && keyValueToCheck.length > 0) {
                        keyValue = keyValueToCheck;
                        Log.d(log, "keyValue : " + new String(keyValue));
                    } else if (algorithm.equals(DESEDE) && keyValueToCheck.length == 24) {
                        keyValue = keyValueToCheck;
                        Log.d(log, "keyValue : " + new String(keyValue));
                    } else {
                        if (algorithm.equals(DESEDE)) {
                            Log.d(log, " password length " + keyValueToCheck.length);
                            Toast.makeText(getActivity(), "Use standard DESede key value " + new String(standardKeyValue24), Toast.LENGTH_SHORT).show();
                            //lookKeyEditText.setText(new String(standardKeyValue24));   <=> keyValueToCheck = standardKeyValue24;
                            keyValueToCheck = standardKeyValue24;
                            keyValue = standardKeyValue24;
                        } else {
                            Log.d(log, " password length " + keyValueToCheck.length);
                            Toast.makeText(getActivity(), "Use standard key value " + new String(standardKeyValue), Toast.LENGTH_SHORT).show();
                            //lookKeyEditText.setText(new String(standardKeyValue));
                            keyValueToCheck = standardKeyValue;
                            keyValue = standardKeyValue;
                        }

                    }
                    Key key = new SecretKeySpec(keyValue, algorithm);

                    if (methodFlag) { //encrypt
                        try {
                            ecipher = Cipher.getInstance(algorithm);         // DES/CBC/PKCS5Padding
                            ecipher.init(Cipher.ENCRYPT_MODE, key);            //  AES/ECB/PKCS5Padding
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (NoSuchPaddingException e) {
                            e.printStackTrace();
                        } catch (InvalidKeyException e) {
                            e.printStackTrace();
                        }

                        Log.d(log, "TEXT1: " + text);
                        try {
                            input = text.getBytes("UTF8");

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        Log.d(log, "char mus: " + new String(input));//,"UTF-8"));
                        try {
                            byte[] boxCharMus = ecipher.doFinal(input);
                            text2 = android.util.Base64.encodeToString(boxCharMus, android.util.Base64.DEFAULT);
                            Log.d(log, "TEXT2: " + text2);
                        } catch (IllegalBlockSizeException e) {
                            e.printStackTrace();
                            Log.d(log, "IllegalBlockSizeException  " + e.toString());
                        } catch (BadPaddingException e) {
                            e.printStackTrace();
                            Log.d(log, "BadPaddingException " + e.toString());
                        }

                        resultTextView.setText(text2);


                    } else {    //decrypt
                        try {
                            dcipher = Cipher.getInstance(algorithm);         // DES/CBC/PKCS5Padding
                            dcipher.init(Cipher.DECRYPT_MODE, key);            //  AES/ECB/PKCS5Padding
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (NoSuchPaddingException e) {
                            e.printStackTrace();
                        } catch (InvalidKeyException e) {
                            e.printStackTrace();
                        }




                        try {
                            byte[] decryptMus = android.util.Base64.decode(text, android.util.Base64.DEFAULT);
                            Log.d(log, "decryptMus simple: " + new String(decryptMus));
                            byte[] boxCharMus1 = dcipher.doFinal(decryptMus);
                            text3 = new String(boxCharMus1, "UTF8");
                            Log.d(log, "TEXT3: " + text3);
                            error = true;

                        } catch (IllegalBlockSizeException e) {
                            Log.d(log, "TEXT3:IllegalBlockSizeException " + e.toString());
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "Wrong info", Toast.LENGTH_SHORT).show();
                            error = false;
                        } catch (BadPaddingException e) {
                            Log.d(log, "TEXT3:BadPaddingException " + e.toString());
                            Toast.makeText(getActivity(), "Wrong key/password", Toast.LENGTH_SHORT).show();
                            error = false;
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            Log.d(log, "TEXT3:UnsupportedEncodingException " + e.toString());
                            Toast.makeText(getActivity(), "Error:UnsupportedEncoding", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                            error = false;
                        }   catch (Exception e) {
                            Log.d(log, "!!!Exception!!! " + e.toString());
                            Toast.makeText(getActivity(), "Can't Decrypt this text", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                            error = false;
                        }

                        if (error) {
                            resultTextView.setText(text3);
                            Log.d(log, "result(error? - ok): " + error);
                        } else {
                            resultTextView.setText("");
                            Log.d(log, "result(error? - bad): " + error);
                        }


                    }
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putBoolean(METHOD_FLAG, methodFlag);
        savedInstanceState.putByteArray(KEY_VALUE,keyValue);
        savedInstanceState.putByteArray(KEY_VALUE_TO_CHECK,keyValueToCheck);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
       }

    private SharedPreferences getSharedPreferences(String name, int mode){
             return getActivity().getSharedPreferences(name,mode);
    }

    private void initializationVariables(){
        aes = true;
        blow = false;
        des = false;
        algorithm = "AES";
        standardKeyValue = new byte[]{'0', '2', 'd', 'c', '5', '6', (byte) 'ф', (byte) 'і', '9', '1', '2', '3', '4', '5', '6', '7'};
        standardKeyValue24 = new byte[]{'0', '2', 'd', 'c', '5', '6', (byte) 'ф', (byte) 'і', '9', '1', '2', '3', '4', '5', '6', '7', '9', '1', '2', '3', '4', '5', '6', '7'};
        error = true;
        methodFlag = true;
        keyValueToCheck=standardKeyValue;
        encryptMethods = getResources().getStringArray(R.array.encrypt_methods);
        encryptMethodsValue = getResources().getStringArray(R.array.encrypt_methods_key_value);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
    }

    private void initializationMainObjects(){


        buttonSwap.setOnClickListener(this);
        Button generateButton = (Button)  getActivity().findViewById(R.id.generateButton);
        encryptEditText = (EditText)  getActivity().findViewById(R.id.encryptEditText);
        resultTextView = (TextView)  getActivity().findViewById(R.id.resultTextView);
        makeButton = (Button)  getActivity().findViewById(R.id.make);
        makeButton.setOnClickListener(this);
        Button lookKeyButton = (Button)  getActivity().findViewById(R.id.lookKeyButton);
        lookKeyButton.setOnClickListener(this);

        Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinner);
        MyAdapter adapter1 = new MyAdapter(getActivity().getApplicationContext(), R.layout.custom_spinner, encryptMethods);
        spinner.setAdapter(adapter1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        algorithm = AES;
                        aes = true;
                        blow = false;
                        des = false;
                        // Toast.makeText(getBaseContext(), "For AES key input 16 symbol  ", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        algorithm = BLOWFISH;
                        aes = false;
                        blow = true;
                        des = false;
                        // Toast.makeText(getBaseContext(), "For Blowfish key input from 1 to 16 symbol  ", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        algorithm = DESEDE;
                        aes = false;
                        blow = false;
                        des = true;
                        // Toast.makeText(getBaseContext(), "For DESede key input 24 symbol  ", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        algorithm = AES;
                        aes = true;
                        blow = false;
                        des = false;
                        // Toast.makeText(getBaseContext(), "For AES key input 16 symbol  ", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        animMove = AnimationUtils.loadAnimation(getActivity(), R.anim.move);
        animMove2 = AnimationUtils.loadAnimation(getActivity(), R.anim.move2);
        animRotate = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);



        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (algorithm.equals(AES)) {
                    //  lookKeyEditText.setText(randomString(16));
                    keyValueToCheck = randomString(16).getBytes();
                    Toast.makeText(getActivity(), "Generated key for " + algorithm + " is: " + new String(keyValueToCheck), Toast.LENGTH_SHORT).show();
                } else if (algorithm.equals(BLOWFISH)) {
                    // lookKeyEditText.setText(randomString(16));
                    keyValueToCheck = randomString(16).getBytes();
                    Toast.makeText(getActivity(), "Generated key for " + algorithm + " is: " + new String(keyValueToCheck), Toast.LENGTH_SHORT).show();
                } else if (algorithm.equals(DESEDE)) {
                    //lookKeyEditText.setText(randomString(24));
                    keyValueToCheck = randomString(24).getBytes();
                    Toast.makeText(getActivity(), "Generated key for " + algorithm + " is: " + new String(keyValueToCheck), Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(log, "Error generate button!!! " + algorithm);
                }
            }
        });
    }

    String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AB.charAt(RND.nextInt(AB.length())));
        }
        return sb.toString();
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (fileList == null) {
            Log.d(log, "No files loaded");
            dialog = builder.create();
            return dialog;
        }

        switch (id) {
            case DIALOG_LOAD_FILE:
                builder.setTitle("Choose your file");
                builder.setAdapter(adapterDialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chosenFile = fileList[which].file;
                        File sel = new File(path + "/" + chosenFile);
                        if (sel.isDirectory()) {
                            firstLvl = false;

                            // Adds chosen directory to list
                            str.add(chosenFile);
                            fileList = null;
                            path = new File(sel + "");

                            loadFileList();

//                            getActivity().removeDialog(DIALOG_LOAD_FILE);
//                            getActivity().showDialog(DIALOG_LOAD_FILE);
                            onCreateDialog(DIALOG_LOAD_FILE);
                            Log.d(log, path.getAbsolutePath());

                        }

                        // Checks if 'up' was clicked
                        else if (chosenFile.equalsIgnoreCase("up") && !sel.exists()) {

                            // present directory removed from list
                            String s = str.remove(str.size() - 1);

                            // path modified to exclude present directory
                            path = new File(path.toString().substring(0,
                                    path.toString().lastIndexOf(s)));
                            fileList = null;

                            // if there are no more directories in the list, then
                            // its the first level
                            if (str.isEmpty()) {
                                firstLvl = true;
                            }
                            loadFileList();

                            onCreateDialog(DIALOG_LOAD_FILE);
//                            getActivity().removeDialog(DIALOG_LOAD_FILE);
//                            getActivity().showDialog(DIALOG_LOAD_FILE);
                            Log.d(log, path.getAbsolutePath());

                        }
                        // File picked
                        else {
                            Toast.makeText(getActivity(), "your choice: " + path + "/" + chosenFile, Toast.LENGTH_LONG).show();
                            readText = getStringFromFile(path.toString() + "/" + chosenFile);
                            encryptEditText.setText(readText);
                        }

                    }
                });
                break;
        }
        dialog = builder.show();
        return dialog;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(log, "onCreateOptionsMenu 1 " );
        inflater.inflate(R.menu.main_menu,menu);
        inOut = menu.findItem(R.id.logoutDropbox);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        accessToken = prefs.getString(ACCESS_TOKEN, null);
        if (accessToken == null){
            autentif=false;
            inOut.setTitle("Login Dropbox");
            Log.d(log, "onCreateOptionsMenu 2 " );
        }  else {
            session.setOAuth2AccessToken(accessToken);
            autentif=true;
            inOut.setTitle("Logout Dropbox");
            Log.d(log, "onCreateOptionsMenu - " );
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.copy:
                copy();
                break;
            case R.id.paste:
                past();
                break;
            case R.id.clear:
                encryptEditText.setText("");
                resultTextView.setText("");
                break;
            case R.id.save:
                if (resultTextView.getText().toString().length() > 0) {
                    Toast.makeText(getActivity(), "save result to file", Toast.LENGTH_SHORT).show();
                    CustomDialogClass cdd = new CustomDialogClass(getActivity(), resultTextView.getText().toString());
                    cdd.show();
                } else {
                    Toast.makeText(getActivity(), "Nothing to save", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.read:
                onCreateDialog(DIALOG_LOAD_FILE);
                break;
            case R.id.saveToDropbox:
                if (resultTextView.getText().toString().length() > 0) {
                    if (autentif){
                        CustomDialogClass cdd = new CustomDialogClass(getActivity(), resultTextView.getText().toString(), 1, mDBApi);
                        cdd.show();
                    }  else {
                        Toast.makeText(getActivity(), "Login To Dropbox first", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Nothing to save", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.readDropbox:
                if (autentif){
                    CustomDropBoxDialogClass cdd1 = new CustomDropBoxDialogClass(getActivity(), mDBApi, encryptEditText);
                    cdd1.show();
                }  else {
                    Toast.makeText(getActivity(), "Login To Dropbox first", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.logoutDropbox:
                logInOutDropbox();
                break;

            case R.id.keyToClipboard:
                saveKey();
                break;

            default:
                return super.onOptionsItemSelected(item);

        }
        return super.onOptionsItemSelected(item);
    }

    private void logInOutDropbox(){
        SharedPreferences prefs;
        if (autentif){
            if (mDBApi != null){
                mDBApi.getSession().unlink();
                prefs = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor edit = prefs.edit();
                edit.clear();
                edit.commit();
                autentif=false;
                inOut.setTitle("Login Dropbox");
                Log.d(log, "logInOutDropbox 6 " );
            }

        }  else {
            Log.d(log, "logInOutDropbox 3 " );
            mDBApi = new DropboxAPI<AndroidAuthSession>(session);
//            prefs = getSharedPreferences(PREFS_NAME, 0);
//            accessToken = prefs.getString(ACCESS_TOKEN, null);
//            if (accessToken != null) {
//                session.setOAuth2AccessToken(accessToken);
//                autentif=true;
//                inOut.setTitle("Logout Dropbox");
//            } else {
                mDBApi.getSession().startOAuth2Authentication(getActivity());
//            }
        }
    }

    private void lookKey(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.alert_dialog_input_key, null);
        EditText alertDialogEditTextInputKey = (EditText) view.findViewById(R.id.alertDialogInputKey);
        if (keyValueToCheck != null && keyValueToCheck.length>0){
            alertDialogEditTextInputKey.setText(new String(keyValueToCheck));
        }

        builder.setView(view)
                //new AlertDialog.Builder(this)
                .setTitle("Input key:")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText edit = (EditText) ((AlertDialog) dialog).findViewById(R.id.alertDialogInputKey);
                        if (edit.getText().toString().length()>0){
                            keyValueToCheck=edit.getText().toString().getBytes();
                            Toast.makeText(getActivity(), "key is: "+ edit.getText().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "Cancel", Toast.LENGTH_SHORT).show();
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void swap(){
        buttonFirstMethod.startAnimation(animMove);
        buttonSecondMethod.startAnimation(animMove2);
        buttonSwap.startAnimation(animRotate);
        if (methodFlag) {
            buttonFirstMethod.setText("Decrypt");
            buttonSecondMethod.setText("Encrypt");
            makeButton.setText("Decrypt");
            methodFlag = false;
        } else {
            buttonFirstMethod.setText("Encrypt");
            buttonSecondMethod.setText("Decrypt");
            makeButton.setText("Encrypt");
            methodFlag = true;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void copy(){
        View viewContainer = getActivity().getCurrentFocus();
        Log.d(log, "getCurrentFocus(), class name of object: " + viewContainer.getClass().getName());
        EditText editText = null;
        TextView textView = null;
        String copyText = "";
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (viewContainer instanceof EditText) {
            editText = (EditText) viewContainer;
            if (editText.getText().toString().length() > 0) {
                copyText = editText.getText().toString();
                if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(copyText);
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("text label", copyText);
                    clipboard.setPrimaryClip(clip);
                }
            } else {
                Toast.makeText(getActivity(), "Nothing to copy", Toast.LENGTH_SHORT).show();
            }
        } else if (viewContainer instanceof TextView) {
            textView = (TextView) viewContainer;
            if (textView.getText().toString().length() > 0) {
                copyText = textView.getText().toString();
                if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(copyText);
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("text label", copyText);
                    clipboard.setPrimaryClip(clip);
                }
            } else {
                Toast.makeText(getActivity(), "Nothing to copy", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Chose text to copy", Toast.LENGTH_SHORT).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void past(){
        View viewContainer1 = getActivity().getCurrentFocus();
        Log.d(log, "getCurrentFocus(), class name of object: " + viewContainer1.getClass().getName());
        EditText editText1 = null;
        TextView textView1 = null;
        int sdk1 = android.os.Build.VERSION.SDK_INT;
        if (viewContainer1 instanceof EditText) {
            editText1 = (EditText) viewContainer1;
            if (sdk1 < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                editText1.setText(clipboard.getText());
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = clipboard.getPrimaryClip();
                editText1.setText(clip.getItemAt(0).getText());      //exception???
            }
        } else if (viewContainer1 instanceof TextView) {
            textView1 = (TextView) viewContainer1;
            if (sdk1 < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                textView1.setText(clipboard.getText());
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = clipboard.getPrimaryClip();
                textView1.setText(clip.getItemAt(0).getText());      //exception???
            }
        } else {
            Toast.makeText(getActivity(), "Chose area to past", Toast.LENGTH_SHORT).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void saveKey(){
        int sdk2 = android.os.Build.VERSION.SDK_INT;
        if (keyValue != null && keyValue.length>0){
            String key = new String(keyValue);
            if (sdk2 < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(key);
                Toast.makeText(getActivity(), "save key:"+key, Toast.LENGTH_SHORT).show();
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("text label", key);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity(), "key copied:"+key, Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(getActivity(), "key is not input", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadFileList() {
        try {
            path.mkdirs();
        } catch (SecurityException e) {
            Log.d(log, "unable to write on the sd card ");
        }

        // Checks whether path exists
        if (path.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    // Filters based on whether the file is hidden or not
                    return (sel.isFile() || sel.isDirectory())
                            && !sel.isHidden();

                }
            };

            String[] fList = path.list(filter);
            fileList = new Item[fList.length];
            for (int i = 0; i < fList.length; i++) {
                fileList[i] = new Item(fList[i], R.drawable.file_icon);

                // Convert into file path
                File sel = new File(path, fList[i]);

                // Set drawables
                if (sel.isDirectory()) {
                    fileList[i].icon = R.drawable.directory_icon;
                    Log.d("DIRECTORY", fileList[i].file);
                } else {
                    Log.d("FILE", fileList[i].file);
                }
            }

            if (!firstLvl) {
                Item temp[] = new Item[fileList.length + 1];
                for (int i = 0; i < fileList.length; i++) {
                    temp[i + 1] = fileList[i];
                }
                temp[0] = new Item("Up", R.drawable.directory_up);
                fileList = temp;
            }
        } else {
            Log.d(log, "path does not exist");
        }

        adapterDialog = new ArrayAdapter<Item>(getActivity(), android.R.layout.select_dialog_item, android.R.id.text1, fileList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // creates view
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view
                        .findViewById(android.R.id.text1);

                // put the image on the text view
                textView.setCompoundDrawablesWithIntrinsicBounds(
                        fileList[position].icon, 0, 0, 0);

                // add margin between image and text (support various screen
                // densities)
                int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                textView.setCompoundDrawablePadding(dp5);

                return view;
            }
        };

    }

    public String getStringFromFile(String filePath) {
        File fl = new File(filePath);
        FileInputStream fin = null;
        String ret = "";
        StringBuilder sb = new StringBuilder();
        String line = null;

        try {
            fin = new FileInputStream(fl);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            ret = sb.toString();
            Log.d(log, "getStringFromFile() ret: " + ret);
            reader.close();
            fin.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private class Item {
        public String file;
        public int icon;

        public Item(String file, Integer icon) {
            this.file = file;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return file;
        }
    }

    public class MyAdapter extends ArrayAdapter<String> {

        public MyAdapter(Context ctx, int txtViewResourceId, String[] objects) {
            super(ctx, txtViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, View cnvtView, ViewGroup prnt) {
            return getCustomView(position, cnvtView, prnt);
        }

        @Override
        public View getView(int pos, View cnvtView, ViewGroup prnt) {
            View mainView = getCustomView(pos, cnvtView, prnt);
            mainView.findViewById(R.id.left_pic).setVisibility(View.INVISIBLE);
            return mainView;

        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View mySpinner = inflater.inflate(R.layout.custom_spinner, parent, false);

            TextView main_text = (TextView) mySpinner.findViewById(R.id.text_main_seen);

            main_text.setText(encryptMethods[position]);


            TextView subSpinner = (TextView) mySpinner.findViewById(R.id.sub_text_seen);
            subSpinner.setText(encryptMethodsValue[position]);

            ImageView left_icon = (ImageView) mySpinner.findViewById(R.id.left_pic);
            left_icon.setVisibility(View.VISIBLE);
            int iconPosition[] = new int[3];
            if (aes) {
                iconPosition[0] = R.drawable.green_point;
                iconPosition[1] = R.drawable.states_compound;
                iconPosition[2] = R.drawable.states_compound;
            } else if (blow) {
                iconPosition[0] = R.drawable.states_compound;
                iconPosition[1] = R.drawable.green_point;
                iconPosition[2] = R.drawable.states_compound;
            } else {
                iconPosition[0] = R.drawable.states_compound;
                iconPosition[1] = R.drawable.states_compound;
                iconPosition[2] = R.drawable.green_point;
            }
            left_icon.setImageResource(iconPosition[position]);
            return mySpinner;
        }
    }
}
