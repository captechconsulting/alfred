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
import axios from 'axios';
import PropTypes from 'prop-types';
import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';
import { Form } from 'react-form';
import Loader from 'react-loader';

// actions
import * as TemplateActions from "../actions/TemplateActions";
import { isEmpty, convertBytes } from '../../../contents/js/utils/helpers.js';

// components
import TemplateInfoForm from '../components/forms/TemplateInfoForm';
import TechnicalForm from '../components/forms/TechnicalForm';
import FieldForm from '../components/forms/FieldForm';
import RegisterSandboxTemplate from '../components/RegisterSandboxTemplate';
import ErrorPage from '../../../contents/js/components/ErrorPage';

/*
* A parent functional component that defines the production and sandbox dataset workflow.
*/
export default class RegisterTemplate extends React.Component {
    constructor(props) {
        super(props);
        this.state = this.getState();
    }
    getState() {
        return ({
            showModal: false,
          confirmation: false,
          confirmationMsg: '',
          templateOption: '',
          file: {},
            step: this.handleInitialData('step'),
            tabIndex: this.handleInitialData('tabIndex'),
            disabled: this.handleInitialData('disabled'),
            showTabs: this.handleInitialData('showTabs'),
          defaultData: this.handleInitialData('defaultData'),
          technicalData: this.handleInitialData('technicalData'),
          fields: this.handleInitialData('fields'),
          attachFileData: {},
          editField: false,
          editFieldData: {},
          editFieldIndex: null,
          bitFlip: false, // Used to reset state of parent (remounts child component with fresh state).
          showAttachFileForm: false,
          templateData: {},
          editForm: (this.props.editForm ? this.props.editForm : false),
          errorModal: false,
          errorMsg: {},
          hideFileUpload: (this.props.disableFileButton ? this.props.disableFileButton : false),
          loaded: true
        });
    }
    handleInitialData(tab) {
        // If the data set is provided initialize the default values (preview template)
        if (this.props.dataSet) {
            const data = this.props.dataSet.file;
            const fields = this.props.dataSet.fields;
            switch(tab) {
            case 'defaultData':
            return (
                {
                        key: data.key,
                        businessName: data.business.name,
                        dataSteward: data.business.dataSteward,
                        owner: data.business.owner,
                        subjectArea: data.subjectArea,
                        description: data.business.description,
                        dataPartition: data.dataPartition
                    }
            );
            break;
            case 'technicalData':
            return (
                {
                        format: data.technical.format,
                        rowFormat: data.technical.rowFormat,
                        fieldDelimiter: (data.technical.rowFormat === 'delimited') ? data.technical.fieldDelimiter : '',
                        lineTerminator: data.technical.lineTerminator,
                        containsHeaderRow: data.technical.containsHeaderRow,
                        fileUpdateType: data.technical.fileUpdateType,
                        tableName: data.technical.tableName
                    }
            );
            break;
            case 'fields':
                return (fields);
                break;
            case 'step':
                return (2);
                break;
            case 'tabIndex':
                return (0);
                break;
            case 'disabled':
                return (this.props.disableForm ? this.props.disableForm : false);
                break;
            case 'showTabs':
                return ({
                        default: true,
                        fields: true,
                        technical: true,
                        attachFile: true
                    });
                    break;
            }
        } else { // No data is provided these are default values
            switch(tab) {
            case 'defaultData':
            return ({});
            break;
            case 'technicalData':
            return ({});
            break;
            case 'fields':
                return ([]);
                break;
            case 'step':
                return (0);
                break;
            case 'tabIndex':
                return (0);
                break;
            case 'disabled':
                return false;
                break;
            case 'showTabs':
                return ({
                        default: true,
                        fields: false,
                        technical: false,
                        attachFile: false
                    });
                    break;
            }
        }
    }
    componentWillMount() {
        // Display form inside a modal view if these props exist
        if (this.props.showAttachFileForm || this.props.showForm) {
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
        // reset state
        this.setState(
            this.getState()
        );

        // close parent forms
        if(this.props.closeAttachFileForm) {
            this.props.closeAttachFileForm();
        }

        // close view modal
        if(this.props.handleCloseModal) {
            this.props.handleCloseModal();
        }
    }
    handleAdvanceStep(e) {
        // Set up the tabs steps
        if (this.state.tabIndex < this.state.step) { // When a user clicks on a previous tab, this restores order
            this.setState({
                tabIndex: this.state.tabIndex + 1,
            });
        } else if(this.state.showTabs.default && !this.state.showTabs.technical && !this.state.showTabs.fields && !this.state.showTabs.attachFile) {
            this.setState({
                step: this.state.step + 1,
                tabIndex: this.state.tabIndex + 1,
                showTabs: {
                    default: true,
                    technical: true,
                    fields: false,
                    attachFile: false
                }
            });
            return (this.submitForm);
        } else if (this.state.showTabs.default && this.state.showTabs.technical && !this.state.showTabs.fields && !this.state.showTabs.attachFile) {
            this.setState({
                step: this.state.step + 1,
                tabIndex: this.state.tabIndex + 1,
                showTabs: {
                    default: true,
                    technical: true,
                    fields: true,
                    attachFile: false
                }
            });
        } else {
            this.handleSubmitTemplate();
        }
    }
    handleSaveTemplate() {
        // save template as draft
        const _this = this;
        const defaultData = this.state.defaultData;
        const technicalData = this.state.technicalData;
        const fieldsData = this.state.fields;

        // set loading state
        this.setState({
            loaded: false
        })

        // add new file prototype
        let fd = new FormData();
            fd.append('file', this.state.file);

        // add position to each field entry
        let fieldsPayload = [];

        // set stage
        let templateStage = !this.props.sandboxForm ? 'draft' : 'sandbox';

        // Build the payload
        let dataObj = {
            file: {
                key: defaultData.key ? defaultData.key : '',
                dataPartition: defaultData.dataPartition ? defaultData.dataPartition : '',
                subjectArea: defaultData.subjectArea ? defaultData.subjectArea : '',
                business: {
                    name: defaultData.businessName ? defaultData.businessName : '',
                    description: defaultData.description ? defaultData.description : '',
                    owner: defaultData.owner ? defaultData.owner : '',
                    dataSteward: defaultData.dataSteward ? defaultData.dataSteward : ''
                },
                technical: {
                    format: technicalData.format ? technicalData.format : '',
                    rowFormat: technicalData.rowFormat ? technicalData.rowFormat : '',
                    fieldDelimiter: technicalData.fieldDelimiter ? technicalData.fieldDelimiter : '',
                    lineTerminator: technicalData.lineTerminator ? technicalData.lineTerminator : '',
                    fileUpdateType: technicalData.fileUpdateType ? technicalData.fileUpdateType : '',
                    tableName: technicalData.tableName ? technicalData.tableName : '',
                    containsHeaderRow: technicalData.containsHeaderRow ? technicalData.containsHeaderRow : false
                }
            },
            fields: fieldsPayload,
            stage: templateStage
        };

        if(!this.state.editForm) { // Handle submission if template is new and not in editing mode.
            axios.post('/fileMetadata', dataObj)
          .then(function (response) {
              _this.setState({
                  editForm: true
              });
              // if successful do another request for the file.
              axios.put('/fileMetadata/' + defaultData.key + '/sample', fd)
              .then(function (response) {
                  _this.handleAppendFieldsFromFile(response);
              })
              .catch(function (error) {
                console.log(error);
                _this.setState({
                    errorModal: true,
                    errorMsg: error,
                    loaded: true,
                    file: {},
                    hideFileUpload: false
                    });
              });
          })
          .catch(function (error) {
            console.log(error);
            _this.setState({
                errorModal: true,
                errorMsg: error,
                loaded: true,
                file: {},
                hideFileUpload: false
                });
          });
        } else {
            axios.put('/fileMetadata/' + defaultData.key + '/sample', fd)
          .then(function (response) {
              _this.handleAppendFieldsFromFile(response);
          })
          .catch(function (error) {
            console.log(error);
            _this.setState({
                errorModal: true,
                errorMsg: error,
                loaded: true,
                file: {}
                });
          });
        }
    }
    handleAppendFieldsFromFile(data) {
        const fields = data.data.fields;
        // Grab the postion of the field that is intended for cloning, then push a new list to state.
      let modifiedFieldList = [];

      // push the new fields onto the list
      for(let i = 0; i < fields.length; i++) {
          modifiedFieldList.push(fields[i])
      }

      this.setState({
          fields: modifiedFieldList,
          hideFileUpload: true,
          editForm: true,
          loaded: true,
          bitFlip: !this.state.bitFlip
      });
    }
    handleSubmitTemplate() {
        const _this = this;
        const defaultData = this.state.defaultData;
        const technicalData = this.state.technicalData;
        const fieldsData = this.state.fields;

        // add position to each field entry
        let fieldsPayload = [];
        for(let x = 0; x < fieldsData.length; x++) {
            let fieldObj = {};
            fieldObj = JSON.parse(JSON.stringify(fieldsData[x]));
            if(typeof fieldObj.position === 'undefined' || !fieldObj.position){
                fieldObj.position = x + 1;    
            }
            fieldsPayload.push(fieldObj);
        }

        let templateStage = !this.props.attachFileForm ? 'final' : 'sandbox';

        if(this.props.dataSet) {
            templateStage = this.props.dataSet.stage;
        }

        // Build the payload
        let dataObj = {
            file: {
                key: defaultData.key ? defaultData.key : '',
                dataPartition: defaultData.dataPartition ? defaultData.dataPartition : '',
                subjectArea: defaultData.subjectArea ? defaultData.subjectArea : '',
                business: {
                    name: defaultData.businessName ? defaultData.businessName : '',
                    description: defaultData.description ? defaultData.description : '',
                    owner: defaultData.owner ? defaultData.owner : '',
                    dataSteward: defaultData.dataSteward ? defaultData.dataSteward : ''
                },
                technical: {
                    format: technicalData.format ? technicalData.format : '',
                    rowFormat: technicalData.rowFormat ? technicalData.rowFormat : '',
                    fieldDelimiter: technicalData.fieldDelimiter ? technicalData.fieldDelimiter : '',
                    lineTerminator: technicalData.lineTerminator ? technicalData.lineTerminator : '',
                    fileUpdateType: technicalData.fileUpdateType ? technicalData.fileUpdateType : '',
                    tableName: technicalData.tableName ? technicalData.tableName : '',
                    containsHeaderRow: technicalData.containsHeaderRow ? technicalData.containsHeaderRow : false
                }
            },
            fields: fieldsPayload,
            stage: templateStage
        };
        
        if(!this.state.editForm) { // Handle submission if template is new and not in editing mode.
            axios.post('/fileMetadata', dataObj)
          .then(function (response) {
              _this.handleConfirmation({
                    confirmationMsg: defaultData.key + ' has been registered.',
                    dataObj: dataObj
                });
              TemplateActions.reloadTemplates();
          })
          .catch(function (error) {
            console.log(error);
            _this.setState({
                errorModal: true,
                errorMsg: error
                });
          });
        } else { // Handle submission for an existing template in edit mode.
            axios.put('/fileMetadata/' + dataObj.file.key, dataObj)
          .then(function (response) {
              _this.handleConfirmation({
                    confirmationMsg: defaultData.key + ' has been updated.',
                    dataObj: dataObj
                });
              TemplateActions.reloadTemplates();
          })
          .catch(function (error) {
            console.log(error);
            _this.setState({
                errorModal: true,
                errorMsg: error
                });
          });
        }
    }
    handleConfirmation(confirm) {
        this.setState({
            confirmation: true,
            confirmationMsg: confirm.confirmationMsg,
            templateData: confirm.dataObj
        });
    }
    handleChange(obj) {
        let state = obj[0];
        let tab = obj[1];
    this.setState({
        [obj[1]]: state
    });
  }
  handleAddField(values) {
      // Add new field to the list, flip bit so the form resets (mounts a pristine field form)
      this.setState({
          fields: this.state.fields.concat([values]),
          bitFlip: !this.state.bitFlip
      });
  }
  handleEditField(values) {
      // We want to push a new fieldlist to maintain the order of the fields when editing one (DO NOT MUTATE PREVIOUS ARRAY).
      let modifiedFieldList = [];
      for(let x = 0; x < this.state.fields.length; x++) {
          if (x !== this.state.editFieldIndex) {
              modifiedFieldList.push(this.state.fields[x]);
          } else {
              modifiedFieldList.push(values);
          }
      }
      this.setState({
          fields: modifiedFieldList,
      });
      this.handleResetForm();
  }
     handleCloneField(fieldPos, e) {
         // Grab the postion of the field that is intended for cloning, then push a new list to state.
      let modifiedFieldList = [];
      for(let x = 0; x < this.state.fields.length; x++) {
          modifiedFieldList.push(this.state.fields[x]);
      }
      modifiedFieldList.push(this.state.fields[fieldPos]);
      this.setState({
          fields: modifiedFieldList,
      });
  }
  handleDeleteField(fieldPos, e) {
      // Delete the specified field and then push a new list to state.
      let newFieldList = [];
      for(let x = 0; x < this.state.fields.length; x++) {
          if(x !== fieldPos) {
              newFieldList.push(this.state.fields[x]);
          }
      }
      this.setState({
          fields: newFieldList
      });
      this.handleResetForm();
  }
  handleResetForm() {
      // Reset field form and flip bit to reset state of parent.
      this.setState({
          editField: false,
          editFieldData: {},
          editFieldIndex: null,
          bitFlip: false
      });
  }
  handleOpenAttachForm() {
      this.setState({
          showAttachFileForm: true
      });
  }
  handleFileUploadClick(e) {
      e.preventDefault();
      this.refs.fileInput.click();
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
  handleFileCancel() {
      this.refs.file.reset();
      this.setState({
          file: {}
      });
  }
  handleFileSubmit() {

      this.handleSaveTemplate()
  }
  renderFields() {
        let arr = [];
        for(let x = 0; x < this.state.fields.length; x++) {
            arr.push(
                <div className="field-list" key={x}>
                    {
                        (!this.state.disabled) ?
                        <div className="field-list__buttons">
                            <span className="glyphicon glyphicon-pencil" aria-hidden="true" onClick={this.renderEditForm.bind(this, x)}></span>
                            <span className="glyphicon glyphicon-plus" aria-hidden="true" onClick={this.handleCloneField.bind(this, x)}></span>
                            <span className="glyphicon glyphicon-minus" aria-hidden="true" onClick={this.handleDeleteField.bind(this, x)}></span>
                        </div> :
                        <div className="field-list__buttons">
                            <span className="glyphicon glyphicon-pencil" aria-hidden="true" onClick={this.renderEditForm.bind(this, x)}></span>
                        </div>
                    }
                    {
                        (x !== this.state.editFieldIndex) ?
                        <div className="field-list__item">
                            {(x+1)}.&nbsp;
                            {this.state.fields[x].name}
                        </div> : 
                        <div className="field-list__item active">
                            {(x+1)}.&nbsp;
                            {this.state.fields[x].name}
                        </div>
                    }
                </div>
        );
        }
        return(arr);
    }
    renderEditForm(e, pos) {
        this.setState({
            editFieldData: this.state.fields[e],
            editFieldIndex: e,
            editField: true,
            bitFlip: !this.state.bitFlip
        });
    }
    renderDefaultTab() {
        return (
            <TabPanel>
        <TemplateInfoForm
            formData={this.state.defaultData}
            handleCloseModal={this.handleCloseModal.bind(this)}
            handleOnChange={this.handleChange.bind(this)}
            handleAdvanceStep={this.handleAdvanceStep.bind(this)}
            disabled={this.state.disabled}
            editForm={this.state.editForm}
            sandboxForm={this.props.sandboxForm} />
            </TabPanel>
        );
    }
    renderTechnicalTab() {
        return (
            <TabPanel>
                <TechnicalForm
                    formData={this.state.technicalData}
                    handleCloseModal={this.handleCloseModal.bind(this)}
            handleOnChange={this.handleChange.bind(this)}
            handleAdvanceStep={this.handleAdvanceStep.bind(this)}
            disabled={this.state.disabled}
            editForm={this.state.editForm}
            sandboxForm={this.props.sandboxForm} />
            </TabPanel>
        );
    }
    renderFieldsTab() {
        let continueTxt = (this.state.editForm) ? 'Submit Changes' : 'Register Template';
        return (
            <TabPanel>
                <div className="fields-container">
                    {
                        (this.state.loaded) &&
                        <div className="new-field-list__container">
                            <div className="new-field-list__btn-container">
                        <h5>Field List:</h5>
                        { // Only show upload button when it's a newform and json or xml format
                            (this.state.technicalData && !this.state.disabled && ((this.state.technicalData.format === "xml" || this.state.technicalData.format === "json")) && !this.state.hideFileUpload) &&
                              <form ref="file" onSubmit={(e)=>this.handleSubmit(e)}>
                              <input className="file-input"
                                  ref="fileInput" 
                                type="file" 
                                onChange={(e)=>this.handleFileChange(e)} />
                              {
                                      (isEmpty(this.state.file)) &&
                                      <button className="btn btn-default" onClick={this.handleFileUploadClick.bind(this)}>Upload File</button>
                                  }
                            </form>
                          }
                        </div>
                        {
                      (this.state.file && this.state.file.name && !this.state.hideFileUpload) &&
                      <div className="new-field-list__file-preview">
                          <h6>File Details:</h6>
                          <div className="file-preview">
                                  <span>Name: {this.state.file.name}</span>
                                  <span>Size: {convertBytes(this.state.file.size, 3)}</span>
                                  <span>Type: {this.state.file.type}</span>
                                  <span><strong>**submitting this file will replace all existing fields**</strong></span>
                          </div>
                          <div className="file-preview__button-container">
                              <button className="btn btn-default" onClick={this.handleFileCancel.bind(this)}>Cancel</button>
                              <button className="btn btn-default" onClick={this.handleFileSubmit.bind(this)}>Submit</button>
                          </div>
                      </div>
                  }
                            {
                        (this.state.fields && (this.state.fields.length !== 0)) &&
                        <div className="new-field-list__items">{this.renderFields()}</div>
                    }
                </div>
              }
              {
                        (!this.state.loaded) &&
                        <div className="new-field-list__container">
                            <div className="new-field-list__btn-container">
                        <span><strong>Loading fields please wait...</strong></span>
                        <div className="loader-block__fields">
                        <Loader loaded={this.state.loaded}></Loader>
                      </div>
                    </div>
                  </div>
              }
            <div className="field-form">
                { // Fresh form
                    (this.state.bitFlip && !this.state.editField) &&    
                            <FieldForm
                                handleAddField={this.handleAddField.bind(this)}
                                editMode={this.state.editField}
                                disabled={this.state.disabled} />    
                        }
                        { // Fresh form reset by bitFlip (remounts component)
                    (!this.state.bitFlip && !this.state.editField) &&
                            <FieldForm
                                handleAddField={this.handleAddField.bind(this)}
                                editMode={this.state.editField}
                                disabled={this.state.disabled} />
                        }
                        { // Edit form
                            (this.state.bitFlip && this.state.editField) &&
                            <FieldForm
                                fieldData={this.state.editFieldData}
                                editMode={this.state.editField}
                                resetForm={this.handleResetForm.bind(this)}
                                handleEditField={this.handleEditField.bind(this)}
                                disabled={this.state.disabled} />
                        }
                        { // Edit form reset by bitFlip (remounts component)
                            (!this.state.bitFlip && this.state.editField) &&
                            <FieldForm
                                fieldData={this.state.editFieldData}
                                editMode={this.state.editField}
                                resetForm={this.handleResetForm.bind(this)}
                                handleEditField={this.handleEditField.bind(this)}
                                disabled={this.state.disabled} />
                        }
                        <div className="form-button-group">
                            <button className="btn btn-default" onClick={this.handleCloseModal.bind(this)}>Exit</button>
                            {/*<button className="btn btn-default" onClick={this.handleSaveTemplate.bind(this)}>Save</button>*/}
                      {
                          (!this.state.disabled) &&
                          <button className="btn btn-default" onClick={this.handleAdvanceStep.bind(this)}>{continueTxt}</button>
                      }
                  </div>
                    </div>
                </div>
            </TabPanel>
        );
    }
    handleCloseError() {
        // close error modal
      this.setState({
          errorModal: false,
          errorMsg: {},
          bitFlip: !this.state.bitFlip
      });
    }
    render() {
        return(
            <div>
        {
            (this.state.showModal) ?
            <ReactModal 
               isOpen={this.state.showModal}
               contentLabel="Register Sandbox Template"
               style={this.adjustModalStyles()}
            >
                { // Ternary statement here to remove modal from the dom when not being used
                    ((this.state.confirmation === false) && (!this.state.showAttachFileForm)) &&
                    <div className="register-form container-fluid">
                    {
                        (this.state.step === 0) &&
                        <div>
                          <Tabs selectedIndex={this.state.tabIndex} onSelect={tabIndex => this.setState({ tabIndex })}>
                                        <TabList>
                                            <Tab>Template Information</Tab>
                                        </TabList>
                                        {this.renderDefaultTab()}
                                    </Tabs>
                                </div>
                            }
                            {
                        (this.state.step === 1) &&
                        <div>
                          <Tabs selectedIndex={this.state.tabIndex} onSelect={tabIndex => this.setState({ tabIndex })}>
                                        <TabList>
                                            <Tab>Template Information</Tab>
                                            <Tab>Technical</Tab>
                                        </TabList>
                                        {this.renderDefaultTab()}
                                        {this.renderTechnicalTab()}
                                    </Tabs>
                                </div>
                            }
                            {
                        (this.state.step === 2) &&
                        <div>
                          <Tabs selectedIndex={this.state.tabIndex} onSelect={tabIndex => this.setState({ tabIndex })}>
                                        <TabList>
                                            <Tab>Template Information</Tab>
                                            <Tab>Technical</Tab>
                                            <Tab>Fields</Tab>
                                        </TabList>
                                        {this.renderDefaultTab()}
                                        {this.renderTechnicalTab()}
                                        {this.renderFieldsTab()}
                                    </Tabs>
                                </div>
                            }
                  </div>
                }
              {
                  ((this.state.confirmation === true) && (this.props.attachFileForm)) &&
                  <div className="register-form container-fluid">
                  <h3>{this.state.confirmationMsg}</h3>
                  <button className="btn btn-default" onClick={this.handleOpenAttachForm.bind(this)}>Attach File</button>
                </div>
              }
              {
                  ((this.state.confirmation === true) && (!this.props.attachFileForm)) &&
                  <div className="register-form container-fluid">
                  <h3>{this.state.confirmationMsg}</h3>
                  <button className="btn btn-default" onClick={this.handleCloseModal.bind(this)}>Exit</button>
                </div>
              }
              {
                  (this.state.showAttachFileForm) ?
                  <RegisterSandboxTemplate 
                      showAttachFileForm={this.state.showAttachFileForm}
                      closeAttachFileForm={this.handleCloseModal.bind(this)}
                      templateData={this.state.templateData} /> : null
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

RegisterTemplate.propTypes = {
    dataSet: PropTypes.object,
    handleCloseModal: PropTypes.func,
    showAttachFileForm: PropTypes.bool,
    closeAttachFileForm: PropTypes.func,
    attachFileForm: PropTypes.bool,
    disableForm: PropTypes.bool,
    editForm: PropTypes.bool,
    sandboxForm: PropTypes.bool,
    disableFileButton: PropTypes.bool
}
