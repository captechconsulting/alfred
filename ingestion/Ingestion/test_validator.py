#!/usr/bin/python2
# title           : test_validator.py
# description     : unit tests for validator.py
# author          : BAC
# python_version  :2.7
# ==============================================================================

import os
import unittest
import yaml
import impala.dbapi
from impala.dbapi import connect
import json
import pydoop.hdfs as hdfs
import time
import sys
import logging
import multiprocessing
from loglib import setup_logging
from uuid import uuid4
sys.path.append(os.path.join(os.path.dirname(os.path.realpath(__file__))))
import registry as reg
import validator

logger = logging.getLogger(__name__)
setup_logging(logger)

with open(os.path.dirname(os.path.realpath(__file__)) + "/config.yml", 'r') as ymlfile:
    cfg = yaml.load(ymlfile)

ingestion_user = cfg['hdfs']['ingestion_user']
ingestion_server = cfg['hdfs']['ingestion_server']
registry_URL = cfg['local']['registry_URL']
registry_port = str(cfg['local']['registry_port'])
landing_zone = cfg['local']['landing_zone']
hive_host = cfg['hdfs']['hive_host']
hive_user = cfg['hdfs']['hive_user']
hive_PW = cfg['hdfs']['hive_PW']
hive_auth_mechanism = cfg['hdfs']['hive_auth_mechanism']
hive_kerberos_service_name = cfg['hdfs']['hive_kerberos_service_name']


