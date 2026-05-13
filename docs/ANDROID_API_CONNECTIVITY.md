# â€œCannot reach the serverâ€ on Android (Cash Link)

The app shows this when **HTTP requests fail** (wrong URL, backend off, firewall, or wrong network).

---

## 1. Start the backend

From the `my-ledger-be` folder:

```powershell
mvn spring-boot:run
```

Wait until you see: `Started CashLinkBeApplication`.

---

## 2. Set the correct **base URL** in the Flutter app

**Do not use `localhost` or `127.0.0.1` on a real phone** â€” that refers to the phone itself, not your PC.

| Where you run the app | Base URL example |
|------------------------|------------------|
| **Android Emulator** (same PC as backend) | `http://10.0.2.2:8080/cashlink-api/` |
| **Physical phone** (USB / Wiâ€‘Fi, same network as PC) | `http://192.168.x.x:8080/cashlink-api/` |

Replace `192.168.x.x` with your PCâ€™s IPv4 **(Wiâ€‘Fi)** address:

```powershell
ipconfig
```

Look for **Wireless LAN adapter Wiâ€‘Fi** â†’ **IPv4 Address**.

---

## 3. Quick test before using the app

On the **phone** browser (same Wiâ€‘Fi), open:

`http://192.168.x.x:8080/cashlink-api/public/ping`

You should see JSON like:

```json
{"status":"ok","service":"my-ledger-be","message":"Backend is reachable"}
```

If this fails, fix the URL/network/firewall before changing app code.

---

## 4. Windows Firewall

If the phone cannot connect but the emulator can:

- Allow **inbound** TCP on port **8080** for Java / `OpenJDK` or create a rule for port 8080.

---

## 5. CORS

Native Android **does not use CORS**. That line in the error is for **web** builds.  
Your backend already allows all origins for browsers.

---

## 6. Summary checklist

| Check |
|-------|
| Backend running on PC (port 8080) |
| App uses `10.0.2.2` (emulator) or PC **LAN IP** (real device), not `localhost` |
| Phone and PC on same Wiâ€‘Fi (for physical device) |
| `/cashlink-api/public/ping` works in phone browser |
| Firewall allows port 8080 |

---

## 7. Production

Use HTTPS and your real API host, e.g. `https://api.yourdomain.com/cashlink-api/`.
