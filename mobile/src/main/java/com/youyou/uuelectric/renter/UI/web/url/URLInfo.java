package com.youyou.uuelectric.renter.UI.web.url;

/**
 * Created by liuchao on 2015/9/15.
 * WEB VIEW URL数据
 */
public class URLInfo {
    // 注册协议
    public WebUrl register = null;
    // 帮助中心URL
    public WebUrl helpCenter = null;
    // 分享URL
    public WebUrl share = null;
    // 余额URL
    public WebUrl balanceList = null;
    // 活动URL
    public WebUrl activeList = null;
    // 收费规则
    public WebUrl billingRule = null;
    // 平台规则URL
    public WebUrl platRule = null;
    // 发票
    /**
     * 若发票信息为空的话则不显示
     */
    public WebUrl invoice = null;
    /**
     * 待支付费用
     */
    public WebUrl paymentNotice = null;
    /**
     * 待处理违章
     */
    public WebUrl peccancyList = null;
    /**
     * 用车早知道
     */
    public WebUrl faq = null;
    /**
     * H5页面需要的domain
     *  ##### UserInfo 中的domain不再使用 ######
     */
    public String b4Domain = null;

    /**
     * 侧边栏充值活动文案
     */
    public String rechargeDesc = null;

    public String getRechargeDesc() {
        return rechargeDesc;
    }

    public void setRechargeDesc(String rechargeDesc) {
        this.rechargeDesc = rechargeDesc;
    }

    public WebUrl getHelpCenter() {
        return helpCenter;
    }

    public WebUrl getShare() {
        return share;
    }

    public WebUrl getBalanceList() {
        return balanceList;
    }

    public WebUrl getActiveList() {
        return activeList;
    }

    public WebUrl getBillingRule() {
        return billingRule;
    }

    public WebUrl getInvoice() {
        return invoice;
    }

    public void setHelpCenter(WebUrl helpCenter) {
        this.helpCenter = helpCenter;
    }

    public void setShare(WebUrl share) {
        this.share = share;
    }

    public void setBalanceList(WebUrl balanceList) {
        this.balanceList = balanceList;
    }

    public void setActiveList(WebUrl activeList) {
        this.activeList = activeList;
    }

    public void setBillingRule(WebUrl billingRule) {
        this.billingRule = billingRule;
    }

    public void setInvoice(WebUrl invoice) {
        this.invoice = invoice;
    }

    public WebUrl getRegister() {
        return register;
    }

    public void setRegister(WebUrl register) {
        this.register = register;
    }

    public WebUrl getPlatRule() {
        return platRule;
    }

    public void setPlatRule(WebUrl platRule) {
        this.platRule = platRule;
    }

    public String getB4Domain() {
        return b4Domain;
    }

    public void setB4Domain(String b4Domain) {
        this.b4Domain = b4Domain;
    }

    public void setPaymentNotice(WebUrl paymentNotice) {
        this.paymentNotice = paymentNotice;
    }

    public void setPeccancyList(WebUrl peccancyList) {
        this.peccancyList = peccancyList;
    }

    public WebUrl getPaymentNotice() {
        return paymentNotice;
    }

    public WebUrl getPeccancyList() {
        return peccancyList;
    }

    public WebUrl getFaq() {
        return faq;
    }

    public void setFaq(WebUrl faq) {
        this.faq = faq;
    }
}
