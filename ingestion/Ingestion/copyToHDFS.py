#!/usr/bin/python2
# title           : copyToHDFS.py
# description     : copyToHDFS moves files to be ingested to the proper location(s) within
#                   HDFS. Most of this work is coordinated in the main function.  It makes
#                   use of the registry script to request file registration information
#                   and uses that to determine how many copies of each data file to make
#                   and where to place them.  In the case of sandbox, spam, and duplicate
#                   files it makes a single copy, otherwise a copy goes to both the raw
#                   and raw_work folder.  Once the files are moved it compares the sizes of
#                   the source and target files and if they match, removes the source file
#                   from the landing zone.
# author          : BAC
# usage           : called by watcher.py
# notes           :
# python_version  :2.7
# ==============================================================================

import os
import requests
import yaml
import json
import logging
import pydoop.hdfs as hdfs
import subprocess
import registry as reg
import validator
from loglib import setup_logging

# get the config file and set up variables
with open(os.path.dirname(os.path.realpath(__file__)) + "/config.yml", 'r') as ymlfile:
    cfg = yaml.load(ymlfile)

# start the logging
logger = logging.getLogger(__name__)
setup_logging(logger)

# set up a bunch of variables to make them easier to use
ingestion_user = cfg['hdfs']['ingestion_user']
ingestion_server = cfg['hdfs']['ingestion_server']
registry_URL = cfg['local']['registry_URL']
registry_port = str(cfg['local']['registry_port'])
landing_zone = cfg['local']['landing_zone']


def file_len(file_name):
    # accepts a file name and returns the number of lines in that file in the landing zone
    cmd = 'zcat ' + file_name + ' | wc -l'
    #create a subprocess to do the linux command.  This won't work on windows because the commands are different
    p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    result, err = p.communicate()
    if p.returncode != 0:
        raise IOError(err)
    # the return value may havemore than just the number we want, only return the integer we're looking for 
    return int(result.strip().split()[0])


def get_header(file_name):
    # accepts a file name and returns the first line from that file in the landing zone
    cmd = 'zcat ' + landing_zone + '/' + file_name + ' | head -n1'
    logger.info('getting header with : ' + cmd)
    #create a subprocess to do the linux command.  This won't work on windows because the commands are different
    p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    result, err = p.communicate()
    if p.returncode != 0:
        raise IOError(err)
    return result


