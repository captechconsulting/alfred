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
import Select from 'react-select';
import ReactModal from 'react-modal';

// stores & actions
import TemplateStore from '../../stores/TemplateStore';
import * as TemplateActions from "../../actions/TemplateActions";

//components
import FieldForm from '../../components/forms/FieldForm';

/*
* A stateful component that handles the ability to select templates from a list &
* allows users to choose fields to be added to the source template data.
* used in: Refined Template form
*/
export default class SourceTemplateForm extends React.Component {
    constructor(props) {
    super(props);
    this.state = this.getState();
  }
  getState() {
        return ({
          summary: TemplateStore.getTemplateData()[0],
          bitFlip: false,
          fields: [],
          viewFieldIndex: 0
        });
    }
    componentWillMount() {
        if(this.props.sourceTemplateData && this.props.sourceTemplateData.templateOption) {
            this.setState({
                templateOption: this.props.sourceTemplateData.templateOption,
                fields: this.props.sourceTemplateData.fields
            })
        } else {
            if (this.props.sourceTemplateData) {
                // build out the templateOption payload
                let currentTemplate = {};
                if (this.state.summary && this.state.summary.summary && this.state.summary.summary.length !== 0) {
                    for (let template in this.state.summary.summary) {
                        if (this.state.summary.summary.hasOwnProperty(template)) {
                            if (this.state.summary.summary[template].stage !== 'refined') {
                                if (this.state.summary.summary[template].file.key === this.props.sourceTemplateData.file.key) {
                                    currentTemplate = {
                                        label: this.props.sourceTemplateData.file.key,
                                        value: this.state.summary.summary[template]
                                    }
                                }
                            }
                        }
                    }
                }
                this.setState({
                    templateOption: currentTemplate, 
                    fields: this.props.sourceTemplateData.fields
                })
            }
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
        this.setState({
            showModal: false
        });
    }
  onAddSource() {
      let sourceObj = {
          templateOption: this.state.templateOption,
          fields: this.state.fields
      }
        this.props.handleAddSourceTemplate(sourceObj);
  }
  onSaveSource() {
      let sourceObj = {
          templateOption: this.state.templateOption,
          fields: this.state.fields
      }
      this.props.handleEditSourceTemplate(sourceObj);
  }
  inputChange(val) {
        if (val !== null) { // new template selected
          this.setState({
              templateOption: val,
              fields: [],
              bitFlip: !this.state.bitFlip
          })
        } else { // resets select and state
            this.setState({
                templateOption: '',
              fields: [],
              bitFlip: !this.state.bitFlip
            });
        }
    }
  renderExistingTemplateSelect() {
      if(this.props.sourceTemplateData && this.props.sourceTemplateData.templateOption) {
          if(!this.props.disabled) {
              return(
                    <Select
                      name="form-field-name"
                      value={this.props.sourceTemplateData.templateOption.value.file.key}
                      options={
                          [
                              {
                                  value: this.props.sourceTemplateData.templateOption.value.file.key,
                                  label: this.props.sourceTemplateData.templateOption.value.file.key
                              }
                            ]
                      }
                      focusedOption={{
                          value: this.props.sourceTemplateData.templateOption.value.file.key,
                          label: this.props.sourceTemplateData.templateOption.value.file.key
                      }}
                      disabled={true}
                    />
                );
          } else {
              return(
                    <Select
                      name="form-field-name"
                      value={this.props.sourceTemplateData.file.key}
                      options={
                          [
                              {
                                  value: this.props.sourceTemplateData.file.key,
                                  label: this.props.sourceTemplateData.file.key
                              }
                            ]
                      }
                      focusedOption={{
                          value: this.props.sourceTemplateData.file.key,
                          label: this.props.sourceTemplateData.key
                      }}
                      disabled={true}
                    />
                );
          }
      } else {
            let optionsD = [];
        if (this.state.summary && this.state.summary.summary && this.state.summary.summary.length !== 0) {
                for (let template in this.state.summary.summary) {
                    if (this.state.summary.summary.hasOwnProperty(template)) {
                        if (this.state.summary.summary[template].stage !== 'refined') {
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
                  disabled={this.props.disabled}
                />
            );
      }
    }
    renderFieldsList() {
        let fieldList = [];
        if (this.state.templateOption && this.state.templateOption.length !== 0) {
            for (let x = 0; x < this.state.templateOption.value.fields.length; x++) {
                let icon = 'glyphicon glyphicon-unchecked';
                // this compares the fields names (TODO pass all field data through to be)
                if (this.props.editMode) {
                    let compare = '';
                    if (this.props.newForm) {
                        compare = JSON.stringify(this.state.templateOption.value.fields[x]);
                    } else {
                        let newCompare = {};
                        for (let attr in this.state.templateOption.value.fields[x]) {
                            if (this.state.templateOption.value.fields[x].hasOwnProperty(attr)) {
                                if(attr === 'name') {
                                    newCompare[attr] = this.state.templateOption.value.fields[x][attr];
                                }
                            }
                        }
                        compare = JSON.stringify(newCompare);
                    }
                    for (let i = 0; i < this.state.fields.length; i++) {
                        let check = '';
                        if (this.props.newForm) {
                            check = JSON.stringify(this.state.fields[i]);
                        } else {
                            let newCheck = {};
                            for (let attr in this.state.fields[i]) {
                                if (this.state.fields[i].hasOwnProperty(attr)) {
                                    if(attr === 'name') {
                                        newCheck[attr] = this.state.fields[i][attr];
                                    }
                                }
                            }
                            check = JSON.stringify(newCheck);
                        }
                        if (compare === check) {
                            icon = 'glyphicon glyphicon-check';
                        }
                    }
                }
                
                fieldList.push(
                    <div key={x}>
                        <span
                            ref={"star_" + x}
                            className={icon}
                            aria-hidden="true" 
                            onClick={this.handleSaveField.bind(this, x)}></span>
                        <span
                            className="glyphicon glyphicon-eye-open"
                            aria-hidden="true" 
                            onClick={this.handleViewField.bind(this, x)}></span>
                        <span>{x}. {this.state.templateOption.value.fields[x].name}</span>
                    </div>
                );
            }
        }
        return fieldList;
    }
    handleSaveField(e, pos) {
        if (!this.props.disabled) {
            let compare = ''; // field to check
            let current  = JSON.stringify(this.state.templateOption.value.fields[e]); // current field adding to the list

            if (this.state.fields.length === 0 ) {
                // list is empty so add to the fieldList
                this.setState({
                  fields: this.state.fields.concat(this.state.templateOption.value.fields[e])
              });
            } else {
                for (let x = 0; x < this.state.fields.length; x++) {
                    compare = JSON.stringify(this.state.fields[x]);
                    if(current === compare) { // if field exist in current field list remove item
                        this.setState({
                            fields: this.state.fields.filter((field) => {
                              return (JSON.stringify(field) !== current);
                            })
                        })
                        break;
                    } else if(x === (this.state.fields.length - 1)) { // if field does not exist append to the end of the list
                        this.setState({
                          fields: this.state.fields.concat(this.state.templateOption.value.fields[e])
                      });
                    }
                }
            }
          // flip icons
            this.swapIcons(e);
        }
    }
    handleViewField(e, pos) {
        this.setState({
            showModal: true,
            viewFieldIndex: e
        })
    }
    swapIcons(e) {
        let field = 'star_' + e;
        if (this.refs[field].className === 'glyphicon glyphicon-unchecked') {
            this.refs[field].className = 'glyphicon glyphicon-check';
        } else {
            this.refs[field].className = 'glyphicon glyphicon-unchecked';
        }
    }
    render() {
        return (
            <div>
                <h6>Please select an existing template:</h6>
                {this.renderExistingTemplateSelect()}
                {
            (this.state.templateOption && this.state.templateOption.length !== 0) ?
            <div>
                {
                    (!this.state.bitFlip) &&
                    <div>
                        <h4>Field List:</h4>
                        <div className="field-list-preview">
                            {this.renderFieldsList()}
                        </div>
                    </div>
                }
                {
                    (this.state.bitFlip) &&
                    <div>
                        <h4>Field List:</h4>
                        <div className="field-list-preview">
                            {this.renderFieldsList()}
                        </div>
                    </div>
                }
                {
                    (this.state.showModal) && 
                    <ReactModal 
                       isOpen={this.state.showModal}
                       contentLabel="Field Attributes"
    		           style={this.adjustModalStyles()}
			        >
			        	<h4>Field Details</h4>
			        	<FieldForm 
				        	fieldData={this.state.templateOption.value.fields[this.state.viewFieldIndex]}
									editMode={false}
									disabled={true} />
			        	<button className="btn btn-default" onClick={this.handleCloseModal.bind(this)}>Return</button>
			        </ReactModal>
	        	}
	        	{
	          	(!this.props.disabled) &&
	          	((!this.props.editMode) ?
		          <div className="form-button-group">
		          	<button className="btn btn-default" onClick={this.props.resetForm}>Reset</button>
					      <button className="btn btn-default" onClick={this.onAddSource.bind(this)}>Add Source Template</button>
				      </div> :
		          <div className="form-button-group">
		          	<button className="btn btn-default" onClick={this.props.resetForm}>Cancel</button>
		          	<button className="btn btn-default" onClick={this.onSaveSource.bind(this)}>Save Template</button>
		          </div>)
		        }
        	</div> : <div className="placeHolder"></div>
        }
			</div>
		);
	}
}

SourceTemplateForm.propTypes = {
	editMode: PropTypes.bool.isRequired,
	sourceTemplateData: PropTypes.object,
	handleAddSourceTemplate: PropTypes.func,
	handleEditSourceTemplate: PropTypes.func,
	handleCloseModal: PropTypes.func.isRequired,
	handleAdvanceStep: PropTypes.func.isRequired,
	disabled: PropTypes.bool.isRequired,
	resetForm: PropTypes.func,
	newForm: PropTypes.bool
}
