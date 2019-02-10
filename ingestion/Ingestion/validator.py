#!/usr/bin/python2
# title           : validator.py
# description     : validator is responsible for validating the data which is largely made up of
#                   Hive queries.  This is done by copying the data from the raw_work table into
#                   the valid_work table.  From there, a query comparing the raw data types to
#                   the cast data types is performed, looking for mismatches.  A row count is
#                   done on raw_work and valid_work to confirm that all data was copied.  If
#                   both of these checks confirm that the data is ok, the data is copied from
#                   valid_work to valid and the valid_work table is destroyed.
# author          : BAC
# usage           : called from copyToHDFS.py
# notes           :
# python_version  :2.7
# ==============================================================================

import os
import yaml
import impala.dbapi
from impala.dbapi import connect
import json
import sys
import pydoop.hdfs as hdfs
import time
import logging
from loglib import setup_logging
import registry as reg

# start the logging
logger = logging.getLogger(__name__)
setup_logging(logger)

# get the config file and set up variables
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


def escape_hive(field_name):
    """ We don't want to allow the use of hive reserved words.  If there is a field using that as a name we need to add something to it
     to make it different enough to not cause a conflict """ 
     # set up the list of reserved words
    reserved_words = ['ALL', 'ALTER', 'AND', 'ARRAY', 'AS', 'AUTHORIZATION', 'BETWEEN', 'BIGINT', 'BINARY', 'BOOLEAN',
                      'BOTH', 'BY', 'CASE', 'CAST', 'CHAR', 'COLUMN', 'CONF', 'CREATE', 'CROSS', 'CUBE', 'CURRENT',
                      'CURRENT_DATE', 'CURRENT_TIMESTAMP', 'CURSOR', 'DATABASE', 'DATE', 'DECIMAL', 'DELETE',
                      'DESCRIBE', 'DISTINCT', 'DOUBLE', 'DROP', 'ELSE', 'END', 'EXCHANGE', 'EXISTS', 'EXTENDED',
                      'EXTERNAL', 'FALSE', 'FETCH', 'FLOAT', 'FOLLOWING', 'FOR', 'FROM', 'FULL', 'FUNCTION', 'GRANT',
                      'GROUP', 'GROUPING', 'HAVING', 'IF', 'IMPORT', 'IN', 'INNER', 'INSERT', 'INT', 'INTERSECT',
                      'INTERVAL', 'INTO', 'IS', 'JOIN', 'LATERAL', 'LEFT', 'LESS', 'LIKE', 'LOCAL', 'MACRO', 'MAP',
                      'MORE', 'NONE', 'NOT', 'NULL', 'OF', 'ON', 'OR', 'ORDER', 'OUT', 'OUTER', 'OVER', 'PARTIALSCAN',
                      'PARTITION', 'PERCENT', 'PRECEDING', 'PRESERVE', 'PROCEDURE', 'RANGE', 'READS', 'REDUCE',
                      'REVOKE', 'RIGHT', 'ROLLUP', 'ROW', 'ROWS', 'SELECT', 'SET', 'SMALLINT', 'TABLE', 'TABLESAMPLE',
                      'THEN', 'TIMESTAMP', 'TO', 'TRANSFORM', 'TRIGGER', 'TRUE', 'TRUNCATE', 'UNBOUNDED', 'UNION',
                      'UNIQUEJOIN', 'UPDATE', 'USER', 'USING', 'UTC_TMESTAMP', 'VALUES', 'VARCHAR', 'WHEN', 'WHERE',
                      'FOREIGN', 'PRIMARY', 'REFERENCES', 'DAYOFWEEK', 'EXTRACT', 'FLOOR', 'INTEGER', 'PRECISION',
                      'VIEWS', 'TIME']
    if str(field_name).upper() in reserved_words:
        # if the field name is in the list add the word _field on the end
        return field_name + '_field'
    else:
        # otherwise just send the field name back
        return field_name


