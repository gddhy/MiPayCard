package com.hy.mipaycard.shortcuts;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.hy.mipaycard.Utils.CardList;
import com.hy.mipaycard.Utils.cmdUtil;

import static com.hy.mipaycard.Config.pay_pic;
import static com.hy.mipaycard.Utils.cmdUtil.runRootShell;

public class CardDefaultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        CardList.initLocalCardList(CardDefaultActivity.this);
        setCardDefault();
        //cmdUtil.runRootShell(new String[]{"rm -rf "+pay_pic});
        //Toast.makeText(CardDefaultActivity.this,"已恢复默认卡面",Toast.LENGTH_LONG).show();
        //finish();
    }

    private void setCardDefault(){
        final String[] cardList = cmdUtil.getTsmclient();
        final String[] cardName = CardList.getCardName(cardList);
        new AlertDialog.Builder(CardDefaultActivity.this)
                .setTitle("请选择要恢复默认的卡面")
                .setItems(cardName,new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int i){
                        runRootShell(new String[]{"rm -rf "+pay_pic+"/"+cardList[i]});
                        Toast.makeText(CardDefaultActivity.this,"已恢复"+cardName[i]+"默认卡面",Toast.LENGTH_LONG).show();
                        setCardDefault();
                    }
                })
                .setCancelable(false)
                .setPositiveButton("全部默认",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int i){
                        runRootShell(new String[]{"rm -rf "+pay_pic});
                        Toast.makeText(CardDefaultActivity.this,"已全部恢复默认卡面",Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();
    }
}
