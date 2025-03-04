package com.xlab.vbrowser.z.module;

import com.novacloud.data.adblock.Config;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ZAdConfig extends Config {
    private static final String TEMP_FOLDER = System.getProperty("java.io.tmpdir") + "/";
    private static final String[] EASY_LIST_URLS = {
        "https://abpvn.com/filter/abpvn-LPDIGe.txt",
        "https://easylist.to/easylist/easylist.txt",
    };
    public String getUrlPath(){
        return TEMP_FOLDER + "adblock_urls.txt";
    }
    public String getWhitePathFromUrl(String url){
        String name = "white_"+md5(url)+".bin";
        return TEMP_FOLDER + name;
    }
    public String getBlackPathFromUrl(String url){
        String name = "black_"+md5(url)+".bin";
        return TEMP_FOLDER + name;
    }

    public String[] getUrls() {
        return EASY_LIST_URLS.clone();
    }

    public static String md5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
