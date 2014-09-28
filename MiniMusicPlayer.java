import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;

/**
 * Created by vchoubard on 26/09/14.
 */
public class MiniMusicPlayer {

    static JFrame f = new JFrame("Music Video player");
    static MyDrawPanel ml;

    public static void main(String[] args) {
        MiniMusicPlayer mini = new MiniMusicPlayer();
        mini.go();
    }

    public void setUpGui() {
        ml = new MyDrawPanel();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setContentPane(ml);
        f.setBounds(30, 30, 300, 300);
        f.setVisible(true);
    }

    public void go() {
        setUpGui();

        try {
            Sequencer sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.addControllerEventListener(ml, new int[] {127});
            Sequence seq = new Sequence(Sequence.PPQ, 4);
            Track track = seq.createTrack();

            int r = 0;
            for (int i = 0; i<60; i+= 4) {
                r = (int) ((Math.random() * 50) + 1);
                track.add(makeEvent(ShortMessage.NOTE_ON, 1, r, 100, i));
                track.add(makeEvent(ShortMessage.CONTROL_CHANGE, 1, 127, 0, i));
                track.add(makeEvent(ShortMessage.NOTE_OFF, 1, r, 100, i+2));
            }

            sequencer.setSequence(seq);
            sequencer.start();
            sequencer.setTempoInBPM(120);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }

        return event;
    }


}
