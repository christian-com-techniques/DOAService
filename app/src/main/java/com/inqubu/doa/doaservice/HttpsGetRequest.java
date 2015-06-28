package com.inqubu.doa.doaservice;

import android.os.AsyncTask;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.ClientProtocolException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import android.util.Log;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import android.content.Context;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import javax.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.conn.params.*;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.conn.ClientConnectionManager;
import java.security.KeyStore;
import org.apache.http.conn.ssl.SSLSocketFactory;
import java.net.URLEncoder;

/**
 * Created by VORHECHR on 17.04.2015.
 */
public class HttpsGetRequest extends AsyncTask<String, Void, String> {

    static boolean needupdate = true;

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = getNewHttpClient();
            HttpGet httpget = null;

            try {
                httpget = new HttpGet("https://78.46.93.179:3000/?value=" + URLEncoder.encode(params[0], "UTF-8"));
                HttpResponse response;
                response = httpclient.execute(httpget);
                Log.i("Praeda",response.getStatusLine().toString());

            } catch (Exception e) {
                System.out.println("NO INTERNET CONNECTION...");
                //e.printStackTrace();
            }
            return "";
        }

        protected void onPostExecute(String param) {
        }

    public static HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, "HTTP.UTF_8");

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }


}