var app = angular.module('ThongKeApp', []);

app.controller('ThongKeController', function ($scope, $http) {
    $http.get("/api/thong-ke/tong-quat").then(resp => {
        const data = resp.data;

        $scope.monthlySales = data.doanhSoThang;
        $scope.todaySales = data.doanhSoHomNay;
        $scope.monthlySoldProducts = data.tongSanPhamDaBan;
        $scope.lowStockProducts = data.sanPhamSapHet;
        $scope.bestSellingProducts = data.sanPhamBanChay;

        // Biểu đồ bán hàng theo ngày
        let chartData = data.bieuDoHangNgay;
        let labels = chartData.map(e => e.date);
        let hoaDonData = chartData.map(e => e.hoaDonCount);
        let sanPhamData = chartData.map(e => e.sanPhamCount);

        new Chart(document.getElementById("thongKeChart"), {
            type: "line",
            data: {
                labels: labels,
                datasets: [
                    {
                        label: "Số đơn hàng",
                        data: hoaDonData,
                        borderWidth: 2,
                        borderColor: "blue",
                        fill: false,
                        tension: 0.1
                    },
                    {
                        label: "Sản phẩm bán ra",
                        data: sanPhamData,
                        borderWidth: 2,
                        borderColor: "orange",
                        fill: false,
                        tension: 0.1
                    }
                ]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        suggestedMax: 100

                    }
                },
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }

        });

        // Biểu đồ trạng thái đơn hàng
        $http.get("/api/thong-ke/order-status").then(resp => {
            let statusData = resp.data.trangThaiDonHang;

            console.log("📊 Dữ liệu trạng thái đơn hàng:", statusData);

            new Chart(document.getElementById("orderStatusChart"), {
                type: "doughnut",
                data: {
                    labels: statusData.labels,
                    datasets: [{
                        data: statusData.data,
                        backgroundColor: [
                            '#FACC15', // Chờ xác nhận - vàng
                            '#FFD700', // Chờ giao hàng - vàng đậm
                            '#3B82F6', // Đang giao hàng - xanh dương
                            '#10B981', // Giao hàng thành công - xanh lá
                            '#8B5CF6', // Hoàn thành - tím
                            '#EF4444'  // Đã hủy - đỏ
                        ],
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    let label = context.label || '';
                                    let value = context.parsed;
                                    return `${label}: ${value} đơn`;
                                }
                            }
                        },
                        legend: {
                            position: 'bottom'
                        }
                    }

                }
            });

        });
    });
});

// Filter tiền tệ
app.filter('vndCurrency', function () {
    return function (input) {
        if (!input) return "0 đ";
        return Number(input).toLocaleString('vi-VN') + " đ";
    };
});
