# Dokumentasi Kelas: `fantasyrpg.ui.world.GamePanel`

Kelas `fantasyrpg.ui.world.GamePanel` adalah mesin utama rendering peta eksplorasi 2D top-down. Kelas ini mengontrol pergerakan pahlawan, deteksi tabrakan (collisions), titik musuh (enemy spawns), dan siklus loop game utama.

## Informasi Dasar
- **Package**: `fantasyrpg.ui.world`
- **Superclass**: `javax.swing.JPanel`
- **Interface**: `java.lang.Runnable`

## Field Utama
- `private Thread gameThread`: Thread utama pemutar siklus game loop.
- `private final KeyboardInput keyHandler`: Pemroses input keyboard pemain.
- `private final TiledMapLoader mapLoader`: Pengurai XML/TMX peta ubin.
- `private final Player player`: Objek visual pahlawan penjelajah.
- `private final List<CollisionBlock> collisions`: Daftar blok rintangan padat.
- `private final List<EnemySpawnPoint> spawnPoints`: Daftar koordinat spawn monster.

## Method Utama
- `public void startThread()`: Menginstansiasi thread game dan memulainya secara asinkron.
- `public void run()`: Perulangan game loop utama dengan target kestabilan render 60 FPS.
- `public void update()`: Memperbarui koordinat gerak pahlawan, memeriksa tabrakan dengan rintangan, memicu transisi battle saat menabrak spawn point musuh, dan mengelola transisi ganti peta (Map 1 ke Map 2).
- `public void paintComponent(Graphics g)`: Menggambar layer peta bawah, sprite karakter, sprite musuh, layer atap atas, dan efek memudar hitam (fade).
