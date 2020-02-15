package com.hy.mipaycard.online_card;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hy.mipaycard.BitmapCropActivity;
import com.hy.mipaycard.Config;
import com.hy.mipaycard.MainUtils;
import com.hy.mipaycard.R;
import com.hy.mipaycard.Utils.CardList;

import java.io.File;
import java.util.List;


import static com.hy.mipaycard.Config.debug_Api;
import static com.hy.mipaycard.Config.fileWork;
import static com.hy.mipaycard.MainActivity.ref_media;
import static com.hy.mipaycard.MainUtils.copyToClipboard;
import static com.hy.mipaycard.MainUtils.toSelfSetting;
import static com.hy.mipaycard.online_card.OnlineCardActivity.getGlideCacheFile;

public class Card2Adapter extends RecyclerView.Adapter<Card2Adapter.ViewHolder>{

    private Context mContext;

    private List<Card2> mCard2List;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView cardImage;
        TextView cardName;
        TextView userName;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            cardImage = (ImageView) view.findViewById(R.id.card_image);
            cardName = (TextView) view.findViewById(R.id.card_name);
            userName = view.findViewById(R.id.user_name);
        }
    }

    public Card2Adapter(List<Card2> card2List) {
        mCard2List = card2List;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.card_item_online, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position = holder.getAdapterPosition();
                final Card2 card2 = mCard2List.get(position);
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.getMenuInflater().inflate(Build.VERSION.SDK_INT>=debug_Api?R.menu.online_popup_menu_q:R.menu.online_popup_menu, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.online_menu_set:
                                getCard(card2,false);
                                break;
                            case R.id.online_menu_add:
                                if(Build.VERSION.SDK_INT>=debug_Api){
                                    getCard(card2,true);
                                } else {
                                    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                        toSelfSetting(mContext);
                                        Toast.makeText(mContext, "请授予储存空间权限", Toast.LENGTH_LONG).show();
                                    } else {
                                        getCard(card2, true);
                                    }
                                }
                                break;
                            case R.id.online_menu_info:
                                new AlertDialog.Builder(mContext)
                                        .setTitle("图片信息")
                                        .setMessage("图片名称："+ card2.getCardName()+"\n图片链接："+ card2.getLink()+"\n图片说明："+ card2.getAbout()+"\n上传者："+ card2.getUserName()+"\n联系方式："+ card2.getEmail())
                                        .setPositiveButton("确定",null)
                                        .setNegativeButton("复制链接", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                copyToClipboard(mContext,card2.getLink());
                                                Toast.makeText(mContext,"已复制",Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .show();
                                break;
                            case R.id.online_menu_save:
                                //todo save card
                                MainUtils.copyPrivateToPictures(mContext,getGlideCacheFile(mContext,card2.getLink()),card2.getFileName());
                                Toast.makeText(mContext,"已保存到\n"+Environment.DIRECTORY_PICTURES+"/MiPayCard",Toast.LENGTH_LONG).show();
                                break;
                            default:
                        }
                        return true;
                    }
                });
                popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        // 控件消失时的事件
                    }
                });
                return true;//true只执行长按事件
            }
        });
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                final Card2 card2 = mCard2List.get(position);
                getCard(card2,false);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Card2 card2 = mCard2List.get(position);
        holder.cardName.setText(card2.getCardName());
        Glide.with(mContext).load(card2.getLink()).into(holder.cardImage);
        if(card2.getUserName().length()>0)
            holder.userName.setText(card2.getUserName());
        else
            holder.userName.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return mCard2List.size();
    }

    private void getCard(final Card2 card2 , final boolean isSaveToFile){
        if (isSaveToFile){
            if (!fileWork(mContext).exists()){
                fileWork(mContext).mkdirs();
            }
        }
        File file = new File((isSaveToFile?fileWork(mContext):mContext.getExternalCacheDir()),card2.getFileName());
        File tmpFile = getGlideCacheFile(mContext,card2.getLink());
        if(tmpFile!=null&&tmpFile.exists()) {
            CardList.copyFile(tmpFile.getPath(),file.getPath());
            if (isSaveToFile) {
                ref_media(mContext,file);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Config.localAction));//发送本地广播
                Toast.makeText(mContext, "已添加", Toast.LENGTH_LONG).show();
            } else {
                setCard(file.getPath());
            }
        } else {
            Toast.makeText(mContext, "图片获取失败\n请稍候重试", Toast.LENGTH_LONG).show();
        }
    }

    private void setCard(String path){
        //Intent intent = new Intent(mContext, SetCardActivity.class);
        //intent.putExtra(Config.file_Path, path);
        //intent.putExtra(Config.is_Auto, isAutoLogo);
        //mContext.startActivity(intent);

        Intent i = new Intent(mContext, BitmapCropActivity.class);
        i.putExtra(Config.open_Crop, path);
        mContext.startActivity(i);
    }
}
