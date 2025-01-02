package com.example.agnoapp;

// T türünde sonuçlarla çalışan genel bir arayüzdür.
// Bu arayüz, bir işlemin başarılı olup olmadığını bildirmek için kullanılır.
public interface ICallBack <T>{
    // İşlem başarılı olduğunda çağrılan yöntem.
    void onSuccess(T result);
    // İşlem başarısız olduğunda çağrılan yöntem.
    void onFailure(Exception e);
}
