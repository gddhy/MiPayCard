package com.hy.mipaycard;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		init();
		copyCard();
		if(!isInstallApp(MainActivity.this,"com.miui.tsmclient")){
			Toast.makeText(MainActivity.this,"您的设备可能不支持本APP",Toast.LENGTH_LONG).show();
		}
    }
	
	private void init(){
		String[] list = {"招行初音卡","天依柠檬卡","恢复默认","打开MiPay","关于"};
		new AlertDialog.Builder(MainActivity.this)
			.setTitle("MiPay卡面工具")
			.setItems(list,new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog,int i){
					switch(i){
						case 2:
							cmdUtil.runRootShell(new String[]{"rm -rf /data/data/com.miui.tsmclient/cache/image_manager_disk_cache"});
							Toast.makeText(MainActivity.this,"已恢复默认卡面",Toast.LENGTH_LONG).show();
							init();
							break;
						case 3:
							init();
							cmdUtil.runRootShell(new String[]{"am start -n com.miui.tsmclient/com.miui.tsmclient.ui.quick.DoubleClickActivity"});
							break;
						case 4:
							new AlertDialog.Builder(MainActivity.this)
								.setTitle("关于")
								.setMessage(".本程序可以修改小米设备MiPay卡片卡面\n.需要root权限，用于读取卡列表、替换卡面、恢复默认卡面、打开MiPay\n.MiPay卡面储存在/data/data/com.miui.tsmclient/cache/image_manager_disk_cache，可以自行更改自己喜欢的图片\n.程序内置的两张卡面来自MiPay\n\n.声明:本软件仅供学习、技术交流使用，严禁用于非法行为，使用此软件所造成的一切后果由用户自行承担！")
								.setPositiveButton("返回",new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog,int i){
										init();
									}
								})
								.setCancelable(false)
								.show();
							break;
						default:
						setCard(i==0);
					}
				}
			})
			.setCancelable(false)
			.setPositiveButton("退出",new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog,int i){
					finish();
				}
			})
			.show();
			
	}
	
	private void setCard(final boolean isMiku){
		final String[] cardList = cmdUtil.getTsmclient();
		new AlertDialog.Builder(MainActivity.this)
			.setTitle("请选择要替换的卡面")
			.setItems(cardList,new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog,int i){
					String filePath = "/data/data/com.miui.tsmclient/cache/image_manager_disk_cache/"+cardList[i];
					String[] cmd = new String[]{"cp "+ new File(MainActivity.this.getFilesDir(),isMiku?"miku.png":"luotianyi.png").getPath()+" "+filePath,"chmod 444 "+filePath};
					cmdUtil.runRootShell(cmd);
					Toast.makeText(MainActivity.this,"已替换",Toast.LENGTH_LONG).show();
					init();

				}
			})
			.setCancelable(false)
			.setPositiveButton("返回",new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog,int i){
					init();
				}
			})
			.show();
	}
	
	private void copyCard(){
		File miku = new File(MainActivity.this.getFilesDir(),"miku.png");
		File lty = new File(MainActivity.this.getFilesDir(),"luotianyi.png");
		String mikuMd5 = "1912841bea156df9208609d05a85f390";
		String ltyMd5 = "e574821c45805b03cb2b72cb7d2d6024";
		
		if(!miku.exists() || !lty.exists() || !mikuMd5.equals(FileMd5.md5(miku)) || !ltyMd5.equals(FileMd5.md5(lty))){
			try{
				UnzipFromAssets.unZip(MainActivity.this, "assets.zip", MainActivity.this.getFilesDir().getPath(), true);
			} catch (IOException e){
				
			}
		}
	}
	
	public static boolean isInstallApp(Context context,String packageName) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName.toLowerCase(Locale.ENGLISH);
                if (pn.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
