/*  Copyright 2020 Pedro Andrade
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	Awair 2 App
 *
 * Author: Pedro Andrade
 *
 *	Updates:
 *	Date: 2020-01-31	v1.0 Initial release
 */

import java.text.DecimalFormat
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

private apiUrl() 			{ "https://developer-apis.awair.is" }
private getVendorName() 	{ "Awair" }
private getLogLevel()       { 5 }

 // Automatically generated. Make future change here.
definition(
    name: "Awair (Connect)",
    namespace: "HE_pfta",
    author: "Pedro Andrade",
    description: "Awair Integration",
    category: "Sensors",
	iconUrl:   "",
	iconX2Url: "",
	oauth: false,
    singleInstance: true
) 

{
	appSetting "serverUrl"
    appSetting "boolFahrenheit"
    appSetting "decimals"
}

preferences {
	page(name: "startPage", title: "Awair Integration", content: "startPage", install: false)
	page(name: "Credentials", title: "Awair Token", content: "authPage", install: false)
	page(name: "mainPage", title: "Awair Integration", content: "mainPage")
	page(name: "completePage", title: "${getVendorName()} is now connected to Hubitat!", content: "completePage")
	page(name: "listDevices", title: "Awair Devices", content: "listDevices", install: false)
    page(name: "advancedOptions", title: "Awair (Connect) Advanced Options", content: "advancedOptions", install: false)
	page(name: "errorPage", title: "Error retrieving devices ", content: "errorPage", install: false)
}

def startPage() {
    if (state.accessToken) {
        addToLog("startPage(): already have token: ${state.accessToken}",2)
        return mainPage()
    }
    else {
        addToLog("startPage(): no token, go to auth page",2)
        return authPage()
    }
}

def authPage() {
	addToLog("authPage()",1)
    def description = null
	description = "Tap to enter Token"
	return dynamicPage(name: "Credentials", title: "Authorize Connection", nextPage:mainPage, uninstall: false , install:false) {
	   section("Token") {
				input "token", "text", title: "Your Awair Token", required: true
			}
	}
}

def mainPage() {
    addToLog("mainPage(): token entered was ${settings.token}",1)
    state.accessToken=settings.token
    if (state.accessToken) {
        addToLog("mainPage(): token stored, go to select devices",1)
        return listDevices()
       } else {
        addToLog("mainPage(): no token stored",2)
        return errorPage()
       }
}

def errorPage(){
    addToLog("errorPage(): entered",1)
    return dynamicPage(name: "errorPage", title: "Error retrieving devices", install:false, uninstall:true, nextPage: Credentials) {
	    section("") {
			paragraph "Could not retrieve devices. Check your token and try again."
        }
    }
}

def listDevices() {

    try {
        addToLog("listDevices(): preparing to retrieve devices",1)
        def options=getDeviceList()
        addToLog("listDevices(): ${options}",2)
        dynamicPage(name: "listDevices", title: "Choose devices", install:false, nextPage: advancedOptions) {
	    	section("Devices") {
		    	input "devices", "enum", title: "Select Device(s)", required: false, multiple: true, options: options, submitOnChange: true
		    }
	    }
    } catch (groovyx.net.http.HttpResponseException e) {
        addToLog("listDevices(): could not retrieve devices: ${e}",5)
        return errorPage()
    } catch (Exception e) {
        addToLog("listDevices(): other error: ${e}",5)
        return errorPage()
    }
}

def advancedOptions() {
	addToLog("advancedOptions(): entered",1)
	dynamicPage(name: "advancedOptions", title: "Select Advanced Options", install:true) {
    	section("Select TRUE for Fahrenheit") {
      	input("boolFahrenheit", "bool", title: "Fahrenheit?", required: true, default: false)
      	input("decimals", "enum", title: "Convert measurements to how many decimals", options: [0,1,2], required: true)
		}
        section(){
    		if (getHubID() == null){
        		input(
            		name		: "myHub"
            		,type		: "hub"
            		,title		: "Select your hub"
            		,multiple		: false
            		,required		: true
            		,submitOnChange	: true
        		)
     		} else {
        		paragraph("Tap done to finish the initial installation.")
     		}
		}
    }
}

def getDeviceList() {
  addToLog("getDeviceList(): preparing to retrieve devices",1)
  def AwairDevices = getDevicesCommand()
  addToLog("getDeviceList(): ${AwairDevices}",2)
  return AwairDevices.sort()
}


def installed() {
    addToLog("installed(): entered", 1)
	initialize()
}

def updated() {
    addToLog("updated(): entered", 1)
  	unsubscribe()
	unschedule()
	initialize()
}

def uninstalled() {
  addToLog("uninstalled(): Uninstalling Awair (Connect)",2)
  removeChildDevices(getChildDevices())
  addToLog("uninstalled(): Awair (Connect) uninstalled",3)
}

