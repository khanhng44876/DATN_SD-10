// Hàm mowr Modal
function openModal(button){
    let mode = button.value;
    document.getElementById("modalTitle").innerText = mode === "update" ? "Cập nhật khuyến mãi" : "Thêm khuyến mãi"
    let saveButton = document.getElementById("save");
    saveButton.setAttribute("data-mode", mode);
    if(mode === "update"){
        let id = button.getAttribute("data-id")
        document.getElementById("km_id").value = id
        fetch(`/khuyen-mai/detail/${id}`,{
            method:"GET"
        })
            .then(response => response.json())

            .then(data =>{
                console.log(data)
                document.getElementById("ma_khuyen_mai").value = data.ma_khuyen_mai;
                document.getElementById("ten_khuyen_mai").value = data.ten_khuyen_mai;
                document.getElementById("so_luong").value = data.so_luong;
                document.getElementById("muc_giam").value = data.muc_giam;
                document.getElementById("giam_toi_da").value = data.giam_toi_da;
                document.getElementById("dieu_kien").value = data.dieu_kien;
                document.getElementById("mo_ta").value = data.mo_ta;
                document.getElementById("ngay_bat_dau").value = data.ngay_bat_dau;
                document.getElementById("ngay_ket_thuc").value = data.ngay_ket_thuc;
                document.getElementById("trang_thai").value = data.trang_thai;
            })
            .catch(error => console.error("Lỗi",error))

    }else{
        document.getElementById("kmForm").reset();

    }

}
// Hàm thêm
function themKm(){
    let kmJson = {
        ma_khuyen_mai: document.getElementById("ma_khuyen_mai").value,
        ten_khuyen_mai: document.getElementById("ten_khuyen_mai").value,
        so_luong: document.getElementById("so_luong").value,
        muc_giam:Number(document.getElementById("muc_giam").value.replace("%","").trim()),
        giam_toi_da:document.getElementById("giam_toi_da").value,
        dieu_kien:document.getElementById("dieu_kien").value,
        mo_ta:document.getElementById("mo_ta").value,
        ngay_bat_dau: document.getElementById("ngay_bat_dau").value,
        ngay_ket_thuc: document.getElementById("ngay_ket_thuc").value

    }
    fetch("/khuyen-mai/add-km",{
        method: "POST",
        headers : {
            "Content-Type" : "application/json"
        },
        body: JSON.stringify(kmJson)
    })
        .then(response => response.json())
        .then(data => {
            console.log(data)
            alert("thêm thành công")

            let table = document.getElementById("kmTable").getElementsByTagName("tbody")[0];
            let newRow = table.insertRow();
            newRow.innerHTML =`
                       
                        <td>${data.ma_khuyen_mai}</td>
                        <td>${data.ten_khuyen_mai}</td>
                        <td>${data.so_luong}</td>
                        <td>${data.mo_ta}</td>
                        <td>${data.muc_giam +"%"}</td>
                        <td>${data.giam_toi_da}</td>
                        <td>${data.dieu_kien}</td>
                        <td>${data.ngay_bat_dau}</td>
                        <td>${data.ngay_ket_thuc}</td>
                        <td>${data.trang_thai}</td>
                        <td>${data.so_luong_sd}</td>
                        <td>
                            <button type="button" data-id=${data.id} value="update" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#kmModal" onclick="openModal(this)">
                                <i class="fa fa-pencil-alt"></i>
                            </button>
                        </td>
                    `;
            document.getElementById("kmForm").reset()
        })
}
// Hàm cập nhật
function capnhatKm(id){
    let kmJson = {
        ma_khuyen_mai: document.getElementById("ma_khuyen_mai").value,
        ten_khuyen_mai: document.getElementById("ten_khuyen_mai").value,
        so_luong: document.getElementById("so_luong").value,
        muc_giam:Number(document.getElementById("muc_giam").value.replace("%","").trim()),
        giam_toi_da:document.getElementById("giam_toi_da").value,
        dieu_kien:document.getElementById("dieu_kien").value,
        mo_ta:document.getElementById("mo_ta").value,
        ngay_bat_dau: document.getElementById("ngay_bat_dau").value,
        ngay_ket_thuc: document.getElementById("ngay_ket_thuc").value,
        so_luong_sd:0
    }
    fetch(`/khuyen-mai/update/${id}`,{
        method:"PUT",
        headers : {
            "Content-Type" : "application/json"
        },
        body: JSON.stringify(kmJson)
    })
        .then(response => response.json())
        .then(data => {
            alert("Cập nhật thành công")
            let row = document.querySelector(`button[data-id='${id}']`).closest("tr")
            row.cells[0].innerText = data.ma_khuyen_mai
            row.cells[1].innerText = data.ten_khuyen_mai
            row.cells[2].innerText = data.so_luong
            row.cells[3].innerText = data.mo_ta
            row.cells[4].innerText = data.muc_giam + "%"
            row.cells[5].innerText = data.giam_toi_da
            row.cells[6].innerText = data.dieu_kien;
            row.cells[7].innerText = data.ngay_bat_dau;
            row.cells[8].innerText = data.ngay_ket_thuc;
            row.cells[9].innerText = data.trang_thai;
            row.cells[10].innerText = data.so_luong_sd
        })
        .catch(error => console.error("Looix",error))
}

