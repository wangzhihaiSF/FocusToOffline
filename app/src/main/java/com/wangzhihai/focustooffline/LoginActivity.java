
package com.wangzhihai.focustooffline;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.wangzhihai.focustooffline.utils.Base64Utils;
import com.wangzhihai.focustooffline.utils.SharedPreferencesUtils;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class LoginActivity extends BasicActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener{

    public PopupWindow mPopWindow;
    private EditText account;
    private EditText password;
    private ImageView iv_see_password;
    private Button btnLogin;
    private CheckBox checkBox_password;
    private CheckBox checkBox_login;
    private LoadingDialog mLoadingDialog; //显示正在加载的对话框
    public CircleImageView userImage;
    private Uri imageUri;

    private static final int TAKE_PHOTO = 1;
    private static final int CHOOSE_PHOTO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        setupEvents();
        initData();
    }

    private void initViews() {
        userImage = findViewById(R.id.user_image);
        account = findViewById(R.id.et_account);
        password = findViewById(R.id.et_password);
        iv_see_password = findViewById(R.id.iv_see_password);
        checkBox_password = findViewById(R.id.checkBox_password);
        checkBox_login = findViewById(R.id.checkBox_login);
        btnLogin = findViewById(R.id.btn_login);

    }

    private void setupEvents() {
        userImage.setOnClickListener(LoginActivity.this);
        checkBox_password.setOnCheckedChangeListener(this);
        checkBox_login.setOnCheckedChangeListener(this);
        iv_see_password.setOnClickListener(this);
        btnLogin.setOnClickListener(LoginActivity.this);
    }

    private void initData() {
        //判断用户第一次登录
        if (isFirstLogin()) {
            checkBox_password.setChecked(false);//取消记住密码的复选框
            checkBox_login.setChecked(false);//取消自动登录的复选框
        }
        //判断是否记住密码
        if (isRememberPassword()) {
            checkBox_password.setChecked(true);//勾选记住密码
            setTextNameAndPassword();//把密码和账号输入到输入框中
        } else {
            setTextName();//把用户账号放到输入账号的输入框中
        }

        //判断是否自动登录
        if (autoLogin()) {
            checkBox_login.setChecked(true);
            login();//去登录就可以

        }
    }

    private boolean isFirstLogin() {
        //获取SharedPreferences对象，使用自定义类的方法来获取对象
        SharedPreferencesUtils helper = new SharedPreferencesUtils(this, "setting");
        boolean first = helper.getBoolean("first", true);
        if (first) {
            //创建一个ContentVa对象（自定义的）设置不是第一次登录，,并创建记住密码和自动登录是默认不选，创建账号和密码为空
            helper.putValues(new SharedPreferencesUtils.ContentValue("first", false),
                    new SharedPreferencesUtils.ContentValue("rememberPassword", false),
                    new SharedPreferencesUtils.ContentValue("autoLogin", false),
                    new SharedPreferencesUtils.ContentValue("name", ""),
                    new SharedPreferencesUtils.ContentValue("password", ""));
            return true;
        }
        return false;
    }

    private boolean isRememberPassword() {
        //获取SharedPreferences对象，使用自定义类的方法来获取对象
        SharedPreferencesUtils helper = new SharedPreferencesUtils(this, "setting");
        boolean rememberPassword;
        rememberPassword = helper.getBoolean("rememberPassword", false);
        return rememberPassword;
    }

    /**
     * 模拟登录情况
     * 用户名csdn，密码123456，就能登录成功，否则登录失败
     */
    private void login() {

        //先做一些基本的判断，比如输入的用户命为空，密码为空，网络不可用多大情况，都不需要去链接服务器了，而是直接返回提示错误
        if (getAccount().isEmpty()){
            showToast("你输入的账号为空！");
            return;
        }

        if (getPassword().isEmpty()){
            showToast("你输入的密码为空！");
            return;
        }
        //登录一般都是请求服务器来判断密码是否正确，要请求网络，要子线程
        showLoading();//显示加载框
        Thread loginRunnable = new Thread() {

            @Override
            public void run() {
                super.run();
                setLoginBtnClickable(false);//点击登录后，设置登录按钮不可点击状态


                //睡眠3秒
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //判断账号和密码
                if (getAccount().equals("csdn") && getPassword().equals("123456")) {
                    showToast("登录成功");
                    loadCheckBoxState();//记录下当前用户记住密码和自动登录的状态;

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();//关闭页面
                } else {
                    showToast("输入的登录账号或密码不正确");
                }

                setLoginBtnClickable(true);  //这里解放登录按钮，设置为可以点击
                hideLoading();//隐藏加载框
            }
        };
        loginRunnable.start();
    }

    /**
     * 保存用户账号
     */
    public void loadUserName() {
        if (!getAccount().equals("") || !getAccount().equals("请输入登录账号")) {
            SharedPreferencesUtils helper = new SharedPreferencesUtils(this, "setting");
            helper.putValues(new SharedPreferencesUtils.ContentValue("name", getAccount()));
        }

    }

    /**
     * 设置密码可见和不可见的相互转换
     */
    private void setPasswordVisibility() {
        if (iv_see_password.isSelected()) {
            iv_see_password.setSelected(false);
            //密码不可见
            password.setInputType(InputType.TYPE_CLASS_TEXT | InputType
                    .TYPE_TEXT_VARIATION_PASSWORD);

        } else {
            iv_see_password.setSelected(true);
            //密码可见
            password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }

    }


    /**
     * 隐藏加载的进度框
     */
    public void hideLoading() {
        if (mLoadingDialog != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLoadingDialog.hide();
                }
            });

        }
    }



    /**
     * 保存按钮的状态值
     */
    public void loadCheckBoxState(CheckBox checkBox_password, CheckBox checkBox_login) {

        //获取SharedPreferences对象，使用自定义类的方法来获取对象
        SharedPreferencesUtils helper = new SharedPreferencesUtils(this, "setting");

        //如果设置自动登录
        if (checkBox_login.isChecked()) {
            //创建记住密码和自动登录是都选择,保存密码数据
            helper.putValues(
                    new SharedPreferencesUtils.ContentValue("rememberPassword", true),
                    new SharedPreferencesUtils.ContentValue("autoLogin", true),
                    new SharedPreferencesUtils.ContentValue("password", Base64Utils.encryptBASE64(getPassword())));

        } else if (!checkBox_password.isChecked()) { //如果没有保存密码，那么自动登录也是不选的
            //创建记住密码和自动登录是默认不选,密码为空
            helper.putValues(
                    new SharedPreferencesUtils.ContentValue("rememberPassword", false),
                    new SharedPreferencesUtils.ContentValue("autoLogin", false),
                    new SharedPreferencesUtils.ContentValue("password", ""));
        } else if (checkBox_password.isChecked()) {   //如果保存密码，没有自动登录
            //创建记住密码为选中和自动登录是默认不选,保存密码数据
            helper.putValues(
                    new SharedPreferencesUtils.ContentValue("rememberPassword", true),
                    new SharedPreferencesUtils.ContentValue("autoLogin", false),
                    new SharedPreferencesUtils.ContentValue("password", Base64Utils.encryptBASE64(getPassword())));
        }
    }

    /**
     * 保存用户选择“记住密码”和“自动登陆”的状态
     */
    private void loadCheckBoxState() {
        loadCheckBoxState(checkBox_password, checkBox_login);
    }

    /**
     * 是否可以点击登录按钮
     */
    public void setLoginBtnClickable(boolean clickable) {
        btnLogin.setClickable(clickable);
    }

    /**
     * 显示加载的进度款
     */
    public void showLoading() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this, getString(R.string.loading), false);
        }
        mLoadingDialog.show();
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * 判断是否自动登录
     */
    private boolean autoLogin() {
        //获取SharedPreferences对象，使用自定义类的方法来获取对象
        SharedPreferencesUtils helper = new SharedPreferencesUtils(this, "setting");
        boolean autoLogin;
        autoLogin = helper.getBoolean("autoLogin", false);
        return autoLogin;
    }

    /**
     * 设置数据到输入框中
     */
    @SuppressLint("SetTextI18n")
    public void setTextName() {
        account.setText("" + getLocalName());
    }

    @SuppressLint("SetTextI18n")
    private void setTextNameAndPassword() {
        account.setText("" + getLocalName());
        password.setText("" + getLocalPassword());
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
        View rootview;
        rootview = LayoutInflater.from(LoginActivity.this).inflate(R.layout.activity_login, null);
        mPopWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_image:
                showPopupWindow();
                break;
            case R.id.btn_login:
                loadUserName();//保存用户名
                login();
                break;
            case R.id.iv_see_password:
                setPasswordVisibility();
                break;
            case R.id.pop_take_photo:
                takePhoto();
                break;
            case R.id.pop_choose_from_album:
                choose_from_album();
                break;
            case R.id.pop_cancel:
                mPopWindow.dismiss();
        }

    }

    /**
     * 获得保存在本地的用户名
     */
    public String getLocalName() {
        //获取SharedPreferences对象，使用自定义类的方法来获取对象
        SharedPreferencesUtils helper = new SharedPreferencesUtils(this, "setting");
        String name;
        name = helper.getString("name");
        return name;
    }

    /**
     * 获得保存在本地的密码
     */
    public String getLocalPassword() {
        //获取SharedPreferences对象，使用自定义类的方法来获取对象
        SharedPreferencesUtils helper = new SharedPreferencesUtils(this, "setting");
        String password = helper.getString("password");
        return Base64Utils.decryptBASE64(password);   //解码一下
    }

    /**
     * 获取账号
     */
    public String getAccount() {
        return account.getText().toString().trim();//去掉空格
    }

    /**
     * 获取密码
     */
    public String getPassword() {
        return password.getText().toString().trim();//去掉空格
    }

    /**
     * CheckBox点击时的回调方法 ,不管是勾选还是取消勾选都会得到回调
     *
     * @param buttonView 按钮对象
     * @param isChecked  按钮的状态
     */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == checkBox_password) {  //记住密码选框发生改变时
            if (!isChecked) {   //如果取消“记住密码”，那么同样取消自动登陆
                checkBox_login.setChecked(false);
            }
        } else if (buttonView == checkBox_login) {   //自动登陆选框发生改变时
            if (isChecked) {   //如果选择“自动登录”，那么同样选中“记住密码”
                checkBox_password.setChecked(true);
            }
        }
    }


    /**
     * 监听回退键
     */
    @Override
    public void onBackPressed() {
        if (mLoadingDialog != null) {
            if (mLoadingDialog.isShowing()) {
                mLoadingDialog.cancel();
            } else {
                finish();
            }
        } else {
            finish();
        }

    }

    /**
     * 页面销毁前回调的方法
     */
    protected void onDestroy() {
        if (mLoadingDialog != null) {
            mLoadingDialog.cancel();
            mLoadingDialog = null;
        }
        super.onDestroy();
    }

    public void takePhoto() {
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

    public void choose_from_album() {
        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission
                .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[] { Manifest
                    .permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else {
            openAlbum();
        }
    }

    public void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
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
                    //4.4及以上系统使用此方法
                    handleImageOnKitKat(data);
                }
                break;
            default:
                break;
        }
    }

//    private void handleImageBeforeKitKat(Intent data) {
//        Uri uri = data.getData();
//        String imagePath = getImagePath(uri, null);
//        displayImage(imagePath);
//    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager
                        .PERMISSION_GRANTED) {
                    openAlbum();
                }else {
                    Toast.makeText(LoginActivity.this, "You denied the permission", Toast
                            .LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
