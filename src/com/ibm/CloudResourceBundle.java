/*
The MIT License (MIT)

Copyright (c) 2014 IBM

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.ibm;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;


public class CloudResourceBundle {
	private static Hashtable<String, Properties> resourceBundleCache = new Hashtable<String, Properties>();
	private static Rows rows = null;
	
	
	private static Rows getServerResponse(CloudDataConnection connect) throws Exception{
		Rows rows = null;
		
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
        		new AuthScope("provide.castiron.com", 443),
                new UsernamePasswordCredentials(connect.getUserid(), connect.getPassword()));
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
		
        try {
        	// Call the service and get all the strings for all the languages
        	HttpGet httpget = new HttpGet(connect.getURL());
            httpget.addHeader("API_SECRET", connect.getSecret());
            CloseableHttpResponse response = httpclient.execute(httpget);
            
            try {
            	InputStream in = response.getEntity().getContent();
                ObjectMapper mapper = new ObjectMapper();
                rows = mapper.readValue(new InputStreamReader(in, "UTF-8"), Rows.class);
                EntityUtils.consume(response.getEntity());
            }
            finally {
                response.close();
            }  
        }
        finally {
            httpclient.close();
        }
		return rows;
	}
	
	  
	public static ResourceBundle getBundle (Locale locale, CloudDataConnection connection){
		ResourceBundle rb = null;
		try {
			rows = getServerResponse(connection);
			rb = ResourceBundle.getBundle("", locale, new CloudRBControl());
		}
		catch(Exception e) {
			System.out.println("Error calling CloudIntegration service");
		}
				
		return rb;	
	}
	
	protected static class CloudRBControl extends ResourceBundle.Control {
		
		private String mapLocaleToLanguage(Locale locale) {
			String language;
			
			if (locale.equals(Locale.CHINA) || 
					locale.equals(Locale.PRC) || 
					locale.equals(Locale.CHINESE) || 
					locale.equals(Locale.SIMPLIFIED_CHINESE))
				language = "zh-Hans";
			else if (locale.equals(Locale.TAIWAN) || 
					locale.equals(Locale.TRADITIONAL_CHINESE))
				language = "zh-Hant";
			else if (locale.equals(Locale.JAPAN) || locale.equals(Locale.JAPANESE))
				language = "ja";
			else {
				language = locale.getLanguage();
			}
			
			return language;
		}
		
		@Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
				throws IllegalAccessException, InstantiationException, IOException {
			
			if (baseName == null || locale == null || format == null || loader == null)
	                 throw new NullPointerException();
			
			ResourceBundle bundle = null;
			String lang = mapLocaleToLanguage(locale);
            bundle = new CloudBundle(lang);       
            return bundle;
		}
	}
	
	
	protected static class CloudBundle extends ResourceBundle{	     
		private Properties props = null;
	    
		CloudBundle(String lang) throws IOException {
			// Set the default fallback to English
			if(lang == null || lang.isEmpty()) {
				lang = "en";
			}
			
			props = resourceBundleCache.get(lang);
			
			// We have no entries for the language
			if(props == null) {
				if (rows != null) {
					ArrayList<TranslationElement> elements = rows.getRow().getElements();
					props = new Properties();
				
					for (TranslationElement element : elements){
						if(element.getLanguage().trim().equals(lang)) {
							System.out.println("Processing: " + element.getKey());
							props.setProperty(element.getKey(), element.getValue());
						}
					}
					// Add the properties to the table
					resourceBundleCache.put(lang, props);
				}
			}
		}
			
	     protected Object handleGetObject(String key) {
	         return props.getProperty(key);
	     }
	     
	     public Enumeration<String> getKeys() {
	    	 return Collections.enumeration(keySet());	    
	     }		
		
	}
	
}
