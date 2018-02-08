/*
 * Copyright 2018 CapTech Ventures, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';
import React from 'react';
import PropTypes from 'prop-types';
import { Checkbox, Form, Text, Textarea, Select } from 'react-form';

// import validation consts
import { fieldValidationAlt } from '../../../../contents/js/constants/validation'

/*
* A form component that renders the fields information.
* Used in: Register & Refined Template
*/
export default class FieldForm extends React.Component {
    constructor(props) {
    super(props);
  }
  onAddClick(values) {
      this.props.handleAddField(values);
  }
  onSaveField(values) {
      this.props.handleEditField(values);
  }
    render() {
    return (
            <Form    
                // Default form values if they exist in props.
              defaultValues={{
                name: (this.props.fieldData && this.props.fieldData.name) ? this.props.fieldData.name : undefined,
              businessName: (this.props.fieldData && this.props.fieldData.businessName) ? this.props.fieldData.businessName : undefined,
              description: (this.props.fieldData && this.props.fieldData.description) ? this.props.fieldData.description : undefined,
              datatype: (this.props.fieldData && this.props.fieldData.datatype) ? this.props.fieldData.datatype : null,
              format: (this.props.fieldData && this.props.fieldData.format) ? this.props.fieldData.format : undefined,
              nullable: (this.props.fieldData && this.props.fieldData.nullable) ? this.props.fieldData.nullable : null,
              pk: (this.props.fieldData && this.props.fieldData.pk) ? this.props.fieldData.pk : null,
              precision: (this.props.fieldData && this.props.fieldData.precision) ? this.props.fieldData.precision : undefined,
              position: (this.props.fieldData && this.props.fieldData.position) ? this.props.fieldData.position : null,
              sourceXpath: (this.props.fieldData && this.props.fieldData.sourceXpath) ? this.props.fieldData.sourceXpath : null,
              parent: (this.props.fieldData && this.props.fieldData.parent) ? this.props.fieldData.parent : null
              }}

              // on submit life-cycle
                onSubmit={(values) => {
                    for (let attr in values) {
                        if (values.hasOwnProperty(attr)) {
                            if (typeof(values[attr]) === 'string') {
                                if (values[attr].length === 0) {
                                    values[attr] = null;
                                }
                            }
                        }
                    }
                    if(!this.props.editMode) {
                        this.onAddClick(values);
                    } else {
                        this.onSaveField(values);
                    }
                }}
    
                // after submit life-cycle
            postSubmit={(values) => {
                console.log('post submit');
              }}
    
              // validate life-cycle
              validate={ fieldValidationAlt }

              // `onValidationFail` is another handy form life-cycle method
              onValidationFail={(values) => {
                  console.log('Form Validation failed.')
              }}
            >
              {({ values, submitForm, addValue, removeValue, getError, resetForm }) => {
                return (
                    <form onSubmit={submitForm} className="new-field-form">
                      <div className="form-group">
                          <h6>Field Name</h6>
                        <Text
                          field='name'
                          placeholder='e.g. date_field'
                          className="form-control"
                          disabled={this.props.disabled} />
                      </div>
                      <div className="form-group">
                          <h6>Field Business Name</h6>
                        <Text
                          field='businessName'
                          placeholder='e.g. Date'
                          className="form-control"
                          disabled={this.props.disabled} />
                      </div>
                      <div className="form-group">
                          <h6>Business Description</h6>
                      <Textarea
                          field='description'
                          placeholder='e.g. Date in format dd/mm/yyyy'
                          className="form-control"
                          disabled={this.props.disabled} />
                      </div>
                      <div className="form-group">
                        <h6>Field Data Type</h6>
                        <Select
                            className="form-control"
                          field='datatype'
                          options={[
                              {
                                label: 'STRING',
                                value: 'string'
                              },
                              {
                                label: 'INT',
                                value: 'int'
                              },
                              {
                                label: 'TINYINT',
                                value: 'tinyint'
                              },
                              {
                                label: 'SMALLINT',
                                value: 'smallint'
                              },
                              {
                                label: 'BIGINT',
                                value: 'bigint'
                              },
                              {
                                label: 'FLOAT',
                                value: 'float'
                              },
                              {
                                label: 'DOUBLE',
                                value: 'double'
                              },
                              {
                                label: 'DECIMAL',
                                value: 'decimal'
                              },
                              {
                                label: 'TIMESTAMP',
                                value: 'timestamp'
                              },
                              {
                                label: 'DATE',
                                value: 'date'
                              },
                              {
                                label: 'VARCHAR',
                                value: 'varchar'
                              },
                              {
                                label: 'CHAR',
                                value: 'char'
                              },
                              {
                                label: 'BOOLEAN',
                                value: 'boolean'
                              },
                              {
                                label: 'BINARY',
                                value: 'binary'
                              },
                              {
                                label: 'ARRAY',
                                value: 'array'
                              },
                              {
                                label: 'MAP',
                                value: 'map'
                              },
                              {
                                label: 'STRUCT',
                                value: 'struct'
                              },
                              {
                                label: 'UNIONTYPE',
                                value: 'uniontype'
                              }
                          ]}
                          disabled={this.props.disabled}
                        />
                      </div>
                      <div className="form-group">
                          <h6>Format</h6>
                        <Text
                          field='format'
                          placeholder='e.g. dd/mm/yy'
                          className="form-control"
                          disabled={this.props.disabled} />
                      </div>
                      <div className="checkbox-group">
                          <span>Nullable:</span>
                    <Checkbox 
                      field='nullable'
                      disabled={this.props.disabled}
                    />
                    <span>PK:</span>
                    <Checkbox 
                      field='pk'
                      disabled={this.props.disabled}
                    />
                      </div>
                      <div className="form-group">
                          <h6>Precision</h6>
                        <Text
                          field='precision'
                          placeholder='e.g. 8,3'
                          className="form-control"
                          disabled={this.props.disabled} />
                      </div>
                      { // Change buttons depending if a user is editing or view specific fields
                          (!this.props.disabled) &&
                          ((!this.props.editMode) ?
                          <div>
                              <button className="btn btn-default">Add Field</button>
                          </div> :
                          <div className="form-button-group">
                              <button className="btn btn-default" onClick={this.props.resetForm}>Cancel</button>
                              <button className="btn btn-default" >Save Field</button>
                          </div>)
                        }
                    </form>
                )
              }}
            </Form>
        );
  }
}

FieldForm.propTypes = {
    editMode: PropTypes.bool.isRequired,
    fieldData: PropTypes.object,
    handleAddField: PropTypes.func,
    handleEditField: PropTypes.func,
    resetForm: PropTypes.func,
    disabled: PropTypes.bool.isRequired
}
