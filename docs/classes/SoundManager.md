# Dokumentasi Kelas: `fantasyrpg.sound.SoundManager`

Kelas `fantasyrpg.sound.SoundManager` mengelola seluruh sistem audio game, mulai dari efek suara pendek (SFX) hingga musik latar (BGM). Kelas ini dapat memutar berkas wav eksternal dan secara otomatis beralih ke komposer MIDI internal jika file eksternal tidak ditemukan.

## Informasi Dasar
- **Package**: `fantasyrpg.sound`

## Field Utama
- `private static Sequencer sequencer`: Mengatur sekuens pemutaran MIDI.
- `private static Synthesizer synthesizer`: Mensintesis instrumen suara MIDI.
- `private static MidiChannel[] channels`: Mengontrol saluran suara instrumen MIDI.
- `private static Thread bgmThread`: Thread terpisah untuk pemutaran loop BGM secara non-blocking.
- `private static boolean bgmRunning`: Flag status aktif lagu latar.
- `private static String currentBgm`: Nama/tipe trek BGM aktif (seperti `"START"`, `"WORLD"`, `"BATTLE"`, `"DUNGEON"`, `"ENDING"`).

## Method Utama
- `public static synchronized void playBGM(String type)`: Memutar lagu latar berdasarkan tipe. Jika file WAV di `assets/sounds/` tidak tersedia, program memicu pembuatan instrumen dan harmoni MIDI secara prosedural.
- `public static synchronized void stopBGM()`: Mematikan musik latar dan membungkam seluruh instrumen aktif.
- `public static synchronized void playSFX(String name)`: Memainkan berkas WAV suara pendek untuk efek pertarungan (seperti `"HIT"`, `"DEFEND"`, `"VICTORY"`) secara asinkron.
