/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 schors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.schors.webchat;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.schors.webchat.websocket.WebChatServlet;

import java.io.File;


public class WebChat {

    public static void main(String[] args) throws Exception {
        QueuedThreadPool threadPool = new QueuedThreadPool(512);
        Server jetty = new Server(threadPool);
        jetty.manage(threadPool);

        HttpConfiguration config = new HttpConfiguration();
        config.setSecurePort(8443);
        config.addCustomizer(new ForwardedRequestCustomizer());
        config.addCustomizer(new SecureRequestCustomizer());
        config.setSendServerVersion(true);

        HttpConnectionFactory http = new HttpConnectionFactory(config);
        ServerConnector httpConnector = new ServerConnector(jetty, http);
        httpConnector.setPort(8080);
        httpConnector.setIdleTimeout(10000);
        jetty.addConnector(httpConnector);

//        SslContextFactory sslContextFactory = new SslContextFactory();
//        sslContextFactory.setKeyStorePath("");
//        sslContextFactory.setKeyStorePassword("");
//
//        PushStrategy push = new ReferrerPushStrategy();
//        HTTPSPDYServerConnectionFactory spdy2 =
//                new HTTPSPDYServerConnectionFactory(2, config, push);
//        spdy2.setInputBufferSize(8192);
//        spdy2.setInitialWindowSize(32768);
//
//        HTTPSPDYServerConnectionFactory spdy3 =
//                new HTTPSPDYServerConnectionFactory(3, config, push);
//        spdy3.setInputBufferSize(8192);
//
//        NPNServerConnectionFactory npn = new NPNServerConnectionFactory(
//                spdy3.getProtocol(), spdy2.getProtocol(), http.getProtocol());
//        npn.setDefaultProtocol(http.getProtocol());
//        npn.setInputBufferSize(1024);
//
//        SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory,
//                npn.getProtocol());
//
//        ServerConnector spdyConnector = new ServerConnector(jetty, ssl,
//                npn, spdy3, spdy2, http);
//        spdyConnector.setPort(8443);
//
//        jetty.addConnector(spdyConnector);

        ContextHandler h1 = new ContextHandler("/app");
        ResourceHandler rs1 = new ResourceHandler();
        rs1.setWelcomeFiles(new String[]{"index.html"});
        File dir = new File("web");
        rs1.setBaseResource(Resource.newResource(dir));
        h1.setHandler(rs1);

        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(WebChatServlet.class, "/ws/api");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{h1, servletHandler, new DefaultHandler()});
        jetty.setHandler(handlers);

        jetty.start();
        jetty.join();

    }
}
