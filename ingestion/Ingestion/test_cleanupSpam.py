#!/usr/bin/python2
# title           : test_cleanupSpam.py
# description     : unit tests for test_cleanupSpam.py
# author          : BAC
# python_version  :2.7
# ==============================================================================

import os
import unittest
import yaml
import sys
import logging
import pydoop.hdfs as hdfs
import time
sys.path.append(os.path.join(os.path.dirname(os.path.realpath(__file__))))
import registry as reg
from loglib import setup_logging
import cleanupSpam

with open(os.path.dirname(os.path.realpath(__file__)) + "/config.yml", 'r') as ymlfile:
    cfg = yaml.load(ymlfile)

logger = logging.getLogger(__name__)
setup_logging(logger)

spam_ttl = 30
ingestion_user = cfg['hdfs']['ingestion_user']
ingestion_server = cfg['hdfs']['ingestion_server']
registry_URL = cfg['local']['registry_URL']
registry_port = str(cfg['local']['registry_port'])
landing_zone = cfg['local']['landing_zone']
if 'spam_ttl' in cfg['hdfs']:
    spam_ttl = cfg['hdfs']['spam_ttl']


class TestHDFSCopy(unittest.TestCase):
    def setUp(self):
        pass

    def tearDown(self):
        pass

    def test_RemoveSpamFile(self):
        cleanupSpam.main()

if __name__ == "__main__":
    TestHDFSCopy().test_RemoveSpamFile()
