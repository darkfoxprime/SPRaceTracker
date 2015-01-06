/**
 * 
 */
package com.github.jearls.SPRaceTracker.ui;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.GroupLayout;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position.Bias;

/**
 * <p>
 * This presents a dialog box to choose the database directory for the
 * SPRaceTracker data store. The dialog box allows a directory name to be
 * entered - but forces the last component (specified in the
 * chooseDatabaseDirectory method as the "dbDirName") to be left unmodified.
 * There is also a button to bring up a directory file chooser dialog to locate
 * the parent of the database directory.
 * </p>
 * <p>
 * Sample usage is:
 * </p>
 * 
 * <pre>
 * File dbDir =
 *         DatabaseDirectoryChooser.chooseDatabaseDirectory(&quot;DatabaseDirectory&quot;);
 * if (dbDir == null) {
 *     System.err.println(&quot;Cancelled by user&quot;);
 * } else {
 *     System.err.println(&quot;User chose: &quot; + dbDir.getAbsolutePath());
 * }
 * </pre>
 * 
 * @author jearls
 *
 */
/**
 * @author jearls
 *
 */
public class DatabaseDirectoryChooser extends JDialog implements ActionListener {
    public static final long serialVersionUID = 1L;

    /**
     * This filter ensures that the caret in the text field is never allowed to
     * enter the final component of the directory name.
     * 
     * @author jearls
     *
     */
    class DirectoryNavigationFilter extends NavigationFilter {
        JTextField inputField;

        public DirectoryNavigationFilter(JTextField inputField) {
            this.inputField = inputField;
        }

        /**
         * Ensures that the caret ("dot") is not moved beyond the final
         * occurrence of the file separator.
         * 
         * @see javax.swing.text.NavigationFilter#setDot(javax.swing.text.NavigationFilter.FilterBypass,
         *      int, javax.swing.text.Position.Bias)
         */
        @Override
        public void setDot(FilterBypass fb, int dot, Bias bias) {
            String path = inputField.getText();
            int inviolateSection = path.lastIndexOf(File.separator);
            if (dot > inviolateSection)
                dot = inviolateSection;
            super.setDot(fb, dot, bias);
        }

        /**
         * Ensures that the caret ("dot") is not moved beyond the final
         * occurrence of the file separator.
         * 
         * @see javax.swing.text.NavigationFilter#moveDot(javax.swing.text.NavigationFilter.FilterBypass,
         *      int, javax.swing.text.Position.Bias)
         */
        @Override
        public void moveDot(FilterBypass fb, int dot, Bias bias) {
            String path = inputField.getText();
            int inviolateSection = path.lastIndexOf(File.separator);
            if (dot > inviolateSection)
                dot = inviolateSection;
            super.moveDot(fb, dot, bias);
        }
    }

    /**
     * The verifier for the full directory name. Ensures that the parent
     * directory exists and that the final component of the path does not. When
     * verified, this also leaves the File to the full path behind in the public
     * fullPath variable.
     * 
     * @author jearls
     *
     */
    class DirectoryVerifier extends InputVerifier {
        /**
         * The full path as verified.
         */
        public File   fullPath;

        /**
         * The component in which to report verification failures
         */
        public JLabel status;

        public DirectoryVerifier(JLabel status) {
            super();
            this.status = status;
        }

        /**
         * Verifies that the JTextField input records a path whose parent
         * directory exists.
         * 
         * @param input
         *            The JTextField input holding the directory name
         * @return true if the parent directory is valid.
         * @see javax.swing.InputVerifier#verify(javax.swing.JComponent)
         */
        @Override
        public boolean verify(JComponent input) {
            return verify(input, false);
        }

        /**
         * Verifies that the JTextField input records a path whose parent
         * directory exists. If fullPathMustNotExist is true, then also verifies
         * that the full path does not exist.
         * 
         * @param input
         *            The JTextField input holding the directory name
         * @param fullPathMustNotExist
         *            If false, only checks the parent directory. If true, also
         *            checks that the full path does not exist.
         * @return true if the parent directory is valid and, if
         *         fullPathMustNotExist is true, that the full path does not
         *         exist.
         */
        public boolean verify(JComponent input, boolean fullPathMustNotExist) {
            fullPath = new File(((JTextField) input).getText());
            if (!fullPath.getParentFile().isDirectory()) {
                status.setText(fullPath.getParentFile().toString()
                        + " does not exist.");
                return false;
            } else if (fullPathMustNotExist && fullPath.exists()) {
                status.setText(fullPath.toString() + " already exists.");
                return false;
            }
            return true;
        }
    }

    class EscapeOrEnterKeyListener extends KeyAdapter {
        JButton invokedOnEnter;
        JButton invokedOnEscape;

