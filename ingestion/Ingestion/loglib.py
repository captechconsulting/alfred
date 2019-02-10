#!/usr/bin/python2
#title           :
#description     :
#author          : BAC
#usage           :
#notes           :
#python_version  :2.7
#==============================================================================

import os
import logging.config
import sys
import yaml
from os import path

def handle_exception(exc_type, exc_value, exc_traceback):
    if issubclass(exc_type, KeyboardInterrupt):
        sys.__excepthook__(exc_type, exc_value, exc_traceback)
        return

    logging.error("Uncaught exception", exc_info=(exc_type, exc_value, exc_traceback))


def setup_logging(logger,
    default_path=path.dirname(path.realpath(__file__)) + '/logging.yml',
    default_level=logging.INFO,
    env_key='LOG_CFG'
):
    """Setup logging configuration

    """
    path = default_path
    value = os.getenv(env_key, None)
    if value:
        path = value
    if os.path.exists(path):
        with open(path, 'rt') as f:
            config = yaml.safe_load(f.read())
        logging.config.dictConfig(config)

    else:
        logging.basicConfig(level=default_level)

    sys.excepthook = handle_exception

