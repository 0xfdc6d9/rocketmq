package com.alibaba.rocketmq.research.gson;

//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//
//
///**
// * @author shijia.wxr<vintage.wang@gmail.com>
// */
//public class TestGson {
//
//    public static void main(String[] args) {
//        ContactBook contactBook = new ContactBook();
//
//        contactBook.setName("������ͨ��¼");
//        contactBook.setCode(100);
//
//        contactBook.getCustomField().put(new Contact("���»�", 54, 60.56, "����\"", SexType.BOY), "CustomValue1");
//        contactBook.getCustomField().put(new Contact("�Ż���", 41, 52.69, "�о���", SexType.GIRL), "CustomValue2");
//        contactBook.getCustomField().put(new Contact("���ǳ�", 54, 61.22, "��ʿ", SexType.BOY), "CustomValue3");
//
//        contactBook.getContactList().add(new Contact("���»�", 54, 60.56, "����\"", SexType.BOY));
//        contactBook.getContactList().add(new Contact("�Ż���", 41, 52.69, "�о���", SexType.GIRL));
//        contactBook.getContactList().add(new Contact("���ǳ�", 54, 61.22, "��ʿ", SexType.BOY));
//
//        GsonBuilder builder = new GsonBuilder();
//        // // builder.excludeFieldsWithoutExposeAnnotation();
//        //
//        Gson gson = builder.create();
//
//        String json = gson.toJson(contactBook);
//        System.out.println(json);
//
//        ContactBook cb = gson.fromJson(json, ContactBook.class);
//        System.out.println(cb);
//        String json2 = gson.toJson(cb);
//        System.out.println(json2);
//
//    }
//}
