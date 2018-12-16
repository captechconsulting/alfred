#!/usr/bin/python2
# title           : registry.py
# description     : This script is responsible for all the registry related functions.  This
#                   includes requesting metadata from the registry, sending instance
#                   registration to the registry, and parsing the returned json.
# author          : BAC
# usage           : called by watcher.py, copyToHDFS.py, refined.py, cleanupSpam.py, and validator.py
# notes           :
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
import base64

logger = logging.getLogger(__name__)
setup_logging(logger)


with open(os.path.dirname(os.path.realpath(__file__)) + "/config.yml", 'r') as ymlfile:
    cfg = yaml.load(ymlfile)

registry_URL = cfg['local']['registry_URL']
registry_port = str(cfg['local']['registry_port'])
data_root = cfg['hdfs']['data_root']

if 'registry_user' in cfg['local']:
    registry_user = cfg['local']['registry_user']
else :
    registry_user = None

if 'registry_PW' in cfg['local']:
    registry_PW = base64.b64decode(str(cfg['local']['registry_PW']))
else:
    registry_PW = None


def template_guid(metadata):
    tmp = metadata['file']['guid']
    return tmp


def file_path(metadata, **kwargs):
    stage = 'raw'
    if 'stage' in kwargs:
        stage = kwargs['stage']

    work_folder = ''
    if 'type' in kwargs:
        work_folder = '_work'

    if 'stage' in metadata and metadata['stage'] == 'sandbox':
        if data_root == "/":
            tmp = '/sandbox/' + metadata['eid'] + '/' + metadata['file']['technical']['tableName'] + work_folder
        elif data_root[0] != "/":
            tmp = hdfs.path.expanduser("~") + "/" + data_root + '/sandbox/' + metadata['eid'] + '/' + \
                  metadata['file']['technical']['tableName'] + work_folder
        else:
            tmp = data_root + '/sandbox/' + metadata['eid'] + '/' + metadata['file']['technical']['tableName'] + work_folder
    else:
        if data_root == "/":
            tmp = "/" + str(metadata["file"]["dataPartition"]) + "/" \
                  + metadata["file"]["subjectArea"] + "/" + stage + work_folder + '/' + metadata['file']['technical']['tableName']
        elif data_root[0] != "/":
            tmp = hdfs.path.expanduser("~") + "/" + data_root + "/" + str(metadata["file"]["dataPartition"]) + "/" \
                  + metadata["file"]["subjectArea"] + "/" + stage + work_folder + '/' + metadata['file']['technical']['tableName']
        else:
            tmp = data_root + "/" + str(metadata["file"]["dataPartition"]) + "/" \
                  + metadata["file"]["subjectArea"] + "/" + stage + work_folder + '/' + metadata['file']['technical']['tableName']
    logger.info("file path is " + tmp)
    return str(tmp).lower()


def db_name(metadata, **kwargs):
    stage = '_raw'
    env = 'dev_'
    if 'stage' in kwargs and kwargs['stage'] != 'raw':
        stage = ''  # kwargs['stage'] : removed stage for valid and refined from db name

    if 'environment' in cfg['local'] and cfg['local']['environment'] is not None and cfg['local']['environment'] != "":
        env = cfg['local']['environment'] + '_'

    if 'env' in kwargs:
        env = kwargs['env'] + '_'

    if 'stage' in metadata and metadata['stage'] == 'sandbox':
        if env == "prod_":
            tmp = metadata['eid']
        else:
            tmp = env + metadata['eid']
    else:
        if env == "prod_":
            tmp = str(metadata["file"]["dataPartition"]) + "_" + metadata["file"]["subjectArea"] + stage
        else:
            tmp = env + str(metadata["file"]["dataPartition"]) + "_" + metadata["file"]["subjectArea"] + stage
    logger.info('db name is ' + tmp)
    return str(tmp).lower()


def key_matched(metadata):
    tmp = metadata['file']['key']
    logger.info('file key is ' + tmp)
    return str(tmp).lower()


def db_table(metadata, **kwargs):
    if 'type' in kwargs and kwargs['type'] == 'work':
        tmp = metadata['file']['technical']['tableName'] + '_work'
    else:
        tmp = metadata['file']['technical']['tableName']
    logger.debug('db table is ' + tmp)
    return str(tmp).lower()


def dup_file_path(metadata=""):
    if data_root == "/":
        tmp = "/duplicate"
    else:
        tmp = data_root + "/duplicate"
    logger.debug('file path for duplicates is ' + tmp)
    return str(tmp).lower()


def spam_file_path(metadata=""):
    if data_root == "/":
        tmp = "/spam"
    else:
        tmp = data_root + "/spam"
    logger.info('file path for spam is ' + tmp)
    return str(tmp).lower()


def get_metadata(file_name, stage=''):
    """
        INPUT
        -----
        file_name : str
            The name of the file that information is being requested for

        OUTPUT
        ------
        the json response from the registry
        """
    request_string = registry_URL + ':' + registry_port + '/fileMetadata?name=' + file_name + '&stage=' + stage

    logger.info(' request string to get metadata : ' + request_string)
    try:
        if registry_user is not None:
            response = requests.get(request_string, auth=(registry_user,registry_PW)).json()
        else:
            response = requests.get(request_string).json()
    except requests.ConnectionError as e:
        # log response error
        logger.error('Failed to connect to Alfred : ' + str(e))
        raise e
    logger.info(response)
    return response

