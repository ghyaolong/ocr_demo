package com.yao.ocr.parseJSON;

import com.alibaba.fastjson.JSON;
import com.yao.ocr.module.Location;
import com.yao.ocr.module.OCREntity;
import com.yao.ocr.module.Words;
import com.yao.ocr.module.WordsResult;
import com.yao.ocr.template.VATCommonInvoice;
import com.yao.ocr.util.MyStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by yaochenglong on 2017/11/23.
 * 增值税普通发票解析器
 */
public class ParseVATCommonInvoice {

    private static final Logger logger = LoggerFactory.getLogger(ParseVATCommonInvoice.class);

    public static void main(String[] args) {
        //System.out.println(baseResult);
        OCREntity entity = JSON.parseObject(baseResult2, OCREntity.class);
        System.out.println();
        Words data = entity.getData();
        Set<WordsResult> wordsResults = data.getWords_result();

        List<WordsResult> list = new ArrayList(wordsResults);
        Collections.sort(list);
        VATCommonInvoice vat = new VATCommonInvoice();
        //记录循环的计数器
        int count = 0;
        //公司名称计数器,第一次出现则为购买方，第二次出现带有名称的字符则为销售方名称。
        int nameCount = 0;
        //纳税人识别号计数器,第一次出现则为购买方，第二次出现带有名称的字符则为销售方名称。
        int TINCount = 0;

        float number1 = 0;
        float number2 = 0;




        for (int i = 0; i < list.size(); i++) {
            WordsResult wordsResult = list.get(i);
            //取出普通发票的抬头
            Location location = wordsResult.getLocation();
            //获取抬头
            String value = wordsResult.getWords();
            if(value.indexOf("发票")!=-1){
                if(value.indexOf("普通")!=-1||value.indexOf("通")!=-1){
                    vat.setTitle("陕西省增值税普通发票");
                    logger.info("发票title："+value);
                }
            }

            /**
             * 无法获取发票号码等属性，因为目前百度的票据识别服务识别出来的发票号码等属性不具有规律性，
             * 目前无法获取，看等到12月份百度出的模板功能，是否可以取出相关的属性数据
             */
            //发票号码
            //xxx 发票号码

            //发票代码 xxx 发票代码


            //发票校验码 xxx 发票校验码


            //开票日期
            if(value.indexOf("开票日期")!=-1){
                String invoice_date = value.substring(value.indexOf(":")+1);
                vat.setInvoice_date(invoice_date);
                logger.info("开票日期:"+invoice_date);
            }
            // 称:西安炳益智能电子科技有限公司
            //名称

            if((value.indexOf("名称")!=-1 ||value.indexOf("称")!=-1||value.indexOf("称:")!=-1)&&value.indexOf("劳")==-1){
                //System.out.println(location.getTop());
                if(value.indexOf(":")!=-1){
                    value = value.substring(value.indexOf(":")+1);
                }else{
                    value = value.substring(value.indexOf("称")+1);
                }

                if(nameCount==0){
                    vat.setPurchaser(value);
                    nameCount++;
                }else{
                    vat.setSeller(value);
                }

                /*if(location.getTop()<400){
                    vat.setPurchaser(value);
                }else{
                    vat.setSeller(value);
                }*/

                logger.info("销售方名称:"+value);
            }

            //购买方/销售方纳税人识别号
            /*String tinReg ="\\*^*(\\\\d{15}|\\\\d{18}|\\\\d{20})$";
            boolean isMatch = Pattern.matches(tinReg,value);
            if(isMatch){
               System.out.println(":::::::"+value);
            }*/

            String TIN = "";
            if (value.indexOf("纳税人识别号")!=-1){


                TIN = value.substring(value.indexOf(":")!=-1?value.indexOf(":")+1:value.length());
                if (TINCount==0){
                    //购买方纳税人
                    vat.setPurchaserTIN(TIN);
                    logger.info("购买方纳税人:"+TIN);
                }else{
                    //销售方，有可能是空
                    vat.setSellerTIN(TIN);
                    logger.info("销售方纳税人:"+TIN);
                }
                /*if (location.getTop()<500){
                    //购买方纳税人
                    vat.setPurchaserTIN(TIN);
                    logger.info("购买方纳税人:"+TIN);
                }else{
                    //销售方，有可能是空
                    vat.setSellerTIN(TIN);
                    logger.info("销售方纳税人:"+TIN);
                }*/

                TINCount++;

            }


            //继续查找销售方
            if(value.length()==18){
                //继续查找，
                vat.setSellerTIN(value);
                logger.info("销售方纳税人:"+TIN);
            }

            //购买方/销售方电话或地址
            //   6//地址、电话:西安市高新区高新路6号1幢1单元12501宽13571882169//96>9/4/4+9128*-/9>719>7<547
            //   地址、电话:西安市高新区锦业一路19号旗远铺抛2号楼二层1020T029-89381952
            String addOrTel="";
            if(value.indexOf("电话")!=-1){
                //

                if(value.indexOf("//")!=-1){
                    addOrTel = value.substring(value.indexOf("//")+2);
                }

                if(value.indexOf("//")!=-1 && value.lastIndexOf("//")!=-1){

                    //addOrTel = addOrTel.substring(value.indexOf("//")+2,value.lastIndexOf("//"));
                    addOrTel = value.substring(value.indexOf("//")+2,value.lastIndexOf("//"));
                }
                addOrTel = addOrTel==""?value:addOrTel;
                addOrTel = addOrTel.substring(addOrTel.indexOf(":")!=-1?addOrTel.indexOf(":")+1:addOrTel.length());

                if(location.getTop()<700){
                    //购买方地址或电话
                    vat.setPurchaserAddOrTel(addOrTel);
                }else{
                    //销售方地址或电话
                    vat.setSellerAddOrTel(addOrTel);
                }
            }


            //开户行及账号
            String sellerBankDepositOrAccount="";
            if(value.indexOf("开户行及账号")!=-1){
                int pos = value.indexOf(":");
                if (pos!=-1){
                    sellerBankDepositOrAccount = value.substring(value.indexOf(":")!=-1?value.indexOf(":")+1:value.length());
                }else{
                    sellerBankDepositOrAccount = value.substring(value.indexOf("号")!=-1?value.indexOf("号")+1:value.length());
                }
                String bankAccount = MyStringUtils.extractNumFromString(value).replace("/","");
                String baseStr  = MyStringUtils.evictNumFromString(sellerBankDepositOrAccount);
                String bankDeposit = baseStr.substring(baseStr.indexOf(":")+1).replace("/","");
                if(location.getTop()<700 && !MyStringUtils.isEmtpy(value)){
                    vat.setPurchaserBankAccount(bankAccount);
                    vat.setPurchaserBankDeposit(bankDeposit);
                }else{
                    vat.setSellerBankAccount(bankAccount);
                    vat.setSellerBankDeposit(bankDeposit);
                }
            }


            //收款人
            if (value.indexOf("收款人")!=-1){
                //String payee = value.substring(value.indexOf(":")!=-1?value.indexOf(":")+1:value.length());
                //vat.setPayee(payee);

                int pos = value.indexOf(":");
                String payee;
                if (pos!=-1){
                    payee = value.substring(value.indexOf(":")!=-1?value.indexOf(":")+1:value.length());
                }else{
                    payee = value.substring(value.indexOf("人")!=-1?value.indexOf("人")+1:value.length());
                }
                vat.setReCheck(payee);
            }

            //复核
            if (value.indexOf("复核")!=-1){
                //String reCheck = value.substring(value.indexOf(":")!=-1?value.indexOf(":")+1:value.length());
                int pos = value.indexOf(":");
                String reCheck;
                if (pos!=-1){
                    reCheck = value.substring(value.indexOf(":")!=-1?value.indexOf(":")+1:value.length());
                }else{
                    reCheck = value.substring(value.indexOf("人")!=-1?value.indexOf("人")+1:value.length());
                }
                vat.setReCheck(reCheck);
            }


            //开票人
            if (value.indexOf("开票人")!=-1){
                int pos = value.indexOf(":");
                String drawer;
                if (pos!=-1){
                    drawer = value.substring(value.indexOf(":")!=-1?value.indexOf(":")+1:value.length());
                }else{
                    drawer = value.substring(value.indexOf("人")!=-1?value.indexOf("人")+1:value.length());
                }
                vat.setDrawer(drawer);
            }
            //


            //合计金额

            if (value.indexOf("￥")!=-1){
                String result = MyStringUtils.extractFlatOrDoubleFromString(value).replace(",",".");
                if(count==0){
                    number1 = Float.parseFloat(result);
                }else if (count == 1){
                    number2 = Float.parseFloat(result);

                    if(number1<number2){
                        vat.setAmountSum(number2);
                        vat.setTaxSum(number1);
                    }else{
                        vat.setAmountSum(number1);
                        vat.setTaxSum(number2);
                    }

                }/*else if (count == 2){
                    //税价合计
                    vat.setAmountAndTaxSum(result);
                }*/
                count++;
            }

            //税价合计
            if(value.indexOf("小写")!=-1){
                String result = MyStringUtils.extractFlatOrDoubleFromString(value);
                vat.setAmountAndTaxSum(Float.parseFloat(result));
                //System.out.println("税价合计:"+result);
            }


            //摘要
        }
        String s = JSON.toJSONString(vat);
        System.out.println(s);



    }

