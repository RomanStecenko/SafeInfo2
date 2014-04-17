package com.example.SafeInfo2;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
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
import com.dropbox.client2.session.Session;

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
import java.util.Random;

public class MainActivity extends ActionBarActivity {
    private String[] mDrawerTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    ArrayAdapter adapter;
    private ActionBarDrawerToggle mDrawerToggle;
    Button buttonSwap, buttonFirstMethod, buttonSecondMethod, makeButton, generateButton, okButton, lookKeyButton;
    Animation animMove, animMove2, animRotate;
    boolean methodFlag;
    String encryptMethods[], encryptMethodsValue[];
    EditText lookKeyEditText, encryptEditText;
    TextView resultTextView;

    String text, text2, text3, readText;
    byte[] input, decryptMus, keyValue, standardKeyValue24, standardKeyValue, boxCharMus, boxCharMus1;
    Cipher ecipher, dcipher;
    String algorithm;
    Spinner spinner;
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static Random rnd = new Random();

    public static final String log = "mylog";
    public static final String PREFS_NAME = "SherPref";
    public static final String AES = "AES";
    public static final String BLOWFISH = "Blowfish";
    public static final String DESEDE = "DESede";
    public static final String DIR_FILES = "SafeInfoFiles";
    boolean aes, blow, des, error;

    ArrayList<String> str = new ArrayList<String>();
    // Check if the first level of the directory structure is the one showing
    private Boolean firstLvl = true;
    private static final String TAG = "F_PATH";
    private Item[] fileList;
    private File path = new File(Environment.getExternalStorageDirectory() + "");
    private File sdPath;
    private String chosenFile;
    private static final int DIALOG_LOAD_FILE = 1000;
    ListAdapter adapterDialog;

    final static private String APP_KEY = "kmm0ovc79qua3q9";
    final static private String APP_SECRET = "ynjvf1poc5zbpw4";
    final static private Session.AccessType ACCESS_TYPE = Session.AccessType.APP_FOLDER;
    final static private String ACCESS_TOKEN = "access_token";
    // In the class declaration section:
    private DropboxAPI<AndroidAuthSession> mDBApi;
    // And later in some initialization function:
    AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
    AndroidAuthSession session = new AndroidAuthSession(appKeys);  //, ACCESS_TYPE
    public SharedPreferences prefs;
    public String accessToken;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.d(log, "SD-карта не доступна: " + Environment.getExternalStorageState());
        } else {
            // получаем путь к SD
            sdPath = Environment.getExternalStorageDirectory();
            // добавляем свой каталог к пути
            sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_FILES);
            // создаем каталог
            boolean success = false;
            if (!sdPath.exists()) {
                success = sdPath.mkdirs();
                Log.d(log, " success: " + success);
            }
            if (success) {
                Log.d(log, " success true: directory is created");
            } else {
                Log.d(log, " success false: directory is not created or is already exist. is it dir? " + sdPath.isDirectory());
            }

        }

