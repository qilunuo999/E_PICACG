package com.pic603.e_picacg;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.pic603.service.UserService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private Boolean bPwdSwitch = false;
    private EditText etPwd;
    private EditText etAccount;
    private View mProgressView;
    private UserLoginTask mAuthTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        final ImageView ivPwdSwitch = findViewById(R.id.iv_pwd_switch);
        etAccount = findViewById(R.id.et_account);
        etPwd = findViewById(R.id.et_pwd);
        mProgressView = findViewById(R.id.l_progress);
        TextView sgup = findViewById(R.id.tv_sign_up);
        Button btLogin = findViewById(R.id.bt_login);

        ivPwdSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bPwdSwitch = !bPwdSwitch;
                if (bPwdSwitch) {
                    ivPwdSwitch.setImageResource(R.drawable.ic_visibility_black_24dp);
                    etPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    ivPwdSwitch.setImageResource(R.drawable.ic_visibility_off_black_24dp);
                    etPwd.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD|InputType.TYPE_CLASS_TEXT);
                    etPwd.setTypeface(Typeface.DEFAULT);
                }
            }
        });

        sgup.setOnClickListener(new View.OnClickListener() {          //跳转
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btLogin.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String account = etAccount.getText().toString();
                String password = etPwd.getText().toString();
                showProgress(true);
                if (!isPasswordValid(password)){
                    etPwd.setError("密码的长度要大于6位且不能包含空格");
                    showProgress(false);
                }
                mAuthTask = new UserLoginTask(account, password, v);
                mAuthTask.execute((Void) null);
            }
        });
    }
    private boolean isPhoneValid(String phone){
        Pattern pattern = Pattern
                .compile("^(13[0-9]|15[0-9]|153|15[6-9]|180|18[23]|18[5-9])\\d{8}$");
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        String expr = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})$";
        return email.matches(expr);
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.trim().length() > 6;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mAccount;
        private final String mPassword;
        private View v;

        UserLoginTask(String account, String password, View view) {
            mAccount = account;
            mPassword = password;
            v = view;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                UserService userService = new UserService();
                if(isEmailValid(mAccount)){
                    userService.loginByEmailPwd(v, mAccount, mPassword);
                }else if (isPhoneValid(mAccount)){
                    userService.loginByPhone(v, mAccount, mPassword);
                }else{
                    Snackbar.make(v, "请检查输入的账号是否为邮箱或手机号", Snackbar.LENGTH_LONG).show();
                    showProgress(false);
                }

                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }
            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                etPwd.setError(getString(R.string.error_incorrect_password));
                etPwd.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}