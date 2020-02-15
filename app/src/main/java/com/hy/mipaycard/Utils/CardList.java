package com.hy.mipaycard.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hy.mipaycard.Config.fileWork;

public class CardList {
    private static String[] CardName;
    private static String[] CardFile;

    public static String getAboutText(){
        String str = "关于信息";
        for (int i = 0;i<CardFile.length;i++){
            if(str.equals(CardFile[i])){
                return CardName[i];
            }
        }
        return str;
    }

    public static String getCardName(String cardFile){
        for (int i = 0;i<CardFile.length;i++){
            if(cardFile.equals(CardFile[i])){
                return CardName[i];
            }
        }
        return cardFile;
    }

    public static String[] getCardName(String[] cardFile){
        String[] tmp = new String[cardFile.length];
        for(int i = 0;i<tmp.length;i++){
            tmp[i]=getCardName(cardFile[i]);
        }
        return tmp;
    }

    public static String[] getWeiShiPeiFileName(){
        String[] nameList = getCardName(cmdUtil.getTsmclient());
        List<String> strList = new ArrayList<String>();
        for (int i =0;i<nameList.length;i++){
            if (!checkChinese(nameList[i])){
                strList.add(nameList[i]);
            }
        }
        return strList.toArray(new String[strList.size()]);
    }

    public static void save(Context context,String data) {
        BufferedWriter writer = null;
        File file = new File(fileWork(context).getParentFile(),"CardInfo.txt");//new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"MiPayCard/CardInfo.txt");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @SuppressLint("SimpleDateFormat") String time = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
        try {
            //append 续写true覆写false
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true), StandardCharsets.UTF_8));
            writer.write("----"+time+"----\n\n");
            writer.write(data);
            writer.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(writer != null){
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean checkChinese(String sequence) {
        final String format = "[\u4e00-\u9fa5]";
        boolean result;
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(sequence);
        result = matcher.find();
        return result;
    }

    public static void  copyFile(String  oldPath,  String  newPath)  {
        try  {
            int  byteread  =  0;
            File  oldfile  =  new  File(oldPath);
            if  (oldfile.exists())  {
                InputStream inStream  =  new FileInputStream(oldPath);
                FileOutputStream  fs  =  new  FileOutputStream(newPath);
                byte[]  buffer  =  new  byte[1444];
                while  (  (byteread  =  inStream.read(buffer))  !=  -1)  {
                    fs.write(buffer,  0,  byteread);
                }
                inStream.close();
            }
        } catch  (Exception  e)  {
            e.printStackTrace();

        }
    }


    //json
    public static void getListFromJson(String jsonData){
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            CardName = new String[jsonArray.length()];
            CardFile = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                CardName[i] = jsonObject.getString("CardName");
                CardFile[i] = jsonObject.getString("CardFile");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //https://blog.csdn.net/weixin_36838630/article/details/79198920
    private static String getJsonData(Context c) {
        AssetManager assetManager = c.getResources().getAssets();
        InputStream inputStream = null;
        BufferedReader br = null;
        StringBuilder a = new StringBuilder();
        try {
            inputStream = assetManager.open("card_list.json");
            br = new BufferedReader(new InputStreamReader(inputStream));
            String temp;
            while ((temp = br.readLine()) != null) {
                temp += "\n";
                a.append(temp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return a.toString();
    }


    //写入
    public static void saveJsonData(Context context, String data) {
        BufferedWriter writer = null;
        File file = new File(context.getFilesDir(),"card_list.json");
        //如果文件不存在，则新建一个
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //写入
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,false), StandardCharsets.UTF_8));
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(writer != null){
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //读取
    private static String readJsonFromFile(Context context) {
        File file = new File(context.getFilesDir(),"card_list.json");
        if(!file.exists()){
            return null;
        }
        BufferedReader reader = null;
        StringBuilder laststr = new StringBuilder();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            reader = new BufferedReader(inputStreamReader);
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                laststr.append(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return laststr.toString();
    }

    public static int getJsonLine(String jsonData){
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            return jsonArray.length();
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }


    public static String initLocalCardList(Context context){
        File file = new File(context.getFilesDir(),"card_list.json");
        String str;
        if(file.exists()){
            str = readJsonFromFile(context);
        } else {
            str = getJsonData(context);
        }
        getListFromJson(str);
        return str;
    }
}
