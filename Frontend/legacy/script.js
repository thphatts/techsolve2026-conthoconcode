// ══════════════════════════════════════
//  Smart Fridge App – script.js
// ══════════════════════════════════════

const viewHome   = document.getElementById('view-home');
const viewFridge = document.getElementById('view-fridge');
const fridgeBtn  = document.getElementById('fridgeBtn');
const backBtn    = document.getElementById('backBtn');
const btnCamera  = document.getElementById('btnCamera');
const btnEvent   = document.getElementById('btnEvent');
const btnWallet  = document.getElementById('btnWallet');
const emotionFill  = document.getElementById('emotionFill');
const emotionEmoji = document.getElementById('emotionEmoji');

// ── Emotion levels ──────────────────────
const emotions = [
  { level: 20,  emoji: '😴', label: 'Buồn ngủ' },
  { level: 40,  emoji: '😐', label: 'Bình thường' },
  { level: 65,  emoji: '😊', label: 'Vui' },
  { level: 85,  emoji: '😄', label: 'Rất vui' },
  { level: 100, emoji: '🥰', label: 'Hạnh phúc' },
];
let currentEmotion = 2; // index 0-4

function updateEmotion() {
  const e = emotions[currentEmotion];
  emotionFill.style.width = e.level + '%';
  emotionEmoji.textContent = e.emoji;
}
updateEmotion();

// ── Navigation helpers ─────────────────
function goToFridge() {
  viewHome.classList.remove('active');
  viewFridge.classList.add('active');
  // Scroll fridge view to top
  viewFridge.scrollTop = 0;
}

function goToHome() {
  viewFridge.classList.remove('active');
  viewHome.classList.add('active');
}

// ── Event listeners ────────────────────

// Click tủ lạnh → vào trang chi tiết
fridgeBtn.addEventListener('click', () => {
  fridgeBtn.style.transform = 'scale(0.92)';
  setTimeout(() => {
    fridgeBtn.style.transform = '';
    goToFridge();
  }, 180);
});

// Nút quay lại
backBtn.addEventListener('click', goToHome);

// Nút Camera quét ở bottom nav home
btnCamera.addEventListener('click', () => {
  showToast('📷 Đang khởi động camera quét...');
  setTimeout(goToFridge, 800);
});

// Nút Sự kiện
btnEvent.addEventListener('click', () => {
  showToast('📅 Chưa có sự kiện nào!');
});

// Nút Ví
btnWallet.addEventListener('click', () => {
  showToast('👜 Ví: 250,000đ');
});

// FAB trong fridge view
const fab = document.querySelector('#view-fridge .fab');
if (fab) {
  fab.addEventListener('click', () => {
    showToast('📷 Đang quét tủ lạnh...');
    startScanAnimation();
  });
}

// Xem công thức
const sBtn = document.querySelector('.s-btn');
if (sBtn) {
  sBtn.addEventListener('click', () => {
    showToast('🍳 Công thức: Trứng chiên cà chua!');
  });
}

// Hover food items → ripple effect
document.querySelectorAll('.food-item').forEach(item => {
  item.addEventListener('click', () => {
    const name = item.querySelector('.fname').textContent;
    const badge = item.querySelector('.food-badge').textContent;
    showToast(`${name} – ${badge}`);
  });
});

// ── Emotion cycling (auto every 5s) ────
setInterval(() => {
  currentEmotion = (currentEmotion + 1) % emotions.length;
  updateEmotion();
}, 5000);

// ── Toast notification ─────────────────
function showToast(msg) {
  // Remove existing toasts
  document.querySelectorAll('.app-toast').forEach(t => t.remove());

  const toast = document.createElement('div');
  toast.className = 'app-toast';
  toast.textContent = msg;
  Object.assign(toast.style, {
    position: 'absolute',
    bottom: '100px',
    left: '50%',
    transform: 'translateX(-50%) translateY(10px)',
    background: 'rgba(0,0,0,0.82)',
    color: '#fff',
    padding: '8px 18px',
    borderRadius: '24px',
    fontSize: '12px',
    fontWeight: '700',
    fontFamily: "'Nunito', sans-serif",
    whiteSpace: 'nowrap',
    zIndex: '999',
    opacity: '0',
    transition: 'opacity 0.25s, transform 0.25s',
    pointerEvents: 'none',
  });
  document.querySelector('.phone').appendChild(toast);

  // Animate in
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      toast.style.opacity = '1';
      toast.style.transform = 'translateX(-50%) translateY(0)';
    });
  });

  // Animate out
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(-50%) translateY(-6px)';
    setTimeout(() => toast.remove(), 300);
  }, 2200);
}

// ── Scan animation ─────────────────────
function startScanAnimation() {
  const scanLine = document.querySelector('#view-fridge .scan-line');
  if (!scanLine) return;
  scanLine.style.animationDuration = '0.4s';
  scanLine.style.boxShadow = '0 0 20px #00e5a0, 0 0 40px #00e5a066';
  setTimeout(() => {
    scanLine.style.animationDuration = '2s';
    scanLine.style.boxShadow = '0 0 8px #00e5a0';
    showToast('✅ Quét xong! Tìm thấy 12 món.');
  }, 1200);
}

// ── Swipe gesture support ──────────────
let touchStartX = 0;
const phone = document.querySelector('.phone');

phone.addEventListener('touchstart', e => {
  touchStartX = e.touches[0].clientX;
}, { passive: true });

phone.addEventListener('touchend', e => {
  const dx = e.changedTouches[0].clientX - touchStartX;
  // Swipe right on fridge view → go back home
  if (dx > 60 && viewFridge.classList.contains('active')) {
    goToHome();
  }
}, { passive: true });

console.log('🐱 Smart Fridge App loaded!');