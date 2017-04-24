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


import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

/**
 * Servlet implementation class CloudTranslation
 */
@WebServlet("/CloudTranslation")
public class CloudTranslation extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static CloudDataConnection connection = null;
	
	
	private static void initCloudIntegration() {
		String userid = null;
	    String password = null;
		String url = null;
		String apiSecret = null;
		
		// 'VCAP_SERVICES' contains all the credentials of services bound to this application.
		// Parse it to obtain the userid and password for the Cloud Integration service
		Map<String, String> env = System.getenv();
		String vcap = env.get("VCAP_SERVICES");

		boolean foundService = false;
		if (vcap == null) {
			System.out.println("No VCAP_SERVICES found");
		} else {
			try {
				// parse the VCAP JSON structure
				JSONObject obj = JSONObject.parse(vcap);
				for (Iterator<?> iter = obj.keySet().iterator(); iter.hasNext();) {
					String key = (String) iter.next();
					if (key.startsWith("CloudIntegration")) {
						JSONArray val = (JSONArray)obj.get(key)!=null?(JSONArray)obj.get(key):null;
						if(val!=null){
							JSONObject service = val.get(0)!=null?(JSONObject)val.get(0):null;
							JSONObject credentials = service!=null?(service.get("credentials")!=null?
									(JSONObject)service.get("credentials"):null):null;
							userid = credentials.get("userid") !=null?(String) credentials.get("userid"):"";
							password =  (String) credentials.get("password") !=null?(String) credentials.get("password"):"";
					
							// get the URLs to the APIs
							JSONArray apis = credentials.get("apis")!=null?(JSONArray) credentials.get("apis"):null;
							if(apis!=null) {
								for(Iterator<?> api = apis.iterator(); api.hasNext();) {
									JSONObject apiAttr = (JSONObject) api.next();
									String name = apiAttr.get("name") != null?(String) apiAttr.get("name"):"";
									if(name.equals("translation")) {
										url = apiAttr.get("url") !=null?(String) apiAttr.get("url"):"";
										apiSecret = apiAttr.get("API_SECRET") !=null?(String) apiAttr.get("API_SECRET"):"";
									}
								}
							}
							
							connection = new CloudDataConnection(userid, password, url, apiSecret);
							foundService = true;
							break;
						}
					}
				}
			} catch (Exception e) {
			}
		}
		if (!foundService) {
			System.out.println("Did not find Cloud Integration service");
		}
		
	}
	
    public CloudTranslation() {
        super();
        System.setProperty("jsse.enableSNIExtension", "false");
        initCloudIntegration();
    }
    
    public Locale getLocaleFromHeader(String header) {
    	Locale locale = null;
    	
    	if(!header.isEmpty()) {
			String[] languages = header.split(",");
			
			if(languages.length > 0) {
				// Grab the first language in the list and ignore the q value by splitting it out 
				String[] lang = languages[0].split(";");
				
				// Split out the parts of the language tag
				String[] elements = lang[0].split("-");
				switch(elements.length) {
					case 1: locale = new Locale(elements[0]); break;
					case 2: locale = new Locale(elements[0], elements[1]); break;
					/* Internet explorer 10 and above specifies the script as the second element
					 *  in the tag and needs to be placed as the third argument in the 
					 *  Locale constructor 
					*/
					case 3: locale = new Locale(elements[0], elements[2], elements[1]); break;
					default: locale = Locale.getDefault(); break;
				}
			}
			
		}
    	else {
    		locale = Locale.getDefault();
    	}
    	return locale;
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String acceptLang = request.getHeader("Accept-Language");
		
		Locale locale = getLocaleFromHeader(acceptLang);
		
		ResourceBundle bundle = CloudResourceBundle.getBundle(locale, connection);
		
		//String content = bundle.getString("put your key here");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
