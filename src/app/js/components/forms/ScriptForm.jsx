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
import { Form, Text, Select, Textarea} from 'react-form';

// import validation consts
import { scriptFormDefault } from '../../../../contents/js/constants/validation'

/*
* A form component that renders the script information.
* used in: Refined Template
*/
export default class ScriptForm extends React.Component {
	constructor(props) {
    super(props);
  }
	render() {
		let continueTxt = (this.props.editForm) ? 'Save & Continue' : 'Continue';
    return (
			<Form
	        onSubmit={(values) => {
	        	this.props.handleOnChange([values, 'scriptData']);
			      this.props.handleAdvanceStep();
			    }}

				  // Default form values if they exist in state.
				  defaultValues={{
				    path: this.props.formData.path ? this.props.formData.path : undefined,
				    name: this.props.formData.name ? this.props.formData.name : undefined,
				    description: this.props.formData.description ? this.props.formData.description : undefined,
				    owner: this.props.formData.owner ? this.props.formData.owner : undefined,
				    schedule: this.props.formData.schedule ? this.props.formData.schedule : undefined
				  }}

				  // When validating fields occurs
				  validate={ scriptFormDefault }

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
				          	<h6>Path</h6>
				            <Text // This is the built-in Text formInput
				              field='path' // field is a string version of the field location
				              placeholder='e.g. domin01/weatherScripts'
				              className="form-control"
				              disabled={ this.props.disabled } />
				          </div>
				          <div className="form-group">
				          	<h6>Name</h6>
				            <Text
				              field='name'
				              placeholder='e.g. average.R' 
				              className="form-control"
				              disabled={this.props.disabled} />
				          </div>
				          <div className="form-group">
				          	<h6>Description</h6>
				            <Textarea
				              field='description'
				              placeholder='Script Description'
				              className="form-control"
				              rows="3"
				              disabled={this.props.disabled} />
				          </div>
				          <div className="form-group">
				          	<h6>Owner</h6>
				            <Text
				              field='owner'
				              placeholder='e.g. domin01' 
				              className="form-control"
				              disabled={this.props.disabled}
				              maxLength="7" />
				          </div>
				          <div className="form-group">
				          	<h6>Schedule</h6>
				            <Text
				              field='schedule'
				              placeholder='e.g. 03***'
				              className="form-control"
				              disabled={this.props.disabled}
				              maxLength="7" />
				          </div>
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

ScriptForm.propTypes = {
	formData: PropTypes.object,
	handleCloseModal: PropTypes.func.isRequired,
	handleOnChange: PropTypes.func.isRequired,
	handleAdvanceStep: PropTypes.func.isRequired,
	disabled: PropTypes.bool.isRequired,
	editForm: PropTypes.bool
}
