    package com.kostify.app;

    public class user {



            public String nama;
            public String email;
            public String whatsapp;

            // Konstruktor kosong dibutuhkan oleh Firebase
            public user() {
            }

            public user(String nama, String email, String whatsapp) {
                this.nama = nama;
                this.email = email;
                this.whatsapp = whatsapp;
            }


    }