        public EscapeOrEnterKeyListener(JButton invokedOnEnter,
                JButton invokedOnEscape) {
            super();
            this.invokedOnEnter = invokedOnEnter;
            this.invokedOnEscape = invokedOnEscape;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                invokedOnEscape.doClick();
            } else if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                invokedOnEnter.doClick();
            } else {
                super.keyPressed(e);
            }
        }
    }

    /* convenience constants for later. these should be i18n'd and l10n'd */
    static final String chooseButtonText      = "Browse";
    static final String okButtonText          = "OK";
    static final String useExistingButtonText = "Use Existing";
    static final String cancelButtonText      = "Cancel";
    static final String directoryLabelText    = "Database Directory:";
    static final String instructionLabelText  =
                                                      "<html><p>Choose the directory which will hold the database.  The last portion of the database path (&quot;.../"
                                                              + "%1$s"
                                                              + "&quot;) cannot be modified.</p><p>Alternately, use the "
                                                              + chooseButtonText
                                                              + " button to bring up a directory selection dialog where you can choose in which directory the &quot;"
                                                              + "%1$s"
                                                              + "&quot; database directory will be created</p></html>";
    static final String fileChooserTitle      =
                                                      "Choose where to create the \"%1$s\" directory";

    /* the UI components and assistant classes */
    JButton             chooseButton          = null;
    JButton             okButton              = null;
    JButton             useExistingButton     = null;
    JButton             cancelButton          = null;
    JTextField          directoryEntry        = null;
    DirectoryVerifier   directoryVerifier     = null;

    /* The dialog components */
    String              dbDirName;
    public File         dbDir;

    /**
     * Creates a new DatabaseDirectoryChooser dialog and displays it as a modal
     * dialog.
     * 
     * @param dbDirName
     *            The last component of the database directory, which is fixed
     *            and cannot be changed by the user.
     * @param defaultParentDir
     *            The default parent directory which will contain the
     *            "dbDirName" directory.
     */
    public DatabaseDirectoryChooser(String dbDirName, File defaultParentDir) {
        // initialize the basic JDialog
        super();
        // verify that the dbDirName is a valid name
        try {
            new File(dbDirName).getCanonicalFile();
            if (dbDirName.indexOf(File.separator) > -1) { throw new IOException(); }
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid dbDirName \""
                    + dbDirName + "\"");
        }
        // save the constructor parameters
        this.dbDirName = dbDirName;
        this.dbDir = defaultParentDir;
        // everything else gets put into a different method, in case I need
        // multiple constructors
        setupUI();
    }

    /**
     * Creates a new DatabaseDirectoryChooser dialog and displays it as a modal
     * dialog. Defaults to the user's home directory, or "." if the user's home
     * directory cannot be determined.
     * 
     * @param dbDirName
     *            The last component of the database directory, which is fixed
     *            and cannot be changed by the user.
     */
    public DatabaseDirectoryChooser(String dbDirName) {
        this(dbDirName, new File(System.getProperty("user.home", ".")));
    }

    /**
     * Set up the dialog box components
     */
    void setupUI() {
        // set my title
        setTitle("Choose where to create the \"" + dbDirName + "\" directory");
        // make sure I'm a modal dialog
        setModal(true);
        // set the window close action to nothing
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        // but catch the window closing event and have it invoke the cancel
        // button, if it exists.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if (cancelButton == null) {
                    e.getWindow().dispose();
                } else {
                    cancelButton.doClick();
                }
            }
        });
        // Create the individual components that will be added to the dialog
        JLabel instructionLabel =
                new JLabel(
                        String.format(instructionLabelText, dbDirName));
        JLabel directoryLabel = new JLabel(directoryLabelText);
        JLabel statusLabel = new JLabel(" ");
        // The buttons: Each button gets this class as an action listener, and
        // an EscapeOrEnterKeyListener to invoke that button on enter or the
        // cancel button on escape.
        // The cancel button must be created first, as it's used in the key
        // listeners for the rest.
        cancelButton = new JButton(cancelButtonText);
        cancelButton.addActionListener(this);
        cancelButton.addKeyListener(new EscapeOrEnterKeyListener(cancelButton,
                cancelButton));
        okButton = new JButton(okButtonText);
        okButton.addActionListener(this);
        okButton.addKeyListener(new EscapeOrEnterKeyListener(okButton,
                cancelButton));
        useExistingButton = new JButton(useExistingButtonText);
        useExistingButton.addActionListener(this);
        useExistingButton.addKeyListener(new EscapeOrEnterKeyListener(
                useExistingButton, cancelButton));
        chooseButton = new JButton(chooseButtonText);
        chooseButton.addActionListener(this);
        chooseButton.addKeyListener(new EscapeOrEnterKeyListener(chooseButton,
                cancelButton));
        // the text field is more complicated: it needs to have both the
        // escapeOrEnterKeyListener, as well as an input verifier and a
        // navigation filter to prevent the cursor from being placed in the
        // dbDirName portion of the path.
        directoryEntry = new JTextField();
        directoryEntry.setColumns(40);
        directoryEntry.setText(dbDir.getAbsolutePath() + File.separator
                + dbDirName);
        directoryEntry.addKeyListener(new EscapeOrEnterKeyListener(
                useExistingButton, cancelButton));
        directoryVerifier = new DirectoryVerifier(statusLabel);
        directoryEntry.setInputVerifier(directoryVerifier);
        directoryEntry.setNavigationFilter(new DirectoryNavigationFilter(
                directoryEntry));
        // now we have to put this stuff together :P
        // whee, I get to learn about the GroupLayout manager
        GroupLayout gl = new GroupLayout(getContentPane());
        getContentPane().setLayout(gl);
        gl.setAutoCreateContainerGaps(true);
        ;
        gl.setAutoCreateGaps(true);
        gl.setHorizontalGroup(gl
                .createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(instructionLabel)
                .addGroup(
                        gl.createSequentialGroup().addComponent(directoryLabel)
                                .addComponent(directoryEntry)
                                .addComponent(chooseButton))
                .addGroup(
                        gl.createSequentialGroup().addComponent(cancelButton)
                                .addComponent(okButton)
                                .addComponent(useExistingButton))
                .addComponent(statusLabel));
        gl.setVerticalGroup(gl
                .createSequentialGroup()
                .addComponent(instructionLabel)
                .addGroup(
                        gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(directoryLabel)
                                .addComponent(directoryEntry)
                                .addComponent(chooseButton))
                .addGroup(
                        gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(cancelButton)
                                .addComponent(okButton)
                                .addComponent(useExistingButton))
                .addComponent(statusLabel));
        gl.linkSize(SwingConstants.HORIZONTAL, cancelButton, okButton,
                chooseButton);
        // pack the layout
        pack();
        // center it on the screen
        DisplayMode dm = this.getGraphicsConfiguration().getDevice().getDisplayMode();
        int screenWidth = dm.getWidth();
        int screenHeight = dm.getHeight();
        Dimension windowSize = getSize();
        setLocation((screenWidth - windowSize.width)/2, (screenHeight - windowSize.height)/2);
        // and show it to run the dialog
        setVisible(true);
    }

    /**
     * <p>
     * Listens for button presses and takes action according to which button:
     * </p>
     * <dl>
     * <dt>cancelButton</dt>
     * <dd>Shows a confirmation dialog asking if the user really wants to exit.</dd>
     * <dt>okButton</dt>
     * <dd>Runs the verifier and, if it's okay, uses its verified file to record
     * into dbDir.</dd>
     * <dt>chooseButton</dt>
     * <dd>Creates the JFileChooser dialog and then transfers the resulting
     * information back into the ui components.</dd>
     * </dl>
     * 
     * @param e
     *            The ActionEvent triggered by the button press.
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelButton) {
            // use a confirmation dialog to make sure the user really wants to
            // exit
            int response =
                    JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to exit?",
                            "Are you sure you want to exit?",
                            JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                // set the dbDir to null to indicate the dialog was cancelled
                this.dbDir = null;
                // dispose the dialog
                this.dispose();
            }
        } else if (e.getSource() == okButton) {
            // use the directory verifier to make sure we're valid. For this
            // verification, the full path must not exist.
            if (directoryVerifier.verify(directoryEntry, true)) {
                // save the verified directory file
                this.dbDir = directoryVerifier.fullPath;
                // and dispose the dialog
                this.dispose();
            }
        } else if (e.getSource() == useExistingButton) {
            // use the directory verifier to make sure we're valid. For this
            // verification, we don't check the full path.
            if (directoryVerifier.verify(directoryEntry, false)) {
                // save the verified directory file
                this.dbDir = directoryVerifier.fullPath;
                // and dispose the dialog
                this.dispose();
            }
        } else if (e.getSource() == chooseButton) {
            // create the JFileChooser dialog
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle(String.format(fileChooserTitle, dbDirName));
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setCurrentDirectory(dbDir);
            // bring up the dialog
            int response = fc.showSaveDialog(this);
            // if the user approved the selection, copy the information back to
            // the ui components
            if (response == JFileChooser.APPROVE_OPTION) {
                dbDir = fc.getSelectedFile();
                if (dbDir.getName().equals(dbDirName)) {
                    directoryEntry.setText(dbDir.getAbsolutePath());
                } else {
                    directoryEntry.setText(dbDir.getAbsolutePath()
                            + File.separator + dbDirName);
                }
                directoryEntry.requestFocusInWindow();
                directoryEntry.setCaretPosition(directoryEntry.getText()
                        .length());
            }
        }
    }

    /**
     * A convenience function that creates the dialog and returns the resulting
     * dbDir File.
     * 
     * @param dbDirName
     *            The database directory name that will be created.
     * @param defaultDir
     *            The default directory in which the database directory will be
     *            created.
     * @return the dbDir File.
     */
    public static File
            chooseDatabaseDirectory(String dbDirName, File defaultDir) {
        DatabaseDirectoryChooser dialog =
                new DatabaseDirectoryChooser(dbDirName, defaultDir);
        return dialog.dbDir;
    }

    /**
     * A convenience function that creates the dialog and returns the resulting
     * dbDir File. The default parent directory is either the user's home
     * directory or, if that cannot be determined, the current directory when
     * the application was started.
     * 
     * @param dbDirName
     *            The database directory name that will be created.
     * @return the dbDir File.
     */
    public static File chooseDatabaseDirectory(String dbDirName) {
        DatabaseDirectoryChooser dialog =
                new DatabaseDirectoryChooser(dbDirName);
        return dialog.dbDir;
    }
}
