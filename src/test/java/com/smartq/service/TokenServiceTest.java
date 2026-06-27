package com.smartq.service;

import com.smartq.entity.*;
import com.smartq.enums.BusinessType;
import com.smartq.enums.CounterType;
import com.smartq.enums.TokenStatus;
import com.smartq.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class TokenServiceTest {

    @Mock private TokenRepository tokenRepository;
    @Mock private BusinessRepository businessRepository;
    @Mock private CounterRepository counterRepository;
    @Mock private UserRepository userRepository;
    @Mock private QueueSessionRepository queueSessionRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private TokenService tokenService;

    private User customer;
    private Business business;
    private Counter counter;

    @BeforeEach
    void setUp() {
        // Mock security context
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("customer@test.com");
        SecurityContextHolder.setContext(securityContext);

        // Build test customer
        customer = User.builder()
                .id(1L)
                .name("Anjali Sharma")
                .email("customer@test.com")
                .role(com.smartq.enums.Role.CUSTOMER)
                .totalVisits(0)
                .build();

        // Build test business
        business = Business.builder()
                .id(1L)
                .name("Spice Garden")
                .businessType(BusinessType.RESTAURANT)
                .isQueueOpen(true)
                .maxQueueSize(20)
                .avgServiceMins(10)
                .build();

        // Build test counter
        counter = Counter.builder()
                .id(1L)
                .business(business)
                .counterName("Dine In")
                .counterType(CounterType.DINE_IN)
                .isActive(true)
                .currentToken(0)
                .tokensServedToday(0)
                .build();
    }

    // ── TEST 1: Wait time formula ────────────────────────────
    // Position 1, avgServiceMins 10, partySize 1
    // Expected: 1 × 10 × 1.0 = 10 mins
    @Test
    void waitTime_singlePerson_firstInQueue_shouldBe10mins() {
        int result = invokeCalculateWaitTime(1, 10, 1);
        assertEquals(10, result);
    }

    // Position 3, partySize 1 → 3 × 10 × 1.0 = 30 mins
    @Test
    void waitTime_singlePerson_thirdInQueue_shouldBe30mins() {
        int result = invokeCalculateWaitTime(3, 10, 1);
        assertEquals(30, result);
    }

    // partySize 3 → factor 1.2 → 1 × 10 × 1.2 = 12 mins
    @Test
    void waitTime_partyOfThree_shouldApplyFactor() {
        int result = invokeCalculateWaitTime(1, 10, 3);
        assertEquals(12, result);
    }

    // partySize 5 → factor 1.5 → 1 × 10 × 1.5 = 15 mins
    @Test
    void waitTime_partyOfFive_shouldApplyLargerFactor() {
        int result = invokeCalculateWaitTime(1, 10, 5);
        assertEquals(15, result);
    }

    // partySize 8 → factor 1.8 → 1 × 10 × 1.8 = 18 mins
    @Test
    void waitTime_largeParty_shouldApplyMaxFactor() {
        int result = invokeCalculateWaitTime(1, 10, 8);
        assertEquals(18, result);
    }

    // ── TEST 2: Queue full check ─────────────────────────────
    @Test
    void generateToken_whenQueueFull_shouldThrowException() {
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(customer));

        // No active token for customer
        when(tokenRepository.findByCustomerIdAndStatusIn(anyLong(), anyList()))
                .thenReturn(Optional.empty());

        when(businessRepository.findById(1L))
                .thenReturn(Optional.of(business));

        when(counterRepository.findById(1L))
                .thenReturn(Optional.of(counter));

        // Queue is full — 20 waiting + 0 called = 20 = maxQueueSize
        when(tokenRepository.countByCounterIdAndStatus(1L, TokenStatus.WAITING))
                .thenReturn(20);
        when(tokenRepository.countByCounterIdAndStatus(1L, TokenStatus.CALLED))
                .thenReturn(0);

        com.smartq.dto.request.TokenRequest request =
                new com.smartq.dto.request.TokenRequest();
        request.setBusinessId(1L);
        request.setCounterId(1L);
        request.setPartySize(1);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> tokenService.generateToken(request));

        assertTrue(ex.getMessage().contains("full"));
    }

    // ── TEST 3: Closed queue check ───────────────────────────
    @Test
    void generateToken_whenQueueClosed_shouldThrowException() {
        business.setIsQueueOpen(false);

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(customer));
        when(tokenRepository.findByCustomerIdAndStatusIn(anyLong(), anyList()))
                .thenReturn(Optional.empty());
        when(businessRepository.findById(1L))
                .thenReturn(Optional.of(business));

        com.smartq.dto.request.TokenRequest request =
                new com.smartq.dto.request.TokenRequest();
        request.setBusinessId(1L);
        request.setCounterId(1L);
        request.setPartySize(1);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> tokenService.generateToken(request));

        assertTrue(ex.getMessage().contains("closed"));
    }

    // ── TEST 4: Duplicate active token check ─────────────────
    @Test
    void generateToken_whenCustomerHasActiveToken_shouldThrowException() {
        Token existingToken = Token.builder()
                .id(1L)
                .tokenNumber(5)
                .status(TokenStatus.WAITING)
                .build();

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(customer));
        when(tokenRepository.findByCustomerIdAndStatusIn(anyLong(), anyList()))
                .thenReturn(Optional.of(existingToken));

        com.smartq.dto.request.TokenRequest request =
                new com.smartq.dto.request.TokenRequest();
        request.setBusinessId(1L);
        request.setCounterId(1L);
        request.setPartySize(1);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> tokenService.generateToken(request));

        assertTrue(ex.getMessage().contains("already have an active token"));
    }

    // ── TEST 5: Inactive counter check ───────────────────────
    @Test
    void generateToken_whenCounterInactive_shouldThrowException() {
        counter.setIsActive(false);

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(customer));
        when(tokenRepository.findByCustomerIdAndStatusIn(anyLong(), anyList()))
                .thenReturn(Optional.empty());
        when(businessRepository.findById(1L))
                .thenReturn(Optional.of(business));
        when(counterRepository.findById(1L))
                .thenReturn(Optional.of(counter));

        com.smartq.dto.request.TokenRequest request =
                new com.smartq.dto.request.TokenRequest();
        request.setBusinessId(1L);
        request.setCounterId(1L);
        request.setPartySize(1);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> tokenService.generateToken(request));

        assertTrue(ex.getMessage().contains("not active"));
    }

    // ── TEST 6: callNextToken with no waiting customers ──────
    @Test
    void callNextToken_whenNoCustomersWaiting_shouldThrowException() {
        User owner = User.builder()
                .id(2L)
                .email("owner@test.com")
                .role(com.smartq.enums.Role.OWNER)
                .build();

        business.setOwner(owner);
        counter.setBusiness(business);

        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("owner@test.com");
        SecurityContextHolder.setContext(ctx);

        when(userRepository.findByEmail("owner@test.com"))
                .thenReturn(Optional.of(owner));
        when(counterRepository.findById(1L))
                .thenReturn(Optional.of(counter));

        // No CALLED tokens
        when(tokenRepository.findByCounterIdAndStatusOrderByTokenNumber(
                1L, TokenStatus.CALLED))
                .thenReturn(List.of());

        // No SERVING tokens
        when(tokenRepository.findByCounterIdAndStatusOrderByTokenNumber(
                1L, TokenStatus.SERVING))
                .thenReturn(List.of());

        // No WAITING tokens
        when(tokenRepository.findByCounterIdAndStatusOrderByTokenNumber(
                1L, TokenStatus.WAITING))
                .thenReturn(List.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> tokenService.callNextToken(1L));

        assertTrue(ex.getMessage().contains("No more customers"));
    }

    // ── Helper: invoke private calculateWaitTime via reflection ──
    private int invokeCalculateWaitTime(
            int position, int avgServiceMins, int partySize) {
        try {
            var method = TokenService.class.getDeclaredMethod(
                    "calculateWaitTime", int.class, int.class, int.class);
            method.setAccessible(true);
            return (int) method.invoke(
                    tokenService, position, avgServiceMins, partySize);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}