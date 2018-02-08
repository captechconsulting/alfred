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
import axios from 'axios';
import { Form, Text, Checkbox } from 'react-form';

//components
import ErrorPage from '../components/ErrorPage';

// stores & actions
import * as TemplateActions from "../../../app/js/actions/TemplateActions";

/*
* A form to reset a user's pasword
* Used in: Register & Refined Template
*/
export default class FieldForm extends React.Component {
	constructor(props) {
    super(props);
    this.state = {
    	showPassword: false,
    	errorModal: false,
		  errorMsg: {}
    };
  }
  handleShowPassword() {
  	this.setState({
  		showPassword: !this.state.showPassword
  	});
  }
  handleCloseError() {
		// close error modal
  	this.setState({
  		errorModal: false,
		  errorMsg: {}
  	});
	}
	render() {
    return (
    	<div>
				<Form	
				  // on submit life-cycle
					onSubmit={(values) => {
						let newPW = {
							newPassword: values.newPassword
						}
						let _this = this;

						axios.put('/authentication/user/password', newPW)
					  .then(function (response) {
					  	location.reload(); // reprompt user to log in with new credentials
					  })
					  .catch(function (error) {
					    console.log(error);
					    _this.setState({
					    	errorModal: true,
					    	errorMsg: error
							});
					  });
					}}
		
					// after submit life-cycle
			    postSubmit={(values) => {
				    console.log('password reset successfully');
				    location.reload();
				  }}
		
				  // validate life-cycle
				  validate={() => {

				  }}

				  // `onValidationFail` is another handy form life-cycle method
				  onValidationFail={(values) => {
				  	console.log('Form Validation failed.')
				  }}
				>
				  {({ values, submitForm, addValue, removeValue, getError }) => {
				    return (
			        <form onSubmit={submitForm} className="new-field-form">
			          <div className="form-group">
			          <div>
			          	<h6>Your password has expired please enter a new password.</h6>
			          </div>
			          {
			          	(!this.state.showPassword) ? 
			            <Text
			            	type="password"
			              field='newPassword'
			              className="form-control" /> : 
			            <Text
			              field='newPassword'
			              className="form-control" />
			          }
			          </div>
			          <div className="checkbox-group centered">
			          	<span>Show Password: </span>
	                <Checkbox 
	                  field='showPassword'
	                  onChange={(e, onChange) => {
							        onChange()
							        this.handleShowPassword();
							      }}
	                />
			          </div>
			          <div className="form-button-group">
			          	<button className="btn btn-default" >Submit New Password</button>
			          </div>
			        </form>
				    )
				  }}
				</Form>
				{
	      	(this.state.errorModal) ? 
	      	<ErrorPage
	      		showForm={true}
	      		handleCloseError={this.handleCloseError.bind(this)}
	      		error={this.state.errorMsg}
	      	/> : null
	      }
			</div>
		);
  }
}
