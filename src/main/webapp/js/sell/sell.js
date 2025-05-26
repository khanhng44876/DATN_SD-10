let orders = JSON.parse(localStorage.getItem("orders")) || [];
let videoStream = null;

// Hàm quét QR
function openQrScanner() {
    let activeTab = document.querySelector("#nav-tab .nav-link.active");
    if (!activeTab) {
        alert("Vui lòng chọn hoặc tạo một đơn hàng trước!");
        return;
    }

    const qrModalEl = document.getElementById('qrScannerModal');
    const qrModal = new bootstrap.Modal(qrModalEl);
    qrModal.show();
    document.getElementById('qrMessage').innerText = 'Đặt mã QR vào khung hình';
    document.getElementById('qrMessage').style.color = 'black';

    navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })
        .then(stream => {
            videoStream = stream;
            const video = document.getElementById('qrVideo');
            video.srcObject = stream;
            video.play();
            scanQrCode();
        })
        .catch(err => {
            document.getElementById('qrMessage').innerText = 'Không thể truy cập camera: ' + err.message;
            document.getElementById('qrMessage').style.color = 'red';
        });
}

function stopQrScanner() {
    if (videoStream) {
        videoStream.getTracks().forEach(track => track.stop());
        videoStream = null;
    }
}

function scanQrCode() {
    const video = document.getElementById('qrVideo');
    const canvas = document.getElementById('qrCanvas');
    const context = canvas.getContext('2d');

    function tick() {
        if (video.readyState === video.HAVE_ENOUGH_DATA) {
            canvas.height = video.videoHeight;
            canvas.width = video.videoWidth;
            context.drawImage(video, 0, 0, canvas.width, canvas.height);
            const imageData = context.getImageData(0, 0, canvas.width, canvas.height);
            const code = jsQR(imageData.data, imageData.width, imageData.height);

            if (code) {
                const productId = extractProductId(code.data);
                if (productId) {
                    addProductFromQr(productId);
                    bootstrap.Modal.getInstance(
                        document.getElementById('qrScannerModal')
                    ).hide();
                    stopQrScanner();
                    return;
                } else {
                    document.getElementById('qrMessage').innerText = 'Mã QR không hợp lệ';
                    document.getElementById('qrMessage').style.color = 'red';
                }
            }
        }
        requestAnimationFrame(tick);
    }
    requestAnimationFrame(tick);
}

function extractProductId(qrData) {
    const match = qrData.match(/(\d+)/);
    return match ? match[0] : null;
}

