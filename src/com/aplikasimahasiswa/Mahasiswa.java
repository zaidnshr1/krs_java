package com.aplikasimahasiswa;

public class Mahasiswa {
    private String nim;
    private String namaLengkap;
    private String nomorTelepon;

    public Mahasiswa(String nim, String namaLengkap, String nomorTelepon) {
        this.nim = nim;
        this.namaLengkap = namaLengkap;
        this.nomorTelepon = nomorTelepon;
    }

    public String getNim() {
        return nim;
    }

    public String getNamaLengkap() {
        return namaLengkap;
    }

    public String getNomorTelepon() {
        return nomorTelepon;
    }

    @Override
    public String toString() {
        return this.namaLengkap;
    }
}