//        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
//
//        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
//        String accessToken = prefs.getString(ACCESS_TOKEN, null);
//        if (accessToken != null) {
//            session.setOAuth2AccessToken(accessToken);
//        } else {
//            mDBApi.getSession().startOAuth2Authentication(MainActivity.this);
//        }

        loadFileList();

        mDrawerTitles = getResources().getStringArray(R.array.mdrawer_titles);
        mTitle = mDrawerTitle = getTitle();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDrawerTitles);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList.setAdapter(adapter);


        final ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);
        final LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
        View view = inflater.inflate(R.layout.action_bar_edit_mode, null);
        ab.setCustomView(view);
        ab.setDisplayShowCustomEnabled(true);
        buttonFirstMethod = (Button) findViewById(R.id.action_bar_firstMethod);
        buttonFirstMethod.setVisibility(View.VISIBLE);
        buttonFirstMethod.setBackgroundColor(Color.TRANSPARENT);
        buttonSecondMethod = (Button) findViewById(R.id.action_bar_secondMethod);
        buttonSecondMethod.setVisibility(View.VISIBLE);
        buttonSecondMethod.setBackgroundColor(Color.TRANSPARENT);
        buttonSwap = (Button) findViewById(R.id.action_bar_swap);
        makeButton = (Button) findViewById(R.id.make);
        generateButton = (Button) findViewById(R.id.generateButton);
        lookKeyEditText = (EditText) findViewById(R.id.lookKeyEditText);
        encryptEditText = (EditText) findViewById(R.id.encryptEditText);
        okButton = (Button) findViewById(R.id.okButton);
        lookKeyButton = (Button) findViewById(R.id.lookKeyButton);
        resultTextView = (TextView) findViewById(R.id.resultTextView);


        animMove = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move);
        animMove2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move2);
        animRotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(false);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);


        aes = true;
        blow = false;
        des = false;
        algorithm = "AES";
        standardKeyValue = new byte[]{'0', '2', 'd', 'c', '5', '6', (byte) 'ф', (byte) 'і', '9', '1', '2', '3', '4', '5', '6', '7'};
        standardKeyValue24 = new byte[]{'0', '2', 'd', 'c', '5', '6', (byte) 'ф', (byte) 'і', '9', '1', '2', '3', '4', '5', '6', '7', '9', '1', '2', '3', '4', '5', '6', '7'};
        error = true;

        lookKeyEditText.setText(new String(standardKeyValue));

        encryptMethods = getResources().getStringArray(R.array.encrypt_methods);
        encryptMethodsValue = getResources().getStringArray(R.array.encrypt_methods_key_value);

        spinner = (Spinner) findViewById(R.id.spinner);
        MyAdapter adapter1 = new MyAdapter(this, R.layout.custom_spinner, encryptMethods);
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
                        Toast.makeText(getBaseContext(), "For AES key input 16 symbol  ", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        algorithm = BLOWFISH;
                        aes = false;
                        blow = true;
                        des = false;
                        Toast.makeText(getBaseContext(), "For Blowfish key input from 1 to 16 symbol  ", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        algorithm = DESEDE;
                        aes = false;
                        blow = false;
                        des = true;
                        Toast.makeText(getBaseContext(), "For DESede key input 24 symbol  ", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        algorithm = AES;
                        aes = true;
                        blow = false;
                        des = false;
                        Toast.makeText(getBaseContext(), "For AES key input 16 symbol  ", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });


        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "hi+ " + position, Toast.LENGTH_SHORT).show();
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        methodFlag = true;

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (algorithm.equals(AES)) {
                    lookKeyEditText.setText(randomString(16));
                } else if (algorithm.equals(BLOWFISH)) {
                    lookKeyEditText.setText(randomString(16));
                } else if (algorithm.equals(DESEDE)) {
                    lookKeyEditText.setText(randomString(24));
                } else {
                    Log.d(log, "Error generate button!!! " + algorithm);
                }
            }
        });

    }

    String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_bar_firstMethod:
                Toast.makeText(getApplicationContext(), "first", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_bar_swap:
                Toast.makeText(getApplicationContext(), "swap ", Toast.LENGTH_SHORT).show();
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
                break;
            case R.id.action_bar_secondMethod:
                Toast.makeText(getApplicationContext(), "second", Toast.LENGTH_SHORT).show();
                break;
            case R.id.lookKeyButton:
                lookKeyButton.setVisibility(View.INVISIBLE);
                lookKeyEditText.setVisibility(View.VISIBLE);
                okButton.setVisibility(View.VISIBLE);
                break;
            case R.id.okButton:
                lookKeyButton.setVisibility(View.VISIBLE);
                lookKeyEditText.setVisibility(View.INVISIBLE);
                okButton.setVisibility(View.INVISIBLE);
                break;
            case R.id.make:
                // Toast.makeText(getApplicationContext(), "make", Toast.LENGTH_SHORT).show();
                text = encryptEditText.getText().toString();
                Log.d(log, "TEXT length: " + text.length());
                if (text.length() <= 0) {
                    Toast.makeText(getApplicationContext(), "Input your text first", Toast.LENGTH_SHORT).show();
                } else {

                    if (algorithm.equals(AES) && lookKeyEditText.getText().toString().length() == 16) {
                        keyValue = lookKeyEditText.getText().toString().getBytes();
                        Log.d(log, "keyValue : " + new String(keyValue));
                    } else if (algorithm.equals(BLOWFISH) && lookKeyEditText.getText().toString().length() <= 16 && lookKeyEditText.getText().toString().length() > 0) {
                        keyValue = lookKeyEditText.getText().toString().getBytes();
                        Log.d(log, "keyValue : " + new String(keyValue));
                    } else if (algorithm.equals(DESEDE) && lookKeyEditText.getText().toString().length() == 24) {
                        keyValue = lookKeyEditText.getText().toString().getBytes();
                        Log.d(log, "keyValue : " + new String(keyValue));
                    } else {
                        if (algorithm.equals(DESEDE)) {
                            Log.d(log, " password length " + lookKeyEditText.getText().toString().length());
                            Toast.makeText(getApplicationContext(), "Use standard DESede key value " + new String(standardKeyValue24), Toast.LENGTH_SHORT).show();
                            lookKeyEditText.setText(new String(standardKeyValue24));
                            keyValue = standardKeyValue24;
                        } else {
                            Log.d(log, " password length " + lookKeyEditText.getText().toString().length());
                            Toast.makeText(getApplicationContext(), "Use standard key value " + new String(standardKeyValue), Toast.LENGTH_SHORT).show();
                            lookKeyEditText.setText(new String(standardKeyValue));
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
                            boxCharMus = ecipher.doFinal(input);
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
                        decryptMus = android.util.Base64.decode(text, android.util.Base64.DEFAULT);
                        Log.d(log, "decryptMus simple: " + new String(decryptMus));

                        try {
                            boxCharMus1 = dcipher.doFinal(decryptMus);
                            text3 = new String(boxCharMus1, "UTF8");
                            Log.d(log, "TEXT3: " + text3);
                            error = true;

                        } catch (IllegalBlockSizeException e) {
                            Log.d(log, "TEXT3:IllegalBlockSizeException " + e.toString());
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Wrong info", Toast.LENGTH_SHORT).show();
                            error = false;
                        } catch (BadPaddingException e) {
                            Log.d(log, "TEXT3:BadPaddingException " + e.toString());
                            Toast.makeText(getApplicationContext(), "Wrong key/password", Toast.LENGTH_SHORT).show();
                            error = false;
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            Log.d(log, "TEXT3:UnsupportedEncodingException " + e.toString());
                            Toast.makeText(getApplicationContext(), "Error:UnsupportedEncoding", Toast.LENGTH_SHORT).show();
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
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...
        switch (item.getItemId()) {
            case R.id.copy:
                //Toast.makeText(getApplicationContext(), "copy", Toast.LENGTH_SHORT).show();
                View viewContainer = getCurrentFocus();
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
                            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboard.setText(copyText);
                        } else {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("text label", copyText);
                            clipboard.setPrimaryClip(clip);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Nothing to copy", Toast.LENGTH_SHORT).show();
                    }
                } else if (viewContainer instanceof TextView) {
                    textView = (TextView) viewContainer;
                    if (textView.getText().toString().length() > 0) {
                        copyText = textView.getText().toString();
                        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboard.setText(copyText);
                        } else {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("text label", copyText);
                            clipboard.setPrimaryClip(clip);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Nothing to copy", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Chose text to copy", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.paste:
                //Toast.makeText(getApplicationContext(), "paste", Toast.LENGTH_SHORT).show();
                View viewContainer1 = getCurrentFocus();
                Log.d(log, "getCurrentFocus(), class name of object: " + viewContainer1.getClass().getName());
                EditText editText1 = null;
                TextView textView1 = null;
                int sdk1 = android.os.Build.VERSION.SDK_INT;
                if (viewContainer1 instanceof EditText) {
                    editText1 = (EditText) viewContainer1;
                    if (sdk1 < android.os.Build.VERSION_CODES.HONEYCOMB) {
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        editText1.setText(clipboard.getText());
                    } else {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = clipboard.getPrimaryClip();
                        editText1.setText(clip.getItemAt(0).getText());      //exception???
                    }
                } else if (viewContainer1 instanceof TextView) {
                    textView1 = (TextView) viewContainer1;
                    if (sdk1 < android.os.Build.VERSION_CODES.HONEYCOMB) {
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        textView1.setText(clipboard.getText());
                    } else {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = clipboard.getPrimaryClip();
                        textView1.setText(clip.getItemAt(0).getText());      //exception???
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Chose area to past", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.clear:
                Toast.makeText(getApplicationContext(), "clear", Toast.LENGTH_SHORT).show();
                encryptEditText.setText("");
                resultTextView.setText("");
                break;
            case R.id.save:
                Toast.makeText(getApplicationContext(), "save result to file", Toast.LENGTH_SHORT).show();
                if (resultTextView.getText().toString().length() > 0) {
                    CustomDialogClass cdd = new CustomDialogClass(MainActivity.this, resultTextView.getText().toString());
                    cdd.show();
                } else {
                    Toast.makeText(getApplicationContext(), "Nothing to save", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.read:
                showDialog(DIALOG_LOAD_FILE);
                Toast.makeText(getApplicationContext(), "read file", Toast.LENGTH_SHORT).show();
                break;
            case R.id.saveToDropbox:
                if (resultTextView.getText().toString().length() > 0) {
                    mDBApi = new DropboxAPI<AndroidAuthSession>(session);

                    prefs = getSharedPreferences(PREFS_NAME, 0);
                    accessToken = prefs.getString(ACCESS_TOKEN, null);
                    if (accessToken != null) {
                        session.setOAuth2AccessToken(accessToken);
                    } else {
                        mDBApi.getSession().startOAuth2Authentication(MainActivity.this);
                    }
                    CustomDialogClass cdd = new CustomDialogClass(MainActivity.this, resultTextView.getText().toString(), 1, mDBApi);
                    cdd.show();
                } else {
                    Toast.makeText(getApplicationContext(), "Nothing to save", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(getApplicationContext(), "save To Dropbox", Toast.LENGTH_SHORT).show();
                break;
            case R.id.readDropbox:

                mDBApi = new DropboxAPI<AndroidAuthSession>(session);

                prefs = getSharedPreferences(PREFS_NAME, 0);
                accessToken = prefs.getString(ACCESS_TOKEN, null);
                if (accessToken != null) {
                    session.setOAuth2AccessToken(accessToken);
                } else {
                    mDBApi.getSession().startOAuth2Authentication(MainActivity.this);
                }
                CustomDropBoxDialogClass cdd1 = new CustomDropBoxDialogClass(MainActivity.this, mDBApi, encryptEditText);
                cdd1.show();
                Toast.makeText(getApplicationContext(), "read from Dropbox", Toast.LENGTH_SHORT).show();
                break;
            case R.id.logoutDropbox:
                Toast.makeText(getApplicationContext(), "logout Dropbox", Toast.LENGTH_SHORT).show();
                if (mDBApi != null) {
                    mDBApi.getSession().unlink();
                    //mDBApi.getSession().finishAuthentication();
                    prefs = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.clear();
                    edit.commit();
                    Log.d(log, "Logout dropbox! accessToken is null ");
                }
                break;
            default:
                return super.onOptionsItemSelected(item);

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (fileList == null) {
            Log.e(TAG, "No files loaded");
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

                            removeDialog(DIALOG_LOAD_FILE);
                            showDialog(DIALOG_LOAD_FILE);
                            Log.d(TAG, path.getAbsolutePath());

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

                            removeDialog(DIALOG_LOAD_FILE);
                            showDialog(DIALOG_LOAD_FILE);
                            Log.d(TAG, path.getAbsolutePath());

                        }
                        // File picked
                        else {
                            // Perform action with file picked
                            Toast.makeText(getApplicationContext(), "your choice: " + path + "/" + chosenFile, Toast.LENGTH_LONG).show();

                            readText = getStringFromFile(path.toString() + "/" + chosenFile);
                            encryptEditText.setText(readText);
                            //setTextToCurrentView(readText);
//                            View viewContainer2 = getCurrentFocus();
//                            Log.d(log, "getCurrentFocus(),Dialog onCreateDialog, class name of object: " + viewContainer2.getClass().getName());
//                            EditText readEditText = null;
//                            TextView readTextView = null;
//                            if (viewContainer2 instanceof EditText) {
//                                readEditText = (EditText) viewContainer2;
//                                readEditText.setText(readText);
//                            }
//                            if (viewContainer2 instanceof TextView) {
//                                readTextView = (TextView) viewContainer2;
//                                readTextView.setText(readText);
//                            }
                        }

                    }
                });
                break;
        }
        dialog = builder.show();
        return dialog;
    }

//    void setTextToCurrentView(String text) {
//        View viewContainer2 = getCurrentFocus();
//        Log.d(log, "setTextToCurrentView(), class name of object: " + viewContainer2.getClass().getName()
//                + "\n text: " + text);
//        EditText readEditText = null;
//        TextView readTextView = null;
//        if (viewContainer2 instanceof EditText) {
//            readEditText = (EditText) viewContainer2;
//            readEditText.setText(text);
//        }
//        if (viewContainer2 instanceof TextView) {
//            readTextView = (TextView) viewContainer2;
//            readTextView.setText(text);
//        }
//    }


    @Override
    protected void onResume() {
        //Log.d(log, "onResume()  showHideFlag: " + showHideFlag);
//        if (showHideFlag) {
//            // checkBox.setChecked(true);
//            passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
//        } else {
//            passwordEditText.setTransformationMethod(null);
//            // checkBox.setChecked(false);
//        }
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
                    Log.d(log, "accessToken= " + accessToken);
                } catch (IllegalStateException e) {
                    Log.d(log, "DbAuthLog Error authenticating: " + e.toString());
                }
            }
        }
        super.onResume();
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
            LayoutInflater inflater = getLayoutInflater();
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        //savedInstanceState.putBoolean(STATE_FLAG, showHideFlag);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //showHideFlag = savedInstanceState.getBoolean(STATE_FLAG);
        super.onRestoreInstanceState(savedInstanceState);
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

    private void loadFileList() {
        try {
            path.mkdirs();
        } catch (SecurityException e) {
            Log.e(TAG, "unable to write on the sd card ");
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
            Log.e(TAG, "path does not exist");
        }

        adapterDialog = new ArrayAdapter<Item>(this, android.R.layout.select_dialog_item, android.R.id.text1, fileList) {
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

}
