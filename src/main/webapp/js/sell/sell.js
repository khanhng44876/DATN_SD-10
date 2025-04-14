let orders = JSON.parse(localStorage.getItem("orders")) || []

// Hàm load lại đơn hàng
async function renderOrders() {
    console.log(orders)
    let activeTab = document.querySelector("#nav-tab .nav-link.active")
    let activeId = activeTab ? activeTab.getAttribute("data-order-id") : null
    document.getElementById("nav-tab").innerHTML = ""
    document.getElementById("nav-tabContent").innerHTML=""
    for(let order of orders){
        updateThanhTien(order.id)
        await updateDiscount(order)
        createElementOrder(order)
        if(order.product.length>0){
            let productList = document.getElementById(`product-list-${order.id}`);
            order.product.forEach(product => {
                let productItem = document.createElement("div")
                productItem.classList.add("d-flex", "justify-content-between", "align-items-center");

                productItem.innerHTML = `
                    <div class="col-3">
                        <img src="../../images/${product.anh_san_pham}" alt="${product.ten_san_pham}" style="width: 150px;height: 150px;" class="img-fluid">
                    </div>
                    <div class="col-5">
                        <h5 class="mb-1">${product.ten_san_pham}</h5>
                        <div class="text-danger fw-bold">
                             <span class="price">${product.don_gia.toLocaleString("vi-VN")}</span> VND
                        </div>
                        <div>Size: ${product.kich_thuoc}</div>
                    </div>
                    <div class="col-1 text-center">
                        <div class="input-group input-group-sm">
                            <button class="btn btn-outline-secondary btn-sm" onclick="updateQuantity(${order.id}, ${product.id}, -1)">-</button>
                            <span class="form-control text-center" >${product.so_luong}</span>
                            <button class="btn btn-outline-secondary btn-sm" onclick="updateQuantity(${order.id}, ${product.id}, +1)">+</button>
                        </div>
                    </div>
                    <div class="col-2 text-end text-danger fw-bold">
                        ${(product.tong_tien).toLocaleString("vi-VN")} VND
                    </div>
                    <div class="col-1 text-end">
                        <button class="btn btn-danger btn-sm" onclick="removeProduct(${order.id}, ${product.id})">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                    <hr>
                `;
                productList.appendChild(productItem)
            });

        }
    }
    if(activeId){
        setTimeout(() => {
            let activeTabElement = document.querySelector(`#li-${activeId} .nav-link`);
            if (activeTabElement) activeTabElement.click();
        }, 100);
    }

}

