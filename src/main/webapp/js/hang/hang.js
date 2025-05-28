function validateForm(action) {
    // Clear error messages
    document.getElementById("errorTenHang").innerText = "";
    document.getElementById("errorTrangThai").innerText = "";



    // Get values
    const tenHang = document.getElementById("tenHang").value;
    const trangThai = document.getElementById("trangThai").value;

    let isValid = true;

    // Validate fields
    if (tenHang.trim() === "") {
        document.getElementById("errorTenHang").innerText = "Tên hãng không được để trống.";
        isValid = false;
    } else if (!isNaN(tenHang) && parseInt(tenHang, 10) < 0) {
        document.getElementById("errorTenHang").innerText = "Không được là số âm.";
        isValid = false;
    }
    if (trangThai.trim() === "") {
        document.getElementById("errorTrangThai").innerText = "Trạng thái không được để trống.";
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

async function loadKTData(idct) {
    console.log("ID hãng nhận được:", idct);
    try {
        const id = parseInt(idct, 10);
        if (isNaN(id)) {
            console.error("ID  không hợp lệ:", idct);
            alert("ID chất liệu không hợp lệ.");
            return;
        }
        // Gửi yêu cầu GET đến server để lấy dữ liệu
        const response = await fetch(`/san-pham/danh-sach-hang/${id}`);

        if (!response.ok) {
            console.error("Lỗi khi tải dữ liệu hãng:", response.statusText);
            alert(`Không thể tải dữ liệu hãng. Mã lỗi: ${response.status}`);
            return;
        }

        const hang = await response.json();

        // Kiểm tra dữ liệu trả về
        if (!hang.tenHang || !hang.trangThai) {
            console.error("Dữ liệu trả về không hợp lệ:", hang);
            alert("Không thể tải dữ liệu chất liệu. Vui lòng kiểm tra lại.");
            return;
        }

        // Điền dữ liệu vào form
        document.getElementById("tenHang").value = hang.tenHang;
        document.getElementById("trangThai").value = hang.trangThai;

        window.currentMsId = idct; // Lưu lại ID màu sắc hiện tại
    } catch (error) {
        console.error("Đã có lỗi xảy ra khi tải dữ liệu hãng:", error);
        alert("Không thể tải dữ liệu hãng u.");
    }
}

async function themMS() {
    var con = window.confirm("Bạn có chắc chắn muốn thêm hãng?");
    if (con == false) {
        return;
    }

    // Thu thập dữ liệu từ form
    var payload = {
        "tenHang": document.getElementById("tenHang").value,
        "trangThai": document.getElementById("trangThai").value,

    };

    // Gửi dữ liệu đến server (POST request)
    const response = await fetch("/san-pham/them-hang", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload) // Chuyển dữ liệu thành JSON
    });

    // Kiểm tra phản hồi từ server
    if (response.ok) {
        alert("Hãng đã được thêm thành công!");
        // Chuyển hướng về trang hiển thị danh sách sản phẩm
        window.location.href = "/san-pham/hang";
    } else {
        alert("Tên hãng sản phẩm đã bị trùng.");
    }
}

async function capNhatS(idct) {
    var con = window.confirm("Bạn có chắc chắn muốn cập nhật hãng?");
    if (con == false) {
        return;
    }

    // Thu thập dữ liệu từ form
    var payload = {
        "tenHang": document.getElementById("tenHang").value,
        "trangThai": document.getElementById("trangThai").value,

    };

    // Gửi dữ liệu đến server (PUT request)
    const response = await fetch(`/san-pham/cap-nhat-hang/${idct}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload) // Chuyển dữ liệu thành JSON
    });

    // Kiểm tra phản hồi từ server
    if (response.ok) {
        alert("Hãng đã được cập nhật thành công!");
        // Chuyển hướng về trang hiển thị danh sách sản phẩm
        window.location.href = "/san-pham/hang";
    } else {
        alert("Tên hãng sản phẩm đã bị trùng.");
    }
}


// sắp xếp
function sortTable(order) {
    const table = document.getElementById("tableBody");
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
        loadKTData(id); // Gọi hàm tải dữ liệu
    }
});