package com.ormediagroup.youngplus.fragment;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ormediagroup.youngplus.MainActivity;
import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.adapter.DailyMenuAdapter;
import com.ormediagroup.youngplus.adapter.SingleSelectAdapter;
import com.ormediagroup.youngplus.bean.DailyMenuBean;
import com.ormediagroup.youngplus.bean.ImageUploadBean;
import com.ormediagroup.youngplus.bean.SingleSelectBean;
import com.ormediagroup.youngplus.lau.API;
import com.ormediagroup.youngplus.lau.CommonHolder;
import com.ormediagroup.youngplus.lau.MaxHeightRecyclerView;
import com.ormediagroup.youngplus.lau.ProcessingDialog;
import com.ormediagroup.youngplus.lau.SimpleEditText;
import com.ormediagroup.youngplus.lau.SingleViewCommonAdapter;
import com.ormediagroup.youngplus.lau.User;
import com.ormediagroup.youngplus.loadAndRetry.LoadingAndRetryManager;
import com.ormediagroup.youngplus.loadAndRetry.OnLoadingAndRetryListener;
import com.ormediagroup.youngplus.network.JSONResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Lau on 2019/5/24.
 */
public class UserHabitFragment extends BaseFragment {
    private View view;
    private SimpleEditText timeSleep, timeGetUp;
    private SimpleEditText textQuality, textSleepHours, textSleepRemarks, textWater, textFruits, textSportHours, textSportType, textStoolTimes, textStoolShape, textStoolRemarks;
    private SimpleEditText timeBreakfast, foodBreakfast, timeSnack1, foodSnack1, timeLunch, foodLunch, timeSnack2, foodSnack2, timeDinner, foodDinner;
    private ImageView imageBreakfast, imageSnack1, imageLunch, imageSnack2, imageDinner;
    private Button btnSubmit;
    private SimpleEditText selectDate;
    private RecyclerView dailyMenuRecyclerView;

    private LoadingAndRetryManager loadingAndRetryManager;
    private ScrollView parentLayout;

    private final int UPLOAD_MAX_SIZE = 2 * 1024 * 1024;
    private final int THUMBNAIL_MAX_SIZE = 512 * 1024;
    private int STOOL_SELECT_POSITION = -1;
    private int SLEEP_SELECT_POSITION = -1;

