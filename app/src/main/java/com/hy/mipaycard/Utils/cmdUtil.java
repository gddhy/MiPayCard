package com.hy.mipaycard.Utils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.hy.mipaycard.Config.mi_wallet;
import static com.hy.mipaycard.Config.pay_pic;

public class cmdUtil
{
	public static String runRootShell(String[] cmds){    

        String result = null;
        int ret = -1;
        java.lang.Process process;    
        try {
            process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            for(int i=0; i< cmds.length; i++) {
                os.writeBytes(cmds[i] + "\n");
            }
            os.writeBytes("exit\n");
            os.flush();
            try {
                ret = process.waitFor();
                Log.d("lx", "ret= " + ret);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            os.close();

            ByteArrayOutputStream resultOutStream = new ByteArrayOutputStream();
            InputStream errorInStream = new BufferedInputStream(process.getErrorStream());
            InputStream processInStream = new BufferedInputStream(process.getInputStream());
            int num = 0;

            byte[] bs = new byte[1024];

            while((num=errorInStream.read(bs))!=-1){

                resultOutStream.write(bs,0,num);

            }

            while((num=processInStream.read(bs))!=-1){

                resultOutStream.write(bs,0,num);

			}

            result=new String(resultOutStream.toByteArray());

            //println( "result: " + result);

            errorInStream.close();
            errorInStream=null;

            processInStream.close();
            processInStream=null;

            resultOutStream.close();
            resultOutStream=null;

        } catch (IOException e) {
            e.printStackTrace();
        }


		result =  "result code: " + ret + " details: \n" +result;
        return result;    
    } 
	
	public static String[] getTsmclient(){
		String str = runRootShell(new String[]{"cd "+pay_pic,"ls"});
		return makeLog(str);
    }

    private static String[] makeLog(String cmdLog){
        List<String> strList = new ArrayList<String>();
        while(cmdLog.contains(".0")){
            String name = tiqu(cmdLog,"\n",".0")+".0";
            if(name.contains(" \n")){
                //未知bug，有时文件名前会有" \n"
                name = name.replace(" \n","");
            }
            strList.add(name);
            cmdLog=cmdLog.replace(name," ");
        }
        return strList.toArray(new String[strList.size()]);
    }

    public static String[] getMiWallet(){
        String str = runRootShell(new String[]{"cd "+mi_wallet,"ls"});
        return makeLog(str);
    }

    public static boolean isRooted(){
	    String log = runRootShell(new String[]{"cd /data","ls"});
	    return !log.contains("Permission denied");
    }
	
	public static String tiqu(String text,String textTop,String textLast){
		return text.substring(text.indexOf(textTop)+textTop.length(),text.indexOf(textLast));
	}
}
