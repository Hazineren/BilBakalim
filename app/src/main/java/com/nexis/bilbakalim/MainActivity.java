package com.nexis.bilbakalim;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.vungle.mediation.VungleAdapter;
import com.vungle.mediation.VungleExtrasBuilder;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    private SQLiteDatabase database;
    private Cursor cursor;
    private SQLiteStatement statement;

    private String sqlSorgusu;
    private TextView txtUserHeartCount,txtViewUserName;
    private int heartIndex,nameIndex,heartCount, imgHeartDuration = 2500, sonCanDurumu;

    private RewardedAd mRewardedAd;
    private Bundle extras;
    private AdRequest adRequest;
    private AdView mAdView;

    private ConstraintLayout constra;
    private ImageView imgHeart;
    private Bitmap imgHeartBitmap;
    private ConstraintLayout.LayoutParams heartParams,heartParams2;
    private ImageView imgCanKutusu;
    private float imgHeartXPos, imgHeartYPos;
    private ObjectAnimator objectAnimatorImgHeartX,objectAnimatorImgHeartY;
    private AnimatorSet imgHeartAnimatorSet;

    private WindowManager.LayoutParams params;

    // Settings Dialog
    private Dialog settingsDialog;
    private ImageView settingsImgClose;
    private Button settingsBtnChangeName, settingsBtnChangeProfileImage;
    private RadioButton settingsRadioOpen, settingsRadioClose;

    // ChangeName Dialog
    private Dialog changeNameDialog;
    private ImageView changeNameImgClose;
    private EditText changeNameEditTxtName;
    private Button changeNameDialogBtn;
    private String getChangeName;

    private int izinVerme=0, izinVerildi = 1;
    private Intent resimDegistirIntent;
    private Uri resimUri;
    private Bitmap resimBitmap;
    private CircleImageView userProfileImage;
    private ImageDecoder.Source resimDosyasi;
    private AlertDialog.Builder alertBuilder;
    private byte[] resimByte;
    private int resimIndex;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private boolean muzikDurumu;

    private ActivityResultLauncher<String> mGetContent;

    private Dialog getHeartDialog;
    private ImageView getHeartImgClose, getHeartShowAndGet, getHeartBuyAndGet;

    private Dialog shopDialog;
    private ImageView shopDialogImgClose;
    private RecyclerView shopDialogRecyclerView;
    private shopAdapter adapter;
    private GridLayoutManager manager;

    private BillingClient mBillingClient;
    private ArrayList<String> skuList;
    private ArrayList<Integer> heartList;
    private int heartPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtUserHeartCount = (TextView)findViewById(R.id.mainActivity_userHeartCount);
        txtViewUserName = (TextView)findViewById(R.id.userNameBox);
        userProfileImage = (CircleImageView)findViewById(R.id.main_activity_circleImageView);
        constra = (ConstraintLayout)findViewById(R.id.main_activity_constra);
        mAdView = (AdView)findViewById(R.id.main_activity_adView);
        imgCanKutusu = (ImageView)findViewById(R.id.imgCanKutusu);

        adRequest = new AdRequest.Builder().build();
        loadAd();

        extras = new VungleExtrasBuilder(new String[]{"MAINREWARDED-4080919"})
                .setUserId("test_user")
                .build();
        adRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(VungleAdapter.class, extras)             // Rewarded.
                .build();

        preferences = this.getSharedPreferences("com.nexis.bilbakalim",MODE_PRIVATE);
        muzikDurumu = preferences.getBoolean("muzikDurumu",true);

        skuList = new ArrayList<>();
        heartList = new ArrayList<>();

        skuList.add("buy_heart1");
        skuList.add("buy_heart2");
        skuList.add("buy_heart3");
        skuList.add("buy_heart4");
        skuList.add("buy_heart5");

        heartList.add(3);
        heartList.add(15);
        heartList.add(45);
        heartList.add(90);
        heartList.add(250);

        mBillingClient = BillingClient.newBuilder(this)
                .setListener(this).enablePendingPurchases().build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if(billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK){
                    Toast.makeText(getApplicationContext(),"Ödeme Sistemi İçin Google Play Hesabınızı Kontrol Ediniz.",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Toast.makeText(getApplicationContext(),"Ödeme Sistemi Şuanda Geçerli Değil",Toast.LENGTH_SHORT).show();
            }
        });

        try {
            database = this.openOrCreateDatabase("BilBakalim",MODE_PRIVATE,null);
            cursor = database.rawQuery("SELECT * FROM Ayarlar",null);

            heartIndex = cursor.getColumnIndex("k_Heart");
            nameIndex = cursor.getColumnIndex("k_Adi");
            resimIndex = cursor.getColumnIndex("k_Image");
            cursor.moveToFirst();




            resimByte = cursor.getBlob(resimIndex);


            if(resimByte != null){
                resimBitmap = BitmapFactory.decodeByteArray(resimByte,0,resimByte.length);
                userProfileImage.setImageBitmap(resimBitmap);
            }

            heartCount = Integer.valueOf(cursor.getString(heartIndex));
            txtUserHeartCount.setText("+" +heartCount);
            txtViewUserName.setText(cursor.getString(nameIndex));

            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }


        mAdView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });

        imgHeart = new ImageView(getApplicationContext());
        imgHeartBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.kalp);
        imgHeart.setImageBitmap(imgHeartBitmap);
        heartParams = new ConstraintLayout.LayoutParams(156,156);
        imgHeart.setLayoutParams(heartParams);
        imgHeart.setX(0);
        imgHeart.setY(0);
        imgHeart.setVisibility(View.INVISIBLE);
        constra.addView(imgHeart);


    }

    public void btnAyarlar(View v){
        ayarlariGoster();
    }

    private void marketDialog(){
        shopDialog = new Dialog(this);
        params =  new WindowManager.LayoutParams();
        params.copyFrom(shopDialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        shopDialog.setCancelable(false);
        shopDialog.setContentView(R.layout.custom_dialog_shop);

        shopDialogImgClose = (ImageView)shopDialog.findViewById(R.id.custom_dialog_shop_ImageViewClose);
        shopDialogRecyclerView = (RecyclerView)shopDialog.findViewById(R.id.custom_dialog_shop_recyclerView);

        shopDialogImgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shopDialog.dismiss();
            }
        });

        adapter = new shopAdapter(Shop.getData(),this);

        shopDialogRecyclerView.setHasFixedSize(true);
        manager = new GridLayoutManager(this,3);
        shopDialogRecyclerView.setLayoutManager(manager);
        shopDialogRecyclerView.addItemDecoration(new GridItemDecoration(3,5));
        shopDialogRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new shopAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Shop mShop, final int pos) {
                if(mBillingClient.isReady()){
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(Collections.singletonList(skuList.get(pos))).setType(BillingClient.SkuType.INAPP);
                    mBillingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
                        @Override
                        public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> list) {
                            if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null){
                                BillingFlowParams flowParams = BillingFlowParams.newBuilder().setSkuDetails(list.get(0)).build();

                                mBillingClient.launchBillingFlow(MainActivity.this,flowParams);
                                heartPos = pos;
                            }
                        }
                    });
                }
            }
        });

        shopDialog.getWindow().setAttributes(params);
        shopDialog.show();
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
        if(billingResult.getResponseCode() == Purchase.PurchaseState.PURCHASED){
            handlePurchase(list.get(0));
        }
    }

    private void handlePurchase(Purchase purchase) {
        if(purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED){
            if(!purchase.isAcknowledged()){
                AcknowledgePurchaseParams purchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();

                AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
                    @Override
                    public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                        Toast.makeText(getApplicationContext(),"Satın Alma İşlemi Başarılı",Toast.LENGTH_SHORT).show();
                        sonCanDurumu = sonCanDurumu +1;
                        sonCanDurumu += heartList.get(heartPos);
                        canMiktariniGuncelle(Integer.valueOf(txtUserHeartCount.getText().toString()),sonCanDurumu);
                    }
                };
                mBillingClient.acknowledgePurchase(purchaseParams,acknowledgePurchaseResponseListener);
            }
        }
    }

    private class GridItemDecoration extends RecyclerView.ItemDecoration{
        private int spanCount;
        private int spacing;

        public GridItemDecoration(int spanCount, int spacing) {
            this.spanCount = spanCount;
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            outRect.left = (column + 1) * spacing / spanCount;
            outRect.right = (column + 1) * spacing / spanCount;
            outRect.bottom=spacing;
        }
    }

    private void canKazanmaMenusu(){
        getHeartDialog = new Dialog(this);
        params = new WindowManager.LayoutParams();
        params.copyFrom(getHeartDialog.getWindow().getAttributes());
        params.width=WindowManager.LayoutParams.MATCH_PARENT;
        params.height=WindowManager.LayoutParams.WRAP_CONTENT;
        getHeartDialog.setCancelable(false);
        getHeartDialog.setContentView(R.layout.custom_dialog_get_heart);

        getHeartImgClose = (ImageView)getHeartDialog.findViewById(R.id.custom_dialog_get_heart_ImageViewClose);
        getHeartShowAndGet = (ImageView)getHeartDialog.findViewById(R.id.custom_dialog_get_heart_ImageView_ShowAndGet);
        getHeartBuyAndGet = (ImageView)getHeartDialog.findViewById(R.id.custom_dialog_get_heart_ImageView_BuyAndGet);

        getHeartImgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getHeartDialog.dismiss();
            }
        });

        getHeartShowAndGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRewardedAd != null){
                    mRewardedAd.show(MainActivity.this, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {


                            loadAd();
                            getHeartDialog.dismiss();
                            imgHeart.setX(constra.getPivotX());
                            imgHeart.setY(constra.getPivotY());
                            imgHeart.setVisibility(View.VISIBLE);

                            imgHeartXPos = (imgCanKutusu.getX() + (imgCanKutusu.getWidth() / 2f)-78);
                            imgHeartYPos = (imgCanKutusu.getY() + (imgCanKutusu.getHeight() / 2F)-78);



                            objectAnimatorImgHeartX = ObjectAnimator.ofFloat(imgHeart,"x",imgHeartXPos);
                            objectAnimatorImgHeartX.setStartDelay(1000);
                            objectAnimatorImgHeartX.setDuration(imgHeartDuration);

                            objectAnimatorImgHeartY = ObjectAnimator.ofFloat(imgHeart,"y",imgHeartYPos);
                            objectAnimatorImgHeartY.setStartDelay(1000);
                            objectAnimatorImgHeartY.setDuration(imgHeartDuration);

                            imgHeartAnimatorSet = new AnimatorSet();
                            imgHeartAnimatorSet.playTogether(objectAnimatorImgHeartX);
                            imgHeartAnimatorSet.playTogether(objectAnimatorImgHeartY);
                            imgHeartAnimatorSet.start();
                            objectAnimatorImgHeartY.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {

                                    heartParams2 = new ConstraintLayout.LayoutParams(96,96);
                                    imgHeart.setLayoutParams(heartParams2);
                                    imgHeart.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getApplicationContext(),"Hak Kazandın",Toast.LENGTH_SHORT).show();
                                    sonCanDurumu = heartCount;
                                    heartCount++;
                                    canMiktariniGuncelle(sonCanDurumu,heartCount);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            });
                        }
                    });

                }else {
                    Toast.makeText(getApplicationContext(),"Video Henüz Yüklenmedi!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        getHeartBuyAndGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marketDialog();
                getHeartDialog.dismiss();
            }
        });

        getHeartDialog.getWindow().setAttributes(params);
        getHeartDialog.show();
    }

    private void ayarlariGoster(){
        settingsDialog = new Dialog(this);
        params = new WindowManager.LayoutParams();
        params.copyFrom(settingsDialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        settingsDialog.setCancelable(false);
        settingsDialog.setContentView(R.layout.custom_dialog_settings);

        settingsImgClose = (ImageView)settingsDialog.findViewById(R.id.custom_dialog_settings_ImageViewClose);
        settingsBtnChangeName = (Button)settingsDialog.findViewById(R.id.custom_dialog_btnChangeName);
        settingsBtnChangeProfileImage = (Button)settingsDialog.findViewById(R.id.custom_dialog_btnChangeProfileImage);
        settingsRadioClose = (RadioButton)settingsDialog.findViewById(R.id.custom_dialog_setting_readioBtnClose);
        settingsRadioOpen = (RadioButton)settingsDialog.findViewById(R.id.custom_dialog_setting_readioBtnOpen);

        if(muzikDurumu){
            settingsRadioOpen.setChecked(muzikDurumu);
        }else{
            settingsRadioClose.setChecked(!muzikDurumu);
        }

        settingsRadioOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    muzikAcKapa(isChecked);
                }
            }
        });

        settingsRadioClose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    muzikAcKapa(!isChecked);
                }
            }
        });

        settingsImgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsDialog.dismiss();
            }
        });

        settingsBtnChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsDialog.dismiss();
                isminiDegistirDialog();
            }
        });
        settingsBtnChangeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},izinVerme);
                }else{
                    resimDegistirIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(resimDegistirIntent,izinVerildi);


                }
            }
        });

        settingsDialog.getWindow().setAttributes(params);
        settingsDialog.show();
    }

    private void muzikAcKapa(boolean b){
        editor = preferences.edit();
        editor.putBoolean("muzikDurumu",b);
        editor.apply();

        Toast.makeText(getApplicationContext(),"Ayar Başarıyla Kayıt Edildi. \nAktif Olması İçin Uygulamayı Yeniden Açın.",Toast.LENGTH_SHORT).show();
    }

    private void isminiDegistirDialog(){
        changeNameDialog = new Dialog(this);
        params = new WindowManager.LayoutParams();
        params.copyFrom(changeNameDialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        changeNameDialog.setCancelable(false);
        changeNameDialog.setContentView(R.layout.custom_dialog_change_name);

        changeNameImgClose = (ImageView)changeNameDialog.findViewById(R.id.custom_dialog_changeName_ImageViewClose);
        changeNameEditTxtName = (EditText)changeNameDialog.findViewById(R.id.custom_dialog_changeName_EditTextName);
        changeNameDialogBtn = (Button)changeNameDialog.findViewById(R.id.custom_dialog_changeName_btnChangeName);

        changeNameImgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeNameDialog.dismiss();
            }
        });

        changeNameDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getChangeName = changeNameEditTxtName.getText().toString();

                if(!TextUtils.isEmpty(getChangeName)){
                    if(!(getChangeName.matches(txtViewUserName.getText().toString()))){
                        ismiGuncelle(getChangeName,txtViewUserName.getText().toString());
                    }else{
                        Toast.makeText(getApplicationContext(),"Zaten Bu İsmi Kullanıyorsunuz",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"İsim Değeri Boş Olamaz",Toast.LENGTH_SHORT).show();
                }
            }
        });

        changeNameDialog.getWindow().setAttributes(params);
        changeNameDialog.show();
    }

    private void ismiGuncelle(String yeniİsim, String eskiİsim){
        try {
            sqlSorgusu = "UPDATE Ayarlar SET k_Adi = ? WHERE k_Adi = ?";
            statement = database.compileStatement(sqlSorgusu);
            statement.bindString(1,yeniİsim);
            statement.bindString(2,eskiİsim);
            statement.execute();

            txtViewUserName.setText(yeniİsim);
            Toast.makeText(getApplicationContext(),"İsminiz Başarıyla Değiştirildi",Toast.LENGTH_SHORT).show();

            if(changeNameDialog.isShowing()){
                changeNameDialog.dismiss();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void mainBtnClick(View v){
        switch (v.getId()){
            case R.id.mainActivity_btnPlay:
                Intent playIntent = new Intent(this,PlayActivity.class);
                finish();
                playIntent.putExtra("heartCount",heartCount);
                startActivity(playIntent);
                overridePendingTransition(R.anim.slide_out_up,R.anim.slide_in_down);
                break;

            case R.id.mainActivity_btnShop:
                marketDialog();
                break;

            case R.id.mainActivity_btnExit:
                uygulamadanCik();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Bil Bakalım");
        alert.setIcon(R.mipmap.ic_bil_bakalim);
        alert.setMessage("Uygulamadan Çıkmak İstediğinize Emin Misiniz ?");
        alert.setPositiveButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.setNegativeButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                uygulamadanCik();
            }
        });
        alert.show();
    }

    private void uygulamadanCik(){
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private void loadAd(){
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917",
                adRequest, new RewardedAdLoadCallback() {

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        System.out.println("Reklam Yüklendi");

                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        onAdLoaded(mRewardedAd);
                        System.out.println(loadAdError.getMessage());
                        mRewardedAd = null;
                    }
                });
    }

    public void btnHakKazan(View v){

        canKazanmaMenusu();
    }

    private void canMiktariniGuncelle(int sonCanSayisi, int canSayisi){
        try {
            sqlSorgusu = "UPDATE Ayarlar SET k_Heart = ? WHERE k_Heart = ?";
            statement = database.compileStatement(sqlSorgusu);
            statement.bindString(1,String.valueOf(canSayisi));
            statement.bindString(2,String.valueOf(sonCanSayisi));
            statement.execute();
            txtUserHeartCount.setText("+"+canSayisi);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == izinVerme){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                resimDegistirIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(resimDegistirIntent,izinVerildi);

                //mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                //    @Override
                //    public void onActivityResult(Uri result) {
                //        mImageView.setImageURI(result);
                //    }
                //});

                //mGetContent.launch("image/*");
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == izinVerildi){
            if(resultCode == RESULT_OK && data != null){
                resimUri = data.getData();

                alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle("Bil Bakalım");
                alertBuilder.setMessage("Profil resminizi değiştirmek istediğinize emin misiniz ?");
                alertBuilder.setIcon(R.mipmap.ic_bil_bakalim);
                alertBuilder.setCancelable(false);
                alertBuilder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if(Build.VERSION.SDK_INT >= 28){
                                resimDosyasi = ImageDecoder.createSource(MainActivity.this.getContentResolver(),resimUri);
                                resimBitmap = ImageDecoder.decodeBitmap(resimDosyasi);
                                userProfileImage.setImageBitmap(resimBitmap);
                            }else{
                                resimBitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(),resimUri);
                                userProfileImage.setImageBitmap(resimBitmap);
                            }
                            profilResminiKaydet(resimBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                alertBuilder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertBuilder.show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void profilResminiKaydet(Bitmap profilResmi){
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            profilResmi.compress(Bitmap.CompressFormat.PNG,75,outputStream);
            resimByte = outputStream.toByteArray();


            sqlSorgusu = "UPDATE Ayarlar SET k_Image = ? WHERE k_Adi = ?";
            statement = database.compileStatement(sqlSorgusu);
            statement.bindBlob(1,resimByte);
            statement.bindString(2,txtViewUserName.getText().toString());
            statement.execute();
            if(settingsDialog.isShowing()){
                settingsDialog.dismiss();
            }

            Toast.makeText(getApplicationContext(),"Profil Resminiz Başarıyla Güncellendi",Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            e.printStackTrace();
        }
    }


}