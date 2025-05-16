document.addEventListener("DOMContentLoaded", function () {
    let dropdowns = document.querySelectorAll(".nav-link[data-toggle]");

    dropdowns.forEach((dropdown) => {
        dropdown.addEventListener("click", function (event) {
            event.preventDefault();
            let targetId = this.getAttribute("data-toggle");
            let submenu = document.getElementById(targetId);

            if (submenu) {
                submenu.classList.toggle("show");
            }
        });
    });
});

