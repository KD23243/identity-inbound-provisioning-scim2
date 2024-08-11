/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.scim2.common.group;

import org.apache.commons.lang.StringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.scim2.common.DAO.GroupDAO;
import org.wso2.carbon.identity.scim2.common.exceptions.IdentitySCIMException;
import org.wso2.carbon.identity.scim2.common.utils.SCIMCommonUtils;
import org.wso2.charon3.core.objects.Group;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.util.Set;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertTrue;

public class SCIMGroupHandlerTest {

    @Mock
    private GroupDAO mockedGroupDAO;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement mockedPreparedStatement;

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);
    }

    MockedConstruction<GroupDAO> mockedConstruction;

    @Test
    public void testAddMandatoryAttributes() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        mockStatic(SCIMCommonUtils.class);
        mockStatic(IdentityDatabaseUtil.class);

        when(SCIMCommonUtils.getSCIMGroupURL(anyString())).thenReturn("ID");
        when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        when(mockedGroupDAO.isExistingGroup(SCIMCommonUtils.getGroupNameWithDomain("GROUP_NAME"), 1)).thenReturn(false);

        SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
        scimGroupHandler.addMandatoryAttributes(anyString());
    }

    @Test
    public void testGetGroupAttributesByName() throws Exception {
        SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
        assertNull(scimGroupHandler.getGroupAttributesByName("managers"));
    }

    @Test
    public void testGetGroupAttributesById() throws Exception {
        SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
        assertNull(scimGroupHandler.getGroupAttributesById("1"));
    }

    @Test
    public void testCreateSCIMAttributes() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(SCIMCommonUtils.class);

        Group group = new Group();
        Date date = new Date();
        group.setCreatedDate(date);
        group.setLastModified(date);
        group.setLocation("LOCATION_URI");

        when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        when(mockedGroupDAO.isExistingGroup(SCIMCommonUtils.getGroupNameWithDomain("NON_EXISTANT_GROUP_NAME"), 1)).thenReturn(false);

        SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
        scimGroupHandler.createSCIMAttributes(group);
        assertNotNull(group);
    }

    @Test
    public void testCreateSCIMAttributesExceptions() throws Exception {
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(SCIMCommonUtils.class);

        Group group = new Group();
        Date date = new Date();
        group.setCreatedDate(date);
        group.setLastModified(date);
        group.setLocation("LOCATION_URI");
        group.setDisplayName("testDisplayName");

        try (MockedConstruction<GroupDAO> mockedConstruction = Mockito.mockConstruction(
                GroupDAO.class,
                (mock, context) -> {
                    when(mock.isExistingGroup(SCIMCommonUtils.getGroupNameWithDomain("ALREADY_EXISTANT_GROUP_NAME"), 1))
                            .thenReturn(false);
                })) {

            ResultSet resultSet = mock(ResultSet.class);

            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
            when(mockedPreparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);

            SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
            ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
            scimGroupHandler.createSCIMAttributes(group);

            // Verify that the mocked GroupDAO instance is used
            GroupDAO mockedGroupDAO = mockedConstruction.constructed().get(0);
            verify(mockedGroupDAO).addSCIMGroupAttributes(anyInt(), argumentCaptor.capture(), anyMap());
            assertEquals("testDisplayName", argumentCaptor.getValue());
        }
    }

    @Test
    public void testGetGroupName() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(StringUtils.class);
        mockStatic(SCIMCommonUtils.class);

        when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(StringUtils.isNotEmpty(nullable(String.class))).thenReturn(true);
        when(SCIMCommonUtils.getPrimaryFreeGroupName(nullable(String.class))).thenReturn("directors");
        when(mockedPreparedStatement.executeQuery()).thenReturn(resultSet);
        when(mockedGroupDAO.getGroupNameById(1, "5")).thenReturn("directors");
        assertEquals(new SCIMGroupHandler(1).getGroupName("5"), "directors", "asserting for existance");

        when(StringUtils.isNotEmpty(nullable(String.class))).thenReturn(false);
        assertNull(new SCIMGroupHandler(1).getGroupName("NON_EXISITNG_GROUP_NAME"), "asserting for non existance");
    }

    @Test
    public void testGetGroupId() throws Exception {
        assertNull(new SCIMGroupHandler(1).getGroupId("directors"));
    }

    @Test
    public void testGetGroupWithAttributes() throws Exception {
        Group group = new Group();
        ResultSet resultSet = mock(ResultSet.class);
        mockStatic(SCIMCommonUtils.class);
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(StringUtils.class);

        Date date = new Date(2017, 10, 10, 10, 10, 10);
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("urn:ietf:params:scim:schemas:core:2.0:id", "100");
        attributes.put("urn:ietf:params:scim:schemas:core:2.0:meta.created", date.toString());
        attributes.put("urn:ietf:params:scim:schemas:core:2.0:meta.lastModified", date.toString());
        attributes.put("urn:ietf:params:scim:schemas:core:2.0:meta.location", "colombo");

        when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(resultSet.next()).thenReturn(false);
        when(mockedPreparedStatement.executeQuery()).thenReturn(resultSet);
        when(mockedGroupDAO.isExistingGroup("NON_EXISTING_GROUP_NAME", 1)).thenReturn(false);
        assertEquals(new SCIMGroupHandler(1).getGroupWithAttributes(group, "NON_EXISTING_GROUP_NAME"), group);
    }

    @Test
    public void testGetGroupWithAttributesSecondScenario() throws Exception {
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        mockStatic(SCIMCommonUtils.class);

        Group group = new Group();
        ResultSet resultSet = mock(ResultSet.class);

        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("urn:ietf:params:scim:schemas:core:2.0:id", "100");
        attributes.put("urn:ietf:params:scim:schemas:core:2.0:meta.created", formatter.format(today));
        attributes.put("urn:ietf:params:scim:schemas:core:2.0:meta.lastModified", formatter.format(today));
        attributes.put("urn:ietf:params:scim:schemas:core:2.0:meta.location",
                "https://localhost:9443/t/TENANT_DOMAIN/Groups/100");

        try (MockedConstruction<GroupDAO> mockedConstruction = Mockito.mockConstruction(
                GroupDAO.class,
                (mock, context) -> {
                    when(mock.isExistingGroup("EXISTING_GROUP_NAME", 1)).thenReturn(true);
                    when(mock.getSCIMGroupAttributes(anyInt(), anyString())).thenReturn(attributes);
                })) {

            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
            when(mockedPreparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);
            when(IdentityTenantUtil.getTenantDomain(1)).thenReturn("TENANT_DOMAIN");
            when(SCIMCommonUtils.getSCIMGroupURL()).thenReturn("https://localhost:9443/t/TENANT_DOMAIN/Groups");

            assertEquals(new SCIMGroupHandler(1).getGroupWithAttributes(group, "EXISTING_GROUP_NAME"), group);
        }
    }

    @Test
    public void testIsGroupExisting() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(SCIMCommonUtils.class);

        when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(resultSet.next()).thenReturn(true);
        when(mockedPreparedStatement.executeQuery()).thenReturn(resultSet);
        when(mockedGroupDAO.isExistingGroup("managers", 1)).thenReturn(true);
        assertTrue(new SCIMGroupHandler(1).isGroupExisting("managers"));

        when(resultSet.next()).thenReturn(false);
        when(mockedGroupDAO.isExistingGroup("directors", 1)).thenReturn(false);
        assertEquals(new SCIMGroupHandler(1).isGroupExisting("directors"), false);
    }

    @Test
    public void testDeleteGroupAttributes() throws Exception {
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(SCIMCommonUtils.class);

        ResultSet resultSet = mock(ResultSet.class);

        try (MockedConstruction<GroupDAO> mockedConstruction = Mockito.mockConstruction(
                GroupDAO.class,
                (mock, context) -> {
                    when(mock.isExistingGroup(anyString(), anyInt())).thenReturn(true);
                })) {

            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
            when(mockedPreparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);

            SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
            ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
            scimGroupHandler.deleteGroupAttributes("GROUP_DELETABLE");

            // Retrieve the mock from MockedConstruction
            GroupDAO mockedGroupDAO = mockedConstruction.constructed().get(0);
            verify(mockedGroupDAO).removeSCIMGroup(anyInt(), argumentCaptor.capture());
            assertEquals("GROUP_DELETABLE", argumentCaptor.getValue());
        }
    }

    @Test
    public void testUpdateRoleName() throws Exception {
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(SCIMCommonUtils.class);

        try (MockedConstruction<GroupDAO> mockedConstruction = Mockito.mockConstruction(
                GroupDAO.class,
                (mock, context) -> {
                    when(mock.isExistingGroup(anyString(), anyInt())).thenReturn(true);
                })) {

            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);

            SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
            ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
            scimGroupHandler.updateRoleName("EXISTENT_ROLE_NAME", "NEW_ROLE_NAME");

            // Retrieve the mock from MockedConstruction
            GroupDAO mockedGroupDAO = mockedConstruction.constructed().get(0);
            verify(mockedGroupDAO).updateRoleName(anyInt(), argumentCaptor.capture(), anyString());
            assertEquals("EXISTENT_ROLE_NAME", argumentCaptor.getValue());
        }
    }

    @Test(expectedExceptions = IdentitySCIMException.class)
    public void testUpdateRoleNameNonExistent() throws Exception {
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(SCIMCommonUtils.class);

        try (MockedConstruction<GroupDAO> mockedConstruction = Mockito.mockConstruction(
                GroupDAO.class,
                (mock, context) -> {
                    when(mock.isExistingGroup("NON_EXISTENT_ROLE_NAME", 1)).thenReturn(false);
                })) {

            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);

            SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
            scimGroupHandler.updateRoleName("NON_EXISTENT_ROLE_NAME", "NEW_ROLE_NAME");
            // This method is to test the throwing of an IdentitySCIMException, hence no assertion.
        }
    }

    @Test
    public void testListSCIMRoles() throws Exception {
        Set<String> groups = mock(HashSet.class);
        ResultSet resultSet = mock(ResultSet.class);
        mockStatic(IdentityDatabaseUtil.class);

        when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(resultSet.next()).thenReturn(false);
        when(mockedPreparedStatement.executeQuery()).thenReturn(resultSet);
        when(mockedGroupDAO.listSCIMGroups()).thenReturn(groups);
        assertNotNull(new SCIMGroupHandler(1).listSCIMRoles());
    }

}
