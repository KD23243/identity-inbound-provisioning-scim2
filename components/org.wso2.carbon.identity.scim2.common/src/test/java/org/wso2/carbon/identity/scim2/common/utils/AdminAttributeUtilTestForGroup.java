/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.scim2.common.utils;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.scim2.common.exceptions.IdentitySCIMException;
import org.wso2.carbon.identity.scim2.common.group.SCIMGroupHandler;
import org.wso2.carbon.identity.scim2.common.internal.SCIMCommonComponentHolder;
import org.wso2.carbon.stratos.common.util.ClaimsMgtUtil;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

public class AdminAttributeUtilTestForGroup {

    @Mock
    RealmService realmService;

    @Mock
    RealmConfiguration realmConfiguration;

    @Mock
    UserRealm userRealm;

    @Mock
    AbstractUserStoreManager userStoreManager;

    @Mock
    SCIMGroupHandler scimGroupHandler;

    @Mock
    AdminAttributeUtil adminAttributeUtil;

    private MockedStatic<SCIMCommonComponentHolder> scimCommonComponentHolder;
    private MockedStatic<ClaimsMgtUtil> claimsMgtUtil;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<UserCoreUtil> userCoreUtil;
    private MockedStatic<IdentityUtil> identityUtil;
    private MockedStatic<SCIMCommonUtils> scimCommonUtils;

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);
        adminAttributeUtil = new AdminAttributeUtil();
        scimCommonComponentHolder = mockStatic(SCIMCommonComponentHolder.class);
        claimsMgtUtil = mockStatic(ClaimsMgtUtil.class);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        userCoreUtil = mockStatic(UserCoreUtil.class);
        identityUtil = mockStatic(IdentityUtil.class);
        scimCommonUtils = mockStatic(SCIMCommonUtils.class);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        scimCommonComponentHolder.close();
        claimsMgtUtil.close();
        identityTenantUtil.close();
        userCoreUtil.close();
        identityUtil.close();
        scimCommonUtils.close();
        System.clearProperty(CarbonBaseConstants.CARBON_HOME);
    }

    @DataProvider(name = "testUpdateAdminUserData")
    public Object[][] testUpdateAdminUserData() {
        return new Object[][]{
                {true},
                {false}
        };
    }

    @DataProvider(name = "testUpdateAdminGroupData")
    public Object[][] testUpdateAdminGroupData() {
        return new Object[][]{
                {"testDomain"},
                {null}
        };
    }

    @Test(dataProvider = "testUpdateAdminGroupData")
    public void testUpdateAdminGroup(String domainName) throws Exception {
        String roleNameWithDomain = "TESTDOMAIN/admin";

        scimCommonComponentHolder.when(() -> SCIMCommonComponentHolder.getRealmService()).thenReturn(realmService);
        when(realmService.getTenantUserRealm(anyInt())).thenReturn(userRealm);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        when(userStoreManager.isSCIMEnabled()).thenReturn(true);
        when(userStoreManager.getTenantId()).thenReturn(1);
        when(userStoreManager.getRealmConfiguration()).thenReturn(realmConfiguration);
        when(userStoreManager.isRoleAndGroupSeparationEnabled()).thenReturn(true);
        when(realmConfiguration.getAdminRoleName()).thenReturn("admin");
        userCoreUtil.when(() -> UserCoreUtil.getDomainName((RealmConfiguration) any())).thenReturn(domainName);
        identityUtil.when(() -> IdentityUtil.getPrimaryDomainName()).thenReturn("TESTDOMAIN");
        userCoreUtil.when(() -> UserCoreUtil.addDomainToName(anyString(), anyString())).thenReturn(roleNameWithDomain);
        scimCommonUtils.when(() -> SCIMCommonUtils.getGroupNameWithDomain(anyString())).thenReturn(roleNameWithDomain);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME = true;
        adminAttributeUtil.updateAdminGroup(1);
        verify(scimGroupHandler).addMandatoryAttributes(argument.capture());

        assertEquals(argument.getValue(), roleNameWithDomain);
    }

//    @Test(expectedExceptions = IdentitySCIMException.class)
//    public void testUpdateAdminGroup1() throws Exception {
//        String roleNameWithDomain = "TESTDOMAIN/admin";
//
//        scimCommonComponentHolder.when(() -> SCIMCommonComponentHolder.getRealmService()).thenReturn(realmService);
//        when(realmService.getTenantUserRealm(anyInt())).thenReturn(userRealm);
//        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
//        when(userStoreManager.isSCIMEnabled()).thenReturn(true);
//        when(userStoreManager.getTenantId()).thenReturn(1);
//        when(userStoreManager.getRealmConfiguration()).thenReturn(realmConfiguration);
//        when(realmConfiguration.getAdminRoleName()).thenReturn("admin");
//        userCoreUtil.when(() -> UserCoreUtil.getDomainName((RealmConfiguration) any())).thenReturn("testDomain");
//        identityUtil.when(() -> IdentityUtil.getPrimaryDomainName()).thenReturn("TESTDOMAIN");
//        userCoreUtil.when(() -> UserCoreUtil.addDomainToName(anyString(), anyString())).thenReturn(roleNameWithDomain);
//        scimCommonUtils.when(() -> SCIMCommonUtils.getGroupNameWithDomain(anyString())).thenReturn(roleNameWithDomain);
//        when(scimGroupHandler.isGroupExisting(anyString())).thenThrow(new IdentitySCIMException("testException"));
//
//        adminAttributeUtil.updateAdminGroup(1);
//        verify(scimGroupHandler.isGroupExisting(anyString()));
//    }

}
