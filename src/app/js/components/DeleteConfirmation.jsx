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
import axios from 'axios';
import React from 'react';
import PropTypes from 'prop-types';
import ReactModal from 'react-modal';

//components
import ErrorPage from '../../../contents/js/components/ErrorPage';

// actions
import * as TemplateActions from "../actions/TemplateActions";

/*
* A presentation component that displays a confirmation modal when a user deletes a template.
* used in: Summary View Item
*/
export default class DeleteConfirmation extends React.Component {
    constructor(props) {
    super(props);
    this.state = this.getState();
  }
  getState() {
        return ({
            showModal: false,
            confirmDelete: false,
            errorModal: false,
          errorMsg: {}
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
    handleDeleteTemplate() {
        // TODO: Mova api request into const file
        let _this = this;
        axios.delete('/fileMetadata/' + this.props.templateKey)
      .then(function (response) {
          _this.setState({
                confirmDelete: true
            });
      })
      .catch(function (error) {
          console.log(error);
        _this.setState({
            errorModal: true,
            errorMsg: error
            });
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
        {
            (this.state.showModal) ? 
              <ReactModal 
             isOpen={this.state.showModal}
             contentLabel="Delete Confirmation"
             style={this.adjustModalStyles()}
          >
          {
              (!this.state.confirmDelete) ? 
              <div className="user-profile-details">
                <h3>Are you sure you want to delete {this.props.templateKey}?</h3>
                <div className="">
                    <button className="btn btn-default" onClick={this.props.handleCloseModal.bind(this)}>Cancel</button>
                    <button className="btn btn-default" onClick={this.handleDeleteTemplate.bind(this)}>Confirm</button>
                </div>
              </div> :
              <div className="user-profile-details">
                <h3>{this.props.templateKey} has been deleted.</h3>
                <div className="">
                    <button className="btn btn-default" onClick={this.handleExit.bind(this)}>Exit</button>
                </div>
              </div>
          }
          {
              (this.state.errorModal) ? 
              <ErrorPage
                  showForm={true}
                  handleCloseError={this.handleCloseError.bind(this)}
                  error={this.state.errorMsg}
              /> : null
          }
          </ReactModal> : null
        }
        </div>
    );
  }
}

DeleteConfirmation.propTypes = {
    showForm: PropTypes.bool.isRequired,
    handleCloseModal: PropTypes.func.isRequired,
    templateKey: PropTypes.string.isRequired
}
