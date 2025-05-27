
    function validateForm(action) {
    console.log("validateForm called with action:", action);

    // Clear error messages
    document.getElementById("errorTenDangNhap").innerText = "";
    document.getElementById("errorMatKhau").innerText = "";
    document.getElementById("errorTenNhanVien").innerText = "";
    document.getElementById("errorChucVu").innerText = "";
    document.getElementById("errorEmail").innerText = "";
    document.getElementById("errorsoDienThoai").innerText = "";
    document.getElementById("errorNgayTao").innerText = "";
    document.getElementById("errorTrangThai").innerText = "";

    // Get values
    const tenDangNhap = document.getElementById("tenDangNhap").value.trim();
    const matKhau = document.getElementById("matKhau").value.trim();
    const tenNhanVien = document.getElementById("tenNhanVien").value.trim();
    const chucVu = document.getElementById("chucVu").value.trim();
    const email = document.getElementById("email").value.trim();
    const soDienThoai = document.getElementById("soDienThoai").value.trim();
    const ngayTao = document.getElementById("ngayTao").value.trim();
    const trangThai = document.getElementById("trangThai").value.trim();

    let isValid = true;

    // Validate tenDangNhap
    if (!tenDangNhap) {
    document.getElementById("errorTenDangNhap").innerText = "Tên đăng nhập không được để trống.";
    isValid = false;
}

    // Validate matKhau
    if (!matKhau) {
    document.getElementById("errorMatKhau").innerText = "Mật khẩu không được để trống.";
    isValid = false;
}

    // Validate tenNhanVien
    if (!tenNhanVien) {
    document.getElementById("errorTenNhanVien").innerText = "Tên nhân viên không được để trống.";
    isValid = false;
}

    // Validate chucVu
    if (!chucVu) {
    document.getElementById("errorChucVu").innerText = "Vui lòng chọn chức vụ.";
    isValid = false;
}

    // Validate email
    if (!email) {
    document.getElementById("errorEmail").innerText = "Email không được để trống.";
    isValid = false;
} else if (!/^[a-zA-Z0-9._%+-]+@gmail\.com$/.test(email)) {
    document.getElementById("errorEmail").innerText = "Email phải có định dạng hợp lệ và đuôi @gmail.com.";
    isValid = false;
}

    // Validate soDienThoai
    if (!soDienThoai) {
    document.getElementById("errorsoDienThoai").innerText = "Số điện thoại không được để trống.";
    isValid = false;
} else if (!/^\d{10}$/.test(soDienThoai)) { // Kiểm tra số điện thoại phải có đúng 10 chữ số
    document.getElementById("errorsoDienThoai").innerText = "Số điện thoại phải có 10 chữ số.";
    isValid = false;
}

    // Validate ngayTao
    if (!ngayTao) {
    document.getElementById("errorNgayTao").innerText = "Ngày tạo không được để trống.";
    isValid = false;
} else if (isNaN(Date.parse(ngayTao))) {
    document.getElementById("errorNgayTao").innerText = "Ngày tạo không hợp lệ.";
    isValid = false;
} else {
    const today = new Date().toISOString().split("T")[0];
    if (ngayTao > today) {
    document.getElementById("errorNgayTao").innerText = "Ngày tạo không được là ngày trong tương lai.";
    isValid = false;
}
}

    // Validate trangThai
    if (!trangThai) {
    document.getElementById("errorTrangThai").innerText = "Vui lòng chọn trạng thái.";
    isValid = false;
}

    // If all fields are valid, proceed with action
    if (isValid) {
    console.log("Form is valid. Proceeding with action:", action);
    if (action === 'add') {
    themSP();
} else if (action === 'update') {
    capNhatSP(window.currentNVId);
}
}
}
    async function themSP() {
        if (!window.confirm("Bạn có chắc chắn muốn thêm nhân viên?")) {
            return;
        }

        const payload = {
            tenDangNhap: document.getElementById("tenDangNhap").value,
            matKhau: document.getElementById("matKhau").value,
            tenNhanVien: document.getElementById("tenNhanVien").value,
            chucVu: document.getElementById("chucVu").value,
            email: document.getElementById("email").value,
            sdt: document.getElementById("soDienThoai").value,
            ngayTao: document.getElementById("ngayTao").value,
            trangThai: document.getElementById("trangThai").value
        };

        try {
            const response = await fetch("/nhan-vien/them-nhan-vien", {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            // Đọc phản hồi một lần và lưu lại vào biến
            const responseText = await response.text();  // Đọc và lưu lại nội dung phản hồi

            console.log('Response Status:', response.status);
            console.log('Response Body:', responseText);

            if (response.ok) {
                alert("Nhân viên đã được thêm thành công!");
                window.location.href = "/nhan-vien/hien-thi";  // Điều hướng tới trang hiển thị danh sách
            } else {
                alert("Lỗi khi thêm nhân viên: " + responseText);  // Sử dụng responseText thay vì gọi lại response.text()
            }
        } catch (error) {
            alert("Lỗi kết nối: " + error.message);
        }
    }

    async function capNhatSP(idnv) {
        if (!window.confirm("Bạn có chắc chắn muốn cập nhật nhân viên?")) {
            return;
        }

        const payload = {
            tenDangNhap: document.getElementById("tenDangNhap").value,
            matKhau: document.getElementById("matKhau").value,
            tenNhanVien: document.getElementById("tenNhanVien").value,
            chucVu: document.getElementById("chucVu").value,
            email: document.getElementById("email").value,
            sdt: document.getElementById("soDienThoai").value,
            ngayTao: document.getElementById("ngayTao").value,
            trangThai: document.getElementById("trangThai").value
        };

        try {
            const response = await fetch(`/nhan-vien/cap-nhat-nhan-vien/${idnv}`, {
                method: 'PUT',  // Sử dụng PUT thay vì POST
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });


            // Lưu lại phản hồi trong biến
            const responseText = await response.text();

            console.log('Response Status:', response.status);
            console.log('Response Body:', responseText);

            if (response.ok) {
                alert("Nhân viên đã được cập nhật thành công!");
                window.location.href = "/nhan-vien/hien-thi";  // Điều hướng tới trang hiển thị danh sách
            } else {
                alert("Lỗi khi cập nhật nhân viên: " + responseText);
            }
        } catch (error) {
            alert("Lỗi kết nối: " + error.message);
        }
    }

// Load data
        async function loadNVData(idnv) {
    try {
    const response = await fetch(`/nhan-vien/danh-sach-nhan-vien/${idnv}`);
    if (!response.ok) {
    throw new Error("Không thể tải dữ liệu nhân viên.");
}
    const taiKhoan = await response.json();

    document.getElementById("tenDangNhap").value = taiKhoan.taiKhoan;
    document.getElementById("matKhau").value = taiKhoan.matKhau;
    document.getElementById("tenNhanVien").value = taiKhoan.tenNhanVien || '';
    document.getElementById("chucVu").value = taiKhoan.chucVu || '';
    document.getElementById("email").value = taiKhoan.email;
    document.getElementById("soDienThoai").value = taiKhoan.sdt;
    document.getElementById("ngayTao").value = new Date(taiKhoan.ngayTao).toISOString().split("T")[0];
    document.getElementById("trangThai").value = taiKhoan.trangThai;

    window.currentNVId = idnv;
} catch (error) {
    console.error("Lỗi tải dữ liệu:", error);
    alert("Lỗi tải dữ liệu nhân viên: " + error.message);
}
}
    // lấy id từ đường dẫn===========================
    document.addEventListener("DOMContentLoaded", function () {
        const urlParams = new URLSearchParams(window.location.search);
        const id = urlParams.get('id'); // Lấy ID từ URL
        if (id) {
            loadNVData(id); // Gọi hàm tải dữ liệu
        }
    });

    function sortTableByName(order) {
        var table = $('#example').DataTable();
        // Cột tên NV là cột thứ 4 (index 3, 0-based)
        var colIndex = 3;
        if (order === 'az') {
            table.order([colIndex, 'asc']).draw();
        } else if (order === 'za') {
            table.order([colIndex, 'desc']).draw();
        } else {
            table.order([]).draw(); // reset sắp xếp
        }
    }

    function sortTableByChucVu(order) {
        var table = $('#example').DataTable();
        // Cột Chức vụ là cột thứ 5 (0-based index = 4)
        var colIndex = 4;
        if (order === 'asc') {
            table.order([colIndex, 'asc']).draw();
        } else if (order === 'desc') {
            table.order([colIndex, 'desc']).draw();
        } else {
            table.order([]).draw(); // reset sắp xếp
        }
    }
