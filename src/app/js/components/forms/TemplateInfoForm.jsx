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
import { Form, Text, Select, Textarea, FormError} from 'react-form';

// import validation consts
import { templateInfoDefault, templateInfoSandbox } from '../../../../contents/js/constants/validation'

/*
* A form component that renders the default template information.
* used in: Register & Refined Template
*/
export default class TemplateInfoForm extends React.Component {
	constructor(props) {
    super(props);
  }
	render() {
		let continueTxt = (this.props.editForm) ? 'Save & Continue' : 'Continue';
    return (
			<Form
	        onSubmit={(values) => {
	        	this.props.handleOnChange([values, 'defaultData']);
			      this.props.handleAdvanceStep();
			    }}

				  // Default form values if they exist in state.
				  defaultValues={{
				    key: this.props.formData.key ? this.props.formData.key : undefined,
				    businessName: this.props.formData.businessName ? this.props.formData.businessName : undefined,
			      dataSteward: this.props.formData.dataSteward ? this.props.formData.dataSteward : undefined,
			      owner: this.props.formData.owner ? this.props.formData.owner : undefined,
			      subjectArea: this.props.formData.subjectArea ? this.props.formData.subjectArea : undefined,
			      description: this.props.formData.description ? this.props.formData.description : undefined,
			      codeOfConduct: this.props.formData.codeOfConduct ? this.props.formData.codeOfConduct : null,
			      dataPartition: this.props.formData.dataPartition ? this.props.formData.dataPartition : undefined
				  }}

				  // When validating fields occurs
				  validate={values => {
					  const { key, dataSteward, owner, subjectArea, dataPartition } = values
					  if (this.props.sandboxForm) {
					  	return {
						    key: !key ? 'Key is required' : undefined,
						    dataPartition: !dataPartition ? 'Data Partition is required' : undefined
						  }
					  } else {
					  	return {
						    key: !key ? 'Key is required' : undefined,
						    dataSteward: !dataSteward ? 'Data Steward is required' : undefined,
						    owner: !owner ? 'Owner is required' : undefined,
						    subjectArea: !subjectArea ? 'Subject Area is required' : undefined,
						    dataPartition: !dataPartition ? 'Data Partition is required' : undefined
						  }
					  }
					}}

				  // When validation error occurs
				  onValidationFail={() => {
				  	console.log('Form Validation failed.')
				  }}
				>
				  {({ values, submitForm, addValue, removeValue, getError, getValue }) => {
				    // This is a stateless component, but you can use any valid react component to render your form.
				    // Forms also supply plenty of useful props for your components to utilize. See the docs for a complete list.
				    return (
				      // When the form is submitted, call the `sumbitForm` callback prop
				        <form onSubmit={submitForm}>
				          <div className="form-group">
				          	<h6>File Name Pattern (Key)</h6>
				            <Text // This is the built-in Text formInput
				              field='key' // field is a string version of the field location
				              placeholder='e.g. householdElectricPowerConsumption'
				              className="form-control"
				              disabled={ this.props.editForm || this.props.disabled } />
				          </div>
				          <div className="form-group">
				          	<h6>Business Name</h6>
				            <Text
				              field='businessName'
				              placeholder='e.g. Household Electric Power Consumption' 
				              className="form-control"
				              disabled={this.props.disabled} />
				          </div>
				          <div className="form-group">
				          	<h6>Data Steward</h6>
				            <Text
				              field='dataSteward'
				              placeholder='e.g. Agent Smith' 
				              className="form-control"
				              disabled={this.props.disabled}
				              maxLength="7" />
				          </div>
				          <div className="form-group">
				          	<h6>Data Owner</h6>
				            <Text
				              field='owner'
				              placeholder='e.g. Mr. Anderson'
				              className="form-control"
				              disabled={this.props.disabled}
				              maxLength="7" />
				          </div>
				          <div className="form-group">
				          	<h6>Source</h6>
				            <Text
				              field='subjectArea'
				              placeholder='e.g. UCI'
				              className="form-control"
				              disabled={this.props.disabled} />
				          </div>
				          <div className="form-group">
				          	<h6>Business Description</h6>
				            <Textarea // This is the built-in Textarea formInput
				              field='description'
				              placeholder='Business Description'
				              className="form-control"
				              rows="3"
				              disabled={this.props.disabled} />
				          </div>
				          {/*
										Replcae existing Code of Conduct question with custom input component or react form dropdown
				          */}
				          <div className="form-group">
				          	<h6>Data Partition</h6>
				            <Text
				              field='dataPartition'
				              placeholder='e.g. Household Electric Power Consumption' 
				              className="form-control"
				              disabled={this.props.disabled} />
				          </div>
				          {//Deprecaited
				          /*<div>
				            <h6>Code of Conduct</h6>
				            <Select // This is the built-in Select formInput
				            	className="form-control"
				              field='dataPartition'
				              options={[
				                { // You can ship it some options like usual
				                  label: 'coc1',
				                  value: 'coc1'
				                }, {
				                  label: 'coc2',
				                  value: 'coc2'
				                }, {
				                  label: 'coc3',
				                  value: 'coc3'
				                }, {
				                  label: 'coc4',
				                  value: 'coc4'
				                }, {
				                  label: 'coc5',
				                  value: 'coc5'
				                }, {
				                  label: 'coc6',
				                  value: 'coc6'
				                }, {
				                  label: 'noncoc',
				                  value: 'noncoc'
				                }
				              ]}
				              disabled={this.props.disabled}
				            />
				          </div>*/}
				          {/* // Since this is the parent form, let's put a submit button in there ;) */}
				          {/* // You can submit your form however you want, as long as you call the `submitForm` callback */}
				          <div className="form-button-group">
										<button className="btn btn-default" onClick={this.props.handleCloseModal}>Exit</button>
							      <button className="btn btn-default">{continueTxt}</button>
						      </div>
				        </form>
				    )
				  }}
				</Form>
		);
  }
}

TemplateInfoForm.propTypes = {
	formData: PropTypes.object,
	handleCloseModal: PropTypes.func.isRequired,
	handleOnChange: PropTypes.func.isRequired,
	handleAdvanceStep: PropTypes.func.isRequired,
	disabled: PropTypes.bool.isRequired,
	editForm: PropTypes.bool,
	sandboxForm: PropTypes.bool
}
