# rutgerhome
Personal project for home automation build in Spring boot.

The initial release only contains a complete (and more robust) rebuild of the wateringsystem (https://github.com/rutgerpuntnet/wateringsystem). This wateringsystem is a scheduled service that triggers the automatic garden watering system (arduino based) with a certain amount of minutes of garden watering. This is based in information retrieved from the Dutch national weather institute (which offers some sort of api). This weatherstation data including evaporation is available the next morning.
Based on the evaporation the number of minutes to enable the sprinkler is determined.
