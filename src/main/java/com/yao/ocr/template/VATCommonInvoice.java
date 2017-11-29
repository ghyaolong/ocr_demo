package com.yao.ocr.template;

/**
 * Created by Administrator on 2017/11/23.
 * 增值税普通发票模板
 */
public class VATCommonInvoice {

    //每张票据的抬头，如：陕西省增值税专用发票    陕西省增值税普通发票  回执单......
    private String title;

    //发票所在省
    private String province;

    //发票号码
    private String invoice_no;

    //发票代码
    private String invoice_code;

    //检验码
    private String checkCode;

    //开票日期
    private String invoice_date;

    //销售方
    private String seller;
    //销售方纳税人识别号
    private String sellerTIN;

    //销售方地址或电话
    private String sellerAddOrTel;

    //销售方开户行
    private String sellerBankDeposit;

    //销售方开户行银行账号
    private String sellerBankAccount;

    private String invoiceDate;
    //购买方
    private String purchaser;

    //购买方纳税人识别号
    private String purchaserTIN;

    //购买方地址或电话
    private String purchaserAddOrTel;

    private String purchaserBankDeposit;

    private String purchaserBankAccount;

    //开票人
    private String drawer;
    //领款人
    private String payee;

    //复核
    private String reCheck;

    //金额合计
    private Float amountSum;
    //税额合计
    private Float taxSum;

    //税价合计
    private Float amountAndTaxSum;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getSellerTIN() {
        return sellerTIN;
    }

    public void setSellerTIN(String sellerTIN) {
        this.sellerTIN = sellerTIN;
    }

    public String getSellerBankDeposit() {
        return sellerBankDeposit;
    }

    public void setSellerBankDeposit(String sellerBankDeposit) {
        this.sellerBankDeposit = sellerBankDeposit;
    }

    public String getSellerBankAccount() {
        return sellerBankAccount;
    }

    public void setSellerBankAccount(String sellerBankAccount) {
        this.sellerBankAccount = sellerBankAccount;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getPurchaser() {
        return purchaser;
    }

    public void setPurchaser(String purchaser) {
        this.purchaser = purchaser;
    }

    public String getPurchaserTIN() {
        return purchaserTIN;
    }

    public void setPurchaserTIN(String purchaserTIN) {
        this.purchaserTIN = purchaserTIN;
    }

    public String getDrawer() {
        return drawer;
    }

    public void setDrawer(String drawer) {
        this.drawer = drawer;
    }

    public String getInvoice_date() {
        return invoice_date;
    }

    public void setInvoice_date(String invoice_date) {
        this.invoice_date = invoice_date;
    }

    public String getSellerAddOrTel() {
        return sellerAddOrTel;
    }

    public void setSellerAddOrTel(String sellerAddOrTel) {
        this.sellerAddOrTel = sellerAddOrTel;
    }

    public String getPurchaserAddOrTel() {
        return purchaserAddOrTel;
    }

    public void setPurchaserAddOrTel(String purchaserAddOrTel) {
        this.purchaserAddOrTel = purchaserAddOrTel;
    }

    public String getPurchaserBankDeposit() {
        return purchaserBankDeposit;
    }

    public void setPurchaserBankDeposit(String purchaserBankDeposit) {
        this.purchaserBankDeposit = purchaserBankDeposit;
    }

    public String getPurchaserBankAccount() {
        return purchaserBankAccount;
    }

    public void setPurchaserBankAccount(String purchaserBankAccount) {
        this.purchaserBankAccount = purchaserBankAccount;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public String getReCheck() {
        return reCheck;
    }

    public void setReCheck(String reCheck) {
        this.reCheck = reCheck;
    }


    public String getInvoice_no() {
        return invoice_no;
    }

    public void setInvoice_no(String invoice_no) {
        this.invoice_no = invoice_no;
    }

    public String getInvoice_code() {
        return invoice_code;
    }

    public void setInvoice_code(String invoice_code) {
        this.invoice_code = invoice_code;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public Float getAmountSum() {
        return amountSum;
    }

    public void setAmountSum(Float amountSum) {
        this.amountSum = amountSum;
    }

    public Float getTaxSum() {
        return taxSum;
    }

    public void setTaxSum(Float taxSum) {
        this.taxSum = taxSum;
    }

    public Float getAmountAndTaxSum() {
        return amountAndTaxSum;
    }

    public void setAmountAndTaxSum(Float amountAndTaxSum) {
        this.amountAndTaxSum = amountAndTaxSum;
    }
}
