# Dungeon Battle: Rise of Hero

## Deskripsi

**Dungeon Battle: Rise of Hero** adalah game berbasis Java yang mensimulasikan pertarungan antara pemain (*Player*) dan musuh (*Enemy*) di dalam sebuah dungeon. Pemain berperan sebagai seorang hero yang harus mengalahkan berbagai jenis musuh untuk memperoleh score dan meningkatkan level karakter.

Setiap karakter dalam game memiliki atribut utama seperti:

- Health Point (HP)
- Attack Power
- Level
- Score

Pemain dapat menyerang musuh menggunakan berbagai jenis serangan dan skill. Setiap musuh memiliki perilaku serta pola serangan yang berbeda-beda, sehingga pemain harus menyesuaikan strategi selama pertarungan berlangsung.

Seiring permainan berjalan, pemain akan memperoleh experience dan naik level. Proses leveling akan meningkatkan kekuatan serangan dan jumlah HP pemain. Sistem scoring juga diterapkan untuk memberikan poin setiap kali pemain berhasil mengalahkan musuh.

---

# Konsep Object-Oriented Programming (OOP)

Game ini dikembangkan dengan menerapkan berbagai konsep **Object-Oriented Programming (OOP)** secara terstruktur untuk membangun sistem yang modular, fleksibel, dan mudah dikembangkan.

## Encapsulation

Konsep encapsulation diterapkan dengan membungkus atribut penting seperti:

- `hp`
- `level`
- `score`
- `attackPower`

ke dalam class dan mengaksesnya melalui method tertentu seperti getter dan setter agar data lebih aman dan terkontrol.

---

## Inheritance

Inheritance digunakan untuk membentuk hierarki class pada sistem game.

Struktur pewarisan class:

```text
Character
├── Player
└── Enemy
    ├── Goblin
    ├── OrcWarrior
    └── DragonBoss
```

Class `Character` menjadi parent class utama yang diturunkan menjadi `Player` dan `Enemy`. Selanjutnya, class `Enemy` memiliki beberapa subclass dengan karakteristik berbeda.

---

## Polymorphism

Polymorphism diterapkan melalui penggunaan method seperti:

```java
attack()
useSkill()
```

Method tersebut dapat dipanggil melalui tipe parent class, namun memiliki implementasi berbeda pada masing-masing subclass.

---

## Method Overloading

Method overloading diterapkan pada class `Player` dengan beberapa variasi method `attack()` yang memiliki parameter berbeda.

Contoh:

```java
attack()
attack(int damage)
attack(String skillName)
```

---

## Method Overriding

Method overriding digunakan pada subclass seperti:

- `Goblin`
- `OrcWarrior`
- `DragonBoss`

untuk mengubah perilaku method `attack()` sesuai karakteristik masing-masing musuh.

---

## Abstract Class

Game ini menggunakan abstract class sebagai dasar sistem karakter.

Abstract class yang digunakan:

- `Character`
- `Enemy`

Class tersebut tidak dapat diinstansiasi secara langsung dan hanya digunakan sebagai dasar pewarisan.

---

## Interface

Interface digunakan untuk mendefinisikan kemampuan tertentu yang harus dimiliki class tertentu.

Interface yang digunakan:

- `Attackable`
- `SkillUser`

Implementasi interface membantu menjaga konsistensi perilaku antar class.

---

# Fitur Game

- Sistem pertarungan Player vs Enemy
- Sistem leveling
- Sistem scoring
- Berbagai jenis enemy dengan kemampuan berbeda
- Skill dan variasi serangan
- Struktur program modular berbasis OOP
- Mudah dikembangkan untuk fitur baru

---

# Teknologi

- Bahasa Pemrograman: Java
- Paradigma: Object-Oriented Programming (OOP)

---

# Tujuan Pengembangan

Project ini dibuat untuk mengimplementasikan dan mempraktikkan konsep-konsep Object-Oriented Programming (OOP) dalam pengembangan game sederhana berbasis Java.

# Requirment

- java 
- javac

## Start the Game

- start.bat