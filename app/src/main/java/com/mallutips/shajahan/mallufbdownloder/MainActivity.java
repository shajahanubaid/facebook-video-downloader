package com.mallutips.shajahan.mallufbdownloder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.capitalize;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "EXAMPLE";
    @SuppressLint("StaticFieldLeak")
    private WebView webo;
    private AdView mAdView;
    private FirebaseAnalytics mFirebaseAnalytics;
    Toolbar mToolbar;
    private static String URL = "https://www.facebook.com/login/";
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private Button streamButton, downloadButton, cancelButton;
    private ProgressBar progress;
    private SwipeRefreshLayout recyclerLayout;
    private RewardedVideoAd mRewardedVideoAd;
    ProgressDialog mProgressDialog;
    String fileN = null;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 123;
    boolean result;
    String urlString;
    Drawer resultDrawer;
    AccountHeader headerResult;
    NativeExpressAdView adNativeView, largeNativeAd;
    VideoController mVideoController, largeMVideoController;
    Dialog main_dialog, downloadDialog;

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Constant.theme);
        setContentView(R.layout.activity_main);




        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setBackgroundColor(Constant.color);
        setSupportActionBar(mToolbar);

        if (getAppIntro(this)) {
            Intent i = new Intent(this, IntroActivity.class);
            startActivity(i);
        }
        progress = (ProgressBar) findViewById(R.id.progressBar);
        progress.setVisibility(View.GONE);
        result = checkPermission();
        if(result){
            checkFolder();
        }
        if (!isConnectingToInternet(this)) {
            Toast.makeText(this, "Please Connect to Internet", Toast.LENGTH_LONG).show();
        }

        ColorDrawable cd = new ColorDrawable(Constant.color);
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(cd)
                .withSelectionListEnabledForSingleProfile(false)
                .withAlternativeProfileHeaderSwitching(false)
                .withCompactStyle(false)
                .withDividerBelowHeader(false)
                .withProfileImagesVisible(true)
                .addProfiles(new ProfileDrawerItem().withIcon(R.mipmap.ic_launcher).withName(getResources().getString(R.string.app_name)).withEmail(getResources()
                        .getString(R.string.blog)))
                .build();
        resultDrawer = new DrawerBuilder()
                .withActivity(this)
                .withSelectedItem(-1)
                .withFullscreen(true)
                .withAccountHeader(headerResult)
                .withActionBarDrawerToggle(true)
                .withCloseOnClick(true)
                .withMultiSelect(false)
                .withTranslucentStatusBar(true)
                .withToolbar(mToolbar)
                .addDrawerItems(
                        new PrimaryDrawerItem().withSelectable(false).withName(R.string.app_name),
                        new PrimaryDrawerItem().withSelectable(false).withName("Gallery").withIcon(R.drawable.ic_if_art_gallery_103283).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                            @Override
                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
                                startActivity(intent);
                                return false;
                            }
                        }),
                        new PrimaryDrawerItem().withSelectable(false).withName("Settings").withIcon(R.drawable.ic_settings).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                            @Override
                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(intent);
                                return false;
                            }
                        }),
                        new PrimaryDrawerItem().withSelectable(false).withName("Recommend to Friends").withIcon(R.drawable.ic_collaboration).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                            @Override
                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                final String shareappPackageName = getPackageName();
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out " + getResources().getString(R.string.app_name) + "at:https://tipze.blogspot.com/2018/11/facebook-video-downloader-android.html ");
                                sendIntent.setType("text/plain");
                                startActivity(sendIntent);
                                return false;
                            }
                        }),
                        new PrimaryDrawerItem().withSelectable(false).withName(R.string.abut),
                        new PrimaryDrawerItem().withSelectable(false).withName(R.string.app),

                        new PrimaryDrawerItem().withSelectable(false).withName("Developer Whatsapp").withIcon(R.drawable.ic_if_whatsapp_3721678).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                            @Override
                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                final String appPackageName = getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=919497199987")));
                                } catch (ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=919497199987")));
                                }
                                return false;
                            }


                        }),
                        new PrimaryDrawerItem().withSelectable(false).withName("Developer Blog").withIcon(R.drawable.ic_if_bloger_245981).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                            @Override
                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                final String appPackageName = getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://tipze.blogspot.com/")));
                                } catch (ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://tipze.blogspot.com/")));
                                }
                                return false;
                            }



                        }),
                        new PrimaryDrawerItem().withSelectable(false).withName("Developer Github").withIcon(R.drawable.ic_github).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                            @Override
                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                final String appPackageName = getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/shajahanubaid")));
                                } catch (ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/shajahanubaid")));
                                }
                                return false;
                            }


                        }),
                        new PrimaryDrawerItem().withSelectable(false).withName("Developer Facebook").withIcon(R.drawable.ic_if_facebook_circle_color_107175).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                            @Override
                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                final String appPackageName = getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/redlove.nimbuzz")));
                                } catch (ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/redlove.nimbuzz")));
                                }
                                return false;
                            }


                        }),
                        new PrimaryDrawerItem().withSelectable(false).withName(R.string.app),
                        new PrimaryDrawerItem().withSelectable(false).withName("Rate Us").withIcon(R.drawable.ic_thumb_up_black_24dp).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                            @Override
                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                final String appPackageName = getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://tipze.blogspot.com/2018/11/facebook-video-downloader-android.html")));
                                } catch (ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://tipze.blogspot.com/2018/11/facebook-video-downloader-android.html")));
                                }
                                return false;
                            }
                        }),

                        new PrimaryDrawerItem().withSelectable(false).withName("Feedback").withIcon(R.drawable.ic_feedback_black_24dp).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                            @Override
                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                DisplayMetrics displaymetrics = new DisplayMetrics();
                                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                                int height = displaymetrics.heightPixels;
                                int width = displaymetrics.widthPixels;
                                PackageManager manager = getApplicationContext().getPackageManager();
                                PackageInfo info = null;
                                try {
                                    info = manager.getPackageInfo(getPackageName(), 0);
                                } catch (PackageManager.NameNotFoundException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                String version = info.versionName;
                                Intent i = new Intent(Intent.ACTION_SEND);
                                i.setType("message/rfc822");
                                i.putExtra(Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.developer_email)});
                                i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name) + version);
                                i.putExtra(Intent.EXTRA_TEXT,
                                        "\n" + " Device :" + getDeviceName() +
                                                "\n" + " System Version:" + Build.VERSION.SDK_INT +
                                                "\n" + " Display Height  :" + height + "px" +
                                                "\n" + " Display Width  :" + width + "px" +
                                                "\n\n" + "Have a problem? Please share it with us and we will do our best to solve it!" +
                                                "\n");
                                startActivity(Intent.createChooser(i, "Send Email"));
                                return false;
                            }
                        }),
                        new PrimaryDrawerItem().withSelectable(false).withName("Exit").withIcon(R.drawable.ic_exit_to_app_black_24dp).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                            @Override
                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                finish();
                                return false;
                            }
                        })
                ).
                        withSavedInstance(savedInstanceState)
                .build();

    }

    private boolean getAppIntro(MainActivity mainActivity) {
        SharedPreferences preferences;
        preferences = mainActivity.getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);
        return preferences.getBoolean("AppIntro", true);
    }

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    @JavascriptInterface
    public void processVideo(String str, String str2) {
        Log.e("WEBVIEWJS", "RUN");
        Log.e("WEBVIEWJS", str);
        Bundle args = new Bundle();
        args.putString("vid_data", str);
        urlString = str;
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // put your code to show dialog here.
                LayoutInflater dialogLayout = LayoutInflater.from(MainActivity.this);
                View DialogView = dialogLayout.inflate(R.layout.dialog_download, null);
                main_dialog = new Dialog(MainActivity.this, R.style.MyDialogTheme);
                main_dialog.setContentView(DialogView);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(main_dialog.getWindow().getAttributes());
                lp.width = (getResources().getDisplayMetrics().widthPixels);
                lp.height = (int)(getResources().getDisplayMetrics().heightPixels*0.65);
                main_dialog.getWindow().setAttributes(lp);
                streamButton = (Button) DialogView.findViewById(R.id.streamButton);
                downloadButton = (Button)DialogView.findViewById(R.id.downloadButton);
                cancelButton = (Button) DialogView.findViewById(R.id.cancelButton);
                // Set its video options.
                adNativeView = (NativeExpressAdView)DialogView.findViewById(R.id.nativeAD);
                adNativeView.setVideoOptions(new VideoOptions.Builder()
                        .setStartMuted(true)
                        .build());

                // The VideoController can be used to get lifecycle events and info about an ad's video
                // asset. One will always be returned by getVideoController, even if the ad has no video
                // asset.
                mVideoController = adNativeView.getVideoController();
                mVideoController.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                    @Override
                    public void onVideoEnd() {
                        Log.d(LOG_TAG, "Video playback is finished.");
                        super.onVideoEnd();
                    }
                });

                // Set an AdListener for the AdView, so the Activity can take action when an ad has finished
                // loading.
                adNativeView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        if (mVideoController.hasVideoContent()) {
                            Log.d(LOG_TAG, "Received an ad that contains a video asset.");
                        } else {
                            Log.d(LOG_TAG, "Received an ad that does not contain a video asset.");
                        }
                    }
                });


                adNativeView.loadAd(new AdRequest.Builder().addTestDevice("0224C93FFD644350DCD7F3D7557C6A5C").build());

                main_dialog.setCancelable(false);
                main_dialog.setCanceledOnTouchOutside(false);
                main_dialog.show();

