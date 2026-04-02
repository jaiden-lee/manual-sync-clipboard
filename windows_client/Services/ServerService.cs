namespace windows_client.Services;

using System;
using System.Reflection.Metadata.Ecma335;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Windows.Forms;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Hosting;

public class ServerService
{
    public static bool isRunning = false;
    private static WebApplication? app = null;

    public static async Task<bool> StartServer(string ip, string port, string token)
    {
        // start in background
        try
        {
            var builder = WebApplication.CreateBuilder();
            builder.WebHost.UseUrls($"http://{ip}:{port}");

            app = builder.Build();

            await app.StartAsync(); // will immediately throw error if failed
            // return true or success, false if failed to start server (port in use)
        } catch
        {
            return false;
        }
        return true;
    }

    public static async Task ShutdownServer()
    {
        if (app != null)
        {
            await app.StopAsync();
            await app.DisposeAsync();
            app = null;
            isRunning = false;
        }
    }

}