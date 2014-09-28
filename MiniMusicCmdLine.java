import javax.sound.midi.*;

/**
 * Created by vchoubard on 25/09/14.
 */
public class MiniMusicCmdLine {
    public static void main(String[] args) {
        MiniMusicCmdLine mini = new MiniMusicCmdLine();
        if (args.length < 2) {
            System.out.println("Dont't forget the instrument and the note args");
        } else {
            int instrument  = Integer.parseInt(args[0]);
            int note = Integer.parseInt(args[1]);
            mini.play(instrument, note);
        }

    }

    private void play(int instrument, int note) {
        try {
            Sequencer player = MidiSystem.getSequencer();
            player.open();

            Sequence sequence = new Sequence(Sequence.PPQ, 4);
            Track track = sequence.createTrack();

            ShortMessage first = new ShortMessage();
            first.setMessage(ShortMessage.PROGRAM_CHANGE, 1, instrument, 0);
            MidiEvent changeInstrument = new MidiEvent(first, 1);
            track.add(changeInstrument);

            ShortMessage start = new ShortMessage();
            start.setMessage(ShortMessage.NOTE_ON, 1, note, 120);
            MidiEvent noteOn = new MidiEvent(start, 1);
            track.add(noteOn);

            ShortMessage stop = new ShortMessage();
            stop.setMessage(ShortMessage.NOTE_OFF, 1, note, 120);
            MidiEvent noteOff = new MidiEvent(stop, 16);
            track.add(noteOff);

            player.setSequence(sequence);
            player.start();
//player.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
