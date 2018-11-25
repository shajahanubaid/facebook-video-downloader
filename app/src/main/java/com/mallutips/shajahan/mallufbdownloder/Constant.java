package com.mallutips.shajahan.mallufbdownloder;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Tushar on 9/10/2017.
 */

class Constant {
    public static int nav_clicked = 0;
    public static Boolean isNavClicked = false;

    public static Boolean isToggle = true;
    public static int color = 0xff3b5998;
    public static int theme = R.style.AppTheme;

    public static class GalleryActivity extends AppCompatActivity {
        Toolbar mToolbar;
        private RecyclerViewAdapter recyclerViewAdapter;
        private RecyclerView recyclerView;
        private File[] files;
        private SwipeRefreshLayout recyclerLayout;
        private final String KEY_RECYCLER_STATE = "recycler_state";
        private static Bundle mBundleRecyclerViewState;
        Parcelable listState;
        private AdView mAdView;
        InterstitialAd mInterstitialAd;
        AdRequest adRequestint;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTheme(theme);
            setContentView(R.layout.activity_gallery);
            initComponents();
            setTitle("Gallery");
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            mToolbar.setBackgroundColor(color);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);

            //ADS
            mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("C69095F3C24675F5F8C9B5031B0E6EEB").build();
            mAdView.loadAd(adRequest);
            setUpRecyclerView();
            loadInterstitialAd();
        }
        private void loadInterstitialAd() {
            adRequestint = new AdRequest.Builder().build();
            mInterstitialAd = new InterstitialAd(getApplicationContext());
            mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad));
            mInterstitialAd.loadAd(adRequestint);
        }
        private void showInterstitial() {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
        }
        private void setUpRecyclerView() {
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewAdapter = new RecyclerViewAdapter(GalleryActivity.this, getData());
            recyclerView.setAdapter(recyclerViewAdapter);
            recyclerViewAdapter.notifyDataSetChanged();
        }

        private ArrayList<Files> getData() {
            ArrayList<Files> filesList = new ArrayList<>();
            Files f;
            String targetPath = Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.FOLDER_NAME;
            File targetDirector = new File(targetPath);
            files = targetDirector.listFiles();
            if (files == null) {
    //            noImageText.setVisibility(View.INVISIBLE);
            }
            try {
                Arrays.sort(files, new Comparator() {
                    public int compare(Object o1, Object o2) {

                        if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                            return -1;
                        } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                            return +1;
                        } else {
                            return 0;
                        }
                    }

                });

                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    f = new Files();
                    f.setName("Saved File: "+(i+1));
                    f.setFilename(file.getName());
                    f.setUri(Uri.fromFile(file));
                    f.setPath(files[i].getAbsolutePath());
                    filesList.add(f);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return filesList;
        }

        private void initComponents() {
            recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            recyclerLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRecyclerView);
            recyclerLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    recyclerLayout.setRefreshing(true);
                    setUpRecyclerView();
                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerLayout.setRefreshing(false);
                            Toast.makeText(GalleryActivity.this, "Refreshed!", Toast.LENGTH_SHORT).show();
                        }
                    }, 2000);

                }
            });
        }
        @Override
        protected void onPause() {
            super.onPause();
            // save RecyclerView state
            if (mBundleRecyclerViewState != null && recyclerView != null) {
                Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
                if (recyclerView.getLayoutManager() != null) {
                    recyclerView.getLayoutManager().onRestoreInstanceState(listState);
                }
            }
        }

        @Override
        protected void onResume() {
            super.onResume();
            // restore RecyclerView state
            if (mBundleRecyclerViewState != null) {
                Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
                recyclerView.getLayoutManager().onRestoreInstanceState(listState);
            }
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()){
                case android.R.id.home:
                    onBackPressed();
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onBackPressed() {
            super.onBackPressed();
            showInterstitial();
        }

    }
}
