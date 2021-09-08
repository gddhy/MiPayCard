package com.hy.mipaycard;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hy.mipaycard.SetCard.SetCardActivity;
import com.hy.mipaycard.Utils.CardList;
import com.hy.mipaycard.Utils.PhotoUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;

import static com.hy.mipaycard.Config.fileWork;
import static com.hy.mipaycard.Config.getTempFile;
import static com.hy.mipaycard.MainActivity.ref_media;
import static com.hy.mipaycard.MainUtils.saveBitmapAsPng;
import static com.hy.mipaycard.MainUtils.toRoundCorner;

public class BitmapCropActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String saveName;
    private boolean isSave;
    private boolean isFinish =true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getSharedPreferences("set", Context.MODE_PRIVATE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        Intent intent = getIntent();
        final String filePath = intent.getStringExtra(Config.open_Crop);
        final File file = new File(filePath);
        saveName = file.getName();
        isSave = pref.getBoolean("CropAutoSave",false);

        View view = LayoutInflater.from(BitmapCropActivity.this).inflate(R.layout.dialog_add,null);
        ImageView imageView = (ImageView)view.findViewById(R.id.dialog_bmp);
        final EditText editText = (EditText)view.findViewById(R.id.dialog_edit);
        editText.setText(saveName);
        Glide.with(BitmapCropActivity.this).load(file).into(imageView);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.dialog_checkbox);
        checkBox.setChecked(isSave);
        final CheckBox checkBox2 = (CheckBox) view.findViewById(R.id.dialog_checkbox_logo);
        checkBox2.setChecked(pref.getBoolean("isAuto",false));

        new AlertDialog.Builder(BitmapCropActivity.this)
                .setTitle("已选择图片")
                .setView(view)
                .setPositiveButton("直接应用", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        savePref(checkBox,checkBox2);
                        checkSave(checkBox,file);
                        Intent intent = new Intent(BitmapCropActivity.this, SetCardActivity.class);
                        intent.putExtra(Config.file_Path, filePath);
                        intent.putExtra(Config.is_Auto, false);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("裁剪", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isFinish = false;
                        savePref(checkBox,checkBox2);
                        if(!checkBox.isChecked()){
                            checkSave(checkBox,file);
                        }
                        if(editText.getText().toString().length()>0){
                            saveName=editText.getText().toString();
                        }
                        Uri cropImageUri = Uri.fromFile(getTempFile());
                        //Todo test
                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R) {
                            UCrop.of(Uri.fromFile(new File(filePath)), cropImageUri)
                                    .withAspectRatio(192, 121)
                                    .withMaxResultSize(960, 605)
                                    .start(BitmapCropActivity.this);
                        } else {
                            Uri newUri = Uri.parse(PhotoUtils.getPath(BitmapCropActivity.this, Uri.fromFile(new File(filePath))));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                newUri = FileProvider.getUriForFile(BitmapCropActivity.this, getPackageName() + ".FileProvider", new File(newUri.getPath()));
                            PhotoUtils.cropImageUri(BitmapCropActivity.this, newUri, cropImageUri, 192, 121, 960, 605, 1);
                        }
                    }
                })
                //.setCancelable(false)
                .setNeutralButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        savePref(checkBox,checkBox2);
                        checkSave(checkBox,file);
                        finish();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if(isFinish)
                            finish();
                    }
                })
                .show();

        if(isSave){
            saveToList(file);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                case UCrop.REQUEST_CROP:
                    File file = getTempFile();
                    Bitmap bmp = PhotoUtils.getBitmapFromUri(Uri.fromFile(file), BitmapCropActivity.this);
                    bmp = toRoundCorner(bmp, 40);
                    saveBitmapAsPng(bmp, file);
                    if (pref.getBoolean("CropAutoSave", false)) {
                        if (!fileWork(BitmapCropActivity.this).exists()) {
                            fileWork(BitmapCropActivity.this).mkdirs();
                        }
                        File f = new File(fileWork(BitmapCropActivity.this), saveName);
                        CardList.copyFile(file.getPath(), f.getPath());
                        ref_media(BitmapCropActivity.this, f);
                        LocalBroadcastManager.getInstance(BitmapCropActivity.this).sendBroadcast(new Intent(Config.localAction));//发送本地广播
                    }
                    Intent intent = new Intent(BitmapCropActivity.this, SetCardActivity.class);
                    intent.putExtra(Config.file_Path, file.getPath());
                    intent.putExtra(Config.is_Auto, pref.getBoolean("isAuto", false));
                    startActivity(intent);
                    finish();
                    break;
                default:
                    finish();
            }
        } else {
            finish();
        }
    }

    private void savePref(CheckBox autoSave,CheckBox autoLogo){
        editor = pref.edit();
        editor.putBoolean("CropAutoSave",autoSave.isChecked());
        editor.putBoolean("isAuto",autoLogo.isChecked());
        editor.apply();
    }

    private void checkSave(CheckBox checkBox,File file){
        File listFile = new File(fileWork(BitmapCropActivity.this), saveName);
        if (checkBox.isChecked()) {
            if(!listFile.exists()){
                saveToList(file);
            }
        } else {
            if(listFile.exists()){
                listFile.delete();
                ref_media(BitmapCropActivity.this,listFile);
                LocalBroadcastManager.getInstance(BitmapCropActivity.this).sendBroadcast(new Intent(Config.localAction));
            }
        }
    }

    private void saveToList(File file){
        if (!fileWork(BitmapCropActivity.this).exists()) {
            fileWork(BitmapCropActivity.this).mkdirs();
        }
        File f = new File(fileWork(BitmapCropActivity.this), saveName);
        if(!f.exists())
            CardList.copyFile(file.getPath(),f.getPath());
        ref_media(BitmapCropActivity.this,f);
        LocalBroadcastManager.getInstance(BitmapCropActivity.this).sendBroadcast(new Intent(Config.localAction));//发送本地广播
    }
}
