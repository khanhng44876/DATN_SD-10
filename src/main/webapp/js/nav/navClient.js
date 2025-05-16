window.stompClient1 = null;
let notiList = [];

function handleClick(event,a){
    event.preventDefault();
    const id = Number(a.getAttribute("data-id"));
    const link = a.getAttribute("data-link");
    console.log(link);
    console.log(id)
    fetch(`/read-noti/${id}`,{
        method:"PUT"
    })
        .catch(err=>onsole.error(err))
        .finally(()=>{
            window.location.href = link;
        })
}

function connectSocketNoti() {
    const socket = new SockJS("/ws");
    stompClient1 = Stomp.over(socket);

    stompClient1.connect({}, function (frame) {
        console.log("Connected to WebSocket: " + frame);

        // Lắng nghe thông báo từ server gửi riêng cho user
        stompClient1.subscribe("/user/topic/notification", function (message) {
            console.log(message.body)
            notiList = JSON.parse(message.body)
            console.log("Nhận trạng thái mới từ Admin:", notiList);
            if (notiList !== null) {
                let ul = document.getElementById("notiList");
                ul.innerHTML = '';
                notiList.forEach(n => {
                    const li = document.createElement('li');
                    li.classList.add('noti-item', n.read ? 'read' : 'unread');
                    li.style.cursor = 'pointer';
                    li.style.padding = '.5rem 1rem';
                    li.style.whiteSpace = 'normal';
                    li.style.wordBreak = 'break-word';

                    const a = document.createElement('a');
                    a.classList.add('dropdown-item', 'd-block');
                    a.setAttribute('data-id', n.id);
                    if (n.link) a.setAttribute('data-link', n.link);
                    a.href = '#';
                    a.style.whiteSpace = 'normal';
                    a.style.wordBreak = 'break-word';
                    a.onclick = e => handleClick(e, a);
                    a.textContent = n.noi_dung;
                    li.appendChild(a);

                    const small = document.createElement('small');
                    small.classList.add('text-muted');
                    small.textContent = 'Vào lúc: ' +
                        formatDate(n.ngayTao, 'HH:mm dd/MM/yyyy');
                    li.appendChild(small);

                    const hr = document.createElement('hr');
                    hr.classList.add('dropdown-divider');
                    li.appendChild(hr);

                    ul.appendChild(li);
                });
                document.getElementById('notiCount').textContent = notiList.length;
            }
        });
    });
}

function formatDate(timestamp){
    const d = new Date(timestamp);
    const hh = String(d.getHours()).padStart(2,"0");
    const mm = String(d.getMinutes()).padStart(2,"0");
    const dd = String(d.getDate()).padStart(2,"0");
    const mon = String(d.getMonth()+1).padStart(2,"0");
    const yyyy = String(d.getFullYear()).padStart(2,"0");
    return `${hh}:${mm} ${dd}/${mon}/${yyyy}`;
}

// function notiClick(link){
//     res = fetch(link,{
//         method : "GET"
//     })
// }

connectSocketNoti();