package com.codeoregonapp.patrickleonard.ribbit.ui;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.codeoregonapp.patrickleonard.ribbit.R;
import com.codeoregonapp.patrickleonard.ribbit.RibbitApplication;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import butterknife.Bind;
import butterknife.ButterKnife;


public class SignUpActivity extends Activity {

    @Bind(R.id.email) EditText mEmailEditText;
    @Bind(R.id.username) EditText mUsernameEditText;
    @Bind(R.id.password) EditText mPasswordEditText;
    @Bind(R.id.sign_up_button) Button mSignUpButton;
    @Bind(R.id.cancelButton) Button mCancelButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.hide();
        }

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = mUsernameEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                String email = mEmailEditText.getText().toString();

                username = username.trim();
                password = password.trim();
                email = email.trim();

                if(username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    MainActivity.errorDialogDisplay(SignUpActivity.this, getString(R.string.error_invalid_sign_up));
                }
                else {
                    //Create a new user.
                    final ParseUser user = new ParseUser();

                    user.setEmail(email);
                    user.setPassword(password);
                    user.setUsername(username);
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            setProgressBarIndeterminateVisibility(false);
                            setProgressBarIndeterminate(false);
                            if(e == null) {
                                RibbitApplication.updateParseInstallation(ParseUser.getCurrentUser());
                                Intent intent = new Intent(SignUpActivity.this,MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            else {
                                MainActivity.errorDialogDisplay(SignUpActivity.this, e.getMessage());
                            }
                        }
                    });
                }
            }
        });

    }

}
