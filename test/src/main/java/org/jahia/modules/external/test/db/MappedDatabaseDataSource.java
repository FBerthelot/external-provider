/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.external.test.db;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.lang.StringUtils;
import org.jahia.modules.external.ExternalDataSource;
import org.jahia.modules.external.ExternalQuery;
import org.jahia.modules.external.query.QueryHelper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Implementation of the external data source that uses sample database to fetch the data and predefined node type mappins.
 * 
 * @author Sergiy Shyrkov
 */
public class MappedDatabaseDataSource extends BaseDatabaseDataSource implements ExternalDataSource.Searchable {

    private static final String DATA_TYPE_AIRLINE = "jtestnt:airline".intern();

    private static final String DATA_TYPE_CATALOG = "jtestnt:catalog".intern();

    private static final String DATA_TYPE_CITY = "jtestnt:city".intern();

    private static final String DATA_TYPE_COUNTRY = "jtestnt:country".intern();

    private static final String DATA_TYPE_DIRECTORY = "jtestnt:directory".intern();

    private static final String DATA_TYPE_FLIGHT = "jtestnt:flight".intern();

    private static final List<String> DIRECTORIES = Arrays.asList("AIRLINES", "COUNTRIES", "CITIES", "FLIGHTS");

    private static final BidiMap DIRECTORY_TYPE_MAPPING;

    private static final Logger logger = LoggerFactory.getLogger(MappedDatabaseDataSource.class);

    private static final Set<String> SUPPORTED_NODETYPES = new HashSet<String>(Arrays.asList(DATA_TYPE_CATALOG,
            DATA_TYPE_DIRECTORY, DATA_TYPE_AIRLINE, DATA_TYPE_CITY, DATA_TYPE_COUNTRY, DATA_TYPE_FLIGHT));

    static {
        DIRECTORY_TYPE_MAPPING = new DualHashBidiMap();
        DIRECTORY_TYPE_MAPPING.put("AIRLINES", DATA_TYPE_AIRLINE);
        DIRECTORY_TYPE_MAPPING.put("COUNTRIES", DATA_TYPE_COUNTRY);
        DIRECTORY_TYPE_MAPPING.put("CITIES", DATA_TYPE_CITY);
        DIRECTORY_TYPE_MAPPING.put("FLIGHTS", DATA_TYPE_FLIGHT);
    }

    @Override
    protected String getRowID(ResultSet rs, List<String> primaryKeys) throws SQLException {
        String val = null;
        if (primaryKeys.size() == 1) {
            val = rs.getString(primaryKeys.get(0));
        } else {
            StringBuilder buff = new StringBuilder();
            for (String col : primaryKeys) {
                if (buff.length() > 0) {
                    buff.append("###");
                }
                buff.append(rs.getString(col));
            }
            val = buff.toString();
        }

        return val;
    }

    @Override
    protected String getRowNodeTypeName(String tableName) throws PathNotFoundException {
        String type = (String) DIRECTORY_TYPE_MAPPING.get(tableName);
        if (type == null) {
            throw new PathNotFoundException(tableName);
        }

        return type;
    }

    @Override
    protected Map<String, String[]> getRowProperties(String table, ResultSet rs) throws SQLException,
            PathNotFoundException {
        Map<String, String[]> props = new LinkedHashMap<String, String[]>();
        ExtendedNodeType nodeType = null;
        try {
            nodeType = NodeTypeRegistry.getInstance().getNodeType(getRowNodeTypeName(table));
        } catch (NoSuchNodeTypeException e) {
            throw new PathNotFoundException(e);
        }

        for (ExtendedPropertyDefinition def : nodeType.getDeclaredPropertyDefinitions()) {
            String val = rs.getString(def.getName());
            if (val != null) {
                props.put(def.getName(), new String[] { val });
            }
        }

        return props;
    }

    @Override
    protected String getSchemaNodeType() {
        return DATA_TYPE_CATALOG;
    }

    @Override
    public Set<String> getSupportedNodeTypes() {
        return SUPPORTED_NODETYPES;
    }

    @Override
    protected List<String> getTableNames() {
        return DIRECTORIES;
    }

    @Override
    protected String getTableNodeType() {
        return DATA_TYPE_DIRECTORY;
    }

    @Override
    protected String[] getValuesForPrimayKeys(String rowId) {
        return StringUtils.split(rowId, "###");
    }

    @Override
    public List<String> search(ExternalQuery query) throws RepositoryException {
        logger.info("Executing search for query: {}", query.getSource());
        List<String> allResults = null;

        String nodeType = QueryHelper.getNodeType(query.getSource());

        List<String> dataTypes = getDataTypesForNodeType(nodeType);
        for (String dataType : dataTypes) {
            logger.info("Performing search for data type: {}", dataType);
            List<String> results = doSearch(query, dataType);
            if (!results.isEmpty()) {
                if (allResults == null) {
                    allResults = results;
                } else {
                    allResults.addAll(results);
                }
            }
        }

        return allResults != null ? allResults : Collections.<String> emptyList();
    }

    private List<String> doSearch(ExternalQuery query, String dataType) throws RepositoryException {
        List<String> result = null;
        if (dataType == DATA_TYPE_CATALOG) {
            result = Arrays.asList("/");
        } else if (dataType == DATA_TYPE_DIRECTORY) {
            result = new LinkedList<String>();
            for (String table : getTableNames()) {
                result.add("/" + table);
            }
        } else if (dataType == DATA_TYPE_AIRLINE || dataType == DATA_TYPE_CITY || dataType == DATA_TYPE_COUNTRY
                || dataType == DATA_TYPE_FLIGHT) {
            String table = (String) DIRECTORY_TYPE_MAPPING.getKey(dataType);
            List<String> rowIDs = getRowIDs(table, QueryHelper.getSimpleAndConstraints(query.getConstraint()));
            if (!rowIDs.isEmpty()) {
                result = new LinkedList<String>();
                for (String rowID : rowIDs) {
                    result.add("/" + table + "/" + rowID);
                }
            }

        }

        return result != null ? result : Collections.<String> emptyList();
    }

    private List<String> getDataTypesForNodeType(String nodeType) {
        NodeTypeRegistry ntRegistry = NodeTypeRegistry.getInstance();
        List<String> types = new LinkedList<String>();
        for (String supportedType : getSupportedNodeTypes()) {
            try {
                if (supportedType.equals(nodeType) || ntRegistry.getNodeType(supportedType).isNodeType(nodeType)) {
                    types.add(supportedType);
                }
            } catch (NoSuchNodeTypeException e) {
                logger.warn(e.getMessage(), e);
            }
        }
        return types;
    }

}
