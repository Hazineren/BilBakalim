package com.nexis.bilbakalim;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class SplashScreenActivity extends AppCompatActivity {

    // Sorular için Listeler
    private String[] sorularList = {"Mutfak aletlerimizden bir tanesi !","İç Anadolu Bölgemizden bir ilimiz !"};
    private String[] sorularKodList = {"mutfakS1","illerS1"};

    // Kelimeler için Listeler
    private String[] kelimeList = {"Çatal","Bıçak","Kaşık","Tabak","Bulaşık Süngeri","Bulaşık Teli","Tencere","Tava","Çaydanlık","Mutfak Robotu","Kesme Tahtası","Süzgeç",
                                    "Aksaray","Ankara","Çankırı","Eskişehir","Karaman","Kayseri","Kırıkkale","Kırşehir","Konya","Nevşehir","Niğde","Sivas","Yozgat"};
    private String[] kelimeKodList = {"mutfakS1","mutfakS1","mutfakS1","mutfakS1","mutfakS1","mutfakS1","mutfakS1","mutfakS1","mutfakS1","mutfakS1","mutfakS1","mutfakS1",
                                        "illerS1","illerS1","illerS1","illerS1","illerS1","illerS1","illerS1","illerS1","illerS1","illerS1","illerS1","illerS1","illerS1"};

    private ProgressBar mProgressBar;
    private TextView mTxtView;
    private SQLiteDatabase database;
    private Cursor cursor;
    private float artacakProgress, maxProgress = 100f, progressMiktari=0;
    static public HashMap<String,String> sorularHashMap;
    private String sqlSorgusu;
    private SQLiteStatement statement;
    private MediaPlayer gameTheme;

    private SharedPreferences preferences;
    private boolean muzikDurumu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mProgressBar = (ProgressBar)findViewById(R.id.splashActivity_progressBar);
        mTxtView = (TextView)findViewById(R.id.splasActivity_txtViewState);
        sorularHashMap = new HashMap<>();
        gameTheme = MediaPlayer.create(this,R.raw.gametheme);
        gameTheme.setLooping(true);

        preferences = this.getSharedPreferences("com.nexis.bilbakalim",MODE_PRIVATE);
        muzikDurumu = preferences.getBoolean("muzikDurumu",true);

        try {
            database = this.openOrCreateDatabase("BilBakalim",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS Ayarlar (k_Adi VARCHAR, k_Heart VARCHAR, k_Image BLOB)");
            cursor = database.rawQuery("SELECT * FROM Ayarlar",null);

            if(cursor.getCount()<1){
                database.execSQL("INSERT INTO Ayarlar (k_Adi,k_Heart) VALUES ('Oyuncu','0')");
            }

            database.execSQL("CREATE TABLE IF NOT EXISTS Sorular (id INTEGER PRIMARY KEY, sKod VARCHAR UNIQUE, soru VARCHAR)");
            database.execSQL("DELETE FROM Sorular");
            sqlInsertQuestions();

            database.execSQL("CREATE TABLE IF NOT EXISTS Kelimeler (kKod VARCHAR, kelime VARCHAR, FOREIGN KEY (kKod) REFERENCES Sorular (sKod))");
            database.execSQL("DELETE FROM Kelimeler");
            sqlInsertWords();



            cursor = database.rawQuery("SELECT * FROM Sorular",null);
            artacakProgress = maxProgress / cursor.getCount();

            int sKodIndex = cursor.getColumnIndex("sKod");
            int soruIndex = cursor.getColumnIndex("soru");

            mTxtView.setText("Sorular Yükleniyor...");

            while (cursor.moveToNext()){
                sorularHashMap.put(cursor.getString(sKodIndex),cursor.getString(soruIndex));
                progressMiktari += artacakProgress;
                mProgressBar.setProgress((int)progressMiktari);
            }

            mTxtView.setText("Sorular Alındı, Uygulama Başlatılıyor...");
            cursor.close();

            new CountDownTimer(1100,1000){

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    Intent mainIntent = new Intent(SplashScreenActivity.this,MainActivity.class);
                    startActivity(mainIntent);
                }
            }.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(muzikDurumu){
            gameTheme.start();
        }
    }

    private void sqlInsertQuestions(){
        try {
            for(int s=0; s<sorularList.length;s++){
                sqlSorgusu = "INSERT INTO Sorular (sKod, soru) VALUES (?, ?)";
                statement = database.compileStatement(sqlSorgusu);
                statement.bindString(1,sorularKodList[s]);
                statement.bindString(2,sorularList[s]);
                statement.execute();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sqlInsertWords(){
        try {
            for(int k=0; k<kelimeList.length;k++){
                sqlSorgusu = "INSERT INTO Kelimeler (kKod, kelime) VALUES (?, ?)";
                statement = database.compileStatement(sqlSorgusu);
                statement.bindString(1,kelimeKodList[k]);
                statement.bindString(2,kelimeList[k]);
                statement.execute();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}