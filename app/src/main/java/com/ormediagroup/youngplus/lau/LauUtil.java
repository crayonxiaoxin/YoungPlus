package com.ormediagroup.youngplus.lau;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ormediagroup.youngplus.MainActivity;
import com.ormediagroup.youngplus.R;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Lau on 2018/11/27.
 */

public class LauUtil {

    public static int getScreenWidth(Context context) {
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        return dm.heightPixels;
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static String formatHTML(String bodyString) {
        String w3Css = "<link rel=\"stylesheet\" href=\"www/w3.css\" type=\"text/css\">";
        String styleCss = "<link rel=\"stylesheet\" href=\"www/style.css\" type=\"text/css\">";
        String myCss = "<link rel=\"stylesheet\" href=\"www/webview.css\" type=\"text/css\">";
        String prefix = "<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">" + w3Css + styleCss + myCss + "</head><body>";
        String suffix = "</body></html>";
        return prefix + bodyString + suffix;
    }

    public static String getLegalURL(String url) {
        if (url.substring(0, 7).equals("http://") || url.substring(0, 8).equals("https://")) {
            return url;
        } else {
            return "http://" + url;
        }
    }

    public static Spanned HTMLTagDecode(String str) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(str);
        }
    }

    public static boolean isNull(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }

    public static boolean isEmail(String email) {
        String regex = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
        return Pattern.matches(regex, email);
    }

    public static boolean isPhone(String phone) {
        return !(phone.contains(" ") || phone.length() < 8 || phone.length() > 11);
    }

}
