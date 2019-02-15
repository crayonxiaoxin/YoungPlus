package com.ormediagroup.youngplus.lau;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Lau on 2019/2/15.
 */

public class User {
    private Context context;
    private SharedPreferences sp;

    public User(Context context) {
        this.context = context;
        initsp();
    }

    private void initsp(){
        sp = context.getSharedPreferences("user_info", MODE_PRIVATE);
    }

    public SharedPreferences getsp(){
        return this.sp;
    }

    public String getUserId(){
        return this.sp.getString("userid","");
    }

    public boolean isUserLoggedIn(){
        return !getUserId().equals("");
    }

}
