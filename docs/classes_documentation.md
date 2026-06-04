# Fantasy RPG - Java OOP (PBO) Project Documentation

Proyek ini adalah game RPG (Role-Playing Game) 2D berbasis GUI Java Swing yang dirancang dengan menerapkan prinsip-prinsip Pemrograman Berorientasi Objek (PBO) secara kuat. Game ini memiliki fitur eksplorasi dunia atas (top-down world map) menggunakan peta Tiled (TMX), sistem pertarungan berbasis giliran (turn-based battle), sound engine MIDI dinamis, dan sistem level-up berbasis progres.

---

## 1. Struktur Package & Arsitektur Proyek

Proyek ini terbagi ke dalam beberapa package utama untuk memisahkan tanggung jawab (Separation of Concerns):

- **`fantasyrpg`**: Entry point aplikasi dan manajemen state global permainan.
- **`fantasyrpg.entities`**: Model karakter (Hero/Player dan berbagai jenis Musuh/Enemy) menggunakan prinsip pewarisan (inheritance) dan polimorfisme.
- **`fantasyrpg.interfaces`**: Kontrak interface untuk aksi/kemampuan karakter (`Attackable` dan `SkillUser`).
- **`fantasyrpg.services`**: Logika bisnis game, seperti alur pertarungan, pembuatan musuh, dan kejadian acak (random events).
- **`fantasyrpg.sound`**: Pengendali audio terpusat untuk musik latar (BGM MIDI/WAV) dan efek suara (SFX).
- **`fantasyrpg.ui`**: Komponen antarmuka pengguna grafis (GUI) yang terbagi menjadi:
  - `start`: Layar awal/mulai menu utama.
  - `world`: Layar penjelajahan dunia 2D top-down dengan peta berubin (Tiled Map).
  - `battle`: Layar mesin pertarungan (battle engine) berbasis giliran.
- **`fantasyrpg.util`**: Utilitas pembantu untuk memperindah output konsol.

---

## 2. Dokumentasi Kelas, Field, dan Method Secara Detil

### ── Package `fantasyrpg` ──

#### A. Class `GameState`
Menyimpan state global permainan untuk mempertahankan koordinat posisi player, peta yang sedang aktif, dan melacak daftar musuh yang sudah dikalahkan saat bertransisi antar-layar pertarungan dan eksplorasi.

* **Fields:**
  - `public static String currentMapPath`: Path file TMX peta aktif saat ini.
  - `public static int playerX`, `playerY`: Koordinat grid player di world map.
  - `public static boolean map1Enemy1Defeated` s.d. `map2Enemy3Defeated`: Status kekalahan masing-masing musuh di peta 1 & 2.
  - `public static int currentEnemyIndex`: Indeks pengenal musuh yang sedang dihadapi.
* **Methods:**
  - `public static void reset()`: Mereset seluruh status game ke kondisi awal baru.

#### B. Class `Main`
Berfungsi sebagai entry point utama aplikasi.
* **Methods:**
  - `public static void main(String[] args)`: Memulai aplikasi dengan memanggil dan menampilkan `StartingScreenFrame`.

---

### ── Package `fantasyrpg.interfaces` ──

#### A. Interface `Attackable`
Kontrak dasar untuk objek yang dapat menerima serangan dalam pertarungan.
* **Methods:**
  - `void receiveDamage(int damage)`: Menghitung damage masuk setelah dikurangi pertahanan.
  - `boolean isAlive()`: Memeriksa apakah sisa HP lebih besar dari 0.

#### B. Interface `SkillUser`
Kontrak untuk karakter yang memiliki kemampuan menggunakan keahlian khusus/sakti (skill).
* **Methods:**
  - `int useSkill(Character target)`: Mengeksekusi skill khusus ke target dan mengembalikan jumlah damage yang dihasilkan.

---

### ── Package `fantasyrpg.entities` ──

#### A. Class `Character` (Abstract Class - Implements `Attackable`)
Merupakan superclass dari semua makhluk hidup di dalam game (Player dan Enemy). Kelas ini mengatur properti dasar pertarungan seperti HP, Attack, Defense, dan pengali status (multipliers).

