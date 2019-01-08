package com.ormediagroup.youngplus;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ormediagroup.youngplus.adapter.SidebarExpandableListAdapter;
import com.ormediagroup.youngplus.bean.MenuBean;
import com.ormediagroup.youngplus.bean.ServicesBean;
import com.ormediagroup.youngplus.fragment.AboutFragment;
import com.ormediagroup.youngplus.fragment.BaseFragment;
import com.ormediagroup.youngplus.fragment.ContactFragment;
import com.ormediagroup.youngplus.fragment.HomeFragment;
import com.ormediagroup.youngplus.fragment.LoginFragment;
import com.ormediagroup.youngplus.fragment.PromotionFragment;
import com.ormediagroup.youngplus.fragment.PromotionFragment2;
import com.ormediagroup.youngplus.fragment.RegisterFragment;
import com.ormediagroup.youngplus.fragment.ServiceDetailFragment;
import com.ormediagroup.youngplus.lau.ProcessingDialog;
import com.ormediagroup.youngplus.notuse.TestFragment;
import com.ormediagroup.youngplus.lau.AlarmService;
import com.ormediagroup.youngplus.lau.LauUtil;
import com.ormediagroup.youngplus.lau.ServiceWebviewClient;
import com.ormediagroup.youngplus.network.JSONResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        HomeFragment.onHomeFragmentListener,
        ServiceDetailFragment.setOnServiceDetailFragmentListener,
        ServiceWebviewClient.ServiceWebviewListener {

    private String TAG = "ORM";
    private String SERVICE_URL = "http://youngplus.com.hk/app-get-services";
    private String BOOKING_URL = "http://youngplus.com.hk/app-booking";
    private boolean isMenuLoaded = false;
    private String JumpType = "";
    private int DetailID = -1;
    private int StaticID = -1;
    private boolean isExit = false;

    private ImageView toHome, topLogo, bookNow, toggle, toggleSide;
    private LinearLayout bookPanel, bookPart, sidebar;
    private Spinner bookSex, bookService, bookTime;
    private ExpandableListView sidebar_menu;
    private ArrayList<MenuBean> group;
    private ArrayList<List<ServicesBean>> child;
    private DrawerLayout drawerLayout;
    private Button bookSubmit;
    private EditText bookDate, bookName, bookPhone;
    private LinearLayout bookNowPart;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
//        requestPermissions(); // test
        if (checkGooglePlayServices()) {
            initFCM();
            receiveIntent();
        }
    }

    private void requestPermissions() {
        requestWhatPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 1, "您禁用了文件讀寫權限");
    }

    private void requestWhatPermission(String permission, int requestCode, String refusedPrompt) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                if (refusedPrompt != null) {
                    Toast.makeText(MainActivity.this, refusedPrompt, Toast.LENGTH_SHORT).show();
                }
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        }
    }

    /**
     * Receive foreground & background message
     * foreground: should use intent.putExtra() in FirebaseMessagingService first, then getIntent()
     * background: getIntent() directly
     */
    private void receiveIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra("type")) {
            String type = intent.getStringExtra("type");
            Log.i(TAG, "receiveIntent: type = " + type);
            switch (type) {
                case "about":
                    replaceFragment(new AboutFragment(), "about", true);
                    break;
                case "link":
                    Uri uri = Uri.parse(LauUtil.getLegalURL(intent.getStringExtra("link")));
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    break;
            }
        }

    }

    private void initFCM() {
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                Log.i(TAG, "onSuccess: token = " + newToken);
                sp = getSharedPreferences("user_info", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("token", newToken);
                editor.apply();
            }
        });

    }

    private boolean checkGooglePlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int rc = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (rc != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(rc)) {
                googleApiAvailability.getErrorDialog(this, rc, 0)
                        .show();
            } else {
                Log.i("ORM", "This device is not supported.");
                finish();
            }
            return false;
        } else {
            return true;
        }
    }

    private void initView() {
        drawerLayout = findViewById(R.id.drawerLayout);
        topLogo = findViewById(R.id.top_logo);
        bookPart = findViewById(R.id.bookPart);
        bookNow = findViewById(R.id.bookNow);
        bookPanel = findViewById(R.id.bookPanel);
        bookSex = findViewById(R.id.book_sex);
        bookService = findViewById(R.id.book_service);
        bookDate = findViewById(R.id.book_date);
        bookTime = findViewById(R.id.book_time);
        bookName = findViewById(R.id.book_name);
        bookPhone = findViewById(R.id.book_phone);
        bookSubmit = findViewById(R.id.book_submit);
        sidebar = findViewById(R.id.sidebar);
        sidebar_menu = findViewById(R.id.sidebar_menu);
        toHome = findViewById(R.id.toHome);
        toggle = findViewById(R.id.top_toggle);
        toggleSide = findViewById(R.id.side_toggle);
        bookNowPart = findViewById(R.id.bookNowPart);
    }

    private void initData() {
        setDrawerToggle();
        setDrawerFullScreen();
        setDrawerHandle();
        showBookPart();
        showHomeContent();
        setLogoAction();
        loadDrawerMenu();
        initAlarmService();
    }

    private void initAlarmService() {
        startService(new Intent(MainActivity.this, AlarmService.class));
    }

    private void showHomeContent() {
        Fragment home = new HomeFragment();
//        Fragment home = new TestFragment();
        replaceFragment(home, "home", true);
    }

    private void setLogoAction() {
        topLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toHome("home", 0);
            }
        });
        toHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(GravityCompat.END);
                JumpType = "home";
            }
        });
    }

    private void setDrawerHandle() {
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Log.i(TAG, "onDrawerOpened: ");
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (slideOffset == 1) {
                    Log.i(TAG, "onDrawerSlide: ");
                    if (!isMenuLoaded) {
                        loadDrawerMenu();
                    }
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.i(TAG, "onDrawerClosed: ");
                if (JumpType.equals("static") && StaticID != -1) {
                    switch (StaticID) {
                        case 1:
                            replaceFragment(new AboutFragment(), "about", true);
                            break;
                        case 2:
                            replaceFragment(new ContactFragment(), "contact", true);
                            break;
                        // test
                        case 3:
                            replaceFragment(new PromotionFragment(), "promotion", true);
                            break;
                        case 4:
                            replaceFragment(new RegisterFragment(), "register", true);
                            break;
                        case 5:
                            replaceFragment(new LoginFragment(), "login", true);
                            break;
                    }
                    initDrawerHandle();
                } else if (JumpType.equals("detail") && DetailID != -1) {
                    FragmentManager fm = getSupportFragmentManager();
                    if (fm.getBackStackEntryCount() >= 2) {
                        replaceFragment(ServiceDetailFragment.newInstance(DetailID),
                                "detail_" + DetailID, true);
                    } else {
                        addFragment(ServiceDetailFragment.newInstance(DetailID),
                                "detail_" + DetailID, true);
                    }
                    initDrawerHandle();
                } else if (JumpType.equals("home")) {
                    toHome("home", 0);
                    initDrawerHandle();
                } else if (JumpType.equals("promotion")) {
                    FragmentManager fm = getSupportFragmentManager();
                    if (fm.getBackStackEntryCount() >= 2) {
                        replaceFragment(PromotionFragment2.newInstance(DetailID),
                                "promotion_" + DetailID, true);
                    } else {
                        addFragment(PromotionFragment2.newInstance(DetailID),
                                "promotion_" + DetailID, true);
                    }
                    initDrawerHandle();
                }
            }
        });
    }

    private void setDrawerToggle() {
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
        toggleSide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });
    }

    private void initDrawerHandle() {
        JumpType = "";
        StaticID = -1;
        DetailID = -1;
    }

    private void hideBookPanel() {
        bookPart.setClickable(false);
        bookPanel.setVisibility(View.GONE);
    }

    private void showBookPart() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LauUtil.getScreenWidth(MainActivity.this) - bookNow.getWidth(),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(-10, 0, 0, 0);
        bookPanel.setLayoutParams(lp);
        bookNow.bringToFront();
        bookPanel.setFocusable(false);
        bookNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bookPanel.getVisibility() == View.VISIBLE) {
                    hideBookPanel();
                } else {
                    bookPart.setClickable(true);
                    bookPanel.setVisibility(View.VISIBLE);
                    Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.in_from_right);
                    bookPart.startAnimation(animation);
                }
            }
        });
        bookPart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBookPanel();
            }
        });


        String[] sex = {"男", "女"};
        setSpinner(bookSex, sex);
        String[] services = {"靶向肽療程", "逆齡療程", "營養管理計劃", "中醫診斷及配方",
                "脊醫診斷及治療", "醫學美容", "DNA基因檢測", "全面體檢"};
        setSpinner(bookService, services);
        bookDate.setInputType(InputType.TYPE_NULL);
        bookDate.setFocusable(false);
        bookDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDatePicker(bookDate);
            }
        });
        ArrayList<String> timesList = new ArrayList<>();
        for (int i = 10; i <= 18; i++) {
            for (int j = 0; j < 60; j += 30) {
                String formatTime = String.format("%02d", i) + ":" + String.format("%02d", j);
                timesList.add(formatTime);
            }
        }
        String[] times = new String[timesList.size()];
        timesList.toArray(times);
        setSpinner(bookTime, times);
        final ProcessingDialog dialog = new ProcessingDialog(MainActivity.this);
        bookSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!LauUtil.isNull(bookName) && !LauUtil.isNull(bookPhone) && !LauUtil.isNull(bookDate)) {
                    if (LauUtil.isPhone(bookPhone.getText().toString())) {
                        dialog.loading("正在提交...");
                        String sexStr = bookSex.getSelectedItem().toString().equals("男") ? "M" : "F";
                        String param = "username=" + bookName.getText() + "&phone=" + bookPhone.getText()
                                + "&sex=" + sexStr + "&service=" + bookService.getSelectedItem()
                                + "&date=" + bookDate.getText() + "&time=" + bookTime.getSelectedItem()
                                + "&action=booking";
                        new JSONResponse(MainActivity.this, BOOKING_URL, param, new JSONResponse.onComplete() {
                            @Override
                            public void onComplete(JSONObject json) {
                                try {
                                    if (json.getInt("rc") == 0) {
                                        Log.i(TAG, "onComplete: json = " + json);
                                        dialog.success("提交成功").setOnDismissListener(new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialog) {
                                                bookName.setText("");
                                                bookPhone.setText("");
                                                bookDate.setText("");
                                                bookSex.setSelection(0);
                                                bookService.setSelection(0);
                                                bookTime.setSelection(0);
                                                hideBookPanel();
                                            }
                                        });
                                    } else {
                                        dialog.loadingToFailed("提交失敗，請聯絡Young+客服");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    dialog.loadingToFailed("請檢查網絡連接");
                                }
                            }
                        });
                    } else {
                        dialog.warning("請輸入8~11位電話號碼").setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                bookPhone.requestFocus();
                            }
                        });
                    }
                } else {
                    dialog.warning("請不要留空").setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            LauUtil.nullEditTextFocus(bookPanel);
                        }
                    });
                }
            }
        });
    }

    private void loadDrawerMenu() {
        new JSONResponse(MainActivity.this, SERVICE_URL, "after=20181201", new JSONResponse.onComplete() {
            @Override
            public void onComplete(JSONObject json) {
                if (!json.isNull("data")) {
                    isMenuLoaded = true;
                    List<ServicesBean> aceServiceList = new ArrayList<>();
                    List<ServicesBean> healthManagementList = new ArrayList<>();
                    List<ServicesBean> promotionList = new ArrayList<>();
                    try {
                        JSONObject data = json.getJSONObject("data");
                        JSONArray aceServices = data.getJSONArray("aceServices");
                        JSONArray healthManagement = data.getJSONArray("healthManagement");
                        JSONArray promotions = data.getJSONArray("promotion");
                        aceServiceList = getMenus(aceServices, aceServiceList);
                        healthManagementList = getMenus(healthManagement, healthManagementList);
                        for (int i = 0; i < promotions.length(); i++) {
                            promotionList.add(new ServicesBean(
                                    3,
                                    promotions.getJSONObject(i).getInt("ID"),
                                    promotions.getJSONObject(i).getString("title"),
                                    "",
                                    promotions.getJSONObject(i).getInt("detail")
                            ));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    group = new ArrayList<MenuBean>();
                    child = new ArrayList<List<ServicesBean>>();
                    group.add(new MenuBean("皇牌服務", 0));
                    group.add(new MenuBean("全方位健康管理", 0));
                    group.add(new MenuBean("Promotion", 3));
                    group.add(new MenuBean("關於Young+", 1));
                    group.add(new MenuBean("聯絡Young+", 2));

                    // test
                    group.add(new MenuBean("test-register", 4));
                    group.add(new MenuBean("test-login", 5));

                    child.add(aceServiceList);
                    child.add(healthManagementList);
                    child.add(promotionList);
                    child.add(new ArrayList<ServicesBean>());
                    child.add(new ArrayList<ServicesBean>());

                    // test
                    child.add(new ArrayList<ServicesBean>());
                    child.add(new ArrayList<ServicesBean>());

                    sidebar_menu.setAdapter(new SidebarExpandableListAdapter(MainActivity.this, group, child));
                    sidebar_menu.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                        @Override
                        public boolean onGroupClick(ExpandableListView parent, View v, final int groupPosition, long id) {
                            if (child.get(groupPosition).isEmpty()) {
                                JumpType = "static";
                                StaticID = group.get(groupPosition).getFlag();
                                drawerLayout.closeDrawer(GravityCompat.END);
                                return true;
                            } else {
                                return false;
                            }
                        }
                    });
                    sidebar_menu.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                        @Override
                        public boolean onChildClick(ExpandableListView parent, View v, final int groupPosition, final int childPosition, long id) {
                            switch (child.get(groupPosition).get(childPosition).getType()) {
                                case 0:
                                    JumpType = "detail";
                                    break;
                                case 3:
                                    JumpType = "promotion";
                                    break;
                            }
                            Log.i(TAG, "onChildClick: jumptype = " + JumpType);
//                            JumpType = "detail";
                            DetailID = child.get(groupPosition).get(childPosition).getDetailID();
                            drawerLayout.closeDrawer(GravityCompat.END);
                            return false;
                        }
                    });
                }
            }
        });
    }

    private List<ServicesBean> getMenus(JSONArray jsonArray, List<ServicesBean> list) {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = null;
            try {
                obj = jsonArray.getJSONObject(i);
                list.add(new ServicesBean(
                        0,
                        obj.getInt("id"),
                        obj.getString("title"),
                        obj.getString("img"),
                        obj.getInt("detail")
                ));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        int backStackCount = fm.getBackStackEntryCount();
        Log.i("ORM", "onBackPressed: " + backStackCount);
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else if (bookPanel.getVisibility() == View.VISIBLE) {
            hideBookPanel();
        } else if (backStackCount > 1) {
            Fragment f = fm.findFragmentById(R.id.frameLayout);
            if (f instanceof BaseFragment) {
                if (((BaseFragment) f).onBackPressed()) {
                    Log.i("ORM", "fragment back button handled");
                    fm.popBackStack();
                } else {
                    super.onBackPressed();
                }
            } else {
                super.onBackPressed();
            }
        } else if (backStackCount == 1) {
            if (isExit) {
                this.finish();
            } else {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                isExit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExit = false;
                    }
                }, 500);
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setDrawerFullScreen();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setDrawerFullScreen();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied
                }
                break;
        }
    }

    private void setDrawerFullScreen() {
        ViewGroup.LayoutParams sidebarParams = sidebar.getLayoutParams();
        sidebarParams.width = LauUtil.getScreenWidth(MainActivity.this);
        sidebarParams.height = LauUtil.getScreenHeight(MainActivity.this);
        sidebar.setLayoutParams(sidebarParams);
    }

    private void setSpinner(Spinner spinner, String[] array) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, array);
        spinner.setAdapter(adapter);
    }

    private String formatDateAndTime(int number) {
        return number < 10 ? "0" + number : "" + number;
    }

    private void addDatePicker(final EditText editText) {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.ENGLISH);
                Calendar c2 = (Calendar) c.clone();
                c2.set(Calendar.DATE, dayOfMonth);
                c2.set(Calendar.MONTH, monthOfYear);
                c2.set(Calendar.YEAR, year);
                long timeMills = c2.getTimeInMillis();
                String dayOfWeek = sdf.format(timeMills);
                if (!dayOfWeek.equals("Sunday") && !dayOfWeek.equals("Saturday")) {
                    String formatDate = formatDateAndTime(monthOfYear + 1) + "/" + formatDateAndTime(dayOfMonth) + "/" + year;
                    editText.setText(formatDate);
                } else {
                    Toast.makeText(MainActivity.this, "請選擇工作日", Toast.LENGTH_SHORT).show();
                    editText.callOnClick();
                }
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        DatePicker datePicker = datePickerDialog.getDatePicker();
        datePicker.setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void addTimePicker(final EditText editText) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                editText.setText(formatDateAndTime(hourOfDay) + ":" + formatDateAndTime(minute));
            }
        }, hour, minute, true).show();
    }

    private void replaceFragment(Fragment f, String tag, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment f1 = fm.findFragmentByTag(tag);
        if (f1 == null) {
            ft.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left);
            ft.replace(R.id.frameLayout, f, tag);
            if (addToBackStack) {
                ft.addToBackStack(tag);
            }
            ft.commit();
        } else {
            fm.popBackStack(tag, 0);
        }
    }

    private void addFragment(Fragment f, String tag, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left);
        ft.hide(fm.findFragmentById(R.id.frameLayout));
        ft.add(R.id.frameLayout, f, tag);
        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.commit();
    }

    @Override
    public void toDetail(int id) {
        addFragment(ServiceDetailFragment.newInstance(id), "detail_" + id, true);
    }

    @Override
    public void toHome(String tag, int flag) {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStackImmediate(tag, 0);
    }

    @Override
    public void toDetailByTitle(String title, String url) {
        if (title.equals("聯絡young")) {
            replaceFragment(new ContactFragment(), "contact", true);
        } else {
            replaceFragment(ServiceDetailFragment.newInstance(title, url), "detail_" + title, true);
        }
    }
}
