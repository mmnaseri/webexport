/*
 * Copyright (c) 2013. AgileApes (http://www.agileapes.scom/), and
 * associated organizations.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 */

package com.agileapes.webexport.net.impl;

import com.agileapes.webexport.net.PageDownloader;
import com.agileapes.webexport.net.ProxyDescriptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.*;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/2/13, 22:43)
 */
public class DefaultPageDownloader implements PageDownloader {

    private final Proxy proxy;
    private final URL url;
    private URLConnection connection;
    private boolean connected;
    private final String userAgent;

    public DefaultPageDownloader(String url) throws MalformedURLException {
        this(url, (String) null);
    }

    public DefaultPageDownloader(URL url) {
        this(url, (String) null);
    }

    public DefaultPageDownloader(String url, String userAgent) throws MalformedURLException {
        this(new URL(url), userAgent);
    }

    public DefaultPageDownloader(URL url, String userAgent) {
        this(url, null, userAgent);
    }

    public DefaultPageDownloader(String url, ProxyDescriptor proxyDescriptor) throws MalformedURLException {
        this(url, proxyDescriptor, null);
    }

    public DefaultPageDownloader(String url, ProxyDescriptor proxyDescriptor, String userAgent) throws MalformedURLException {
        this(new URL(url), proxyDescriptor, userAgent);
    }

    public DefaultPageDownloader(URL url, ProxyDescriptor proxyDescriptor) {
        this(url, proxyDescriptor, null);
    }

    public DefaultPageDownloader(URL url, ProxyDescriptor proxyDescriptor, String userAgent) {
        this.url = url;
        this.userAgent = userAgent;
        if (proxyDescriptor == null) {
            proxy = Proxy.NO_PROXY;
        } else {
            proxy = new Proxy(proxyDescriptor.getType(), new InetSocketAddress(proxyDescriptor.getHost(), proxyDescriptor.getPort()));
        }
        connected = false;
    }

    private Proxy getProxy() {
        return proxy;
    }

    @Override
    public void connect() throws IOException {
        if (connected) {
            throw new IllegalStateException("Downloader is already connected");
        }
        URLConnection connection = url.openConnection(getProxy());
        if (connection instanceof HttpURLConnection) {
            final CookieManager cookieManager = new CookieManager();
            HttpURLConnection urlConnection = (HttpURLConnection) connection;
            urlConnection.setInstanceFollowRedirects(false);
            if (userAgent != null) {
                urlConnection.setRequestProperty("User-Agent", userAgent);
            }
            urlConnection.connect();
            cookieManager.storeCookies(urlConnection);
            while (urlConnection.getResponseCode() / 100 == 3) {
                //we are being redirected
                final String location = urlConnection.getHeaderField("Location");
                urlConnection = (HttpURLConnection) new URL(location).openConnection(getProxy());
                cookieManager.setCookies(urlConnection);
                urlConnection.setInstanceFollowRedirects(false);
                if (userAgent != null) {
                    urlConnection.setRequestProperty("User-Agent", userAgent);
                }
                urlConnection.connect();
                cookieManager.storeCookies(urlConnection);
            }
            connection = urlConnection;
        } else {
            connection.connect();
        }
        connected = true;
        this.connection = connection;
    }

    @Override
    public void download(Writer writer) throws IOException {
        if (!connected) {
            throw new IllegalStateException("Downloader is not connected");
        }
        final InputStream connectionInputStream = connection.getInputStream();
        int data;
        while ((data = connectionInputStream.read()) != -1) {
            writer.write(data);
        }
    }

    @Override
    public URLConnection getConnection() {
        return connection;
    }

    public static void main(String[] args) throws Exception {
        final DefaultPageDownloader downloader = new DefaultPageDownloader("http://en.wikipedia.org/robots.txt", new ImmutableProxyDescriptor(Proxy.Type.SOCKS, "127.0.0.1", 32201), "wget");
        final StringWriter writer = new StringWriter();
        downloader.connect();
        downloader.download(writer);
        System.out.println(writer.toString());
    }

}
