package com.codeoregonapp.patrickleonard.ribbit;

import android.app.Application;

import com.codeoregonapp.patrickleonard.ribbit.utils.ParseConstants;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.PushService;

/**
 * Created by Patrick Leonard on 11/11/2015.
 */
public class RibbitApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        InitializeParse();
    }


    private void InitializeParse() {
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "oY22ZOOqknKimZhynJkivoQOI1s29ySR6aTpZlfn", "jyWZ0GhnZ3MbZL0PAULXVPIL6IZy7mMw1dxpFo8j");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    public static void updateParseInstallation(ParseUser user) {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(ParseConstants.KEY_USER_ID,user.getObjectId());
        installation.saveInBackground();
    }
}

