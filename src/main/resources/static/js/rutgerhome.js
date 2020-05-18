$( document ).ready(function() {
    console.log( "Document ready!" );
    loadLatestData();

    setSliderListener();
});

function loadLatestData() {
    console.log("Get latest");

    $.ajax({
        url: "/watering/status",
        contentType: "application/json",
        dataType: "json",
        type: 'GET',
        success: function (response) {
            console.log("Got response from backend (latest):", response);

            $('#latestDay').text(response.latestJob.day);
            $('#latestMakkink').text(response.latestJob.makkinkIndexString);
            $('#latestPrecip').text(response.latestJob.precipitationString);
            $('#latestPrecipDuration').text(response.latestJob.precipitationDurationString);
            $('#latestMaxTemp').text(response.latestJob.maxTemperatureString);
            $('#latestMeanTemp').text(response.latestJob.meanTemperatureString);
            $('#latestNumberOfMinutes').text(response.latestJob.numberOfMinutes);
            $('#latestMinutesLeft').text(response.latestJob.minutesLeft);
            $('#latestFactor').text(response.latestJob.staticWateringData.factorString);

            if(response.latestJob.minutesLeft > 0) {
                $('#stopCurrentJob').show();
            } else {
                $('#stopCurrentJob').hide();
            }
            $('#nextJob').text(response.nextRunDay);
            if(response.nextEnforceFactor !== null) {
                $('#nextFactor').text(response.nextEnforceFactor);
                $('#nextFactorAvailable').show();
            }

        }
    });
}

function setSliderListener() {
    console.log("Set slider listener");
    const $valueSpan = $('.valueSpan2');
    const $value = $('#customRange11');
    $valueSpan.html($value.val());
    $value.on('input change', () => {
        $valueSpan.html($value.val());
    });
}


// Post enforce factor
$(function () {
    $('#sendRange').click(function () {
        console.log("sendRange click")

        var data = {}  // object to hold the user input data
        data["factor"] = $('#customRange11').val()
        data["days"] = $('#factorDays').val()

        console.log("Data",data)
        $.ajax({
            url: "/watering/enforceFactor",
            contentType: "application/json",
            data: JSON.stringify(data),
          //  dataType: "json",
            type: 'POST',
            success: function (response) {
                console.log("Done enforce", response);
                loadLatestData();
            }
        });
    });
});


// Post enforce factor
$(function () {
    $('#stopCurrentJob').click(function () {
        console.log("stopCurrentJob click")

        $.ajax({
            url: "/watering/killCurrentJob",
            type: 'POST',
            success: function (response) {
                console.log("Done kill", response);
                loadLatestData();
            }
        });
    });
});

