package com.sl.achat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
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

import com.alibaba.fastjson.JSON;
import com.sl.SLService;
import com.sl.protocol.Puser;
import com.sl.usermgr.SLUserManager;
import com.sl.usermgr.SLUserManagerListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sl.protocol.Puser.UserRegReq.SL_USERMGR_REQ_REG;

/**
 * A login screen that offers login via email/password.
 */
public class UserRegActivity extends AppCompatActivity /*implements LoaderCallbacks<Cursor>*/ {

    public static final String UNAME_VALID = "[`~!#$%^&*()+=|{}':;',\\[\\]<>/?~！#￥%……&*（）——+|{}【】‘；：”“’。，、？\\u4e00-\\u9fa5]";

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private Button mSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_reg);
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);

        mSignUpButton = (Button) findViewById(R.id.sign_up_button);
        mSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(attemptReg() == true) {
                    mSignUpButton.setClickable(false);
                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private boolean attemptReg() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (isEmailInvalid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
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

            Puser.UserRegReq req = new Puser.UserRegReq();

            req.commid = SLService.getInstance().commID();
            req.uname = email;
            req.email = email;
            req.passwd = password;
            req.mobile = "";
            req.sex = 0;
            req.age = 0;

            String jsondata = JSON.toJSONString(req);

            SLUserManager mgr = SLUserManager.get();
            mgr.setSLUserManagerListener(new MySLUserManagerListener());

            int rs = mgr.request(SL_USERMGR_REQ_REG, jsondata);
            return rs == 0;
        }

        return false;
    }

    private boolean isEmailInvalid(String email) {
        //TODO: Replace this with your own logic
//        return email.contains("@");
        // 编译正则表达式
        Pattern pattern = Pattern.compile(UNAME_VALID);
        // 忽略大小写的写法
        Matcher matcher = pattern.matcher(email);
        // 字符串是否与正则表达式相匹配
        return matcher.find();
    }

    private boolean isPasswordInvalid(String password) {
        //TODO: Replace this with your own logic
//        return password.length() > 4;
        // 编译正则表达式
        Pattern pattern = Pattern.compile(UNAME_VALID);
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
        public void onResponse(int reqtype, int result, String jsondata){

            int rs = -1;
            if(result == 0){
                if(reqtype == SL_USERMGR_REQ_REG){
                    Puser.UserRegRes regRes = JSON.parseObject(jsondata, Puser.UserRegRes.class);
                    rs = regRes.rc;
                }
            }

            Snackbar.make(mSignUpButton, rs == 0 ? "Sing up success" : "Sing up Failed("+ rs + ")", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            if(rs == 0){
                handler.sendEmptyMessageDelayed(SIGN_UP_OK, 300);
            }
        }
    }

    private static final int SIGN_UP_OK = 20;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SIGN_UP_OK: {
                    finish();
                    break;
                }
            }
        }
    };

}

