# Laporan PPBO Berdasarkan Implementasi Saat Ini

Dokumen ini adalah versi terisi dari template analisis OOP untuk proyek `RPG-Java`. Isinya disusun berdasarkan source code saat ini sehingga bisa dipakai sebagai dasar laporan, presentasi, atau bahan diskusi kelas.

## 1. Desain Class

### `Character`

- Peran: abstract base class untuk semua entitas yang dapat bertarung.
- Atribut utama: `name`, `maxHp`, `hp`, `attackPower`, `defense`, `attackMultiplier`, `defenseMultiplier`, `defending`.
- Method utama: `receiveDamage(...)`, `heal(...)`, `defend()`, `restoreTurnModifiers()`, `attack(...)`, dan helper damage calculation.
- Nilai desain: menampung state dan perilaku umum agar tidak diduplikasi oleh semua turunan.

### `Player` pada `fantasyrpg.entities`

- Peran: representasi pemain pada sistem battle.
- Atribut utama: `level`, `experience`, `score`, `potionCount`, `fireballCharges`.
- Method utama: `attack(...)`, `useSkill(...)`, `usePotion()`, `gainExperience(...)`, `addScore(...)`.
- Nilai desain: menambahkan perilaku yang hanya relevan untuk pemain, seperti level-up, potion, dan skill charge.

### `Enemy`

- Peran: abstract class untuk musuh.
- Atribut utama: `rewardExperience`, `rewardScore`.
- Method utama: getter reward.
- Nilai desain: mengelompokkan properti reward yang tidak dibutuhkan oleh `Player`.

### `DragonBoss`

- Peran: boss battle dengan HP, attack, dan skill lebih tinggi.
- Atribut utama: mewarisi seluruh atribut `Enemy` dan `Character`.
- Method utama: override `attack(...)`, implementasi `useSkill(...)`.
- Nilai desain: menjadi contoh enemy kuat yang juga memenuhi kontrak `SkillUser`.

### `BattleService`

- Peran: mengorkestrasi jalannya battle.
- Atribut utama: `randomEventService`.
- Method utama: `beginRound(...)`, `executePlayerAction(...)`, `executeEnemyTurn(...)`, `applyVictoryRewards(...)`, `startBattle(...)`.
- Nilai desain: memisahkan aturan permainan dari tampilan UI.

### `RandomEventService`

- Peran: memicu event acak setiap ronde.
- Atribut utama: `random`.
- Method utama: `triggerRoundEvent(...)`.
- Nilai desain: memusatkan variasi ronde di satu service agar battle tidak monoton.

## 2. Implementasi OOP

### Encapsulation

- Dipakai pada: `Character`, `Player`, `Enemy`, `EnemySpawnPoint`.
- Penjelasan:
  - field penting dibuat `private`,
  - validasi state dilakukan lewat setter,
  - proses seperti `levelUp()` disembunyikan sebagai detail internal,
  - `EnemySpawnPoint` dibuat immutable.
- Catatan kritis:
  - encapsulation belum merata pada `ui.world.Player`, `CollisionBlock`, dan `TiledMapLoader`.

### Inheritance

- Struktur:

```text
Character
  -> Player
  -> Enemy
       -> Goblin
       -> OrcWarrior
       -> DragonBoss
```

- Penjelasan:
  - `Character` menyimpan karakteristik umum semua aktor tempur.
  - `Enemy` menambahkan reward.
  - subclass enemy mengkhususkan stat dan perilaku serangan.

### Polymorphism

- Method terkait:
  - `attack(Character target)`
  - `useSkill(Character target)`
- Penjelasan:
  - `BattleService` dapat memanggil `enemy.attack(player)` tanpa mengetahui detail apakah musuh itu `Goblin`, `OrcWarrior`, atau `DragonBoss`.
  - `BattleService.executeEnemyTurn(...)` juga memanfaatkan `SkillUser` untuk memanggil skill hanya pada object yang memang memiliki kontrak itu.

### Overloading

- Contoh:
  - `attack(Character target)`
  - `attack(Character target, int bonusDamage)`
  - `attack(Character target, String skillName)`
- Penjelasan:
  - nama method sama tetapi signature berbeda,
  - memudahkan perluasan mode serangan tanpa harus mengganti nama method inti.

### Overriding

- Contoh:
  - `Player.attack(...)`
  - `Goblin.attack(...)`
  - `OrcWarrior.attack(...)`
  - `DragonBoss.attack(...)`
- Penjelasan:
  - tiap class memberikan formula damage yang berbeda,
  - sistem battle tetap konsisten karena semua memakai kontrak method yang sama.

### Abstract Class

- Class: `Character`, `Enemy`
- Alasan:
  - keduanya mewakili konsep umum,
  - object konkret yang benar-benar dipakai game adalah turunan mereka.

### Interface

- Interface:
  - `Attackable`
  - `SkillUser`
- Implementor:
  - `Character` mengimplementasikan `Attackable`
  - `Player`, `Goblin`, `DragonBoss` mengimplementasikan `SkillUser`
- Alasan:
  - interface memisahkan kontrak perilaku dari pewarisan state,
  - tidak semua karakter dipaksa punya skill.

## 3. Analisis

### Mengapa OOP cocok untuk game ini?

Game RPG secara alami terdiri dari banyak objek yang punya state dan perilaku berbeda:

- player,
- musuh,
- battle service,
- world scene,
- input handler,
- map loader.

OOP cocok karena:

- setiap objek bisa memegang tanggung jawabnya sendiri,
- hierarki karakter mudah dimodelkan dengan inheritance,
- perilaku dinamis seperti skill dan attack mudah dimodelkan dengan polymorphism.

### Keuntungan inheritance dalam desain game

- stat umum tidak perlu diulang di semua musuh,
- penambahan jenis musuh baru menjadi lebih cepat,
- service battle cukup bekerja dengan tipe parent,
- balancing dapat dilakukan per subclass tanpa mengubah struktur dasar.

### Bagaimana polymorphism mempermudah pengembangan fitur?

- battle service tidak perlu `if` besar untuk semua rumus attack,
- cukup tambahkan subclass baru yang override `attack(...)`,
- interface `SkillUser` membuat skill menjadi kemampuan opsional yang fleksibel.

### Risiko jika tidak menggunakan encapsulation

Jika field seperti HP, defense, dan attack dibuka bebas:

- state karakter bisa menjadi tidak valid,
- bug balancing lebih mudah terjadi,
- logic penting menyebar ke banyak tempat,
- perubahan aturan game akan lebih sulit dilacak.

### Pengembangan lanjutan

Beberapa arah pengembangan yang logis:

1. Gunakan `EnemyFactory` sebagai penghubung resmi antara world encounter dan domain enemy.
2. Satukan battle prototype `ui.battle` dengan `BattleService`.
3. Tingkatkan encapsulation pada package world.
4. Pisahkan class UI besar menjadi komponen yang lebih kecil.
5. Tambahkan test untuk service dan entity domain.

## 4. Lampiran yang disarankan

- Screenshot start screen
- Screenshot world map
- Screenshot battle GUI
- UML class diagram domain battle
- Diagram alur `Main -> World -> Battle`
- Contoh log battle atau output aksi ronde

