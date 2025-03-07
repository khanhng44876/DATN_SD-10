let orders = JSON.parse(localStorage.getItem("orders")) || []

function renderOrders(){
    document.getElementById("nav-tab").innerHTML = ""
    document.getElementById("nav-tabContent").innerHTML=""
    orders.forEach(order => createElementOrder(order))
}

function saveOrderToLocalStorage(){
    localStorage.setItem("orders",JSON.stringify(orders))
}

function createOrder(){
    let orderId = orders.length ? orders[orders.length - 1].id + 1 : 1;
    let orderName = `Đơn hàng ${orderId}`
    let order ={id: orderId,name:orderName}
    orders.push(order)
    saveOrderToLocalStorage()
    createElementOrder(order);
}

function createElementOrder(order){
    if(orders.length > 5){
        alert("Tối đa 5")
        return;
    }
    let li = document.createElement("li");
    li.classList.add("nav-item");
    li.id = `li-${order.id}`;

    let tab = document.createElement("button");
    tab.classList.add("nav-link");
    tab.setAttribute("data-bs-toggle", "tab");
    tab.setAttribute("data-bs-target", `#content-${order.id}`);
    tab.innerHTML = `${order.name} 
        <span class="text-danger" style="cursor:pointer;" onclick="removeOrder(${order.id})">❌</span>`;

    li.appendChild(tab);
    document.getElementById("nav-tab").appendChild(li);

    // 📌 Tạo nội dung đơn hàng
    let tabContent = document.createElement("div");
    tabContent.classList.add("tab-pane", "fade");
    tabContent.id = `content-${order.id}`;
    tabContent.innerHTML = `<p>Chi tiết đơn hàng ${order.id}</p>`;

    document.getElementById("nav-tabContent").appendChild(tabContent);
}

function removeOrder(orderId){
    orders = orders.filter(order => order.id !== orderId)
    saveOrderToLocalStorage()
    renderOrders()
}

renderOrders()