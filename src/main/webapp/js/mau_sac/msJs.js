function openModal(button){
    let mode = button.value;
    document.getElementById("modalTitle").innerText = mode === "update" ? "Cập nhật màu sắc" : "Thêm màu sắc"
    let saveButton = document.querySelector(".modal-footer .btn-primary")
    if(mode === "update"){
        let id = button.getAttribute("data-id")

        fetch(`/mau-sac/detail/${id}`,{
            method:"GET"
        })
            .then(response => response.json())

            .then(data =>{
                console.log(data)
                document.getElementById("ten_mau_sac").value = data.ten_mau_sac;
                document.getElementById("mo_ta").value = data.mo_ta;

            })
            .catch(error => console.error("Lỗi",error))
        saveButton.setAttribute("onclick", `updateMs(${id})`);
    }else{
        document.getElementById("msForm").reset();
        saveButton.setAttribute("onclick","themMs()")
    }

}


function themMs(){
    let tenMau = document.getElementById("ten_mau_sac").value.trim();
    let moTa = document.getElementById("mo_ta").value.trim();

    // Lấy danh sách tên màu đã tồn tại
    let existingRows = document.querySelectorAll("#msTable tbody tr");
    for (let row of existingRows) {
        let existingName = row.cells[0].innerText.trim().toLowerCase();
        if (existingName === tenMau.toLowerCase()) {
            alert("Tên màu đã tồn tại!");
            return; // dừng thêm mới
        }
    }

    let msJson = {
        ten_mau_sac: tenMau,
        mo_ta: moTa
    };

    fetch("/mau-sac/add", {
        method : "POST",
        headers: {
            "Content-Type" : "application/json"
        },
        body: JSON.stringify(msJson)
    })
        .then(response => response.json())
        .then(data =>{
            alert("Thêm thành công");
            let table = document.getElementById("msTable").getElementsByTagName("tbody")[0];
            let newRow = table.insertRow();
            newRow.innerHTML = `
            <td>${data.ten_mau_sac}</td>
            <td>${data.mo_ta}</td>
            <td>
                <button type="button" data-id="${data.id}" value="update" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#msModal" onclick="openModal(this)">
                    <i class="fa fa-pencil-alt"></i>
                </button>
            </td>
        `;
            document.getElementById("msForm").reset();
        })
        .catch(error => console.error("Lỗi", error));
}
//
function updateMs(id){
    let tenMau = document.getElementById("ten_mau_sac").value.trim();
    let moTa = document.getElementById("mo_ta").value.trim();

    let existingRows = document.querySelectorAll("#msTable tbody tr");
    for (let row of existingRows) {
        let btn = row.querySelector("button");
        if (btn.getAttribute("data-id") !== id.toString()) {
            let existingName = row.cells[0].innerText.trim().toLowerCase();
            if (existingName === tenMau.toLowerCase()) {
                alert("Tên màu đã tồn tại!");
                return;
            }
        }
    }

    let msJson = {
        ten_mau_sac: tenMau,
        mo_ta: moTa
    };

    fetch(`/mau-sac/update/${id}`, {
        method:"PUT",
        headers: {
            "Content-Type":"application/json"
        },
        body: JSON.stringify(msJson)
    })
        .then(response => response.json())
        .then(data =>{
            alert("Cập nhật thành công");
            let row = document.querySelector(`button[data-id="${id}"]`).closest("tr");
            row.cells[0].innerText = data.ten_mau_sac;
            row.cells[1].innerText = data.mo_ta;
        })
        .catch(error => console.error("Lỗi", error));
}


//
function sortTable(order) {
    let table = document.getElementById("msTable").getElementsByTagName("tbody")[0];
    let rows = Array.from(table.rows);

    rows.sort(function(a, b) {
        let nameA = a.cells[0].innerText.toLowerCase();
        let nameB = b.cells[0].innerText.toLowerCase();

        if (order === "az") {
            return nameA.localeCompare(nameB);
        } else if (order === "za") {
            return nameB.localeCompare(nameA);
        }
        return 0;
    });

    // Clear existing rows and re-append sorted
    table.innerHTML = "";
    rows.forEach(row => table.appendChild(row));
}

