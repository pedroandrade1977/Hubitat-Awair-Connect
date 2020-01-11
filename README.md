# Hubitat-Awair-Connect
App and Driver to connect Hubitat to Awair air quality sensors

In order to use this connector you need to register for a Developer Console access token at https://developer.getawair.com/:
1) Using your Awair credentials (used for the mobile application), request access to the Developer Console
2) When approved (can take a couple days), log into the Developer Console and retrieve the access token. You will use this to setup access through Hubitat

To use:
1) Create app by copying and pasting app code into Hubitat
2) Create driver by copying and pasting driver code into Hubitat
3) Add User App "Awair (Connect)"
4) When requested, enter your access token
5) Your Awair devices should then be retrieved and you can select which ones to install in Hubitat
6) Before completing, you can indicate whether you want to retrieve Temperature information in Fahrenheit (default is Celsius)
7) You can also indicate whether you want to store data with 0, 1 or 2 decimal places. I use this in order to display correctly in Dashboard tiles

Additional settings:
- in App code, row 26, you can set debug level: 1 is trace level (everything), 5 is error only
- in App code, row 240, you can set polling refresh frequency. I usually comment this out, as I refresh based on a rule in RM


That's it. Enjoy and feel free to let me know of any issues.
