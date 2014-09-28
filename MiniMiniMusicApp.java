import javax.sound.midi.*;

/**
 * Created by vchoubard on 25/09/14.
 */
public class MiniMiniMusicApp {
    public static void main (String[] args) {
        MiniMiniMusicApp mini = new MiniMiniMusicApp();
        mini.play();
    }

    public void play() {
        try {
            Sequencer player = MidiSystem.getSequencer();
            player.open();

            Sequence seq = new Sequence(Sequence.PPQ, 4);

            Track track = seq.createTrack();

            ShortMessage a = new ShortMessage();
            a.setMessage(ShortMessage.NOTE_ON, 1, 44, 100);
            MidiEvent noteOn = new MidiEvent(a, 1);
            track.add(noteOn);

            ShortMessage first = new ShortMessage();
            first.setMessage(ShortMessage.PROGRAM_CHANGE, 1, 102, 0);
            MidiEvent change = new MidiEvent(first, 1);
            track.add(change);

            ShortMessage b = new ShortMessage();
            b.setMessage(ShortMessage.NOTE_OFF, 1, 44, 100);
            MidiEvent noteOff = new MidiEvent(b, 3);
            track.add(noteOff);

            player.setSequence(seq);

            player.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
