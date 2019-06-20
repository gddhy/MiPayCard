package com.hy.mipaycard;

import android.util.*;
import java.io.*;
import java.util.*;

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
                // TODO Auto-generated catch block
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
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }


		result =  "result code: " + ret + " details: \n" +result;
        return result;    
    } 
	
	public static String[] getTsmclient(){
		String str = runRootShell(new String[]{"cd /data/data/com.miui.tsmclient/cache/image_manager_disk_cache","ls"});
		List<String> strList = new ArrayList<String>();
		while(str.contains(".0")){
			String name = tiqu(str,"\n",".0")+".0";
			if(name.contains(" \n")){
				//未知bug，有时文件名前会有" \n"
				name = name.replace(" \n","");
			}
			strList.add(name);
			str=str.replace(name," ");
		}
		return strList.toArray(new String[strList.size()]);
    } 
	
	public static String tiqu(String text,String textTop,String textLast){
		return text.substring(text.indexOf(textTop)+textTop.length(),text.indexOf(textLast));
	}
}
