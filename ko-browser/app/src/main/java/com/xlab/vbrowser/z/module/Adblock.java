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
                EasyListRuleSet ruleSet = (EasyListRuleSet) rulesetAdBlocker.getRuleSet();
//                try {
//                    String url = "https://easylist-downloads.adblockplus.org/easylist.txt";
//                    List<Rule> rules = new InternetLoader(new URL(url)).load();
//                    for (Rule rule : rules) {
//                        ruleSet.add(rule);
//                    }
//                }catch (Exception e){
//                    toast(context, e.toString());
//                }
                ruleSet.load();
//                rulesetAdBlocker.load();

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
