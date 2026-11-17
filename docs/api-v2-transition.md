# Transisi API v1 ke API v2

Dokumen ini menjelaskan hubungan API v1 dan API v2 pada Cinema Ticket Booking System.

> [!IMPORTANT]
> Pada Iterasi 9, API v2 baru berupa **kontrak OpenAPI**. Endpoint `/api/v2` belum ada di kode Java dan belum bisa dipanggil. Implementasi dijadwalkan pada Iterasi 10 (GET, POST) dan Iterasi 11 (PUT, PATCH, DELETE), lalu diuji pada Iterasi 12.

## API v1 tetap tersedia

- Seluruh endpoint `/api/v1/...` tetap berjalan dengan request, response, dan status code yang sama.
- File `openapi-v1.yaml` tidak diubah.
- Klien yang sudah memakai v1 tidak perlu melakukan apa pun.

## Alasan memakai prefix `/api/v2/`

Beberapa perubahan di v2 tidak kompatibel dengan v1. Contohnya, field `description` pada film diganti oleh `synopsis`, dan field wajib baru seperti `genres` dan `posterUrl` ditambahkan. Jika perubahan ini diterapkan langsung di `/api/v1`, klien lama akan menerima `400 Bad Request` atau response dengan bentuk yang berbeda. Prefix `/api/v2/` memisahkan kontrak baru sehingga v1 dan v2 bisa berjalan berdampingan.

## Perubahan utama di v2

| Area | API v1 | API v2 |
|---|---|---|
| Metadata film | `title`, `durationMinutes`, `description` | `title`, `synopsis`, `durationMinutes`, `genres`, `posterUrl`, `ageRating`, `director`, `cast`, `trailerUrl`, `releaseDate` |
| Poster film | Tidak ada | `posterUrl` wajib, berupa URL HTTPS eksternal |
| Jadwal tayang | Tidak ada resource showtime | Resource `Showtime` di Catalog Service (`/api/v2/showtimes`, `/api/v2/movies/{movieId}/showtimes`) |
| Ketersediaan kursi | Tidak ada | `GET /api/v2/showtimes/{showtimeId}/seats` di Booking Service dengan status `AVAILABLE`, `HELD`, `BOOKED` |
| Aturan booking | `showtimeId` tidak divalidasi, nomor kursi bebas | `showtimeId` divalidasi ke Catalog, kursi harus ada pada denah standar A01 sampai E10 |
| Status booking | Enum v1 mencantumkan `PAYMENT_FAILED` dan `EXPIRED` | `PENDING_PAYMENT`, `CANCELLED`, `CONFIRMED`. Hold yang kedaluwarsa dibaca dari `expiresAt` |
| Status pembayaran | Enum v1 mencantumkan `PENDING` | `SUCCESS` atau `FAILED` sesuai aturan simulator |
| Format error | Contoh di dokumen v1 memakai `timestamp`, `error`, `message`, `path` | ProblemDetail standar: `type`, `title`, `status`, `detail`, `instance` dengan content type `application/problem+json` |
| Swagger | Satu file gabungan untuk tiga service | Satu file per service dengan server URL dan port masing-masing |

## Kontrak ditulis sebelum implementasi

Kontrak v2 disusun lebih dulu agar implementasi (Iterasi 10 dan 11) dan pengujian (Iterasi 12) memakai acuan yang sama. Request, response, validasi, dan status code di file OpenAPI v2 menjadi kriteria penerimaan saat implementasi.

Sebuah fitur baru dianggap selesai setelah endpoint-nya diimplementasikan dan lulus pengujian. Fitur yang sudah tercatat di dokumen ini belum tentu sudah berjalan.

## Catatan terhadap usulan sebelumnya

`docs/api-versioning.md` pernah mengusulkan v2 dengan `screeningId`, banyak kursi (`seats[]`), dan `customerId`. Kontrak v2 ini **tidak** memakai usulan tersebut. Satu booking tetap mewakili satu kursi pada satu showtime, dan perubahan v2 dipusatkan pada metadata film, showtime, ketersediaan kursi, aturan booking, dan format error.

## Lokasi file

- Catalog Service: `services/catalog-service/src/main/resources/static/openapi-v2.yaml` (server `http://localhost:8081`)
- Booking Service: `services/booking-service/src/main/resources/static/openapi-v2.yaml` (server `http://localhost:8082`)
- Payment Service: `services/payment-service/src/main/resources/static/openapi-v2.yaml` (server `http://localhost:8083`)
- Desain dan alur: [api-v2-design.md](api-v2-design.md)