#
# ## Unit Tests
class ValidatorTest(unittest.TestCase):
    def setUp(self):
        self.t_hive = validator.Hive()
        metadata = reg.get_metadata("household_power_consumption")
        self.t_hive.create_hive_table(metadata, reset=True, type="work")
        self.t_hive.create_hive_table(metadata, reset=True, stage="valid")
        # hdfs.utime

    def tearDown(self):
        metadata = reg.get_metadata("household_power_consumption")
        self.t_hive._query("drop table if exists " + reg.db_name(metadata) + '.' + reg.db_table(metadata))
        self.t_hive._query("drop table if exists " + reg.db_name(metadata) + '.' + reg.db_table(metadata, type="work"))
        self.t_hive._query("drop table if exists " + reg.db_name(metadata, stage="valid") +
                           '.' + reg.db_table(metadata))

        metadata2 = reg.get_metadata("small")
        self.t_hive._query("drop table if exists " + reg.db_name(metadata2) + '.' + reg.db_table(metadata2))
        self.t_hive._query("drop table if exists " + reg.db_name(metadata2) + '.'
                           + reg.db_table(metadata2, type="work"))
        self.t_hive._query("drop table if exists " + reg.db_name(metadata2, stage="valid") +
                           '.' + reg.db_table(metadata2))

    def test_db_table(self):
        meta = {"file": {"key": "householdElectricPowerConsumption","technical": {"tableName" : "householdelectricpowerconsumption"}}}
        self.assertEquals(reg.db_table(meta), "householdelectricpowerconsumption")

    def test_row_count(self):
        self.assertEquals(self.t_hive.row_count('dev_none_uci.household_electric_power_consumption'), 0)

    def test_row_count_where(self):
        self.assertEquals(self.t_hive.row_count('dev_none_uci.household_electric_power_consumption',
                                                'globalintensity < 20'), 0)

    def test_delta(self):
        metadata = reg.get_metadata("householdElectricPowerConsumption")
        self.t_hive.create_hive_table(metadata, stage="valid", type='work')
        self.t_hive.delta(metadata)

    def test_dev_db_name(self):
        meta = {"file": {"dataPartition": "xtrnl", "subjectArea": "UCI","technical": {"tableName" : "householdelectricpowerconsumption"}}}
        self.assertEquals(reg.db_name(meta), "dev_xtrnl_uci_raw", "Incorrect DB name")

    # Test invalidated because we're not using environments this way anymore
    def test_prod_db_name(self):
        meta = {"file": {"dataPartition": "xtrnl", "subjectArea": "UCI","technical": {"tableName" : "householdelectricpowerconsumption"}}}
        self.assertEquals(reg.db_name(meta, stage="raw", env="prod"), "xtrnl_uci_raw")

    # Test invalidated because we're testing it in the registry script
    def test_dev_file_path(self):
        meta = {"file": {"dataPartition": "xtrnl", "subjectArea": "UCI", "technical": {"tableName": "small"}}}
        self.assertEquals(reg.file_path(meta), "/user/cloudera/data/xtrnl/uci/raw/small")

    # Test invalidated because we're not using environments this way anymore
    def test_prod_file_path(self):
        # /environment/dataPartition/SubjectArea/stage/filename
        meta = {"file": {"dataPartition": "xtrnl", "subjectArea": "UCI", "technical": {"tableName": "small"}}}
        self.assertEquals(reg.file_path(meta, stage="valid", env="prod"), "/user/cloudera/data/xtrnl/uci/valid/small")

    def test_raw_hive(self):
        metadata = reg.get_metadata("small")
        # self.t_hive.hive_raw(metadata, reset=True)
        self.t_hive.create_hive_table(metadata, reset=True)
        self.assertEqual(self.t_hive._query('describe ' + reg.db_name(metadata) + '.'
                                            + reg.db_table(metadata)),
                         [('date_field', 'string', 'from deserializer'), ('time_field', 'string', 'from deserializer'),
                          ('globalactivepower', 'string', 'from deserializer')]
                         )

    def test_raw_hive_work(self):
        metadata = reg.get_metadata("small")
        self.t_hive.create_hive_table(metadata, reset=True, type="work")
        self.assertEqual(self.t_hive._query('describe ' + reg.db_name(metadata) + '.'
                                            + reg.db_table(metadata, type="work")),
                         [('date_field', 'string', 'from deserializer'), ('time_field', 'string', 'from deserializer'),
                          ('globalactivepower', 'string', 'from deserializer')]
                         )

    def test_valid_hive_work(self):
        metadata = reg.get_metadata("small")
        self.t_hive.create_hive_table(metadata, reset=True, type="work", stage="valid")
        self.assertEqual(
            self.t_hive._query('describe ' + reg.db_name(metadata, type="work", stage="valid") + '.' +
                               reg.db_table(metadata, type="work", stage="valid")),
            [
                ("date_field", "string", ""),
                ("time_field", "string", ""),
                ("globalactivepower", "decimal(8,3)", ""),
                ("instance_guid", "string", "")
            ]
            )
        self.t_hive._query("drop table if exists " + reg.db_name(metadata) + '.' + reg.db_table(metadata))

    def test_valid_hive(self):
        metadata = reg.get_metadata("small")
        self.t_hive.create_hive_table(metadata, reset=True, stage="valid")
        self.assertEqual(
            self.t_hive._query('describe ' + reg.db_name(metadata, stage="valid") + '.' + reg.db_table(metadata)),
            [('globalactivepower', 'decimal(8,3)', ''),
             ('instance_guid', 'string', ''),
             ('time_field', 'string', ''), ('date_field', 'string', ''), ('', None, None),
             ('# Partition Information', None, None),
             ('# col_name            ', 'data_type           ', 'comment             '), ('', None, None),
             ('time_field', 'string', ''), ('date_field', 'string', '')]
            )

    # def test_copy_data(self):
    #     md = reg.get_metadata("household_power_consumption")
    #     raw_work = reg.file_path(md, stage="raw", type='work') + "/household_power_consumption_pipe.txt.gz"
    #     raw_reg = reg.file_path(md, stage="raw") + "/household_power_consumption_pipe.txt.gz"
    #
    #     logging.info("raw work : " + raw_work + " : " + str(hdfs.path.getatime(raw_work)))
    #     logging.info("raw regular : " + raw_reg + " : " + str(hdfs.path.getatime(raw_reg)))
    #
    #     self.assertTrue(hdfs.path.getatime(raw_reg) > hdfs.path.getatime(raw_work))

    def test_z_copy_compare_append(self):
        # z in the name so this test runs last in Pycharm
        metadata = reg.get_metadata("append_power_consumption")
        self.t_hive.copy_and_compare(metadata, uuid4(), "append_power_consumption")
        raw_work = reg.file_path(metadata, stage="raw", type='work') + "/household_power_consumption_50.txt.gz"
        raw_reg = reg.file_path(metadata, stage="raw") + "/household_power_consumption_50.txt.gz"
        logging.info("raw_work : " + raw_work)
        logging.info("raw regular : " + raw_reg)
        # self.assertTrue(hdfs.path.getatime(raw_reg) > hdfs.path.getatime(raw_work))

    def test_z_copy_compare_full(self):
        # z in the name so this test runs last in Pycharm
        metadata = reg.get_metadata("household_power_consumption")
        raw_reg = reg.file_path(metadata, stage="raw") + "/household_power_consumption_50.txt.gz"
        valid = reg.file_path(metadata, stage="valid")

        logging.info("valid work : " + valid)
        logging.info("raw regular : " + raw_reg)
        self.t_hive.copy_and_compare(metadata, uuid4(), "household_power_consumption")
        # logging.info("valid : " + str(hdfs.path.getmtime(valid)))
        # logging.info("raw regular : " + str(hdfs.path.getatime(raw_reg)))
        # self.assertTrue(hdfs.path.getatime(raw_reg) > hdfs.path.getmtime(valid))

    def test_query(self):
        self.assertEqual(
            self.t_hive._query("show databases like 'default'"),
            [('default',)])

    def test_query_no_response(self):
        self.assertIsNone(self.t_hive._query("create database if not exists default"))

    def test_escape_hive_replace(self):
        self.assertEqual(validator.escape_hive('Date'), 'Date_field')

    def test_escape_hive_no_replace(self):
        self.assertEqual(validator.escape_hive('fieldname'), 'fieldname')

    def test_query_bad_query(self):
        self.assertIsNone(self.t_hive._query("not valid query"))

    def test_sandbox_header_delim(self):
        metadata = reg.get_metadata("sandbox")
        test_header = 'Date;Time;Global_active_power;Global_reactive_power;Voltage;Global_intensity;'
        test_header += 'Sub_metering_1;Sub_metering_2;Sub_metering_3'
        self.t_hive.create_hive_table(metadata, reset=True, header=test_header)
        self.assertEqual(self.t_hive._query('describe ' + reg.db_name(metadata) + '.' + reg.db_table(metadata)),
                         [('date_field', 'string', 'from deserializer'),
                          ('time_field', 'string', 'from deserializer'),
                          ('globalactivepower', 'string', 'from deserializer'),
                          ('globalreactivepower', 'string', 'from deserializer'),
                          ('voltage', 'string', 'from deserializer'),
                          ('globalintensity', 'string', 'from deserializer'),
                          ('submetering1', 'string', 'from deserializer'),
                          ('submetering2', 'string', 'from deserializer'),
                          ('submetering3', 'string', 'from deserializer')]
                         )

    def test_sandbox_delim(self):
        metadata = reg.get_metadata("delim")
        test_header = 'Date;Time;Global_active_power;Global_reactive_power;Voltage;Global_intensity;'
        test_header += 'Sub_metering_1;Sub_metering_2;Sub_metering_3'
        self.t_hive.create_hive_table(metadata, reset=True, header=test_header)
        self.assertEqual(self.t_hive._query('describe ' + reg.db_name(metadata) + '.' + reg.db_table(metadata)),
                         [('column1', 'string', 'from deserializer'),
                          ('column2', 'string', 'from deserializer'),
                          ('column3', 'string', 'from deserializer'),
                          ('column4', 'string', 'from deserializer'),
                          ('column5', 'string', 'from deserializer'),
                          ('column6', 'string', 'from deserializer'),
                          ('column7', 'string', 'from deserializer'),
                          ('column8', 'string', 'from deserializer'),
                          ('column9', 'string', 'from deserializer')]
                         )

    def test_sandbox_nodelim(self):
        metadata = reg.get_metadata("no_delim")
        test_header = 'Date;Time;Global_active_power;Global_reactive_power;Voltage;Global_intensity;'
        test_header += 'Sub_metering_1;Sub_metering_2;Sub_metering_3'
        self.t_hive.create_hive_table(metadata, reset=True, header=test_header)
        self.assertEqual(self.t_hive._query('describe ' + reg.db_name(metadata) + '.' + reg.db_table(metadata)),
                         [('column1', 'string', '')]
                         )

    def test_sandbox_full_MD(self):
        metadata = reg.get_metadata("fullsand")
        self.t_hive.create_hive_table(metadata, reset=True)
        self.assertEqual(self.t_hive._query('describe ' + reg.db_name(metadata) + '.' + reg.db_table(metadata)),
                         [('date_field', 'string', 'from deserializer'),
                          ('time_field', 'string', 'from deserializer'),
                          ('globalactivepower', 'string', 'from deserializer'),
                          ('globalreactivepower', 'string', 'from deserializer'),
                          ('voltage', 'string', 'from deserializer'),
                          ('globalintensity', 'string', 'from deserializer'),
                          ('submetering1', 'string', 'from deserializer'),
                          ('submetering2', 'string', 'from deserializer'),
                          ('submetering3', 'string', 'from deserializer')]
                         )

    def test_field_conversion_date(self):
        field = {"name": "date_field",
                "format": "dd/mm/yyyy",
                "datatype": "date"}
        self.assertEqual(validator.field_conversion(field), "cast(to_date(from_unixtime(unix_timestamp(date_field, " +
                        "'dd/mm/yyyy'))) as date)")

    def test_field_conversion_date_alias(self):
        field = {"name": "date_field",
                "format": "dd/mm/yyyy",
                "datatype": "date"}
        self.assertEqual(validator.field_conversion(field, 'a'), "cast(to_date(from_unixtime(unix_timestamp(a.date_field, " +
                         "'dd/mm/yyyy'))) as date)")

    def test_field_conversion_timestamp(self):
        field = {"name": "time_field",
                 "format": "MM/dd/yyyy hh:mm:ss aa",
                 "datatype": "timestamp"}
        self.assertEqual(validator.field_conversion(field), "cast(from_unixtime(unix_timestamp(time_field, " +
                         "'MM/dd/yyyy hh:mm:ss aa')) as timestamp)")

    def test_field_conversion_timestamp_alias(self):
        field = {"name": "time_field",
                 "format": "MM/dd/yyyy hh:mm:ss aa",
                 "datatype": "timestamp"}
        self.assertEqual(validator.field_conversion(field, 'a'), "cast(from_unixtime(unix_timestamp(a.time_field, " +
                         "'MM/dd/yyyy hh:mm:ss aa')) as timestamp)")

    def test_field_conversion_none_alias(self):
        field = {"name": "string_field",
                 "datatype": "varchar"}
        self.assertEqual(validator.field_conversion(field, 'a'),
                         "cast(a.string_field as varchar)")

    def test_field_conversion_none(self):
        field = {"name": "string_field",
                 "datatype": "varchar"}
        self.assertEqual(validator.field_conversion(field),
                         "cast(string_field as varchar)")

    def test_field_conversion_varchar_precision(self):
        field = {"name": "string_field",
                 "datatype": "varchar",
                 "precision": "8", }
        self.assertEqual(validator.field_conversion(field),
                         "cast(string_field as varchar(8))")
