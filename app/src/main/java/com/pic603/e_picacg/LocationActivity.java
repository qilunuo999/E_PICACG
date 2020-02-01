package com.pic603.e_picacg;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class LocationActivity extends AppCompatActivity {
    private TextView etFileLoc;
    private Button btChangeLoc;
    private Button btSaveLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        etFileLoc = findViewById(R.id.et_file_loc);
        btChangeLoc = findViewById(R.id.bt_change_loc);
        btSaveLoc = findViewById(R.id.bt_save_loc);


    }
}
