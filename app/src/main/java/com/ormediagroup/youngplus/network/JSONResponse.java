package com.ormediagroup.youngplus.network;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import com.ormediagroup.youngplus.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;

/**
 * Created by Vivienne on 2016/1/7.
 * Edited by Pan on 2018/07/26
 * Edited by Lau on 2019/05/30
 */

public class JSONResponse {
    public interface onComplete {
        void onComplete(JSONObject json);
    }

    public interface JSONResponseComplete {
        void onComplete(JSONObject json, boolean netError);
    }

    private Context context;
    private String url;
    private String data;
    private onComplete listener;

    private Map<String, String> params;
    private Map<String, File> files;
    private JSONResponseComplete newListener;

    //  private JSONObject jsonObj;
    public JSONResponse(Context c, String u, onComplete cb) {
        context = c;
        url = u;
        listener = cb;
        getJsonAsync();
    }

    public JSONResponse(Context c, String u, String d, onComplete cb) {
        context = c;
        url = u;
        listener = cb;
        data = d;
        getJsonAsync();
    }

    public JSONResponse(Context c, String u) {
        context = c;
        url = u;
    }

    public JSONResponse(Context c, String u, Map<String, String> params, JSONResponseComplete cb) {
        init(c, u, params, null, cb);
    }

    public JSONResponse(Context c, String u, Map<String, String> params, Map<String, File> files, JSONResponseComplete cb) {
        init(c, u, params, files, cb);
    }

    private void init(Context c, String u, Map<String, String> params, Map<String, File> files, JSONResponseComplete cb) {
        this.context = c;
        this.url = u;
        this.newListener = cb;
        this.params = params;
        this.files = files;
        getJsonAsyncByMap();
    }


    public void getJsonAsync() {

        if (!TextUtils.isEmpty(data)) {
            asyncNetwork network = (asyncNetwork) new asyncNetwork(new asyncNetwork.OnAsyncTaskCompleted() {
                @Override
                public void onAsyncTaskCompleted(String response) {
                    JSONObject jsonObj = new JSONObject();
                    try {
                        jsonObj = new JSONObject(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (context != null) {
                            Toast.makeText(context, R.string.connection_error, Toast.LENGTH_LONG).show();
                        }
                    }
                    listener.onComplete(jsonObj);
                }
            }, this.context, url, data).execute();
        } else {
            asyncNetwork network = (asyncNetwork) new asyncNetwork(new asyncNetwork.OnAsyncTaskCompleted() {
                @Override
                public void onAsyncTaskCompleted(String response) {
                    JSONObject jsonObj = new JSONObject();
                    try {
                        jsonObj = new JSONObject(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (context != null) {
                            Toast.makeText(context, R.string.connection_error, Toast.LENGTH_LONG).show();
                        }
                    }
                    listener.onComplete(jsonObj);
                }
            }, this.context, url).execute();
        }

    }

    private void getJsonAsyncByMap() {
        new asyncNetwork(new asyncNetwork.OnAsyncNetworkCompleted() {
            @Override
            public void onAsyncTaskCompleted(String response, int responseCode) {
                Log.i("ORM", "onAsyncTaskCompleted: " + response);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("ORM", "onAsyncTaskCompleted: rc = " + responseCode);
                newListener.onComplete(jsonObject, responseCode != 200);
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