package com.wangzhihai.focustooffline;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends BasicActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonOffline = findViewById(R.id.force_offline);
        buttonOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.wang.zhihai.forceOffline.FORCE_OFFLINE");
                sendBroadcast(intent);
            }
        });
    }
}
