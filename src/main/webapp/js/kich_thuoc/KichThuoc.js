function validateForm(action) {
    // Clear error messages
    document.getElementById("errorTenKichThuoc").innerText = "";
    document.getElementById("errorMoTa").innerText = "";



    // Get values
    const tenKichThuoc = document.getElementById("tenKichThuoc").value;
    const moTa = document.getElementById("moTa").value;

    let isValid = true;

    // Validate fields
    if (tenKichThuoc.trim() === "") {
        document.getElementById("errorTenKichThuoc").innerText = "Kích thước không được để trống.";
        isValid = false;
    } else if (!isNaN(tenKichThuoc) && parseInt(tenKichThuoc, 10) < 0) {
        document.getElementById("errorTenKichThuoc").innerText = "Không được là số âm.";
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

async function loadKTData(idct) {
    console.log("ID chất liệu nhận được:", idct);
    try {
        const id = parseInt(idct, 10);
        if (isNaN(id)) {
            console.error("ID không hợp lệ:", idct);
            alert("ID kích thước không hợp lệ.");
            return;
        }
        // Gửi yêu cầu GET đến server để lấy dữ liệu
        const response = await fetch(`/san-pham/danh-sach-kich-thuoc/${id}`);

        if (!response.ok) {
            console.error("Lỗi khi tải dữ liệu kích thước:", response.statusText);
            alert(`Không thể tải dữ liệu kích thước. Mã lỗi: ${response.status}`);
            return;
        }

        const kichThuoc = await response.json();

        // Kiểm tra dữ liệu trả về
        if (!kichThuoc.tenKichThuoc || !kichThuoc.moTa) {
            console.error("Dữ liệu trả về không hợp lệ:", kichThuoc);
            alert("Không thể tải dữ liệu kích thước. Vui lòng kiểm tra lại.");
            return;
        }

        // Điền dữ liệu vào form
        document.getElementById("tenKichThuoc").value = kichThuoc.tenKichThuoc;
        document.getElementById("moTa").value = kichThuoc.moTa;

        window.currentMsId = idct; // Lưu lại ID màu sắc hiện tại
    } catch (error) {
        console.error("Đã có lỗi xảy ra khi tải dữ liệu kích thước:", error);
        alert("Không thể tải dữ liệu kích thước.");
    }
}

async function themMS() {
    var con = window.confirm("Bạn có chắc chắn muốn thêm kích thước?");
    if (con == false) {
        return;
    }

    // Thu thập dữ liệu từ form
    var payload = {
        "tenKichThuoc": document.getElementById("tenKichThuoc").value,
        "moTa": document.getElementById("moTa").value,

    };

    // Gửi dữ liệu đến server (POST request)
    const response = await fetch("/san-pham/them-kich-thuoc", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload) // Chuyển dữ liệu thành JSON
    });

    // Kiểm tra phản hồi từ server
    if (response.ok) {
        alert("Kích thước đã được thêm thành công!");
        // Chuyển hướng về trang hiển thị danh sách sản phẩm
        window.location.href = "/san-pham/kich-thuoc";
    } else {
        alert("Tên kích thước đã bị trùng.");
    }
}

async function capNhatS(idct) {
    var con = window.confirm("Bạn có chắc chắn muốn cập nhật kích thước?");
    if (con == false) {
        return;
    }

    // Thu thập dữ liệu từ form
    var payload = {
        "tenKichThuoc": document.getElementById("tenKichThuoc").value,
        "moTa": document.getElementById("moTa").value,

    };

    // Gửi dữ liệu đến server (PUT request)
    const response = await fetch(`/san-pham/cap-nhat-kich-thuoc/${idct}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload) // Chuyển dữ liệu thành JSON
    });

    // Kiểm tra phản hồi từ server
    if (response.ok) {
        alert("Kích thước đã được cập nhật thành công!");
        // Chuyển hướng về trang hiển thị danh sách sản phẩm
        window.location.href = "/san-pham/kich-thuoc";
    } else {
        alert("Tên kích thước đã bị trùng.");
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