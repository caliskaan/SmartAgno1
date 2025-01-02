package com.example.agnoapp;

import androidx.annotation.NonNull;
import okhttp3.*;
import okio.ByteString;
public class WebSocketClient {
    // WebSocket çalışması için OkHttpClient nesnesi tanımlanıyor
    private final OkHttpClient client;
    private String url;// Bağlanılacak WebSocket URL'si
    public boolean isConnected;// Bağlantı durumu
    private WebSocket webSocket;// WebSocket nesnesi
    public WebSocketClient() {
        client = new OkHttpClient();
    } // OkHttpClient nesnesi oluşturuluyor
    private IMessageListener messageListener; // Mesaj dinleyicisi

    // Mesaj dinleyicisini ayarlamak için metot
    public void setMessageListener(IMessageListener listener) {
        this.messageListener = listener;
    }


    // WebSocket bağlantısını başlatır
    public void start(String url) {
        System.out.println("start");
        this.url=url;
        // WebSocket bağlantısı için gerekli istek ayarı yapılıyor
        Request request = new Request.Builder().url(url).build();
        webSocket=client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                System.out.println("on open");
                isConnected=true; // Bağlantı açıldığında durum güncelleniyor
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                super.onMessage(webSocket, text);
                System.out.println("onmessage");
                if (messageListener != null) {
                    messageListener.onMessageReceived(text); // Gelen mesaj dinleyiciye iletiliyor
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                isConnected=false;// Bağlantı hatası durumunda durum güncelleniyor
            }
            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosed(webSocket, code, reason);
                isConnected=false;// Bağlantı kapatıldığında durum güncelleniyor
            }
        });
    }
    // WebSocket aracılığıyla mesaj gönderir
    public void sendMessage(String message) {
        if (!(isConnected)) {
            start(url);// Bağlı değilse bağlantıyı yeniden başlatıyor
            webSocket.send(message); // Mesajı gönderiyor
        }
        else {
            webSocket.send(message); // Bağlı ise direkt mesajı gönderiyor
        }
    }
    // WebSocket bağlantısını kapatır
    public void stop() {
        if (webSocket != null && isConnected) {
            webSocket.close(1000, "Normal closure");// Normal kapanış koduyla kapatıyor
            isConnected = false;// Bağlantı durumu güncelleniyor
        }
    }
}
