let selected_promo = JSON.parse(localStorage.getItem('selected_promo'||'{}'));
window.stompClient = null;
// Reload trang
function renderOnlOrder(){
    document.getElementById("product-list").innerHTML = "";
    let orderId = Number(document.getElementById("orderId").value);
    let order = ordersOnl.find(o => o.id === orderId)
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
    order.total_amount = order.listhdct.reduce((sum, c) => sum + c.total, 0);
    
    // Áp dụng khuyến mãi nếu có
    if (selected_promo && selected_promo.id) {
        const temp = order.total_amount * Number(selected_promo.discount) / 100;
        if (temp > Number(selected_promo.max)) {
            order.total_amount -= Number(selected_promo.max);
        } else {
            order.total_amount -= temp;
        }
    }

    // Lưu vào localStorage
    localStorage.setItem("ordersOnl", JSON.stringify(ordersOnl));

    // Gửi cập nhật lên server
    const payload = {
        orderId: order.id,
        itemId: item.id,
        totalAmount: order.total_amount,
        quantity: item.quantity
    };

    // Gửi qua WebSocket
    if (stompClient && stompClient.connected) {
        stompClient.send("/app/update-quantity", {}, JSON.stringify(payload));
    } else {
        console.error("WebSocket không kết nối!");
    }

    // Cập nhật giao diện
    renderOnlOrder();
}

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
                order.total_amount = newStatus.totalAmount;
                localStorage.setItem("ordersOnl",JSON.stringify(ordersOnl))
                renderOnlOrder()
            }else {
                // Cập nhật localStorage và giao diện
                const order = ordersOnl.find(o => o.id === orderId);
                const oldStatus = order.status;
                if (order) {
                    order.status = newStatus.status;
                    // Nếu trạng thái mới chưa có trong steps thì thêm vào
                    if (newStatus.status === "Đã hủy") {
                        if (!order.steps.some(s => s.label === newStatus.status)) {
                            let currentStep = order.steps.findIndex(step => step.label === oldStatus);
                            order.steps.splice(currentStep + 1, 0, {
                                label: "Đã hủy",
                                icon: "fas fa-times-circle"
                            });
                        }
                    }
                    if (newStatus.status === "Chờ hoàn tiền") {
                        if (!order.steps.some(s => s.label === newStatus.status)) {
                            let currentStep = order.steps.findIndex(step => step.label === oldStatus);
                            order.steps.splice(currentStep + 1, 0, {
                                label: "Chờ hoàn tiền",
                                icon: "fa-solid fa-money-bill-transfer"
                            });
                        }
                    }
                    if (newStatus.status === "Đã hoàn tiền") {
                        if (!order.steps.some(s => s.label === newStatus.status)) {
                            let currentStep = order.steps.findIndex(step => step.label === oldStatus);
                            order.steps.splice(currentStep + 1, 0, {
                                label: "Đã hoàn tiền",
                                icon: "fas fa-circle-check"
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

function cancelOrder() {
    let orderId = Number(document.getElementById("orderId").value);
    let order = ordersOnl.find(o => o.id === orderId);
    
    if (!order) {
        alert("Không tìm thấy đơn hàng!");
        return;
    }

    if (!confirm("Bạn có chắc muốn hủy đơn hàng này?")) {
        return;
    }

    fetch(`/ban-hang-online/cancel-order/${orderId}`, {
        method: "PUT"
    })
    .then(res => {
        if (!res.ok) {
            if (res.status === 404) {
                throw new Error("Không tìm thấy đơn hàng");
            } else if (res.status === 400) {
                throw new Error("Không thể hủy đơn hàng ở trạng thái này");
            } else {
                throw new Error("Lỗi server: " + res.status);
            }
        }
        return res.json();
    })
    .then(data => {
        if (order) {
            order.status = "Đã hủy";
            
            // Cập nhật steps
            const currentStep = order.steps.findIndex(step => step.label === order.status);
            if (!order.steps.some(step => step.label === "Đã hủy")) {
                order.steps.splice(currentStep + 1, 0, {
                    label: "Đã hủy",
                    icon: "fas fa-times-circle"
                });
            }

            localStorage.setItem("ordersOnl", JSON.stringify(ordersOnl));
            renderOnlOrder();
            alert("Hủy đơn hàng thành công!");
        }
    })
    .catch(err => {
        console.error("Lỗi khi hủy đơn hàng:", err);
        alert(err.message);
    });
}
// Hàm này sẽ update thanh trạng thái
function updateStatusBar(status, orderId) {
    const order = ordersOnl.find(o => o.id === orderId);
    const statusBar = document.getElementById("status-bar");
    statusBar.innerHTML = "";

    // Nếu trạng thái là "Đã Hoàn thành", coi mọi step là completed
    const isFinished = (status === "Đã hoàn thành");
    const lastIndex = order.steps.length - 1;
    const currentIndex = isFinished
        ? lastIndex
        : order.steps.findIndex(step => step.label === status);

    order.steps.forEach((step, index) => {
        const stepDiv = document.createElement("div");
        stepDiv.classList.add("step");

        if (isFinished || index < currentIndex) {
            stepDiv.classList.add("completed");
        } else if (index === currentIndex) {
            stepDiv.classList.add("current");
        }
        let iconAttrs = ""
        if(step.label === "Đã hủy"){
             iconAttrs = "style='background-color:red;color:white;'";
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

renderOnlOrder()
connectSocket()