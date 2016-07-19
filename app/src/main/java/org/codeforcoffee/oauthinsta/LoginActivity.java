package org.codeforcoffee.oauthinsta;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // find our webview
        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            @SuppressWarnings("deprecation")    // triggers false api check
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("code=")) {
                    Log.i("URL", url);
                    int codeStartingPoint = url.indexOf("=") + 1;
                    String accessCode = url.substring(codeStartingPoint);
                    Log.i("ACCESSTOKEN", accessCode);
                    getAccessToken(accessCode); // hand off access token
                    return true;
                } else {
                    return false;
                }
            }
        }); //end webview setup
        mWebView.loadUrl("https://instagram.com/oauth/authorize/?client_id="+ InstagramIdentity.CLIENT_ID +"&redirect_uri="+ InstagramIdentity.CALLBACK_URL  +"&response_type=code&scope=public_content");

    }// end oncreate

    private void getAccessToken(String code){
        Log.d(LoginActivity.class.getName(),"Trying to get access token");
        final OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("client_secret", InstagramIdentity.CLIENT_SECRET)
                .add("client_id",InstagramIdentity.CLIENT_ID)
                .add("grant_type","authorization_code")
                .add("redirect_uri", InstagramIdentity.CALLBACK_URL)
                .add("code",code)
                .build();

        Request request = new Request.Builder()
                .url("https://api.instagram.com/oauth/access_token")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Headers responseHeaders = response.headers();
                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                    System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }

                String responseBody = response.body().string();
                Log.d(LoginActivity.class.getName(),responseBody);
                try {
                    JSONObject result = new JSONObject(responseBody);
                    JSONObject user = result.getJSONObject("user");

                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    intent.putExtra("accessToken",result.getString("access_token"));
                    intent.putExtra("userId",user.getString("id"));
                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
