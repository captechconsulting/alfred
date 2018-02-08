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

// stores & actions
import UserStore from '../../../contents/js/stores/UserStore';
import * as UserActions from "../../../contents/js/actions/UserActions";

//components
import RegisterTemplate from '../components/RegisterTemplate';

// helpers
import { hasPermission, isAdmin } from '../../../contents/js/utils/helpers.js';

/*
* Register Production Template button
*/
export default class RegisterTemplateButton extends React.Component {
    constructor(props) {
        super(props);
        this.state = this.getState();
    }
    getState() {
        return ({
            userData: UserStore.getUserData(),
            showForm: false
        });
    }
    componentWillMount() {
    UserStore.on("change", () => {
      this.setState({
        userData: UserStore.getUserData()
      });
    });
  }
    handleOpenForm() {
        this.setState({
            showForm: true
        });
    }
    handleCloseForm() {
        this.setState(
            this.getState()
        );
    }
    render() {
        let disabled = !hasPermission(this.state.userData, 'ADD');
        if (isAdmin(this.state.userData)) { //enable button if user is admin
            disabled = false;
        }
        return(
            <div className="col-xs-4 col-sm-3 btn-container">
        <button
            className="btn btn-default reg-btn"
            onClick={this.handleOpenForm.bind(this)}
            disabled={disabled}> Register Data </button>
        {
            (this.state.showForm) ?
            <RegisterTemplate
                showForm={this.state.showForm}
                closeAttachFileForm={this.handleCloseForm.bind(this)}
                attachFileForm={false}
                sandboxForm={false}/> : null
        }
      </div>
        );
    }
}