def get_refined_metadata(file_name):
    """
        INPUT
        -----
        file_name : str
            The name of the script that information is being requested for

        OUTPUT
        ------
        the json response from the registry
        """
    request_string = registry_URL + ':' + registry_port + '/refinedDatasets/' + file_name
    logger.info(' request string to get metadata : ' + request_string)
    try:
        if registry_user is not None:
            response = requests.get(request_string, auth=(registry_user, registry_PW)).json()
        else:
            response = requests.get(request_string).json()

    except requests.ConnectionError as e:
        # log response error
        logger.error('Failed to connect to Alfred : ' + str(e))
        raise e
    logger.info(response)
    return response


def raw_json(metadata, file_name, stage, row_count):
    post_json = {
                "rowCount": row_count,
                "path": file_path(metadata),
                "table": db_table(metadata),
                "templateGuid": template_guid(metadata),
                "stage": stage,
                "filename": file_name,
                "keyMatched": key_matched(metadata)
               }
    logger.info('json sent to register raw file : ' + str(post_json))
    return post_json


def spam_json(file_name, row_count):
    sp_json = {
        "rowCount": row_count,
        "path": "",
        "table": "None",
        "templateGuid": "deadbeef-cafe-face-1234-123456789abc",
        "stage": "spam",
        "filename": file_name,
        "keyMatched": "None"
    }
    logger.info('json sent to register spam file : ' + str(sp_json))
    return sp_json


def register_valid(metadata, instance_guid, file_name, row_count, validation_query):
    logger.info('instance guid : ' + str(instance_guid))
    request_string = registry_URL + ':' + registry_port + '/instanceLog/' + str(instance_guid)
    headers = {'content-type': 'application/json'}
    put_json = {
                "rowCount": row_count,
                "path": file_path(metadata),
                "table": db_table(metadata),
                "templateGuid": template_guid(metadata),
                "stage": "valid",
                "valid": True,
                "filename": file_name,
                "keyMatched": key_matched(metadata),
                "validQuery": validation_query
               }
    logger.info('json sent to register valid file : ' + str(put_json))

    try:
        if registry_user is not None:
            response = requests.put(request_string, auth=(registry_user, registry_PW),
                                     data=json.dumps(put_json), headers=headers)
        else:
            response = requests.put(request_string, data=json.dumps(put_json), headers=headers)

        if 'status' in response.json():
            logger.error('Invalid json sent for valid file registration')
            raise ValueError('Invalid JSON in valid file instance registration')
        else:
            return response.json()['guid']
    except requests.ConnectionError as e:
        # log response error
        logger.error('Failed to connect to Alfred : ' + str(e))
        raise e


def register_invalid(metadata, instance_guid, file_name, invalid_reasons, row_count, validation_query):
    logger.info('instance guide for invalid file : ' + str(instance_guid))
    request_string = registry_URL + ':' + registry_port + '/instanceLog/' + str(instance_guid)
    headers = {'content-type': 'application/json'}
    put_json = {
                "rowCount": row_count,
                "path": file_path(metadata),
                "table": db_table(metadata),
                "templateGuid": template_guid(metadata),
                "valid": "false",
                "reasons": invalid_reasons,
                "stage": "valid",
                "filename": file_name,
                "keyMatched": key_matched(metadata),
                "validQuery": validation_query
               }
    logger.info('json sent to register invalid file : ' + str(put_json))

    try:
        if registry_user is not None:
            response = requests.put(request_string, auth=(registry_user,registry_PW),
                                    data=json.dumps(put_json), headers=headers)
        else:
            response = requests.put(request_string, data=json.dumps(put_json), headers=headers)
        if 'status' in response.json():
            logger.error('Invalid json sent for invalid file registration')
            raise ValueError('Invalid JSON in invalid file instance registration')
        else:
            return response.json()['guid']
    except requests.ConnectionError as e:
        # log response error
        logger.error('Failed to connect to Alfred : ' + str(e))
        raise e


def register_raw(metadata, file_name, stage, row_count):
    request_string = registry_URL + ':' + registry_port + '/instanceLog'
    headers = {'content-type': 'application/json'}

    if stage == "spam":
        post_json = spam_json(file_name, row_count)
    else:
        post_json = raw_json(metadata, file_name, stage, row_count)

    try:
        # response = requests.post(request_string, data=post_json , headers=headers)
        logger.info(headers)
        logger.info(request_string)
        logger.info(post_json)

        if registry_user is not None:
            response = requests.post(request_string, auth=(registry_user,registry_PW),
                                     data=json.dumps(post_json), headers=headers)
        else:
            response = requests.post(request_string, data=json.dumps(post_json), headers=headers)
        if 'status' in response.json():
            logger.error('Invalid json sent for raw file registration')
            logger.error(response.json())
            raise ValueError('Invalid JSON in raw file instance registration')
        else:
            return response.json()['guid']
    except requests.ConnectionError as e:
        # log response error
        logger.error('Failed to connect to Alfred : ' + str(e))
        raise e


def validate_uuid(uuid_string):
    try:
        val = UUID(uuid_string, version=4)
    except ValueError:
        return False
    return True


