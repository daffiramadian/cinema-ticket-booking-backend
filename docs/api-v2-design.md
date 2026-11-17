# Desain Arsitektur dan Alur API v2

Dokumen ini menjabarkan pembagian tanggung jawab antar service, alur pemesanan tiket dari hulu ke hilir, keputusan penyimpanan poster film, serta rancangan penguncian kursi (*seat locking*) pada API versi 2.0.

> [!IMPORTANT]
> Seluruh fitur API v2 pada dokumen ini berstatus **rancangan kontrak terencana (Iterasi 9)** dan **belum diimplementasikan** pada kode Java saat ini. Implementasi akan dikerjakan secara bertahap pada Iterasi 10 (GET, POST), Iterasi 11 (PUT, PATCH, DELETE), dan Iterasi 12 (pengujian).

---

## 1. Kepemilikan Resource per Service (*Service Ownership*)

Sistem terdiri atas tiga microservice terpisah dengan batas kepemilikan data sebagai berikut:

| Service | Port Lokal | Database | Resource yang Dimiliki | Tanggung Jawab Utama |
|---|---|---|---|---|
| **Catalog Service** | `8081` | `catalog_db` | `Movie`, `Showtime` | Mengelola metadata film lengkap (sinopsis, genre, poster, rating usia, pemeran, sutradara, trailer, tanggal rilis) dan jadwal tayang (*showtime*) per film beserta harga tiket. |
| **Booking Service** | `8082` | `booking_db` | `SeatAvailability`, `Booking` | Mengelola inventaris/ketersediaan kursi per *showtime* (`AVAILABLE`, `HELD`, `BOOKED`) serta siklus hidup reservasi kursi (`PENDING_PAYMENT`, `CONFIRMED`, `CANCELLED`). |
| **Payment Service** | `8083` | `payment_db` | `Payment` | Mengelola pencatatan dan simulasi deterministik transaksi pembayaran (`SUCCESS`, `FAILED`) berdasarkan `bookingId`. |

Setiap service hanya mengakses database miliknya sendiri. Validasi lintas service (seperti pemeriksaan keberadaan *showtime* oleh Booking Service ke Catalog Service) dilakukan melalui komunikasi HTTP REST sinkron.

---

## 2. Alur Pemesanan Tiket Lengkap (*Full Booking Flow*)

Alur pengguna dari melihat daftar film hingga konfirmasi atau pembatalan tiket berjalan melalui tahapan berikut:

```
Movie List ──> Movie Detail ──> Showtime List ──> Seat Map ──> Booking ──> Payment ──> Confirmation / Cancellation
```

1. **Daftar Film (*Movie List*):**
   Klien memanggil `GET /api/v2/movies` pada Catalog Service untuk menampilkan daftar film yang sedang tayang beserta `posterUrl`, `title`, `genres`, dan `ageRating`.
2. **Detail Film (*Movie Detail*):**
   Klien memilih satu film dan memanggil `GET /api/v2/movies/{id}` pada Catalog Service untuk membaca metadata lengkap (`synopsis`, `durationMinutes`, `director`, `cast`, `trailerUrl`, `releaseDate`).
3. **Daftar Jadwal Tayang (*Showtime List*):**
   Klien memanggil `GET /api/v2/movies/{movieId}/showtimes` pada Catalog Service untuk memperoleh daftar jadwal tayang film tersebut (`id`, `cinemaName`, `studioName`, `startsAt`, `price`).
4. **Denah dan Ketersediaan Kursi (*Seat Map*):**
   Setelah memilih satu `showtimeId`, klien memanggil `GET /api/v2/showtimes/{showtimeId}/seats` pada Booking Service. Booking Service memvalidasi `showtimeId` ke Catalog Service lalu mengembalikan daftar kursi (baris `A`-`E`, nomor `1`-`10`, format `A01`-`E10`) beserta status masing-masing (`AVAILABLE`, `HELD`, atau `BOOKED`).
5. **Pembuatan Reservasi (*Booking*):**
   Klien memilih satu kursi yang berstatus `AVAILABLE` dan mengirim `POST /api/v2/bookings` dengan `{ "showtimeId": "ST-001", "seatNumber": "C08" }`. Jika kursi tersedia, Booking Service membuat reservasi berstatus `PENDING_PAYMENT` dengan batas waktu penahanan kursi (`expiresAt`, 15 menit) dan mengubah status kursi menjadi `HELD`. Jika kursi sedang `HELD` atau `BOOKED`, Booking Service menolak permintaan dengan `409 Conflict`.
6. **Simulasi Pembayaran (*Payment*):**
   Sebelum waktu `expiresAt` habis, klien mengirim `POST /api/v2/payments` ke Payment Service dengan `bookingId` dan `amount`. Simulator menghasilkan status `SUCCESS` (atau `FAILED` jika `bookingId` berakhiran `-FAIL`).