async function addProductFromQr(productId) {
    try {
        // Lấy tab đang hoạt động
        let activeTab = document.querySelector("#nav-tab .nav-link.active");
        if (!activeTab) {
            throw new Error('Vui lòng chọn một đơn hàng trước!');
        }
        let orderId = parseInt(activeTab.getAttribute("data-order-id"));
        let order = orders.find(o => o.id === orderId);
        if (!order) {
            throw new Error('Không tìm thấy đơn hàng!');
        }

        // Lấy chi tiết sản phẩm từ API
        const response = await fetch(`/ban-hang-off/detail/${productId}`, {
            headers: { 'Accept': 'application/json' }
        });
        if (!response.ok) throw new Error('Sản phẩm không tồn tại');
        const product = await response.json();

        // Kiểm tra số lượng tồn kho
        const quantityToAdd = 1; // Mặc định thêm 1 sản phẩm
        if (product.soLuong < quantityToAdd) {
            throw new Error('Sản phẩm đã hết hàng!');
        }

        // Cập nhật số lượng tồn kho
        const updateResponse = await fetch(`/ban-hang-off/update-sp/${productId}/${quantityToAdd}`, {
            method: 'PUT',
            headers: { 'Accept': 'application/json' }
        });
        if (!updateResponse.ok) throw new Error('Không thể cập nhật số lượng: Hết hàng hoặc lỗi hệ thống');
        const updatedProduct = await updateResponse.json();

        // Thêm sản phẩm vào mảng orders.product
        const productData = {
            id: product.id,
            anh_san_pham: product.anhSanPham,
            ten_san_pham: product.sanPham.tenSanPham,
            mau_sac: product.mauSac.ten_mau_sac,
            kich_thuoc: product.kichThuoc.tenKichThuoc,
            don_gia: product.donGia,
            so_luong: quantityToAdd,
            tong_tien: product.donGia * quantityToAdd
        };

        // Kiểm tra xem sản phẩm đã tồn tại trong đơn hàng chưa
        let existingProduct = order.product.find(p => Number(p.id) === Number(productId));
        if (existingProduct) {
            existingProduct.so_luong += quantityToAdd;
            existingProduct.tong_tien = existingProduct.so_luong * existingProduct.don_gia;
        } else {
            order.product.push(productData);
        }

        // Cập nhật tổng tiền và khuyến mãi
        updateThanhTien(orderId);
        await updateDiscount(order);

        // Lưu vào localStorage và làm mới giao diện
        saveOrderToLocalStorage();
        await renderOrderDetails(orderId);
    } catch (err) {
        alert('Lỗi: ' + err.message);
    }
}
// Hàm load lại đơn hàng
async function renderOrders() {
    // Giữ tab active
    const active = document.querySelector("#nav-tab .nav-link.active");
    const activeId = active?.getAttribute("data-order-id");

    // Sắp xếp theo id tăng dần
    orders.sort((a, b) => a.id - b.id);

    // Xóa cũ
    document.getElementById("nav-tab").innerHTML = "";
    document.getElementById("nav-tabContent").innerHTML = "";

    // Duyệt orders
    for (const order of orders) {
        updateThanhTien(order.id);
        await updateDiscount(order);
        createElementOrder(order);
        renderOrderDetails(order.id);
    }

    // Restore tab active
    if (activeId) {
        setTimeout(() => {
            document.querySelector(`#li-${activeId} .nav-link`)?.click();
        }, 50);
    }
}
function renderOrderDetails(orderId) {
    const order = orders.find(o => o.id === orderId);
    if (!order) return;
    // Cập nhật tổng tiền hiển thị
    document.getElementById(`amount-${orderId}`).innerText =
        order.totalAmount.toLocaleString("vi-VN");
    // Xóa & vẽ lại list sản phẩm
    const list = document.getElementById(`product-list-${orderId}`);
    list.innerHTML = "";
    for (const p of order.product) {
        const item = document.createElement("div");
        item.className = "row align-items-center gx-2 gy-1 ";
        item.innerHTML = `
      <div class='col-2'><img src='../../images/${p.anh_san_pham}' style='width:90px;height:90px'></div>
      <div class='col-4'>
        <h6>${p.ten_san_pham}</h6>
        <div class='text-danger fw-bold'>${p.don_gia.toLocaleString()} VND</div>
        <div>Size: ${p.kich_thuoc}</div>
      </div>
      <div class='col-2 text-center'>
        <button onclick='updateQuantity(${orderId},${p.id},-1)'>-</button>
        <span>${p.so_luong}</span>
        <button onclick='updateQuantity(${orderId},${p.id},+1)'>+</button>
      </div>
      <div class='col-3 text-end text-danger fw-bold'>${p.tong_tien.toLocaleString()} VND</div>
      <div class='col-1'><button class='btn btn-sm btn-danger' onclick='removeProduct(${orderId},${p.id})'>🗑</button></div>
      <hr>
    `;
        list.appendChild(item);
    }
}

// Xóa sản phẩm trong đơn hàng
async function removeProduct(orderId,productId) {
    let order = orders.find(o => o.id === Number(orderId));
    if (!order) {
        console.error("❌ Không tìm thấy đơn hàng!");
        return;
    }
    const product = order.product.find(p => Number(p.id) === Number(productId));
    if(!product){
        console.error("Không tìm thấy sản pham");
        return;
    }

    const resp = await fetch(`/ban-hang-off/remove-sp/${Number(product.id)}/${Number(product.so_luong)}`,{
        method : "PUT"
    });
    if(!resp.ok){
        throw new Error("Lỗi" + resp.status);
    }

    // Sửa logic lọc sản phẩm
    order.product = order.product.filter(p => Number(p.id) !== Number(productId));
    // Cập nhật localStorage và giao diện
    saveOrderToLocalStorage();
    renderOrders();
}

