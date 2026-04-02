namespace windows_client.Services;


using System.Threading.Tasks;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Http;
using System.Windows.Forms;



public class ServerService
{
    public static bool isRunning = false;
    private static WebApplication? app = null;

    public static async Task<bool> StartServer(string ip, string port, string token, Form form)
    {
        // start in background
        try
        {
            var builder = WebApplication.CreateBuilder();
            builder.WebHost.UseUrls($"http://{ip}:{port}");

            app = builder.Build();

            app.MapGet("/", () => "Hello World!");
            app.MapGet("/clipboard", (HttpContext context) =>
            {
                // get Authorization header
                var authHeader = context.Request.Headers["Authorization"].ToString();
                if (authHeader != $"Bearer {token}")
                {
                    return Results.Json(
                        new { error = "Unauthorized" },
                        statusCode: 401
                    );
                }

                string result = "";
                // clipboard can only run on sta thread; server runs on background thread
                form.Invoke(() =>
                {
                    if (Clipboard.ContainsText())
                    {
                        string clipboard = Clipboard.GetText();
                        byte[] clipboardBytes = System.Text.Encoding.UTF8.GetBytes(clipboard);
                        result = System.Convert.ToBase64String(clipboardBytes);
                    }
                });

                return Results.Json(
                    new {
                        kind = "text",
                        mime = "text/plain",
                        data_base64 = result
                    },
                    statusCode: 200
                );
            });


            app.MapPost("/clipboard", (ClipboardData data, HttpContext context) =>
            {
                var authHeader = context.Request.Headers["Authorization"].ToString();
                if (authHeader != $"Bearer {token}")
                {
                    return Results.Json(
                        new { error = "Unauthorized" },
                        statusCode: 401
                    );
                }
                
                if (data.kind == "text" && data.mime == "text/plain")
                {
                    byte[] clipboardBytes = System.Convert.FromBase64String(data.data_base64);
                    string clipboard = System.Text.Encoding.UTF8.GetString(clipboardBytes);
                    form.Invoke(() =>
                    {
                        Clipboard.SetText(clipboard);
                    });
                }
                return Results.Json(
                    new { status = "success" },
                    statusCode: 200
                );
            });

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

public record ClipboardData(string kind, string mime, string data_base64);