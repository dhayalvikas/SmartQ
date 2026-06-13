package com.smartq.service;

import com.smartq.dto.request.TokenRequest;
import com.smartq.dto.response.TokenStatusResponse;
import com.smartq.entity.*;
import com.smartq.enums.TokenStatus;
import com.smartq.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;
    private final BusinessRepository businessRepository;
    private final CounterRepository counterRepository;
    private final UserRepository userRepository;
    private final QueueSessionRepository queueSessionRepository;
    private final NotificationService notificationService;

    // Get current logged in user
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // ── GENERATE TOKEN ──────────────────────────────────────
    @Transactional
    public TokenStatusResponse generateToken(TokenRequest request) {
        User customer = getCurrentUser();

        // Check if customer already has active token
        List<TokenStatus> activeStatuses = Arrays.asList(
                TokenStatus.WAITING, TokenStatus.CALLED,
                TokenStatus.SERVING);

        tokenRepository.findByCustomerIdAndStatusIn(
                        customer.getId(), activeStatuses)
                .ifPresent(t -> {
                    throw new RuntimeException(
                            "You already have an active token: #"
                                    + t.getTokenNumber());
                });

        // Get business and validate queue is open
        Business business = businessRepository
                .findById(request.getBusinessId())
                .orElseThrow(() ->
                        new RuntimeException("Business not found"));

        if (!business.getIsQueueOpen()) {
            throw new RuntimeException(
                    "Queue is currently closed");
        }

        // Get counter and validate it is active
        Counter counter = counterRepository
                .findById(request.getCounterId())
                .orElseThrow(() ->
                        new RuntimeException("Counter not found"));

        if (!counter.getIsActive()) {
            throw new RuntimeException(
                    "This counter is not active");
        }

        // Check max queue size
        int currentWaiting = tokenRepository
                .countByCounterIdAndStatus(
                        counter.getId(), TokenStatus.WAITING);

        if (currentWaiting >= business.getMaxQueueSize()) {
            throw new RuntimeException(
                    "Queue is full. Please try again later.");
        }

        // Generate next token number for today
        int nextTokenNumber = tokenRepository
                .findMaxTokenNumberForToday(counter.getId())
                .map(max -> max + 1)
                .orElse(1);

        // Calculate position in queue
        int position = currentWaiting + 1;

        // Calculate estimated wait time using SmartQ formula
        int estimatedWait = calculateWaitTime(
                position,
                business.getAvgServiceMins(),
                request.getPartySize()
        );

        // Create and save token
        Token token = Token.builder()
                .business(business)
                .counter(counter)
                .customer(customer)
                .tokenNumber(nextTokenNumber)
                .partySize(request.getPartySize())
                .specialRequest(request.getSpecialRequest())
                .status(TokenStatus.WAITING)
                .estimatedWaitMins(estimatedWait)
                .positionInQueue(position)
                .notificationSent(false)
                .build();

        tokenRepository.save(token);

        // Update customer total visits
        customer.setTotalVisits(customer.getTotalVisits() + 1);
        userRepository.save(customer);

        // Update max queue reached in session
        updateSessionMaxQueue(business.getId(), position);

        return buildStatusResponse(token, business, counter);
    }

    // ── GET LIVE TOKEN STATUS ────────────────────────────────
    public TokenStatusResponse getTokenStatus(Long tokenId) {

        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() ->
                        new RuntimeException("Token not found"));

        Business business = token.getBusiness();
        Counter counter = token.getCounter();

        // Recalculate live position
        if (token.getStatus() == TokenStatus.WAITING) {
            List<Token> waitingTokens = tokenRepository
                    .findByCounterIdAndStatusOrderByTokenNumber(
                            counter.getId(), TokenStatus.WAITING);

            int livePosition = 1;
            for (Token t : waitingTokens) {
                if (t.getId().equals(token.getId())) break;
                livePosition++;
            }

            // Update position if changed
            if (!token.getPositionInQueue()
                    .equals(livePosition)) {
                token.setPositionInQueue(livePosition);

                // Recalculate wait time
                int newWait = calculateWaitTime(
                        livePosition,
                        business.getAvgServiceMins(),
                        token.getPartySize()
                );
                token.setEstimatedWaitMins(newWait);
                tokenRepository.save(token);
            }

            // Send notification if customer is 3 tokens away
            if (token.getPositionInQueue() <= 3
                    && token.getPositionInQueue() > 0
                    && !token.getNotificationSent()) {

                notificationService.sendTurnComingEmail(
                        token.getCustomer().getEmail(),
                        token.getCustomer().getName(),
                        business.getName(),
                        token.getTokenNumber(),
                        counter.getCounterName()
                );

                token.setNotificationSent(true);
                tokenRepository.save(token);
            }
        }

        return buildStatusResponse(token, business, counter);
    }

    // ── CALL NEXT TOKEN (Owner) ──────────────────────────────
    @Transactional
    public TokenStatusResponse callNextToken(Long counterId) {
        User owner = getCurrentUser();

        Counter counter = counterRepository
                .findById(counterId)
                .orElseThrow(() ->
                        new RuntimeException("Counter not found"));

        // Verify ownership
        if (!counter.getBusiness().getOwner()
                .getId().equals(owner.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        // Mark current SERVING token as DONE
        List<Token> servingTokens = tokenRepository
                .findByCounterIdAndStatusOrderByTokenNumber(
                        counterId, TokenStatus.SERVING);

        servingTokens.forEach(t -> {
            t.setStatus(TokenStatus.DONE);
            t.setServedAt(LocalDateTime.now());
            tokenRepository.save(t);

            // Update counter stats
            counter.setTokensServedToday(
                    counter.getTokensServedToday() + 1);
        });

        // Also mark CALLED tokens as NO_SHOW if not arrived
        List<Token> calledTokens = tokenRepository
                .findByCounterIdAndStatusOrderByTokenNumber(
                        counterId, TokenStatus.CALLED);

        calledTokens.forEach(t -> {
            t.setStatus(TokenStatus.NO_SHOW);
            tokenRepository.save(t);
        });

        // Get next WAITING token
        List<Token> waitingTokens = tokenRepository
                .findByCounterIdAndStatusOrderByTokenNumber(
                        counterId, TokenStatus.WAITING);

        if (waitingTokens.isEmpty()) {
            // Update counter current token
            counterRepository.save(counter);
            throw new RuntimeException(
                    "No more customers waiting");
        }

        Token nextToken = waitingTokens.get(0);
        nextToken.setStatus(TokenStatus.CALLED);
        nextToken.setCalledAt(LocalDateTime.now());
        nextToken.setPositionInQueue(0);
        tokenRepository.save(nextToken);

        // Update counter current token number
        counter.setCurrentToken(nextToken.getTokenNumber());
        counterRepository.save(counter);

        // Update session stats
        updateSessionStats(counter.getBusiness().getId());

        return buildStatusResponse(
                nextToken,
                nextToken.getBusiness(),
                counter
        );
    }

    // ── CUSTOMER LEAVES QUEUE ────────────────────────────────
    @Transactional
    public TokenStatusResponse leaveQueue(Long tokenId) {
        User customer = getCurrentUser();

        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() ->
                        new RuntimeException("Token not found"));

        // Verify this token belongs to customer
        if (!token.getCustomer().getId()
                .equals(customer.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        if (token.getStatus() == TokenStatus.DONE ||
                token.getStatus() == TokenStatus.LEFT) {
            throw new RuntimeException(
                    "Token is already completed");
        }

        token.setStatus(TokenStatus.LEFT);
        tokenRepository.save(token);

        // Update session abandoned count
        queueSessionRepository.findByBusinessIdAndDate(
                        token.getBusiness().getId(),
                        java.time.LocalDate.now())
                .ifPresent(session -> {
                    session.setTotalAbandoned(
                            session.getTotalAbandoned() + 1);
                    queueSessionRepository.save(session);
                });

        return buildStatusResponse(
                token,
                token.getBusiness(),
                token.getCounter()
        );
    }

    // ── SMART WAIT TIME FORMULA ──────────────────────────────
    private int calculateWaitTime(int position,
                                  int avgServiceMins,
                                  int partySize) {
        // Party size factor
        double partySizeFactor;
        if (partySize <= 1) {
            partySizeFactor = 1.0;
        } else if (partySize <= 3) {
            partySizeFactor = 1.2;
        } else if (partySize <= 5) {
            partySizeFactor = 1.5;
        } else {
            partySizeFactor = 1.8;
        }

        // SmartQ formula:
        // position × avgServiceMins × partySizeFactor
        return (int) Math.ceil(
                position * avgServiceMins * partySizeFactor);
    }

    // ── UPDATE SESSION MAX QUEUE ─────────────────────────────
    private void updateSessionMaxQueue(
            Long businessId, int currentSize) {
        queueSessionRepository.findByBusinessIdAndDate(
                        businessId, java.time.LocalDate.now())
                .ifPresent(session -> {
                    if (currentSize >
                            session.getMaxQueueReached()) {
                        session.setMaxQueueReached(currentSize);
                        queueSessionRepository.save(session);
                    }
                });
    }

    // ── UPDATE SESSION STATS ─────────────────────────────────
    private void updateSessionStats(Long businessId) {
        queueSessionRepository.findByBusinessIdAndDate(
                        businessId, java.time.LocalDate.now())
                .ifPresent(session -> {
                    int totalServed = tokenRepository
                            .countTodaysTokensByStatus(
                                    businessId, TokenStatus.DONE);
                    session.setTotalServed(totalServed);
                    queueSessionRepository.save(session);
                });
    }

    // ── BUILD RESPONSE ───────────────────────────────────────
    private TokenStatusResponse buildStatusResponse(
            Token token, Business business, Counter counter) {

        int totalWaiting = tokenRepository
                .countByCounterIdAndStatus(
                        counter.getId(), TokenStatus.WAITING);

        return TokenStatusResponse.builder()
                .tokenId(token.getId())
                .tokenNumber(token.getTokenNumber())
                .status(token.getStatus())
                .positionInQueue(token.getPositionInQueue())
                .estimatedWaitMins(token.getEstimatedWaitMins())
                .businessName(business.getName())
                .counterName(counter.getCounterName())
                .calledMessage(business.getCalledMessage())
                .currentlyServing(counter.getCurrentToken())
                .totalWaiting(totalWaiting)
                .build();
    }
}