// Thêm dữ liệu đơn hàng vào LocalStorage
function saveOrderToLocalStorage(){
    localStorage.setItem("orders",JSON.stringify(orders))
}

// Hàm tạo đơn hàng
function createOrder() {
    if (orders.length >= 5) {
        alert("Tối đa 5 đơn");
        return;
    }
    const orderId = orders.length ? orders[orders.length - 1].id + 1 : 1;
    const order = {
        id: orderId,
        name: `Đơn hàng ${orderId}`,
        product: [],
        customer: { id: 1, name: "Khách lẻ" },
        totalAmount: 0,
        discount: { id: null, ma: "", phan_tram: 0, tien_giam: 0 },
        tien_phai_tra: 0,
        hinh_thuc_thanh_toan: null
    };
    orders.push(order);
    saveOrderToLocalStorage();
    renderOrders();
}

// Hàm tạo các thành phần trong đơn hàng
function createElementOrder(order) {
    if (document.getElementById(`li-${order.id}`)
        || document.getElementById(`content-${order.id}`)) {
        return;
    }
    // Tab item
    const li = document.createElement("li");
    li.className = "nav-item";
    li.id = `li-${order.id}`;
    const tab = document.createElement("button");
    tab.className = "nav-link";
    tab.setAttribute("data-bs-toggle", "tab");
    tab.setAttribute("data-bs-target", `#content-${order.id}`);
    tab.setAttribute("data-order-id", order.id);
    tab.innerHTML = `${order.name} <span class='text-danger' onclick='removeOrder(${order.id})'>❌</span>`;
    li.appendChild(tab);
    document.getElementById("nav-tab").appendChild(li);

    // Content pane
    const content = document.createElement("div");
    content.className = "tab-pane fade";
    content.id = `content-${order.id}`;
    content.innerHTML = `
    <div class='d-flex justify-content-between align-items-center mt-3'>
      <h4>Sản phẩm</h4>
      <div>
        <button class='btn btn-warning' onclick='reloadProductModal()' data-bs-toggle='modal' data-bs-target='#spModal'>+ Thêm SP</button>
        <button class='btn btn-primary' onclick='openQrScanner()'><i class='fas fa-qrcode'></i> Quét QR</button>
      </div>
    </div>
    <hr>
    <div class='row'>
      <div class='col-md-8'>
        <div id='product-list-${order.id}' class='product-list'></div>
        <p class='mt-2 text-danger'><strong>Thành tiền: </strong><span id='amount-${order.id}'>0</span> VND</p>
      </div>
      <div class='col-md-4'>
        <!-- Thông tin đơn hàng bên phải -->
        <h4>Thông tin đơn hàng</h4><hr>
        <div class='d-flex justify-content-between align-items-center'>
          <h5>Khách hàng</h5>
          <button class='btn btn-warning' data-bs-toggle='modal' data-bs-target='#khModal'>Chọn khách</button>
        </div>
        <p><strong>Tên:</strong> ${order.customer.name}</p>
        <p><input id='ma-km-${order.id}' placeholder='Mã khuyến mãi' value='${order.discount.ma}' readonly>
           <input id='phan-tram-${order.id}' value='${order.discount.phan_tram}% ' readonly></p>
        <p>Tiền hàng: <span id='amount-${order.id}'>${order.totalAmount.toLocaleString("vi-VN")}</span> VND</p>
        <p>Giảm giá: <span id='tien-giam-${order.id}'>${order.discount.tien_giam.toLocaleString("vi-VN")}</span> VND</p>
        <h5 class='text-danger'>Tổng: <span id='totalAmount-${order.id}'>${order.tien_phai_tra.toLocaleString("vi-VN")}</span> VND</h5><hr>
        <p>Thanh toán:
          <input type='radio' name='httt-${order.id}' value='Tiền mặt' onchange='paymentChange(this,${order.id})'> Tiền mặt
          <input type='radio' name='httt-${order.id}' value='Chuyển khoản' onchange='paymentChange(this,${order.id})'> Chuyển khoản
          <span class="text-danger" id="error-httt-${order.id}"></span>
        </p>
        <div id='httt-${order.id}'></div>
        <button class='btn btn-success' onclick='confirmOrder(${order.id})'>Xác nhận</button>
      </div>
      <!-- Thông tin đơn hàng bên phải giữ nguyên -->
    </div>
  `;
    document.getElementById("nav-tabContent").appendChild(content);
}


