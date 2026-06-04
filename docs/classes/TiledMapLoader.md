# Dokumentasi Kelas: `fantasyrpg.ui.world.TiledMapLoader`

Kelas `fantasyrpg.ui.world.TiledMapLoader` bertugas mengurai file XML peta ubin berekstensi `.tmx` (dari Tiled Map Editor) secara dinamis untuk merender ubin lantai dan mendeteksi penempatan dinding/musuh di peta eksplorasi.

## Informasi Dasar
- **Package**: `fantasyrpg.ui.world`

## Field Utama
- `private int width`, `height`: Lebar dan tinggi peta dalam satuan tile.
- `private int tileWidth`, `tileHeight`: Ukuran dimensi piksel setiap ubin (default 48x48).
- `private List<BufferedImage> tileImages`: Kumpulan potongan gambar ubin lantai.
- `private int[][][] layers`: Array 3 dimensi penyimpan peta berubin ([layer][y][x]).
- `private List<CollisionBlock> collisions`: Blok area tabrakan padat.
- `private List<EnemySpawnPoint> spawnPoints`: Letak titik spawn musuh.

## Method Utama
- `public void loadMap(String path)`: Membaca file XML TMX, memuat gambar ubin, dan mengisi array layers ubin serta daftar kolisi dinding dan spawn point musuh.
- `public void draw(Graphics2D g, int layerIndex)`: Menggambar layer ubin tertentu ke layar sesuai susunan ubin pada grid.
