/*
 * Copyright 2018 CapTech Ventures, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
* Sample MOCK data used for testing only
*/

export const dataSetOne = {
  dataOne : {
    "file": {
      "key": "householdElectricPowerConsumption",
      "guid": "96c95ee3-95a2-420a-94d2-deccc950042f",
      "codeOfConduct": "noncoc",
      "subjectArea": "UCI",
      "business": {
        "name": "Household Electric Power Consumption",
        "description": "Measurements of electric power consumption in one household with a one-minute sampling rate over a period of almost 4 years.",
        "owner": "Bruce Wayne",
        "upstreamDataSteward": "The Oracle",
        "downstreamDataSteward": "Nightwing",
        "velocity": "monthly"
      },
      "technical": {
        "format": "tabular",
        "tableName": "Household_Electric_Power_Consumption",
        "compression": "avro",
        "rowFormat": "delimited",
        "fieldDelimiter": ";",
        "lineTerminator": "\n",
        "containsHeaderRow": "true"
      }
    },
    "fields": [
      {
        "name": "date_field",
        "position": 1,
        "businessName": "Date",
        "description": "Date in format dd/mm/yyyy",
        "datatype": "string",
        "format": "dd/mm/yyyy",
        "nullable": "true",
        "precision": null,
        "pk": true,
        "partitionPosition": 2,
        "quality": {
          "uniqueness": "The combination of date and time values should not repeat for a given premise in the D_ELECTRIC_PREMISE table",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "NA",
          "accuracy": "NA",
          "validity": "Verify that date is within the expected range"
        }
      },
      {
        "name": "time_field",
        "position": 2,
        "businessName": "Time",
        "description": "Time in format hh:mm:ss",
        "datatype": "string",
        "format": "HH:mm:ss",
        "nullable": "true",
        "precision": null,
        "pk": true,
        "partitionPosition": 1,
        "quality": {
          "uniqueness": "Time values can repeat. The combination of date and time values should not repeat for a given premise in the D_ELECTRIC_PREMISE table",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "NA",
          "accuracy": "NA",
          "validity": "Verify that time is within the appropriate range, i.e. 24 hour time frame"
        }
      },
      {
        "name": "globalActivePower",
        "position": 3,
        "businessName": "Global Active Power",
        "description": "Household global minute-averaged active power (in kilowatt)",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalReactivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable kilowatt ranges, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      },
      {
        "name": "globalReactivePower",
        "position": 4,
        "businessName": "Global Reactive Power",
        "description": "Household global minute-averaged reactive power (in kilowatt)",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalActivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable kilowatt ranges, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      },
      {
        "name": "voltage",
        "position": 5,
        "businessName": "Voltage",
        "description": "Minute-averaged voltage (in volt)",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalReactivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable voltage ranges, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      },
      {
        "name": "globalIntensity",
        "position": 6,
        "businessName": "Global Intensity",
        "description": "Household global minute-averaged current intensity (in ampere)",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalReactivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable ampere ranges, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      },
      {
        "name": "subMetering1",
        "position": 7,
        "businessName": "Sub Metering 1",
        "description": "Energy sub-metering No. 1 (in watt-hour of active energy). It corresponds to the kitchen, containing mainly a dishwasher, an oven and a microwave (hot plates are not electric but gas powered)",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalReactivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable watt-hour, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      },
      {
        "name": "subMetering2",
        "position": 8,
        "businessName": "Sub Metering 2",
        "description": "Energy sub-metering No. 2 (in watt-hour of active energy). It corresponds to the laundry room, containing a washing-machine, a tumble-drier, a refrigerator and a light",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalReactivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable watt-hour, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      },
      {
        "name": "subMetering3",
        "position": 9,
        "businessName": "Sub Metering 3",
        "description": "Energy sub-metering No. 3 (in watt-hour of active energy). It corresponds to an electric water-heater and an air-conditioner",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalReactivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable watt-hour, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      }
    ],
    "version": "2017-05-04 14:14:30.123",
    "stage": "final"
  },
  dataTwo : {
    "file": {
      "key": "householdElectricAccount",
      "guid": "96c95ee3-95a2-420a-94d2-deccc950042f",
      "codeOfConduct": "noncoc",
      "subjectArea": "UCI",
      "business": {
        "name": "Household Electric Power Consumption",
        "description": "Measurements of electric power consumption in one household with a one-minute sampling rate over a period of almost 4 years.",
        "owner": "Bruce Wayne",
        "upstreamDataSteward": "The Oracle",
        "downstreamDataSteward": "Nightwing",
        "velocity": "monthly"
      },
      "technical": {
        "format": "tabular",
        "tableName": "Household_Electric_Power_Consumption",
        "compression": "avro",
        "rowFormat": "delimited",
        "fieldDelimiter": ";",
        "lineTerminator": "\n",
        "containsHeaderRow": "true"
      }
    },
    "fields": [
      {
        "name": "date_field",
        "position": 1,
        "businessName": "Date",
        "description": "Date in format dd/mm/yyyy",
        "datatype": "string",
        "format": "dd/mm/yyyy",
        "nullable": "true",
        "precision": null,
        "pk": true,
        "partitionPosition": 2,
        "quality": {
          "uniqueness": "The combination of date and time values should not repeat for a given premise in the D_ELECTRIC_PREMISE table",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "NA",
          "accuracy": "NA",
          "validity": "Verify that date is within the expected range"
        }
      },
      {
        "name": "time_field",
        "position": 2,
        "businessName": "Time",
        "description": "Time in format hh:mm:ss",
        "datatype": "string",
        "format": "HH:mm:ss",
        "nullable": "true",
        "precision": null,
        "pk": true,
        "partitionPosition": 1,
        "quality": {
          "uniqueness": "Time values can repeat. The combination of date and time values should not repeat for a given premise in the D_ELECTRIC_PREMISE table",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "NA",
          "accuracy": "NA",
          "validity": "Verify that time is within the appropriate range, i.e. 24 hour time frame"
        }
      },
      {
        "name": "globalActivePower",
        "position": 3,
        "businessName": "Global Active Power",
        "description": "Household global minute-averaged active power (in kilowatt)",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalReactivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable kilowatt ranges, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      },
      {
        "name": "globalReactivePower",
        "position": 4,
        "businessName": "Global Reactive Power",
        "description": "Household global minute-averaged reactive power (in kilowatt)",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalActivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable kilowatt ranges, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      },
      {
        "name": "voltage",
        "position": 5,
        "businessName": "Voltage",
        "description": "Minute-averaged voltage (in volt)",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalReactivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable voltage ranges, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      },
      {
        "name": "globalIntensity",
        "position": 6,
        "businessName": "Global Intensity",
        "description": "Household global minute-averaged current intensity (in ampere)",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalReactivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable ampere ranges, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      },
      {
        "name": "subMetering1",
        "position": 7,
        "businessName": "Sub Metering 1",
        "description": "Energy sub-metering No. 1 (in watt-hour of active energy). It corresponds to the kitchen, containing mainly a dishwasher, an oven and a microwave (hot plates are not electric but gas powered)",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalReactivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable watt-hour, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      },
      {
        "name": "subMetering2",
        "position": 8,
        "businessName": "Sub Metering 2",
        "description": "Energy sub-metering No. 2 (in watt-hour of active energy). It corresponds to the laundry room, containing a washing-machine, a tumble-drier, a refrigerator and a light",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalReactivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable watt-hour, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      },
      {
        "name": "subMetering3",
        "position": 9,
        "businessName": "Sub Metering 3",
        "description": "Energy sub-metering No. 3 (in watt-hour of active energy). It corresponds to an electric water-heater and an air-conditioner",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalReactivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable watt-hour, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      }
    ],
    "version": "2017-05-04 14:14:30.123",
    "stage": "final"
  },
  dataThree : {
    "file": {
      "key": "CommercialElectricPowerAccount",
      "guid": "96c95ee3-95a2-420a-94d2-deccc950042f",
      "codeOfConduct": "noncoc",
      "subjectArea": "UCI",
      "business": {
        "name": "Household Electric Power Consumption",
        "description": "Measurements of electric power consumption in one household with a one-minute sampling rate over a period of almost 4 years.",
        "owner": "Bruce Wayne",
        "upstreamDataSteward": "The Oracle",
        "downstreamDataSteward": "Nightwing",
        "velocity": "monthly"
      },
      "technical": {
        "format": "tabular",
        "tableName": "Household_Electric_Power_Consumption",
        "compression": "avro",
        "rowFormat": "delimited",
        "fieldDelimiter": ";",
        "lineTerminator": "\n",
        "containsHeaderRow": "true"
      }
    },
    "fields": [
      {
        "name": "date_field",
        "position": 1,
        "businessName": "Date",
        "description": "Date in format dd/mm/yyyy",
        "datatype": "string",
        "format": "dd/mm/yyyy",
        "nullable": "true",
        "precision": null,
        "pk": true,
        "partitionPosition": 2,
        "quality": {
          "uniqueness": "The combination of date and time values should not repeat for a given premise in the D_ELECTRIC_PREMISE table",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "NA",
          "accuracy": "NA",
          "validity": "Verify that date is within the expected range"
        }
      },
      {
        "name": "time_field",
        "position": 2,
        "businessName": "Time",
        "description": "Time in format hh:mm:ss",
        "datatype": "string",
        "format": "HH:mm:ss",
        "nullable": "true",
        "precision": null,
        "pk": true,
        "partitionPosition": 1,
        "quality": {
          "uniqueness": "Time values can repeat. The combination of date and time values should not repeat for a given premise in the D_ELECTRIC_PREMISE table",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "NA",
          "accuracy": "NA",
          "validity": "Verify that time is within the appropriate range, i.e. 24 hour time frame"
        }
      },
      {
        "name": "globalActivePower",
        "position": 3,
        "businessName": "Global Active Power",
        "description": "Household global minute-averaged active power (in kilowatt)",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalReactivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable kilowatt ranges, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      },
      {
        "name": "globalReactivePower",
        "position": 4,
        "businessName": "Global Reactive Power",
        "description": "Household global minute-averaged reactive power (in kilowatt)",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalActivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable kilowatt ranges, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      },
      {
        "name": "voltage",
        "position": 5,
        "businessName": "Voltage",
        "description": "Minute-averaged voltage (in volt)",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalReactivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable voltage ranges, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      },
      {
        "name": "globalIntensity",
        "position": 6,
        "businessName": "Global Intensity",
        "description": "Household global minute-averaged current intensity (in ampere)",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalReactivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable ampere ranges, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      },
      {
        "name": "subMetering1",
        "position": 7,
        "businessName": "Sub Metering 1",
        "description": "Energy sub-metering No. 1 (in watt-hour of active energy). It corresponds to the kitchen, containing mainly a dishwasher, an oven and a microwave (hot plates are not electric but gas powered)",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalReactivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable watt-hour, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      },
      {
        "name": "subMetering2",
        "position": 8,
        "businessName": "Sub Metering 2",
        "description": "Energy sub-metering No. 2 (in watt-hour of active energy). It corresponds to the laundry room, containing a washing-machine, a tumble-drier, a refrigerator and a light",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalReactivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable watt-hour, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      },
      {
        "name": "subMetering3",
        "position": 9,
        "businessName": "Sub Metering 3",
        "description": "Energy sub-metering No. 3 (in watt-hour of active energy). It corresponds to an electric water-heater and an air-conditioner",
        "datatype": "decimal",
        "format": null,
        "nullable": "true",
        "precision": "8,3",
        "pk": false,
        "partitionPosition": null,
        "quality": {
          "uniqueness": "NA",
          "integrity": "NA",
          "completeness": "Calculate a ratio of missing fields to total fields",
          "consistency": "Captures the relationship to globalReactivePower with the expectation of an inverse relationship",
          "accuracy": "NA",
          "validity": "Acceptable watt-hour, connect to premise field in table D_ELECTRIC_PREMISE for range thresholds  "
        }
      }
    ],
    "version": "2017-05-04 14:14:30.123",
    "stage": "final"
  }
};
