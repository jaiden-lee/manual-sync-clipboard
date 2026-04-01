#nullable enable
namespace CustomForms;

using System.Windows.Forms;
using System.Drawing;
using windows_client.Utils;

partial class MainForm
{
    private System.ComponentModel.IContainer components = null;

    private NotifyIcon trayIcon;
    private ContextMenuStrip trayMenu;
    private bool allowExit = false;
    private Label titleLabel = null!;
    private Label bindAddressLabel = null!;
    private ComboBox bindAddressComboBox = null!;
    private Label portLabel = null!;
    private TextBox portTextBox = null!;
    private Label tokenLabel = null!;
    private TextBox tokenTextBox = null!;
    private CheckBox showTokenCheckBox = null!;
    private Label statusCaptionLabel = null!;
    private Label statusValueLabel = null!;
    private Button saveButton = null!;
    private Button startServerButton = null!;
    private readonly Color bgMain = Color.FromArgb(20, 20, 20);
    private readonly Color bgCard = Color.FromArgb(30, 30, 30);
    private readonly Color accent = Color.FromArgb(255, 60, 80);
    private readonly Color textPrimary = Color.White;
    private readonly Color textSecondary = Color.FromArgb(180, 180, 180);

    protected override void Dispose(bool disposing)
    {
        if (disposing && (components != null))
        {
            components.Dispose();
        }
        base.Dispose(disposing);
    }

    private void InitializeComponent()
    {
        components = new System.ComponentModel.Container();
        AutoScaleMode = AutoScaleMode.Font;
        ClientSize = new Size(640, 400);
        Text = "Clipboard Sync";
        MaximizeBox = false;
        StartPosition = FormStartPosition.CenterScreen;
        FormBorderStyle = FormBorderStyle.FixedSingle;
        ShowInTaskbar = true;
        BackColor = Color.FromArgb(20, 20, 20);
        ForeColor = Color.White;

        InitializeTray();
        InitializeUi();
    }

    public NetworkInterfaceOption? GetSelectedNetworkInterface()
    {
        if (bindAddressComboBox.SelectedItem is NetworkInterfaceOption option)
        {
            return option;
        }
        return null;
    }

    private void InitializeTray()
    {
        trayMenu = new ContextMenuStrip();
        trayMenu.Items.Add("Open", null, (_, __) =>
        {
            Show();
            WindowState = FormWindowState.Normal;
            ShowInTaskbar = true;
            Activate();
        });

        trayMenu.Items.Add("Exit", null, (_, __) =>
        {
            allowExit = true;
            trayIcon.Visible = false;
            Application.Exit();
        });

        trayIcon = new NotifyIcon();
        trayIcon.Text = "Clipboard Sync";
        trayIcon.Icon = SystemIcons.Application;
        trayIcon.ContextMenuStrip = trayMenu;
        trayIcon.Visible = true;

        trayIcon.DoubleClick += (_, __) =>
        {
            Show();
            WindowState = FormWindowState.Normal;
            ShowInTaskbar = true;
            Activate();
        };
    }

    protected override void OnFormClosing(FormClosingEventArgs e)
    {
        if (!allowExit && e.CloseReason == CloseReason.UserClosing)
        {
            e.Cancel = true;
            Hide();
            ShowInTaskbar = false;
            return;
        }

        base.OnFormClosing(e);
    }

