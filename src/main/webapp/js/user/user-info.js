fetch('/user-info')
    .then(response => response.text())
    .then(data => {
        document.getElementById('user-name').innerText = data;
    })
    .catch(error => console.error('Lỗi lấy user info:', error));

function confirmLogout(event) {
    event.preventDefault(); // Ngăn chặn điều hướng mặc định
    if (confirm("Bạn có chắc chắn muốn đăng xuất không?")) {
        window.location.href = "/logout"; // Chuyển hướng nếu xác nhận
    }
}
//kiem tra dang nhap cua khach hang
document.addEventListener("DOMContentLoaded", function () {
    fetch("/ttdn") // Gọi API lấy thông tin user
        .then(response => response.json())
        .then(data => {
            const userNameSpan = document.getElementById("user-name");
            const authButton = document.getElementById("auth-button");

            if (data.authenticated) {
                userNameSpan.textContent = `Xin chào, ${data.hoTen}!`;
                authButton.innerHTML = '<i class="bi bi-box-arrow-right me-1"></i> Đăng xuất';
                authButton.href = "#"; // Chặn link
                authButton.classList.remove("login");
                authButton.classList.add("logout");

                authButton.addEventListener("click", function (event) {
                    event.preventDefault();
                    fetch("/logout", { method: "POST" })
                        .then(() => {
                            window.location.href = "/ban-hang-online"; // Redirect sau khi logout
                        })
                        .catch(error => console.error("Lỗi khi đăng xuất:", error));
                });
            } else {
                userNameSpan.textContent = "";
                authButton.innerHTML = '<i class="bi bi-box-arrow-in-right me-1"></i> Đăng nhập';
                authButton.href = "/login";
                authButton.classList.remove("logout");
                authButton.classList.add("login");
            }
        })
        .catch(error => console.error("Lỗi khi lấy thông tin user:", error));
});

