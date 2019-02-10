#!/usr/bin/python2
# title           : test_refined.py
# description     : unit tests for refined.py
# author          : BAC
# python_version  :2.7
# ==============================================================================

from os import path
import logging
import yaml
import unittest
import sys
import json
sys.path.append(path.join(path.dirname(path.realpath(__file__))))
from loglib import setup_logging
import registry as reg
import validator
import refined


logger = logging.getLogger(__name__)
setup_logging(logger)


class TestRefined(unittest.TestCase):
    def test_two_arg(self):
        tmp = ['script', '', '']
        with self.assertRaises(SystemExit) as cm:
            refined.main(tmp)
        self.assertEqual(cm.exception.code, 0)

    def test_no_arg(self):
        tmp = ['script']
        with self.assertRaises(SystemExit) as cm:
            refined.main(tmp)
        self.assertEqual(cm.exception.code, 0)

    def test_script(self):
        tmp = ['script', 'consumptionByWeather']
        self.assertEqual(refined.main(tmp), 0)

# testing metadata
    '''md = {
    "script": {
      "path": "test.sh", #"path": "calli01/weatherScripts/average.R",
      "name": "average",
      "description": "average use over a given time span",
      "owner": "calli01",
      "schedule": "03***"
    },
    "sourceTemplates": [
      {
        "file": {
          "key": "householdElectricPowerConsumption",
          "guid": "96c95ee3-95a2-420a-94d2-deccc950042f"
        },
        "fields": [
          {
            "name": "date_field"
          },
          {
            "name": "time_field"
          },
          {
            "name": "globalReactivePower"
          }
        ],
        "version": "2017-05-04 14:14:30.123"
      },
      {
        "file": {
          "key": "weatherInformation",
          "guid": "beeffeed-95a2-420a-94d2-deccc950042f"
        },
        "fields": [
          {
            "name": "date_field"
          },
          {
            "name": "time_field"
          },
          {
            "name": "temperature"
          }
        ],
        "version": "2017-05-04 14:14:30.123"
      }
    ],
    "refinedDataset": {
      "file": {
        "key": "consumptionByWeather",
        "guid": "deadbeef-6649-48ac-b7e8-bd6cc9fe2f6e",
        "dataPartition": "none",
        "subjectArea": "UCI",
        "business": {
          "name": "Household Electric Power Consumption",
          "description": "Measurements of electric power",
          "owner": "Bruce Wayne",
          "dataSteward": "Nightwing"
        },
        "technical": {
          "format": "table",
          "tableName": "consumption_by_weather-provide",
          "compression": "parquet"
        }
      },
      "fields": [
        {
          "name": "averageUse",
          "position": 1,
          "businessName": "average use",
          "description": "average use for given time span",
          "datatype": "string",
          "nullable": True,
          "precision": None,
          "pk": True,
          "partitionPosition": 1,
          "derived": True,
          "quality": {
            "uniqueness": "The combination of date and time values should not repeat for",
            "integrity": "NA",
            "completeness": "Calculate a ratio of missing fields to total fields",
            "consistency": "NA",
            "accuracy": "NA",
            "validity": "Verify that date is within the expected range"
          }
        },
        {
          "name": "startDateTime",
          "position": 2,
          "businessName": "start DateTime",
          "description": "Date and Time in format dd/MM/yyyy hh:mm:ss",
          "datatype": "decimal",
          "nullable": True,
          "precision": "10,2",
          "pk": True,
          "partitionPosition": 2,
          "derived": True,
          "quality": {
            "uniqueness": "Time values can repeat. The combination of date and time values should not ",
            "integrity": "NA",
            "completeness": "Calculate a ratio of missing fields to total fields",
            "consistency": "NA",
            "accuracy": "NA",
            "validity": "Verify that time is within the appropriate range, i.e. 24 hour time frame"
          }
        },
        {
          "name": "endDateTime",
          "position": 3,
          "businessName": "end DateTime",
          "description": "Date and Time in format dd/MM/yyyy hh:mm:ss",
          "datatype": "int",
          "nullable": True,
          "precision": None,
          "pk": True,
          #  "partitionPosition": 3,
          "derived": True,
          "quality": {
            "uniqueness": "Time values can repeat. The combination of date and time values should not repeat ",
            "integrity": "NA",
            "completeness": "Calculate a ratio of missing fields to total fields",
            "consistency": "NA",
            "accuracy": "NA",
            "validity": "Verify that time is within the appropriate range, i.e. 24 hour time frame"
          }
        }
      ],
      "version": "2017-05-24 14:14:30.123",
      "stage": "final",
      "eid": "calli01"
    }
    }
    '''
    # end testing metadata