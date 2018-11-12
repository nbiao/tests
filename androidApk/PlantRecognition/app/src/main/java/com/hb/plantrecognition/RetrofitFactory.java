package com.hb.plantrecognition;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by 译丹 on 2017/5/13.
 */

public class RetrofitFactory {

    static private final String KEYSTORE_ALIAS = "wildfly";
    static private final int CERTIFICATE_RESOURCE_ID = R.raw.wildfly;
    static private final String CERTIFICATE_TYPE = "X.509";
    static private final String SSL_PROTOCOL = "TLS";

    static private final String HOST_PROTOCOL = "https";
    static private final String HOST_NAME = "hubo-hb.vicp.io";
    static private final String HOST_PORT = "12496";
    static private final String HOST_PATH = "PlantRecognition/rest/";

    static private PlantRecognitionService mRetrofitService = null;

    static public PlantRecognitionService getRetrofitService() {
        return mRetrofitService;
    }

    static public void init(Context ctx) {
        try {
            SSLSocketFactory ssl = getSSLSocketFactory(ctx);
            OkHttpClient.Builder client = getHttpClient(ssl);
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(getGson()))
                    .baseUrl(HOST_PROTOCOL + "://" + HOST_NAME + ":" + HOST_PORT + "/" + HOST_PATH)
                    .client(client.build())
                    .build();
            mRetrofitService = retrofit.create(PlantRecognitionService.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static private OkHttpClient.Builder getHttpClient(SSLSocketFactory ssl) {
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

    static private SSLSocketFactory getSSLSocketFactory(Context ctx) throws Exception {
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


    static private Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("yyyy-MM-dd hh:mm:ss");
        gsonBuilder.registerTypeAdapter(Timestamp.class, new TimestampTypeAdapter());
        return gsonBuilder.create();
    }

    static private class TimestampTypeAdapter
            implements JsonSerializer<Timestamp>, JsonDeserializer<Timestamp> {

        public JsonElement serialize(Timestamp ts, Type t, JsonSerializationContext jsc) {
            return new JsonPrimitive(ts.getTime());
        }

        public Timestamp deserialize(JsonElement json, Type t, JsonDeserializationContext jsc) throws JsonParseException {
            if (!(json instanceof JsonPrimitive)) {
                throw new JsonParseException("The date should be a string value");
            }
            return new Timestamp(Long.parseLong(json.getAsString()));
        }
    }
}
