package com.smartq.service;

import com.smartq.dto.response.BusinessResponse;
import com.smartq.dto.response.PlatformStatsResponse;
import com.smartq.entity.Business;
import com.smartq.enums.BusinessType;
import com.smartq.enums.Role;
import com.smartq.enums.TokenStatus;
import com.smartq.repository.BusinessRepository;
import com.smartq.repository.TokenRepository;
import com.smartq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    public PlatformStatsResponse getPlatformStats() {
        return PlatformStatsResponse.builder()
                .totalBusinesses(businessRepository.count())
                .totalUsers(userRepository.count())
                .totalCustomers(userRepository
                        .countByRole(Role.CUSTOMER))
                .totalOwners(userRepository
                        .countByRole(Role.OWNER))
                .totalTokensIssued(tokenRepository.count())
                .totalTokensServedAllTime(tokenRepository
                        .countAllByStatus(TokenStatus.DONE))
                .restaurantCount(businessRepository
                        .countByBusinessType(BusinessType.RESTAURANT))
                .hospitalCount(businessRepository
                        .countByBusinessType(BusinessType.HOSPITAL))
                .bankCount(businessRepository
                        .countByBusinessType(BusinessType.BANK))
                .salonCount(businessRepository
                        .countByBusinessType(BusinessType.SALON))
                .governmentCount(businessRepository
                        .countByBusinessType(BusinessType.GOVERNMENT))
                .otherCount(businessRepository
                        .countByBusinessType(BusinessType.OTHER))
                .build();
    }

    public List<BusinessResponse> getAllBusinesses() {
        return businessRepository.findAll()
                .stream()
                .map(b -> BusinessResponse.builder()
                        .id(b.getId())
                        .name(b.getName())
                        .businessType(b.getBusinessType())
                        .address(b.getAddress())
                        .city(b.getCity())
                        .isQueueOpen(b.getIsQueueOpen())
                        .maxQueueSize(b.getMaxQueueSize())
                        .avgServiceMins(b.getAvgServiceMins())
                        .avgRating(b.getAvgRating())
                        .totalReviews(b.getTotalReviews())
                        .ownerName(b.getOwner().getName())
                        .build())
                .collect(Collectors.toList());
    }
}