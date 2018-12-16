#!/usr/bin/python2
# title           : watcher.py
# description     : watcher.py is the script triggered by a cron job.  It invokes tendo to prevent
#                   more than one copy of itself from running at a time.  During each run, the
#                   list of gzipped (.gz) files in the landing zone is generated.  A list of the
#                   keys for these files is obtained from the registry.  Each file that does not
#                   match an already running key  is then passed to the copyToHDFS.py script in a
#                   new process in order to copy and validate them in parallel.
# author          : BAC
# usage           : python watcher.py
# notes           :
# python_version  :2.7
# ==============================================================================

from os import listdir, path
from tendo import singleton
import yaml
import time
import logging
import threading
from loglib import setup_logging
import registry as reg
import copyToHDFS

# get the config file and set up variables
with open(path.dirname(path.realpath(__file__)) + "/config.yml", 'r') as ymlfile:
    cfg = yaml.load(ymlfile)

landing_zone = cfg['local']['landing_zone']

# using tendo.singleton prevent from running more than once at a time
me = singleton.SingleInstance()  

# start the logging
logger = logging.getLogger(path.basename(__file__))
setup_logging(logger)

#get the directory listing.  Only get files ending with .gz and older than 60 seconds
lz_list = [f for f in listdir(landing_zone) if path.isfile(landing_zone + "/" + f) and f.endswith('.gz')
           and (time.time() - path.getmtime(landing_zone + "/" + f) > 60) ]
logger.info('Found ' + str(len(lz_list)) + ' files in landing zone')

# TODO implement pooling so we don't do more than X at a time

# Using the file list, get the keys for every file in the LZ.
key_list = {}
for file_name in lz_list:
    if file_name.startswith('sbx_'):
        # asking the registry for sandbox file and stripping "sbx_" off the file name
        metadata = reg.get_metadata(file_name[4:], stage='sandbox')
    else:
        metadata = reg.get_metadata(file_name)

    if 'key' in metadata['file']:
        key_list[file_name] = metadata['file']['key']
    else:
        key_list[file_name] = file_name

# loop through the list of files with keys, if there is no thread already running with that file's key name
# start a new thread with the key name to process that file
for file_name in key_list:
    if key_list[file_name] not in [thread.name for thread in threading.enumerate()]:
        logger.info("Copy " + file_name + " from " + landing_zone)
        thread = threading.Thread(target=copyToHDFS.main, args=(file_name,), name=key_list[file_name])
        thread.start()