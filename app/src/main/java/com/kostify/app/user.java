    package com.kostify.app;

    public class user {
            public String nama;
            public String email;
            public String telepon;

            // Konstruktor kosong dibutuhkan oleh Firebase
            public user() {
            }

            public user(String nama, String email, String telepon) {
                this.nama = nama;
                this.email = email;
                this.telepon = telepon;
            }

        public String getNama() { return nama; }
        public String getEmail() { return email; }
        public String getTelepon() { return telepon; }


    }
