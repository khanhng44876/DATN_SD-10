let cart = JSON.parse(localStorage.getItem("cart")) || [];
let total_price = JSON.parse(localStorage.getItem("total_price")) || 0;

function saveToLocalStorage(){
    localStorage.setItem("cart",JSON.stringify(cart))

}

function renderCart(){
    document.getElementById("countItem").innerText= cart !== "" ? cart.length : 0;
    document.getElementById("product").innerHTML = ""
    document.getElementById("total-price").innerText = total_price
    createElementCart()
}

function createElementCart(){
    let productList = document.getElementById("product")
    cart.forEach(p=>{
        let isActive = (p.active) ? "checked" : ""
        let cardProduct = document.createElement("div");
        let hr = document.createElement("hr")
        cardProduct.classList.add("d-flex","justify-content-space-between","align-item-center")
        cardProduct.innerHTML = `
            <div class="row ">
                <div class="col-1">
                    <input type="checkbox" class="checkbox-item" data-id="${p.id}" data-price = "${p.total}" ${isActive}>
                </div>
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
                <div class="col text-canter">
                    <div class="input-group input-group-sm">
                        <button class="btn btn-outline-secondary btn-sm" onclick="updateQuantity(${p.id},-1)">-</button>
                        <span class="form-control text-center" id="quantity-${p.id}">${p.quantity}</span>
                        <button class="btn btn-outline-secondary btn-sm" onclick="updateQuantity(${p.id},1)">+</button>
                    </div>
                </div>
                <div class="col text-end text-danger fw-bold">
                        <span id="total-${p.id}">${p.total==null ? 0 : p.total.toLocaleString("vi-VN")}</span> VND
                </div>
                <div class="col text-end">
                     <button class="btn btn-danger btn-sm" onclick="removeItem(${p.id})">
                            <i class="bi bi-trash"></i>
                     </button>
                </div>   
            </div>
            
        `;
        productList.appendChild(cardProduct)
        productList.appendChild(hr)
    })
}

function updateQuantity(itemId,num){
    let item = cart.find(c=>c.id===itemId)
    console.log(item)
    if(!item){
        alert("không tìm thấy item")
        return;
    }
    item.quantity = Math.max(1,item.quantity+num)
    item.total = item.quantity * item.price
    saveToLocalStorage();
    renderQuantity(item.id);

}
function renderQuantity(itemId){
    let item = cart.find(c=>c.id===itemId)
    document.getElementById(`quantity-${item.id}`).innerText = item.quantity
    document.getElementById(`total-${item.id}`).innerText = item.total.toLocaleString("vi-VN")
    let checkbox = document.querySelector(`.checkbox-item[data-id="${item.id}"]`);
    if (checkbox) {
        checkbox.setAttribute("data-price", item.total);
    }

    handleCheckboxChange({ target: checkbox });
}
function removeItem(cartId){
    cart = cart.filter(c => c.id !== cartId)
    saveToLocalStorage();
    renderCart();
}

function handleCheckboxChange(event) {
    if (event.target.classList.contains("checkbox-item")) {
        let checkbox = event.target;
        let id = checkbox.getAttribute("data-id");
        let total = 0;

        let item = cart.find(c => c.id === Number(id));
        if (item) {
            item.active = checkbox.checked;
        }

        document.querySelectorAll('.checkbox-item:checked').forEach(checkbox => {
            total += parseInt(checkbox.getAttribute('data-price')) || 0;
        });

        let total_price = total;

        saveToLocalStorage();
        localStorage.setItem("total_price", JSON.stringify(total_price));

        document.getElementById('total-price').innerText = total_price.toLocaleString("vi-VN");
    }
}


document.addEventListener("change", handleCheckboxChange);

function changeLocation(){
    let newLocation = document.getElementById("newLocation").value
    if(newLocation === ""){
        document.getElementById("errLocation").innerText = "Không được để trống trường này"
        return;
    }
    document.getElementById("newLocation").value = newLocation
    let modal = bootstrap.Modal.getInstance(document.getElementById('quantityModal'));
    modal.hide()
    document.getElementById("errLocation").innerText = ""
}
document.addEventListener("DOMContentLoaded", function () {
    const paymentOptions = document.querySelectorAll(".payment-option");

    paymentOptions.forEach(option => {
        option.addEventListener("click", function () {
            // Xóa class "active" khỏi tất cả
            paymentOptions.forEach(opt => opt.classList.remove("active"));

            // Thêm class "active" vào phần tử được chọn
            this.classList.add("active");
        });
    });
});
renderCart();