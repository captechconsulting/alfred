
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
 */'use strict';
import React from 'react';
import ReactModal from 'react-modal';
import PropTypes from 'prop-types';
import RegisterSandboxTemplate from '../components/RegisterSandboxTemplate';
import RegisterTemplate from '../components/RegisterTemplate';

/*
* Register template container component
*/
export default class SandboxOptionsModal extends React.Component {
    constructor(props) {
        super(props);
        this.state = this.getState();
    }
    getState() {
        return ({
            showModal: false,
          confirmation: false,
          confirmationMsg: '',
          showAttachFileForm: false,
          showNewSandboxForm: false
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
        this.setState({
            showModal: true
        });
    }
    handleCloseModal() {
        this.setState(
            this.getState()
        );
        this.props.hideForm();
    }
    handleConfirmation(confirm) {
        this.setState({
            confirmation: true,
            confirmationMsg: confirm.confirmationMsg
        })
    }
    handleNewSandboxTemplate() {
        this.setState({
            showNewSandboxForm: true,
            showModal: false
        })
    }
    handleExistingTemplate() {
        this.setState({
            showAttachFileForm: true,
            showModal: false
        });
    }
    render() {
        return(
            <div>
        {
            (!this.state.showAttachFileForm) ?
            <ReactModal 
               isOpen={this.state.showModal}
               contentLabel="Register Sandbox Template"
               style={this.adjustModalStyles()}
            >
              <div className="form-options">
                  <h4>Select Option:</h4>
                  <button className="btn btn-default" onClick={this.handleNewSandboxTemplate.bind(this)}>Create New Template</button>
                     <button className="btn btn-default" onClick={this.handleExistingTemplate.bind(this)}>Attach File to Existing Template</button>
                  <button className="btn btn-default" onClick={this.handleCloseModal.bind(this)}>Exit</button>
              </div>
            </ReactModal> : null
          }
          {
              (this.state.showNewSandboxForm) ?
              <RegisterTemplate showAttachFileForm={this.state.showNewSandboxForm} closeAttachFileForm={this.handleCloseModal.bind(this)} attachFileForm={true} sandboxForm={true} /> : null
          }
          {
              (this.state.showAttachFileForm) ?
              <RegisterSandboxTemplate showAttachFileForm={this.state.showAttachFileForm} closeAttachFileForm={this.handleCloseModal.bind(this)}/> : null
          }
      </div>
        );
    }
}

SandboxOptionsModal.propTypes = {
    showForm: PropTypes.bool,
    hideForm: PropTypes.func
}
