package com.codeoregonapp.patrickleonard.ribbit.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.codeoregonapp.patrickleonard.ribbit.adapters.UserAdapter;
import com.codeoregonapp.patrickleonard.ribbit.utils.ParseConstants;
import com.codeoregonapp.patrickleonard.ribbit.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;


import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Patrick Leonard 11-14-2015
 */
public class EditFriendsActivity extends Activity {

    public static final String TAG = EditFriendsActivity.class.getSimpleName();
    protected List<ParseUser> mParseUsers;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;

    @Bind(R.id.friends_grid)  GridView mGridView;
    @Bind(android.R.id.empty) TextView mEmptyTextView;

    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_grid);
        ButterKnife.bind(this);
        TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);
        mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
        setProgressBarIndeterminateVisibility(true);
        setProgressBarVisibility(true);
        ParseQuery<ParseUser> userParseQuery = ParseUser.getQuery();
        userParseQuery.orderByAscending(ParseConstants.KEY_USERNAME);
        userParseQuery.setLimit(1000);

        userParseQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                setProgressBarVisibility(false);
                if (e == null) {
                    mParseUsers = parseUsers;
                    if(mGridView.getAdapter() == null) {
                        UserAdapter adapter = new UserAdapter(EditFriendsActivity.this,mParseUsers);
                        setupGridView(adapter);
                    }
                    else
                    {
                        ((UserAdapter)(mGridView.getAdapter())).refill(mParseUsers);
                    }
                    addFriendCheckMarks();
                } else {
                    Log.e(EditFriendsActivity.TAG, e.getMessage(), e);
                    MainActivity.errorDialogDisplay(EditFriendsActivity.this, "Error finding friends");
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

                ImageView checkImageView = (ImageView) view.findViewById(R.id.checkImageView);

                if (mGridView.isItemChecked(position)) {
                    mFriendsRelation.add(mParseUsers.get(position));
                    checkImageView.setVisibility(View.VISIBLE);
                } else {
                    mFriendsRelation.remove(mParseUsers.get(position));
                    checkImageView.setVisibility(View.INVISIBLE);
                }
                setProgressBarIndeterminateVisibility(true);
                mCurrentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        setProgressBarIndeterminateVisibility(false);
                        if (e != null) {
                            Log.e(EditFriendsActivity.TAG, e.getMessage(), e);
                            MainActivity.errorDialogDisplay(EditFriendsActivity.this, e.getMessage());
                        }
                    }
                });
            }
        });
    }

    private void addFriendCheckMarks() {
        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                if(e == null) {
                    for(int i=0;i<mParseUsers.size();++i) {
                        ParseUser parseUser = mParseUsers.get(i);

                        for(ParseUser friend: friends) {
                            if(friend.getObjectId().equals(parseUser.getObjectId())) {
                                mGridView.setItemChecked(i,true);
                            }
                        }
                    }
                }
                else {
                    Log.e(EditFriendsActivity.TAG, e.getMessage(), e);
                    MainActivity.errorDialogDisplay(EditFriendsActivity.this, e.getMessage());
                }
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
        if (id == R.id.action_logout) {
            ParseUser.logOut();
            MainActivity.NavigateToLogin(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return true;
    }

}
