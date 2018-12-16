#!/usr/bin/python2
# title           : test_copyToHDFS.py
# description     : unit tests for copyToHDFS.py
# author          : BAC
# python_version  :2.7
# ==============================================================================


import os
import unittest
import requests
import yaml
import json
import sys
import logging
import pydoop.hdfs as hdfs
import subprocess
sys.path.append(os.path.join(os.path.dirname(os.path.realpath(__file__))))
import registry as reg
import validator
from loglib import setup_logging
import copyToHDFS
with open(os.path.dirname(os.path.realpath(__file__)) + "/config.yml", 'r') as ymlfile:
    cfg = yaml.load(ymlfile)

logger = logging.getLogger(__name__)
setup_logging(logger)


ingestion_user = cfg['hdfs']['ingestion_user']
ingestion_server = cfg['hdfs']['ingestion_server']
registry_URL = cfg['local']['registry_URL']
registry_port = str(cfg['local']['registry_port'])
landing_zone = cfg['local']['landing_zone']


class TestHDFSCopy(unittest.TestCase):
    def setUp(self):
        import gzip
        import shutil
        try:
            os.utime(landing_zone + '/badfile.txt', None)
        except OSError:
            f = open(landing_zone + '/badfile.txt', 'a')
            for x in range(0, 10):
                f.write('line ' + str(x) + '\n')
            f.close()
            with open(landing_zone + '/badfile.txt', 'rb') as f_in, \
                    gzip.open(landing_zone + '/badfile.txt.gz', 'wb') as f_out:
                shutil.copyfileobj(f_in, f_out)
        try:
            os.utime(landing_zone + '/badfile.txt', None)
        except OSError:
            open(landing_zone + '/badfile.txt', 'a').close()

        try:
            os.utime(landing_zone + '/sample.txt', None)
        except OSError:
            open(landing_zone + '/sample.txt', 'a').close()

        try:
            os.utime(landing_zone + '/sandbox.txt', None)
        except OSError:
            open(landing_zone + '/sandbox.txt', 'a').close()

        try:
            hdfs.rmr(hdfs.path.expanduser("~") + '/data/none/test')
        except IOError:
            pass

        try:
            if hdfs.path.exists(hdfs.path.expanduser("~") + '/data/duplicate/'):
                hdfs.rmr(hdfs.path.expanduser("~") + '/data/duplicate/')
        except IOError:
            pass

        try:
            if hdfs.path.exists(hdfs.path.expanduser("~") + '/data/spam/'):
                hdfs.rmr(hdfs.path.expanduser("~") + '/data/spam/')
        except IOError:
            pass
        try:
            if hdfs.path.exists(hdfs.path.expanduser("~") + '/data/sandbox/bria644/sandboxFile/sandbox.txt'):
                hdfs.rmr(hdfs.path.expanduser("~") + '/data/sandbox/bria644/sandboxFile/sandbox.txt')
        except IOError:
            pass

    def tearDown(self):
        try:
            os.remove(landing_zone + '/sample.txt')
        except OSError:
            pass

        try:
            os.remove(landing_zone + '/badfile.txt')
        except OSError:
            pass

    def test_MoveSpamFile(self):
        copyToHDFS.main('badfile.txt', copy_only=True)
        file_moved = hdfs.path.exists('data/spam/badfile.txt')
        file_removed = not os.path.exists(landing_zone + '/badfile.txt')
        self.assertTrue(file_moved and file_removed, "Failed to move the file.")

    def test_MoveGoodFile(self):
        copyToHDFS.main('sample.txt', copy_only=True)
        self.assertTrue(
                hdfs.path.exists(hdfs.path.expanduser("~") + '/data/none/test/raw/sample/sample.txt') and
                hdfs.path.exists(hdfs.path.expanduser("~") + '/data/none/test/raw_work/sample/sample.txt')
        )

    def test_MoveGoodFile_sandbox(self):
        copyToHDFS.main('sandbox.txt', copy_only=True)
        self.assertTrue(hdfs.path.exists('/user/cloudera/data/sandbox/bria644/household_electric_power_consumption/sandbox.txt'))

    def test_MoveDuplicateFile(self):
        copyToHDFS.main('sample.txt', copy_only=True)
        try:
            os.utime(landing_zone + '/sample.txt', None)
        except OSError:
            open(landing_zone + '/sample.txt', 'a').close()
            copyToHDFS.main('sample.txt', copy_only=True)
        self.assertTrue(hdfs.path.exists(hdfs.path.expanduser("~") + '/data/duplicate/sample.txt'))

    def test_file_len(self):
        copyToHDFS.main('sandbox.txt', copy_only=True)

        file_name = "/user/cloudera/data/sandbox/bria644/household_electric_power_consumption/sandbox.txt"
        with hdfs.open(file_name) as fi:
            print fi.readline()
        self.assertEquals(
            copyToHDFS.file_len(landing_zone + '/' + '/badfile.txt.gz'),
            10, 'Incorrect file length')

    def test_get_header(self):
        file_name = '/Spam/household_power_consumption_50.txt.gz'
        test_header = 'Date;Time;Global_active_power;Global_reactive_power;Voltage;Global_intensity;'
        test_header += 'Sub_metering_1;Sub_metering_2;Sub_metering_3'
        self.assertEqual(copyToHDFS.get_header(file_name).strip(), test_header)

if __name__ == "__main__":
    unittest.main()