// Ham Validate Form
function validateForm(input){
    let value = input.value.trim()
    let id = input.id
    if(value === "" && id!=="mo_ta" && id !== "searchInput"){
        showErr(input,"Không đucợ để trống trường này")
        return false
    }
    if(id === "so_luong" || id === "dieu_kien" || id === "giam_toi_da"){
        if(isNaN(value) || Number(value) <=0 ){
            showErr(input,"Vui lòng nhập số lon hon 0")
            return false
        }
    }
    if(id === "muc_giam"){
        if(Number(value.replace("%","").trim()) > 100 || Number(value.replace("%","").trim())<=0){
            showErr(input,"Vui long nhap so trong khoang tu 1->100")
            return false;
        }
    }
    if(id === "muc_giam"){
        if(isNaN(value.replace("%","").trim())){
            showErr(input,"Vui lòng nhập số")
            return false
        }
    }
    if (id === "ngay_bat_dau") {
        let today = new Date().toISOString().split("T")[0];
        if (value < today) {
            showErr(input, "Ngày bắt đầu phải lớn hơn hôm nay");
            return false;
        }
    }

    if (id === "ngay_ket_thuc") {
        let ngayBatDau = document.getElementById("ngay_bat_dau").value;
        if (value <= ngayBatDau) {
            showErr(input, "Ngày kết thúc phải lớn hơn ngày bắt đầu");
            return false;
        }
    }
    hideErr(input);
    return true;
}
// Hàm hiện lỗi
function showErr(input,message){
    hideErr(input)
    let error = document.createElement("span");
    error.className = "error-message text-danger d-block mt-1"
    error.innerText = message
    input.parentNode.appendChild(error)
    input.classList.add("is-invalid")
    input.insertAdjacentElement("afterend", error);
}
// Hàm ẩn lỗi
function hideErr(input){
    input.classList.remove("is-invalid");
    let errorLine = input.parentNode.querySelector(".error-message");
    if (errorLine) errorLine.remove();
}
// Hàm check lại khi nhấn save
function checkThenSave(){
    let isValid = true

    document.querySelectorAll(".form-control").forEach(input =>{
        if(!validateForm(input)){
            isValid = false
        }
    })
    if(!isValid){
        alert("Vui lòng nhập đúng các trường")
        return;
    }
    let mode = document.getElementById("save").getAttribute("data-mode")
    if(mode === "update"){
        let id = document.getElementById("km_id").value
        capnhatKm(id)
    }else {
        themKm()
    }
}
// gán sự kiện click cho nút save
document.getElementById("save").addEventListener("click", checkThenSave);
// gán sự kiện cho các input
document.querySelectorAll(".form-control").forEach(input => {
    input.addEventListener("blur",function (){
        validateForm(this)
    })
    input.addEventListener("input",function (){
        validateForm(this)
    })

})
// Đóng Modal sẽ reset các error-message
document.getElementById("kmModal").addEventListener("hidden.bs.modal", function () {
    document.querySelectorAll(".error-message").forEach(e => e.innerText = ""); // Xóa nội dung lỗi nhưng không xóa thẻ
    document.querySelectorAll(".is-invalid").forEach(e => e.classList.remove("is-invalid")); // Xóa class nhưng giữ nguyên input
});
// Khi không thao tác vào ô input nếu đúng sẽ thêm %
document.getElementById("muc_giam").addEventListener("blur",function (){
    if (validateForm(this)){
        this.value += "%"
    }
})
// khi focus sẽ reset ô input
document.getElementById("muc_giam").addEventListener("focus",function (){
    this.value = ""

})
// thêm id cho input search
$(document).ready(function () {
    var table = $('#kmTable').DataTable();

    $('.dataTables_filter input').attr('id', 'searchInput');
});

