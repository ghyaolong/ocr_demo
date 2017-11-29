package com.yao.ocr;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/27.
 */
public class ListTest {

    @Test
    public void testList(){
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");

        for (int i = 0; i < list.size(); i++) {
            if(i==3){
                list.remove(list.get(i));
            }
            //System.out.println(list.get(i).toString());
        }

        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i).toString());
        }
    }
}
