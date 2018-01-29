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
import ReactModal from 'react-modal';

// actions
import * as TemplateActions from "../../../app/js/actions/TemplateActions";

/*
* A presentation component that displays a the errors wwhen submitting a form.
* used in: All http request error handling methods.
*/
export default class ErrorPage extends React.Component {
	constructor(props) {
    super(props);
    this.state = this.getState();
  }
  getState() {
		return ({
			showModal: false,
			confirm: false
		});
	}
	componentWillMount() {
		if (this.props.showForm) {
			this.handleOpenModal();
		}
	}
	adjustModalStyles() {
		return({
      overlay: {
      	backgroundColor: 'rgba(128, 128, 128, 0.75)'
      },
      content : {
		    top                   : '50%',
		    left                  : '50%',
		    right                 : 'auto',
		    bottom                : 'auto',
		    marginRight           : '-50%',
		    transform             : 'translate(-50%, -50%)'
		  }
    });
	}
	handleOpenModal() {
		// Open Modal
		this.setState({
			showModal: true
		});
	}
	handleExit() {
		// Template list gets update and then the summary item component gets removed from the dom.
		TemplateActions.reloadTemplates();
	}
	handleErrorList(statusCode) {
		const error = this.props.error.response;
		switch(statusCode) { // List of errors in case you want to do something unique for a specific reason.
			case 500: {
				return (
					<p key='500-error'>{this.props.error.response.data.errors}</p>
				);
				break;
			}
			case 409: {
				return (
					<p key='408-error'>{this.props.error.response.data.errors}</p>
				);
				break;
			}
			case 405: {
				return (
					<p key='405-error'>{this.props.error.response.data.errors}</p>
				);
				break;
			}
			case 404: {
				return (
					<p key='404-error'>{this.props.error.response.data.errors}</p>
				);
				break;
			}
			case 401: {
				return (
					<p key='401-error'>{this.props.error.response.data.errors}</p>
				);
				break;
			}
			case 400: {
				return (
					<p key='400-error'>{this.props.error.response.data.errors}</p>
				);
				break;
			}
			default:
    		return (
					<p key='unknown-error'>Unknown Error Occurred. {this.props.error.response.data.errors}</p>
				);
		}
	}
	handlePrintErrors() {
		let errorList = [];
		let index = 0; // persist index
		if (this.props.error && this.props.error.response) {
			errorList.push(this.handleErrorList(this.props.error.response.status));
		}
		return errorList;
	}
	render() {
    return (
    	<div>
    	{
    		(this.state.showModal) ? 
	  		<ReactModal 
	         isOpen={this.state.showModal}
	         contentLabel="Error Page"
	         style={this.adjustModalStyles()}
	      >
	      	<div className="user-profile-details">
	        	<h3>There sems to be an error (details below):</h3>
	        	<p>HTTP response status: {this.props.error.response.status}</p>
	        	{this.handlePrintErrors()}
	        	<div className="">
	        		<button className="btn btn-default" onClick={this.props.handleCloseError.bind(this)}>Return</button>
	        	</div>
	      	</div>
	      </ReactModal> : null
	    }
	    </div>
    );
  }
}

ErrorPage.propTypes = {
	showForm: PropTypes.bool.isRequired,
	handleCloseError: PropTypes.func.isRequired,
	error: PropTypes.object
}