//                dialogBuilder = new AlertDialog.Builder(MainActivity.this , R.style.MyDialogTheme);
//                View view = getLayoutInflater().inflate(R.layout.dialog_download, null);
//                streamButton = (Button) view.findViewById(R.id.streamButton);
//                downloadButton = (Button)view.findViewById(R.id.downloadButton);
//                cancelButton = (Button) view.findViewById(R.id.cancelButton);
//                adNativeView = (NativeExpressAdView)view.findViewById(R.id.nativeAD);
//                dialogBuilder.setView(view);
//                dialog = dialogBuilder.create();
//                adNativeView = (NativeExpressAdView)view.findViewById(R.id.nativeAD);
//                AdRequest request = new AdRequest.Builder()
//                        .addTestDevice("C69095F3C24675F5F8C9B5031B0E6EEB")
//                        .build();
//                adNativeView.loadAd(request);
//                dialog.show();
                streamButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "Streaming", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MainActivity.this, VideoPlayer.class);
                        intent.putExtra("video_url",urlString);
                        startActivity(intent);
                        main_dialog.dismiss();
                    }
                });
                downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "Downloading", Toast.LENGTH_SHORT).show();
                        newDownload(urlString);
                        main_dialog.dismiss();
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        main_dialog.dismiss();
                    }
                });
            }
        });
    }

    public void setValue(int progress) {
        this.progress.setProgress(progress);
        if (progress>=100) // code to be added
            this.progress.setVisibility(View.GONE);
    }

    private void createPopupDialog() {
    }

    public static boolean isConnectingToInternet(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_go_to_fb:
                gotoFB();
                break;
            case R.id.action_refresh:
                if(webo != null){
                    webo.reload();
                }
                break;
            default:
                break;
        }
        return true;
    }

    public void gotoFB(){
        webo = (WebView) findViewById(R.id.webViewFb);
        webo.getSettings().setJavaScriptEnabled(true);
        webo.addJavascriptInterface(this, "FBDownloader");
        webo.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progress.setVisibility(View.VISIBLE);
                MainActivity.this.setValue(newProgress);
                super.onProgressChanged(view, newProgress);
            }
        });
        webo.setWebViewClient(new WebViewClient() {



            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                progress.setVisibility(View.VISIBLE);
//                MainActivity.this.progress.setProgress(0);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                MainActivity.this.webo.loadUrl("javascript:(function() { var el = document.querySelectorAll('div[data-sigil]');for(var i=0;i<el.length; i++){var sigil = el[i].dataset.sigil;if(sigil.indexOf('inlineVideo') > -1){delete el[i].dataset.sigil;var jsonData = JSON.parse(el[i].dataset.store);el[i].setAttribute('onClick', 'FBDownloader.processVideo(\"'+jsonData['src']+'\");');}}})()");
                Log.e("WEBVIEWFIN", url);
                progress.setVisibility(View.GONE);
//                MainActivity.this.progress.setProgress(100);
                super.onPageFinished(view, url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                MainActivity.this.webo.loadUrl("javascript:(function prepareVideo() { var el = document.querySelectorAll('div[data-sigil]');for(var i=0;i<el.length; i++){var sigil = el[i].dataset.sigil;if(sigil.indexOf('inlineVideo') > -1){delete el[i].dataset.sigil;console.log(i);var jsonData = JSON.parse(el[i].dataset.store);el[i].setAttribute('onClick', 'FBDownloader.processVideo(\"'+jsonData['src']+'\",\"'+jsonData['videoID']+'\");');}}})()");
                MainActivity.this.webo.loadUrl("javascript:( window.onload=prepareVideo;)()");
            }
        });
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        }
        cookieManager.setAcceptCookie(true);
        webo.loadUrl(URL);
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    public class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }
        private NumberProgressBar bnp;
        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                java.net.URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                int fileLength = connection.getContentLength();

                input = connection.getInputStream();
                fileN = "FbDownloader_" + UUID.randomUUID().toString().substring(0, 10) + ".mp4";
                    File filename = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                            Constants.FOLDER_NAME, fileN);
                    output = new FileOutputStream(filename);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    if (fileLength > 0)
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            LayoutInflater dialogLayout = LayoutInflater.from(MainActivity.this);
            View DialogView = dialogLayout.inflate(R.layout.progress_dialog, null);
            downloadDialog = new Dialog(MainActivity.this, R.style.CustomAlertDialog);
            downloadDialog.setContentView(DialogView);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(downloadDialog.getWindow().getAttributes());
            lp.width = (getResources().getDisplayMetrics().widthPixels);
            lp.height = (int)(getResources().getDisplayMetrics().heightPixels*0.65);
            downloadDialog.getWindow().setAttributes(lp);

            final Button cancel = (Button) DialogView.findViewById(R.id.cancel_btn);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //stopping the Asynctask
                    cancel(true);
                    downloadDialog.dismiss();

                }
            });
            largeNativeAd = (NativeExpressAdView) DialogView.findViewById(R.id.adView);
