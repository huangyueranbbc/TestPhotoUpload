package com.example.sda;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.ClientProtocolException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private ImageView ivGetPhoto, ivCode;
    private LinearLayout lin;
    private boolean isKeyBoardShow = false, isBQViewShow = false;
    private boolean isADJUST_PAN = false, isADJUST_RESIZE = false;
    private EditText etContent;
    private ImageView ivPic;
    RelativeLayout relPic;

    private int SELECT_PICTURE = 1; // 从图库中选择图片
    private int SELECT_CAMER = 2; // 用相机拍摄照片
    private String filename;
    private File filetemp; // 要上传的图片
    private Button btnSubmit;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setADJUST_RESIZE();
        setContentView(R.layout.activity_main);

        // 添加权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<String>();

            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissions.add(Manifest.permission.CHANGE_WIFI_STATE);
            permissions.add(Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS);
            permissions.add(Manifest.permission.CAMERA);
            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 1);
            }
        }

        etContent = (EditText) findViewById(R.id.wpost_et);
        ivPic = (ImageView) findViewById(R.id.wpost_img);
        relPic = (RelativeLayout) findViewById(R.id.wpost_imglayout);
        btnSubmit = (Button) findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                doPost(fileName); // TODO 访问网络
                Log.i(TAG, "提交按钮被点击了!: ");
                new Thread() {
                    public void run() {
                        try {
                            AndroidUploadFile.uploadFile(filetemp.getAbsolutePath(), "http://192.168.191.1:8080/pic/upload");
                        } catch (ClientProtocolException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
        });
        findViewById(R.id.wpost_remimg).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                relPic.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.wpost_getimg).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isKeyBoardShow) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
                showSetHeadimg();
            }
        });

        final TextView tvWordNum = (TextView) findViewById(R.id.wpost_wordnum);
        final int wordnum = 300;
        etContent.addTextChangedListener(new TextWatcher() {
            int con = 0;
            CharSequence c;

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                con = arg0.length();
                c = arg0;
                tvWordNum.setText((wordnum - con) + "");
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                if (con > wordnum) {
                    c = c.subSequence(0, wordnum);
                    etContent.setText(c.toString());
                    etContent.setSelection(wordnum);
                }
            }
        });
        lin = (LinearLayout) findViewById(R.id.wpos_layout);
        // lin.addView(bqView);
        lin.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                int n = lin.getRootView().getHeight() - lin.getHeight();
                if (n > 100) {// 软键盘已弹出
                    if (isBQViewShow) {

                        setADJUST_RESIZE();
                        isBQViewShow = false;
                    }
                    isKeyBoardShow = true;
                } else {// 软键盘未弹出
                    isKeyBoardShow = false;
                    if (isADJUST_PAN) {
                        setADJUST_RESIZE();
                    }
                }

            }
        });

    }

    @Override
    protected void onResume() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                // InputMethodManager inputManager = (InputMethodManager)
                // etContent
                // .getContext().getSystemService(
                // Context.INPUT_METHOD_SERVICE);
                // inputManager.showSoftInput(etContent, 0);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);

                // // InputMethodManager imm = (InputMethodManager)
                // // getSystemService(Context.INPUT_METHOD_SERVICE);
                // // imm.toggleSoftInput(0,
                // InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 300);
        super.onResume();
    }

    private void setADJUST_PAN() {
        isADJUST_RESIZE = false;
        isADJUST_PAN = true;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

    }

    private void setADJUST_RESIZE() {
        isADJUST_RESIZE = true;
        isADJUST_PAN = false;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }

    public void cancle(View v) {
        finish();
    }

    public void button(View v) {

    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; // 0.0-1.0
        getWindow().setAttributes(lp);
    }

    private void showSetHeadimg() {
        final PopupWindow popupWindow = new PopupWindow(this);
        View v = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.dialog_changetx, null);
        popupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.color.transparent));
        popupWindow.setContentView(v);
        popupWindow.setAnimationStyle(R.style.AnimationPreview);
        popupWindow.showAtLocation(new View(this), Gravity.BOTTOM, 0, 0);
        backgroundAlpha(0.35f);
        popupWindow.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                backgroundAlpha(1f);
            }
        });
        v.findViewById(R.id.tx_camera).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                // 进入相机
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(getExternalCacheDir(), "edtimg.jpg")));
                startActivityForResult(intent, SELECT_CAMER);
            }
        });
        v.findViewById(R.id.tx_photo).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                // 进入图库
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_PICTURE);
            }
        });
        v.findViewById(R.id.tx_cancle).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    Bitmap bitmap = null;
                    if (requestCode == SELECT_CAMER) {
                        bitmap = BitmapFactory.decodeFile(getExternalCacheDir() + "/edtimg.jpg");
                    } else if (requestCode == SELECT_PICTURE) {
                        Uri uri = data.getData();
                        ContentResolver cr = MainActivity.this.getContentResolver();

                        try {
                            bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                            // 将得到的图片存放在文件夹中
                            filename = UUID.randomUUID() + ".jpg";
                            saveBitmap2file(bitmap, filename);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    Message msg = new Message();
                    msg.obj = ThumbnailUtils.extractThumbnail(bitmap, 300, 300);
                    handler.sendMessage(msg);

//                    BitmapUtil.comp(bitmap);
//                    Message msg2 = new Message();
//                    msg2.obj = bitmap;
//                    handler.sendMessage(msg2);
                }
            });

            thread.start();

        } else {
            Toast.makeText(this, "选择图片失败,请重新选择", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage: ");
            Bitmap bitmap = (Bitmap) msg.obj;
            relPic.setVisibility(View.VISIBLE);
            ivPic.setImageBitmap(bitmap);
        }
    };

    /**
     * 保存图片
     *
     * @param bmp
     * @param filename
     * @return
     */
    boolean saveBitmap2file(Bitmap bmp, String filename) {
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        int quality = 100;
        OutputStream stream = null;
        try {
            File file = getCacheDir();// 本应用的缓存文件夹

            Log.i(TAG, "saveBitmap2file: " + file.getAbsolutePath() + "/" + filename);
            filetemp = new File(file.getAbsolutePath() + "/" + filename);
            if (!file.exists()) {
                filetemp.createNewFile();
                fileName = file.getAbsolutePath() + "/" + filename;
            }
            stream = new FileOutputStream(filetemp);
            Log.i(TAG, "stream: " + stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmp.compress(format, quality, stream);
    }


//    private String doPost(String imagePath) {
//        OkHttpClient mOkHttpClient = new OkHttpClient();
//
//        String result = "error";
//        MultipartBody.Builder builder = new MultipartBody.Builder();
//        // 这里演示添加用户ID
////        builder.addFormDataPart("userId", "20160519142605");
//        builder.addFormDataPart("image", imagePath,
//                RequestBody.create(MediaType.parse("image/jpeg"), new File(imagePath)));
//
//        RequestBody requestBody = builder.build();
//        Request.Builder reqBuilder = new Request.Builder();
//        Request request = reqBuilder
//                .url("BASE_URL" + "/uploadimage")
//                .post(requestBody)
//                .build();
//
//        Log.d(TAG, "请求地址 " + "BASE_URL" + "/uploadimage");
//        try {
//            Response response = mOkHttpClient.newCall(request).execute();
//            Log.d(TAG, "响应码 " + response.code());
//            if (response.isSuccessful()) {
//                String resultValue = response.body().string();
//                Log.d(TAG, "响应体 " + resultValue);
//                return resultValue;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }

}
