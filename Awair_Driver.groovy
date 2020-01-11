/**
 *  Copyright 2020 Pedro Andrade
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
 *	Awair 2 Driver
 *
 * Author: Pedro Andrade
 *
 *	Updates:
 *	Date: 2020-01-31	v1.0 Initial release
 */

import java.text.DecimalFormat
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

metadata {
	definition (name: "Awair Device", namespace: "HE_pfta", author: "Pedro Andrade") {
    capability "Temperature Measurement"
	capability "Relative Humidity Measurement"
    capability "CarbonDioxideMeasurement"
	capability "Polling"
	capability "Refresh"
	attribute "awairScore", "number"
	attribute "voc", "number"
	attribute "pm25", "number"
	attribute "pm10", "number"
	attribute "lux", "number"
	attribute "spl_a", "number"
    attribute "tempIndex", "number"
    attribute "humidIndex", "number"
    attribute "co2Index", "number"
    attribute "vocIndex", "number"
    attribute "pm25Index", "number"
	}
}

def getLatestAirData(){
    parent.getLatestAirDataCommand(this)
}
   
def updated(){
	getLatestAirData()
}

def installed(){
	getLatestAirData()
}

def poll() {
	log.debug "Executing 'poll'"
	refresh()
}

def refresh() {
	log.debug "Executing 'refresh'"
    getLatestAirData()
}
