package com.ormediagroup.youngplus.fragment;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.bean.DailyMenuBean;
import com.ormediagroup.youngplus.bean.ImageUploadBean;
import com.ormediagroup.youngplus.lau.API;
import com.ormediagroup.youngplus.lau.CommonHolder;
import com.ormediagroup.youngplus.lau.ProcessingDialog;
import com.ormediagroup.youngplus.lau.SingleViewCommonAdapter;
import com.ormediagroup.youngplus.network.JSONResponse;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Lau on 2019/5/22.
 */
public class TestFragment extends BaseFragment {

    private View view;
    private RecyclerView recyclerView;
    private ImageView image;
    private Button upload;
    private TextView result;

    private final int UPLOAD_MAX_SIZE = 2 * 1024 * 1024;
    private Map<Integer, File> tmpFileMap = new HashMap<>();
    private Map<String, File> fileMap = new HashMap<>();
    private Map<String, File> cacheFile = new HashMap<>();
    private ImageUploadBean testBean;
    private LocalBroadcastManager broadcastManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broadcastManager = LocalBroadcastManager.getInstance(mActivity);
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
        image = view.findViewById(R.id.image);
        upload = view.findViewById(R.id.upload);
        result = view.findViewById(R.id.result);
    }

    private void initData() {
        testBean = new ImageUploadBean(image, "file", 1001, 2001);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(testBean);
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> params = new HashMap<>();
//                params.put("user_key", "18b242c33a14220058c07f01537082cf");
//                new JSONResponse(mActivity, API.API_CALORIE_MAMA, params, fileMap, new JSONResponse.JSONResponseComplete() {
//                    @Override
//                    public void onComplete(JSONObject json, boolean netError) {
//                        Log.i(TAG, "onComplete: json = " + json);
//                        if (netError) {
//                            result.setText("netError");
//                        } else {
//                            Log.i(TAG, "onComplete: calorie json = " + json.toString());
//                            result.setText(json.toString());
//                        }
//                    }
//                });
//                new JSONResponse(mActivity, API.API_CALORIE_MAMA, new JSONResponse.onComplete() {
//                    @Override
//                    public void onComplete(JSONObject json) {
//                        Log.e(TAG, "onComplete: url = " + API.API_CALORIE_MAMA);
//                        Log.i(TAG, "onComplete: json = " + json);
//                    }
//                });

                new JSONResponse(mActivity, API.API_CALORIE_MAMA, null, new JSONResponse.JSONResponseComplete() {
                    @Override
                    public void onComplete(JSONObject json, boolean netError) {
                        Log.e(TAG, "onComplete: url = " + API.API_CALORIE_MAMA);
                        Log.i(TAG, "onComplete: json = " + json);
                    }
                });
            }
        });
    }

    private void showPopup(ImageUploadBean bean) {
        final ImageView imageView = bean.getImageView();
        final String filed = bean.getField();
        final int rc1 = bean.getRc1();
        final int rc2 = bean.getRc2();
        boolean flag = fileMap.get(filed) != null;
        final View popup = LayoutInflater.from(mActivity).inflate(R.layout.popup_upload_image, null, false);
        TextView cancelWindow = popup.findViewById(R.id.icon_cancel);
        TextView openCamera = popup.findViewById(R.id.icon_open_camera);
        TextView openGallery = popup.findViewById(R.id.icon_open_gallery);
        LinearLayout removePart = popup.findViewById(R.id.icon_remove_part);
        TextView remove = popup.findViewById(R.id.icon_remove);
        removePart.setVisibility(flag ? View.VISIBLE : View.GONE);
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
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileMap.remove(filed);
                imageView.setImageResource(R.drawable.add_image);
                popupWindow.dismiss();
            }
        });
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
                    File photo = createImageFile();
                    Log.i(TAG, "onClick: file = " + photo + " rc1 = " + rc1);
                    tmpFileMap.put(rc1, photo);
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

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String filename = "YoungPlus_" + timeStamp;
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/camera/" + filename + ".jpeg");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return file;
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
            Log.i(TAG, "onReceive: file type=" + type + " path=" + path);
            if (path != null && type != 0) {
                switch (type) {
                    case 2001:
                        addPhotoByGallery(path, testBean);
                        break;
//                    case 2002:
//                        addPhotoByGallery(path, snack1Bean);
//                        break;
//                    case 2003:
//                        addPhotoByGallery(path, lunchBean);
//                        break;
//                    case 2004:
//                        addPhotoByGallery(path, snack2Bean);
//                        break;
//                    case 2005:
//                        addPhotoByGallery(path, dinnerBean);
//                        break;
                }
            } else {
                switch (type) {
                    case 1001:
                        addPhotoByCamera(type, testBean);
                        break;
//                    case 1002:
//                        addPhotoByCamera(type, snack1Bean);
//                        break;
//                    case 1003:
//                        addPhotoByCamera(type, lunchBean);
//                        break;
//                    case 1004:
//                        addPhotoByCamera(type, snack2Bean);
//                        break;
//                    case 1005:
//                        addPhotoByCamera(type, dinnerBean);
//                        break;
                }
            }
        }
    };

    private void addPhotoByCamera(int type, ImageUploadBean bean) {
        File file = tmpFileMap.get(type);
        Log.i(TAG, "addPhotoByCamera: temp file = " + file + " type = " + type);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        addPhoto(file, bitmap, bean.getImageView(), bean.getField());
    }

    private void addPhotoByGallery(String path, ImageUploadBean bean) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        File file = new File(path);
        addPhoto(file, bitmap, bean.getImageView(), bean.getField());
    }

    private void addPhoto(File file, Bitmap bitmap, ImageView imageView, String filed) {
        if (file.length() > UPLOAD_MAX_SIZE) {
            File file1 = createImageFile();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap = cropBitmap(bitmap);
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
                    File newFile = new File(file1.getPath());
                    FileOutputStream fos = new FileOutputStream(newFile);
                    fos.write(baos.toByteArray());
                    fos.flush();
                    fos.close();
                    imageView.setImageBitmap(getThumbnail(newFile));
                    cacheFile.put(filed, newFile);
                    fileMap.put(filed, newFile);
                    refreshMediaDir(newFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            imageView.setImageBitmap(getThumbnail(file));
            fileMap.put(filed, new File(file.getPath()));
        }
    }

    private Bitmap cropBitmap(Bitmap bitmap) {
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();
        int cropWidth = w >= h ? h : w;// 裁切后所取的正方形区域边长
//        cropWidth /= 2;
        int cropHeight = cropWidth;
        int x = w >= h ? (w - cropWidth) / 2 : 0;
        int y = w >= h ? 0 : (h - cropHeight) / 2;
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        return Bitmap.createBitmap(bitmap, x, y, cropWidth, cropHeight, matrix, false);
    }

    private Bitmap getThumbnail(File file) {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        double scale = (double) bitmap.getWidth() / bitmap.getHeight();
//        return ThumbnailUtils.extractThumbnail(bitmap, 480, (int) (480 / scale));
        return ThumbnailUtils.extractThumbnail(bitmap, 544, (int) (544 / scale));
    }

    private void refreshMediaDir(File file) {
        Uri photoURI;
        if (Build.VERSION.SDK_INT >= 24) {
            photoURI = FileProvider.getUriForFile(mActivity, "com.ormediagroup.FileProvider", file);
        } else {
            photoURI = Uri.fromFile(file);
        }
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, photoURI);
        mActivity.sendBroadcast(intent);
    }

    private void deleteCacheFile() {
        for (Map.Entry<String, File> cf : cacheFile.entrySet()) {
            if (cf.getValue().exists()) {
                cf.getValue().delete();
                refreshMediaDir(cf.getValue());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deleteCacheFile();
        broadcastManager.unregisterReceiver(mMessageReceiver); // important,else oom
    }
}
