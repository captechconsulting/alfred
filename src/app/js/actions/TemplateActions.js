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
import dispatcher from "../../../contents/js/dispatchers/dispatcher";
import axios from 'axios';

/*
* Actions that handles the template data.
*/

// The following three methods are prototype extract methods. 
// If it's required to modularize the template data actions, then use these to separate them.
export function addTemplate(key) {
  dispatcher.dispatch({
    type: "ADD_TEMPLATE",
    key
  });
}

export function removeTemplate(key) {
  dispatcher.dispatch({
    type: "REMOVE_TEMPLATE",
    key
  });
}

export function modifyTemplate(key) {
  dispatcher.dispatch({
    type: "REMOVE_TEMPLATE",
    key
  });
}

export function reloadTemplates() {
  // fetch template data
  axios.get('/fileMetadata/summary?begin=1&end=10')
    .then(function (response) {
      // async load
      dispatcher.dispatch({
        type: "RECEIVE_TEMPLATES",
        payload: [response.data]
      })
    })
    .catch(function (error) {
      if (error.response.status === 403) {
        dispatcher.dispatch({
          type: "RESET_PASSWORD",
          payload: {reset:true}
        })
      } else {
        console.log(error);
      }
    }
  );
}
