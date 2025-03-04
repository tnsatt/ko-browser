package com.xlab.vbrowser.z.module;

import com.novacloud.data.adblock.Config;
import com.novacloud.data.adblock.EasyListRuleSet;
import com.novacloud.data.adblock.RuleSet;
import com.novacloud.data.adblock.cache.LocalCache;
import com.novacloud.data.adblock.io.SerializedFile;
import com.novacloud.data.adblock.loader.InternetLoader;
import com.novacloud.data.adblock.model.Rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ZEasyListRuleSet implements RuleSet {
    private static final Logger logger = LoggerFactory.getLogger(ZEasyListRuleSet.class);
    private final List<Set<Rule>> whitelists;
    private final List<Set<Rule>> blacklists;
    private final Map<String, Set<Rule>> whitelistsMap;
    private final Map<String, Set<Rule>> blacklistsMap;
    private final Object blacklock = new Object();
    private final Object whitelock = new Object();
    private List<String> internetUrls;
    private static final ZAdConfig config = new ZAdConfig();
    private final static ConcurrentMap<String, ConcurrentSkipListSet<String>> selectors = new ConcurrentHashMap<>(512);
    private static final AtomicLong FILTER_HIT = new AtomicLong(0);
    private static final AtomicLong CACHE_HIT = new AtomicLong(0);
    private static final AtomicLong NB_REQUEST = new AtomicLong(0);
    private static final AtomicLong PROCESS_TIME = new AtomicLong(0);
    private static final LocalCache<String, Boolean> URL_EXCEPTIONS_CACHE = new LocalCache<>("easylist_exceptions", 1500);
    private static final LocalCache<String, Boolean> URL_EXCLUSIONS_CACHE = new LocalCache<>("easylist_exclusions", 1500);
    private static final AtomicInteger autoUpdateThreadCount = new AtomicInteger(0);
    private boolean useWhitelist;
    static {
        selectors.put("*",new ConcurrentSkipListSet<String>());
    }
    public ZEasyListRuleSet(boolean useWhitelist) {
        this.internetUrls = new ArrayList(Arrays.asList(config.getUrls()));
        loadUrls();

        this.whitelists = new ArrayList<>();
        this.blacklists = new ArrayList<>();
        this.whitelistsMap = new HashMap<>();
        this.blacklistsMap = new HashMap<>();

        this.useWhitelist = useWhitelist;
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                URL_EXCEPTIONS_CACHE.save();
                URL_EXCLUSIONS_CACHE.save();
            }
        }));
    }