function paymentChange(radio,orderId){
    let value = radio.value;
    let order = orders.find(o=>o.id===orderId);
    order.hinh_thuc_thanh_toan = value;
    saveOrderToLocalStorage()
    renderPaymentMethod(order)
}

async function renderPaymentMethod(order){
    let methodDiv = document.getElementById(`httt-${order.id}`);
    methodDiv.innerHTML = '';
    if(order.hinh_thuc_thanh_toan){
        if(order.hinh_thuc_thanh_toan === "Tiền mặt"){
            methodDiv.innerHTML=`
             <p>Tiền khách đưa : 
                 <input type="text" id="customer-pay-${order.id}">
             </p>
             <span style="color: red" id="error-tkd-${order.id}"></span>
             <p>Tiền thừa : 
                 <input type="text" id="refund-money-${order.id}" readonly>
             </p>
        `;
        }else{
            const res = await fetch(`/ban-hang-off/generate-qr/${order.tien_phai_tra}`);
            const { redirectUrl } = await res.json();
            order.qrUrl = redirectUrl;
            saveOrderToLocalStorage();
            methodDiv.innerHTML = `
              <canvas id="qr-${order.id}"></canvas>
              <p>Quét QR để thanh toán ${order.tien_phai_tra.toLocaleString()} VND</p>
            `;
            // Nếu đã có URL thanh toán sandbox (order.qrUrl), thì vẽ QR
            if (order.qrUrl) {
                QRCode.toCanvas(
                    document.getElementById(`qr-${order.id}`),
                    order.qrUrl,
                    { width: 300,
                        // Margin viền trắng chỉ 1 module
                        margin: 1,
                        // Chỉ sửa lỗi tối thiểu, máy quét vẫn đọc được
                        errorCorrectionLevel: 'L',
                        // Màu đen-trắng cơ bản
                        color: {
                            dark: '#000000',
                            light: '#FFFFFF'
                        }
                    },
                    err => err && console.error('QR lỗi:', err)
                );
            }
        }
    }
}

// Hàm xóa đơn hàng
async function removeOrder(orderId){
    const order = orders.find(o => o.id === orderId);
    const resp = order.product.map(p =>{
        return fetch(`/ban-hang-off/remove-sp/${Number(p.id)}/${Number(p.so_luong)}`,{
            method : "PUT"
        });
    });
    await Promise.all(resp);
    orders = orders.filter(order => order.id !== orderId)
    saveOrderToLocalStorage()
    renderOrderDetails(order.id)
}

// Hàm cập nhật số lượng ngay tren giao diện
async function updateQuantity(orderId,productId,change){
    let order =orders.find(o=> o.id === orderId)
    if(!order){
        console.log("không tìm thấy order")
        return;
    }
    let product = order.product.find(p => Number(p.id) === Number(productId));
    if (!product){
        console.log("không tìm thấy product")
        return;
    }
    const resp = await fetch(`/ban-hang-off/update-sp/${Number(product.id)}/${change}`,{
        method : "PUT"
    })
    if (!resp.ok) {
        alert("Số lượng trong kho đã hết");
        return;
    }
    product.so_luong = Math.max(1,Number(product.so_luong) + change)
    product.tong_tien = Number(product.so_luong) * product.don_gia;
    saveOrderToLocalStorage()
    updateThanhTien(orderId)
    renderOrderDetails(orderId)
}