def initialize() {
    addToLog("initialize(): initializing with settings: ${settings}",2)
	
/*    // Pull the latest device info into state
	
    try{
        getDeviceList()
    } catch (Exception e) {
        addToLog("initialize(): error retrieving devices: ${e}",5)
        return errorPage()
    }*/

    // get installed devices
    def children = getChildDevices()

        
        // first remove devices which are not in the settings anymore
        // section is commented to prevent accidental deletion of installed device
/*    if (children) {
        children.each { device ->
            addToLog("initialize(): inspecting installed device: ${device.inspect()}",2)
       		def item = device.deviceNetworkId.tokenize('|')
            def deviceType = item[0]
            def deviceId = item[1]
            def deviceName = item[2]
            def deviceMacAddress = item[3]
            def existingDevices = settings.devices.find{ d -> d.contains(deviceId + "|" + deviceType) } // check if device selected
            addToLog("initialize(): existing devices inspected: ${existingDevices.inspect()}",2)

            if(!existingDevices) { // if not selected
                addToLog("initialize(): device not selected: ${deviceName}",3)
                try {
                    addToLog("initialize(): removing device: ${deviceName}",3)
                    deleteChildDevice(device.deviceNetworkId)
 			    } catch (Exception e) {
                addToLog("initialize(): error deleting device: ${e}",5)
			    }
    		}
        }
    }*/

         // then add new devices   
    if(settings.devices) {
    	settings.devices.each { device ->
            addToLog("initialize(): inspecting selected device: ${device.inspect()}",2)
       		def item = device.tokenize('|')
            def deviceType = item[0]
            def deviceId = item[1]
            def deviceName = item[2]
            def deviceMacAddress = item[3]
            def existingDevices = children.find{ d -> d.deviceNetworkId.contains(deviceId + "|" + deviceType) } // check if device already installed
            addToLog("initialize(): existing device: ${existingDevices.inspect()}",2)

            if(!existingDevices) { // if not installed
                addToLog("initialize(): device does not exist: ${deviceName}",3)
                try {
                    addToLog("initialize(): creating device: ${deviceName}",3)
                    createChildDevice("Awair Device", deviceId + "|" + deviceType + "|" + deviceName + "|" + deviceMacAddress, "${deviceName}", deviceName) // install device
 			    } catch (Exception e) {
                addToLog("initialize(): error creating device: ${e}",5)
			    }
    		}
		}
    }
    

	// Do the initial poll
    getInititialDeviceInfo()
	
	// Schedule it to run every 5 minutes
	runEvery5Minutes("poll") /* I usually have it commented because i run refresh with rules */
}

def getInititialDeviceInfo(){
	addToLog("getInititialDeviceInfo(): getting inicial readings for all devices",2)
	
	def children = getChildDevices() // get installed devices
	if(settings.devices) {
        settings.devices.each { device -> // for each selected device
          addToLog("getInititialDeviceInfo(): inspecting device: ${device.inspect()}",2)
          def item = device.tokenize('|')
          def deviceType = item[0]
          def deviceId = item[1]
          def deviceName = item[2]
          def existingDevices = children.find{ d -> d.deviceNetworkId.contains(deviceId + "|" + deviceType) }
      if(existingDevices) { // if it was installed
          addToLog("getInititialDeviceInfo(): initial readings will be retrieved for: ${existingDevices}",2)
          existingDevices.getLatestAirData()
      }
	}
  }
}

def getHubID(){
    addToLog("getHubID(): retrieving hub id",1)
	def hubID
    if (myHub){
        hubID = myHub.id
    } else {
        log.debug("hub type is ${location.hubs[0].type}")
        def hubs = location.hubs.findAll{ it.type == "PHYSICAL" } 
        if (hubs.size() == 1){
            hubID = hubs[0].id
        }
    }
    addToLog("getHubID(): Returning Hub ID: ${hubID}",2)
    return hubID
}

def poll() {
	addToLog("poll(): in poll",1)
	
    def children = getChildDevices() // get list of devices to poll

    children.each { device ->
        addToLog("poll(): Polling device ${device.deviceNetworkId}",2)
        device.poll()
    }
}

        
def createChildDevice(deviceFile, dni, name, label) {
	addToLog("createChildDevice(): entered",1)
    try{
		def childDevice = addChildDevice("HE_pfta", deviceFile, dni, getHubID(), [name: name, label: label, completedSetup: true])
	} catch (e) {
        addToLog("createChildDevice(): error creating device ${name}: ${e}",5)
	}
}

private sendCommand(method,childDevice,args = []) {
    addToLog("sendCommand(): called with arguments ${args}",2)
    
    def methods = [
	'getDevices': [
        			uri: apiUrl(),
                    path: "/v1/users/self/devices",
                    requestContentType: "application/json",
                    headers: [Authorization: "Bearer ${state.accessToken}"],
                    timeout: 30
                    ],
    'getLatestAirData': [
        			uri: apiUrl(),
         			path: "/v1/users/self/devices/${args[1]}/${args[0]}/air-data/latest",
        			requestContentType: "application/json",
                    headers: [Authorization: "Bearer ${state.accessToken}"],
                    query: [fahrenheit: args[2]],
                    timeout: 30
                    ]
	]

	def request = methods.getAt(method)
  
    addToLog("sendCommand(): HTTP Request = ${request}",2)

    if (method == "getDevices"){
        httpGet(request) { resp ->
            parseDevicesResponse(resp)
        }
    }else if (method == "getLatestAirData"){
        httpGet(request) { resp ->
            parseAirDataResponse(childDevice,resp)
        }
    }else{
        httpGet(request)
    }
}