* **Fields:**
  - `private final String name`: Nama karakter.
  - `private final int maxHp`: Batas HP maksimal karakter.
  - `private int hp`: Sisa HP saat ini.
  - `private int attackPower`: Kekuatan serangan dasar.
  - `private int defense`: Tingkat pertahanan dasar.
  - `private double attackMultiplier`: Pengali serangan (default 1.0).
  - `private double defenseMultiplier`: Pengali pertahanan (default 1.0).
  - `private boolean defending`: Status apakah karakter sedang berada dalam posisi bertahan.
* **Methods:**
  - Getter & Setter untuk seluruh properti (misal: `getName()`, `getHp()`, `setHp()`, dll).
  - `public void heal(int amount)`: Menambah HP karakter tanpa melebihi `maxHp`.
  - `public void restoreTurnModifiers()`: Mengembalikan multiplier serangan/pertahanan ke default `1.0` dan menonaktifkan status bertahan (`defending = false`) di akhir giliran.
  - `public void defend()`: Mengaktifkan status bertahan dan meningkatkan multiplier pertahanan ke `1.5`.
  - `public void stopDefending()`: Menonaktifkan status bertahan.
  - `protected int calculateFinalDamage(int rawDamage)`: Menghitung damage bersih setelah dikurangi dengan kalkulasi defense dikalikan defense multiplier.
  - `protected int calculateBaseAttack()`: Menghitung kekuatan serangan dasar yang dipengaruhi pengali serangan.
  - `public void receiveDamage(int damage)`: Mengurangi HP berdasarkan damage bersih. Jika sedang `defending`, damage akhir dipotong lagi sebesar 45% (hanya menerima 55% damage).
  - `public boolean isAlive()`: Mengembalikan `true` jika HP > 0.
  - `public abstract int attack(Character target)`: Menyerang target (diimplementasikan oleh subclass).

#### B. Class `Player` (Extends `Character` - Implements `SkillUser`)
Representasi dari karakter pahlawan (Hero) yang dikendalikan oleh pemain. Memiliki fitur tambahan seperti level-up, penggunaan ramuan (potion), muatan energi sihir (fireball charges), dan waktu tunggu skill (cooldown).

* **Fields:**
  - `private int maxFireballCharges`: Jumlah maksimal penggunaan skill fireball.
  - `private int fireballCharges`: Sisa penggunaan skill fireball yang tersedia saat ini.
  - `private int skillCooldown`: Sisa giliran waktu tunggu penggunaan skill.
  - `private int potionCount`: Jumlah ramuan penyembuh yang dimiliki.
  - `private int potionCooldown`: Sisa giliran waktu tunggu penggunaan ramuan.
  - `private int level`: Level pahlawan saat ini.
  - `private int experience`: Akumulasi EXP player.
  - `private int score`: Skor pertempuran player.
* **Methods:**
  - Getter & Setter status level, EXP, skor, potion, cooldown, dll.
  - `public void tickCooldowns()`: Mengurangi sisa giliran cooldown skill dan potion sebanyak 1 setiap pergantian ronde pertempuran.
  - `public void resetFireballCharges()`: Mengisi ulang sisa fireball ke jumlah maksimal.
  - `public boolean canUseFireball()`: Memeriksa kelayakan penggunaan skill (sisa charge > 0 dan cooldown = 0).
  - `public boolean canUsePotion()`: Memeriksa kelayakan penggunaan potion (jumlah > 0 dan cooldown = 0).
  - `public boolean usePotion()`: Memulihkan HP sebesar `22 + (level * 4)` dan memicu cooldown ramuan selama 3 giliran.
  - `public void gainExperience(int amount)`: Menambah EXP player dan memicu `levelUp()` jika EXP memenuhi batas syarat naik level.
  - `private void levelUp()`: Menaikkan level player, meningkatkan Attack Power sebesar +3, Defense +1, dan memulihkan HP sebesar +20.
  - `private int requiredExperience()`: Menghitung batas EXP naik level berikutnya (`level * 50`).
  - `public int attack(Character target)`: Melancarkan serangan biasa dengan tambahan variasi damage acak dan bonus level.
  - `public int useSkill(Character target)`: Melancarkan serangan sihir Fireball berdamage tinggi dan memicu cooldown skill.

