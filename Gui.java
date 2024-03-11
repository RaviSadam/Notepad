import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.undo.UndoManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


class RemoveHigligh implements Runnable{

    Highlighter highlighter;

    public RemoveHigligh(Highlighter highlighter) {
        this.highlighter = highlighter;
    }
    @Override
    public void run() {
        highlighter.removeAllHighlights();
    }

}

class Gui extends JFrame implements ActionListener,WindowListener,KeyListener,DocumentListener{

    JTextArea textArea;
    JScrollPane scrollPane;
    JSpinner fontSpinner;
    JLabel fontLabel,fontSizeLabel,fontNameLabel,linesLabel,fileName,wordsCountLabel;
    JButton colorButton;
    JComboBox<String> comboBox;
    JMenuBar menuBar;
    JMenu fileMenu,editMenu;
    JMenuItem openFile,saveFile,exit,undo,redo,copy,cut,past,search;
    JPanel serchPanel;
    JTextField searchField;


    static int textAreaWidth=770,textAreaHeigth=720,TEXT_SIZE=20;
    static int MARGIN_TOP=5,MARGIN_LEFT=5,MARGIN_BOTTOM=5,MARGIN_RIGHT=5,linesCount=1,wordsCount=0;
    static Set<Integer> keyCodes=Set.of(KeyEvent.VK_BACK_SPACE,KeyEvent.VK_ALT,KeyEvent.VK_CAPS_LOCK,KeyEvent.VK_SHIFT);

    boolean cntrlPressed=false,textModified=false;
    String filePath=null;


    RandomAccessFile randomAccessFile=null;
    FileChannel fileChannel=null;
    FileLock fileLock=null;


    StringBuilder word;

    UndoManager um;

    Clipboard clipboard;

    Highlighter highlighter;
    Highlighter.HighlightPainter painter;
    ExecutorService executorService;
    RemoveHigligh removeHigligh;



