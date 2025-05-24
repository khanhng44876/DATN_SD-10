let cart = JSON.parse(localStorage.getItem("cart")) || [];
let total_price = JSON.parse(localStorage.getItem("total_price")) || 0;
let selected_promo = JSON.parse(localStorage.getItem("selected_promo"))||null;
let old_total = JSON.parse(localStorage.getItem("old_total"))||0;
let total_div = document.getElementById('total_price');

let applied_promo = document.getElementById("applied-promo")

const modal = new bootstrap.Modal(document.getElementById('promoModal'));

function openPromoModal() {
    document.querySelectorAll('#promoModal .promo-box').forEach(box => {
            box.classList.remove('selected');
            const chk = box.querySelector('.promo-check');
            if (chk) chk.style.visibility = 'hidden';
        });

    // 2. Lấy promo đã chọn (nếu có)
    const selected = JSON.parse(localStorage.getItem('selected_promo'));
    if (selected) {
        // 3. Tìm box trong modal và đánh dấu
        const box = document.querySelector(`#promoModal .promo-box[data-id="${selected.id}"]`);
        if (box) {
            box.classList.add('selected');
            const chk = box.querySelector('.promo-check');
            if (chk) chk.style.visibility = 'visible';
            // optional: scroll vào view
            box.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
    }

    modal.show();
}

function saveToLocalStorage(){
    localStorage.setItem("cart",JSON.stringify(cart))

}

function renderCart(){
    document.getElementById("countItem").innerText= cart !== "" ? cart.length : 0;
    document.getElementById("product").innerHTML = ""
    if(selected_promo===null){
        total_div.innerHTML = `
            <strong class="text-danger">Thành tiền : <span>${total_price}</span> VNĐ</strong>
        `
    }else{
        old_total = total_price;
        let discount = total_price * selected_promo.discount / 100;
        total_price -= Math.min(discount,selected_promo.max);
        localStorage.setItem('old_total',old_total);
        localStorage.setItem('total_price',total_price);
        total_div.innerHTML=`
            <strong class="text-danger">Thành tiền :</strong><span style="color: #888;text-decoration: line-through;">${old_total.toLocaleString("vi-VN")} VNĐ</span>
            <strong class="text-danger">${total_price.toLocaleString("vi-VN")} VNĐ</strong>
        `
    }
    createElementCart()
    if(total_price === 0){
        applied_promo.innerHTML = `
                <div class="promo-box apply" onclick="openPromoModal()">
                    <p>Chọn khuyến mãi</p>
                </div>
            `
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

        total_price = total;

        saveToLocalStorage();
        localStorage.setItem("total_price", JSON.stringify(total_price));

        const res = await fetch(`/ban-hang-online/best-km/${total_price}`,{
            method:"GET"
        })
        const data = await res.json();
        console.log(data)
        if(data.id && total_price > 0){
            applied_promo.innerHTML = `
                <div class="promo-box apply" data-id="${data.id}" data-condition="${data.dieu_kien}" onclick="openPromoModal()">
                    <span class="promo-check" style="visibility:visible;">✔️</span>
                    <h5>Tên KM: ${data.ten_khuyen_mai}</h5>
                    <p>Giảm: ${data.phan_tram_giam}% (tối đa ${Number(data.giam_toi_da).toLocaleString('vi-VN')}₫)</p>
                    <p>Áp dụng cho đơn từ ${Number(data.dieu_kien).toLocaleString('vi-VN')}₫</p>
                </div>
            `
            selected_promo = {
                id:data.id,
                discount:data.phan_tram_giam,
                max:data.giam_toi_da
            }
            localStorage.setItem('selected_promo',JSON.stringify(selected_promo));
        }else {
            applied_promo.innerHTML = `
                <div class="promo-box apply" onclick="openPromoModal()">
                    <p>Chọn khuyến mãi</p>
                </div>
            `
            selected_promo = null
        }
        if(selected_promo === null){
            total_div.innerHTML = `
            <strong class="text-danger">Thành tiền : <span>${total_price}</span> VNĐ</strong>
        `
        }else{
            old_total = total_price;
            let discount = total_price * selected_promo.discount / 100;
            total_price -= Math.min(discount,selected_promo.max);
            localStorage.setItem('old_total',JSON.stringify(old_total));
            localStorage.setItem('total_price',JSON.stringify(total_price));
            total_div.innerHTML=`
            <strong class="text-danger">Thành tiền :</strong><span style="color: #888;text-decoration: line-through;">${old_total.toLocaleString("vi-VN")} VNĐ</span>
            <strong class="text-danger">${total_price.toLocaleString("vi-VN")} VNĐ</strong>
        `
        }
        updatePromoListByTotal()

    }
}

function buying(){
    let activeElement = document.querySelector('.payment-option.active');
    let httt = activeElement.querySelector('input[type="hidden"]').value;
    if(httt === "Online"){
        confirmOrder_Vnpay();
    }else{
        confirmOrder();
    }
}

 async function confirmOrder_Vnpay(){
     let idkh = Number(document.getElementById("idkh").value);
     let today = new Date().toISOString().split("T")[0];
     let total = total_price;
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
         id_khuyen_mai: selected_promo===null?null:selected_promo.id,
         ngay_tao: today,
         ngay_sua: null,
         don_gia: null,
         tong_tien: total,
         trang_thai_thanh_toan: "Chờ xác nhận",
         hinh_thuc_thanh_toan: "Online",
         dia_chi_giao_hang: location,
         ghi_chu: ghichu || null
     }
     try {
         const resp = await fetch("/ban-hang-online/create-order", {
             method: "POST",
             headers: { "Content-Type": "application/json" },
             body: JSON.stringify(hdJson)
         });

         if (!resp.ok) {
             // đọc thêm message từ server nếu cần
             const error = await resp.json();
             throw new Error(error.error || "Lỗi khi tạo đơn Online");
         }

         const result = await resp.json();

         let productList = cart.filter(c => c.active === true);
         let requests = productList.map((p) => {
             let hdctJson = {
                 id_hoa_don:result.id,
                 id_san_pham_chi_tiet:Number(p.id),
                 so_luong:p.quantity,
                 don_gia:p.price,
                 tong_tien:p.total,
                 thanh_tien:p.total,
                 ngay_tao: today,
                 ngay_sua: null,
                 trang_thai: "Chưa thanh toán"
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

         if (result.redirectUrl) {
             window.location.href = result.redirectUrl;
         } else {
             throw new Error("VnPay không trả về URL");
         }
     } catch (error) {
         console.error(error);
         alert("Lỗi VNPay: " + error.message);
     }
}

// Hàm này sẽ confirm hóa đơn gửi về DB
async function confirmOrder() {
    let idkh = Number(document.getElementById("idkh").value);
    let today = new Date().toISOString().split("T")[0];
    let total = total_price;
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
            id_khuyen_mai: selected_promo===null?null:selected_promo.id,
            ngay_tao: today,
            ngay_sua: null,
            don_gia: null,
            tong_tien: total,
            trang_thai_thanh_toan: "Chờ xác nhận",
            hinh_thuc_thanh_toan: "COD",
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
        localStorage.setItem("cart", JSON.stringify(cart));
        localStorage.setItem("total_price", JSON.stringify(total_price));
        localStorage.removeItem("selected_promo");
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

function updatePromoListByTotal() {
    let total = parseFloat(localStorage.getItem("total_price")) || 0;
    let old_total = parseFloat(localStorage.getItem("old_total"))||0;
    if(old_total !== 0){
        total = old_total;
    }
    const promoBoxes = document.querySelectorAll(".promo-box:not(.apply)");
    promoBoxes.forEach(box => {
        const condition = parseFloat(box.getAttribute("data-condition")) || 0;

        if (total === 0 || total < condition) {
            box.classList.add("disabled");
        } else {
            box.classList.remove("disabled");
        }
    });
}

function selectPromo(el) {
    document.querySelectorAll('.promo-box').forEach(box => {
        box.classList.remove('selected');
        const chk = box.querySelector('.promo-check');
        if (chk) chk.style.visibility = 'hidden';
    });

    el.classList.add('selected');
    const promoCheck = el.querySelector('.promo-check');
    if (promoCheck) promoCheck.style.visibility = 'visible';

    const id        = el.dataset.id;
    const name      = el.dataset.name;
    const discount  = el.dataset.discount;
    const condition = el.dataset.condition;
    const max       = el.dataset.max;

    applied_promo.innerHTML = `
    <div class="promo-box apply" data-id="${id}" data-condition="${condition}" onclick="openPromoModal()">
      <span class="promo-check" style="visibility:visible;">✔️</span>
      <h5>Tên KM: ${name}</h5>
      <p>Giảm: ${discount}% (tối đa ${Number(max).toLocaleString('vi-VN')}₫)</p>
      <p>Áp dụng cho đơn từ ${Number(condition).toLocaleString('vi-VN')}₫</p>
    </div>
   `;
    let selected_promo = {
        id:id,
        discount:discount,
        max:max
    }
    localStorage.setItem('selected_promo', JSON.stringify(selected_promo));
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