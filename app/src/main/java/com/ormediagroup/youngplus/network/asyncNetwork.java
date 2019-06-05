package com.ormediagroup.youngplus.network;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jackymok on 7/1/16.
 * Edited by Pan on 2018/07/26
 * Edited by Lau on 2019/05/30
 */

public class asyncNetwork extends AsyncTask<String, Void, String> {

    public interface OnAsyncTaskCompleted {
        void onAsyncTaskCompleted(String response);
    }

    public interface OnAsyncNetworkCompleted {
        void onAsyncTaskCompleted(String response, int responseCode);
    }

    //  JSONObject jObj = null;
    String json = "";
    Context context;
    String url;
    String data = "";
    private int responseCode = -1;
    private Map<String, String> fields;
    private Map<String, File> files;
    private OnAsyncTaskCompleted listener;
    private OnAsyncNetworkCompleted newListener;

    public asyncNetwork(OnAsyncTaskCompleted callback, Context c, String u) {
        context = c;
        url = u;
        listener = callback;
    }

    public asyncNetwork(OnAsyncTaskCompleted callback, Context c, String u, String d) {
        context = c;
        url = u;
        listener = callback;
        data = d;
    }

    public asyncNetwork(Context c, String u) {
        context = c;
        url = u;
    }

    public asyncNetwork(OnAsyncNetworkCompleted callback, Context c, String u, Map<String, String> params) {
        init(callback, c, u, params, null);
    }

    public asyncNetwork(OnAsyncNetworkCompleted callback, Context c, String u, Map<String, String> params, Map<String, File> files) {
        init(callback, c, u, params, files);
    }

    private void init(OnAsyncNetworkCompleted callback, Context c, String u, Map<String, String> params, Map<String, File> files) {
        this.context = c;
        this.url = u;
        this.fields = params;
        this.files = files;
        this.newListener = callback;
    }


    @Override
    protected String doInBackground(String... params) {
        InputStream myStream;

        if (TextUtils.isEmpty(data) && fields == null) {
            myStream = getStream(url);
        } else if (!TextUtils.isEmpty(data) && fields == null) {
            myStream = getStream(url, data);
        } else {
            myStream = getStream(url, fields, files);
        }

        if (myStream != null) {
            try {
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(myStream));
                String newLine = System.getProperty("line.separator");
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append(newLine);
                }
                json = sb.toString();
            } catch (Exception e) {
                Log.e("Buffer Error", "Error converting result " + e.toString());
            }
        } else {
            Log.e("ORM", "ASyncNetwork doInBackground Exception ");
        }
        return json;
    }


    private InputStream getStream(String u) {
        try {
            URL url = new URL(u);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestProperty("connection", "close");
            return urlConnection.getInputStream();
        } catch (Exception ex) {
            Log.e("ORM", ex.toString());
            return null;
        }
    }

    private InputStream getStream(String u, String data) {
        try {
            URL url = new URL(u);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
            writer.write(data);
            writer.flush();
            urlConnection.setConnectTimeout(15000);
            return urlConnection.getInputStream();
        } catch (Exception ex) {
            Log.e("ORM", ex.toString());
            return null;
        }
    }

    private InputStream getStream(String u, Map<String, String> params, Map<String, File> files) {
        String BOUNDARY = UUID.randomUUID().toString();
        String PREFIX = "--";
        String LINE_END = "\r\n";
        try {
            URL url = new URL(u);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15 * 1000);
            conn.setConnectTimeout(15 * 1000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
            if (params != null) {
                StringBuffer sb = new StringBuffer();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    sb.append(PREFIX);
                    sb.append(BOUNDARY);
                    sb.append(LINE_END);
                    sb.append("Content-Disposition:form-data;name=\"" + entry.getKey() + "\"" + LINE_END);
                    sb.append("Content-Transfer-Encoding:8bit" + LINE_END);
                    sb.append(LINE_END);
                    sb.append(entry.getValue());
                    sb.append(LINE_END);
                }
                DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
                outputStream.write(sb.toString().getBytes());
                if (files != null) {
                    for (Map.Entry<String, File> file : files.entrySet()) {
                        StringBuffer sb1 = new StringBuffer();
                        sb1.append(PREFIX);
                        sb1.append(BOUNDARY);
                        sb1.append(LINE_END);
                        sb1.append("Content-Disposition:form-data;name=\"" + file.getKey() + "\";filename=\"" + file.getValue().getName() + "\"" + LINE_END);
                        sb1.append("Content-Type:application/octet-stream;charset=UTF-8" + LINE_END);
                        sb1.append(LINE_END);
                        outputStream.write(sb1.toString().getBytes());
                        InputStream is = new FileInputStream(file.getValue());
                        byte[] buffer = new byte[1024];
                        int len = 0;
                        while ((len = is.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, len);
                        }
                        is.close();
                        outputStream.write(LINE_END.getBytes());
                    }
                }
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                outputStream.write(end_data);
                outputStream.flush();
            }
            responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode == 200 ? conn.getInputStream() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void onPostExecute(String result) {
        if (listener != null) {
            listener.onAsyncTaskCompleted(result);
        }
        if (newListener != null) {
            newListener.onAsyncTaskCompleted(result, responseCode);
        }
    }

}