//            largeNativeAd.setVideoOptions(new VideoOptions.Builder()
//                    .setStartMuted(true)
//                    .build());
//            mVideoController = largeNativeAd.getVideoController();
//            largeMVideoController.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
//                @Override
//                public void onVideoEnd() {
//                    Log.d(LOG_TAG, "Video playback is finished.");
//                    super.onVideoEnd();
//                }
//            });
//            largeNativeAd.setAdListener(new AdListener() {
//                @Override
//                public void onAdLoaded() {
//                    if (largeMVideoController.hasVideoContent()) {
//                        Log.d(LOG_TAG, "Received an ad that contains a video asset.");
//                    } else {
//                        Log.d(LOG_TAG, "Received an ad that does not contain a video asset.");
//                    }
//                }
//            });
            largeNativeAd.loadAd(new AdRequest.Builder().addTestDevice("02").build());
            downloadDialog.setCancelable(false);
            downloadDialog.setCanceledOnTouchOutside(false);
            bnp = (NumberProgressBar)DialogView.findViewById(R.id.number_progress_bar);
            bnp.setProgress(0);
            bnp.setMax(100);
            downloadDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            bnp.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            downloadDialog.dismiss();
            if (result != null)
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
            MediaScannerConnection.scanFile(MainActivity.this,
                    new String[]{Environment.getExternalStorageDirectory().getAbsolutePath() +
                            Constants.FOLDER_NAME + fileN}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String newpath, Uri newuri) {
                            Log.i("ExternalStorage", "Scanned " + newpath + ":");
                            Log.i("ExternalStorage", "-> uri=" + newuri);
                        }
                    });

        }
    }

    public void newDownload(String url) {
        final DownloadTask downloadTask = new DownloadTask(MainActivity.this);
        downloadTask.execute(url);
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean checkPermission() {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("Write Storage permission is necessary to Download Images and Videos!!!");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
    public void checkAgain() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
            alertBuilder.setCancelable(true);
            alertBuilder.setTitle("Permission necessary");
            alertBuilder.setMessage("Write Storage permission is necessary to Download Images and Videos!!!");
            alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
                }
            });
            AlertDialog alert = alertBuilder.create();
            alert.show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkFolder();
                } else {
                    //code for deny
                    checkAgain();
                }
                break;
        }
    }

    public void checkFolder() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FBDownloader/";
        File dir = new File(path);
        boolean isDirectoryCreated = dir.exists();
        if (!isDirectoryCreated) {
            isDirectoryCreated = dir.mkdir();
        }
        if (isDirectoryCreated) {
            // do something\
            Log.d("Folder", "Already Created");
        }
    }
}
