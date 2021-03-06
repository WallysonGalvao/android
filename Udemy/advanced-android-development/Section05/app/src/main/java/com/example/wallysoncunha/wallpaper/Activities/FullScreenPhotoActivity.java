package com.example.wallysoncunha.wallpaper.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wallysoncunha.wallpaper.Models.Photo;
import com.example.wallysoncunha.wallpaper.R;
import com.example.wallysoncunha.wallpaper.Utils.Functions;
import com.example.wallysoncunha.wallpaper.Utils.GlideApp;
import com.example.wallysoncunha.wallpaper.WebServices.ApiInterface;
import com.example.wallysoncunha.wallpaper.WebServices.ServiceGenerator;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Wallyson Galvão on 30/08/2018.
 */

public class FullScreenPhotoActivity extends AppCompatActivity {

    private final String TAG = FullScreenPhotoActivity.class.getSimpleName();

    @BindView(R.id.activity_fullscreen_photo_photo)
    ImageView fullscreenPhoto;

    @BindView(R.id.activity_fullscreen_photo_user_avatar)
    CircleImageView userAvatar;

    @BindView(R.id.activity_fullscreen_photo_fab_menu)
    FloatingActionMenu fabMenu;

    @BindView(R.id.activity_fullscreen_photo_fab_favorite)
    FloatingActionButton fabFavorite;

    @BindView(R.id.activity_fullscreen_photo_fab_wallpaper)
    FloatingActionButton fabWallpaper;

    @BindView(R.id.activity_fullscreen_photo_username)
    TextView username;

    private Bitmap photoBitmap;

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_photo);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        unbinder = ButterKnife.bind(this);
        Intent intent = getIntent();
        String photoId = intent.getStringExtra("photoId");
        getPhoto(photoId);
    }

    private void getPhoto(String photoId) {
        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class);
        Call<Photo> call = apiInterface.getPhoto(photoId);
        call.enqueue(new Callback<Photo>() {
            @Override
            public void onResponse(Call<Photo> call, Response<Photo> response) {
                if (response.isSuccessful()) {
                    Photo photo = response.body();
                    updateUI(photo);
                } else {
                    Log.e(TAG, "fail " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Photo> call, Throwable t) {
                Log.e(TAG, "fail " + t.getMessage());
            }
        });
    }

    private void updateUI(Photo photo) {

        try {
            username.setText(photo.getUser().getUsername());

            GlideApp
                    .with(FullScreenPhotoActivity.this)
                    .load(photo.getUser().getProfileImage().getSmall())
                    .into(userAvatar);

            GlideApp
                    .with(FullScreenPhotoActivity.this)
                    .asBitmap()
                    .load(photo.getUrl().getRegular())
                    .centerCrop()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            fullscreenPhoto.setImageBitmap(resource);
                            photoBitmap = resource;
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.activity_fullscreen_photo_fab_favorite)
    public void setFabFavorite() {
        fabMenu.close(true);
    }

    @OnClick(R.id.activity_fullscreen_photo_fab_wallpaper)
    public void setFabWallpaper() {
        if (photoBitmap != null) {
            if (Functions.setWallpaper(FullScreenPhotoActivity.this, photoBitmap)) {
                Toast.makeText(FullScreenPhotoActivity.this, "Set Wallpaper Successfully", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(FullScreenPhotoActivity.this, "Set Wallpaper Fail", Toast.LENGTH_LONG).show();
            }
        }
        fabMenu.close(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
