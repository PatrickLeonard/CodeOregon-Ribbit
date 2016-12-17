package com.codeoregonapp.patrickleonard.ribbit.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;


import android.os.Bundle;

import android.view.View;
import android.view.Window;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.codeoregonapp.patrickleonard.ribbit.R;
import com.codeoregonapp.patrickleonard.ribbit.RibbitApplication;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends Activity {

    public static final String TAG = LoginActivity.class.getSimpleName();

    @Bind(R.id.signUp) TextView mSignUpTextView;
    @Bind(R.id.username) EditText mUsernameEditText;
    @Bind(R.id.password) EditText mPasswordEditText;
    @Bind(R.id.log_in_button)   Button mLogInButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.hide();
        }

        mSignUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        mLogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = mUsernameEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                username = username.trim();
                password = password.trim();

                if(username.isEmpty() || password.isEmpty()) {
                    MainActivity.errorDialogDisplay(LoginActivity.this, getString(R.string.error_invalid_login));
                }
                else {
                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if(e == null) {
                                RibbitApplication.updateParseInstallation(ParseUser.getCurrentUser());
                                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            else {
                                MainActivity.errorDialogDisplay(LoginActivity.this,e.getMessage());
                            }
                        }
                    });
                }
            }
        });
    }

}

