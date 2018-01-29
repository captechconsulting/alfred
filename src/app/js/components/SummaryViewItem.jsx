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

//components
import RegisterTemplate from '../components/RegisterTemplate';
import RefinedTemplate from '../components/RefinedTemplate';
import DeleteConfirmation from '../components/DeleteConfirmation';

/*
* A presentation component that displays the template information
* used in: SummaryView component
*/
export default class SummaryViewItem extends React.Component {
    constructor(props) {
    super(props);
    this.state = this.getState();
  }
  getState() {
        return ({
            showModal: false,
          confirmation: false,
          attachFileForm: false,
          disableForm: false,
          editForm: false,
          showDeleteModal: false,
          sandbox: false,
          refined: false,
          newForm: true
        });
    }
    componentWillMount() {
        if (this.props.dataSet.stage === 'sandbox') {
            this.setState({
                sandbox: true
            });
        } else if (this.props.dataSet.stage === 'final') {
            this.setState({
                sandbox: false
            });
        } else if (this.props.dataSet.stage === 'refined') {
            this.setState({
                refined: true
            });
        }
    }
    handleCloseModal() {
        this.setState(
            this.getState()
        );
    }
    handleOpenDisabledForm() {
        if(this.props.options.view) {
            this.setState({
                showModal: true,
                disableForm: true
            });
        }
    }
    handleOpenEditForm() {
        // just another check to be robust
        if(this.props.options.edit) {
            this.setState({
                showModal: true,
                editForm: true
            });
        }
    }
    handleDeleteForm() {
        if(this.props.options.delete) {
            this.setState({
                showDeleteModal: true
            });
        }
    }
    renderOptions() {
        let opts = []
        for (let option in this.props.options) {
            if (this.props.options.hasOwnProperty(option)) {
                if (option === 'view' && this.props.options[option]) {
                    opts.push(
                        <span
                            className="glyphicon glyphicon-eye-open"
                            aria-hidden="true"
                            key={option}
                            onClick={this.handleOpenDisabledForm.bind(this)}></span>
                    );
                } else if (option === 'edit' && this.props.options[option]) {
                    opts.push(
                        <span
                            className="glyphicon glyphicon-edit"
                            aria-hidden="true"
                            key={option}
                            onClick={this.handleOpenEditForm.bind(this)}></span>
                    );
                } else if (option === 'delete' && this.props.options[option]) {
                    opts.push(
                        <span
                            className="glyphicon glyphicon-trash"
                            aria-hidden="true"
                            key={option}
                            onClick={this.handleDeleteForm.bind(this)}></span>
                    );
                }
            }
        }
        return opts;
    }
    render() {
        let template = {};
        if(!this.props.refined) {
            template = this.props.dataSet;
        } else {
            template = this.props.dataSet.refinedDataset;
        }
    return (
          <tr className="sv__template-row">
              <td>
                  <div className="sv-template__icons-container">{this.renderOptions()}</div>
              </td>
              <td className="sv__template-row__medium-row" onClick={this.handleOpenDisabledForm.bind(this)}>{template.version}</td>
            <td onClick={this.handleOpenDisabledForm.bind(this)}>{template.file.key}</td>
            <td onClick={this.handleOpenDisabledForm.bind(this)}>{template.stage}</td> 
            <td className="sv__template-row__small-row" onClick={this.handleOpenDisabledForm.bind(this)}>{template.file.subjectArea}</td>
            <td className="sv__template-row__small-row" onClick={this.handleOpenDisabledForm.bind(this)}>{template.file.business.dataSteward}</td>
            <td className="sv__template-row__small-row" onClick={this.handleOpenDisabledForm.bind(this)}>{template.file.business.owner}</td>
            {
                (!this.props.refined) ?
                <td style={{display:'none'}}>
                {
                    (this.state.showModal) ?
                    <RegisterTemplate
                        showForm={this.state.showModal}
                        dataSet={this.props.dataSet}
                        handleCloseModal={this.handleCloseModal.bind(this)}
                        attachFileForm={this.state.attachFileForm}
                        disableForm={this.state.disableForm}
                        editForm={this.state.editForm}
                        sandboxForm={this.state.sandbox}
                        disableFileButton={true}/> : null
                }
                {
                    (this.state.showDeleteModal) ?
                    <DeleteConfirmation
                        // handleConfirmDelete={} this is to call templateAction to delete
                        showForm={this.state.showDeleteModal}
                        handleCloseModal={this.handleCloseModal.bind(this)}
                        templateKey={this.props.dataSet.file.key}
                     /> : null
                }
                   </td> :
                   <td style={{display:'none'}}>
                {
                    (this.state.showModal) ?
                    <RefinedTemplate
                        showForm={this.state.showModal}
                        dataSet={this.props.dataSet}
                        closeAttachFileForm={this.handleCloseModal.bind(this)}
                    attachFileForm={false}
                        disableForm={this.state.disableForm}
                        editForm={this.state.editForm}
                        sandboxForm={this.state.sandbox}
                        newForm={false}/> : null
                }
                {
                    (this.state.showDeleteModal) ?
                    <DeleteConfirmation
                        showForm={this.state.showDeleteModal}
                        handleCloseModal={this.handleCloseModal.bind(this)}
                        templateKey={this.props.dataSet.refinedDataset.file.key}
                     /> : null
                }
                   </td> 
            }
          </tr>
    );
  }
}

SummaryViewItem.propTypes = {
    dataSet: PropTypes.object.isRequired,
    options: PropTypes.object,
    refined: PropTypes.bool
}
