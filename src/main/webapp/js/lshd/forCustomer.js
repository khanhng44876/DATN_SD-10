
// Reload trang
function renderOnlOrder(){
    let orderId = Number(document.getElementById("orderId").value);
    let order = orders.find(o => o.id === orderId)
    console.log(order)
    let status = order.status;
    updateStatusBar(status);
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

function cancelOrder() {
    let orderId = Number(document.getElementById("orderId").value);
    let order = orders.find(o => o.id === orderId)

    const currentStep = order.steps.findIndex(step => step.label === order.status);

    if (!order.steps.some(s => s.label === "Đã hủy")) {
        order.steps.splice(currentIndex + 1, 0, {
            label: "Đã hủy",
            icon: "fas fa-times-circle"
        });
    }

    order.status = "Đã hủy";
    localStorage.setItem("orders", JSON.stringify(orders));
    renderOnlOrder();
}
// Hàm này sẽ update thanh trạng thái
function updateStatusBar(status) {
    console.log(status)
    const statusBar = document.getElementById("status-bar");
    statusBar.innerHTML = "";

    const currentIndex = steps.findIndex(step => step.label === status);

    // Nếu không phải "Đã hủy" thì chỉ hiển thị từ đầu đến trạng thái hiện tại
    const visibleSteps = steps.filter(step => step.id > 0).slice(0, currentIndex); // lọc bỏ id -1

    steps.slice(0, currentIndex + 1).forEach((step, index) => {
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