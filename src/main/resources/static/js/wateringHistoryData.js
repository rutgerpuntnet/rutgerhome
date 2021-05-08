var historyTable = undefined;

$(document).ready(function() {
    $("#history-tab").click(function() {
        loadChartData();
        if (historyTable === undefined) {
            loadTableData();
        }
    });
});

function loadChartData() {
    console.log("Get chart data");

    $.ajax({
        url: "/watering/graph/7",
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
                            label: "Sproeiduur minuten boven",
                            data: response.durationUpper,
                            backgroundColor: [
                                'rgba(255, 159, 64, .2)',
                            ],
                            borderColor: [
                                'rgba(255, 159, 64, .7)',
                            ],
                            borderWidth: 2
                        },
                        {
                            label: "Sproeiduur minuten onder",
                            data: response.durationLower,
                            backgroundColor: [
                                'rgba(64, 255, 159, .2)',
                            ],
                            borderColor: [
                                'rgba(64, 255, 159, .7)',
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


function loadTableData() {
    console.log("Get table data");

    $.ajax({
        url: "/watering/table/365",
        contentType: "application/json",
        dataType: "json",
        type: 'GET',
        success: function (response) {
            console.log("Got table response from backend (latest 365)");
            historyTable = $('#historyTable').DataTable({
                data: response.rows,
                columns: response.columns,
                fixedHeader: {
                    header: true,
                    footer: true
                }
            });
            $("#historyTable_wrapper").css("width","100%");

            response.columns.forEach(function(item, index) {
                $('#historyTableToggle').append('&nbsp;-&nbsp;<a class=\"toggle-vis\" style=\"color: black;\" data-column=\"'+index+'\">'+item.title+'</a>');
            });


            $('a.toggle-vis').on( 'click', function (e) {
                e.preventDefault();

                // Get the column API object
                var column = historyTable.column( $(this).attr('data-column') );

                // Toggle the visibility
                if (column.visible()) {
                    column.visible(false);
                    $(this).css('color', 'silver');
                } else {
                    column.visible(true);
                    $(this).css('color', 'black');
                }
            } );

        }
    });
}
