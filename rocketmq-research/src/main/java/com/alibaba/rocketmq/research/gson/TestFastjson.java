package com.alibaba.rocketmq.research.gson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;


/**
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class TestFastjson {

    public static void main(String[] args) {
        ContactBook contactBook = new ContactBook();

        contactBook.setName("������ͨ��¼");
        contactBook.setCode(100);

        contactBook.getCustomField().put("CustomKey1", "CustomValue1");
        contactBook.getCustomField().put("CustomKey2", "CustomValue2");
        contactBook.getCustomField().put("CustomKey3", "CustomValue3");

        contactBook.getContactList().add(new Contact("���»�", 54, 60.56, "����\"", SexType.BOY));
        contactBook.getContactList().add(new Contact("�Ż���", 41, 52.69, "�о���", SexType.GIRL));
        contactBook.getContactList().add(new Contact("���ǳ�", 54, 61.22, "��ʿ", SexType.BOY));

        String json = JSON.toJSONString(contactBook, SerializerFeature.WriteClassName);
        System.out.println(json);

        // ContactBook fan = (ContactBook) JSON.parse(json);

        ContactBook fan = JSON.parseObject(json, ContactBook.class);
        System.out.println(fan);
    }
}
