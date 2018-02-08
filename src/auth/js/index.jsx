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
import { render } from 'react-dom';
import Loader from 'react-loader';

//stores & actions
import UserStore from '../../contents/js/stores/UserStore';
import * as UserActions from "../../contents/js/actions/UserActions";

//components
import UserProfile from '../../contents/js/components/UserProfile';
import UserMgmt from './components/UserMgmt';

//helper methods
import { isEmpty } from '../../contents/js/utils/helpers.js';

//styles
import style from '../../contents/css/main.scss';

class Auth extends React.Component {
    constructor( props ) {
        super( props );
        this.state = {
            userData: UserStore.getUserData(),
            errorStatus: UserStore.getErrorStatus(),
            loaded: false
        };
    }
    componentWillMount() {
        UserStore.on( "change", () => {
            this.setState( {
                userData: UserStore.getUserData(),
                errorStatus: UserStore.getErrorStatus(),
                loaded: true
            } );
        } );
        UserActions.fetchUserData();
    }

    render() {
        return (
            <div>
                {
                    ( isEmpty( this.state.userData ) ) &&
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
                    ( this.state.errorStatus && this.state.errorStatus.resetPassword ) &&
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
                    ( !isEmpty( this.state.userData ) && !this.state.errorStatus.resetPassword ) &&
                    <div className="container">
                        <div className="header row">
                            <div className="col-xs-12 header-row">
                                <h1 className="login-container__icon">
                                    <span className="glyphicon glyphicon-equalizer" aria-hidden="true"></span>
                                    <span className="login-container__title"> Alfred </span>
                                </h1>
                                <UserProfile />
                            </div>
                        </div>
                        <div className="main-content row">
                            <div className="col-xs-12">
                                <UserMgmt />
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

render( <Auth />, document.getElementById( 'app' ) );