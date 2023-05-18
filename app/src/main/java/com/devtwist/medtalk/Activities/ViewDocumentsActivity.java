package com.devtwist.medtalk.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.ActivityViewDocumentsBinding;
import com.squareup.picasso.Picasso;

public class ViewDocumentsActivity extends AppCompatActivity {

    private ActivityViewDocumentsBinding binding;

    private Intent intent;

    //get values from intent...
    private Bundle bundle;

    //store image url got from intent...
    private String imageUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewDocumentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


/*
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        setUpAds();
        runAds();

         */

        bundle = getIntent().getExtras();

        if (bundle.getString("tag").equals("i")) {
            imageUrl = bundle.getString("imgUrl");
            Picasso.get().load(imageUrl).placeholder(R.drawable.placholder).into(binding.viewChatImage);
            binding.viewChatImage.setVisibility(ImageView.VISIBLE);
        }

        //show text if tag value equals t...
        else if (bundle.getString("tag").equals("t")){
            binding.textView.setText(bundle.getString("text"));
            binding.textScrollView.setVisibility(View.VISIBLE);
        }

        //show terms and conditions if tag equals c...
        else if (bundle.getString("tag").equals("c")) {
            binding.viewWebView.getSettings().setJavaScriptEnabled(true);
            binding.viewWebView.loadUrl("https://doc-hosting.flycricket.io/medtalk-terms/88c2a964-c6fd-4e91-a422-6c652997e4ec/privacy");
            binding.viewWebView.setVisibility(View.VISIBLE);
        }

        //show privacy policy if tag value equals p...
        else if (bundle.getString("tag").equals("p")) {
            binding.viewWebView.getSettings().setJavaScriptEnabled(true);
            binding.viewWebView.loadUrl("https://doc-hosting.flycricket.io/medtalk/2ad2ce52-8b89-4144-8feb-3f74a9740e1a/privacy");
            binding.viewWebView.setVisibility(View.VISIBLE);
        }

    }


    /*
    private void setUpAds() {
        //initialize ads request...
        AdRequest adRequest = new AdRequest.Builder().build();

        //load ad...
        InterstitialAd.load(this,"ca-app-pub-8385601672345207/4369747004", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i("AdError", loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });
    }

    private void runAds() {
        //start timer to of 7 seconds to load ad properly...
        new CountDownTimer(7000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (mInterstitialAd != null) {
                    //load ad in InterstitialAd...
                    mInterstitialAd.show(ViewDocsActivity.this);
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();

                        }

                    });
                } else {

                }
            }
        }.start();
    }

 */


}