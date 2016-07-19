package org.codeforcoffee.oauthinsta;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static String mAccessToken;
    private ImageView mRecentImage;
    private OkHttpClient mHttp = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // grab my access token
        mAccessToken = getIntent().getStringExtra("accessToken");
        // set my imageview
        mRecentImage = (ImageView) findViewById(R.id.image_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Request tagReq = new Request.Builder()
                        .url("https://api.instagram.com/v1/tags/magdagram?access_token=" + mAccessToken)
                        .build();

                mHttp.newCall(tagReq).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData = response.body().string();
                        Log.i("HASHTAG", responseData);

                    }
                });

                Request req = new Request.Builder()
                        .url("https://api.instagram.com/v1/users/self/media/recent/?access_token=" + mAccessToken)
                        .build();

                mHttp.newCall(req).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        // get our leet datas
                        String responseBody = response.body().string();
                        Log.i("JSON", responseBody);
                        try {
                            JSONObject results = new JSONObject(responseBody);
                            JSONArray dataArray = results.getJSONArray("data");
                            JSONObject data = dataArray.getJSONObject(0);
                            final JSONObject image = data.getJSONObject("images");
                            JSONObject standardResolutionImage = image.getJSONObject("standard_resolution");
                            final String imageUrl = standardResolutionImage.getString("url");

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Picasso.with(MainActivity.this).load(imageUrl).into(mRecentImage);
                                }
                            });

                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                Snackbar.make(view, "Fetching most recent image...", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
