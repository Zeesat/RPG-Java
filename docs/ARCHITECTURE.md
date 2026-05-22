# Arsitektur Sistem

## 1. Gambaran umum

`RPG-Java` adalah proyek game Java berbasis Swing yang menggabungkan:

- world exploration berbasis tile map,
- battle loop berbasis turn,
- entity model yang cukup jelas untuk pembelajaran OOP.

Secara arsitektur, kode dapat dibaca sebagai kombinasi tiga layer:

1. `Domain layer`
   Berisi model karakter, musuh, kontrak interface, dan aturan battle inti.
2. `Application/service layer`
   Berisi koordinasi battle, random event, dan pembuatan enemy.
3. `Presentation layer`
   Berisi start screen, world UI, dan battle UI.

## 2. Paket dan tanggung jawab

### `fantasyrpg`

- `Main` adalah entry point utama aplikasi.
- Saat ini `Main` mengarahkan pemain ke `StartingScreenFrame`.

### `fantasyrpg.core`

- `Game` adalah entry point alternatif.
- Kelas ini langsung membuat `entities.Player`, membuat `JFrame`, lalu memasang `ui.BattleGUI`.
- Secara praktis, `core.Game` adalah shortcut untuk menjalankan battle GUI tanpa world exploration.

### `fantasyrpg.entities`

Paket ini adalah inti domain tempur.

- `Character` adalah abstract base class untuk semua aktor yang bisa bertarung.
- `Player` adalah karakter yang dikendalikan pemain dalam domain battle.
- `Enemy` adalah base class untuk musuh.
- `Goblin`, `OrcWarrior`, dan `DragonBoss` adalah konkret enemy.

Di sinilah sebagian besar konsep OOP paling jelas terlihat.

### `fantasyrpg.interfaces`

- `Attackable` mendefinisikan kemampuan minimum entitas yang dapat menerima damage dan dicek status hidupnya.
- `SkillUser` mendefinisikan kontrak penggunaan skill pada target bertipe `Character`.

Paket ini membantu menurunkan coupling karena perilaku dapat dipanggil lewat kontrak, bukan lewat tipe konkret.

### `fantasyrpg.services`

- `BattleService` mengorkestrasi urutan aksi dalam battle.
- `RandomEventService` menambahkan variasi efek per ronde.
- `EnemyFactory` memetakan stage ke tipe enemy.

Ini adalah layer yang memisahkan aturan permainan dari UI.

### `fantasyrpg.ui`

- `BattleGUI` adalah battle screen yang memakai domain layer secara langsung.
- Kelas ini memanggil `BattleService` untuk logika dan menangani animasi/UI sendiri.

Secara arsitektural, `BattleGUI` adalah contoh paling dekat ke pola "UI memanggil service, service memanggil domain".

### `fantasyrpg.ui.start`

- `StartingScreenFrame` menampilkan layar pembuka sederhana.
- Saat pengguna menekan tombol apa pun, flow dipindahkan ke world scene.

### `fantasyrpg.ui.world`

Paket ini berisi eksplorasi map.

- `WorldFrame` dan `GameFrame` adalah container Swing untuk world panel.
- `GamePanel` adalah loop utama world exploration.
- `Player` di paket ini adalah representasi sprite world, bukan domain battle player.
- `KeyboardInput` menangani input WASD dan interaksi `E`.
- `TiledMapLoader` memuat TMX/TSX map, collision, spawn point, dan enemy spawn marker.
- `CollisionBlock`, `EnemySpawnPoint`, dan `EnemySpawnConfig` adalah helper object untuk collision dan marker musuh.

### `fantasyrpg.ui.battle`

Paket ini berisi battle scene lain yang lebih bersifat self-contained/prototype.

- `GamePanel` menyimpan HP, MP, log, dan animasi battle secara lokal.
- `ActionPanel` menangani tombol aksi.
- `HealthBar` adalah helper rendering.
- `GameFrame` dan `MainFrame` adalah bootstrap Swing.

Paket ini belum menggunakan `entities.Player`, `Enemy`, atau `BattleService`, sehingga secara arsitektur ia terpisah dari domain battle utama.

### `fantasyrpg.util`

- `ConsoleFormatter` adalah helper output console.
- Saat ini util ini relevan untuk CLI style flow, tetapi tidak dominan pada UI Swing utama.

## 3. Dependency direction

Secara ideal, arah dependensi proyek ini dibaca seperti berikut:

```text
UI -> Services -> Entities -> Interfaces
```

Implementasi saat ini mendekati arah tersebut pada jalur `core.Game -> ui.BattleGUI -> services -> entities`.

Namun pada jalur `ui.battle.*`, state battle dan logika aksi diletakkan langsung di panel UI, sehingga arah dependensinya menjadi lebih datar:

```text
ui.battle.GamePanel -> state + animation + battle calculation
```

## 4. Diagram arsitektur tekstual

```text
fantasyrpg.Main
  -> StartingScreenFrame
      -> WorldFrame
          -> ui.world.GamePanel
              -> TiledMapLoader
              -> ui.world.Player
              -> KeyboardInput
              -> EnemySpawnPoint / CollisionBlock
              -> transition ke ui.battle.GameFrame

fantasyrpg.core.Game
  -> ui.BattleGUI
      -> BattleService
          -> RandomEventService
          -> entities.Player
          -> Enemy subclasses
```

