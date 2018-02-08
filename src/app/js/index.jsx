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
import {render} from 'react-dom';
import Loader from 'react-loader';

// stores & actions
import UserStore from '../../contents/js/stores/UserStore';
import * as UserActions from "../../contents/js/actions/UserActions";
import TemplateStore from './stores/TemplateStore';
import * as TemplateActions from "./actions/TemplateActions";

// components
import SummaryView from './components/SummaryView';
import RegisterTemplateButton from './components/RegisterTemplateButton';
import RegisterSandboxButton from './components/RegisterSandboxButton';
import RefinedTemplateButton from './components/RefinedTemplateButton';
import RStudioLink from './components/RStudioLink';
import UserProfile from '../../contents/js/components/UserProfile';
import ResetPasswordForm from '../../contents/js/components/ResetPassword';

// helper methods
import { isEmpty } from '../../contents/js/utils/helpers.js';

// styles
import style from '../../contents/css/main.scss';

class App extends React.Component {
    constructor(props) {
    super(props);
    this.state = {            
      userData: UserStore.getUserData(),
      summary: TemplateStore.getTemplateData()[0],
      errorStatus: UserStore.getErrorStatus(),
      loaded: false
    };
    
  }
  componentWillMount() {
    UserStore.on("change", () => {
      this.setState({
        userData: UserStore.getUserData(),
        errorStatus: UserStore.getErrorStatus(),
          loaded: true
      });     
    });
    TemplateStore.on("change", () => {
      this.setState({
        summary: TemplateStore.getTemplateData()[0]
      });
    });
    UserActions.fetchUserData();    
  }
  renderEnvironmentIndicator() {
    if (!isEmpty(this.state.summary)) {
      switch(this.state.summary.env) {
        case 'local': {
          return(
            <span className="indicator-box">
              <span className="indicator-box--red">local</span>
            </span>);
          break;
        }
        case 'dev': {
          return(
            <span className="indicator-box">
              <span className="indicator-box--blue">dev</span>
            </span>);
          break;
        }
        case 'test': {
          return(
            <span className="indicator-box">
              <span className="indicator-box--yellow">test</span>
            </span>);
          break;
        }
        case 'production': {
          return(
            <span className="indicator-box">
              <span className="indicator-box--green">production</span>
            </span>);
          break;
        }
        default: {
          return([]);
        }
      }
    }
  }
  render () {
    return (
        <div>
            {
                (isEmpty(this.state.userData)) &&
          <div className="container">
                  <div className="login-container">
                      <h1 className="login-container__icon">
                              <span className="glyphicon glyphicon-equalizer" aria-hidden="true"></span>
                <span className="login-container__title"> Alfred </span>
                          </h1>
                  </div>
          </div>
            }
        {
          (this.state.errorStatus && this.state.errorStatus.resetPassword) &&
          <div className="container">
            <div className="login-container">
              <h1>
                <span className="glyphicon glyphicon-equalizer" aria-hidden="true"></span> Alfred 
              </h1>
              <ResetPasswordForm />
            </div>
          </div>
        }
        {
          (!isEmpty(this.state.userData) && !this.state.errorStatus.resetPassword) &&
          <div className="container">
            <div className="header row">
              <div className="col-xs-12 header-row">
                <h1 className="login-container__icon">
                  <span className="glyphicon glyphicon-equalizer" aria-hidden="true"></span> 
                  <span className="login-container__title"> Alfred </span>
                  {this.renderEnvironmentIndicator()}
                </h1>
                <UserProfile />
              </div>
            </div>
            <div className="main-content row">
              <div className="col-xs-12 col-md-8 button-nav">
                <RegisterTemplateButton />
                <RegisterSandboxButton />
                <RefinedTemplateButton />
                <RStudioLink />
              </div>
              <div className="col-xs-12">
                <SummaryView />
              </div>
            </div>
          <div className="footer row">
              
          </div>
          </div>
        }
          </div>
    );
  }
}

render(<App/>, document.getElementById('app'));
