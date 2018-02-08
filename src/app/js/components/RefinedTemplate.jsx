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

// actions
import * as TemplateActions from "../actions/TemplateActions";
import { isEmpty, convertBytes } from '../../../contents/js/utils/helpers.js';

// components
import ScriptForm from '../components/forms/ScriptForm';
import SourceTemplateForm from '../components/forms/SourceTemplateForm';
import TemplateInfoForm from '../components/forms/TemplateInfoForm';
import SourceTechnicalForm from '../components/forms/SourceTechnicalForm';
import FieldForm from '../components/forms/FieldForm';
import RegisterSandboxTemplate from '../components/RegisterSandboxTemplate';
import ErrorPage from '../../../contents/js/components/ErrorPage';

/*
* A parent functional component that defines the refined dataset workflow.
*/
export default class RefinedTemplate extends React.Component {
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
            scriptData: this.handleInitialData('scriptData'),
            sourceTemplatesList: this.handleInitialData('sourceTemplatesList'),
          defaultData: this.handleInitialData('defaultData'),
          technicalData: this.handleInitialData('technicalData'),
          fields: this.handleInitialData('fields'),
          attachFileData: {},
          editField: false,
          editFieldData: {},
          editFieldIndex: null,
          editSourceData: {},
            editSource: false,
          editSourceTemplateIndex: null,
          bitFlip: false,
          showAttachFileForm: false,
          templateData: {},
          editForm: (this.props.editForm ? this.props.editForm : false),
          sourceValidation: false,
          sourceError: 'Please add at least one source template.',
          errorModal: false,
          errorMsg: {}
        });
    }
    handleInitialData(tab) {
        // If the data set is provided initialize the default values (preview template)
        if (this.props.dataSet) {
            const data = this.props.dataSet.refinedDataset.file;
            const scriptData = this.props.dataSet.script;
            const fields = this.props.dataSet.refinedDataset.fields;
            const sourceList = this.props.dataSet.sourceTemplates;
            switch(tab) {
                case 'scriptData':
            return (
                {
                        path: scriptData.path,
                        name: scriptData.name,
                        description: scriptData.description,
                        owner: scriptData.owner,
                        schedule: scriptData.schedule
                    }
            );
            break;
          case 'sourceTemplatesList':
            return (sourceList);
            break;
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
                return (4);
                break;
            case 'tabIndex':
                return (0);
                break;
            case 'disabled':
                return (this.props.disableForm ? this.props.disableForm : false);
                break;
            case 'showTabs':
                return ({
                    script: true,
                    source: true,
                        default: true,
                        fields: true,
                        technical: true,
                        attachFile: true
                    });
                    break;
            }
        } else { // No data is provided these are default values
            switch(tab) {
                case 'scriptData':
            return ({});
            break;
          case 'sourceTemplatesList':
            return ([]);
            break;
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
                    script: true,
                    source: false,
                        default: false,
                        fields: false,
                        technical: false,
                        attachFile: false
                    });
                    break;
            }
        }
    }
    componentWillMount() {
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
        this.setState(
            this.getState()
        );

        //close parent forms
        if(this.props.closeAttachFileForm) {
            this.props.closeAttachFileForm();
        }

        //close view modal
        if(this.props.handleCloseModal) {
            this.props.handleCloseModal();
        }
    }
    handleAdvanceStep(e) {
        // Set up the tabs steps
        if (this.state.tabIndex < this.state.step) {
            this.setState({
                tabIndex: this.state.tabIndex + 1,
            });
        } else if(this.state.showTabs.script && !this.state.showTabs.source && !this.state.showTabs.default && !this.state.showTabs.technical && !this.state.showTabs.fields && !this.state.showTabs.attachFile) {
            this.setState({
                step: this.state.step + 1,
                tabIndex: this.state.tabIndex + 1,
                showTabs: {
                    script: true,
                    source: true,
                    default: false,
                    technical: false,
                    fields: false,
                    attachFile: false
                },
                bitFlip: !this.state.bitFlip
            });
            return (this.submitForm);
        } else if(this.state.showTabs.script && this.state.showTabs.source && !this.state.showTabs.default && !this.state.showTabs.technical && !this.state.showTabs.fields && !this.state.showTabs.attachFile) {
            this.setState({
                step: this.state.step + 1,
                tabIndex: this.state.tabIndex + 1,
                showTabs: {
                    script: true,
                    source: true,
                    default: true,
                    technical: false,
                    fields: false,
                    attachFile: false
                },
                bitFlip: !this.state.bitFlip
            });
            return (this.submitForm);
        } else if(this.state.showTabs.script && this.state.showTabs.source && this.state.showTabs.default && !this.state.showTabs.technical && !this.state.showTabs.fields && !this.state.showTabs.attachFile) {
            this.setState({
                step: this.state.step + 1,
                tabIndex: this.state.tabIndex + 1,
                showTabs: {
                    script: true,
                    source: true,
                    default: true,
                    technical: true,
                    fields: false,
                    attachFile: false
                },
                bitFlip: !this.state.bitFlip
            });
            return (this.submitForm);
        } else if (this.state.showTabs.script && this.state.showTabs.source && this.state.showTabs.default && this.state.showTabs.technical && !this.state.showTabs.fields && !this.state.showTabs.attachFile) {
            this.setState({
                step: this.state.step + 1,
                tabIndex: this.state.tabIndex + 1,
                showTabs: {
                    script: true,
                    source: true,
                    default: true,
                    technical: true,
                    fields: true,
                    attachFile: false
                },
                bitFlip: !this.state.bitFlip
            });
        } else {
            this.handleSubmitTemplate();
        }
    }
    handleCheckSource() {
        // Throw error is source list is empty
        if (!this.state.sourceValidation) {
            if (this.state.sourceTemplatesList.length === 0) {
                this.setState({
                    sourceValidation: true
                })
            } else {
                this.handleAdvanceStep();
            }
        } else if (this.state.sourceValidation) {
            if (this.state.sourceTemplatesList.length !== 0) {
                this.setState({
                    sourceValidation: false
                })
                this.handleAdvanceStep();
            }
        }
    }
    handleSaveTemplate() {
        return(
            console.log('TODO: Handle saving a draft template. ')
        );    
    }
    handleSubmitTemplate() {
        const _this = this;
        const defaultData = this.state.defaultData;
        const technicalData = this.state.technicalData;
        const fieldsData = this.state.fields;
        const scriptData = this.state.scriptData;
        const sourceTemplatesList = this.state.sourceTemplatesList;

        // add position to each field entry
        let fieldsPayload = [];
        let fieldObj = {};
        for(let x = 0; x < fieldsData.length; x++) {
            fieldObj = fieldsData[x]
            fieldObj.position = x + 1;
            fieldsPayload.push(fieldObj);
        }

        // define stage
        let templateStage = 'refined';

        // initialize source template list
        let templateList = [];

        for (let i = 0; i < sourceTemplatesList.length; i++) {
            // shape source template data
            let template = {};

            if (sourceTemplatesList[i].templateOption) {
                template = sourceTemplatesList[i].templateOption.value;
            } else {
                template = sourceTemplatesList[i];
            }

          let fieldList = [];
          // Build the field list that contains only names.
          for(let x = 0; x < sourceTemplatesList[i].fields.length; x++) {
              let name = sourceTemplatesList[i].fields[x].name;
              fieldList.push({name});
          }
          // Build the source template data.
            let sourceObj  = {
                file: {
                    key: template.file.key ? template.file.key : '',
                    guid: template.file.guid ? template.file.guid : ''
                },
                fields: fieldList,
                version: template.version
            };

            templateList.push(sourceObj);
        }

        let payload = {
            script: {
                path: scriptData.path ? scriptData.path : '',
                name: scriptData.name ? scriptData.name : '',
                description: scriptData.description ? scriptData.description : '',
                owner: scriptData.owner ? scriptData.owner : '',
                schedule: scriptData.schedule ? scriptData.schedule : '',
            },
            sourceTemplates: templateList,
            refinedDataset: {
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
                        containsHeaderRow: technicalData.containsHeaderRow ? technicalData.containsHeaderRow : ''
                    }
                },
                fields: fieldsPayload,
                stage: templateStage
            }
        }

        if(!this.state.editForm) {
            axios.post('/refinedDatasets', payload)
          .then(function (response) {
              _this.handleConfirmation({
                    confirmationMsg: defaultData.key + ' has been registered.',
                    dataObj: payload
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
        } else { //handle edit submission
            axios.put('/refinedDatasets/' + payload.refinedDataset.file.key, payload)
          .then(function (response) {
              _this.handleConfirmation({
                    confirmationMsg: defaultData.key + ' has been updated.',
                    dataObj: payload
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
      let modifiedFieldList = [];

      // rebuild field list and push the edited field in the same spot.
      for(let x = 0; x < this.state.fields.length; x++) {
          if (x !== this.state.editFieldIndex) {
              modifiedFieldList.push(this.state.fields[x]);
          } else {
              modifiedFieldList.push(values);
          }
      }
      this.setState({
          fields: modifiedFieldList
      });
      this.handleResetForm('fields');
  }
     handleCloneField(fieldPos, e) {
         // appends the specified field to the end of the list
      let modifiedFieldList = [];
      for(let x = 0; x < this.state.fields.length; x++) {
          modifiedFieldList.push(this.state.fields[x]);
      }
      modifiedFieldList.push(this.state.fields[fieldPos]);

      // always set new state never mutate existing
      this.setState({
          fields: modifiedFieldList,
      });
  }
  handleDeleteField(fieldPos, e) {
      // deleted specified field
      let newFieldList = [];
      for(let x = 0; x < this.state.fields.length; x++) {
          if(x !== fieldPos) {
              newFieldList.push(this.state.fields[x]);
          }
      }
      this.setState({
          fields: newFieldList
      });
      this.handleResetForm('fields');
  }
  handleDeleteSourceTemplate(templatePos, e) {
      // delete specified source template
      let newSourceList = [];
      for(let x = 0; x < this.state.sourceTemplatesList.length; x++) {
          if(x !== templatePos) {
              newSourceList.push(this.state.sourceTemplatesList[x]);
          }
      }
      this.setState({
          sourceTemplatesList: newSourceList
      });
      this.handleResetForm('source');
  }
  handleResetForm(type) {
      // 'resets' form, bitflip is to re-mount component
      if(type === 'fields') {
          this.setState({
              editField: false,
              editFieldData: {},
              editFieldIndex: null,
              bitFlip: !this.state.bitFlip
          });
      } else if (type === 'source') {
          this.setState({
              editSource: false,
              editSourceData: {},
              editSourceTemplateIndex: null,
              bitFlip: !this.state.bitFlip
          });
      }
  }
  handleOpenAttachForm() {
      this.setState({
          showAttachFileForm: true
      });
  }
  handleAddSourceTemplate(sourceTemplate) {
      // Add new field to the list, flip bit so the form resets (mounts a pristine field form)
      this.setState({
          sourceTemplatesList: this.state.sourceTemplatesList.concat([sourceTemplate]),
          bitFlip: !this.state.bitFlip,
          sourceValidation: false
      });
  }
  handleEditSourceTemplate(sourceTemplate) {
      let modifiedSourceList = [];

      // rebuild source list and push the edited source template in the same spot
      for(let x = 0; x < this.state.sourceTemplatesList.length; x++) {
          if (x !== this.state.editSourceTemplateIndex) { // check the edited source index
              modifiedSourceList.push(this.state.sourceTemplatesList[x]);
          } else {
              modifiedSourceList.push(sourceTemplate);
          }
      }
      // replace current template list with new modified
      this.setState({
          sourceTemplatesList: modifiedSourceList,
      });
      // reset form
      this.handleResetForm('source');
  }
  renderSourceTemplatesList() {
      let arr = [];
      for (let x = 0; x < this.state.sourceTemplatesList.length; x++) {
          let templateKey = '';
          if (this.state.sourceTemplatesList[x] && this.state.sourceTemplatesList[x].templateOption) { // if template came from new form
              templateKey = this.state.sourceTemplatesList[x].templateOption.value.file.key;
          } else { // if template data came from existing dataprop
              templateKey = this.state.sourceTemplatesList[x].file.key;
          }
          arr.push(
              <div className="field-list" key={x}>
              {
                    (!this.state.disabled) ?
                    <div className="field-list__buttons">
                        <span className="glyphicon glyphicon-pencil" aria-hidden="true" onClick={this.renderEditSourceForm.bind(this, x)}></span>
                        <span className="glyphicon glyphicon-minus" aria-hidden="true" onClick={this.handleDeleteSourceTemplate.bind(this, x)}></span>
                    </div> :
                    <div className="field-list__buttons">
                        <span className="glyphicon glyphicon-pencil" aria-hidden="true" onClick={this.renderEditSourceForm.bind(this, x)}></span>
                    </div>
                }
                {
                    (x !== this.state.editSourceTemplateIndex) ?
                    <div className="field-list__item">
                        {(x+1)}.&nbsp;
                        {templateKey}
                    </div> : 
                    <div className="field-list__item active">
                        {(x+1)}.&nbsp;
                        {templateKey}
                    </div>
                }
              </div>
          );
      }
      return(arr);
  }
  renderEditSourceForm(e, pos) {
      this.setState({
            editSourceData: this.state.sourceTemplatesList[e],
            editSourceTemplateIndex: e,
            editSource: true,
            bitFlip: !this.state.bitFlip
        });
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
    renderScriptTab() {
        return (
            <TabPanel>
        <ScriptForm
            formData={this.state.scriptData}
            handleCloseModal={this.handleCloseModal.bind(this)}
            handleOnChange={this.handleChange.bind(this)}
            handleAdvanceStep={this.handleAdvanceStep.bind(this)}
            disabled={this.state.disabled}
            editForm={this.state.editForm} />
            </TabPanel>
        );
    }
    renderSourceTemplateTab() {
        let continueTxt = (this.state.editForm) ? 'Save Changes' : 'Continue';
        return (
            <TabPanel>
                <div className="sourcetemplates-container">
                    <div className="new-field-list__container">
                <h5>Source Template List:</h5>
                {
                    (this.state.sourceTemplatesList && (this.state.sourceTemplatesList.length !== 0)) &&
                    <div className="new-field-list__items">{this.renderSourceTemplatesList()}</div>
                }
            </div>
            <div className="field-form">
                {
                    (this.state.bitFlip && !this.state.editSource) &&    
                    <SourceTemplateForm
                        handleAddSourceTemplate={this.handleAddSourceTemplate.bind(this)}
                        handleEditSourceTemplate={this.handleEditSourceTemplate.bind(this)}
                        handleCloseModal={this.handleCloseModal.bind(this)}
                        handleAdvanceStep={this.handleAdvanceStep.bind(this)}
                        disabled={this.state.disabled}
                        editMode={this.state.editForm}
                        resetForm={this.handleResetForm.bind(this, 'source')}
                        newForm={this.props.newForm} />
                  }
                  {
                    (!this.state.bitFlip && !this.state.editSource) &&    
                    <SourceTemplateForm
                        handleAddSourceTemplate={this.handleAddSourceTemplate.bind(this)}
                        handleEditSourceTemplate={this.handleEditSourceTemplate.bind(this)}
                        handleCloseModal={this.handleCloseModal.bind(this)}
                        handleAdvanceStep={this.handleAdvanceStep.bind(this)}
                        disabled={this.state.disabled}
                        editMode={this.state.editForm}
                        resetForm={this.handleResetForm.bind(this, 'source')}
                        newForm={this.props.newForm} />
                  }
                  {
                    (this.state.bitFlip && this.state.editSource) &&    
                    <SourceTemplateForm
                        sourceTemplateData={this.state.editSourceData}
                        handleAddSourceTemplate={this.handleAddSourceTemplate.bind(this)}
                        handleEditSourceTemplate={this.handleEditSourceTemplate.bind(this)}
                        handleCloseModal={this.handleCloseModal.bind(this)}
                        handleAdvanceStep={this.handleAdvanceStep.bind(this)}
                        disabled={this.state.disabled}
                        editMode={this.state.editSource}
                        resetForm={this.handleResetForm.bind(this, 'source')}
                        newForm={this.props.newForm} />
                  }
                  {
                    (!this.state.bitFlip && this.state.editSource) &&    
                    <SourceTemplateForm
                        sourceTemplateData={this.state.editSourceData}
                        handleAddSourceTemplate={this.handleAddSourceTemplate.bind(this)}
                        handleEditSourceTemplate={this.handleEditSourceTemplate.bind(this)}
                        handleCloseModal={this.handleCloseModal.bind(this)}
                        handleAdvanceStep={this.handleAdvanceStep.bind(this)}
                        disabled={this.state.disabled}
                        editMode={this.state.editSource}
                        resetForm={this.handleResetForm.bind(this, 'source')}
                        newForm={this.props.newForm} />
                  }
                  {
                      (this.state.sourceValidation) &&
                      <div className="FormError">{this.state.sourceError}</div>
                  }
                <div className="form-button-group">
                            <button className="btn btn-default" onClick={this.handleCloseModal.bind(this)}>Exit</button>
                      <button className="btn btn-default" onClick={this.handleCheckSource.bind(this)}>{continueTxt}</button>
                  </div>
                    </div>
                </div>
            </TabPanel>
        );
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
            editForm={this.state.editForm} />
            </TabPanel>
        );
    }
    renderTechnicalTab() {
        return (
            <TabPanel>
                <SourceTechnicalForm
                    formData={this.state.technicalData}
                    handleCloseModal={this.handleCloseModal.bind(this)}
            handleOnChange={this.handleChange.bind(this)}
            handleAdvanceStep={this.handleAdvanceStep.bind(this)}
            disabled={this.state.disabled}
            editForm={this.state.editForm} />
            </TabPanel>
        );
    }
    renderFieldsTab() {
        let continueTxt = (this.state.editForm) ? 'Submit Changes' : 'Register Template';
        return (
            <TabPanel>
                <div className="fields-container">
                    <div className="new-field-list__container">
                <h5>Field List:</h5>
                        {
                    (this.state.fields && (this.state.fields.length !== 0)) &&
                    <div className="new-field-list__items">{this.renderFields()}</div>
                }
            </div>
            <div className="field-form">
                {
                    (this.state.bitFlip && !this.state.editField) &&    
                            <FieldForm
                                handleAddField={this.handleAddField.bind(this)}
                                editMode={this.state.editField}
                                disabled={this.state.disabled} />    
                        }
                        {
                    (!this.state.bitFlip && !this.state.editField) &&
                            <FieldForm
                                handleAddField={this.handleAddField.bind(this)}
                                editMode={this.state.editField}
                                disabled={this.state.disabled} />
                        }
                        {
                            (this.state.bitFlip && this.state.editField) &&
                            <FieldForm
                                fieldData={this.state.editFieldData}
                                editMode={this.state.editField}
                                resetForm={this.handleResetForm.bind(this, 'fields')}
                                handleEditField={this.handleEditField.bind(this)}
                                disabled={this.state.disabled} />
                        }
                        {
                            (!this.state.bitFlip && this.state.editField) &&
                            <FieldForm
                                fieldData={this.state.editFieldData}
                                editMode={this.state.editField}
                                resetForm={this.handleResetForm.bind(this, 'fields')}
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
                {
                    ((this.state.confirmation === false) && (!this.state.showAttachFileForm)) &&
                    <div className="register-form container-fluid">
                    {
                        (this.state.step === 0) &&
                        <div>
                          <Tabs selectedIndex={this.state.tabIndex} onSelect={tabIndex => this.setState({ tabIndex })}>
                                        <TabList>
                                            <Tab>Script Information</Tab>
                                        </TabList>
                                        {this.renderScriptTab()}
                                    </Tabs>
                                </div>
                            }
                            {
                        (this.state.step === 1) &&
                        <div>
                          <Tabs selectedIndex={this.state.tabIndex} onSelect={tabIndex => this.setState({ tabIndex })}>
                                        <TabList>
                                            <Tab>Script Information</Tab>
                                            <Tab>Source Templates</Tab>
                                        </TabList>
                                        {this.renderScriptTab()}
                                        {this.renderSourceTemplateTab()}
                                    </Tabs>
                                </div>
                            }
                    {
                        (this.state.step === 2) &&
                        <div>
                          <Tabs selectedIndex={this.state.tabIndex} onSelect={tabIndex => this.setState({ tabIndex })}>
                                        <TabList>
                                            <Tab>Script Information</Tab>
                                            <Tab>Source Templates</Tab>
                                            <Tab>Template Information</Tab>
                                        </TabList>
                                        {this.renderScriptTab()}
                                        {this.renderSourceTemplateTab()}
                                        {this.renderDefaultTab()}
                                    </Tabs>
                                </div>
                            }
                            {
                        (this.state.step === 3) &&
                        <div>
                          <Tabs selectedIndex={this.state.tabIndex} onSelect={tabIndex => this.setState({ tabIndex })}>
                                        <TabList>
                                            <Tab>Script Information</Tab>
                                            <Tab>Source Templates</Tab>
                                            <Tab>Template Information</Tab>
                                            <Tab>Technical</Tab>
                                        </TabList>
                                        {this.renderScriptTab()}
                                        {this.renderSourceTemplateTab()}
                                        {this.renderDefaultTab()}
                                        {this.renderTechnicalTab()}
                                    </Tabs>
                                </div>
                            }
                            {
                        (this.state.step === 4) &&
                        <div>
                          <Tabs selectedIndex={this.state.tabIndex} onSelect={tabIndex => this.setState({ tabIndex })}>
                                        <TabList>
                                            <Tab>Script Information</Tab>
                                            <Tab>Source Templates</Tab>
                                            <Tab>Template Information</Tab>
                                            <Tab>Technical</Tab>
                                            <Tab>Fields</Tab>
                                        </TabList>
                                        {this.renderScriptTab()}
                                        {this.renderSourceTemplateTab()}
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

RefinedTemplate.propTypes = {
    dataSet: PropTypes.object,
    handleCloseModal: PropTypes.func,
    showAttachFileForm: PropTypes.bool,
    closeAttachFileForm: PropTypes.func,
    attachFileForm: PropTypes.bool,
    disableForm: PropTypes.bool,
    editForm: PropTypes.bool,
    sandboxForm: PropTypes.bool,
    newForm: PropTypes.bool
}
