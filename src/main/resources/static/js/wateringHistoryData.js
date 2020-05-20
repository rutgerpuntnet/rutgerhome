$(document).ready(function() {
    $("#history-tab").click(function() {
        loadHistoryChartData();
    });
});

function loadHistoryChartData() {
    console.log("Get chart data");

    $.ajax({
        url: "/watering/history/7",
        contentType: "application/json",
        dataType: "json",
        type: 'GET',
        success: function (response) {
            console.log("Got chart response from backend (latest):", response);

            let historyLength = response.labels.length;
            console.log("historyLength:", historyLength);

            $('#historyChartDays').text(historyLength);


            var ctxL = document.getElementById("wateringHistoryChart").getContext('2d');
            var myLineChart = new Chart(ctxL, {
                type: 'line',
                data: {
                    labels: response.labels,
                    datasets: [{
                        label: "Neerslag mm.",
                        data: response.precipitation,
                        backgroundColor: [
                            'rgba(105, 0, 132, .2)',
                        ],
                        borderColor: [
                            'rgba(200, 99, 132, .7)',
                        ],
                        borderWidth: 2
                    },
                        {
                            label: "Verdamping mm.",
                            data: response.makkink,
                            backgroundColor: [
                                'rgba(0, 137, 132, .2)',
                            ],
                            borderColor: [
                                'rgba(0, 10, 130, .7)',
                            ],
                            borderWidth: 2
                        },
                        {
                            label: "Sproeiduur minuten",
                            data: response.duration,
                            backgroundColor: [
                                'rgba(255, 159, 64, .2)',
                            ],
                            borderColor: [
                                'rgba(255, 159, 64, .7)',
                            ],
                            borderWidth: 2
                        }
                    ]
                },
                options: {
                    responsive: true
                }
            });


        }
    });
}

//line
