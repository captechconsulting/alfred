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
/*
* Validations constants for the forms.
*/

export const fieldValidationDefault = values => {
  const { name } = values
  return {
    name: !name ? 'Field name (key) is required' : undefined
  }
};

export const fieldValidationAlt = values => {
  const { name, datatype } = values
  return {
    name: !name ? 'Field name (key) is required' : undefined,
    datatype: !datatype ? 'Field Data Type is required' : undefined
  }
};

export const scriptFormDefault = values => {
  const { path, name } = values
  return {
    path: !path ? 'Path is required' : undefined,
    name: !name ? 'Name is required' : undefined
  }
};

export const sourceTechnicalDefalt = values => {
  const { format } = values
  return {
    format: !format ? 'Please select an option' : null
  }
};

export const technicalFormDefault = values => {
  const { format } = values
  return {
    format: !format ? 'Please select an option' : null
  }
};

export const templateInfoDefault = values => {
  const { key, dataSteward, owner, subjectArea, codeOfConduct } = values
  return {
    key: !key ? 'Key is required' : undefined,
    dataSteward: !dataSteward ? 'Data Steward is required' : undefined,
    owner: !owner ? 'Owner is required' : undefined,
    subjectArea: !subjectArea ? 'Subject Area is required' : undefined,
    codeOfConduct: !codeOfConduct ? 'Please select an option' : null
    /*fields: (!fields || !fields.length) ? 'You need at least one field' : fields.map(friend => {
      const { name, businessName, ordinalPosition, nullable, pk, dataType} = friend
      return {
        name: !name ? 'A name is required' : undefined,
        businessName: !businessName ? 'A businessName is required' : undefined,
        ordinalPosition: !ordinalPosition ? 'A ordinal position is required' : undefined,
        nullable: !nullable ? 'Please select a value' : undefined,
        pk: !pk ? 'Please select a value' : undefined,
        dataType: !dataType ? 'Please select a data type' : undefined
      }
    })*/
  }
};

export const templateInfoSandbox = values => {
  const { key } = values
  return {
    key: !key ? 'Key is required' : undefined
  }
};