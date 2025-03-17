let orders = JSON.parse(localStorage.getItem("orders")) || []

function renderOrders() {
    console.log(orders)
    let activeTab = document.querySelector("#nav-tab .nav-link.active")
    let activeId = activeTab ? activeTab.getAttribute("data-order-id") : null
    document.getElementById("nav-tab").innerHTML = ""
    document.getElementById("nav-tabContent").innerHTML=""
    orders.forEach(order => {
        createElementOrder(order)
        if(order.product.length>0){
            let tbody = document.querySelector(`#content-${order.id} table tbody`);

            order.product.forEach(product => {
                let tr = document.createElement("tr");
                tr.innerHTML = `
                    <td><input type="checkbox" class="selectProduct" onchange="updateThanhTien(${order.id})"></td>
                    <td>${product.id}</td>
                    <td>${product.ten_san_pham}</td>
                    <td>${product.mau_sac}</td>
                    <td>${product.kich_thuoc}</td>
                    <td>${product.so_luong}</td>
                    <td>${product.don_gia}</td>
                    <td class="product-total">${product.tong_tien}</td>
                    <td>
                        <button class="btn btn-danger btn-sm" onclick="removeProduct(${order.id}, ${product.id})">Xóa</button>
                    </td>
                `;
                tbody.appendChild(tr);
            });

        }
    })
    if(activeId){
        setTimeout(() => {
            let activeTabElement = document.querySelector(`#li-${activeId} .nav-link`);
            if (activeTabElement) activeTabElement.click();
        }, 100);
    }

}
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
function saveOrderToLocalStorage(){
    localStorage.setItem("orders",JSON.stringify(orders))
}

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
        customer : "Khách lẻ",
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
        <div>
            <table class="table table-bordered">
                <thead>
                    <tr>
                        <th>Chọn</th>
                        <th>Mã CTSP</th>
                        <th>Tên sản phẩm</th>
                        <th>Màu sắc</th>
                        <th>Kích thước</th>
                        <th>Số lượng</th>
                        <th>Đơn giá</th>
                        <th>Tổng tiền</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                
                </tbody>
            </table>
            <p style="color: red">
                <strong>Thành tiền : </strong>
                <span class="thanh_tien-${order.id}">${order.totalAmount}</span> VND
            </p>
        </div>
        <hr>
        <div>
            <h4>Thông tin đơn hàng</h4>
            <hr>
            <div class="row">
                <div class="col-md-6">
                    <div class="d-flex justify-content-between">
                        <h4>Khách hàng</h4>
                        <button type="button" data-bs-toggle="modal" data-bs-target="#khModal">Thêm khách hàng</button>
                    </div>
                    <hr>
                    <div class="kh-container d-flex justify-content-between">
                        <strong>Tên khách hàng : </strong>
                        <span class="ten-khach-hang">${order.customer}</span>
                    </div>
                    <input type="hidden" id="kh-id-${order.id}" value="">
                </div>
                <div class="col-md-6">
                    <p>
                    <input type="text" id="ma-km-${order.id}" placeholder="Mã khuyến mãi" value="${order.discount.ma}" readonly>
                    <input type="text" id="phan-tram-${order.id}" placeholder="Phần trăm giảm" value="${order.discount.phan_tram}" readonly>
                    </p>
                    <p>Tiền hàng: <span class="thanh_tien-${order.id}">${order.totalAmount}</span> VND</p>
                    <p>Giảm giá: <span id="tien-giam-${order.id}">${order.discount.tien_giam}</span> VND</p>
                    <hr>
                    <h5 class="text-danger">Tổng số tiền: <span id="totalAmount-${order.id}">0</span> VND</h5>
                    <hr>
                    <p>Hình thức thanh toán : 
                        <input type="radio" value="Tiền mặt" name="hinhthuctt"> Tiền mặt
                        <input type="radio" value="Chuyển khoản" name="hinhthuctt"> Chuyển khoản
                    </p>
                    <p>Tiền khách đưa : 
                        <input type="text" id="customer-pay-${order.id}">
                    </p>
                    <p>Tiền thừa : 
                        <input type="text" id="refund-money-${order.id}" readonly>
                    </p>
                    <button>Xác nhận</button>
                </div>
            </div>
        </div>
    `;
    document.getElementById("nav-tabContent").appendChild(tabContent);
}

function removeOrder(orderId){
    orders = orders.filter(order => order.id !== orderId)
    saveOrderToLocalStorage()
    renderOrders()
}

function openModalQuantity(button){

    let id = button.getAttribute("data-id")
    fetch(`/ban-hang-off/detail/${id}`,{
        method : "GET"
    })
        .then(response => response.json())
        .then(data => {
            console.log(data)
            document.getElementById("ctsp_id").value = data.id
            document.getElementById("ten_sp").innerText = data.sanPham.tenSanPham;
            document.getElementById("mau_sac").innerText = data.mauSac.ten_mau_sac;
            document.getElementById("kich_thuoc").innerText = data.kichThuoc.tenKichThuoc;
            document.getElementById("don_gia").innerText = data.donGia;
            document.getElementById("so_luong").innerText = data.soLuong;
        })
        .catch(error => console.error("Lỗi",error))
}

function updateThanhTien(orderId){
    let orderContent = document.querySelector(`#content-${orderId}`)
    let checkBoxes = document.querySelectorAll(".selectProduct")
    let total = 0;
    checkBoxes.forEach(checkBox =>{
        if(checkBox.checked){
            let row = checkBox.closest("tr");
            let totalPrice = parseFloat(row.querySelector(".product-total").innerText);
            total += totalPrice;
        }
    })
    let order = orders.find(o=>o.id===orderId)
    if (order) {
        order.totalAmount = total;
        saveOrderToLocalStorage();
    }
    document.querySelectorAll(`.thanh_tien-${orderId}`).forEach(el => {
        el.innerText = order.totalAmount.toLocaleString("vi-VN");
    });
    updateDiscount(orderId)
}

