package com.xlab.vbrowser.z.ad;

import com.novacloud.data.adblock.RuleSet;
import com.novacloud.data.adblock.RulesetAdBlockerImpl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class AdblockRuleSet extends RulesetAdBlockerImpl {
    private static final Logger logger = LoggerFactory.getLogger(AdblockRuleSet.class);
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
    @Override
    public String removeAd(String html) {
        return removeAd(Jsoup.parse(html)).html();
    }

    /**
     * remove ad from src document
     *
     * @param src src document
     * @return document that ad removed
     */
    @Override
    public Document removeAd(Document src) {
        long start = System.currentTimeMillis();
        long end = 0l;
        Elements urls = src.select("[href]");
        for (Element urlElement : urls) {
            if (ruleSet.matchesBlacklist(urlElement.attr("href"))) {
                urlElement.attr("_href", urlElement.attr("href"));
                urlElement.removeAttr("href");
            }
        }

        if (logger.isDebugEnabled()) {
            end = System.currentTimeMillis();
            logger.debug("remove href elapsed: {} ms \n", end - start);
        }
        Elements srcs = src.select("[src]");
        for (Element urlElement : srcs) {
            if (ruleSet.matchesBlacklist(urlElement.attr("src"))) {
                urlElement.attr("_src", urlElement.attr("src"));
                urlElement.removeAttr("src");
            }
        }

        if (logger.isDebugEnabled()) {
            start = end;
            end = System.currentTimeMillis();
            logger.debug("remove src elapsed: {} ms\n", end - start);
        }


        String domainName = getDomainName(src.baseUri());
        //remove ad using domain specific selectors
        Set<String> selectors = ruleSet.getAdElementSelectors(domainName);
        for (String selector : selectors) {
            try {
                src.select(selector).empty().attr("_ad", selector);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ad Element Selector error:{}", selector);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            start = end;
            end = System.currentTimeMillis();
            logger.debug("remove ad element using domain specific selector elapsed: {} ms\n", end - start);
        }
        if (selectors.size() == 0) {
            //remove ad using global selectors
            selectors = ruleSet.getAdElementSelectors();
            for (String selector : selectors) {//todo: optimize performance ,remove ad element using global selector elapsed: 2179 ms
                try {
                    src.select(selector).empty().attr("_ad", selector);
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Ad Element Selector error:{}", selector);
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                start = end;
                end = System.currentTimeMillis();
                logger.debug("remove ad element using global selector elapsed: {} ms\n", end - start);
            }
        }
        return src;
    }
    public static AdblockRuleSet getInstance() {
        return InstanceHolder.instance;
    }
    private static class InstanceHolder {
        public static AdblockRuleSet instance = new AdblockRuleSet();
    }
}
