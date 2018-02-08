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
import { Form, Text, Select } from 'react-form';

// import validation consts
import { sourceTechnicalDefalt } from '../../../../contents/js/constants/validation'

/*
* A form component that renders the refined technical information.
* used in: Refined Template
*/
export default class SourceTechnicalForm extends React.Component {
	constructor(props) {
    super(props);
  }
	render() {
		let continueTxt = (this.props.editForm) ? 'Save & Continue' : 'Continue';
    return (
			<Form
					// on submit life-cycle
					onSubmit={(values) => {
			      this.props.handleAdvanceStep();
			    }}

			    // on change of any input field call handleOnChange event
			    onChange={(state, props, initial, instance) => {
			    	this.props.handleOnChange([state.values, 'technicalData']);
			    }}

				  // Default form values if they exist in state.
				  defaultValues={{
				    format: this.props.formData.format ? this.props.formData.format : null,
				    tableName: this.props.formData.tableName ? this.props.formData.tableName : undefined
				  }}

				  // validate life-cycle
				  validate={ sourceTechnicalDefalt }

				  // `onValidationFail` is another handy form life-cycle method
				  onValidationFail={() => {
				  	console.log('Form Validation failed.')
				  }}
				>
				  {({ values, submitForm, addValue, removeValue, getError }) => {
				    return (
				        <form onSubmit={submitForm}>
				          <div className="form-group">
				            <h6>Format</h6>
				            <Select
				            	className="form-control"
				              field='format'
				              options={[{
				                label: 'Tabular',
				                value: 'tabular'
				              }]}
				              disabled={this.props.disabled}
				            />
				          </div>
				          {
				          	(this.props.formData && (this.props.formData.format === 'tabular')) && 
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
							      <button className="btn btn-default">{continueTxt}</button>
						      </div>
				        </form>
				    )
				  }}
				</Form>
		);
  }
}

SourceTechnicalForm.propTypes = {
	formData: PropTypes.object,
	handleCloseModal: PropTypes.func.isRequired,
	handleOnChange: PropTypes.func.isRequired,
	handleAdvanceStep: PropTypes.func.isRequired,
	disabled: PropTypes.bool.isRequired
}
