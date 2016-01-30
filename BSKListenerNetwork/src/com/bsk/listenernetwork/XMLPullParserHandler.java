package com.bsk.listenernetwork;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class XMLPullParserHandler {
	private HashMap<Integer, String> hashmap;

	public HashMap<Integer, String> parse(InputStream is) {
		// ����XML���࣬ϵͳ��
		XmlPullParserFactory factory = null;
		// ����XML���࣬ϵͳ��
		XmlPullParser parser = null;
		int i = 1;
		try {
			// newһ��XmlPullParserFactory����
			factory = XmlPullParserFactory.newInstance();
			// ָ���ɴ˴������ɵĽ��������ṩ�� XML ���ƿռ��֧��
			factory.setNamespaceAware(true);
			parser = factory.newPullParser();
			// is��XML�ļ�·����nullΪ��������
			parser.setInput(is, null);

			String text = null;
			// ���ص�ǰ�¼������ͣ�һ������
			// 1��0 2:2 3:4 4:2 5:4 6:3 7:4 8:2 9:3
			int eventType = parser.getEventType();
			/*
			 * ��ȡ�� xml���������� START_DOCUMENT; ��ȡ�� xml�Ľ������� END_DOCUMENT ; ��ȡ��
			 * xml�Ŀ�ʼ��ǩ���� START_TAG ��ȡ�� xml�Ľ�����ǩ���� END_TAG ��ȡ�� xml���ı����� TEXT
			 */
			/*
			 * ���������Σ�<start> <package>com.tencent.mm</package>
			 * <package>com.sankuai.meituan</package> </start> 1.��ʼ����
			 */
			// ���eventType�������ı�ǩ���ͽ���
			while (eventType != XmlPullParser.END_DOCUMENT) {
				// 1.null 2.start 3.null 4.package 5.null 6.package 7.null
				// 8.startActivity
				// 9.startActivity
				String tagname = parser.getName();
				switch (eventType) {
				// 1��2....
				case XmlPullParser.START_TAG:
					if (tagname.equalsIgnoreCase("start")) {
						// 1.null 2.{} 3.{} 4.{} 5.{} 6.{}
						// 7.{1=com.tencent.mobileqq} 8.{1=com.tencent.mobileqq}
						// 9.{1=com.tencent.mobileqq}
						hashmap = new HashMap<Integer, String>();
					}
					break;
				// 1��4....
				case XmlPullParser.TEXT:
					// 1.null 2.null 3.\n\t\t 4.\n\t\t 5.com.tencent.mobileqq
					// 6.com.tencent.mobileqq
					// 7.\t\t\n\t\t 8.\t\t\n\t\t 9.\t\t\n\t\t
					text = parser.getText();
					break;
				// 1:3....
				case XmlPullParser.END_TAG:
					if (tagname.equalsIgnoreCase("connect")) {
						// 1.null 2.{} 3.{} 4.{} 5.{} 6.{1=com.tencent.mobileqq}
						// 7.{1=com.tencent.mobileqq}
						// 8.{1=com.tencent.mobileqq} 9.startActivity
						hashmap.put(i, text);
						i++;
					} 
					break;
				default:
					break;
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// System.out.println(hashmap);

		return hashmap;
	}
}