// Xóa sản phẩm trong đơn hàng
function removeProduct(orderId, productId) {
    let order = orders.find(o => o.id === Number(orderId));
    if (!order) {
        console.error("❌ Không tìm thấy đơn hàng!");
        return;
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
function createOrder(){
    if(orders.length >= 5){
        alert("Tối đa 5")
        return;
    }
    let orderId = orders.length ? orders[orders.length - 1].id + 1 : 1;
    let orderName = `Đơn hàng ${orderId}`
    let order ={
        id: orderId,
        name:orderName,
        product:[],
        customer : {
            id:1,
            name:"Khách lẻ"
        },
        totalAmount: 0,
        discount:{
            id:null,
            ma:"",
            phan_tram:0,
            tien_giam:0
        },
        tien_phai_tra:0

    }
    orders.push(order)
    saveOrderToLocalStorage()
    createElementOrder(order);
}

// Hàm tạo các thành phần trong đơn hàng
function createElementOrder(order){

    let li = document.createElement("li");
    li.classList.add("nav-item");
    li.id = `li-${order.id}`;

    let tab = document.createElement("button");
    tab.classList.add("nav-link");
    tab.setAttribute("data-bs-toggle", "tab");
    tab.setAttribute("data-bs-target", `#content-${order.id}`);
    tab.setAttribute("data-order-id", order.id);
    tab.innerHTML = `${order.name} 
        <span class="text-danger" style="cursor:pointer;" onclick="removeOrder(${order.id})">❌</span>`;

    li.appendChild(tab);
    document.getElementById("nav-tab").appendChild(li);

    // 📌 Tạo nội dung đơn hàng
    let tabContent = document.createElement("div");
    tabContent.classList.add("tab-pane", "fade");
    tabContent.id = `content-${order.id}`;
    tabContent.innerHTML = `
        <div class="d-flex justify-content-between mt-3">
            <h4>Sản phẩm</h4>
            <button type="button" class="btn btn-warning px-4 py-2 fw-bold text-white rounded-pill" data-bs-toggle="modal" data-bs-target="#spModal">
                + Thêm sản phẩm
            </button>
        </div>
        <hr>
        <div class="row">
            <div class="col-md-8">
                <div id="product-list-${order.id}" class="product-list"></div>
                    <p style="color: red">
                        <strong>Thành tiền : </strong>
                        <span class="thanh_tien-${order.id}">${order.totalAmount}</span> VND
                    </p>
            </div>
        
            <div class="col-md-4">
                <div>
                    <h4>Thông tin đơn hàng</h4>
                    <hr>
                            <div class="d-flex justify-content-between">
                                <h4>Khách hàng</h4>
                                <button type="button" data-bs-toggle="modal" data-bs-target="#khModal" class="btn btn-warning px-4 py-2 fw-bold text-white rounded-pill">Chọn khách hàng</button>
                            </div>
                            <div class="kh-container d-flex justify-content-between">
                                <strong>Tên khách hàng : </strong>
                                <span class="ten-khach-hang">${order.customer.name}</span>
                            </div>
                            <hr>
                            
                            <input type="hidden" id="kh-id-${order.id}" value="${order.customer.id}">
                        
                            <p>
                            <input type="text" id="ma-km-${order.id}" placeholder="Mã khuyến mãi" value="${order.discount.ma}" readonly>
                            <input type="text" id="phan-tram-${order.id}" placeholder="Phần trăm giảm" value="${order.discount.phan_tram + '%'}" readonly>
                            </p>
                            <p>Tiền hàng: <span class="thanh_tien-${order.id}">${order.totalAmount}</span> VND</p>
                            <p>Giảm giá: <span id="tien-giam-${order.id}">${order.discount.tien_giam}</span> VND</p>
                            <hr>
                            <h5 class="text-danger">Tổng số tiền: <span id="totalAmount-${order.id}">${order.tien_phai_tra}</span> VND</h5>
                            <hr>
                            <p>Hình thức thanh toán : 
                                <input type="radio" value="Tiền mặt" name="hinhthuctt"> Tiền mặt
                                <input type="radio" value="Chuyển khoản" name="hinhthuctt"> Chuyển khoản
                            </p>
                            <span style="color: red" id="error-httt-${order.id}"></span>
                            <p>Tiền khách đưa : 
                                <input type="text" id="customer-pay-${order.id}">
                            </p>
                            <span style="color: red" id="error-tkd-${order.id}"></span>
                            <p>Tiền thừa : 
                                <input type="text" id="refund-money-${order.id}" readonly>
                            </p>
                            <p>
                                <input type="hidden" id="km-id-${order.id}">
                                <button class="btn btn-warning px-4 py-2 fw-bold text-white rounded-pill" onclick="confirmOrder(${order.id})">Xác nhận</button>
                            </p>    
                </div>
            </div>
        </div>

    `;
    document.getElementById("nav-tabContent").appendChild(tabContent);
}

// Hàm xóa đơn hàng
function removeOrder(orderId){
    orders = orders.filter(order => order.id !== orderId)
    saveOrderToLocalStorage()
    renderOrders()
}

// Hàm cập nhật số lượng ngay tren giao diện
function updateQuantity(orderId,productId,change){
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
    product.so_luong = Math.max(1,Number(product.so_luong) + change)
    product.tong_tien = Number(product.so_luong) * product.don_gia;
    saveOrderToLocalStorage()
    updateThanhTien(orderId)
    renderOrders()
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
    renderOrders()
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

    let order = orders.find(o => o.id === orderId);
    if (order.product.length === 0) {
        alert("Vui lòng thêm sản phẩm vào đơn hàng");
        return;
    }
    if(!validateOrder(order.id)){
        alert("Vui lòng nhập đủ thông tin thanh toán")
        return
    }
    idnv = document.getElementById("idNv").value
    let paymentMethods = document.querySelector(`input[name="hinhthuctt"]:checked`)?.value;
    let today = new Date().toISOString().split("T")[0];

    let hdJson = {
        id_nhan_vien:idnv,
        id_khach_hang:order.customer.id,
        id_khuyen_mai:order.discount.id,
        ngay_tao: today,
        ngay_sua: null,
        don_gia: null,
        tong_tien: parseFloat(order.totalAmount),
        trang_thai_thanh_toan: "Đã thanh toán",
        hinh_thuc_thanh_toan: paymentMethods,
        dia_chi_giao_hang: "Tại cửa hàng",
        ghi_chu: null
    };
    console.log("Dữ liệu gửi đi:", JSON.stringify(hdJson));
    try {
        // Gửi request tạo hóa đơn
        let response = await fetch("/ban-hang-off/add-hoa-don", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(hdJson)
        });

        let hoaDon = await response.json();
        let idHoaDon = hoaDon.id;

        if (!idHoaDon) {
            alert("Không thể tạo hóa đơn!");
            return;
        }

        let productList = order.product;

        await new Promise(resolve => setTimeout(resolve, 500));

        // **Tạo danh sách request để gửi song song**
        let requests = productList.map((p) => {
            let hdctJson = {
                id_hoa_don:idHoaDon,
                id_san_pham_chi_tiet:Number(p.id),
                so_luong:p.so_luong,
                don_gia:p.don_gia,
                tong_tien:p.tong_tien,
                thanh_tien:p.tong_tien,
                ngay_tao: today,
                ngay_sua: null,
                trang_thai: "Đã thanh toán"
            };
            // **Tạo request POST thêm hóa đơn chi tiết**
            let hdctRequest = fetch("/ban-hang-off/add-hoa-don-ct", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(hdctJson)
            }).then(response => response.json())
                .then(data => console.log("Dữ liệu trả về : ",data))
                .catch(err => console.error("Lỗi thêm hóa đơn chi tiết:", err))

            // **Tạo request PUT cập nhật số lượng sản phẩm**
            let updateSpRequest = fetch(`/ban-hang-off/update-sp/${p.id}/${p.so_luong}`, {
                method: "PUT"
            }).catch(err => console.error("Lỗi cập nhật spct:", err));

            return Promise.all([hdctRequest, updateSpRequest]); // Gửi cả 2 request song song
        });
        if(order.discount.id !== null){
            let updateKmRequest = fetch(`/ban-hang-off/update-km/${order.discount.id}`,{
                method:"PUT"
            }).catch(err => console.error("Lỗi cập nhật khuyến mãi:", err));
            requests.push(updateKmRequest)
        }
        // **Chạy tất cả các request cùng lúc**
        await Promise.all(requests.flat());

        alert("Xác nhận đơn hàng thành công!");
        printInvoice(order.id)
        orders = orders.filter(o => o.id !== order.id);
        saveOrderToLocalStorage()
        await renderOrders()
    } catch (error) {
        console.error("Lỗi:", error);
        alert("Đã xảy ra lỗi khi xác nhận đơn hàng!");
    }
}

