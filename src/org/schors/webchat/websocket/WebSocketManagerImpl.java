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

import com.google.gson.Gson;
import org.schors.webchat.jabber.JabberEndpoint;
import org.schors.webchat.json.WSMessage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WebSocketManagerImpl implements WebSocketManager {

    private List<WebChatSocket> webEndPoints = new CopyOnWriteArrayList<>();
    private List<JabberEndpoint> listeners = new CopyOnWriteArrayList<>();
    private Executor executor = Executors.newCachedThreadPool();
    private Gson gson = new Gson();

    @Override
    public void registerSocket(WebChatSocket webChatSocket) {
        webEndPoints.add(webChatSocket);
    }

    @Override
    public void removeSocket(WebChatSocket session) {
        webEndPoints.remove(session);
    }

    @Override
    public void onMessage(WebChatSocket webChatSocket, String message) {
        RequestHandler handler = new RequestHandler(webChatSocket, message);
        executor.execute(handler);
    }

    @Override
    public void addListener(JabberEndpoint listener) {
        listeners.add(listener);
    }

    @Override
    public void broadCast(String message) {
        broadCast("", message);
    }

    @Override
    public void broadCast(String who, String message) {
        WSMessage wsMessage = new WSMessage(String.valueOf(System.currentTimeMillis()), who, "msg", message);
        String msg = gson.toJson(wsMessage);

        for (WebChatSocket chat : webEndPoints) {
            chat.send(msg);
        }
    }

    private class RequestHandler implements Runnable {

        private String message;
        private WebChatSocket webChatSocket;
        private Gson gson = new Gson();

        public RequestHandler(WebChatSocket webChatSocket, String message) {
            this.webChatSocket = webChatSocket;
            this.message = message;
        }

        @Override
        public void run() {
            WSMessage wsMessage = gson.fromJson(message, WSMessage.class);

            switch (wsMessage.getCommand()) {
                case "join":
                    if (webChatSocket.getName() == null) {
                        //ask jabber to join
                        webChatSocket.setName(wsMessage.getWho());
                        registerSocket(webChatSocket);
                        WSMessage joinRes = new WSMessage(String.valueOf(System.currentTimeMillis()), wsMessage.getWho(), "join", ""); //set room members list
                        webChatSocket.send(gson.toJson(joinRes));
                    } else sendError("Already joined");
                    break;
                case "msg":
                    if (webChatSocket.getName() != null) {
                        for (JabberEndpoint listener : listeners) {
                            listener.onMessage(webChatSocket.getName(), message);
                        }
                    } else sendError("Not joined");
                    break;
                case "list":
                    // get list
                    if (webChatSocket.getName() != null) {
                        WSMessage listRes = new WSMessage(String.valueOf(System.currentTimeMillis()), webChatSocket.getName(), "list", ""); //set room members list
                        webChatSocket.send(gson.toJson(listRes));
                    } else sendError("Not joined");
                    break;
                case "quit":
                    // exit room
                    if (webChatSocket.getName() != null) {
                        WSMessage quitRes = new WSMessage(String.valueOf(System.currentTimeMillis()), webChatSocket.getName(), "quit", "ok");
                        webChatSocket.send(gson.toJson(quitRes));
                        removeSocket(webChatSocket);
                        webChatSocket.close();
                    } else sendError("Not joined");
                    break;
            }
        }

        private void sendError(String error) {
            WSMessage errorMessage = new WSMessage(String.valueOf(System.currentTimeMillis()), "", "error", error);
            webChatSocket.send(gson.toJson(errorMessage));
        }
    }
}
