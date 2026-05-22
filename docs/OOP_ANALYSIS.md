# Analisis OOP

Dokumen ini membahas penerapan OOP pada proyek berdasarkan implementasi nyata, bukan berdasarkan desain ideal semata. Dengan begitu, pembaca bisa melihat bagaimana konsep OOP dipakai dalam kode yang benar-benar berjalan.

## 1. Abstraksi desain domain

Pada proyek ini, battle domain dibangun dari ide sederhana:

- semua aktor tempur adalah `Character`,
- sebagian `Character` adalah `Player`,
- sebagian `Character` lain adalah `Enemy`,
- sebagian actor memiliki skill, sehingga mengimplementasikan `SkillUser`.

Representasi strukturnya:

```text
Attackable (interface)
  ^
  |
Character (abstract)
  ^
  |--------------------\
  |                     \
Player                  Enemy (abstract)
implements SkillUser      ^
                           |-------------------\
                           |         |          |
                        Goblin   OrcWarrior  DragonBoss
                        implements SkillUser  implements SkillUser

SkillUser (interface)
```

Struktur ini sangat baik untuk mata kuliah OOP karena memisahkan:

- apa yang dimiliki semua karakter,
- apa yang hanya dimiliki pemain,
- apa yang hanya dimiliki musuh,
- apa yang bersifat opsional seperti skill.

## 2. Encapsulation

Encapsulation berarti data internal objek dilindungi dan hanya diubah lewat interface yang terkontrol.

### Contoh penerapan yang baik

Pada `Character`:

- `name`, `hp`, `attackPower`, `defense`, dan multiplier dibuat `private`.
- perubahan `hp` dilakukan lewat `setHp(...)` yang otomatis melakukan clamping antara `0` dan `maxHp`.
- perubahan `attackPower` dan `defense` dibatasi lewat setter `protected`.

Manfaatnya:

- objek tidak mudah masuk ke state tidak valid,
- subclass tetap bisa berevolusi,
- caller dari luar tidak bisa sembarang menulis nilai rusak.

Contoh lain:

- `Player.levelUp()` dibuat `private`, sehingga proses level-up tidak bisa dipicu sembarang dari luar.
- `Enemy.rewardExperience` dan `rewardScore` dibuat `final`, sehingga reward musuh stabil setelah object dibuat.
- `EnemySpawnPoint` bersifat immutable karena field final dan hanya punya getter.

### Area yang encapsulation-nya masih lemah

Tidak semua paket konsisten.

Contoh:

- `ui.world.Player.x` dan `y` tidak private.
- `CollisionBlock.rectangle` juga tidak private.
- `TiledMapLoader` mengekspos banyak field publik yang bisa diubah langsung oleh object lain.

Ini bukan berarti kodenya salah total, tetapi menunjukkan bahwa encapsulation paling kuat ada di domain battle, sedangkan di world/UI masih lebih longgar.

### Kesimpulan encapsulation

Untuk presentasi akademik, kita bisa menyimpulkan:

- pilar encapsulation **sudah diterapkan dengan baik pada layer domain**,
- tetapi **belum merata pada layer UI dan loader**.

Kesimpulan seperti ini lebih jujur dan lebih kuat daripada sekadar berkata "encapsulation sudah ada di semua class".

## 3. Inheritance

Inheritance dipakai untuk menurunkan atribut dan perilaku umum ke kelas turunan.

### Rantai inheritance utama

```text
Character
  -> Player
  -> Enemy
       -> Goblin
       -> OrcWarrior
       -> DragonBoss
```

### Mengapa inheritance cocok di sini

Semua karakter tempur punya kesamaan:

- nama,
- HP,
- attack power,
- defense,
- kemampuan menerima damage,
- status hidup/mati,
- kemampuan defend.

Daripada mengulang atribut dan method ini di semua class, proyek memusatkannya di `Character`.

Lalu `Enemy` memperluas `Character` dengan konsep reward:

