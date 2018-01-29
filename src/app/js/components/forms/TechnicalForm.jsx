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
import { Checkbox, Form, Text, Select, Textarea, FormError} from 'react-form';

// import validation consts
import { technicalFormDefault } from '../../../../contents/js/constants/validation'

/*
* A form component that renders the technical information.
* used in: Register & Refined Template
*/
export default class TechnicalForm extends React.Component {
    constructor(props) {
    super(props);
  }
    render() {
        let continueTxt = (this.props.editForm) ? 'Save & Continue' : 'Continue';
    return (
            <Form
                    onSubmit={(values) => {
                  this.props.handleAdvanceStep();
                }}

                onChange={(state, props, initial, instance) => {
                    this.props.handleOnChange([state.values, 'technicalData']);
                }}

                  // Default form values if they exist in state.
                  defaultValues={{
                    format: this.props.formData.format ? this.props.formData.format : null,
                    rowFormat: this.props.formData.rowFormat ? this.props.formData.rowFormat : null,
                    fieldDelimiter: this.props.formData.fieldDelimiter ? this.props.formData.fieldDelimiter : undefined,
                    lineTerminator: this.props.formData.lineTerminator ? this.props.formData.lineTerminator : undefined,
                    fileUpdateType: this.props.formData.fileUpdateType ? this.props.formData.fileUpdateType : null,
                    tableName: this.props.formData.tableName ? this.props.formData.tableName : undefined,
                    containsHeaderRow: this.props.formData.containsHeaderRow ? this.props.formData.containsHeaderRow : undefined,
                    ignoreNamespace: this.props.formData.ignoreNamespace ? this.props.formData.ignoreNamespace : false
                  }}

                  // validate life-cycle
                  validate={values => {
                      const { format, rowFormat, lineTerminator } = values
                      if (this.props.sandboxForm) {
                          return {
                            format: !format ? 'Please select an option' : null
                          }
                      } else if (format === "tabular") {
                          return {
                            format: !format ? 'Please select an option' : null,
                            rowFormat: !rowFormat ? 'Please select a row format' : null,
                            lineTerminator: !lineTerminator ? 'Line Terminator is required' : undefined
                          }
                      } else if ((format === "xml") || (format === "json")) {
                          return {
                            format: !format ? 'Please select an option' : null
                          }
                      }
                    }}

                  // `onValidationFail` is another handy form life-cycle method
                  onValidationFail={() => {
                      console.log('Form Validation failed.')
                  }}
                >
                  {({ values, submitForm, addValue, removeValue, getError }) => {
                    // This is a stateless component, but you can use any valid react component to render your form.
                    // Forms also supply plenty of useful props for your components to utilize. See the docs for a complete list.
                    return (
                      // When the form is submitted, call the `sumbitForm` callback prop
                        <form onSubmit={submitForm}>
                          <div className="form-group">
                            <h6>Format</h6>
                            <Select
                                className="form-control"
                              field='format'
                              options={[
                                  {
                                    label: 'Tabular',
                                    value: 'tabular'
                                  }
                                  ,
                                  {
                                      label: 'XML',
                                      value: 'xml'
                                  },
                                  {
                                      label: 'JSON',
                                      value: 'json'
                                  }
                                 ]}
                              disabled={this.props.disabled}
                            />
                          </div>
                          {
                              (this.props.formData && (this.props.formData.format === 'tabular')) && 
                              <div>
                                  <div className="form-group">
                                    <h6>Row Format</h6>
                                    <Select
                                        className="form-control"
                                      field='rowFormat'
                                      options={[{
                                        label: 'Delimited',
                                        value: 'delimited'
                                      }]}
                                      disabled={this.props.disabled}
                                    />
                                  </div>
                                  {
                                      (this.props.formData.rowFormat === 'delimited') &&
                                      <div className="form-group">
                                          <h6>Field Delimiter</h6>
                                        <Text
                                          field='fieldDelimiter'
                                          placeholder='e.g. ;'
                                          className="form-control"
                                          disabled={this.props.disabled} />
                                      </div>
                                  }
                                  <div className="form-group">
                                      <h6>Line Terminator</h6>
                                    <Text
                                      field='lineTerminator'
                                      placeholder='e.g. \n'
                                      className="form-control"
                                      disabled={this.props.disabled} />
                                  </div>
                                  {
                                      (!this.props.sandboxForm) &&
                                      <div className="form-group">
                                        <h6>File Update Type</h6>
                                        <Select
                                            className="form-control"
                                          field='fileUpdateType'
                                          options={[{
                                            label: 'Full',
                                            value: 'full'
                                          },
                                          {
                                              label: 'Delta',
                                            value: 'delta'
                                          },
                                          {
                                              label: 'Append',
                                            value: 'append'
                                          }]}
                                          disabled={this.props.disabled}
                                        />
                                      </div>
                                    }
                                  <div className="form-group">
                                      <h6>Table Name</h6>
                                    <Text
                                      field='tableName'
                                      placeholder='e.g. Household_Electric_Power_Consumption'
                                      className="form-control"
                                      disabled={this.props.disabled} />
                                  </div>
                                  <div className="checkbox-group">
                                      <span>Column Header:</span>
                                <Checkbox 
                                  field='containsHeaderRow'
                                  disabled={this.props.disabled}
                                />
                                  </div>
                              </div>
                          }
                          {
                              (this.props.formData && ((this.props.formData.format === 'json') || (this.props.formData.format === 'xml'))) && 
                              <div>
                                  <div className="form-group">
                                      <h6>Table Name</h6>
                                    <Text
                                      field='tableName'
                                      placeholder='e.g. Household_Electric_Power_Consumption'
                                      className="form-control"
                                      disabled={this.props.disabled} />
                                  </div>
                              </div>
                          }
                          <div className="form-button-group">
                                        <button className="btn btn-default" onClick={this.props.handleCloseModal}>Exit</button>
                                        {/*<button className="btn btn-default" onClick={this.handleSaveTemplate.bind(this)}>Save</button>*/}
                                  <button className="btn btn-default">{continueTxt}</button>
                              </div>
                        </form>
                    )
                  }}
                </Form>
        );
  }
}

TechnicalForm.propTypes = {
    formData: PropTypes.object,
    handleCloseModal: PropTypes.func.isRequired,
    handleOnChange: PropTypes.func.isRequired,
    handleAdvanceStep: PropTypes.func.isRequired,
    disabled: PropTypes.bool.isRequired,
    sandboxForm: PropTypes.bool
}
