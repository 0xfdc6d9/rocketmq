/**
 * 
 */
package com.alibaba.rocketmq.research.gson;

/**
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class TestConvert {

    public static void main(String[] args) {
        ContactBook contactBook = new ContactBook();

        contactBook.setName("������ͨ��¼");
        contactBook.setCode(100);

        contactBook.getCustomField().put("CustomKey1", "CustomValue1");
        contactBook.getCustomField().put("CustomKey2", "CustomValue2");
        contactBook.getCustomField().put("CustomKey3", "CustomValue3");

        contactBook.getContactList().add(new Contact("���»�", 54, 60.56, "����", SexType.BOY));
        contactBook.getContactList().add(new Contact("�Ż���", 41, 52.69, "�о���", SexType.GIRL));
        contactBook.getContactList().add(new Contact("���ǳ�", 54, 61.22, "��ʿ", SexType.BOY));

        // String json = contactBook.encode();
        // System.out.println(json);
        //
        // ContactBook d = ContactBook.decode(json, ContactBook.class);
        // System.out.println(d);
    }
}
