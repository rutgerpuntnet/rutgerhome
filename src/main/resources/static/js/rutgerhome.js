$( document ).ready(function() {
    console.log( "Document ready!" );
    loadLatestData();
    loadStaticDataUpper();
    loadStaticDataLower();

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
            $('#latestNumberOfMinutesUpper').text(response.latestJob.numberOfMinutesUpper);
            $('#latestNumberOfMinutesLower').text(response.latestJob.numberOfMinutesLower);
            $('#latestMinutesLeftUpper').text(response.latestJob.minutesLeftUpper);
            $('#latestMinutesLeftLower').text(response.latestJob.minutesLeftLower);
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
            url: "/watering/manualAction/" + $('#minutesManualUpper').val() + "/" + $('#minutesManualLower').val(),
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


function setStaticFactorSliderListenerUpper() {
    console.log("Set upper staticFactor slider listener");

    const $valueSpan = $('#staticFactorValueSpanUpper');
    const $value = $('#staticFactorUpper');
    $valueSpan.html($value.val());
    $value.on('input change', () => {
        $valueSpan.html($value.val());
    });
}


function loadStaticDataUpper() {
    console.log("Get upper static data");

    $.ajax({
        url: "/watering/staticdata/UPPER",
        contentType: "application/json",
        dataType: "json",
        type: 'GET',
        success: function (response) {
            console.log("Got response from backend (upper static data):", response);

            $('#staticDataModifiedSinceUpper').text(response.modifiedSince);
            $('#staticFactorUpper').val(response.factor);
            $('#staticFactorValueSpanUpper').html(response.factor);
            $('#minutesPerMmUpper').val(response.minutesPerMm);
            $('#defaultMinutesUpper').val(response.defaultMinutes);
            $('#dailyLimitMinutesUpper').val(response.dailyLimitMinutes);
            $('#maxDurationMinutesUpper').val(response.maxDurationMinutes);
            $('#initialMmUpper').val(response.initialMm);
            $('#intervalMinutesUpper').val(response.intervalMinutes);

        }
    });
}


// send upper static data factor
$(function () {
    $('#sendStaticDataUpper').click(function () {
        console.log("send upper static data click")

        var data = {}  // object to hold the user input data
        data["factor"] = $('#staticFactorUpper').val()
        data["minutesPerMm"] = $('#minutesPerMmUpper').val()
        data["defaultMinutes"] = $('#defaultMinutesUpper').val()
        data["dailyLimitMinutes"] = $('#dailyLimitMinutesUpper').val()
        data["maxDurationMinutes"] = $('#maxDurationMinutesUpper').val()
        data["initialMm"] = $('#initialMmUpper').val()
        data["intervalMinutes"] = $('#intervalMinutesUpper').val()

        console.log("Upper data",data)
        $.ajax({
            url: "/watering/staticdata/UPPER",
            contentType: "application/json",
            data: JSON.stringify(data),
            type: 'POST',
            success: function (response) {
                console.log("Done set upper static data", response);
                loadStaticDataUpper();
            }
        });
    });
});



function setStaticFactorSliderListenerLower() {
    console.log("Set lower staticFactor slider listener");

    const $valueSpan = $('#staticFactorValueSpanLower');
    const $value = $('#staticFactorLower');
    $valueSpan.html($value.val());
    $value.on('input change', () => {
        $valueSpan.html($value.val());
    });
}

function loadStaticDataLower() {
    console.log("Get lower static data");

    $.ajax({
        url: "/watering/staticdata/LOWER",
        contentType: "application/json",
        dataType: "json",
        type: 'GET',
        success: function (response) {
            console.log("Got response from backend (lower static data):", response);

            $('#staticDataModifiedSinceLower').text(response.modifiedSince);
            $('#staticFactorLower').val(response.factor);
            $('#staticFactorValueSpanLower').html(response.factor);
            $('#minutesPerMmLower').val(response.minutesPerMm);
            $('#defaultMinutesLower').val(response.defaultMinutes);
            $('#dailyLimitMinutesLower').val(response.dailyLimitMinutes);
            $('#maxDurationMinutesLower').val(response.maxDurationMinutes);
            $('#initialMmLower').val(response.initialMm);
            $('#intervalMinutesLower').val(response.intervalMinutes);

        }
    });
}

// send lower static data factor
$(function () {
    $('#sendStaticDataLower').click(function () {
        console.log("send lower static data click")

        var data = {}  // object to hold the user input data
        data["factor"] = $('#staticFactorLower').val()
        data["minutesPerMm"] = $('#minutesPerMmLower').val()
        data["defaultMinutes"] = $('#defaultMinutesLower').val()
        data["dailyLimitMinutes"] = $('#dailyLimitMinutesLower').val()
        data["maxDurationMinutes"] = $('#maxDurationMinutesLower').val()
        data["initialMm"] = $('#initialMmLower').val()
        data["intervalMinutes"] = $('#intervalMinutesLower').val()

        console.log("Lower data",data)
        $.ajax({
            url: "/watering/staticdata/LOWER",
            contentType: "application/json",
            data: JSON.stringify(data),
            type: 'POST',
            success: function (response) {
                console.log("Done set lower static data", response);
                loadStaticDataLower();
            }
        });
    });
});


