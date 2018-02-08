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
import Loader from 'react-loader';

// stores & actions
import TemplateStore from '../stores/TemplateStore';
import * as TemplateActions from "../actions/TemplateActions";
import UserStore from '../../../contents/js/stores/UserStore';
import * as UserActions from "../../../contents/js/actions/UserActions";

// components
import SummaryViewItem from '../components/SummaryViewItem';

// helpers
import { isEmpty, hasPermission, isAdmin, addCommas } from '../../../contents/js/utils/helpers.js';
import { dataSetOne } from '../../../contents/js/constants/mockData';


/*
* The summary view that displays all the current summary information
*/
export default class TestComponent extends React.Component {
    constructor(props) {
    super(props);
    this.state = this.getState();
  }
  getState() {      
    return ({
      userData: {},
      summary: [],
      currentPage: 1,
      itemsPerPage: 10,
      indexOfFirstItem: 0,
      indexOfLastItem: 0,
      loaded: false
    });
  }
  componentWillMount() {      
    TemplateStore.on("change", () => {
      this.setState({
        userData: UserStore.getUserData(),
        summary: TemplateStore.getTemplateData()[0],
        loaded: true
      });      
    });
    // Use this method only if the async call in the action store fails
    // this.handleRequest();
  }
  handleOnPageClick(e) {
    e.stopPropagation();
    this.setState({
      currentPage: Number(e.target.id)
    });
  }
  handleRequest() {
    let _this = this;
    axios.get('/fileMetadata/summary?begin=1&end=10')
      .then(function (response) {
        _this.setState({
          summary: response.data
        });
      })
      .catch(function (error) {
        console.log(error);
      }
    );
  }
  handleViewItems() {
    const { summary, currentPage, itemsPerPage } = this.state;

    if (!isEmpty(this.state.summary) && this.state.summary.summary) {
      const summaryList = summary.summary;
      const indexOfLastItem = currentPage * itemsPerPage;
      const indexOfFirstItem = indexOfLastItem - itemsPerPage;
      const currentItems = summaryList.slice(indexOfFirstItem, indexOfLastItem);

      let elem = [];
      currentItems.map((item, index) => {
        //options needs to be built out for each template (unfortunately)
        let options = {};
        let viewOP = ((isAdmin(this.state.userData)) || hasPermission(this.state.userData, 'VIEW'));
        let deleteOP = ((isAdmin(this.state.userData)) || hasPermission(this.state.userData, 'DELETE'));
        let editOP = false;

        if(isAdmin(this.state.userData)) { //check user if they are admin
          editOP = true;
        } else if ((hasPermission(this.state.userData, 'EDIT_FINAL')) && item.stage === 'final') { //enable edit for final
          editOP = true;
        } else if ((hasPermission(this.state.userData, 'EDIT')) && item.stage !== 'final' ) { //enable edit for sandbox and refine
          editOP = true;
        }

        options = {
          view: viewOP,
          edit: editOP,
          delete: deleteOP
        }

        if(item.stage !== 'refined') {
          elem.push(
            <SummaryViewItem
              dataSet={item}
              key={item.file.key}
              options={options}
              refined={false}/>
          );
        } else {
          elem.push(
            <SummaryViewItem
              dataSet={item}
              key={item.refinedDataset.file.key}
              options={options}
              refined={true}/>
          );
        }
      })
      return elem;
    }
  }
  handlePreviousClick() {
    this.setState({
      currentPage: (this.state.currentPage - 1)
    });
  }
  handleNextClick() {
    this.setState({
      currentPage: (this.state.currentPage + 1)
    });
  }
  renderPageNumbers() {
    const pageNum = [];
    const pageRend = [];
    if (!isEmpty(this.state.summary) && this.state.summary.summary) {
      for (let x = 1; x <= Math.ceil(this.state.summary.summary.length / this.state.itemsPerPage); x++) {
        pageNum.push(x);
      }
    }
    pageNum.map(number => {
      if (number === this.state.currentPage) {
        pageRend.push(
          <li key={number} className="active">
            <a onClick={this.handleOnPageClick.bind(this)} id={number}>
              <strong>{number}</strong>
            </a>
          </li>
        );
      } else {
        pageRend.push(
          <li key={number}>
            <a onClick={this.handleOnPageClick.bind(this)} id={number}>
              {number}
            </a>
          </li>
        );
      }
      
    })
    return pageRend;
  }
  renderTotalCount() {
    const { summary, currentPage, itemsPerPage } = this.state;
    if (!isEmpty(this.state.summary) && this.state.summary.summary) {
      const summaryList = summary.summary;
      const indexOfLastItem = currentPage * itemsPerPage;
      const indexOfFirstItem = indexOfLastItem - itemsPerPage;
      const currentItems = summaryList.slice(indexOfFirstItem, indexOfLastItem);
      return (
        <div className="summary-view__count">
          {indexOfFirstItem + 1}-{indexOfFirstItem + currentItems.length} of {addCommas(summaryList.length)}
        </div>
      );
    }
  }
  renderLeftArrow() {
    let arrowRender = [];
    const { summary, currentPage, itemsPerPage } = this.state;

    // render an disabled button if the user is on the first page.
    if (!isEmpty(this.state.summary) && this.state.summary.summary) {
      if(this.state.currentPage === 1) {
        arrowRender.push(
          <li className='disabled' key='left-arrow'>
            <a href="#" aria-label="Previous">
              <span aria-hidden="true">&laquo;</span>
            </a>
          </li>
        );
      } else { // allow the button to be clicked if not on the first page
        arrowRender.push(
          <li className='enabled' onClick={this.handlePreviousClick.bind(this)} key='left-arrow'>
            <a href="#" aria-label="Previous">
              <span aria-hidden="true">&laquo;</span>
            </a>
          </li>
        );
      }
    }
    return arrowRender;
  }
  renderRightArrow() {
    let arrowRender = [];
    const { summary, currentPage, itemsPerPage } = this.state;

    // render an disabled button if the user is on the last page.
    if (!isEmpty(this.state.summary) && this.state.summary.summary) {
      if(this.state.currentPage === (Math.ceil(this.state.summary.summary.length / this.state.itemsPerPage))) {
        arrowRender.push(
          <li className='disabled' key='right-arrow'>
            <a href="#" aria-label="Next">
            <span aria-hidden="true">&raquo;</span>
          </a>
          </li>
        );
      } else { // allow the button to be clicked if not on the last page
        arrowRender.push(
          <li className='enabled' onClick={this.handleNextClick.bind(this)} key='right-arrow'>
            <a href="#" aria-label="Next">
            <span aria-hidden="true">&raquo;</span>
          </a>
          </li>
        );
      }
    }
    return arrowRender;
  }
  render() {
    return (
      <div className="summary-view">
       {
        (this.state.loaded) ? 
        <div>
          {this.renderTotalCount()}
          <table className="sv-table bordered">
            <tbody>
              <tr className="sv-table__header">
                <th className="text-center">options:</th>
                <th>version:</th>
                <th>template:</th>
                <th>stage:</th> 
                <th>subject area:</th>
                <th>data steward:</th>
                <th>owner:</th>
              </tr>
              {this.handleViewItems()}
            </tbody>
          </table>
          {
              (isEmpty(this.state.summary) || isEmpty(this.state.summary.summary) || this.state.summary.summary.length === 0) ?
                  <div>There are no templates available.</div>: null
           }
        </div> : 
        <div>
          <div className="count-placeholder"></div>
          <table className="sv-table bordered">
            <tbody>
              <tr className="sv-table__header">
                <th className="text-center">options:</th>
                <th>version:</th>
                <th>template:</th>
                <th>stage:</th> 
                <th>subject area:</th>
                <th>data steward:</th>
                <th>owner:</th>
              </tr>
            </tbody>
          </table>
          <div className="loader-block">
            <Loader loaded={this.state.loaded}></Loader>
          </div>
        </div>
       }
        <div>
          <nav aria-label="Page navigation">
            <ul className="pagination pagination-sm">
              {this.renderLeftArrow()}
              {this.renderPageNumbers()}
              {this.renderRightArrow()}
            </ul>
          </nav>
        </div>
      </div>
    );
  }
}
