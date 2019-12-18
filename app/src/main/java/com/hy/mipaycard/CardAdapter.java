package com.hy.mipaycard;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

import static com.hy.mipaycard.MainActivity.ref_media;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder>{

    private Context mContext;

    private List<Card> mCardList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView cardImage;
        TextView cardName;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            cardImage = (ImageView) view.findViewById(R.id.card_image);
            cardName = (TextView) view.findViewById(R.id.card_name);
        }
    }

    public CardAdapter(List<Card> cardList) {
        mCardList = cardList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.card_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final int position = holder.getAdapterPosition();
                final Card card = mCardList.get(position);
                final File file = card.getImageFile();
                if (position > 1){
                    String[] list = {"设为卡面","设为卡面并添加水印","重命名","删除"};
                    new AlertDialog.Builder(mContext)
                            .setTitle(card.getName())
                            .setItems(list, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i){
                                        case 0:
                                            setCard(file.getPath());
                                            break;
                                        case 1:
                                            setCard(file.getPath(),true);
                                            break;
                                        case 2:
                                            View view = setView(file,true);
                                            final EditText editText = (EditText)view.findViewById(R.id.dialog_edit);
                                            new AlertDialog.Builder(mContext)
                                                    .setTitle("重命名")
                                                    .setView(view)
                                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            String input = editText.getText().toString();
                                                            if (input.length()==0){
                                                                Toast.makeText(mContext,"未输入文件名",Toast.LENGTH_LONG).show();
                                                            } else {
                                                                boolean b = renameFile(file,input);
                                                                if (b) {
                                                                    ref_media(mContext,file);
                                                                    ref_media(mContext,new File(file.getParentFile(),input));
                                                                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Config.localAction));//发送本地广播
                                                                } else {
                                                                    Toast.makeText(mContext,"重命名失败",Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        }
                                                    })
                                                    .setNegativeButton("取消",null)
                                                    .show();
                                            break;
                                        case 3:
                                            View delView = setView(file,false);
                                            new AlertDialog.Builder(mContext)
                                                    .setTitle("删除")
                                                    .setMessage("您确定要删除 "+card.getName()+" ？")
                                                    .setView(delView)
                                                    .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            boolean b = file.delete();
                                                            if (b){
                                                                ref_media(mContext,file);
                                                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Config.localAction));//发送本地广播
                                                            } else {
                                                                Toast.makeText(mContext, "删除失败",Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    })
                                                    .setNegativeButton("取消",null)
                                                    .show();

                                            break;
                                        default:
                                    }
                                }
                            })
                            .show();
                }
                return true;
            }
        });
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = holder.getAdapterPosition();
                final Card card = mCardList.get(position);
                final File file = card.getImageFile();
                setCard(file.getPath());
                /*
                if (position==0||position==1){
                    setCard(file.getPath());
                } else {
                    String[] list = {"设为卡面","设为卡面并添加水印","重命名","删除"};
                    new AlertDialog.Builder(mContext)
                            .setTitle(card.getName())
                            .setItems(list, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i){
                                        case 0:
                                            setCard(file.getPath());
                                            break;
                                        case 1:
                                            setCard(file.getPath(),true);
                                            break;
                                        case 2:
                                            View view = setView(file,true);
                                            final EditText editText = (EditText)view.findViewById(R.id.dialog_edit);
                                            new AlertDialog.Builder(mContext)
                                                    .setTitle("重命名")
                                                    .setView(view)
                                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            String input = editText.getText().toString();
                                                            if (input.length()==0){
                                                                Toast.makeText(mContext,"未输入文件名",Toast.LENGTH_LONG).show();
                                                            } else {
                                                                boolean b = renameFile(file,input);
                                                                if (b) {
                                                                    ref_media(mContext,file);
                                                                    ref_media(mContext,new File(file.getParentFile(),input));
                                                                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Config.localAction));//发送本地广播
                                                                } else {
                                                                    Toast.makeText(mContext,"重命名失败",Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        }
                                                    })
                                                    .setNegativeButton("取消",null)
                                                    .show();
                                            break;
                                        case 3:
                                            View delView = setView(file,false);
                                            new AlertDialog.Builder(mContext)
                                                    .setTitle("删除")
                                                    .setMessage("您确定要删除 "+card.getName()+" ？")
                                                    .setView(delView)
                                                    .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            boolean b = file.delete();
                                                            if (b){
                                                                ref_media(mContext,file);
                                                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Config.localAction));//发送本地广播
                                                            } else {
                                                                Toast.makeText(mContext, "删除失败",Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    })
                                                    .setNegativeButton("取消",null)
                                                    .show();

                                            break;
                                        default:
                                    }
                                }
                            })
                            .show();
                }*/
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Card card = mCardList.get(position);
        holder.cardName.setText(card.getName());
        Glide.with(mContext).load(card.getImageFile()).into(holder.cardImage);
    }

    @Override
    public int getItemCount() {
        return mCardList.size();
    }

    private void setCard(String path){
        setCard(path,false);
    }

    private void setCard(String path,boolean isAutoLogo){
        Intent intent = new Intent(mContext, SetCardActivity.class);
        intent.putExtra(Config.file_Path, path);
        intent.putExtra(Config.is_Auto, isAutoLogo);
        mContext.startActivity(intent);
    }

    /** 文件重命名
     * @param file
     *            源文件
     * @param newName
     *            新的文件名
     */
    public static boolean renameFile(File file, String newName) {
        File path = file.getParentFile();
        String oldName = file.getName();
        if (!oldName.equals(newName)) {// 新的文件名和以前文件名不同时,才有必要进行重命名
            File oldfile = new File(path , oldName);
            File newfile = new File(path , newName);
            if (!oldfile.exists()) {
                return false;// 重命名文件不存在
            }
            if (newfile.exists()) {// 若在该目录下已经有一个文件和新文件名相同，则不允许重命名
                return false;
            } else {
                return oldfile.renameTo(newfile);
            }
        }  //og.error("新文件名和旧文件名相同...");
        return false;
    }

    private View setView(File bitmapFile,boolean isShowEditText){
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_add,null);
        ImageView imageView = (ImageView)view.findViewById(R.id.dialog_bmp);
        EditText editText = (EditText)view.findViewById(R.id.dialog_edit);
        Glide.with(mContext).load(bitmapFile).into(imageView);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.dialog_checkbox);
        checkBox.setVisibility(View.GONE);
        CheckBox checkBox2 = (CheckBox) view.findViewById(R.id.dialog_checkbox_logo);
        checkBox2.setVisibility(View.GONE);
        if (isShowEditText){
            editText.setText(bitmapFile.getName());
        } else {
            editText.setVisibility(View.GONE);
        }
        return view;
    }
}

