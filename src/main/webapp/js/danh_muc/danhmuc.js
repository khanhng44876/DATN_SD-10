async function validateForm(action) {
    // Xóa lỗi cũ
    document.getElementById("errorTenDanhMuc").innerText = "";
    document.getElementById("errorMoTa").innerText = "";
    document.getElementById("errorTrangThai").innerText = "";

    // Lấy dữ liệu
    const tenDanhMuc = document.getElementById("tenDanhMuc").value.trim();
    const moTa = document.getElementById("moTa").value.trim();
    const trangThai = document.getElementById("trangThai").value.trim();
    const id = (action === 'update') ? (window.currentMsId || "") : null;

    let isValid = true;

    if (!tenDanhMuc) {
        document.getElementById("errorTenDanhMuc").innerText = "Tên danh mục không được để trống.";
        isValid = false;
    }

    if (!moTa) {
        document.getElementById("errorMoTa").innerText = "Mô tả không được để trống.";
        isValid = false;
    }

    if (!trangThai) {
        document.getElementById("errorTrangThai").innerText = "Bạn phải chọn trạng thái.";
        isValid = false;
    }

    // Nếu dữ liệu đầu vào OK thì gọi API check trùng
    if (isValid) {
        try {
            const url = `/san-pham/kiem-tra-trung-danh-muc?tenDanhMuc=${encodeURIComponent(tenDanhMuc)}&moTa=${encodeURIComponent(moTa)}${id !== null ? `&id=${id}` : ''}`;
            const response = await fetch(url);
            const result = await response.json();

            if (result.tenTrung) {
                document.getElementById("errorTenDanhMuc").innerText = "Tên danh mục đã tồn tại.";
                isValid = false;
            }

            if (result.moTaTrung) {
                document.getElementById("errorMoTa").innerText = "Ghi chú đã tồn tại.";
                isValid = false;
            }

            if (isValid) {
                if (action === 'add') {
                    themMS();
                } else if (action === 'update') {
                    capNhatS(id);
                }
            }

        } catch (err) {
            console.error("Lỗi khi kiểm tra trùng:", err);
            alert("Lỗi kiểm tra trùng danh mục.");
        }
    }
}




async function themMS() {
    if (!confirm("Bạn có chắc chắn muốn thêm danh mục?")) return;

    const payload = {
        tenDanhMuc: document.getElementById("tenDanhMuc").value.trim(),
        moTa: document.getElementById("moTa").value.trim(),
        trangThai: document.getElementById("trangThai").value.trim()
    };

    try {
        const response = await fetch("/san-pham/them-danh-muc", {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            alert("Danh mục đã được thêm thành công!");
            window.location.href = "/san-pham/danh-muc";
        } else {
            alert("Đã có lỗi xảy ra khi thêm danh mục.");
        }
    } catch (err) {
        console.error("Lỗi gửi yêu cầu:", err);
        alert("Đã xảy ra lỗi kết nối.");
    }
}



async function loadDMData(iddm) {
    try {
        const response = await fetch(`/san-pham/danh-sach-danh-muc/${iddm}`);
        if (!response.ok) throw new Error("Không thể tải dữ liệu");

        const danhMuc = await response.json();

        document.getElementById("tenDanhMuc").value = danhMuc.tenDanhMuc || danhMuc.tendanhmuc || "";
        document.getElementById("moTa").value = danhMuc.moTa || danhMuc.mota || "";
        document.getElementById("trangThai").value = danhMuc.trangThai || danhMuc.trangthai || "Hoạt động";

        window.currentMsId = iddm;
    } catch (err) {
        console.error("Lỗi khi tải danh mục:", err);
        alert("Không thể tải danh mục.");
    }
}


async function capNhatS(iddm) {
    if (!confirm("Bạn có chắc chắn muốn cập nhật danh mục?")) return;

    const payload = {
        tenDanhMuc: document.getElementById("tenDanhMuc").value.trim(),
        moTa: document.getElementById("moTa").value.trim(),
        trangThai: document.getElementById("trangThai").value.trim()
    };

    const response = await fetch(`/san-pham/cap-nhat-danh-muc/${iddm}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });

    if (response.ok) {
        alert("Cập nhật thành công!");
        window.location.href = "/san-pham/danh-muc";
    } else {
        alert("Lỗi khi cập nhật.");
    }
}

// sắp xếp
function sortTable(order) {
    const table = document.getElementById("danhMucTableBody");
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
    const id = new URLSearchParams(window.location.search).get("id");
    if (id) loadDMData(id);
});

