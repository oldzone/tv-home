# tv-home

Apple TV tarzı TV ekranı + iPhone'dan TV'ye tek dokunuşla link gönderme. Sunucu, APK veya uygulama mağazası gerekmez.

- **iPhone:** `https://oldzone.github.io/tv-home/` → Safari → Paylaş → **Ana Ekrana Ekle**
- **TV:** `https://oldzone.github.io/tv-home/tv.html` → TV tarayıcısında aç, yer imlerine ekle

## Nasıl çalışır
1. TV sayfası ilk açılışta 6 haneli bir **TV kodu** üretir ve ekranda gösterir.
2. iPhone sayfasında Ayarlar → **TV kodu** alanına bu kodu bir kez gir.
3. Linki yapıştır → **Gönder**. Link, [ntfy.sh](https://ntfy.sh) üzerinden TV'ye iletilir; TV 4 saniye geri sayımdan sonra açar (Geri tuşu iptal eder).
4. YouTube linkleri, tarayıcı destekliyorsa doğrudan YouTube TV uygulamasında açılır.

## Paylaş menüsü (Kısayol)
iPhone sayfasındaki "Paylaş menüsüne ekle" bölümündeki adımlarla bir Kestirme kurulur; ardından Safari/YouTube/Google'da **Paylaş → TV'ye Gönder** yeterli.

## Notlar
- TV kodu kanalın adıdır (`tvhome-KOD`). Kodu bilen herkes TV'ye link gönderebilir, kimseyle paylaşma. Değiştirmek için TV adresinin sonuna `#k=YENIKOD` ekle.
- Sayfa, Google TV ana ekranının yerine geçmez. Link alma, TV'de sayfa açıkken çalışır.
- Uygulama kutuları `tv.html` içindeki `APPS` listesinden düzenlenir.
