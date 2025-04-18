let ordersOnl = JSON.parse(localStorage.getItem("ordersOnl"));
let stompClient = null;
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

function updateQuantity(itemId,num){
    let orderId = Number(document.getElementById("orderId").value);
    let order = ordersOnl.find(o => o.id === orderId)
    console.log(order)
    let item = order.listhdct.find(c=>c.id===itemId)
    order.total_amount = 0;
    console.log(item)
    if(!item){
        alert("không tìm thấy item")
        return;
    }
    item.quantity = Math.max(1,item.quantity+num)
    item.total = item.quantity * item.price
    order.listhdct.forEach(c=>{
        order.total_amount += c.total;
    })
    localStorage.setItem("ordersOnl", JSON.stringify(ordersOnl));
    const payload = {
        orderId : order.id,
        itemId : item.id,
        totalAmount : order.totalAmount,
        quantity : item.quantity
    }
    stompClient.send("app/update-quantity",{},JSON.stringify(payload));
    renderOnlOrder()

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
            const newStatus = message.body;
            // const payload = JSON.parse(message.body);
            console.log("Nhận trạng thái mới từ Admin:", newStatus);

            // if(payload.type === "update-quantity"){
            //     let order = ordersOnl.find(o => o.id === orderId)
            //     let item = order.listhdct.find(p => p.id === payload.itemId)
            //     if(item){
            //         item.quantity = payload.quantity;
            //         item.total = payload.quantity * item.price;
            //     }
            //     order.total_amount = payload.totalAmount;
            //     localStorage.setItem("orders",JSON.stringify(orders))
            //     renderOnlOrder()
            // }
            // Cập nhật localStorage và giao diện
            const order = ordersOnl.find(o => o.id === orderId);
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

                localStorage.setItem("ordersOnl", JSON.stringify(ordersOnl));
                renderOnlOrder();
            }
        });
    });
}

function cancelOrder() {
    let orderId = Number(document.getElementById("orderId").value);
    let order = ordersOnl.find(o => o.id === orderId)

    const currentStep = order.steps.findIndex(step => step.label === order.status);

    order.status = "Đã hủy";
    localStorage.setItem("ordersOnl", JSON.stringify(ordersOnl));
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

        localStorage.setItem("ordersOnl", JSON.stringify(ordersOnl));
        renderOnlOrder();
    }
    }).catch(err => alert(err.message));
}
// Hàm này sẽ update thanh trạng thái
function updateStatusBar(status,orderId) {
    let order = ordersOnl.find(o => o.id === orderId)
    const statusBar = document.getElementById("status-bar");
    statusBar.innerHTML = "";

    const currentIndex = order.steps.findIndex(step => step.label === status);

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