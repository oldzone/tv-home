// Ortak uygulama listesi — hem TV ekranı hem iPhone kumandası bunu kullanır.
// domain: ikon o sitenin kendisinden çekilir. "img" verirsen kendi ikon adresin kullanılır.
// pkg: Android TV paket adı (uygulamayı doğrudan açmak için). web: uygulama yoksa açılacak site.
window.TVHOME_APPS = [
  {id:"youtube",  name:"YouTube",     domain:"youtube.com",    bg:"#ffffff", pkg:"com.google.android.youtube.tv", web:"https://www.youtube.com"},
  {id:"netflix",  name:"Netflix",     domain:"netflix.com",    bg:"#000000", pkg:"com.netflix.ninja",            web:"https://www.netflix.com"},
  {id:"disney",   name:"Disney+",     domain:"disneyplus.com", bg:"linear-gradient(160deg,#0e3a8c,#04163f)", pkg:"com.disney.disneyplus", web:"https://www.disneyplus.com"},
  {id:"prime",    name:"Prime Video", domain:"primevideo.com", bg:"linear-gradient(160deg,#1b2c3d,#0f171e)", pkg:"com.amazon.amazonvideo.livingroom", web:"https://www.primevideo.com"},
  {id:"spotify",  name:"Spotify",     domain:"spotify.com",    bg:"#121212", pkg:"com.spotify.tv.android",       web:"https://open.spotify.com"},
  {id:"tabii",    name:"tabii",       domain:"tabii.com",      bg:"#0b0b0f", pkg:null,                           web:"https://www.tabii.com"},
  {id:"google",   name:"Google",      domain:"google.com",     bg:"#ffffff", pkg:null,                           web:"https://www.google.com"},
  {id:"settings", name:"Ayarlar",     icon:"⚙︎",              bg:"linear-gradient(160deg,#8e8e93,#48484a)", settings:true}
];
window.TVHOME_NTFY = "https://ntfy.sh";
window.tvhomeIcon = d => `https://www.google.com/s2/favicons?domain=${d}&sz=256`;