    static String baseResult = "{\n" +
            "\t\"errno\": 0,\n" +
            "\t\"msg\": \"success\",\n" +
            "\t\"data\": {\n" +
            "\t\t\"words_result_num\": 67,\n" +
            "\t\t\"words_result\": [\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.032964,\n" +
            "\t\t\t\t\t\"average\": 0.896663,\n" +
            "\t\t\t\t\t\"min\": 0.526981\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 691,\n" +
            "\t\t\t\t\t\"top\": 103,\n" +
            "\t\t\t\t\t\"height\": 89,\n" +
            "\t\t\t\t\t\"left\": 261\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"回圣口6100171320\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.004965,\n" +
            "\t\t\t\t\t\"average\": 0.95908,\n" +
            "\t\t\t\t\t\"min\": 0.815114\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 947,\n" +
            "\t\t\t\t\t\"top\": 52,\n" +
            "\t\t\t\t\t\"height\": 164,\n" +
            "\t\t\t\t\t\"left\": 1081\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"陕西增花通发票№\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.00009,\n" +
            "\t\t\t\t\t\"average\": 0.992767,\n" +
            "\t\t\t\t\t\"min\": 0.968441\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 317,\n" +
            "\t\t\t\t\t\"top\": 100,\n" +
            "\t\t\t\t\t\"height\": 91,\n" +
            "\t\t\t\t\t\"left\": 2012\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"14062420\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.003789,\n" +
            "\t\t\t\t\t\"average\": 0.976479,\n" +
            "\t\t\t\t\t\"min\": 0.791919\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 200,\n" +
            "\t\t\t\t\t\"top\": 153,\n" +
            "\t\t\t\t\t\"height\": 44,\n" +
            "\t\t\t\t\t\"left\": 2413\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"6100171320\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000001,\n" +
            "\t\t\t\t\t\"average\": 0.998211,\n" +
            "\t\t\t\t\t\"min\": 0.997015\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 228,\n" +
            "\t\t\t\t\t\"top\": 203,\n" +
            "\t\t\t\t\t\"height\": 47,\n" +
            "\t\t\t\t\t\"left\": 2380\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"14062420\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.007879,\n" +
            "\t\t\t\t\t\"average\": 0.973133,\n" +
            "\t\t\t\t\t\"min\": 0.575832\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 783,\n" +
            "\t\t\t\t\t\"top\": 261,\n" +
            "\t\t\t\t\t\"height\": 94,\n" +
            "\t\t\t\t\t\"left\": 323\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"论的校验码81137175201335742089\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.053633,\n" +
            "\t\t\t\t\t\"average\": 0.754348,\n" +
            "\t\t\t\t\t\"min\": 0.522759\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 220,\n" +
            "\t\t\t\t\t\"top\": 231,\n" +
            "\t\t\t\t\t\"height\": 80,\n" +
            "\t\t\t\t\t\"left\": 1354\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"票联\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000002,\n" +
            "\t\t\t\t\t\"average\": 0.999026,\n" +
            "\t\t\t\t\t\"min\": 0.995455\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 576,\n" +
            "\t\t\t\t\t\"top\": 256,\n" +
            "\t\t\t\t\t\"height\": 75,\n" +
            "\t\t\t\t\t\"left\": 1995\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"开票日期:2017年08月26日\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.047232,\n" +
            "\t\t\t\t\t\"average\": 0.812273,\n" +
            "\t\t\t\t\t\"min\": 0.504924\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 117,\n" +
            "\t\t\t\t\t\"top\": 250,\n" +
            "\t\t\t\t\t\"height\": 78,\n" +
            "\t\t\t\t\t\"left\": 2734\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"A25\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.046834,\n" +
            "\t\t\t\t\t\"average\": 0.626631,\n" +
            "\t\t\t\t\t\"min\": 0.402723\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 245,\n" +
            "\t\t\t\t\t\"top\": 340,\n" +
            "\t\t\t\t\t\"height\": 133,\n" +
            "\t\t\t\t\t\"left\": 147\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"购/名\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000014,\n" +
            "\t\t\t\t\t\"average\": 0.997604,\n" +
            "\t\t\t\t\t\"min\": 0.987138\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 677,\n" +
            "\t\t\t\t\t\"top\": 356,\n" +
            "\t\t\t\t\t\"height\": 61,\n" +
            "\t\t\t\t\t\"left\": 537\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"称:西安炳益智能电子科技有限公司\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000149,\n" +
            "\t\t\t\t\t\"average\": 0.993545,\n" +
            "\t\t\t\t\t\"min\": 0.952323\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 1000,\n" +
            "\t\t\t\t\t\"top\": 412,\n" +
            "\t\t\t\t\t\"height\": 72,\n" +
            "\t\t\t\t\t\"left\": 326\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"纳税人识别号:91610131MA6U106Q1M\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.011011,\n" +
            "\t\t\t\t\t\"average\": 0.978098,\n" +
            "\t\t\t\t\t\"min\": 0.432979\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 916,\n" +
            "\t\t\t\t\t\"top\": 362,\n" +
            "\t\t\t\t\t\"height\": 80,\n" +
            "\t\t\t\t\t\"left\": 1610\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"密13+>270<6/2>/933/>/24829297\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.008951,\n" +
            "\t\t\t\t\t\"average\": 0.970325,\n" +
            "\t\t\t\t\t\"min\": 0.521401\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 2508,\n" +
            "\t\t\t\t\t\"top\": 412,\n" +
            "\t\t\t\t\t\"height\": 220,\n" +
            "\t\t\t\t\t\"left\": 27\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"6//地址、电话:西安市高新区高新路6号1幢1单元12501宽13571882169//96>9/4/4+9128*-/9>719>7<547\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.039106,\n" +
            "\t\t\t\t\t\"average\": 0.802164,\n" +
            "\t\t\t\t\t\"min\": 0.604411\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 133,\n" +
            "\t\t\t\t\t\"top\": 451,\n" +
            "\t\t\t\t\t\"height\": 44,\n" +
            "\t\t\t\t\t\"left\": 158\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"q买\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.00316,\n" +
            "\t\t\t\t\t\"average\": 0.984612,\n" +
            "\t\t\t\t\t\"min\": 0.70764\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 836,\n" +
            "\t\t\t\t\t\"top\": 471,\n" +
            "\t\t\t\t\t\"height\": 61,\n" +
            "\t\t\t\t\t\"left\": 1688\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"2>5<85<00<8+9722/934*\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.005447,\n" +
            "\t\t\t\t\t\"average\": 0.980707,\n" +
            "\t\t\t\t\t\"min\": 0.581182\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 1184,\n" +
            "\t\t\t\t\t\"top\": 540,\n" +
            "\t\t\t\t\t\"height\": 64,\n" +
            "\t\t\t\t\t\"left\": 326\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"开户行及账号:中国民生银行股份有限公司西安高新开发区支行699227091\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000629,\n" +
            "\t\t\t\t\t\"average\": 0.990351,\n" +
            "\t\t\t\t\t\"min\": 0.891071\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 911,\n" +
            "\t\t\t\t\t\"top\": 518,\n" +
            "\t\t\t\t\t\"height\": 64,\n" +
            "\t\t\t\t\t\"left\": 1616\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"区835384/99/-28<929+--1763012\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.046395,\n" +
            "\t\t\t\t\t\"average\": 0.57738,\n" +
            "\t\t\t\t\t\"min\": 0.412131\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 108,\n" +
            "\t\t\t\t\t\"top\": 557,\n" +
            "\t\t\t\t\t\"height\": 55,\n" +
            "\t\t\t\t\t\"left\": 2742\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"UF思\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.02591,\n" +
            "\t\t\t\t\t\"average\": 0.949145,\n" +
            "\t\t\t\t\t\"min\": 0.415455\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 660,\n" +
            "\t\t\t\t\t\"top\": 590,\n" +
            "\t\t\t\t\t\"height\": 130,\n" +
            "\t\t\t\t\t\"left\": 136\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"s物或应税劳务、服务名称\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000001,\n" +
            "\t\t\t\t\t\"average\": 0.999466,\n" +
            "\t\t\t\t\t\"min\": 0.99631\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 579,\n" +
            "\t\t\t\t\t\"top\": 607,\n" +
            "\t\t\t\t\t\"height\": 52,\n" +
            "\t\t\t\t\t\"left\": 908\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"规格型号单位数量\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000097,\n" +
            "\t\t\t\t\t\"average\": 0.989687,\n" +
            "\t\t\t\t\t\"min\": 0.97982\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 122,\n" +
            "\t\t\t\t\t\"top\": 610,\n" +
            "\t\t\t\t\t\"height\": 50,\n" +
            "\t\t\t\t\t\"left\": 1608\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"单价\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999861,\n" +
            "\t\t\t\t\t\"min\": 0.999835\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 164,\n" +
            "\t\t\t\t\t\"top\": 610,\n" +
            "\t\t\t\t\t\"height\": 50,\n" +
            "\t\t\t\t\t\"left\": 1886\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"金额\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000048,\n" +
            "\t\t\t\t\t\"average\": 0.995652,\n" +
            "\t\t\t\t\t\"min\": 0.981865\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 521,\n" +
            "\t\t\t\t\t\"top\": 604,\n" +
            "\t\t\t\t\t\"height\": 55,\n" +
            "\t\t\t\t\t\"left\": 2165\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"税率税额第\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.777516,\n" +
            "\t\t\t\t\t\"min\": 0.777516\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 72,\n" +
            "\t\t\t\t\t\"top\": 629,\n" +
            "\t\t\t\t\t\"height\": 69,\n" +
            "\t\t\t\t\t\"left\": 2742\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"●\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000074,\n" +
            "\t\t\t\t\t\"average\": 0.996082,\n" +
            "\t\t\t\t\t\"min\": 0.973519\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 440,\n" +
            "\t\t\t\t\t\"top\": 671,\n" +
            "\t\t\t\t\t\"height\": 103,\n" +
            "\t\t\t\t\t\"left\": 158\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"电费(物业管理)\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.922961,\n" +
            "\t\t\t\t\t\"min\": 0.922961\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 52,\n" +
            "\t\t\t\t\t\"top\": 680,\n" +
            "\t\t\t\t\t\"height\": 50,\n" +
            "\t\t\t\t\t\"left\": 1212\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"元\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000018,\n" +
            "\t\t\t\t\t\"average\": 0.996773,\n" +
            "\t\t\t\t\t\"min\": 0.987314\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 239,\n" +
            "\t\t\t\t\t\"top\": 682,\n" +
            "\t\t\t\t\t\"height\": 47,\n" +
            "\t\t\t\t\t\"left\": 2020\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"871.7917%\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000008,\n" +
            "\t\t\t\t\t\"average\": 0.997346,\n" +
            "\t\t\t\t\t\"min\": 0.991508\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 170,\n" +
            "\t\t\t\t\t\"top\": 682,\n" +
            "\t\t\t\t\t\"height\": 52,\n" +
            "\t\t\t\t\t\"left\": 2486\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"148.21\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000013,\n" +
            "\t\t\t\t\t\"average\": 0.997236,\n" +
            "\t\t\t\t\t\"min\": 0.989183\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 440,\n" +
            "\t\t\t\t\t\"top\": 719,\n" +
            "\t\t\t\t\t\"height\": 91,\n" +
            "\t\t\t\t\t\"left\": 156\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"水费(物业管理)\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.888249,\n" +
            "\t\t\t\t\t\"min\": 0.888249\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 44,\n" +
            "\t\t\t\t\t\"top\": 730,\n" +
            "\t\t\t\t\t\"height\": 44,\n" +
            "\t\t\t\t\t\"left\": 1217\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"元\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.0002,\n" +
            "\t\t\t\t\t\"average\": 0.989564,\n" +
            "\t\t\t\t\t\"min\": 0.954827\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 248,\n" +
            "\t\t\t\t\t\"top\": 733,\n" +
            "\t\t\t\t\t\"height\": 41,\n" +
            "\t\t\t\t\t\"left\": 2017\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"108.4011%\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000003,\n" +
            "\t\t\t\t\t\"average\": 0.995403,\n" +
            "\t\t\t\t\t\"min\": 0.992642\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 94,\n" +
            "\t\t\t\t\t\"top\": 735,\n" +
            "\t\t\t\t\t\"height\": 39,\n" +
            "\t\t\t\t\t\"left\": 2513\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"11.92\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.00001,\n" +
            "\t\t\t\t\t\"average\": 0.997835,\n" +
            "\t\t\t\t\t\"min\": 0.992251\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 415,\n" +
            "\t\t\t\t\t\"top\": 763,\n" +
            "\t\t\t\t\t\"height\": 91,\n" +
            "\t\t\t\t\t\"left\": 150\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"物业管理服务费\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.926648,\n" +
            "\t\t\t\t\t\"min\": 0.926648\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 41,\n" +
            "\t\t\t\t\t\"top\": 780,\n" +
            "\t\t\t\t\t\"height\": 41,\n" +
            "\t\t\t\t\t\"left\": 1220\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"元\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000036,\n" +
            "\t\t\t\t\t\"average\": 0.995868,\n" +
            "\t\t\t\t\t\"min\": 0.982696\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 231,\n" +
            "\t\t\t\t\t\"top\": 777,\n" +
            "\t\t\t\t\t\"height\": 44,\n" +
            "\t\t\t\t\t\"left\": 2017\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"531.646%\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.006953,\n" +
            "\t\t\t\t\t\"average\": 0.963581,\n" +
            "\t\t\t\t\t\"min\": 0.759419\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 181,\n" +
            "\t\t\t\t\t\"top\": 774,\n" +
            "\t\t\t\t\t\"height\": 50,\n" +
            "\t\t\t\t\t\"left\": 2500\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"31.90|发\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999483,\n" +
            "\t\t\t\t\t\"min\": 0.999483\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 44,\n" +
            "\t\t\t\t\t\"top\": 858,\n" +
            "\t\t\t\t\t\"height\": 41,\n" +
            "\t\t\t\t\t\"left\": 2633\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"联\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.773134,\n" +
            "\t\t\t\t\t\"min\": 0.773134\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 47,\n" +
            "\t\t\t\t\t\"top\": 889,\n" +
            "\t\t\t\t\t\"height\": 44,\n" +
            "\t\t\t\t\t\"left\": 167\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"三\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999756,\n" +
            "\t\t\t\t\t\"min\": 0.999756\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 50,\n" +
            "\t\t\t\t\t\"top\": 919,\n" +
            "\t\t\t\t\t\"height\": 44,\n" +
            "\t\t\t\t\t\"left\": 2631\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"购\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.995916,\n" +
            "\t\t\t\t\t\"min\": 0.995916\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 41,\n" +
            "\t\t\t\t\t\"top\": 961,\n" +
            "\t\t\t\t\t\"height\": 39,\n" +
            "\t\t\t\t\t\"left\": 2636\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"买\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.961806,\n" +
            "\t\t\t\t\t\"min\": 0.961806\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 41,\n" +
            "\t\t\t\t\t\"top\": 1006,\n" +
            "\t\t\t\t\t\"height\": 39,\n" +
            "\t\t\t\t\t\"left\": 2636\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"万\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.99858,\n" +
            "\t\t\t\t\t\"min\": 0.99858\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 52,\n" +
            "\t\t\t\t\t\"top\": 1070,\n" +
            "\t\t\t\t\t\"height\": 47,\n" +
            "\t\t\t\t\t\"left\": 392\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"合\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.996566,\n" +
            "\t\t\t\t\t\"min\": 0.996566\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 47,\n" +
            "\t\t\t\t\t\"top\": 1073,\n" +
            "\t\t\t\t\t\"height\": 44,\n" +
            "\t\t\t\t\t\"left\": 629\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"计\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.002691,\n" +
            "\t\t\t\t\t\"average\": 0.971188,\n" +
            "\t\t\t\t\t\"min\": 0.842173\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 236,\n" +
            "\t\t\t\t\t\"top\": 1070,\n" +
            "\t\t\t\t\t\"height\": 61,\n" +
            "\t\t\t\t\t\"left\": 1900\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"￥1511.83\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.974167,\n" +
            "\t\t\t\t\t\"min\": 0.974167\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 47,\n" +
            "\t\t\t\t\t\"top\": 1045,\n" +
            "\t\t\t\t\t\"height\": 47,\n" +
            "\t\t\t\t\t\"left\": 2633\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"记\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.0003,\n" +
            "\t\t\t\t\t\"average\": 0.991775,\n" +
            "\t\t\t\t\t\"min\": 0.946317\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 292,\n" +
            "\t\t\t\t\t\"top\": 1070,\n" +
            "\t\t\t\t\t\"height\": 61,\n" +
            "\t\t\t\t\t\"left\": 2394\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"￥192.03账\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999914,\n" +
            "\t\t\t\t\t\"min\": 0.999914\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 44,\n" +
            "\t\t\t\t\t\"top\": 1125,\n" +
            "\t\t\t\t\t\"height\": 41,\n" +
            "\t\t\t\t\t\"left\": 2636\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"凭\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.092757,\n" +
            "\t\t\t\t\t\"average\": 0.680223,\n" +
            "\t\t\t\t\t\"min\": 0.375663\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 122,\n" +
            "\t\t\t\t\t\"top\": 1170,\n" +
            "\t\t\t\t\t\"height\": 44,\n" +
            "\t\t\t\t\t\"left\": 5\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"W2\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000758,\n" +
            "\t\t\t\t\t\"average\": 0.987451,\n" +
            "\t\t\t\t\t\"min\": 0.914857\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 370,\n" +
            "\t\t\t\t\t\"top\": 1148,\n" +
            "\t\t\t\t\t\"height\": 58,\n" +
            "\t\t\t\t\t\"left\": 353\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"价税合计(大写)\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000324,\n" +
            "\t\t\t\t\t\"average\": 0.991252,\n" +
            "\t\t\t\t\t\"min\": 0.935778\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 560,\n" +
            "\t\t\t\t\t\"top\": 1148,\n" +
            "\t\t\t\t\t\"height\": 64,\n" +
            "\t\t\t\t\t\"left\": 903\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"壹仟柒佰零叁圆捌角陆分\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.008856,\n" +
            "\t\t\t\t\t\"average\": 0.966619,\n" +
            "\t\t\t\t\t\"min\": 0.65613\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 468,\n" +
            "\t\t\t\t\t\"top\": 1151,\n" +
            "\t\t\t\t\t\"height\": 55,\n" +
            "\t\t\t\t\t\"left\": 1950\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"(小写)￥1703.86\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.02734,\n" +
            "\t\t\t\t\t\"average\": 0.757804,\n" +
            "\t\t\t\t\t\"min\": 0.592456\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 228,\n" +
            "\t\t\t\t\t\"top\": 1223,\n" +
            "\t\t\t\t\t\"height\": 108,\n" +
            "\t\t\t\t\t\"left\": 153\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"销名\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.001671,\n" +
            "\t\t\t\t\t\"average\": 0.982222,\n" +
            "\t\t\t\t\t\"min\": 0.830655\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 674,\n" +
            "\t\t\t\t\t\"top\": 1229,\n" +
            "\t\t\t\t\t\"height\": 61,\n" +
            "\t\t\t\t\t\"left\": 532\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"称:陕西诚悦物业管理有限贵任公司\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.996891,\n" +
            "\t\t\t\t\t\"min\": 0.996891\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 122,\n" +
            "\t\t\t\t\t\"top\": 1315,\n" +
            "\t\t\t\t\t\"height\": 47,\n" +
            "\t\t\t\t\t\"left\": 167\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"售\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000025,\n" +
            "\t\t\t\t\t\"average\": 0.996787,\n" +
            "\t\t\t\t\t\"min\": 0.985963\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 259,\n" +
            "\t\t\t\t\t\"top\": 1287,\n" +
            "\t\t\t\t\t\"height\": 47,\n" +
            "\t\t\t\t\t\"left\": 326\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"纳税人识别号\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.006718,\n" +
            "\t\t\t\t\t\"average\": 0.964941,\n" +
            "\t\t\t\t\t\"min\": 0.749273\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 314,\n" +
            "\t\t\t\t\t\"top\": 1237,\n" +
            "\t\t\t\t\t\"height\": 86,\n" +
            "\t\t\t\t\t\"left\": 1602\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"畚银座:7-8月\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999242,\n" +
            "\t\t\t\t\t\"min\": 0.99729\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 641,\n" +
            "\t\t\t\t\t\"top\": 1296,\n" +
            "\t\t\t\t\t\"height\": 58,\n" +
            "\t\t\t\t\t\"left\": 691\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"916100007412779719\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.978847,\n" +
            "\t\t\t\t\t\"min\": 0.978847\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 72,\n" +
            "\t\t\t\t\t\"top\": 1374,\n" +
            "\t\t\t\t\t\"height\": 69,\n" +
            "\t\t\t\t\t\"left\": 33\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"●\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.008775,\n" +
            "\t\t\t\t\t\"average\": 0.961591,\n" +
            "\t\t\t\t\t\"min\": 0.574436\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 1240,\n" +
            "\t\t\t\t\t\"top\": 1337,\n" +
            "\t\t\t\t\t\"height\": 66,\n" +
            "\t\t\t\t\t\"left\": 323\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"地址、电话:西安市高新区锦业一路19号旗远铺抛2号楼二层1020T029-89381952\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.001156,\n" +
            "\t\t\t\t\t\"average\": 0.990224,\n" +
            "\t\t\t\t\t\"min\": 0.837528\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 1443,\n" +
            "\t\t\t\t\t\"top\": 1357,\n" +
            "\t\t\t\t\t\"height\": 122,\n" +
            "\t\t\t\t\t\"left\": 239\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"方|开户行及账号:民生银行西安高新开发区支行1203014210003578/\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.012574,\n" +
            "\t\t\t\t\t\"average\": 0.587657,\n" +
            "\t\t\t\t\t\"min\": 0.475524\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 454,\n" +
            "\t\t\t\t\t\"top\": 1357,\n" +
            "\t\t\t\t\t\"height\": 133,\n" +
            "\t\t\t\t\t\"left\": 1998\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"0空\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000001,\n" +
            "\t\t\t\t\t\"average\": 0.998963,\n" +
            "\t\t\t\t\t\"min\": 0.997555\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 142,\n" +
            "\t\t\t\t\t\"top\": 1466,\n" +
            "\t\t\t\t\t\"height\": 52,\n" +
            "\t\t\t\t\t\"left\": 259\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"收款人\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000008,\n" +
            "\t\t\t\t\t\"average\": 0.997097,\n" +
            "\t\t\t\t\t\"min\": 0.994218\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 100,\n" +
            "\t\t\t\t\t\"top\": 1468,\n" +
            "\t\t\t\t\t\"height\": 50,\n" +
            "\t\t\t\t\t\"left\": 933\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"复核\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000004,\n" +
            "\t\t\t\t\t\"average\": 0.998378,\n" +
            "\t\t\t\t\t\"min\": 0.994628\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 340,\n" +
            "\t\t\t\t\t\"top\": 1466,\n" +
            "\t\t\t\t\t\"height\": 55,\n" +
            "\t\t\t\t\t\"left\": 1452\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"开票人:贾锦秀\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.032106,\n" +
            "\t\t\t\t\t\"average\": 0.887433,\n" +
            "\t\t\t\t\t\"min\": 0.577141\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 256,\n" +
            "\t\t\t\t\t\"top\": 1440,\n" +
            "\t\t\t\t\t\"height\": 114,\n" +
            "\t\t\t\t\t\"left\": 2095\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"票专厝章\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.022165,\n" +
            "\t\t\t\t\t\"average\": 0.889847,\n" +
            "\t\t\t\t\t\"min\": 0.663533\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 183,\n" +
            "\t\t\t\t\t\"top\": 1532,\n" +
            "\t\t\t\t\t\"height\": 58,\n" +
            "\t\t\t\t\t\"left\": 2140\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"012524\"\n" +
            "\t\t\t}\n" +
            "\t\t]\n" +
            "\t}\n" +
            "}";

