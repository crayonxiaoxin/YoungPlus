package com.ormediagroup.youngplus.network;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;

/**
 * Created by Vivienne on 2016/1/7.
 * Edited by Pan on 2018/07/26
 * Edited by Lau on 2019/05/23
 */

public class JSONResponse2 {
    public interface onComplete {
        void onComplete(JSONObject json, boolean netError);
    }

    private Context context;
    private String url;
    private Map<String, String> params;
    private Map<String, File> files;
    private onComplete listener;

    public JSONResponse2(Context c, String u, onComplete cb) {
        init(c, u, null, null, cb);
    }

    public JSONResponse2(Context c, String u, Map<String, String> params, onComplete cb) {
        init(c, u, params, null, cb);
    }

    public JSONResponse2(Context c, String u, Map<String, String> params, Map<String, File> files, onComplete cb) {
        init(c, u, params, files, cb);
    }

    private void init(Context c, String u, Map<String, String> params, Map<String, File> files, onComplete cb) {
        this.context = c;
        this.url = u;
        this.listener = cb;
        this.params = params;
        this.files = files;
        getJsonAsync();
    }

    private void getJsonAsync() {
        new asyncNetwork2(new asyncNetwork2.OnAsyncTaskCompleted() {
            @Override
            public void onAsyncTaskCompleted(String response, int responseCode) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                listener.onComplete(jsonObject, responseCode != 200);
            }
        }, this.context, this.url, this.params, this.files).execute();

    }

    public JSONObject getJsonObj(int i, String responseText) {
        JSONObject c = null;
        if (responseText != null) {
            try {
                JSONObject jsonObj = new JSONObject(responseText);
                JSONArray unitPt = jsonObj.getJSONArray("data");
                c = unitPt.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return c;
    }

}