#### C. Class `Enemy` (Abstract Class - Extends `Character`)
Superclass dari semua jenis monster di dalam game. Menyimpan properti hadiah pertarungan jika monster tersebut dikalahkan.

* **Fields:**
  - `private final int rewardExperience`: Jumlah EXP hadiah jika dikalahkan.
  - `private final int rewardScore`: Jumlah skor hadiah jika dikalahkan.
* **Methods:**
  - `getRewardExperience()` dan `getRewardScore()`: Mengambil nilai hadiah.

#### D. Subclass Enemy Khusus:
- **`Slime`**: Musuh tingkat dasar di Map 1. Memiliki skill khusus `Acid Surge` berdamage tinggi yang mengimplementasikan `SkillUser`.
- **`Golem`**: Musuh tangguh berlapis batu di Map 1. Memiliki skill khusus `Boulder Smash` berdamage masif yang mengimplementasikan `SkillUser`.
- **`Goblin`**: Musuh tingkat menengah di Map 2. Memiliki skill khusus `Goblin Rampage` yang mengimplementasikan `SkillUser`.
- **`OrcWarrior`**: Musuh dengan kekuatan fisik murni tanpa memiliki keahlian sihir/skill (tidak mengimplementasikan `SkillUser`).
- **`DragonBoss`**: Naga Azhrax sebagai bos akhir di Map 2. Mengimplementasikan `SkillUser` dengan skill mematikan `Dragon Breath`.

---

### ── Package `fantasyrpg.services` ──

#### A. Class `BattleService`
Mengelola logika alur pertarungan teks konsol (turn-based battle flow) dan menyediakan method pembantu untuk diintegrasikan pada GUI Battle.

* **Fields:**
  - `private final RandomEventService randomEventService`: Layanan untuk memicu kejadian acak pada pertarungan.
* **Methods:**
  - `public boolean startBattle(Player player, Enemy enemy, Scanner scanner)`: Memulai simulasi pertarungan berbasis konsol.
  - `public String beginRound(Player player, Enemy enemy)`: Memulai ronde baru, mereset modifier, memproses cooldown, serta mengembalikan string event acak.
  - `public ActionResult executePlayerAction(Player player, Enemy enemy, PlayerAction action)`: Memproses aksi pertarungan yang dipilih player.
  - `public ActionResult executeEnemyTurn(Player player, Enemy enemy)`: Menghitung kecerdasan buatan (AI) musuh untuk memilih menyerang biasa atau memakai skill khusus.
  - `public ActionResult applyVictoryRewards(Player player, Enemy enemy)`: Memberikan EXP dan skor setelah memenangkan pertarungan.

#### B. Class `EnemyFactory`
Implementasi dari creational design pattern Factory Method untuk mempermudah instansiasi monster musuh berdasarkan stage permainan.
* **Methods:**
  - `public Enemy createStageEnemy(int stage, Random random)`: Mengembalikkan objek subclass Enemy yang sesuai berdasarkan parameter nomor stage.

#### C. Class `RandomEventService`
Menyediakan kejutan dan efek status dinamis yang berubah-ubah secara acak di setiap awal ronde pertarungan untuk membuat alur permainan lebih bervariasi.
* **Methods:**
  - `public String triggerRoundEvent(Player player, Character enemy)`: Menggulung angka acak untuk memicu event khusus (seperti peningkatan attack, defense, pemulihan HP, dll) atau tidak memicu apa pun.

---

### ── Package `fantasyrpg.sound` ──

#### A. Class `SoundManager`
Mesin audio game yang canggih. Dapat memutar file audio eksternal berformat `.wav` dari folder `assets/sounds/` secara dinamis. Apabila file suara tidak ditemukan, sound manager akan memutar komposisi melodi aransemen instrumen MIDI secara prosedural melalui Java Midi System.

