package org.apereo.cas.ticket.expiration;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.factory.BaseTicketFactoryTests;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link MultiTimeUseOrTimeoutExpirationPolicy}.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("ExpirationPolicy")
@TestPropertySource(properties = "cas.ticket.tgt.core.only-track-most-recent-session=true")
class MultiTimeUseOrTimeoutExpirationPolicyTests extends BaseTicketFactoryTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "multiTimeUseOrTimeoutExpirationPolicy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final long TIMEOUT_SECONDS = 1;

    private static final int NUMBER_OF_USES = 5;

    private MultiTimeUseOrTimeoutExpirationPolicy expirationPolicy;

    private TicketGrantingTicketImpl ticket;

    
    @BeforeEach
    public void initialize() {
        expirationPolicy = new MultiTimeUseOrTimeoutExpirationPolicy(NUMBER_OF_USES, TIMEOUT_SECONDS);
        ticket = new TicketGrantingTicketImpl("test", CoreAuthenticationTestUtils.getAuthentication(), expirationPolicy);
    }

    @Test
    void verifyTicketIsNull() throws Throwable {
        assertTrue(expirationPolicy.isExpired(null));
        assertNotNull(expirationPolicy.toMaximumExpirationTime(ticket));
    }

    @Test
    void verifyTicketIsNotExpired() throws Throwable {
        expirationPolicy.setClock(Clock.fixed(ticket.getCreationTime().toInstant().plusSeconds(TIMEOUT_SECONDS).minusNanos(1), ZoneOffset.UTC));
        assertFalse(ticket.isExpired());
        assertEquals(0, expirationPolicy.getTimeToIdle());
    }

    @Test
    void verifyTicketIsExpiredByTime() throws Throwable {
        expirationPolicy.setClock(Clock.fixed(ticket.getCreationTime().toInstant().plusSeconds(TIMEOUT_SECONDS).plusNanos(1), ZoneOffset.UTC));
        assertTrue(ticket.isExpired());
    }

    @Test
    void verifyTicketIsExpiredByCount() throws Throwable {
        IntStream.range(0, NUMBER_OF_USES)
            .forEach(i -> ticket.grantServiceTicket("test", RegisteredServiceTestUtils.getService(),
                NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy));
        assertTrue(ticket.isExpired());
    }

    @Test
    void verifySerializeATimeoutExpirationPolicyToJson() throws IOException {
        MAPPER.writeValue(JSON_FILE, expirationPolicy);
        val policyRead = MAPPER.readValue(JSON_FILE, MultiTimeUseOrTimeoutExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }

    @Test
    void verifySerialization() throws Throwable {
        val result = SerializationUtils.serialize(expirationPolicy);
        val policyRead = SerializationUtils.deserialize(result, MultiTimeUseOrTimeoutExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }
}
