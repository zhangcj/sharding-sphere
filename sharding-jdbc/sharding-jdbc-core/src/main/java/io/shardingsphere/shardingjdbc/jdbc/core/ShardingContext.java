/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
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
 * </p>
 */

package io.shardingsphere.shardingjdbc.jdbc.core;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.executor.ShardingExecuteEngine;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.shardingjdbc.jdbc.metadata.JDBCTableMetaDataConnectionManager;
import lombok.Getter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Sharding runtime context.
 * 
 * @author gaohongtao
 * @author panjuan
 */
@Getter
public final class ShardingContext implements AutoCloseable {
    
    private final ShardingRule shardingRule;
    
    private final DatabaseType databaseType;
    
    private final ShardingExecuteEngine executeEngine;
    
    private final int maxConnectionsSizePerQuery;
    
    private final boolean showSQL;
    
    private final ShardingMetaData metaData;
    
    public ShardingContext(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule, 
                           final DatabaseType databaseType, final ShardingExecuteEngine executeEngine, final int maxConnectionsSizePerQuery, final boolean showSQL) throws SQLException {
        this.shardingRule = shardingRule;
        this.databaseType = databaseType;
        this.executeEngine = executeEngine;
        this.maxConnectionsSizePerQuery = maxConnectionsSizePerQuery;
        this.showSQL = showSQL;
        metaData = new ShardingMetaData(
                getDataSourceURLs(dataSourceMap), shardingRule, databaseType, executeEngine, new JDBCTableMetaDataConnectionManager(dataSourceMap), maxConnectionsSizePerQuery);
    }
    
    private Map<String, String> getDataSourceURLs(final Map<String, DataSource> dataSourceMap) throws SQLException {
        Map<String, String> result = new LinkedHashMap<>(dataSourceMap.size(), 1);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            result.put(entry.getKey(), getDataSourceURL(entry.getValue()));
        }
        return result;
    }
    
    private String getDataSourceURL(final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getURL();
        }
    }
    
    @Override
    public void close() {
        executeEngine.close();
    }
}
