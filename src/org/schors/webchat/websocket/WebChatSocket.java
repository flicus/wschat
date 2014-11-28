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

package org.schors.webchat.websocket;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

public class WebChatSocket implements WebSocketListener {

    private static final Logger log = Logger.getLogger(WebChatSocket.class);
    private Session session;
    private String name;

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {

    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        WebSocketFactory.getInstance().removeSocket(this);
    }

    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
    }

    @Override
    public void onWebSocketError(Throwable cause) {

    }

    @Override
    public void onWebSocketText(String message) {
        WebSocketFactory.getInstance().onMessage(this, message);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void send(String text) {
        session.getRemote().sendStringByFuture(text);
    }

    public void close() {
        session.close();
    }
}
