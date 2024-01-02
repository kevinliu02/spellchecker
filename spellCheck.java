import javax.management.loading.PrivateClassLoader;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.*;
import javax.swing.text.*;
import javax.swing.text.StyleConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.text.Utilities;
import java.util.Set;
import java.util.HashSet;

public class spellCheck extends JFrame implements ActionListener, Runnable {
    //Text object
    JTextPane text;
    //Frame/window
    JFrame frame;
    //thread to constantly check words in text file for errors
    Thread detection;
    //string to load text from JTextPane and check for misspelled words
    String check;
    //dictionary to store correctly spelled words to compare with user text
    Dictionary globalDict = new Dictionary("words_alpha.txt");
    Dictionary userDict = new Dictionary("user_dictionary.txt");
    //attribute sets to highlight misspelled words
    SimpleAttributeSet underline;
    //default style to remove highlight from corrected words
    Style defaultStyle;
    //styled doc to underline parts of text pane
    StyledDocument style;
    //Corrector object to provide correction suggestions for misspelled words
    Corrector corrector;
    //list of words for ignoreAll feature to ignore during the session
    Set<String> ignoreAllSet;
    //constructor to initialize and run spellcheck application
    HashMap<String, List<Integer>> textMap;
    int index;
    Detector detector;
    public spellCheck(){
        //instantiate spellcheck window
            frame = new JFrame("SpellCheck");
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
            } catch (Exception e) {
            }
            //instantiate text
            text = new JTextPane();
            detection = new Thread(this);
            style = text.getStyledDocument();
            defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
            //create header menu
            JMenuBar header = new JMenuBar();
            //instantiate file menu
            JMenu headerF = new JMenu("File");
            //instantiate file menu options
            JMenuItem f1 = new JMenuItem("New");
            JMenuItem f2 = new JMenuItem("Load");
            JMenuItem f3 = new JMenuItem("Save as...");
            //give file menu options action listeners and add them to file menu
            f1.addActionListener(this);
            headerF.add(f1);
            f2.addActionListener(this);
            headerF.add(f2);
            f3.addActionListener(this);
            headerF.add(f3);
            //instantiate edit menu
            JMenu headerE = new JMenu("Edit");
            //instantiate edit menu options
            JMenuItem e1 = new JMenuItem("Add to dictionary");
            JMenuItem e2 = new JMenuItem("Remove from dictionary");
            JMenuItem e3 = new JMenuItem("Clear dictionary");
            //give edit menu options action listeners and add them to edit menu
            e1.addActionListener(this);
            headerE.add(e1);
            e2.addActionListener(this);
            headerE.add(e2);
            e3.addActionListener(this);
            headerE.add(e3);
            //instantiate about menu
            JMenuItem headerA = new JMenuItem("About");
            headerA.setMaximumSize(headerA.getPreferredSize());
            //add action listener to display about page
            headerA.addActionListener(this);
            //instantiate help menu
            JMenuItem headerH = new JMenuItem("Help");
            headerH.setMaximumSize(headerH.getPreferredSize());
            //add action listener to display help page
            headerH.addActionListener(this);
            //instantiate metrics menu
            JMenuItem headerM = new JMenuItem("Metrics");
            headerM.setMaximumSize(headerM.getPreferredSize());
            //add action listener to display metrics data
            headerM.addActionListener(this);

            //add menus to header menu
            header.add(headerF);
            header.add(headerE);
            header.add(headerA);
            header.add(headerH);
            header.add(headerM);

            underline = new SimpleAttributeSet();
            StyleConstants.setUnderline(underline, true);
            StyleConstants.setBackground(underline, Color.RED);
            corrector = new Corrector(globalDict, userDict, textMap);
            ignoreAllSet = new HashSet<>();

