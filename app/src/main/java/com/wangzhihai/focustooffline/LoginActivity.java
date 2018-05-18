
package com.wangzhihai.focustooffline;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class LoginActivity extends BasicActivity implements View.OnClickListener{

    private PopupWindow mPopWindow;
    private EditText account;
    private EditText password;

    private CircleImageView userImage;
    private Uri imageUri;

    private static final int TAKE_PHOTO = 1;
    private static final int CHOOSE_PHOTO = 2;

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
                showPopupWindow();
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
                        //将拍摄的照片显示出来
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver()
                                .openInputStream(imageUri));
                        userImage.setImageBitmap(bitmap);
                        mPopWindow.dismiss();
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        //4.4及以上系统使用此方法
                        handleImageOnKitKat(data);
                    }else {
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();

        if (DocumentsContract.isDocumentUri(this, uri)) {
            //如果 Document 类型是 Uri, 则通过document id 处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);

            }else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri
                        .parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        }else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是Content 类型的 Uri , 则使用普通方法处理
            imagePath = getImagePath(uri, null);
        }else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是 file 类型的Uri, 直接获取图片路径
            imagePath = uri.getPath();
        }
        displayImage(imagePath);
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            userImage.setImageBitmap(bitmap);
            mPopWindow.dismiss();
        }else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过 Uri 和 selection来获取图片真实路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path =  cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void showPopupWindow() {
        //设置contentView
        View contentView = LayoutInflater.from(LoginActivity.this).inflate(R.layout.popuplayout, null);
        mPopWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopWindow.setContentView(contentView);
        //设置各个控件的点击响应
        TextView tv1 = contentView.findViewById(R.id.pop_take_photo);
        TextView tv2 = contentView.findViewById(R.id.pop_choose_from_album);
        TextView tv3 = contentView.findViewById(R.id.pop_cancel);
        tv1.setOnClickListener(this);
        tv2.setOnClickListener(this);
        tv3.setOnClickListener(this);
        //显示PopupWindow
        View rootview = LayoutInflater.from(LoginActivity.this).inflate(R.layout.activity_login, null);
        mPopWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pop_take_photo:
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
                break;
            case R.id.pop_choose_from_album:
                if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission
                        .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(LoginActivity.this, new String[] { Manifest
                            .permission.WRITE_EXTERNAL_STORAGE}, 1);
                }else {
                    openAlbum();
                }
                break;
            case R.id.pop_cancel:
                mPopWindow.dismiss();

        }

    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager
                        .PERMISSION_GRANTED) {
                    openAlbum();
                }else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
