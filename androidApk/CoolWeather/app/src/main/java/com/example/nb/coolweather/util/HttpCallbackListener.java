package com.example.nb.coolweather.util;

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}