// Hàm mở form Nhập số lượng
function openModalQuantity(button){

    let id = button.getAttribute("data-id")
    fetch(`/ban-hang-off/detail/${id}`,{
        method : "GET"
    })
        .then(response => response.json())
        .then(data => {
            console.log(data)
            document.getElementById("ctsp_id").value = data.id
            document.getElementById("anhsp_id").value = data.anhSanPham
            document.getElementById("ten_sp").innerText = data.sanPham.tenSanPham;
            document.getElementById("mau_sac").innerText = data.mauSac.ten_mau_sac;
            document.getElementById("kich_thuoc").innerText = data.kichThuoc.tenKichThuoc;
            document.getElementById("don_gia").innerText = data.donGia;
            document.getElementById("so_luong").innerText = data.soLuong;
        })
        .catch(error => console.error("Lỗi",error))
}

// Hàm Tính tiền hàng
function updateThanhTien(orderId){
    let order = orders.find(o=>o.id===orderId)
    if(!order){
        console.log("Khong tim thay don hang")
        return
    }
    order.totalAmount = order.product.reduce((sum, p) => sum + (p.so_luong * p.don_gia), 0)
    saveOrderToLocalStorage()
}

// Hàm chọn khách hàng
function selectKH(button){
    let idKH = button.getAttribute("data-id");
    let tenKH = button.getAttribute("data-name");
    let activeTab = document.querySelector("#nav-tab .nav-link.active")
    if(!activeTab){
        alert("Vui lòng chọn một đơn hàng trước!");
        return;
    }
    let orderId = parseInt(activeTab.getAttribute("data-order-id"))
    let order = orders.find(o=>o.id===orderId)
    if (!order) {
        console.error("Không tìm thấy đơn hàng!");
        return;
    }
    order.customer.id = Number(idKH)
    order.customer.name = tenKH
    saveOrderToLocalStorage()
    renderOrderDetails(orderId)
    let modal = bootstrap.Modal.getInstance(document.getElementById('khModal'));
    modal.hide()
}

// Hàm để tự động cập nhật khuyến mãi
async function updateDiscount(order) {  // Thêm async vào đây
    let totalAmount = order.totalAmount;

    try {
        let response = await fetch(`/ban-hang-off/best-km/${totalAmount}`); // Dùng await
        let data = await response.json(); // Dùng await để lấy JSON

        order.discount = {
            id: data.id || null,
            ma: data.ma_khuyen_mai || "",
            phan_tram: data.phan_tram_giam || 0,
            tien_giam: data.tien_giam || 0
        };
        order.tien_phai_tra = totalAmount - (data.tien_giam || 0);

        saveOrderToLocalStorage();
    } catch (error) {
        console.error("Lỗi khi lấy khuyến mãi:", error);
    }
}

// Phần input nhập số tiền khách đưa
document.addEventListener("focus", function (event) {
    if (event.target.matches("[id^=customer-pay-]")) {
        event.target.value = event.target.value.replace(" VND", ""); // Bỏ " VND"
    }
}, true);
document.addEventListener("blur", function (event) {
    if (event.target.matches("[id^=customer-pay-]")) {
        let value = event.target.value.replace(/\D/g, ""); // Lọc bỏ tất cả ký tự không phải số
        if (value.trim() !== "") {
            event.target.value = Number(value).toLocaleString("vi-VN") + " VND"; // Định dạng lại số tiền
        }
    }
}, true);
document.addEventListener("input", function (event) {
    if (event.target.matches("[id^=customer-pay-]")) {
        let orderId = parseInt(event.target.id.replace("customer-pay-", ""));
        let order = orders.find(o => o.id === orderId);
        if (!order) return;

        let customerPay = parseFloat(event.target.value.replace(/\D/g, "")) || 0;
        let finalTotal = order.tien_phai_tra;
        let change = customerPay - finalTotal;

        event.target.value = customerPay.toLocaleString("vi-VN") + " VND";
        document.getElementById(`refund-money-${order.id}`).value = change.toLocaleString("vi-VN") + " VND";
    }
});

