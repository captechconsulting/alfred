#!/usr/bin/python2
# title           : test_registry.py
# description     : unit tests for registry.py
# author          : BAC
# python_version  :2.7
# ==============================================================================

import requests
from loglib import setup_logging
import logging
import os
import yaml
import unittest
from uuid import UUID, uuid4
import json
import pydoop.hdfs as hdfs
import multiprocessing
import registry

logger = logging.getLogger(__name__)
setup_logging(logger)


with open(os.path.dirname(os.path.realpath(__file__)) + "/config.yml", 'r') as ymlfile:
    cfg = yaml.load(ymlfile)


class RegistryTest(unittest.TestCase):

    maxDiff = None

    def test_RegisteredFile(self):
        print json.dumps(registry.get_metadata('sampleFile.db'), indent=4)
        self.assertEqual(registry.get_metadata('sampleFile.db'),
                         {
                             "fields": [],
                             "file": {
                                 "dataPartition": "none",
                                 "technical": {
                                     "tableName": "sample"
                                 },
                                 "subjectArea": "test",
                                 "deleted": "true",
                                 "key": "sample",
                                 "guid": "7e1a614c-9570-42a6-9bc7-315f2b6218be"
                             }
                         }
                         )

    def test_NotRegisteredFile(self):
        # self.assertEqual(get_metadata('badfile.db'), {u'fields': [], u'file': {}})
        self.assertEqual(registry.get_metadata('badfile.db'), {u'fields': [], u'file': {}})

    def test_validate_uuid(self):
        self.assertEqual(registry.validate_uuid('7e1a614c-9570-42a6-9bc7-315f2b6218be'), True, "not a valid uuid")

    def test_invalidate_uuid(self):
        self.assertEqual(registry.validate_uuid('qqqqqqqq-cafe-face-1234-123456789abc'), False, "valid uuid")

    def test_register_raw(self):
        # self.assertTrue(validate_uuid(register_raw(get_metadata('sampleFile.txt'), 'sampleFile.txt', 'raw', 123)),
        #                 "Didn't get UUID")
        self.assertTrue(registry.validate_uuid(registry.register_raw(registry.get_metadata('household_power_consumption_50.txt.gz'),
                                                   'household_power_consumption_50.txt.gz', 'raw', 123)),
                        "Didn't get UUID")

    def test_register_valid(self):
        self.assertTrue(registry.validate_uuid(registry.register_valid(
            registry.get_metadata('sample.txt'),
            uuid4(),
            'sample.txt',
            123,
            'validation query'
        )))

    def test_register_invalid(self):
        self.assertTrue(registry.validate_uuid(registry.register_invalid(registry.get_metadata('sample.txt'), uuid4(), 'sample.txt',
                                                       {"datatypeMismatch": 3}, 123, 'validation_query')))

    def test_register_invalid_multireason(self):
        self.assertTrue(registry.validate_uuid(
            registry.register_invalid(registry.get_metadata('sample.txt'), uuid4(), 'sample.txt',
                             {"datatypeMismatch": 3, "rowCountMismatch": 12}, 123, 'validation_query')))

    def test_template_guid(self):
        self.assertEqual(
            registry.template_guid(registry.get_metadata('sample')),
            '7e1a614c-9570-42a6-9bc7-315f2b6218be',
            "Didn't get UUID")

    def test_key_matched(self):
        self.assertEqual(registry.key_matched(registry.get_metadata('sample')), 'sample', "Didn't get UUID")

    def test_db_name(self):
        self.assertEqual(
            registry.db_name(registry.get_metadata('sample')),
            'dev_none_test_raw')

    def test_db_name_test(self):
        self.assertEqual(
            registry.db_name(registry.get_metadata('sample.txt'), env='test'),
            'test_none_test_raw')

    def test_db_name_prod(self):
        self.assertEqual(
            registry.db_name(registry.get_metadata('sample.txt'), env='prod'),
            'none_test_raw')

    def test_db_name_work(self):
        self.assertEqual(
            registry.db_name(registry.get_metadata('sample.txt'), type='work'),
            'dev_none_test_raw')

    def test_db_name_valid(self):
        self.assertEqual(
            registry.db_name(registry.get_metadata('sampleFile.txt'), stage='valid'),
            'dev_none_test')

    def test_db_name_refined(self):
        self.assertEqual(
            registry.db_name(registry.get_metadata('sample.txt'), stage='refined'),
            'dev_none_test',
            "Incorrect db returned")

    def test_file_path(self):
        self.assertEqual(
            registry.file_path(registry.get_metadata('sample')),
            registry.data_root + '/none/test/raw/sample')

    def test_file_path_sandbox(self):
        # service_account/data/sandbox/uid/table_name
        self.assertEqual(
            registry.file_path(registry.get_metadata('sandbox')),
            registry.data_root + '/sandbox/bria644/household_electric_power_consumption')

    def test_file_path_stage(self):
        self.assertEqual(
            registry.file_path(registry.get_metadata('sample'), stage='valid'),
            registry.data_root + '/none/test/valid/sample',
            "incorrect file path")

    def test_file_path_work(self):
        logger.info(registry.file_path(registry.get_metadata('sample'), type='work'))
        self.assertEqual(
            registry.file_path(registry.get_metadata('sample'), type='work'),
            registry.data_root + '/none/test/raw_work/sample',
            "incorrect file path")

    def test_file_path_work_sandbox(self):
        logger.info(registry.file_path(registry.get_metadata('sandbox'), type='work'))
        self.assertEqual(
            registry.file_path(registry.get_metadata('sandbox'), type='work'),
            '/user/cloudera/data/sandbox/bria644/household_electric_power_consumption_work')

    def test_db_table(self):
        self.assertEqual(
            registry.db_table(registry.get_metadata('sample')),
            'sample',
            "wrong db table")

    def test_db_table_work(self):
        logger.info(registry.db_table(registry.get_metadata('sample'), type='work'))
        self.assertEqual(
            registry.db_table(registry.get_metadata('sample'), type='work'),
            'sample_work',
            "Bad Work Table")