def field_conversion(field, alias=''):
    """
        Accepts a field name and an option alias.  
        The Alias is used to specificy which of a field is intended when doing joins etc.
    """
    # if the alias isn't empty, add a . to the end of it for alias.field purposes
    if alias != '':
        alias += '.'
    
    # casting for dates
    if field['datatype'].upper() == 'DATE':
        # cast(to_date(from_unixtime(unix_timestamp(LIFE_SUPPORT_EFFECTIVE_DT, 'MM/dd/yyyy'))) as date)
        tmp = "cast(to_date(from_unixtime(unix_timestamp(" + alias + field['name'] + ", '" + field['format'] +\
              "'))) as date)"
    # casting for timestamps
    elif field['datatype'].upper() == 'TIMESTAMP':
        # cast(from_unixtime(unix_timestamp(SCD_EFFECTIVE_TS, 'MM/dd/yyyy hh:mm:ss aa')) as timestamp)
        tmp = "cast(from_unixtime(unix_timestamp(" + alias + field['name'] + ", '" + field['format'] + \
              "')) as timestamp)"
    # everything else is not cast
    else:
        tmp = "cast(" + alias + field['name'] + " as " + field['datatype']
        # but if there's a precision field that gets added to the end ie  decimal(10,2)
        if 'precision' in field and field['precision'] != '':
            tmp += '(' + field['precision'] + ')'
        tmp += ")"
    return tmp


