function validateForm(action) {
    document.getElementById("errorTenKhachHang").innerText = "";
    document.getElementById("errorEmail").innerText = "";
    document.getElementById("errorSoDienThoai").innerText = "";
    document.getElementById("errorDiaChi").innerText = "";
    document.getElementById("errorNgaySinh").innerText = "";
    document.getElementById("errorTaiKhoan").innerText = "";
    document.getElementById("errorMatKhau").innerText = "";
    document.getElementById("errorGioiTinh").innerText = "";

    const tenKhachHang = document.getElementById("tenKhachHang").value.trim();
    const email = document.getElementById("email").value.trim();
    const soDienThoai = document.getElementById("soDienThoai").value.trim();
    const diaChi = document.getElementById("diaChi").value.trim();
    const ngaySinh = document.getElementById("ngaySinh").value.trim();
    const taiKhoan = document.getElementById("taiKhoan").value.trim();
    const matKhau = document.getElementById("matKhau").value.trim();
    const gioiTinh = document.getElementById("gioiTinh").value.trim();

    let isValid = true;

    if (tenKhachHang === "") {
        document.getElementById("errorTenKhachHang").innerText = "Tên khách hàng không được để trống.";
        isValid = false;
    }
    if (email === "" || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        document.getElementById("errorEmail").innerText = "Email không hợp lệ.";
        isValid = false;
    }
    if (soDienThoai === "" || !/^[0-9]{10,11}$/.test(soDienThoai)) {
        document.getElementById("errorSoDienThoai").innerText = "Số điện thoại không hợp lệ.";
        isValid = false;
    }
    if (diaChi === "") {
        document.getElementById("errorDiaChi").innerText = "Địa chỉ không được để trống.";
        isValid = false;
    }
    if (ngaySinh === "") {
        document.getElementById("errorNgaySinh").innerText = "Ngày sinh không được để trống.";
        isValid = false;
    }
    if (taiKhoan === "") {
        document.getElementById("errorTaiKhoan").innerText = "Tài khoản  không được để trống.";
        isValid = false;
    }
    if (matKhau === "") {
        document.getElementById("errorMatKhau").innerText = "Mật khẩu không được để trống.";
        isValid = false;
    } else if (matKhau.length < 6 || !/[!@#$%^&*(),.?":{}|<>]/.test(matKhau)) {
        document.getElementById("errorMatKhau").innerText = "Mật khẩu phải có ít nhất 6 ký tự và chứa ký tự đặc biệt.";
        isValid = false;
    }

    if (gioiTinh === "") {
        document.getElementById("errorGioiTinh").innerText = "Giới tính không được để trống.";
        isValid = false;
    }

    if (isValid) {
        if (action === 'add') {
            themKhachHang();
        } else if (action === 'update') {
            capNhatKhachHang(window.currentNVId);
        }
    }
}


async function loadKHData(idkh) {
    console.log("ID khách hàng nhận được:", idkh); // In ra để kiểm tra idsp
    try {
        const id = parseInt(idkh, 10);
        if (isNaN(id)) {
            console.error("ID khách hàng không hợp lệ:", idkh);
            alert("ID khách hàng không hợp lệ.");
            return;
        }
        // Gửi yêu cầu GET đến server để lấy dữ liệu sản phẩm
        const response = await fetch(`/khach-hang/danh-sach-khach-hang/${id}`);

        // Kiểm tra nếu phản hồi không thành công
        if (!response.ok) {
            console.error("Lỗi khi tải dữ liệu khách hàng:", response.statusText);
            alert("Không thể tải dữ liệu khách hàng.");
            return;
        }

        const khachHang = await response.json();

        // Điền dữ liệu vào form
        document.getElementById("tenKhachHang").value = khachHang.tenKhachHang;
        document.getElementById("email").value = khachHang.email;
        document.getElementById("soDienThoai").value = khachHang.soDienThoai;
        document.getElementById("diaChi").value = khachHang.diaChi;
        document.getElementById("ngaySinh").value = khachHang.ngaySinh;
        document.getElementById("taiKhoan").value = khachHang.taiKhoan;
        document.getElementById("matKhau").value = khachHang.matKhau;
        document.getElementById("gioiTinh").value = khachHang.gioiTinh;

        window.currentNVId = idkh;
    } catch (error) {
        console.error("Đã có lỗi xảy ra khi tải dữ liệu khách hàng:", error);
        // alert("Không thể tải dữ liệu nhân viên.");
    }
}

async function themKhachHang() {
    var con = window.confirm("Bạn có chắc chắn muốn thêm khách hàng?");
    if (con == false) {
        return;
    }

    // Thu thập dữ liệu từ form
    var payload = {
        "tenKhachHang": document.getElementById("tenKhachHang").value,
        "email": document.getElementById("email").value,
        "soDienThoai": document.getElementById("soDienThoai").value,
        "diaChi": document.getElementById("diaChi").value,
        "ngaySinh": document.getElementById("ngaySinh").value,
        "taiKhoan": document.getElementById("taiKhoan").value,
        "matKhau": document.getElementById("matKhau").value,
        "gioiTinh": document.getElementById("gioiTinh").value,
    };

    // Gửi dữ liệu đến server (POST request)
    const response = await fetch("/khach-hang/them-khach-hang", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload) // Chuyển dữ liệu thành JSON
    });

    // Kiểm tra phản hồi từ server
    if (response.ok) {
        alert("Khách hàng đã được thêm thành công!");
        // Chuyển hướng về trang hiển thị danh sách sản phẩm
        window.location.href = "/khach-hang/hien-thi";
    } else {
        const message = await response.text();
        alert("Lỗi: " + message);

    }
}

async function capNhatKhachHang(idkh) {
    var con = window.confirm("Bạn có chắc chắn muốn cập nhật khách hàng?");
    if (con == false) {
        return;
    }

    // Thu thập dữ liệu từ form
    var payload = {
        "tenKhachHang": document.getElementById("tenKhachHang").value,
        "email": document.getElementById("email").value,
        "soDienThoai": document.getElementById("soDienThoai").value,
        "diaChi": document.getElementById("diaChi").value,
        "ngaySinh": document.getElementById("ngaySinh").value,
        "taiKhoan": document.getElementById("taiKhoan").value,
        "matKhau": document.getElementById("matKhau").value,
        "gioiTinh": document.getElementById("gioiTinh").value,
    };

    // Gửi dữ liệu đến server (PUT request)
    const response = await fetch(`/khach-hang/cap-nhat-khach-hang/${idkh}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload) // Chuyển dữ liệu thành JSON
    });

    // Kiểm tra phản hồi từ server
    if (response.ok) {
        alert("Khách hàng đã được cập nhật thành công!");
        // Chuyển hướng về trang hiển thị danh sách sản phẩm
        window.location.href = "/khach-hang/hien-thi";
    } else {
        const message = await response.text();
        alert("Lỗi: " + message);

    }


}
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

document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get('id'); // Lấy ID từ URL
    if (id) {
        loadKHData(id); // Gọi hàm tải dữ liệu
    }
});
