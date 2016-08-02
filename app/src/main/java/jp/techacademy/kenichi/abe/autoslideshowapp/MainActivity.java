package jp.techacademy.kenichi.abe.autoslideshowapp;
import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private Button forwardButton;
    private Button backButton;
    private Button playstopButton;
    private Cursor mCursor;
    private android.os.Handler handler = new android.os.Handler();
    private Runnable timer;

    private void setButton(boolean forward, boolean back, boolean playstop){//タップ制御
        forwardButton.setEnabled(forward);
        backButton.setEnabled(back);
        playstopButton.setEnabled(playstop);
    }
    public void forward(View v){    //進むボタン
        selectImage(1);
    }
    public void back(View v){       //戻るボタン
        selectImage(2);
    }
    public void playstop(View v){   //再生・停止ボタン
        if(playstopButton.getText().toString().equals("再生")){
            playstopButton.setText("停止");
            autoplay();
            setButton(false,false,true);
        }else {
            playstopButton.setText("再生");
            handler.removeCallbacks(timer);
            setButton(true,true,true);
        }
    }
    private void autoplay(){        //自動送り
        timer = new Runnable() {
            @Override
            public void run() {
                selectImage(1);                   //繰り返し処理部分
                handler.postDelayed(timer,2000);  //次回処理を２秒後にセット
            }
        };
        selectImage(1);
        handler.postDelayed(timer,2000);          //初回実行処理
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        forwardButton = (Button) findViewById(R.id.button1);
        backButton = (Button) findViewById(R.id.button2);
        playstopButton = (Button) findViewById(R.id.button3);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
        // Android 5系以下の場合
        } else {
            getContentsInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mCursor.close();
    }

    private void getContentsInfo() {
        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );
        mCursor = cursor;
        selectImage(0);
    }

    private void selectImage(int flag) {    //画像を選択
        switch (flag){
            case 0: //最初の画像を選択
                if (mCursor.moveToFirst()) {
                    setImage();
                }else{
                    setButton(false,false,false);   //画像が一枚もなければ全ボタン無効＆メッセージ出力
                    Toast.makeText(this, "画像が見つかりません", Toast.LENGTH_LONG).show();
                }
                break;
            case 1: //1つ先の画像を選択
                if (mCursor.moveToNext()) { }
                else{ mCursor.moveToFirst(); }
                setImage();
                break;
            case 2: //1つ前の画像を選択
                if (mCursor.moveToPrevious()) { }
                else{ mCursor.moveToLast(); }
                setImage();
                break;
            default:
                break;
        }
    }

    private void setImage() {   //選択した画像を表示
        int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = mCursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);
    }
}