- EXP reward,
- score reward.

Ini membuat subclass `Goblin`, `OrcWarrior`, dan `DragonBoss` hanya perlu fokus pada:

- stat awal,
- formula serangan,
- skill khusus bila ada.

### Keuntungan teknis

- kode tidak duplikatif,
- perilaku dasar konsisten,
- penambahan enemy baru lebih mudah,
- service battle dapat bekerja dengan tipe parent `Enemy` atau `Character`.

## 4. Polymorphism

Polymorphism adalah kemampuan memperlakukan object berbeda melalui tipe umum yang sama, lalu membiarkan implementasi konkret berjalan sesuai class aslinya.

### Polymorphism lewat overriding `attack(...)`

`Character` mendeklarasikan:

```text
public abstract int attack(Character target);
```

Lalu tiap subclass memberi implementasi sendiri:

- `Player.attack(...)`
- `Goblin.attack(...)`
- `OrcWarrior.attack(...)`
- `DragonBoss.attack(...)`

Akibatnya, `BattleService` tidak perlu tahu rumus tiap musuh. Ia cukup memanggil:

```text
enemy.attack(player)
```

Method yang benar akan dipilih berdasarkan object nyata yang sedang aktif.

### Polymorphism lewat interface `SkillUser`

Dalam `BattleService.executeEnemyTurn(...)`, ada pola:

```text
if (enemy instanceof SkillUser skillUser && enemy.getHp() <= enemy.getMaxHp() / 2) {
    damage = skillUser.useSkill(player);
}
```

Maknanya:

- service tidak mengharuskan semua enemy punya skill,
- service hanya memanggil skill bila object tersebut memang memenuhi kontrak `SkillUser`.

Ini adalah contoh penting polymorphism berbasis interface.

### Polymorphism pada UI

`BattleGUI` menyimpan enemy aktif sebagai:

```text
private Enemy currentEnemy;
```

Tetapi object nyatanya bisa berubah menjadi:

- `DragonBoss`,
- `Goblin`,
- atau subclass lain di masa depan.

UI tetap bekerja karena yang diandalkan adalah antarmuka perilaku umum enemy, bukan tipe konkret tertentu.

## 5. Abstraction

Abstraction berarti hanya menampilkan konsep penting dan menyembunyikan detail yang tidak perlu bagi pemakai object.

### Abstract class `Character`

`Character` adalah abstraksi untuk "sesuatu yang bisa bertarung".

Class ini tidak mewakili aktor spesifik, sehingga tepat bila dibuat abstract.

Ia menyediakan:

- state umum,
- helper calculation,
- kontrak `attack(...)` yang wajib diisi subclass.

### Abstract class `Enemy`

`Enemy` juga abstract karena "enemy" masih terlalu umum.

Yang dibutuhkan game adalah musuh konkret seperti:

- `Goblin`,
- `OrcWarrior`,
- `DragonBoss`.

### Interface sebagai abstraksi perilaku

- `Attackable` mengabstraksikan kemampuan diserang.
- `SkillUser` mengabstraksikan kemampuan mengeluarkan skill.

Ini membuat desain lebih fleksibel daripada menaruh semua perilaku di satu pohon inheritance besar.

## 6. Overriding

Overriding terjadi ketika subclass mengganti implementasi method dari parent class.

Contoh utama pada proyek ini:

- `Player.attack(...)`
- `Goblin.attack(...)`
- `OrcWarrior.attack(...)`
- `DragonBoss.attack(...)`

Setiap implementasi punya formula damage berbeda:

- player punya variance acak dan scaling level,
- goblin menambah fixed damage,
- orc punya formula sendiri,
- dragon boss memberi tekanan damage lebih tinggi.

Inilah yang membuat battle terasa berbeda antar entitas meskipun semua berbagi method bernama sama.

## 7. Overloading

Overloading berarti satu method name dipakai dalam beberapa signature berbeda.

Pada `Character`, terdapat tiga variasi `attack`:

