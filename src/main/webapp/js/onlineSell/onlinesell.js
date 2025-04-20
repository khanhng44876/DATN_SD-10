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
// Hàm này tạo các thành phần trong giỏ hàng
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
// Hàm cập nhật số lượng
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
// Chỉ load so lượng và giá không reload ảnh
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
// Xóa item khỏi giỏ
function removeItem(cartId){
    cart = cart.filter(c => c.id !== cartId)
    saveToLocalStorage();
    renderCart();
}
// Theo dõi những thay đổi của checkbox
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
// Hàm này sẽ confirm hóa đơn gửi về DB
async function confirmOrder() {
    let idkh = Number(document.getElementById("idkh").value);
    let today = new Date().toISOString().split("T")[0];
    let total = document.getElementById("total-price").innerText.replace(/[^\d]/g, "");
    let activeElement = document.querySelector('.payment-option.active');
    let httt = activeElement.querySelector('input[type="hidden"]').value;
    let location = document.getElementById("locationKh").innerText;
    let ghichu = document.getElementById("ghiChu").value;


    // Lấy hình thức thanh toán từ input hidden
    let hiddenInput = document.querySelector('input[name="paymentMethod"][type="hidden"]');

    // Kiểm tra input hidden
    if (!hiddenInput) {
        alert("Không tìm thấy thông tin hình thức thanh toán!");
        return;
    }
    console.log(idkh, today, total, httt, location, ghichu)
      let  hdJson = {
            id_nhan_vien: 1,
            id_khach_hang: Number(idkh),
            id_khuyen_mai: null,
            ngay_tao: today,
            ngay_sua: null,
            don_gia: null,
            tong_tien: total,
            trang_thai_thanh_toan: "Chờ xác nhận",
            hinh_thuc_thanh_toan: httt,
            dia_chi_giao_hang: location,
            ghi_chu: ghichu || null
        }
    try {
        let response = await fetch("/ban-hang-online/create-order", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(hdJson)
        });
        if (!response.ok) throw new Error("Lỗi khi tạo đơn hàng");
        let result = await response.json();
                console.log("Kết quả:", result);
        let id = result.id;
        if (!id) {
            alert("Không thể tạo hóa đơn!");
            return;
        }

        await new Promise(resolve => setTimeout(resolve, 500));

        let productList = cart.filter(c => c.active === true);
        let requests = productList.map((p) => {
            let hdctJson = {
                id_hoa_don:id,
                id_san_pham_chi_tiet:Number(p.id),
                so_luong:p.quantity,
                don_gia:p.price,
                tong_tien:p.total,
                thanh_tien:p.total,
                ngay_tao: today,
                ngay_sua: null,
                trang_thai: "Chờ xác nhận"
            };
            // **Tạo request POST thêm hóa đơn chi tiết**
            return fetch("/ban-hang-online/create-order-ct", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(hdctJson)
            }).then(response => {
                if (!response.ok) throw new Error("Lỗi khi tạo chi tiết hóa đơn");
                return response.json();
            });
        });
        await Promise.all(requests);

        // Xóa giỏ hàng
        cart = cart.filter(c => !c.active);
        total_price = 0;
        saveToLocalStorage();
        renderCart();

        // Xử lý thanh toán
        if (httt === "Online" && result.redirectUrl) {
            window.location.href = result.redirectUrl;
        } else if (httt === "COD") {
            alert("Đặt hàng COD thành công!");
            window.location.href = "/ban-hang-online";
        } else {
            throw new Error("Phản hồi không hợp lệ từ server");
        }
    } catch (error) {
        console.error("Lỗi:", error);
        alert("Đã xảy ra lỗi khi xác nhận đơn hàng: " + error.message);
    }
}
// Gán sự kiện thayddooiri cho checkbox
document.addEventListener("change", handleCheckboxChange);

// Hàm thay đổi địa chỉ
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

// Gán sự kiện cho 2 nút Hình thức thanh toán
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