    Gui(){
        executorService=Executors.newFixedThreadPool(2);
        this.word=new StringBuilder();

        clipboard=this.getToolkit().getSystemClipboard();

        //window icon
        ImageIcon imageIcon=new ImageIcon("Icon.png");
        this.setIconImage(imageIcon.getImage());

        um=new UndoManager();

        //text area
        textArea=new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Arial", Font.PLAIN, TEXT_SIZE));
        textArea.setMargin(new Insets(MARGIN_TOP,MARGIN_LEFT,MARGIN_BOTTOM,MARGIN_RIGHT));
        textArea.requestFocus();
        textArea.getDocument().addDocumentListener(this);
        textArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                um.addEdit(e.getEdit());
            }
        });
        textArea.getCaret().setBlinkRate(0);

        //scroll bar for text area
        scrollPane=new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(textAreaWidth, textAreaHeigth));  
        
        fontSizeLabel=new JLabel("Font Size: 20");
        fontSizeLabel.setForeground(Color.BLUE);
        fontNameLabel=new JLabel("Font Name: Arial");
        fontNameLabel.setForeground(Color.RED);
        linesLabel=new JLabel("Lines: 1");
        linesLabel.setForeground(Color.GREEN);
        wordsCountLabel=new JLabel("Words:0");
        wordsCountLabel.setForeground(Color.magenta);
        fontSizeLabel.setFont(new Font("Arial",Font.BOLD,15));
        fontNameLabel.setFont(new Font("Arial",Font.BOLD,15));
        linesLabel.setFont(new Font("Arial",Font.BOLD,15));
        wordsCountLabel.setFont(new Font("Arial",Font.BOLD,15));
        

        fontSpinner=new JSpinner();
        fontSpinner.setPreferredSize(new Dimension(50, 25));
        fontSpinner.setValue(20);
        fontSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                fontSizeLabel.setText("Font Size:"+fontSpinner.getValue());
                textArea.setFont(new Font(textArea.getFont().getFamily(),Font.PLAIN,(int)fontSpinner.getValue()));
            }
        });

        //color button
        colorButton=new JButton("Color");
        colorButton.addActionListener(this);

        //font chooser
        comboBox=new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        comboBox.setSelectedItem("Arial");
        comboBox.addActionListener(this);

        //font label for spinner
        fontLabel=new JLabel("Font Size:");
        
        //--------Start File Menu Bar------------//
        openFile=new JMenuItem("Open File");
        saveFile=new JMenuItem("Save File");
        exit=new JMenuItem("Exit");

        openFile.addActionListener(this);
        saveFile.addActionListener(this);
        exit.addActionListener(this);
        

        fileMenu=new JMenu("File");
        
        fileMenu.add(openFile);
        fileMenu.add(saveFile);
        fileMenu.add(exit);

        editMenu=new JMenu("Edit");

        copy=new JMenuItem("Copy");
        copy.addActionListener(this);

        cut=new JMenuItem("Cut");
        cut.addActionListener(this);

        past=new JMenuItem("Past");
        past.addActionListener(this);

        undo=new JMenuItem("Undo");
        redo=new JMenuItem("Redo");
        search=new JMenuItem("Search");

        undo.addActionListener(this);
        redo.addActionListener(this);
        search.addActionListener(this);


        editMenu.add(undo);
        editMenu.add(redo);
        editMenu.add(copy);
        editMenu.add(past);
        editMenu.add(cut);
        editMenu.add(search);


        menuBar=new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(editMenu);

        
        //--------Compelete File Menu Bar------------//


        //-------------components adding to JFrame is started------------//
        this.setJMenuBar(menuBar);
        this.add(fontLabel);
        this.add(fontSpinner);
        this.add(colorButton);
        this.add(comboBox);
        
        this.add(scrollPane);
        this.add(fontNameLabel);
        this.add(fontSizeLabel);
        this.add(linesLabel);
        this.add(wordsCountLabel);

        

        //-------------------Components adding to JFrame is completed------------------//

        //--------------------------------frame properties--------------------------//
        this.setLayout(new FlowLayout());
        this.setSize(800, 850);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setTitle("Text Editor");
        this.setVisible(true);

        //window listerer
        this.addWindowListener(this);
        textArea.addKeyListener(this);
        highlighter=textArea.getHighlighter();
        painter=new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
        removeHigligh=new RemoveHigligh(highlighter);
    }



    //------------------------------Start of Event Handlers------------------------------//


    //--------------------------------------action listener handler---------------------//
    
    public void actionPerformed(ActionEvent actionEvent){
        if(actionEvent.getSource()==openFile){
            try {
                this.openFilee();
                textModified=false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(actionEvent.getSource()==saveFile){
            try {
                this.saveFilee();
                textModified=false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(actionEvent.getSource()==comboBox){
            changeFont();
        }
        else if(actionEvent.getSource()==exit){
            
            //closing the file channel ,releasing the lock and then distroying the window
            this.fileClose();
            this.dispose();
        }
        else if(actionEvent.getSource()==undo){
            um.undo();
        }
        else if(actionEvent.getSource()==redo){
            um.redo();
        }
        else if(actionEvent.getSource()==copy){
            copyData();
        }
        else if(actionEvent.getSource()==cut){
            cutData();
        }
        else if(actionEvent.getSource()==past){
            try {
                pastData();
            } catch (UnsupportedFlavorException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(actionEvent.getSource()==search){
            String res=JOptionPane.showInputDialog(this,"Word","Search",JOptionPane.OK_CANCEL_OPTION);
            if(res==null || res.length()==0)
                return;
            else
                searchWord(res);
        }
    }


    //---------------------------window events--------------------//
    @Override
    public void windowClosing(WindowEvent e) {
        if(! textModified){
            this.dispose();
            return;
        }
        int res=JOptionPane.showConfirmDialog(this, "Your Not saved the file. Do you want to save the file", "Save File", JOptionPane.INFORMATION_MESSAGE);
        
        //save file closing file and  destroying window(terminate program)
        if(res==JOptionPane.OK_OPTION){
            try {
                this.saveFilee();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            this.fileClose();
            this.dispose();
        }
        else if(res==JOptionPane.NO_OPTION){
            //closing file and window
            this.fileClose();
            this.dispose();
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}

    //------------------------------Key events---------------//
    @Override
    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_CONTROL){
            cntrlPressed=false;
        }
    }
    
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_ENTER){
            linesCount+=1;
            linesLabel.setText("Lines:"+linesCount);
        }
        else if(e.getKeyCode()==KeyEvent.VK_CONTROL){
            cntrlPressed=true;
        }
        else if(e.getKeyCode()==KeyEvent.VK_SHIFT)
            return;
        else if(e.getKeyCode()==KeyEvent.VK_BACK_SPACE){
            int pre=linesCount;
            if(linesCount>=textArea.getLineCount()){
                linesCount=Math.max(linesCount-1,1);
            }
            if(pre==linesCount)
                wordsCount-=1;
            linesLabel.setText("Lines:"+linesCount);
        }
        else if(cntrlPressed && e.getKeyCode()==KeyEvent.VK_S){
            cntrlPressed=false;
            try {
                this.saveFilee();
                textModified=false;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        else if(cntrlPressed && e.getKeyCode()==KeyEvent.VK_F){
            String res=JOptionPane.showInputDialog(this,"Word","Search",JOptionPane.OK_CANCEL_OPTION);
            if(res==null || res.length()==0)
                return;
            else
                searchWord(res);
            
        }
        else if(cntrlPressed && e.getKeyCode()==KeyEvent.VK_O){
            cntrlPressed=false;
            try {
                this.openFilee();
                textModified=false;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        else if(cntrlPressed && e.getKeyCode()==KeyEvent.VK_C){
            copyData();
        }
        else if(cntrlPressed && e.getKeyCode()==KeyEvent.VK_V){
            try {
                pastData();
            } catch (UnsupportedFlavorException e1) {} catch (IOException e1) {e1.printStackTrace();}
        }
        else if(cntrlPressed && e.getKeyCode()==KeyEvent.VK_X){
            cutData();
        }
        else if(cntrlPressed && e.getKeyCode()==KeyEvent.VK_Z){
            um.undo();
        }
        else if(cntrlPressed && e.getKeyCode()==KeyEvent.VK_Y){
            um.redo();
        }
        else if(e.getKeyCode()==KeyEvent.VK_DELETE){
            textArea.setText("");

        }
        else if(e.getKeyCode()==KeyEvent.VK_SPACE){
            wordsCount+=1;
        }
        else if(!cntrlPressed){
            wordsCount+=1;
            textModified=true;
        }
        wordsCountLabel.setText("Words:"+wordsCount);
    }
    
    //----------------------------------------Document Handlers---------------------------------//
    @Override
    public void insertUpdate(DocumentEvent e) {
    }

    public void removeUpdate(DocumentEvent e) {
    }
    public void changedUpdate(DocumentEvent e) {}




    //----------------------------End of Event handlers -----------------------//



    //_---------------------------------------Helper methods---------------------//


    void searchWord(String word){
        try {
            highlighter.removeAllHighlights();
            int index=0,length=word.length();
            while(index!=-1){
                index=textArea.getText().indexOf(word,index);
                if(index!=-1){
                    highlighter.addHighlight(index, index+length, painter);
                    index+=length;
                }
            }

            ScheduledExecutorService exe=Executors.newScheduledThreadPool(1);
            exe.schedule(()->{
                highlighter.removeAllHighlights();
                exe.shutdown();
            },10,TimeUnit.SECONDS);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    void changeFont(){
        fontNameLabel.setText("Font Name:"+comboBox.getSelectedItem());
        textArea.setFont(new Font((String)comboBox.getSelectedItem(),Font.PLAIN,textArea.getFont().getSize()));
    }

    @SuppressWarnings("static-access")
    void changeColor(){
        JColorChooser colorChooser=new JColorChooser();
        colorChooser.setPreviewPanel(new JPanel());
        Color color=colorChooser.showDialog(this, "Choose Color", Color.GRAY);
        textArea.setForeground(color);

    }

    //save file to local 
    void saveFilee() throws IOException{

        if(filePath!=null){
            this.saveFileWithoutDialog();
            return;
        }

        JFileChooser fileChooser=new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        int res=fileChooser.showSaveDialog(this);

        if(res==JFileChooser.APPROVE_OPTION){
            filePath=fileChooser.getSelectedFile().getAbsolutePath();
            randomAccessFile=new RandomAccessFile(filePath,"rw");

            fileChannel=randomAccessFile.getChannel();
            fileLock=fileChannel.lock();

            //write data to file
            saveFileWithoutDialog();

        }
    }

    void saveFileWithoutDialog(){
        try {
            // randomAccessFile.seek(0);
            randomAccessFile.setLength(0);
            randomAccessFile.writeBytes(textArea.getText());
        } catch (IOException e) {
           e.printStackTrace();
        }
    }

    //open a file and read the append content of file to text aread
    void openFilee() throws FileNotFoundException,IOException{
        JFileChooser fileChooser=new JFileChooser(".");
        FileNameExtensionFilter filter=new FileNameExtensionFilter("Text", "txt");
        fileChooser.setFileFilter(filter);

        int res=fileChooser.showOpenDialog(this);
        if(res==JFileChooser.APPROVE_OPTION){ 
            
            //close previous file
            this.fileClose();
            
            //open file acqurie lock for channel
            filePath=fileChooser.getSelectedFile().getAbsolutePath();
            randomAccessFile=new RandomAccessFile(filePath,"rw");

            fileChannel=randomAccessFile.getChannel();
            fileLock=fileChannel.lock();
            
            byte b[]=new byte[(int)randomAccessFile.length()];
            randomAccessFile.read(b);

            //write data to text area
            textArea.setText(new String(b));
        }
    }


    //deletign a file
    void deleteFile(){
        if(filePath==null)
            return;
        
        this.fileClose();
        try {
            Files.delete(Path.of(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        filePath=null;
    }

    //releasing lock and closing file
    void fileClose(){
        if(randomAccessFile!=null){
            try {
                fileLock.release();
                fileChannel.close();
                randomAccessFile.close();

                fileChannel=null;
                fileLock=null;
                randomAccessFile=null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void copyData(){
        String text=textArea.getSelectedText();
        if(text==null)
            return;
        StringSelection selection=new StringSelection(text);
        clipboard.setContents(selection, selection);
    }
    void cutData(){
        String text=textArea.getSelectedText();
        if(text==null)
            return;
        StringSelection selection=new StringSelection(text);
        clipboard.setContents(selection, null);
        textArea.replaceRange("", textArea.getSelectionStart(), textArea.getSelectionEnd());
    }
    void pastData() throws UnsupportedFlavorException, IOException{
        textArea.paste();
    }

}