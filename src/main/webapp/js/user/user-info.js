document.addEventListener("DOMContentLoaded", function () {
    fetch("/ttdn")
        .then(response => response.json()) // Không cần kiểm tra response.ok vì server luôn trả 200
        .then(data => {
            console.log("Dữ liệu từ /ttdn:", data);
            const userNameSpan = document.getElementById("user-name");
            const authButton = document.getElementById("auth-button");
            if (!userNameSpan || !authButton) {
                console.error("Không tìm thấy #user-name hoặc #auth-button trong DOM");
                return;
            }
            // Kiểm tra authenticated để xác định trạng thái
            if (data.authenticated === true) {
                userNameSpan.textContent = `Xin chào, ${data.hoTen || "Khách"}!`;
                authButton.innerHTML = '<i class="bi bi-box-arrow-right me-1"></i> Đăng xuất';
                authButton.href = "#";
                authButton.classList.remove("login");
                authButton.classList.add("logout");

                // Xử lý sự kiện đăng xuất
                authButton.onclick = function (event) {
                    event.preventDefault();
                    fetch("/logout", { method: "POST" })
                        .then(() => {
                            window.location.href = "/ban-hang-online";
                        })
                        .catch(error => console.error("Lỗi khi đăng xuất:", error));
                };
            } else {
                userNameSpan.textContent = "";
                authButton.innerHTML = '<i class="bi bi-box-arrow-in-right me-1"></i> Đăng nhập';
                authButton.href = "/login";
                authButton.classList.remove("logout");
                authButton.classList.add("login");
                authButton.onclick = null; // Xóa sự kiện nếu có
            }
        })
        .catch(error => {
            console.error("Lỗi khi lấy thông tin user:", error);
            // Đặt giao diện về trạng thái mặc định (chưa đăng nhập) khi có lỗi
            const userNameSpan = document.getElementById("user-name");
            const authButton = document.getElementById("auth-button");
            userNameSpan.textContent = "";
            authButton.innerHTML = '<i class="bi bi-box-arrow-in-right me-1"></i> Đăng nhập';
            authButton.href = "/login";
            authButton.classList.remove("logout");
            authButton.classList.add("login");
            authButton.onclick = null;
        });
});