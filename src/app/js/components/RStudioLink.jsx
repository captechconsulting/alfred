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
import PropTypes from 'prop-types';
import ErrorPage from '../../../contents/js/components/ErrorPage';

// stores & actions
import UserStore from '../../../contents/js/stores/UserStore';
import * as UserActions from "../../../contents/js/actions/UserActions";

/*
* R Studio Link button component.
*/
export default class RStudioLink extends React.Component {
    constructor(props) {
        super(props);
        this.state = this.getState();
    }
    getState() {
        return ({
            userData: UserStore.getUserData(),
            errorModal: false,
          errorMsg: {}
        });
    }
    componentWillMount() {
    UserStore.on("change", () => {
      this.setState({
        userData: UserStore.getUserData()
      });
    });
  }
    onLinkClick() {
        // create request to the rstudio link with a custom header
        axios.request('http://192.168.203.1:8787', {
            headers: { 'X-RStudio-Username' : this.state.userData.username }
        })
        .then(function (response) {
          console.log(response.headers);
        })
        .catch(function (error) {
          console.log(error);
          _this.setState({
                errorModal: true,
                errorMsg: error
                });
        }
      );
    }
    handleCloseError() {
        // close error modal
      this.setState({
          errorModal: false,
          errorMsg: {}
      });
    }
    render() {
        let enabled = false;
        for (let x = 0; x < this.state.userData.roles.length; x++) {
            if (this.state.userData.roles[x].name === 'ROLE_RSTUDIO') {
                enabled = true;
            }
        }
        return (
            <div className="col-xs-4 col-sm-3 btn-container">
            {
                (enabled) &&
                <button className="btn btn-default reg-btn" onClick={this.onLinkClick.bind(this)}>RStudio</button>
            }
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