    private Map<Integer, File> tmpFileMap = new HashMap<>();
    private Map<String, File> fileMap = new HashMap<>();
    private Map<String, File> cacheFile = new HashMap<>();
    private ImageUploadBean breakfastBean, snack1Bean, lunchBean, snack2Bean, dinnerBean;
    private LocalBroadcastManager broadcastManager;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadingAndRetryManager = LoadingAndRetryManager.generate(this.parentLayout, new OnLoadingAndRetryListener() {
            @Override
            public void setRetryEvent(View retryView) {
                retryView.findViewById(R.id.base_retry).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadingAndRetryManager.showLoading();
                        initData();
                    }
                });
            }
        });
        loadingAndRetryManager.showLoading();
        initData();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broadcastManager = LocalBroadcastManager.getInstance(mActivity);
        broadcastManager.registerReceiver(mMessageReceiver, new IntentFilter(API.ACTION_UPLOAD));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_habit, container, false);
        initView();
        return view;
    }

    private void initData() {
        dailyMenuRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        Map<String, String> menuParams = new HashMap<>();
        menuParams.put("uid", new User(mActivity).getUserId());
        menuParams.put("action", "get_menu");
        new JSONResponse(mActivity, API.API_NUTRITION, menuParams, new JSONResponse.JSONResponseComplete() {
            @Override
            public void onComplete(JSONObject json, boolean netError) {
                loadingAndRetryManager.showContent();
                try {
                    int rc = json.getInt("rc");
                    if (rc == 0) {
                        List<DailyMenuBean> menuBeanList = new ArrayList<>();
                        JSONArray data = json.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            String title = obj.getString("title");
                            String time = obj.getString("time");
                            JSONArray foodArr = obj.getJSONArray("food");
                            JSONArray quantityArr = obj.getJSONArray("quantity");
                            String content = "";
                            for (int j = 0; j < foodArr.length(); j++) {
                                content += foodArr.getString(j) + " " + quantityArr.getString(j);
                                if (j < foodArr.length() - 1) {
                                    content += "\n";
                                }
                            }
                            menuBeanList.add(new DailyMenuBean(title, time, content));
                        }
                        DailyMenuAdapter adapter = new DailyMenuAdapter(mActivity, menuBeanList, R.layout.item_habit_menu);
                        if (menuBeanList.size() > 0) {
                            TextView header = new TextView(mActivity);
                            header.setTextSize(18);
                            header.setText("建議餐單");
                            header.setTextColor(ContextCompat.getColor(mActivity, R.color.styleColor));
                            header.setPadding(0, 10, 0, 10);
                            adapter.setHeaderView(header);
                        }
                        dailyMenuRecyclerView.setAdapter(adapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dailyEdit();
            }
        });


    }

    private void dailyEdit() {
        String s = getRealFormat("yyyy-MM-dd").format(new Date());
        selectDate.getEditText().setText(s);
        checkExists(s);

        addDatePicker(selectDate.getEditText());
        addTimePicker(timeSleep.getEditText());
        addTimePicker(timeGetUp.getEditText());
        addTimePicker(timeBreakfast.getEditText());
        addTimePicker(timeSnack1.getEditText());
        addTimePicker(timeLunch.getEditText());
        addTimePicker(timeSnack2.getEditText());
        addTimePicker(timeDinner.getEditText());

        breakfastBean = new ImageUploadBean(imageBreakfast, "img_breakfast", 1001, 2001);
        snack1Bean = new ImageUploadBean(imageSnack1, "img_snack1", 1002, 2002);
        lunchBean = new ImageUploadBean(imageLunch, "img_lunch", 1003, 2003);
        snack2Bean = new ImageUploadBean(imageSnack2, "img_snack2", 1004, 2004);
        dinnerBean = new ImageUploadBean(imageDinner, "img_dinner", 1005, 2005);

        textQuality.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sleepQuality();
            }
        });
        imageBreakfast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(breakfastBean);
            }
        });
        imageSnack1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(snack1Bean);
            }
        });
        imageLunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(lunchBean);
            }
        });
        imageSnack2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(snack2Bean);
            }
        });
        imageDinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(dinnerBean);
            }
        });

        textStoolShape.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stoolShape();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> params = new HashMap<>();
                params.put("action", "upload");
                params.put("date", selectDate.getValue());
                params.put("uid", new User(mActivity).getUserId());
                params.put("time_sleep", timeSleep.getValue());
                params.put("time_get_up", timeGetUp.getValue());
                params.put("sleep_quality", textQuality.getValue());
                params.put("sleep_hours", textSleepHours.getValue());
                params.put("sleep_remarks", textSleepRemarks.getValue());
                params.put("time_breakfast", timeBreakfast.getValue());
                params.put("food_breakfast", foodBreakfast.getValue());
                params.put("time_snack1", timeSnack1.getValue());
                params.put("food_snack1", foodSnack1.getValue());
                params.put("time_lunch", timeLunch.getValue());
                params.put("food_lunch", foodLunch.getValue());
                params.put("time_snack2", timeSnack2.getValue());
                params.put("food_snack2", foodSnack2.getValue());
                params.put("time_dinner", timeDinner.getValue());
                params.put("food_dinner", foodDinner.getValue());
                params.put("water", textWater.getValue());
                params.put("fruit", textFruits.getValue());
                params.put("sport_hours", textSportHours.getValue());
                params.put("sport_type", textSportType.getValue());
                params.put("stool_times", textStoolTimes.getValue());
                params.put("stool_shape", textStoolShape.getValue());
                params.put("stool_remarks", textStoolRemarks.getValue());
                final ProcessingDialog processingDialog = new ProcessingDialog(mActivity);
                processingDialog.loading("正在提交...");
                processingDialog.getLoading().setCancelable(false);
                new JSONResponse(mActivity, API.API_NUTRITION, params, fileMap, new JSONResponse.JSONResponseComplete() {
                    @Override
                    public void onComplete(JSONObject json, boolean netError) {
                        if (netError) {
                            processingDialog.loadingToFailed(getString(R.string.connection_error));
                        } else {
                            try {
                                Log.i(TAG, "onComplete: json = " + json);
                                int rc = json.getInt("rc");
                                if (rc == 0) {
                                    processingDialog.loadingToSuccess("提交成功");
                                    deleteCacheFile();
                                    processingDialog.getLoading().setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            UserHabitFragmentListener uhfl = (UserHabitFragmentListener) mActivity;
                                            if (uhfl != null) {
                                                uhfl.toHome("home", 0);
                                            }
                                        }
                                    });
                                } else {
                                    processingDialog.loadingToFailed("提交失敗，請稍後重試");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                processingDialog.loadingToFailed(getString(R.string.connection_error));
                            }
                        }
                    }
                });
            }
        });
    }

    private void initView() {
        parentLayout = view.findViewById(R.id.parentLayout);

        timeSleep = view.findViewById(R.id.habit_time_sleep);
        timeGetUp = view.findViewById(R.id.habit_time_get_up);
        textQuality = view.findViewById(R.id.habit_text_quality);
        textSleepHours = view.findViewById(R.id.habit_text_sleep_hours);
        textSleepRemarks = view.findViewById(R.id.habit_text_sleep_remarks);

        timeBreakfast = view.findViewById(R.id.habit_time_breakfast);
        foodBreakfast = view.findViewById(R.id.habit_food_breakfast);
        imageBreakfast = view.findViewById(R.id.habit_image_breakfast);

        timeSnack1 = view.findViewById(R.id.habit_time_snack1);
        foodSnack1 = view.findViewById(R.id.habit_food_snack1);
        imageSnack1 = view.findViewById(R.id.habit_image_snack1);

        timeLunch = view.findViewById(R.id.habit_time_lunch);
        foodLunch = view.findViewById(R.id.habit_food_lunch);
        imageLunch = view.findViewById(R.id.habit_image_lunch);

        timeSnack2 = view.findViewById(R.id.habit_time_snack2);
        foodSnack2 = view.findViewById(R.id.habit_food_snack2);
        imageSnack2 = view.findViewById(R.id.habit_image_snack2);

        timeDinner = view.findViewById(R.id.habit_time_dinner);
        foodDinner = view.findViewById(R.id.habit_food_dinner);
        imageDinner = view.findViewById(R.id.habit_image_dinner);

        textWater = view.findViewById(R.id.habit_text_water);
        textFruits = view.findViewById(R.id.habit_text_fruits);

        textSportHours = view.findViewById(R.id.habit_text_sport_hours);
        textSportType = view.findViewById(R.id.habit_text_sport_type);

        textStoolTimes = view.findViewById(R.id.habit_text_stool_times);
        textStoolShape = view.findViewById(R.id.habit_text_stool_shape);
        textStoolRemarks = view.findViewById(R.id.habit_text_stool_remarks);

        btnSubmit = view.findViewById(R.id.habit_submit);

        timeSleep.getEditText().setFocusable(false);
        timeGetUp.getEditText().setFocusable(false);
        timeBreakfast.getEditText().setFocusable(false);
        timeSnack1.getEditText().setFocusable(false);
        timeLunch.getEditText().setFocusable(false);
        timeSnack2.getEditText().setFocusable(false);
        timeDinner.getEditText().setFocusable(false);
        textStoolShape.getEditText().setFocusable(false);
        textQuality.getEditText().setFocusable(false);

        dailyMenuRecyclerView = view.findViewById(R.id.habit_recyclerView);
        selectDate = view.findViewById(R.id.habit_today);
        selectDate.getEditText().setFocusable(false);
    }

    private void deleteCacheFile() {
        for (Map.Entry<String, File> cf : cacheFile.entrySet()) {
            if (cf.getValue().exists()) {
                cf.getValue().delete();
                refreshMediaDir(cf.getValue());
            }
        }
    }

    private SimpleDateFormat getRealFormat(String pattern) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        return format;
    }

    private void addDatePicker(final EditText editText) {
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(editText);
            }
        });
    }

    private void showDatePicker(final EditText editText) {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(mActivity, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar c2 = (Calendar) c.clone();
                c2.set(Calendar.DATE, dayOfMonth);
                c2.set(Calendar.MONTH, monthOfYear);
                c2.set(Calendar.YEAR, year);
                String formatDate = year + "-" + formatDateAndTime(monthOfYear + 1) + "-" + formatDateAndTime(dayOfMonth);
                editText.setText(formatDate);
                checkExists(formatDate);
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        DatePicker datePicker = datePickerDialog.getDatePicker();
        datePicker.setMaxDate(System.currentTimeMillis() + 1000);
        datePickerDialog.show();
    }

    private void checkExists(String date) {
        Map<String, String> params = new HashMap<>();
        params.put("uid", new User(mActivity).getUserId());
        params.put("action", "check");
        params.put("date", date);
        new JSONResponse(mActivity, API.API_NUTRITION, params, new JSONResponse.JSONResponseComplete() {
            @Override
            public void onComplete(JSONObject json, boolean netError) {
                Log.i(TAG, "onComplete: json = " + json);
                if (netError) {
                    Toast.makeText(mActivity, R.string.connection_error, Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        int rc = json.getInt("rc");
                        if (rc == 0) {

                        } else if (rc == -2) {
                            new AlertDialog.Builder(mActivity)
                                    .setIcon(R.mipmap.ic_youngplus)
                                    .setTitle("提示")
                                    .setMessage("您已提交過當前日期的資料，按確定重新填寫提交。")
                                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            showDatePicker(selectDate.getEditText());
                                        }
                                    }).setCancelable(false).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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

    private void stoolShape() {
        final View popup = LayoutInflater.from(mActivity).inflate(R.layout.popup_stool_shape, null, false);
        MaxHeightRecyclerView popupRecyclerView = popup.findViewById(R.id.recyclerView);
        popupRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        popupRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.onDrawOver(c, parent, state);
                int left = parent.getPaddingLeft();
                int right = parent.getWidth() - parent.getPaddingRight();
                Drawable divider = ContextCompat.getDrawable(mActivity, R.drawable.sep_line);
                int childCount = parent.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = parent.getChildAt(i);
                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                    int top = child.getBottom() + params.bottomMargin;
                    int bottom = top + 1;
                    if (divider != null) {
                        divider.setBounds(left, top, right, bottom);
                        divider.draw(c);
                    }
                }
            }
        });
        final List<SingleSelectBean> list = new ArrayList<>();
        list.add(new SingleSelectBean(R.drawable.stool_shape_1, "第一型", "一顆顆硬球（很難通過）"));
        list.add(new SingleSelectBean(R.drawable.stool_shape_2, "第二型", "香腸狀，但表面凹凸"));
        list.add(new SingleSelectBean(R.drawable.stool_shape_3, "第三型", "香腸狀，但表面有裂痕"));
        list.add(new SingleSelectBean(R.drawable.stool_shape_4, "第四型", "像香腸或蛇一樣，且表面很光滑"));
        list.add(new SingleSelectBean(R.drawable.stool_shape_5, "第五型", "斷邊光滑的柔軟塊狀（容易通過）"));
        list.add(new SingleSelectBean(R.drawable.stool_shape_6, "第六型", "粗邊蓬鬆塊，糊狀大便"));
        list.add(new SingleSelectBean(R.drawable.stool_shape_7, "第七型", "水狀，無固體塊（完全呈液體狀）"));

        final SingleSelectAdapter adapter = new SingleSelectAdapter(mActivity, list, R.layout.item_stool_shape);
        popupRecyclerView.setAdapter(adapter);
        final PopupWindow popupWindow = new PopupWindow(popup, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        setWindowAlpha(0.4f);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setWindowAlpha(1.0f);
            }
        });
        adapter.setSelectPosition(STOOL_SELECT_POSITION >= 0 ? STOOL_SELECT_POSITION : 0);
        popupRecyclerView.scrollToPosition(STOOL_SELECT_POSITION >= 0 ? STOOL_SELECT_POSITION : 0);
        adapter.setOnItemClickListener(new SingleViewCommonAdapter.setOnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                adapter.setSelectPosition(position);
                STOOL_SELECT_POSITION = position;
                textStoolShape.setValue(list.get(position).getTitle());
                popupWindow.dismiss();
            }
        });
    }

    private void sleepQuality() {
        final View popup = LayoutInflater.from(mActivity).inflate(R.layout.popup_stool_shape, null, false);
        MaxHeightRecyclerView popupRecyclerView = popup.findViewById(R.id.recyclerView);
        popupRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        popupRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.onDrawOver(c, parent, state);
                int left = parent.getPaddingLeft();
                int right = parent.getWidth() - parent.getPaddingRight();
                Drawable divider = ContextCompat.getDrawable(mActivity, R.drawable.sep_line);
                int childCount = parent.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = parent.getChildAt(i);
                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                    int top = child.getBottom() + params.bottomMargin;
                    int bottom = top + 1;
                    if (divider != null) {
                        divider.setBounds(left, top, right, bottom);
                        divider.draw(c);
                    }
                }
            }
        });
        final List<SingleSelectBean> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            list.add(new SingleSelectBean(i + "分"));
        }
        final SingleSelectAdapter adapter = new SingleSelectAdapter(mActivity, list, R.layout.item_sleep_quality);
        popupRecyclerView.setAdapter(adapter);
        final PopupWindow popupWindow = new PopupWindow(popup, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        setWindowAlpha(0.4f);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setWindowAlpha(1.0f);
            }
        });
        adapter.setSelectPosition(SLEEP_SELECT_POSITION >= 0 ? SLEEP_SELECT_POSITION : 0);
        popupRecyclerView.scrollToPosition(SLEEP_SELECT_POSITION >= 0 ? SLEEP_SELECT_POSITION : 0);
        adapter.setOnItemClickListener(new SingleViewCommonAdapter.setOnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                adapter.setSelectPosition(position);
                SLEEP_SELECT_POSITION = position;
                textQuality.setValue(list.get(position).getTitle());
                popupWindow.dismiss();
            }
        });
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String filename = "YoungPlus_" + timeStamp;
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/camera/" + filename + ".jpg");
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
                        addPhotoByGallery(path, breakfastBean);
                        break;
                    case 2002:
                        addPhotoByGallery(path, snack1Bean);
                        break;
                    case 2003:
                        addPhotoByGallery(path, lunchBean);
                        break;
                    case 2004:
                        addPhotoByGallery(path, snack2Bean);
                        break;
                    case 2005:
                        addPhotoByGallery(path, dinnerBean);
                        break;
                }
            } else {
                switch (type) {
                    case 1001:
                        addPhotoByCamera(type, breakfastBean);
                        break;
                    case 1002:
                        addPhotoByCamera(type, snack1Bean);
                        break;
                    case 1003:
                        addPhotoByCamera(type, lunchBean);
                        break;
                    case 1004:
                        addPhotoByCamera(type, snack2Bean);
                        break;
                    case 1005:
                        addPhotoByCamera(type, dinnerBean);
                        break;
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

    private Bitmap getThumbnail(File file) {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        double scale = (double) bitmap.getWidth() / bitmap.getHeight();
        return ThumbnailUtils.extractThumbnail(bitmap, 480, (int) (480 / scale));
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        deleteCacheFile();
        broadcastManager.unregisterReceiver(mMessageReceiver); // important,else oom
    }

    public interface UserHabitFragmentListener {
        void toHome(String tag, int flag);
    }
}
