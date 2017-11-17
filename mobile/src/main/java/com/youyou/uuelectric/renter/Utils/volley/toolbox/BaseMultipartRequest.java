package com.youyou.uuelectric.renter.Utils.volley.toolbox;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//import org.apache.http.entity.mime.HttpMultipartMode;

public abstract class BaseMultipartRequest<T> extends Request<T> {
    MultipartEntityBuilder entity = MultipartEntityBuilder.create();
    HttpEntity httpentity;
    private static final String FILE_PART_NAME = "file";

    private final Response.Listener<T> mListener;
    private final Map<String, File> mFilePart;
    private final Map<String, String> mStringPart;

    public BaseMultipartRequest(String url, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);

        mListener = listener;
        mFilePart = new HashMap<String, File>();
        this.mStringPart = new HashMap<String, String>();
        entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
    }

    Map<String, String> header;

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public void addStringBody(String param, String value) {
        mStringPart.put(param, value);
    }

    public void addFileBody(String param, File value) {
        mFilePart.put(param, value);
    }


    public void buildMultipartEntity() {
//        entity.addPart(FILE_PART_NAME, new FileBody(mFilePart));
        try {
            for (Map.Entry<String, File> entry : mFilePart.entrySet()) {
                entity.addBinaryBody(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, String> entry : mStringPart.entrySet()) {
                entity.addTextBody(entry.getKey(), entry.getValue());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getBodyContentType() {
        return httpentity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            httpentity = entity.build();
            httpentity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }
}
