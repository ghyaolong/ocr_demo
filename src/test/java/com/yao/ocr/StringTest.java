package com.yao.ocr;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/11/24.
 */
public class StringTest {
    @Test
    public void testString() {
        /*String baseResult = "开票日期:2017年08月26日";
        int index = baseResult.indexOf("asdf");
        System.out.println(index);*/
    }

    @Test
    public void testSplitStringAndString(){
        String baseStr = "方|开户行及账号:民生银行西安高新开发区支行1203014210003578";
        String str = baseStr.replaceAll(".*[^\\d](?=(\\d+))","");
        System.out.println(str);
    }

    @Test
    public void testEvictNumFromString(){
        String baseStr = "方|开户行及账号:民生银行西安高新开发区支行1203014210003578";
        String str = baseStr.replaceFirst("\\d+([^\\d]*?$)", "$1");
        str = str.substring(str.indexOf(":")+1);
        System.out.println(str);
    }

    /**
     * ppp:￥192.03账
        ppp:￥1511.83
         ppp:(小写)￥1703.86
     */
    @Test
    public void testExtractMoenyFromString(){
        String str ="(小写)￥1703.86";
        str =str.replaceAll("(?<!\\d)\\D", "").replaceAll("[\\u4E00-\\u9FA5\\\\s]","");
        System.out.println(str);
        Map map =  new HashMap<String,String>();
    }

    @Test
    public void test(){
        String aa ="6//地址、电话:西安市高新区高新路6号1幢1单元12501宽13571882169//96>9/4/4+9128*-/9>719>7<547";
        System.out.println(aa.indexOf("//"));
        System.out.println(aa.lastIndexOf("//"));
        System.out.println(aa.substring(aa.indexOf("//")+2,aa.lastIndexOf("//")));
    }


}