7. **Konfirmasi atau Pembatalan (*Confirmation or Cancellation*):**
   - **Konfirmasi:** Pembayaran yang berhasil (`SUCCESS`) pada rancangan alur akhir v2 akan mengubah status booking menjadi `CONFIRMED` dan status kursi menjadi `BOOKED` secara permanen.
   - **Pembatalan / Kedaluwarsa:** Pengguna dapat membatalkan reservasi yang masih `PENDING_PAYMENT` melalui `PATCH /api/v2/bookings/{id}` dengan `{"action": "CANCEL"}`, yang mengubah status booking menjadi `CANCELLED`. Apabila waktu `expiresAt` terlewati tanpa pembayaran atau booking dibatalkan, kursi kembali menjadi `AVAILABLE`.

---

## 3. Keputusan Penyimpanan Poster Film (*Poster Decision*)

Untuk memenuhi kebutuhan tampilan katalog visual pada API v2 tanpa menambah kompleksitas infrastruktur pada tahap spesifikasi ini:

- **URL HTTPS Eksternal:** Field `posterUrl` (dan `trailerUrl` opsional) menyimpan string URL eksternal yang wajib menggunakan skema `https://` (misalnya `https://images.example.com/posters/interstellar.jpg`).
- **Tanpa Endpoint Upload atau Cloud Storage di Iterasi Ini:** Kontrak API v2 tidak menyertakan endpoint unggah file gambar (`multipart/form-data`), tidak mengintegrasikan Supabase Storage atau layanan *object storage* lain, serta tidak memakai integrasi TMDB API.
- **Evaluasi Penyimpanan di Masa Depan:** Apabila pada pengembangan berikutnya dibutuhkan fitur unggah poster kustom oleh admin, solusi penyimpanan objek (*object storage*) dapat dievaluasi secara terpisah.
- **Keamanan Kredensial:** Tidak ada API key, token, atau *secret* apa pun yang boleh dimasukkan atau di-*commit* ke dalam repositori.

---

## 4. Rancangan Penguncian Kursi (*Seat-Locking Design*)

> [!WARNING]
> **Status Implementasi Saat Ini vs Rancangan Terencana:**
> Mekanisme penguncian kursi di bawah ini adalah **rancangan terencana untuk implementasi API v2 (Iterasi 10-11)** dan **belum ada pada kode saat ini**.

### Perilaku Implementasi v1 Saat Ini
- Kode `BookingService` pada API v1 saat ini hanya melakukan pengecekan query biasa (`existsByShowtimeIdAndSeatNumberAndStatusIn`) untuk memeriksa apakah sudah ada booking berstatus `PENDING_PAYMENT` atau `CONFIRMED` sebelum menyimpan data baru.
- Pendekatan v1 tersebut belum memiliki tabel inventaris kursi per *showtime* dan belum menggunakan penguncian baris transaksional (*row-level locking*), sehingga dua request yang masuk pada milidetik yang sama berpotensi melewati pengecekan awal secara bersamaan (*race condition*).

### Rancangan Penguncian Transaksional v2 (Terencana)
Untuk mencegah *double booking* pada skenario konkurensi tinggi, implementasi API v2 dirancang dengan aturan berikut:

1. **Inventaris Kursi per Showtime:**
   Booking Service menyiapkan representasi inventaris kursi per `showtimeId` (baris `A` sampai `E`, nomor `1` sampai `10`) beserta *unique constraint* pada kombinasi `(showtime_id, seat_number)`.
2. **Proteksi Transaksional & *Row-Level / Pessimistic Locking*:**
   Saat memproses `POST /api/v2/bookings`, transaksi database harus mengunci baris inventaris kursi yang diminta (misalnya dengan *pessimistic write lock* `SELECT ... FOR UPDATE` pada baris kursi `(showtimeId, seatNumber)`). Request kedua yang meminta kursi yang sama pada saat bersamaan akan menunggu hingga transaksi pertama selesai, kemudian mendapati kursi sudah berstatus `HELD` dan langsung ditolak dengan `409 Conflict`.
3. **Pelepasan Kursi dari Hold Kedaluwarsa dan Pembatalan:**
   - Booking dengan status `PENDING_PAYMENT` yang telah melewati waktu `expiresAt` dianggap kedaluwarsa, sehingga kursinya kembali berstatus `AVAILABLE` dan dapat dipesan pengguna lain.
   - Booking yang dibatalkan melalui `PATCH /api/v2/bookings/{id}` dengan `{"action": "CANCEL"}` mengubah status reservasi menjadi `CANCELLED` dan langsung mengembalikan status kursi menjadi `AVAILABLE`.
4. **Transisi Menuju Status `BOOKED` / `CONFIRMED`:**
   Pembayaran yang berhasil (`SUCCESS`) pada alur terintegrasi v2 pada akhirnya harus memperbarui status booking menjadi `CONFIRMED` dan mengunci status kursi menjadi `BOOKED` sehingga kursi tersebut tidak lagi memiliki batas kedaluwarsa dan tidak dapat dibatalkan maupun dipesan ulang.
