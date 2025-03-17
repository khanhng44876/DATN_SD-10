function validateForm(action) {
    // Clear error messages
    document.getElementById("errorTenDanhMuc").innerText = "";
    document.getElementById("errorMoTa").innerText = "";



    // Get values
    const tenDanhMuc = document.getElementById("tenDanhMuc").value;
    const moTa = document.getElementById("moTa").value;

    let isValid = true;

    // Validate fields
    if (tenDanhMuc.trim() === "") {
        document.getElementById("errorTenDanhMuc").innerText = "Danh mục không được để trống.";
        isValid = false;
    } else if (!isNaN(tenDanhMuc) && parseInt(tenDanhMuc, 10) < 0) {
        document.getElementById("errorTenDanhMuc").innerText = "Không được là số âm.";
        isValid = false;
    }
    if (moTa.trim() === "") {
        document.getElementById("errorMoTa").innerText = "Không được để trống.";
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

async function loadDMData(iddm) {
    console.log("ID Danh mục nhận được:", iddm);
    try {
        const id = parseInt(iddm, 10);
        if (isNaN(id)) {
            console.error("ID Danh mục không hợp lệ:", iddm);
            alert("ID Danh mục không hợp lệ.");
            return;
        }
        // Gửi yêu cầu GET đến server để lấy dữ liệu
        const response = await fetch(`/san-pham/danh-sach-danh-muc/${id}`);

        if (!response.ok) {
            console.error("Lỗi khi tải dữ liệu danh mục:", response.statusText);
            alert(`Không thể tải dữ liệu danh mục. Mã lỗi: ${response.status}`);
            return;
        }

        const danhMuc = await response.json();

        // Kiểm tra dữ liệu trả về
        if (!danhMuc.tenDanhMuc || !danhMuc.moTa) {
            console.error("Dữ liệu trả về không hợp lệ:", danhMuc);
            alert("Không thể tải dữ liệu danh mục. Vui lòng kiểm tra lại.");
            return;
        }

        // Điền dữ liệu vào form
        document.getElementById("tenDanhMuc").value = danhMuc.tenDanhMuc;
        document.getElementById("moTa").value = danhMuc.moTa;

        window.currentMsId = iddm; // Lưu lại ID màu sắc hiện tại
    } catch (error) {
        console.error("Đã có lỗi xảy ra khi tải dữ liệu danh mục:", error);
        // alert("Không thể tải dữ liệu danh mục.");
    }
}

async function themMS() {
    var con = window.confirm("Bạn có chắc chắn muốn thêm danh mục?");
    if (con == false) {
        return;
    }

    // Thu thập dữ liệu từ form
    var payload = {
        "tenDanhMuc": document.getElementById("tenDanhMuc").value,
        "moTa": document.getElementById("moTa").value,

    };

    // Gửi dữ liệu đến server (POST request)
    const response = await fetch("/san-pham/them-danh-muc", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload) // Chuyển dữ liệu thành JSON
    });

    // Kiểm tra phản hồi từ server
    if (response.ok) {
        alert("Danh mục đã được thêm thành công!");
        // Chuyển hướng về trang hiển thị danh sách sản phẩm
        window.location.href = "/san-pham/danh-muc";
    } else {
        alert("Đã có lỗi xảy ra khi thêm danh mục.");
    }
}

async function capNhatS(iddm) {
    var con = window.confirm("Bạn có chắc chắn muốn cập nhật danh mục?");
    if (con == false) {
        return;
    }

    // Thu thập dữ liệu từ form
    var payload = {
        "tenDanhMuc": document.getElementById("tenDanhMuc").value,
        "moTa": document.getElementById("moTa").value,

    };

    // Gửi dữ liệu đến server (PUT request)
    const response = await fetch(`/san-pham/cap-nhat-danh-muc/${iddm}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload) // Chuyển dữ liệu thành JSON
    });

    // Kiểm tra phản hồi từ server
    if (response.ok) {
        alert("Danh mục đã được cập nhật thành công!");
        // Chuyển hướng về trang hiển thị danh sách sản phẩm
        window.location.href = "/san-pham/danh-muc";
    } else {
        alert("Đã có lỗi xảy ra khi cập nhật danh mục.");
    }
}
document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get('id'); // Lấy ID từ URL
    if (id) {
        loadDMData(id); // Gọi hàm tải dữ liệu
    }
});