    static final String baseResult1 ="{\n" +
            "\t\"errno\": 0,\n" +
            "\t\"msg\": \"success\",\n" +
            "\t\"data\": {\n" +
            "\t\t\"words_result_num\": 51,\n" +
            "\t\t\"words_result\": [\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.002298,\n" +
            "\t\t\t\t\t\"average\": 0.980135,\n" +
            "\t\t\t\t\t\"min\": 0.800803\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 531,\n" +
            "\t\t\t\t\t\"top\": 22,\n" +
            "\t\t\t\t\t\"height\": 60,\n" +
            "\t\t\t\t\t\"left\": 82\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"6100171320陕西增植花通发票№086814556100\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.99994,\n" +
            "\t\t\t\t\t\"min\": 0.999896\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 20,\n" +
            "\t\t\t\t\t\"top\": 60,\n" +
            "\t\t\t\t\t\"height\": 10,\n" +
            "\t\t\t\t\t\"left\": 111\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"编号\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.00002,\n" +
            "\t\t\t\t\t\"average\": 0.993374,\n" +
            "\t\t\t\t\t\"min\": 0.987796\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 35,\n" +
            "\t\t\t\t\t\"top\": 57,\n" +
            "\t\t\t\t\t\"height\": 15,\n" +
            "\t\t\t\t\t\"left\": 569\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"08681\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000003,\n" +
            "\t\t\t\t\t\"average\": 0.998698,\n" +
            "\t\t\t\t\t\"min\": 0.993702\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 82,\n" +
            "\t\t\t\t\t\"top\": 75,\n" +
            "\t\t\t\t\t\"height\": 15,\n" +
            "\t\t\t\t\t\"left\": 91\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"539905915167\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.032821,\n" +
            "\t\t\t\t\t\"average\": 0.817974,\n" +
            "\t\t\t\t\t\"min\": 0.636808\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 17,\n" +
            "\t\t\t\t\t\"top\": 70,\n" +
            "\t\t\t\t\t\"height\": 10,\n" +
            "\t\t\t\t\t\"left\": 334\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"务总\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.00107,\n" +
            "\t\t\t\t\t\"average\": 0.98833,\n" +
            "\t\t\t\t\t\"min\": 0.867083\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 122,\n" +
            "\t\t\t\t\t\"top\": 72,\n" +
            "\t\t\t\t\t\"height\": 17,\n" +
            "\t\t\t\t\t\"left\": 481\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"开票日期:2017年10月17\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999931,\n" +
            "\t\t\t\t\t\"min\": 0.999931\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 12,\n" +
            "\t\t\t\t\t\"top\": 99,\n" +
            "\t\t\t\t\t\"height\": 11,\n" +
            "\t\t\t\t\t\"left\": 72\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"名\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000133,\n" +
            "\t\t\t\t\t\"average\": 0.994395,\n" +
            "\t\t\t\t\t\"min\": 0.951336\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 157,\n" +
            "\t\t\t\t\t\"top\": 92,\n" +
            "\t\t\t\t\t\"height\": 19,\n" +
            "\t\t\t\t\t\"left\": 124\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"称:中国农业科学院农产品加工研究所\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.008755,\n" +
            "\t\t\t\t\t\"average\": 0.957024,\n" +
            "\t\t\t\t\t\"min\": 0.514549\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 223,\n" +
            "\t\t\t\t\t\"top\": 95,\n" +
            "\t\t\t\t\t\"height\": 20,\n" +
            "\t\t\t\t\t\"left\": 383\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"03<9<*2+3<26<+>027+6-250+31\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.865646,\n" +
            "\t\t\t\t\t\"min\": 0.865646\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 24,\n" +
            "\t\t\t\t\t\"top\": 110,\n" +
            "\t\t\t\t\t\"height\": 23,\n" +
            "\t\t\t\t\t\"left\": 28\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"产\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999589,\n" +
            "\t\t\t\t\t\"min\": 0.999589\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 11,\n" +
            "\t\t\t\t\t\"top\": 106,\n" +
            "\t\t\t\t\t\"height\": 11,\n" +
            "\t\t\t\t\t\"left\": 54\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"购\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000794,\n" +
            "\t\t\t\t\t\"average\": 0.975439,\n" +
            "\t\t\t\t\t\"min\": 0.915983\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 64,\n" +
            "\t\t\t\t\t\"top\": 114,\n" +
            "\t\t\t\t\t\"height\": 13,\n" +
            "\t\t\t\t\t\"left\": 73\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"纳税人识别号\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000406,\n" +
            "\t\t\t\t\t\"average\": 0.988456,\n" +
            "\t\t\t\t\t\"min\": 0.920435\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 206,\n" +
            "\t\t\t\t\t\"top\": 112,\n" +
            "\t\t\t\t\t\"height\": 16,\n" +
            "\t\t\t\t\t\"left\": 399\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"351-8*397-><71<89-/361--396\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.049045,\n" +
            "\t\t\t\t\t\"average\": 0.888416,\n" +
            "\t\t\t\t\t\"min\": 0.445499\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 63,\n" +
            "\t\t\t\t\t\"top\": 129,\n" +
            "\t\t\t\t\t\"height\": 11,\n" +
            "\t\t\t\t\t\"left\": 73\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"地址,电话\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.007622,\n" +
            "\t\t\t\t\t\"average\": 0.969277,\n" +
            "\t\t\t\t\t\"min\": 0.580035\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 205,\n" +
            "\t\t\t\t\t\"top\": 125,\n" +
            "\t\t\t\t\t\"height\": 17,\n" +
            "\t\t\t\t\t\"left\": 397\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"40<9<*2+3<26<+>027730*8*38*\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.990969,\n" +
            "\t\t\t\t\t\"min\": 0.990969\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 12,\n" +
            "\t\t\t\t\t\"top\": 139,\n" +
            "\t\t\t\t\t\"height\": 12,\n" +
            "\t\t\t\t\t\"left\": 53\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"万\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000012,\n" +
            "\t\t\t\t\t\"average\": 0.996116,\n" +
            "\t\t\t\t\t\"min\": 0.991273\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 63,\n" +
            "\t\t\t\t\t\"top\": 145,\n" +
            "\t\t\t\t\t\"height\": 11,\n" +
            "\t\t\t\t\t\"left\": 73\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"开户行及账号\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.001043,\n" +
            "\t\t\t\t\t\"average\": 0.988694,\n" +
            "\t\t\t\t\t\"min\": 0.860458\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 209,\n" +
            "\t\t\t\t\t\"top\": 140,\n" +
            "\t\t\t\t\t\"height\": 15,\n" +
            "\t\t\t\t\t\"left\": 388\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"区4>+163>248013-><06932<6/83\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.021094,\n" +
            "\t\t\t\t\t\"average\": 0.941867,\n" +
            "\t\t\t\t\t\"min\": 0.531847\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 90,\n" +
            "\t\t\t\t\t\"top\": 161,\n" +
            "\t\t\t\t\t\"height\": 12,\n" +
            "\t\t\t\t\t\"left\": 95\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"应税劳务,服务名称\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000167,\n" +
            "\t\t\t\t\t\"average\": 0.993118,\n" +
            "\t\t\t\t\t\"min\": 0.964304\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 90,\n" +
            "\t\t\t\t\t\"top\": 161,\n" +
            "\t\t\t\t\t\"height\": 12,\n" +
            "\t\t\t\t\t\"left\": 214\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"规格型号单位\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000877,\n" +
            "\t\t\t\t\t\"average\": 0.969269,\n" +
            "\t\t\t\t\t\"min\": 0.939653\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 30,\n" +
            "\t\t\t\t\t\"top\": 161,\n" +
            "\t\t\t\t\t\"height\": 11,\n" +
            "\t\t\t\t\t\"left\": 386\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"单份\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000006,\n" +
            "\t\t\t\t\t\"average\": 0.997539,\n" +
            "\t\t\t\t\t\"min\": 0.995141\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 41,\n" +
            "\t\t\t\t\t\"top\": 161,\n" +
            "\t\t\t\t\t\"height\": 11,\n" +
            "\t\t\t\t\t\"left\": 454\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"金额\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.994571,\n" +
            "\t\t\t\t\t\"min\": 0.9941\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 24,\n" +
            "\t\t\t\t\t\"top\": 161,\n" +
            "\t\t\t\t\t\"height\": 11,\n" +
            "\t\t\t\t\t\"left\": 523\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"税率\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000001,\n" +
            "\t\t\t\t\t\"average\": 0.996698,\n" +
            "\t\t\t\t\t\"min\": 0.995663\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 42,\n" +
            "\t\t\t\t\t\"top\": 161,\n" +
            "\t\t\t\t\t\"height\": 12,\n" +
            "\t\t\t\t\t\"left\": 573\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"税额\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000024,\n" +
            "\t\t\t\t\t\"average\": 0.997271,\n" +
            "\t\t\t\t\t\"min\": 0.987444\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 64,\n" +
            "\t\t\t\t\t\"top\": 168,\n" +
            "\t\t\t\t\t\"height\": 16,\n" +
            "\t\t\t\t\t\"left\": 33\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"打印复印费\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000646,\n" +
            "\t\t\t\t\t\"average\": 0.982754,\n" +
            "\t\t\t\t\t\"min\": 0.92321\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 38,\n" +
            "\t\t\t\t\t\"top\": 172,\n" +
            "\t\t\t\t\t\"height\": 10,\n" +
            "\t\t\t\t\t\"left\": 368\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"25.2427\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.009249,\n" +
            "\t\t\t\t\t\"average\": 0.927607,\n" +
            "\t\t\t\t\t\"min\": 0.791605\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 16,\n" +
            "\t\t\t\t\t\"top\": 170,\n" +
            "\t\t\t\t\t\"height\": 15,\n" +
            "\t\t\t\t\t\"left\": 407\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"844\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.00058,\n" +
            "\t\t\t\t\t\"average\": 0.990079,\n" +
            "\t\t\t\t\t\"min\": 0.922073\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 71,\n" +
            "\t\t\t\t\t\"top\": 171,\n" +
            "\t\t\t\t\t\"height\": 15,\n" +
            "\t\t\t\t\t\"left\": 472\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"5825.243%\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.952119,\n" +
            "\t\t\t\t\t\"min\": 0.952119\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 10,\n" +
            "\t\t\t\t\t\"top\": 175,\n" +
            "\t\t\t\t\t\"height\": 10,\n" +
            "\t\t\t\t\t\"left\": 593\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"1\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999669,\n" +
            "\t\t\t\t\t\"min\": 0.999669\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 12,\n" +
            "\t\t\t\t\t\"top\": 201,\n" +
            "\t\t\t\t\t\"height\": 10,\n" +
            "\t\t\t\t\t\"left\": 640\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"记\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.844633,\n" +
            "\t\t\t\t\t\"min\": 0.844633\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 12,\n" +
            "\t\t\t\t\t\"top\": 211,\n" +
            "\t\t\t\t\t\"height\": 10,\n" +
            "\t\t\t\t\t\"left\": 640\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"账\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.9999,\n" +
            "\t\t\t\t\t\"min\": 0.9999\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 13,\n" +
            "\t\t\t\t\t\"top\": 221,\n" +
            "\t\t\t\t\t\"height\": 11,\n" +
            "\t\t\t\t\t\"left\": 639\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"联\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.981045,\n" +
            "\t\t\t\t\t\"min\": 0.981045\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 13,\n" +
            "\t\t\t\t\t\"top\": 230,\n" +
            "\t\t\t\t\t\"height\": 11,\n" +
            "\t\t\t\t\t\"left\": 33\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"主\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.754977,\n" +
            "\t\t\t\t\t\"min\": 0.754977\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 13,\n" +
            "\t\t\t\t\t\"top\": 253,\n" +
            "\t\t\t\t\t\"height\": 12,\n" +
            "\t\t\t\t\t\"left\": 34\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"g\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999318,\n" +
            "\t\t\t\t\t\"min\": 0.999318\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 13,\n" +
            "\t\t\t\t\t\"top\": 275,\n" +
            "\t\t\t\t\t\"height\": 13,\n" +
            "\t\t\t\t\t\"left\": 145\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"计\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.004981,\n" +
            "\t\t\t\t\t\"average\": 0.966759,\n" +
            "\t\t\t\t\t\"min\": 0.782598\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 59,\n" +
            "\t\t\t\t\t\"top\": 273,\n" +
            "\t\t\t\t\t\"height\": 14,\n" +
            "\t\t\t\t\t\"left\": 445\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"￥5825,24\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.00828,\n" +
            "\t\t\t\t\t\"average\": 0.939886,\n" +
            "\t\t\t\t\t\"min\": 0.782886\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 33,\n" +
            "\t\t\t\t\t\"top\": 274,\n" +
            "\t\t\t\t\t\"height\": 14,\n" +
            "\t\t\t\t\t\"left\": 568\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"￥174\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000001,\n" +
            "\t\t\t\t\t\"average\": 0.998714,\n" +
            "\t\t\t\t\t\"min\": 0.997443\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 89,\n" +
            "\t\t\t\t\t\"top\": 294,\n" +
            "\t\t\t\t\t\"height\": 14,\n" +
            "\t\t\t\t\t\"left\": 80\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"价税合计(大写)\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.052262,\n" +
            "\t\t\t\t\t\"average\": 0.800421,\n" +
            "\t\t\t\t\t\"min\": 0.366103\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 56,\n" +
            "\t\t\t\t\t\"top\": 288,\n" +
            "\t\t\t\t\t\"height\": 18,\n" +
            "\t\t\t\t\t\"left\": 208\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"0陪仟圆整\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.006743,\n" +
            "\t\t\t\t\t\"average\": 0.952184,\n" +
            "\t\t\t\t\t\"min\": 0.712364\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 83,\n" +
            "\t\t\t\t\t\"top\": 291,\n" +
            "\t\t\t\t\t\"height\": 19,\n" +
            "\t\t\t\t\t\"left\": 478\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"小写半6000.00\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000228,\n" +
            "\t\t\t\t\t\"average\": 0.987246,\n" +
            "\t\t\t\t\t\"min\": 0.951006\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 98,\n" +
            "\t\t\t\t\t\"top\": 309,\n" +
            "\t\t\t\t\t\"height\": 18,\n" +
            "\t\t\t\t\t\"left\": 123\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"称西安诺利奇商贸有限\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000698,\n" +
            "\t\t\t\t\t\"average\": 0.991308,\n" +
            "\t\t\t\t\t\"min\": 0.866243\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 160,\n" +
            "\t\t\t\t\t\"top\": 312,\n" +
            "\t\t\t\t\t\"height\": 17,\n" +
            "\t\t\t\t\t\"left\": 394\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"校验码0300195140/7273674357\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.001046,\n" +
            "\t\t\t\t\t\"average\": 0.98563,\n" +
            "\t\t\t\t\t\"min\": 0.857265\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 264,\n" +
            "\t\t\t\t\t\"top\": 324,\n" +
            "\t\t\t\t\t\"height\": 44,\n" +
            "\t\t\t\t\t\"left\": 26\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"纳税人识别号:91610133MA6U109914\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.009946,\n" +
            "\t\t\t\t\t\"average\": 0.963681,\n" +
            "\t\t\t\t\t\"min\": 0.481821\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 262,\n" +
            "\t\t\t\t\t\"top\": 340,\n" +
            "\t\t\t\t\t\"height\": 18,\n" +
            "\t\t\t\t\t\"left\": 72\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"地址、电话西安市莲湖区北稍门213楼1单元5号029-8627303\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.002164,\n" +
            "\t\t\t\t\t\"average\": 0.986297,\n" +
            "\t\t\t\t\t\"min\": 0.761051\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 305,\n" +
            "\t\t\t\t\t\"top\": 353,\n" +
            "\t\t\t\t\t\"height\": 17,\n" +
            "\t\t\t\t\t\"left\": 51\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"方|开户行及账号中国民生银行股份有限公司西安西大街支行151813135\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000006,\n" +
            "\t\t\t\t\t\"average\": 0.995239,\n" +
            "\t\t\t\t\t\"min\": 0.99344\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 63,\n" +
            "\t\t\t\t\t\"top\": 341,\n" +
            "\t\t\t\t\t\"height\": 28,\n" +
            "\t\t\t\t\t\"left\": 521\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"专用章\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999494,\n" +
            "\t\t\t\t\t\"min\": 0.998289\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 77,\n" +
            "\t\t\t\t\t\"top\": 369,\n" +
            "\t\t\t\t\t\"height\": 18,\n" +
            "\t\t\t\t\t\"left\": 57\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"收款人:管理员\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000055,\n" +
            "\t\t\t\t\t\"average\": 0.993962,\n" +
            "\t\t\t\t\t\"min\": 0.978577\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 68,\n" +
            "\t\t\t\t\t\"top\": 371,\n" +
            "\t\t\t\t\t\"height\": 18,\n" +
            "\t\t\t\t\t\"left\": 219\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"复核:管理员\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000001,\n" +
            "\t\t\t\t\t\"average\": 0.999305,\n" +
            "\t\t\t\t\t\"min\": 0.997717\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 73,\n" +
            "\t\t\t\t\t\"top\": 374,\n" +
            "\t\t\t\t\t\"height\": 12,\n" +
            "\t\t\t\t\t\"left\": 348\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"开票人管理员\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.92303,\n" +
            "\t\t\t\t\t\"min\": 0.92303\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 13,\n" +
            "\t\t\t\t\t\"top\": 374,\n" +
            "\t\t\t\t\t\"height\": 12,\n" +
            "\t\t\t\t\t\"left\": 487\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"销\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.015856,\n" +
            "\t\t\t\t\t\"average\": 0.939431,\n" +
            "\t\t\t\t\t\"min\": 0.559031\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 301,\n" +
            "\t\t\t\t\t\"top\": 438,\n" +
            "\t\t\t\t\t\"height\": 22,\n" +
            "\t\t\t\t\t\"left\": 9\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"田放大e缩小(左旋转右旋转\"\n" +
            "\t\t\t}\n" +
            "\t\t]\n" +
            "\t}\n" +
            "}";

    static final String baseResult2="{\n" +
            "\t\"errno\": 0,\n" +
            "\t\"msg\": \"success\",\n" +
            "\t\"data\": {\n" +
            "\t\t\"words_result_num\": 59,\n" +
            "\t\t\"words_result\": [\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.018359,\n" +
            "\t\t\t\t\t\"average\": 0.959419,\n" +
            "\t\t\t\t\t\"min\": 0.396824\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 613,\n" +
            "\t\t\t\t\t\"top\": 16,\n" +
            "\t\t\t\t\t\"height\": 38,\n" +
            "\t\t\t\t\t\"left\": 74\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"6100173130陕西发票№。。420948610730\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000002,\n" +
            "\t\t\t\t\t\"average\": 0.998468,\n" +
            "\t\t\t\t\t\"min\": 0.995522\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 61,\n" +
            "\t\t\t\t\t\"top\": 49,\n" +
            "\t\t\t\t\t\"height\": 16,\n" +
            "\t\t\t\t\t\"left\": 620\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"00420948\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.908392,\n" +
            "\t\t\t\t\t\"min\": 0.908392\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 14,\n" +
            "\t\t\t\t\t\"top\": 74,\n" +
            "\t\t\t\t\t\"height\": 13,\n" +
            "\t\t\t\t\t\"left\": 77\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"回\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.004077,\n" +
            "\t\t\t\t\t\"average\": 0.975505,\n" +
            "\t\t\t\t\t\"min\": 0.735459\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 147,\n" +
            "\t\t\t\t\t\"top\": 68,\n" +
            "\t\t\t\t\t\"height\": 15,\n" +
            "\t\t\t\t\t\"left\": 524\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"开票日期:2017年11月08日\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999982,\n" +
            "\t\t\t\t\t\"min\": 0.999982\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 12,\n" +
            "\t\t\t\t\t\"top\": 95,\n" +
            "\t\t\t\t\t\"height\": 11,\n" +
            "\t\t\t\t\t\"left\": 95\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"名\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999933,\n" +
            "\t\t\t\t\t\"min\": 0.999933\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 13,\n" +
            "\t\t\t\t\t\"top\": 102,\n" +
            "\t\t\t\t\t\"height\": 12,\n" +
            "\t\t\t\t\t\"left\": 74\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"购\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000091,\n" +
            "\t\t\t\t\t\"average\": 0.995405,\n" +
            "\t\t\t\t\t\"min\": 0.965329\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 150,\n" +
            "\t\t\t\t\t\"top\": 94,\n" +
            "\t\t\t\t\t\"height\": 15,\n" +
            "\t\t\t\t\t\"left\": 149\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"称:陕西谢谢文化传播有限公司\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000113,\n" +
            "\t\t\t\t\t\"average\": 0.995817,\n" +
            "\t\t\t\t\t\"min\": 0.943671\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 207,\n" +
            "\t\t\t\t\t\"top\": 96,\n" +
            "\t\t\t\t\t\"height\": 18,\n" +
            "\t\t\t\t\t\"left\": 453\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"05<930336568*>5<371-675546\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.002563,\n" +
            "\t\t\t\t\t\"average\": 0.97056,\n" +
            "\t\t\t\t\t\"min\": 0.795052\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 253,\n" +
            "\t\t\t\t\t\"top\": 110,\n" +
            "\t\t\t\t\t\"height\": 15,\n" +
            "\t\t\t\t\t\"left\": 95\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"纳税人识别号:91610113MA6U72EC61\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000092,\n" +
            "\t\t\t\t\t\"average\": 0.996295,\n" +
            "\t\t\t\t\t\"min\": 0.955066\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 160,\n" +
            "\t\t\t\t\t\"top\": 108,\n" +
            "\t\t\t\t\t\"height\": 18,\n" +
            "\t\t\t\t\t\"left\": 445\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"+14<3<1694*/1048*384\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.001133,\n" +
            "\t\t\t\t\t\"average\": 0.947126,\n" +
            "\t\t\t\t\t\"min\": 0.913459\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 13,\n" +
            "\t\t\t\t\t\"top\": 111,\n" +
            "\t\t\t\t\t\"height\": 13,\n" +
            "\t\t\t\t\t\"left\": 647\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \")/\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.998805,\n" +
            "\t\t\t\t\t\"min\": 0.998805\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 11,\n" +
            "\t\t\t\t\t\"top\": 120,\n" +
            "\t\t\t\t\t\"height\": 11,\n" +
            "\t\t\t\t\t\"left\": 74\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"买\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.043934,\n" +
            "\t\t\t\t\t\"average\": 0.909119,\n" +
            "\t\t\t\t\t\"min\": 0.355406\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 113,\n" +
            "\t\t\t\t\t\"top\": 120,\n" +
            "\t\t\t\t\t\"height\": 22,\n" +
            "\t\t\t\t\t\"left\": 94\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"地址、电话:的器\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.032832,\n" +
            "\t\t\t\t\t\"average\": 0.837438,\n" +
            "\t\t\t\t\t\"min\": 0.406809\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 178,\n" +
            "\t\t\t\t\t\"top\": 124,\n" +
            "\t\t\t\t\t\"height\": 10,\n" +
            "\t\t\t\t\t\"left\": 211\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"市塔区米南路华万象62单元21层21103号房\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000208,\n" +
            "\t\t\t\t\t\"average\": 0.993375,\n" +
            "\t\t\t\t\t\"min\": 0.93519\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 222,\n" +
            "\t\t\t\t\t\"top\": 119,\n" +
            "\t\t\t\t\t\"height\": 19,\n" +
            "\t\t\t\t\t\"left\": 423\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"码37/98611/+86**6-88>2/1653\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.057075,\n" +
            "\t\t\t\t\t\"average\": 0.708669,\n" +
            "\t\t\t\t\t\"min\": 0.469765\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 15,\n" +
            "\t\t\t\t\t\"top\": 141,\n" +
            "\t\t\t\t\t\"height\": 15,\n" +
            "\t\t\t\t\t\"left\": 51\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"(G\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.992269,\n" +
            "\t\t\t\t\t\"min\": 0.992269\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 12,\n" +
            "\t\t\t\t\t\"top\": 137,\n" +
            "\t\t\t\t\t\"height\": 12,\n" +
            "\t\t\t\t\t\"left\": 74\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"万\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.002019,\n" +
            "\t\t\t\t\t\"average\": 0.985646,\n" +
            "\t\t\t\t\t\"min\": 0.787427\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 290,\n" +
            "\t\t\t\t\t\"top\": 141,\n" +
            "\t\t\t\t\t\"height\": 16,\n" +
            "\t\t\t\t\t\"left\": 95\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"开户行及账号:招商银行西安白沙路支行129908476910303\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000661,\n" +
            "\t\t\t\t\t\"average\": 0.989133,\n" +
            "\t\t\t\t\t\"min\": 0.906671\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 200,\n" +
            "\t\t\t\t\t\"top\": 134,\n" +
            "\t\t\t\t\t\"height\": 15,\n" +
            "\t\t\t\t\t\"left\": 445\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"5742936<>770-3456<4-*7<26\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.001149,\n" +
            "\t\t\t\t\t\"average\": 0.985728,\n" +
            "\t\t\t\t\t\"min\": 0.877525\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 126,\n" +
            "\t\t\t\t\t\"top\": 160,\n" +
            "\t\t\t\t\t\"height\": 15,\n" +
            "\t\t\t\t\t\"left\": 86\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"货物或应税劳务、服务名称\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.001147,\n" +
            "\t\t\t\t\t\"average\": 0.985172,\n" +
            "\t\t\t\t\t\"min\": 0.902337\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 131,\n" +
            "\t\t\t\t\t\"top\": 160,\n" +
            "\t\t\t\t\t\"height\": 14,\n" +
            "\t\t\t\t\t\"left\": 243\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"规格型号单位数\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.991589,\n" +
            "\t\t\t\t\t\"min\": 0.991589\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 12,\n" +
            "\t\t\t\t\t\"top\": 162,\n" +
            "\t\t\t\t\t\"height\": 12,\n" +
            "\t\t\t\t\t\"left\": 525\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"额\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999913,\n" +
            "\t\t\t\t\t\"min\": 0.999892\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 25,\n" +
            "\t\t\t\t\t\"top\": 161,\n" +
            "\t\t\t\t\t\"height\": 13,\n" +
            "\t\t\t\t\t\"left\": 567\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"税率\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.99978,\n" +
            "\t\t\t\t\t\"min\": 0.999754\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 42,\n" +
            "\t\t\t\t\t\"top\": 162,\n" +
            "\t\t\t\t\t\"height\": 12,\n" +
            "\t\t\t\t\t\"left\": 620\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"税额\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999802,\n" +
            "\t\t\t\t\t\"min\": 0.999802\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 14,\n" +
            "\t\t\t\t\t\"top\": 159,\n" +
            "\t\t\t\t\t\"height\": 13,\n" +
            "\t\t\t\t\t\"left\": 689\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"第\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000006,\n" +
            "\t\t\t\t\t\"average\": 0.997687,\n" +
            "\t\t\t\t\t\"min\": 0.994368\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 32,\n" +
            "\t\t\t\t\t\"top\": 175,\n" +
            "\t\t\t\t\t\"height\": 10,\n" +
            "\t\t\t\t\t\"left\": 79\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"金税盘\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999088,\n" +
            "\t\t\t\t\t\"min\": 0.999088\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 12,\n" +
            "\t\t\t\t\t\"top\": 176,\n" +
            "\t\t\t\t\t\"height\": 11,\n" +
            "\t\t\t\t\t\"left\": 321\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"块\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.015464,\n" +
            "\t\t\t\t\t\"average\": 0.921151,\n" +
            "\t\t\t\t\t\"min\": 0.606012\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 54,\n" +
            "\t\t\t\t\t\"top\": 175,\n" +
            "\t\t\t\t\t\"height\": 13,\n" +
            "\t\t\t\t\t\"left\": 395\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"1170.9401\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000509,\n" +
            "\t\t\t\t\t\"average\": 0.983894,\n" +
            "\t\t\t\t\t\"min\": 0.928551\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 63,\n" +
            "\t\t\t\t\t\"top\": 173,\n" +
            "\t\t\t\t\t\"height\": 17,\n" +
            "\t\t\t\t\t\"left\": 529\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"70.9417%\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.006764,\n" +
            "\t\t\t\t\t\"average\": 0.943606,\n" +
            "\t\t\t\t\t\"min\": 0.7954\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 50,\n" +
            "\t\t\t\t\t\"top\": 174,\n" +
            "\t\t\t\t\t\"height\": 21,\n" +
            "\t\t\t\t\t\"left\": 653\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"29.066联\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.92029,\n" +
            "\t\t\t\t\t\"min\": 0.92029\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 13,\n" +
            "\t\t\t\t\t\"top\": 213,\n" +
            "\t\t\t\t\t\"height\": 13,\n" +
            "\t\t\t\t\t\"left\": 689\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"票\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999708,\n" +
            "\t\t\t\t\t\"min\": 0.999708\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 14,\n" +
            "\t\t\t\t\t\"top\": 223,\n" +
            "\t\t\t\t\t\"height\": 14,\n" +
            "\t\t\t\t\t\"left\": 688\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"联\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999944,\n" +
            "\t\t\t\t\t\"min\": 0.999944\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 14,\n" +
            "\t\t\t\t\t\"top\": 239,\n" +
            "\t\t\t\t\t\"height\": 13,\n" +
            "\t\t\t\t\t\"left\": 688\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"购\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999548,\n" +
            "\t\t\t\t\t\"min\": 0.999548\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 12,\n" +
            "\t\t\t\t\t\"top\": 251,\n" +
            "\t\t\t\t\t\"height\": 12,\n" +
            "\t\t\t\t\t\"left\": 689\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"买\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.742851,\n" +
            "\t\t\t\t\t\"min\": 0.742851\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 10,\n" +
            "\t\t\t\t\t\"top\": 264,\n" +
            "\t\t\t\t\t\"height\": 10,\n" +
            "\t\t\t\t\t\"left\": 689\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"万\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.994519,\n" +
            "\t\t\t\t\t\"min\": 0.994519\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 13,\n" +
            "\t\t\t\t\t\"top\": 273,\n" +
            "\t\t\t\t\t\"height\": 11,\n" +
            "\t\t\t\t\t\"left\": 689\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"记\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.966753,\n" +
            "\t\t\t\t\t\"min\": 0.966753\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 13,\n" +
            "\t\t\t\t\t\"top\": 280,\n" +
            "\t\t\t\t\t\"height\": 12,\n" +
            "\t\t\t\t\t\"left\": 112\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"合\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999689,\n" +
            "\t\t\t\t\t\"min\": 0.999689\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 13,\n" +
            "\t\t\t\t\t\"top\": 281,\n" +
            "\t\t\t\t\t\"height\": 12,\n" +
            "\t\t\t\t\t\"left\": 171\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"计\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.001457,\n" +
            "\t\t\t\t\t\"average\": 0.975147,\n" +
            "\t\t\t\t\t\"min\": 0.903168\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 58,\n" +
            "\t\t\t\t\t\"top\": 279,\n" +
            "\t\t\t\t\t\"height\": 14,\n" +
            "\t\t\t\t\t\"left\": 505\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"￥170.94\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.03344,\n" +
            "\t\t\t\t\t\"average\": 0.88784,\n" +
            "\t\t\t\t\t\"min\": 0.497793\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 50,\n" +
            "\t\t\t\t\t\"top\": 277,\n" +
            "\t\t\t\t\t\"height\": 16,\n" +
            "\t\t\t\t\t\"left\": 633\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"￥29.06\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.999637,\n" +
            "\t\t\t\t\t\"min\": 0.999637\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 13,\n" +
            "\t\t\t\t\t\"top\": 283,\n" +
            "\t\t\t\t\t\"height\": 12,\n" +
            "\t\t\t\t\t\"left\": 689\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"账\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.024407,\n" +
            "\t\t\t\t\t\"average\": 0.818217,\n" +
            "\t\t\t\t\t\"min\": 0.661989\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 63,\n" +
            "\t\t\t\t\t\"top\": 304,\n" +
            "\t\t\t\t\t\"height\": 15,\n" +
            "\t\t\t\t\t\"left\": 3\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"F动\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.013202,\n" +
            "\t\t\t\t\t\"average\": 0.947518,\n" +
            "\t\t\t\t\t\"min\": 0.667163\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 83,\n" +
            "\t\t\t\t\t\"top\": 300,\n" +
            "\t\t\t\t\t\"height\": 13,\n" +
            "\t\t\t\t\t\"left\": 102\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"价税合计(大写\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.014427,\n" +
            "\t\t\t\t\t\"average\": 0.905914,\n" +
            "\t\t\t\t\t\"min\": 0.677634\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 64,\n" +
            "\t\t\t\t\t\"top\": 297,\n" +
            "\t\t\t\t\t\"height\": 15,\n" +
            "\t\t\t\t\t\"left\": 241\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"⑧贰佰圆整\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.01843,\n" +
            "\t\t\t\t\t\"average\": 0.955133,\n" +
            "\t\t\t\t\t\"min\": 0.525877\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 114,\n" +
            "\t\t\t\t\t\"top\": 298,\n" +
            "\t\t\t\t\t\"height\": 16,\n" +
            "\t\t\t\t\t\"left\": 511\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"(小写)￥200.00\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.959523,\n" +
            "\t\t\t\t\t\"min\": 0.959523\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 14,\n" +
            "\t\t\t\t\t\"top\": 293,\n" +
            "\t\t\t\t\t\"height\": 13,\n" +
            "\t\t\t\t\t\"left\": 688\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"凭\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.057588,\n" +
            "\t\t\t\t\t\"average\": 0.734426,\n" +
            "\t\t\t\t\t\"min\": 0.441773\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 58,\n" +
            "\t\t\t\t\t\"top\": 320,\n" +
            "\t\t\t\t\t\"height\": 23,\n" +
            "\t\t\t\t\t\"left\": 50\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"山销/名\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000029,\n" +
            "\t\t\t\t\t\"average\": 0.996495,\n" +
            "\t\t\t\t\t\"min\": 0.97973\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 131,\n" +
            "\t\t\t\t\t\"top\": 321,\n" +
            "\t\t\t\t\t\"height\": 15,\n" +
            "\t\t\t\t\t\"left\": 147\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"称:陕西航天信息有限公司\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.016949,\n" +
            "\t\t\t\t\t\"average\": 0.905464,\n" +
            "\t\t\t\t\t\"min\": 0.721372\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 27,\n" +
            "\t\t\t\t\t\"top\": 321,\n" +
            "\t\t\t\t\t\"height\": 16,\n" +
            "\t\t\t\t\t\"left\": 444\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"X雁塔\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0,\n" +
            "\t\t\t\t\t\"average\": 0.988259,\n" +
            "\t\t\t\t\t\"min\": 0.988259\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 12,\n" +
            "\t\t\t\t\t\"top\": 345,\n" +
            "\t\t\t\t\t\"height\": 12,\n" +
            "\t\t\t\t\t\"left\": 73\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"售\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000136,\n" +
            "\t\t\t\t\t\"average\": 0.995608,\n" +
            "\t\t\t\t\t\"min\": 0.944351\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 258,\n" +
            "\t\t\t\t\t\"top\": 334,\n" +
            "\t\t\t\t\t\"height\": 17,\n" +
            "\t\t\t\t\t\"left\": 94\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"纳税人识别号:91610131783582683H\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000001,\n" +
            "\t\t\t\t\t\"average\": 0.999171,\n" +
            "\t\t\t\t\t\"min\": 0.99836\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 33,\n" +
            "\t\t\t\t\t\"top\": 331,\n" +
            "\t\t\t\t\t\"height\": 20,\n" +
            "\t\t\t\t\t\"left\": 523\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"天信\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.016181,\n" +
            "\t\t\t\t\t\"average\": 0.950824,\n" +
            "\t\t\t\t\t\"min\": 0.404743\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 314,\n" +
            "\t\t\t\t\t\"top\": 351,\n" +
            "\t\t\t\t\t\"height\": 15,\n" +
            "\t\t\t\t\t\"left\": 93\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"地址、电话:西安市高新区高新路61号国税大厦屈楼202富029-62623805\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000138,\n" +
            "\t\t\t\t\t\"average\": 0.992326,\n" +
            "\t\t\t\t\t\"min\": 0.945562\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 321,\n" +
            "\t\t\t\t\t\"top\": 360,\n" +
            "\t\t\t\t\t\"height\": 23,\n" +
            "\t\t\t\t\t\"left\": 72\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"方|开户行及账号:工行西安高新路支行3700028719200070864\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.052741,\n" +
            "\t\t\t\t\t\"average\": 0.700233,\n" +
            "\t\t\t\t\t\"min\": 0.369714\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 74,\n" +
            "\t\t\t\t\t\"top\": 363,\n" +
            "\t\t\t\t\t\"height\": 19,\n" +
            "\t\t\t\t\t\"left\": 515\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"16037263\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.000003,\n" +
            "\t\t\t\t\t\"average\": 0.997616,\n" +
            "\t\t\t\t\t\"min\": 0.99545\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 40,\n" +
            "\t\t\t\t\t\"top\": 383,\n" +
            "\t\t\t\t\t\"height\": 14,\n" +
            "\t\t\t\t\t\"left\": 78\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"收款人\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.00001,\n" +
            "\t\t\t\t\t\"average\": 0.994915,\n" +
            "\t\t\t\t\t\"min\": 0.991737\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 26,\n" +
            "\t\t\t\t\t\"top\": 384,\n" +
            "\t\t\t\t\t\"height\": 11,\n" +
            "\t\t\t\t\t\"left\": 249\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"复核\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.001613,\n" +
            "\t\t\t\t\t\"average\": 0.981007,\n" +
            "\t\t\t\t\t\"min\": 0.891236\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 75,\n" +
            "\t\t\t\t\t\"top\": 381,\n" +
            "\t\t\t\t\t\"height\": 16,\n" +
            "\t\t\t\t\t\"left\": 383\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"开票人:刘欣\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"probability\": {\n" +
            "\t\t\t\t\t\"variance\": 0.054737,\n" +
            "\t\t\t\t\t\"average\": 0.751609,\n" +
            "\t\t\t\t\t\"min\": 0.363514\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"location\": {\n" +
            "\t\t\t\t\t\"width\": 64,\n" +
            "\t\t\t\t\t\"top\": 384,\n" +
            "\t\t\t\t\t\"height\": 19,\n" +
            "\t\t\t\t\t\"left\": 516\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"words\": \"为销售音\"\n" +
            "\t\t\t}\n" +
            "\t\t]\n" +
            "\t}\n" +
            "}";

}