// Parse incoming device messages to generate events
private parseDevicesResponse(resp) {
    addToLog("parseDevicesResponse(): Response Status = ${resp.status}",2)
    addToLog("parseDevicesResponse(): Response Data = ${resp.data}",2)

    if(resp.status == 200) {
        def restDevices = resp.data.devices
        def AwairDevices = []
      restDevices.each { 
          awair -> AwairDevices << ["${awair.deviceType}|${awair.deviceId}|${awair.name}|${awair.macAddress}":"${awair.name}"] 
      }
        addToLog("parseDevicesResponse(): Devices found = ${AwairDevices}",2)
        return AwairDevices
    }

}

def parseAirDataResponse(childDevice, resp) {
    addToLog("parseAirDataResponse(): Response Status = ${resp.status}",2)
    addToLog("parseAirDataResponse(): Response Data = ${resp.data}",2)

    def multiplier=(10**settings.decimals.toInteger())?:10
    addToLog("parseAirDataResponse(): multiplier = ${multiplier} / ${settings.decimals}",2)
    
    if(resp.status == 200) {

        addToLog("parseAirDataResponse(): send event for awairScore = ${resp.data.data.score}",2)
        childDevice?.sendEvent(name:"awairScore",value: resp.data.data.score)

        resp.data.data.sensors.each {sensors ->
            sensors.each {sensor ->
                addToLog("parseAirDataResponse(): processing sensor = ${sensor}",2)
                if (sensor.comp=="temp") {
                    addToLog("parseAirDataResponse(): send event for temperature = ${sensor.value}",2)
                    childDevice?.sendEvent(name:"temperature",value: Math.round(sensor.value*multiplier)/multiplier)
                }
                else if (sensor.comp=="humid") {
                    addToLog("parseAirDataResponse(): send event for humidity = ${sensor.value}",2)
                    childDevice?.sendEvent(name:"humidity",value: Math.round(sensor.value*multiplier)/multiplier)
                }
                else if (sensor.comp=="co2") {
                    addToLog("parseAirDataResponse(): send event for carbonDioxide = ${sensor.value}",2)
                    childDevice?.sendEvent(name:"carbonDioxide",value: Math.round(sensor.value*multiplier)/multiplier)
                } else if (sensor.comp=="dust" || sensor.comp=="pm25") {
                    addToLog("parseAirDataResponse(): send event for ${sensor.comp} = ${sensor.value}",2)
                    childDevice?.sendEvent (name:"pm25", value: Math.round(sensor.value*multiplier)/multiplier)
                } else {
                    addToLog("parseAirDataResponse(): send event for ${sensor.comp} = ${sensor.value}",2)
                    childDevice?.sendEvent (name:sensor.comp, value: Math.round(sensor.value*multiplier)/multiplier)
                }
            }
        }

        addToLog("parseAirDataResponse(): processing indices",1)
        resp.data.data["indices"].each {awairIndices ->
            awairIndices.each {awairIndex ->
                addToLog("parseAirDataResponse(): processing index ${awairIndex}",2)
                if (awairIndex.comp=="dust") {
                    childDevice?.sendEvent (name:"pm25Index", value: awairIndex.value)
                } else {
                    childDevice?.sendEvent (name:awairIndex.comp + "Index", value: awairIndex.value)
                }
            }
        }
        
        
    }
}

def getDevicesCommand(){
	addToLog("getDevicesCommand(): entered",1)
	sendCommand("getDevices",null,[])
}

def getLatestAirDataCommand(childDevice){
    addToLog("getLatestAirDataCommand(): executing for device = ${childDevice}",2)
    def item = (childDevice.device.deviceNetworkId).tokenize('|')
    def deviceId = item[0]
    def deviceType = item[1]
    sendCommand("getLatestAirData",childDevice,[deviceId, deviceType, settings.boolFahrenheit?:false])
}

private removeChildDevices(delete) {
	try {
    	delete.each {
        	deleteChildDevice(it.deviceNetworkId)
            addToLog("removeChildDevices(): ccessfully Removed Child Device: ${it.displayName} (${it.deviceNetworkId}",3)
    		}
   		}
    catch (e) { addToLog("removeChildDevices(): There was an error (${e}) when trying to delete the child device",5) }
}

def addToLog(message, level) {
    message = "Awair (Connect) -> ${message ?: 'empty'}"
        
    if (level >= getLogLevel()) {
        switch (level) {
            case 1:
                log.trace "${message}"
                break
            case 2:
                log.debug "${message}"
                break
            case 3:
                log.info "${message}"
                break
            case 4:
                log.warn "${message}"
                break
            case 5:
                log.error "${message}"
                break
            default:
                log.error "${message}"
        }
    }
}
