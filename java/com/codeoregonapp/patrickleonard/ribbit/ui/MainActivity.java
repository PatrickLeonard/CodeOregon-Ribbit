package com.codeoregonapp.patrickleonard.ribbit.ui;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;


import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.codeoregonapp.patrickleonard.ribbit.R;
import com.codeoregonapp.patrickleonard.ribbit.RibbitApplication;
import com.codeoregonapp.patrickleonard.ribbit.adapters.SectionsPagerAdapter;
import com.codeoregonapp.patrickleonard.ribbit.utils.ParseConstants;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    public static String TAG = MainActivity.class.getSimpleName();
    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int TAKE_VIDEO_REQUEST = 1;
    public static final int PICK_PHOTO_REQUEST = 2;
    public static final int PICK_VIDEO_REQUEST = 3;

    public static final int MEDIA_TYPE_IMAGE = 4;
    public static final int MEDIA_TYPE_VIDEO = 5;

    public static final int FILE_SIZE_LIMIT = 1024*1024*10; //MEGABYTES
    protected Uri mMediaURI;

    // Create the adapter that will return a fragment for each of the three
    // primary sections of the activity.
            /*
              The {@link android.support.v4.view.PagerAdapter} that will provide
              fragments for each of the sections. We use a
              {@link FragmentPagerAdapter} derivative, which will keep every
              loaded fragment in memory. If this becomes too memory intensive, it
              may be best to switch to a
              {@link android.support.v4.app.FragmentStatePagerAdapter}.
             */

    protected SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
    private ViewPager mViewPager;

    protected DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            switch(which) {
                case 0: //Take Photo
                    takePhotoSelectedAction();
                    break;
                case 1://Take Video
                    takeVideoSelectedAction();
                    break;
                case 2://Choose Photo
                    selectPhotoSelectedAction();
                    break;
                case 3://Choose Video
                    selectVideoSelectedAction();
                    break;
            }
        }

        private void takePhotoSelectedAction() {
            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mMediaURI = getOutputMediaFileUri(MainActivity.MEDIA_TYPE_IMAGE);
            if(mMediaURI == null) {
                Toast.makeText(MainActivity.this, R.string.error_external_storage, Toast.LENGTH_LONG).show();
            }
            else {
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaURI);
                startActivityForResult(takePhotoIntent, MainActivity.TAKE_PHOTO_REQUEST);
            }
        }

        private void takeVideoSelectedAction() {
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            mMediaURI = getOutputMediaFileUri(MainActivity.MEDIA_TYPE_VIDEO);
            if(mMediaURI == null) {
                Toast.makeText(MainActivity.this, R.string.error_external_storage, Toast.LENGTH_LONG).show();
            }
            else {
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaURI);
                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,10);
                takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,0);
                startActivityForResult(takeVideoIntent, MainActivity.TAKE_VIDEO_REQUEST);
            }
        }


        private void selectPhotoSelectedAction() {
            Intent selectPhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
            selectPhotoIntent.setType("image/*");
            startActivityForResult(selectPhotoIntent,MainActivity.PICK_PHOTO_REQUEST);
        }

        private void selectVideoSelectedAction() {

            Toast.makeText(MainActivity.this, R.string.video_size_error,Toast.LENGTH_LONG).show();

            Intent selectVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
            selectVideoIntent.setType("video/*");
            startActivityForResult(selectVideoIntent, MainActivity.PICK_VIDEO_REQUEST);
        }

        private Uri getOutputMediaFileUri(int mediaType) {

            if(isExternalStorageAvailable()) {

                //Get External Storage Directory
                String appName = MainActivity.this.getString(R.string.app_name);
                File mediaFileDir = new File(Environment.
                        getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),appName);
                //Create a subdirectory
                if(!mediaFileDir.exists()) {
                    if(mediaFileDir.mkdirs()) {
                        Log.e(MainActivity.TAG,"Failed to create directory");
                        return null;
                    }
                }
                //Create a file name
                File mediaFile;
                Date now = new Date();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(now);
                String path = mediaFileDir.getPath() + File.separator;

                if(mediaType == MainActivity.MEDIA_TYPE_IMAGE) {
                    mediaFile = new File(path + "IMG_" + timeStamp + ".jpg");
                }
                else if(mediaType == MainActivity.MEDIA_TYPE_VIDEO) {
                    mediaFile = new File(path + "VID_" + timeStamp + ".mp4");
                }
                else {
                    return null;
                }

                Log.d(MainActivity.TAG,"File:" + Uri.fromFile(mediaFile));

                //return the Uri
                return Uri.fromFile(mediaFile);
            }

            return null;
        }

        private boolean isExternalStorageAvailable() {
            String state = Environment.getExternalStorageState();
            return state.equals(Environment.MEDIA_MOUNTED);
        }

    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {

            if(data == null) {
                Toast.makeText(this,R.string.general_error_string,Toast.LENGTH_LONG).show();
            }
            else {
                mMediaURI = data.getData();
                if(mMediaURI == null) {
                    Toast.makeText(this,R.string.general_error_string,Toast.LENGTH_LONG).show();
                }
            }

            if(requestCode == MainActivity.PICK_PHOTO_REQUEST
                    || requestCode == MainActivity.PICK_VIDEO_REQUEST) {

                if(requestCode == MainActivity.PICK_VIDEO_REQUEST) {
                    int fileSize;
                    InputStream inputStream = null;
                    try {
                        inputStream = getContentResolver().openInputStream(mMediaURI);
                        fileSize = inputStream.available();
                    }

                    catch (FileNotFoundException e) {
                        Log.e(MainActivity.TAG,e.getMessage(),e);
                        return;
                    }
                    catch (IOException e) {
                        Log.e(MainActivity.TAG,e.getMessage(),e);
                        return;
                    }
                    finally {
                        try {
                            inputStream.close();
                        }
                        catch (IOException e) {
                            Log.e(MainActivity.TAG,e.getMessage(),e);
                        }
                    }

                    if(fileSize >= MainActivity.FILE_SIZE_LIMIT) {
                        Toast.makeText(this, R.string.file_size_too_large, Toast.LENGTH_LONG).show();
                        return;
                    }

                }

                Log.v(MainActivity.TAG,"Not a video, no need to worry about file size.");
            } else {
                //add to gallery
                Log.v(MainActivity.TAG,"Adding to gallery");
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaURI);
                sendBroadcast(mediaScanIntent);
            }

            String fileType="";
            if(requestCode == MainActivity.PICK_PHOTO_REQUEST ||
                    requestCode == MainActivity.TAKE_PHOTO_REQUEST) {
                fileType = ParseConstants.TYPE_IMAGE;
            }
            else if (requestCode == MainActivity.PICK_VIDEO_REQUEST ||
                    requestCode == MainActivity.TAKE_VIDEO_REQUEST){
                fileType = ParseConstants.TYPE_VIDEO;
            }

            Log.v(MainActivity.TAG,"Starting up RecipientsActivity");
            Intent recipientsIntent = new Intent(this, RecipientsActivity.class);
            recipientsIntent.setData(mMediaURI);
            recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE,fileType);
            startActivity(recipientsIntent);
        }
        else if(resultCode != RESULT_CANCELED) {
            Toast.makeText(this, R.string.general_error_string,Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser == null) {
            NavigateToLogin(this);
        } else {

            Log.v(MainActivity.TAG, "Current Username: " + currentUser.getUsername());
            Log.v(MainActivity.TAG, "Current User Email: " + currentUser.getEmail());
            // Set up the action bar.
            final ActionBar actionBar = getActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mSectionsPagerAdapter);

            // When swiping between different sections, select the corresponding
            // tab. We can also use ActionBar.Tab#select() to do this if we have
            // a reference to the Tab.
            mViewPager
                    .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                        @Override
                        public void onPageSelected(int position) {
                            actionBar.setSelectedNavigationItem(position);
                        }
                    });

            // For each of the sections in the app, add a tab to the action bar.
            for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
                // Create a tab with text corresponding to the page title defined by
                // the adapter. Also specify this Activity object, which implements
                // the TabListener interface, as the callback (listener) for when
                // this tab is selected.
                actionBar.addTab(actionBar.newTab()
                        .setIcon(mSectionsPagerAdapter.getIcon(i))
                        .setTabListener(this));
            }
        }
    }

    public static void NavigateToLogin(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            //noinspection SimplifiableIfStatement
            case R.id.action_logout: {
                ParseUser.logOut();
                NavigateToLogin(this);
                break;
            }
            case R.id.action_edit_friends: {
                Intent intent = new Intent(this,EditFriendsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.action_camera: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(R.array.camera_choices, mDialogListener);
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    public static void errorDialogDisplay(Context context, String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(errorMessage).setTitle(context.getString(R.string.error_title))
                .setPositiveButton(context.getString(android.R.string.ok),null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

    }
}
