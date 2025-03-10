package org.apereo.cas.support.oauth.services;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.RegexUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;
import java.io.Serial;
import java.util.Set;

/**
 * This is {@link DefaultRegisteredServiceOAuthTokenExchangePolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Accessors(chain = true)
public class DefaultRegisteredServiceOAuthTokenExchangePolicy implements RegisteredServiceOAuthTokenExchangePolicy {
    @Serial
    private static final long serialVersionUID = 1415436756392637729L;

    private Set<String> allowedResources;

    private Set<String> allowedAudience;

    private Set<String> allowedTokenTypes;

    @Override
    public boolean isTokenExchangeAllowed(final RegisteredService registeredService, final Set<String> resources,
                                          final Set<String> audience, final String requestedType) {
        val resourceAllowed = allowedResources == null || allowedResources.stream().anyMatch(resource -> RegexUtils.find(resource, requestedType));
        val audienceAllowed = allowedAudience == null || allowedAudience.stream().anyMatch(aud -> RegexUtils.find(aud, requestedType));
        val tokenTypeAllowed = allowedTokenTypes == null || allowedTokenTypes.stream().anyMatch(type -> RegexUtils.find(type, requestedType));
        return resourceAllowed && audienceAllowed && tokenTypeAllowed;
    }
}
