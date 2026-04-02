#nullable enable
namespace CustomForms;

using System.Windows.Forms;
using System.Drawing;
using windows_client.Utils;
using System.Net.NetworkInformation;
using System.Linq;
using windows_client.Services;
using System;

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

    private bool CanStartServer()
    {
            bool canStart = !string.IsNullOrEmpty(portTextBox.Text) && int.TryParse(portTextBox.Text, out int port) && port > 0 && port <= 65535;
            // token must not be empty too
            canStart = canStart && !string.IsNullOrEmpty(tokenTextBox.Text);
            return canStart;
    }

    private void StopServer()
    {
        ServerService.isRunning = false;
        statusValueLabel.Text = "Stopped";
        startServerButton.Text = "Start";
        bindAddressComboBox.Enabled = true;
        portTextBox.Enabled = true;
        tokenTextBox.Enabled = true;
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

        startServerButton = CreateButton(
            text: "Start Server",
            x: 268,
            y: 272,
            width: 76,
            isAccent: true
        );
        startServerButton.Click += async (_, _) =>
        {
            if (!ServerService.isRunning)
            {
                if (!CanStartServer())
                {
                    MessageBox.Show("Please enter a valid port number (1-65535) and a non-empty API token.", "Invalid Input", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    return;
                }

                ServerService.isRunning = true;
                statusValueLabel.Text = "Running";
                startServerButton.Text = "Stop";
                bindAddressComboBox.Enabled = false;
                portTextBox.Enabled = false;
                tokenTextBox.Enabled = false;

                // start server in background
                // if it fails, then we have to stop
                var success = await ServerService.StartServer(
                    ip: GetSelectedNetworkInterface()?.IpAddress ?? "",
                    port: portTextBox.Text,
                    token: tokenTextBox.Text
                ); // server now runs asynchronously
                if (!success)
                {
                    MessageBox.Show("Failed to start server. Please check if the port is already in use or if you have the necessary permissions.", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    StopServer(); 
                }
                // start async just starts it, but doesn't keep blocking afterwards
                // so no need to handle the True case, just do nothing
            } else {
                StopServer();
                await ServerService.ShutdownServer(); // shutdown server asynchronously, but we don't need to wait for it to finish
            }
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