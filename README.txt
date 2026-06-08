DOCUMENTATION PROGRAM KRS JAVA

1. REPOSITORI
Salin source code dari repositori:
https://github.com/zaidnshr1/krs_java

2. KONFIGURASI DATABASE (POSTGRESQL)
Buat database bernama 'akademik_db'. Jika ingin menggunakan nama lain, sesuaikan konfigurasi pada class 'DatabaseConnection.java' (URL, USER, PASSWORD, PORT).

Eksekusi query berikut untuk membuat tabel dan memasukkan data awal:

-- Tabel 1: mahasiswa
CREATE TABLE public.mahasiswa (
    nim CHARACTER VARYING(15) NOT NULL,
    nama_lengkap CHARACTER VARYING(100) NOT NULL,
    nomor_telepon CHARACTER VARYING(15) NOT NULL,
    CONSTRAINT mahasiswa_pkey PRIMARY KEY (nim)
);

-- Tabel 2: mata_kuliah
CREATE TABLE public.mata_kuliah (
    kode_mk CHARACTER VARYING(10) NOT NULL,
    nama_mk CHARACTER VARYING(100) NOT NULL,
    sks INTEGER NOT NULL,
    semester INTEGER NOT NULL,
    CONSTRAINT mata_kuliah_pkey PRIMARY KEY (kode_mk)
);

-- Tabel 3: krs_pengajuan
CREATE TABLE public.krs_pengajuan (
    id_pengajuan SERIAL NOT NULL,
    nim CHARACTER VARYING(15) NOT NULL,
    kode_mk CHARACTER VARYING(10) NOT NULL,
    dosen_pa CHARACTER VARYING(100) NOT NULL,
    tanggal_pengajuan TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT krs_pengajuan_pkey PRIMARY KEY (id_pengajuan)
);

-- Seed Data: mata_kuliah
INSERT INTO public.mata_kuliah (kode_mk, nama_mk, sks, semester) VALUES
('IF101', 'Pemrograman Dasar', 3, 1),
('IF102', 'Matematika Diskrit', 3, 1),
('IF201', 'Struktur Data', 3, 2),
('IF202', 'Pemrograman Berorientasi Objek', 4, 2),
('IF301', 'Basis Data', 3, 3),
('IF302', 'Rekayasa Perangkat Lunak', 3, 3),
('IF401', 'Sistem Operasi', 3, 4),
('IF402', 'Jaringan Komputer', 3, 4),
('IF501', 'Pemrograman Web', 3, 5),
('IF502', 'Kecerdasan Buatan', 3, 5),
('IF601', 'Keamanan Informasi', 3, 6),
('IF602', 'Kerja Praktik', 2, 6),
('IF701', 'Metodologi Penelitian', 2, 7),
('IF702', 'Skripsi / Tugas Akhir', 6, 7);

3. MENJALANKAN APLIKASI
- Pastikan kredensial database lokal telah disesuaikan di class 'DatabaseConnection.java'.
- Jalankan file utama 'Main.java' untuk memulai aplikasi.

4. INFORMASI LOGIN
Sistem menyediakan dua metode autentikasi default:
- Metode 1 (Username & Password):
  Username: admin
  Password: 123
- Metode 2 (Passcode):
  Passcode: 456789

Kredensial login di atas dapat diubah melalui file 'LoginFrame.java'.
