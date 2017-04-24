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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TranslationElement {
	@JsonProperty("id")
	private long id;
	
	@JsonProperty("language")
	private String language;
	
	@JsonProperty("key")
	private String key;
	
	@JsonProperty("value")
	private String value;
	
	
	@JsonCreator
	public TranslationElement(@JsonProperty("id") long i, @JsonProperty("language") String l, @JsonProperty("key") String k, @JsonProperty("value") String v) {
		id = i;
		language = l;
		key = k;
		value = v;
	}
	
	
	@JsonProperty("key")
	public void setKey(String k) {
		key = k;
	}
	
	@JsonProperty("key")
	public String getKey() {
		return key;
	}
	
	@JsonProperty("language")
	public void setLanguage(String l) {
		language = l;
	}
	
	@JsonProperty("language")
	public String getLanguage() {
		return language;
	}
	
	@JsonProperty("value")
	public void setValue(String v) {
		value = v;
	}
	
	@JsonProperty("value")
	public String getValue() {
		return value;
	}
	
	@JsonProperty("id")
	public void setID(long i) {
		id = i;
	}
	
	@JsonProperty("id")
	public long getID() {
		return id;
	}
}
