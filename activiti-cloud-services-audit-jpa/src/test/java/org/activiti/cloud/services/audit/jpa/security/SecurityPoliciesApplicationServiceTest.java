package org.activiti.cloud.services.audit.jpa.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.cloud.services.audit.jpa.repository.EventSpecification;
import org.activiti.cloud.services.common.security.SpringSecurityAuthenticationWrapper;
import org.activiti.cloud.services.security.SecurityPoliciesService;
import org.activiti.cloud.services.security.SecurityPolicy;
import org.activiti.runtime.api.auth.AuthorizationLookup;
import org.activiti.runtime.api.identity.IdentityLookup;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class SecurityPoliciesApplicationServiceTest {

    @InjectMocks
    @Spy
    private SecurityPoliciesApplicationService securityPoliciesApplicationService;

    @Mock
    private IdentityLookup identityLookup;

    @Mock
    private AuthorizationLookup authorizationLookup;

    @Mock
    private SecurityPoliciesService securityPoliciesService;

    @Mock
    private SpringSecurityAuthenticationWrapper authenticationWrapper;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void testWithNullKeysSpec() {
        EventSpecification search = mock(EventSpecification.class);

        List<String> groups = Arrays.asList("hr");

        when(securityPoliciesService.policiesDefined()).thenReturn(true);
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn("bob");
        when(authorizationLookup.isAdmin("bob")).thenReturn(false);
        when(securityPoliciesService.getWildcard()).thenReturn("*");

        when(identityLookup.getGroupsForCandidateUser("bob")).thenReturn(groups);
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        map.put("rb1",
                null);

        when(securityPoliciesService.getProcessDefinitionKeys("bob",
                                                              groups,
                                                              SecurityPolicy.READ)).thenReturn(map);

        securityPoliciesApplicationService.createSpecWithSecurity(search,
                                                                  SecurityPolicy.READ);
    }

    @Test
    public void shouldNotModifyQueryWhenNoPoliciesDefined() {

        EventSpecification search = mock(EventSpecification.class);

        when(securityPoliciesService.policiesDefined()).thenReturn(false);
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn("bob");

        securityPoliciesApplicationService.createSpecWithSecurity(search,
                                                                  SecurityPolicy.READ);

        verifyZeroInteractions(search);
    }

    @Test
    public void shouldNotModifyQueryWhenNoUser() {
        EventSpecification search = mock(EventSpecification.class);

        when(securityPoliciesService.policiesDefined()).thenReturn(true);
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn(null);

        securityPoliciesApplicationService.createSpecWithSecurity(search,
                                                                  SecurityPolicy.READ);

        verifyZeroInteractions(search);
    }

    @Test
    public void shouldRestrictQueryWhenGroupsAndPoliciesAvailable() {
        EventSpecification search = mock(EventSpecification.class);

        when(securityPoliciesService.policiesDefined()).thenReturn(true);
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn("bob");

        when(identityLookup.getGroupsForCandidateUser("bob")).thenReturn(Arrays.asList("hr"));

        Map<String, Set<String>> policies = new HashMap<String, Set<String>>();
        policies.put("rb1",
                     new HashSet<>(Arrays.asList("SimpleProcess")));

        when(securityPoliciesService.getProcessDefinitionKeys(anyString(),
                                                              anyCollection(),
                                                              any(SecurityPolicy.class))).thenReturn(policies);

        securityPoliciesApplicationService.createSpecWithSecurity(search,
                                                                  SecurityPolicy.READ);
        ArgumentCaptor<Specification> securitySpecArgumentCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(search,
               times(1)).and(securitySpecArgumentCaptor.capture());

        assertThat(securitySpecArgumentCaptor.getValue()).isInstanceOf(ApplicationProcessDefSecuritySpecification.class);
    }

    @Test
    public void shouldNotRestrictQueryByProcDefWhenWildcard() {
        EventSpecification search = mock(EventSpecification.class);

        when(securityPoliciesService.policiesDefined()).thenReturn(true);
        when(securityPoliciesService.getWildcard()).thenReturn("*");
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn("bob");

        when(identityLookup.getGroupsForCandidateUser("bob")).thenReturn(Arrays.asList("hr"));

        Map<String, Set<String>> policies = new HashMap<String, Set<String>>();
        policies.put("rb1",
                     new HashSet<>(Arrays.asList(securityPoliciesService.getWildcard())));

        when(securityPoliciesService.getProcessDefinitionKeys(anyString(),
                                                              anyCollection(),
                                                              any(SecurityPolicy.class))).thenReturn(policies);

        securityPoliciesApplicationService.createSpecWithSecurity(search,
                                                                  SecurityPolicy.READ);

        ArgumentCaptor<Specification> securitySpecArgumentCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(search,
               times(1)).and(securitySpecArgumentCaptor.capture());

        assertThat(securitySpecArgumentCaptor.getValue()).isInstanceOf(ApplicationSecuritySpecification.class);
    }

    @Test
    public void shouldHavePermissionWhenDefIsInPolicy() {
        List<String> groups = Arrays.asList("hr");

        when(securityPoliciesService.policiesDefined()).thenReturn(true);
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn("bob");
        when(authorizationLookup.isAdmin("bob")).thenReturn(false);

        when(identityLookup.getGroupsForCandidateUser("bob")).thenReturn(groups);
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        map.put("rb1",
                new HashSet(Arrays.asList("key")));
        when(securityPoliciesService.getProcessDefinitionKeys("bob",
                                                              groups,
                                                              SecurityPolicy.WRITE)).thenReturn(map);
        when(securityPoliciesService.getProcessDefinitionKeys("bob",
                                                              groups,
                                                              SecurityPolicy.READ)).thenReturn(map);

        assertThat(securityPoliciesApplicationService.canRead("key",
                                                              "rb1")).isTrue();
    }

    @Test
    public void shouldHavePermissionWhenAdmin() {

        when(securityPoliciesService.policiesDefined()).thenReturn(true);
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn("admin");
        when(authorizationLookup.isAdmin("admin")).thenReturn(true);

        assertThat(securityPoliciesApplicationService.canRead("key",
                                                              "rb1")).isTrue();
    }

    @Test
    public void shouldRestrictQueryWhenKeysFromPolicy() {
        List<String> groups = Arrays.asList("hr");

        when(securityPoliciesService.policiesDefined()).thenReturn(true);
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn("bob");
        when(authorizationLookup.isAdmin("bob")).thenReturn(false);

        when(identityLookup.getGroupsForCandidateUser("bob")).thenReturn(groups);
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        map.put("rb1",
                new HashSet(Arrays.asList("key")));
        when(securityPoliciesService.getProcessDefinitionKeys("bob",
                                                              groups,
                                                              SecurityPolicy.READ)).thenReturn(map);

        EventSpecification search = mock(EventSpecification.class);
        securityPoliciesApplicationService.createSpecWithSecurity(search,
                                                                  SecurityPolicy.READ);

        ArgumentCaptor<Specification> securitySpecArgumentCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(search,
               times(1)).and(securitySpecArgumentCaptor.capture());

        assertThat(securitySpecArgumentCaptor.getValue()).isInstanceOf(ApplicationProcessDefSecuritySpecification.class);
    }

    @Test
    public void shouldRestrictQueryWhenPoliciesButNotForUser() {

        when(securityPoliciesService.policiesDefined()).thenReturn(true);
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn("intruder");
        when(authorizationLookup.isAdmin("intruder")).thenReturn(false);

        when(identityLookup.getGroupsForCandidateUser("intruder")).thenReturn(null);
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();

        when(securityPoliciesService.getProcessDefinitionKeys("intruder",
                                                              null,
                                                              SecurityPolicy.READ)).thenReturn(map);

        EventSpecification search = mock(EventSpecification.class);
        securityPoliciesApplicationService.createSpecWithSecurity(search,
                                                                  SecurityPolicy.READ);

        ArgumentCaptor<Specification> securitySpecArgumentCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(search,
               times(1)).and(securitySpecArgumentCaptor.capture());

        assertThat(securitySpecArgumentCaptor.getValue()).isInstanceOf(ImpossibleSpecification.class);
    }


}