    private void InitializeUi()
    {
        SuspendLayout();

        titleLabel = new Label
        {
            Text = "Clipboard Sync",
            AutoSize = true,
            Font = new Font("Segoe UI", 15, FontStyle.Bold),
            ForeColor = textPrimary,
            Location = new Point(24, 20)
        };

        bindAddressLabel = new Label
        {
            Text = "Bind Address",
            AutoSize = true,
            Font = new Font("Segoe UI", 9, FontStyle.Regular),
            ForeColor = textSecondary,
            Location = new Point(28, 70)
        };

        bindAddressComboBox = new ComboBox
        {
            DropDownStyle = ComboBoxStyle.DropDownList,
            FlatStyle = FlatStyle.Flat,
            BackColor = bgCard,
            ForeColor = textPrimary,
            Size = new Size(400, 28),
            Location = new Point(28, 92)
        };

        // Placeholder items for now
        var nics = windows_client.Utils.NetworkInterfaceOption.GetAvailableInterfaces();
        if (nics.Count == 0)
        {
            bindAddressComboBox.Items.Add("no interfaces available");
            
        } else
        {
            foreach (var nic in nics)
            {
                bindAddressComboBox.Items.Add(nic);
            }
        }
        bindAddressComboBox.SelectedIndex = 0;
        

        portLabel = new Label
        {
            Text = "Port",
            AutoSize = true,
            Font = new Font("Segoe UI", 9, FontStyle.Regular),
            ForeColor = textSecondary,
            Location = new Point(28, 132)
        };

        portTextBox = new TextBox
        {
            Text = "8787",
            BorderStyle = BorderStyle.FixedSingle,
            BackColor = bgCard,
            ForeColor = textPrimary,
            Size = new Size(140, 27),
            Location = new Point(28, 154)
        };

        tokenLabel = new Label
        {
            Text = "API Token",
            AutoSize = true,
            Font = new Font("Segoe UI", 9, FontStyle.Regular),
            ForeColor = textSecondary,
            Location = new Point(28, 194)
        };

        tokenTextBox = new TextBox
        {
            BorderStyle = BorderStyle.FixedSingle,
            BackColor = bgCard,
            ForeColor = textPrimary,
            Size = new Size(400, 27),
            Location = new Point(28, 216),
            UseSystemPasswordChar = true
        };

        showTokenCheckBox = new CheckBox
        {
            Text = "Show token",
            AutoSize = true,
            Font = new Font("Segoe UI", 9, FontStyle.Regular),
            ForeColor = textSecondary,
            BackColor = bgMain,
            Location = new Point(28, 248)
        };
        showTokenCheckBox.CheckedChanged += (_, _) =>
        {
            tokenTextBox.UseSystemPasswordChar = !showTokenCheckBox.Checked;
        };

        statusCaptionLabel = new Label
        {
            Text = "Status:",
            AutoSize = true,
            Font = new Font("Segoe UI", 9, FontStyle.Regular),
            ForeColor = textSecondary,
            Location = new Point(28, 280)
        };

        statusValueLabel = new Label
        {
            Text = "Not running",
            AutoSize = true,
            Font = new Font("Segoe UI", 9, FontStyle.Bold),
            ForeColor = accent,
            Location = new Point(78, 280)
        };

        saveButton = CreateButton(
            text: "Save",
            x: 268,
            y: 272,
            width: 76,
            isAccent: false
        );
        saveButton.Click += (_, _) =>
        {
            statusValueLabel.Text = "Saved";
        };

        startServerButton = CreateButton(
            text: "Start Server",
            x: 352,
            y: 272,
            width: 76,
            isAccent: true
        );
        startServerButton.Click += (_, _) =>
        {
            statusValueLabel.Text = "Running";
        };

        Controls.Add(titleLabel);
        Controls.Add(bindAddressLabel);
        Controls.Add(bindAddressComboBox);
        Controls.Add(portLabel);
        Controls.Add(portTextBox);
        Controls.Add(tokenLabel);
        Controls.Add(tokenTextBox);
        Controls.Add(showTokenCheckBox);
        Controls.Add(statusCaptionLabel);
        Controls.Add(statusValueLabel);
        Controls.Add(saveButton);
        Controls.Add(startServerButton);

        ResumeLayout(false);
        PerformLayout();
    }

    private Button CreateButton(string text, int x, int y, int width, bool isAccent)
    {
        Button button = new Button
        {
            Text = text,
            Size = new Size(width, 32),
            Location = new Point(x, y),
            FlatStyle = FlatStyle.Flat,
            ForeColor = textPrimary,
            BackColor = isAccent ? accent : bgCard
        };

        button.FlatAppearance.BorderSize = 0;
        button.TabStop = false;

        return button;
    }
}