package com.cnlaunch.autoclear.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class XMLPullParserHandler
{
	List<ClearData> mClearDatas;

	public XMLPullParserHandler()
	{
		mClearDatas = new ArrayList<ClearData>();
	}

	public List<ClearData> parse(InputStream is)
	{
		XmlPullParserFactory factory = null;
		XmlPullParser parser = null;
		try
		{
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			parser = factory.newPullParser();
			
			parser.setInput(is, null);

			ClearData clearData = null;
			String text = null;
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT)
			{
				String tagname = parser.getName();
				switch (eventType)
				{
					case XmlPullParser.START_TAG:
						if (tagname.equalsIgnoreCase("data"))
						{
							// create a new instance of cleardata
							clearData = new ClearData();
						}
						break;

					case XmlPullParser.TEXT:
						text = parser.getText();
						break;

					case XmlPullParser.END_TAG:
						if (tagname.equalsIgnoreCase("data"))
						{
							// add cleardata object to list
							mClearDatas.add(clearData);
						}
						else if (tagname.equalsIgnoreCase("type"))
						{
							clearData.setType(text);
						}
						else if (tagname.equalsIgnoreCase("path"))
						{
							clearData.setPath(text);
						}
						break;

					default:
						break;
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		return mClearDatas;
	}
}
