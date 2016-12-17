package com.codeoregonapp.patrickleonard.ribbit.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.codeoregonapp.patrickleonard.ribbit.R;
import com.codeoregonapp.patrickleonard.ribbit.adapters.UserAdapter;
import com.codeoregonapp.patrickleonard.ribbit.utils.ParseConstants;

import com.codeoregonapp.patrickleonard.ribbit.ui.MainActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

/**
 * Used to extend the ListFragment class
 * Was update to a Fragment to use a GridView instead of a ListView 11/19/2015
 * Created by Patrick Leonard on 11/14/2015.
 */
public class FriendsFragment extends Fragment {

    public static final String TAG = FriendsFragment.class.getSimpleName();
    protected List<ParseUser> mFriends;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;
    protected GridView mGridView;

    public FriendsFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FriendsFragment newInstance() {
        return new FriendsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_grid, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.friends_grid);
        TextView emptyTextView = (TextView) rootView.findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
        ParseQuery<ParseUser> query =  mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        getActivity().setProgressBarIndeterminateVisibility(true);
        getActivity().setProgressBarVisibility(true);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                getActivity().setProgressBarIndeterminateVisibility(false);
                getActivity().setProgressBarVisibility(false);
                if (e == null) {
                    mFriends = friends;
                    String[] friendsArray = new String[mFriends.size()];
                    int i = 0;
                    for (ParseUser friend : friends) {
                        friendsArray[i] = friend.getUsername();
                        ++i;
                    }

                    if(mGridView.getAdapter() == null) {
                        UserAdapter adapter = new UserAdapter(getContext(),mFriends);
                        mGridView.setAdapter(adapter);
                    }
                    else
                    {
                        ((UserAdapter)(mGridView.getAdapter())).refill(mFriends);
                    }


                } else {
                    Log.e(FriendsFragment.TAG, e.getMessage(), e);
                    MainActivity.errorDialogDisplay(getActivity(), e.getMessage());
                }
            }
        });
    }

}
