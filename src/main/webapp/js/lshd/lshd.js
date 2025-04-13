
// Reload trang
function renderOnlOrder(){
    document.getElementById("product-list").innerHTML = ""
    let orderId = Number(document.getElementById("orderId").value);
    let order = orders.find(o => o.id === orderId)
    console.log(order)
    let status = order.status;
    if(status!=="Chờ xác nhận"){
        document.querySelector(".btn.btn-danger").style.display = "none";
    }
    let productList = document.getElementById("product-list")
    order.listhdct.forEach(p=>{
        let product = document.createElement("div");
        let hr = document.createElement("hr")
        product.classList.add("d-flex","justify-content-space-between","align-item-center")
        let quantityHTML = status === "Chờ xác nhận"
            ? `
                <div class="col text-center">
                    <div class="input-group input-group-sm">
                        <button class="btn btn-outline-secondary btn-sm" onclick="updateQuantity(${p.id},-1)">-</button>
                        <span class="form-control text-center" id="quantity-${p.id}">${p.soLuong}</span>
                        <button class="btn btn-outline-secondary btn-sm" onclick="updateQuantity(${p.id},1)">+</button>
                    </div>
                </div>
              `
            : `
                <div class="col">
                    <p>Số lượng: <span>${p.soLuong}</span></p>
                </div>
              `;
        product.innerHTML = `
            <div class="row ">
                <div class="col">
                    <img src="../../images/${p.sanPhamChiTiet.anhSanPham}" style="width: 150px;height: 150px;" alt="">
                </div>
                <div class="col">
                    <h5>
                        ${p.sanPhamChiTiet.sanPham.tenSanPham}
                    </h5>
                    <div class="text-danger fw-bold"> 
                        <span class="price">${p.sanPhamChiTiet.donGia.toLocaleString("vi-VN")}</span> VND
                    </div>
                    <div>Size: ${p.sanPhamChiTiet.kichThuoc.tenKichThuoc}</div> 
                </div>
                ${quantityHTML}
                <div class="col text-end text-danger fw-bold">
                        <span id="total-${p.id}">${p.tongTien==null ? 0 : p.tongTien.toLocaleString("vi-VN")}</span> VND
                </div>
                ${order.status === "Chờ xác nhận"
            ? `<div class="col text-end">
                            <button class="btn btn-danger btn-sm" onclick="removeItem(${p.id})">
                                <i class="bi bi-trash"></i>
                            </button>
                        </div>`
            : ""
        }
            </div>
        `;
        productList.appendChild(product)
        productList.appendChild(hr)
    })
    updateStatusBar(status,order.id);
}

// Hàm này sẽ cập nhật trạng thái đơn hàng Vào DB và Local
async function updateOrder() {
    let orderId = Number(document.getElementById("orderId").value);
    let order = orders.find(o => o.id === orderId)
    let hdctList = order.listhdct
    for (const hdct of hdctList) {
        try {
            const response = await fetch(`/ban-hang-online/updateCT/${hdct.id}/${order.status}`, {
                method: "PUT"
            });
            const updated = await response.json();
            hdct.trangThai = updated.trangThai;
        } catch (error) {
            console.error("Lỗi khi cập nhật hdct:", error);
        }
    }
    if (order.status === "Chờ giao hàng") {
        for (const hdct of hdctList) {
            try {
                const response = await fetch(`/ban-hang-online/update-sp/${hdct.ctsp_id}/${hdct.soLuong}`, {
                    method: "PUT"
                });
                const updated = await response.json();
                hdct.soLuong = updated.soLuong;
            } catch (error) {
                console.error("Lỗi cập nhật số lượng sp:", error);
            }
        }
    }
    try{
        const response = await fetch(`/ban-hang-online/updateHD/${order.id}/${order.status}`, {
            method: "PUT"
        });
        const updated = await response.json();
        order.status = updated.trangThaiThanhToan;
    }catch(error){
        console.log(error)
    }
    localStorage.setItem("orders", JSON.stringify(orders));
    renderOnlOrder();
}

// Hàm này đê
function connectSocket() {
    const socket = new SockJS("/ws");
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log("Connected to WebSocket: " + frame);

        // Lấy ID đơn hàng hiện tại
        const orderId = Number(document.getElementById("orderId").value);

        // Lắng nghe thông báo từ server gửi riêng cho user
        stompClient.subscribe("/user/topic/order/" + orderId, function (message) {
            console.log(message.body)
            const newStatus = message.body;
            console.log("Nhận trạng thái mới từ Admin:", newStatus);

            // Cập nhật localStorage và giao diện
            const order = orders.find(o => o.id === orderId);
            const oldStatus = order.status;
            if (order) {
                order.status = newStatus;

                // Nếu trạng thái mới chưa có trong steps thì thêm vào
                if (!order.steps.some(s => s.label === newStatus)) {
                    let currentStep = order.steps.findIndex(step => step.label === oldStatus);
                    order.steps.splice(currentStep+1, 0, {
                        label: "Đã hủy",
                        icon: "fas fa-times-circle"
                    });
                }

                localStorage.setItem("orders", JSON.stringify(orders));
                renderOnlOrder();
            }
        });
    });
}
// Nút hủy đơn
function cancelOrder() {
    let orderId = Number(document.getElementById("orderId").value);
    let order = orders.find(o => o.id === orderId)

    const currentStep = order.steps.findIndex(step => step.label === order.status);

    fetch(`/ban-hang-online/cancel-order/${order.id}`,{
        method:"PUT"
    }).then(res => {
        if (!res.ok) throw new Error("Không thể hủy đơn hàng");
        return res.json();
    }).then(data => {
        if (order) {
            order.status = "Đã hủy";

            if (!order.steps.some(step => step.label === "Đã hủy")) {
                order.steps.splice(currentStep+1, 0, {
                    label: "Đã hủy",
                    icon: "fas fa-times-circle"
                });
            }

            localStorage.setItem("orders", JSON.stringify(orders));
            renderOnlOrder();
        }
    }).catch(err => alert(err.message));
}

// Hàm này sẽ update thanh trạng thái
function updateStatusBar(status,orderId) {
    console.log(status)
    let order = orders.find(o => o.id === orderId)
    const statusBar = document.getElementById("status-bar");
    statusBar.innerHTML = "";

    const currentIndex = order.steps.findIndex(step => step.label === status);

    // Nếu không phải "Đã hủy" thì chỉ hiển thị từ đầu đến trạng thái hiện tại
    const visibleSteps = order.steps.filter(step => step.id > 0).slice(0, currentIndex); // lọc bỏ id -1

    order.steps.slice(0, currentIndex + 1).forEach((step, index) => {
        const stepDiv = document.createElement("div");
        stepDiv.classList.add("step");

        if (index < currentIndex) {
            stepDiv.classList.add("completed");
        } else if (index === currentIndex) {
            stepDiv.classList.add("current");
        }

        let iconColor = "";
        if (step.label === "Đã hủy") {
            iconColor = "style='background-color:red;color:white;'";
        }

        stepDiv.innerHTML = `
            <div class="icon" ${iconColor}><i class="${step.icon}"></i></div>
            <p>${step.label}</p>
        `;
        statusBar.appendChild(stepDiv);
    });
}
renderOnlOrder()
connectSocket()