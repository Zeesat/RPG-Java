# Alur Runtime

## 1. Startup aplikasi utama

Entry point default proyek adalah `fantasyrpg.Main`.

Alur sederhananya:

```text
fantasyrpg.Main.main()
  -> StartingScreenFrame.showScreen()
  -> pengguna menekan tombol
  -> WorldFrame dibuat
  -> ui.world.GamePanel mulai game loop
```

Pada jalur ini, pemain masuk ke dunia eksplorasi lebih dulu, bukan langsung ke battle domain.

## 2. Startup alternatif untuk battle domain

Jika aplikasi dijalankan dari `fantasyrpg.core.Game`, alurnya berbeda:

```text
Game.main()
  -> new Game().start()
  -> SwingUtilities.invokeLater(...)
  -> new entities.Player("Arin")
  -> JFrame + new BattleGUI(player)
```

Jalur ini berguna untuk menguji battle logic yang sudah terhubung ke `BattleService`.

## 3. Flow start screen

`StartingScreenFrame` memiliki panel internal `StartingScreenPanel`.

Urutannya:

1. Frame dibuat.
2. Panel di-set sebagai content pane.
3. Panel meminta fokus keyboard ketika `addNotify()` terpanggil.
4. Saat user menekan key apa pun, callback `onContinue` dijalankan.
5. Frame start screen di-dispose.
6. `WorldFrame` dibuat pada EDT.

Poin desain penting:

- transisi scene dipisahkan ke method `moveToWorld()`,
- flag `movingToWorld` mencegah transisi ganda bila tombol ditekan berkali-kali.

## 4. Flow world scene

`ui.world.WorldFrame` hanya bertugas sebagai container Swing.

Logika sebenarnya ada di `ui.world.GamePanel`.

### Saat `GamePanel` dibuat

1. Menentukan ukuran preferensi.
2. Mengaktifkan double buffering.
3. Menambahkan `KeyboardInput`.
4. Memuat TMX map melalui `TiledMapLoader`.
5. Mengubah data collision dari loader menjadi `CollisionBlock`.
6. Membuat `ui.world.Player`.
7. Menyusun placeholder monster.

### Saat thread world dimulai

`startGameThread()` membuat thread baru lalu menjalankan `run()`.

Loop utama:

```text
while (gameThread != null)
  hitung delta berdasarkan target 60 FPS
  jika delta >= 1
    update()
    repaint()
```

### Isi `update()`

1. Simpan posisi lama player.
2. Panggil `player.update()` untuk membaca input dan collision.
3. Tambah counter frame.
4. Tandai bahwa pemain sudah bergerak bila posisi berubah.
5. Cek kedekatan ke monster placeholder.

### Kondisi trigger battle di world

Battle tidak langsung aktif saat game dimulai. Ada dua syarat:

- world sudah melewati `WORLD_ENTRY_GRACE_FRAMES`,
- pemain sudah bergerak dari posisi spawn.

Lalu jika jarak pemain ke salah satu monster placeholder cukup dekat dan tombol `E` ditekan:

```text
enterBattleScene()
  -> battleTriggered = true
  -> gameThread = null
  -> dispose window world
  -> new fantasyrpg.ui.battle.GameFrame()
```

Catatan penting:

Transisi ini tidak membawa object `Enemy` domain apa pun. Jadi enemy encounter belum menjadi bagian dari domain battle yang terstruktur.

## 5. Flow pemuatan map

`TiledMapLoader` memuat file `assets/maps/maps.tmx`.

Urutan kerjanya:

1. Parse XML TMX.
2. Baca dimensi map dan ukuran tile.
3. Ambil tileset TSX dan muat image tiap tile.
4. Baca semua layer CSV menjadi `int[][]`.
5. Cari object group collision.
6. Cari spawn point.
7. Cari object group enemy spawn.

Data hasil parsing disimpan di field publik loader:

- `mapLayers`
- `mapLayerNames`
- `tiles`
- `collisions`
- `spawnX`, `spawnY`
- `enemySpawnPoints`

Secara runtime, `ui.world.GamePanel` kemudian memakai data ini untuk render, collision, dan placeholder marker.

## 6. Flow battle pada `ui.BattleGUI`

Battle GUI ini adalah implementasi yang memakai domain battle sesungguhnya.

### Saat konstruktor dipanggil