function selectKH(idKH,tenKH,button){
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
    document.getElementById(`kh-id-${orderId}`).value = idKH
    order.customer = tenKH
    saveOrderToLocalStorage()
    renderOrders()
    let modal = bootstrap.Modal.getInstance(document.getElementById('khModal'));
    modal.hide()
}

function updateDiscount(orderId){
    let order = orders.find(o=>o.id===orderId)
    if(!order) return;
    console.log(order)
    let totalAmount = parseInt(order.totalAmount)
    console.log
    fetch(`/ban-hang-off/best-km/${totalAmount}`)
        .then(response => response.json())
        .then(data =>{
            console.log(data)
            document.getElementById(`ma-km-${orderId}`).value = data.ma_khuyen_mai
            document.getElementById(`phan-tram-${orderId}`).value =data.phan_tram_giam + "%"
            document.getElementById(`tien-giam-${order.id}`).innerText = data.tien_giam.toLocaleString("vi-VN")
            document.getElementById(`totalAmount-${order.id}`).innerText = (totalAmount - data.tien_giam).toLocaleString("vi-VN")
            order.discount = {
                id:data.id,
                ma:data.ma_khuyen_mai,
                phan_tram:data.phan_tram_giam,
                tien_giam:data.tien_giam
            }
            order.tien_phai_tra = totalAmount - data.tien_giam
        })
        .catch(error => console.error("Lỗi khi lấy khuyến mãi:", error))

}
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
document.querySelector("#quantityModal .btn-primary").addEventListener("click",function (){
    let productId = document.getElementById("ctsp_id").value
    let tenSp = document.getElementById("ten_sp").textContent
    let mauSac = document.getElementById("mau_sac").textContent
    let kichThuoc = document.getElementById("kich_thuoc").textContent
    let donGia = parseFloat(document.getElementById("don_gia").textContent)
    let soLuong = parseInt(document.getElementById("inp_so_luong").value)
    let tongTien = parseInt(soLuong) * parseFloat(donGia)

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

    let existingProduct = order.product.find(p => Number(p.id) === Number(productId))
    if(existingProduct){
        existingProduct.so_luong += soLuong
        existingProduct.tong_tien = existingProduct.so_luong * donGia
    }else{
        order.product.push({
            id:productId,
            ten_san_pham:tenSp,
            mau_sac:mauSac,
            kich_thuoc:kichThuoc,
            don_gia:donGia,
            so_luong:soLuong,
            tong_tien:tongTien
        })
    }
    saveOrderToLocalStorage()
    renderOrders()
})
document.getElementById("quantityModal").addEventListener("hidden.bs.modal", function () {
    console.log("đóng rồi")
    document.getElementById("ten_sp").innerText = "";
    document.getElementById("mau_sac").innerText = "";
    document.getElementById("kich_thuoc").innerText = "";
    document.getElementById("don_gia").innerText = "";
    document.getElementById("so_luong").innerText = "";
});
renderOrders()