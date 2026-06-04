package fantasyrpg.sound;

import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {
    private static Synthesizer synth;
    private static MidiChannel[] channels;
    private static Thread bgmThread;
    private static String currentBgm = "";
    private static boolean bgmRunning = false;
    private static Clip activeWavClip = null;

    static {
        try {
            synth = MidiSystem.getSynthesizer();
            if (synth != null) {
                synth.open();
                channels = synth.getChannels();
            }
        } catch (Exception e) {
            System.err.println("Midi Initialization error: " + e.getMessage());
        }
    }

    private static boolean playWavSFX(String name) {
        try {
            File file = new File("assets/sounds/" + name + ".wav");
            if (file.exists()) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clip.start();
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error playing WAV SFX: " + e.getMessage());
        }
        return false;
    }

    private static boolean playWavBGM(String name) {
        try {
            stopWavBGM();
            File file = new File("assets/sounds/" + name + ".wav");
            if (file.exists()) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                activeWavClip = AudioSystem.getClip();
                activeWavClip.open(ais);
                activeWavClip.loop(Clip.LOOP_CONTINUOUSLY);
                activeWavClip.start();
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error playing WAV BGM: " + e.getMessage());
        }
        return false;
    }

    private static void stopWavBGM() {
        if (activeWavClip != null) {
            try {
                if (activeWavClip.isRunning()) {
                    activeWavClip.stop();
                }
                activeWavClip.close();
            } catch (Exception e) {
                
            }
            activeWavClip = null;
        }
    }

    private static Thread sfxThread = null;

    public static synchronized void silenceAllChannels() {
        silenceChannels(false);
    }

    private static synchronized void silenceChannels(boolean skipSfx) {
        if (sfxThread != null && !skipSfx) {
            try {
                sfxThread.interrupt();
            } catch (Exception e) {
                
            }
            sfxThread = null;
        }
        if (channels != null) {
            for (int i = 0; i < channels.length; i++) {
                if (skipSfx && i == 5) continue;
                MidiChannel chan = channels[i];
                if (chan != null) {
                    try {
                        chan.allNotesOff();
                        chan.allSoundOff();
                    } catch (Exception e) {
                        
                    }
                }
            }
        }
    }

    public static synchronized void playBGM(String type) {
        if (currentBgm.equals(type) && bgmRunning) {
            return;
        }
        stopBGM();
        currentBgm = type;
        bgmRunning = true;

        
        String filename = type.toLowerCase() + "_bgm";
        if (playWavBGM(filename)) {
            return;
        }

        
        if (channels == null) return;
        bgmThread = new Thread(() -> {
            try {
                if ("START".equals(type)) {
                    channels[0].programChange(46); 
                    channels[1].programChange(60); 
                    channels[2].programChange(48); 

                    int[][] chords = {
                        {50, 53, 57}, 
                        {55, 59, 62}, 
                        {48, 52, 55}, 
                        {53, 57, 60}, 
                        {58, 62, 65}, 
                        {57, 62, 64}, 
                        {57, 61, 64}, 
                        {50, 53, 57}  
                    };
                    int[] hornMelody = {
                        50, 53, 57, 62,
                        55, 59, 62, 67,
                        48, 52, 55, 60,
                        53, 57, 60, 65,
                        58, 62, 65, 70,
                        64, 62, 64, 69,
                        61, 57, 59, 61,
                        62, 62, 62, 62
                    };

                    int step = 0;
                    while (bgmRunning) {
                        int chordIndex = (step / 4) % chords.length;
                        int[] chord = chords[chordIndex];
                        
                        if (step % 4 == 0) {
                            for (int note : chord) {
                                channels[2].noteOn(note - 12, 45);
                                channels[2].noteOn(note, 35);
                            }
                        }
                        
                        int harpNote = chord[step % chord.length] + 12;
                        channels[0].noteOn(harpNote, 60);
                        
                        int hornNote = hornMelody[step % hornMelody.length];
                        channels[1].noteOn(hornNote, 70);
                        
                        Thread.sleep(500);
                        
                        channels[0].noteOff(harpNote);
                        channels[1].noteOff(hornNote);
                        
                        if (step % 4 == 3) {
                            for (int note : chord) {
                                channels[2].noteOff(note - 12);
                                channels[2].noteOff(note);
                            }
                        }
                        step++;
                    }
                } else if ("WORLD".equals(type)) {
                    channels[0].programChange(24); 
                    channels[1].programChange(48); 
                    
                    int[][] chords = {
                        {57, 60, 64}, 
                        {60, 64, 67}, 
                        {55, 59, 62}, 
                        {53, 57, 60}  
                    };

                    while (bgmRunning) {
                        for (int i = 0; i < chords.length && bgmRunning; i++) {
                            for (int note : chords[i]) {
                                channels[1].noteOn(note - 12, 45); 
                            }
                            int[] chord = chords[i];
                            for (int stepL = 0; stepL < 4 && bgmRunning; stepL++) {
                                int note = chord[stepL % chord.length];
                                channels[0].noteOn(note, 65);
                                Thread.sleep(600);
                                channels[0].noteOff(note);
                            }
                            for (int note : chords[i]) {
                                channels[1].noteOff(note - 12);
                            }
                        }
                    }
                } else if ("DUNGEON".equals(type)) {
                    channels[0].programChange(48); 
                    channels[1].programChange(14); 
                    channels[2].programChange(52); 
                    
                    int[][] chords = {
                        {50, 53, 56}, 
                        {51, 54, 57}, 
                        {49, 52, 55}, 
                        {46, 50, 53}  
                    };
                    
                    int[] bellMelody = {
                        50, 0, 53, 56,
                        55, 0, 51, 54,
                        49, 0, 52, 58,
                        50, 62, 58, 0
                    };
                    
                    int step = 0;
                    while (bgmRunning) {
                        int chordIndex = (step / 4) % chords.length;
                        int[] chord = chords[chordIndex];
                        
                        if (step % 4 == 0) {
                            for (int note : chord) {
                                channels[0].noteOn(note - 24, 42); 
                                channels[2].noteOn(note - 12, 38); 
                            }
                        }
                        
                        int bellNote = bellMelody[step % bellMelody.length];
                        if (bellNote > 0) {
                            channels[1].noteOn(bellNote + 12, 70); 
                        }
                        
                        Thread.sleep(850); 
                        
                        if (bellNote > 0) {
                            channels[1].noteOff(bellNote + 12);
                        }
                        
                        if (step % 4 == 3) {
                            for (int note : chord) {
                                channels[0].noteOff(note - 24);
                                channels[2].noteOff(note - 12);
                            }
                        }
                        step++;
                    }
                } else if ("BATTLE".equals(type)) {
                    channels[0].programChange(30); 
                    channels[1].programChange(80); 
                    channels[9].programChange(0);  

                    int[] bassNotes = {50, 50, 53, 53, 55, 55, 48, 48};
                    int[] melody = {62, 65, 67, 69, 67, 65, 62, 60};

                    int step = 0;
                    while (bgmRunning) {
                        int bass = bassNotes[step % bassNotes.length];
                        int mel = melody[step % melody.length];

                        channels[0].noteOn(bass - 12, 80);
                        if (step % 2 == 0) {
                            channels[1].noteOn(mel, 65);
                        }
                        channels[9].noteOn(35, 85); 
                        if (step % 4 == 2) {
                            channels[9].noteOn(38, 75); 
                        }

                        Thread.sleep(250);

                        channels[0].noteOff(bass - 12);
                        channels[1].noteOff(mel);
                        step++;
                    }
                } else if ("ENDING".equals(type)) {
                    channels[0].programChange(91);  // Choir Aahs
                    channels[1].programChange(48);  // String Ensemble 1
                    channels[2].programChange(60);  // French Horn (Melody)
                    channels[3].programChange(47);  // Timpani

                    int[][] chords = {
                        {50, 53, 57, 62}, // Dm
                        {46, 50, 53, 58}, // Bb
                        {48, 52, 55, 60}, // C
                        {45, 48, 52, 57}, // Am
                        {43, 46, 50, 55}, // Gm
                        {50, 53, 57, 62}, // Dm
                        {46, 50, 53, 58}, // Bb
                        {45, 49, 52, 57}  // A
                    };

                    int[] melody = {
                        // Dm
                        50, 53, 57, 62,
                        // Bb
                        58, 53, 50, 53,
                        // C
                        52, 55, 60, 64,
                        // Am
                        57, 52, 48, 52,
                        // Gm
                        50, 53, 55, 58,
                        // Dm
                        57, 53, 50, 53,
                        // Bb
                        58, 62, 65, 62,
                        // A
                        61, 57, 53, 57
                    };

                    int step = 0;
                    while (bgmRunning) {
                        int chordIndex = (step / 4) % chords.length;
                        int[] chord = chords[chordIndex];

                        if (step % 4 == 0) {
                            channels[3].noteOn(chord[0] - 12, 100); // Booming Timpani
                            for (int note : chord) {
                                channels[1].noteOn(note - 12, 55); // String pad
                                channels[0].noteOn(note, 65);      // Choir
                            }
                        }

                        int mNote = melody[step % melody.length];
                        channels[2].noteOn(mNote, 80);

                        Thread.sleep(600); // Slow, majestic tempo

                        channels[2].noteOff(mNote);
                        if (step % 4 == 0) {
                            channels[3].noteOff(chord[0] - 12);
                        }

                        if (step % 4 == 3) {
                            for (int note : chord) {
                                channels[1].noteOff(note - 12);
                                channels[0].noteOff(note);
                            }
                        }
                        step++;
                    }
                }
            } catch (InterruptedException e) {
                
            } catch (Exception e) {
                System.err.println("MIDI BGM playing error: " + e.getMessage());
            } finally {
                silenceChannels(true); 
            }
        });
        bgmThread.setDaemon(true);
        bgmThread.start();
    }

    public static synchronized void stopBGM() {
        bgmRunning = false;
        currentBgm = "";
        if (bgmThread != null) {
            try {
                bgmThread.interrupt();
            } catch (Exception e) {
                
            }
            bgmThread = null;
        }
        stopWavBGM();
        silenceChannels(true); 
    }

    public static synchronized void playSFX(String type) {
        if (playWavSFX(type.toLowerCase())) {
            return;
        }

        if (channels == null) return;
        if (sfxThread != null) {
            try {
                sfxThread.interrupt();
            } catch (Exception e) {
                
            }
            sfxThread = null;
        }

        sfxThread = new Thread(() -> {
            try {
                if ("ATTACK".equals(type)) {
                    channels[5].programChange(116);
                    channels[5].noteOn(48, 100);
                    Thread.sleep(100);
                    channels[5].noteOn(52, 90);
                    Thread.sleep(100);
                    channels[5].noteOff(48);
                    channels[5].noteOff(52);
                } else if ("SKILL".equals(type)) {
                    channels[5].programChange(81);
                    for (int note = 60; note <= 76; note += 4) {
                        channels[5].noteOn(note, 100);
                        Thread.sleep(50);
                        channels[5].noteOff(note);
                    }
                } else if ("DEFEND".equals(type)) {
                    channels[5].programChange(91);
                    channels[5].noteOn(48, 90);
                    channels[5].noteOn(52, 90);
                    channels[5].noteOn(55, 90);
                    Thread.sleep(400);
                    channels[5].noteOff(48);
                    channels[5].noteOff(52);
                    channels[5].noteOff(55);
                } else if ("HIT".equals(type)) {
                    channels[5].programChange(127);
                    channels[5].noteOn(36, 110);
                    Thread.sleep(150);
                    channels[5].noteOff(36);
                } else if ("VICTORY".equals(type)) {
                    channels[5].programChange(56);
                    int[] notes = {60, 60, 60, 60, 64, 62, 64, 67, 72};
                    int[] durations = {150, 150, 150, 300, 300, 150, 150, 150, 600};
                    for (int i = 0; i < notes.length; i++) {
                        channels[5].noteOn(notes[i], 100);
                        Thread.sleep(durations[i]);
                        channels[5].noteOff(notes[i]);
                    }
                } else if ("DEFEAT".equals(type)) {
                    channels[5].programChange(57);
                    int[] notes = {60, 59, 58, 55};
                    int[] durations = {250, 250, 250, 600};
                    for (int i = 0; i < notes.length; i++) {
                        channels[5].noteOn(notes[i], 90);
                        Thread.sleep(durations[i]);
                        channels[5].noteOff(notes[i]);
                    }
                } else if ("CLICK".equals(type)) {
                    channels[5].programChange(120);
                    channels[5].noteOn(64, 80);
                    Thread.sleep(50);
                    channels[5].noteOff(64);
                }
            } catch (InterruptedException e) {
                
            } catch (Exception e) {
                System.err.println("MIDI SFX error: " + e.getMessage());
            } finally {
                if (channels != null && channels[5] != null) {
                    try {
                        channels[5].allNotesOff();
                        channels[5].allSoundOff();
                    } catch (Exception e) {
                        
                    }
                }
            }
        });
        sfxThread.setDaemon(true);
        sfxThread.start();
    }
}