            text.addMouseListener(new MouseAdapter() {
                private boolean activePopUp = false;

                private void pop(final MouseEvent event) {
                    if (SwingUtilities.isRightMouseButton(event)) {
                        try {
                            final StyledDocument correction = text.getStyledDocument();
                            final int offset = text.viewToModel(event.getPoint());
                            final int start = Utilities.getWordStart(text, offset),
                                    end = Utilities.getWordEnd(text, offset);
                            text.setSelectionStart(start);
                            text.setSelectionEnd(end);
                            final String currWord = correction.getText(start, end - start);
                            List<String> corrections;
                            final JPanel corrPanel = new JPanel();
                            corrections = corrector.suggestCorrectSpellings(currWord, index);
                            final ArrayList<JButton> corrList = new ArrayList<>();
                            for (int i = 0; i < corrections.size(); ++i) {
                                final JButton corrButton = new JButton(corrections.get(i));
                                corrPanel.add(corrButton);
                                corrList.add(corrButton);
                            }
                            final JButton delete = new JButton("Delete");
                            final JButton ignoreOnce = new JButton("Ignore once");
                            final JButton ignoreAll = new JButton("Ignore all");
                            final JButton addToUserDict = new JButton("Add to dictionary");
                            final JButton cancel = new JButton("Cancel");
                            corrPanel.add(delete);
                            corrPanel.add(ignoreOnce);
                            corrPanel.add(ignoreAll);
                            corrPanel.add(addToUserDict);
                            corrPanel.add(cancel);
                            final Popup popup = PopupFactory.getSharedInstance().getPopup(text, corrPanel, event.getXOnScreen(), event.getYOnScreen());
                            corrList.forEach(corrButton -> corrButton.addActionListener(e -> {
                                try {
                                    final String newWord = ((JButton) e.getSource()).getText();
                                    correction.remove(start, end - start);
                                    correction.insertString(start, newWord, null);
                                    text.setCaretPosition(start + newWord.length());
                                } catch (final BadLocationException | RuntimeException x){}
                                finally {
                                    popup.hide();
                                    activePopUp = false;
                                }
                            }));
                            activePopUp = true;
                            popup.show();
                            delete.addActionListener(e -> {
                                try {
                                    correction.remove(start, end - start);
                                } catch (BadLocationException ex) {}
                                popup.hide();
                                activePopUp = false;
                            });
                            //incomplete
                            ignoreOnce.addActionListener(e -> {
                                style.setCharacterAttributes(start, end - start, defaultStyle, true);
                                popup.hide();
                                activePopUp = false;
                            });
                            ignoreAll.addActionListener(e -> {
                                ignoreAllSet.add(currWord);
                                popup.hide();
                                activePopUp = false;
                            });
                            addToUserDict.addActionListener(e -> {
                                userDict.addWord(currWord);
                                popup.hide();
                                activePopUp = false;
                            });
                            cancel.addActionListener(e -> {
                                popup.hide();
                                text.setSelectionStart(offset);
                                text.setSelectionEnd(offset);
                                activePopUp = false;
                            });

                        } catch (final BadLocationException | RuntimeException x){}
                    }
                }
                private void maybePop(final MouseEvent event) {
                    if (event.isPopupTrigger()) {
                        if (activePopUp){}
                        else
                            pop(event);
                    }
                }

                @Override
                public void mouseClicked(final MouseEvent event) {
                    maybePop(event);
                }

                @Override
                public void mousePressed(final MouseEvent event) {
                    maybePop(event);
                }

                @Override
                public void mouseReleased(final MouseEvent event) {
                    maybePop(event);
                }
            });


