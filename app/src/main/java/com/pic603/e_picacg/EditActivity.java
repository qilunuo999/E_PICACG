package com.pic603.e_picacg;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pic603.service.UserService;

public class EditActivity extends AppCompatActivity {

    private TextView etFileLoc;
    private Button btSaveLoc;

    private static final int CHANGE_NICKNAME = 1;
    private static final int CHANGE_EMAIL = 2;
    private static final int CHANGE_INTRO = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        etFileLoc = findViewById(R.id.et_file_loc);
        btSaveLoc = findViewById(R.id.bt_save_loc);

        btSaveLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserService userService = new UserService();
                switch (getIntent().getIntExtra("CHANGE_TYPE",-1)){
                    case CHANGE_NICKNAME : userService.changeUserNickname(v, etFileLoc.getText().toString());break;
                    case CHANGE_EMAIL : userService.changeUserEmail(v, etFileLoc.getText().toString());break;
                    case CHANGE_INTRO : userService.changeUserIntroduction(v, etFileLoc.getText().toString());break;
                    default:break;
                }
            }
        });
    }
}
