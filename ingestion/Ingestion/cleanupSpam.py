#!/usr/bin/python2
# title           : cleanupSpam.py
# description     : cleanupSpam is responsible for removing files from the spam folder
#                  in HDFS that have been there longer than the number of days specified
#                  in the config.yml file.  The script gets a directory listing of the spam
#                  folder, loops through that listing, and deletes the files with a timestamp
#                  greater than the number of seconds in a day (86400) times the number of days
#                  specified by config.  If no spam_ttl is specified the default is 30 days.
# author          : BAC
# usage           : python cleanupSpam.py
# notes           :
# python_version  :2.7
# ==============================================================================

import os
import unittest
import yaml
import sys
import logging
import pydoop.hdfs as hdfs
import time
from Ingestion import setup_logging
from Ingestion import registry as reg

# get the config file and set up variables
with open(os.path.dirname(os.path.realpath(__file__)) + "/config.yml", 'r') as ymlfile:
    cfg = yaml.load(ymlfile)

spam_ttl = 30
ingestion_user = cfg['hdfs']['ingestion_user']
ingestion_server = cfg['hdfs']['ingestion_server']
registry_URL = cfg['local']['registry_URL']
registry_port = str(cfg['local']['registry_port'])
landing_zone = cfg['local']['landing_zone']
if 'spam_ttl' in cfg['hdfs']:
    spam_ttl = cfg['hdfs']['spam_ttl']

# start the logging
logger = logging.getLogger(os.path.basename(__file__))
setup_logging(logger)

def clean_directory(dir, spam_life=spam_ttl):
    # Accepts a directory name and deletes anything older than TTL in days
    file_list = []
    
    # check the existance of the directory
    if hdfs.path.exists(dir):
        # get a list of all files there
        file_list = hdfs.lsl(dir)

    # loop through the file list
    for listing in file_list:
        # get the last access time of the file and compare to spam lifetime
        if time.time() - listing['last_access'] > 86400 * spam_life: # 86400 seconds in a day
            # if its too old delete it and log that it was deleted
            logger.info('Deleting ' + listing['name'])
            hdfs.rmr(listing['name'])


def main():
    # clean up both the spam and duplicate directories
    clean_directory(reg.spam_file_path(None))
    clean_directory(reg.dup_file_path(None))


if __name__ == "__main__":
    main()
