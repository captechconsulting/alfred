#!/usr/bin/python2
# title           : refined.py
# description     : refined is responsible running R and SparkR script to process valid data
#                   into refined data. It accepts a single parameter, the name of the script
#                   it will run.  If there is metadata for a table, it will create that table
#                   if it does not exists.  It then assembles a command string from the
#                   config file information and the metadata for the script registry entry
#                   and executes that command
# author          : BAC
# usage           : python refined.py <script key>
# notes           :
# python_version  :2.7
# ==============================================================================

from os import path
import logging
import yaml
import unittest
import sys
import json
from loglib import setup_logging
import registry as reg
import validator
import subprocess

# get the config file and set up variables
with open(path.dirname(path.realpath(__file__)) + "/config.yml", 'r') as ymlfile:
    cfg = yaml.load(ymlfile)

if 'script_dir' in cfg['local'] and cfg['local']['script_dir'] != "None":
    script_dir = cfg['local']['script_dir']
else:
    script_dir = "none"

if 'script_driver' in cfg['local'] and cfg['local']['script_driver'] != "None" \
        and cfg['local']['script_driver'] is not None:
    script_driver = cfg['local']['script_driver']
else:
    script_driver = ""

if 'environment' in cfg['local'] and cfg['local']['environment'] is not None and cfg['local']['environment'] != "":
    environment = " --environment=" + cfg['local']['environment']
else:
    environment = ""

# set up and start logging
logger = logging.getLogger(path.basename(__file__))
setup_logging(logger)


def run_script(script_name, metadata):
    # accept the name of the script to be run and the metadata about that script
    
    # set up location for script based on metadata, user folder, and config file
    if script_dir is not None and script_dir != 'none':
        if script_dir == "/":
            cmd = "/" + metadata['script']['name']
        elif script_dir[0] != "/":
            cmd = path.expanduser("~") + "/" + script_dir + "/" + metadata['script']['name']
        else:
            cmd = script_dir + "/" + metadata['script']['path'] + "/" + metadata['script']['name']
    else:
        cmd = metadata['script']['path'] + "/" + metadata['script']['name']

    script_settings = ""

    # set up neccesary script settings from config file, these apply to all refined scripts 
    if 'script_settings' in cfg['local'] and cfg['local']['script_settings'] is not None:
        for setting in cfg['local']['script_settings']:
            if setting != '':
                script_settings += " --" + setting

    # todo it may eventually be useful to add script parameters in the regirstry metadata
    
    # define the command to be run
    cmd = script_driver + " " + script_settings + " " + cmd + environment
    logger.info("Script path and name : " + cmd)

    # execute the command as an OS level process
    p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    result, err = p.communicate()
    if p.returncode != 0:
        # if there is a non success return code, log it and raise it
        logging.error(p.returncode)
        raise IOError(err)


def main(args):
    # Check the registry for entries and run the script appropriately
    
    script = ""
    # Check the number of arguments, if its not exactly one, end
    if len(args) > 2:
        # first arguments is the file itself and is disregarded, so > 2 is really > 1
        logger.info("Too many arguments provided, ending process")
        exit(0)
    elif len(args) == 1:
        # first arguments is the file itself and is disregarded, so 1 is really 0
        logger.info("No arguments provided, ending process")
        exit(0)
    else:
        # we have a single argument, its the name of the script we want to run
        script = args[1]

    # Get the metadata for the script
    metadata = reg.get_refined_metadata(script)

    # Start a hive connection and use it to create the table for the refined script if it doesn't already exist
    val = validator.Hive()
    val.create_hive_table(metadata['refinedDataset'], stage='refined')

    # Count the number rows in the target table
    initial_count = val.row_count(reg.db_name(metadata['refinedDataset'], stage='refined') + '.' +
                                  reg.db_table(metadata['refinedDataset'], stage='refined'))
    logger.info("row count ")

    # print json.dumps(metadata['script'], indent=3) # left for debugging
    # execute the script 
    run_script(metadata['script']['path'], metadata)
    
    # get the row count of the target table after execution
    final_count = val.row_count(reg.db_name(metadata['refinedDataset'], stage='refined') + '.' +
                                reg.db_table(metadata['refinedDataset'], stage='refined'))

    # register that the script was run
    reg.register_raw(metadata['refinedDataset'], metadata['script']['path'], 'refined', final_count - initial_count)
    return 0

if __name__ == '__main__':
    # if this script is called directly, pass the arguements to the main function
    main(sys.argv)

