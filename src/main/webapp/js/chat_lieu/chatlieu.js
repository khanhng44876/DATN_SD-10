function validateForm(action) {
    // Clear error messages
    document.getElementById("errorChatLieu").innerText = "";
    document.getElementById("errorMoTa").innerText = "";



    // Get values
    const tenChatLieu = document.getElementById("tenChatLieu").value;
    const moTa = document.getElementById("moTa").value;

    let isValid = true;

    // Validate fields
    if (tenChatLieu.trim() === "") {
        document.getElementById("errorChatLieu").innerText = "Chất liệu không được để trống.";
        isValid = false;
    } else if (!isNaN(tenChatLieu) && parseInt(tenChatLieu, 10) < 0) {
        document.getElementById("errorChatLieu").innerText = "Chất liệu không được là số âm.";
        isValid = false;
    }
    if (moTa.trim() === "") {
        document.getElementById("errorMoTa").innerText = "Mô tả không được để trống.";
        isValid = false;
    }



    // If all fields are valid, proceed with action
    if (isValid) {
        if (action === 'add') {
            themMS();
        } else if (action === 'update') {
            capNhatS(window.currentMsId);
        }
    }
}

async function loadMSData(idct) {
    console.log("ID chất liệu nhận được:", idct);
    try {
        const id = parseInt(idct, 10);
        if (isNaN(id)) {
            console.error("ID màu sắc không hợp lệ:", idct);
            alert("ID chất liệu không hợp lệ.");
            return;
        }
        // Gửi yêu cầu GET đến server để lấy dữ liệu
        const response = await fetch(`/san-pham/danh-sach-chat-lieu/${id}`);

        if (!response.ok) {
            console.error("Lỗi khi tải dữ liệu chất liệu:", response.statusText);
            alert(`Không thể tải dữ liệu chất liệu. Mã lỗi: ${response.status}`);
            return;
        }

        const chatLieu = await response.json();

        // Kiểm tra dữ liệu trả về
        if (!chatLieu.tenChatLieu || !chatLieu.moTa) {
            console.error("Dữ liệu trả về không hợp lệ:", chatLieu);
            alert("Không thể tải dữ liệu chất liệu. Vui lòng kiểm tra lại.");
            return;
        }

        // Điền dữ liệu vào form
        document.getElementById("tenChatLieu").value = chatLieu.tenChatLieu;
        document.getElementById("moTa").value = chatLieu.moTa;

        window.currentMsId = idct; // Lưu lại ID màu sắc hiện tại
    } catch (error) {
        console.error("Đã có lỗi xảy ra khi tải dữ liệu chất liệu:", error);
        alert("Không thể tải dữ liệu chất liệu.");
    }
}

async function themMS() {
    var con = window.confirm("Bạn có chắc chắn muốn thêm chất liệu?");
    if (con == false) {
        return;
    }

    // Thu thập dữ liệu từ form
    var payload = {
        "tenChatLieu": document.getElementById("tenChatLieu").value,
        "moTa": document.getElementById("moTa").value,

    };

    // Gửi dữ liệu đến server (POST request)
    const response = await fetch("/san-pham/them-chat-lieu", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload) // Chuyển dữ liệu thành JSON
    });

    // Kiểm tra phản hồi từ server
    if (response.ok) {
        alert("Chất liệu đã được thêm thành công!");
        // Chuyển hướng về trang hiển thị danh sách sản phẩm
        window.location.href = "/san-pham/chat-lieu";
    } else {
        alert("Tên chất liệu đã bị trùng.");
    }
}

async function capNhatS(idct) {
    var con = window.confirm("Bạn có chắc chắn muốn cập nhật chất liệu?");
    if (con == false) {
        return;
    }

    // Thu thập dữ liệu từ form
    var payload = {
        "tenChatLieu": document.getElementById("tenChatLieu").value,
        "moTa": document.getElementById("moTa").value,

    };




    // Gửi dữ liệu đến server (PUT request)
    const response = await fetch(`/san-pham/cap-nhat-chat-lieu/${idct}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload) // Chuyển dữ liệu thành JSON
    });

    // Kiểm tra phản hồi từ server
    if (response.ok) {
        alert("Chất liệu đã được cập nhật thành công!");
        // Chuyển hướng về trang hiển thị danh sách sản phẩm
        window.location.href = "/san-pham/chat-lieu";
    } else {
        alert("Tên chất liệu đã bị trùng.");
    }
}

// sắp xếp
function sortTable(order) {
    const table = document.getElementById("chatLieuTableBody");
    const rows = Array.from(table.rows);

    rows.sort((a, b) => {
        const nameA = a.cells[1].innerText.trim().toLowerCase(); // Cột "Tên màu"
        const nameB = b.cells[1].innerText.trim().toLowerCase();

        if (order === "az") {
            return nameA.localeCompare(nameB);
        } else if (order === "za") {
            return nameB.localeCompare(nameA);
        } else {
            return 0;
        }
    });

    table.innerHTML = '';
    rows.forEach(row => table.appendChild(row));
}
document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get('id'); // Lấy ID từ URL
    if (id) {
        loadMSData(id); // Gọi hàm tải dữ liệu
    }
});