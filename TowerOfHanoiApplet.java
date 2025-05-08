import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;

public class TowerOfHanoiApplet extends Applet implements Runnable, ActionListener {
    int[][] rods;
    int n_disks;

    TextField disk_input;
    Label message_label;
    Button start_button;

    Thread solver_thread;
    boolean is_running = false;
    static final int DELAY = 1000;

    public void init() { // inherited from Applet
        setLayout(new BorderLayout());

        // Top panel with input and button
        Panel topPanel = new Panel(new FlowLayout());
        topPanel.add(new Label("The number of disks:"));

        disk_input = new TextField(5);
        topPanel.add(disk_input);

        start_button = new Button("Start");
        start_button.addActionListener(this);
        topPanel.add(start_button);

        // Message label in red below input
        message_label = new Label("", Label.CENTER);
        message_label.setForeground(Color.RED);

        // Use another panel to stack input and message vertically
        Panel inputPanel = new Panel(new GridLayout(2, 1));
        inputPanel.add(topPanel);
        inputPanel.add(message_label);

        add(inputPanel, BorderLayout.NORTH);
        setBackground(Color.WHITE);
    }

    public void paint(Graphics g) { // inherited from Applet
        if (rods == null)
            return;

        final int screen_width  = getWidth();
        final int[] rod_x = {screen_width  / 4, screen_width  / 2, 3 * screen_width  / 4};

        final int screen_height = getHeight();
        final int rod_width = 10;
        final int disk_height = 20;

        g.setColor(Color.BLACK);
        for (int x : rod_x) {
            g.fillRect(x - rod_width / 2, screen_height / 4, rod_width, screen_height / 2);
        }

        for (int rod = 0; rod != 3; rod++) {
            int y = screen_height * 3 / 4;
            for (int i = 0; i != n_disks; i++) {
                final int disk = rods[rod][i];
                if (disk == 0) {
                    continue;
                }

                final int disk_width = 20 + disk * 20;
                g.setColor(new Color(50 * disk % 255, 100, 150));
                g.fillRect(rod_x[rod] - disk_width / 2, y - disk_height, disk_width, disk_height);
                y -= disk_height;
            }
        }
    }

    public void run() { // inherited from Runnable
        synchronized(this) {
            is_running = true;
        }

        fill_rods();
        solve(n_disks, 0, 2, 1);

        synchronized(this) {
            is_running = false;
        }
    }

    void fill_rods() {
        rods = new int[3][n_disks];

        for (int disk_i = 0; disk_i != n_disks; disk_i++) {
            rods[0][disk_i] = n_disks - disk_i;  // top is smallest
        }
    }

    void solve(int n_disks, int from, int to, int aux) {
        if (n_disks == 0) {
            return;
        }

        solve(n_disks - 1, from, aux, to);

        repaint();

        try {
            Thread.sleep(DELAY / 2);
        } catch (InterruptedException e) {
            // do nothing
        }

        move_disk(from, to);

        try {
            Thread.sleep(DELAY / 2);
        } catch (InterruptedException e) {
            // do nothing
        }

        repaint();

        solve(n_disks - 1, aux, to, from);
    }

    void move_disk(int from, int to) {
        // searching top-down
        int fromIndex = -1;
        for (int i = n_disks - 1; i >= 0; i--) {
            if (rods[from][i] != 0) {
                fromIndex = i;
                break;
            }
        }
        if (fromIndex == -1)
            return;

        final int disk = rods[from][fromIndex];
        rods[from][fromIndex] = 0;

        // searching bottom-up
        for (int i = 0; i != n_disks; i++) {
            if (rods[to][i] == 0) {
                rods[to][i] = disk;
                break;
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        synchronized(this) {
            if (is_running)
                return;
        }

        try {
            n_disks = Integer.parseUnsignedInt(disk_input.getText());
            if (n_disks < 3) {
                throw new NumberFormatException("the number shall be not less than 3");
            }
        } catch (NumberFormatException exception) {
            message_label.setText("Enter a positive number not less than 3");
            return;
        }

        message_label.setText("");

        solver_thread = new Thread(this);
        solver_thread.start();
    }
}