//    @Override
//    public synchronized void load() {
//        super.load();
//        if(getRuleCount()==0){
//            loadInternet();
//        }
//    }
    @Override
    public boolean isUseWhitelist() {
    return useWhitelist;
}

    @Override
    public void setUseWhitelist(final boolean useWhitelist) {
        this.useWhitelist = useWhitelist;
    }
    @Override
    public boolean isEmpty() {
        return getRuleCount() == 0;
    }
    @Override
    public int getRuleCount() {
        int count = 0;
        synchronized (whitelock){
            for(Set<Rule> item: whitelists){
                count+=item.size();
            }
        }
        synchronized (blacklock){
            for(Set<Rule> item: blacklists){
                count+=item.size();
            }
        }
        return count;
    }
    @Override
    public int getExclusionRuleCount(){
        int count = 0;
        synchronized (blacklock){
            for(Set<Rule> item: blacklists){
                count+=item.size();
            }
        }
        return count;
    }
    @Override
    public int getExceptionRuleCount(){
        int count = 0;
        synchronized (whitelock){
            for(Set<Rule> item: whitelists){
                count+=item.size();
            }
        }
        return count;
    }
    public void add(final Rule rule, Set<Rule> blacklist, Set<Rule> whitelist) {
        switch (rule.getType()) {
            case exception:
                whitelist.add(rule);
                break;
            case exclusion:
                blacklist.add(rule);
                if (rule.getSelector() !=null && rule.getSelector().length()>0 && rule.isForAllDomain()){
                    selectors.get("*").add(rule.getSelector());
                }
                break;
            case empty:
                break;
        }
    }
    public void addAll(final ZEasyListRuleSet easyList) {
        synchronized (whitelock){
            whitelists.addAll(easyList.whitelists);
        }
        synchronized (blacklock){
            blacklists.addAll(easyList.blacklists);
        }
    }
    @Override
    public void load() {
        loadAll(false);
    }
    public void refresh(){
        loadAll(true);
    }
    public synchronized void loadAll(Boolean skip){
        for(String url: internetUrls){
            if(skip && getRuleCount(url)>0) continue;
            try {
                loadUrl(url);
            }catch (Exception e){}
        }
        loadCache();
    }
    public List<String> getInternetUrls(){
        return internetUrls;
    }
    public List<String> getAllInternetUrls(){
        return new ArrayList(Arrays.asList(config.getUrls()));
    }
    public void loadUrl(String url) throws Exception {
        deb("loadUrl", url);
        String blackfile = config.getBlackPathFromUrl(url);
        final SerializedFile<Set<Rule>> localBlacklistFile = new SerializedFile<>(blackfile);
        if (!localBlacklistFile.exists() || localBlacklistFile.isOlder(Calendar.DAY_OF_YEAR, Config.EASYLIST_UPDATE_PERIOD_DAY)) {
            deb("loadUrl", "new");
            loadUrlFromInternet(url);
        }else {
            try {
                //todo: old local Black and white list File version  cause load error
                final long start = System.currentTimeMillis();
                Set<Rule> blacklist = localBlacklistFile.load();
                Set<Rule> whitelist = new SerializedFile<Set<Rule>>(config.getWhitePathFromUrl(url)).load();
                addBlackList(blacklist, url);
                addWhiteList(whitelist, url);
                deb("loadUrl", "blacklist "+blacklist.size());
            } catch (ClassNotFoundException | IOException e) {
                deb("loadUrl", e.toString());
                throw e;
            }
            if(getRuleCount(url)==0){
                loadUrlFromInternet(url);
            }
        }
    }
    public void loadUrlFromInternet(String url) throws MalformedURLException {
        try {
            deb("loadUrlFromInternet", url);
            List<Rule> rules = new InternetLoader(new URL(url)).load();
            deb("loadUrlFromInternet", "Rules "+rules.size());
            Set<Rule> blacklist = getBlackList(url);
            Set<Rule> whitelist = getWhiteList(url);
            blacklist.clear();
            whitelist.clear();
            for (Rule rule : rules) {
                add(rule, blacklist, whitelist);
            }
        } catch (final Exception e) {
            deb("loadUrlFromInternet", e.toString());
            throw e;
        }
        save(url);
    }
    public int getRuleCount(String url){
        int count = 0;
        synchronized (blacklock){
            if(blacklistsMap.containsKey(url)){
                count+=blacklistsMap.get(url).size();
            }
        }
        synchronized (whitelock){
            if(whitelistsMap.containsKey(url)){
                count+=whitelistsMap.get(url).size();
            }
        }
        return count;
    }
    public void addBlackList(Set<Rule> blacklist, String url){
        synchronized (blacklist){
            if(blacklistsMap.containsKey(url)){
                Set<Rule> blacklist2 = blacklistsMap.get(url);
                blacklistsMap.remove(url);
                if(blacklists.contains(blacklist2)){
                    blacklists.remove(blacklist2);
                }
            }
            blacklists.add(blacklist);
            blacklistsMap.put(url, blacklist);
        }
    }
    public void addWhiteList(Set<Rule> whitelist, String url){
        synchronized (whitelock){
            if(whitelistsMap.containsKey(url)){
                Set<Rule> whitelist2 = whitelistsMap.get(url);
                whitelistsMap.remove(url);
                if(whitelists.contains(whitelist2)){
                    whitelists.remove(whitelist2);
                }
            }
            whitelists.add(whitelist);
            whitelistsMap.put(url, whitelist);
        }
    }
    public void deb(String e, String msg){
        logger.debug(e, msg);
    }

    /**
     * schedule update easylist from internet
     */
    @Override
    public void scheduleAutoUpdate() {
//        if (autoUpdateThreadCount.compareAndSet(0,1)) {
//            new Timer("easy list auto update", true).scheduleAtFixedRate(new TimerTask() {
//                @Override
//                public void run() {
//                    loadInternet();
//                }
//            }, Config.EASYLIST_UPDATE_PERIOD_DAY * 24 * 60 * 60 * 1000, Config.EASYLIST_UPDATE_PERIOD_DAY * 24 * 60 * 60 * 1000);
//        }else {
//        }
    }
    private void loadCache() {
        final long start = System.currentTimeMillis();
        URL_EXCEPTIONS_CACHE.load();
        URL_EXCLUSIONS_CACHE.load();
    }
    private Set<Rule> getBlackList(String url){
        synchronized (blacklock){
            if(blacklistsMap.containsKey(url)){
                return blacklistsMap.get(url);
            }
        }
        Set<Rule> blacklist = new CopyOnWriteArraySet<>();
        addBlackList(blacklist, url);
        return blacklist;
    }
    private Set<Rule> getWhiteList(String url){
        synchronized (whitelock){
            if(whitelistsMap.containsKey(url)){
                return whitelistsMap.get(url);
            }
        }
        Set<Rule> whitelist = new CopyOnWriteArraySet<>();
        addWhiteList(whitelist, url);
        return whitelist;
    }
    public void saveUrls(){
        String file = config.getUrlPath();
        SerializedFile<List<String>> o = new SerializedFile<List<String>>(file);
        try {
            o.save(internetUrls);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void loadUrls(){
        String file = config.getUrlPath();
        SerializedFile<List<String>> o = new SerializedFile<List<String>>(file);
        if(o.exists()){
            try {
                internetUrls = o.load();
                if(internetUrls.size() == 0){
                    internetUrls = new ArrayList(Arrays.asList(config.getUrls()));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void addUrl(String url){
        if(internetUrls.contains(url)) return;
        internetUrls.add(url);
        saveUrls();
    }
    public void removeUrl(String url){
        if(internetUrls.contains(url)){
            internetUrls.remove(url);
            saveUrls();
        }
        removeBlack(url);
        removeWhite(url);
    }
    public void cleanUrl(String url){
        removeBlack(url);
        removeWhite(url);
    }
    private void removeBlack(String url){
        synchronized (blacklock){
            if(blacklistsMap.containsKey(url)){
                Set<Rule> blacklist = blacklistsMap.get(url);
                blacklistsMap.remove(url);
                if(blacklists.contains(blacklist)){
                    blacklists.remove(blacklist);
                }
            }
        }
    }
    private void removeWhite(String url){
        synchronized (whitelock){
            if(whitelistsMap.containsKey(url)){
                Set<Rule> whitelist = whitelistsMap.get(url);
                whitelistsMap.remove(url);
                if(whitelists.contains(whitelist)){
                    whitelists.remove(whitelist);
                }
            }
        }
    }
    private void save(String url) {
        try {
            new SerializedFile<Set<Rule>>(config.getBlackPathFromUrl(url)).save(getBlackList(url));
        } catch (final IOException e) {
        }
        try {
            new SerializedFile<Set<Rule>>(config.getWhitePathFromUrl(url)).save(getWhiteList(url));
        } catch (final IOException e) {
        }
    }
    @Override
    public boolean matchesWhitelist(final String url) {
        return matches(url, URL_EXCEPTIONS_CACHE, whitelists);
    }

    @Override
    public boolean matchesBlacklist(final String url) {
        return matches(url, URL_EXCLUSIONS_CACHE, blacklists);
    }
    @Override
    public Set<String> getAdElementSelectors() {
        ConcurrentSkipListSet<String> set = selectors.get("*");
        if (set.size()<=0 ){
            for(Set<Rule> blacklist: blacklists) {
                for (Rule rule : blacklist) {
                    if (rule.getSelector() != null && rule.getSelector().length() > 0 && rule.isForAllDomain()) {
                        set.add(rule.getSelector());
                    }
                }
            }
        }
        return set;
    }
    @Override
    @Nonnull
    public Set<String> getAdElementSelectors(@Nullable String domain) {
        if(domain==null || domain.length()==0 ||"*".equals(domain)) {
            return getAdElementSelectors();
        }
        ConcurrentSkipListSet<String> set=selectors.get(domain);
        if (set==null){
            set = new ConcurrentSkipListSet<>();
            for(Set<Rule> blacklist: blacklists) {
                for (Rule rule : blacklist) {
                    if (rule.getSelector() != null && rule.getSelector().length() > 0 && rule.containDomain(domain)) {
                        set.add(rule.getSelector());
                    }
                }
            }
            selectors.putIfAbsent(domain,set);
        }
        return set;
    }
    private boolean matches(final String url, final LocalCache<String, Boolean> urlCache, final List<Set<Rule>> rules) {
        final long timer = System.nanoTime();
        NB_REQUEST.incrementAndGet();
        Boolean match = urlCache.get(url);
        if (match != null) {
            CACHE_HIT.incrementAndGet();
        } else {
            match = matches(url, rules);
            urlCache.put(url, match);
        }
        if (match) {
            FILTER_HIT.incrementAndGet();
        }
        PROCESS_TIME.addAndGet(System.nanoTime() - timer);

        return match;
    }

    private boolean matches(final String url, final List<Set<Rule>> rulelists) {
        for(Set<Rule> rules: rulelists) {
            for (final Rule rule : rules) {
                if (rule.applies(url)) {
                    return true;
                }
            }
        }
        return false;
    }
    public void deleteUrl(String url){
        File blackfile = new File(config.getBlackPathFromUrl(url));
        if(blackfile.exists()) blackfile.delete();
        File whitefile = new File(config.getWhitePathFromUrl(url));
        if(whitefile.exists()) whitefile.delete();
    }
}
