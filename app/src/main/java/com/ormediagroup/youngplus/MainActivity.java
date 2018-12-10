package com.ormediagroup.youngplus;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.FrameLayout;
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
import com.ormediagroup.youngplus.adapter.SidebarExpandableListAdapter;
import com.ormediagroup.youngplus.bean.MenuBean;
import com.ormediagroup.youngplus.bean.ServicesBean;
import com.ormediagroup.youngplus.fragment.AboutFragment;
import com.ormediagroup.youngplus.fragment.BaseFragment;
import com.ormediagroup.youngplus.fragment.ContactFragment;
import com.ormediagroup.youngplus.fragment.HomeFragment;
import com.ormediagroup.youngplus.fragment.HomeFragment2;
import com.ormediagroup.youngplus.fragment.ServiceDetailFragment;
import com.ormediagroup.youngplus.lau.LauUtil;
import com.ormediagroup.youngplus.lau.ServiceWebviewClient;
import com.ormediagroup.youngplus.network.JSONResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        HomeFragment.onHomeFragmentListener,
        HomeFragment2.onHomeFragmentListener,
        ServiceDetailFragment.setOnServiceDetailFragmentListener,
        ServiceWebviewClient.ServiceWebviewListener {

    private String TAG = "ORM";
    private FrameLayout frameLayout;
    private boolean isExit = false;
    private ImageView bookNow;
    private LinearLayout bookPanel, bookPart;
    private Spinner bookSex, bookService;
    private EditText bookDate;
    private Spinner bookTime;
    private ExpandableListView sidebar_menu;
    private ArrayList<MenuBean> group;
    private ArrayList<List<ServicesBean>> child;
    private String SERVICE_URL = "http://youngplus.com.hk/app-get-services";
    private DrawerLayout drawerLayout;
    private LinearLayout sidebar;
    private ImageView toHome;
    private ImageView topLogo;
    private Button bookSubmit;
    private EditText bookName, bookPhone;
    private boolean isMenuLoaded = false;
    private String JumpType = "";
    private int DetailID = -1;
    private int StaticID = -1;
    private ImageView toggle;
    private ImageView toggle_side;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        initView();
        initData();
        if (checkGooglePlayServices()) {
            initFCM();
            receiveIntent();
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
                    Uri uri = Uri.parse(intent.getStringExtra("link"));
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    break;
            }
        }

    }

    private void initFCM() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                Log.i(TAG, "onSuccess: token = " + newToken);
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
        frameLayout = findViewById(R.id.frameLayout);
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
        toggle_side = findViewById(R.id.side_toggle);
    }

    private void initData() {
        setDrawerToggle();
        setDrawerFullScreen();
        setDrawerHandle();
        showBookPart();
        showHomeContent();
        setLogoAction();
        loadDrawerMenu();
    }

    private void showHomeContent() {
        Fragment home = new HomeFragment2();
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
                    }
                    initDrawerHandle();
                } else if (JumpType.equals("detail") && DetailID != -1) {
                    FragmentManager fm = getSupportFragmentManager();
                    if (fm.getBackStackEntryCount() >= 2) {
                        replaceFragment(ServiceDetailFragment.newInstance(DetailID),
                                "detail", true);
                    } else {
                        addFragment(ServiceDetailFragment.newInstance(DetailID),
                                "detail", true);
                    }
                    initDrawerHandle();
                } else if (JumpType.equals("home")) {
                    toHome("home", 0);
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
        toggle_side.setOnClickListener(new View.OnClickListener() {
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

    private void showBookPart() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LauUtil.getScreenWidth(MainActivity.this) - bookNow.getWidth(),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        bookPanel.setLayoutParams(lp);
        bookPanel.setFocusable(true);
        bookNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bookPanel.getVisibility() == View.VISIBLE) {
                    bookPart.setClickable(false);
                    bookPanel.setVisibility(View.GONE);
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
                bookPanel.setVisibility(View.GONE);
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
        String[] times = {"10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "13:00",
                "13:30", "14:00", "14:30", "15:00", "15:30", "14:00", "14:30", "15:00",
                "15:30", "16:00", "16:30", "17:00", "17:30", "18:00", "18:30"};
        setSpinner(bookTime, times);
        bookSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, bookName.getText() + " " +
                        bookPhone.getText() + " " + bookSex.getSelectedItem() + " " +
                        bookDate.getText() + " " + bookTime.getSelectedItem() + " " +
                        bookService.getSelectedItem(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDrawerMenu() {
        new JSONResponse(MainActivity.this, SERVICE_URL, "", new JSONResponse.onComplete() {
            @Override
            public void onComplete(JSONObject json) {
                if (!json.isNull("data")) {
                    isMenuLoaded = true;
                    List<ServicesBean> aceServiceList = new ArrayList<>();
                    List<ServicesBean> healthManagementList = new ArrayList<>();
                    try {
                        JSONArray aceServices = json.getJSONObject("data").getJSONArray("aceServices");
                        JSONArray healthManagement = json.getJSONObject("data").getJSONArray("healthManagement");
                        for (int i = 0; i < aceServices.length(); i++) {
                            aceServiceList.add(new ServicesBean(
                                    aceServices.getJSONObject(i).getInt("id"),
                                    aceServices.getJSONObject(i).getString("title"),
                                    aceServices.getJSONObject(i).getString("img"),
                                    aceServices.getJSONObject(i).getInt("detail")
                            ));
                        }
                        for (int i = 0; i < healthManagement.length(); i++) {
                            healthManagementList.add(new ServicesBean(
                                    healthManagement.getJSONObject(i).getInt("id"),
                                    healthManagement.getJSONObject(i).getString("title"),
                                    healthManagement.getJSONObject(i).getString("img"),
                                    healthManagement.getJSONObject(i).getInt("detail")
                            ));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    group = new ArrayList<MenuBean>();
                    child = new ArrayList<List<ServicesBean>>();
                    group.add(new MenuBean("皇牌服務", 0));
                    group.add(new MenuBean("全方位健康管理", 0));
                    group.add(new MenuBean("關於Young+", 1));
                    group.add(new MenuBean("聯絡Young+", 2));
                    child.add(aceServiceList);
                    child.add(healthManagementList);
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
                            JumpType = "detail";
                            DetailID = child.get(groupPosition).get(childPosition).getDetailID();
                            drawerLayout.closeDrawer(GravityCompat.END);
                            return false;
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        int backStackCount = fm.getBackStackEntryCount();
        Log.i("ORM", "onBackPressed: " + backStackCount);
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else if (bookPanel.getVisibility() == View.VISIBLE) {
            bookPanel.setVisibility(View.GONE);
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

    private void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    private void addDatePicker(final EditText editText) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                editText.setText(formatDateAndTime(monthOfYear + 1) + "/" + formatDateAndTime(dayOfMonth) + "/" + year);
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
        ft.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left);
        ft.replace(R.id.frameLayout, f, tag);
        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.commit();
        Log.i(TAG, "replaceFragment: count = " + fm.getBackStackEntryCount());
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
        Log.i(TAG, "addFragment: count = " + fm.getBackStackEntryCount());
    }

    @Override
    public void toDetail(int id) {
        addFragment(ServiceDetailFragment.newInstance(id), "detail", true);
    }

    @Override
    public void toHome(String tag, int flag) {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStackImmediate(tag, 0);
    }

    @Override
    public void toDetailByTitle(String title, String url) {
//        Toast.makeText(MainActivity.this, "url => " + title, Toast.LENGTH_SHORT).show();
        if (title.equals("聯絡young")) {
            replaceFragment(new ContactFragment(), "contact", true);
        } else {
            replaceFragment(ServiceDetailFragment.newInstance(title, url), "detail", true);
        }
    }


}
