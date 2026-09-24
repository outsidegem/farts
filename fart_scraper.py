import os
import requests

LIBRARY = "/sdcard/Notifications/fart_library"
print("Scanning Internet Archive for SHORT sound effects...")

search_url = "https://archive.org/advancedsearch.php"
params = {
    "q": 'subject:"fart" AND mediatype:"audio"',
    "fl[]": "identifier",
    "rows": "60",
    "output": "json"
}

try:
    response = requests.get(search_url, params=params, timeout=10).json()
    items = response.get("response", {}).get("docs", [])
    
    count = 0
    for item in items:
        if count >= 15:
            break
            
        identifier = item["identifier"]
        meta = requests.get(f"https://archive.org/metadata/{identifier}").json()
        
        for file in meta.get("files", []):
            if file.get("name", "").endswith(".mp3"):
                size = int(file.get("size", 0))
                # THE FIX: Only allow files smaller than 150 KB (short sound effects)
                if 0 < size < 150000:
                    safe_name = f"short_track_{count}.mp3"
                    filepath = os.path.join(LIBRARY, safe_name)
                    
                    if not os.path.exists(filepath):
                        print(f"Downloading short clip: {safe_name} ({size/1000:.1f} KB)...")
                        dl_url = f"https://archive.org/download/{identifier}/{file['name']}"
                        audio_data = requests.get(dl_url, timeout=15).content
                        with open(filepath, "wb") as f:
                            f.write(audio_data)
                        count += 1
                    break # Move to next identifier
                    
    print(f"\nSuccess! {count} short sound effects are locked in.")
except Exception as e:
    print(f"Scraper error: {e}")