async function confirmOrder(orderId) {
    const order = orders.find(o => o.id === orderId);
    // 1. Validate
    if (!order || order.product.length === 0) {
        return alert("Vui lòng thêm sản phẩm vào đơn hàng");
    }
    if (!validateOrder(orderId)) {
        return alert("Vui lòng nhập đủ thông tin thanh toán");
    }

    const idnv = document.getElementById("idNv").value;
    const paymentMethod = order.hinh_thuc_thanh_toan;
    if (paymentMethod === null) {
        return alert("Vui lòng chọn hình thức thanh toán");
    }

    const today = new Date().toISOString().split("T")[0];
    // Dữ liệu chung chung cho cả offline & online
    const hdJson = {
        id_nhan_vien: Number(idnv),
        id_khach_hang: order.customer.id,
        id_khuyen_mai: order.discount.id,
        ngay_tao: today,
        tong_tien: order.tien_phai_tra,
        trang_thai_thanh_toan: "Đã thanh toán",
        hinh_thuc_thanh_toan: paymentMethod,
        dia_chi_giao_hang: "Tại cửa hàng",
        ghi_chu: null
    };

    try {
        // Nếu chuyển khoản: gọi API online sandbox để lấy redirectUrl
        if (paymentMethod === "Chuyển khoản") {
            const res = await fetch("/ban-hang-online/create-order", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(hdJson)
            });
            if (!res.ok) throw new Error("Lỗi tạo đơn online");
            const result = await res.json();
            if (!result.redirectUrl) throw new Error("Server không trả redirectUrl");

            // Gán QR URL và hiển thị QR
            order.qrUrl = result.redirectUrl;
            saveOrderToLocalStorage();
            renderPaymentMethod(order);
            return; // dừng ở đây, chờ người dùng quét QR
        }

        // Ngược lại: flow Tiền mặt (offline)
        // 2. Tạo hoá đơn offline
        const resOffline = await fetch("/ban-hang-off/add-hoa-don", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(hdJson)
        });
        if (!resOffline.ok) throw new Error("Lỗi tạo đơn tiền mặt");
        const hoaDon = await resOffline.json();
        const idHoaDon = hoaDon.id;
        if (!idHoaDon) throw new Error("Không lấy được ID hóa đơn");

        // 3. Tạo chi tiết & cập nhật tồn kho, KM
        const tasks = order.product.map(p => {
            const hdct = {
                id_hoa_don: idHoaDon,
                id_san_pham_chi_tiet: Number(p.id),
                so_luong: p.so_luong,
                don_gia: p.don_gia,
                tong_tien: p.tong_tien,
                thanh_tien: p.tong_tien,
                ngay_tao: today,
                ngay_sua: null,
                trang_thai: "Đã thanh toán"
            };
            const req1 = fetch("/ban-hang-off/add-hoa-don-ct", {
                method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(hdct)
            });
        });
        if (order.discount.id !== null) {
            tasks.push(fetch(`/ban-hang-off/update-km/${order.discount.id}`, { method: "PUT" }));
        }
        await Promise.all(tasks.flat());

        // 4. Kết thúc offline: in hoá đơn, xoá order, render lại
        alert("Xác nhận đơn hàng thành công (Tiền mặt)!");
        printInvoice(orderId);
        orders = orders.filter(o => o.id !== orderId);
        saveOrderToLocalStorage();
        await renderOrders();
    } catch (err) {
        console.error(err);
        alert("Lỗi: " + err.message);
    }
}

// Hàm validate
function validateOrder(orderId) {
    let order = orders.find(o => o.id === orderId);
    if (!order) return false;
    let paymentMethods = document.querySelectorAll(`[name="hinhthuctt"]:checked`);
    let customerPayInput = document.getElementById(`customer-pay-${orderId}`);
    let errorHttt = document.getElementById(`error-httt-${orderId}`);
    errorHttt.innerText = "";

    // Kiểm tra hình thức thanh toán
    if (order.hinh_thuc_thanh_toan !== "Tiền mặt" && order.hinh_thuc_thanh_toan !== "Chuyển khoản") {
        errorHttt.innerText = "Vui lòng chọn hình thức thanh toán!";
        return false;
    }
    if(order.hinh_thuc_thanh_toan === "Tiền mặt"){
        let errorTkd = document.getElementById(`error-tkd-${orderId}`);
        errorTkd.innerText = "";
    }

    // Kiểm tra tiền khách đưa
    let customerPay = parseFloat(customerPayInput.value.replace(/\D/g, "")) || 0;

    let finalTotal = order.tien_phai_tra;
    if (customerPay < finalTotal) {
        errorTkd.innerText = "Số tiền khách đưa không đủ!";
        return false;
    }

    return true;
}

