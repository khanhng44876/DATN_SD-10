// Reload trang
function renderOnlOrder(){
    document.getElementById("product-list").innerHTML = "";
    let confirm_div = document.getElementById("confirm-button");
    let orderId = Number(document.getElementById("orderId").value);
    let order = ordersOnl.find(o => o.id === orderId)
    console.log(order)
    let status = order.status;
    
    if(status === "Hoàn thành"){
        confirm_div.innerHTML=`
            <button
                type="button"
                class="btn btn-secondary rounded-pill"
                data-bs-toggle="modal"
                data-bs-target="#completeModal"
            >
                Hoàn thành
            </button>
        `;
    } else if(status === "Đã hoàn thành" || status === "Đã hoàn tiền"){
        confirm_div.innerHTML=``;
    } else if(status !== "Chờ xác nhận" && status !== "Chờ hoàn tiền") {
        confirm_div.innerHTML=`
            <button
                type="button"
                class="btn btn-primary rounded-pill me-2"
                onclick="updateOrder()"
            >
                Xác nhận
            </button>
            <button
                type="button"
                class="btn btn-warning rounded-pill"
                onclick="backOrder()"
            >
                Quay về
            </button>
        `;
    }

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
                        <span class="form-control text-center" id="quantity-${p.id}">${p.quantity}</span>
                        <button class="btn btn-outline-secondary btn-sm" onclick="updateQuantity(${p.id},1)">+</button>
                    </div>
                </div>
              `
            : `
                <div class="col">
                    <p>Số lượng: <span>${p.quantity}</span></p>
                </div>
              `;
        product.innerHTML = `
            <div class="row ">
                <div class="col">
                    <img src="../../images/${p.image}" style="width: 150px;height: 150px;" alt="">
                </div>
                <div class="col">
                    <h5>
                        ${p.name}
                    </h5>
                    <div class="text-danger fw-bold"> 
                        <span class="price">${p.price.toLocaleString("vi-VN")}</span> VND
                    </div>
                    <div>Size: ${p.size}</div> 
                </div>
                ${quantityHTML}
                <div class="col text-end text-danger fw-bold">
                        <span id="total-${p.id}">${p.total==null ? 0 : p.total.toLocaleString("vi-VN")}</span> VND
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

function validateLast(id, customer_pay) {
    let order = ordersOnl.find(o => o.id === id)
    if(order.total > customer_pay){
        return false;
    }
    if(order.total < customer_pay){
        customer_pay = order.total
        document.getElementById("customer-pay").value = customer_pay;
        return false;
    }
    return true;
}

// Hàm này sẽ cập nhật trạng thái đơn hàng Vào DB và Local
async function updateOrder() {
    const orderId = Number(document.getElementById("orderId").value);
    const customer_pay = Number(document.getElementById('customer-pay').value);
    const order = ordersOnl.find(o => o.id === orderId);

    // Kiểm tra trạng thái "Hoàn thành"
    if (order.status === "Hoàn thành" && !validateLast(order.id, customer_pay)) {
        return;
    }

    if (!confirm("Bạn có chắc muốn xác nhận đơn hàng này?")) {
        return;
    }

    const hdctList = order.listhdct;
    console.log("Danh sách chi tiết hóa đơn:", hdctList);

    // 1) Tạo mảng Promise cho updateCT và update-sp ngay lập tức
    const allPromises = hdctList.flatMap(hdct => {
        // Đảm bảo quantity luôn có giá trị
        const quantity = hdct.quantity || 1;
        console.log("Chi tiết hóa đơn:", hdct);
        console.log("ID sản phẩm chi tiết:", hdct.ctsp_id);
        console.log("Số lượng:", quantity);

        // update trạng thái chi tiết
        const p1 = fetch(
            `/ban-hang-online/updateCT/${hdct.id}/${order.status}`,
            { method: "PUT" }
        )
            .then(res => {
                if (!res.ok) throw new Error("Lỗi updateCT");
                return res.json();
            })
            .then(updated => {
                hdct.trangThai = updated.trangThai;
            });
        return [p1];
    });

    try {
        await Promise.all(allPromises);
    } catch (err) {
        console.error("Lỗi trong quá trình cập nhật:", err);
        return alert(err.message);
    }

    try {
        const resHD = await fetch(
            `/ban-hang-online/updateHD/${order.id}/${order.status}`,
            { method: "PUT" }
        );
        if (!resHD.ok) throw new Error("Lỗi updateHD");
        const updatedHD = await resHD.json();
        
        // Xử lý trường hợp đặc biệt cho "Đã hoàn tiền"
        if (updatedHD.trangThaiThanhToan === "Đã hoàn tiền") {
            // Tìm và cập nhật step "Chờ hoàn tiền" thành "Đã hoàn tiền"
            const refundStep = order.steps.find(step => step.label === "Chờ hoàn tiền");
            if (refundStep) {
                refundStep.label = "Đã hoàn tiền";
            }
        }
        
        order.status = updatedHD.trangThaiThanhToan;
    } catch (err) {
        console.error(err);
    }

    // 4) Lưu và render lại
    localStorage.setItem("ordersOnl", JSON.stringify(ordersOnl));
    renderOnlOrder();
}

function updateQuantity(itemId, num) {
    let orderId = Number(document.getElementById("orderId").value);
    let order = ordersOnl.find(o => o.id === orderId);
    
    if (!order) {
        alert("Không tìm thấy đơn hàng!");
        return;
    }

    let item = order.listhdct.find(c => c.id === itemId);
    if (!item) {
        alert("Không tìm thấy sản phẩm!");
        return;
    }

    // Tính toán số lượng mới
    const newQuantity = Math.max(1, item.quantity + num);
    
    // Cập nhật số lượng và tổng tiền
    item.quantity = newQuantity;
    item.total = item.quantity * item.price;
    
    // Tính lại tổng tiền đơn hàng
    order.total = order.listhdct.reduce((sum, c) => sum + c.total, 0);
    
    // Lưu vào localStorage
    localStorage.setItem("ordersOnl", JSON.stringify(ordersOnl));

    // Gửi cập nhật lên server
    const payload = {
        orderId: order.id,
        itemId: item.id,
        totalAmount: order.total,
        quantity: newQuantity // Sử dụng newQuantity thay vì item.quantity
    };

    // Gửi qua WebSocket
    if (stompClient && stompClient.connected) {
        stompClient.send("/app/update-quantity", {}, JSON.stringify(payload));
    } else {
        console.error("WebSocket không kết nối!");
        alert("Không thể kết nối với server. Vui lòng thử lại sau!");
        return;
    }

    // Cập nhật giao diện
    renderOnlOrder();
}

// Hàm này để kết nối Socket Nhận tin nhắn socket từ người khác
function connectSocket() {
    const socket = new SockJS("/ws");
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log("Connected to WebSocket: " + frame);

        // Lấy ID đơn hàng hiện tại
        const orderId = Number(document.getElementById("orderId").value);

        // Lắng nghe thông báo từ server gửi riêng cho user
        stompClient.subscribe("/user/topic/order/" + orderId, function (message) {
            console.log(message.body)
            const newStatus = JSON.parse(message.body)
            console.log("Nhận trạng thái mới từ Admin:", newStatus);

            if(newStatus.type === "update-quantity"){
                let order = ordersOnl.find(o => o.id === orderId)
                let item = order.listhdct.find(p => p.id === newStatus.itemId)
                if(item){
                    item.quantity = newStatus.quantity;
                    item.total = newStatus.quantity * item.price;
                }
                order.total = newStatus.total;
                localStorage.setItem("ordersOnl",JSON.stringify(ordersOnl))
                renderOnlOrder()
            }else {
                // Cập nhật localStorage và giao diện
                const order = ordersOnl.find(o => o.id === orderId);
                const oldStatus = order.status;
                if (order) {
                    order.status = newStatus;

                    // Nếu trạng thái mới chưa có trong steps thì thêm vào
                    if (!order.steps.some(s => s.label === newStatus)) {
                        if(newStatus==="Đã hủy"){
                            let currentStep = order.steps.findIndex(step => step.label === oldStatus);
                            order.steps.splice(currentStep + 1, 0, {
                                label: "Đã hủy",
                                icon: "fas fa-times-circle"
                            });
                        }
                    }

                    localStorage.setItem("ordersOnl", JSON.stringify(ordersOnl));
                    renderOnlOrder();
                }
            }
        });
    });
}
// Nút hủy đơn
async function cancelOrder() {
    let orderId = Number(document.getElementById("orderId").value);
    let order = ordersOnl.find(o => o.id === orderId)

    const currentStep = order.steps.findIndex(step => step.label === order.status);

    await fetch(`/ban-hang-online/cancel-order/${order.id}`,{
        method:"PUT"
    }).then(res => {
        if (!res.ok) throw new Error("Không thể hủy đơn hàng");
        return res.json();
    }).then(data => {
        if (order) {
            order.status = data.newStatus;

            if (!order.steps.some(step => step.label === data.newStatus)) {
                order.steps.splice(currentStep+1, 0, {
                    label: "Đã hủy",
                    icon: "fas fa-times-circle"
                });
            }

            localStorage.setItem("ordersOnl", JSON.stringify(ordersOnl));
            renderOnlOrder();
        }
    }).catch(err => alert(err.message));
}

