package com.geekplaya.AdventureLobbies.Objects;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;

public class WebsiteReader {
	
	private URL siteURL;
	private String siteContent;
	
	public WebsiteReader(String url) {
		try {
		siteURL = new URL(url);
		refresh();
		} catch(MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		return siteContent;
	}
	
	public String getContent() {
		return siteContent;
	}
	
	public void setURL(String url) {
		try {
		siteURL = new URL(url);
		siteContent = "";
		} catch(MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public void refresh() {
		try {
			URLConnection con = siteURL.openConnection();
			InputStream in = con.getInputStream();
			String encoding = con.getContentEncoding();
			encoding = encoding == null ? "UTF-8" : encoding;
			siteContent = IOUtils.toString(in, encoding).trim().replace("\n", "").replace("\r", "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean contains(String s, boolean caseSensitive) {
		if(caseSensitive)
			return siteContent.contains(s);
		else
			return siteContent.toLowerCase().contains(s.toLowerCase());
	}
	
}