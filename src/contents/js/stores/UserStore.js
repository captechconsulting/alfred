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
import { EventEmitter } from "events";
import dispatcher from "../dispatchers/dispatcher";

// stores & actions
import * as TemplateActions from "../../../app/js/actions/TemplateActions";

/*
* Store that handles the user data
*/
class UserStore extends EventEmitter {
  constructor() {
    super();
    this.userData = {};
    this.errorStatus = {
      resetPassword: false
    }
  }
  fetchUserData(userData) {
    this.userData = Object.assign({}, userData[0]);

    let _this = this;
    setTimeout(function() { // Run after dispatcher has finished
      _this.emit("change");
      TemplateActions.reloadTemplates();
    }, 0);
  }
  getUserData() {
    return this.userData;
  }
  resetUserPassword(data) {
    this.errorStatus = Object.assign({}, {resetPassword: true});

    let _this = this;
    setTimeout(function() { // Run after dispatcher has finished
      _this.emit("change");
    }, 0);
  }
  getErrorStatus() {
    return this.errorStatus;
  }
  handleActions(action) {
    switch(action.type) {
      case "RECEIVE_USERDATA": {
        this.fetchUserData(action.payload);
        break;
      }
      case "RESET_PASSWORD": {
        this.resetUserPassword(action.payload);
        break;
      }
    }
  }
}

const userStore = new UserStore;
dispatcher.register(userStore.handleActions.bind(userStore));

export default userStore;
