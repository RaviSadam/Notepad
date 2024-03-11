# Notepad
The Text Editor is a Java Swing-based application that allows users to create, edit, save, and open text files. It provides a graphical user interface (GUI) for performing various text editing operations.

## Features:

### Graphical User Interface (GUI):
The GUI is built using Java Swing components like JFrame, JTextArea, JScrollPane, JMenuBar, JMenu, JMenuItem, etc.
It provides a user-friendly interface for text editing operations.

### Text Editing:
Users can type and edit text in the main text area.
Text wrapping is enabled to ensure proper formatting of the text.

### Font and Font Size Selection:
Users can select different fonts and adjust the font size using the provided controls (JComboBox and JSpinner).
Changes in font and font size are immediately reflected in the text area.

### Text Color Selection:
Users can change the text color using the "Color" button.
A color chooser dialog is displayed to select the desired text color.

### File Operations:
Users can open existing text files (openFile menu item) and save the edited content to a file (saveFile menu item).
The application supports saving files with custom file names and extensions.

### Undo and Redo:
The "Edit" menu provides options for undoing (undo) and redoing (redo) text editing operations.
The application uses an UndoManager to manage undo and redo actions.

### Copy, Cut, and Paste:
Users can perform standard copy (copy), cut (cut), and paste (past) operations using the respective menu items.

### Search Functionality:
Users can search for specific words within the text using the "Search" menu item.
A dialog box prompts the user to enter the word to search, and the application highlights all occurrences of the word in the text area.

### Word and Line Count:
The application displays the current word count (wordsCountLabel) and line count (linesLabel) of the text being edited.
Word count is updated dynamically as the user types or modifies the text.

### Keyboard Shortcuts:
Users can utilize keyboard shortcuts (e.g., Ctrl + S for saving, Ctrl + Z for undo, etc.) for quick access to certain functionalities.
Keyboard shortcuts enhance the usability and efficiency of the text editor.

### File Locking and Management:
The application uses file locking mechanisms (FileLock) to ensure exclusive access to files being edited.
It properly releases file locks and closes file channels when files are closed or the application is terminated.

### Additional Notes:
The application's code is well-structured, with separate classes for the GUI (Gui) and background tasks (RemoveHigligh).
Multithreading is utilized for tasks like highlighting text and scheduling highlight removal.
The application follows good coding practices, including comments, proper variable naming, and exception handling.
Overall, the Text Editor provides essential text editing functionalities in a user-friendly GUI, making it suitable for basic text editing tasks.
