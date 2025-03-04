package com.xlab.vbrowser.z.module;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.novacloud.data.adblock.EasyListRuleSet;

public class Adblock {
    public static void init(Context context){
        AdblockRuleSet rulesetAdBlocker = AdblockRuleSet.getInstance();
        (new Thread(new Runnable() {
            @Override
            public void run() {
                ZEasyListRuleSet ruleSet = (ZEasyListRuleSet) rulesetAdBlocker.getRuleSet();
                ruleSet.load();

                toast(context, ruleSet.getRuleCount()+" rules");
            }
        })).start();
    }
    public static void reload(Context context){
        AdblockRuleSet rulesetAdBlocker = AdblockRuleSet.getInstance();
        (new Thread(new Runnable() {
            @Override
            public void run() {
                ZEasyListRuleSet ruleSet = (ZEasyListRuleSet) rulesetAdBlocker.getRuleSet();
                ruleSet.refresh();
                toast(context, ruleSet.getRuleCount()+" rules");
            }
        })).start();
    }
    public static void toast(Context context, String s){
        (new Handler(Looper.getMainLooper())).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
