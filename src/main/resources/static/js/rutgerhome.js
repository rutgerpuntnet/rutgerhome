$( document ).ready(function() {
    console.log( "Document ready!" );
    loadLatestData();
    loadStaticData();

    setNewNextFactorSliderListener();
    setStaticFactorSliderListener();
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
            $('#latestFactor').text(response.latestJob.usedFactorString);

            if(response.latestJob.minutesLeft > 0) {
                $('#stopCurrentJob').show();
                $('#manualJobAlertActive').show();
                $('#sendManual').prop("disabled", true);
                $('#minutesManual').prop("disabled", true);
            } else {
                $('#stopCurrentJob').hide();
                $('#manualJobAlertActive').hide();
                $('#sendManual').prop("disabled", false);
                $('#minutesManual').prop("disabled", false);
            }

            if (response.automaticJobUpcoming) {
                $('#manualJobAlertSoon').show();
            } else {
                $('#manualJobAlertSoon').hide();
            }

            $('#nextJob').text(response.nextRunDay);
            if(response.nextEnforceFactor !== null) {
                $('#newNextFactor').val(response.nextEnforceFactor);
                $('#newNextFactorValueSpan').html(response.nextEnforceFactor);

                $('#nextFactor').text(response.nextEnforceFactor);
                $('#nextFactorAvailable').show();
            }

        }
    });
}

function setNewNextFactorSliderListener() {
    console.log("Set newNextFactorValueSpan slider listener");

    const $valueSpan = $('#newNextFactorValueSpan');
    const $value = $('#newNextFactor');
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
        data["factor"] = $('#newNextFactor').val()
        data["days"] = 1; // TODO implement $('#factorDays').val()

        console.log("Data",data)
        $.ajax({
            url: "/watering/enforceFactor",
            contentType: "application/json",
            data: JSON.stringify(data),
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
    $('#sendManual').click(function () {
        console.log("sendManual click")

        $.ajax({
            url: "/watering/manualAction/" + $('#minutesManual').val(),
            type: 'POST',
            success: function (response) {
                console.log("Done send manual job", response);
                loadLatestData();
            }
        });
    });
});


// kill switch
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


function setStaticFactorSliderListener() {
    console.log("Set staticFactor slider listener");

    const $valueSpan = $('#staticFactorValueSpan');
    const $value = $('#staticFactor');
    $valueSpan.html($value.val());
    $value.on('input change', () => {
        $valueSpan.html($value.val());
    });
}


function loadStaticData() {
    console.log("Get static data");

    $.ajax({
        url: "/watering/staticdata",
        contentType: "application/json",
        dataType: "json",
        type: 'GET',
        success: function (response) {
            console.log("Got response from backend (static data):", response);

            $('#staticDataModifiedSince').text(response.modifiedSince);
            $('#staticFactor').val(response.factor);
            $('#staticFactorValueSpan').html(response.factor);
            $('#minutesPerMm').val(response.minutesPerMm);
            $('#defaultMinutes').val(response.defaultMinutes);
            $('#dailyLimitMinutes').val(response.dailyLimitMinutes);
            $('#maxDurationMinutes').val(response.maxDurationMinutes);
            $('#initialMm').val(response.initialMm);
            $('#intervalMinutes').val(response.intervalMinutes);

        }
    });
}


// send static data factor
$(function () {
    $('#sendStaticData').click(function () {
        console.log("send static data click")

        var data = {}  // object to hold the user input data
        data["factor"] = $('#staticFactor').val()
        data["minutesPerMm"] = $('#minutesPerMm').val()
        data["defaultMinutes"] = $('#defaultMinutes').val()
        data["dailyLimitMinutes"] = $('#dailyLimitMinutes').val()
        data["maxDurationMinutes"] = $('#maxDurationMinutes').val()
        data["initialMm"] = $('#initialMm').val()
        data["intervalMinutes"] = $('#intervalMinutes').val()

        console.log("Data",data)
        $.ajax({
            url: "/watering/staticdata",
            contentType: "application/json",
            data: JSON.stringify(data),
            type: 'POST',
            success: function (response) {
                console.log("Done set static data", response);
                loadStaticData();
            }
        });
    });
});


