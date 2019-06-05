package com.ormediagroup.youngplus.fragment;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.lau.API;
import com.ormediagroup.youngplus.lau.ProcessingDialog;
import com.ormediagroup.youngplus.network.JSONResponse;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lau on 2019/5/22.
 */
public class TestFragment extends BaseFragment {

    private View view;
    private Button upload, select;
    private TextView showJson;
    private ImageView showImg1, showImg2;
    private final int UPLOAD_MAX_SIZE = 2 * 1024 * 1024;

    private Map<Integer, File> tmpFileMap = new HashMap<>();
    private Map<String, File> fileMap = new HashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mActivity);
        broadcastManager.registerReceiver(mMessageReceiver, new IntentFilter(API.ACTION_UPLOAD));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_test, container, false);
        initView();
        initData();
        return view;
    }

    private void initView() {
        select = view.findViewById(R.id.btn_select);
        upload = view.findViewById(R.id.btn_upload);
        showImg1 = view.findViewById(R.id.show_img1);
        showImg2 = view.findViewById(R.id.show_img2);
        showJson = view.findViewById(R.id.text_json);
    }

    private void initData() {
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(1001, 2001);
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> params = new HashMap<>();
                params.put("action", "upload");
                final ProcessingDialog processingDialog = new ProcessingDialog(mActivity);
                processingDialog.loading("uploading...");
                Log.i(TAG, "onClick: test = " + getFullUrl(API.API_TEST, params));
                new JSONResponse(mActivity, API.API_TEST, params, fileMap, new JSONResponse.JSONResponseComplete() {
                    @Override
                    public void onComplete(JSONObject json, boolean netError) {
                        processingDialog.hideLoading();
                        if (netError) {
                            showJson.setText("network error");
                        } else {
                            showJson.setText(json.toString());
                            Log.i(TAG, "onSuccess: test json = " + json);
                        }
                    }
                });
            }
        });
    }


    private void showPopup(final int rc1, final int rc2) {
        View popup = LayoutInflater.from(mActivity).inflate(R.layout.popup_upload_image, null, false);
        TextView cancelWindow = popup.findViewById(R.id.icon_cancel);
        TextView openCamera = popup.findViewById(R.id.icon_open_camera);
        TextView openGallery = popup.findViewById(R.id.icon_open_gallery);
        final PopupWindow popupWindow = new PopupWindow(popup, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        setWindowAlpha(0.4f);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setWindowAlpha(1.0f);
            }
        });
        cancelWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
//                Intent intent = new Intent();
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
//                if (getActivity() != null) {
//                    getActivity().startActivityForResult(intent, rc1);
//                }

                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
                    File photo = null;
                    try {
                        photo = createImageFile();
                        tmpFileMap.put(rc1, photo);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Uri photoURI;
                    if (Build.VERSION.SDK_INT >= 24) {
                        photoURI = FileProvider.getUriForFile(mActivity, "com.ormediagroup.FileProvider", photo);
                    } else {
                        photoURI = Uri.fromFile(photo);
                    }
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    if (getActivity() != null && photo != null) {
                        getActivity().startActivityForResult(intent, rc1);
                    }
                }
            }
        });
        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (getActivity() != null) {
                    getActivity().startActivityForResult(Intent.createChooser(intent, "選擇圖片"), rc2);
                }
            }
        });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String filename = "YoungPlus_" + timeStamp;
        File storageDir = mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(filename, ".jpg", storageDir);
        return image;
    }

    private void setWindowAlpha(float f) {
        if (getActivity() != null) {
            WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
            lp.alpha = f;
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            getActivity().getWindow().setAttributes(lp);
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", 0);
            String path = intent.getStringExtra("path");
            Log.i(TAG, "onReceive: type=" + type + " path=" + path);
            if (path != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                File file = new File(path);
                Log.i(TAG, "onReceive: file size = " + file.length());
                if (file.length() > UPLOAD_MAX_SIZE) {
                    Toast.makeText(mActivity, "圖片超出上載限制大小", Toast.LENGTH_SHORT).show();
                } else {
                    switch (type) {
                        case 102:
                            showImg2.setImageBitmap(bitmap);
                            fileMap.put("test2", new File(path));
                            break;
                    }
                }

            } else {
                switch (type) {
                    case 101:
                        File file = tmpFileMap.get(type);
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        if (file.length() > UPLOAD_MAX_SIZE) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            int quality = 90;
                            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                            while (baos.toByteArray().length > UPLOAD_MAX_SIZE && quality >= 10) {
                                quality -= 10;
                                baos.reset();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                            }
                            if (baos.toByteArray().length > UPLOAD_MAX_SIZE) {
                                Toast.makeText(mActivity, "圖片超出上載限制大小", Toast.LENGTH_SHORT).show();
                            } else {
                                try {
                                    File newFile = new File(file.getPath());
                                    FileOutputStream fos = new FileOutputStream(newFile);
                                    fos.write(baos.toByteArray());
                                    fos.flush();
                                    fos.close();
                                    showImg1.setImageBitmap(bitmap);
                                    fileMap.put("test1", newFile);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            showImg1.setImageBitmap(bitmap);
                            fileMap.put("test1", new File(file.getPath()));
                        }
                        showImg1.setImageBitmap(bitmap);
                        fileMap.put("test1", new File(file.getPath()));
                        break;
                }
            }
        }
    };
}
