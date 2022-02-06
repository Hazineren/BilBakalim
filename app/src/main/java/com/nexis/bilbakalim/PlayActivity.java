package com.nexis.bilbakalim;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.media.Image;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class PlayActivity extends AppCompatActivity {


    private AlertDialog.Builder alert;
    private Intent get_intent;
    private int hakSayisi, sonHakSayisi;

    private SQLiteStatement statement;
    private String sqlSorgusu;

    private TextView txtQuestion, txtWord,txtViewCanSayisi;
    private EditText editTxtTahmin;
    private SQLiteDatabase database;
    private Cursor cursor;
    private ArrayList<String> sorularList;
    private ArrayList<String> sorularKodList;
    private ArrayList<String> kelimelerList;
    private ArrayList<Character> kelimeHarfleri;

    private Random rndSoru, rndKelime, rndHarf;
    private int rndSoruNumber, rndKelimeNumber, rndHarfNumber;
    private String rastgeleSoru, rastgeleSoruKodu, rastgeleKelime, kelimeBilgisi, txtTahminDegeri;
    private int rastgeleBelirlenecekHarfSayısı;

    private Dialog statisticTableDialog;
    private ImageView statisticTableImgClose;
    private LinearLayout statisticTableLinear;
    private Button statisticTableBtnMainMenu, statisticTableBtnPlayAgain;
    private TextView txtStatisticQuestionCount, txtStatisticWordCount, txtStatisticFalseGuessCount;
    private ProgressBar pBarStatisticQuestionCount, pBarStatisticWordCount,pBarStatisticFalseGuessCount;
    private WindowManager.LayoutParams params;
    private int cozulenKelimeSayisi = 0, cozulenSoruSayisi = 0, yapilanYanlisSayisi = 0, maxSoruSayisi, maxKelimeSayisi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        txtQuestion = (TextView) findViewById(R.id.playActivity_txtQuestion);
        txtWord = (TextView) findViewById(R.id.playActivity_txtViewWord);
        txtViewCanSayisi = (TextView)findViewById(R.id.playActivity_userHeartCount);
        editTxtTahmin = (EditText) findViewById(R.id.playActivity_editTxtTahmin);
        sorularList = new ArrayList<>();
        sorularKodList = new ArrayList<>();
        kelimelerList = new ArrayList<>();

        rndSoru = new Random();
        rndKelime = new Random();
        rndHarf = new Random();


        get_intent = getIntent();
        hakSayisi = get_intent.getIntExtra("heartCount", 0);
        txtViewCanSayisi.setText("+"+hakSayisi);

        for (Map.Entry soru : SplashScreenActivity.sorularHashMap.entrySet()) {
            sorularList.add(String.valueOf(soru.getValue()));
            sorularKodList.add(String.valueOf(soru.getKey()));
        }

        randomSoruGetir();
    }

    @Override
    public void onBackPressed() {
        alert = new AlertDialog.Builder(this);
        alert.setTitle("Bil Bakalım");
        alert.setMessage("Geri Dönmek İstediğinize Emin misiniz ?");
        alert.setIcon(R.mipmap.ic_bil_bakalim);
        alert.setPositiveButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.setNegativeButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mainIntent();
            }
        });
        alert.show();
    }

    public void btnIstatisticTablosu(View v){
        maxVerileriHesapla("");
    }

    private void istatistikTablosunuGoster(String oyunDurumu,int maxSoruSayisi, int maxKelimeSayisi, int cozulenSoruSayisi, int cozulenKelimeSayisi, int yapilanYanlisSayisi){
        statisticTableDialog = new Dialog(this);
        params = new WindowManager.LayoutParams();
        params.copyFrom(statisticTableDialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        statisticTableDialog.setContentView(R.layout.custom_dialog_statistic_table);

        statisticTableImgClose = (ImageView)statisticTableDialog.findViewById(R.id.custom_dialog_statistic_table_ImageViewClose);
        statisticTableLinear = (LinearLayout)statisticTableDialog.findViewById(R.id.custom_dialog_statistic_table_LinearLayout);
        statisticTableBtnPlayAgain = (Button)statisticTableDialog.findViewById(R.id.custom_dialog_statistic_table_btnPlayAgain);
        statisticTableBtnMainMenu = (Button)statisticTableDialog.findViewById(R.id.custom_dialog_statistic_table_btnMainMenu);
        txtStatisticQuestionCount = (TextView)statisticTableDialog.findViewById(R.id.custom_dialog_statistic_table_txtViewQuestionCount);
        txtStatisticWordCount = (TextView)statisticTableDialog.findViewById(R.id.custom_dialog_statistic_table_txtViewWordCount);
        txtStatisticFalseGuessCount = (TextView)statisticTableDialog.findViewById(R.id.custom_dialog_statistic_table_txtViewFalseGuessCount);
        pBarStatisticQuestionCount = (ProgressBar)statisticTableDialog.findViewById(R.id.custom_dialog_statistic_table_prosgressBarQuestionCount);
        pBarStatisticWordCount = (ProgressBar)statisticTableDialog.findViewById(R.id.custom_dialog_statistic_table_prosgressBarWordCount);
        pBarStatisticFalseGuessCount = (ProgressBar)statisticTableDialog.findViewById(R.id.custom_dialog_statistic_table_prosgressBarFalseGuessCount);


        if(oyunDurumu.matches("Oyun Bitti")){
            statisticTableDialog.setCancelable(false);
            statisticTableLinear.setVisibility(View.VISIBLE);
            statisticTableImgClose.setVisibility(View.INVISIBLE);
        }
        txtStatisticQuestionCount.setText(cozulenSoruSayisi+ " / "+maxSoruSayisi);
        txtStatisticWordCount.setText(cozulenKelimeSayisi+ " / "+maxKelimeSayisi);
        txtStatisticFalseGuessCount.setText(yapilanYanlisSayisi+" / "+maxKelimeSayisi);

        pBarStatisticQuestionCount.setProgress(cozulenSoruSayisi);
        pBarStatisticWordCount.setProgress(cozulenKelimeSayisi);
        pBarStatisticFalseGuessCount.setProgress(yapilanYanlisSayisi);

        statisticTableImgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statisticTableDialog.dismiss();
            }
        });

        statisticTableBtnMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Main Menu
                mainIntent();
            }
        });

        statisticTableBtnPlayAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // PLAY Again
                Intent thisIntent = new Intent(PlayActivity.this,PlayActivity.class);
                thisIntent.putExtra("heartCount",Integer.valueOf(txtViewCanSayisi.getText().toString()));
                finish();
                startActivity(thisIntent);
            }
        });

        statisticTableDialog.getWindow().setAttributes(params);
        statisticTableDialog.show();
    }

    private void maxVerileriHesapla(String oyunDurumu){

        try {
            cursor = database.rawQuery("SELECT * FROM Kelimeler, Sorular WHERE Kelimeler.kKod = Sorular.sKod",null);
            maxKelimeSayisi = cursor.getCount();

            cursor = database.rawQuery("SELECT * FROM Sorular",null);
            maxSoruSayisi=cursor.getCount();

            cursor.close();

            istatistikTablosunuGoster(oyunDurumu, maxSoruSayisi, maxKelimeSayisi,cozulenSoruSayisi,cozulenKelimeSayisi,yapilanYanlisSayisi);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void btnHarfAl(View v) {

        if (hakSayisi > 0) {
            rastgeleHarfAl();
            sonHakSayisi = hakSayisi;
            hakSayisi--;
            kalanHakkiKaydet(hakSayisi, sonHakSayisi);
        } else {
            Toast.makeText(getApplicationContext(), "Harf Alabilmek İçin Kalp Sayısı Yetersiz", Toast.LENGTH_SHORT).show();
        }

    }

    public void kalanHakkiKaydet(int hSayisi, int sonHSayisi) {
        try {
            sqlSorgusu = "UPDATE Ayarlar SET k_Heart = ? WHERE k_Heart = ?";
            statement = database.compileStatement(sqlSorgusu);
            statement.bindString(1, String.valueOf(hSayisi));
            statement.bindString(2, String.valueOf(sonHSayisi));
            statement.execute();

            txtViewCanSayisi.setText("+"+hSayisi);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void btnTahminEt(View v) {
        txtTahminDegeri = editTxtTahmin.getText().toString();
        if (!TextUtils.isEmpty(txtTahminDegeri)) {
            if (txtTahminDegeri.matches(rastgeleKelime)) {
                Toast.makeText(getApplicationContext(), "Tebrikler, Doğru tahminde bulundunuz.", Toast.LENGTH_SHORT).show();
                editTxtTahmin.setText("");
                cozulenKelimeSayisi++;

                if(kelimelerList.size() > 0){
                    randomKelimeGetir();
                }else{
                    if(sorularList.size()>0){
                        cozulenSoruSayisi++;
                        randomSoruGetir();
                    }else{
                        maxVerileriHesapla("Oyun Bitti");

                    }

                }
            } else {
                if(hakSayisi>0){
                    sonHakSayisi=hakSayisi;
                    hakSayisi--;
                    yapilanYanlisSayisi++;
                    kalanHakkiKaydet(hakSayisi,sonHakSayisi);
                    Toast.makeText(getApplicationContext(),"Yanlış Tahminde Bulundunuz, Canınız bir azaldı",Toast.LENGTH_SHORT).show();
                }else{
                    maxVerileriHesapla("Oyun Bitti");
                    Toast.makeText(getApplicationContext(),"Oyun Bitti !",Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Tahmin Değeri Boş Olamaz", Toast.LENGTH_SHORT).show();
        }
    }

    private void rastgeleHarfAl() {
        if (kelimeHarfleri.size() > 0) {
            rndHarfNumber = rndHarf.nextInt(kelimeHarfleri.size());
            String[] txtHarfler = txtWord.getText().toString().split(" ");
            char[] gelenKelimeHarfler = rastgeleKelime.toCharArray();

            for (int i = 0; i < rastgeleKelime.length(); i++) {
                if (txtHarfler[i].equals("_") && gelenKelimeHarfler[i] == kelimeHarfleri.get(rndHarfNumber)) {
                    txtHarfler[i] = String.valueOf(kelimeHarfleri.get(rndHarfNumber));
                    kelimeBilgisi = "";

                    for (int j = 0; j < txtHarfler.length; j++) {
                        if (j < txtHarfler.length - 1) {
                            kelimeBilgisi += txtHarfler[j] + " ";
                        } else {
                            kelimeBilgisi += txtHarfler[j];
                        }
                    }
                    break;
                }
            }
            txtWord.setText(kelimeBilgisi);
            kelimeHarfleri.remove(rndHarfNumber);
        }
    }

    private void mainIntent(){
        Intent mainIntent = new Intent(this, MainActivity.class);
        finish();
        startActivity(mainIntent);
        overridePendingTransition(R.anim.slide_out_up, R.anim.slide_in_down);
    }

    private void randomSoruGetir(){
        rndSoruNumber = rndSoru.nextInt(sorularList.size());
        rastgeleSoru = sorularList.get(rndSoruNumber);
        rastgeleSoruKodu = sorularKodList.get(rndSoruNumber);
        sorularList.remove(rndSoruNumber);
        sorularKodList.remove(rndSoruNumber);

        txtQuestion.setText(rastgeleSoru);


        try {
            database = this.openOrCreateDatabase("BilBakalim", MODE_PRIVATE, null);
            cursor = database.rawQuery("SELECT * FROM Kelimeler WHERE kKod= ?", new String[]{rastgeleSoruKodu});

            int kelimeIndex = cursor.getColumnIndex("kelime");

            while (cursor.moveToNext()) {
                kelimelerList.add(cursor.getString(kelimeIndex));
            }
            cursor.close();



        } catch (Exception e) {
            e.printStackTrace();
        }
        randomKelimeGetir();
    }

    private void randomKelimeGetir(){
        kelimeBilgisi="";
        rndKelimeNumber = rndKelime.nextInt(kelimelerList.size());
        rastgeleKelime = kelimelerList.get(rndKelimeNumber);
        kelimelerList.remove(rndKelimeNumber);

        for (int i = 0; i < rastgeleKelime.length(); i++) {
            if (i < rastgeleKelime.length() - 1) {
                kelimeBilgisi += "_ ";
            } else {
                kelimeBilgisi += "_";
            }
        }

        txtWord.setText(kelimeBilgisi);
        System.out.println("Kelime : " + rastgeleKelime);
        System.out.println("Harf Sayısı : " + rastgeleKelime.length());
        kelimeHarfleri = new ArrayList<>();

        for (char harf : rastgeleKelime.toCharArray()) {
            kelimeHarfleri.add(harf);
        }

        if (rastgeleKelime.length() >= 5 && rastgeleKelime.length() <= 7) {
            rastgeleBelirlenecekHarfSayısı = 1;
        } else if (rastgeleKelime.length() >= 8 && rastgeleKelime.length() <= 10) {
            rastgeleBelirlenecekHarfSayısı = 2;
        } else if (rastgeleKelime.length() >= 11 && rastgeleKelime.length() <= 14) {
            rastgeleBelirlenecekHarfSayısı = 3;
        } else if (rastgeleKelime.length() >= 15) {
            rastgeleBelirlenecekHarfSayısı = 4;
        } else {
            rastgeleBelirlenecekHarfSayısı = 0;
        }

        for (int i = 0; i < rastgeleBelirlenecekHarfSayısı; i++) {
            rastgeleHarfAl();
        }
    }
}