- `attack(Character target)`
- `attack(Character target, int bonusDamage)`
- `attack(Character target, String skillName)`

Tujuannya:

- signature dasar dipakai untuk serangan normal,
- signature dengan `int` dipakai untuk bonus damage eksplisit,
- signature dengan `String` dipakai untuk mode serangan khusus seperti `"critical"`.

Untuk kebutuhan akademik, ini adalah contoh yang jelas dan mudah dipresentasikan karena semua overload berada dalam satu konteks semantik yang sama, yaitu serangan.

## 8. Interface segregation yang sederhana

Desain proyek ini cukup baik karena tidak semua karakter dipaksa punya method skill.

Jika semua skill diletakkan di `Character`, maka:

- `OrcWarrior` akan mewarisi method yang tidak relevan,
- battle service harus selalu menganggap semua enemy bisa skill.

Dengan memisahkan `SkillUser`, desain menjadi lebih bersih:

- yang punya skill mengimplementasikan interface,
- yang tidak punya skill tetap sederhana.

## 9. Composition dan orchestration

OOP pada proyek ini tidak hanya soal inheritance.

Composition juga terlihat jelas:

- `BattleService` memiliki `RandomEventService`.
- `BattleGUI` memiliki `BattleService`.
- `WorldFrame` memiliki `ui.world.GamePanel`.
- `ui.world.GamePanel` memiliki `KeyboardInput`, `TiledMapLoader`, dan `ui.world.Player`.

Ini penting karena dalam desain produksi, composition sering lebih fleksibel daripada inheritance.

## 10. Di mana OOP proyek ini sudah kuat

- Hierarki `Character -> Enemy/Player` sangat natural untuk game RPG.
- Interface `SkillUser` dipakai tepat untuk memodelkan kemampuan opsional.
- `BattleService` memisahkan rule battle dari detail render UI.
- Helper method di `Character` seperti `calculateBaseAttack()` dan `calculateFinalDamage()` menunjukkan reuse yang baik.

## 11. Di mana OOP proyek ini masih bisa ditingkatkan

### Domain dan UI battle masih ganda

- `ui.BattleGUI` sudah memakai entity/service domain.
- `ui.battle.GamePanel` masih menyimpan logic battle sendiri.

Akibatnya, ada dua implementasi "kebenaran" battle.

### Encapsulation layer world masih longgar

- field posisi dan collision belum dibungkus rapi.

### Factory belum menjadi pusat penciptaan enemy

- `EnemyFactory` ada, tetapi belum menjadi penghubung utama antara world encounter dan battle.

### Beberapa class UI terlalu besar

- `BattleGUI` dan `ui.world.GamePanel` sudah memegang banyak tanggung jawab sekaligus.

Untuk produksi, class besar seperti ini biasanya dipecah ke input controller, scene state, renderer, dan transition handler.

## 12. Ringkasan untuk kebutuhan presentasi mata kuliah

Jika harus menjelaskan proyek ini dalam presentasi OOP, narasi yang paling kuat adalah:

1. `Character` menjadi abstraksi inti dari semua aktor tempur.
2. `Player` dan `Enemy` mewarisi state umum dari `Character`.
3. Musuh konkret melakukan overriding `attack(...)` untuk menghasilkan perilaku yang berbeda-beda.
4. `SkillUser` menunjukkan polymorphism berbasis interface karena tidak semua karakter wajib punya skill.
5. `BattleService` memperlihatkan bagaimana object-object OOP tersebut dikoordinasikan untuk membentuk battle flow.
6. Layer world dan UI memberi contoh bahwa OOP tidak hanya dipakai untuk domain, tetapi juga untuk membagi tanggung jawab sistem.

Itulah alasan proyek ini cukup baik dijadikan studi kasus OOP: ia bukan sekadar contoh class kecil, tetapi sudah memperlihatkan bagaimana object saling bekerja sama dalam satu aplikasi game yang utuh.
