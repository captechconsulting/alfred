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
/**
* Helper Utility Methods
*/

/**
 * Takes in an object and checks if it is empty
 * @param {obj} object to be evaulated
 */
export const isEmpty = (obj) => {
    for(let prop in obj) {
      if(obj.hasOwnProperty(prop)) {
        return false;
      }
    }
    return true;
}

/**
 * Converts Bytes to a more readable format
 * @param {bytes} integer byte size to be converted
 * @param {decimal} as the position of decimal
 */
export const convertBytes = (bytes,decimals) => {
  if(bytes == 0) return '0 Bytes';
  let k = 1000,
    dm = decimals || 2,
    sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
    i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
};


/**
 * Check User Permissions
 * Consolidates the method into one, good for finding throughout the app. 
 * @param {userData} object to be iterated through
 * @param {permToCheck} string to be evaluated
 */

export const hasPermission = (userPermissions, permToCheck) => {
  return (userPermissions.permissions.includes(permToCheck) || userPermissions.roles[0].permissions.includes(permToCheck));
}


/**
 * Check User Admin role
 * @param {userData} object to be iterated through
 * @param {permToCheck} string to be evaluated
 */

export const isAdmin = (userPermissions) => {
  for (let x = 0; x < userPermissions.roles.length; x++) {
    if (userPermissions.roles[x].name === 'ADMIN') {
      return true;
    }
  }
  return false;
}

/**
 * Regex to add commas thousands seperators
 * source *stackoverflow
 * @param {x} the number to be editted
 */

 export const addCommas = (x) => {
  return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
 }