def main(file_name, **kwargs):
    # check each file against the registry
    # determine if its spam, a duplicate, or should be ingested
    # copy the file to the appropriate place, check for equal file size, and delete one of the files appropriately
    metadata = {}
    write_path = instance_guid = stage = header = ""
    file_type = "raw"
    # json.dumps(metadata)

    # Log the PID to help in debugging 
    logger.info('Pid : ' + str(os.getpid()))
    try:
        # attempt to get the registry entry.  If Alfred isn't working properly we'll get a connection error
        if file_name.startswith('sbx_'):
            # asking the registry for sandbox file and stripping "sbx_" off the file name, keys don't have the prefix
            metadata = reg.get_metadata(file_name[4:], stage='sandbox')
        else:
            metadata = reg.get_metadata(file_name)

    except requests.ConnectionError as e:
        # log response error
        logger.error('Failed to connect to Alfred : ' + str(e))
        exit(e)

    if 'stage' in metadata:
        stage = metadata['stage']

    # get the count of the number of rows in the source file
    row_count = file_len(landing_zone + '/' + file_name)
    logger.info('row count = ' + str(row_count))

    if 'file' in metadata and metadata['file'] != {}:
        # a registry entry exists for the file, process it
        logger.info("Moving " + file_name + " to hdfs://" + reg.file_path(metadata, **kwargs))
        
        # set the write path based on the metadata
        write_path = reg.file_path(metadata, **kwargs)
        logger.info("Moving " + file_name + " to " + write_path)

        if stage == 'sandbox' and hdfs.path.exists(write_path + '/' + file_name):
            # in the case of sandbox files previous data is always overwritten
            logging.info("Sandbox file already exists, overwriting")
            # Delete from HDFS is not strictly needed if the table was created as external
            hdfs.rmr(write_path + '/' + file_name)
            # set up a hive connection
            hive = validator.Hive()
            # use the hive connection to delete the sandbox table
            hive.drop_table(metadata, stage=stage)
            # close the hive connection
            hive = None

        # check to make sure the file doesn't already exist
        if not hdfs.path.exists(write_path + '/' + file_name):
            # if it doesn't, write it to the appropriate location
            hdfs.put(landing_zone + '/' + file_name, write_path + '/' + file_name)
            # create second copy for work table unless its a sandbox file
            if stage != 'sandbox':
                # create work copy write path
                work_write_path = reg.file_path(metadata, type='work', **kwargs)
                # delete the work file if there is already one present
                if hdfs.path.exists(work_write_path):  
                    logger.info("Deleting existing work files at  " + work_write_path)
                    hdfs.rmr(work_write_path)
                # write the file to the work file location
                hdfs.put(landing_zone + '/' + file_name, work_write_path + '/' + file_name)
            else:
                # if this is a sandbox file, we might need the header row, its far easier to get this now than from hdfs
                header = get_header(file_name)
            # register that the raw file was written
            instance_guid = reg.register_raw(metadata, file_name, file_type, row_count)
        else:
            # if the file does exist, its treated as a duplicate
            logger.info("Duplicate file")
            file_type = "duplicate"
            
            # set up duplicate write path
            write_path = reg.dup_file_path(metadata)  # + '/' + metadata['file']['key']
            
            #check to see if its a duplicate of an existing duplicate
            if hdfs.path.exists(write_path + '/' + file_name):
                # delete existing duplicate and write the new one.
                logging.info("duplicate file already exists, overwriting")
                hdfs.rmr(write_path + '/' + file_name)
                hdfs.put(landing_zone + '/' + file_name, write_path + '/' + file_name)
                logger.info("writing duplicate file " + write_path + '/' + file_name)
                reg.register_raw(metadata, file_name, file_type, row_count)

            else:
                # first time duplicates just get written
                hdfs.put(landing_zone + '/' + file_name, write_path + '/' + file_name)
                logger.info("writing duplicate file " + write_path + '/' + file_name)
                reg.register_raw(metadata, file_name, file_type, row_count)

    else:
        # no registry entry for this file, move it to spam
        file_type = "spam"
        
        # set up write path for spam
        write_path = reg.spam_file_path(metadata)
        logger.info("Moving " + file_name + " to " + write_path + '/' + file_name)

        #check to see if its a duplicate of an existing spam file
        if hdfs.path.exists(write_path + '/' + file_name):
            # delete existing spam and write the new one.
            logging.info("spam file already exists, overwriting")
            hdfs.rmr(write_path + '/' + file_name)
            hdfs.put(landing_zone + '/' + file_name, write_path + '/' + file_name)
            logger.info("writing spam file " + write_path + '/' + file_name)
            reg.register_raw(metadata, file_name, file_type, row_count)
        else:
            # first time spam gets written as normal
            hdfs.put(landing_zone + '/' + file_name, write_path + '/' + file_name)
            logger.info("writing spam file " + write_path + '/' + file_name)
            reg.register_raw(metadata, file_name, file_type, row_count)
            
    # confirm that source file and target file have the same size, regardless of spam, duplicate or normal
    if hdfs.path.exists(write_path + '/' + file_name) and \
            hdfs.path.getsize(write_path + '/' + file_name) == os.stat(landing_zone + '/' + file_name).st_size:
        # if the file sizes match, delete the source file
        os.remove(landing_zone + '/' + file_name)
        logger.info("Landing zone file removed " + landing_zone + '/' + file_name)
    else:
        # if the file sizes do not match, delete the target file and rename the source file so it doesn't get reprocessed repeatedly
        logger.error("Source and target file sizes didn't match, not deleting source.")
        hdfs.rmr(write_path + '/' + file_name)
        os.rename(landing_zone + '/' + file_name, landing_zone + '/' + file_name + '.err')
        raise ValueError("Source and target file sizes don't match")

    # copy only is an option set up in case there's ever a reason not to process beyond moving the file to HDFS
    if 'copy_only' not in kwargs or not kwargs['copy_only']:
        if file_type == "raw":  # raw, meaning not spam or duplicate. No reason to validate those
            if stage != 'sandbox':
                # if its not a sandbox file proceed with full validation
                logger.info("Validate " + file_name)
                validator.main(file_name, instance_guid, metadata)
            elif stage == 'sandbox':
                # if it is a sandbox file, we need to mark it as such so validator only creates the table
                logger.info("Sandbox validate " + file_name)
                validator.main(file_name, instance_guid, metadata, header=header, stage=stage)
    
    # log that this PID is ending
    logger.info('Pid ending : ' + str(os.getpid()))

