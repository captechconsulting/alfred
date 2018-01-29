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

package com.captech.alfred;

import com.captech.alfred.dataConnections.DataStoreService;
import com.captech.alfred.instance.Guid;
import com.captech.alfred.instance.InstanceLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@EnableAutoConfiguration
@RequestMapping(value="/instanceLog")
public class InstanceController {

    private static final Logger logger = LoggerFactory.getLogger(InstanceController.class);

    @Autowired
    DataStoreService dataConnection;

    public InstanceController(DataStoreService dataConnection) {
        this.dataConnection = dataConnection;
        logger.debug("initialized InstanceController with dataConnection class " + dataConnection.getClass().getCanonicalName());
    }

    @GetMapping(value = "{guid:.+}", produces = "application/json")
    public List<InstanceLog> getInstanceLog(@PathVariable String guid) {
        logger.debug("getting instance logs for guid: " + guid);
        return dataConnection.getInstanceLog(guid);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public Guid registerNewInstance(@RequestBody InstanceLog log) {
        logger.debug("POST: adding new instance log");
        log.setTimestamp(new SimpleDateFormat(Constants.VERSION_FORMAT).format(new Date()));
        return dataConnection.writeInstanceLog(log, null);
    }

    @PutMapping(value="{guid:.+}", consumes = "application/json", produces = "application/json")
    public Guid continueInstanceLog(@PathVariable String guid, @RequestBody InstanceLog log) {
        logger.debug("PUT: continuing instance log");
        log.setTimestamp(new SimpleDateFormat(Constants.VERSION_FORMAT).format(new Date()));
        return dataConnection.writeInstanceLog(log, guid);
    }

}
