package com.ormediagroup.youngplus.network;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;


import com.ormediagroup.youngplus.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Vivienne on 2016/1/7.
 * Edited by Pan on 2018/07/26
 */

public class JSONResponse {
    public interface onComplete {
        void onComplete(JSONObject json);
    }

    private Context context;
    private String url;
    private String data;
    private onComplete listener;

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