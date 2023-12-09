package com.example.destinyone;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.destiny_one.R;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class MainActivity extends AppCompatActivity {

    private static final int IMAGE_WIDTH = 800;
    private static final int IMAGE_HEIGHT = 600;

    private HorizontalScrollView horizontalScrollView;
    private LinearLayout imageContainer;
    private Button loadMoreButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Initialize Picasso (add this line)
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        Picasso.setSingletonInstance(built);

        horizontalScrollView = findViewById(R.id.horizontalScrollView);
        imageContainer = findViewById(R.id.imageContainer);
        loadMoreButton = findViewById(R.id.loadMoreButton);

        loadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fetch more images when the button is clicked
                new FetchImagesTask().execute();
            }
        });

        // Initial loading of images
        new FetchImagesTask().execute();
    }

    // Retrofit Service Interface
    interface UnsplashService {
        @GET("photos/random?count=5&client_id=dilo0Mjj1g9zF6Plc82M-NLpE4dHKdeP4HCExjUZV7U")
        Call<List<UnsplashPhoto>> getRandomPhotos();
    }

    // Data class for Unsplash Photo
    static class UnsplashPhoto {
        JSONObject urls;

        public String getImageUrl() {
            try {
                return urls.getString("regular");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private class FetchImagesTask extends AsyncTask<Void, Void, List<UnsplashPhoto>> {

        @Override
        protected List<UnsplashPhoto> doInBackground(Void... voids) {
            try {
                // Use Retrofit to fetch a list of images
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://api.unsplash.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                UnsplashService service = retrofit.create(UnsplashService.class);
                Call<List<UnsplashPhoto>> call = service.getRandomPhotos();

                return call.execute().body();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<UnsplashPhoto> photos) {
            if (photos != null) {
                // Process the response and add images to the layout
                for (UnsplashPhoto photo : photos) {
                    String imageUrl = photo.getImageUrl();

                    if (imageUrl != null) {
                        // Create ImageView
                        ImageView imageView = new ImageView(MainActivity.this);
                        imageView.setLayoutParams(new LinearLayout.LayoutParams(IMAGE_WIDTH, IMAGE_HEIGHT));

                        // Ensure Picasso is initialized
                        Picasso.get().load(imageUrl).into(imageView);

                        // Add ImageView to the container
                        imageContainer.addView(imageView);
                    }
                }
            }
        }
    }
}
