
package com.wangzhihai.focustooffline;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class LoginActivity extends BasicActivity {

    private EditText account;
    private EditText password;

    private CircleImageView userImage;
    private Uri imageUri;

    private static final int TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userImage = findViewById(R.id.user_image);
        account = findViewById(R.id.et_account);
        password = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建 file 对象，用于储存拍照后的图片
                File outputImage = new File(getExternalCacheDir(), "outputImage.jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch (Exception e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    imageUri = FileProvider.getUriForFile(LoginActivity.this, "com.wangzhihai." +
                            "fileprovider", outputImage);
                }else {
                    imageUri = Uri.fromFile(outputImage);
                }
                //启动相机
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);


            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String accountContent = account.getText().toString();
                String passwordContent = password.getText().toString();

                if (accountContent.equals("admin") && passwordContent.equals("123456")) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Toast.makeText(LoginActivity.this, "账号密码错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        //将怕死和的照片显示出来
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver()
                                .openInputStream(imageUri));
                        userImage.setImageBitmap(bitmap);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }
}