async function backOrder() {
    let orderId = Number(document.getElementById("orderId").value);
    let order = ordersOnl.find(o => o.id === orderId)

    const currentStep = order.steps.findIndex(step => step.label === order.status);

    await fetch(`/ban-hang-online/back-order-status/${order.id}`,{
        method:"PUT"
    }).then(res => {
        if (!res.ok) throw new Error("Không thể hủy đơn hàng");
        return res.json();
    }).then(data => {
        if (order) {
            order.status = data.trangThaiThanhToan;
            if (!order.steps.some(step => step.label === data.trangThaiThanhToan)) {
                if(data.trangThaiThanhToan === "Đã hủy"){
                    order.steps.splice(currentStep+1, 0, {
                        label: "Đã hủy",
                        icon: "fas fa-times-circle"
                    });
                }else{
                    // Chèn "Chờ hoàn tiền" vào vị trí hiện tại
                    order.steps.splice(currentStep, 0, {
                        label: "Chờ hoàn tiền",
                        icon: "fa-solid fa-money-bill-transfer"
                    });
                }
            }
            localStorage.setItem("ordersOnl", JSON.stringify(ordersOnl));
            renderOnlOrder();
        }
    }).catch(err => alert(err.message));
}

// Hàm này sẽ update thanh trạng thái
function updateStatusBar(status, orderId) {
    const order = ordersOnl.find(o => o.id === orderId);
    const statusBar = document.getElementById("status-bar");
    statusBar.innerHTML = "";

    // Nếu trạng thái là "Đã hoàn thành", coi mọi step là completed
    const isFinished = (status === "Đã hoàn thành");
    const lastIndex = order.steps.length - 1;
    const currentIndex = isFinished
        ? lastIndex
        : order.steps.findIndex(step => step.label === status);

    order.steps.forEach((step, index) => {
        const stepDiv = document.createElement("div");
        stepDiv.classList.add("step");

        // Xử lý đặc biệt cho trường hợp "Đã hoàn tiền"
        if (status === "Đã hoàn tiền" && step.label === "Chờ hoàn tiền") {
            stepDiv.classList.add("completed");
        } else if (isFinished || index < currentIndex) {
            stepDiv.classList.add("completed");
        } else if (index === currentIndex) {
            stepDiv.classList.add("current");
        }

        let iconAttrs = "";

        if(step.label === "Đã hủy"){
            iconAttrs = "style='background-color:red;color:white;'";
        }else if(step.label === "Đã hoàn tiền"){
            iconAttrs = "style='background-color:green;color:white;'";
        }

        stepDiv.innerHTML = `
            <div class="icon" ${iconAttrs}>
                <i class="${step.icon}"></i>
            </div>
            <p>${step.label}</p>
        `;
        statusBar.appendChild(stepDiv);
    });
}

async function completeRefund() {
    let orderId = Number(document.getElementById("orderId").value);
    let order = ordersOnl.find(o => o.id === orderId);

    if (!confirm("Bạn có chắc muốn xác nhận đã hoàn tiền cho đơn hàng này?")) {
        return;
    }

    try {
        const response = await fetch(`/ban-hang-online/complete-refund/${order.id}`, {
            method: "PUT"
        });

        if (!response.ok) {
            throw new Error("Không thể cập nhật trạng thái hoàn tiền");
        }

        const data = await response.json();
        
        // Cập nhật trạng thái đơn hàng
        order.status = "Đã hoàn tiền";
        
        // Cập nhật steps
        const currentStep = order.steps.findIndex(step => step.label === "Chờ hoàn tiền");
        if (currentStep !== -1) {
            order.steps[currentStep].label = "Đã hoàn tiền";
        }

        // Lưu vào localStorage
        localStorage.setItem("ordersOnl", JSON.stringify(ordersOnl));
        
        // Render lại giao diện
        renderOnlOrder();
    } catch (err) {
        alert(err.message);
    }
}

renderOnlOrder()
connectSocket()