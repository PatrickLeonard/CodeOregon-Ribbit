package com.codeoregonapp.patrickleonard.ribbit.ui;


import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.codeoregonapp.patrickleonard.ribbit.R;
import com.codeoregonapp.patrickleonard.ribbit.adapters.UserAdapter;
import com.codeoregonapp.patrickleonard.ribbit.utils.FileHelper;
import com.codeoregonapp.patrickleonard.ribbit.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RecipientsActivity extends Activity {

    public static final String TAG = RecipientsActivity.class.getSimpleName();
    protected List<ParseUser> mFriends;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;

    protected MenuItem mSendMenuItem;

    protected Uri mMediaUri;
    protected String mFileType;
    @Bind(R.id.friends_grid)  GridView mGridView;
    @Bind(android.R.id.empty) TextView mEmptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.user_grid);
        ButterKnife.bind(this);
        mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mFileType = getIntent().getStringExtra(ParseConstants.KEY_FILE_TYPE);
        mMediaUri = getIntent().getData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
        ParseQuery<ParseUser> query =  mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        setProgressBarIndeterminate(true);
        setProgressBarIndeterminateVisibility(true);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                setProgressBarIndeterminate(false);
                setProgressBarIndeterminateVisibility(false);
                if (e == null) {
                    mFriends = friends;
                    if(mGridView.getAdapter() == null) {
                        UserAdapter adapter = new UserAdapter(RecipientsActivity.this,mFriends);
                        setupGridView(adapter);
                    }
                    else
                    {
                        ((UserAdapter)(mGridView.getAdapter())).refill(mFriends);
                    }

                } else {
                    Log.e(RecipientsActivity.TAG, e.getMessage(), e);
                    MainActivity.errorDialogDisplay(RecipientsActivity.this, e.getMessage());
                }
            }
        });
    }

    private void setupGridView(UserAdapter userAdapter) {
        mGridView.setAdapter(userAdapter);
        mGridView.setEmptyView(mEmptyTextView);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mGridView.getCheckedItemCount() > 0) {
                    mSendMenuItem.setVisible(true);
                } else {
                    mSendMenuItem.setVisible(false);
                }

                ImageView checkImageView = (ImageView) view.findViewById(R.id.checkImageView);

                if (mGridView.isItemChecked(position)) {
                    mFriendsRelation.add(mFriends.get(position));
                    checkImageView.setVisibility(View.VISIBLE);
                } else {
                    mFriendsRelation.remove(mFriends.get(position));
                    checkImageView.setVisibility(View.INVISIBLE);
                }
                setProgressBarIndeterminateVisibility(true);
                mCurrentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        setProgressBarIndeterminateVisibility(false);
                        if (e != null) {
                            Log.e(EditFriendsActivity.TAG, e.getMessage(), e);
                            MainActivity.errorDialogDisplay(RecipientsActivity.this, e.getMessage());
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_message) {
            ParseObject message = createMessage();
            if(message == null) {
                MainActivity.errorDialogDisplay(this,getString(R.string.error_file_message));
            }
            else {
                sendMessage(message);
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recipients, menu);
        mSendMenuItem =  menu.getItem(0);
        return true;
    }

    protected ParseObject createMessage() {
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGE);

        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
        message.put(ParseConstants.KEY_FILE_TYPE,mFileType);

        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);

        if(fileBytes == null) {
            return null;
        }
        else {
            if(mFileType.equals(ParseConstants.TYPE_IMAGE)) {
                fileBytes = FileHelper.reduceImageForUpload(fileBytes);
            }

            String fileName = FileHelper.getFileName(this,mMediaUri,mFileType);

            ParseFile parseFile = new ParseFile(fileName,fileBytes);
            message.put(ParseConstants.KEY_FILE,parseFile);
            return message;
        }
    }

    protected ArrayList<String> getRecipientIds() {
        ArrayList<String> recipientIds = new ArrayList<>();

        for(int i=0;i<mGridView.getCount();++i) {
            if(mGridView.isItemChecked(i)) {
                recipientIds.add(mFriends.get(i).getObjectId());
            }
        }

        return recipientIds;
    }

    protected void sendMessage(ParseObject message) {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    Toast.makeText(RecipientsActivity.this,getString(R.string.success_message),Toast.LENGTH_LONG).show();
                    sendPushNotifications();
                }
                else {
                    MainActivity.errorDialogDisplay(RecipientsActivity.this,getString(R.string.error_message_sent));
                }

            }
        });
    }

    protected void sendPushNotifications() {
        ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
        query.whereContainedIn(ParseConstants.KEY_USER_ID,getRecipientIds());
        ParsePush push = new ParsePush();
        push.setQuery(query);
        push.setMessage(getString(R.string.push_notification_message,ParseUser.getCurrentUser().getUsername()));
        push.sendInBackground();
    }
}
