
'use strict';
import React from 'react';

// stores & actions
import UserStore from '../../../contents/js/stores/UserStore';
import * as UserActions from "../../../contents/js/actions/UserActions";

//components
import RegisterTemplate from '../components/RegisterTemplate';
import SandboxOptionsModal from '../components/SandboxOptionsModal';

// helpers
import { hasPermission, isAdmin } from '../../../contents/js/utils/helpers.js';;

/*
* Register Sandbox Template button
*/
export default class RegisterSandboxButton extends React.Component {
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
            disabled={disabled}>Add Sandbox Data</button>
        {
            (this.state.showForm) ?
            <SandboxOptionsModal showForm={this.state.showForm} hideForm={this.handleCloseForm.bind(this)}/> : null
        }
      </div>
        );
    }
}