            //build and display frame
            frame.setJMenuBar(header);
            frame.add(text);
            frame.setSize(1000, 800);
            frame.show();
            detection.start();
}

    @Override
    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();

        if (s.equals("Save as...")) {
            //create JFileChooser object
            JFileChooser j = new JFileChooser("f:");
            //displays save dialogue
            int r = j.showSaveDialog(null);
            if (r == JFileChooser.APPROVE_OPTION) {
                File file = new File(j.getSelectedFile().getAbsolutePath());
                try {
                    //instantiate file writer and buffered writer
                    FileWriter write = new FileWriter(file, false);
                    BufferedWriter bufferedWriter = new BufferedWriter(write);
                    //write text to new file  to be saved
                    bufferedWriter.write(text.getText());
                    bufferedWriter.flush();
                    bufferedWriter.close();
                } catch (Exception event) {
                    JOptionPane.showMessageDialog(frame, event.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Operation cancelled");
            }
        } else if (s.equals("About")){
            Object aboutMsg = "This spell checker takes user text files and allows you\n" +
                    "to create new text files and type your passage. The spell\n" +
                    "checker then uses the dictionary to find incorrectly\n" +
                    "spelled words and show you correction suggestions. it\n" +
                    "also tracks metrics data of your work.";
            JOptionPane.showMessageDialog(frame, aboutMsg/*, "About Spell Checker"*/);
        }else if (s.equals("Load")) {
            //create JFileChooser object
            JFileChooser j = new JFileChooser("f:");
            //displays save dialogue
            int r = j.showSaveDialog(null);
            if (r == JFileChooser.APPROVE_OPTION) {
                File file = new File(j.getSelectedFile().getAbsolutePath());
                try {
                    //instantiate strings to read text from loading file and copy to spell checker
                    String sFile = "", sImport = "";
                    //instantiate file reader and buffered reader
                    FileReader fr = new FileReader(file);
                    BufferedReader br = new BufferedReader(fr);
                    //initialize text from file
                    sFile = br.readLine();
                    //import all text from file
                    while ((sImport = br.readLine()) != null){
                        sFile = sFile + "\n" + sImport;
                    }
                    text.setText(sFile);
                } catch (Exception event) {
                    JOptionPane.showMessageDialog(frame, event.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Operation cancelled");
            }
        } else if (s.equals("Help")){
            String helpMsg = "Click file to load a .txt file to check\n" +
                            "the file for spelling errors. Click\n" +
                            "on words in red to correct or ignore\n" +
                            "them. Click file and then click\n" +
                            "Save as... to save your work";
            JOptionPane.showMessageDialog(frame, helpMsg);
        } else if (s.equals("New")){
            //user prompt window to save their work
            JButton yes = new JButton("Yes");
            JButton no = new JButton("No");
            yes.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //create JFileChooser object
                    JFileChooser j = new JFileChooser("f:");
                    //displays save dialogue
                    int r = j.showSaveDialog(null);
                    if (r == JFileChooser.APPROVE_OPTION) {
                        File file = new File(j.getSelectedFile().getAbsolutePath());
                        try {
                            //instantiate file writer and buffered writer
                            FileWriter write = new FileWriter(file, false);
                            BufferedWriter bufferedWriter = new BufferedWriter(write);
                            //write text to new file  to be saved
                            bufferedWriter.write(text.getText());
                            bufferedWriter.flush();
                            bufferedWriter.close();
                        } catch (Exception event) {
                            UIManager.put("OptionPane.okButtonText", "OK");
                            JOptionPane.showMessageDialog(frame, event.getMessage());
                        }
                    } else {
                        UIManager.put("OptionPane.okButtonText", "OK");
                        JOptionPane.showMessageDialog(frame, "Operation cancelled");
                    }
                    Window win = SwingUtilities.getWindowAncestor(yes);
                    win.setVisible(false);
                    text.setText("");
                }
            });
            no.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Window win = SwingUtilities.getWindowAncestor(no);
                    win.setVisible(false);
                    text.setText("");
                }
            });
            Object[] options = {yes, no};
            UIManager.put("OptionPane.okButtonText", "Cancel");
            final JOptionPane save = new JOptionPane("Would you like to save your file?\n" +
                    "Unsaved work will be lost.",
                     JOptionPane.QUESTION_MESSAGE);
            save.setOptions(options);
            JOptionPane.showMessageDialog(frame, save);
            UIManager.put("OptionPane.okButtonText", "OK");
        } else if (s.equals("Metrics")){
            String userText = text.getText();
            int characters = userText.length() - (2*(userText.split("\n").length - 1));
            int lines = userText.split("\n").length;
            int words = userText.split("\\s+").length;

        }
    }
    public void run(){
        double updateInterval = 1000000000;
        double nextUpdateTime = System.nanoTime() + updateInterval;

        while (detection != null){

            update();

            try{
                double remainingTime = nextUpdateTime - System.nanoTime();
                remainingTime = remainingTime/1000000;

                if (remainingTime < 0) {
                    remainingTime = 0;
                }
                Thread.sleep((long) remainingTime);

                nextUpdateTime += updateInterval;

            } catch (InterruptedException e){}
        }
    }
    /*public void update(){
        check = text.getText();
        textMap = new HashMap<String, List<Integer>>();
        check.replaceAll("-", " "); 
        check.replaceAll("/", " ");
        String currWord = "";
        index = 0;
        int currWordLength = 0;
        for (int i = 0; i < check.length(); i++){
            if (check.charAt(i) > 64 && check.charAt(i) < 91){
                currWordLength++;
            } else if (check.charAt(i) > 96 && check.charAt(i) < 123){
                currWordLength++;
            } else {
                style.setCharacterAttributes(i - check.substring(0, i).split("\n").length + 1, 1, defaultStyle, true);
                if (currWordLength != 0) {
                    currWord = check.substring(i - currWordLength, i);
                    if (!textMap.containsKey(currWord)) {
                    	List<Integer> newKey = new ArrayList<Integer>();
                    	newKey.add(index);
                    	textMap.put(currWord, newKey);
                    }
                    else {
                    	List<Integer> currList = textMap.get(currWord);
                    	currList.add(index);
                    }
                    index++;
                    if (!dictionary.hasWord(currWord) && !ignoreAllSet.contains(currWord)) {
                        style.setCharacterAttributes(i - currWordLength - check.substring(0, i).split("\n").length + 1, currWordLength, underline, true);
                    } else {
                        style.setCharacterAttributes(i - currWordLength , currWordLength, defaultStyle, true);
                    }
                    currWordLength = 0;
                }
            }
        }
    }*/
    
    public void update(){
        check = text.getText();
        textMap = new HashMap<String, List<Integer>>();
        index = 0;
        String[] wordList = check.split(" ");
        String currWord = "";
        int currWordLength = 0;
        Detector detector = corrector.getDetector();
        for (int i = 0; i < check.length(); i++){
            if (check.charAt(i) > 64 && check.charAt(i) < 91){
                currWordLength++;
            } else if (check.charAt(i) > 96 && check.charAt(i) < 123){
                currWordLength++;
            } else if (check.charAt(i) == '.' || check.charAt(i) == '!' || check.charAt(i) == '?')
            	currWordLength++;
            else {
                style.setCharacterAttributes(i - check.substring(0, i).split("\n").length + 1, 1, defaultStyle, true);
                if (currWordLength != 0) {
                    currWord = check.substring(i - currWordLength, i);
                    String formattedWord = currWord; // formattedWord is likely mostly redundant
                	formattedWord.replace('.', '\0');
                	formattedWord.replace('!', '\0');
                	formattedWord.replace('?', '\0');
                	formattedWord.replace('-', '\0'); // for cases such as co-worker, consistent with our global dictionary
                	// not sure about cases like free-for-all (maybe have user do free for all)
                	formattedWord = formattedWord.trim();
                    if (!textMap.containsKey(formattedWord)) {
                    	List<Integer> newKey = new ArrayList<Integer>();
                    	newKey.add(index);
                    	textMap.put(currWord, newKey);
                    }
                    else {
                    	List<Integer> currList = textMap.get(currWord);
                    	currList.add(index);
                    }
                    index++;
                    detector.updateTextList(textMap);
                    if (detector.isValidWord(currWord, index)) {
                    	style.setCharacterAttributes(i - currWordLength , currWordLength, defaultStyle, true);
                    } else {
                    	style.setCharacterAttributes(i - currWordLength - check.substring(0, i).split("\n").length + 1, currWordLength, underline, true);
                    }
                    currWordLength = 0;
                }
            }
        }
        /*for (int j = 0; j < wordList.length; j++) {
        	String currWordString = wordList[j];
        	String formattedWord = currWordString;
        	formattedWord.replace('.', '\0');
        	formattedWord.replace('!', '\0');
        	formattedWord.replace('?', '\0');
        	formattedWord.replace('-', '\0'); // for cases such as co-worker
        	// not sure about cases like free-for-all (maybe have user do free for all)
        	formattedWord = formattedWord.trim();
        	if (!textMap.containsKey(formattedWord)) {
            	List<Integer> newKey = new ArrayList<Integer>();
            	newKey.add(index);
            	textMap.put(currWord, newKey); // Need to have currWord to determine if its first word in sentence, etc.
            }
            else {
            	List<Integer> currList = textMap.get(currWord);
            	currList.add(index);
            }
            index++;
        }*/
        
    }

    public static void main(String []args){
    	spellCheck sc = new spellCheck();
    	}
}
