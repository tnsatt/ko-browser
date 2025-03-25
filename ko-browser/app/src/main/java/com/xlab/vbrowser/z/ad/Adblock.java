package com.xlab.vbrowser.z.ad;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.xlab.vbrowser.z.utils.Toast;

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
