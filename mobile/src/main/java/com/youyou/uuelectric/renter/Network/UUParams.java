/*
 * Copyright (C) 2013 www.418log.org Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.youyou.uuelectric.renter.Network;

import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// : Auto-generated Javadoc

/**
 * Copyright (c) 2012 All rights reserved 名称：AbRequestParams.java 描述：Http请求参数
 *
 * @author zhaoqp
 * @version v1.0
 * @date：2013-11-13 上午10:28:55
 */
public class UUParams {
    /**
     * The Constant TAG.
     */
    private static final String TAG = "AbRequestParams";

    public LinkedHashMap<String, String> getUrlParams() {
        return urlParams;
    }

    public ConcurrentHashMap<String, FileWrapper> getFileParams() {
        return fileParams;
    }


    public void setFileParams(ConcurrentHashMap<String, FileWrapper> fileParams) {
        this.fileParams = fileParams;
    }

    /**
     * url参数.
     */
    protected LinkedHashMap<String, String> urlParams;
    /**
     * 文件参数.
     */
    protected ConcurrentHashMap<String, FileWrapper> fileParams;


    /**
     * 构造一个空的请求参数.
     */
    public UUParams() {
        init();
    }

    /**
     * 用一个map构造请求参数
     *
     * @param source
     */
    public UUParams(Map<String, String> source) {
        init();
        for (Map.Entry<String, String> entry : source.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 用一个key和value构造请求参数
     *
     * @param key
     * @param value
     */
    public UUParams(String key, String value) {
        init();
        put(key, value);
    }

    /**
     * 初始化.
     */
    private void init() {
        urlParams = new LinkedHashMap<String, String>();
        fileParams = new ConcurrentHashMap<String, FileWrapper>();
    }

    /**
     * 增加一对请求参数.
     *
     * @param key
     * @param value
     */
    public void put(String key, String value) {
        if (key != null && value != null) {
            urlParams.put(key, value);
        }
    }

    /**
     * 增加一个文件域.
     *
     * @param key
     * @param file
     * @param contentType the content type of the file, eg. application/json
     */
    public void put(String key, File file, String contentType) {
        if (key != null && file != null) {
            fileParams.put(key, new FileWrapper(file, contentType));
        }
    }

    /**
     * 增加一个文件域.
     *
     * @param key
     * @param file
     */
    public void put(String key, File file) {
        put(key, file, "application/octet-stream");
    }

    /**
     * 删除一个请求参数.
     *
     * @param key
     */
    public void remove(String key) {
        urlParams.remove(key);
        fileParams.remove(key);
    }

    /**
     * 描述：转换为参数字符串
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
            if (result.length() > 0)
                result.append("&");
            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }
        return result.toString();
    }

    /**
     * 获取参数列表.
     *
     * @return the params list
     */
    public List<BasicNameValuePair> getParamsList() {
        List<BasicNameValuePair> lparams = new LinkedList<BasicNameValuePair>();
        for (LinkedHashMap.Entry<String, String> entry : urlParams.entrySet()) {
            lparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return lparams;
    }


    private List<Map.Entry<String, String>> mHashMapEntryList;


    public void setUrlParams(LinkedHashMap<String, String> urlParams) {
        this.urlParams = urlParams;
    }

    /**
     * 文件类.
     */
    public static class FileWrapper {
        /**
         * The file.
         */
        public File file;
        /**
         * The content type.
         */
        public String contentType;

        /**
         * 构造.
         *
         * @param file        the file
         * @param contentType the content type
         */
        public FileWrapper(File file, String contentType) {
            this.file = file;
            this.contentType = contentType;
        }
    }
}
