package com.sl.achat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.sl.db.DB;
import com.sl.db.DBService;
import com.sl.usermgr.SLUserManager;
import com.sl.usermgr.SLUserManagerListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity /*implements LoaderCallbacks<Cursor>*/ {

    // UI references.
    private EditText mUnameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private Button mSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUnameView = (EditText) findViewById(R.id.uname);
//        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
//        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == R.id.login || id == EditorInfo.IME_NULL) {
//                    attemptLogin();
//                    return true;
//                }
//                return false;
//            }
//        });

        mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(attemptLogin() == true) {
                    mSignInButton.setClickable(false);
                }
            }
        });

        findViewById(R.id.sign_up_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, UserRegActivity.class);
                startActivity(intent);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SLUserManager.release();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private boolean attemptLogin() {
        // Reset errors.
        mUnameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String uname = mUnameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(uname)) {
            mUnameView.setError(getString(R.string.error_field_required));
            focusView = mUnameView;
            cancel = true;
        } else if (isEmailInvalid(uname)) {
            mUnameView.setError(getString(R.string.error_invalid_email));
            focusView = mUnameView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || isPasswordInvalid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
//            mAuthTask = new UserLoginTask(email, password);
//            mAuthTask.execute((Void) null);
            SLUserManager mgr = SLUserManager.get();
            mgr.setSLUserManagerListener(new MySLUserManagerListener());
            int rs = mgr.login(uname, password);
            return rs == 0;
        }

        return false;
    }

    private boolean isEmailInvalid(String email) {
        //TODO: Replace this with your own logic
//        return email.contains("@");
        // 编译正则表达式
        Pattern pattern = Pattern.compile(UserRegActivity.UNAME_VALID);
        // 忽略大小写的写法
        Matcher matcher = pattern.matcher(email);
        // 字符串是否与正则表达式相匹配
        return matcher.find();
    }

    private boolean isPasswordInvalid(String password) {
        //TODO: Replace this with your own logic
//        return password.length() > 4;
        // 编译正则表达式
        Pattern pattern = Pattern.compile(UserRegActivity.UNAME_VALID);
        // 忽略大小写的写法
        Matcher matcher = pattern.matcher(password);
        // 字符串是否与正则表达式相匹配
        return matcher.find();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private class MySLUserManagerListener extends SLUserManagerListener {
        @Override
        public void onLogin(int result) {
            Snackbar.make(mSignInButton, result == 0 ? "Login success" : "Login Failed", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            if(result == 0){
                String uname = mUnameView.getText().toString();
                String pwd = mPasswordView.getText().toString();

                SharedPreferences sp = getSharedPreferences(MainApplication.LOGIN_SAVE_FILE, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(MainApplication.LOGIN_NAME, uname);
                editor.commit();

                DBService db = new DBService(LoginActivity.this);
                DB.User user = db.queryUser(DB.User.SELECT_ONE, new String[]{uname});
                if(user != null){
                    db.executeSQL(DB.User.UPDATE_ONE, new Object[]{pwd, user.getId()});
                }else{
                    db.executeSQL(DB.User.INSERT, new Object[]{null, uname, pwd});

                    user = db.queryUser(DB.User.SELECT_ONE, new String[]{uname});
                }
                db.close();

                ((MainApplication)getApplication()).setUser(user);

                handler.sendEmptyMessageDelayed(LOGIN_OK, 300);
            }else{
                mSignInButton.setClickable(true);
            }
        }
    }

    private static final int LOGIN_OK = 20;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOGIN_OK: {
                    finish();
                    break;
                }
            }
        }
    };

}

