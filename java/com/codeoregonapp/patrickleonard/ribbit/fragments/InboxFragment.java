package com.codeoregonapp.patrickleonard.ribbit.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


import com.codeoregonapp.patrickleonard.ribbit.R;
import com.codeoregonapp.patrickleonard.ribbit.adapters.MessageAdapter;
import com.codeoregonapp.patrickleonard.ribbit.utils.ParseConstants;
import com.codeoregonapp.patrickleonard.ribbit.ui.MainActivity;
import com.codeoregonapp.patrickleonard.ribbit.ui.ViewImageActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Inbox ListFragment
 * Created by Patrick Leonard on 11/14/2015.
 */
public class InboxFragment extends ListFragment {

    public static final String TAG = InboxFragment.class.getSimpleName();

    private List<ParseObject> mMessages;
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    public InboxFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static InboxFragment newInstance() {
        return new InboxFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.swipe_refresh_1,
                R.color.swipe_refresh_2,
                R.color.swipe_refresh_3,
                R.color.swipe_refresh_4);

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

        getActivity().setProgressBarIndeterminateVisibility(true);
        getActivity().setProgressBarVisibility(true);

        retrieveMessages();
    }

    private void retrieveMessages() {
        ParseQuery<ParseObject> query = new ParseQuery<>(ParseConstants.CLASS_MESSAGE);
        query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
        query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messages, ParseException e) {
                getActivity().setProgressBarIndeterminateVisibility(false);
                getActivity().setProgressBarVisibility(false);

                if(mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                if (e == null) {
                    //we found messages
                    mMessages = messages;
                    if(getListAdapter() == null) {
                        MessageAdapter adapter = new MessageAdapter(getContext(), mMessages);
                        setListAdapter(adapter);
                    }
                    else {
                        //refill adapter
                        ((MessageAdapter)getListAdapter()).refill(mMessages);
                    }
                } else {
                    Log.e(FriendsFragment.TAG, e.getMessage(), e);
                    MainActivity.errorDialogDisplay(getActivity(), e.getMessage());
                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l,v,position,id);

        ParseObject message = mMessages.get(position);
        String messageType = message.getString(ParseConstants.KEY_FILE_TYPE);

        ParseFile file = message.getParseFile(ParseConstants.KEY_FILE);

        Uri fileUri = Uri.parse(file.getUrl());

        Log.v(InboxFragment.TAG,"FileUri: " + fileUri.toString());
        Log.v(InboxFragment.TAG, "MessageTYpe: " + messageType);

        if(messageType.equals(ParseConstants.TYPE_IMAGE)) {
            Intent intent = new Intent(getActivity(), ViewImageActivity.class);
            intent.setData(fileUri);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(Intent.ACTION_VIEW,fileUri);
            intent.setDataAndType(fileUri,"video/*");
            startActivity(intent);
        }

        List<String> ids = message.getList(ParseConstants.KEY_RECIPIENT_IDS);

        if(ids.size() == 1) {
            //delete the whole message
            message.deleteInBackground();
        }
        else {
            //remove recipient and save.
            ids.remove(ParseUser.getCurrentUser().getObjectId());
            ArrayList<String> idsToRemove = new ArrayList<>();
            idsToRemove.add(ParseUser.getCurrentUser().getObjectId());
            message.removeAll(ParseConstants.KEY_RECIPIENT_IDS,idsToRemove);
            message.saveInBackground();
        }

    }

    protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            retrieveMessages();
        }
    };
}