// Hàm validate
function validateOrder(orderId) {
    let paymentMethods = document.querySelectorAll(`[name="hinhthuctt"]:checked`);
    let customerPayInput = document.getElementById(`customer-pay-${orderId}`);
    let errorHttt = document.getElementById(`error-httt-${orderId}`);
    let errorTkd = document.getElementById(`error-tkd-${orderId}`);

    errorHttt.innerText = "";
    errorTkd.innerText = "";

    // Kiểm tra hình thức thanh toán
    if (paymentMethods.length === 0) {
        errorHttt.innerText = "Vui lòng chọn hình thức thanh toán!";
        return false;
    }

    // Kiểm tra tiền khách đưa
    let customerPay = parseFloat(customerPayInput.value.replace(/\D/g, "")) || 0;
    let order = orders.find(o => o.id === orderId);
    if (!order) return false;

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

    let printWindow = window.open("", "", "width=900,height=700");
    printWindow.document.open();
    printWindow.document.write(invoiceHTML);
    printWindow.document.close();
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
    if(inpQuantity < 0){
        document.getElementById("errQuantityMes").innerText = "Vui lòng nhập số lớn hơn 0"
        return;
    }
    if(inpQuantity > soLuong){
        document.getElementById("errQuantityMes").innerText = "Vượt quá số lượng trong kho"
        return;
    }
    let existingProduct = order.product.find(p => Number(p.id) === Number(productId))
    if(existingProduct){
        existingProduct.so_luong += inpQuantity
        existingProduct.tong_tien = existingProduct.so_luong * donGia
    }else{
        order.product.push({
            id:productId,
            anh_san_pham:imgId,
            ten_san_pham:tenSp,
            mau_sac:mauSac,
            kich_thuoc:kichThuoc,
            don_gia:donGia,
            so_luong:inpQuantity,
            tong_tien:tongTien
        })
    }
    saveOrderToLocalStorage()
    renderOrders()
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
renderOrders()