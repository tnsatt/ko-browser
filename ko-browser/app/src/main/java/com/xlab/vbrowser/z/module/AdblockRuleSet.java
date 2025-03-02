package com.xlab.vbrowser.z.module;

import com.novacloud.data.adblock.RuleSet;
import com.novacloud.data.adblock.RulesetAdBlockerImpl;

public class AdblockRuleSet extends RulesetAdBlockerImpl {
    private final RuleSet ruleSet = new ZEasyListRuleSet(true);
    protected AdblockRuleSet(){

    }
    public void load(){
        ruleSet.load();
        ruleSet.scheduleAutoUpdate();
    }
    public void loadBackground(){
        (new Thread(new Runnable() {
            @Override
            public void run() {
                load();
            }
        })).start();
    }
    public RuleSet getRuleSet() {
        return ruleSet;
    }
    public static AdblockRuleSet getInstance() {
        return InstanceHolder.instance;
    }
    private static class InstanceHolder {
        public static AdblockRuleSet instance = new AdblockRuleSet();
    }
}
