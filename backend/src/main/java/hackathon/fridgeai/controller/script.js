// ══════════════════════════════════════
//  Smart Fridge App – script.js
// ══════════════════════════════════════

const viewHome = document.getElementById('view-home');
const viewFridge = document.getElementById('view-fridge');
const fridgeBtn = document.getElementById('fridgeBtn');
const backBtn = document.getElementById('backBtn');
const btnCamera = document.getElementById('btnCamera');
const btnEvent = document.getElementById('btnEvent');
const btnWallet = document.getElementById('btnWallet');
const emotionFill = document.getElementById('emotionFill');
const emotionEmoji = document.getElementById('emotionEmoji');

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

// ── Khởi tạo Input để chụp ảnh Camera ──
const cameraInput = document.createElement('input');
cameraInput.type = 'file';
cameraInput.accept = 'image/*';
cameraInput.style.display = 'none';
document.body.appendChild(cameraInput);

cameraInput.addEventListener('change', async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  showToast('⏳ Đang phân tích ảnh qua AI...');
  goToFridge();
  startScanAnimation();

  const formData = new FormData();
  formData.append("file", file); // Tên tham số tương ứng trên Spring Boot (@RequestParam("file"))
  formData.append("userId", localStorage.getItem("userId") || 1);
  formData.append("fridgeId", localStorage.getItem("fridgeId") || 1);

  const token = localStorage.getItem("jwt_token");
  
  try {
    // Gọi API Scan bên backend. Đảm bảo thay đổi URL đúng với API Controller của bạn
    const response = await fetch(`/api/v1/scan/receipt`, {
        method: 'POST',
        headers: {
            "Authorization": `Bearer ${token}`,
            "ngrok-skip-browser-warning": "69420"
        },
        body: formData
    });

    if (response.ok) {
        const data = await response.json();
        showToast('✅ Quét hoàn tất! Đã lưu thông tin từ hóa đơn.');
        fetchFridgeItems(); // Reload danh sách tủ lạnh
    } else {
        showToast('❌ Xử lý ảnh thất bại!');
    }
  } catch (error) {
      console.error("Lỗi gọi API Scan:", error);
      showToast('❌ Lỗi kết nối đến máy chủ!');
  } finally {
      cameraInput.value = ''; // Reset input để có thể chụp tiếp
  }
});

// Click tủ lạnh → vào trang chi tiết
fridgeBtn.addEventListener('click', () => {
  fridgeBtn.style.transform = 'scale(0.92)';
  setTimeout(() => {
    fridgeBtn.style.transform = '';
    goToFridge();
    fetchFridgeItems(); // Gọi API lấy số lượng đồ thật trong tủ lạnh
  }, 180);
});

// Nút quay lại
backBtn.addEventListener('click', goToHome);

// Nút Camera quét ở bottom nav home
btnCamera.addEventListener('click', () => {
  showToast('📷 Đang khởi động camera...');
  cameraInput.click();
});


// Nút Ví - Tích hợp API Lấy User Profile thay vì Hardcode
btnWallet.addEventListener('click', async () => {
  showToast('⏳ Đang tải thông tin ví...');
  const userId = localStorage.getItem("userId") || 1; 
  const token = localStorage.getItem("jwt_token");

  try {
      const response = await fetch(`/api/v1/users/${userId}`, {
          headers: {
              "Authorization": `Bearer ${token}`,
              "ngrok-skip-browser-warning": "69420"
          }
      });
      if (response.ok) {
          const userData = await response.json();
          const balance = userData.walletBalance || userData.totalPoints || 0;
          showToast(`👜 Ví của bạn: ${balance} đ/điểm`);
      } else {
          showToast('❌ Lỗi tải thông tin ví!');
      }
  } catch (error) {
      showToast('❌ Lỗi kết nối API!');
  }
});

// FAB trong fridge view
const fab = document.querySelector('#view-fridge .fab');
if (fab) {
  fab.addEventListener('click', () => {
    showToast('📷 Đang khởi động camera...');
    cameraInput.click();
  });
}

// Xem công thức - Tích hợp API Lấy ngẫu nhiên công thức
const sBtn = document.querySelector('.s-btn');
if (sBtn) {
  sBtn.addEventListener('click', async () => {
    showToast('⏳ Đang tìm gợi ý công thức...');
    const token = localStorage.getItem("jwt_token");
    try {
        const response = await fetch(`/api/v1/recipes`, {
            headers: {
                "Authorization": `Bearer ${token}`,
                "ngrok-skip-browser-warning": "69420"
            }
        });
        if (response.ok) {
            const recipes = await response.json();
            if (recipes && recipes.length > 0) {
                // Random 1 món trong list
                const randomRecipe = recipes[Math.floor(Math.random() * recipes.length)];
                showToast(`🍳 Gợi ý món: ${randomRecipe.name}!`);
            } else {
                showToast('🍳 Hệ thống chưa có công thức nào.');
            }
        }
    } catch (error) {
        console.error("Lỗi lấy công thức", error);
        showToast('❌ Không tải được công thức!');
    }
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

// Hàm lấy danh sách món đồ trong Tủ Lạnh
async function fetchFridgeItems() {
    const fridgeId = localStorage.getItem("fridgeId") || 1; // Tạm dùng id = 1 hoặc lấy từ local
    const token = localStorage.getItem("jwt_token");

    try {
        const response = await fetch(`/api/v1/fridges/${fridgeId}/items`, {
            headers: {
                "Authorization": `Bearer ${token}`,
                "ngrok-skip-browser-warning": "69420"
            }
        });

        if (response.ok) {
            const items = await response.json();
            // Tạm thời chỉ hiển thị Toast thông báo số lượng thực tế
            // TODO: Bạn có thể code thêm logic parse `items` thành HTML (thẻ div .food-item) và render vào DOM
            showToast(`❄️ Tủ lạnh đang có ${items.length} món đồ.`);
        }
    } catch (error) {
        console.error("Lỗi tải danh sách tủ lạnh:", error);
    }
}

// Hàm kiểm tra thực phẩm sắp hết hạn
async function checkUnreadNotifications() {
    const userId = localStorage.getItem("userId") || 1;
    const token = localStorage.getItem("jwt_token");

    try {
        const response = await fetch(`/api/v1/notifications/users/${userId}/unread`, {
            headers: { 
                "Authorization": `Bearer ${token}`,
                "ngrok-skip-browser-warning": "69420"
            }
        });

        if (response.ok) {
            const alerts = await response.json();
            if (alerts && alerts.length > 0) {
                // Nếu có cảnh báo, đếm số lượng và hiển thị cho người dùng
                const alertCount = alerts.length;
                const firstAlertName = alerts[0].itemName || "một số thực phẩm"; // Giả sử entity có itemName
                
                showToast(`🔔 Bạn có ${alertCount} cảnh báo! ${firstAlertName} sắp hết hạn.`);
                
                // Nếu bạn có nút chuông thông báo (btnEvent), hãy cập nhật icon ở đây
                const btnEvent = document.getElementById('btnEvent');
                if (btnEvent) {
                    btnEvent.innerHTML = `🔔<span style="color:red; font-size:10px;">(${alertCount})</span>`;
                }
            }
        }
    } catch (error) {
        console.error("Lỗi lấy thông báo:", error);
    }
}