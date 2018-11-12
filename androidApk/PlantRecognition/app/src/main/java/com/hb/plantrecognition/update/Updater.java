package com.hb.plantrecognition.update;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.hb.plantrecognition.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by HB on 2017/6/13.
 */

public class Updater {

    static public final int MSG_START = 0;
    static public final int MSG_PROGRESS = 1;
    static public final int MSG_FAILED = 2;
    static public final int MSG_FINISH = 3;


    static private final String KEYSTORE_ALIAS = "wildfly";
    static private final int CERTIFICATE_RESOURCE_ID = R.raw.wildfly;
    static private final String CERTIFICATE_TYPE = "X.509";
    static private final String SSL_PROTOCOL = "TLS";

    static private final String HOST_PROTOCOL = "https";
    static private final String HOST_NAME = "hubo-hb.vicp.io";
    static private final String HOST_PORT = "12496";
    static private final String HOST_PATH = "download/android/plantrecognition/";

    static private final String VERSION_FILE_NAME = "version.json";
    static private final String APK_FILE_NAME = "plantrecognition.apk";

    static private final String DOWNLOAD_PATH = "/download";

    static private final String VERSION_PATH = HOST_PROTOCOL
            + "://" + HOST_NAME
            + ":" + HOST_PORT
            + "/" + HOST_PATH
            + VERSION_FILE_NAME;

    private OkHttpClient mHttpClient;
    private Gson mGson;
    private int mLocalVersion;
    private Version mVersion;

    final static private Updater mUpdaterInstance = new Updater();

    public static Updater getInstance() {
        return mUpdaterInstance;
    }

    private Updater() {
    }

    public void check(Context context) {
        SSLSocketFactory ssl = null;
        try {
            mGson = new Gson();
            ssl = getSSLSocketFactory(context);
            mHttpClient = getHttpClient(ssl).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Context ctx = context.getApplicationContext();

        getLocalVersion(ctx);
        getServerVersion(ctx);
    }

    public void updateApk(final Handler handler) {

        Request request = new Request.Builder().url(mVersion.getUrl()).build();
        Call c = mHttpClient.newCall(request);
        handler.sendEmptyMessage(MSG_START);

        c.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                handler.sendEmptyMessage(MSG_FAILED);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    handler.sendEmptyMessage(MSG_FAILED);
                    return;
                }

                String path = Environment.getExternalStorageDirectory() + DOWNLOAD_PATH;
                File file = new File(path);
                if (!file.exists()) file.mkdir();

                File apkFile = new File(path, APK_FILE_NAME);
                byte[] buf = new byte[1024];
                int len;
                long size = response.body().contentLength();
                long sum = 0;

                try (FileOutputStream fos = new FileOutputStream(apkFile);
                     InputStream is = response.body().byteStream()) {
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum = sum + len;
                        handler.obtainMessage(MSG_PROGRESS, (int) (sum * 100 / size), (int) size).sendToTarget();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(MSG_FAILED);
                    return;
                }

                handler.obtainMessage(MSG_FINISH, apkFile).sendToTarget();
            }
        });
    }

    private void getServerVersion(final Context ctx) {
        Request request = new Request.Builder().url(VERSION_PATH).build();
        Call c = mHttpClient.newCall(request);
        c.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) return;
                mVersion = mGson.fromJson(response.body().charStream(), Version.class);
                if (mLocalVersion >= mVersion.getVersion()) return;

                Intent i = new Intent(ctx, UpdateActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(i);
            }
        });
    }


    private void getLocalVersion(Context ctx) {
        mLocalVersion = -1;
        try {
            mLocalVersion = ctx.getPackageManager().getPackageInfo(
                    ctx.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private OkHttpClient.Builder getHttpClient(SSLSocketFactory ssl) {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.sslSocketFactory(ssl, new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        });
        client.hostnameVerifier((hostname, session) -> hostname.equals(HOST_NAME));
        return client;
    }

    private SSLSocketFactory getSSLSocketFactory(Context ctx) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_TYPE);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);

        try (InputStream is = ctx.getResources().openRawResource(CERTIFICATE_RESOURCE_ID)) {
            ks.setCertificateEntry(KEYSTORE_ALIAS, cf.generateCertificate(is));
        }

        SSLContext sslContext = SSLContext.getInstance(SSL_PROTOCOL);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(ks);
        sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext.getSocketFactory();
    }
}
