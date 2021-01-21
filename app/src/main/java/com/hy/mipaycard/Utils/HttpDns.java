package com.hy.mipaycard.Utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Dns;

//https://blog.csdn.net/sbsujjbcy/article/details/51612832
public class HttpDns implements Dns {
    private static final Dns SYSTEM = Dns.SYSTEM;
    private static final String rawGitHost = "raw.githubusercontent.com";
    private static final String rawGitIPDefault = "151.101.128.133";

    //ip来自 http://119.29.29.29/d?dn=raw.githubusercontent.com

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        //Log.e("HttpDns", "lookup:" + hostname);

        //国内部分地区dns污染，无法解析raw.githubusercontent.com，仅自定义这个解析
        if(hostname.equals(rawGitHost)){
            List<InetAddress> addresses = Arrays.asList(InetAddress.getAllByName(rawGitIPDefault));
            return addresses;
        }

        return SYSTEM.lookup(hostname);
    }
}