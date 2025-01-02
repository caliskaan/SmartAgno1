package com.example.agnoapp;
// Bu arayüz, mesaj alma olaylarını dinlemek için kullanılır.
public interface IMessageListener {
    // Yeni bir mesaj alındığında çağrılan yöntem.
    void onMessageReceived(String message);
}
