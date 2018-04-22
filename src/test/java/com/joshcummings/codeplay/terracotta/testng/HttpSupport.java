/*
 * Copyright 2015-2018 Josh Cummings
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.joshcummings.codeplay.terracotta.testng;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HttpSupport {
	protected CloseableHttpClient httpclient = HttpClients.createDefault();
	protected HttpHost proxy = new HttpHost("localhost", 8081, "http");
	protected RequestConfig config;
	
	protected final String host;

	public HttpSupport() {
		this(null);
	}

	public HttpSupport(String host) {
		this.host = host == null ? "localhost:8080" : host;

		if ( this.host.startsWith("localhost") ) {
			config = RequestConfig.custom().build();
		} else {
			config = RequestConfig.custom().setProxy(proxy).build();
		}
	}

	public CloseableHttpResponse post(String path, BasicNameValuePair... body) throws IOException {
		try ( CloseableHttpResponse csrf = getForEntity("/csrf.jsp") ) {
			String token = csrf.getStatusLine().getStatusCode() == 200 ?
					new String(IOUtils.toByteArray(csrf.getEntity().getContent())) : null;
			
			HttpPost post = new HttpPost("http://" + host + path);
			post.setConfig(config);
			List<NameValuePair> nvps = new ArrayList<>(Arrays.asList(body));
			nvps.add(new BasicNameValuePair("csrfToken", token));
			post.setEntity(new UrlEncodedFormEntity(nvps));
			CloseableHttpResponse response = httpclient.execute(post);
			return response;
		}
	}
	
	public CloseableHttpResponse getForEntity(String path) throws IOException {
		HttpGet get = new HttpGet("http://" + host + path);
		get.setConfig(config);
		CloseableHttpResponse response = httpclient.execute(get);
		return response;
	}
}
