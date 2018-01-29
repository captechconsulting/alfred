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
import ReactModal from 'react-modal';

// stores & actions
import UserStore from '../stores/UserStore';
import * as UserActions from "../actions/UserActions";

/*
* A presentation component that displays the user information.
*/
export default class UserProfile extends React.Component {
	constructor(props) {
    super(props);
    this.state = this.getState();
  }
  getState() {
		return ({
			userData: UserStore.getUserData(),
			showModal: false
		});
	}
	componentWillMount() {
    UserStore.on("change", () => {
      this.setState({
        userData: UserStore.getUserData()
      });
    });
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
		this.setState({
			showModal: true
		});
	}
	handleCloseModal() {
		this.setState(
			this.getState()
		);
	}
	renderPermissions() {
		let permissionsList = [];
		let permissionsArr = [];
		let index = 0; // persist index
		if (this.state.userData) {
			if(this.state.userData.roles.length !== 0) {
				if (this.state.userData.roles[0].permissions.length !== 0) { // checks the default permissions
					for (let x = 0; x < this.state.userData.roles[0].permissions.length; x++) {
						index++;
						permissionsList.push(
							<div key={'default-' + this.state.userData.roles[0].permissions[x]}>{index}. {this.state.userData.roles[0].permissions[x]}</div>
						);
						permissionsArr.push(this.state.userData.roles[0].permissions[x]);
					}
				}
			}
			if (this.state.userData.permissions.length !== 0) { // checks the given permissions
				for (let x = 0; x < this.state.userData.permissions.length; x++) {
					index++;
					if(!permissionsArr.includes(this.state.userData.permissions[x])) {
						permissionsList.push(
							<div key={'given-' + this.state.userData.permissions[x]}>{index}. {this.state.userData.permissions[x]}</div>
						);
					}
				}
			}
		}
		return permissionsList;
	}
	render() {
		let name = (this.state.userData && this.state.userData.username) ? this.state.userData.username : '';
		let role = (this.state.userData && (this.state.userData.roles.length !== 0) && this.state.userData.roles[0].name) ? this.state.userData.roles[0].name : '';
    return (
		  <div>
		  	<div className="user-profile">
		  		<h3>
						<span className="user-profile__icon glyphicon glyphicon-user" onClick={this.handleOpenModal.bind(this)} aria-hidden="true"></span> 
					</h3>
		  		<span className="user-profile__title" onClick={this.handleOpenModal.bind(this)}>{name}</span>
		  	</div>
		  	{
		  		(this.state.showModal) ? 
		  		<ReactModal 
	           isOpen={this.state.showModal}
	           contentLabel="Register Sandbox Template"
	           style={this.adjustModalStyles()}
	        >
	        	<div className="user-profile-details">
		        	<h3 className="user-profile-details__title">User Profile Details</h3>
		        	<div><strong>Username:</strong> {name}</div>
		        	{
		        		(this.state.userData.roles.length !== 0) &&
			        	<div><strong>Role:</strong> {role}</div>
			        }
				  		<div><strong>Permissions: </strong>
				  			{this.renderPermissions()}
				  		</div>
				  		<div className="user-profile-details__button-container">
		        		<button className="btn btn-default" onClick={this.handleCloseModal.bind(this)}>Exit</button>
		        	</div>
	        	</div>
	        </ReactModal> : null
		  	}
		  </div>
    );
  }
}
