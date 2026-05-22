# Dokumentasi Teknis RPG-Java

Dokumentasi ini menjelaskan implementasi kode pada repo `RPG-Java` berdasarkan kondisi source saat ini. Isi dokumen difokuskan pada dua kebutuhan sekaligus:

1. Kebutuhan teknis produksi: memahami struktur paket, alur runtime, tanggung jawab kelas, dan area technical debt.
2. Kebutuhan akademik OOP: menunjukkan bagaimana encapsulation, inheritance, polymorphism, abstraction, interface, overloading, dan overriding diterapkan pada kode nyata.

## Cara membaca dokumentasi ini

Jika pembaca masih berada pada learning curve, urutan baca yang paling aman adalah:

1. `ARCHITECTURE.md`
2. `RUNTIME_FLOW.md`
3. `CLASS_REFERENCE.md`
4. `OOP_ANALYSIS.md`
5. `REPORT_TEMPLATE.md`
6. `UML_NOTES.md`

## Ringkasan proyek

Secara implementasi, repo ini berisi tiga area utama:

- Domain battle OOP di `fantasyrpg.entities`, `fantasyrpg.interfaces`, dan `fantasyrpg.services`.
- World exploration berbasis Swing + TMX map loader di `fantasyrpg.ui.world`.
- Dua implementasi battle UI:
  - `fantasyrpg.ui.BattleGUI` yang memakai domain layer `entities` dan `services`.
  - `fantasyrpg.ui.battle.*` yang masih berupa battle scene terpisah/prototipe dan belum memakai `entities.Player`, `Enemy`, atau `BattleService`.

## Entry point yang tersedia

| Entry point | Peran | Catatan |
| --- | --- | --- |
| `fantasyrpg.Main` | Entry point utama proyek | Membuka start screen, lalu world scene |
| `fantasyrpg.core.Game` | Demo battle langsung | Membuka `BattleGUI` dengan `entities.Player` |
| `fantasyrpg.ui.world.Main` | Masuk langsung ke world scene | Berguna untuk menguji map/collision |
| `fantasyrpg.ui.battle.MainFrame` | Masuk langsung ke battle prototype | Tidak memakai domain battle OOP penuh |

## Struktur dokumen

- `ARCHITECTURE.md`: batas tanggung jawab paket, dependency direction, dan observasi desain.
- `RUNTIME_FLOW.md`: alur program dari startup, world loop, battle transition, dan turn resolution.
- `CLASS_REFERENCE.md`: referensi seluruh class/interface di source.
- `OOP_ANALYSIS.md`: analisis mendalam pilar OOP dan implementasinya pada proyek ini.
- `REPORT_TEMPLATE.md`: versi terisi untuk kebutuhan laporan/presentasi mata kuliah.
- `UML_NOTES.md`: panduan relasi class dan catatan diagram UML berbasis implementasi saat ini.

## Prinsip pembacaan

Dokumentasi ini sengaja jujur terhadap kondisi kode sekarang. Artinya, selain menjelaskan bagian yang sudah baik, dokumen ini juga menandai hal-hal berikut:

- duplikasi struktur UI,
- entry point yang berbeda-beda,
- pemisahan domain dan presentasi yang belum sepenuhnya konsisten,
- kelas yang sudah cukup OOP dan kelas yang masih procedural/stateful.

Pendekatan ini penting agar pembaca tidak hanya tahu "apa yang dibuat", tetapi juga memahami "mengapa desainnya bekerja" dan "apa yang masih bisa ditingkatkan".