* **Fields:**
  - `private static Sequencer sequencer`: Pengendali urutan MIDI.
  - `private static Synthesizer synthesizer`: Synthesizer instrumen musik MIDI.
  - `private static MidiChannel[] channels`: Daftar channel MIDI (mengatur nomor instrumen seperti Piano, Harp, Strings, Trumpet, Drums).
  - `private static Thread bgmThread`: Thread khusus untuk memainkan perulangan lagu latar secara non-blocking.
  - `private static boolean bgmRunning`: Status apakah lagu latar sedang berjalan.
  - `private static String currentBgm`: Penanda tipe lagu latar aktif saat ini (misal: `"START"`, `"WORLD"`, `"BATTLE"`, `"DUNGEON"`, `"ENDING"`).
* **Methods:**
  - `public static synchronized void playBGM(String type)`: Memulai lagu latar berdasarkan tipe tema. Jika file wav bersangkutan tidak ada, akan otomatis menyusun melodi MIDI secara langsung dan melingkarnya di dalam thread latar belakang.
  - `public static synchronized void stopBGM()`: Menghentikan putaran lagu latar dan membungkam suara instrumen.
  - `public static synchronized void playSFX(String name)`: Memainkan efek suara pendek (misal: `"HIT"`, `"DEFEND"`, `"VICTORY"`) secara asinkron.

---

### ── Package `fantasyrpg.ui.start` ──

#### A. Class `StartingScreenFrame` (Extends `JFrame`)
Menampilkan jendela menu utama sebelum permainan dimulai. Memiliki tombol panel untuk memulai petualangan baru, membuka konfigurasi, informasi pembuat game, dan tombol keluar.

---

### ── Package `fantasyrpg.ui.world` ──

#### A. Class `WorldFrame` (Extends `JFrame`)
Jendela kontainer GUI yang membungkus komponen peta dunia eksplorasi 2D.

#### B. Class `GamePanel` (Extends `JPanel` - Implements `Runnable`)
Jantung utama visualisasi eksplorasi. Menggambar peta berubin (tile grid) lapis demi lapis, karakter pahlawan, rintangan tabrakan, dan memperbarui logika perulangan game (game loop) dengan kecepatan 60 bingkai per detik (FPS).

* **Fields:**
  - `private Thread gameThread`: Thread utama pemutar siklus game loop.
  - `private final KeyboardInput keyHandler`: Pengendali tombol keyboard terikat.
  - `private final TiledMapLoader mapLoader`: Pembuat data visual peta dari file TMX.
  - `private final Player player`: Objek visual pahlawan penjelajah.
  - `private final List<CollisionBlock> collisions`: Kumpulan blok dinding penghalang.
  - `private final List<EnemySpawnPoint> spawnPoints`: Posisi monster musuh di peta.
* **Methods:**
  - `public void startThread()`: Menginisialisasi dan memulai thread game loop.
  - `public void run()`: Siklus game loop utama yang terus berjalan memperbarui data koordinat dan menggambar ulang panel secara konsisten.
  - `public void update()`: Memperbarui pergerakan pahlawan, mendeteksi peristiwa tabrakan dinding, memicu pertarungan saat bersinggungan dengan titik spawn musuh, dan menangani transisi peta.
  - `public void paintComponent(Graphics g)`: Menggambar urutan layer peta bawah, sprite karakter, objek musuh, layer atas (atap/pepohonan), serta transisi efek hitam saat memudar (fade).

#### C. Class `Player`
Merepresentasikan model grafis 2D pahlawan di world map. Mengontrol arah hadap sprite, urutan animasi langkah kaki, dan koordinat grid berjalan.

#### D. Class `TiledMapLoader`
Menganalisis file XML TMX secara terprogram, mengekstrak id petak ubin, memuat berkas gambar tileset (kumpulan aset ubin gambar), dan memetakan struktur visual peta eksplorasi.

