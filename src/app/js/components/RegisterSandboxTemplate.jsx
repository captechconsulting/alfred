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
import PropTypes from 'prop-types';
import Select from 'react-select';
import axios from 'axios';
import { isEmpty, convertBytes } from '../../../contents/js/utils/helpers.js';;

// stores & actions
import TemplateStore from '../stores/TemplateStore';
import * as TemplateActions from "../actions/TemplateActions";
import ErrorPage from '../../../contents/js/components/ErrorPage';

/*
* Register Sandbox Template container component
*/
export default class RegisterSandboxTemplate extends React.Component {
    constructor(props) {
        super(props);
        this.state = this.getState();
    }
    getState() {
        return ({
            showModal: false,
          confirmation: false,
          templateOption: (this.props.templateData) ? this.props.templateData : '',
          file: {},
          summary: TemplateStore.getTemplateData()[0],
          errorModal: false,
          errorMsg: {}
        });
    }
    componentWillMount() {
        if (this.props.showAttachFileForm) {
            this.handleOpenModal();
        }
        TemplateStore.on("change", () => {
      this.setState({
        summary: TemplateStore.getTemplateData()[0]
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
        this.props.closeAttachFileForm();
    }
    handleSubmit(e) {
    e.preventDefault();

    // create new file data to be sent
    let _this = this;
    let fd = new FormData();    
        fd.append('file', this.state.file);
    let key = (this.props.templateData ? this.props.templateData.file.key : this.state.templateOption.label);

    axios.post('/fileMetadata/' + key + '/upload/', fd)
      .then(function (response) {
          _this.setState({
              confirmation: true
          });
      })
      .catch(function (error) {
        console.log(error);
        _this.setState({
            errorModal: true,
            errorMsg: error,
            file: {}
            });
      });
  }
  handleFileChange(e) {
    e.preventDefault();

    let reader = new FileReader();
    let file = e.target.files[0];

    reader.onloadend = () => {
      this.setState({
        file: file
      });
    }

    if(file && file.length !== 0) {
        reader.readAsDataURL(file)
    } else {
        this.setState({
            file: {}
        });
    }
  }
  handleFileUploadClick(e) {
      e.preventDefault();
      this.refs.fileInput.click();
  }
  handleRemoveFile() {
      this.setState({
      file: {}
    });
  }
    inputChange(val) {
        if (val !== null) {
          this.setState({
              templateOption: val
          })
        } else {
            this.setState({
                templateOption: '',
              file: {}
            });
        }
    }
    renderExistingTemplateSelect() {
        if(this.props.templateData) {
            return(
                <Select
                  name="form-field-name"
                  value={this.props.templateData.file.key}
                  options={
                      [
                          {
                              value: this.props.templateData.file.key,
                              label: this.props.templateData.file.key
                          }
                        ]
                  }
                  focusedOption={{
                      value: this.props.templateData.file.key,
                      label: this.props.templateData.file.key
                  }}
                  disabled={true}
                />
            );
      } else {
            let optionsD = [];
      if (this.state.summary && this.state.summary.summary && this.state.summary.summary.length !== 0) {
                for (let template in this.state.summary.summary) {
                    if (this.state.summary.summary.hasOwnProperty(template)) {
                        if (this.state.summary.summary[template].stage === 'sandbox') {
                            optionsD.push({
                                value: this.state.summary.summary[template], label: this.state.summary.summary[template].file.key
                            });
                        }
                    }
                }
            } else {
                optionsD.push({ value: null, label: 'No existing templates available.'})
            }

        return(
                <Select
                  name="form-field-name"
                  value={this.state.templateOption}
                  options={optionsD}
                  onChange={this.inputChange.bind(this)}
                />
            );
      }
    }
    handleCloseError() {
        // close error modal
      this.setState({
          errorModal: false,
          errorMsg: {}
      });
    }
    render() {
        return(
            <div>
          <ReactModal 
           isOpen={this.state.showModal}
           contentLabel="Register Sandbox Template"
           style={this.adjustModalStyles()}
        >
            <div className="register-sandbox-form container-fluid">
              <h4>Upload Sandbox File</h4>
              {
                  (!this.props.templateData) ?
                  <h6>Please select an existing template:</h6>
                  :
                  <h6>Current template:</h6>
              }
              {this.renderExistingTemplateSelect()}
              {
                  (this.state.templateOption && this.state.templateOption.length !== 0) &&
                  <div>
                      <form onSubmit={(e)=>this.handleSubmit(e)}>
                          <input className="file-input"
                              ref="fileInput" 
                            type="file" 
                            onChange={(e)=>this.handleFileChange(e)} />
                          {
                              (this.state.file && !this.state.file.name) &&
                              <div>
                                  <span className="btn btn-default uploadButton" onClick={this.handleFileUploadClick.bind(this)}>Select File</span>
                              </div>
                          }
                          {
                              (this.state.file && this.state.file.name) &&
                              <div>
                                  <h6>File Details:</h6>
                                  <div className="file-preview">
                                          <span>Name: {this.state.file.name}</span>
                                          <span>Size: {convertBytes(this.state.file.size, 3)}</span>
                                          <span>Type: {this.state.file.type}</span>
                                  </div>
                              </div>
                          }
                          {
                              (this.state.file && this.state.file.name && !this.state.confirmation) &&
                              <div className="form-button-group">
                                  <button className="btn btn-default" onClick={this.handleRemoveFile.bind(this)}>Remove File</button>
                                  <button className="btn btn-default submitButton"  type="submit" onClick={(e)=>this.handleSubmit(e)}>Submit to Sandbox</button>
                              </div>
                          }
                          {
                              (this.state.confirmation) &&
                              <h3>File has been uploaded.</h3>
                          }
                          {
                                  (this.state.errorModal) ? 
                                  <ErrorPage
                                      showForm={true}
                                      handleCloseError={this.handleCloseError.bind(this)}
                                      error={this.state.errorMsg}
                                  /> : null
                              }
                        </form>
                    </div>
                  }
                    <button className="btn btn-default btn-exit" onClick={this.handleCloseModal.bind(this)}>Exit</button>
          </div>
        </ReactModal>
      </div>
        );
    }
}

RegisterSandboxTemplate.propTypes = {
    showAttachFileForm: PropTypes.bool,
    closeAttachFileForm: PropTypes.func,
    templateData: PropTypes.object
}