1. Simpan referensi `entities.Player`.
2. Pilih enemy awal.
3. Buat `BattleService` + `RandomEventService`.
4. Muat aset umum dan aset stage.
5. Panggil `beginBattle()`.
6. Daftarkan input mouse dan keyboard.

### Saat battle dimulai

`beginBattle()` melakukan:

1. Tambahkan log stage.
2. Mulai ronde pertama lewat `beginRound()`.
3. Aktifkan auto attack enemy lewat `startEnemyAutoAttack()`.

### Awal ronde

`beginRound()` memanggil:

```text
battleService.beginRound(player, currentEnemy)
```

Method tersebut:

1. me-reset modifier player,
2. me-reset modifier enemy,
3. memicu random event ronde.

### Aksi player

UI menerjemahkan input menjadi `BattleService.PlayerAction`:

- `ATTACK`
- `SKILL`
- `DEFEND`
- `POTION`

Lalu `resolvePlayerAction(...)` memanggil:

```text
battleService.executePlayerAction(player, currentEnemy, action)
```

Method service mengembalikan `ActionResult` berisi:

- pesan hasil aksi,
- jumlah damage.

### Aksi enemy

Enemy menyerang otomatis melalui `Timer`.

`resolveEnemyTurn()` memanggil:

```text
battleService.executeEnemyTurn(player, currentEnemy)
```

Service akan:

- memakai `useSkill(...)` jika enemy adalah `SkillUser` dan HP sudah <= 50%,
- memakai `attack(...)` biasa jika tidak.

### Penyelesaian kemenangan

Jika enemy mati:

- timer enemy dihentikan,
- jika stage 1 selesai maka lanjut ke stage 2,
- jika stage terakhir selesai maka diputar animasi kemenangan.

### Penyelesaian kekalahan

Jika player mati:

- `isGameOver = true`,
- timer enemy dihentikan,
- overlay game over ditampilkan.

## 7. Flow battle pada `ui.battle.GamePanel`

Battle package ini punya flow sendiri yang tidak memakai service/domain battle utama.

### State battle lokal

State seperti berikut disimpan langsung di panel:

- `heroHp`
- `heroMp`
- `enemyHp`
- `turn`
- `logLines`
- `battleEnded`

### Input

- tombol `J` = attack,
- tombol `K` = skill,
- tombol `L` = defend,
- mouse click di `ActionPanel` juga memicu aksi yang sama.

### Resolusi aksi

Method `handleAction(String action)`:

1. memvalidasi state panel,
2. mengantrekan efek animasi,
3. menghitung damage dengan `Random`,
4. memperbarui HP/MP,
5. menulis log,
6. memicu giliran musuh bila perlu.

### Antrean animasi

Efek visual diatur dengan `Queue<String> effectQueue` dan `Timer`.

Alurnya:

```text
queueEffect(effect)
  -> effectQueue.add(effect)
  -> jika timer belum jalan, startNextEffect()

startNextEffect()
  -> ambil effect berikutnya
  -> disable action panel sementara
  -> timer 35ms/frame
  -> setelah 12 frame, lanjut effect berikutnya
```

Ini adalah pola sederhana tetapi efektif untuk serialisasi animasi UI.

## 8. Flow battle CLI style di `BattleService.startBattle`

Walaupun aplikasi utama sekarang berbasis Swing, `BattleService` masih punya method `startBattle(Player, Enemy, Scanner)`.

Method ini menunjukkan bahwa service awalnya dirancang juga untuk CLI/console flow:

1. mulai ronde,
2. baca input user dari `Scanner`,
3. terjemahkan pilihan ke enum aksi,
4. eksekusi aksi player,
5. eksekusi aksi enemy,
6. ulang sampai salah satu kalah.

Untuk pembelajaran OOP, method ini penting karena menunjukkan bahwa rules battle sebenarnya dapat dijalankan tanpa UI grafis.

## 9. Kesimpulan alur runtime

Jika disederhanakan, proyek ini memiliki dua alur utama:

```text
Flow aplikasi utama:
Main -> Start Screen -> World -> Battle Prototype

Flow battle domain:
core.Game -> BattleGUI -> BattleService -> Entities
```

Memahami dua flow ini sangat penting agar pembaca tidak salah menyimpulkan bahwa semua bagian proyek sudah berada dalam satu pipeline arsitektur yang sama. Secara fungsional proyek berjalan, tetapi secara desain masih ada dua jalur evolusi yang hidup berdampingan.