#### E. Class `CollisionBlock`
Menyimpan area kotak pembatas (`Rectangle`) untuk mendeteksi apakah pahlawan sedang berjalan menembus rintangan seperti air, pohon besar, atau dinding batu.

#### F. Class `EnemySpawnPoint`
Menyimpan lokasi koordinat kemunculan musuh serta jenis monster yang menempati lokasi tersebut. Jika pahlawan bersinggungan dengan area spawn, game akan bertransisi ke layar pertarungan.

#### G. Class `KeyboardInput` (Implements `KeyListener`)
Mengubah masukan tombol fisik keyboard (W, A, S, D atau tombol panah arah) menjadi boolean arah pergerakan pahlawan di peta.

---

### ── Package `fantasyrpg.ui.battle` ──

#### A. Class `GameFrame` (Extends `JFrame`)
Jendela GUI kontainer yang membungkus antarmuka pertarungan.

#### B. Class `GamePanel` (Extends `JPanel` - Implements `ActionListener`)
Mesin utama perenderan visual pertarungan turn-based. Menampilkan animasi pukulan, hit flash merah, efek goyangan layar (screen shake), aura pertahanan biru, angka damage melayang, status pahlawan dinamis, dan overlay end screen kemenangan penuh.

* **Fields:**
  - `private int heroHp`, `enemyHp`: Sisa HP runtime petarung.
  - `private int skillUsesLeft`, `maxSkillUses`: Penggunaan maksimal skill fireball pahlawan yang disesuaikan progres peta.
  - `private fantasyrpg.entities.Player playerEntity`: Model PBO pahlawan.
  - `private fantasyrpg.entities.Enemy enemyEntity`: Model PBO musuh aktif.
  - `private final List<FloatingText> floatingTexts`: Kumpulan angka damage yang melayang naik ke atas.
  - `private final Queue<String> effectQueue`: Antrean urutan animasi efek pertarungan.
  - `private final Timer effectTimer`: Timer Swing pemicu perputaran animasi efek visual.
  - `private BufferedImage endScreenImg`: Gambar visual penutup kemenangan penuh game.
* **Methods:**
  - `private void initBattle()`: Menginisialisasi model PBO player dan musuh, menyelaraskan HP awal, menetapkan jumlah charges fireball, dan mengkalkulasi level pahlawan secara adaptif berdasarkan riwayat musuh yang dikalahkan di `GameState`.
  - `private void drawCharacters(Graphics2D g)`: Menggambar bayangan karakter di lantai, sprite pahlawan, sprite musuh, efek aura defensif, kilatan slash tebasan, ledakan sihir, serta lampu HP kritis yang berkedip.
  - `private void handleAction(String action)`: Merespons klik tombol pertarungan (Attack, Skill, Defend, Potion, Run).
  - `private void doAttack()`, `doSkill()`, `doDefend()`: Melaksanakan kalkulasi formula damage OOP pahlawan terhadap musuh dan sebaliknya.
  - `private void enemyTurnIfAlive()`: Menjalankan logika keputusan kecerdasan buatan musuh.
  - `private void handleEnemyDefeat()`: Menghentikan pertarungan, memutar lagu `"ENDING"`, menampilkan overlay end screen kemenangan penuh game selama 5 detik sebelum otomatis keluar secara bersih dengan `System.exit(0)`.

#### C. Class `ActionPanel` (Extends `JPanel`)
Menampilkan tombol aksi interaktif pertarungan (Attack, Skill, Defend, Potion, Run) yang dapat dipilih pemain melalui mouse atau jalan pintas tombol keyboard (J, K, L, I, U).

#### D. Class `HealthBar`
Utilitas visual rendering yang menggambar batang progresif HP (warna hijau/kuning/merah dinamis) dan MP/Skill (warna biru) secara proporsional.

---

### ── Package `fantasyrpg.util` ──

#### A. Class `ConsoleFormatter`
Menyediakan string kode format warna teks ANSI di terminal konsol untuk mempermudah pelacakan log kesalahan atau debugging.