## 5. Alur scene yang benar-benar terjadi

Ada dua alur runtime yang penting dipahami.

### Jalur A: aplikasi utama

`fantasyrpg.Main` -> `StartingScreenFrame` -> `WorldFrame` -> `ui.world.GamePanel` -> `ui.battle.GameFrame`

Artinya, entry point utama proyek saat ini **tidak** mengarah ke `ui.BattleGUI`, melainkan ke battle prototype di `ui.battle`.

### Jalur B: battle GUI berbasis domain

`fantasyrpg.core.Game` -> `ui.BattleGUI`

Jalur ini justru memakai model OOP battle yang lebih matang.

## 6. Threading model

Proyek ini memakai dua model eksekusi utama:

- Swing Event Dispatch Thread untuk frame, panel, input event, dan `Timer`.
- Thread manual di `ui.world.GamePanel` untuk world loop 60 FPS.

Implikasinya:

- operasi render Swing tetap terjadi di ekosistem Swing,
- state world berubah pada thread game loop,
- transisi scene ke battle dilakukan lewat `SwingUtilities.invokeLater(...)`.

Untuk proyek belajar, pendekatan ini cukup masuk akal. Untuk standar produksi, sinkronisasi state lintas thread perlu diawasi lebih ketat jika kompleksitas game bertambah.

## 7. Resource loading

Ada dua pola pemuatan aset:

- Berbasis filesystem path, misalnya `new File("assets/background.png")`.
- Berbasis classpath resource, misalnya `GamePanel.class.getResource(...)` pada `ui.battle.GamePanel`.

Keduanya valid, tetapi mencampur dua strategi ini membuat packaging dan distribusi aplikasi menjadi lebih sulit jika nantinya proyek dibangun menjadi JAR.

## 8. Integrasi world dan battle

Integrasi world ke battle saat ini masih longgar:

- `ui.world.GamePanel` mengetahui posisi monster placeholder.
- Ketika pemain cukup dekat dan menekan `E`, panel world menutup window saat ini.
- Setelah itu world langsung membuka `fantasyrpg.ui.battle.GameFrame`.

Yang penting dicatat:

- `EnemySpawnPoint.enemyId` saat ini dipakai untuk styling marker.
- `EnemyFactory` belum dipakai untuk menentukan enemy nyata dari marker world.
- Transisi ke battle belum membawa data domain seperti tipe enemy hasil world encounter.

Secara desain, ini berarti koneksi "world encounter -> actual enemy domain" masih bisa diperdalam.

## 9. Kekuatan arsitektur saat ini

- Domain battle sudah dipisah cukup baik dari entity dan service.
- Abstract class dan interface digunakan pada lokasi yang tepat untuk pembelajaran OOP.
- `BattleService` membuat aksi turn-based bisa dipanggil dari UI tanpa menyalin logika utama.
- `TiledMapLoader` memisahkan parsing map dari panel render.

## 10. Technical debt dan observasi produksi

### Duplikasi entry point dan frame

- Ada `fantasyrpg.Main`, `fantasyrpg.core.Game`, `ui.world.Main`, `ui.world.WorldFrame`, `ui.world.GameFrame`, dan `ui.battle.MainFrame`.
- Secara produksi, terlalu banyak entry point akan membingungkan onboarding.

### Dua model battle

- `ui.BattleGUI` memakai domain `entities/services`.
- `ui.battle.GamePanel` menyimpan rule battle sendiri.

Ini membuat source of truth battle menjadi ganda.

### Dua kelas `Player`

- `fantasyrpg.entities.Player` untuk domain combat.
- `fantasyrpg.ui.world.Player` untuk sprite di map.

Ini sah secara package, tetapi perlu dokumentasi kuat karena mudah membingungkan pemula.

### `EnemyFactory` belum terintegrasi penuh

- Method `createStageEnemy(int stage, Random random)` tidak memakai parameter `random`.
- Factory belum dipakai oleh world encounter ataupun `BattleGUI`.

### Encapsulation tidak merata

- Domain entity memakai field private dan mutator terkontrol.
- Sebaliknya, beberapa class UI/world masih memakai field package-private seperti `player.x`, `player.y`, atau `CollisionBlock.rectangle`.

Secara akademik, ini bagus untuk diskusi perbedaan antara implementasi yang "cukup jalan" dan implementasi yang "rapi secara OOP".

## 11. Rekomendasi evolusi arsitektur

Jika proyek ini ingin dinaikkan menuju standar produksi yang lebih konsisten, langkah paling berdampak adalah:

1. Jadikan `BattleService` sebagai satu-satunya sumber rule battle.
2. Satukan jalur world encounter ke domain enemy nyata lewat `EnemyFactory`.
3. Kurangi jumlah entry point publik.
4. Konsistenkan strategi asset loading, idealnya lewat resource/classpath.
5. Bungkus state world penting dengan getter/setter atau value object agar encapsulation lebih kuat.
