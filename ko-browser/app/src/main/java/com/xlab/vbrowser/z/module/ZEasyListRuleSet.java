package com.xlab.vbrowser.z.module;

import com.novacloud.data.adblock.Config;
import com.novacloud.data.adblock.EasyListRuleSet;
import com.novacloud.data.adblock.io.SerializedFile;
import com.novacloud.data.adblock.model.Rule;

import java.io.IOException;
import java.util.Calendar;
import java.util.Set;

public class ZEasyListRuleSet extends EasyListRuleSet {
    public ZEasyListRuleSet(boolean useWhitelist) {
        super(useWhitelist);
    }
    @Override
    public synchronized void load() {
        super.load();
        if(getRuleCount()==0){
            loadInternet();
        }
    }
}
