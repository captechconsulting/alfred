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
import dispatcher from "../../../contents/js/dispatchers/dispatcher";

/*
* Store that handles the template data
*/
class TemplateStore extends EventEmitter {
  constructor() {
    super();
    this.templateSummary = [];
  }
  addTemplate(templateData) {
    this.emit("change");
  }
  receiveTemplates(payload) {
    this.templateSummary = payload.slice(0);
    this.emit("change");
  }
  getUserData() {
    return this.userData;
  }
  getTemplateData() {
    return this.templateSummary;
  }
  handleActions(action) {
    switch(action.type) {
      case "ADD_TEMPLATE": {
        this.addTemplate(action.key);
        break;
      }
      case "RECEIVE_TEMPLATES": {
        this.receiveTemplates(action.payload);
        break;
      }
    }
  }
}

const templateStore = new TemplateStore;
dispatcher.register(templateStore.handleActions.bind(templateStore));

export default templateStore;