// Hàm in hóa đơn Sang file PDF
function printInvoice(orderId) {
    if(!validateOrder(orderId)) return
    let order = orders.find(o => o.id === orderId);
    if (!order) {
        alert("Không tìm thấy đơn hàng!");
        return;
    }

    let invoiceHTML = `
        <html>
        <head>
            <title>Hóa đơn bán hàng</title>
            <style>
                body { font-family: Arial, sans-serif; padding: 20px; }
                .invoice-box { width: 80%; margin: auto; border: 1px solid #ccc; padding: 20px; }
                h2, h3 { text-align: center; }
                table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                table, th, td { border: 1px solid black; padding: 8px; text-align: center; }
                .total { font-weight: bold; }
                .logo { text-align: center; }
                .logo img { width: 100px; }
            </style>
        </head>
        <body>
            <div class="invoice-box">
                <div class="logo">
                    <img src="/images/logo.png" alt="Logo">
                </div>
                <h2>Sky Football Fashion</h2>
                <p>📞 0123456789 | 📧 skyfootballfashion8386@gmail.com</p>
                <p>🏠 Địa chỉ: FPT Polytechnic Cơ Sở Kiều Mai, Từ Liêm, Hà Nội</p>
                <h3>HÓA ĐƠN BÁN HÀNG</h3>

                <p><strong>Tên khách hàng:</strong> ${order.customer || "Khách lẻ"}</p>
                <p><strong>Địa chỉ nhận hàng:</strong> ${order.address || "Tại cửa hàng"}</p>
                <p><strong>Mã hóa đơn:</strong> HD${order.id}</p>
                <p><strong>Ngày tạo:</strong> ${new Date().toLocaleString()}</p>
                <p><strong>Trạng thái:</strong> Hoàn thành</p>

                <table>
                    <tr>
                        <th>STT</th>
                        <th>Tên sản phẩm</th>
                        <th>Số lượng</th>
                        <th>Đơn giá</th>
                        <th>Thành tiền</th>
                    </tr>
                    ${order.product.map((product, index) => `
                        <tr>
                            <td>${index + 1}</td>
                            <td>${product.ten_san_pham}</td>
                            <td>${product.so_luong}</td>
                            <td>${product.don_gia.toLocaleString()} VND</td>
                            <td>${product.tong_tien.toLocaleString()} VND</td>
                        </tr>
                    `).join("")}
                </table>

                <p class="total">Tổng tiền hàng: ${order.totalAmount.toLocaleString()} VND</p>
                <p class="total">Giảm giá: ${order.discount ? order.discount.tien_giam.toLocaleString() : "0"} VND</p>
                <p class="total">Phí giao hàng: 0 VND</p>
                <p class="total">Tổng tiền cần thanh toán: ${order.tien_phai_tra.toLocaleString()} VND</p>
                <h4>Cảm ơn quý khách đã mua hàng!</h4>
            </div>
            <script>window.print();</script>
        </body>
        </html>
    `;

    let printWindow = window.open('about:blank', '_blank', 'width=900,height=700');
    printWindow.document.open();
    printWindow.document.write(invoiceHTML);
    printWindow.document.close();
}

