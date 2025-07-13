package com.galapea.techblog.pftgriddbcloud.webapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GridDbCloudSQLStmt(@JsonProperty("stmt") String statement) {}
