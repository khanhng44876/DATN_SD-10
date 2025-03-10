let orders = JSON.parse(localStorage.getItem("orders")) || []

function renderOrders() {
    document.getElementById("nav-tab").innerHTML = ""
    document.getElementById("nav-tabContent").innerHTML=""
    orders.forEach(order => createElementOrder(order))
    let bien_dem = 0
    if(orders.product){
        order.products.forEach(product => {
            totalAmount += product.totalPrice;
            let row = `
                    <tr>
                        <td>${product.id}</td>
                        <td>${product.ten_san_pham}</td>
                        <td>${product.mau_sac}</td>
                        <td>${product.kich_thuoc }</td>
                        <td>${product.don_gia}</td>
                        <td>${product.so_luong}</td>
                        <td>${product.tong_tien}</td>
                        <td><button class="btn btn-danger btn-sm" onclick="removeProduct(${order.id}, '${product.id}')">🗑</button></td>
                    </tr>
            `
        ;
                    document.getElementById(`order-items-${order.id}`).insertAdjacentHTML("beforeend", row);
        });
    }
}
function removeProduct(orderId, productId) {
    let order = orders.find(o => o.id === orderId);
    if (order) {
        order.products = order.products.filter(p => p.id !== productId);
        saveOrderToLocalStorage();
        renderOrders();
    }
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
        product:[]
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
        <div class="d-flex justify-content">
            <h4>Sản phẩm</h4>
            <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#spModal">
                + Thêm sản phẩm
            </button>
        </div>
        <hr>
        <div>
            <table class="table table-bordered">
                <tr>
                    <th>Mã CTSP</th>
                    <th>Tên sản phẩm</th>
                    <th>Màu sắc</th>
                    <th>Kích thước</th>
                    <th>Số lượng</th>
                    <th>Đơn giá</th>
                    <th>Tổng tiền</th>
                    <th>Thao tác</th>
                </tr>
            </table>
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
            document.getElementById("ten_sp").innerText = data.sanPham.tenSanPham;
            document.getElementById("mau_sac").innerText = data.mauSac.ten_mau_sac;
            document.getElementById("kich_thuoc").innerText = data.kichThuoc.ten_kich_thuoc;
            document.getElementById("don_gia").innerText = data.donGia;
            document.getElementById("so_luong").innerText = data.soLuong;
        })
        .catch(error => console.error("Lỗi",error))
}
document.querySelector("#quantityModal .btn-primary").addEventListener("click",function (){
    let productId = document.getElementById("quantityModal").getAttribute("data-id")
    let tenSp = document.getElementById("ten_sp").textContent
    let mauSac = document.getElementById("mau_sac").textContent
    let kichThuoc = document.getElementById("kich_thuoc").textContent
    let donGia = document.getElementById("don_gia").textContent
    let soLuong = document.getElementById("so_luong").value
    let tongTien = soLuong * donGia

    let activeTab = document.querySelector("#nav-tab .nav-link.active");
    if (!activeTab) {
        alert("Vui lòng chọn một đơn hàng trước!");
        return;
    }
    let orderId = parseInt(activeTab.getAttribute("data-order-id"));

    let order = orders.find(o => o.id === orderId-1);

    if (!order) {
        console.error("Không tìm thấy đơn hàng!");
        return;
    }

    let existingProduct = order.product.find(p => p.id === productId)
    if(existingProduct){
        existingProduct.so_luong += soLuong
        existingProduct.tong_tien = existingProduct.so_luong * donGia
    }else{
        orders.product.push({
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