async function reloadProductModal(){
    const resp = await fetch(`/ban-hang-off/reload-product-modal`,{
        method : "PUT"
    });
    if(!resp.ok){
        throw new Error (`Lỗi ${resp.status}`)
    }

    const data = await resp.json();

    const tbody = document.getElementById('spModalBody');
    tbody.innerHTML = '';
    data.forEach(d => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
        <td>${d.id}</td>
        <td>${d.sanPham.tenSanPham}</td>
        <td>${d.mauSac.ten_mau_sac}</td>
        <td>${d.kichThuoc.tenKichThuoc}</td>
        <td>${d.chatLieu.tenChatLieu}</td>
        <td>${d.soLuong}</td>
        <td>${new Intl.NumberFormat('vi-VN').format(d.donGia)}</td>
        <td>${d.moTa || ''}</td>
        <td><img src="../../images/${d.anhSanPham}" width="100" height="100"  alt=""/></td>
        <td>
          <button
            type="button"
            class="btn btn-primary"
            data-id="${d.id}"
            data-bs-toggle="modal"
            data-bs-target="#quantityModal"
            onclick="openModalQuantity(this)">
            Chọn
          </button>
        </td>
      `;
        tbody.appendChild(tr);
    });
}

// Lấy thông tin ở form nhập số lượng truyền vào order.product
document.querySelector("#quantityModal .btn-primary").addEventListener("click",function (){
    let productId = document.getElementById("ctsp_id").value
    let inpQuantity = document.getElementById("inp_so_luong").value
    let imgId = document.getElementById("anhsp_id").value
    let tenSp = document.getElementById("ten_sp").textContent
    let mauSac = document.getElementById("mau_sac").textContent
    let kichThuoc = document.getElementById("kich_thuoc").textContent
    let donGia = parseFloat(document.getElementById("don_gia").textContent)
    let soLuong = parseInt(document.getElementById("so_luong").textContent)
    let tongTien = parseInt(inpQuantity) * parseFloat(donGia)
    let activeTab = document.querySelector("#nav-tab .nav-link.active");
    if (!activeTab) {
        alert("Vui lòng chọn một đơn hàng trước!");
        return;
    }
    let orderId = parseInt(activeTab.getAttribute("data-order-id"));

    let order = orders.find(o => o.id === orderId);

    if (!order) {
        console.error("Không tìm thấy đơn hàng!");
        return;
    }
    if(inpQuantity === ""){
        document.getElementById("errQuantityMes").innerText = "Không được để trống trường này!"
        return;
    }
    if(inpQuantity <= 0){
        document.getElementById("errQuantityMes").innerText = "Vui lòng nhập số lớn hơn 0"
        return;
    }
    if(inpQuantity > soLuong){
        document.getElementById("errQuantityMes").innerText = "Vượt quá số lượng trong kho"
        return;
    }
    let existingProduct = order.product.find(p => Number(p.id) === Number(productId))
    if(existingProduct){
        existingProduct.so_luong += Number(inpQuantity)
        existingProduct.tong_tien = existingProduct.so_luong * donGia
    }else{
        order.product.push({
            id:productId,
            anh_san_pham:imgId,
            ten_san_pham:tenSp,
            mau_sac:mauSac,
            kich_thuoc:kichThuoc,
            don_gia:donGia,
            so_luong:Number(inpQuantity),
            tong_tien:tongTien
        })
    }
    fetch(`/ban-hang-off/update-sp/${Number(productId)}/${Number(inpQuantity)}`,{
        method : "PUT"
    }).then(resp =>{
        if(!resp.ok){
            throw new Error("Lỗi"+resp.status)
        }
    }).then(()=>{
        saveOrderToLocalStorage()
        renderOrders()
        bootstrap.Modal.getInstance(document.getElementById("quantityModal")).hide();
        bootstrap.Modal.getInstance(document.getElementById("spModal")).hide();
    }).catch(e => {
        console.error(e);
    });

    saveOrderToLocalStorage()
    renderOrderDetails(orderId)
    let modal = bootstrap.Modal.getInstance(document.getElementById('quantityModal'));
    modal.hide()
})
document.getElementById("quantityModal").addEventListener("hidden.bs.modal", function () {
    console.log("đóng rồi")
    document.getElementById("ten_sp").innerText = "";
    document.getElementById("mau_sac").innerText = "";
    document.getElementById("kich_thuoc").innerText = "";
    document.getElementById("don_gia").innerText = "";
    document.getElementById("so_luong").innerText = "";
    document.getElementById("errQuantityMes").innerText="";
});
document.addEventListener("DOMContentLoaded", () => {
    renderOrders();
});