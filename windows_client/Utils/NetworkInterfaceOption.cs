namespace windows_client.Utils;

using System.Net.NetworkInformation;
using System.Net.Sockets;
using System.Collections.Generic;

public class NetworkInterfaceOption
{
    
    public string DisplayName { get; set; } = "";
    public string IpAddress { get; set; } = "";

    public NetworkInterfaceOption(string displayName, string ipAddress)
    {
        this.DisplayName = displayName;
        this.IpAddress = ipAddress;
    }

    public override string ToString() => $"{DisplayName} ({IpAddress})";

    public static List<NetworkInterfaceOption> GetAvailableInterfaces()
    {
        NetworkInterface[] interfaces = NetworkInterface.GetAllNetworkInterfaces();
        List<NetworkInterfaceOption> options = new List<NetworkInterfaceOption>();
        foreach (NetworkInterface nic in interfaces) {
            if (nic.OperationalStatus == OperationalStatus.Up)
            {
                IPInterfaceProperties ipProps = nic.GetIPProperties();
                foreach (UnicastIPAddressInformation ip in ipProps.UnicastAddresses)
                {
                    if (ip.Address.AddressFamily == AddressFamily.InterNetwork)
                    {
                        NetworkInterfaceOption option = new NetworkInterfaceOption(nic.Name, ip.Address.ToString());
                        options.Add(option);
                    }
                }
            }
        }
        return options;
    }
}