class Hive(object):
    """
        A hive object so the same connection can be used multiple times
    """
    
    # create connection and cursor 
    _conn = None
    _cur = None

    def __init__(self):
        # start the connection 
        self._conn = connect(host=hive_host,
                             port=10000,
                             auth_mechanism=hive_auth_mechanism,
                             user=hive_user,
                             password=hive_PW,
                             database='default',
                             kerberos_service_name=hive_kerberos_service_name)
        logging.info('Created db connection')

        # open the cursor
        self._cur = self._conn.cursor()
        logging.info('Created db cursor')

        # if there are any hive settings in the config.yml file, apply those with the "set" command
        if 'hive_settings' in cfg['hdfs'] and cfg['hdfs']['hive_settings'] is not None:
            for setting in cfg['hdfs']['hive_settings']:
                if setting != '':
                    self._cur.execute('set ' + setting)
        logging.info('Added hive settings')

        # if there are any jar files mentioned in the config.yml file, apply those with the "add jar" command
        if 'hive_jars' in cfg['hdfs'] and cfg['hdfs']['hive_jars'] is not None:
            for setting in cfg['hdfs']['hive_jars']:
                if setting != '':
                    self._cur.execute('add jar ' + setting)
        logging.info('Added hive jars')

    def __del__(self):
        # close the cursor and connection
        self._cur.close()
        self._conn.close()

    def _query(self, query):
        # execute a supplied query.  Should probably refactor this out
        try:
            logger.info(query)
            self._cur.execute(query)
            return self._cur.fetchall()
        except:
            pass

    def drop_table(self, metadata, **kwargs):
        # drops the provided table
        # get db and table name from the metadata
        db_name = reg.db_name(metadata, **kwargs)
        db_table = reg.db_table(metadata, **kwargs)

        # create the drop table query
        drop_table = 'drop table if exists ' + db_name + '.' + db_table
        logging.info('Dropping table for recreation ' + db_name + '.' + db_table)
        try:
            # execute drop table
            self._cur.execute(drop_table)
            logging.info('table dropped')
        except Exception as e:
            # log errors if they occur
            logging.error(e)

    def create_hive_table(self, metadata, **kwargs):
        logging.info('Create Hive Table')

        # set up the list for partition fields and other variables
        partition_list = []
        raw_table = work_table = sandbox = False
        
        if 'stage' in kwargs and kwargs['stage'] == 'sandbox':
            sandbox = True
        elif 'stage' not in kwargs or (kwargs['stage'] != 'valid' and kwargs['stage'] != 'refined'):
            raw_table = True

        if 'type' in kwargs and kwargs['type'] == 'work':
            work_table = True
        
        # check for a delimiter and set it up, or set it to nothing
        if 'technical' in metadata['file'] and 'fieldDelimiter' in metadata['file']['technical'] \
                and metadata['file']['technical']['fieldDelimiter'] is not None \
                and metadata['file']['technical']['fieldDelimiter'] != '':
            delimiter = metadata['file']['technical']['fieldDelimiter']
        else:
            delimiter = ''

        # get db name and table name from registry
        db_name = reg.db_name(metadata, **kwargs)
        db_table = reg.db_table(metadata, **kwargs)
        logging.info("Creating " + db_name + '.' + db_table)
        
        # create the DB if it doesn't exist
        self._cur.execute("create database if not exists " + db_name)
        
        # sort the fields in the metadata by order
        field_order = sorted(metadata['fields'], key=lambda k: k['position'])
        
        # start the table creation script
        logging.info('Begin table creation script')
        if not raw_table:
            table_create = 'CREATE '
        else:
            table_create = 'CREATE external '

        # add db and table to table create script
        table_create += 'TABLE if not exists ' + db_name + '.' + db_table + ' '
        
        # loop through the field list
        if len(field_order) > 0:
            # table with metadata
            field_order = sorted(metadata['fields'], key=lambda k: k['position'])

            table_create += ' ( '

            # build the field list and partition list for the create statement
            for field in field_order:  
                # raw tables are always string, unless they're boolean which we have to interpret as boolean
                if raw_table:
                    if str(field["datatype"]).upper() == 'BOOLEAN':
                        table_create += field["name"] + ' ' + field["datatype"] + ', '
                    else:
                        table_create += field["name"] + ' string, '
                elif 'partitionPosition' in field and not work_table and not sandbox:
                    # if its not raw, we may have partition fields, they go somewhere different in the query than the rest of the fileds
                    partition_list.append(field)
                #elif sandbox and str(field['datatype']).upper() in {'DATE', 'TIMESTAMP'}:
                    # sandboxes usually get the specified data types, but dates stay string
                #    table_create += field["name"] + ' string, '
                else:
                    # add in the precision on data types that have it ie, decimal and varchar
                    if "precision" in field and field["precision"] is not None and  field["precision"] != '':
                        table_create += field["name"] + ' ' + field["datatype"] + '(' + str(field["precision"]) + '), '
                    elif str(field["datatype"]).upper() == 'VARCHAR':
                        table_create += field["name"] + ' ' + field["datatype"] + '(65355), '
                    elif str(field["datatype"]).upper() == 'CHAR':
                        table_create += field["name"] + ' ' + field["datatype"] + '(255), '
                    else:
                        table_create += field["name"] + ' ' + field["datatype"] + ', '

            if 'stage' in kwargs and kwargs['stage'] == 'valid':
                # valid tables get the instance guid tacked on
                table_create += "instance_guid string)"
            elif len(partition_list) != len(field_order):
                # get rid of the trailing comma
                table_create = table_create[:-2] + ") "  
            else:
                # if it gets to this point, there are no fields in the table that aren't in the
                #  partition list.  This is bad
                sys.exit('No non-partition fields in table')
                table_create = table_create[:-2]   # get rid of the trailing comma

        # dealing with the header row from sandbox files
        elif 'technical' in metadata['file'] \
                and ('containsHeaderRow' in metadata['file']['technical'] and metadata['file']['technical']['containsHeaderRow']) \
                and ('fieldDelimiter' in metadata['file']['technical']
                     and metadata['file']['technical']['fieldDelimiter'] is not None
		     and metadata['file']['technical']['fieldDelimiter'] != ''):

            # table with field names from a header row and a delimiter
            fields = kwargs['header'].replace('.', '_').replace('"', '').split(delimiter.decode('string_escape'))
            table_create += ' ( '
            for field in fields:
                table_create += escape_hive(field) + ' string, '
            table_create = table_create[:-2] + ") "
        # dealing with delimited files that don't have field names
        elif 'technical' in metadata['file'] and ('containsHeaderRow' not in metadata['file']['technical'] or
                                                not metadata['file']['technical']['containsHeaderRow']):
            # no header row but there's a delimiter
            table_create += ' ( '
            if delimiter is not None and len(delimiter) > 0:
                fields = kwargs['header'].split(delimiter)
                for n in range(len(fields)):
                    table_create += ' column' + str(n + 1) + ' string, '
            else:
                fields = kwargs['header']
                table_create += ' column1 string, '

            table_create = table_create[:-2] + ") "
        else:  # no header row or delimiter
            table_create += '(column1 string) '

        logging.info('field list done, moving to partitioning')

        # if the partition list has anything in it, add partitioning to the table create
        if len(partition_list) > 0:
            partition = 'PARTITIONED BY ('
            partition_list.sort(key=lambda k: k['partitionPosition'])

            for field in partition_list:
                if "precision" in field and field["precision"] is not None:
                    partition += field["name"] + ' ' + field["datatype"] + '(' + str(field["precision"]) + '), '
                else:

                    partition += field["name"] + ' ' + field["datatype"] + ', '

            partition = partition[:-2] + ") "
            table_create += partition

        if delimiter == ';':
            delimiter = '\u0059'

        logging.info('partitioning done, moving to delimiting')
        
        # add in the delimiter, if there's a mulit-character delimiter, include the neccesary serde
        if 'technical' in metadata['file'] and 'fieldDelimiter' in metadata['file']['technical'] \
                and metadata['file']['technical']['fieldDelimiter'] is not None and len(delimiter) > 0:
            if len(delimiter) == 1 and (raw_table or sandbox):
                logging.info('single character delimiter ' + delimiter)
                table_create += 'ROW FORMAT DELIMITED FIELDS TERMINATED BY "' + delimiter + '" '
                # table_create += 'ROW FORMAT DELIMITED'
            elif raw_table or sandbox:
                logging.info('multi character delimiter ' + delimiter)
                table_create += 'ROW FORMAT SERDE "org.apache.hadoop.hive.contrib.serde2.MultiDelimitSerDe" ' + \
                    'WITH SERDEPROPERTIES ("field.delim"="' + delimiter + '") '
        else:
                logging.info(' no delimiter ')

        logging.info('delimiting done, moving to storage type')

        # add storage type, raw and sandbox is textfile, anything else attempts to use the compression mentioned in the registry
        if raw_table or sandbox:
            logging.info('STORED AS TEXTFILE ')
            table_create += 'STORED AS TEXTFILE '
        else:
            logging.info('STORED AS compression ')
            if 'compression' in metadata['file']['technical'] and metadata['file']['technical']['compression'] != '' \
                    and metadata['file']['technical']['compression'] is not None:
                table_create += 'STORED AS  ' + metadata['file']['technical']['compression'] + ' '
            else:
                table_create += 'STORED AS parquet '

        logging.info('storage type done, moving to location')

        # add storage location for everything except the valid_work table
        if raw_table or not work_table:
            logging.info('location ')
            table_create += 'LOCATION \'' + reg.file_path(metadata, **kwargs) + '/\' '

        logging.info('location done, moving to header row')

        # set the flag to skip the header row on raw and sandbox files if neccesary
        if (raw_table or sandbox) and 'technical' in metadata['file'] and 'containsHeaderRow' in metadata['file']['technical'] \
                and metadata['file']['technical']['containsHeaderRow']:
            table_create += 'tblproperties("skip.header.line.count" = "1")'

        # allow for resettting/deleting the table
        if "reset" in kwargs and kwargs["reset"]:
            logger.info('dropping  ' + db_name + '.' + db_table)
            self._cur.execute('drop table if exists ' + db_name + '.' + db_table)
        logger.info('table creation script : ' + table_create)
        
        # execute the table creation
        try:
            self._cur.execute(table_create)
            logging.info('Table ' + db_name + '.' + db_table + 'created')
        except Exception as e:
            logging.error(e)

    def copy_table_data(self, metadata, instance_guid, **kwargs):
        tgt_db_name = reg.db_name(metadata, stage="valid")
        work_copy = False
        if 'copy_type' in kwargs and kwargs['copy_type'] == 'compare':
            work_copy = True
            src_db_name = reg.db_name(metadata)
            src_db_table = reg.db_table(metadata, type='work')
            tgt_db_table = reg.db_table(metadata, type='work')
        else:
            src_db_name = reg.db_name(metadata, stage="valid")
            src_db_table = reg.db_table(metadata, type='work', stage="valid")
            tgt_db_table = reg.db_table(metadata)

        if 'update_type' in kwargs:
            update_type = kwargs['update_type']
        else :
            update_type = 'append'

        field_order = sorted(metadata['fields'], key=lambda k: k['position'])
        select_list = partition = partition_conversion = ''
        partition_list = []

        if 'copy_type' in kwargs and kwargs['copy_type'] == 'compare':
            work_copy = True

        for field in field_order:  # build the field list for the create statement
            if 'partitionPosition' in field and not work_copy:
                partition_list.append(field)
            else:
                # select_list += field["name"] + ', '
                select_list += field_conversion(field) + ', '

        if update_type == 'delta':
            insert_statement = 'INSERT OVERWRITE TABLE ' + tgt_db_name + '.' + tgt_db_table
        else:
            insert_statement = 'INSERT INTO TABLE ' + tgt_db_name + '.' + tgt_db_table

        if len(partition_list) > 0:
            partition_list.sort(key=lambda k: k['partitionPosition'])
            for field in partition_list:
                partition += field["name"] + ', '
                partition_conversion += field_conversion(field) + ', '

            insert_statement += ' PARTITION (' + partition[:-2] + ')'

            if 'valid_copy' in kwargs and kwargs['valid_copy']:
                insert_statement += ' SELECT ' + select_list + ' instance_guid, ' + \
                                partition_conversion[:-2] + ' FROM ' + src_db_name + '.' + src_db_table
            else:
                insert_statement += ' SELECT ' + select_list + '"' + str(instance_guid) + '", ' + \
                                    partition_conversion[:-2] + ' FROM ' + src_db_name + '.' + src_db_table
        else:
            if 'valid_copy' in kwargs and kwargs['valid_copy']:
                insert_statement += ' SELECT ' + select_list + ' instance_guid FROM ' + src_db_name + '.' + src_db_table
            else:
                insert_statement += ' SELECT ' + select_list + '"' + str(instance_guid) + \
                                    '" FROM ' + src_db_name + '.' + src_db_table
        logger.info('insert statement : ' + insert_statement)

        try:
            self._cur.execute(insert_statement)
        except RuntimeError as e:
            logger.error("Metadata didn't match file" + e.message)
            raise
        except impala.dbapi.OperationalError as e:
            logger.error("Metadata didn't match file" + e.message)
            raise

    def copy_and_compare(self, metadata, instance_guid, file_name):

        src_db_name = reg.db_name(metadata)
        src_db_table = reg.db_table(metadata, type='work')
        work_db_name = reg.db_name(metadata, stage="valid", type='work')
        work_db_table = reg.db_table(metadata, stage="valid", type='work')
        invalid_reason = {}

        if 'fileUpdateType' in metadata['file']['technical']:
            update_type = metadata['file']['technical']['fileUpdateType']
        else:
            update_type = 'append'

        field_order = sorted(metadata['fields'], key=lambda k: k['position'])
        select_list = partition = ''
        partition_list = []
        for field in field_order:  # build the field list for the create statement
            if 'partitionPosition' in field:
                partition_list.append(field)
            else:
                # select_list += field["name"] + ', '
                select_list += field_conversion(field) + ', '

        now = time.time()
        logger.info('Comparison start')

        # dropping work table if it exists
        self._query('drop table if exists ' + work_db_name + '.' + work_db_table)

        self.create_hive_table(metadata, stage="valid", type='work')
        try:
            self.copy_table_data(metadata, instance_guid, copy_type='compare')
        except RuntimeError:
            invalid_reason["badMetadata"] = "Metadata didn't match file and caused hive to fail, check ingestion logs"
        except impala.dbapi.OperationalError:
            invalid_reason["badMetadata"] = "Metadata didn't match file and caused hive to fail, check ingestion logs"

        logger.info('Data loaded to validation table')

        compare = ('SELECT count(*) FROM ' + src_db_name + '.' + src_db_table + ' where ')
        for field in metadata['fields']:
            if str(field['datatype']).upper() in {'TIMESTAMP','DATE'}:
                compare += '((' + field["name"] + ' is not null and length(' + field["name"] + ') > 0) and ' +\
                           field_conversion(field) + ' is null) or '

            elif str(field['datatype']).upper() in {'FLOAT'}:
                compare += '((' + field["name"] + ' is not null  and length(' + field["name"] + ') > 0) and  not(' +\
                           field_conversion(field) + ' <=> cast(' + field["name"] + ' as float))) or '

            elif str(field['datatype']).upper() == 'BOOLEAN':
                compare += '( not(' + field_conversion(field) + ' <=> ' + field["name"] + ')) or '


            elif str(field['datatype']).upper() == 'BINARY':
                pass

            else:
                compare += '((' + field["name"] + ' is not null  and length(' + field["name"] + ') > 0) and  not(' +\
                           field_conversion(field) + ' <=> ' + field["name"] + ')) or '
        compare = compare[:-3]

        logger.info('comparison query : ' + compare)

        raw_rows = valid_rows = invalid_rows = 0
        try:
            self._cur.execute(compare)
        except Exception as e:
            logger.info(e.message)
            raise

        logger.info('Valid data check query complete')
        # get the number of invalid rows from the comparison query
        for row in self._cur:
            invalid_rows = row[0]

        if invalid_rows > 0:
            logger.info(str(invalid_rows) + " invalid rows ")
            invalid_reason["datatypeMismatch"] = invalid_rows
        else:
            logger.info("All copied rows are valid")

        raw_rows = self.row_count(src_db_name + '.' + src_db_table)
        logger.info('Raw row count complete')

        valid_rows = self.row_count(work_db_name + '.' + work_db_table, 'instance_guid = "' + str(instance_guid) + '"')
        logger.info('Valid row count complete')

        logger.info("Raw rows = " + str(raw_rows) + ": Valid Rows = " + str(valid_rows))
        if raw_rows - valid_rows != 0:
            logger.info("Mismatch count = " + str(raw_rows - valid_rows))
            invalid_reason["rowCountMismatch"] = raw_rows - valid_rows

        logger.info("End copy and compare" + str(time.time()))
        logger.info("finished in " + str(time.time() - now) + " seconds")

        if len(invalid_reason) > 0:
            reg.register_invalid(metadata, instance_guid, file_name, invalid_reason, valid_rows, compare)
        else:

            # Adding append vs full file logic
            if 'fileUpdateType' in metadata['file']['technical']:

                if update_type == 'append':
                    # this is the default path so we don't do anything
                    logging.info('append file')
                    pass
                elif update_type == 'full':
                    # delete everything in the valid file location
                    logger.info('Deleting existing data from valid table')
                    if hdfs.path.exists(reg.file_path(metadata, stage="valid")):
                        hdfs.rmr(reg.file_path(metadata, stage="valid"))
                elif update_type == 'delta':
                    logging.info('delta file')
                    self.delta(metadata)
                else:
                    logging.info('update type blank, treating as append file')
            else:
                logging.info('no update type or update type null, treating as append file')

            self.create_hive_table(metadata, stage="valid")
            try:
                self.copy_table_data(metadata, instance_guid, valid_copy=True, update_type=update_type)
                reg.register_valid(metadata, instance_guid, file_name, valid_rows, compare)
                self._query('drop table if exists ' + work_db_name + '.' + work_db_table)
                if 'fileUpdateType' in metadata['file']['technical'] and metadata['file']['technical']['fileUpdateType'] == 'full':
                    logger.info('Deleting existing data from raw table')
                    if hdfs.path.exists(reg.file_path(metadata, stage="raw")):
                        hdfs.rmr(reg.file_path(metadata, stage="raw"))
                    hdfs.cp(reg.file_path(metadata, stage="raw", type='work'), reg.file_path(metadata, stage="raw"))

            except RuntimeError or impala.dbapi.OperationalError:
                invalid_reason["badMetadata"] = "Metadata didn't match file and " + \
                        "caused hive to fail, check ingestion logs"
                reg.register_invalid(metadata, instance_guid, file_name, invalid_reason, valid_rows, compare)

    def delta(self, metadata):
        # Adding rows that were in the main file, but not in delta into the work tableff
        logging.info('Adding rows that were not new or edited to work table')
        src_db_name = reg.db_name(metadata, stage="valid")
        src_db_table = reg.db_table(metadata, stage="valid")
        tgt_db_name = reg.db_name(metadata, stage="valid")
        tgt_db_table = reg.db_table(metadata, type='work')

        join = select_list = ""
        partition_list = []

        logging.debug('starting field list in delta')
        for field in metadata['fields']:
            if 'pk' in field and str(field['pk']).lower() == 'true':
                join += "valid." + field["name"] + " = delta." + field["name"] + " and "

            if 'partitionPosition' in field and field['partitionPosition'] == 1:
                partition_list.append(field)

        logging.debug('starting insert_statement in delta')
        insert_statement = 'INSERT INTO TABLE ' + tgt_db_name + '.' + tgt_db_table

        logging.debug('starting field_order in delta')
        field_order = sorted(metadata['fields'], key=lambda k: k['position'])

        for field in field_order:
            select_list += field_conversion(field) + ', '

        insert_statement += ' SELECT ' + select_list + 'instance_guid ' + \
                            ' FROM ' + src_db_name + '.' + src_db_table + ' valid '
        insert_statement += 'where not exists (select 1 from ' + tgt_db_name + '.' + tgt_db_table + ' delta where '
        insert_statement += join

        if len(partition_list) > 0:
            partition_query = 'select ' + partition_list[0]["name"] + ' from ' + tgt_db_name + '.' + tgt_db_table
            partition_query += ' group by ' + partition_list[0]["name"]

            logging.info('Partition list query : ' + partition_query )
            compare_values = self._query(partition_query)

            if compare_values is not None and  len(compare_values) > 0:
                existing_partitions = ''
                logging.info('Found ' + str(len(compare_values)) + ' values')
                for row in compare_values:
                    logging.info(str(row))
                    if str(partition_list[0]['datatype']).upper() in ('DECIMAL', 'FLOAT', 'INTEGER', 'DOUBLE', 'BIGINT','SMALLINT', 'TINYINT'):
                        existing_partitions += str(row[0]) + ', '
                    elif str(partition_list[0]['datatype']).upper() in ("TIMESTAMP"):
                        existing_partitions += "cast('" + str(row[0]) + "' as timestamp), "
                    elif str(partition_list[0]['datatype']).upper() in ("DATE"):
                        existing_partitions += "cast('" + str(row[0]) + "' as date), "
                    else:
                        existing_partitions += "'" + str(row[0]) + "', "
                insert_statement += partition_list[0]["name"] + ' in  (' + existing_partitions[:-2] + ')) '
            else:
                insert_statement = insert_statement[:-5] + ' )'
        else:
            insert_statement = insert_statement[:-5] + ' )'
        logger.info("delta insert to work : " + insert_statement)

        try:
            self._cur.execute(insert_statement)
        except RuntimeError as e:
            logger.error("delta update failed" + e.message)
            raise

    def row_count(self, table_name, where=""):
        if where != '':
            query = 'select count(*) from ' + table_name + ' where ' + where
        else:
            query = 'select count(*) from ' + table_name
        logger.info(query)
        self._cur.execute(query)
        tmp = self._cur.fetchone()[0]
        return int(tmp)


def main(file_name, instance_guid, metadata, **kwargs):
    # metadata = reg.get_metadata(file_name)

    if metadata['file'] is None:
        exit()

    hive = Hive()
    hive.create_hive_table(metadata, **kwargs)  # create raw table

    if 'stage' in metadata and metadata['stage'] != 'sandbox':
        hive.create_hive_table(metadata, reset=True, type="work")  # create work table for raw

        hive.create_hive_table(metadata, stage="valid", instance_guid=instance_guid)

        hive.copy_and_compare(metadata, instance_guid, file_name)

