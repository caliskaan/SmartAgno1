package com.example.agnoapp;
// Bu arayüz, veri yükleme gibi temel işlemleri tanımlar.
public interface IDataManagement {
    // Uygulamanın ihtiyaç duyduğu veri kaynağını (ör. dosya, API, veritabanı) yüklemek için kullanılır.
    void LoadData();
}
