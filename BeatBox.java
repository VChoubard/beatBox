import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by vchoubard on 28/09/14.
 */
public class BeatBox {
    JFrame theFrame;
    JPanel mainPanel;
    JList incomingList;
    JTextField userMessage;
    ArrayList<JCheckBox> checkboxList;
    int nextNum;
    Vector<String> listVector = new Vector<String>();
    String userName;
    ObjectOutputStream out;
    ObjectInputStream in;
    HashMap<String, Boolean[]> otherSegsMap = new HashMap<String, Boolean[]>();

    Sequencer sequencer;
    Sequence sequence;
    Sequence mySequence = null;
    Track track;

    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal",
            "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom",
            "High Agogo", "Open Hi Conga"};

    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main (String[] args) {
        new BeatBox().startUp(args[0]);
    }

    private void startUp(String name) {
        userName = name;
        try {
            Socket sock = new Socket("127.0.0.1", 4242);
            out = new ObjectOutputStream(sock.getOutputStream());
            in = new ObjectInputStream(sock.getInputStream());
            Thread remote = new Thread(new RemoteReader());
            remote.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setUpMidi();
        buildGUI();
    }

    private void buildGUI() {
        theFrame = new JFrame("Beat Box");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        checkboxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton tempoUp = new JButton("Tempo Up");
        tempoUp.addActionListener(new MyUpTempoListener());
        buttonBox.add(tempoUp);

        JButton tempoDown = new JButton("Tempo Down");
        tempoDown.addActionListener(new MyDownTempoListener());
        buttonBox.add(tempoDown);

        JButton save = new JButton("Save");
        save.addActionListener(new MySaveListener());
        buttonBox.add(save);

        JButton load = new JButton("Load");
        load.addActionListener(new MyLoadListener());
        buttonBox.add(load);

        JButton sendIt = new JButton("Sent It");
        sendIt.addActionListener(new MySendItListener());
        buttonBox.add(sendIt);

        userMessage = new JTextField();
        buttonBox.add(userMessage);

        incomingList = new JList();
        incomingList.addListSelectionListener(new MyListSelectionListener());
        incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        incomingList.setListData(listVector);
        JScrollPane theList = new JScrollPane(incomingList);
        buttonBox.add(theList);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (String name : instrumentNames) {
            nameBox.add(new Label(name));
        }

        GridLayout grid = new GridLayout(16, 16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        for (int i = 0; i < 256; i++) {
            JCheckBox cBox = new JCheckBox();
            cBox.setSelected(false);
            checkboxList.add(cBox);
            mainPanel.add(cBox);
            cBox.addItemListener(new MyCheckboxListener());
        }

        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);
        background.add(BorderLayout.CENTER, mainPanel);

        setUpMidi();

        theFrame.getContentPane().add(background);
        theFrame.setBounds(50, 50, 300, 300);
        theFrame.pack();
        theFrame.setVisible(true);
    }

    private void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void buildTrackAndStart() {
        ArrayList<Integer> trackList;

        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            trackList = new ArrayList<Integer>();

            for (int j = 0; j < 16; j ++) {
                JCheckBox jc = checkboxList.get(j + 16*i);
                if (jc.isSelected()) {
                    int key = instruments[i];
                    trackList.add(key);
                } else {
                    trackList.add(null);
                }
            }

            makeTrack(trackList);
        }

        track.add(makeEvent(ShortMessage.PROGRAM_CHANGE, 9, 1, 0, 15));
        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    private void makeTrack(ArrayList list) {
        Iterator it = list.iterator();
        for (int i = 0; i < 16; i++) {
            Integer num = (Integer)it.next();

            if (num != null) {
                track.add(makeEvent(ShortMessage.NOTE_ON, 9, num, 100, i));
                track.add(makeEvent(ShortMessage.NOTE_OFF, 9, num, 100, i+1));
            }
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

    private class MyStartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            buildTrackAndStart();
        }
    }

    private class MyStopListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            sequencer.stop();
        }
    }

    private class MyUpTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 1.03));
        }
    }

    private class MyDownTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 0.97));
        }
    }

    private class MyCheckboxListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent itemEvent) {
            if(sequencer.isRunning()) {
                buildTrackAndStart();
            }
        }
    }

    private class MySaveListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            Boolean[] checkboxState = new Boolean[256];
            Arrays.fill(checkboxState, false);

            for (int i = 0; i < 256; i++) {
                JCheckBox jc = checkboxList.get(i);
                if (jc.isSelected()) {
                    checkboxState[i] = true;
                }
            }

            try {
                FileOutputStream fs = new FileOutputStream(new File("CheckBox.ser"));
                ObjectOutputStream os = new ObjectOutputStream(fs);
                os.writeObject(checkboxState);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class MyLoadListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            Boolean[] checkboxState =  new Boolean[256];

            try {
                FileInputStream fi = new FileInputStream(new File("CheckBox.ser"));
                ObjectInputStream is = new ObjectInputStream(fi);
                checkboxState = (Boolean[]) is.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }

            changeSequence(checkboxState);
        }
    }

    private class RemoteReader implements Runnable {
        Object obj = null;

        @Override
        public void run() {
            try {
                while ((obj = in.readObject()) != null) {
                    System.out.println("got an object from server");
                    System.out.println(obj.getClass());
                    String nameToShow = (String)obj;
                    Boolean[] checkboxState = (Boolean[]) in.readObject();
                    otherSegsMap.put(nameToShow, checkboxState);
                    listVector.add(nameToShow);
                    incomingList.setListData(listVector);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class MySendItListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            Boolean[] checkboxState = new Boolean[256];
            Arrays.fill(checkboxState, false);
            for (int i = 0; i < 256; i++) {
                JCheckBox check = checkboxList.get(i);
                if (check.isSelected()) {
                    checkboxState[i] = true;
                }
            }
            String messageToSend = userName + " " + nextNum++ + ": Song";
            if (!userMessage.getText().isEmpty()) {
                messageToSend = userName + " " + nextNum++ + ": " + userMessage.getText();
            }
            try {
                out.writeObject(messageToSend);
                out.writeObject(checkboxState);
            } catch (IOException e) {
                e.printStackTrace();
            }
            userMessage.setText("");
        }
    }

    private class MyListSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent listSelectionEvent) {
            if (!listSelectionEvent.getValueIsAdjusting()) {
                String selected = (String) incomingList.getSelectedValue();
                if (selected != null) {
                    Boolean[] selectedState = otherSegsMap.get(selected);
                    changeSequence(selectedState);
                    sequencer.stop();
                    buildTrackAndStart();
                }
            }
        }
    }

    private void changeSequence(Boolean[] selectedState) {
        for (int i = 0; i < 256; i++) {
            JCheckBox cBox = checkboxList.get(i);
            cBox.setSelected(selectedState[i]);
        }
    }
}
