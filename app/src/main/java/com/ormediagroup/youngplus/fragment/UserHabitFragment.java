package com.ormediagroup.youngplus.fragment;

import android.app.ActionBar;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ormediagroup.youngplus.MainActivity;
import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.lau.API;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lau on 2019/5/24.
 */
public class UserHabitFragment extends BaseFragment {
    private View view;
    private EditText timeSleep, timeGetUp;
    private EditText textQuality, textSleepHours, textSleepRemarks, textWater, textFruits, textSportHours, textSportType, textStoolTimes, textStoolShape, textStoolRemarks;
    private EditText timeBreakfast, foodBreakfast, timeSnack1, foodSnack1, timeLunch, foodLunch, timeSnack2, foodSnack2, timeDinner, foodDinner;
    private ImageView imageBreakfast, imageSnack1, imageLunch, imageSnack2, imageDinner;

    private final int UPLOAD_MAX_SIZE = 2 * 1024 * 1024;
    private final int THUMBNAIL_MAX_SIZE = 512 * 1024;

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
        view = inflater.inflate(R.layout.fragment_user_habit, container, false);
        initView();
        initData();
        return view;
    }

    private void initData() {
        addTimePicker(timeSleep);
        addTimePicker(timeGetUp);
        addTimePicker(timeBreakfast);
        addTimePicker(timeSnack1);
        addTimePicker(timeLunch);
        addTimePicker(timeSnack2);
        addTimePicker(timeDinner);

        imageBreakfast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(1001, 2001);
            }
        });
        imageSnack1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(1002, 2002);
            }
        });
        imageLunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(1003, 2003);
            }
        });
        imageSnack2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(1004, 2004);
            }
        });
        imageDinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(1005, 2005);
            }
        });
    }

    private void initView() {
        timeSleep = view.findViewById(R.id.habit_time_sleep);
        timeSleep.setFocusable(false);
        timeGetUp = view.findViewById(R.id.habit_time_get_up);
        timeGetUp.setFocusable(false);
        textQuality = view.findViewById(R.id.habit_text_quality);
        textSleepHours = view.findViewById(R.id.habit_text_sleep_hours);
        textSleepRemarks = view.findViewById(R.id.habit_text_sleep_remarks);

        timeBreakfast = view.findViewById(R.id.habit_time_breakfast);
        timeBreakfast.setFocusable(false);
        foodBreakfast = view.findViewById(R.id.habit_food_breakfast);
        imageBreakfast = view.findViewById(R.id.habit_image_breakfast);

        timeSnack1 = view.findViewById(R.id.habit_time_snack1);
        timeSnack1.setFocusable(false);
        foodSnack1 = view.findViewById(R.id.habit_food_snack1);
        imageSnack1 = view.findViewById(R.id.habit_image_snack1);

        timeLunch = view.findViewById(R.id.habit_time_lunch);
        timeLunch.setFocusable(false);
        foodLunch = view.findViewById(R.id.habit_food_lunch);
        imageLunch = view.findViewById(R.id.habit_image_lunch);

        timeSnack2 = view.findViewById(R.id.habit_time_snack2);
        timeSnack2.setFocusable(false);
        foodSnack2 = view.findViewById(R.id.habit_food_snack2);
        imageSnack2 = view.findViewById(R.id.habit_image_snack2);

        timeDinner = view.findViewById(R.id.habit_time_dinner);
        timeDinner.setFocusable(false);
        foodDinner = view.findViewById(R.id.habit_food_dinner);
        imageDinner = view.findViewById(R.id.habit_image_dinner);

        textWater = view.findViewById(R.id.habit_text_water);
        textFruits = view.findViewById(R.id.habit_text_fruits);

        textSportHours = view.findViewById(R.id.habit_text_sport_hours);
        textSportType = view.findViewById(R.id.habit_text_sport_type);

        textStoolTimes = view.findViewById(R.id.habit_text_stool_times);
        textStoolShape = view.findViewById(R.id.habit_text_stool_shape);
        textStoolRemarks = view.findViewById(R.id.habit_text_stool_remarks);
    }

    private void addTimePicker(final EditText editText) {
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                new TimePickerDialog(mActivity, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        editText.setText(formatDateAndTime(hourOfDay) + ":" + formatDateAndTime(minute));
                    }
                }, hour, minute, true).show();
            }
        });

    }

    private String formatDateAndTime(int number) {
        return number < 10 ? "0" + number : "" + number;
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
            if (path != null && type != 0) {
                switch (type) {
                    case 2001:
                        addPhotoByGallery(path, imageBreakfast, "img_breakfast");
                        break;
                    case 2002:
                        addPhotoByGallery(path, imageSnack1, "img_snack1");
                        break;
                    case 2003:
                        addPhotoByGallery(path, imageLunch, "img_lunch");
                        break;
                    case 2004:
                        addPhotoByGallery(path, imageSnack2, "img_snack2");
                        break;
                    case 2005:
                        addPhotoByGallery(path, imageDinner, "img_dinner");
                        break;
                }
            } else {
                switch (type) {
                    case 1001:
                        addPhotoByCamera(type, imageBreakfast, "img_breakfast");
                        break;
                    case 1002:
                        addPhotoByCamera(type, imageSnack1, "img_snack1");
                        break;
                    case 1003:
                        addPhotoByCamera(type, imageLunch, "img_lunch");
                        break;
                    case 1004:
                        addPhotoByCamera(type, imageSnack2, "img_snack2");
                        break;
                    case 1005:
                        addPhotoByCamera(type, imageDinner, "img_dinner");
                        break;
                }
            }
        }
    };

    private void addPhotoByCamera(int type, ImageView imageView, String filed) {
        File file = tmpFileMap.get(type);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        addPhoto(file, bitmap, imageView, filed);
    }

    private void addPhotoByGallery(String path, ImageView imageView, String filed) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        File file = new File(path);
        addPhoto(file, bitmap, imageView, filed);
    }

    private void addPhoto(File file, Bitmap bitmap, ImageView imageView, String filed) {
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
                    imageView.setImageBitmap(getThumbnail(newFile));
                    fileMap.put(filed, newFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            imageView.setImageBitmap(getThumbnail(file));
            fileMap.put(filed, new File(file.getPath()));
        }
    }

    private Bitmap getThumbnail(File file) {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        double scale = (double) bitmap.getWidth() / bitmap.getHeight();// 1/2  480/0.5
        return ThumbnailUtils.extractThumbnail(bitmap, 480, (int) (480 / scale));
    }

}
