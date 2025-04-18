let cart = JSON.parse(localStorage.getItem("cart")) || [];
let total_price = JSON.parse(localStorage.getItem("total_price")) || 0;

let applied_promo = document.getElementById("applied-promo")

const modal = new bootstrap.Modal(document.getElementById('promoModal'));

function openPromoModal() {
    modal.show();
}

function saveToLocalStorage(){
    localStorage.setItem("cart",JSON.stringify(cart))

}

function renderCart(){
    document.getElementById("countItem").innerText= cart !== "" ? cart.length : 0;
    document.getElementById("product").innerHTML = ""
    document.getElementById("total-price").innerText = total_price
    createElementCart()
    if(total_price === 0){
        let promoActive = document.createElement("p")
        promoActive.innerHTML = "Chọn khuyến mãi"
        applied_promo.appendChild(promoActive)
    }
    updatePromoListByTotal()
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
async function handleCheckboxChange(event) {
    applied_promo.innerHTML=""
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
        const res = await fetch(`/ban-hang-online/best-km/${total_price}`,{
            method:"GET"
        })
        const data = await res.json();
        if(data === [] || total_price === 0){
            applied_promo.innerHTML = `
                <div className="promo-box" onClick="openPromoModal()">
                    <p>Chọn khuyến mãi</p>
                </div>
            `
        }else {
            applied_promo.innerHTML = `
                <div className="promo-box" onClick="openPromoModal()">
                    <h5 id="promo-name">Tên khuyến mãi: ${data.ten_khuyen_mai}</h5>
                    <p id="promo-discount">Phần trăm giảm: ${Number(data.phan_tram_giam)}%</p>
                    <p id="promo-condition">Điều kiện: Áp dụng cho đơn từ ${Number(data.dieu_kien).toLocaleString("vi-VN")} VNĐ</p>
                    <p id="promo-max-discount">Giảm tối đa: ${Number(data.giam_toi_da).toLocaleString("vi-VN")} VNĐ</p>
                </div>
            `
        }

        updatePromoListByTotal()

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
            let hdctRequest = fetch("/ban-hang-online/create-order-ct", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(hdctJson)
            }).then(response => response.json())
                .then(data => console.log(data))
                .catch(err => {
                    console.error("Lỗi thêm hóa đơn chi tiết:", err);
                    throw err;
                });

            return Promise.all([hdctRequest]);
        })
        await Promise.all(requests.flat());
        cart = cart.filter(item => !item.active);
        total_price = 0;
        localStorage.setItem("cart", JSON.stringify(cart));
        localStorage.setItem("total_price", JSON.stringify(total_price));
        localStorage.removeItem("selected_promo");
        alert("Thành công");
        renderCart()
    } catch (error) {
        console.error("Lỗi:", error);
        alert("Đã xảy ra lỗi khi xác nhận đơn hàng!");
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

function updatePromoListByTotal() {
    const total = parseFloat(localStorage.getItem("total_price")) || 0;
    const promoBoxes = document.querySelectorAll(".promo-box");
    promoBoxes.forEach(box => {
        const condition = parseFloat(box.getAttribute("data-condition")) || 0;

        if (total === 0 || total < condition) {
            box.classList.add("disabled");
        } else {
            box.classList.remove("disabled");
        }
    });
}

function selectPromo(element) {
    if (element.classList.contains("disabled")) return;

    // Clear all selections
    document.querySelectorAll(".promo-box").forEach(box => {
        box.classList.remove("selected");
        box.querySelector(".promo-check").style.visibility = "hidden";
    });

    // Mark current
    element.classList.add("selected");
    element.querySelector(".promo-check").style.visibility = "visible";

    // Gán ID promo đã chọn vào local hoặc gửi về server
    const selectedId = element.getAttribute("data-id");
    localStorage.setItem("selected_promo", selectedId);
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